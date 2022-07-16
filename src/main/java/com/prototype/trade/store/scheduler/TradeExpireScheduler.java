package com.prototype.trade.store.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.prototype.trade.store.service.TradeService;

@Component
@EnableAsync
public class TradeExpireScheduler {

	private static final Logger logger = LoggerFactory.getLogger(TradeExpireScheduler.class);

	private TradeService tradeService;

	@Autowired
	public void setTradeService(TradeService tradeService) {
		this.tradeService = tradeService;
	}

	@Async
	@Scheduled(fixedDelayString = "${fixedDelay.in.milliseconds}", initialDelay = 30000)
	public void checkForExipredTrades() {

		logger.info("Start checkig for expired trade >>>>");
		tradeService.markMaturedTradeExpired();

	}

}
