package com.prototype.trade.store.date.util;

import java.time.LocalDateTime;
import java.time.Period;

public interface DateUtils {

	public static Period differenceBetweenDates(LocalDateTime baseDate, LocalDateTime targetDate) {

		return Period.between(baseDate.toLocalDate(), targetDate.toLocalDate());

	}

}