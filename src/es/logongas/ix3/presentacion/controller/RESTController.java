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
import es.logongas.ix3.persistencia.services.dao.BussinessException;
import es.logongas.ix3.persistencia.services.dao.DAOFactory;
import es.logongas.ix3.persistencia.services.dao.GenericDAO;
import es.logongas.ix3.persistencia.services.metadata.MetaData;
import es.logongas.ix3.persistencia.services.metadata.EntityMetaDataFactory;
import es.logongas.ix3.presentacion.json.JsonTransformer;
import es.logongas.ix3.presentacion.json.JsonTransformerFactory;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
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
    @Autowired
    ConversionService conversionService;
    @Autowired
    JsonTransformerFactory jsonTransformerFactory;

    @RequestMapping(value = {"/{entityName}/metadata"}, method = RequestMethod.GET, consumes = "application/json")
    public void metadata(HttpServletRequest httpRequest, HttpServletResponse httpServletResponse, @PathVariable("entityName") String entityName) {

        MetaData entityMetaData = entityMetaDataFactory.getEntityMetaData(entityName);
        JsonTransformer jsonTransformer = jsonTransformerFactory.getJsonTransformer(entityMetaData.getType());

        httpServletResponse.setContentType("application/json; charset=UTF-8");

        try {
            String msg = jsonTransformer.toJson(entityMetaData);
            httpServletResponse.getWriter().println(msg);
        } catch (Exception ex) {
            httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try {
                String msg = jsonTransformer.toJson(ex.getStackTrace());
                httpServletResponse.getWriter().println(msg);
            } catch (Exception ex2) {
            }
        }
    }

    @RequestMapping(value = {"/{entityName}/search"}, method = RequestMethod.GET, consumes = "application/json")
    public void search(HttpServletRequest httpRequest, HttpServletResponse httpServletResponse, @PathVariable("entityName") String entityName) {
        GenericDAO genericDAO = daoFactory.getDAO(entityName);
        MetaData entityMetaData = entityMetaDataFactory.getEntityMetaData(entityName);
        JsonTransformer jsonTransformer = jsonTransformerFactory.getJsonTransformer(entityMetaData.getType());

        httpServletResponse.setContentType("application/json; charset=UTF-8");

        try {
            Map<String, Object> filter = new HashMap<>();


            Enumeration<String> enumeration = httpRequest.getAttributeNames();
            while (enumeration.hasMoreElements()) {
                String propertyName = enumeration.nextElement();
                Class propertyType = entityMetaData.getPropertiesMetaData().get(propertyName).getType();
                Object value = conversionService.convert(httpRequest.getParameter(propertyName), propertyType);

                filter.put(propertyName, value);
            }

            Object entity = genericDAO.search(filter);

            String msg = jsonTransformer.toJson(entity);
            httpServletResponse.getWriter().println(msg);

        } catch (BussinessException ex) {
            try {
                String msg = jsonTransformer.toJson(ex.getBussinessMessages());
                httpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                httpServletResponse.getWriter().println(msg);
            } catch (Exception ex2) {
                httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } catch (Exception ex) {
            httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try {
                String msg = jsonTransformer.toJson(ex.getStackTrace());
                httpServletResponse.getWriter().println(msg);
            } catch (Exception ex2) {
            }
        }
    }

    @RequestMapping(value = {"/{entityName}/{id}"}, method = RequestMethod.GET, consumes = "application/json")
    public void get(HttpServletRequest httpRequest, HttpServletResponse httpServletResponse, @PathVariable("entityName") String entityName, @PathVariable("id") int id) {
        GenericDAO genericDAO = daoFactory.getDAO(entityName);
        MetaData entityMetaData = entityMetaDataFactory.getEntityMetaData(entityName);
        JsonTransformer jsonTransformer = jsonTransformerFactory.getJsonTransformer(entityMetaData.getType());

        httpServletResponse.setContentType("application/json; charset=UTF-8");

        try {

            Object entity = genericDAO.read(id);

            String msg = jsonTransformer.toJson(entity);
            httpServletResponse.getWriter().println(msg);

        } catch (BussinessException ex) {
            try {
                String msg = jsonTransformer.toJson(ex.getBussinessMessages());
                httpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                httpServletResponse.getWriter().println(msg);
            } catch (Exception ex2) {
                httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } catch (Exception ex) {
            httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try {
                String msg = jsonTransformer.toJson(ex.getStackTrace());
                httpServletResponse.getWriter().println(msg);
            } catch (Exception ex2) {
            }
        }
    }

    @RequestMapping(value = {"/{entityName}/"}, method = RequestMethod.GET, consumes = "application/json")
    public void create(HttpServletRequest httpRequest, HttpServletResponse httpServletResponse, @PathVariable("entityName") String entityName) {
        GenericDAO genericDAO = daoFactory.getDAO(entityName);
        MetaData entityMetaData = entityMetaDataFactory.getEntityMetaData(entityName);
        JsonTransformer jsonTransformer = jsonTransformerFactory.getJsonTransformer(entityMetaData.getType());

        httpServletResponse.setContentType("application/json; charset=UTF-8");

        try {

            Object entity = genericDAO.create();
            String msg = jsonTransformer.toJson(entity);
            httpServletResponse.getWriter().println(msg);

        } catch (BussinessException ex) {
            try {
                String msg = jsonTransformer.toJson(ex.getBussinessMessages());
                httpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                httpServletResponse.getWriter().println(msg);
            } catch (Exception ex2) {
                httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } catch (Exception ex) {
            httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try {
                String msg = jsonTransformer.toJson(ex.getStackTrace());
                httpServletResponse.getWriter().println(msg);
            } catch (Exception ex2) {
            }
        }

    }

    @RequestMapping(value = {"/{entityName}/"}, method = RequestMethod.POST, consumes = "application/json")
    public void insert(HttpServletRequest httpRequest, HttpServletResponse httpServletResponse, @PathVariable("entityName") String entityName, @RequestBody String jsonEntity) {
        GenericDAO genericDAO = daoFactory.getDAO(entityName);
        MetaData entityMetaData = entityMetaDataFactory.getEntityMetaData(entityName);
        JsonTransformer jsonTransformer = jsonTransformerFactory.getJsonTransformer(entityMetaData.getType());

        httpServletResponse.setContentType("application/json; charset=UTF-8");

        try {

            Object entity = jsonTransformer.fromJson(jsonEntity);

            genericDAO.insert(entity);
            String msg = jsonTransformer.toJson(entity);
            httpServletResponse.getWriter().println(msg);

        } catch (BussinessException ex) {
            try {
                String msg = jsonTransformer.toJson(ex.getBussinessMessages());
                httpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                httpServletResponse.getWriter().println(msg);
            } catch (Exception ex2) {
                httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } catch (Exception ex) {
            httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try {
                String msg = jsonTransformer.toJson(ex.getStackTrace());
                httpServletResponse.getWriter().println(msg);
            } catch (Exception ex2) {
            }
        }
    }

    @RequestMapping(value = {"/{entityName}/{id}"}, method = RequestMethod.PUT, consumes = "application/json")
    public void update(HttpServletRequest httpRequest, HttpServletResponse httpServletResponse, @PathVariable("entityName") String entityName, @PathVariable("id") int id, @RequestBody String jsonEntity) {
        GenericDAO genericDAO = daoFactory.getDAO(entityName);
        MetaData entityMetaData = entityMetaDataFactory.getEntityMetaData(entityName);
        JsonTransformer jsonTransformer = jsonTransformerFactory.getJsonTransformer(entityMetaData.getType());

        httpServletResponse.setContentType("application/json; charset=UTF-8");

        try {

            Object entity = jsonTransformer.fromJson(jsonEntity);

            genericDAO.update(entity);
            String msg = jsonTransformer.toJson(entity);
            httpServletResponse.getWriter().println(msg);

        } catch (BussinessException ex) {
            try {
                String msg = jsonTransformer.toJson(ex.getBussinessMessages());
                httpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                httpServletResponse.getWriter().println(msg);
            } catch (Exception ex2) {
                httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } catch (Exception ex) {
            try {
                httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                String msg = jsonTransformer.toJson(ex.getStackTrace());
            } catch (Exception ex2) {
            }
        }
    }

    @RequestMapping(value = {"/{entityName}/{id}"}, method = RequestMethod.DELETE, consumes = "application/json")
    public void delete(HttpServletRequest httpRequest, HttpServletResponse httpServletResponse, @PathVariable("entityName") String entityName, @PathVariable("id") int id) {
        GenericDAO genericDAO = daoFactory.getDAO(entityName);
        MetaData entityMetaData = entityMetaDataFactory.getEntityMetaData(entityName);
        JsonTransformer jsonTransformer = jsonTransformerFactory.getJsonTransformer(entityMetaData.getType());

        httpServletResponse.setContentType("application/json; charset=UTF-8");


        try {
            genericDAO.delete(id);
            String msg = jsonTransformer.toJson(null);
            httpServletResponse.getWriter().println(msg);
        } catch (BussinessException ex) {
            try {
                String msg = jsonTransformer.toJson(ex.getBussinessMessages());
                httpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                httpServletResponse.getWriter().println(msg);
            } catch (Exception ex2) {
                httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } catch (Exception ex) {
            httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try {
                String msg = jsonTransformer.toJson(ex.getStackTrace());
                httpServletResponse.getWriter().println(msg);
            } catch (Exception ex2) {
            }
        }
    }
}
