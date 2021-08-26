package com.binmod.datatypes;

import java.util.ArrayList;
import java.util.Collections;

public class ItemAuctionData {
	public String tag;
	public Auction minAuction;
	public ArrayList<Integer> prices = new ArrayList<Integer>();
	
	public ItemAuctionData(String tag, Auction auction) {
		this.tag = tag;
		this.minAuction = auction;
		this.prices.add(auction.starting_bid);
	}
	
	public void addAuction(Auction auction) {
		if(auction.starting_bid < this.minAuction.starting_bid) {
			this.minAuction = auction;
		}
		this.prices.add(auction.starting_bid);
		Collections.sort(this.prices);
	}
	
	public double averagePrice() {
		
		double avg = 0;
		int counter = 0;
		for(int i = 1; i < Math.min(7, this.prices.size()); i++) {
			avg += this.prices.get(i);
			counter++;
		}
		return avg / counter;
	}
	
	public double standardDeviation() {
		double mean = this.averagePrice();
		

		int counter = 0;
		double stdDev = 0;
		for(int i = 1; i < Math.min(7, this.prices.size()); i++) {
			stdDev += Math.pow(this.prices.get(i) - mean, 2);
			counter++;
		}
		
		return Math.sqrt(stdDev / counter);
	}
	
	public double getProfit() {
		return (this.prices.get(1) * 0.98) - this.prices.get(0);
	}
	
	public double pval() {
		return (this.prices.get(1) - this.prices.get(0)) / this.standardDeviation();
	}
}
