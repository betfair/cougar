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

package com.betfair.testing.utils.cougar.misc;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringHelpers {

	public static String generateRandomString(int Length, String caseType) {

		String[] caseList = new String[Length];
		if (caseType.toUpperCase().matches("UPPER")) {
			for (int i = 0; i < caseList.length; i++) {
				caseList[i] = "UPPER";
			}
		} else if (caseType.toUpperCase().matches("LOWER")) {
			for (int i = 0; i < caseList.length; i++) {
				caseList[i] = "LOWER";
			}
		} else if (caseType.toUpperCase().matches("FIRSTUPPER")) {
			for (int i = 0; i < caseList.length; i++) {
				if (i == 0) {
					caseList[i] = "UPPER";
				} else {
					caseList[i] = "LOWER";
				}
			}
		} else if (caseType.toUpperCase().matches("MIXED")) {
			for (int i = 0; i < caseList.length; i++) {
				Random RND = new Random();
				boolean yBool = RND.nextBoolean();
				if (yBool) {
					caseList[i] = "UPPER";
				} else {
					caseList[i] = "LOWER";
				}
			}
		} else {
			for (int i = 0; i < caseList.length; i++) {
				Random RND = new Random();
				boolean yBool = RND.nextBoolean();
				if (yBool) {
					caseList[i] = "UPPER";
				} else {
					caseList[i] = "LOWER";
				}
			}
		}

		String returnString = "";
		int tempInt;
		char randomChar;
		for (int i = 0; i < caseList.length; i++) {
			if (caseList[i].matches("UPPER")) {
				Random RNG1 = new Random();
				tempInt = RNG1.nextInt(90 - 65 + 1) + 65;
				randomChar = (char) tempInt;
			} else {
				Random RNG2 = new Random();
				tempInt = (char) RNG2.nextInt(122 - 97 + 1) + 97;
				randomChar = (char) tempInt;
			}
			returnString = returnString + String.valueOf(randomChar);
		}

		return returnString;

	}
	
	/*
	 * Parses the passed in string to a srandardised DateTime string
	 * 
	 * @param str_date
	 * @return
	 */
	public static String formatDateString(String str_date) {

		String[] patterns = new String[6];
		Pattern p0;
		Pattern p1;
		Pattern p2;
		Pattern p3;
		Pattern p4;
		Pattern p5;
		
		/* dd/mm/yy hh:mm:ss.S */
		patterns[0] = "(0[1-9]|[12][0-9]|3[01])/(0[1-9]|1[012])/((\\d\\d)|((19|20)\\d\\d))\\s([1-9]|([01][0-9])|(2[1-3])):((0[0-9])|([12345][0-9])):((0[0-9])|([12345][0-9])).[0-9]*";
		/* dd/mm/yy hh:mm:ss */
		patterns[1] = "(0[1-9]|[12][0-9]|3[01])/(0[1-9]|1[012])/((\\d\\d)|((19|20)\\d\\d))\\s([1-9]|([01][0-9])|(2[1-3])):((0[0-9])|([12345][0-9])):((0[0-9])|([12345][0-9]))";
		/* dd/mm/yy hh:mm */
		patterns[2] = "(0[1-9]|[12][0-9]|3[01])/(0[1-9]|1[012])/((\\d\\d)|((19|20)\\d\\d))\\s([1-9]|([01][0-9])|(2[1-3])):((0[0-9])|([12345][0-9]))";
		/* dd/mm/yy */
		patterns[3] = "(0[1-9]|[12][0-9]|3[01])/(0[1-9]|1[012])/((\\d\\d)|((19|20)\\d\\d))";
		/* yyyy-mm-dd hh:mm:ss.S */
		patterns[4] = "((\\d\\d)|((19|20)\\d\\d))-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])\\s([1-9]|([01][0-9])|(2[0-3])):((0[0-9])|([12345][0-9])):((0[0-9])|([12345][0-9])).[0-9]*";
		/* yyyymmddhhmmss */
		patterns[5] = "((\\d\\d)|((19|20)\\d\\d))(0[1-9]|1[012])(0[1-9]|[12][0-9]|3[01])([1-9]|([01][0-9])|(2[0-3]))((0[0-9])|([12345][0-9]))((0[0-9])|([12345][0-9]))";
						       
		p0 = Pattern.compile(patterns[0]);
		p1 = Pattern.compile(patterns[1]);
		p2 = Pattern.compile(patterns[2]);
		p3 = Pattern.compile(patterns[3]);
		p4 = Pattern.compile(patterns[4]);
		p5 = Pattern.compile(patterns[5]);
		
		/* Set up array containing the supported date patterns */

		
		Matcher m;
		boolean b;
		int dateType = 0;

		
		m = p0.matcher(str_date);
		b = m.matches();
		if (b) {
			dateType = 1;
		}

		m = p1.matcher(str_date);
		b = m.matches();
		if (b) {
			dateType = 2;
		}

		m = p2.matcher(str_date);
		b = m.matches();
		if (b) {
			dateType = 3;
		}

		m = p3.matcher(str_date);
		b = m.matches();
		if (b) {
			dateType = 4;
		}

		m = p4.matcher(str_date);
		b = m.matches();
		if (b) {
			dateType = 5;
		}

		m = p5.matcher(str_date);
		b = m.matches();
		if (b) {
			dateType = 6;
		}

		switch (dateType) {
		case 1:
			/* Do nothing, string in correct format */
			break;
		case 2:
			/* Append milliseconds */
			str_date = str_date + ".0";
			break;
		case 3:
			/* Append seconds and milliseconds */
			str_date = str_date + ":00.0";
			break;
		case 4:
			/* Append hours, minutes, seconds and milliseconds */
			str_date = str_date + " 00:00:00.0";
			break;
		case 5:
			/* Reformat */
			String datePortion = str_date.split(" ")[0];
			String timePortion = str_date.split(" ")[1];

			String yearString = datePortion.split("-")[0].substring(2, 4);
			String monthString = datePortion.split("-")[1];
			String dayString = datePortion.split("-")[2];

			str_date = dayString + "/" + monthString + "/" + yearString + " "
					+ timePortion;
			break;
		case 6:
			/* need to return dd/mm/yy hh:mm:ss.S  from yyyymmddhhmmss */
			String year = str_date.substring(0, 4);
			String month = str_date.substring(4, 6);
			String day = str_date.substring(6, 8);
			String hour = str_date.substring(8, 10);
			String minute = str_date.substring(10, 12);
			String second = str_date.substring(12, 14);
			
			str_date =  day + "/" + month + "/" + year + " " + hour + ":" + minute + ":" + second + ".0";
			break;
		default:
			/* Do nothing, string is not recognised as a Time/Date string */
			break;
		}

		return str_date;

	}
	
	/*
	 * Parses the passed in string to a srandardised DateTime string
	 * to dd/mm/yyyy hh:mm:ss.S
	 * 
	 * @param str_date
	 * @return
	 */
	public static String formatDateString2(String str_date) throws ParseException {
	
		String[] patterns = new String[6];
		Pattern p0;
		Pattern p1;
		Pattern p2;
		Pattern p3;
		Pattern p4;
		Pattern p5;
		
		/* dd/mm/yy hh:mm:ss.S */
		patterns[0] = "(0[1-9]|[12][0-9]|3[01])/(0[1-9]|1[012])/((\\d\\d)|((19|20)\\d\\d))\\s([1-9]|([01][0-9])|(2[1-3])):((0[0-9])|([12345][0-9])):((0[0-9])|([12345][0-9])).[0-9]*";
		/* dd/mm/yy hh:mm:ss */
		patterns[1] = "(0[1-9]|[12][0-9]|3[01])/(0[1-9]|1[012])/((\\d\\d)|((19|20)\\d\\d))\\s([1-9]|([01][0-9])|(2[1-3])):((0[0-9])|([12345][0-9])):((0[0-9])|([12345][0-9]))";
		/* dd/mm/yy hh:mm */
		patterns[2] = "(0[1-9]|[12][0-9]|3[01])/(0[1-9]|1[012])/((\\d\\d)|((19|20)\\d\\d))\\s([1-9]|([01][0-9])|(2[1-3])):((0[0-9])|([12345][0-9]))";
		/* dd/mm/yy */
		patterns[3] = "(0[1-9]|[12][0-9]|3[01])/(0[1-9]|1[012])/((\\d\\d)|((19|20)\\d\\d))";
		/* yyyy-mm-dd hh:mm:ss.S */
		patterns[4] = "((\\d\\d)|((19|20)\\d\\d))-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])\\s([1-9]|([01][0-9])|(2[0-3])):((0[0-9])|([12345][0-9])):((0[0-9])|([12345][0-9])).[0-9]*";
		/* yyyymmddhhmmss */
		patterns[5] = "((\\d\\d)|((19|20)\\d\\d))(0[1-9]|1[012])(0[1-9]|[12][0-9]|3[01])([1-9]|([01][0-9])|(2[0-3]))((0[0-9])|([12345][0-9]))((0[0-9])|([12345][0-9]))";
						       
		p0 = Pattern.compile(patterns[0]);
		p1 = Pattern.compile(patterns[1]);
		p2 = Pattern.compile(patterns[2]);
		p3 = Pattern.compile(patterns[3]);
		p4 = Pattern.compile(patterns[4]);
		p5 = Pattern.compile(patterns[5]);
		
		/* Set up array containing the supported date patterns */
	
		
		Matcher m;
		boolean b;
		int dateType = 0;
	
		
		m = p0.matcher(str_date);
		b = m.matches();
		if (b) {
			dateType = 1;
		}
	
		m = p1.matcher(str_date);
		b = m.matches();
		if (b) {
			dateType = 2;
		}
	
		m = p2.matcher(str_date);
		b = m.matches();
		if (b) {
			dateType = 3;
		}
	
		m = p3.matcher(str_date);
		b = m.matches();
		if (b) {
			dateType = 4;
		}
	
		m = p4.matcher(str_date);
		b = m.matches();
		if (b) {
			dateType = 5;
		}
	
		m = p5.matcher(str_date);
		b = m.matches();
		if (b) {
			dateType = 6;
		}
	
		
		SimpleDateFormat requiredFormat = new SimpleDateFormat("dd/mm/yyyy hh:mm:ss.S");
		SimpleDateFormat currentFormat;
		Date passedDate;
		switch (dateType) {
		case 1:
			currentFormat = new SimpleDateFormat("dd/mm/yy hh:mm:ss.S");
			passedDate = currentFormat.parse(str_date);
			str_date = requiredFormat.format(passedDate);			
			break;
		case 2:
			currentFormat = new SimpleDateFormat("dd/mm/yy hh:mm:ss");
			passedDate = currentFormat.parse(str_date);
			str_date = requiredFormat.format(passedDate);			
			break;
		case 3:
			currentFormat = new SimpleDateFormat("dd/mm/yy hh:mm");
			passedDate = currentFormat.parse(str_date);
			str_date = requiredFormat.format(passedDate);			
			break;
		case 4:
			currentFormat = new SimpleDateFormat("dd/mm/yy");
			passedDate = currentFormat.parse(str_date);
			str_date = requiredFormat.format(passedDate);			
			break;
		case 5:
			/* Reformat */
			String datePortion = str_date.split(" ")[0];
			String timePortion = str_date.split(" ")[1];
	
			String yearString = datePortion.split("-")[0].substring(0, 4);
			String monthString = datePortion.split("-")[1];
			String dayString = datePortion.split("-")[2];
	
			str_date = dayString + "/" + monthString + "/" + yearString + " "
					+ timePortion;
			break;
		case 6:
			/* need to return dd/mm/yy hh:mm:ss.S  from yyyymmddhhmmss */
			String year = str_date.substring(0, 4);
			String month = str_date.substring(4, 6);
			String day = str_date.substring(6, 8);
			String hour = str_date.substring(8, 10);
			String minute = str_date.substring(10, 12);
			String second = str_date.substring(12, 14);
			
			str_date =  day + "/" + month + "/" + year + " " + hour + ":" + minute + ":" + second + ".0";
			break;
		default:
			/* Do nothing, string is not recognised as a Time/Date string */
			break;
		}
	
		return str_date;
	
	}
	
	public static String getCurrentDateTimeString(String returnFormat) {
		
		/* For now just output to a hardcoded text file until style sheets done */
		DateFormat dateFormat = new SimpleDateFormat(returnFormat);
		Date date = new Date();
		String dateString = dateFormat.format(date);
		dateString = dateString.replace("/", "");
		dateString = dateString.replace(" ", "");
		dateString = dateString.replace(":", "");
		dateString = dateString.replace(".", "");
		
		return dateString;
		
	}
	
}
