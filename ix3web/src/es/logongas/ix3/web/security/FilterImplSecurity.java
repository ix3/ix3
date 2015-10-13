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
package es.logongas.ix3.web.security;

import es.logongas.ix3.core.BusinessException;
import es.logongas.ix3.security.authentication.AuthenticationManager;
import es.logongas.ix3.security.authentication.Principal;
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
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Filtro web para controlar las peteciones en función del sistema de seguridad
 *
 * @author Lorenzo González
 */
public class FilterImplSecurity implements Filter {

    @Autowired
    AuthorizationInterceptorImplURL authorizationInterceptorImplURL;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    PrincipalLocator principalLocator;

    @Autowired
    WebSessionSidStorage webSessionSidStorage;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;

        try {
            Principal principal = getPrincipal(httpServletRequest, httpServletResponse);
            if (authorizationInterceptorImplURL.checkAuthorized(principal, httpServletRequest, httpServletResponse) == true) {
                try {
                    principalLocator.bindPrincipal(principal);
                    filterChain.doFilter(servletRequest, servletResponse);
                } finally {
                    principalLocator.unbindPrincipal();
                }
            } else {
                httpServletResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            }
        } catch (BusinessException ex) {
            httpServletResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
        }

    }

    @Override
    public void destroy() {
    }

    private Principal getPrincipal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws BusinessException {
        Principal principal;

        Serializable sid = webSessionSidStorage.getSid(httpServletRequest, httpServletResponse);
        if (sid == null) {
            principal = null;
        } else {
            principal = authenticationManager.getPrincipalBySID(sid);
        }

        return principal;
    }

}
