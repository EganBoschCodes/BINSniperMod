package com.binmod.commands;

import com.binmod.async.ThreadManager;
import com.binmod.main.BinSnipe;
import com.binmod.main.Helpers;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

public class ChangeMinProfit extends CommandBase {

    @Override
    public String getCommandName() {
        return "minprofit";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "Changes the minimum profit of acceptable trades.";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
    	for(int i = 0; i < args.length; i++) {
    		System.out.println(args[i]);
    	}
    	if(args.length > 0) {
    		try {
    			int newMinProfit = Integer.parseInt(args[0]);
    			ThreadManager.MINPROFIT.set(newMinProfit);
    	        BinSnipe.config.save();
    			Helpers.sendTimestampedChat("Minimum Profit changed to $"+ Helpers.localeString(newMinProfit));
    		}
    		catch(NumberFormatException nfe) {
    			Helpers.sendError("Sorry, but \""+args[0]+"\" is not a valid number!");
    		}
    	}
    	else {
    		Helpers.sendTimestampedChat("Current Minimum Profit: $"+Helpers.localeString(ThreadManager.MINPROFIT.getInt()));
    	}
    }
    
    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }
}
