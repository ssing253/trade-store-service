package com.prototype.trade.store.service.impl;

import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.prototype.trade.store.date.util.DateUtils;
import com.prototype.trade.store.exception.TradeStoreException;
import com.prototype.trade.store.model.Trade;
import com.prototype.trade.store.repository.TradeDao;
import com.prototype.trade.store.service.TradeService;

@Service
public class TradeServiceImpl implements TradeService {

	private static final Logger logger = LoggerFactory.getLogger(TradeServiceImpl.class);

	private TradeDao tradeDao;

	@Autowired
	public void setTradeDao(TradeDao tradeDao) {
		this.tradeDao = tradeDao;
	}

	@Override
	public Optional<Trade> getTradeWithIdTradeVersion(Integer tradeId, Integer tradeVersion) {

		return tradeDao.findTradeByTradeIdTradeVersion(tradeId, tradeVersion);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@Override
	public String processAndSaveTrade(Trade trade) {

		// check if trade to be persisted has future maturity date
		tradeWithFutureMaturtyDate(trade.getMaturityDate());

		Optional<List<Trade>> savedTrades = tradeDao.findTradesByTradeId(trade.getTradeId());
		
		if (savedTrades.isPresent()) { 	// when trades already exists in trade-store

			validateTradeVersionAndUpdateTrade(trade, savedTrades.get());

		} else { // when fresh trade is received-trade not exists in trade-store

			trade = tradeDao.saveAndFlush(trade);
			logger.info("Saved tradeId={}, Tradeversion={}", trade.getTradeId(), trade.getTradeVersion());
		}

		return "Successfully processed & saved tradeId=" + trade.getTradeId() + ", tradeVersion=" + trade.getTradeVersion();

	}
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void markMaturedTradeExpired() {

		Optional<List<Trade>> activeTradesList = tradeDao.findAllActiveTrades();

		if (activeTradesList.isPresent()) {

			List<Trade> activeTrades = activeTradesList.get();
			logger.info("Total unexpired trades={}", activeTrades.size());

			activeTrades.stream().forEach(t -> {

				Period p = DateUtils.differenceBetweenDates(LocalDateTime.now(), t.getMaturityDate());
				if (p.isNegative()) {

					tradeDao.expireTrade(t.getTradeId(), t.getTradeVersion());
					
					logger.info("TradeId={}, TradeVersion={} has been set expired={}", t.getTradeId(),
							t.getTradeVersion(), t.getExpired());
				}
			});
		}
	}

	private void validateTradeVersionAndUpdateTrade(final Trade trade, final List<Trade> existingTradesList) {

		// reject to process/save if lower trade version received
		if (tradeWithLowerVersion(trade, existingTradesList)) {

			String message = "Rejected tradeId/tradeVerssion=" + trade.getTradeId() + "/" + trade.getTradeVersion()
					+ " as lower trade version is received";
			logger.info(message);
			throw new TradeStoreException(message);
		}

		// if trade with same exiting version is received then update existing trade
		Trade existingTrade =  tradeWithSameVersion(trade, existingTradesList); 
		if (null != existingTrade) {

			// update existing trade
			existingTrade.setBookId(trade.getBookId());
			existingTrade.setCounterpartyId(trade.getCounterpartyId());
			existingTrade.setCreatedDate(trade.getCreatedDate());
			existingTrade.setMaturityDate(trade.getMaturityDate());
			existingTrade.setExpired(trade.getExpired());
			existingTrade.setVersion(existingTrade.getVersion() + 1);
			tradeDao.saveAndFlush(trade);

			logger.info("Updated tradeId={}, tradeversion={}", existingTrade.getTradeId(),
					existingTrade.getTradeVersion());
		}
	}

	private void tradeWithFutureMaturtyDate(LocalDateTime tradeMaturityDate) {

		if (isMaturityDateBeforeToday(tradeMaturityDate)) {

			String message = "Trade maturiry date=" + tradeMaturityDate.toLocalDate() + " is before today date";
			logger.info(message);
			throw new TradeStoreException(message);
		}
	}

	private boolean isMaturityDateBeforeToday(LocalDateTime maturityDate) {

		return DateUtils.differenceBetweenDates(LocalDateTime.now(), maturityDate).isNegative() ? true : false;
	}

	private boolean tradeWithLowerVersion(Trade newTrade, List<Trade> existingTradesList) {

		Trade higherVersionTrade = existingTradesList.stream()
				.filter(t -> t.getTradeVersion() > newTrade.getTradeVersion())
				.findAny()
				.orElse(null);

		if (null != higherVersionTrade) {
			
			logger.info("Higher version={} found for tradeId={}", higherVersionTrade.getTradeVersion(),
					higherVersionTrade.getTradeId());

			return true;
		}
		return false;
	}
	
	private Trade tradeWithSameVersion(Trade newTrade, List<Trade> existingTrades) {

		return existingTrades.stream()
					.filter(t -> t.getTradeVersion() == newTrade.getTradeVersion())
					.findAny()
					.orElse(null);
	}
	
	
}
