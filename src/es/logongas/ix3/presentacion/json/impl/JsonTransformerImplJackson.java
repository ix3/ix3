/*
 * Copyright 2013 Lorenzo González.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package es.logongas.ix3.presentacion.json.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.logongas.ix3.presentacion.json.JsonTransformer;

/**
 *
 * @author Lorenzo González
 */
public class JsonTransformerImplJackson<T> implements JsonTransformer<T> {

    Class<T> clazz;
    ObjectMapper objectMapper = new ObjectMapper();
    
    public JsonTransformerImplJackson(Class<T> clazz) {
        this.clazz=clazz;
    }

    
    @Override
    public String toJson(T data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public T fromJson(String data) {
        try {
            return (T)objectMapper.readValue(data, clazz);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    
}
