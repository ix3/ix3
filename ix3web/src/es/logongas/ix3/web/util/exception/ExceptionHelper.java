/*
 * Copyright 2021 logongas.
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
package es.logongas.ix3.web.util.exception;

import es.logongas.ix3.core.BusinessException;
import es.logongas.ix3.security.authorization.BusinessSecurityException;
import es.logongas.ix3.util.ExceptionUtil;
import es.logongas.ix3.web.json.JsonFactory;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.MapMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.HttpMediaTypeNotAcceptableException;

/**
 *
 * @author logongas
 */
public class ExceptionHelper {

    private static final Logger log = LogManager.getLogger(ExceptionHelper.class);
    private static final Logger logException = LogManager.getLogger(Exception.class);
    private static final Logger logBusinessSecurityException = LogManager.getLogger(BusinessSecurityException.class);

    @Autowired
    private JsonFactory jsonFactory;
    @Autowired
    private ExceptionNotify exceptionNotify;

    public void exceptionToHttpResponse(Throwable throwable, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        try {
            BusinessException businessException = ExceptionUtil.getBusinessExceptionFromThrowable(throwable);

            if (businessException != null) {

                if (businessException instanceof BusinessSecurityException) {
                    BusinessSecurityException businessSecurityException = (BusinessSecurityException) businessException;
                    logBusinessSecurityException.warn(getMapMessage(null, httpServletRequest), businessSecurityException);
                    try {
                        exceptionNotify.notify(businessSecurityException, httpServletRequest);
                    } catch (Exception ex) {
                        logException.error("Fallo la notificación", ex);
                    }

                    if (httpServletResponse.isCommitted() == false) {
                        httpServletResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        httpServletResponse.setContentType("text/plain; charset=UTF-8");
                    } else {
                        log.warn("La respuesta BusinessSecurityException isCommitted=true");
                    }

                } else {
                    if (httpServletResponse.isCommitted() == false) {
                        httpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        httpServletResponse.setContentType("application/json; charset=UTF-8");
                        httpServletResponse.getWriter().println(jsonFactory.getJsonWriter().toJson(businessException.getBusinessMessages()));
                    } else {
                        log.warn("La respuesta BusinessException isCommitted=true");
                    }
                }

            } if (throwable instanceof HttpMediaTypeNotAcceptableException) {
                if (httpServletResponse.isCommitted() == false) {
                    httpServletResponse.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
                    httpServletResponse.setContentType("text/plain; charset=UTF-8");
                } else {
                    log.warn("La respuesta HttpMediaTypeNotAcceptableException isCommitted=true");
                }
            } else {
                logException.error(getMapMessage("Falló la llamada al servidor", httpServletRequest), throwable);
                try {
                    exceptionNotify.notify(throwable, httpServletRequest);
                } catch (Exception ex) {
                    logException.error("Fallo la notificación", ex);
                }

                if (httpServletResponse.isCommitted() == false) {
                    httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    httpServletResponse.setContentType("text/plain");
                    httpServletResponse.getWriter().println(throwable.getClass().getName());
                } else {
                    log.warn("La respuesta Exception isCommitted=true");
                }

            }
        } catch (Exception exception) {
            logException.error(getMapMessage("Falló al gestionar la excepción", httpServletRequest), exception);

            if (httpServletResponse.isCommitted() == false) {
                httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            } else {
                log.warn("La respuesta gestion Exception isCommitted=true");
            }
        }

    }

    private MapMessage getMapMessage(String msg, HttpServletRequest httpServletRequest) {
        Map<String, String> map = new HashMap<>();

        map.put("message", msg);
        map.put("RemoteAddr", httpServletRequest.getRemoteAddr());
        map.put("RequestURI", httpServletRequest.getRequestURI());
        map.put("RequestURL", httpServletRequest.getRequestURL() + "");
        map.put("QueryString", httpServletRequest.getQueryString());
        map.put("Method", httpServletRequest.getMethod());
        Enumeration<String> names = httpServletRequest.getHeaderNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            String value = httpServletRequest.getHeader(name);

            map.put("Header_" + name, value);
        }

        return new MapMessage(map);
    }

}
