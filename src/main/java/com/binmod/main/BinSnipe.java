package com.binmod.main;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.lwjgl.input.Keyboard;

import com.binmod.datatypes.AuctionHouse;
import com.binmod.datatypes.AuctionsResponse;
import com.binmod.datatypes.ItemAuctionData;
import com.google.gson.Gson;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

@Mod(modid = BinSnipe.MODID, version = BinSnipe.VERSION)
public class BinSnipe extends Thread
{
    public static final String MODID = "binsnipe";
    public static final String VERSION = "1.0";

    public static KeyBinding autoRun;
    public static KeyBinding toggleSniper;
    public static KeyBinding openAuctionHouse;
    public static KeyBinding openBazaar;
    
    public static boolean ACTIVE = false;
    
    public static HashMap<String, Integer> usedKeys = new HashMap<String, Integer>();
    
    
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        MinecraftForge.EVENT_BUS.register(this);
        
        autoRun = new KeyBinding("Auto-Run", Keyboard.KEY_EQUALS, "Bin Sniper");
        ClientRegistry.registerKeyBinding(autoRun);
        
        toggleSniper = new KeyBinding("Toggle Bin Sniper", Keyboard.KEY_9, "Bin Sniper");
        ClientRegistry.registerKeyBinding(toggleSniper);
        
        openAuctionHouse = new KeyBinding("Open Auction House", Keyboard.KEY_8, "Bin Sniper");
        ClientRegistry.registerKeyBinding(openAuctionHouse);
        
        openBazaar = new KeyBinding("Open Bazaar", Keyboard.KEY_7, "Bin Sniper");
        ClientRegistry.registerKeyBinding(openBazaar);
        
		this.start();
        
    }
    
    public static AuctionHouse getAuctionData() throws Exception 
    {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		//LOOK INTO MULTITHREADING THIS
        AuctionHouse AuctionData = new AuctionHouse();
        try
        {
            int page = 0;
            int finalPage = 1;
        	
            while(page < finalPage) {
            	HttpGet getRequest = new HttpGet("https://api.hypixel.net/skyblock/auctions?page="+page);
                //System.out.println("Getting Page: "+page);
                
                HttpResponse response = httpClient.execute(getRequest);
                 
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode != 200) 
                {
                    throw new RuntimeException("Failed with HTTP error code : " + statusCode);
                }
                 
                HttpEntity httpEntity = response.getEntity();
                String apiOutput = EntityUtils.toString(httpEntity);
                 
                Gson g = new Gson();
                AuctionsResponse AuctionPage =  g.fromJson(apiOutput, AuctionsResponse.class);
                AuctionData.assimilatePage(AuctionPage);
                
                finalPage = AuctionPage.totalPages;
                page++;
            }
            System.out.println("Data Refreshed");
            return AuctionData;
            
        }
        finally
        {
            httpClient.close();
        }
    }
    
    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
    	if(autoRun.isPressed()) {
    		KeyBinding forward = FMLClientHandler.instance().getClient().gameSettings.keyBindForward;
    		KeyBinding sprint = FMLClientHandler.instance().getClient().gameSettings.keyBindSprint;
            if (forward.isKeyDown()) { //Player is going forwards, make them stop
                KeyBinding.setKeyBindState(forward.getKeyCode(), false);
                KeyBinding.setKeyBindState(sprint.getKeyCode(), false);
            } else { //Player is not going forwards, make them start
                KeyBinding.setKeyBindState(forward.getKeyCode(), true);
                KeyBinding.setKeyBindState(sprint.getKeyCode(), true);
            }
    	}
    	
    	if(toggleSniper.isPressed()) {
    		KeyBinding jump = FMLClientHandler.instance().getClient().gameSettings.keyBindJump;
    		KeyBinding crouch = FMLClientHandler.instance().getClient().gameSettings.keyBindSneak;
    		this.ACTIVE = !this.ACTIVE;
    		System.out.println("ACTIVE:" + this.ACTIVE);
    		if(this.ACTIVE) {
                KeyBinding.setKeyBindState(jump.getKeyCode(), true);
    		}
    		else {
    			KeyBinding.setKeyBindState(crouch.getKeyCode(), true);
    		}
    	}
    	
    	if(openAuctionHouse.isPressed()) {
    		Minecraft.getMinecraft().thePlayer.sendChatMessage("/ah");
    	}
    	
    	if(openBazaar.isPressed()) {
    		Minecraft.getMinecraft().thePlayer.sendChatMessage("/bz");
    	}
    }
    
    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        /*String message = event.message.getUnformattedText();
        
        if(message.contains("ah flip")) {
        	
        	IChatComponent comp = new ChatComponentText("Text");
        	final int counter = 0;
        	ChatStyle style = new ChatStyle().setChatClickEvent(new ClickEvent(Action.RUN_COMMAND, "") {
	        	@Override
	        	public Action getAction() {
	        		Minecraft.getMinecraft().thePlayer.sendChatMessage("/kill");
	        		return Action.RUN_COMMAND;
	        	}
        	});
        	comp.setChatStyle(style);
        	Minecraft.getMinecraft().thePlayer.addChatComponentMessage(comp);
        }*/
    }

	@Override
	public void run() {
		try {
			while(true) {
				System.out.println("LIVE LOOP");
    			if(ACTIVE && Helpers.isWhiteListed()) {
    				System.out.println("ACTIVE LOOP");
					AuctionHouse AuctionData = getAuctionData();
					HashMap<String, ItemAuctionData> ProcessedData = AuctionData.itemRegistry;
					List<String> ItemList = new ArrayList<String>(ProcessedData.keySet());
					
					for(int i = 0; i < ItemList.size(); i++) {
						String tag = ItemList.get(i);
						ItemAuctionData data = ProcessedData.get(tag);
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
		} catch (Exception e) { 
    		System.out.println("UH OH");
			e.printStackTrace();
		}
	}
}
