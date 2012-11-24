/*
 * Copyright 2012 Lorenzo González.
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
package es.logongas.ix3.presentacion.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.logongas.ix3.persistencia.dao.BussinessException;
import es.logongas.ix3.persistencia.dao.Criteria;
import es.logongas.ix3.persistencia.dao.DAOFactory;
import es.logongas.ix3.persistencia.dao.GenericDAO;
import es.logongas.ix3.persistencia.metadata.EntityMetaData;
import es.logongas.ix3.persistencia.metadata.EntityMetaDataFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 *
 * @author Lorenzo González
 */
@Controller
public class RESTController {

    @Autowired
    DAOFactory daoFactory;
    
    @Autowired
    EntityMetaDataFactory entityMetaDataFactory;    
    
    ObjectMapper objectMapper = new ObjectMapper();

     @RequestMapping(value = {"/{entityName}/search"}, method = RequestMethod.GET, consumes = "application/json")
    public void search(HttpServletRequest httpRequest, HttpServletResponse httpServletResponse, @PathVariable("entityName") String entityName) {

        httpServletResponse.setContentType("application/json; charset=UTF-8");

        try {
            List<Criteria> criterias=new ArrayList<>();
            
            GenericDAO genericDAO = daoFactory.getDAO(entityName);

            Enumeration<String> enumeration=httpRequest.getAttributeNames();
            while (enumeration.hasMoreElements()) {
                String propertyName=enumeration.nextElement();
                
                
            }
            
            Object entity = genericDAO.search(criterias);

            String msg = objectMapper.writeValueAsString(entity);
            httpServletResponse.getWriter().println(msg);

        } catch (BussinessException ex) {
            try {
                String msg = objectMapper.writeValueAsString(ex.getBussinessMessages());
                httpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                httpServletResponse.getWriter().println(msg);
            } catch (Exception ex2) {
                httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } catch (Exception ex) {
            httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try {
                String msg = objectMapper.writeValueAsString(ex.getStackTrace());
                httpServletResponse.getWriter().println(msg);
            } catch (Exception ex2) {
                
            }
        }
    }   
    
    
    @RequestMapping(value = {"/{entityName}/{id}"}, method = RequestMethod.GET, consumes = "application/json")
    public void get(HttpServletRequest httpRequest, HttpServletResponse httpServletResponse, @PathVariable("entityName") String entityName, @PathVariable("id") int id) {

        httpServletResponse.setContentType("application/json; charset=UTF-8");

        try {
            GenericDAO genericDAO = daoFactory.getDAO(entityName);

            Object entity = genericDAO.read(id);

            String msg = objectMapper.writeValueAsString(entity);
            httpServletResponse.getWriter().println(msg);

        } catch (BussinessException ex) {
            try {
                String msg = objectMapper.writeValueAsString(ex.getBussinessMessages());
                httpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                httpServletResponse.getWriter().println(msg);
            } catch (Exception ex2) {
                httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } catch (Exception ex) {
            httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try {
                String msg = objectMapper.writeValueAsString(ex.getStackTrace());
                httpServletResponse.getWriter().println(msg);
            } catch (Exception ex2) {
                
            }
        }
    }

    @RequestMapping(value = {"/{entityName}/"}, method = RequestMethod.GET, consumes = "application/json")
    public void create(HttpServletRequest httpRequest, HttpServletResponse httpServletResponse, @PathVariable("entityName") String entityName) {

        httpServletResponse.setContentType("application/json; charset=UTF-8");

        try {
            GenericDAO genericDAO = daoFactory.getDAO(entityName);

            Object entity = genericDAO.create();
            String msg = objectMapper.writeValueAsString(entity);
            httpServletResponse.getWriter().println(msg);

        } catch (BussinessException ex) {
            try {
                String msg = objectMapper.writeValueAsString(ex.getBussinessMessages());
                httpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                httpServletResponse.getWriter().println(msg);
            } catch (Exception ex2) {
                httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } catch (Exception ex) {
            httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try {
                String msg = objectMapper.writeValueAsString(ex.getStackTrace());
                httpServletResponse.getWriter().println(msg);
            } catch (Exception ex2) {
                
            }
        }

    }

    @RequestMapping(value = {"/{entityName}/"}, method = RequestMethod.POST, consumes = "application/json")
    public void insert(HttpServletRequest httpRequest, HttpServletResponse httpServletResponse, @PathVariable("entityName") String entityName, @RequestBody String jsonEntity) {

        httpServletResponse.setContentType("application/json; charset=UTF-8");

        try {
            GenericDAO genericDAO = daoFactory.getDAO(entityName);
            EntityMetaData entityMetaData=entityMetaDataFactory.getEntityMetaData(entityName);
            
            Object entity = objectMapper.readValue(jsonEntity, entityMetaData.getEntiyType());

            genericDAO.insert(entity);
            String msg = objectMapper.writeValueAsString(entity);
            httpServletResponse.getWriter().println(msg);

        } catch (BussinessException ex) {
            try {
                String msg = objectMapper.writeValueAsString(ex.getBussinessMessages());
                httpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                httpServletResponse.getWriter().println(msg);
            } catch (Exception ex2) {
                httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } catch (Exception ex) {
            httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try {
                String msg = objectMapper.writeValueAsString(ex.getStackTrace());
                httpServletResponse.getWriter().println(msg);
            } catch (Exception ex2) {
                
            }
        }
    }

    @RequestMapping(value = {"/{entityName}/{id}"}, method = RequestMethod.PUT, consumes = "application/json")
    public void update(HttpServletRequest httpRequest, HttpServletResponse httpServletResponse, @PathVariable("entityName") String entityName, @PathVariable("id") int id, @RequestBody String jsonEntity) {

        httpServletResponse.setContentType("application/json; charset=UTF-8");

        try {
            GenericDAO genericDAO = daoFactory.getDAO(entityName);
            EntityMetaData entityMetaData=entityMetaDataFactory.getEntityMetaData(entityName);
            
            Object entity = objectMapper.readValue(jsonEntity, entityMetaData.getEntiyType());

            genericDAO.update(entity);
            String msg = objectMapper.writeValueAsString(entity);
            httpServletResponse.getWriter().println(msg);

        } catch (BussinessException ex) {
            try {
                String msg = objectMapper.writeValueAsString(ex.getBussinessMessages());
                httpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                httpServletResponse.getWriter().println(msg);
            } catch (Exception ex2) {
                httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } catch (Exception ex) {
            httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try {
                String msg = objectMapper.writeValueAsString(ex.getStackTrace());
                httpServletResponse.getWriter().println(msg);
            } catch (Exception ex2) {
                
            }
        }
    }

    @RequestMapping(value = {"/{entityName}/{id}"}, method = RequestMethod.DELETE, consumes = "application/json")
    public void delete(HttpServletRequest httpRequest, HttpServletResponse httpServletResponse, @PathVariable("entityName") String entityName, @PathVariable("id") int id) {

        httpServletResponse.setContentType("application/json; charset=UTF-8");


        try {
            GenericDAO genericDAO = daoFactory.getDAO(entityName);

            genericDAO.delete(id);
            String msg = objectMapper.writeValueAsString(null);
            httpServletResponse.getWriter().println(msg);

        } catch (BussinessException ex) {
            try {
                String msg = objectMapper.writeValueAsString(ex.getBussinessMessages());
                httpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                httpServletResponse.getWriter().println(msg);
            } catch (Exception ex2) {
                httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } catch (Exception ex) {
            httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try {
                String msg = objectMapper.writeValueAsString(ex.getStackTrace());
                httpServletResponse.getWriter().println(msg);
            } catch (Exception ex2) {
                
            }
        }
    }
}
