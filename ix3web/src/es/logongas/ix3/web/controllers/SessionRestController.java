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

import es.logongas.ix3.web.util.HttpResult;
import es.logongas.ix3.dao.DataSession;
import es.logongas.ix3.dao.DataSessionFactory;
import es.logongas.ix3.core.Principal;
import es.logongas.ix3.web.businessprocess.WebSessionBusinessProcess;
import es.logongas.ix3.web.util.ControllerHelper;
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
public class SessionRestController {

    @Autowired WebSessionBusinessProcess webSessionBusinessProcess;
    
    @Autowired private DataSessionFactory dataSessionFactory;
    @Autowired private ControllerHelper controllerHelper;
    
    
    @RequestMapping(value = {"/session"}, method = RequestMethod.POST, headers = "Accept=application/json")
    public void login(final HttpServletRequest httpServletRequest,final HttpServletResponse httpServletResponse) {
        
        try (DataSession dataSession = dataSessionFactory.getDataSession()) {
            Principal principal = controllerHelper.getPrincipal(httpServletRequest, httpServletResponse, dataSession);
            
            Principal outPrincipal=webSessionBusinessProcess.createWebSession(new WebSessionBusinessProcess.CreateWebSessionArguments(principal, dataSession, httpServletRequest, httpServletResponse));
            controllerHelper.objectToHttpResponse(new HttpResult(outPrincipal),  httpServletRequest, httpServletResponse);
        } catch (Exception ex) {
            controllerHelper.exceptionToHttpResponse(ex, httpServletResponse);
        }
        
    }

    @RequestMapping(value = {"/session"}, method = RequestMethod.GET, headers = "Accept=application/json")
    public void logged(final HttpServletRequest httpServletRequest,final HttpServletResponse httpServletResponse) {
        
        try (DataSession dataSession = dataSessionFactory.getDataSession()) {
            Principal principal = controllerHelper.getPrincipal(httpServletRequest, httpServletResponse, dataSession);
            
            Principal outPrincipal=webSessionBusinessProcess.getCurrentWebSession(new WebSessionBusinessProcess.GetCurrentWebSessionArguments(principal, dataSession,httpServletRequest, httpServletResponse));
            controllerHelper.objectToHttpResponse(new HttpResult(outPrincipal),  httpServletRequest, httpServletResponse);
        } catch (Exception ex) {
            controllerHelper.exceptionToHttpResponse(ex, httpServletResponse);
        }

    }

    @RequestMapping(value = {"/session"}, method = RequestMethod.DELETE)
    public void logout(final HttpServletRequest httpServletRequest,final HttpServletResponse httpServletResponse) {
        
        try (DataSession dataSession = dataSessionFactory.getDataSession()) {
            Principal principal = controllerHelper.getPrincipal(httpServletRequest, httpServletResponse, dataSession);
            
            webSessionBusinessProcess.deleteCurrentWebSession(new WebSessionBusinessProcess.DeleteCurrentWebSessionArguments(principal, dataSession,httpServletRequest,httpServletResponse));
            controllerHelper.objectToHttpResponse(new HttpResult(null),  httpServletRequest, httpServletResponse);
        } catch (Exception ex) {
            controllerHelper.exceptionToHttpResponse(ex, httpServletResponse);
        }        

    }
}
