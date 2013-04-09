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

import es.logongas.ix3.security.services.authentication.AuthenticationProvider;
import es.logongas.ix3.security.services.authentication.Credential;
import es.logongas.ix3.security.services.authentication.User;

/**
 *
 * @author Lorenzo González
 */
public class AuthenticationProviderImplDummy implements AuthenticationProvider {

    @Override
    public User authenticate(Credential credential) {
        if (credential instanceof CredentialImplLoginPassword) {
            CredentialImplLoginPassword credentialImplLoginPassword=(CredentialImplLoginPassword)credential;
            if (("admin".equals(credentialImplLoginPassword.getLogin())) && ("admin".equals(credentialImplLoginPassword.getPassword()))  ) {
                return new UserImpl("admin","Administrador");
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public User getUserByIdUser(String idUser) {
        if ("admin".equals(idUser)) {
            return new UserImpl("admin","Administrador");
        } else {
            return null;
        }
    }

}
