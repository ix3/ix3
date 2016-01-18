/*
 * Copyright 2013 Lorenzo González.
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
package es.logongas.ix3.security.authentication.impl;

import es.logongas.ix3.core.BusinessException;
import es.logongas.ix3.dao.DataSession;
import es.logongas.ix3.security.authentication.AuthenticationManager;
import es.logongas.ix3.security.authentication.AuthenticationProvider;
import es.logongas.ix3.security.authentication.Credential;
import es.logongas.ix3.core.Principal;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Lorenzo González
 */
public class AuthenticationManagerImpl implements AuthenticationManager {

    private List<AuthenticationProvider> authenticationProviders=new ArrayList<AuthenticationProvider>();

    @Override
    public Principal authenticate(Credential credential, DataSession dataSession) throws BusinessException {
        for(AuthenticationProvider authenticationProvider:getAuthenticationProviders()) {
            Principal principal=authenticationProvider.authenticate(credential, dataSession);
            if (principal!=null) {
                return principal;
            }
        }

        return null;
    }

    @Override
    public Principal getPrincipalBySID(Serializable sid, DataSession dataSession) throws BusinessException {
        for(AuthenticationProvider authenticationProvider:getAuthenticationProviders()) {
            Principal principal=authenticationProvider.getPrincipalBySID(sid, dataSession);
            if (principal!=null) {
                return principal;
            }
        }

        return null;
    }

    /**
     * @return the authenticationProviders
     */
    public List<AuthenticationProvider> getAuthenticationProviders() {
        return authenticationProviders;
    }

    /**
     * @param authenticationProviders the authenticationProviders to set
     */
    public void setAuthenticationProviders(List<AuthenticationProvider> authenticationProviders) {
        this.authenticationProviders = authenticationProviders;
    }

}
