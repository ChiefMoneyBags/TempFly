package com.moneybags.tempfly.util;

import java.util.Date;

public class DailyDate extends Date {

	private static final long serialVersionUID = 7853013854292147249L;

	public DailyDate(long millis) {
		super(millis);
	}
	
	//Shhhh
	@SuppressWarnings("deprecation")
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Date)) {
			return false;
		}
		Date date = (Date) o;
		
		return date.getDay() == this.getDay() && date.getMonth() == this.getMonth() && date.getYear() == this.getYear();
	}

}
