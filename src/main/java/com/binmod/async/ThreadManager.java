package com.binmod.async;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.binmod.datatypes.AuctionHouse;
import com.binmod.datatypes.AuctionsResponse;
import com.binmod.datatypes.ItemAuctionData;
import com.binmod.main.BinSnipe;
import com.binmod.main.Helpers;
import com.google.gson.Gson;

import net.minecraft.client.Minecraft;

public class ThreadManager extends Thread implements Runnable {
	
    public static HashMap<String, Integer> usedKeys = new HashMap<String, Integer>();
    public static final Gson GSON = new Gson();
    
    public Flag[] dataGathered = {new Flag(), new Flag(), new Flag(), new Flag()};
    public Flag[] dataParsed = {new Flag(), new Flag(), new Flag(), new Flag()};
    public Flag[] threadDone = {new Flag(), new Flag(), new Flag(), new Flag()};

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
    			if(BinSnipe.ACTIVE && Helpers.isWhiteListed()) {
					AuctionHouse AuctionData = getAuctionData();
					HashMap<String, ItemAuctionData> ProcessedData = AuctionData.itemRegistry;
					List<String> ItemList = new ArrayList<String>(ProcessedData.keySet());
					
					for(int i = 0; i < ItemList.size(); i++) {
						String tag = ItemList.get(i);
						ItemAuctionData data = ProcessedData.get(tag);
						//System.out.println(tag+": "+data.prices.size()+" auctions found");
						if(data.prices.size() > 20 && (data.getProfit() > 1000000 || (data.getProfit() / data.minAuction.starting_bid > 5 && data.getProfit() > 100000)) && data.minAuction.starting_bid < 50000000 && data.pval() > 10&& data.pval() < 1000000) {
							if(!usedKeys.containsKey(data.minAuction.uuid)) {
								usedKeys.put(data.minAuction.uuid, 0);
				        		Minecraft.getMinecraft().thePlayer.sendChatMessage("/viewauction " + data.minAuction.uuid);
				        		Thread.sleep(300);
		    		    		Rectangle b = Helpers.getBounds();
		    					Helpers.moveMouse(new Point(b.width/2, b.height/2 - 30));
								System.out.println(tag);
								System.out.println("Min Price: "+data.minAuction.starting_bid+"");
								System.out.println("Avg Price: "+data.averagePrice());
								System.out.println("Std-Dev: "+data.standardDeviation());
								System.out.println("P-Value: "+data.pval());
								System.out.println("Profit: "+data.getProfit());
								System.out.println(data.minAuction.uuid +"\n");
							}
							
						}
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
        	runThreads[i].set(true);
        	dataParsed[i].set(true);
        	threadDone[i].set(false);
        }
        
        
        System.out.println("STARTING WHILE LOOP");
    	while(!threadDone[0].get() || !threadDone[1].get() || !threadDone[2].get() || !threadDone[3].get()) {
    		//System.out.println(!threadDone[0].get() +" "+ !threadDone[1].get() +" "+ !threadDone[2].get() +" "+ !threadDone[3].get());
    		for(int i = 0; i < 4; i++) {
    			if(dataGathered[i].get()) {
    				dataGathered[i].set(false);
    				AuctionsResponse AuctionPage =  GSON.fromJson(SnipeThreads[i].output, AuctionsResponse.class);
    				//System.out.println("Page Read:" + SnipeThreads[i].index);
		            AuctionData.assimilatePage(AuctionPage);
		            SnipeThreads[i].setSoftMax(AuctionPage.totalPages);
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
