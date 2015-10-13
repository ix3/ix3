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
package es.logongas.ix3.web.security;

import java.io.Serializable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Gestiona como se almacena la sesión en el servidor. Almacenamiento del Sid en la sesion web.
 * Lo normal es usar cookies o Jws ,etc.
 * @author logongas
 */
public interface WebSessionSidStorage {
    
    /**
     * Guarda en la capa Web, el Secure ID (SID) de un usuario .
     * @param httpServletRequest
     * @param httpServletResponse
     * @param sid El SID a guardar
     */
    void setSid(HttpServletRequest httpServletRequest,HttpServletResponse httpServletResponse,Serializable sid);
    /**
     * Borrar en la capa Web, el Secure ID (SID) de un usuario .
     * @param httpServletRequest
     * @param httpServletResponse 
     */
    void deleteSid(HttpServletRequest httpServletRequest,HttpServletResponse httpServletResponse);
    /**
     * Obtiene en la capa Web, el Secure ID (SID) de un usuario .
     * @param httpServletRequest
     * @param httpServletResponse
     * @return 
     */
    Serializable getSid(HttpServletRequest httpServletRequest,HttpServletResponse httpServletResponse);    
    
}
