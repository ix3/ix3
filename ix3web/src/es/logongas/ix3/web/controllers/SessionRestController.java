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

import es.logongas.ix3.web.controllers.helper.AbstractRestController;
import es.logongas.ix3.web.controllers.command.CommandResult;
import es.logongas.ix3.web.controllers.command.Command;
import es.logongas.ix3.core.BusinessException;
import es.logongas.ix3.security.authentication.Principal;
import es.logongas.ix3.web.controllers.endpoint.EndPoint;
import es.logongas.ix3.web.service.WebSessionService;
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
public class SessionRestController extends AbstractRestController {

    @Autowired
    WebSessionService webSessionService;
    

    @RequestMapping(value = {"/session"}, method = RequestMethod.POST, headers = "Accept=application/json")
    public void login(final HttpServletRequest httpServletRequest,final HttpServletResponse httpServletResponse) {
        
        restMethod(httpServletRequest, httpServletResponse,"login",null, new Command() {

            @Override
            public CommandResult run() throws Exception, BusinessException {

                Principal principal=webSessionService.createWebSession(httpServletRequest, httpServletResponse);

                return new CommandResult(Principal.class, principal,HttpServletResponse.SC_CREATED);

            }
        });

    }

    @RequestMapping(value = {"/session"}, method = RequestMethod.GET, headers = "Accept=application/json")
    public void logged(final HttpServletRequest httpServletRequest,final HttpServletResponse httpServletResponse) {
        
        restMethod(httpServletRequest, httpServletResponse,"logged",null, new Command() {

            @Override
            public CommandResult run() throws Exception, BusinessException {

                Principal principal=webSessionService.getCurrentWebSession(httpServletRequest, httpServletResponse);

                return new CommandResult(Principal.class, principal);

            }
        });

    }

    @RequestMapping(value = {"/session"}, method = RequestMethod.DELETE)
    public void logout(final HttpServletRequest httpServletRequest,final HttpServletResponse httpServletResponse) {
        restMethod(httpServletRequest, httpServletResponse,"logout",null, new Command() {

            @Override
            public CommandResult run() throws Exception, BusinessException {
                
                webSessionService.deleteCurrentWebSession(httpServletRequest,httpServletResponse);

                return new CommandResult(null);

            }
        });
    }
}
