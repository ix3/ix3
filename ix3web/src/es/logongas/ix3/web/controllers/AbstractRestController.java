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

import es.logongas.ix3.core.BusinessException;
import es.logongas.ix3.web.controllers.endpoint.EndPoint;
import es.logongas.ix3.web.controllers.endpoint.EndPointsFactory;
import es.logongas.ix3.web.json.beanmapper.BeanMapper;
import es.logongas.ix3.web.json.JsonFactory;
import es.logongas.ix3.web.json.JsonWriter;
import es.logongas.ix3.web.json.beanmapper.Expands;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerMapping;

public class AbstractRestController extends AbstractController {

    protected final String PARAMETER_EXPAND = "$expand";

    @Autowired
    protected JsonFactory jsonFactory;

    @Autowired
    private EndPointsFactory endPointFactory;

    protected Log log = LogFactory.getLog(AbstractRestController.class);

    final protected void restMethod(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Command command) {
        try {

            String path = (String) httpServletRequest.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
            String method = httpServletRequest.getMethod();
            EndPoint endPoint = EndPoint.getBestEndPoint(EndPoint.getMatchEndPoint(endPointFactory.getEndPoints(), path, method), path, method);
            if (endPoint == null) {
                httpServletResponse.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                httpServletResponse.getWriter().println("No existe el EndPoint para el path '" + path + "' y el método '" + method + "'");
                return;
            }

            CommandResult commandResult = command.run(endPoint);

            if (commandResult != null) {
                if (commandResult.getResult() != null) {
                    JsonWriter jsonWriter = jsonFactory.getJsonWriter(commandResult.getResultClass());
                    Expands expands = Expands.createExpandsWithoutAsterisk(httpServletRequest.getParameter(PARAMETER_EXPAND));

                    BeanMapper beanMapper;
                    if (commandResult.getBeanMapper() != null) {
                        beanMapper = commandResult.getBeanMapper();
                    } else {
                        beanMapper = endPoint.getBeanMapper();
                    }

                    String jsonOut = jsonWriter.toJson(commandResult.getResult(), expands, beanMapper);

                    if (commandResult.isCache()) {
                        cache(httpServletResponse);
                    } else {
                        noCache(httpServletResponse);
                    }
                    httpServletResponse.setStatus(commandResult.getHttpSuccessStatus());
                    httpServletResponse.setContentType("application/json; charset=UTF-8");
                    httpServletResponse.getWriter().println(jsonOut);
                } else {
                    noCache(httpServletResponse);
                    httpServletResponse.setStatus(HttpServletResponse.SC_NO_CONTENT);
                }
            } else {
                //Si no nos reponden nada , no hacemos nada ya que significa que el "command" es el responsable de todo.
            }

        } catch (BusinessException ex) {
            try {
                String jsonOut = jsonFactory.getJsonWriter().toJson(ex.getBusinessMessages());

                httpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                httpServletResponse.setContentType("application/json; charset=UTF-8");
                httpServletResponse.getWriter().println(jsonOut);
            } catch (Exception ex2) {
                httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                httpServletResponse.setContentType("text/plain");
                try {
                    ex.printStackTrace(httpServletResponse.getWriter());
                } catch (Exception ex3) {
                    log.error("Falló al imprimir la traza", ex3);
                }
            }
        } catch (Exception ex) {
            log.error("Fallo al ejecutar el método del controlador REST", ex);

            httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            httpServletResponse.setContentType("text/plain");
            try {
                ex.printStackTrace(httpServletResponse.getWriter());
            } catch (Exception ex2) {
                log.error("Falló al imprimir la traza", ex2);
            }
        }

    }

}
