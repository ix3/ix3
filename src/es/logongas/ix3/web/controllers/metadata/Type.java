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

package es.logongas.ix3.web.controllers.metadata;

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
    DATE,
    DATETIME;
    
    public static Type getTypeFromClass(Class clazz) {
        Type type;
        
        if (clazz==null) {
            throw new RuntimeException("El argumento clazz no puede ser null");
        }
        
        if (clazz.isAssignableFrom(String.class)) {
            type=Type.STRING;
        } else if (clazz.isAssignableFrom(Byte.class) || clazz.isAssignableFrom(Short.class) || clazz.isAssignableFrom(Integer.class) || clazz.isAssignableFrom(Long.class) || clazz.isAssignableFrom(byte.class) || clazz.isAssignableFrom(short.class) || clazz.isAssignableFrom(int.class)  || clazz.isAssignableFrom(long.class) ) {
            type=Type.INTEGER;
        } else if (clazz.isAssignableFrom(BigDecimal.class) || clazz.isAssignableFrom(Float.class) || clazz.isAssignableFrom(Short.class) || clazz.isAssignableFrom(Long.class) || clazz.isAssignableFrom(float.class) || clazz.isAssignableFrom(double.class)) {
            type=Type.NUMBER;
        } else if (clazz.isAssignableFrom(Boolean.class) || clazz.isAssignableFrom(boolean.class)) {
            type=Type.BOOLEAN;
        } else if (clazz.isAssignableFrom(Date.class)) {
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
