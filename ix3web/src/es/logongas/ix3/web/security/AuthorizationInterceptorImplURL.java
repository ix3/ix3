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

import es.logongas.ix3.security.authentication.Principal;
import es.logongas.ix3.security.authorization.AuthorizationInterceptor;
import es.logongas.ix3.security.authorization.AuthorizationManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author logongas
 */
public class AuthorizationInterceptorImplURL implements AuthorizationInterceptor {
    
    @Autowired
    AuthorizationManager authorizationManager;

    
    public boolean checkAuthorized(Principal principal,HttpServletRequest httpServletRequest,HttpServletResponse httpServletResponse) {
        String uri = httpServletRequest.getRequestURI();
        String method = httpServletRequest.getMethod();        
        
        boolean isAuthorized=authorizationManager.authorized(principal, "URL", getSecureURI(uri, httpServletRequest.getContextPath()), method, httpServletRequest.getParameterMap());
        
        return isAuthorized;
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
