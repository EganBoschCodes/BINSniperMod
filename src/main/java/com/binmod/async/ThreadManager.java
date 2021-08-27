package com.binmod.async;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import com.binmod.datatypes.AuctionHouse;
import com.binmod.datatypes.AuctionsResponse;
import com.binmod.datatypes.ItemAuctionData;
import com.binmod.main.BinSnipe;
import com.binmod.main.Helpers;
import com.google.gson.Gson;

import net.minecraft.client.Minecraft;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.ClickEvent.Action;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;

public class ThreadManager extends Thread implements Runnable {
	
    public static HashMap<String, Integer> usedKeys = new HashMap<String, Integer>();
    public static final Gson GSON = new Gson();
    
    public Flag[] dataGathered = {new Flag(), new Flag(), new Flag(), new Flag()};
    public Flag[] dataParsed = {new Flag(), new Flag(), new Flag(), new Flag()};
    public Flag[] threadDone = {new Flag(), new Flag(), new Flag(), new Flag()};
    public int[] failsafe = {-1, 14, 29, 44};

    public Flag[] runThreads = {new Flag(), new Flag(), new Flag(), new Flag()};
    
    private SnipeThread[] SnipeThreads = {new SnipeThread(this, 0, 0, 15), new SnipeThread(this, 1, 15, 30), new SnipeThread(this, 2, 30, 45), new SnipeThread(this, 3, 45, 1000) };
    
    AuctionHouse AuctionData;
    
    @Override
    public void start() {
    	super.start();
    	
    	for(int i = 0; i < 4; i++) {
    		System.out.println("THREAD "+i+" STARTED");
        	SnipeThreads[i].start();
        }
    	
    }

	@Override
	public void run() {
		try {
			while(true) {
				if(!BinSnipe.WHITELISTED) {
					BinSnipe.WHITELISTED = Helpers.isWhiteListed();
				}
    			if(BinSnipe.ACTIVE && BinSnipe.WHITELISTED) {
    	    		Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.GOLD+Helpers.getTimeStamp()+": "+EnumChatFormatting.WHITE+"Data Refreshed."));
					AuctionHouse AuctionData = getAuctionData();
					HashMap<String, ItemAuctionData> ProcessedData = AuctionData.itemRegistry;
					List<String> ItemList = new ArrayList<String>(ProcessedData.keySet());
					
					ItemAuctionData bestTrade = null;
					
					for(int i = 0; i < ItemList.size(); i++) {
						String tag = ItemList.get(i);
						ItemAuctionData data = ProcessedData.get(tag);
						if(data.prices.size() > 20 && (data.getProfit() > 500000 && data.getProfit() > 250 * Math.sqrt(data.minAuction.starting_bid)) && data.minAuction.starting_bid < 30000000 && data.pval() > 10 && data.pval() < 1000000) {
							
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
				        		//System.out.println(chat);
				        		Minecraft.getMinecraft().thePlayer.addChatMessage(comp);
							}
							
						}
					}
					
					if(!Objects.isNull(bestTrade)) {
						Minecraft.getMinecraft().thePlayer.sendChatMessage("/viewauction " + bestTrade.minAuction.uuid);
					}
            	}
    			else {
    				Thread.sleep(1000);
    			}
			}
		} 
		catch (Exception e) { 
    		System.out.println("UH OH");
			e.printStackTrace();
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
