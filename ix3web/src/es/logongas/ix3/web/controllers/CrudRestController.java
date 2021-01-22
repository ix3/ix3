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

import es.logongas.ix3.businessprocess.CRUDBusinessProcess;
import es.logongas.ix3.businessprocess.CRUDBusinessProcessFactory;
import es.logongas.ix3.web.util.MimeType;
import es.logongas.ix3.web.util.HttpResult;
import es.logongas.ix3.core.BusinessException;
import es.logongas.ix3.core.Order;
import es.logongas.ix3.core.PageRequest;
import es.logongas.ix3.dao.SearchResponse;
import es.logongas.ix3.dao.metadata.MetaData;
import es.logongas.ix3.dao.metadata.MetaDataFactory;
import es.logongas.ix3.dao.DataSession;
import es.logongas.ix3.dao.DataSessionFactory;
import es.logongas.ix3.core.Principal;
import es.logongas.ix3.dao.Filters;
import es.logongas.ix3.service.CRUDService;
import es.logongas.ix3.service.CRUDServiceFactory;
import es.logongas.ix3.util.ReflectionUtil;
import es.logongas.ix3.web.businessprocess.SchemaBusinessProcess;
import es.logongas.ix3.web.controllers.schema.Schema;
import es.logongas.ix3.web.json.JsonFactory;
import es.logongas.ix3.web.json.beanmapper.BeanMapper;
import es.logongas.ix3.web.json.JsonReader;
import es.logongas.ix3.web.json.beanmapper.Expands;
import es.logongas.ix3.web.util.ControllerHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
public class CrudRestController {

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
    private CRUDBusinessProcessFactory crudBusinessProcessFactory;
    @Autowired
    private CRUDServiceFactory crudServiceFactory;
    @Autowired
    private ControllerHelper controllerHelper;
    @Autowired
    private DataSessionFactory dataSessionFactory;
    @Autowired
    private JsonFactory jsonFactory;
    @Autowired
    private SearchHelper searchHelper;
    @Autowired
    private SchemaBusinessProcess schemaBusinessProcess;

    @RequestMapping(value = {"{path}/{entityName}/" + PATH_SCHEMA}, method = RequestMethod.GET, produces = "application/json")
    public void schema(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, @PathVariable("entityName") String entityName) {

        try (DataSession dataSession = dataSessionFactory.getDataSession()) {
            Principal principal = controllerHelper.getPrincipal(httpServletRequest, httpServletResponse, dataSession);
            MetaData metaData = metaDataFactory.getMetaData(entityName);
            if (metaData == null) {
                throw new RuntimeException("No existe la entidad " + entityName);
            }
            Expands expands = controllerHelper.getRequestExpands(httpServletRequest);

            Schema schema = schemaBusinessProcess.getSchema(new SchemaBusinessProcess.GetSchemaArguments(principal, dataSession, metaData.getType(), expands));

            HttpResult httpResult = new HttpResult(Schema.class, schema, 200, true, new BeanMapper(Schema.class, null, "<*"), MimeType.JSON);

            controllerHelper.objectToHttpResponse(httpResult, httpServletRequest, httpServletResponse);
        } catch (Exception ex) {
            controllerHelper.exceptionToHttpResponse(ex, httpServletRequest, httpServletResponse);
        }

    }

    @RequestMapping(value = {"{path}/{entityName}"}, method = RequestMethod.GET, produces = "application/json")
    public void search(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, @PathVariable("entityName") String entityName) throws BusinessException {
        try (DataSession dataSession = dataSessionFactory.getDataSession()) {
            Principal principal = controllerHelper.getPrincipal(httpServletRequest, httpServletResponse, dataSession);
            MetaData metaData = metaDataFactory.getMetaData(entityName);
            if (metaData == null) {
                throw new RuntimeException("No existe la entidad " + entityName);
            }
            CRUDBusinessProcess crudBusinessProcess = crudBusinessProcessFactory.getBusinessProcess(metaData.getType());

            List<Order> orders = searchHelper.getOrders(metaData, httpServletRequest.getParameter(PARAMETER_ORDERBY));
            PageRequest pageRequest = searchHelper.getPageRequest(httpServletRequest.getParameter(PARAMETER_PAGENUMBER), httpServletRequest.getParameter(PARAMETER_PAGESIZE));
            SearchResponse searchResponse = searchHelper.getSearchResponse(httpServletRequest.getParameter(PARAMETER_DISTINCT));
            String namedSearch = httpServletRequest.getParameter(PARAMETER_NAMEDSEARCH);
            Map<String, String[]> parametersMap = httpServletRequest.getParameterMap();

            Object result;
            if ((namedSearch != null) && (namedSearch.trim().equals("") == false)) {
                switch (searchHelper.getNamedSearchType(crudBusinessProcess, namedSearch)) {
                    case FILTER:
                        Filters filters = searchHelper.getFiltersSearchFromWebParameters(parametersMap, metaData);
                        result = searchHelper.executeNamedSearchFilters(principal, dataSession, crudBusinessProcess, namedSearch, filters, pageRequest, orders, searchResponse);

                        break;
                    case PARAMETERS:
                        Map<String, Object> parameters = searchHelper.getParametersSearchFromWebParameters(parametersMap, crudBusinessProcess, namedSearch, dataSession);
                        result = searchHelper.executeNamedSearchParameters(principal, dataSession, crudBusinessProcess, namedSearch, parameters, pageRequest, orders, searchResponse);
                        break;
                    default:
                        throw new RuntimeException("El tipo del name search es desconocido:" + searchHelper.getNamedSearchType(crudBusinessProcess, namedSearch));
                }
            } else {
                Filters filters = searchHelper.getFiltersSearchFromWebParameters(parametersMap, metaData);
                if (pageRequest == null) {
                    result = crudBusinessProcess.search(new CRUDBusinessProcess.SearchArguments(principal, dataSession, filters, orders, searchResponse));
                } else {
                    result = crudBusinessProcess.pageableSearch(new CRUDBusinessProcess.PageableSearchArguments(principal, dataSession, filters, orders, pageRequest, searchResponse));
                }
            }

            controllerHelper.objectToHttpResponse(new HttpResult(metaData.getType(), result), httpServletRequest, httpServletResponse);

        } catch (Exception ex) {
            controllerHelper.exceptionToHttpResponse(ex, httpServletRequest, httpServletResponse);
        }
    }

