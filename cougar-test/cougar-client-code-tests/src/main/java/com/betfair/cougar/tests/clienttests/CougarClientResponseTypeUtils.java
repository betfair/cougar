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

package com.betfair.cougar.tests.clienttests;

import com.betfair.baseline.v2.enumerations.SimpleEnum;
import com.betfair.baseline.v2.to.BodyParamByteObject;
import com.betfair.baseline.v2.to.ByteOperationResponseObject;
import com.betfair.baseline.v2.to.ComplexObject;
import com.betfair.baseline.v2.to.SomeComplexObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CougarClientResponseTypeUtils {
	
	private static final String MACHINE_NAME_ERROR = "Unable to retrieve machine name";
	
	public Map<String,String> buildMap(String keys, String values){
		
		Map<String,String> returnMap = new LinkedHashMap<String,String>();
		String[] keysArray = keys.split(",");
		String[] valuesArray = values.split(",");
		
		for(int i = 0; i < keysArray.length && i < valuesArray.length; i++){
			returnMap.put(keysArray[i], valuesArray[i]);
		}
		
		return returnMap;
	}
	
	public Map<Integer,Integer> buildIntMap(String keys, String values){
		
		Map<Integer,Integer> returnMap = new HashMap<Integer,Integer>();
		String[] keysArray = keys.split(",");
		String[] valuesArray = values.split(",");
		
		for(int i = 0; i < keysArray.length && i < valuesArray.length; i++){
			returnMap.put(Integer.valueOf(keysArray[i].trim()), Integer.valueOf(valuesArray[i].trim()));
		}
		
		return returnMap;
	}
	
	public List<SimpleEnum> buildEnumList(String values){
		
		String[] valuesArray = values.split(",");	
		List<SimpleEnum> returnList = new ArrayList<SimpleEnum>();
		
		for(String s : valuesArray){
			returnList.add(SimpleEnum.valueOf(s.trim()));
		}
		
		return returnList;
	}
	
	public List<Integer> buildIntList(String values){
		
		String[] valuesArray = values.split(",");	
		List<Integer> returnList = new ArrayList<Integer>();
		for(int i = 0; i < valuesArray.length; i++){
			returnList.add(Integer.valueOf(valuesArray[i].trim()));
		}
		
		return returnList;
	}
	
	public List<String> buildList(String values){
		
		String[] valuesArray = values.split(",");	
		List<String> returnList = new ArrayList<String>();
		returnList.addAll(Arrays.asList(valuesArray));
		
		return returnList;
	}
	
	public Set<SimpleEnum> buildEnumSet(String values){
		
		Set<SimpleEnum> returnSet = new HashSet<SimpleEnum>();
		String[] valuesArray = values.split(",");
		
		for(int i = 0; i < valuesArray.length; i++){
			returnSet.add(SimpleEnum.valueOf(valuesArray[i].trim()));
		}
		return returnSet;
	}
	
	public Set<Integer> buildIntSet(String values){
		
		Set<Integer> returnSet = new HashSet<Integer>();
		String[] valuesArray = values.split(",");
		
		for(int i = 0; i < valuesArray.length; i++){
			returnSet.add(Integer.valueOf(valuesArray[i].trim()));
		}
		return returnSet;
	}
	
	public Set<String> buildSet(String values){
		
		Set<String> returnSet = new HashSet<String>();
		String[] valuesArray = values.split(",");
		
		for(int i = 0; i < valuesArray.length; i++){
			returnSet.add(valuesArray[i]);
		}
		return returnSet;
	}
	
	public Map<String,SomeComplexObject> buildComplexMap(List<SomeComplexObject> listOfObjects){
		
		Map<String,SomeComplexObject> returnMap = new HashMap<String,SomeComplexObject>();
		
		for(SomeComplexObject obj : listOfObjects){
			returnMap.put(obj.getStringParameter(),obj);
		}
		return returnMap;
	}
	
	public boolean compareComplexMaps(Map<String, SomeComplexObject> map1, Map<String, SomeComplexObject> map2){
		
		SomeComplexObject map1Complex1 = map1.get("String value for aaa");
		SomeComplexObject map1Complex2 = map1.get("String value for bbb");
		SomeComplexObject map1Complex3 = map1.get("String value for ccc");
		
		SomeComplexObject map2Complex1 = map1.get("String value for aaa");
		SomeComplexObject map2Complex2 = map1.get("String value for bbb");
		SomeComplexObject map2Complex3 = map1.get("String value for ccc");
		
		if(!map1Complex1.equals(map2Complex1)){
			return false;
		}
		if(!map1Complex2.equals(map2Complex2)){
			return false;
		}
		if(!map1Complex3.equals(map2Complex3)){
			return false;
		}
		
		return true;
	}
	
	public Map<String,SomeComplexObject> buildComplexDelegateMap(){
		
		Map<String,SomeComplexObject> returnMap = new HashMap<String, SomeComplexObject>();
		
		returnMap.put("DELEGATE", new SomeComplexObject());
		return returnMap;
	}
	
	public Map<String,SomeComplexObject> buildComplexDelegateReturnMap(List<SomeComplexObject> listOfObjects){
		
		Map<String,SomeComplexObject> returnMap = new HashMap<String, SomeComplexObject>();
		
		returnMap.put("object1", listOfObjects.get(0));
		returnMap.put("object2", listOfObjects.get(1));
		returnMap.put("object3", listOfObjects.get(2));
		return returnMap;
	}
	
	public boolean compareComplexDelegateMaps(Map<String, SomeComplexObject> map1, Map<String, SomeComplexObject> map2){
		
		SomeComplexObject map1Complex1 = map1.get("object1");
		SomeComplexObject map1Complex2 = map1.get("object2");
		SomeComplexObject map1Complex3 = map1.get("object3");
		
		SomeComplexObject map2Complex1 = map1.get("object1");
		SomeComplexObject map2Complex2 = map1.get("object2");
		SomeComplexObject map2Complex3 = map1.get("object3");
		
		if(!map1Complex1.equals(map2Complex1)){
			return false;
		}
		if(!map1Complex2.equals(map2Complex2)){
			return false;
		}
		if(!map1Complex3.equals(map2Complex3)){
			return false;
		}
		
		return true;
	}
	
	public Set<SomeComplexObject> buildComplexSet(List<SomeComplexObject> listOfObjects){
		
		Set<SomeComplexObject> returnSet = new HashSet<SomeComplexObject>();
		
		for(SomeComplexObject obj : listOfObjects){
			returnSet.add(obj);
		}
		
		return returnSet;
	}
	
	public List<ComplexObject> createEmptyComplexList(){
		return new ArrayList<ComplexObject>();
	}
	
	public Date createDateFromString(String dateString) {

		DateFormat formatStandard = new SimpleDateFormat("yyyy'-'MM'-'dd'T'HH':'mm':'ss'.'SSS'Z'");
		DateFormat formatWithOffset = new SimpleDateFormat("yyyy'-'MM'-'dd'T'HH':'mm':'ss'.'SSSZ");
		try {
			return formatStandard.parse(dateString);
		} catch (ParseException e) {
			try {
				return formatWithOffset.parse(dateString);
			} catch (ParseException e1) {
				return null;
			} 
		}
	}
	
	public String formatDateToString(Date date){
		SimpleDateFormat format = new SimpleDateFormat("yyyy'-'MM'-'dd'T 'HH':'mm':'ss'.'SSS'Z'");
		return format.format(date);
	}
	
	public String formatSetOfDatesToString(Set<Date> dates){
		
		StringBuffer buff = new StringBuffer();
		buff.append("[");
		for(Date d : dates){
			String dateString = formatDateToString(d)+", ";
			buff.append(dateString);
		}
		buff.delete(buff.length()-2, buff.length());
		buff.append("]");
		
		return buff.toString();
	}
	
	public String formatMapOfDatesToString(Map<String,Date> dates){
		
		StringBuffer buff = new StringBuffer();
		buff.append("{");
		for(Map.Entry<String, Date> entry : dates.entrySet()){
			String entryString = "("+entry.getKey()+", "+formatDateToString(entry.getValue())+"), ";
			buff.append(entryString);
		}
		buff.delete(buff.length()-2, buff.length());
		buff.append("}");
		
		return buff.toString();		
	}
	
	public Set<Date> createSetOfDates(List<Date> dates){
		
		Set<Date> returnSet = new HashSet<Date>();
		
		for(Date d : dates){
			returnSet.add(d);
		}
		return returnSet;
	}	
	
	public Map<String,Date> createMapOfDates(List<Date> dates){
		
		Map<String,Date> returnMap = new HashMap<String,Date>();

		for(int i = 0; i < dates.size(); i++){
			returnMap.put("date"+(i+1), dates.get(i));
		}
		
		return returnMap;
	}	
	
	public BodyParamByteObject buildByteBodyParamObject(String bytes){
		
		String[] input = bytes.split(",");
		byte[] byteArray = new byte[input.length];
		
		for(int i = 0; i < input.length; i++){
			byteArray[i] = Byte.valueOf(input[i]);
		}
		
		BodyParamByteObject byteObject = new BodyParamByteObject();
		byteObject.setBodyParameter(byteArray);
		
		return byteObject;
	}
	
	public boolean compareByteArrays(BodyParamByteObject inputWrapper, ByteOperationResponseObject outputWrapper){
		
		byte[] inputBytes = inputWrapper.getBodyParameter();
		byte[] outputBytes = outputWrapper.getBodyParameter();
		
		if(inputBytes.length != outputBytes.length){
			return false;
		}
		
		for(int i = 0; i < inputBytes.length; i ++){
			String in = String.valueOf(inputBytes[i]);
			String out = String.valueOf(outputBytes[i]);
			
			if(!in.equals(out)){
				return false;			
			}
		}
		
		return true;
	}
	
	//workaround to compare sets of ComplexObjects
	public boolean compareSets(Set<?> set1, Set<?> set2){
		Set<String> stringSet1 = new HashSet<String>();
		Set<String> stringSet2 = new HashSet<String>();
		for(Object o: set1){
			stringSet1.add(o.toString());
		}
		for(Object o:set2){
			stringSet2.add(o.toString());
		}
		return stringSet1.equals(stringSet2);
	}
	
	public boolean compareMaps(Map<?,?> map1, Map <?,?> map2){	
		
		return map1.equals(map2);
	}
}
