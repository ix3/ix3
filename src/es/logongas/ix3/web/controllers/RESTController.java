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
package es.logongas.ix3.web.controllers;

import es.logongas.ix3.persistence.services.dao.BusinessException;
import es.logongas.ix3.persistence.services.dao.BusinessMessage;
import es.logongas.ix3.persistence.services.dao.DAOFactory;
import es.logongas.ix3.persistence.services.dao.GenericDAO;
import es.logongas.ix3.persistence.services.dao.OrderDirection;
import es.logongas.ix3.persistence.services.dao.Order;
import es.logongas.ix3.persistence.services.dao.database.ConstraintViolation;
import es.logongas.ix3.persistence.services.metadata.MetaData;
import es.logongas.ix3.persistence.services.metadata.MetaDataFactory;
import es.logongas.ix3.web.services.json.JsonFactory;
import es.logongas.ix3.web.services.json.JsonReader;
import es.logongas.ix3.web.services.json.JsonWriter;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
    JsonFactory jsonFactory;
    private static Log log = LogFactory.getLog(RESTController.class);

    @RequestMapping(value = {"/{entityName}/metadata"}, method = RequestMethod.GET, produces = "application/json")
    public void metadata(HttpServletRequest httpRequest, HttpServletResponse httpServletResponse, @PathVariable("entityName") String entityName) {
        try {
            MetaData metaData = metaDataFactory.getMetaData(entityName);
            if (metaData == null) {
                throw new BusinessException(new BusinessMessage(null, "No existe la entidad " + entityName));
            }
            JsonWriter jsonWriter = jsonFactory.getJsonWriter(MetaData.class);

            String jsonOut = jsonWriter.toJson(metaData);

            httpServletResponse.setStatus(HttpServletResponse.SC_OK);
            httpServletResponse.setContentType("application/json; charset=UTF-8");
            httpServletResponse.getWriter().println(jsonOut);
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

    @RequestMapping(value = {"/{entityName}"}, method = RequestMethod.GET, produces = "application/json")
    public void search(HttpServletRequest httpRequest, HttpServletResponse httpServletResponse, @PathVariable("entityName") String entityName) {
        try {
            MetaData metaData = metaDataFactory.getMetaData(entityName);
            if (metaData == null) {
                throw new BusinessException(new BusinessMessage(null, "No existe la entidad " + entityName));
            }
            GenericDAO genericDAO = daoFactory.getDAO(metaData.getType());
            JsonWriter jsonWriter = jsonFactory.getJsonWriter(metaData.getType());

            Map<String, Object> filter = new HashMap<String, Object>();
            Enumeration<String> enumeration = httpRequest.getParameterNames();
            while (enumeration.hasMoreElements()) {
                String propertyName = enumeration.nextElement();
                MetaData propertyMetaData = metaData.getPropertiesMetaData().get(propertyName);
                if (propertyMetaData != null) {
                    Class propertyType = propertyMetaData.getType();
                    Object value = conversionService.convert(httpRequest.getParameter(propertyName), propertyType);
                    if (value != null) {
                        filter.put(propertyName, value);
                    }
                }
            }

            List<Order> orders = getOrders(metaData,httpRequest.getParameter("orderBy"));

            Object entity = genericDAO.search(filter, orders);
            String jsonOut = jsonWriter.toJson(entity);

            httpServletResponse.setStatus(HttpServletResponse.SC_OK);
            httpServletResponse.setContentType("application/json; charset=UTF-8");
            httpServletResponse.getWriter().println(jsonOut);
        } catch (BusinessException ex) {
            try {
                String jsonOut = jsonFactory.getJsonWriter().toJson(ex.getBusinessMessages());

                httpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                httpServletResponse.setContentType("application/json; charset=UTF-8");
                httpServletResponse.getWriter().println(jsonOut);
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

    @RequestMapping(value = {"/{entityName}/namedsearch"}, method = RequestMethod.GET, produces = "application/json")
    public void namedSearch(HttpServletRequest httpRequest, HttpServletResponse httpServletResponse, @PathVariable("entityName") String entityName) {
        try {
            MetaData metaData = metaDataFactory.getMetaData(entityName);
            if (metaData == null) {
                throw new BusinessException(new BusinessMessage(null, "No existe la entidad " + entityName));
            }
            GenericDAO genericDAO = daoFactory.getDAO(metaData.getType());
            JsonWriter jsonWriter;

            String namedSearch = httpRequest.getParameter("name");

            Method method = null;
            Method[] methods = genericDAO.getClass().getMethods();
            for (int i = 0; i < methods.length; i++) {
                Method currentMethod = methods[i];
                if (currentMethod.getName().equals(namedSearch)) {
                    method = currentMethod;
                    break;
                }
            }

            if (method == null) {
                throw new BusinessException(new BusinessMessage(null, "No existe el método " + namedSearch + " en la entidad " + entityName));
            }

            List args = new ArrayList();
            for (int i = 0; i < method.getParameterTypes().length; i++) {
                Class parameterType = method.getParameterTypes()[i];
                Object parameterValue;

                MetaData metaDataParameter = metaDataFactory.getMetaData(parameterType);
                if (metaDataParameter != null) {
                    //El parámetro es una Entidad de negocio pero solo nos han pasado la clave primaria.

                    //Vamos a obtener el tipo de la clave primaria
                    Class primaryKeyType = metaDataParameter.getPropertiesMetaData().get(metaDataParameter.getPrimaryKeyPropertyName()).getType();

                    //Ahora vamos a obtener el valor de la clave primaria
                    Serializable primaryKey;
                    try {
                        primaryKey = (Serializable) conversionService.convert(httpRequest.getParameter("parameter" + i), primaryKeyType);
                    } catch (Exception ex) {
                        throw new BusinessException(new BusinessMessage(null, "El " + i + "º parámetro no tiene el formato adecuado para ser una PK:" + httpRequest.getParameter("parameter" + i)));
                    }

                    //Y finalmente Leemos la entidad en función de la clave primaria
                    GenericDAO genericDAOParameter = daoFactory.getDAO(parameterType);
                    parameterValue = genericDAOParameter.read(primaryKey);
                    if (parameterValue == null) {
                        throw new BusinessException(new BusinessMessage(null, "El " + i + "º parámetro con valor '" + httpRequest.getParameter("parameter" + i) + "' no es de ninguna entidad."));
                    }
                } else {
                    try {
                        parameterValue = conversionService.convert(httpRequest.getParameter("parameter" + i), parameterType);
                    } catch (Exception ex) {
                        throw new BusinessException(new BusinessMessage(null, "El " + i + "º parámetro no tiene el formato adecuado:" + httpRequest.getParameter("parameter" + i)));
                    }
                }
                args.add(parameterValue);
            }

            Object result = method.invoke(genericDAO, args.toArray());
            if (result != null) {
                jsonWriter = jsonFactory.getJsonWriter(null);
            } else {
                jsonWriter = jsonFactory.getJsonWriter(metaData.getType());
            }
            String jsonOut = jsonWriter.toJson(result);

            httpServletResponse.setStatus(HttpServletResponse.SC_OK);
            httpServletResponse.setContentType("application/json; charset=UTF-8");
            httpServletResponse.getWriter().println(jsonOut);
        } catch (BusinessException ex) {
            try {
                String jsonOut = jsonFactory.getJsonWriter().toJson(ex.getBusinessMessages());

                httpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                httpServletResponse.setContentType("application/json; charset=UTF-8");
                httpServletResponse.getWriter().println(jsonOut);
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

    @RequestMapping(value = {"/{entityName}/{id}"}, method = RequestMethod.GET,  produces = "application/json")
    public void read(HttpServletRequest httpRequest, HttpServletResponse httpServletResponse, @PathVariable("entityName") String entityName, @PathVariable("id") int id) {
        try {
            MetaData metaData = metaDataFactory.getMetaData(entityName);
            if (metaData == null) {
                throw new BusinessException(new BusinessMessage(null, "No existe la entidad " + entityName));
            }
            GenericDAO genericDAO = daoFactory.getDAO(metaData.getType());
            JsonWriter jsonWriter = jsonFactory.getJsonWriter(metaData.getType());

            Object entity = genericDAO.read(id);
            String jsonOut = jsonWriter.toJson(entity);

            httpServletResponse.setStatus(HttpServletResponse.SC_OK);
            httpServletResponse.setContentType("application/json; charset=UTF-8");
            httpServletResponse.getWriter().println(jsonOut);
        } catch (BusinessException ex) {
            try {
                String jsonOut = jsonFactory.getJsonWriter().toJson(ex.getBusinessMessages());

                httpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                httpServletResponse.setContentType("application/json; charset=UTF-8");
                httpServletResponse.getWriter().println(jsonOut);
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

    @RequestMapping(value = {"/{entityName}/create"}, method = RequestMethod.GET, produces = "application/json")
    public void create(HttpServletRequest httpRequest, HttpServletResponse httpServletResponse, @PathVariable("entityName") String entityName) {
        try {
            MetaData metaData = metaDataFactory.getMetaData(entityName);
            if (metaData == null) {
                throw new BusinessException(new BusinessMessage(null, "No existe la entidad " + entityName));
            }
            GenericDAO genericDAO = daoFactory.getDAO(metaData.getType());
            JsonWriter jsonWriter = jsonFactory.getJsonWriter(metaData.getType());

            Object entity = genericDAO.create();
            String jsonOut = jsonWriter.toJson(entity);

            httpServletResponse.setStatus(HttpServletResponse.SC_OK);
            httpServletResponse.setContentType("application/json; charset=UTF-8");
            httpServletResponse.getWriter().println(jsonOut);
        } catch (BusinessException ex) {
            try {
                String jsonOut = jsonFactory.getJsonWriter().toJson(ex.getBusinessMessages());

                httpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                httpServletResponse.setContentType("application/json; charset=UTF-8");
                httpServletResponse.getWriter().println(jsonOut);
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

    @RequestMapping(value = {"/{entityName}"}, method = RequestMethod.POST, consumes = "application/json",produces = "application/json")
    public void insert(HttpServletRequest httpRequest, HttpServletResponse httpServletResponse, @PathVariable("entityName") String entityName, @RequestBody String jsonIn) {
        try {
            MetaData metaData = metaDataFactory.getMetaData(entityName);
            if (metaData == null) {
                throw new BusinessException(new BusinessMessage(null, "No existe la entidad " + entityName));
            }
            GenericDAO genericDAO = daoFactory.getDAO(metaData.getType());
            JsonWriter jsonWriter = jsonFactory.getJsonWriter(metaData.getType());
            JsonReader jsonReader = jsonFactory.getJsonReader(metaData.getType());

            Object entity = jsonReader.fromJson(jsonIn);
            genericDAO.insert(entity);
            String jsonOut = jsonWriter.toJson(entity);

            httpServletResponse.setStatus(HttpServletResponse.SC_OK);
            httpServletResponse.setContentType("application/json; charset=UTF-8");
            httpServletResponse.getWriter().println(jsonOut);
        } catch (BusinessException ex) {
            try {
                String jsonOut = jsonFactory.getJsonWriter().toJson(ex.getBusinessMessages());

                httpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                httpServletResponse.setContentType("application/json; charset=UTF-8");
                httpServletResponse.getWriter().println(jsonOut);
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

    @RequestMapping(value = {"/{entityName}/{id}"}, method = RequestMethod.PUT, consumes = "application/json", produces = "application/json")
    public void update(HttpServletRequest httpRequest, HttpServletResponse httpServletResponse, @PathVariable("entityName") String entityName, @PathVariable("id") int id, @RequestBody String jsonIn) {
        try {
            MetaData metaData = metaDataFactory.getMetaData(entityName);
            if (metaData == null) {
                throw new BusinessException(new BusinessMessage(null, "No existe la entidad " + entityName));
            }
            GenericDAO genericDAO = daoFactory.getDAO(metaData.getType());
            JsonWriter jsonWriter = jsonFactory.getJsonWriter(metaData.getType());
            JsonReader jsonReader = jsonFactory.getJsonReader(metaData.getType());

            Object entity = jsonReader.fromJson(jsonIn);
            genericDAO.update(entity);
            String jsonOut = jsonWriter.toJson(entity);

            httpServletResponse.setStatus(HttpServletResponse.SC_OK);
            httpServletResponse.setContentType("application/json; charset=UTF-8");
            httpServletResponse.getWriter().println(jsonOut);

        } catch (BusinessException ex) {
            try {
                String jsonOut = jsonFactory.getJsonWriter().toJson(ex.getBusinessMessages());

                httpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                httpServletResponse.setContentType("application/json; charset=UTF-8");
                httpServletResponse.getWriter().println(jsonOut);
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

    @RequestMapping(value = {"/{entityName}/{id}"}, method = RequestMethod.DELETE, consumes = "application/json", produces = "application/json")
    public void delete(HttpServletRequest httpRequest, HttpServletResponse httpServletResponse, @PathVariable("entityName") String entityName, @PathVariable("id") int id) {
        try {
            MetaData metaData = metaDataFactory.getMetaData(entityName);
            if (metaData == null) {
                throw new BusinessException(new BusinessMessage(null, "No existe la entidad " + entityName));
            }
            GenericDAO genericDAO = daoFactory.getDAO(metaData.getType());

            boolean deletedSuccess = genericDAO.delete(id);
            if (deletedSuccess == false) {
                throw new BusinessException(new BusinessMessage(null, "No existe la entidad a borrar"));
            }

            httpServletResponse.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (BusinessException ex) {

            try {
                String jsonOut = jsonFactory.getJsonWriter().toJson(ex.getBusinessMessages());

                httpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                httpServletResponse.setContentType("application/json; charset=UTF-8");
                httpServletResponse.getWriter().println(jsonOut);
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

    /**
     * Como se ordenan los datos
     *
     * @param metaData Metadatos de la que se quieren ordenar
     * @param orderBy Debe tener la forma de "[campo [asc desc],](campo [asc
     * desc])*)
     * @return
     */
    private List<Order> getOrders(MetaData metaData, String orderBy) {
        List<Order> orders = new ArrayList<Order>();

        if (orderBy != null) {
            String[] splitOrderFields = orderBy.split(",");

            Pattern pattern = Pattern.compile("\\s*([^\\s]*)\\s*([^\\s]*)?\\s*");
            for (String s : splitOrderFields) {
                Matcher matcher = pattern.matcher(s);
                if (matcher.matches() == false) {
                    throw new RuntimeException("El campo orderBy no tiene el formato adecuado:" + s + " en "+orderBy);
                }

                String fieldName = matcher.group(1);
                String orderName = matcher.group(2);
                OrderDirection orderDirection;

                if (metaData.getPropertiesMetaData().get(fieldName) == null) {
                    throw new RuntimeException("No existe el campo de ordenación '" + fieldName + "' en la entidad '" + metaData.getType().getName() + "'");
                }

                if ((orderName == null) || (orderName.trim().length() == 0)) {
                    orderDirection = OrderDirection.Ascending;
                } else if (orderName.equalsIgnoreCase("ASC")) {
                    orderDirection = OrderDirection.Ascending;
                } else if (orderName.equalsIgnoreCase("DESC")) {
                    orderDirection = OrderDirection.Descending;
                } else {
                    throw new RuntimeException("La dirección de la ordenacion no es válida:" + orderName);
                }

                Order order = new Order(fieldName, orderDirection);

                orders.add(order);

            }
        }

        return orders;
    }
}