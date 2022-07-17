package com.prototype.trade.store.repository;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.google.common.collect.Lists;
import com.prototype.trade.store.model.Trade;

@DataJpaTest
public class TradeDaoTest {

	@Autowired
	private TradeDao tradeDao;

	@BeforeAll // This is class level
	public static void init() {
		System.out.println("BeforeAll init() method called");
	}

	@AfterAll // This is class level
	public static void teardown() {
		System.out.println("BeforeAll teardown() method called");
	}

	@BeforeEach // This is method level
	public void initEach() {
		tradeDao.deleteAll();
	}

	@AfterEach // This is method level
	public void teardownEach() {

	}

	@DisplayName("JUnit test for saving trades to trade store")
	@Test
	public void shouldSaveTradeToTradeStore() {

		Integer tradeId = 1;
		Integer tradeVersion = 100;
		String counterParty = "CPTY-1";
		String book = "BOOK-1";
		LocalDateTime createdDateTime = LocalDateTime.now();
		LocalDateTime maturityDateTime = createdDateTime.plusDays(5);
		Boolean expired = false;
		Integer version = 1;

		Trade tradeToBePersisted = getTrade(tradeId, tradeVersion, counterParty, book, createdDateTime,
				maturityDateTime, expired, version);
		Trade persistedTrade = tradeDao.save(tradeToBePersisted);

		// ensure trade has been persisted
		assertAll(() -> assertNotNull(persistedTrade, "Trade has been saved successfully"),
				() -> assertEquals(persistedTrade.getTradeId(), tradeId),
				() -> assertEquals(persistedTrade.getTradeVersion(), tradeVersion));
	}

	@DisplayName("JUnit test for fetching all trades from trade store by tradeId")
	@Test
	public void shouldFetchTradesByTradeId() {

		LocalDateTime today = LocalDateTime.now();

		// persist trades
		Trade tarde1 = getTrade(1, 1, "CPTY-1", "BOOK-1", today, today.plusDays(5), false, 1);
		Trade tarde2 = getTrade(1, 2, "CPTY-1", "BOOK-2", today, today.plusDays(1), false, 1);
		Trade tarde3 = getTrade(1, 3, "CPTY-1", "BOOK-3", today, today, true, 1);
		Trade tarde4 = getTrade(2, 1, "CPTY-4", "BOOK-4", today, today, true, 1);

		List<Trade> trades = tradeDao.saveAll(Lists.newArrayList(tarde1, tarde2, tarde3, tarde4));
		assertNotNull(trades, "All trades saved successfully");
		assertEquals(trades.size(), 4);

		// fetch trade for tardeId=1
		Optional<List<Trade>> exectedTrades = tradeDao.findTradesByTradeId(1);
		List<Trade> tradeList = exectedTrades.isPresent() ? exectedTrades.get() : null;

		assertNotNull(tradeList);
		assertEquals(tradeList.size(), 3);
		
		tradeList.stream().forEach(t -> {
			
			// ensure trade has been persisted
			assertAll(
						() -> assertEquals(t.getTradeId(), 1),
						() -> assertEquals(t.getCounterpartyId(), "CPTY-1")
					);
		});
		
	}
	
	@DisplayName("JUnit test for fetching a sngle trade from trade store by tradeId & TradeVersion")
	@Test
	public void shouldFetchTradeByTradeIdTradeVersion() {

		LocalDateTime today = LocalDateTime.now();

		// persist trades
		Trade tarde1 = getTrade(1, 1, "CPTY-1", "BOOK-1", today, today.plusDays(5), false, 1);
		Trade tarde2 = getTrade(1, 2, "CPTY-2", "BOOK-2", today, today.plusDays(1), false, 1);
		Trade tarde3 = getTrade(1, 3, "CPTY-3", "BOOK-3", today, today, true, 1);
		Trade tarde4 = getTrade(2, 1, "CPTY-4", "BOOK-4", today, today, true, 1);

		List<Trade> trades = tradeDao.saveAll(Lists.newArrayList(tarde1, tarde2, tarde3, tarde4));
		assertNotNull(trades, "All trades saved successfully");
		assertEquals(trades.size(), 4);

		// fetch trade for tardeId=1 and tradeVersion=2
		Optional<Trade> exectedTrade = tradeDao.findTradeByTradeIdTradeVersion(1, 2);
		Trade trade = exectedTrade.isPresent() ? exectedTrade.get() : null;

		// ensure trade has been persisted
		assertAll(
					() -> assertNotNull(trade, "Successful fetched trade"),
					() -> assertEquals(trade.getTradeId(), 1),
					() -> assertEquals(trade.getTradeVersion(), 2)
				);
	}

	@DisplayName("JUnit test for fetching all trades from  trade store which have not crossed maturity date")
	@Test
	public void shouldFetchAllActiveTrade() {

		LocalDateTime today = LocalDateTime.now();

		// persist active & expired trades trades
		Trade aciveTrade1 = getTrade(1, 1, "CPTY-1", "BOOK-1", today, today.plusDays(5), false, 1);
		Trade aciveTrade2 = getTrade(2, 1, "CPTY-2", "BOOK-2", today, today.plusDays(1), false, 1);

		Trade expiredTrade1 = getTrade(3, 1, "CPTY-3", "BOOK-3", today, today, true, 1);
		Trade expiredTrade2 = getTrade(4, 1, "CPTY-4", "BOOK-4", today, today, true, 1);

		List<Trade> trades = tradeDao
				.saveAll(Lists.newArrayList(aciveTrade1, aciveTrade2, expiredTrade1, expiredTrade2));
		assertNotNull(trades, "All trades saved successfully");
		assertEquals(trades.size(), 4);

		// fetch all active trades
		Optional<List<Trade>> activeTradesOption = tradeDao.findAllActiveTrades();
		List<Trade> activeTrades = activeTradesOption.isPresent() ? activeTradesOption.get() : null;
		
		assertEquals(activeTrades.size(), 2);
		activeTrades.forEach(t -> assertEquals(t.getExpired(), false));

	}

	private Trade getTrade(final Integer tradeId, final Integer tradeVersion, final String counterParty,
			final String book, final LocalDateTime createdDateTime, final LocalDateTime maturityDateTime,
			final Boolean expired, final Integer version) {

		return new Trade(tradeId, tradeVersion, counterParty, book, createdDateTime, maturityDateTime, expired,
				version);
	}

}
