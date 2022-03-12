package com.binmod.datatypes;

import java.util.HashMap;

import com.binmod.main.Helpers;

public class AuctionHouse {
	public HashMap<String, ItemAuctionData> itemRegistry = new HashMap<String, ItemAuctionData>();
	public long lastUpdated = 0;
	
	public void assimilatePage(AuctionsResponse auctionData) {
		lastUpdated = Math.max(lastUpdated, auctionData.lastUpdated);
		for(int i = 0; i < auctionData.auctions.length; i++) {
			Auction auction = auctionData.auctions[i];
			
			if(auction.bin && auction.item_name.charAt(0) != '[' && auction.highest_bid_amount == 0 && (auction.item_lore != null && auction.item_lore.length() > 12 && !auction.item_lore.substring(2,11).equals("Furniture"))) {
				String tag = Helpers.pruneName(auction) + " " + auction.tier.toLowerCase();
				
				if(this.itemRegistry.containsKey(tag)) {
					this.itemRegistry.get(tag).addAuction(auction);
				}
				else {
					this.itemRegistry.put(tag, new ItemAuctionData(tag, auction));
				}
			}
		}
		
	}
	
}
