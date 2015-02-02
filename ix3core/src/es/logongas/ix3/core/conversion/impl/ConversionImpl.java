/*
 * Copyright 2015 Lorenzo González.
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
package es.logongas.ix3.core.conversion.impl;

import es.logongas.ix3.core.conversion.Conversion;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author logongas
 */
public class ConversionImpl implements Conversion {

    @Override
    public Object convertFromString(String value, Class type) {
        try {
            if ((value == null) || (value.trim().isEmpty())) {
                return null;
            }

            if ((type == Boolean.TYPE) || (type == Boolean.class)) {
                return getBooleanFromObject(value);
            }
            if ((type == Byte.TYPE) || (type == Byte.class)) {
                return new Byte(value);
            }
            if ((type == Integer.TYPE) || (type == Integer.class)) {
                return new Integer(value);
            }
            if ((type == Short.TYPE) || (type == Short.class)) {
                return new Short(value);
            }
            if ((type == Long.TYPE) || (type == Long.class)) {
                return new Long(value);
            }
            if ((type == Float.TYPE) || (type == Float.class)) {
                return new Float(value);
            }
            if ((type == Double.TYPE) || (type == Double.class)) {
                return new Double(value);
            }
            if (type == BigDecimal.class) {
                return new BigDecimal(value);
            }
            if (type == String.class) {
                return value;
            }
            if (type == Date.class) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

                try {
                    return simpleDateFormat.parse(value);
                } catch (ParseException ex) {
                    throw new RuntimeException("El formato de la fecha '" + value + "' no es correcto");
                }
            }

        } catch (RuntimeException rex) {
            throw rex;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        //Si llegamos hasta aqui es que el tipo de datos no es uno valido
        throw new RuntimeException("Tipo de datos desconocido:" + type.getName());

    }

    /**
     * Transforma cualquier objeto en un booleano
     *
     * @param bol Objeto a transformar en booleano
     * @return Valor booleano al transformar el objeto de entrada Si el valor no
     * se puede transformar retorna una excepcion
     */
    private boolean getBooleanFromObject(Object bol) {
        if (bol != null) {
            if (bol.getClass() == Boolean.class) {
                return (Boolean) bol;
            }

            if (bol.getClass() == String.class) {
                String s = (String) bol;
                s = s.trim().toUpperCase();
                if ((s.equals("S") == true)
                        || (s.equals("SI") == true)
                        || (s.equals("YES") == true)
                        || (s.equals("Y") == true)
                        || (s.equals("VERDADERO") == true)
                        || (s.equals("V") == true)
                        || (s.equals("TRUE") == true)
                        || (s.equals("T") == true)
                        || (s.equals("-1") == true)
                        || (s.equals("1") == true)) {
                    return true;
                } else {
                    return false;
                }
            }

            if (bol.getClass() == Long.class) {
                return (((Long) bol) != 0 ? true : false);
            }

            if (bol.getClass() == Integer.class) {
                return (((Integer) bol) != 0 ? true : false);
            }

            if (bol.getClass() == Short.class) {
                return (((Short) bol) != 0 ? true : false);
            }

            if (bol.getClass() == Byte.class) {
                return (((Byte) bol) != 0 ? true : false);
            }

            if (bol.getClass() == BigDecimal.class) {
                BigDecimal cero = new BigDecimal("0");
                if (cero.equals(bol)) {
                    return false;
                } else {
                    return true;
                }
            }

            throw new RuntimeException("El tipo de datos no se puede transformar en booleano:" + bol.getClass().getName());
        } else {
            return false;
        }
    }

}
