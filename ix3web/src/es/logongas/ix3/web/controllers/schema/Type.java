/*
 * Copyright 2013 Lorenzo.
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

package es.logongas.ix3.web.controllers.schema;

import java.math.BigDecimal;
import java.util.Date;

/**
 *
 * @author Lorenzo
 */
public enum Type {
    OBJECT,
    STRING,
    INTEGER,
    NUMBER,
    BOOLEAN,
    DATE;
    
    public static Type getTypeFromClass(Class clazz) {
        Type type;
        
        if (clazz==null) {
            throw new RuntimeException("El argumento clazz no puede ser null");
        }
        
        if (String.class.isAssignableFrom(clazz)) {
            type=Type.STRING;
        } else if (Byte.class.isAssignableFrom(clazz) || Short.class.isAssignableFrom(clazz) || Integer.class.isAssignableFrom(clazz) || Long.class.isAssignableFrom(clazz) || byte.class.isAssignableFrom(clazz) || short.class.isAssignableFrom(clazz) || int.class.isAssignableFrom(clazz)  || long.class.isAssignableFrom(clazz) ) {
            type=Type.INTEGER;
        } else if (Number.class.isAssignableFrom(clazz) || BigDecimal.class.isAssignableFrom(clazz) || Float.class.isAssignableFrom(clazz) || Double.class.isAssignableFrom(clazz)  || float.class.isAssignableFrom(clazz) || double.class.isAssignableFrom(clazz)) {
            type=Type.NUMBER;
        } else if (Boolean.class.isAssignableFrom(clazz) || boolean.class.isAssignableFrom(clazz)) {
            type=Type.BOOLEAN;
        } else if (Date.class.isAssignableFrom(clazz)) {
            type=Type.DATE;
        } else if (clazz.isEnum()) {
            //Los Enumerados se generaran como un String
            type=Type.STRING;            
        } else {
           type=Type.OBJECT;
        }
        
        return type;
    }
    
}
