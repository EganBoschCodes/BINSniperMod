package com.binmod.commands;

import com.binmod.async.ThreadManager;
import com.binmod.main.BinSnipe;
import com.binmod.main.Helpers;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

public class BlackList extends CommandBase {

    @Override
    public String getCommandName() {
        return "blacklist";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "Prevents certain items from being flagged as good trades.";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
    	if(args.length == 0) {
			String[] blacklist = ThreadManager.BLACKLIST.getStringList();
			if(blacklist.length == 0) {
				Helpers.sendTimestampedChat("No items currently blacklisted!");
			}
			else {
				String message = "Current Blacklist: ";
				for(int i = 0; i < blacklist.length; i++) {
					message += blacklist[i] + (i+1 < blacklist.length ? ", " : ".");
				}
				Helpers.sendTimestampedChat(message);
			}
    	}
    	else {
    		String blacklistedTerm = "";
			for(int i = 0; i < args.length; i++) {
				blacklistedTerm += args[i] + (i+1 < args.length ? " " : "");
			}
			
			String[] blacklist = ThreadManager.BLACKLIST.getStringList();
			boolean alreadyIn = false;
			for(int i = 0; i < blacklist.length; i++) {
				alreadyIn = alreadyIn || (blacklist[i].contains(blacklistedTerm));
			}
			String[] newBlacklist;
			if(alreadyIn) {
				newBlacklist = new String[blacklist.length - 1];
				String containedTerm = "";
				int index = 0;
				for(int i = 0; i < blacklist.length; i++) {
					if(!blacklist[i].contains(blacklistedTerm)) {
						newBlacklist[index] = blacklist[i];
						index++;
					}
					else {
						containedTerm = blacklist[i];
					}
				}
				ThreadManager.BLACKLIST.set(newBlacklist);
				Helpers.sendTimestampedChat("Removed \"" + containedTerm +"\" from blacklist!");
			}
			else {
				newBlacklist = new String[blacklist.length + 1];
				for(int i = 0; i < blacklist.length; i++) {
					newBlacklist[i] = blacklist[i];
				}
				newBlacklist[blacklist.length] = blacklistedTerm;
				ThreadManager.BLACKLIST.set(newBlacklist);
				Helpers.sendTimestampedChat("Added \"" + blacklistedTerm +"\" to blacklist!");
			}
			/*String message = "";
			for(int i = 0; i < newBlacklist.length; i++) {
				message += newBlacklist[i] + " ";
			}
			Helpers.sendTimestampedChat(message);*/
	        BinSnipe.config.save();
    	}
    }
    
    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }
}
