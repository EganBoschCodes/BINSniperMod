package com.binmod.commands;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import com.binmod.async.ThreadManager;
import com.binmod.datatypes.TimeStamp;
import com.binmod.main.BinSnipe;
import com.binmod.main.Helpers;
import com.google.gson.Gson;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

public class Ping extends CommandBase {

    @Override
    public String getCommandName() {
        return "ping";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "Pings the API and checks for response times.";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		try {
	    	HttpGet getRequest = new HttpGet(BinSnipe.API_HOST+"/ping/time="+System.currentTimeMillis());
	        
	        HttpResponse response = httpClient.execute(getRequest);
	         
	        int statusCode = response.getStatusLine().getStatusCode();
	        if (statusCode != 200) 
	        {
	            throw new RuntimeException("Failed with HTTP error code : " + statusCode);
	        }
	         
	        HttpEntity httpEntity = response.getEntity();
	        String apiOutput = EntityUtils.toString(httpEntity);
	        
	        Gson GSON = new Gson();
	        TimeStamp resp =  GSON.fromJson(apiOutput, TimeStamp.class);
	    	
	        Helpers.sendTimestampedChat("Round-Trip Ping to API: "+(System.currentTimeMillis() - resp.timeSent)+"ms");
		} 
		catch (Exception e) { e.printStackTrace(); }
		finally {
			try { httpClient.close();} catch (IOException e) { e.printStackTrace(); }
		}
    }
    
    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }
}
