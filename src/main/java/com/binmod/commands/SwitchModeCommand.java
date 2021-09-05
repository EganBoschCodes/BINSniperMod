package com.binmod.commands;

import com.binmod.async.ThreadManager;
import com.binmod.main.BinSnipe;
import com.binmod.main.Helpers;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

public class SwitchModeCommand extends CommandBase {

    @Override
    public String getCommandName() {
        return "switchmode";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "Toggles the mode between Local and API based.";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        ThreadManager.MODE.set(1 - ThreadManager.MODE.getInt());
        BinSnipe.config.save();
        Helpers.sendTimestampedChat("Changed mode to " + (ThreadManager.MODE.getInt() > 0 ? "local." : "API-based."));
    }
    
    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }
}
