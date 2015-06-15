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

import es.logongas.ix3.core.BusinessException;
import es.logongas.ix3.service.NamedSearch;
import es.logongas.ix3.core.OrderDirection;
import es.logongas.ix3.core.Order;
import es.logongas.ix3.core.PageRequest;
import es.logongas.ix3.core.conversion.Conversion;
import es.logongas.ix3.dao.Filter;
import es.logongas.ix3.dao.FilterOperator;
import es.logongas.ix3.dao.SearchResponse;
import es.logongas.ix3.dao.metadata.MetaData;
import es.logongas.ix3.dao.metadata.MetaDataFactory;
import es.logongas.ix3.dao.metadata.MetaType;
import es.logongas.ix3.service.CRUDService;
import es.logongas.ix3.service.CRUDServiceFactory;
import es.logongas.ix3.util.ReflectionUtil;
import es.logongas.ix3.web.controllers.metadata.Metadata;
import es.logongas.ix3.web.controllers.metadata.MetadataFactory;
import es.logongas.ix3.web.json.JsonReader;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
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
public class CRUDRESTController extends AbstractRESTController {

    private static final Log log = LogFactory.getLog(CRUDRESTController.class);

    private final String PARAMETER_ORDERBY = "$orderby";
    private final String PARAMETER_PAGENUMBER = "$pagenumber";
    private final String PARAMETER_PAGESIZE = "$pagesize";
    private final String PARAMETER_DISTINCT = "$distinct";
    private final String PARAMETER_NAMEDSEARCH = "$namedsearch";
    private final String PATH_METADATA = "$metadata";
    private final String PATH_CREATE = "$create";

    @Autowired
    private MetaDataFactory metaDataFactory;

    @Autowired
    private Conversion conversion;

    @Autowired
    private CRUDServiceFactory crudServiceFactory;

    @RequestMapping(value = {"/{entityName}/" + PATH_METADATA}, method = RequestMethod.GET, produces = "application/json")
    public void metadata(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, final @PathVariable("entityName") String entityName) {

        restMethod(httpServletRequest, httpServletResponse, null, new Command() {

            @Override
            public CommandResult run(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Map<String, Object> arguments) throws Exception, BusinessException {

                MetaData metaData = metaDataFactory.getMetaData(entityName);
                if (metaData == null) {
                    throw new BusinessException("No existe la entidad " + entityName);
                }

                List<String> expand = getExpand(httpServletRequest.getParameter(PARAMETER_EXPAND));

                Metadata metadata = (new MetadataFactory()).getMetadata(metaData, metaDataFactory, crudServiceFactory, httpServletRequest.getContextPath(), expand);
                return new CommandResult(Metadata.class, metadata, true);

            }

        });

    }

    @RequestMapping(value = {"/{entityName}"}, method = RequestMethod.GET, produces = "application/json")
    public void search(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, final @PathVariable("entityName") String entityName) {

        restMethod(httpServletRequest, httpServletResponse, null, new Command() {

            @Override
            public CommandResult run(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Map<String, Object> arguments) throws Exception, BusinessException {

                MetaData metaData = metaDataFactory.getMetaData(entityName);
                if (metaData == null) {
                    throw new BusinessException("No existe la entidad " + entityName);
                }
                CRUDService crudService = crudServiceFactory.getService(metaData.getType());

                List<Order> orders = getOrders(metaData, httpServletRequest.getParameter(PARAMETER_ORDERBY));
                PageRequest pageRequest = getPageRequest(httpServletRequest);
                SearchResponse searchResponse = getSearchResponse(httpServletRequest);
                String namedSearch = httpServletRequest.getParameter(PARAMETER_NAMEDSEARCH);

                Object result;
                if ((namedSearch != null) && (namedSearch.trim().equals("") == false)) {
                    switch (getNamedSearchType(crudService, namedSearch)) {
                        case FILTER:
                            List<Filter> filters = getFiltersSearchFromWebParameters(httpServletRequest, metaData);
                            result = executeNamedSearchFilters(crudService, namedSearch, filters, pageRequest, orders, searchResponse);
                            break;
                        case PARAMETERS:
                            Map<String, Object> parameters = getParametersSearchFromWebParameters(httpServletRequest, crudService, namedSearch);
                            result = executeNamedSearchParameters(crudService, namedSearch, parameters, pageRequest, orders, searchResponse);
                            break;
                        default:
                            throw new RuntimeException("El tipo del name search es desconocido:" + getNamedSearchType(crudService, namedSearch));
                    }
                } else {
                    List<Filter> filters = getFiltersSearchFromWebParameters(httpServletRequest, metaData);
                    if (pageRequest == null) {
                        result = crudService.search(filters, orders, searchResponse);
                    } else {
                        result = crudService.pageableSearch(filters, orders, pageRequest, searchResponse);
                    }
                }
                return new CommandResult(metaData.getType(), result);

            }
        });

    }

