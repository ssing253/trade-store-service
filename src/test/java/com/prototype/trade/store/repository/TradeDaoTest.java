package com.prototype.trade.store.repository;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.google.common.collect.Lists;
import com.prototype.trade.store.model.Trade;

@DataJpaTest
public class TradeDaoTest {
	
	@Autowired
	private TradeDao tradeDao;
	
	@BeforeAll	// This is class level
    public static void init(){
        System.out.println("BeforeAll init() method called");
    }
	
	@AfterAll	// This is class level
	public static void teardown(){
	        System.out.println("BeforeAll teardown() method called");
	}
	
	@BeforeEach	// This is method level
    public void initEach() {
       tradeDao.deleteAll();
    }
	
	@AfterEach	// This is method level
    public void teardownEach() {
       
    }
	
	@Test
	public void testSaveTrade() {

		Integer tradeId = 1;
		Integer tradeVersion = 100;
		String counterParty = "CPTY-1";
		String book = "BOOK-1";
		LocalDateTime createdDateTime = LocalDateTime.now();
		LocalDateTime maturityDateTime = createdDateTime.plusDays(5);
		Boolean expired = false;
		Integer version = 1;

		Trade tradeToBePersisted = getTrade(tradeId, tradeVersion, counterParty, book, createdDateTime, maturityDateTime, expired, version);
		Trade persistedTrade = tradeDao.save(tradeToBePersisted);

		// ensure trade has been persisted
		assertAll(
				() -> assertNotNull(persistedTrade, "Trade has been saved successfully"),
				() -> assertEquals(persistedTrade.getTradeId(), tradeId),
				() -> assertEquals(persistedTrade.getTradeVersion(), tradeVersion)
        );
	}
	
	@Test
	public void testFindAllActiveTrade() {
		
		LocalDateTime today = LocalDateTime.now();
		
		//persist active & expired trades trades
		Trade aciveTrade1 = getTrade(1, 1, "CPTY-1", "BOOK-1", today, today.plusDays(5), false, 1);
		Trade aciveTrade2 = getTrade(2, 1, "CPTY-2", "BOOK-2", today, today.plusDays(1), false, 1);
		
		Trade expiredTrade1 = getTrade(3, 1, "CPTY-3", "BOOK-3", today, today, true, 1);
		Trade expiredTrade2 = getTrade(4, 1, "CPTY-4", "BOOK-4", today, today, true, 1);
		
		List<Trade> trades = tradeDao.saveAll(Lists.newArrayList(aciveTrade1, aciveTrade2, expiredTrade1, expiredTrade2));
		assertNotNull(trades, "All trades saved successfully");
		assertEquals(trades.size(), 4);

		// fetch all active trades
		List<Trade> activeTrades = tradeDao.findAllActiveTrades().isPresent() 
				? tradeDao.findAllActiveTrades().get()
				: null;
		
		assertEquals(activeTrades.size(), 2);
		activeTrades.forEach(t -> assertEquals(t.getExpired(), false));
		
	}
	
	private Trade getTrade(final Integer tradeId, final Integer tradeVersion, final String counterParty,
			final String book, final LocalDateTime createdDateTime, final LocalDateTime maturityDateTime,
			final Boolean expired, final Integer version) {
		
		return new Trade(tradeId, tradeVersion, counterParty, book, createdDateTime,
				maturityDateTime, expired, version);
	}

}
