package com.prototype.trade.store.service;

import java.util.Optional;

import com.prototype.trade.store.model.Trade;

public interface TradeService {

	String processAndSaveTrade(Trade trade);
	
	Optional<Trade> getTradeWithIdTradeVersion(Integer tradeId, Integer tradeVersion);
	
	void markMaturedTradeExpired();
		
}