    @RequestMapping(value = {"{path}/{entityName}/{id}"}, method = RequestMethod.GET, produces = "application/json")
    public void read(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, @PathVariable("entityName") String entityName, @PathVariable("id") int id) {

        try (DataSession dataSession = dataSessionFactory.getDataSession()) {
            MetaData metaData = metaDataFactory.getMetaData(entityName);
            if (metaData == null) {
                throw new RuntimeException("No existe la entidad " + entityName);
            }
            Principal principal = controllerHelper.getPrincipal(httpServletRequest, httpServletResponse, dataSession);
            CRUDBusinessProcess crudBusinessProcess = crudBusinessProcessFactory.getBusinessProcess(metaData.getType());
            Object entity = crudBusinessProcess.read(new CRUDBusinessProcess.ReadArguments(principal, dataSession, id));
            controllerHelper.objectToHttpResponse(new HttpResult(metaData.getType(), entity), httpServletRequest, httpServletResponse);
        } catch (Exception ex) {
            controllerHelper.exceptionToHttpResponse(ex, httpServletRequest, httpServletResponse);
        }

    }

    @RequestMapping(value = {"{path}/{entityName}/{id}/{child}"}, method = RequestMethod.GET, produces = "application/json")
    public void readChild(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, @PathVariable("entityName") String entityName, @PathVariable("id") int id, @PathVariable("child") String child) {
        try (DataSession dataSession = dataSessionFactory.getDataSession()) {
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
            Principal principal = controllerHelper.getPrincipal(httpServletRequest, httpServletResponse, dataSession);

            CRUDBusinessProcess crudBusinessProcess = crudBusinessProcessFactory.getBusinessProcess(metaData.getType());
            Object entity = crudBusinessProcess.read(new CRUDBusinessProcess.ReadArguments(principal, dataSession, id));
            Object childData;
            if (entity != null) {
                childData = ReflectionUtil.getValueFromBean(entity, child);
            } else {
                //Si no hay datos , retornamos una lista vacia
                childData = new ArrayList();
            }

            controllerHelper.objectToHttpResponse(new HttpResult(metaData.getPropertiesMetaData().get(child).getType(), childData), httpServletRequest, httpServletResponse);

        } catch (Exception ex) {
            controllerHelper.exceptionToHttpResponse(ex, httpServletRequest, httpServletResponse);
        }

    }

    @RequestMapping(value = {"{path}/{entityName}/" + PATH_CREATE}, method = RequestMethod.GET, produces = "application/json")
    public void create(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, @PathVariable("entityName") String entityName) {
        try (DataSession dataSession = dataSessionFactory.getDataSession()) {
            Principal principal = controllerHelper.getPrincipal(httpServletRequest, httpServletResponse, dataSession);
            MetaData metaData = metaDataFactory.getMetaData(entityName);
            if (metaData == null) {
                throw new RuntimeException("No existe la entidad " + entityName);
            }
            CRUDBusinessProcess crudBusinessProcess = crudBusinessProcessFactory.getBusinessProcess(metaData.getType());
            Map<String, String[]> parametersMap = httpServletRequest.getParameterMap();

            Map<String, Object> initialProperties = searchHelper.getPropertiesFromParameters(metaData, searchHelper.removeDollarParameters(parametersMap), dataSession);
            Object entity = crudBusinessProcess.create(new CRUDBusinessProcess.CreateArguments(principal, dataSession, initialProperties));

            controllerHelper.objectToHttpResponse(new HttpResult(metaData.getType(), entity), httpServletRequest, httpServletResponse);

        } catch (Exception ex) {
            controllerHelper.exceptionToHttpResponse(ex, httpServletRequest, httpServletResponse);
        }

    }

