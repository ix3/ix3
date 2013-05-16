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

import es.logongas.ix3.security.services.authentication.AuthenticationManager;
import es.logongas.ix3.security.services.authentication.User;
import es.logongas.ix3.security.services.authorization.AuthorizationManager;
import java.io.IOException;
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
 * @author Lorenzo González
 */
public class FilterImplSecurity implements Filter {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    AuthorizationManager authorizationManager;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;

            String uri = httpServletRequest.getRequestURI();
            String method = httpServletRequest.getMethod();

            User user;
            Integer idUser = (Integer) httpServletRequest.getSession().getAttribute("idUser");
            if (idUser == null) {
                user = null;
            } else {
                user = authenticationManager.getUserByIdUser(idUser);
            }

            if (authorizationManager.authorized(user, "URL", uri, method) == true) {
                filterChain.doFilter(servletRequest, servletResponse);
            } else {
                httpServletResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            }
    }

    @Override
    public void destroy() {
    }

}
