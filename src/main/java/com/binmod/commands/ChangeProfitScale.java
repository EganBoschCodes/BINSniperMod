package com.binmod.commands;

import com.binmod.async.ThreadManager;
import com.binmod.main.BinSnipe;
import com.binmod.main.Helpers;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

public class ChangeProfitScale  extends CommandBase {

    @Override
    public String getCommandName() {
        return "profitscale";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "Changes how Minimum Profit scales with input money (profitScale * sqrt(price))";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
    	for(int i = 0; i < args.length; i++) {
    		System.out.println(args[i]);
    	}
    	if(args.length > 0) {
    		try {
    			int newProfitScale = Integer.parseInt(args[0]);
    			ThreadManager.PROFITSCALE.set(newProfitScale);
    	        BinSnipe.config.save();
    			Helpers.sendTimestampedChat("Profit Scale changed to $"+ Helpers.localeString(newProfitScale));
    		}
    		catch(NumberFormatException nfe) {
    			Helpers.sendError("Sorry, but \""+args[0]+"\" is not a valid number!");
    		}
    	}
    	else {
    		Helpers.sendTimestampedChat("Current Profit Scale: "+Helpers.localeString(ThreadManager.PROFITSCALE.getInt()));
    	}
    }
    
    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }
}
