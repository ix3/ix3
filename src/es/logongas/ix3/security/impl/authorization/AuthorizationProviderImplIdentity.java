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

import es.logongas.ix3.model.Identity;
import es.logongas.ix3.model.Permission;
import es.logongas.ix3.model.SpecialUsers;
import es.logongas.ix3.persistence.services.dao.BusinessException;
import es.logongas.ix3.persistence.services.dao.DAOFactory;
import es.logongas.ix3.persistence.services.dao.GenericDAO;
import es.logongas.ix3.security.services.authentication.Principal;
import es.logongas.ix3.security.services.authorization.AuthorizationProvider;
import es.logongas.ix3.security.services.authorization.AuthorizationType;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Delega preguntando al objeto "User" si tiene o no permiso
 *
 * @author Lorenzo González
 */
public class AuthorizationProviderImplIdentity implements AuthorizationProvider {

    @Autowired
    DAOFactory daoFactory;

    @Override
    public AuthorizationType authorized(Principal principal, String secureResource, Permission permission, Object arguments) {
        try {
            AuthorizationType authorizationType;
            GenericDAO<Identity, Integer> genericDAO = daoFactory.getDAO(es.logongas.ix3.model.Identity.class);

            if (principal != null) {
                if (principal instanceof Identity) {
                    Identity identity = (Identity) principal;
                    authorizationType = identity.authorized(secureResource, permission, arguments);
                    if (authorizationType == AuthorizationType.Abstain) {
                        Identity authenticatedIdentity = genericDAO.readByNaturalKey(SpecialUsers.Authenticated.name());
                        if (authenticatedIdentity!=null) {
                            authorizationType = authenticatedIdentity.authorized(secureResource, permission, arguments);
                        } else {
                            authorizationType = AuthorizationType.Abstain;
                        }
                        if (authorizationType == AuthorizationType.Abstain) {
                            Identity allIdentity = genericDAO.readByNaturalKey(SpecialUsers.All.name());
                            if (allIdentity!=null) {
                                authorizationType = allIdentity.authorized(secureResource, permission, arguments);
                            } else {
                               authorizationType = AuthorizationType.Abstain;
                            }
                        }
                    }
                } else {
                    authorizationType = AuthorizationType.Abstain;
                }
            } else {
                Identity allIdentity = genericDAO.readByNaturalKey(SpecialUsers.All.name());
                if (allIdentity!=null) {
                    authorizationType = allIdentity.authorized(secureResource, permission, arguments);
                } else {
                   authorizationType = AuthorizationType.Abstain;
                }
            }

            return authorizationType;
        } catch (BusinessException be) {
            //Si se produce algún error , no permitimos el acceso
            return AuthorizationType.AccessDeny;
        }
    }
}