    @RequestMapping(value = {"{path}/{entityName}"}, method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public void insert(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, @PathVariable("entityName") String entityName, @RequestBody String jsonIn) {

        try (DataSession dataSession = dataSessionFactory.getDataSession()) {
            Principal principal = controllerHelper.getPrincipal(httpServletRequest, httpServletResponse, dataSession);
            MetaData metaData = metaDataFactory.getMetaData(entityName);
            if (metaData == null) {
                throw new RuntimeException("No existe la entidad " + entityName);
            }

            JsonReader jsonReader = jsonFactory.getJsonReader(metaData.getType());
            Object entity = jsonReader.fromJson(jsonIn, controllerHelper.getBeanMapper(httpServletRequest), dataSession);

            CRUDBusinessProcess crudBusinessProcess = crudBusinessProcessFactory.getBusinessProcess(metaData.getType());

            Object resultEntity = crudBusinessProcess.insert(new CRUDBusinessProcess.InsertArguments(principal, dataSession, entity));

            controllerHelper.objectToHttpResponse(new HttpResult(metaData.getType(), resultEntity, HttpServletResponse.SC_CREATED), httpServletRequest, httpServletResponse);

        } catch (Exception ex) {
            controllerHelper.exceptionToHttpResponse(ex, httpServletRequest, httpServletResponse);
        }

    }

    @RequestMapping(value = {"{path}/{entityName}/{id}"}, method = RequestMethod.PUT, consumes = "application/json", produces = "application/json")
    public void update(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, @PathVariable("entityName") String entityName, @PathVariable("id") int id, @RequestBody String jsonIn) throws BusinessException {
        try (DataSession dataSession = dataSessionFactory.getDataSession()) {
            Principal principal = controllerHelper.getPrincipal(httpServletRequest, httpServletResponse, dataSession);
            MetaData metaData = metaDataFactory.getMetaData(entityName);
            if (metaData == null) { 
                throw new RuntimeException("No existe la entidad " + entityName);
            }
            CRUDBusinessProcess crudBusinessProcess = crudBusinessProcessFactory.getBusinessProcess(metaData.getType());

            JsonReader jsonReader = jsonFactory.getJsonReader(metaData.getType());
            Object entity = jsonReader.fromJson(jsonIn, controllerHelper.getBeanMapper(httpServletRequest), dataSession);

            int entityId = (Integer) ReflectionUtil.getValueFromBean(entity, metaData.getPrimaryKeyPropertyName());

            if (entityId != id) {
                throw new RuntimeException("No coincciden el id de la entidad y el de la url:" + id + "," + entityId);
            }

            Object originalEntity = crudServiceFactory.getService(metaData.getType()).readOriginal(dataSession, entityId);

            Object resultEntity = crudBusinessProcess.update(new CRUDBusinessProcess.UpdateArguments(principal, dataSession, entity, originalEntity));

            controllerHelper.objectToHttpResponse(new HttpResult(metaData.getType(), resultEntity), httpServletRequest, httpServletResponse);

        } catch (Exception ex) {
            controllerHelper.exceptionToHttpResponse(ex, httpServletRequest, httpServletResponse);
        }
    }

    @RequestMapping(value = {"{path}/{entityName}/{id}"}, method = RequestMethod.DELETE)
    public void delete(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, @PathVariable("entityName") String entityName, @PathVariable("id") int id) throws BusinessException {
        try (DataSession dataSession = dataSessionFactory.getDataSession()) {
            Principal principal = controllerHelper.getPrincipal(httpServletRequest, httpServletResponse, dataSession);
            MetaData metaData = metaDataFactory.getMetaData(entityName);
            if (metaData == null) {
                throw new RuntimeException("No existe la entidad " + entityName);
            }
            
            CRUDService crudservice = crudServiceFactory.getService(metaData.getType());
            Object entity = crudservice.read(dataSession, id);
            
            CRUDBusinessProcess crudBusinessProcess = crudBusinessProcessFactory.getBusinessProcess(metaData.getType());
            boolean deletedSuccess = crudBusinessProcess.delete(new CRUDBusinessProcess.DeleteArguments(principal, dataSession, entity));
            if (deletedSuccess == false) {
                throw new BusinessException("No existe la entidad a borrar");
            }

            controllerHelper.objectToHttpResponse(new HttpResult(null), httpServletRequest, httpServletResponse);

        } catch (Exception ex) {
            controllerHelper.exceptionToHttpResponse(ex, httpServletRequest, httpServletResponse);
        }
    }

}
