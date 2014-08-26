/*
 * Copyright 2014, The Sporting Exchange Limited
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

package com.betfair.cougar.netutil.nio.marshalling;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.betfair.cougar.netutil.nio.CougarProtocol;
import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import com.betfair.cougar.core.api.transcription.Parameter;
import com.betfair.cougar.core.api.transcription.ParameterType;
import com.betfair.cougar.core.api.transcription.TranscriptionException;
import com.betfair.cougar.marshalling.impl.to.ComplexTO;
import com.betfair.cougar.marshalling.impl.to.EnumTO;
import com.betfair.cougar.marshalling.impl.to.EnumType;
import com.betfair.cougar.marshalling.impl.to.TO;
import com.betfair.cougar.marshalling.impl.util.ByteArrayClassLoader;
import com.betfair.cougar.marshalling.impl.util.ComplexObjectCreator;
import com.betfair.cougar.marshalling.impl.util.EnumCreator;
import com.betfair.cougar.marshalling.impl.util.Pair;
import com.betfair.cougar.transport.api.protocol.CougarObjectOutput;
import com.betfair.cougar.netutil.nio.hessian.HessianObjectIOFactory;
import com.betfair.cougar.netutil.nio.hessian.HessianObjectInput;
import com.betfair.cougar.netutil.nio.hessian.HessianObjectOutput;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class SocketRMIMarshallerArgsTest {


	private SocketRMIMarshaller cut;
	private HessianObjectIOFactory ioFactory;
    private boolean server;

    public SocketRMIMarshallerArgsTest(boolean server) {
        this.server = server;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> params() {
        List<Object[]> ret = new ArrayList<Object[]>();
        ret.add(new Object[] {true});
        ret.add(new Object[] {false});
        return ret;
    }

    @Before
	public void setup() {
		cut= new SocketRMIMarshaller();
		ioFactory = new HessianObjectIOFactory(false);
	}

	@Test
	/**
	 * dynamically create a class that has a single integer field (set to value 12) and write it, read it back using the TO class that has two
	 * fields (initialised to 9 and 7)
	 */
	public void testAdditionalField() throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, SecurityException, NoSuchFieldException {

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ComplexObjectCreator complexObjectCreator = new ComplexObjectCreator("com.betfair.cougar.marshalling.impl.to.TO");
		complexObjectCreator.create(bos, new Pair[] {new Pair<String,String>(Integer.class.getName(),"i")});

		ByteArrayClassLoader bcl = new ByteArrayClassLoader(complexObjectCreator.objectType, bos.toByteArray());

		Class<?> clazz = Class.forName(complexObjectCreator.objectType, true, bcl);

		Object o = clazz.newInstance();
		Field f = clazz.getField("i");
		f.set(o, 12);

		Parameter[] params = new Parameter[1];
		ParameterType[] parameterTypes = new ParameterType[1];
		parameterTypes[0] = new ParameterType(int.class,null);
		params[0] = new Parameter("TO", new ParameterType(TO.class,parameterTypes),false);

		bos.reset();
		CougarObjectOutput hoo = ioFactory.newCougarObjectOutput(bos, CougarProtocol.TRANSPORT_PROTOCOL_VERSION_MAX_SUPPORTED);
		cut.writeArgs(params, new Object[] {o}, hoo);
		hoo.flush();

		Parameter[] toParameters = new Parameter[1];
		parameterTypes = new ParameterType[2];
		parameterTypes[0] = new ParameterType(int.class,null);
		parameterTypes[1] = new ParameterType(int.class,null);
		toParameters[0] = new Parameter("TO", new ParameterType(TO.class,parameterTypes) ,false);

		Object[] result = cut.readArgs(toParameters, ioFactory.newCougarObjectInput(new ByteArrayInputStream(bos.toByteArray()), CougarProtocol.TRANSPORT_PROTOCOL_VERSION_MAX_SUPPORTED));

		Assert.assertEquals(1, result.length);
		Assert.assertEquals(result[0].getClass(), TO.class);
		Assert.assertFalse(TO.class.equals(clazz));
		Assert.assertEquals(12, ((TO)result[0]).i);
		Assert.assertEquals(0, ((TO)result[0]).j);

	}

	@Test
	/**
	 * dynamically create a class that has a three integer field (set to 1,2,3) and write it, read it back using the TO class that has two
	 * fields (initialised to 9 and 7)
	 */
	public void testRemovedField() throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, SecurityException, NoSuchFieldException {

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ComplexObjectCreator complexObjectCreator = new ComplexObjectCreator("com.betfair.cougar.marshalling.impl.to.TO");
		complexObjectCreator.create(bos, new Pair[] {
				new Pair<String,String>(Integer.class.getName(),"i"),
				new Pair<String,String>(Integer.class.getName(),"j"),
				new Pair<String,String>(Integer.class.getName(),"x")
				});

		ByteArrayClassLoader bcl = new ByteArrayClassLoader(complexObjectCreator.objectType, bos.toByteArray());

		Class<?> clazz = Class.forName(complexObjectCreator.objectType, true, bcl);

		Object o = clazz.newInstance();
		Field f = clazz.getField("i");
		f.set(o, 1);
		f = clazz.getField("j");
		f.set(o, 2);
		f = clazz.getField("x");
		f.set(o, 3);

		Parameter[] params = new Parameter[1];
		params[0] = new Parameter("TO", new ParameterType(TO.class,null),false);

		bos.reset();
		CougarObjectOutput hoo = ioFactory.newCougarObjectOutput(bos, CougarProtocol.TRANSPORT_PROTOCOL_VERSION_MAX_SUPPORTED);
		cut.writeArgs(params, new Object[] {o}, hoo);
		hoo.flush();

		params = new Parameter[1];
		params[0] = new Parameter("TO", new ParameterType(TO.class,null) ,false);

		Object[] result = cut.readArgs(params, ioFactory.newCougarObjectInput(new ByteArrayInputStream(bos.toByteArray()), CougarProtocol.TRANSPORT_PROTOCOL_VERSION_MAX_SUPPORTED));

		Assert.assertEquals(1, result.length);
		Assert.assertEquals(result[0].getClass(), TO.class);
		Assert.assertFalse(TO.class.equals(clazz));
		Assert.assertEquals(1, ((TO)result[0]).i);
		Assert.assertEquals(2, ((TO)result[0]).j);

	}

	@Test
	/**
	 * dynamically create a class that has an enum with two values (initially set to TWO), write it, and read it back using the EnumTO class that now has three
	 * enum types (set to THREE)
	 */
	public void testEnumAdded() throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, SecurityException, NoSuchFieldException {

		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		EnumCreator enumCreator = new EnumCreator("com.betfair.cougar.marshalling.impl.to.EnumType" ,new String[] {"ONE","TWO"});
		enumCreator.create(bos);

		ByteArrayClassLoader twoEnum = new ByteArrayClassLoader(EnumType.class.getName(), bos.toByteArray());
		Class<Enum> twoEnumClass = (Class<Enum>) Class.forName("com.betfair.cougar.marshalling.impl.to.EnumType",true,twoEnum);
		Enum TWO = Enum.valueOf(twoEnumClass, "TWO");
		bos.reset();

		ComplexObjectCreator complexObjectCreator = new ComplexObjectCreator("com.betfair.cougar.marshalling.impl.to.EnumTO");
		complexObjectCreator.create(bos, new Pair[]{ new Pair<String,String>("com.betfair.cougar.marshalling.impl.to.EnumType","enumType")});

		ByteArrayClassLoader bcl = new ByteArrayClassLoader(twoEnum,"com.betfair.cougar.marshalling.impl.to.EnumTO", bos.toByteArray());
		Class<?> enumTOClass = Class.forName("com.betfair.cougar.marshalling.impl.to.EnumTO", true, bcl);
		Object enumTO = enumTOClass.newInstance();
		Field f = enumTOClass.getField("enumType");
		f.set(enumTO, TWO);

		Parameter[] params = new Parameter[1];
		params[0] = new Parameter("EnumTO", new ParameterType(EnumTO.class,null),false);

		bos.reset();
		CougarObjectOutput hoo = ioFactory.newCougarObjectOutput(bos, CougarProtocol.TRANSPORT_PROTOCOL_VERSION_MAX_SUPPORTED);
		cut.writeArgs(params, new Object[] {enumTO}, hoo);
		hoo.flush();
		Object[] result = cut.readArgs(params, ioFactory.newCougarObjectInput(new ByteArrayInputStream(bos.toByteArray()), CougarProtocol.TRANSPORT_PROTOCOL_VERSION_MAX_SUPPORTED));

		Assert.assertEquals(1, result.length);
		Assert.assertEquals(result[0].getClass(), EnumTO.class);
		Assert.assertFalse(EnumTO.class.equals(enumTOClass));
		Assert.assertEquals(new EnumTO().enumType, EnumType.THREE);
		Assert.assertEquals(EnumType.TWO, ((EnumTO)result[0]).enumType);

	}

	@Test(expected=TranscriptionException.class)
	/**
	 * dynamically create a class that has an enum with four values (initially set to FOUR), write it, and read it back using the EnumTO class that now has three
	 * enum types (with FOUR being removed)
	 */
	public void testEnumRemoved() throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, SecurityException, NoSuchFieldException {

		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		EnumCreator enumCreator = new EnumCreator("com.betfair.cougar.marshalling.impl.to.EnumType" ,new String[] {"ONE","TWO","THREE","FOUR"});
		enumCreator.create(bos);

		ByteArrayClassLoader enumCL = new ByteArrayClassLoader("com.betfair.cougar.marshalling.impl.to.EnumType",bos.toByteArray());
		Class<Enum> enumClass = (Class<Enum>) Class.forName("com.betfair.cougar.marshalling.impl.to.EnumType",true,enumCL);
		Enum FOUR = Enum.valueOf(enumClass, "FOUR");
		bos.reset();

		ComplexObjectCreator complex = new ComplexObjectCreator("com.betfair.cougar.marshalling.impl.to.EnumTO");
		complex.create(bos, new Pair[] {new Pair<String,String>("com.betfair.cougar.marshalling.impl.to.EnumType","enumType")});

		ByteArrayClassLoader bcl = new ByteArrayClassLoader(enumCL, "com.betfair.cougar.marshalling.impl.to.EnumTO", bos.toByteArray());

		Class<?> clazz = Class.forName("com.betfair.cougar.marshalling.impl.to.EnumTO", true, bcl);
		Object o = clazz.newInstance();
		Field f = clazz.getField("enumType");
		f.set(o, FOUR);


		Parameter[] params = new Parameter[1];
		params[0] = new Parameter("EnumTO", new ParameterType(EnumTO.class,null),false);

		bos.reset();
		CougarObjectOutput hoo = ioFactory.newCougarObjectOutput(bos, CougarProtocol.TRANSPORT_PROTOCOL_VERSION_MAX_SUPPORTED);
		cut.writeArgs(params, new Object[] {o}, hoo);
		hoo.flush();
		Object[] result = cut.readArgs(params, ioFactory.newCougarObjectInput(new ByteArrayInputStream(bos.toByteArray()), CougarProtocol.TRANSPORT_PROTOCOL_VERSION_MAX_SUPPORTED));


	}

	@Test
	/**
	 * A complex type, where the composed type has gone from having one field to having two fields
	 */
	public void testFieldAdded() throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, SecurityException, NoSuchFieldException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ComplexObjectCreator complexObjectCreator = new ComplexObjectCreator("com.betfair.cougar.marshalling.impl.to.TO");
		complexObjectCreator.create(bos, new Pair[] {
				new Pair<String,String>(Integer.class.getName(),"i"),
				});

		//Use the binary class loader to load ComplexTO so that we can wire it up to the ClassLoader that loads the composed (TO) object
		ByteArrayClassLoader cl = new ByteArrayClassLoader(complexObjectCreator.objectType,bos.toByteArray());
		bos.reset();
		InputStream is = ComplexTO.class.getResourceAsStream(ComplexTO.class.getSimpleName()+".class");
		byte[] buffer = new byte[1024];
		int bytesRead = 0;
		while ((bytesRead = is.read(buffer)) != -1) {
			bos.write(buffer,0,bytesRead);
		}

		ByteArrayClassLoader complexTOCL = new ByteArrayClassLoader(cl,ComplexTO.class.getName(), bos.toByteArray());

		Class<?> clazz = Class.forName(ComplexTO.class.getName(),true,complexTOCL);
		Object o = clazz.newInstance();

		Field f = clazz.getField("to");
		Field toField = f.get(o).getClass().getField("i");

		toField.set(f.get(o), 12);

		try {
			f.getDeclaringClass().getField("j");
			Assert.fail();

		}
		catch (NoSuchFieldException e) {
			//just testing that this really is a different class to TO.java
		}

		Parameter[] params = new Parameter[1];
		params[0] = new Parameter("ComplexTO", new ParameterType(clazz,null),false);

		bos.reset();
		CougarObjectOutput hoo = ioFactory.newCougarObjectOutput(bos, CougarProtocol.TRANSPORT_PROTOCOL_VERSION_MAX_SUPPORTED);
		cut.writeArgs(params, new Object[]{o}, hoo);
		hoo.flush();

		params = new Parameter[1];
		params[0] = new Parameter("ComplexTO", new ParameterType(ComplexTO.class,null),false);

		Object[] result = cut.readArgs(params, ioFactory.newCougarObjectInput(new ByteArrayInputStream(bos.toByteArray()), CougarProtocol.TRANSPORT_PROTOCOL_VERSION_MAX_SUPPORTED));

		Assert.assertEquals(12, ((ComplexTO)result[0]).to.i);
		Assert.assertEquals(0, ((ComplexTO)result[0]).to.j);

	}




}
