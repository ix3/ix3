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
package es.logongas.ix3.web.security;

import es.logongas.ix3.dao.DataSession;
import es.logongas.ix3.core.Principal;
import es.logongas.ix3.security.authorization.AuthorizationInterceptor;
import es.logongas.ix3.security.authorization.AuthorizationManager;
import es.logongas.ix3.security.authorization.BusinessSecurityException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author logongas
 */
public class AuthorizationInterceptorImplURL implements AuthorizationInterceptor {
    
    private static final String SECURE_RESOURCE_TYPE_NAME = "URL";    
    
    @Autowired
    AuthorizationManager authorizationManager;

    
    public void checkAuthorized(Principal principal,HttpServletRequest httpServletRequest,HttpServletResponse httpServletResponse,DataSession dataSession) throws BusinessSecurityException {
        String secureResourceTypeName=SECURE_RESOURCE_TYPE_NAME;
        String secureResource=getSecureURI(httpServletRequest.getRequestURI(), httpServletRequest.getContextPath());
        String permissionName=httpServletRequest.getMethod();
        Object arguments=getArguments(httpServletRequest.getParameterMap()); 
        
        boolean isAuthorized=authorizationManager.authorized(principal,secureResourceTypeName, secureResource, permissionName, arguments, dataSession);
        
        if (isAuthorized==false) {
            throw new BusinessSecurityException("El usuario " + principal + " no tiene acceso a la URL:"+secureResource);
        } 
    } 
    
    /**
     * Esta funcición es para transformar en array de valores del Map en un String cuando solo hay un valor. Ya que es lo que suele pasar.
     * Ya que aunque haya un solo valor , siempre es un array y eso es un poco incomodo.
     * @param parameters
     * @return 
     */
    private Map<String,Object> getArguments(Map<String,String[]> parameters) {
        Map<String,Object> arguments=new HashMap<String,Object>();
        
        for(String key:parameters.keySet()) {
            String[] values=parameters.get(key);
            if (values==null) {
                arguments.put(key, null);
            } else if (values.length==1) {
                arguments.put(key, values[0]);
            } else {
                arguments.put(key, values);
            }
        }
        
        return arguments;
        
    }
    
    /**
     * Obtiene la URL pero si la parte del ContextPath De esa forma al
     * establecer la seguridad no tenemos que saber donde está desplegada la
     * aplicación
     *
     * @param uri
     * @param contextPath
     * @return
     */
    private String getSecureURI(String uri, String contextPath) {
        int beginIndex;
        if (contextPath == null) {
            beginIndex = 0;
        } else {
            beginIndex = contextPath.length();
            if (uri.startsWith(contextPath) == false) {
                throw new RuntimeException("uri no empieza por '" + contextPath + "':" + uri);
            }
        }

        String secureURI = uri.substring(beginIndex);

        if (secureURI.startsWith("/") == false) {
            throw new RuntimeException("secureURI no empieza por '/':" + secureURI);
        }

        return secureURI;
    }
    
}
