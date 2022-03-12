package com.binmod.main;

import java.awt.AWTException;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import com.binmod.datatypes.Auction;
import com.binmod.datatypes.WhiteListReturn;
import com.binmod.sounds.NotifSound;
import com.google.gson.Gson;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class Helpers {
	
	private static boolean WHITELISTED = false;
	public static boolean DEVBUILD = false;
	
	public static String pruneName(Auction input) {
		
		String name = input.item_name;
		
		if(name.contains("Enchanted Book")) {
			int firstComma = input.item_lore.indexOf(',') > 0 ? input.item_lore.indexOf(',') : Integer.MAX_VALUE;
			int firstNewline = input.item_lore.indexOf('\n') > 0 ? input.item_lore.indexOf('\n') : Integer.MAX_VALUE;
			
			int end = Math.min(firstComma, firstNewline);
			int start = 2;
			while(!Character.isLetterOrDigit(input.item_lore.charAt(start))) {
				start += 2;
			}
			name = input.item_lore.substring(start, end);
		}
		
		name = name.replaceAll("[^\\x00-\\x7F]", "").trim();
		String[] reforgesArr = {"NECROTIC", "ANCIENT", "FABLED", "GIANT", "GENTLE", "ODD", "FAST", "FAIR", "EPIC", "SHARP", "HEROIC", "SPICY", "LEGENDARY", "DIRTY", "GILDED", "WARPED", "BULKY", "SALTY", "TREACHEROUS", "STIFF", "LUCKY", "DEADLY", "FINE", "GRAND", "HASTY", "NEAT", "RAPID", "UNREAL", "AWKWARD", "RICH", "PRECISE", "HEADSTRONG", "CLEAN", "FIERCE", "HEAVY", "LIGHT", "MYTHIC", "PURE", "SMART", "TITANIC", "WISE", "PERFECT", "SPIKED", "RENOWNED", "CUBIC", "WARPED", "REINFORCED", "LOVING", "RIDICULOUS", "BIZARRE", "ITCHY", "OMINOUS", "PLEASANT", "PRETTY", "SHINY", "SIMPLE", "STRANGE", "VIVID", "GODLY", "DEMONIC", "FORCEFUL", "HURTFUL", "KEEN", "STRONG", "SUPERIOR", "UNPLEASANT", "ZEALOUS", "SILKY", "BLOODY", "SHADED", "SWEET", "FRUITFUL", "MAGNETIC", "REFINED", "BLESSED", "FLEET", "STELLAR", "MITHRAIC", "AUSPICIOUS", "HEATED", "AMBERED"};
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
	
	public static String getName() {
		if( Objects.isNull(Minecraft.getMinecraft())) {
			return "";
		}
		
		if( Objects.isNull(Minecraft.getMinecraft().thePlayer) ) {
			return "";
		}
		
		try {
			String s = ((EntityPlayer) Minecraft.getMinecraft().thePlayer).getName();
			return s;
		}
		catch(Exception e) {
			e.printStackTrace();
			return "";
		}
	}
	
	public static String getNameHash() {
		if(DEVBUILD) {
			return "16765c3b8b6a579759c7006d0825b06f5879a34bd1dccf1935fa3fc83ecbe44e";
		}
		
		return sha256(getName()+"_verify");
	}
	
	public static boolean isWhiteListed() {
		
		if(WHITELISTED) {
			return true;
		}
		
		if( Objects.isNull(Minecraft.getMinecraft())) {
			//sendTimestampedChat("Minecraft is breaking...");
			return false;
		}
		
		if( Objects.isNull(Minecraft.getMinecraft().thePlayer) ) {
			//sendTimestampedChat("Player is breaking...");
			return false;
		}
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		//sendTimestampedChat("Opening web client");
		
		try {
			String s = ((EntityPlayer) Minecraft.getMinecraft().thePlayer).getName();
			
			if(s.length() >= 6 && s.substring(0, 6).contains("Player")) {
				WHITELISTED = true;
				DEVBUILD = true;
				return true;
			}
			
			//MAKE S GET HASHED
			sendTimestampedChat("Pinging API...");
			HttpGet getRequest = new HttpGet(BinSnipe.API_HOST+"/whitelist/username="+Helpers.sha256(s+"_verify"));
            
            HttpResponse response = httpClient.execute(getRequest);
    		sendTimestampedChat("Response retrieved.");
             
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) 
            {
        		//sendTimestampedChat("Ruh roh");
                throw new RuntimeException("Failed with HTTP error code : " + statusCode);
            }
             
            HttpEntity httpEntity = response.getEntity();
            String apiOutput = EntityUtils.toString(httpEntity);
    		//sendTimestampedChat("API RESPONSE: "+apiOutput);
            
            Gson GSON = new Gson();
			WhiteListReturn resp =  GSON.fromJson(apiOutput, WhiteListReturn.class);
			
			httpClient.close();
			if(resp.status == 200 && resp.whitelisted) {
				//sendTimestampedChat("You are whitelisted!");
				WHITELISTED = true;
			}
			else {
				//sendTimestampedChat(resp.status+ (resp.status == 200 ? ": "+resp.whitelisted : ""));
			}
			return WHITELISTED;
		}
		catch(Exception e) {
    		sendTimestampedChat(e.toString());
			try {
				httpClient.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			return false;
		}
		
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
	
	public static String sha256(String str) {
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(str.getBytes(StandardCharsets.UTF_8));
			StringBuilder hexString = new StringBuilder(2 * hash.length);
		    for (int i = 0; i < hash.length; i++) {
		        String hex = Integer.toHexString(0xff & hash[i]);
		        if(hex.length() == 1) {
		            hexString.append('0');
		        }
		        hexString.append(hex);
		    }
		    return hexString.toString();
		} catch (NoSuchAlgorithmException e) {
			return "ERROR! NO SUCH ALGORITHM";
		}
	}
	
	public static double cleanRound(double d, int i) {
		return ((double)Math.round(d * Math.pow(10, i))) / Math.pow(10, i);
	}
	
	public static void sendTimestampedChat(String s) {
		Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.GOLD + getTimeStamp()+": "+EnumChatFormatting.WHITE+s));
	}
	
	public static void playNotification() {
		Minecraft.getMinecraft().getSoundHandler().playSound(new NotifSound());
	}
	

	public static void sendError(String s) {
		Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.GOLD + getTimeStamp()+": "+EnumChatFormatting.RED+s));
	}
}


