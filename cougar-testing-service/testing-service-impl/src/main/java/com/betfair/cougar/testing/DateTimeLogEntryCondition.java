/*
 * Copyright 2013, The Sporting Exchange Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.betfair.cougar.testing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;

public abstract class DateTimeLogEntryCondition implements LogEntryCondition {
	final static Logger LOGGER = LoggerFactory
	.getLogger(DateTimeLogEntryCondition.class);

	//
	private final SimpleDateFormat dateFormat;
	// caching this on construction to save time in use - the dateFormat is immutable
	private final int dateFormatLength;

	private Date checkDate;

	public DateTimeLogEntryCondition(String dateTimeFormatString)
	{
		this.dateFormat = new SimpleDateFormat(dateTimeFormatString);
		this.dateFormatLength = dateTimeFormatString.length();
	}

	private int getDateFormatLength()
	{
		return this.dateFormatLength;
	}

	private SimpleDateFormat getDateFormat()
	{
		return this.dateFormat;
	}

	public Date getCheckDate() {
		return checkDate;
	}

	public void setCheckDate(Date checkDate)
	{
		this.checkDate = checkDate;
	}

	public void setCheckDate(String checkDateString)
	{
		this.checkDate = getDateFromLogEntry(checkDateString);
	}

	// assumes the date is at the start of the log entry
	protected Date getDateFromLogEntry(String logEntry)
	{
		Date returnDate= null;
		if((logEntry != null) && (logEntry.length() >= this.getDateFormatLength()))
		{
			String datePart = logEntry.substring(0, this.getDateFormatLength());
			try
			{
				returnDate = this.getDateFormat().parse(datePart);
			}
			catch(ParseException ex)
			{
				// it's not a date in the right format
				LOGGER.debug(datePart+" is not a date time");
			}
		}
		return returnDate;
	}

}
