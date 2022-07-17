package com.prototype.trade.store.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.prototype.trade.store.model.Trade;

@Repository
public interface TradeDao extends JpaRepository<Trade, Integer> {

	//@Query("select t1 from Trade t1 where t1.tradeId = :tradeId and t1.tradeVersion=(select max(t2.tradeVersion) from Trade t2)")
	//Optional<Trade> findMaxVersionTrade(@Param("tradeId") Integer tradeId);
	
	@Query("select t from Trade t where t.tradeId = :tradeId")
	Optional<List<Trade>> findTradesByTradeId(@Param("tradeId") Integer tradeId);

	@Query("select t from Trade t where t.expired=false")
	Optional<List<Trade>> findAllActiveTrades();

	@Modifying
	@Query("update Trade t set t.expired=true where t.tradeId = :tradeId and t.tradeVersion = :tradeVersion")
	void expireTrade(@Param("tradeId") Integer tradeId, @Param("tradeVersion") Integer tradeVersion);

	@Query("select t from Trade t where t.tradeId = :tradeId and t.tradeVersion = :tradeVersion")
	Optional<Trade> findTradeByTradeIdTradeVersion(@Param("tradeId") Integer tradeId, @Param("tradeVersion") Integer tradeVersion);

}
