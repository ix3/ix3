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
package es.logongas.ix3.security.impl.authentication;

import es.logongas.ix3.model.Identity;
import es.logongas.ix3.persistence.services.dao.BusinessException;
import es.logongas.ix3.persistence.services.dao.DAOFactory;
import es.logongas.ix3.persistence.services.dao.GenericDAO;
import es.logongas.ix3.security.services.authentication.AuthenticationManager;
import es.logongas.ix3.security.services.authentication.AuthenticationProvider;
import es.logongas.ix3.security.services.authentication.Credential;
import es.logongas.ix3.security.services.authentication.Principal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author Lorenzo González
 */
public class AuthenticationManagerImpl implements AuthenticationManager {
    @Autowired
    DAOFactory daoFactory;

    private List<AuthenticationProvider> authenticationProviders=new ArrayList<AuthenticationProvider>();

    @Override
    public Identity authenticate(Credential credential) throws BusinessException {
        boolean authenticated=false;
        for(AuthenticationProvider authenticationProvider:getAuthenticationProviders()) {
            authenticated=authenticationProvider.authenticate(credential);
            if (authenticated==true) {
                break;
            }
        }

        if (authenticated==true) {
            GenericDAO<Identity,Integer> genericDAO=daoFactory.getDAO(Identity.class);
            Identity identity=genericDAO.readByNaturalKey(credential.getLogin());

            return identity;
        } else {
            return null;
        }

    }

    @Override
    public Principal getPrincipalBySID(int sid) throws BusinessException {
        GenericDAO<Identity,Integer> genericDAO=daoFactory.getDAO(Identity.class);

        return genericDAO.read(sid);
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
