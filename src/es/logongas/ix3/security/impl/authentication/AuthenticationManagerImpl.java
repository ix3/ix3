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

import es.logongas.ix3.security.services.authentication.AuthenticationManager;
import es.logongas.ix3.security.services.authentication.AuthenticationProvider;
import es.logongas.ix3.security.services.authentication.Credential;
import es.logongas.ix3.security.services.authentication.User;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Lorenzo González
 */
public class AuthenticationManagerImpl implements AuthenticationManager {

    List<AuthenticationProvider> authenticationProviders=new ArrayList<AuthenticationProvider>();

    @Override
    public User authenticate(Credential credential) {
        User user=null;


        for(AuthenticationProvider authenticationProvider:authenticationProviders) {
            user=authenticationProvider.authenticate(credential);
            if (user!=null) {
                break;
            }
        }

        return user;
    }

    @Override
    public User getUserByIdUser(String idUser) {
        User user=null;

        for(AuthenticationProvider authenticationProvider:authenticationProviders) {
            user=authenticationProvider.getUserByIdUser(idUser);
            if (user!=null) {
                break;
            }
        }

        return user;
    }

    public void setAuthenticationProviders(List<AuthenticationProvider> authenticationProviders) {
        this.authenticationProviders=authenticationProviders;
    }

}
