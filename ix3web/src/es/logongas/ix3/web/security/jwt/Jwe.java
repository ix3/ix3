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
package es.logongas.ix3.web.security.jwt;

/**
 *
 * @author logongas
 */
public interface Jwe {
    /**
     * Crea un nuevo token Jws
     * @param payload La información del token
     * @param secretKey La clave
     * @return Un String en base64 separado por puntos con el token Jws
     */
    String getJwsCompactSerialization(String payload, byte[] secretKey);

    /**
     * Obtiene el copntenido del token Jwe 
     * @param jwsCompactSerialization El token Jws en base64
     * @param secretKey La clave
     * @param maxMinutesValid Los minutos que es válido el token
     * @return El contenido del Token
     * @throws es.logongas.ix3.web.security.jwt.TokenExpiredException
     */
    String getPayloadFromJwsCompactSerialization(String jwsCompactSerialization, byte[] secretKey,int maxMinutesValid) throws TokenExpiredException;
}
