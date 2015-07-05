/*
 * Copyright 2013 Lorenzo González.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package es.logongas.ix3.web.controllers;

import es.logongas.ix3.core.BusinessException;
import es.logongas.ix3.security.authentication.impl.CredentialImplLoginPassword;
import es.logongas.ix3.security.authentication.AuthenticationManager;
import es.logongas.ix3.security.authentication.Principal;
import es.logongas.ix3.security.util.WebSessionSidStorage;
import java.io.Serializable;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Controlador REST que gestiona el log-in, log-out, etc.
 *
 * @author Lorenzo González
 */
@Controller
public class SesionRESTController extends AbstractRESTController {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    WebSessionSidStorage webSessionSidStorage;
    
    @RequestMapping(value = {"/session"}, method = RequestMethod.POST, headers = "Accept=application/json")
    public void login(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {

        restMethod(httpServletRequest, httpServletResponse, null, new Command() {

            @Override
            public CommandResult run(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Map<String, Object> arguments) throws Exception, BusinessException {
                httpServletRequest.setCharacterEncoding("UTF-8");

                String login = httpServletRequest.getParameter("login");
                String password = httpServletRequest.getParameter("password");

                CredentialImplLoginPassword credentialImplLoginPassword = new CredentialImplLoginPassword(login, password);

                Principal principal = authenticationManager.authenticate(credentialImplLoginPassword);

                if (principal == null) {
                    throw new BusinessException("El usuario o contraseña no son válidos");
                }

                webSessionSidStorage.setSid(httpServletRequest,httpServletResponse,principal.getSid());

                return new CommandResult(Principal.class, principal,HttpServletResponse.SC_CREATED);

            }
        });

    }

    @RequestMapping(value = {"/session"}, method = RequestMethod.GET, headers = "Accept=application/json")
    public void logged(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {

        restMethod(httpServletRequest, httpServletResponse, null, new Command() {

            @Override
            public CommandResult run(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Map<String, Object> arguments) throws Exception, BusinessException {
                Principal principal;

                Serializable sid =  webSessionSidStorage.getSid(httpServletRequest,httpServletResponse);

                if (sid == null) {
                    principal = null;
                } else {
                    principal = authenticationManager.getPrincipalBySID(sid);
                }

                return new CommandResult(Principal.class, principal);

            }
        });

    }

    @RequestMapping(value = {"/session"}, method = RequestMethod.DELETE)
    public void logout(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        restMethod(httpServletRequest, httpServletResponse, null, new Command() {

            @Override
            public CommandResult run(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Map<String, Object> arguments) throws Exception, BusinessException {
                
                webSessionSidStorage.deleteSid(httpServletRequest,httpServletResponse);

                return null;

            }
        });
    }
}
