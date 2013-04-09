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
package es.logongas.ix3.web.impl.json;

import es.logongas.ix3.persistence.services.metadata.MetaData;
import es.logongas.ix3.persistence.services.metadata.MetaDataFactory;
import es.logongas.ix3.web.services.json.JsonFactory;
import es.logongas.ix3.web.services.json.JsonReader;
import es.logongas.ix3.web.services.json.JsonWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

/**
 *
 * @author Lorenzo González
 */
public class JsonFactoryImpl implements JsonFactory {

    @Autowired
    private ApplicationContext context;
    @Autowired
    private MetaDataFactory metaDataFactory;

    @Override
    public JsonReader getJsonReader(Class clazz) {
        MetaData metaData = metaDataFactory.getMetaData(clazz);
        JsonReader jsonReader;

        if (metaData != null) {
            jsonReader = new JsonReaderImplEntityJackson(clazz);
        } else {
            jsonReader = new JsonReaderImplJackson(clazz);
        }

        context.getAutowireCapableBeanFactory().autowireBean(jsonReader);

        return jsonReader;
    }

    @Override
    public JsonWriter getJsonWriter(Class clazz) {
        JsonWriter jsonWriter;

        if (clazz == null) {
            jsonWriter = new JsonWriterImplJackson();
        } else {
            MetaData metaData = metaDataFactory.getMetaData(clazz);
            if (metaData != null) {
                jsonWriter = new JsonWriterImplEntityJackson();
            } else {
                jsonWriter = new JsonWriterImplJackson();
            }
        }

        context.getAutowireCapableBeanFactory().autowireBean(jsonWriter);

        return jsonWriter;
    }

    @Override
    public JsonWriter getJsonWriter() {
        JsonWriter jsonWriter = new JsonWriterImplJackson();

        context.getAutowireCapableBeanFactory().autowireBean(jsonWriter);

        return jsonWriter;
    }
}