    @RequestMapping(value = {"/{entityName}/{id}"}, method = RequestMethod.GET, produces = "application/json")
    public void read(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, final @PathVariable("entityName") String entityName, final @PathVariable("id") int id) {

        restMethod(httpServletRequest, httpServletResponse, null, new Command() {

            @Override
            public CommandResult run(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Map<String, Object> arguments) throws Exception, BusinessException {

                MetaData metaData = metaDataFactory.getMetaData(entityName);
                if (metaData == null) {
                    throw new BusinessException("No existe la entidad " + entityName);
                }
                CRUDService crudService = crudServiceFactory.getService(metaData.getType());
                Object entity = crudService.read(id);

                return new CommandResult(metaData.getType(), entity);

            }
        });

    }

    @RequestMapping(value = {"/{entityName}/{id}/{child}"}, method = RequestMethod.GET, produces = "application/json")
    public void readChild(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, final @PathVariable("entityName") String entityName, final @PathVariable("id") int id, final @PathVariable("child") String child) {

        restMethod(httpServletRequest, httpServletResponse, null, new Command() {

            @Override
            public CommandResult run(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Map<String, Object> arguments) throws Exception, BusinessException {

                MetaData metaData = metaDataFactory.getMetaData(entityName);
                if (metaData == null) {
                    throw new BusinessException("No existe la entidad " + entityName);
                }
                if (metaData.getPropertiesMetaData().get(child) == null) {
                    throw new BusinessException("En la entidad '" + entityName + "' no existe la propiedad '" + child + "'");
                }
                if (metaData.getPropertiesMetaData().get(child).isCollection() == false) {
                    throw new BusinessException("En la entidad '" + entityName + "'  la propiedad '" + child + "' no es una colección");
                }

                CRUDService crudService = crudServiceFactory.getService(metaData.getType());
                Object entity = crudService.read(id);
                Object childData;
                if (entity != null) {
                    childData = ReflectionUtil.getValueFromBean(entity, child);
                } else {
                    //Si no hay datos , retornamos una lista vacia
                    childData = new ArrayList();
                }

                return new CommandResult(metaData.getPropertiesMetaData().get(child).getType(), childData);

            }
        });

    }

    @RequestMapping(value = {"/{entityName}/" + PATH_CREATE}, method = RequestMethod.GET, produces = "application/json")
    public void create(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, final @PathVariable("entityName") String entityName) {

        restMethod(httpServletRequest, httpServletResponse, null, new Command() {

            @Override
            public CommandResult run(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Map<String, Object> arguments) throws Exception, BusinessException {

                MetaData metaData = metaDataFactory.getMetaData(entityName);
                if (metaData == null) {
                    throw new BusinessException("No existe la entidad " + entityName);
                }
                CRUDService crudService = crudServiceFactory.getService(metaData.getType());
                Map<String, Object> initialProperties = getPropertiesFromParameters(metaData, removeDollarParameters(httpServletRequest.getParameterMap()));
                Object entity = crudService.create(initialProperties);

                return new CommandResult(metaData.getType(), entity);

            }
        });
    }

