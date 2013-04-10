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
package es.logongas.ix3.persistence.services.dao.database;

import java.util.HashMap;
import java.util.Map;

/**
 * Restriccion que se ha violado en la base de datos.
 * @author Lorenzo González
 */
public class ConstraintViolation {
    private String propertyName;
    private String message;

    public ConstraintViolation(String propertyName, String value, Type constraintViolationType) {
        this.propertyName = propertyName;
        this.message=createMessage(value, constraintViolationType);
    }
    public ConstraintViolation(String propertyName,String message) {
        this.propertyName = propertyName;
        this.message=message;
    }

    /**
     * La propiedad donde ha ocurrido el error
     * @return
     */
    public String getPropertyName() {
        return propertyName;
    }

    /**
     * Mensaje del error
     * @return
     */
    public String getMessage() {
        return message;
    }


    private String createMessage(String value, Type constraintViolationType) {
        Map<Type,String> messageTemplates=new HashMap<Type,String>();
        messageTemplates.put(Type.DuplicateEntry, "El valor '%s' ya existe");

       return String.format(messageTemplates.get(constraintViolationType), value);
    }

    /**
     * Tipos de restricciones que se comprueban.
     */
    public enum Type {
        DuplicateEntry
    }

}
