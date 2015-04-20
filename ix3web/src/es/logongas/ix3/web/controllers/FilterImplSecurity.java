/*
 * Copyright 2013 Lorenzo González.
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
import es.logongas.ix3.security.authentication.AuthenticationManager;
import es.logongas.ix3.security.authentication.Principal;
import es.logongas.ix3.security.authorization.AuthorizationManager;
import es.logongas.ix3.security.util.PrincipalLocator;
import java.io.IOException;
import java.io.Serializable;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Filtro web para controlar las peteciones en función del sistema de seguridad
 *
 * @author Lorenzo González
 */
public class FilterImplSecurity implements Filter {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    AuthorizationManager authorizationManager;
    
    @Autowired
    PrincipalLocator principalLocator;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;

        String uri = httpServletRequest.getRequestURI();
        String method = httpServletRequest.getMethod();

        Principal principal;
        HttpSession httpSession = httpServletRequest.getSession();
        if (httpSession != null) {
            Serializable sid = (Serializable) httpSession.getAttribute("sid");
            if (sid == null) {
                principal = null;
            } else {
                try {
                    principal = authenticationManager.getPrincipalBySID(sid);
                } catch (BusinessException ex) {
                    httpServletResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }
            }
        } else {
            principal = null;
        }

        if (authorizationManager.authorized(principal, "URL", getSecureURI(uri, httpServletRequest.getContextPath()), method, httpServletRequest.getParameterMap()) == true) {
            try {
                principalLocator.bindPrincipal(principal);
                filterChain.doFilter(servletRequest, servletResponse);
            }finally {
                principalLocator.unbindPrincipal();
            }
        } else {
            httpServletResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
        }
    }

    @Override
    public void destroy() {
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
