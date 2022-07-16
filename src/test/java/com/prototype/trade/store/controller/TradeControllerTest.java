package com.prototype.trade.store.controller;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.prototype.trade.store.exception.TradeStoreException;
import com.prototype.trade.store.model.Trade;
import com.prototype.trade.store.repository.TradeDao;
import com.prototype.trade.store.service.impl.TradeServiceImpl;


@SpringBootTest
@AutoConfigureMockMvc
// @WebMvcTest(TradeController.class)
public class TradeControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@InjectMocks
	private TradeController tradeController;

	@InjectMocks
	private TradeServiceImpl tradeService;

	@Mock
	private TradeDao tradeDao;

	@BeforeEach
	public void initEach() {

		tradeService.setTradeDao(tradeDao);
		tradeController.setTradeService(tradeService);
		mockMvc = MockMvcBuilders.standaloneSetup(tradeController).build();
	}
	
	@Test
	public void shouldReturnDefaultMessage() throws Exception {
		this.mockMvc.perform(get("/trade")).andDo(print()).andExpect(status().isOk())
				.andExpect(content().string(containsString("Hello, Welcome to trade-store Service !!!")));
	}

	@DisplayName("JUnit test for throw exception when Maturity date is before Today date")
	@Test
	public void testExceptionAndShouldNotSaveTradeWhenMaturityDateIsBeforeToday() throws Exception {

		final LocalDateTime todayDateTime = LocalDateTime.now();
		final LocalDateTime maturityDateTime = todayDateTime.minusDays(1);

		final String expectedErrorMessage = "Trade maturiry date=" + maturityDateTime.toLocalDate()
				+ " is before today date";

		Trade trade = new Trade(1, 1, "CPTY-1", "BOOK-1", todayDateTime, maturityDateTime, true, 1);

		Exception exception = assertThrows(TradeStoreException.class, () -> {
			tradeController.saveTrade(trade);
		});

		String actualErrorMessage = exception.getMessage();
		assertTrue(actualErrorMessage.contains(expectedErrorMessage));
	}

	@DisplayName("JUnit test for throw exception when lower version of trade arrived")
	@Test
	public void testExceptionAndShouldNotSaveTradeWhenLowerTradeVesrionIsReceived() throws Exception {

		final LocalDateTime todayDateTime = LocalDateTime.now();
		final LocalDateTime maturityDateTime = todayDateTime.plusDays(5);
		final Integer tradeId = 1;
		final Integer higherTradeVersion = 2;
		final Integer lowerTradeVersion = 1;
		final String expectedResponseText = "Successfully processed & saved tradeId=" + tradeId + ", tradeVersion=" + higherTradeVersion;

		Trade higherVersionTrade = new Trade(tradeId, higherTradeVersion, "CPTY-1", "BOOK-1", todayDateTime, maturityDateTime, true, 1);
		Mockito.when(tradeDao.saveAndFlush(any())).thenReturn(higherVersionTrade);
		
		// Save initial version of trade
		ResponseEntity<String> response = tradeController.saveTrade(higherVersionTrade);
		
		assertAll(
				() -> assertNotNull(response),
				() -> assertEquals(response.getStatusCode(), HttpStatus.CREATED),
				() -> assertEquals(response.getBody(), expectedResponseText)
        );

		Optional<Trade> optional = Optional.ofNullable(higherVersionTrade);
	    Mockito.when(tradeDao.findMaxVersionTrade(any(Integer.class))).thenReturn(optional);   
			
		// should throw exception when - try to save same trade again but with lower
		// trade version
		Exception exception = assertThrows(TradeStoreException.class, () -> {
			tradeController.saveTrade(new Trade(tradeId, lowerTradeVersion, "CPTY-1", "BOOK-1", todayDateTime,
					maturityDateTime, true, 1));
		});

		String expectedErrorMessage = "Rejected tradeId/tradeVerssion=" + tradeId + "/" + lowerTradeVersion
				+ " as lower tarde version is received";

		String actualErrorMessage = exception.getMessage();
		assertEquals(actualErrorMessage, expectedErrorMessage);

	}
	
	@DisplayName("JUnit test for persisting new trade")
	@Test
	public void testShouldSaveTrade() throws Exception {

		final LocalDateTime todayDateTime = LocalDateTime.now();
		final LocalDateTime maturityDateTime = todayDateTime.plusDays(1);
		final Integer tradeId = 100;
		final Integer tradeVersion = 1;
		final String expectedMessage = "Successfully processed & saved tradeId=" + tradeId + ", tradeVersion="
				+ tradeVersion;
		
		Trade trade = new Trade(tradeId, tradeVersion, "CPTY-100", "BOOK-100", todayDateTime, maturityDateTime, true, 1);
		Mockito.when(tradeDao.saveAndFlush(any(Trade.class))).thenReturn(trade);
	    
		// Save initial version of trade
		ResponseEntity<String> response = tradeController.saveTrade(trade);
			
		assertAll(
				() -> assertNotNull(response),
				() -> assertEquals(response.getStatusCode(), HttpStatus.CREATED),
				() -> assertEquals(response.getBody(), expectedMessage)
        );
	}

}
