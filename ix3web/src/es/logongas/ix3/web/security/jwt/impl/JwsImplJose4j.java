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

            JsonWebSignature jws = new JsonWebSignature();
            jws.setPayload(payload);
            jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.HMAC_SHA256);
            jws.setKey(key);
            String jwsCompactSerialization = jws.getCompactSerialization();

            return jwsCompactSerialization;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public boolean verifyJwsCompactSerialization(String jwsCompactSerialization, byte[] secretKey) {
        try {        
            Key key = new AesKey(secretKey);
            JsonWebSignature jws = new JsonWebSignature();
            jws.setCompactSerialization(jwsCompactSerialization);
            jws.setKey(key);

            boolean signatureVerified = jws.verifySignature();
            
            return signatureVerified;
            
        } catch (JoseException ex) {
            throw new RuntimeException(ex);
        }            
    }

    @Override
    public String getUnverifiedPayloadFromJwsCompactSerialization(String jwsCompactSerialization) {
        try {        
            JsonWebSignature jws = new JsonWebSignature();
            jws.setCompactSerialization(jwsCompactSerialization);

            String unverifiedPayload = jws.getUnverifiedPayload();
            
            return unverifiedPayload;
            
        } catch (JoseException ex) {
            throw new RuntimeException(ex);
        }
    }


}
