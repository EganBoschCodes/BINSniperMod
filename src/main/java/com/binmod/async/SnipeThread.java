package com.binmod.async;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

public class SnipeThread extends Thread {
	private int id;
	
	public int index;
	private int minIndex;
	private int maxIndex;
	private int maxIndexInitial;
	
	private ThreadManager manager;
	public String output;
	
	public SnipeThread(ThreadManager manage, int id, int minInd, int maxInd) {
		this.index = minInd;
		this.minIndex = minInd;
		this.maxIndex = maxInd;
		this.maxIndexInitial = maxInd;
		
		this.manager = manage;
		this.id = id;
	}
	
	public void run() {
		while(true) {
			await(manager.runThreads[this.id], true);
			//System.out.println("TIME TO GO: THREAD "+this.id);
			manager.runThreads[this.id].set(false);
			
			CloseableHttpClient httpClient = HttpClientBuilder.create().build();
			
			try {
				
				while(this.index < this.maxIndex) {
					//System.out.println("ABOUT TO TRY PAGE " + this.index);
					HttpGet getRequest = new HttpGet("https://api.hypixel.net/skyblock/auctions?page="+this.index);
		            
		            HttpResponse response = httpClient.execute(getRequest);
		             
		            int statusCode = response.getStatusLine().getStatusCode();
		            if (statusCode != 200) 
		            {
		                throw new RuntimeException("Failed with HTTP error code : " + statusCode);
		            }
		             
		            HttpEntity httpEntity = response.getEntity();
		            String apiOutput = EntityUtils.toString(httpEntity);

					//System.out.println("THREAD "+this.id+": AWAITING PARSING");
		            await(manager.dataParsed[this.id], true);
		            manager.dataParsed[this.id].set(false);
		            this.index++;

					//System.out.println("THREAD "+this.id+": SETTING DATA");
		            this.output = apiOutput;
		            manager.dataGathered[this.id].set(true);
		            
				}
				manager.threadDone[this.id].set(true);
			} catch (ParseException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} 
			finally {
				try {
					httpClient.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
        }
	}
	
	public void resetThread() {
		this.index = this.minIndex;
		this.maxIndex = this.maxIndexInitial;
	}
	
	public void setSoftMax(int max) {
		this.maxIndex = Math.min(this.maxIndex, max);
	}
	
	public void await(Flag f, boolean expected) {
		while(f.get() != expected) {try { this.sleep(5); } catch (InterruptedException e) { e.printStackTrace(); }}
	}
}
