package com.binmod.main;

import java.awt.AWTException;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import com.binmod.datatypes.Auction;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

public class Helpers {
	
	public static String pruneName(Auction input) {
		
		String name = input.item_name;
		
		if(name.contains("Enchanted Book")) {
			int firstComma = input.item_lore.indexOf(',') > 0 ? input.item_lore.indexOf(',') : Integer.MAX_VALUE;
			int firstNewline = input.item_lore.indexOf('\n') > 0 ? input.item_lore.indexOf('\n') : Integer.MAX_VALUE;
			
			int end = Math.min(firstComma, firstNewline);
			name = input.item_lore.substring(2, end);
		}
		
		name = name.replaceAll("[^\\x00-\\x7F]", "").trim();
		String[] reforgesArr = {"NECROTIC", "ANCIENT", "FABLED", "GIANT", "GENTLE", "ODD", "FAST", "FAIR", "EPIC", "SHARP", "HEROIC", "SPICY", "LEGENDARY", "DIRTY", "GILDED", "WARPED", "BULKY", "SALTY", "TREACHEROUS", "STIFF", "LUCKY", "DEADLY", "FINE", "GRAND", "HASTY", "NEAT", "RAPID", "UNREAL", "AWKWARD", "RICH", "PRECISE", "HEADSTRONG", "CLEAN", "FIERCE", "HEAVY", "LIGHT", "MYTHIC", "PURE", "SMART", "TITANIC", "WISE", "PERFECT", "SPIKED", "RENOWNED", "CUBIC", "WARPED", "REINFORCED", "LOVING", "RIDICULOUS", "SUBMERGED", "JADED", "BIZARRE", "ITCHY", "OMINOUS", "PLEASANT", "PRETTY", "SHINY", "SIMPLE", "STRANGE", "VIVID", "GODLY", "DEMONIC", "FORCEFUL", "HURTFUL", "KEEN", "STRONG", "SUPERIOR", "UNPLEASANT", "ZEALOUS", "SILKY", "BLOODY", "SHADED", "SWEET", "FRUITFUL", "MAGNETIC", "REFINED", "BLESSED", "FLEET", "STELLAR", "MITHRAIC", "AUSPICIOUS", "HEATED", "AMBERED"};
		List<String> reforges = Arrays.asList(reforgesArr);
		
		String tag = name.toUpperCase();
		
		List<String> tagSplit = Arrays.asList(tag.split(" "));
		
		if (reforges.indexOf(tagSplit.get(0)) >= 0) {
            tagSplit = tagSplit.subList(1, tagSplit.size());
        }
		
		return String.join(" ", tagSplit).toLowerCase();
	}
	
	public static void moveMouse(Point p) {
	    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
	    GraphicsDevice[] gs = ge.getScreenDevices();

	    // Search the devices for the one that draws the specified point.
	    for (GraphicsDevice device: gs) { 
	        GraphicsConfiguration[] configurations = device.getConfigurations();
	        for (GraphicsConfiguration config: configurations) {
	            Rectangle bounds = config.getBounds();
	            if(bounds.contains(p)) {
	                // Set point to screen coordinates.
	                Point b = bounds.getLocation();
	                Point s = new Point(p.x - b.x, p.y - b.y);

	                try {
	                    Robot r = new Robot(device);
	                    r.mouseMove(s.x, s.y);
	                } catch (AWTException e) {
	                    e.printStackTrace();
	                }

	                return;
	            }
	        }
	    }
	    // Couldn't move to the point, it may be off screen.
	    return;
	}
	
	public static Rectangle getBounds() {
	    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
	    GraphicsDevice[] gs = ge.getScreenDevices();

	    // Search the devices for the one that draws the specified point.
	    for (GraphicsDevice device: gs) { 
	        GraphicsConfiguration[] configurations = device.getConfigurations();
	        for (GraphicsConfiguration config: configurations) {
	            Rectangle bounds = config.getBounds();
	            return bounds;
	        }
	    }
	    // Couldn't move to the point, it may be off screen.
	    return null;
	}
	
	public static void click(){
	    Robot bot;
		try {
			bot = new Robot();
		    bot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
		    bot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
		} catch (AWTException e) {
			e.printStackTrace();
		}   
	}
	
	public static boolean isWhiteListed() {
		String[] whitelistArr = {"BoschMods", "Player"};
		List<String> whitelist = Arrays.asList(whitelistArr);
		
		if( Minecraft.getMinecraft() == null) {
			return false;
		}
		
		if( Minecraft.getMinecraft().thePlayer == null) {
			return false;
		}
		
		String s = ((EntityPlayer) Minecraft.getMinecraft().thePlayer).getName();
		System.out.println("PLAYER NAME: "+s);
		boolean wl = false;
		for(String name : whitelist) {
			wl = wl || s.contains(name);
		}
		
		return wl;
	}
	
	public static String niceName(String pruned) {
		String[] s = pruned.split(" ");
		String output = "";
		for(int i = 0; i < s.length;i++) {
			s[i] = (s[i].charAt(0) + "").toUpperCase() + s[i].substring(1);
		}
		for(int i = 0; i < s.length - 1;i++) {
			output += s[i] + " ";
		}
		return output + "(" + s[s.length - 1] + ")";
	}
	
	public static String localeString(int n) {
		String s = "" + n;
		int counter = 0;
		String output = "";
		while(s.length() - counter > 0) {
			output = s.charAt(s.length() - 1 - counter) + output;
			if(counter % 3 == 2 && s.length() - counter > 1) {
				output = "," + output;
			}
			counter++;
		}
		return output;
	}
	
	public static String getTimeStamp() {

		Calendar calendar = Calendar.getInstance();
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);
		int second = calendar.get(Calendar.SECOND);
		return "[" + (hour > 9 ? hour : ("0" + hour)) + ":" + (minute > 9 ? minute : ("0" + minute)) + ":" + (second > 9 ? second : ("0" + second)) +"]";
		
	}
}
