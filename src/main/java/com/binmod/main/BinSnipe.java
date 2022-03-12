package com.binmod.main;

import java.io.File;

import org.lwjgl.input.Keyboard;

import com.binmod.async.ThreadManager;
import com.binmod.commands.BlackList;
import com.binmod.commands.ChangeMinProfit;
import com.binmod.commands.ChangeProfitScale;
import com.binmod.commands.Ping;
import com.binmod.commands.SwitchModeCommand;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

@Mod(modid = BinSnipe.MODID, version = BinSnipe.VERSION)
public class BinSnipe
{
    public static final String MODID = "binsnipe";
    public static final String VERSION = "4.0";
    
    public static boolean WHITELISTED = false;

    public static KeyBinding autoRun;
    public static KeyBinding toggleSniper;
    public static KeyBinding openAuctionHouse;
    public static KeyBinding openBazaar;
    
    public static boolean ACTIVE = false;
    public static final String API_HOST = "https://skyblockapi.azurewebsites.net";
    //public static final String API_HOST = "http://localhost:3000";
    
    public static Configuration config;
    
    private static ThreadManager threadManager;
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
    	File configFile = new File(Loader.instance().getConfigDir(), "binsniper.cfg");
        config = new Configuration(configFile);
        config.load();
        
        
        ThreadManager.MODE = config.get("binsniper", "mode", 0);
        ThreadManager.MINPROFIT = config.get("binsniper", "minprofit", 500000);
        ThreadManager.PROFITSCALE = config.get("binsniper", "profitscale", 250);
        String[] emptyArray = {};
        ThreadManager.BLACKLIST = config.get("binsniper", "blacklist", emptyArray);
        
    }
    
    
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
        
        ClientCommandHandler.instance.registerCommand(new SwitchModeCommand());
        ClientCommandHandler.instance.registerCommand(new ChangeMinProfit());
        ClientCommandHandler.instance.registerCommand(new ChangeProfitScale());
        ClientCommandHandler.instance.registerCommand(new Ping());
        ClientCommandHandler.instance.registerCommand(new BlackList());

        threadManager = new ThreadManager();
        threadManager.start();
        
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
    		if(!WHITELISTED) {
				WHITELISTED = Helpers.isWhiteListed();
			}
    		if(WHITELISTED) {
    			this.ACTIVE = !this.ACTIVE;
        		System.out.println("ACTIVE:" + this.ACTIVE);
        		if(this.ACTIVE) {
        			Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.GOLD+"BIN Sniper: "+EnumChatFormatting.GREEN+"Activated!"));
        		}
        		else {
        			Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.GOLD+"BIN Sniper: "+EnumChatFormatting.RED+"Deactivated!"));
        		}
    		}
    		else {
    			Helpers.sendTimestampedChat("You aren't whitelisted! Piss off");
    		}
    		
    	}
    	
    	if(openAuctionHouse.isPressed()) {
    		Minecraft.getMinecraft().thePlayer.sendChatMessage("/ah");
    	}
    	
    	if(openBazaar.isPressed()) {
    		Minecraft.getMinecraft().thePlayer.sendChatMessage("/bz");
    	}
    }

	
}
