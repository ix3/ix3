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

import es.logongas.ix3.security.model.Identity;
import es.logongas.ix3.security.model.Permission;
import es.logongas.ix3.security.model.SecureResourceType;
import es.logongas.ix3.security.model.SpecialUsers;
import es.logongas.ix3.core.BusinessException;
import es.logongas.ix3.dao.DAOFactory;
import es.logongas.ix3.dao.Filter;
import es.logongas.ix3.dao.GenericDAO;
import es.logongas.ix3.security.authentication.Principal;
import es.logongas.ix3.security.authorization.AuthorizationProvider;
import es.logongas.ix3.security.authorization.AuthorizationType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    public AuthorizationType authorized(Principal principal,String secureResourceTypeName, String secureResource, String permissionName, Object arguments) {
        try {
            AuthorizationType authorizationType;
            GenericDAO<Identity, Integer> identityDAO = daoFactory.getDAO(es.logongas.ix3.security.model.Identity.class);
            Permission permission=getPermission(secureResourceTypeName, permissionName);
            
            
            if (principal != null) {
                if (principal instanceof Identity) {
                    Identity identity = (Identity) principal;
                    authorizationType = identity.authorized(secureResource, permission, arguments);
                    if (authorizationType == AuthorizationType.Abstain) {
                        Identity authenticatedIdentity = identityDAO.readByNaturalKey(SpecialUsers.Authenticated.name());
                        if (authenticatedIdentity!=null) {
                            authorizationType = authenticatedIdentity.authorized(secureResource, permission, arguments);
                        } else {
                            authorizationType = AuthorizationType.Abstain;
                        }
                        if (authorizationType == AuthorizationType.Abstain) {
                            Identity allIdentity = identityDAO.readByNaturalKey(SpecialUsers.All.name());
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
                Identity allIdentity = identityDAO.readByNaturalKey(SpecialUsers.All.name());
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
    
    private Permission getPermission(String secureResourceTypeName,String permissionName) throws BusinessException {
            GenericDAO<SecureResourceType,Integer> secureResourceTypeDAO=daoFactory.getDAO(SecureResourceType.class);
            GenericDAO<Permission,Integer> permissionDAO=daoFactory.getDAO(Permission.class);

            SecureResourceType secureResourceType=secureResourceTypeDAO.readByNaturalKey(secureResourceTypeName);
            List<Filter> filters=new ArrayList<Filter>();
            filters.add(new Filter("secureResourceType", secureResourceType));
            filters.add(new Filter("name", permissionName));

            List<Permission> permissions=permissionDAO.search(filters);
            if (permissions.size()==0) {
                throw new RuntimeException("No existe el permiso con nombre:"+permissionName + " del tipo:"+secureResourceType);
            }
            if (permissions.size()>1) {
                throw new RuntimeException("Existe más de un permiso con nombre:"+permissionName + " del tipo:"+secureResourceType);
            }

            Permission permission=permissions.get(0);
            
            return permission;
    }
    
}
