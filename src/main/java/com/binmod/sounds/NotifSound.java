package com.binmod.sounds;

import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.ISound.AttenuationType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class NotifSound implements ISound {

	@Override
	public ResourceLocation getSoundLocation() {
		return new ResourceLocation("binmod", "notify");
	}

	@Override
	public boolean canRepeat() {
		return false;
	}

	@Override
	public int getRepeatDelay() {
		return 0;
	}

	@Override
	public float getVolume() {
		return 1.5f;
	}

	@Override
	public float getPitch() {
		return 1.0f;
	}

	@Override
	public float getXPosF() {
		return 0;
	}

	@Override
	public float getYPosF() {
		return 0;
	}

	@Override
	public float getZPosF() {
		return 0;
	}

	@Override
	public net.minecraft.client.audio.ISound.AttenuationType getAttenuationType() {
		return net.minecraft.client.audio.ISound.AttenuationType.NONE;
	}

}
