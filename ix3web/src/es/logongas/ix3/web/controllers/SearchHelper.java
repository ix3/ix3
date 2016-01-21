/*
 * Copyright 2015 Lorenzo.
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

import es.logongas.ix3.businessprocess.BusinessProcess;
import es.logongas.ix3.businessprocess.CRUDBusinessProcess;
import es.logongas.ix3.core.BusinessException;
import es.logongas.ix3.core.Order;
import es.logongas.ix3.core.OrderDirection;
import es.logongas.ix3.core.PageRequest;
import es.logongas.ix3.core.Principal;
import es.logongas.ix3.core.conversion.Conversion;
import es.logongas.ix3.dao.DataSession;
import es.logongas.ix3.dao.Filter;
import es.logongas.ix3.dao.FilterOperator;
import es.logongas.ix3.dao.Filters;
import es.logongas.ix3.dao.SearchResponse;
import es.logongas.ix3.dao.metadata.MetaData;
import es.logongas.ix3.dao.metadata.MetaDataFactory;
import es.logongas.ix3.dao.metadata.MetaType;
import es.logongas.ix3.service.CRUDService;
import es.logongas.ix3.service.CRUDServiceFactory;
import es.logongas.ix3.util.ExceptionUtil;
import es.logongas.ix3.util.ReflectionUtil;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author logongas
 */
public class SearchHelper {

    @Autowired
    private Conversion conversion;
    @Autowired
    private MetaDataFactory metaDataFactory;
    @Autowired
    private CRUDServiceFactory crudServiceFactory;

    /**
     * Como se ordenan los datos
     *
     * @param metaData Metadatos de la que se quieren ordenar
     * @param orderBy Debe tener la forma de "[campo [asc desc],](campo [asc desc])*)
     * @return
     */
    public List<Order> getOrders(MetaData metaData, String orderBy) {
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

    public PageRequest getPageRequest(String httpParameterPageNumber, String httpParameterPageSize) {
        Integer pageNumber = getIntegerFromString(httpParameterPageNumber);
        Integer pageSize = getIntegerFromString(httpParameterPageSize);

        if ((pageNumber == null) && (pageSize == null)) {
            return null;
        } else if ((pageNumber != null) && (pageSize != null)) {
            return new PageRequest(pageNumber, pageSize);
        } else {
            throw new RuntimeException("Debe estar los 2 números para paginar pero solo está uno de ellos:" + httpParameterPageNumber + "-" + httpParameterPageSize);
        }
    }

    public SearchResponse getSearchResponse(String httpParameterDistinct) {
        boolean distinct = getBooleanFromString(httpParameterDistinct);

        SearchResponse searchResponse = new SearchResponse(distinct);

        return searchResponse;
    }

    public Filters getFiltersSearchFromWebParameters(Map<String, String[]> parametersMap, MetaData metaData) {
        Filters filters = new Filters();
        for (Map.Entry<String, String[]> entry : parametersMap.entrySet()) {
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

    public Map<String, Object> getParametersSearchFromWebParameters(Map<String, String[]> parametersMap, CRUDBusinessProcess crudBusinessProcess, String methodName, DataSession dataSession) throws BusinessException {
        Map<String, Object> parameters = new HashMap<String, Object>();
        Class businessProcessArgumentsClass = getBusinessProcessMethodArguments(crudBusinessProcess, methodName);
        Map<String, String[]> webParameters = removeDollarParameters(parametersMap);

        for (Field field : businessProcessArgumentsClass.getFields()) {
            String parameterName = field.getName();
            Class parameterType = field.getType();
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
                    throw new BusinessException("El parámetro " + field.getName() + " no tiene el formato adecuado para ser una PK:" + stringParameterValue);
                }

                if (primaryKey == null) {
                    parameterValue = null;
                } else {
                    //Y finalmente Leemos la entidad en función de la clave primaria
                    CRUDService crudServiceParameter = crudServiceFactory.getService(parameterType);
                    parameterValue = crudServiceParameter.read(dataSession, primaryKey);
                    if (parameterValue == null) {
                        throw new BusinessException("El parámetro " + field.getName() + " con valor '" + stringParameterValue + "' no es de ninguna entidad.");
                    }
                }
            } else {
                try {
                    parameterValue = conversion.convertFromString(stringParameterValue, parameterType);
                } catch (Exception ex) {
                    throw new BusinessException("El parámetro " + field.getName() + " no tiene el formato adecuado:" + stringParameterValue);
                }
            }

            parameters.put(parameterName, parameterValue);

        }

        return parameters;
    }

    /**
     * Esta funcion transforma los valores iniciales de la petición HTTP en una serie de objetos. Si las propiedaes hacen referencia a una propiedad de una entida o a una clave primaria de una entidad
     * se leerá dicha entidad Sino simplemente se pondrá el valor de la entidad.
     *
     * @param metaData Metada desde la que se quiere
     * @param parameters
     * @param dataSession
     * @return
     * @throws BusinessException
     */
    public Map<String, Object> getPropertiesFromParameters(MetaData metaData, Map<String, String[]> parameters, DataSession dataSession) throws BusinessException {
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
                                //nos han pasado la clave primaria de una entidad ,así que leemos la entidad
                                //y el valor inicial será el de la entidad ya leida y no el de la clave primaria.

                                Class primaryKeyType = leftPropertyMetaData.getPropertyMetaData(leftPropertyMetaData.getPrimaryKeyPropertyName()).getType();
                                Serializable primaryKey = (Serializable) conversion.convertFromString(rawValue, primaryKeyType);

                                realPropertyName = leftPropertyName;
                                value = crudServiceFactory.getService(leftPropertyMetaData.getType()).read(dataSession, primaryKey);

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
                    Class primaryKeyType = initialValueMetaData.getPropertyMetaData(initialValueMetaData.getPrimaryKeyPropertyName()).getType();
                    Serializable primaryKey = (Serializable) conversion.convertFromString(rawValue, primaryKeyType);

                    realPropertyName = propertyName;
                    value = crudServiceFactory.getService(initialValueMetaData.getType()).read(dataSession, primaryKey);

                    break;
                default:
                    throw new RuntimeException("El meta tipo es desconocido:" + initialValueMetaData.getMetaType());
            }

            newParameters.put(realPropertyName, value);
        }

