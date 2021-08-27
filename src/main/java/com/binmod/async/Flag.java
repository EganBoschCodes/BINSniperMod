package com.binmod.async;

public class Flag {
	private boolean value = false;
	
	public Flag() {}
	
	public boolean get() { return value; }
	public Flag set(boolean b) { value = b; return this; }
}
