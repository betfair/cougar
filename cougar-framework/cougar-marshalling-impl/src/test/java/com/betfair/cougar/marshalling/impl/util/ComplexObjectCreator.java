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

package com.betfair.cougar.marshalling.impl.util;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.bcel.Constants;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.FieldGen;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.Type;

public class ComplexObjectCreator implements Constants {
  public final String objectType ;
private InstructionFactory _factory;
  private ConstantPoolGen    _cp;
  private ClassGen           _cg;

  public ComplexObjectCreator(String objectType) {
    _cg = new ClassGen(objectType, "java.lang.Object", "ComplexObject.java", ACC_PUBLIC | ACC_SUPER, new String[] { "com.betfair.cougar.api.Result" });

    _cp = _cg.getConstantPool();
    _factory = new InstructionFactory(_cg, _cp);
    this.objectType = objectType;
  }

  public void create(OutputStream out, Pair<String,String>[] fieldDefs ) throws IOException {
    createFields(fieldDefs);
    createMethod_0();
    _cg.getJavaClass().dump(out);
  }

  private void createFields(Pair<String,String>[] fieldDefs) {
    FieldGen field;

    for (Pair<String,String> aField : fieldDefs) {
	    field = new FieldGen(ACC_PUBLIC, new ObjectType(aField.first), aField.second, _cp);
	    _cg.addField(field.getField());
    }
  }

  private void createMethod_0() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.VOID, Type.NO_ARGS, new String[] {  }, "<init>", objectType, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke("java.lang.Object", "<init>", Type.VOID, Type.NO_ARGS, Constants.INVOKESPECIAL));
    InstructionHandle ih_4 = il.append(_factory.createReturn(Type.VOID));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }


}