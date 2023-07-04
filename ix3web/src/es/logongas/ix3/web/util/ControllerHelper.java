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
import es.logongas.ix3.web.controllers.endpoint.EndPoint;
import es.logongas.ix3.web.controllers.endpoint.EndPointsFactory;
import es.logongas.ix3.web.json.JsonFactory;
import es.logongas.ix3.web.json.JsonWriter;
import es.logongas.ix3.web.json.beanmapper.BeanMapper;
import es.logongas.ix3.web.json.beanmapper.Expands;
import es.logongas.ix3.web.security.WebSessionSidStorage;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerMapping;

/**
 *
 * @author logongas
 */
public class ControllerHelper {
    
    private static final Logger log = LogManager.getLogger(ControllerHelper.class);
 
    
    private final String PARAMETER_EXPAND = "$expand";

    @Autowired
    private EndPointsFactory endPointsFactory;
    @Autowired
    private JsonFactory jsonFactory;
    @Autowired
    private WebSessionSidStorage webSessionSidStorage;
    @Autowired
    private AuthenticationManager authenticationManager;

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
    
    public void refreshSession(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, DataSession dataSession) throws BusinessException {

        Serializable sid = webSessionSidStorage.getSid(httpServletRequest, httpServletResponse);
        if (sid != null) {
            webSessionSidStorage.setSid(httpServletRequest, httpServletResponse, sid);
        }

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
    
    
    public String getParameter(HttpServletRequest httpServletRequest,String parameterName) {

        String valueOriginal=httpServletRequest.getParameter(parameterName);
        String valueUTF8=changeCharset(valueOriginal,StandardCharsets.ISO_8859_1,StandardCharsets.UTF_8);

        return valueUTF8;

    }
    
    public Map<String, String[]> getParameterMap(HttpServletRequest httpServletRequest) {

        Map<String, String[]> parametersUTF8=new HashMap<>();

        Map<String, String[]> parametersOriginal=httpServletRequest.getParameterMap();

        for (String parameterName:parametersOriginal.keySet()) {
            String[] valuesOriginal=parametersOriginal.get(parameterName);
            String[] valuesUTF8=new String[valuesOriginal.length];

            for (int i=0;i<valuesOriginal.length;i++) {
                valuesUTF8[i]=changeCharset(valuesOriginal[i],StandardCharsets.ISO_8859_1,StandardCharsets.UTF_8);
            }


            parametersUTF8.put(parameterName, valuesUTF8);
        }


        return parametersUTF8;

    }
    
    
    private String changeCharset(String s,Charset currentCharset,Charset newCharset) {
        if (s==null) {
            return null;
        }
        
        return new String(s.getBytes(currentCharset),newCharset);
    }
    
}
