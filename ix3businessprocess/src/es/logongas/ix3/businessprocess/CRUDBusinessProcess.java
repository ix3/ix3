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
package es.logongas.ix3.businessprocess;

import es.logongas.ix3.core.BusinessException;
import es.logongas.ix3.core.Order;
import es.logongas.ix3.core.Page;
import es.logongas.ix3.core.PageRequest;
import es.logongas.ix3.dao.DataSession;
import es.logongas.ix3.dao.SearchResponse;
import es.logongas.ix3.core.Principal;
import es.logongas.ix3.dao.Filters;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Lorenzo
 * @param <EntityType>
 * @param <PrimaryKeyType>
 */
public interface CRUDBusinessProcess<EntityType, PrimaryKeyType extends Serializable> extends BusinessProcess<EntityType> {

    EntityType create(CreateArguments createArguments) throws BusinessException;

    EntityType read(ReadArguments<PrimaryKeyType> readArguments) throws BusinessException;

    EntityType readByNaturalKey(ReadByNaturalKeyArguments readByNaturalKeyArguments) throws BusinessException;

    EntityType insert(InsertArguments<EntityType> insertArguments) throws BusinessException;

    EntityType update(UpdateArguments<EntityType> updateArguments) throws BusinessException;

    boolean delete(DeleteArguments<EntityType> deleteArguments) throws BusinessException;

    List<EntityType> search(SearchArguments searchArguments) throws BusinessException;

    Page<EntityType> pageableSearch(PageableSearchArguments pageableSearchArguments) throws BusinessException;

    public class CreateArguments extends BusinessProcessArguments {

        final public Map<String, Object> initialProperties;

        public CreateArguments(Principal principal, DataSession dataSession, Map<String, Object> initialProperties) {
            super(principal, dataSession);
            this.initialProperties = initialProperties;
        }
    }

    public class ReadArguments<PrimaryKeyType extends Serializable> extends BusinessProcessArguments {

        final public PrimaryKeyType id;

        public ReadArguments(Principal principal, DataSession dataSession, PrimaryKeyType id) {
            super(principal, dataSession);
            this.dataSession = dataSession;
            this.id = id;
        }

    }

    public class ReadByNaturalKeyArguments extends BusinessProcessArguments {

        final public Object naturalKey;

        public ReadByNaturalKeyArguments(Principal principal, DataSession dataSession, Object naturalKey) {
            super(principal, dataSession);
            this.naturalKey = naturalKey;
        }
    }

    public class InsertArguments<EntityType> extends BusinessProcessArguments {

        final public EntityType entity;

        public InsertArguments(Principal principal, DataSession dataSession, EntityType entity) {
            super(principal, dataSession);
            this.entity = entity;
        }
    }

    public class UpdateArguments<EntityType> extends BusinessProcessArguments {

        final public EntityType entity;
        final public EntityType originalEntity;

        public UpdateArguments(Principal principal, DataSession dataSession, EntityType entity, EntityType originalEntity) {
            super(principal, dataSession);
            this.entity = entity;
            this.originalEntity = originalEntity;
        }

    }

    public class DeleteArguments<EntityType> extends BusinessProcessArguments {

        final public EntityType entity;

        public DeleteArguments(Principal principal, DataSession dataSession, EntityType entity) {
            super(principal, dataSession);
            this.entity = entity;
        }

    }

    
    public class ParametrizedSearchArguments extends BusinessProcessArguments {

        public ParametrizedSearchArguments() {
        }
        
        public ParametrizedSearchArguments(Principal principal, DataSession dataSession) {
            super(principal, dataSession);
        }

    }    
    
    public class SearchArguments extends BusinessProcessArguments {

        public Filters filters;
        public List<Order> orders;
        public SearchResponse searchResponse;

        public SearchArguments() {
        }

        
        
        public SearchArguments(Principal principal, DataSession dataSession, Filters filters, List<Order> orders, SearchResponse searchResponse) {
            super(principal, dataSession);
            this.filters = filters;
            this.orders = orders;
            this.searchResponse = searchResponse;
        }

    }

    public class PageableSearchArguments extends SearchArguments {

        public PageRequest pageRequest;

        public PageableSearchArguments() {
        }
        
        
        public PageableSearchArguments(Principal principal, DataSession dataSession, Filters filters, List<Order> orders, PageRequest pageRequest, SearchResponse searchResponse) {
            super(principal, dataSession, filters, orders, searchResponse);
            this.filters = filters;
            this.orders = orders;
            this.pageRequest = pageRequest;
            this.searchResponse = searchResponse;
        }

    }
    
    
    
}
