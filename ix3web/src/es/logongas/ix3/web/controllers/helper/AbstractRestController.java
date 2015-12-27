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
package es.logongas.ix3.web.controllers.helper;

import es.logongas.ix3.web.controllers.command.CommandResult;
import es.logongas.ix3.web.controllers.command.Command;
import es.logongas.ix3.web.controllers.command.MimeType;
import es.logongas.ix3.web.controllers.endpoint.EndPoint;
import es.logongas.ix3.web.controllers.endpoint.EndPointsFactory;
import es.logongas.ix3.web.json.beanmapper.BeanMapper;
import es.logongas.ix3.web.json.JsonFactory;
import es.logongas.ix3.web.json.JsonWriter;
import es.logongas.ix3.web.json.beanmapper.Expands;
import es.logongas.ix3.web.security.AuthorizationInterceptorImplController;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
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

    @Autowired
    private AuthorizationInterceptorImplController authorizationInterceptorImplController;

    private Log log = LogFactory.getLog(AbstractRestController.class);

    final protected void restMethod(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, String methodName, Class entityType, Command command) {
        try {
            EndPoint endPoint = getEndPoint(httpServletRequest);
            if (endPoint == null) {
                httpServletResponse.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                httpServletResponse.getWriter().println("No existe el EndPoint");
                return;
            }

            Map<String, Object> arguments = this.getArguments(command);

            authorizationInterceptorImplController.checkPreAuthorized(this, methodName, entityType, arguments);

            CommandResult commandResult = command.run();

            if (commandResult == null) {
                throw new IllegalArgumentException("El objeto commandResult no puede estar vacio");
            }

            authorizationInterceptorImplController.checkPostAuthorized(this, methodName, entityType, arguments, commandResult);

            commandResultToHttpResponse(commandResult, endPoint, httpServletRequest, httpServletResponse);
            
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

    }

    private void commandResultToHttpResponse(CommandResult commandResult, EndPoint endPoint, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException {
        httpServletResponse.setStatus(commandResult.getHttpSuccessStatus());
        if (commandResult.isCache()) {
            cache(httpServletResponse);
        } else {
            noCache(httpServletResponse);
        }
        if (commandResult.getMimeType() != null) {
            httpServletResponse.setContentType(commandResult.getMimeType().getText());
        }

        if (commandResult.getResult() != null) {
            switch (commandResult.getMimeType()) {
                case JSON:
                    JsonWriter jsonWriter = jsonFactory.getJsonWriter(commandResult.getResultClass());
                    Expands expands = Expands.createExpandsWithoutAsterisk(httpServletRequest.getParameter(PARAMETER_EXPAND));
                    BeanMapper beanMapper;
                    if (commandResult.getBeanMapper() != null) {
                        beanMapper = commandResult.getBeanMapper();
                    } else {
                        beanMapper = endPoint.getBeanMapper();
                    }
                    String jsonOut = jsonWriter.toJson(commandResult.getResult(), expands, beanMapper);
                    httpServletResponse.getWriter().println(jsonOut);
                    break;
                case OCTET_STREAM:
                    if (commandResult.getResult() instanceof byte[]) {
                        byte[] result = (byte[]) commandResult.getResult();
                        httpServletResponse.getOutputStream().write(result);
                        httpServletResponse.getOutputStream().flush();
                        httpServletResponse.getOutputStream().close();
                    } else {
                        throw new RuntimeException("Si el MimeType es OCTET_STREAM es tipo de result debe ser byte[] pero es " + commandResult.getResult().getClass().getName());
                    }

                    break;
                default:
                    throw new RuntimeException("MimeType no soportado:" + commandResult.getMimeType());
            }
        }
    }

    private EndPoint getEndPoint(HttpServletRequest httpServletRequest) {
        String path = (String) httpServletRequest.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String method = httpServletRequest.getMethod();
        EndPoint endPoint = EndPoint.getBestEndPoint(EndPoint.getMatchEndPoint(endPointFactory.getEndPoints(), path, method), path, method);

        return endPoint;
    }

    protected BeanMapper getBeanMapper(HttpServletRequest httpServletRequest) {
        EndPoint endPoint = getEndPoint(httpServletRequest);
        if (endPoint == null) {
            throw new RuntimeException("No existe el EndPoint");
        }

        return endPoint.getBeanMapper();

    }

    private Map<String, Object> getArguments(final Command command) {
        try {
            Field[] fields = command.getClass().getFields();

            final HashMap<String, Object> arguments = new HashMap<String, Object>();

            for (Field field : fields) {
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }
                String fieldName = field.getName();
                Object value = field.get(command);

                arguments.put(fieldName, value);
            }

            return arguments;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

}
