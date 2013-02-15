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

import es.logongas.ix3.persistencia.services.dao.BussinessException;
import es.logongas.ix3.persistencia.services.dao.DAOFactory;
import es.logongas.ix3.persistencia.services.dao.GenericDAO;
import es.logongas.ix3.persistencia.services.metadata.MetaDataFactory;
import es.logongas.ix3.persistencia.services.metadata.MetaData;
import es.logongas.ix3.presentacion.json.JsonTransformer;
import es.logongas.ix3.presentacion.json.JsonTransformerFactory;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
    MetaDataFactory metaDataFactory;
    @Autowired
    ConversionService conversionService;
    @Autowired
    JsonTransformerFactory jsonTransformerFactory;
    
    private static Log log = LogFactory.getLog(RESTController.class);

    @RequestMapping(value = {"/{entityName}/metadata"}, method = RequestMethod.GET, consumes = "application/json")
    public void metadata(HttpServletRequest httpRequest, HttpServletResponse httpServletResponse, @PathVariable("entityName") String entityName) {

        MetaData metaData = metaDataFactory.getMetaData(entityName);
        JsonTransformer jsonTransformer = jsonTransformerFactory.getJsonTransformer(MetaData.class);

        httpServletResponse.setContentType("application/json; charset=UTF-8");

        try {

            String msg = jsonTransformer.toJson(metaData);
            httpServletResponse.setStatus(HttpServletResponse.SC_OK);
            httpServletResponse.setContentType("application/json; charset=UTF-8");
            httpServletResponse.getWriter().println(msg);
        } catch (Exception ex) {
            httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            httpServletResponse.setContentType("text/plain");
            try {
                ex.printStackTrace(httpServletResponse.getWriter());
            } catch (Exception ex2) {
                log.error("Falló al imprimir la traza", ex2);
            }
        }
    }

    @RequestMapping(value = {"/{entityName}/search"}, method = RequestMethod.GET, consumes = "application/json")
    public void search(HttpServletRequest httpRequest, HttpServletResponse httpServletResponse, @PathVariable("entityName") String entityName) {
        MetaData metaData = metaDataFactory.getMetaData(entityName);
        GenericDAO genericDAO = daoFactory.getDAO(metaData.getType());
        JsonTransformer jsonTransformer = jsonTransformerFactory.getJsonTransformer(metaData.getType());

        httpServletResponse.setContentType("application/json; charset=UTF-8");

        try {
            Map<String, Object> filter = new HashMap<>();


            Enumeration<String> enumeration = httpRequest.getAttributeNames();
            while (enumeration.hasMoreElements()) {
                String propertyName = enumeration.nextElement();
                Class propertyType = metaData.getPropertiesMetaData().get(propertyName).getType();
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
                httpServletResponse.setContentType("application/json; charset=UTF-8");
                httpServletResponse.getWriter().println(msg);
            } catch (Exception ex2) {
                httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                httpServletResponse.setContentType("text/plain");
                try {
                    ex.printStackTrace(httpServletResponse.getWriter());
                } catch (Exception ex3) {
                    log.error("Falló al imprimir la traza", ex3);
                }
            }
        } catch (Exception ex) {
            httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            httpServletResponse.setContentType("text/plain");
            try {
                ex.printStackTrace(httpServletResponse.getWriter());
            } catch (Exception ex2) {
                log.error("Falló al imprimir la traza", ex2);
            }
        }
    }

    @RequestMapping(value = {"/{entityName}/{id}"}, method = RequestMethod.GET, consumes = "application/json")
    public void get(HttpServletRequest httpRequest, HttpServletResponse httpServletResponse, @PathVariable("entityName") String entityName, @PathVariable("id") int id) {
        MetaData metaData = metaDataFactory.getMetaData(entityName);
        GenericDAO genericDAO = daoFactory.getDAO(metaData.getType());
        JsonTransformer jsonTransformer = jsonTransformerFactory.getJsonTransformer(metaData.getType());

        httpServletResponse.setContentType("application/json; charset=UTF-8");

        try {

            Object entity = genericDAO.read(id);

            String msg = jsonTransformer.toJson(entity);
            httpServletResponse.getWriter().println(msg);

        } catch (BussinessException ex) {
            try {
                String msg = jsonTransformer.toJson(ex.getBussinessMessages());
                httpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                httpServletResponse.setContentType("application/json; charset=UTF-8");
                httpServletResponse.getWriter().println(msg);
            } catch (Exception ex2) {
                httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                httpServletResponse.setContentType("text/plain");
                try {
                    ex.printStackTrace(httpServletResponse.getWriter());
                } catch (Exception ex3) {
                    log.error("Falló al imprimir la traza", ex3);
                }
            }
        } catch (Exception ex) {
            httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            httpServletResponse.setContentType("text/plain");
            try {
                ex.printStackTrace(httpServletResponse.getWriter());
            } catch (Exception ex2) {
                log.error("Falló al imprimir la traza", ex2);
            }
        }
    }

    @RequestMapping(value = {"/{entityName}/"}, method = RequestMethod.GET, consumes = "application/json")
    public void create(HttpServletRequest httpRequest, HttpServletResponse httpServletResponse, @PathVariable("entityName") String entityName) {
        MetaData metaData = metaDataFactory.getMetaData(entityName);
        GenericDAO genericDAO = daoFactory.getDAO(metaData.getType());
        JsonTransformer jsonTransformer = jsonTransformerFactory.getJsonTransformer(metaData.getType());

        httpServletResponse.setContentType("application/json; charset=UTF-8");

        try {

            Object entity = genericDAO.create();
            String msg = jsonTransformer.toJson(entity);
            httpServletResponse.getWriter().println(msg);

        } catch (BussinessException ex) {
            try {
                String msg = jsonTransformer.toJson(ex.getBussinessMessages());
                httpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                httpServletResponse.setContentType("application/json; charset=UTF-8");
                httpServletResponse.getWriter().println(msg);
            } catch (Exception ex2) {
                httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                httpServletResponse.setContentType("text/plain");
                try {
                    ex.printStackTrace(httpServletResponse.getWriter());
                } catch (Exception ex3) {
                    log.error("Falló al imprimir la traza", ex3);
                }
            }
        } catch (Exception ex) {
            httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            httpServletResponse.setContentType("text/plain");
            try {
                ex.printStackTrace(httpServletResponse.getWriter());
            } catch (Exception ex2) {
                log.error("Falló al imprimir la traza", ex2);
            }
        }

    }

    @RequestMapping(value = {"/{entityName}/"}, method = RequestMethod.POST, consumes = "application/json")
    public void insert(HttpServletRequest httpRequest, HttpServletResponse httpServletResponse, @PathVariable("entityName") String entityName, @RequestBody String jsonEntity) {
        MetaData metaData = metaDataFactory.getMetaData(entityName);
        GenericDAO genericDAO = daoFactory.getDAO(metaData.getType());
        JsonTransformer jsonTransformer = jsonTransformerFactory.getJsonTransformer(metaData.getType());

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
                httpServletResponse.setContentType("application/json; charset=UTF-8");
                httpServletResponse.getWriter().println(msg);
            } catch (Exception ex2) {
                httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                httpServletResponse.setContentType("text/plain");
                try {
                    ex.printStackTrace(httpServletResponse.getWriter());
                } catch (Exception ex3) {
                    log.error("Falló al imprimir la traza", ex3);
                }
            }
        } catch (Exception ex) {
            httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            httpServletResponse.setContentType("text/plain");
            try {
                ex.printStackTrace(httpServletResponse.getWriter());
            } catch (Exception ex2) {
                log.error("Falló al imprimir la traza", ex2);
            }
        }
    }

    @RequestMapping(value = {"/{entityName}/{id}"}, method = RequestMethod.PUT, consumes = "application/json")
    public void update(HttpServletRequest httpRequest, HttpServletResponse httpServletResponse, @PathVariable("entityName") String entityName, @PathVariable("id") int id, @RequestBody String jsonEntity) {
        MetaData metaData = metaDataFactory.getMetaData(entityName);
        GenericDAO genericDAO = daoFactory.getDAO(metaData.getType());
        JsonTransformer jsonTransformer = jsonTransformerFactory.getJsonTransformer(metaData.getType());

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
                httpServletResponse.setContentType("application/json; charset=UTF-8");
                httpServletResponse.getWriter().println(msg);
            } catch (Exception ex2) {
                httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                httpServletResponse.setContentType("text/plain");
                try {
                    ex.printStackTrace(httpServletResponse.getWriter());
                } catch (Exception ex3) {
                    log.error("Falló al imprimir la traza", ex3);
                }
            }
        } catch (Exception ex) {
            httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            httpServletResponse.setContentType("text/plain");
            try {
                ex.printStackTrace(httpServletResponse.getWriter());
            } catch (Exception ex2) {
                log.error("Falló al imprimir la traza", ex2);
            }
        }
    }

    @RequestMapping(value = {"/{entityName}/{id}"}, method = RequestMethod.DELETE, consumes = "application/json")
    public void delete(HttpServletRequest httpRequest, HttpServletResponse httpServletResponse, @PathVariable("entityName") String entityName, @PathVariable("id") int id) {
        MetaData metaData = metaDataFactory.getMetaData(entityName);
        GenericDAO genericDAO = daoFactory.getDAO(metaData.getType());
        JsonTransformer jsonTransformer = jsonTransformerFactory.getJsonTransformer(metaData.getType());

        httpServletResponse.setContentType("application/json; charset=UTF-8");


        try {
            genericDAO.delete(id);
            String msg = jsonTransformer.toJson(null);
            httpServletResponse.getWriter().println(msg);
        } catch (BussinessException ex) {
            try {
                String msg = jsonTransformer.toJson(ex.getBussinessMessages());
                httpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                httpServletResponse.setContentType("application/json; charset=UTF-8");
                httpServletResponse.getWriter().println(msg);
            } catch (Exception ex2) {
                httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                httpServletResponse.setContentType("text/plain");
                try {
                    ex.printStackTrace(httpServletResponse.getWriter());
                } catch (Exception ex3) {
                    log.error("Falló al imprimir la traza", ex3);
                }
            }
        } catch (Exception ex) {
            httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            httpServletResponse.setContentType("text/plain");
            try {
                ex.printStackTrace(httpServletResponse.getWriter());
            } catch (Exception ex2) {
                log.error("Falló al imprimir la traza", ex2);
            }
        }
    }
}
