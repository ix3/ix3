/*
 * Copyright 2015 logongas.
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
package es.logongas.ix3.web.security.impl;

import es.logongas.ix3.security.authentication.Credential;
import es.logongas.ix3.security.authentication.impl.CredentialImplLoginPassword;
import es.logongas.ix3.web.security.WebCredentialFactory;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Obtiene las credenciales a partir de los parámetros "login" y "password" de la petición
 * @author logongas
 */
public class WebCredentialFactoryImplLoginPassword implements WebCredentialFactory {

    @Override
    public Credential getCredential(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        String login = httpServletRequest.getParameter("login");
        String password = httpServletRequest.getParameter("password");

        CredentialImplLoginPassword credentialImplLoginPassword = new CredentialImplLoginPassword(login, password);

        return credentialImplLoginPassword;
    }

}