        return newParameters;
    }

    public Object executeNamedSearchParameters(Principal principal, DataSession dataSession, CRUDBusinessProcess crudBusinessProcess, String namedSearch, Map<String, Object> filter, PageRequest pageRequest, List<Order> orders, SearchResponse searchResponse) throws BusinessException {
        try {
            if (getNamedSearchType(crudBusinessProcess, namedSearch) != NameSearchType.PARAMETERS) {
                throw new RuntimeException("El método '" + namedSearch + "' de '" + crudBusinessProcess.getClass() + "' debe ser de tipo Parameters");
            }

            if (filter == null) {
                filter = new HashMap<String, Object>();
            }
            if (orders == null) {
                orders = new ArrayList<Order>();
            }

            Class businessProcessArgumentsClass = getBusinessProcessMethodArguments(crudBusinessProcess, namedSearch);

            Object businessProcessArgument = businessProcessArgumentsClass.newInstance();

            for (Field field : businessProcessArgumentsClass.getFields()) {
                Class parameterClass = field.getType();
                if (parameterClass.isAssignableFrom(PageRequest.class) == true) {
                    field.set(businessProcessArgument, pageRequest);
                } else if (parameterClass.isAssignableFrom(orders.getClass()) == true) {
                    field.set(businessProcessArgument, orders);
                } else if (parameterClass.isAssignableFrom(SearchResponse.class) == true) {
                    field.set(businessProcessArgument, searchResponse);
                } else if (parameterClass.isAssignableFrom(Principal.class) == true) {
                    field.set(businessProcessArgument, principal);
                } else if (parameterClass.isAssignableFrom(DataSession.class) == true) {
                    field.set(businessProcessArgument, dataSession);
                } else {
                    Object parameterValue = filter.get(field.getName());
                    field.set(businessProcessArgument, parameterValue);
                }

            }

            Method method = ReflectionUtil.getDeclaredMethod(crudBusinessProcess.getClass(), namedSearch);
            Object result = method.invoke(crudBusinessProcess, businessProcessArgument);

            return result;

        } catch (InvocationTargetException ex) {
            BusinessException businessException = ExceptionUtil.getBusinessExceptionFromThrowable(ex);
            if (businessException != null) {
                throw businessException;
            } else {
                throw new RuntimeException(ex);
            }
        } catch (RuntimeException ex) {
            throw ex;
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public Object executeNamedSearchFilters(Principal principal, DataSession dataSession, CRUDBusinessProcess crudBusinessProcess, String namedSearch, Filters filters, PageRequest pageRequest, List<Order> orders, SearchResponse searchResponse) throws BusinessException {
        try {
            if (getNamedSearchType(crudBusinessProcess, namedSearch) != NameSearchType.FILTER) {
                throw new RuntimeException("El método '" + namedSearch + "' de '" + crudBusinessProcess.getClass() + "' debe ser de tipo Filter");
            }

            if (filters == null) {
                filters = new Filters();
            }

            if (orders == null) {
                orders = new ArrayList<Order>();
            }

            Class businessProcessArgumentsClass = getBusinessProcessMethodArguments(crudBusinessProcess, namedSearch);

            Object businessProcessArgument = businessProcessArgumentsClass.newInstance();

            for (Field field : businessProcessArgumentsClass.getFields()) {
                Class parameterClass = field.getType();
                if (parameterClass.isAssignableFrom(filters.getClass()) == true) {
                    field.set(businessProcessArgument, filters);
                } else if (parameterClass.isAssignableFrom(PageRequest.class) == true) {
                    field.set(businessProcessArgument, pageRequest);
                } else if (parameterClass.isAssignableFrom(orders.getClass()) == true) {
                    field.set(businessProcessArgument, orders);
                } else if (parameterClass.isAssignableFrom(SearchResponse.class) == true) {
                    field.set(businessProcessArgument, searchResponse);
                } else if (parameterClass.isAssignableFrom(Principal.class) == true) {
                    field.set(businessProcessArgument, principal);
                } else if (parameterClass.isAssignableFrom(DataSession.class) == true) {
                    field.set(businessProcessArgument, dataSession);
                } else {
                    throw new RuntimeException("El tipo del argumento no es válido");
                }
            }

            Method method = ReflectionUtil.getMethod(crudBusinessProcess.getClass(), namedSearch);
            Object result = method.invoke(crudBusinessProcess, businessProcessArgument);

            return result;

        } catch (InvocationTargetException ex) {
            BusinessException businessException = ExceptionUtil.getBusinessExceptionFromThrowable(ex);
            if (businessException != null) {
                throw businessException;
            } else {
                throw new RuntimeException(ex);
            }
        } catch (RuntimeException ex) {
            throw ex;
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public Filter getFilterFromPropertyName(String rawPropertyName) {
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

    public enum NameSearchType {

        PARAMETERS,
        FILTER
    }

    public NameSearchType getNamedSearchType(CRUDBusinessProcess crudBusinessProcess, String namedSearch) throws BusinessException {
        Class<BusinessProcess.BusinessProcessArguments> businessProcessArgumentsClass = getBusinessProcessMethodArguments(crudBusinessProcess, namedSearch);

        if (CRUDBusinessProcess.ParametrizedSearchArguments.class.isAssignableFrom(businessProcessArgumentsClass)) {
            return NameSearchType.PARAMETERS;
        } else if (CRUDBusinessProcess.SearchArguments.class.isAssignableFrom(businessProcessArgumentsClass)) {
            return NameSearchType.FILTER;
        } else {
            throw new RuntimeException("El método " + namedSearch + " de la clase " + crudBusinessProcess.getClass().getName() + " tiene un argumento de un tipo errorneo:" + businessProcessArgumentsClass.getName());
        }

    }

    private Class getBusinessProcessMethodArguments(CRUDBusinessProcess crudBusinessProcess, String methodName) {
        Method method = ReflectionUtil.getDeclaredMethod(crudBusinessProcess.getClass(), methodName);
        if (method == null) {
            throw new RuntimeException("El método " + methodName + " no exiete en la clase " + crudBusinessProcess.getClass().getName());
        }

        Class[] patametersTypes = method.getParameterTypes();
        if (patametersTypes == null) {
            throw new RuntimeException("El método " + methodName + " de la clase " + crudBusinessProcess.getClass().getName() + " no tiene argumentos");
        }

        if (patametersTypes.length != 1) {
            throw new RuntimeException("El método " + methodName + " de la clase " + crudBusinessProcess.getClass().getName() + " debe tenr un solo argumento");
        }

        Class parameterType = patametersTypes[0];

        if (BusinessProcess.BusinessProcessArguments.class.isAssignableFrom(parameterType) == false) {
            throw new RuntimeException("El método " + methodName + " de la clase " + crudBusinessProcess.getClass().getName() + " tiene un argumento de un tipo erroneo:" + parameterType.getName());
        }

        return parameterType;
    }

    /**
     * Obtiene un integer a partir de un null
     *
     * @param s El String que se transforma en un Integer
     * @return Si el string es null se retornará null, sino se retornará el Integer
     */
    public Integer getIntegerFromString(String s) {
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
    public boolean getBooleanFromString(String s) {
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
    public Map<String, String[]> removeDollarParameters(Map<String, String[]> parameterMap) {
        Map<String, String[]> cleanParameterMap = new LinkedHashMap<String, String[]>();

        for (String key : parameterMap.keySet()) {
            if ((key != null) && (key.trim().startsWith("$") == false)) {
                cleanParameterMap.put(key, parameterMap.get(key));
            }
        }

        return cleanParameterMap;
    }

}
