/*
 * Copyright (c) 2018 - 2023, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.entgra.device.mgt.core.certificate.mgt.core.util;

import java.io.*;

/**
 * Serialize/deserialize  a given object to and from a byte array.
 */
public class Serializer {

    /**
     *  Serialize a given object to a byte array.
     * @param object object to be deserialized.
     * @return byte array representing the object.
     * @throws IOException
     */
    public static byte[] serialize(Object object) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        if(objectOutputStream != null) {
            objectOutputStream.writeObject(object);
            objectOutputStream.flush();
            objectOutputStream.close();
        }
        if(byteArrayOutputStream != null) {
            byteArrayOutputStream.close();
        }
        return byteArrayOutputStream.toByteArray();
    }

    /**
     * Deserialize a given object from a byte array.
     * @param bytes array of bytes that makes up an object.
     * @return resulted object after deserializing.
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
        if(byteArrayInputStream != null) {
            byteArrayInputStream.close();
        }
        return objectInputStream.readObject();
    }
}
