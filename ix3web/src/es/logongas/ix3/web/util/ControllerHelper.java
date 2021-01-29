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
import es.logongas.ix3.web.controllers.CrudRestController;
import es.logongas.ix3.web.controllers.endpoint.EndPoint;
import es.logongas.ix3.web.controllers.endpoint.EndPointsFactory;
import es.logongas.ix3.web.json.JsonFactory;
import es.logongas.ix3.web.json.JsonWriter;
import es.logongas.ix3.web.json.beanmapper.BeanMapper;
import es.logongas.ix3.web.json.beanmapper.Expands;
import es.logongas.ix3.web.security.WebSessionSidStorage;
import java.io.Serializable;
import java.util.Enumeration;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerMapping;

/**
 *
 * @author logongas
 */
public class ControllerHelper {
    
    private static final Log log = LogFactory.getLog(ControllerHelper.class);
    
    
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
                    log.warn("BusinessSecurityException:"+businessException.getLocalizedMessage());
                    
                    
                    httpServletResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    httpServletResponse.setContentType("text/plain; charset=UTF-8");
                    
                    try {
                        exceptionNotify.notify(businessSecurityException, httpServletRequest);
                    } catch (Exception ex) {

                    } 

                } else  {
                    httpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    httpServletResponse.setContentType("application/json; charset=UTF-8");
                    httpServletResponse.getWriter().println(jsonFactory.getJsonWriter().toJson(businessException.getBusinessMessages()));
                }

            } else {
                log.error("Falló la llamada al servidor:"+throwable.getLocalizedMessage()+getHttpRequestAsString(httpServletRequest), throwable);


                httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                httpServletResponse.setContentType("text/plain");
                httpServletResponse.getWriter().println(throwable.getClass().getName());
                
                
                try {
                    exceptionNotify.notify(throwable, httpServletRequest);
                } catch (Exception ex) {

                } 
                
            }
        } catch (Exception exception) {
            log.error("Falló al gestionar la excepción:"+ getHttpRequestAsString(httpServletRequest) , exception);
            log.error("Excepcion original:", throwable);

            httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            try {
                exceptionNotify.notify(exception, httpServletRequest);
            } catch (Exception ex) {
                
            }
            try {
                exceptionNotify.notify(throwable, httpServletRequest);
            } catch (Exception ex) {
                
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
    
    private String getHttpRequestAsString(HttpServletRequest httpServletRequest) {
        StringBuilder sb=new StringBuilder();
        sb.append("\n\tURL=");
        sb.append(httpServletRequest.getRequestURI()); 
        sb.append("\n\tQueryString=");
        sb.append(httpServletRequest.getQueryString()); 
        sb.append("\n\tMethod=");
        sb.append(httpServletRequest.getMethod()); 
        sb.append("\n\tHeaders:");
        Enumeration<String> names=httpServletRequest.getHeaderNames();
        while (names.hasMoreElements()) {
            String name=names.nextElement();
            String value=httpServletRequest.getHeader(name);

            sb.append("\n\t\t");
            sb.append(name);
            sb.append("=");
            sb.append(value);
         
        }

        return sb.toString();
    }
    
}
