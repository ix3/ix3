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
import es.logongas.ix3.persistence.services.dao.BusinessException;
import es.logongas.ix3.persistence.services.dao.DAOFactory;
import es.logongas.ix3.persistence.services.dao.GenericDAO;
import es.logongas.ix3.security.services.authentication.Principal;
import es.logongas.ix3.security.services.authorization.AuthorizationManager;
import es.logongas.ix3.security.services.authorization.AuthorizationProvider;
import es.logongas.ix3.security.services.authorization.AuthorizationType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Busca por orden correlativo todos los AuthorizationProvider y se queda con el primero que encuentra que permite o deniega
 * @author Lorenzo González
 */
public class AuthorizationManagerImplIdentity implements AuthorizationManager {
    private List<AuthorizationProvider> authorizationProviders=new ArrayList<AuthorizationProvider>();
    private boolean defaultAuthorization=false;

    @Autowired
    DAOFactory daoFactory;

    @Override
    public boolean authorized(Principal principal,String secureResource, Permission permission, Object arguments) {
        for(AuthorizationProvider authorizationProvider:authorizationProviders) {
            AuthorizationType authorizationType=authorizationProvider.authorized(principal,secureResource, permission, arguments);

            if (authorizationType==AuthorizationType.AccessAllow) {
                return true;
            } else if (authorizationType==AuthorizationType.AccessDeny) {
                return false;
            }
        }
        return defaultAuthorization;
    }

    @Override
    public boolean authorized(Principal principal,String secureResourceTypeName,String secureResource,String permissionName,Object arguments) {
        try {
            GenericDAO<SecureResourceType,Integer> secureResourceTypeDAO=daoFactory.getDAO(SecureResourceType.class);
            GenericDAO<Permission,Integer> permissionDAO=daoFactory.getDAO(Permission.class);

            SecureResourceType secureResourceType=secureResourceTypeDAO.readByNaturalKey(secureResourceTypeName);
            Map<String,Object> filter=new HashMap<String,Object>();
            filter.put("secureResourceType", secureResourceType);
            filter.put("name", permissionName);

            List<Permission> permissions=permissionDAO.search(filter);
            if (permissions.size()==0) {
                throw new RuntimeException("No existe el permiso con nombre:"+permissionName + " del tipo:"+secureResourceType);
            }
            if (permissions.size()>1) {
                throw new RuntimeException("Existe más de un permiso con nombre:"+permissionName + " del tipo:"+secureResourceType);
            }

            Permission permission=permissions.get(0);

            return authorized(principal, secureResource, permission, arguments);

        } catch (BusinessException ex) {
            //Si las reglas de negocio no nos dejan , es que no estamos autorizados
            return false;
        }
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
