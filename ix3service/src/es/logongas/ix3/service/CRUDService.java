/*
 * Copyright 2014 Lorenzo.
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


package es.logongas.ix3.service;

import es.logongas.ix3.core.BusinessException;
import es.logongas.ix3.core.Order;
import es.logongas.ix3.core.Page;
import es.logongas.ix3.core.PageRequest;
import es.logongas.ix3.dao.DataSession;
import es.logongas.ix3.dao.Filter;
import es.logongas.ix3.dao.SearchResponse;
import es.logongas.ix3.core.Principal;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author Lorenzo
 * @param <EntityType>
 * @param <PrimaryKeyType> 
 */
public interface CRUDService<EntityType,PrimaryKeyType extends Serializable> extends Service<EntityType> {
    EntityType create(DataSession dataSession,Map<String,Object> initialProperties) throws BusinessException;
    
    EntityType read(DataSession dataSession,PrimaryKeyType primaryKey) throws BusinessException;
    EntityType readOriginal(DataSession dataSession,PrimaryKeyType primaryKey) throws BusinessException;
    EntityType readByNaturalKey(DataSession dataSession,Object value) throws BusinessException;
    EntityType readOriginalByNaturalKey(DataSession dataSession,Object value) throws BusinessException;    
    
    EntityType insert(DataSession dataSession,EntityType entity) throws BusinessException;
    EntityType update(DataSession dataSession,EntityType entity) throws BusinessException;
    boolean delete(DataSession dataSession,EntityType entity) throws BusinessException;

    List<EntityType> search(DataSession dataSession,List<Filter> filters,List<Order> orders, SearchResponse searchResponse) throws BusinessException;
    Page<EntityType> pageableSearch(DataSession dataSession,List<Filter> filters,List<Order> orders,PageRequest pageRequest, SearchResponse searchResponse) throws BusinessException;    
    
}
