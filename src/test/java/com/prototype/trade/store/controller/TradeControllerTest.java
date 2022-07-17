package com.prototype.trade.store.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.google.common.collect.Lists;
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
	
	@DisplayName("JUnit test for default hello message")
	@Test
	public void shouldReturnHelloMessage() throws Exception {
		this.mockMvc.perform(get("/trade")).andDo(print()).andExpect(status().isOk())
				.andExpect(content().string(containsString("Hello, Welcome to trade-store Service !!!")));
	}
	
	@DisplayName("JUnit test for throw exception when Maturity date is before Today date")
	@Test
	public void shouldThrowExceptionAndNotSaveTradeWhenMaturityDateIsBeforeToday() throws Exception {

		final LocalDateTime todayDateTime = LocalDateTime.now();
		final LocalDateTime maturityDateTime = todayDateTime.minusDays(1);

		final String expectedErrorMessage = "Trade maturiry date=" + maturityDateTime.toLocalDate()
				+ " is before today date";

		Trade trade = new Trade(1, 1, "CPTY-1", "BOOK-1", todayDateTime, maturityDateTime, true, 1);

		Exception exception = assertThrows(TradeStoreException.class, () -> {
			tradeController.saveTrade(trade);
		});

		String actualErrorMessage = exception.getMessage();
		assertEquals(actualErrorMessage, expectedErrorMessage);
	}

	@DisplayName("JUnit test for throw exception when lower version of trade arrived to trade-store")
	@Test
	public void shouldThrowExceptionAndNotSaveTradeWhenLowerTradeVesrionIsReceived() throws Exception {

		final LocalDateTime todayDateTime = LocalDateTime.now();
		final LocalDateTime maturityDateTime = todayDateTime.plusDays(5);
		final Integer tradeId = 1;
		final Integer tradeVersion_1 = 1;
		final Integer tradeVersion_2 = 2;
		final Integer tradeVersion_3 = 3;
		
		final String expectedErrorMessage = "Rejected tradeId/tradeVerssion=" + tradeId + "/" + tradeVersion_1 + " as lower trade version is received";
		
		Trade higerVersionTrade_v2 = new Trade(tradeId, tradeVersion_2, "CPTY-2", "BOOK-2", todayDateTime, maturityDateTime, false, 1);
		Trade higerVersionTrade_v3 = new Trade(tradeId, tradeVersion_3, "CPTY-3", "BOOK-3", todayDateTime, maturityDateTime, false, 1);
		
		Optional<List<Trade>> optional =  Optional.ofNullable(Lists.newArrayList(higerVersionTrade_v2, higerVersionTrade_v3));
		Mockito.when(tradeDao.findTradesByTradeId(any(Integer.class))).thenReturn(optional);  
		
		// start processing/saving lower trade version
		Trade lowerVersionTrade = new Trade(tradeId, tradeVersion_1, "CPTY-1", "BOOK-1", todayDateTime, maturityDateTime, true, 1);
		Exception exception = assertThrows(TradeStoreException.class, () -> {
			tradeController.saveTrade(lowerVersionTrade);
		});
		
		
		String actualErrorMessage = exception.getMessage();
		assertEquals(actualErrorMessage, expectedErrorMessage);
		verify(tradeDao, times(1)).findTradesByTradeId(any(Integer.class));

	}
	
	@DisplayName("JUnit test for persisting fresh trade (i.e.non existing tardeId) in trad-store")
	@Test
	public void shouldSaveTradeWhenNewTradeWithFutureMaturityDateArrives() throws Exception {

		final LocalDateTime todayDateTime = LocalDateTime.now();
		final LocalDateTime maturityDateTime = todayDateTime.plusDays(1);
		final Integer tradeId = 100;
		final Integer tradeVersion = 1;
		final String expectedMessage = "Successfully processed & saved tradeId=" + tradeId + ", tradeVersion="
				+ tradeVersion;
		
		Trade trade = new Trade(tradeId, tradeVersion, "CPTY-100", "BOOK-100", todayDateTime, maturityDateTime, true, 1);
		
		Mockito.when(tradeDao.findTradesByTradeId(any(Integer.class))).thenReturn(Optional.ofNullable(null));
		Mockito.when(tradeDao.saveAndFlush(any(Trade.class))).thenReturn(trade);
	    
		// process & save fresh trade
		ResponseEntity<String> response = tradeController.saveTrade(trade);
			
		assertAll(
				() -> assertNotNull(response),
				() -> assertEquals(response.getStatusCode(), HttpStatus.CREATED),
				() -> assertEquals(response.getBody(), expectedMessage)
        );
		
		verify(tradeDao, times(1)).findTradesByTradeId(any(Integer.class));
		verify(tradeDao, times(1)).saveAndFlush(any(Trade.class));
	}
	
	@DisplayName("JUnit test for updating trade in trade-store if same version arrives")
	@Test
	public void shouldUpdateTradeWhenSameTradeVersionArrives() throws Exception {

		final LocalDateTime todayDateTime = LocalDateTime.now();
		final LocalDateTime maturityDateTime = todayDateTime.plusDays(1);
		final Integer tradeId = 100;
		final Integer tradeVersion = 1;
		final String expectedMessage = "Successfully processed & saved tradeId=" + tradeId + ", tradeVersion="
				+ tradeVersion;
		
		Trade trade = new Trade(tradeId, tradeVersion, "CPTY-100", "BOOK-100", todayDateTime, maturityDateTime, true, 1);
		
		Mockito.when(tradeDao.findTradesByTradeId(any(Integer.class))).thenReturn(Optional.ofNullable(null));
		Mockito.when(tradeDao.saveAndFlush(any(Trade.class))).thenReturn(trade);
	    
		// process & save fresh trade
		ResponseEntity<String> response = tradeController.saveTrade(trade);
					
		assertAll(
					() -> assertNotNull(response),
					() -> assertEquals(response.getStatusCode(), HttpStatus.CREATED),
					() -> assertEquals(response.getBody(), expectedMessage)
		        );
		
		// trade with same tradeId/tradeVesrion but updated counterpart & book details
		Trade tradeWithSameTradeIdTradeVersion = new Trade(tradeId, tradeVersion, "CPTY-200", "BOOK-200", todayDateTime, maturityDateTime, true, 1);
		Mockito.when(tradeDao.findTradesByTradeId(any(Integer.class))).thenReturn(Optional.ofNullable(Lists.newArrayList(trade)));
		Mockito.when(tradeDao.saveAndFlush(any(Trade.class))).thenReturn(tradeWithSameTradeIdTradeVersion);
		
		// trade with same tradeId/tradeVesrion arrived again
		ResponseEntity<String> responseEntity = tradeController.saveTrade(trade);
		assertAll(
				() -> assertNotNull(responseEntity),
				() -> assertEquals(response.getStatusCode(), HttpStatus.CREATED),
				() -> assertEquals(response.getBody(), expectedMessage)
        );
		
		verify(tradeDao, times(2)).findTradesByTradeId(any(Integer.class));
		verify(tradeDao, times(2)).saveAndFlush(any(Trade.class));
	}
	
	@DisplayName("JUnit test for fetching a trade from trade stored by tradeId & tradeVersion")
	@Test
	public void shouldReturnSavedTradeWhenSearchByTradeIdAndTradeVersion() throws Exception {

		final LocalDateTime todayDateTime = LocalDateTime.now();
		final LocalDateTime maturityDateTime = todayDateTime.plusDays(1);
		final Integer tradeId = 10;
		final Integer tradeVersion = 100;
		final String expectedMessage = "Successfully processed & saved tradeId=" + tradeId + ", tradeVersion="
				+ tradeVersion;
		
		//save a trade to trade store
		Trade trade = new Trade(tradeId, tradeVersion, "CPTY-100", "BOOK-100", todayDateTime, maturityDateTime, true, 1);
		Mockito.when(tradeDao.saveAndFlush(any(Trade.class))).thenReturn(trade);
	    
		// Save initial version of trade
		ResponseEntity<String> response = tradeController.saveTrade(trade);
			
		assertAll(
				() -> assertNotNull(response),
				() -> assertEquals(response.getStatusCode(), HttpStatus.CREATED),
				() -> assertEquals(response.getBody(), expectedMessage)
        );
		
		Optional<Trade> option= Optional.ofNullable(trade);  
		Mockito.when(tradeDao.findTradeByTradeIdTradeVersion(any(Integer.class), any(Integer.class))).thenReturn(option);
		
		// make a get trade call by tradeId & tradeVersion verify the response 
		mockMvc.perform(get("/trade/10/100")
			      .contentType(MediaType.APPLICATION_JSON))
			      .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			      .andDo(print())
			      .andExpect(jsonPath("$.*", hasSize(9)))
			      //.andExpect(jsonPath("$", aMapWithSize(9))) // alternate to above
			      .andExpect(jsonPath("$.tradeId", is(10)))
			      .andExpect(jsonPath("$.tradeVersion", is(100)))
			      .andExpect(jsonPath("$.counterpartyId", is("CPTY-100")))
			      .andExpect(jsonPath("$.bookId", is("BOOK-100")));
		
		verify(tradeDao, times(1)).findTradeByTradeIdTradeVersion(any(Integer.class), any(Integer.class));
	}

}
