package com.prototype.trade.store.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

import com.google.common.collect.Lists;
import com.prototype.trade.store.exception.TradeStoreException;
import com.prototype.trade.store.model.Trade;
import com.prototype.trade.store.repository.TradeDao;
import com.prototype.trade.store.service.impl.TradeServiceImpl;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class TradeServiceTest {

	@Mock
	private TradeDao tradeDao;

	@InjectMocks
	private TradeServiceImpl tradeService;

	@BeforeEach
	public void setup() {
		
		
	}

	@DisplayName("Junit for method 'processAndSaveTrade' when trade with past maturtyDate is received")
	@Test
	public void shouldThrowExceptionWhenTradeWithExpiredMaturtyDateArrive() {

		final LocalDateTime todayDateTime = LocalDateTime.now();
		final LocalDateTime maturityDateTime = todayDateTime.minusDays(1);
		final Integer tradeId = 1;
		final Integer tradeVersion = 10;
		final String expectedErrorMessage = "Trade maturiry date=" + maturityDateTime.toLocalDate()
				+ " is before today date";

		Trade trade = new Trade(tradeId, tradeVersion, "CPTY-1", "BOOK-1", todayDateTime, maturityDateTime, true, 1);

		Exception exception = assertThrows(TradeStoreException.class, () -> {
			tradeService.processAndSaveTrade(trade);
		});

		String actualErrorMessage = exception.getMessage();
		assertEquals(actualErrorMessage, expectedErrorMessage);

	}
	
	@DisplayName("Junit for method 'processAndSaveTrade' when trade with lower version is received")
	@Test
	public void shouldThrowExceptionWhenTradeWithLowerVersionArrive() {

		final LocalDateTime todayDateTime = LocalDateTime.now();
		final LocalDateTime maturityDateTime = todayDateTime.plusDays(1);
		final Integer tradeId = 1;
		final Integer higherTradeVersion_1 = 20;
		final Integer higherTradeVersion_2 = 30;
		final Integer lowerTradeVersion = 10;
		final String counterParty = "CPTY-1";
		final String book = "BOOK-1";
		final Boolean expired = false;
		final Integer version = 1;
		final String expectedErrorMessage = "Rejected tradeId/tradeVerssion=" + tradeId + "/" + lowerTradeVersion + " as lower trade version is received";
		
		Trade higerVersionTrade1 = new Trade(tradeId, higherTradeVersion_1, counterParty, book, todayDateTime, maturityDateTime, expired, version);
		Trade higerVersionTrade2 = new Trade(tradeId, higherTradeVersion_2, counterParty, book, todayDateTime, maturityDateTime, expired, version);
		
		Optional<List<Trade>> optional =  Optional.ofNullable(Lists.newArrayList(higerVersionTrade1, higerVersionTrade2));
		Mockito.when(tradeDao.findTradesByTradeId(any(Integer.class))).thenReturn(optional);   
		
		// start processing/saving lower trade version
		Trade lowerVersionTrade = new Trade(tradeId, lowerTradeVersion, counterParty, book, todayDateTime, maturityDateTime, expired, version);
		Exception exception = assertThrows(TradeStoreException.class, () -> {
			tradeService.processAndSaveTrade(lowerVersionTrade);
		});

		String actualErrorMessage = exception.getMessage();

		assertEquals(actualErrorMessage, expectedErrorMessage);
		verify(tradeDao, times(1)).findTradesByTradeId(any(Integer.class));

	}
	
	@DisplayName("Junit for method 'processAndSaveTrade' when trade with same version is received")
	@Test
	public void shouldUpdateTradeSuccessfullyIfTradeWithSameVersionArrivedAgain() {

		final LocalDateTime todayDateTime = LocalDateTime.now();
		final LocalDateTime maturityDateTime = todayDateTime.plusDays(1);
		final Integer tradeId = 1;
		final Integer tradeVersion_1 = 10;
		final Integer tradeVersion_2 = 20;
		final Integer tradeVersion_3 = 30;
		final String counterParty_1 = "CPTY-1";
		final String book_2 = "BOOK-1";
		final String counterParty_2 = "CPTY-2";
		final String book_1 = "BOOK-2";
		final Boolean expired = false;
		final Integer version = 1;
		
		Trade trade1 = new Trade(tradeId, tradeVersion_1, counterParty_1, book_1, todayDateTime, maturityDateTime, expired, version);
		Trade trade2 = new Trade(tradeId, tradeVersion_2, counterParty_1, book_1, todayDateTime, maturityDateTime.plusDays(1), expired, version);
		Trade trade3 = new Trade(tradeId, tradeVersion_3, counterParty_1, book_1, todayDateTime, maturityDateTime.plusDays(2), expired, version);
		
		Optional<List<Trade>> optional =  Optional.ofNullable(Lists.newArrayList(trade1, trade2, trade3));
		Mockito.when(tradeDao.findTradesByTradeId(any(Integer.class))).thenReturn(optional);   
		
		// start processing/saving trade3 again with same tradeId & tradeVersion
		Trade trade3_withUpdates = new Trade(tradeId, tradeVersion_3, counterParty_2, book_2, todayDateTime, maturityDateTime, expired, version);
		tradeService.processAndSaveTrade(trade3_withUpdates);
		
		verify(tradeDao, times(1)).findTradesByTradeId(any(Integer.class));
		verify(tradeDao, times(1)).saveAndFlush(any(Trade.class));

	}
	
	@DisplayName("Junit for method 'markMaturedTradeExpired' when trade maturity date has been crossed")
	@Test
	public void shouldExpireTradeWhenTradeMaturiyDateIsCrossed() {

		final LocalDateTime todayDateTime = LocalDateTime.now();
		// trade to be expired trades trades
		Trade trade1 = new Trade(3, 1, "CPTY-3", "BOOK-3", todayDateTime, todayDateTime.minusDays(1), true, 1);
		Trade trade2 = new Trade(4, 1, "CPTY-4", "BOOK-4", todayDateTime, todayDateTime.minusDays(2), true, 1);

		Optional<List<Trade>> optional = Optional.ofNullable(Lists.newArrayList(trade1, trade2));
		Mockito.when(tradeDao.findAllActiveTrades()).thenReturn(optional);

		tradeService.markMaturedTradeExpired();

		verify(tradeDao, times(1)).findAllActiveTrades();
		verify(tradeDao, times(2)).expireTrade(any(Integer.class), any(Integer.class));

	}

}
