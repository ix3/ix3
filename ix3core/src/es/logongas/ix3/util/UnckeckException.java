/*
 * Copyright 2012 Lorenzo González.
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
package es.logongas.ix3.util;

/**
 * Permite lanzar excepciones Checked en métodos que no lo permiten
 * @author logongas
 */
public class UnckeckException {

    /**
     * Lanza una excepción checked desde un método que no lo permite
     * @param ex 
     */
    public static void throwCkeckedExceptionAsUnckeckedException(Exception ex) {
        UnckeckException.<RuntimeException>throwAsUnckeckedException(ex);

        throw new AssertionError("Esta línea  nunca se ejecutará pero Java no lo sabe");
    }

    private static <T extends Exception> void throwAsUnckeckedException(Exception toThrow) throws T {
        throw (T) toThrow;
    }

}
