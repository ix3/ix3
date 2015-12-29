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

import es.logongas.ix3.web.controllers.helper.AbstractRestController;
import es.logongas.ix3.web.controllers.command.CommandResult;
import es.logongas.ix3.web.controllers.command.Command;
import es.logongas.ix3.core.BusinessException;
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
import es.logongas.ix3.service.FilterSearch;
import es.logongas.ix3.service.ParameterSearch;
import es.logongas.ix3.util.ReflectionUtil;
import es.logongas.ix3.web.controllers.command.MimeType;
import es.logongas.ix3.web.controllers.schema.Schema;
import es.logongas.ix3.web.controllers.schema.SchemaFactory;
import es.logongas.ix3.web.json.beanmapper.BeanMapper;
import es.logongas.ix3.web.json.JsonReader;
import es.logongas.ix3.web.json.beanmapper.Expands;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
public class CrudRestController extends AbstractRestController {

    private static final Log log = LogFactory.getLog(CrudRestController.class);

    private final String PARAMETER_ORDERBY = "$orderby";
    private final String PARAMETER_PAGENUMBER = "$pagenumber";
    private final String PARAMETER_PAGESIZE = "$pagesize";
    private final String PARAMETER_DISTINCT = "$distinct";
    private final String PARAMETER_NAMEDSEARCH = "$namedsearch";
    private final String PATH_SCHEMA = "$schema";
    private final String PATH_CREATE = "$create";

    @Autowired
    private MetaDataFactory metaDataFactory;

    @Autowired
    private Conversion conversion;

    @Autowired
    private CRUDServiceFactory crudServiceFactory;

    @RequestMapping(value = {"{path}/{entityName}/" + PATH_SCHEMA}, method = RequestMethod.GET, produces = "application/json")
    public void schema(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, @PathVariable("entityName") String entityName) {

        MetaData metaData = metaDataFactory.getMetaData(entityName);
        if (metaData == null) {
            throw new RuntimeException("No existe la entidad " + entityName);
        }
        Expands expands = Expands.createExpandsWithoutAsterisk(httpServletRequest.getParameter(PARAMETER_EXPAND));

        restMethod(httpServletRequest, httpServletResponse, "schema", metaData.getType(), new Command() {
            public MetaData metaData;
            public Expands expands;

            public Command inicialize(MetaData metaData, Expands expands) {
                this.metaData = metaData;
                this.expands = expands;
                return this;
            }

            @Override
            public CommandResult run() throws Exception, BusinessException {
                Schema schema = (new SchemaFactory()).getSchema(metaData, metaDataFactory, crudServiceFactory, expands);
                CommandResult commandResult = new CommandResult(Schema.class, schema, 200, true, new BeanMapper(Schema.class, null, "<*"), MimeType.JSON);
                return commandResult;
            }

        }.inicialize(metaData, expands));

    }

    @RequestMapping(value = {"{path}/{entityName}"}, method = RequestMethod.GET, produces = "application/json")
    public void search(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, @PathVariable("entityName") String entityName) throws BusinessException {

        MetaData metaData = metaDataFactory.getMetaData(entityName);
        if (metaData == null) {
            throw new RuntimeException("No existe la entidad " + entityName);
        }
        CRUDService crudService = crudServiceFactory.getService(metaData.getType());
        List<Order> orders = getOrders(metaData, httpServletRequest.getParameter(PARAMETER_ORDERBY));
        PageRequest pageRequest = getPageRequest(httpServletRequest);
        SearchResponse searchResponse = getSearchResponse(httpServletRequest);
        String namedSearch = httpServletRequest.getParameter(PARAMETER_NAMEDSEARCH);
        Map<String, String[]> parametersMap = httpServletRequest.getParameterMap();
        List<Filter> filters;
        Map<String, Object> parameters;

        if ((namedSearch != null) && (namedSearch.trim().equals("") == false)) {
            switch (getNamedSearchType(crudService, namedSearch)) {
                case FILTER:
                    filters = getFiltersSearchFromWebParameters(parametersMap, metaData);
                    parameters=null;
                    break;
                case PARAMETERS:
                    parameters = getParametersSearchFromWebParameters(parametersMap, crudService, namedSearch);
                    filters=null;
                    break;
                default:
                    throw new RuntimeException("El tipo del name search es desconocido:" + getNamedSearchType(crudService, namedSearch));
            }
        } else {
            filters = getFiltersSearchFromWebParameters(parametersMap, metaData);
            parameters=null;
        }

        restMethod(httpServletRequest, httpServletResponse, "search", metaData.getType(), new Command() {

            public MetaData metaData;
            public List<Order> orders;
            public PageRequest pageRequest;
            public SearchResponse searchResponse;
            public String namedSearch;
            public Map<String, String[]> parametersMap;
            public List<Filter> filters;
            public Map<String, Object> parameters;
        
            public Command inicialize(MetaData metaData, List<Order> orders, PageRequest pageRequest, SearchResponse searchResponse, String namedSearch, Map<String, String[]> parametersMap,List<Filter> filters,Map<String, Object> parameters) {
                this.metaData = metaData;
                this.orders = orders;
                this.pageRequest = pageRequest;
                this.searchResponse = searchResponse;
                this.namedSearch = namedSearch;
                this.parametersMap = parametersMap;
                this.filters=filters;
                this.parameters=parameters;
                
                return this;
            }

            @Override
            public CommandResult run() throws Exception, BusinessException {

                CRUDService crudService = crudServiceFactory.getService(metaData.getType());

                Object result;
                if ((namedSearch != null) && (namedSearch.trim().equals("") == false)) {
                    switch (getNamedSearchType(crudService, namedSearch)) {
                        case FILTER:
                            result = executeNamedSearchFilters(crudService, namedSearch, filters, pageRequest, orders, searchResponse);
                            break;
                        case PARAMETERS:
                            result = executeNamedSearchParameters(crudService, namedSearch, parameters, pageRequest, orders, searchResponse);
                            break;
                        default:
                            throw new RuntimeException("El tipo del name search es desconocido:" + getNamedSearchType(crudService, namedSearch));
                    }
                } else {
                    if (pageRequest == null) {
                        result = crudService.search(filters, orders, searchResponse);
                    } else {
                        result = crudService.pageableSearch(filters, orders, pageRequest, searchResponse);
                    }
                }
                return new CommandResult(metaData.getType(), result);

            }
        }.inicialize(metaData, orders, pageRequest, searchResponse, namedSearch, parametersMap,filters, parameters));

    }

