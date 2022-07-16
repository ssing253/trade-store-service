package com.prototype.trade.store.model;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.Version;

@Entity
@Table(name = "TRADE")
//@DynamicUpdate
public class Trade implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "trade_seq")
	@SequenceGenerator(name = "trade_seq", sequenceName = "SEQ_TRADE_STORE", allocationSize = 1, initialValue = 1)
	@Column(name = "ID")
	private Integer id;

	@Column(name = "TRADE_ID")
	private Integer tradeId;

	@Column(name = "TRADE_VERSION")
	private Integer tradeVersion;

	@Column(name = "COUNTERPARTY_ID")
	private String counterpartyId;

	@Column(name = "BOOK_ID")
	private String bookId;

	@Column(name = "MATURITY_DATE")
	private LocalDateTime maturityDate;

	@Column(name = "CREATED_DATE")
	private LocalDateTime createdDate;

	@Column(name = "EXPIRED")
	private Boolean expired;
	
	@Column(name = "Version")
	@Version
	private Integer version;

	public Trade() {

	}

	public Trade(Integer tradeId, Integer tradeVersion, String counterpartyId, String bookId,
			LocalDateTime createdDate, LocalDateTime maturityDate, Boolean expired, Integer version) {
		super();
		this.tradeId = tradeId;
		this.tradeVersion = tradeVersion;
		this.counterpartyId = counterpartyId;
		this.bookId = bookId;
		this.createdDate = createdDate;
		this.maturityDate = maturityDate;
		this.expired = expired;
		this.version =  version;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getTradeId() {
		return tradeId;
	}

	public void setTradeId(Integer tradeId) {
		this.tradeId = tradeId;
	}

	public Integer getTradeVersion() {
		return tradeVersion;
	}

	public void setTradeVersion(Integer tradeVersion) {
		this.tradeVersion = tradeVersion;
	}

	public String getCounterpartyId() {
		return counterpartyId;
	}

	public void setCounterpartyId(String counterpartyId) {
		this.counterpartyId = counterpartyId;
	}

	public String getBookId() {
		return bookId;
	}

	public void setBookId(String bookId) {
		this.bookId = bookId;
	}

	public LocalDateTime getMaturityDate() {
		return maturityDate;
	}

	public void setMaturityDate(LocalDateTime maturityDate) {
		this.maturityDate = maturityDate;
	}

	public LocalDateTime getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(LocalDateTime createdDate) {
		this.createdDate = createdDate;
	}

	public Boolean getExpired() {
		return expired;
	}

	public void setExpired(Boolean expired) {
		this.expired = expired;
	}
	
	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	@Override
	public String toString() {
		return "Trade [tradeId=" + tradeId + ", tradeVersion=" + tradeVersion + ", counterpartyId=" + counterpartyId
				+ ", bookId=" + bookId + ", maturityDate=" + maturityDate + ", createdDate=" + createdDate
				+ ", expired=" + expired + ", version=" + version +"]";
	}

}
