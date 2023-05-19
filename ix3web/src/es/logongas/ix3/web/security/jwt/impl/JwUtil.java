/*
 * Copyright 2023 logongas.
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
package es.logongas.ix3.web.security.jwt.impl;

import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Random;

/**
 *
 * @author logongas
 */
public class JwUtil {
    
    private static final String PAYLOAD_PARTS_SEPARATOR=";";
    private static final String CREATE_DATE_FORMAT="yyyy-MM-dd'T'HH:mm:ss";
    
    /**
     * Le añade al payload la fecha de creación y un número aleatorio
     * @param payload
     * @return 
     */
    public static String getStructuredPayLoadFromPayload(String payload) {
        if ((payload==null) || (payload.isEmpty())) {
            throw new RuntimeException("El payload no puede estar vacio");
        }

        if (payload.contains(PAYLOAD_PARTS_SEPARATOR)) {
            throw new RuntimeException("El payload no puede contener el caracter '" + PAYLOAD_PARTS_SEPARATOR + "'");
        }
        
        
        Date date=new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(CREATE_DATE_FORMAT);

        
        String structuredPayLoad=payload + PAYLOAD_PARTS_SEPARATOR + simpleDateFormat.format(date) + PAYLOAD_PARTS_SEPARATOR + getLongRandomNumber(5);
        
        return structuredPayLoad;
        
    }
    
    public static String getPayloadFromStructuredPayload(String structuredPayload) {
        String payload;
        
        if ((structuredPayload==null) || (structuredPayload.isEmpty())) {
            throw new RuntimeException("El structuredPayload no puede estar vacio");
        }
       
        if (structuredPayload.contains(PAYLOAD_PARTS_SEPARATOR)==true) {
            payload=structuredPayload.split(PAYLOAD_PARTS_SEPARATOR)[0];
        } else {
            //Es un viejo payload que no tenía la fecha de creación, asi que "todo" es el payload
            payload=structuredPayload;
        }
        
        return payload;
    }
    public static Date getDateFromStructuredPayload(String structuredPayload) {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(CREATE_DATE_FORMAT);
            Date date;
            
            if ((structuredPayload==null) || (structuredPayload.isEmpty())) {
                throw new RuntimeException("El payload no puede estar vacio");
            }

            if (structuredPayload.contains(PAYLOAD_PARTS_SEPARATOR)==true) {
                date=simpleDateFormat.parse(structuredPayload.split(PAYLOAD_PARTS_SEPARATOR)[1]);
            } else {
                //Es un viejo payload que no tenía la fecha de creación
                //Así que ponermos una fecha muy vieja para que seguro que esté caducado
                GregorianCalendar gregorianCalendar=new GregorianCalendar(1793, 1, 1, 1, 1, 1);
                date=gregorianCalendar.getTime();                
            }
            
            return date;
        } catch (ParseException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    
    public static Date dateAddMinutes(Date date ,int minutes) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MINUTE, minutes);
        Date result = calendar.getTime();

        return result;      
    }
    
    private static String getLongRandomNumber(int size) {
        Random random = new Random();
        byte[] b = new byte[size];
        random.nextBytes(b);
        BigInteger bigInteger = (new BigInteger(b)).abs();

        return bigInteger.toString();
    }
}
