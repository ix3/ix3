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

import es.logongas.ix3.web.security.jwt.Jwe;
import java.security.Key;
import java.util.Date;
import org.jose4j.jwe.ContentEncryptionAlgorithmIdentifiers;
import org.jose4j.jwe.JsonWebEncryption;
import org.jose4j.jwe.KeyManagementAlgorithmIdentifiers;
import org.jose4j.keys.AesKey;
import org.jose4j.lang.JoseException;

/**
 *
 * @author logongas
 */
public class JweImplJose4j implements Jwe {


    @Override
    public String getJwsCompactSerialization(String payload, byte[] secretKey) {
        String structuredPayload = JwUtil.getStructuredPayLoadFromPayload(payload);

        String encryptedJwsCompactSerialization = encryptData(structuredPayload, secretKey);

        return encryptedJwsCompactSerialization;
    }

    @Override
    public String getPayloadFromJwsCompactSerialization(String encryptedJwsCompactSerialization, byte[] secretKey, int maxMinutesValid) {
        String structuredPayload = decryptData(encryptedJwsCompactSerialization, secretKey);

        Date creationDate = JwUtil.getDateFromStructuredPayload(structuredPayload);
        Date maxDate = JwUtil.dateAddMinutes(creationDate, maxMinutesValid);
        Date now = new Date();

        if (now.after(maxDate)) {
            //Ha caducado el Token
            throw new RuntimeException("El token no es válido:" + encryptedJwsCompactSerialization);
        }

        return JwUtil.getPayloadFromStructuredPayload(structuredPayload);
    }

    private String encryptData(String plainData, byte[] secretKey) {
        try {
            JsonWebEncryption jwe = new JsonWebEncryption();
            jwe.setEncryptionMethodHeaderParameter(ContentEncryptionAlgorithmIdentifiers.AES_128_CBC_HMAC_SHA_256);
            jwe.setAlgorithmHeaderValue(KeyManagementAlgorithmIdentifiers.A128KW);
            Key key = new AesKey(secretKey);
            jwe.setKey(key);

            jwe.setPlaintext(plainData);
            String encryptedData = jwe.getCompactSerialization();

            return encryptedData;
        } catch (JoseException ex) {
            throw new RuntimeException(ex);
        }
    }

    private String decryptData(String encryptedData, byte[] secretKey) {
        try {
            JsonWebEncryption jwe = new JsonWebEncryption();
            jwe.setEncryptionMethodHeaderParameter(ContentEncryptionAlgorithmIdentifiers.AES_128_CBC_HMAC_SHA_256);
            jwe.setAlgorithmHeaderValue(KeyManagementAlgorithmIdentifiers.A128KW);
            Key key = new AesKey(secretKey);
            jwe.setKey(key);

            jwe.setCompactSerialization(encryptedData);
            String plainData = jwe.getPlaintextString();

            return plainData;
        } catch (JoseException ex) {
            throw new RuntimeException(ex);
        }
    }



}
