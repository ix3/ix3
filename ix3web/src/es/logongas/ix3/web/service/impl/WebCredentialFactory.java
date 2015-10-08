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
package es.logongas.ix3.web.service.impl;

import es.logongas.ix3.security.authentication.Credential;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Obtener las credenciales de un usuario a partir de los datos que vienen de la web
 * @author logongas
 */
public interface WebCredentialFactory {
    
    /**
     * Obtener las credenciales de un usuario a partir de los datos que vienen de la web
     * @param httpServletRequest
     * @param httpServletResponse
     * @return Las credenciales que presenta el usuario para identificarse
     */
    Credential getCredential(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse);
    
}
