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
package es.logongas.ix3.web.businessprocess.impl;

import es.logongas.ix3.web.security.WebCredentialFactory;
import es.logongas.ix3.core.BusinessException;
import es.logongas.ix3.security.authentication.AuthenticationManager;
import es.logongas.ix3.security.authentication.Credential;
import es.logongas.ix3.core.Principal;
import es.logongas.ix3.web.security.WebSessionSidStorage;
import es.logongas.ix3.web.businessprocess.WebSessionBusinessProcess;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author logongas
 */
public class WebSessionBusinessProcessImpl implements WebSessionBusinessProcess {

    @Autowired
    WebSessionSidStorage webSessionSidStorage;

    @Autowired
    AuthenticationManager authenticationManager;
    
    @Autowired
    WebCredentialFactory webCredentialFactory;
    
    private Log log = LogFactory.getLog(WebSessionBusinessProcessImpl.class);
    
    @Override
    final public Principal createWebSession(CreateWebSessionArguments createWebSessionArguments) throws BusinessException {
        try {
            createWebSessionArguments.httpServletRequest.setCharacterEncoding("UTF-8");

            Credential credental=webCredentialFactory.getCredential(createWebSessionArguments.httpServletRequest, createWebSessionArguments.httpServletResponse);
            Principal principal = authenticationManager.authenticate(credental, createWebSessionArguments.dataSession);
            if (principal == null) {
                if (log.isInfoEnabled()) {
                    log.info("Login fallido con la credencial:" + credental);
                }
                throw new BusinessException("El usuario o contraseña no son válidos");
            }
            webSessionSidStorage.setSid(createWebSessionArguments.httpServletRequest, createWebSessionArguments.httpServletResponse, principal.getSid());

            if (log.isInfoEnabled()) {
                log.info("Login usuario:" + principal.getName());
            }
            
            return principal;
        } catch (BusinessException ex) {
            throw ex;
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }

    
    @Override
    final public void deleteCurrentWebSession(DeleteCurrentWebSessionArguments deleteCurrentWebSessionArguments) throws BusinessException {
        webSessionSidStorage.deleteSid(deleteCurrentWebSessionArguments.httpServletRequest,deleteCurrentWebSessionArguments.httpServletResponse);
    }

    @Override
    final public Principal getCurrentWebSession(GetCurrentWebSessionArguments getCurrentWebSessionArguments) throws BusinessException {
        Serializable sid =  webSessionSidStorage.getSid(getCurrentWebSessionArguments.httpServletRequest,getCurrentWebSessionArguments.httpServletResponse);
        
        if (sid == null) {
            return null;
        }

        Principal principal = authenticationManager.getPrincipalBySID(sid, getCurrentWebSessionArguments.dataSession);

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
