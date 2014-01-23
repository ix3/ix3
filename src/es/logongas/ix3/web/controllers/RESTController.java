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
import es.logongas.ix3.persistence.services.dao.NamedSearch;
import es.logongas.ix3.persistence.services.dao.OrderDirection;
import es.logongas.ix3.persistence.services.dao.Order;
import es.logongas.ix3.persistence.services.metadata.MetaData;
import es.logongas.ix3.persistence.services.metadata.MetaType;
import es.logongas.ix3.persistence.services.metadata.MetaDataFactory;
import es.logongas.ix3.util.ReflectionUtil;
import es.logongas.ix3.web.controllers.metadata.Metadata;
import es.logongas.ix3.web.controllers.metadata.MetadataFactory;
import es.logongas.ix3.web.services.json.JsonFactory;
import es.logongas.ix3.web.services.json.JsonReader;
import es.logongas.ix3.web.services.json.JsonWriter;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
    private static final Log log = LogFactory.getLog(RESTController.class);

    private final String PARAMETER_EXPAND = "$expand";
    private final String PARAMETER_ORDERBY = "$orderby";
    private final String PARAMETER_PAGENUMBER = "$pagenumber";
    private final String PARAMETER_PAGESIZE = "$pagesize";
    private final String PATH_METADATA = "$metadata";
    private final String PATH_NAMEDSEARCH = "$namedsearch";
    private final String PATH_CREATE = "$create";

    @RequestMapping(value = {"/{entityName}/" + PATH_METADATA}, method = RequestMethod.GET, produces = "application/json")
    public void metadata(HttpServletRequest httpRequest, HttpServletResponse httpServletResponse, @PathVariable("entityName") String entityName) {
        try {
            MetaData metaData = metaDataFactory.getMetaData(entityName);
            if (metaData == null) {
                throw new BusinessException(new BusinessMessage(null, "No existe la entidad " + entityName));
            }

            //Entidades a expandir
            List<String> expand = getExpand(httpRequest.getParameter(PARAMETER_EXPAND));

            Metadata metadata = (new MetadataFactory()).getMetadata(metaData, metaDataFactory, daoFactory, httpRequest.getContextPath(), expand);
            JsonWriter jsonWriter = jsonFactory.getJsonWriter();

            String jsonOut = jsonWriter.toJson(metadata);

            cache(httpServletResponse);
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

            List<String> expand = getExpand(httpRequest.getParameter(PARAMETER_EXPAND));

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

            List<Order> orders = getOrders(metaData, httpRequest.getParameter(PARAMETER_ORDERBY));
            Integer pageSize = getIntegerFromString(httpRequest.getParameter(PARAMETER_PAGESIZE));
            Integer pageNumber = getIntegerFromString(httpRequest.getParameter(PARAMETER_PAGENUMBER));

            Object entity;
            if ((pageSize == null) && (pageNumber == null)) {
                entity = genericDAO.search(filter, orders);
            } else if ((pageSize != null) && (pageNumber != null)) {
                entity = genericDAO.pageableSearch(filter, orders, pageNumber, pageSize);
            } else {
                throw new RuntimeException("Los datos de la paginacion no son correctos, es necesario los 2 datos:" + PARAMETER_PAGENUMBER + " y " + PARAMETER_PAGESIZE);
            }

            String jsonOut = jsonWriter.toJson(entity, expand);

            noCache(httpServletResponse);
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

    @RequestMapping(value = {"/{entityName}/" + PATH_NAMEDSEARCH + "/{namedSearch}"}, method = RequestMethod.GET, produces = "application/json")
    public void namedSearch(HttpServletRequest httpRequest, HttpServletResponse httpServletResponse, @PathVariable("entityName") String entityName, @PathVariable("namedSearch") String namedSearch) {
        try {
            MetaData metaData = metaDataFactory.getMetaData(entityName);
            if (metaData == null) {
                throw new BusinessException(new BusinessMessage(null, "No existe la entidad " + entityName));
            }
            GenericDAO genericDAO = daoFactory.getDAO(metaData.getType());
            JsonWriter jsonWriter;

            //Entidades a expandir
            List<String> expand = getExpand(httpRequest.getParameter(PARAMETER_EXPAND));

            Map<String, Object> filter = getFilterFromParameters(genericDAO, namedSearch, removeDollarParameters(httpRequest.getParameterMap()));
            Object result = genericDAO.namedSearch(namedSearch, filter);

            if (result != null) {
                jsonWriter = jsonFactory.getJsonWriter(null);
            } else {
                jsonWriter = jsonFactory.getJsonWriter(metaData.getType());
            }
            String jsonOut = jsonWriter.toJson(result, expand);

            noCache(httpServletResponse);
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

    @RequestMapping(value = {"/{entityName}/{id}"}, method = RequestMethod.GET, produces = "application/json")
    public void read(HttpServletRequest httpRequest, HttpServletResponse httpServletResponse, @PathVariable("entityName") String entityName, @PathVariable("id") int id) {
        try {
            MetaData metaData = metaDataFactory.getMetaData(entityName);
            if (metaData == null) {
                throw new BusinessException(new BusinessMessage(null, "No existe la entidad " + entityName));
            }
            GenericDAO genericDAO = daoFactory.getDAO(metaData.getType());
            JsonWriter jsonWriter = jsonFactory.getJsonWriter(metaData.getType());

            //Entidades a expandir
            List<String> expand = getExpand(httpRequest.getParameter(PARAMETER_EXPAND));

            Object entity = genericDAO.read(id);
            String jsonOut = jsonWriter.toJson(entity, expand);

            noCache(httpServletResponse);
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

    @RequestMapping(value = {"/{entityName}/{id}/{child}"}, method = RequestMethod.GET, produces = "application/json")
    public void readChild(HttpServletRequest httpRequest, HttpServletResponse httpServletResponse, @PathVariable("entityName") String entityName, @PathVariable("id") int id, @PathVariable("child") String child) {
        try {
            MetaData metaData = metaDataFactory.getMetaData(entityName);
            if (metaData == null) {
                throw new BusinessException(new BusinessMessage(null, "No existe la entidad " + entityName));
            }
            if (metaData.getPropertiesMetaData().get(child) == null) {
                throw new BusinessException(new BusinessMessage(null, "En la entidad '" + entityName + "' no existe la propiedad '" + child + "'"));
            }
            if (metaData.getPropertiesMetaData().get(child).isCollection() == false) {
                throw new BusinessException(new BusinessMessage(null, "En la entidad '" + entityName + "'  la propiedad '" + child + "' no es una colección"));
            }

            GenericDAO genericDAO = daoFactory.getDAO(metaData.getType());
            JsonWriter jsonWriter = jsonFactory.getJsonWriter(metaData.getType());

            //Entidades a expandir
            List<String> expand = getExpand(httpRequest.getParameter(PARAMETER_EXPAND));

            Object entity = genericDAO.read(id);
            Object childData;
            if (entity != null) {
                childData = ReflectionUtil.getValueFromBean(entity, child);
            } else {
                //Si no hay datos , retornamos una lista vacia
                childData = new ArrayList();
            }
            String jsonOut = jsonWriter.toJson(childData, expand);

            noCache(httpServletResponse);
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

    @RequestMapping(value = {"/{entityName}/" + PATH_CREATE}, method = RequestMethod.GET, produces = "application/json")
    public void create(HttpServletRequest httpRequest, HttpServletResponse httpServletResponse, @PathVariable("entityName") String entityName) {
        try {
            MetaData metaData = metaDataFactory.getMetaData(entityName);
            if (metaData == null) {
                throw new BusinessException(new BusinessMessage(null, "No existe la entidad " + entityName));
            }
            GenericDAO genericDAO = daoFactory.getDAO(metaData.getType());
            JsonWriter jsonWriter = jsonFactory.getJsonWriter(metaData.getType());

            //Entidades a expandir
            List<String> expand = getExpand(httpRequest.getParameter(PARAMETER_EXPAND));

            Map<String, Object> initialProperties = getPropertiesFromParameters(metaData, removeDollarParameters(httpRequest.getParameterMap()));

            Object entity = genericDAO.create(initialProperties);
            String jsonOut = jsonWriter.toJson(entity, expand);

            noCache(httpServletResponse);
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

    @RequestMapping(value = {"/{entityName}"}, method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public void insert(HttpServletRequest httpRequest, HttpServletResponse httpServletResponse, @PathVariable("entityName") String entityName, @RequestBody String jsonIn) {
        try {
            MetaData metaData = metaDataFactory.getMetaData(entityName);
            if (metaData == null) {
                throw new BusinessException(new BusinessMessage(null, "No existe la entidad " + entityName));
            }
            GenericDAO genericDAO = daoFactory.getDAO(metaData.getType());
            JsonWriter jsonWriter = jsonFactory.getJsonWriter(metaData.getType());
            JsonReader jsonReader = jsonFactory.getJsonReader(metaData.getType());

            //Entidades a expandir
            List<String> expand = getExpand(httpRequest.getParameter(PARAMETER_EXPAND));            
            
            Object entity = jsonReader.fromJson(jsonIn);
            genericDAO.insert(entity);
            
            
            String jsonOut = jsonWriter.toJson(entity,expand);

            noCache(httpServletResponse);
            httpServletResponse.setStatus(HttpServletResponse.SC_CREATED);
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

            //Entidades a expandir
            List<String> expand = getExpand(httpRequest.getParameter(PARAMETER_EXPAND));            
            
            Object entity = jsonReader.fromJson(jsonIn);
            genericDAO.update(entity);
            
            String jsonOut = jsonWriter.toJson(entity,expand);

            noCache(httpServletResponse);
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

    @RequestMapping(value = {"/{entityName}/{id}"}, method = RequestMethod.DELETE)
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

            noCache(httpServletResponse);
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

    private void noCache(HttpServletResponse httpServletResponse) {
        httpServletResponse.setHeader("Cache-Control", "no-cache");
    }

    private void cache(HttpServletResponse httpServletResponse) {
        cache(httpServletResponse, 60);
    }

    private void cache(HttpServletResponse httpServletResponse, long expireSeconds) {
        httpServletResponse.setHeader("Cache-Control", "private, no-transform, max-age=" + expireSeconds);
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

        if ((orderBy != null) && (orderBy.trim().isEmpty() == false)) {
            String[] splitOrderFields = orderBy.split(",");

            Pattern pattern = Pattern.compile("\\s*([^\\s]*)\\s*([^\\s]*)?\\s*");
            for (String s : splitOrderFields) {
                Matcher matcher = pattern.matcher(s);
                if (matcher.matches() == false) {
                    throw new RuntimeException("El campo orderBy no tiene el formato adecuado:" + s + " en " + orderBy);
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

    /**
     * Transforma el parámetro "expand" que viene por la petición http en una
     * array
     *
     * @param expand El String con varios expand separados por comas
     * @return El array con cada uno de ello.
     */
    private List<String> getExpand(String expand) {
        if ((expand == null) || (expand.trim().isEmpty())) {
            return new ArrayList<String>();
        } else {
            return Arrays.asList(expand.split(","));
        }
    }

    private Map<String, Object> getFilterFromParameters(GenericDAO genericDAO, String methodName, Map<String, String[]> parametersMap) throws BusinessException {
        Map<String, Object> filter = new HashMap<String, Object>();

        Method method = ReflectionUtil.getMethod(genericDAO.getClass(), methodName);
        if (method == null) {
            throw new BusinessException(new BusinessMessage(null, "No existe el método " + methodName + " en la clase " + genericDAO.getClass().getName()));
        }

        NamedSearch namedSearchAnnotation = ReflectionUtil.getAnnotation(genericDAO.getClass(), methodName, NamedSearch.class);
        if (namedSearchAnnotation == null) {
            throw new RuntimeException("No es posible llamar al método '" + genericDAO.getClass().getName() + "." + methodName + "' si no contiene la anotacion NamedSearch");
        }

        String[] parameterNames = namedSearchAnnotation.parameterNames();
        if ((parameterNames == null) && (method.getParameterTypes().length > 0)) {
            throw new RuntimeException("Es necesario la lista de nombre de parametros para la anotación NameSearch del método:" + genericDAO.getClass().getName() + "." + methodName);
        }

        if (method.getParameterTypes().length != parameterNames.length) {
            throw new RuntimeException("La lista de nombre de parametros para la anotación NameSearch debe coincidir con el nº de parámetro del método: " + genericDAO.getClass().getName() + "." + methodName);
        }

        for (int i = 0; i < method.getParameterTypes().length; i++) {
            String parameterName = parameterNames[i];
            Class parameterType = method.getParameterTypes()[i];
            String stringParameterValue;
            Object parameterValue;

            if (parametersMap.get(parameterName) == null) {
                stringParameterValue = "";
            } else {

                if (parametersMap.get(parameterName).length != 1) {
                    throw new RuntimeException("El parametro de la petición http '" + parameterName + "' solo puede teenr un único valor pero tiene:" + parametersMap.get(parameterName).length);
                }

                stringParameterValue = parametersMap.get(parameterName)[0];
            }

            MetaData metaDataParameter = metaDataFactory.getMetaData(parameterType);
            if (metaDataParameter != null) {
                //El parámetro es una Entidad de negocio pero solo nos han pasado la clave primaria.

                //Vamos a obtener el tipo de la clave primaria
                Class primaryKeyType = metaDataParameter.getPropertiesMetaData().get(metaDataParameter.getPrimaryKeyPropertyName()).getType();

                //Ahora vamos a obtener el valor de la clave primaria
                Serializable primaryKey;
                try {
                    primaryKey = (Serializable) conversionService.convert(stringParameterValue, primaryKeyType);
                } catch (Exception ex) {
                    throw new BusinessException(new BusinessMessage(null, "El " + i + "º parámetro no tiene el formato adecuado para ser una PK:" + stringParameterValue));
                }

                //Y finalmente Leemos la entidad en función de la clave primaria
                GenericDAO genericDAOParameter = daoFactory.getDAO(parameterType);
                parameterValue = genericDAOParameter.read(primaryKey);
                if (parameterValue == null) {
                    throw new BusinessException(new BusinessMessage(null, "El " + i + "º parámetro con valor '" + stringParameterValue + "' no es de ninguna entidad."));
                }
            } else {
                try {
                    parameterValue = conversionService.convert(stringParameterValue, parameterType);
                } catch (Exception ex) {
                    throw new BusinessException(new BusinessMessage(null, "El " + i + "º parámetro no tiene el formato adecuado:" + stringParameterValue));
                }
            }

            filter.put(parameterName, parameterValue);

        }

        return filter;
    }

    /**
     * Obtiene un integer a partir de un null
     *
     * @param s El String que se transforma en un Integer
     * @return Si el string es null se retornará null, sino se retornará el
     * Integer
     */
    private Integer getIntegerFromString(String s) {
        if (s == null) {
            return null;
        } else {
            return Integer.parseInt(s);
        }
    }

    /**
     * Esta función quita aquellos parametros que viene nen la petición http que
     * empiezan por "$" pq esos parámetros tienen un significado especial y no
     * son "normales" para el modelo de negocio.
     *
     * @param parameterMap El map con los parametros
     * @return Otro map con los mismos valores excepto los que empiezan por "$"
     */
    private Map<String, String[]> removeDollarParameters(Map<String, String[]> parameterMap) {
        Map<String, String[]> cleanParameterMap = new LinkedHashMap<String, String[]>();

        for (String key : parameterMap.keySet()) {
            if ((key != null) && (key.trim().startsWith("$") == false)) {
                cleanParameterMap.put(key, parameterMap.get(key));
            }
        }

        return cleanParameterMap;
    }

    /**
     * Esta funcion transforma los valores iniciales de la petición HTTP en una serie de objetos.
     * Si las propiedaes hacen referencia a una propiedad de una entida o a una clave primaria de una entidad se leerá dicha entidad
     * Sino simplemente se pondrá el valor de la entidad.
     * @param metaData Metada desde la que se quiere 
     * @param parameters
     * @return
     * @throws BusinessException 
     */
    private Map<String, Object> getPropertiesFromParameters(MetaData metaData, Map<String, String[]> parameters) throws BusinessException {
        Map<String, Object> newParameters = new LinkedHashMap<String, Object>();

        for (String propertyName : parameters.keySet()) {
            String[] rawValues = parameters.get(propertyName);
            String realPropertyName;

            if (rawValues.length != 1) {
                throw new RuntimeException("Solo se permite un valor en cada parametro:" + propertyName);
            }
            String rawValue = rawValues[0];
            Object value;
            MetaData initialValueMetaData = metaData.getPropertyMetaData(propertyName);

            if (initialValueMetaData==null) {
                throw new RuntimeException("No existe la propiedad:" + propertyName);
            }
            
            if (initialValueMetaData.isCollection()) {
                throw new RuntimeException("No se permite como valor inicial una coleccion:" + propertyName);
            }

            switch (initialValueMetaData.getMetaType()) {
                case Scalar:
                    String leftPropertyName; //El nombre de la propiedad antes del primer punto
                    String rigthPropertyName; //El nombre de la propiedad antes del primer punto

                    int indexPoint = propertyName.lastIndexOf(".");
                    if (indexPoint < 0) {
                        leftPropertyName = null;
                        rigthPropertyName = propertyName;
                    } else if ((indexPoint > 0) && (indexPoint < (propertyName.length() - 1))) {
                        leftPropertyName = propertyName.substring(0, indexPoint);
                        rigthPropertyName = propertyName.substring(indexPoint + 1);
                    } else {
                        throw new RuntimeException("El punto no puede estar ni al principio ni al final");
                    }

                    //Nos han pasado un valor directamente
                    if (leftPropertyName == null) {
                        realPropertyName = rigthPropertyName;
                        value = conversionService.convert(rawValue, initialValueMetaData.getType());
                    } else {
                        MetaData leftPropertyMetaData = metaData.getPropertyMetaData(leftPropertyName);
                        if (leftPropertyMetaData.getMetaType() == MetaType.Entity) {
                            if (rigthPropertyName.equals(leftPropertyMetaData.getPrimaryKeyPropertyName())) {
                                //nos ham pasado la clave primaria de una entidad ,así que leemos la entidad
                                //y el valor inicial será el de la entidad ya leida y no el de la clave primaria.
                                GenericDAO genericDAO = daoFactory.getDAO(leftPropertyMetaData.getType());
                                Class primaryKeyType = leftPropertyMetaData.getPropertyMetaData(leftPropertyMetaData.getPrimaryKeyPropertyName()).getType();
                                Serializable primaryKey = (Serializable) conversionService.convert(rawValue, primaryKeyType);

                                realPropertyName = leftPropertyName;
                                value = genericDAO.read(primaryKey);
                            } else {
                                throw new RuntimeException("No se puede pasar una propiedad de una entidad, solo se permite la clave primaria:" + propertyName);
                            }
                        } else {
                            //Como la propieda anterior no era una entidad no era nada "raro" y nos habian pasado simplemente el valor
                            realPropertyName = propertyName;
                            value = conversionService.convert(rawValue, initialValueMetaData.getType());
                        }
                    }

                    break;
                case Component:
                    throw new RuntimeException("No se permite como valor inicial un componente:" + propertyName);
                case Entity:
                    //La propiedad corresponde a una entidad , así que se supondrá que el valor era la clave primaria de dicha entidad
                    GenericDAO genericDAO = daoFactory.getDAO(initialValueMetaData.getType());
                    Class primaryKeyType = initialValueMetaData.getPropertyMetaData(initialValueMetaData.getPrimaryKeyPropertyName()).getType();
                    Serializable primaryKey = (Serializable) conversionService.convert(rawValue, primaryKeyType);

                    realPropertyName = propertyName;
                    value = genericDAO.read(primaryKey);

                    break;
                default:
                    throw new RuntimeException("El meta tipo es desconocido:" + initialValueMetaData.getMetaType());
            }

            newParameters.put(realPropertyName, value);
        }

        return newParameters;
    }

}
