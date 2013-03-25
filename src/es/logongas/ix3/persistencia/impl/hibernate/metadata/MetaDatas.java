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
package es.logongas.ix3.persistencia.impl.hibernate.metadata;

import es.logongas.ix3.persistencia.services.metadata.MetaData;
import java.util.LinkedHashMap;




/**
 *
 * @author Lorenzo González
 */
public class MetaDatas extends LinkedHashMap<String,MetaData> {

    @Override
    public MetaData get(Object key) {
        String propertyName=(String)key;

        if (propertyName==null) {
            return null;
        }

        if (propertyName.indexOf(".")==0) {
            return super.get(propertyName);
        } else {
           //Si tiene "." puntos hay que separarlo en trozos.
            String[] propertyNames=propertyName.split("\\.");

            MetaData propertyMetaData=super.get(propertyNames[0]);

            if (propertyMetaData!=null) {
                for(int i=1;i<propertyNames.length;i++) {
                    propertyMetaData=propertyMetaData.getPropertiesMetaData().get(propertyNames[i]);
                    if (propertyMetaData==null) {
                        return null;
                    }
                }

                return propertyMetaData;
            } else {
                //El primer elemento ya no es una propiedad
                return null;
            }

        }
    }
}
