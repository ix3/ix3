/*
 * Copyright 2015 logongas.
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

import es.logongas.ix3.web.security.jwt.Jws;
import java.math.BigInteger;
import java.security.Key;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Random;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.keys.AesKey;
import org.jose4j.lang.JoseException;

/**
 * Clase de utilidad para gestionar los tokens Jws firmados
 *
 * @author logongas
 */
public class JwsImplJose4j implements Jws {

    private static final String PAYLOAD_PARTS_SEPARATOR=";";
    private static final String CREATE_DATE_FORMAT="yyyy-MM-dd'T'HH:mm:ss";
    
    
    @Override
    public String getJwsCompactSerialization(String payload, byte[] secretKey) {
        try {
            Key key = new AesKey(secretKey);
            String structuredPayload=getStructuredPayLoadFromPayload(payload);
            
            JsonWebSignature jws = new JsonWebSignature();
            jws.setPayload(structuredPayload);
            jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.HMAC_SHA256);
            jws.setKey(key);
            String jwsCompactSerialization = jws.getCompactSerialization();

            return jwsCompactSerialization;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public boolean verifyJwsCompactSerialization(String jwsCompactSerialization, byte[] secretKey,int maxMinutesValid) {
        try {        
            Key key = new AesKey(secretKey);
            JsonWebSignature jws = new JsonWebSignature();
            jws.setCompactSerialization(jwsCompactSerialization);
            jws.setKey(key);

            boolean signatureVerified = jws.verifySignature();
            
            if (signatureVerified==false) {
                return false;
            }
            
            Date creationDate=getDateFromStructuredPayload(jws.getUnverifiedPayload());
            Date maxDate=dateAddMinutes(creationDate,maxMinutesValid);
            Date now=new Date();
            
            if (now.after(maxDate)) {
                //Ha caducado el Token
                return false;
            }
            
            return true;
            
        } catch (JoseException ex) {
            throw new RuntimeException(ex);
        }            
    }

    @Override
    public String getUnverifiedPayloadFromJwsCompactSerialization(String jwsCompactSerialization) {
        try {        
            JsonWebSignature jws = new JsonWebSignature();
            jws.setCompactSerialization(jwsCompactSerialization);

            String structuredPayload = jws.getUnverifiedPayload();
            
            return getPayloadFromStructuredPayload(structuredPayload);
            
        } catch (JoseException ex) {
            throw new RuntimeException(ex);
        }
    }


    /**
     * Le añade al payload la fecha de creación y un número aleatorio
     * @param payload
     * @return 
     */
    String getStructuredPayLoadFromPayload(String payload) {
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
    
    private String getPayloadFromStructuredPayload(String structuredPayload) {
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
    private Date getDateFromStructuredPayload(String structuredPayload) {
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
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }    
            
    
    
    private String getLongRandomNumber(int size) {
        Random random = new Random();
        byte[] b = new byte[size];
        random.nextBytes(b);
        BigInteger bigInteger = (new BigInteger(b)).abs();

        return bigInteger.toString();
    }
    
    private Date dateAddMinutes(Date date ,int minutes) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MINUTE, minutes);
        Date result = calendar.getTime();

        return result;      
    }
    
}
