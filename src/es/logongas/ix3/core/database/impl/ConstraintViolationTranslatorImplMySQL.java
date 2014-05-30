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
package es.logongas.ix3.core.database.impl;

import es.logongas.ix3.core.database.ConstraintViolation;
import es.logongas.ix3.core.database.ConstraintViolationTranslator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Transforma ciertos errores de MySQL en mensajes para el usuario
 * ver http://dev.mysql.com/doc/refman/5.5/en/error-messages-server.html
 * @author Lorenzo González
 */
public class ConstraintViolationTranslatorImplMySQL implements ConstraintViolationTranslator {

    @Override
    public ConstraintViolation translate(String message, int errorCode, String sqlState) {
        if ((errorCode == 1062) && (sqlState.equals("23000"))) {
            Pattern pattern = Pattern.compile("Duplicate entry '(.*)' for key '(.*)'");

            Matcher matcher = pattern.matcher(message);
            if (matcher.matches()) {
                String value =matcher.group(1);
                String propertyName = matcher.group(2);

                return new ConstraintViolation(propertyName, value,ConstraintViolation.Type.DuplicateEntry);
            } else {
                //Si el mensaje no tiene el formato que conocemos no pasa nada
                //retornamos el propio mensaje.
                return new ConstraintViolation(null,message);
            }
        } else {
            //No sabemos nada de ese error así que debe no retornamos nada
            return null;
        }
    }
}
