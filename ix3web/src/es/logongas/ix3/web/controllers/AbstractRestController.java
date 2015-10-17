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
import es.logongas.ix3.web.json.JsonFactory;
import es.logongas.ix3.web.json.JsonWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class AbstractRestController extends AbstractController {

    protected final String PARAMETER_EXPAND = "$expand";
    
    @Autowired
    protected JsonFactory jsonFactory;

    protected Log log = LogFactory.getLog(AbstractRestController.class);

    final protected void restMethod(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Map<String, Object> arguments, Command command) {
        try {

            CommandResult commandResult = command.run(httpServletRequest, httpServletResponse, arguments);
            
            if ((commandResult != null) && (commandResult.getResult() != null)) {
                JsonWriter jsonWriter = jsonFactory.getJsonWriter(commandResult.getResultClass());
                List<String> expand = getExpand(httpServletRequest.getParameter(PARAMETER_EXPAND));
                String jsonOut = jsonWriter.toJson(commandResult.getResult(),expand);

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

    /**
     * Transforma el parámetro "expand" que viene por la petición http en una
     * array
     *
     * @param expand El String con varios expand separados por comas
     * @return El array con cada uno de ello.
     */
    final protected List<String> getExpand(String expand) {
        if ((expand == null) || (expand.trim().isEmpty())) {
            return new ArrayList<String>();
        } else {
            return Arrays.asList(expand.split(","));
        }
    }
   
}
