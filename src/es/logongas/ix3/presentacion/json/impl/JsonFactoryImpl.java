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

import es.logongas.ix3.presentacion.json.JsonFactory;
import es.logongas.ix3.presentacion.json.JsonReader;
import es.logongas.ix3.presentacion.json.JsonWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

/**
 *
 * @author Lorenzo González
 */
public class JsonFactoryImpl implements JsonFactory {
    @Autowired
    private ApplicationContext context;
    
    @Override
    public JsonReader getJsonReader(Class clazz) {
        JsonReader jsonReader=new JsonReaderImplJackson(clazz);
        
        context.getAutowireCapableBeanFactory().autowireBean(jsonReader);
        
        return jsonReader;
    }

    @Override
    public JsonWriter getJsonWriter(Class clazz) {
        JsonWriter jsonWriter=new JsonWriterImplJackson(clazz);
        
        context.getAutowireCapableBeanFactory().autowireBean(jsonWriter);
        
        return jsonWriter; 
    }
    
    @Override
    public JsonWriter getJsonWriter() {
        JsonWriter jsonWriter=new JsonWriterImplJackson(null);
        
        context.getAutowireCapableBeanFactory().autowireBean(jsonWriter);
        
        return jsonWriter; 
    }    
    
}
