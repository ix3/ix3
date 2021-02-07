/*
 * Copyright 2016 logongas.
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
package es.logongas.ix3.web.util;

import es.logongas.ix3.core.BusinessException;
import es.logongas.ix3.dao.DataSession;
import es.logongas.ix3.security.authentication.AuthenticationManager;
import es.logongas.ix3.core.Principal;
import es.logongas.ix3.security.authorization.BusinessSecurityException;
import es.logongas.ix3.util.ExceptionUtil;
import es.logongas.ix3.web.controllers.endpoint.EndPoint;
import es.logongas.ix3.web.controllers.endpoint.EndPointsFactory;
import es.logongas.ix3.web.json.JsonFactory;
import es.logongas.ix3.web.json.JsonWriter;
import es.logongas.ix3.web.json.beanmapper.BeanMapper;
import es.logongas.ix3.web.json.beanmapper.Expands;
import es.logongas.ix3.web.security.WebSessionSidStorage;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.MapMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerMapping;

/**
 *
 * @author logongas
 */
public class ControllerHelper {
    
    private static final Logger log = LogManager.getLogger(ControllerHelper.class);
    private static final Logger logException = LogManager.getLogger(Exception.class);
    private static final Logger logBusinessSecurityException = LogManager.getLogger(BusinessSecurityException.class);
    
    
    private final String PARAMETER_EXPAND = "$expand";

    @Autowired
    private EndPointsFactory endPointsFactory;
    @Autowired
    private JsonFactory jsonFactory;
    @Autowired
    private WebSessionSidStorage webSessionSidStorage;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private ExceptionNotify exceptionNotify;
    
    public Expands getRequestExpands(HttpServletRequest httpServletRequest) {
        return Expands.createExpandsWithoutAsterisk(httpServletRequest.getParameter(PARAMETER_EXPAND));
    }

