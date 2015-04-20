/**
 * ix3 Copyright (C) 2015 Lorenzo González
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
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