    @RequestMapping(value = {"{path}/{entityName}/{id}"}, method = RequestMethod.GET, produces = "application/json")
    public void read(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, @PathVariable("entityName") String entityName, @PathVariable("id") int id) {
        MetaData metaData = metaDataFactory.getMetaData(entityName);
        if (metaData == null) {
            throw new RuntimeException("No existe la entidad " + entityName);
        }

        restMethod(httpServletRequest, httpServletResponse, "read", metaData.getType(), new Command() {

            public MetaData metaData;
            public int id;

            public Command inicialize(MetaData metaData, int id) {
                this.metaData = metaData;
                this.id = id;
                return this;
            }

            @Override
            public CommandResult run() throws Exception, BusinessException {

                CRUDService crudService = crudServiceFactory.getService(metaData.getType());
                Object entity = crudService.read(id);

                return new CommandResult(metaData.getType(), entity);

            }
        }.inicialize(metaData, id));

    }

    @RequestMapping(value = {"{path}/{entityName}/{id}/{child}"}, method = RequestMethod.GET, produces = "application/json")
    public void readChild(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, @PathVariable("entityName") String entityName, @PathVariable("id") int id, @PathVariable("child") String child) {

        MetaData metaData = metaDataFactory.getMetaData(entityName);
        if (metaData == null) {
            throw new RuntimeException("No existe la entidad " + entityName);
        }
        if (metaData.getPropertiesMetaData().get(child) == null) {
            throw new RuntimeException("En la entidad '" + entityName + "' no existe la propiedad '" + child + "'");
        }
        if (metaData.getPropertiesMetaData().get(child).isCollection() == false) {
            throw new RuntimeException("En la entidad '" + entityName + "'  la propiedad '" + child + "' no es una colección");
        }

        restMethod(httpServletRequest, httpServletResponse, "readChild", metaData.getType(), new Command() {

            public MetaData metaData;
            public int id;
            public String child;

            public Command inicialize(MetaData metaData, int id, String child) {
                this.metaData = metaData;
                this.id = id;
                this.child = child;
                return this;
            }

            @Override
            public CommandResult run() throws Exception, BusinessException {

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
        }.inicialize(metaData, id, child));

    }

    @RequestMapping(value = {"{path}/{entityName}/" + PATH_CREATE}, method = RequestMethod.GET, produces = "application/json")
    public void create(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, @PathVariable("entityName") String entityName) {

        MetaData metaData = metaDataFactory.getMetaData(entityName);
        if (metaData == null) {
            throw new RuntimeException("No existe la entidad " + entityName);
        }
        Map<String, String[]> parametersMap = httpServletRequest.getParameterMap();

        restMethod(httpServletRequest, httpServletResponse, "create", metaData.getType(), new Command() {

            public MetaData metaData;
            public Map<String, String[]> parametersMap;

            public Command inicialize(MetaData metaData, Map<String, String[]> parametersMap) {
                this.metaData = metaData;
                this.parametersMap = parametersMap;
                return this;
            }

            @Override
            public CommandResult run() throws Exception, BusinessException {

                CRUDService crudService = crudServiceFactory.getService(metaData.getType());
                Map<String, Object> initialProperties = getPropertiesFromParameters(metaData, removeDollarParameters(parametersMap));
                Object entity = crudService.create(initialProperties);

                return new CommandResult(metaData.getType(), entity);

            }
        }.inicialize(metaData, parametersMap));
    }

    @RequestMapping(value = {"{path}/{entityName}"}, method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public void insert(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, @PathVariable("entityName") String entityName, @RequestBody String jsonIn) {

        MetaData metaData = metaDataFactory.getMetaData(entityName);
        if (metaData == null) {
            throw new RuntimeException("No existe la entidad " + entityName);
        }
        JsonReader jsonReader = jsonFactory.getJsonReader(metaData.getType());
        Object entity = jsonReader.fromJson(jsonIn, getBeanMapper(httpServletRequest));

        restMethod(httpServletRequest, httpServletResponse, "insert", metaData.getType(), new Command() {
            public MetaData metaData;
            public Object entity;

            public Command inicialize(MetaData metaData, Object entity) {
                this.metaData = metaData;
                this.entity = entity;

                return this;
            }

            @Override
            public CommandResult run() throws Exception, BusinessException {

                CRUDService crudService = crudServiceFactory.getService(metaData.getType());

                crudService.insert(entity);

                return new CommandResult(metaData.getType(), entity, HttpServletResponse.SC_CREATED);

            }
        }.inicialize(metaData, entity));
    }

    @RequestMapping(value = {"{path}/{entityName}/{id}"}, method = RequestMethod.PUT, consumes = "application/json", produces = "application/json")
    public void update(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, @PathVariable("entityName") String entityName, @PathVariable("id") int id, @RequestBody String jsonIn) throws BusinessException {
        MetaData metaData = metaDataFactory.getMetaData(entityName);
        if (metaData == null) {
            throw new RuntimeException("No existe la entidad " + entityName);
        }
        CRUDService crudService = crudServiceFactory.getService(metaData.getType());        
        
        JsonReader jsonReader = jsonFactory.getJsonReader(metaData.getType());
        Object entity = jsonReader.fromJson(jsonIn, getBeanMapper(httpServletRequest));

        int entityId=(Integer)ReflectionUtil.getValueFromBean(entity,metaData.getPrimaryKeyPropertyName());
        
        if (entityId!=id) {
            throw new RuntimeException("No coincciden el id de la entidad y el de la url:" + id + "," + entityId);
        }
        
        Object originalEntity=crudService.readOriginal(entityId);
        
        restMethod(httpServletRequest, httpServletResponse, "update", metaData.getType(), new Command() {
            public MetaData metaData;
            public Object entity;
            public Object originalEntity;

            public Command inicialize(MetaData metaData, Object entity, Object originalEntity) {
                this.metaData = metaData;
                this.entity = entity;
                this.originalEntity = originalEntity;

                return this;
            }

            @Override
            public CommandResult run() throws Exception, BusinessException {

                CRUDService crudService = crudServiceFactory.getService(metaData.getType());

                crudService.update(entity);

                return new CommandResult(metaData.getType(), entity);

            }
        }.inicialize(metaData, entity,originalEntity));

    }

    @RequestMapping(value = {"{path}/{entityName}/{id}"}, method = RequestMethod.DELETE)
    public void delete(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, @PathVariable("entityName") String entityName, @PathVariable("id") int id) throws BusinessException {
        MetaData metaData = metaDataFactory.getMetaData(entityName);
        if (metaData == null) {
            throw new RuntimeException("No existe la entidad " + entityName);
        }
        CRUDService crudService = crudServiceFactory.getService(metaData.getType());
        Object entity = crudService.read(id);

        restMethod(httpServletRequest, httpServletResponse, "delete", metaData.getType(), new Command() {

            public MetaData metaData;
            public Object entity;

            public Command inicialize(MetaData metaData, Object entity) {
                this.metaData = metaData;
                this.entity = entity;
                return this;
            }

            @Override
            public CommandResult run() throws Exception, BusinessException {
                CRUDService crudService = crudServiceFactory.getService(metaData.getType());

                boolean deletedSuccess = crudService.delete((Integer) ReflectionUtil.getValueFromBean(entity, metaData.getPrimaryKeyPropertyName()));
                if (deletedSuccess == false) {
                    throw new BusinessException("No existe la entidad a borrar");
                }

                return new CommandResult(null);

            }
        }.inicialize(metaData, entity));
    }

    /**
     * Como se ordenan los datos
     *
     * @param metaData Metadatos de la que se quieren ordenar
     * @param orderBy Debe tener la forma de "[campo [asc desc],](campo [asc desc])*)
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

    private List<Filter> getFiltersSearchFromWebParameters(Map<String, String[]> parametersMap, MetaData metaData) {
        List<Filter> filters = new ArrayList<Filter>();
        for (Entry<String, String[]> entry : parametersMap.entrySet()) {
            String rawPropertyName = entry.getKey();
            Filter filter = getFilterFromPropertyName(rawPropertyName);

            MetaData propertyMetaData = metaData.getPropertiesMetaData().get(filter.getPropertyName());
            if (propertyMetaData != null) {
                Class propertyType = propertyMetaData.getType();
                String[] parameterValues = entry.getValue();
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

    private Map<String, Object> getParametersSearchFromWebParameters(Map<String, String[]> parametersMap, CRUDService crudService, String methodName) throws BusinessException {
        Map<String, Object> parameters = new HashMap<String, Object>();

        Method method = ReflectionUtil.getMethod(crudService.getClass(), methodName);
        if (method == null) {
            throw new BusinessException("No existe el método " + methodName + " en la clase " + crudService.getClass().getName());
        }

        ParameterSearch parameterSearchAnnotation = getAnnotation(crudService.getClass(), methodName, ParameterSearch.class);

        String[] parameterNames = parameterSearchAnnotation.parameterNames();
        if ((parameterNames == null) && (method.getParameterTypes().length > 0)) {
            throw new RuntimeException("Es necesario la lista de nombre de parametros para la anotación NameSearch del método:" + crudService.getClass().getName() + "." + methodName);
        }

        if (method.getParameterTypes().length != parameterNames.length) {
            throw new RuntimeException("La lista de nombre de parametros para la anotación NameSearch debe coincidir con el nº de parámetro del método: " + crudService.getClass().getName() + "." + methodName);
        }

        Map<String, String[]> webParameters = removeDollarParameters(parametersMap);
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
     * @return Si el string es null se retornará null, sino se retornará el Integer
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
     * @return Si el string es null se retornará false, sino se retornará el booleano
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
     * Esta función quita aquellos parametros que viene nen la petición http que empiezan por "$" pq esos parámetros tienen un significado especial y no son "normales" para el modelo de negocio.
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
     * Esta funcion transforma los valores iniciales de la petición HTTP en una serie de objetos. Si las propiedaes hacen referencia a una propiedad de una entida o a una clave primaria de una entidad
     * se leerá dicha entidad Sino simplemente se pondrá el valor de la entidad.
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

            ParameterSearch parameterSearchAnnotation = getAnnotation(crudService.getClass(), namedSearch, ParameterSearch.class);
            String[] parameterNames = parameterSearchAnnotation.parameterNames();
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

            FilterSearch filterSearchAnnotation = getAnnotation(crudService.getClass(), namedSearch, FilterSearch.class);
            if (filterSearchAnnotation == null) {
                throw new RuntimeException("El método '" + namedSearch + "' debe tener la anotación FilterSearch");
            }

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
        int indexDollar = rawPropertyName.indexOf("$");

        if (indexDollar > 0) {
            try {
                filterOperator = FilterOperator.valueOf(rawPropertyName.substring(indexDollar + 1));
            } catch (IllegalArgumentException ex) {
                throw new RuntimeException("El formato del modificador de la propiedad no es valido:" + rawPropertyName);
            }
            propertyName = rawPropertyName.substring(0, indexDollar);
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
        FilterSearch filterSearchAnnotation;
        ParameterSearch parameterSearchAnnotation;

        filterSearchAnnotation = getAnnotation(crudService.getClass(), namedSearch, FilterSearch.class);
        parameterSearchAnnotation = getAnnotation(crudService.getClass(), namedSearch, ParameterSearch.class);

        if ((filterSearchAnnotation == null) && (parameterSearchAnnotation == null)) {
            throw new BusinessException("No existe el método " + namedSearch + " en la clase de Servicio o no tiene la anotacion FilterSearch o ParameterSearch: " + crudService.getClass().getName());
        } else if ((filterSearchAnnotation == null) && (parameterSearchAnnotation != null)) {
            return NameSearchType.PARAMETERS;
        }
        if ((filterSearchAnnotation != null) && (parameterSearchAnnotation == null)) {
            return NameSearchType.FILTER;
        }
        if ((filterSearchAnnotation != null) && (parameterSearchAnnotation != null)) {
            throw new BusinessException("El método " + namedSearch + " no puede tener a la vez las anotaciones anotacion FilterSearch o ParameterSearch: " + crudService.getClass().getName());
        } else {
            throw new BusinessException("Error de lógica");
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