    public Principal getPrincipal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, DataSession dataSession) throws BusinessException {
        Principal principal;

        Serializable sid = webSessionSidStorage.getSid(httpServletRequest, httpServletResponse);
        if (sid == null) {
            principal = null;
        } else {
            principal = authenticationManager.getPrincipalBySID(sid, dataSession);
        }

        return principal;
    }

    public void objectToHttpResponse(HttpResult httpResult, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
        httpServletResponse.setStatus(httpResult.getHttpSuccessStatus());
        MimeType mimeType;
        if (httpResult.isCache()) {
            cache(httpServletResponse);
        } else {
            noCache(httpServletResponse);
        }
        if (httpResult.getMimeType() == null) {
            mimeType = MimeType.JSON;
        } else {
            mimeType = httpResult.getMimeType();
        }
        httpServletResponse.setContentType(mimeType.getText());

        if (httpResult.getResult() != null) {
            switch (mimeType) {
                case JSON:
                    JsonWriter jsonWriter = jsonFactory.getJsonWriter(httpResult.getResultClass());
                    Expands expands = getRequestExpands(httpServletRequest);
                    BeanMapper beanMapper;
                    if (httpResult.getBeanMapper() != null) {
                        beanMapper = httpResult.getBeanMapper();
                    } else {
                        beanMapper = getEndPoint(httpServletRequest).getBeanMapper();
                    }
                    String jsonOut = jsonWriter.toJson(httpResult.getResult(), expands, beanMapper);
                    httpServletResponse.getWriter().println(jsonOut);
                    break;
                case OCTET_STREAM:
                case PDF:
                    if (httpResult.getResult() instanceof byte[]) {
                        byte[] result = (byte[]) httpResult.getResult();
                        httpServletResponse.getOutputStream().write(result);
                        httpServletResponse.getOutputStream().flush();
                        httpServletResponse.getOutputStream().close();
                    } else {
                        throw new RuntimeException("Si el MimeType es " + mimeType + " es tipo de result debe ser byte[] pero es " + httpResult.getResult().getClass().getName());
                    }

                    break;
                default:
                    throw new RuntimeException("MimeType no soportado:" + httpResult.getMimeType());
            }
        }
    }

    ;

    private void noCache(HttpServletResponse httpServletResponse) {
        httpServletResponse.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        httpServletResponse.addHeader("Cache-Control", "post-check=0, pre-check=0");
        httpServletResponse.setHeader("Pragma", "no-cache");
        httpServletResponse.setHeader("Expires", "Sat, 6 May 1995 12:00:00 GMT");        
    }

    private void cache(HttpServletResponse httpServletResponse) {
        cache(httpServletResponse, 60);
    }

    private void cache(HttpServletResponse httpServletResponse, long expireSeconds) {
        httpServletResponse.setHeader("Cache-Control", "private, no-transform, max-age=" + expireSeconds);
    }

    public void exceptionToHttpResponse(Throwable throwable, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        try {
            BusinessException businessException = ExceptionUtil.getBusinessExceptionFromThrowable(throwable);

            if (businessException != null) {

                if (businessException instanceof BusinessSecurityException) {
                    BusinessSecurityException businessSecurityException = (BusinessSecurityException) businessException;
                    logBusinessSecurityException.warn(getMapMessage(null,httpServletRequest),businessSecurityException);
                    try {
                        exceptionNotify.notify(businessSecurityException, httpServletRequest);
                    } catch (Exception ex) {
                        logException.error("Fallo la notificación",ex);
                    } 
                    
                    if (httpServletResponse.isCommitted()==false) {
                        httpServletResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        httpServletResponse.setContentType("text/plain; charset=UTF-8");
                    } else {
                        log.warn("La respuesta BusinessSecurityException isCommitted=true");
                    }
                    

                } else  {
                    if (httpServletResponse.isCommitted()==false) {
                        httpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        httpServletResponse.setContentType("application/json; charset=UTF-8");
                        httpServletResponse.getWriter().println(jsonFactory.getJsonWriter().toJson(businessException.getBusinessMessages()));
                    } else {
                        log.warn("La respuesta BusinessException isCommitted=true");
                    }
                }

            } else {
                logException.error(getMapMessage("Falló la llamada al servidor",httpServletRequest), throwable);
                try {
                    exceptionNotify.notify(throwable, httpServletRequest);
                } catch (Exception ex) {
                    logException.error("Fallo la notificación",ex);
                }

                if (httpServletResponse.isCommitted()==false) {
                    httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    httpServletResponse.setContentType("text/plain");
                    httpServletResponse.getWriter().println(throwable.getClass().getName());
                } else {
                    log.warn("La respuesta Exception isCommitted=true");
                }
                
            }
        } catch (Exception exception) {
            logException.error(getMapMessage("Falló al gestionar la excepción",httpServletRequest), exception);
            
            if (httpServletResponse.isCommitted()==false) {
                httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); 
            } else {
                log.warn("La respuesta gestion Exception isCommitted=true");
            }
        }

    }
    public EndPoint getEndPoint(HttpServletRequest httpServletRequest) {
        String path = (String) httpServletRequest.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String method = httpServletRequest.getMethod();
        EndPoint endPoint = EndPoint.getBestEndPoint(EndPoint.getMatchEndPoint(endPointsFactory.getEndPoints(), path, method), path, method);

        return endPoint;
    }

    public BeanMapper getBeanMapper(HttpServletRequest httpServletRequest) {
        EndPoint endPoint = getEndPoint(httpServletRequest);
        if (endPoint == null) {
            throw new RuntimeException("No existe el EndPoint");
        }

        return endPoint.getBeanMapper();

    }
    
    private MapMessage getMapMessage(String msg,HttpServletRequest httpServletRequest) {
        Map<String,String> map=new HashMap<>();
        
        map.put("message",msg);
        map.put("RemoteAddr",httpServletRequest.getRemoteAddr());
        map.put("RequestURI",httpServletRequest.getRequestURI());
        map.put("RequestURL",httpServletRequest.getRequestURL()+"");
        map.put("QueryString",httpServletRequest.getQueryString());
        map.put("Method",httpServletRequest.getMethod());
        Enumeration<String> names=httpServletRequest.getHeaderNames();
        while (names.hasMoreElements()) {
            String name=names.nextElement();
            String value=httpServletRequest.getHeader(name);

            map.put("Header_"+name,value);
        }
        
        return new MapMessage(map);
    }
    
}
