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
package es.logongas.ix3.security.impl.authorization;

import es.logongas.ix3.security.services.authentication.User;
import es.logongas.ix3.security.services.authorization.AuthorizationManager;
import es.logongas.ix3.security.services.authorization.AuthorizationProvider;
import es.logongas.ix3.security.services.authorization.AuthorizationType;
import es.logongas.ix3.security.services.authorization.ResourceType;
import java.util.ArrayList;
import java.util.List;

/**
 * Solicita a todos los AuthorizationProvider si se tiene o no acceso a un recurso
 * @author Lorenzo González
 */
public class AuthorizationManagerImpl implements AuthorizationManager {

    boolean defaultAllow=true;
    List<AuthorizationProvider> authorizationProviders=new ArrayList<>();

    @Override
    public boolean authorized(User user, ResourceType resourceType, Object accessType, Object resource) {
        Boolean authorized=null;


        for(AuthorizationProvider authorizationProvider:authorizationProviders) {
            AuthorizationType authorizationType=authorizationProvider.authorized(user, resourceType, accessType, resource);

            switch (authorizationType) {
                case AccessAllow:
                    if (authorized!=false) {
                        authorized=true;
                    }
                    break;
                case AccessDeny:
                    authorized=false;
                    break;
                case Abstain:
                    break;
            }
        }

        if (authorized==Boolean.TRUE) {
            return true;
        } else if (authorized==Boolean.FALSE) {
            return false;
        } else if (authorized==null) {
            //Si nadie ha dicho nada , usamos el valor por defecto
            return defaultAllow;
        } else {
            throw new RuntimeException("Error de lógica");
        }

    }


    public void setAuthorizationProviders(List<AuthorizationProvider> authorizationProviders) {
        this.authorizationProviders=authorizationProviders;
    }

    public void setDefaultAllow(boolean defaultAllow) {
        this.defaultAllow=defaultAllow;
    }

}
