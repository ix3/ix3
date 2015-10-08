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

import es.logongas.ix3.core.BusinessException;
import es.logongas.ix3.security.authentication.AuthenticationManager;
import es.logongas.ix3.security.authentication.Credential;
import es.logongas.ix3.security.authentication.Principal;
import es.logongas.ix3.security.util.WebSessionSidStorage;
import es.logongas.ix3.web.service.WebSessionService;
import java.io.Serializable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author logongas
 */
public class WebSessionServiceImpl implements WebSessionService {

    @Autowired
    WebSessionSidStorage webSessionSidStorage;

    @Autowired
    AuthenticationManager authenticationManager;
    
    @Autowired
    WebCredentialFactory webCredentialFactory;

    @Override
    final public Principal createWebSession(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws BusinessException {
        try {
            httpServletRequest.setCharacterEncoding("UTF-8");

            Credential credental=webCredentialFactory.getCredential(httpServletRequest, httpServletResponse);
            Principal principal = authenticationManager.authenticate(credental);
            if (principal == null) {
                throw new BusinessException("El usuario o contraseña no son válidos");
            }
            webSessionSidStorage.setSid(httpServletRequest, httpServletResponse, principal.getSid());

            return principal;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    
    @Override
    final public void deleteCurrentWebSession(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws BusinessException {
        webSessionSidStorage.deleteSid(httpServletRequest,httpServletResponse);
    }

    @Override
    final public Principal getCurrentWebSession(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws BusinessException {
        Serializable sid =  webSessionSidStorage.getSid(httpServletRequest,httpServletResponse);
        
        if (sid == null) {
            return null;
        }

        Principal principal = authenticationManager.getPrincipalBySID(sid);

        return principal;
    }

    @Override
    final public void setEntityType(Class t) {
        throw new UnsupportedOperationException("No se puede cambiar la entidad");
    }

    @Override
    final public Class getEntityType() {
        return null;
    }

}
