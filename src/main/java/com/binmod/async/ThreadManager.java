package com.binmod.async;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import com.binmod.datatypes.AuctionHouse;
import com.binmod.datatypes.AuctionsResponse;
import com.binmod.datatypes.ItemAuctionData;
import com.binmod.datatypes.Trade;
import com.binmod.datatypes.TradeListReturn;
import com.binmod.main.BinSnipe;
import com.binmod.main.Helpers;
import com.google.gson.Gson;

import net.minecraft.client.Minecraft;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.ClickEvent.Action;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.config.Property;

public class ThreadManager extends Thread implements Runnable {
	
	public static long lastUpdated = 0;
	
    public static HashMap<String, Integer> usedKeys = new HashMap<String, Integer>();
    public static final Gson GSON = new Gson();
    
    public Flag[] dataGathered = {new Flag(), new Flag(), new Flag(), new Flag()};
    public Flag[] dataParsed = {new Flag(), new Flag(), new Flag(), new Flag()};
    public Flag[] threadDone = {new Flag(), new Flag(), new Flag(), new Flag()};
    public int[] failsafe = {-1, 14, 29, 44};

    public Flag[] runThreads = {new Flag(), new Flag(), new Flag(), new Flag()};
    
    private SnipeThread[] SnipeThreads = {new SnipeThread(this, 0, 0, 17), new SnipeThread(this, 1, 17, 34), new SnipeThread(this, 2, 34, 51), new SnipeThread(this, 3, 51, 1000) };
    
    AuctionHouse AuctionData;
    CloseableHttpClient httpClient;
    
    public static Property MODE;
    public static Property MINPROFIT;
    public static Property PROFITSCALE;
    
    boolean APIFirstAwait = true;
    
    @Override
    public void start() {
    	super.start();
    	
    	for(int i = 0; i < 4; i++) {
    		System.out.println("THREAD "+i+" STARTED");
        	SnipeThreads[i].start();
        }
    	
    }
    
    public void runAPI() throws ClientProtocolException, IOException, InterruptedException {

    	HttpGet getRequest = new HttpGet(BinSnipe.API_HOST + "/gettrades/minprofit=" + this.MINPROFIT.getInt() + "&profitscale=" + this.PROFITSCALE.getInt() + "&username=" + Helpers.getNameHash());
    	
    	
        HttpResponse response = this.httpClient.execute(getRequest);
         
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode != 200) 
        {
            throw new RuntimeException("Failed with HTTP error code : " + statusCode);
        }
         
        HttpEntity httpEntity = response.getEntity();
        String apiOutput = EntityUtils.toString(httpEntity);
		
        TradeListReturn tradeList = GSON.fromJson(apiOutput, TradeListReturn.class);
        
