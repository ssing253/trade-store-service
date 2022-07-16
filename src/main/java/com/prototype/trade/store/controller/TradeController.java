package com.prototype.trade.store.controller;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.prototype.trade.store.exception.TradeStoreException;
import com.prototype.trade.store.model.Trade;
import com.prototype.trade.store.service.TradeService;

@RestController
public class TradeController {

	private static final Logger logger = LoggerFactory.getLogger(TradeController.class);

	private TradeService tradeService;

	@Autowired
	public void setTradeService(TradeService tradeService) {
		this.tradeService = tradeService;
	}

	@GetMapping("/trade")
	public ResponseEntity<String> helloTrades() {

		return new ResponseEntity<String>("Hello, Welcome to trade-store Service !!!", HttpStatus.OK);

	}

	@PostMapping("/trade")
	public ResponseEntity<String> saveTrade(@RequestBody Trade trade) {

		logger.info("Start processing tradeId={}/tradeVersion={}", trade.getTradeVersion(), trade.getTradeVersion());

		String responseMessage = "";

		try {

			responseMessage = tradeService.storeTrade(trade);

		} catch (Exception exp) {
			
			logger.error("Error while processing/storing trade: {}", exp);
			throw new TradeStoreException(exp.getMessage());

		}
		
		return ResponseEntity.status(HttpStatus.CREATED).body(responseMessage);

	}

}
