package com.prototype.trade.store.service;

import java.util.Optional;

import com.prototype.trade.store.model.Trade;

public interface TradeService {

	Optional<Trade> getTradeWithId(Integer tradeId);
	
	String storeTrade(Trade trade);
	
	void markMaturedTradeExpired();
		
}
