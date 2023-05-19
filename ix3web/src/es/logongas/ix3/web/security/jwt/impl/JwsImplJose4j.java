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
import java.security.Key;
import java.util.Date;
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
   
    @Override
    public String getJwsCompactSerialization(String payload, byte[] secretKey) {
        try {
            Key key = new AesKey(secretKey);
            String structuredPayload=JwUtil.getStructuredPayLoadFromPayload(payload);
            
            JsonWebSignature jws = new JsonWebSignature();
            jws.setPayload(structuredPayload);
            jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.HMAC_SHA256);
            jws.setKey(key);
            String jwsCompactSerialization = jws.getCompactSerialization();

            return jwsCompactSerialization;
        } catch (JoseException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public boolean verifyJwsCompactSerialization(String jwsCompactSerialization, byte[] secretKey,int maxMinutesValid) {
        try {        
            JsonWebSignature jws = new JsonWebSignature();
            jws.setCompactSerialization(jwsCompactSerialization);
            
            
            Key key = new AesKey(secretKey);
            jws.setKey(key);
            boolean signatureVerified = jws.verifySignature();
            
            if (signatureVerified==false) {
                return false;
            }
            
            
            String structuredPayload = jws.getUnverifiedPayload();
            Date creationDate=JwUtil.getDateFromStructuredPayload(structuredPayload);
            Date maxDate=JwUtil.dateAddMinutes(creationDate,maxMinutesValid);
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
            String payload=JwUtil.getPayloadFromStructuredPayload(structuredPayload);
            
            return payload;
            
        } catch (JoseException ex) {
            throw new RuntimeException(ex);
        }
    }

 

    
}