        if(tradeList.status == 200) {
        	if(tradeList.timestamp > this.lastUpdated) {
        		Trade bestTrade = null;
            	for(Trade data : tradeList.trades) {
            		if(!usedKeys.containsKey(data.uuid)) {
						usedKeys.put(data.uuid, 0);
						String chat = "\n"+EnumChatFormatting.DARK_RED+"==============\n" + EnumChatFormatting.RED + EnumChatFormatting.UNDERLINE + EnumChatFormatting.BOLD + data.name  +"\n"+EnumChatFormatting.DARK_RED+"==============\n"+ EnumChatFormatting.RESET;
		        		chat += EnumChatFormatting.DARK_GREEN + "Price: "+EnumChatFormatting.GOLD + "$" + Helpers.localeString(data.price) + "\n";
		        		chat += EnumChatFormatting.DARK_GREEN + "Average BIN: "+EnumChatFormatting.GOLD + "$" + Helpers.localeString((int)data.avg) + "\n";
		        		chat += EnumChatFormatting.DARK_GREEN + "Profit: "+EnumChatFormatting.GOLD + "$" + Helpers.localeString((int)data.profit);
		        		ChatStyle style = new ChatStyle().setChatClickEvent(new ClickEvent(Action.RUN_COMMAND, "/viewauction " + data.uuid));
		        		ChatComponentText comp = new ChatComponentText(chat);
		        		comp.setChatStyle(style);
		        		if(Objects.isNull(bestTrade)) {
		        			bestTrade = data;
		        		}
		        		else if(bestTrade.profit < data.profit) {
		        			bestTrade = data;
		        		}
		        		//System.out.println(chat);
		        		Minecraft.getMinecraft().thePlayer.addChatMessage(comp);
					}
            	}
            	if(!Objects.isNull(bestTrade)) {
					Minecraft.getMinecraft().thePlayer.sendChatMessage("/viewauction " + bestTrade.uuid);
			        Helpers.playNotification();
				}

            	this.lastUpdated = tradeList.timestamp;
            	
            	long sleepTime = Math.max(0, (70000 - (System.currentTimeMillis() - tradeList.timestamp)));
            	Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.GOLD+Helpers.getTimeStamp()+": "+EnumChatFormatting.WHITE+"API refreshed "+ Helpers.cleanRound(((double)(System.currentTimeMillis() - tradeList.timestamp))/1000, 1) + " sec ago."));
            	APIFirstAwait = true;
            	Thread.sleep(sleepTime);
        	}
        	else {
        		if(APIFirstAwait) {
        			Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.GOLD+Helpers.getTimeStamp()+": "+EnumChatFormatting.WHITE+"Awaiting new data..."));
        			APIFirstAwait = false;
        		}
        		
        		Thread.sleep(500);
        	}
        }
        else {
        	System.out.println(tradeList.error +" (DEVBUILD: "+Helpers.DEVBUILD+")");
        	Thread.sleep(5000);
        }
    }

    public void runLocal() throws Exception {
    	Helpers.sendTimestampedChat("Beginning data watch...");
		AuctionHouse AuctionData = getAuctionData();
		HashMap<String, ItemAuctionData> ProcessedData = AuctionData.itemRegistry;
		List<String> ItemList = new ArrayList<String>(ProcessedData.keySet());
		
		ItemAuctionData bestTrade = null;
		
		for(int i = 0; i < ItemList.size(); i++) {
			String tag = ItemList.get(i);
			ItemAuctionData data = ProcessedData.get(tag);
			if(data.prices.size() > 20 && (data.getProfit() > this.MINPROFIT.getInt() && data.getProfit() > this.PROFITSCALE.getInt() * Math.sqrt(data.minAuction.starting_bid)) && data.minAuction.starting_bid < 30000000 && data.pval() > 10 && data.pval() < 1000000) {
				
				if(!usedKeys.containsKey(data.minAuction.uuid)) {
					usedKeys.put(data.minAuction.uuid, 0);
					String chat = "\n"+EnumChatFormatting.DARK_RED+"==============\n" + EnumChatFormatting.RED + EnumChatFormatting.UNDERLINE + EnumChatFormatting.BOLD + Helpers.niceName(tag)  +"\n"+EnumChatFormatting.DARK_RED+"==============\n"+ EnumChatFormatting.RESET;
	        		chat += EnumChatFormatting.DARK_GREEN + "Price: "+EnumChatFormatting.GOLD + "$" + Helpers.localeString(data.minAuction.starting_bid) + "\n";
	        		chat += EnumChatFormatting.DARK_GREEN + "Average BIN: "+EnumChatFormatting.GOLD + "$" + Helpers.localeString((int)data.averagePrice()) + "\n";
	        		chat += EnumChatFormatting.DARK_GREEN + "Profit: "+EnumChatFormatting.GOLD + "$" + Helpers.localeString((int)data.getProfit());
	        		ChatStyle style = new ChatStyle().setChatClickEvent(new ClickEvent(Action.RUN_COMMAND, "/viewauction " + data.minAuction.uuid));
	        		ChatComponentText comp = new ChatComponentText(chat);
	        		comp.setChatStyle(style);
	        		
	        		if(Objects.isNull(bestTrade)) {
	        			bestTrade = data;
	        		}
	        		else if(bestTrade.getProfit() < data.getProfit()) {
	        			bestTrade = data;
	        		}
	        		
	        		Minecraft.getMinecraft().thePlayer.addChatMessage(comp);
				}
				
			}
		}
		
		if(!Objects.isNull(bestTrade)) {
			Minecraft.getMinecraft().thePlayer.sendChatMessage("/viewauction " + bestTrade.minAuction.uuid);
	        Helpers.playNotification();
		}
		Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.GOLD+Helpers.getTimeStamp()+": "+EnumChatFormatting.WHITE+"API refreshed "+ Helpers.cleanRound(((double)(System.currentTimeMillis() - AuctionData.lastUpdated))/1000, 1) + " sec ago."));
		
		Thread.sleep(Math.max(60000 - System.currentTimeMillis() + AuctionData.lastUpdated, 5000));
    }
    
	@Override
	public void run() {
		this.httpClient = HttpClientBuilder.create().build();
		try {
			while(true) {
				if(!BinSnipe.WHITELISTED) {
					BinSnipe.WHITELISTED = Helpers.isWhiteListed();
					//System.out.println("WHITELISTED: "+BinSnipe.WHITELISTED);
				}
    			if(BinSnipe.ACTIVE && (BinSnipe.WHITELISTED || Helpers.getName().contains("Player"))) {
    				
    				switch(this.MODE.getInt()) {
    				case 0:
    					runAPI();
    					break;
    				case 1:
    					runLocal();
    					break;
    				}
    				
            	}
    			else {
    				Thread.sleep(1000);
    			}
			}
		} 
		catch (Exception e) { 
    		System.out.println("UH OH");
    		Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("ERROR: "+e.toString()));
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
	
	public AuctionHouse getAuctionData() throws Exception 
    {
		
        AuctionHouse AuctionData = new AuctionHouse();
        
        for(int i = 0; i < 4; i++) {
        	dataParsed[i].set(true);
        	threadDone[i].set(false);
			dataGathered[i].set(false);
			failsafe[i] = i * 15 - 1;
        }
        
        for(int i = 0; i < 4; i++) {
        	runThreads[i].set(true);
        }
        
        System.out.println("STARTING WHILE LOOP");
    	while(!threadDone[0].get() || !threadDone[1].get() || !threadDone[2].get() || !threadDone[3].get()) {
    		//System.out.println(!threadDone[0].get() +" "+ !threadDone[1].get() +" "+ !threadDone[2].get() +" "+ !threadDone[3].get());
    		//System.out.println(dataGathered[0].get() +" "+ dataGathered[1].get() +" "+ dataGathered[2].get() +" "+ dataGathered[3].get());
    		for(int i = 0; i < 4; i++) {
    			if(dataGathered[i].get()) {
    				dataGathered[i].set(false);
    				//System.out.println("STARTING PARSING FOR THREAD "+i);
    				AuctionsResponse AuctionPage =  GSON.fromJson(SnipeThreads[i].output, AuctionsResponse.class);
    				//System.out.println("Page Read:" + AuctionPage.page +" " + (SnipeThreads[i].index-1));
		            AuctionData.assimilatePage(AuctionPage);
		            SnipeThreads[i].setSoftMax(AuctionPage.totalPages);
		            failsafe[i] = (SnipeThreads[i].index-1);
    				//System.out.println("FINISHED PARSING FOR THREAD "+i);
		            dataParsed[i].set(true);
    			}
    			if(failsafe[i] == SnipeThreads[i].index - 1) {
		            dataParsed[i].set(true);
    			}
    		}
    		
    	}
    	
        System.out.println("Data Refreshed");
        for(int i = 0; i < 4; i++) {
        	SnipeThreads[i].resetThread();
        }
        
        return AuctionData;	
    }

}
