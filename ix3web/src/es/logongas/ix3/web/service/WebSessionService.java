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
package es.logongas.ix3.web.service;

import es.logongas.ix3.core.BusinessException;
import es.logongas.ix3.security.authentication.Principal;
import es.logongas.ix3.service.Service;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Gestión de la sesión del usuario en la capa de Web
 * @author logongas
 */
public interface WebSessionService extends Service {
    
    Principal createWebSession(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws BusinessException;
    void deleteCurrentWebSession(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws BusinessException;
    Principal getCurrentWebSession(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws BusinessException;
    
}
