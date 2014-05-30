/*
 * Copyright 2013 Lorenzo González.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package es.logongas.ix3.web.controllers;


import es.logongas.ix3.core.BusinessException;
import es.logongas.ix3.core.BusinessMessage;
import es.logongas.ix3.dao.DAOFactory;
import es.logongas.ix3.security.impl.authentication.CredentialImplLoginPassword;
import es.logongas.ix3.security.services.authentication.AuthenticationManager;
import es.logongas.ix3.security.services.authentication.Principal;
import es.logongas.ix3.web.services.json.JsonFactory;
import es.logongas.ix3.web.services.json.JsonWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Controlador REST que gestion ael log-in, log-out.
 * @author Lorenzo González
 */
@Controller
public class SesionController {

    @Autowired
    DAOFactory daoFactory;

    @Autowired
    JsonFactory jsonFactory;

    @Autowired
    AuthenticationManager authenticationManager;


    @RequestMapping(value = {"/session"}, method = RequestMethod.POST, headers = "Accept=application/json")
    public void login(HttpServletRequest request, HttpServletResponse httpServletResponse) {
        try {
            request.setCharacterEncoding("UTF-8");
            
            String login = request.getParameter("login");
            String password = request.getParameter("password");

            CredentialImplLoginPassword credentialImplLoginPassword=new CredentialImplLoginPassword(login, password);


            Principal principal=authenticationManager.authenticate(credentialImplLoginPassword);

            if (principal==null) {
                throw new BusinessException(new BusinessMessage(null, "El usuario o contraseña no son válidos"));
            }

            //Creamos la sesión y la el sid
            HttpSession httpSession = request.getSession();
            httpSession.setAttribute("sid", principal.getSid());


            //Retornamos el user
            JsonWriter jsonWriter = jsonFactory.getJsonWriter(Principal.class);
            String datos = jsonWriter.toJson(principal);
            httpServletResponse.setStatus(HttpServletResponse.SC_OK);
            httpServletResponse.setContentType("application/json; charset=UTF-8");
            httpServletResponse.getWriter().println(datos);



        } catch (BusinessException be) {
            JsonWriter jsonWriter = jsonFactory.getJsonWriter(BusinessMessage.class);
            Collection<BusinessMessage> businessMessages = be.getBusinessMessages();
            String jsonBusinessMessages = jsonWriter.toJson(businessMessages);

            httpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            httpServletResponse.setContentType("application/json; charset=UTF-8");
            try {
                httpServletResponse.getWriter().println(jsonBusinessMessages);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        } catch (Exception ex) {
            httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            httpServletResponse.setContentType("text/plain; charset=UTF-8");
            try {
                ex.printStackTrace(httpServletResponse.getWriter());
            } catch (IOException ex1) {
                throw new RuntimeException(ex1);
            }
        }
    }

    @RequestMapping(value = {"/session"}, method = RequestMethod.GET, headers = "Accept=application/json")
    public void logged(HttpServletRequest request, HttpServletResponse httpServletResponse) {
        try {
            Principal principal;

            HttpSession httpSession = request.getSession();
            Serializable sid = (Serializable) httpSession.getAttribute("sid");

            if (sid == null) {
                principal = null;
            } else {
                principal = authenticationManager.getPrincipalBySID(sid);
            }

            if (principal != null) {
                JsonWriter jsonWriter = jsonFactory.getJsonWriter(Principal.class);
                String datos = jsonWriter.toJson(principal);

                httpServletResponse.setStatus(HttpServletResponse.SC_OK);
                httpServletResponse.setContentType("application/json; charset=UTF-8");
                httpServletResponse.getWriter().println(datos);
            } else {
                httpServletResponse.setStatus(HttpServletResponse.SC_NO_CONTENT);
            }
        } catch (Exception ex) {
            httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            httpServletResponse.setContentType("text/plain; charset=UTF-8");
            try {
                ex.printStackTrace(httpServletResponse.getWriter());
            } catch (IOException ex1) {
                throw new RuntimeException(ex1);
            }
        }
    }

    @RequestMapping(value = {"/session"}, method = RequestMethod.DELETE)
    public void logout(HttpServletRequest request, HttpServletResponse httpServletResponse) {
        HttpSession httpSession = request.getSession();
        httpSession.setAttribute("sid", null);

        httpServletResponse.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }
}
