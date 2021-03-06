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
package es.logongas.ix3.security.authorization.impl;

import es.logongas.ix3.dao.DataSession;
import es.logongas.ix3.core.Principal;
import es.logongas.ix3.security.authorization.AuthorizationManager;
import es.logongas.ix3.security.authorization.AuthorizationProvider;
import es.logongas.ix3.security.authorization.AuthorizationType;
import java.util.ArrayList;
import java.util.List;

/**
 * Busca por orden correlativo todos los AuthorizationProvider y se queda con el primero que encuentra que permite o deniega
 * @author Lorenzo González
 */
public class AuthorizationManagerImpl implements AuthorizationManager {
    private List<AuthorizationProvider> authorizationProviders=new ArrayList<AuthorizationProvider>();
    private boolean defaultAuthorization=false;


    @Override
    public boolean authorized(Principal principal,String secureResourceTypeName,String secureResource,String permissionName,Object arguments, DataSession dataSession) {
        for(AuthorizationProvider authorizationProvider:authorizationProviders) {
            AuthorizationType authorizationType=authorizationProvider.authorized(principal,secureResourceTypeName,secureResource, permissionName, arguments, dataSession);

            if (authorizationType==AuthorizationType.AccessAllow) {
                return true;
            } else if (authorizationType==AuthorizationType.AccessDeny) {
                return false;
            }
        }
        return defaultAuthorization;
    }

    /**
     * @return the authorizationProviders
     */
    public List<AuthorizationProvider> getAuthorizationProviders() {
        return authorizationProviders;
    }

    /**
     * @param authorizationProviders the authorizationProviders to set
     */
    public void setAuthorizationProviders(List<AuthorizationProvider> authorizationProviders) {
        this.authorizationProviders = authorizationProviders;
    }

    /**
     * @return the defaultAuthorization
     */
    public boolean isDefaultAuthorization() {
        return defaultAuthorization;
    }

    /**
     * @param defaultAuthorization the defaultAuthorization to set
     */
    public void setDefaultAuthorization(boolean defaultAuthorization) {
        this.defaultAuthorization = defaultAuthorization;
    }

}