    @RequestMapping(value = {"/{entityName}"}, method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public void insert(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, final @PathVariable("entityName") String entityName, final @RequestBody String jsonIn) {
        restMethod(httpServletRequest, httpServletResponse, null, new Command() {

            @Override
            public CommandResult run(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Map<String, Object> arguments) throws Exception, BusinessException {

                MetaData metaData = metaDataFactory.getMetaData(entityName);
                if (metaData == null) {
                    throw new BusinessException("No existe la entidad " + entityName);
                }
                CRUDService crudService = crudServiceFactory.getService(metaData.getType());
                JsonReader jsonReader = jsonFactory.getJsonReader(metaData.getType());
                Object entity = jsonReader.fromJson(jsonIn);
                crudService.insert(entity);

                return new CommandResult(metaData.getType(), entity, HttpServletResponse.SC_CREATED);

            }
        });
    }

    @RequestMapping(value = {"/{entityName}/{id}"}, method = RequestMethod.PUT, consumes = "application/json", produces = "application/json")
    public void update(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, final @PathVariable("entityName") String entityName, final @PathVariable("id") int id, final @RequestBody String jsonIn) {

        restMethod(httpServletRequest, httpServletResponse, null, new Command() {

            @Override
            public CommandResult run(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Map<String, Object> arguments) throws Exception, BusinessException {

                MetaData metaData = metaDataFactory.getMetaData(entityName);
                if (metaData == null) {
                    throw new BusinessException("No existe la entidad " + entityName);
                }
                CRUDService crudService = crudServiceFactory.getService(metaData.getType());
                JsonReader jsonReader = jsonFactory.getJsonReader(metaData.getType());
                Object entity = jsonReader.fromJson(jsonIn);
                crudService.update(entity);

                return new CommandResult(metaData.getType(), entity);

            }
        });

    }

    @RequestMapping(value = {"/{entityName}/{id}"}, method = RequestMethod.DELETE)
    public void delete(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, final @PathVariable("entityName") String entityName, final @PathVariable("id") int id) {

        restMethod(httpServletRequest, httpServletResponse, null, new Command() {

            @Override
            public CommandResult run(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Map<String, Object> arguments) throws Exception, BusinessException {

                MetaData metaData = metaDataFactory.getMetaData(entityName);
                if (metaData == null) {
                    throw new BusinessException("No existe la entidad " + entityName);
                }
                CRUDService crudService = crudServiceFactory.getService(metaData.getType());
                boolean deletedSuccess = crudService.delete(id);
                if (deletedSuccess == false) {
                    throw new BusinessException("No existe la entidad a borrar");
                }

                return null;

            }
        });
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

    private PageRequest getPageRequest(HttpServletRequest httpServletRequest) {
        Integer pageNumber = getIntegerFromString(httpServletRequest.getParameter(PARAMETER_PAGENUMBER));
        Integer pageSize = getIntegerFromString(httpServletRequest.getParameter(PARAMETER_PAGESIZE));

        if ((pageNumber == null) && (pageSize == null)) {
            return null;
        } else if ((pageNumber != null) && (pageSize != null)) {
            return new PageRequest(pageNumber, pageSize);
        } else {
            throw new RuntimeException("Debe estar los 2 números para paginar pero solo está uno de ellos:" + getIntegerFromString(httpServletRequest.getParameter(PARAMETER_PAGENUMBER)) + "-" + getIntegerFromString(httpServletRequest.getParameter(PARAMETER_PAGESIZE)));
        }
    }

    private SearchResponse getSearchResponse(HttpServletRequest httpServletRequest) {
        boolean distinct = getBooleanFromString(httpServletRequest.getParameter(PARAMETER_DISTINCT));

        SearchResponse searchResponse = new SearchResponse(distinct);

        return searchResponse;
    }

    private List<Filter> getFiltersSearchFromWebParameters(HttpServletRequest httpServletRequest, MetaData metaData) {
        List<Filter> filters = new ArrayList<Filter>();
        Enumeration<String> enumeration = httpServletRequest.getParameterNames();
        while (enumeration.hasMoreElements()) {
            String rawPropertyName = enumeration.nextElement();
            Filter filter = getFilterFromPropertyName(rawPropertyName);

            MetaData propertyMetaData = metaData.getPropertiesMetaData().get(filter.getPropertyName());
            if (propertyMetaData != null) {
                Class propertyType = propertyMetaData.getType();
                String[] parameterValues = httpServletRequest.getParameterValues(rawPropertyName);
                if (parameterValues.length == 1) {
                    Object value;
                    if (filter.getFilterOperator() == FilterOperator.isnull) {
                        value = conversion.convertFromString(parameterValues[0], Boolean.class);
                    } else {
                        value = conversion.convertFromString(parameterValues[0], propertyType);
                    }
                    if (value != null) {
                        filter.setValue(value);
                        filters.add(filter);
                    }

                } else {
                    List<Object> values = new ArrayList<Object>();
                    
                    if (filter.getFilterOperator() == FilterOperator.isnull) {
                        throw new RuntimeException("No se permiten varios valores con el operador 'isnull'");
                    }
                    
                    for (String parameterValue : parameterValues) {
                        values.add(conversion.convertFromString(parameterValue, propertyType));
                    }
                    filter.setValue(values);
                    filters.add(filter);
                }
            }
        }

        return filters;
    }

    private Map<String, Object> getParametersSearchFromWebParameters(HttpServletRequest httpServletRequest, CRUDService crudService, String methodName) throws BusinessException {
        Map<String, Object> parameters = new HashMap<String, Object>();

        Method method = ReflectionUtil.getMethod(crudService.getClass(), methodName);
        if (method == null) {
            throw new BusinessException("No existe el método " + methodName + " en la clase " + crudService.getClass().getName());
        }

        NamedSearch namedSearchAnnotation = getAnnotation(crudService.getClass(), methodName, NamedSearch.class);

        String[] parameterNames = namedSearchAnnotation.parameterNames();
        if ((parameterNames == null) && (method.getParameterTypes().length > 0)) {
            throw new RuntimeException("Es necesario la lista de nombre de parametros para la anotación NameSearch del método:" + crudService.getClass().getName() + "." + methodName);
        }

        if (method.getParameterTypes().length != parameterNames.length) {
            throw new RuntimeException("La lista de nombre de parametros para la anotación NameSearch debe coincidir con el nº de parámetro del método: " + crudService.getClass().getName() + "." + methodName);
        }

        Map<String, String[]> webParameters = removeDollarParameters(httpServletRequest.getParameterMap());
        for (int i = 0; i < method.getParameterTypes().length; i++) {
            String parameterName = parameterNames[i];
            Class parameterType = method.getParameterTypes()[i];
            String stringParameterValue;
            Object parameterValue;

            if (webParameters.get(parameterName) == null) {
                stringParameterValue = "";
            } else {

                if (webParameters.get(parameterName).length != 1) {
                    throw new RuntimeException("El parametro de la petición http '" + parameterName + "' solo puede teenr un único valor pero tiene:" + webParameters.get(parameterName).length);
                }

                stringParameterValue = webParameters.get(parameterName)[0];
            }

            MetaData metaDataParameter = metaDataFactory.getMetaData(parameterType);
            if (metaDataParameter != null) {
                //El parámetro es una Entidad de negocio pero solo nos han pasado la clave primaria.

                //Vamos a obtener el tipo de la clave primaria
                Class primaryKeyType = metaDataParameter.getPropertiesMetaData().get(metaDataParameter.getPrimaryKeyPropertyName()).getType();

                //Ahora vamos a obtener el valor de la clave primaria
                Serializable primaryKey;
                try {
                    primaryKey = (Serializable) conversion.convertFromString(stringParameterValue, primaryKeyType);
                } catch (Exception ex) {
                    throw new BusinessException("El " + i + "º parámetro no tiene el formato adecuado para ser una PK:" + stringParameterValue);
                }

                if (primaryKey == null) {
                    parameterValue = null;
                } else {
                    //Y finalmente Leemos la entidad en función de la clave primaria
                    CRUDService crudServiceParameter = crudServiceFactory.getService(parameterType);
                    parameterValue = crudServiceParameter.read(primaryKey);
                    if (parameterValue == null) {
                        throw new BusinessException("El " + i + "º parámetro con valor '" + stringParameterValue + "' no es de ninguna entidad.");
                    }
                }
            } else {
                try {
                    parameterValue = conversion.convertFromString(stringParameterValue, parameterType);
                } catch (Exception ex) {
                    throw new BusinessException("El " + i + "º parámetro no tiene el formato adecuado:" + stringParameterValue);
                }
            }

            parameters.put(parameterName, parameterValue);

        }

        return parameters;
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
     * Obtiene un boolean a partir de un null
     *
     * @param s El String que se transforma en un Integer
     * @return Si el string es null se retornará false, sino se retornará el
     * booleano
     */
    private boolean getBooleanFromString(String s) {
        if (s == null) {
            return false;
        } else if (s.equals("0")) {
            return false;
        } else if (s.equals("no")) {
            return false;
        } else if (s.equals("false")) {
            return false;
        } else if (s.equals("1")) {
            return true;
        } else if (s.equals("yes")) {
            return true;
        } else if (s.equals("true")) {
            return true;
        } else if (s.equals("si")) {
            return true;
        } else {
            throw new RuntimeException("El String no se puede transformar a booleano:" + s);
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
     * Esta funcion transforma los valores iniciales de la petición HTTP en una
     * serie de objetos. Si las propiedaes hacen referencia a una propiedad de
     * una entida o a una clave primaria de una entidad se leerá dicha entidad
     * Sino simplemente se pondrá el valor de la entidad.
     *
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

            if (initialValueMetaData == null) {
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
                        value = conversion.convertFromString(rawValue, initialValueMetaData.getType());
                    } else {
                        MetaData leftPropertyMetaData = metaData.getPropertyMetaData(leftPropertyName);
                        if (leftPropertyMetaData.getMetaType() == MetaType.Entity) {
                            if (rigthPropertyName.equals(leftPropertyMetaData.getPrimaryKeyPropertyName())) {
                                //nos ham pasado la clave primaria de una entidad ,así que leemos la entidad
                                //y el valor inicial será el de la entidad ya leida y no el de la clave primaria.
                                CRUDService crudService = crudServiceFactory.getService(leftPropertyMetaData.getType());
                                Class primaryKeyType = leftPropertyMetaData.getPropertyMetaData(leftPropertyMetaData.getPrimaryKeyPropertyName()).getType();
                                Serializable primaryKey = (Serializable) conversion.convertFromString(rawValue, primaryKeyType);

                                realPropertyName = leftPropertyName;
                                value = crudService.read(primaryKey);
                            } else {
                                throw new RuntimeException("No se puede pasar una propiedad de una entidad, solo se permite la clave primaria:" + propertyName);
                            }
                        } else {
                            //Como la propieda anterior no era una entidad no era nada "raro" y nos habian pasado simplemente el valor
                            realPropertyName = propertyName;
                            value = conversion.convertFromString(rawValue, initialValueMetaData.getType());
                        }
                    }

                    break;
                case Component:
                    throw new RuntimeException("No se permite como valor inicial un componente:" + propertyName);
                case Entity:
                    //La propiedad corresponde a una entidad , así que se supondrá que el valor era la clave primaria de dicha entidad
                    CRUDService crudService = crudServiceFactory.getService(initialValueMetaData.getType());
                    Class primaryKeyType = initialValueMetaData.getPropertyMetaData(initialValueMetaData.getPrimaryKeyPropertyName()).getType();
                    Serializable primaryKey = (Serializable) conversion.convertFromString(rawValue, primaryKeyType);

                    realPropertyName = propertyName;
                    value = crudService.read(primaryKey);

                    break;
                default:
                    throw new RuntimeException("El meta tipo es desconocido:" + initialValueMetaData.getMetaType());
            }

            newParameters.put(realPropertyName, value);
        }

        return newParameters;
    }

    private Object executeNamedSearchParameters(CRUDService crudService, String namedSearch, Map<String, Object> filter, PageRequest pageRequest, List<Order> orders, SearchResponse searchResponse) throws BusinessException {
        try {
            if (filter == null) {
                filter = new HashMap<String, Object>();
            }
            if (orders == null) {
                orders = new ArrayList<Order>();
            }

            Method method = ReflectionUtil.getMethod(crudService.getClass(), namedSearch);
            if (method == null) {
                throw new BusinessException("No existe el método " + namedSearch + " en la clase de Servicio: " + crudService.getClass().getName());
            }

            NamedSearch namedSearchAnnotation = getAnnotation(crudService.getClass(), namedSearch, NamedSearch.class);
            String[] parameterNames = namedSearchAnnotation.parameterNames();
            if ((parameterNames == null) && (method.getParameterTypes().length > 0)) {
                throw new RuntimeException("Es necesario la lista de nombre de parametros para la anotación NameSearch del método:" + crudService.getClass().getName() + "." + namedSearch);
            }

            List args = new ArrayList();
            for (int i = 0; i < method.getParameterTypes().length; i++) {
                Class parameterClass = method.getParameterTypes()[i];
                if (parameterClass.isAssignableFrom(PageRequest.class) == true) {
                    args.add(pageRequest);
                } else if (parameterClass.isAssignableFrom(orders.getClass()) == true) {
                    args.add(orders);
                } else if (parameterClass.isAssignableFrom(SearchResponse.class) == true) {
                    args.add(searchResponse);
                } else {
                    Object parameterValue = filter.get(parameterNames[i]);
                    args.add(parameterValue);
                }

            }

            if (method.getParameterTypes().length != args.size()) {
                throw new RuntimeException("La lista de nombre de parametros para la anotación NameSearch debe coincidir con el nº de parámetro del método: " + crudService.getClass().getName() + "." + namedSearch);
            }

            Object result = method.invoke(crudService, args.toArray());

            return result;

        } catch (InvocationTargetException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof BusinessException) {
                BusinessException businessException = (BusinessException) cause;

                throw businessException;
            } else {
                throw new RuntimeException(cause);
            }
        } catch (RuntimeException ex) {
            throw ex;
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private Object executeNamedSearchFilters(CRUDService crudService, String namedSearch, List<Filter> filters, PageRequest pageRequest, List<Order> orders, SearchResponse searchResponse) throws BusinessException {
        try {

            if (filters == null) {
                filters = new ArrayList<Filter>();
            }

            if (orders == null) {
                orders = new ArrayList<Order>();
            }

            Method method = ReflectionUtil.getMethod(crudService.getClass(), namedSearch);
            if (method == null) {
                throw new BusinessException("No existe el método " + namedSearch + " en la clase de Servicio: " + crudService.getClass().getName());
            }

            NamedSearch namedSearchAnnotation = getAnnotation(crudService.getClass(), namedSearch, NamedSearch.class);

            List args = new ArrayList();
            for (int i = 0; i < method.getParameterTypes().length; i++) {
                Class parameterClass = method.getParameterTypes()[i];
                if (parameterClass.isAssignableFrom(filters.getClass()) == true) {
                    args.add(filters);
                } else if (parameterClass.isAssignableFrom(PageRequest.class) == true) {
                    args.add(pageRequest);
                } else if (parameterClass.isAssignableFrom(orders.getClass()) == true) {
                    args.add(orders);
                } else if (parameterClass.isAssignableFrom(SearchResponse.class) == true) {
                    args.add(searchResponse);
                } else {
                    throw new RuntimeException("El tipo del argumento no es válido");
                }
            }

            if (method.getParameterTypes().length != args.size()) {
                throw new RuntimeException("La lista de nombre de parametros para la anotación NameSearch debe coincidir con el nº de parámetro del método: " + crudService.getClass().getName() + "." + namedSearch);
            }

            Object result = method.invoke(crudService, args.toArray());

            return result;

        } catch (InvocationTargetException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof BusinessException) {
                BusinessException businessException = (BusinessException) cause;

                throw businessException;
            } else {
                throw new RuntimeException(cause);
            }
        } catch (RuntimeException ex) {
            throw ex;
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private Filter getFilterFromPropertyName(String rawPropertyName) {
        FilterOperator filterOperator;
        String propertyName;

        if (rawPropertyName.endsWith("__")) {
            filterOperator = null;
            propertyName = null;

            for (FilterOperator searcherfilterOperator : FilterOperator.values()) {
                String end = "__" + searcherfilterOperator.name().toUpperCase() + "__";
                if (rawPropertyName.endsWith(end)) {
                    filterOperator = searcherfilterOperator;
                    propertyName = rawPropertyName.substring(0, rawPropertyName.length() - end.length());
                    break;
                }
            }

            if (filterOperator == null) {
                if (rawPropertyName.matches("__[a-zA-Z]__$")) {
                    throw new RuntimeException("El formato del modificado no es valido:" + rawPropertyName);
                } else {
                    filterOperator = FilterOperator.eq;
                    propertyName = rawPropertyName;
                }
            }

        } else {
            filterOperator = FilterOperator.eq;
            propertyName = rawPropertyName;
        }

        return new Filter(propertyName, null, filterOperator);

    }

    private enum NameSearchType {

        PARAMETERS,
        FILTER
    }

    private NameSearchType getNamedSearchType(CRUDService crudService, String namedSearch) throws BusinessException {
        NamedSearch namedSearchAnnotation = getAnnotation(crudService.getClass(), namedSearch, NamedSearch.class);
        if (namedSearchAnnotation == null) {
            throw new BusinessException("No existe el método " + namedSearch + " en la clase de Servicio o no tiene la anotacion NamedSearch: " + crudService.getClass().getName());
        }

        if (namedSearchAnnotation.useFilters() == true) {
            return NameSearchType.FILTER;
        } else {
            return NameSearchType.PARAMETERS;
        }

    }

    private <T extends Annotation> T getAnnotation(Class clazz, String methodName, Class<T> annotationClass) {

        T annotation = ReflectionUtil.getAnnotation(clazz, methodName, annotationClass);
        if (annotation == null) {
            //Vemos si alguno de sus interfaces la tiene
            Class[] interfaces = clazz.getInterfaces();

            for (Class interfaze : interfaces) {
                annotation = ReflectionUtil.getAnnotation(interfaze, methodName, annotationClass);
                if (annotation != null) {
                    break;
                }
            }

        }

        return annotation;
    }

}
