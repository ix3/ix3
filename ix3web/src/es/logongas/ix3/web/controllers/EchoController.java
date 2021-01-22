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
package es.logongas.ix3.web.controllers;

import es.logongas.ix3.web.util.ControllerHelper;
import es.logongas.ix3.businessprocess.echo.EchoBusinessProcess;
import es.logongas.ix3.businessprocess.echo.EchoResult;
import es.logongas.ix3.core.Principal;
import es.logongas.ix3.web.util.HttpResult;
import es.logongas.ix3.dao.DataSession;
import es.logongas.ix3.dao.DataSessionFactory;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 *
 * @author logongas
 */
@Controller
public class EchoController {

    private static final Log log = LogFactory.getLog(EchoController.class);

    @Autowired private EchoBusinessProcess echoBusinessProcess;
    @Autowired private DataSessionFactory dataSessionFactory;
    @Autowired private ControllerHelper controllerHelper;

    @RequestMapping(value = {"/$echo/{id}"}, method = RequestMethod.GET)
    public void echoDataBase(final HttpServletRequest httpServletRequest, final HttpServletResponse httpServletResponse, final @PathVariable("id") long id)  {

        try (DataSession dataSession = dataSessionFactory.getDataSession()) {
            Principal principal = controllerHelper.getPrincipal(httpServletRequest, httpServletResponse, dataSession);
            
            EchoResult echoResult=echoBusinessProcess.echoDataBase(new EchoBusinessProcess.EchoDataBaseArguments(principal, dataSession, id));
            controllerHelper.objectToHttpResponse(new HttpResult(echoResult),  httpServletRequest, httpServletResponse);
        } catch (Exception ex) {
            controllerHelper.exceptionToHttpResponse(ex, httpServletRequest, httpServletResponse);
        }      

    }

    @RequestMapping(value = {"/$echo"}, method = RequestMethod.GET, produces = "application/json")
    public void echoNoDatabase(final HttpServletRequest httpServletRequest, final HttpServletResponse httpServletResponse) {

        try (DataSession dataSession = dataSessionFactory.getDataSession()) {
            Principal principal = controllerHelper.getPrincipal(httpServletRequest, httpServletResponse, dataSession);
            
            EchoResult echoResult=echoBusinessProcess.echoNoDataBase(new EchoBusinessProcess.EchoNoDataBaseArguments(principal, dataSession));
            controllerHelper.objectToHttpResponse(new HttpResult(echoResult), httpServletRequest, httpServletResponse);
        } catch (Exception ex) {
            controllerHelper.exceptionToHttpResponse(ex, httpServletRequest, httpServletResponse);
        }

    }

}
