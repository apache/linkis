/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.apache.linkis.rpc.serializer;

import io.protostuff.*;
import io.protostuff.runtime.Delegate;
import scala.Option;

import java.io.IOException;


public class NoneDelegate implements Delegate<Option> {
    @Override
    public WireFormat.FieldType getFieldType() {
        return WireFormat.FieldType.UINT32;
    }

    @Override
    public Option readFrom(Input input) throws IOException {
        throw new ProtostuffException("Corrupt input. option cannot serialize");
    }

    @Override
    public void writeTo(Output output, int i, Option option, boolean b) throws IOException {
        throw new ProtostuffException("Corrupt input. option cannot serialize");
    }

    @Override
    public void transfer(Pipe pipe, Input input, Output output, int i, boolean b) throws IOException {
        throw new ProtostuffException("Corrupt input. option cannot serialize");
    }

    @Override
    public Class<?> typeClass() {
        return Option.class;
    }

    /*@Override
    public WireFormat.FieldType getFieldType() {
        return WireFormat.FieldType.UINT32;
    }

    @Override
    public None$ readFrom(Input input) throws IOException {
        if(0 != input.readUInt32())
            throw new ProtostuffException("Corrupt input.");

        return None$.MODULE$;
    }

    @Override
    public void writeTo(Output output, int number, None$ none$, boolean repeated) throws IOException {
        output.writeFixed64(number, 0, repeated);
    }

    @Override
    public void transfer(Pipe pipe, Input input, Output output, int number, boolean repeated) throws IOException {
        output.writeUInt32(number, input.readUInt32(), repeated);
    }

    @Override
    public Class<?> typeClass() {
        return None$.class;
    }*/
}
