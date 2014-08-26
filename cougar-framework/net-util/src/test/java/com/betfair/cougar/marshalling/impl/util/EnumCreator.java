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

import org.apache.bcel.generic.*;
import org.apache.bcel.classfile.*;
import org.apache.bcel.*;
import java.io.*;

public class EnumCreator implements Constants {
    private final String enumType ;
    private InstructionFactory _factory;
    private ConstantPoolGen    _cp;
    private ClassGen           _cg;
    private String[] enums;

    public EnumCreator(String enumType, String[] enums) {
        _cg = new ClassGen(enumType, "java.lang.Enum", "EnumOperationHeaderParamEnum.java", ACC_PUBLIC | ACC_FINAL | ACC_SUPER | ACC_ENUM, new String[] {  });
        _cg.setMajor(50);
        _cg.setMinor(0);
        _cp = _cg.getConstantPool();
        _factory = new InstructionFactory(_cg, _cp);
        this.enumType = enumType;
        this.enums = enums;
    }

    public void create(OutputStream out) throws IOException {
        createFields();
        createMethod_0();
        createMethod_1();
        createMethod_2();
        createMethod_3();
        _cg.getJavaClass().dump(out);
    }

    private void createFields() {
        FieldGen field;


        for (String anEnum : enums) {
            field = new FieldGen(ACC_PUBLIC | ACC_STATIC | ACC_FINAL | ACC_ENUM, new ObjectType(enumType), anEnum, _cp);
            _cg.addField(field.getField());
        }


        field = new FieldGen(ACC_PRIVATE | ACC_STATIC | ACC_FINAL | ACC_SYNTHETIC, new ArrayType(new ObjectType(enumType), 1), "ENUM$VALUES", _cp);
        _cg.addField(field.getField());
    }

    private void createMethod_0() {
        InstructionList il = new InstructionList();
        MethodGen method = new MethodGen(ACC_STATIC, Type.VOID, Type.NO_ARGS, new String[] {  }, "<clinit>", enumType, il, _cp);


        int code = 0;
        for (String anEnum : enums) {
            InstructionHandle ih_0 = il.append(_factory.createNew(enumType));
            il.append(InstructionConstants.DUP);
            il.append(new PUSH(_cp, anEnum));
            il.append(new PUSH(_cp, code++));
            il.append(_factory.createInvoke(enumType, "<init>", Type.VOID, new Type[] { Type.STRING, Type.INT }, Constants.INVOKESPECIAL));
            il.append(_factory.createFieldAccess(enumType, anEnum, new ObjectType(enumType), Constants.PUTSTATIC));
        }


        InstructionHandle ih_39 = il.append(new PUSH(_cp, enums.length));
        il.append(_factory.createNewArray(new ObjectType(enumType), (short) 1));

        code =0;
        for (String anEnum : enums) {
            il.append(InstructionConstants.DUP);
            il.append(new PUSH(_cp, code++));
            il.append(_factory.createFieldAccess(enumType, anEnum, new ObjectType(enumType), Constants.GETSTATIC));
            il.append(InstructionConstants.AASTORE);
        }


        il.append(_factory.createFieldAccess(enumType, "ENUM$VALUES", new ArrayType(new ObjectType(enumType), 1), Constants.PUTSTATIC));
        il.append(_factory.createReturn(Type.VOID));
        method.setMaxStack();
        method.setMaxLocals();
        _cg.addMethod(method.getMethod());
        il.dispose();
    }

    private void createMethod_1() {
        InstructionList il = new InstructionList();
        MethodGen method = new MethodGen(ACC_PRIVATE, Type.VOID, new Type[] { Type.STRING, Type.INT }, new String[] { "arg0", "arg1" }, "<init>", enumType, il, _cp);

        InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
        il.append(_factory.createLoad(Type.OBJECT, 1));
        il.append(_factory.createLoad(Type.INT, 2));
        il.append(_factory.createInvoke("java.lang.Enum", "<init>", Type.VOID, new Type[] { Type.STRING, Type.INT }, Constants.INVOKESPECIAL));
        InstructionHandle ih_6 = il.append(_factory.createReturn(Type.VOID));
        method.setMaxStack();
        method.setMaxLocals();
        _cg.addMethod(method.getMethod());
        il.dispose();
    }

    private void createMethod_2() {
        InstructionList il = new InstructionList();
        MethodGen method = new MethodGen(ACC_PUBLIC | ACC_STATIC, new ArrayType(new ObjectType(enumType), 1), Type.NO_ARGS, new String[] {  }, "values", enumType, il, _cp);

        InstructionHandle ih_0 = il.append(_factory.createFieldAccess(enumType, "ENUM$VALUES", new ArrayType(new ObjectType(enumType), 1), Constants.GETSTATIC));
        il.append(InstructionConstants.DUP);
        il.append(_factory.createStore(Type.OBJECT, 0));
        il.append(new PUSH(_cp, 0));
        il.append(_factory.createLoad(Type.OBJECT, 0));
        il.append(InstructionConstants.ARRAYLENGTH);
        il.append(InstructionConstants.DUP);
        il.append(_factory.createStore(Type.INT, 1));
        il.append(_factory.createNewArray(new ObjectType(enumType), (short) 1));
        il.append(InstructionConstants.DUP);
        il.append(_factory.createStore(Type.OBJECT, 2));
        il.append(new PUSH(_cp, 0));
        il.append(_factory.createLoad(Type.INT, 1));
        il.append(_factory.createInvoke("java.lang.System", "arraycopy", Type.VOID, new Type[] { Type.OBJECT, Type.INT, Type.OBJECT, Type.INT, Type.INT }, Constants.INVOKESTATIC));
        il.append(_factory.createLoad(Type.OBJECT, 2));
        il.append(_factory.createReturn(Type.OBJECT));
        method.setMaxStack();
        method.setMaxLocals();
        _cg.addMethod(method.getMethod());
        il.dispose();
    }

    private void createMethod_3() {
        InstructionList il = new InstructionList();
        MethodGen method = new MethodGen(ACC_PUBLIC | ACC_STATIC, new ObjectType(enumType), new Type[] { Type.STRING }, new String[] { "arg0" }, "valueOf", enumType, il, _cp);

        InstructionHandle ih_0 = il.append(new LDC(4));
        il.append(_factory.createLoad(Type.OBJECT, 0));
        il.append(_factory.createInvoke("java.lang.Enum", "valueOf", new ObjectType("java.lang.Enum"), new Type[] { new ObjectType("java.lang.Class"), Type.STRING }, Constants.INVOKESTATIC));
        il.append(_factory.createCheckCast(new ObjectType(enumType)));
        InstructionHandle ih_9 = il.append(_factory.createReturn(Type.OBJECT));
        method.setMaxStack();
        method.setMaxLocals();
        _cg.addMethod(method.getMethod());
        il.dispose();
    }

}
