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

import es.logongas.ix3.model.Permission;
import es.logongas.ix3.model.SecureResourceType;
import es.logongas.ix3.model.User;
import es.logongas.ix3.model.SpecialUsers;
import es.logongas.ix3.persistence.services.dao.BusinessException;
import es.logongas.ix3.persistence.services.dao.DAOFactory;
import es.logongas.ix3.persistence.services.dao.GenericDAO;
import es.logongas.ix3.security.services.authorization.AuthorizationProvider;
import es.logongas.ix3.security.services.authorization.AuthorizationType;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Delega preguntando al objeto "User" si tiene o no permiso
 *
 * @author Lorenzo González
 */
public class AuthorizationProviderImplUser implements AuthorizationProvider {

    @Autowired
    DAOFactory daoFactory;

    @Override
    public AuthorizationType authorized(User user, String secureResource, Permission permission, Object arguments) {
        try {
            AuthorizationType authorizationType;
            GenericDAO genericDAO = (GenericDAO) daoFactory.getDAO(User.class);

            if (user != null) {
                authorizationType = user.authorized(secureResource, permission, arguments);
                if (authorizationType == AuthorizationType.Abstain) {
                    User authenticatedUser = (User) genericDAO.readByNaturalKey(SpecialUsers.Authenticated.name());
                    authorizationType = authenticatedUser.authorized( secureResource, permission, arguments);
                    if (authorizationType == AuthorizationType.Abstain) {
                        User allUser = (User) genericDAO.readByNaturalKey(SpecialUsers.All.name());
                        authorizationType = allUser.authorized(secureResource, permission, arguments);
                    }
                }
            } else {
                User allUser = (User) genericDAO.readByNaturalKey(SpecialUsers.All.name());
                authorizationType = allUser.authorized(secureResource, permission, arguments);
            }

            return authorizationType;
        } catch (BusinessException be) {
            //Si se produce algún error , no permitimos el acceso
            return AuthorizationType.AccessDeny;
        }
    }
}
