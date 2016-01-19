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
import es.logongas.ix3.dao.DataSession;
import es.logongas.ix3.dao.DataSessionFactory;
import es.logongas.ix3.security.authentication.AuthenticationManager;
import es.logongas.ix3.core.Principal;
import es.logongas.ix3.web.json.JsonFactory;
import es.logongas.ix3.web.util.ControllerHelper;
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
public final class FilterImplSecurity implements Filter {

    @Autowired
    AuthorizationInterceptorImplURL authorizationInterceptorImplURL;


    @Autowired
    ControllerHelper controllerHelper;
    @Autowired
    DataSessionFactory dataSessionFactory;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;

        try {

            try (DataSession dataSession = dataSessionFactory.getDataSession()) {
                Principal principal = controllerHelper.getPrincipal(httpServletRequest, httpServletResponse, dataSession);
                authorizationInterceptorImplURL.checkAuthorized(principal, httpServletRequest, httpServletResponse, dataSession);
            }
            filterChain.doFilter(servletRequest, servletResponse);
        } catch (Exception ex) {
            controllerHelper.exceptionToHttpResponse(ex, httpServletResponse);
        }
    }

    @Override
    public void destroy() {
    }



}
