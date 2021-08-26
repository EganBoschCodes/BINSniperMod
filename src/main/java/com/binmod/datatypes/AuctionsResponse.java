package com.binmod.datatypes;

import java.util.HashMap;
import java.util.Map;

import com.binmod.main.Helpers;

public class AuctionsResponse {
	public boolean success;
	public int page;
	public int totalPages;
	public int totalAuctions;
	public long lastUpdated;
	public Auction[] auctions;
	
	public static HashMap<String, ItemAuctionData> processData(AuctionsResponse auctionData){
		HashMap<String, ItemAuctionData> processedData = new HashMap<String, ItemAuctionData>();
		
		for(int i = 0; i < auctionData.auctions.length; i++) {
			Auction auction = auctionData.auctions[i];
			
			if(auction.bin && auction.item_name.charAt(0) != '[' && auction.highest_bid_amount == 0 && !auction.item_name.contains(" Skin")) {
				String tag = Helpers.pruneName(auction) + " " + auction.tier.toLowerCase();
				
				if(processedData.containsKey(tag)) {
					processedData.get(tag).addAuction(auction);
				}
				else {
					processedData.put(tag, new ItemAuctionData(tag, auction));
				}
			}
			
		}
		
		return processedData;
	}
}
