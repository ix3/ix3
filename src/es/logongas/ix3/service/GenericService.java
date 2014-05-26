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

import es.logongas.ix3.persistence.services.dao.BusinessException;
import es.logongas.ix3.persistence.services.dao.Order;
import es.logongas.ix3.persistence.services.dao.Page;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author Lorenzo
 * @param <EntityType>
 * @param <PrimaryKeyType> 
 */
public interface GenericService<EntityType,PrimaryKeyType extends Serializable> {
    EntityType create() throws BusinessException;
    EntityType create(Map<String,Object> initialProperties) throws BusinessException;
    void insert(EntityType entity) throws BusinessException;
    EntityType read(PrimaryKeyType primaryKey) throws BusinessException;
    boolean update(EntityType entity) throws BusinessException;
    boolean delete(PrimaryKeyType primaryKey) throws BusinessException;
    EntityType readByNaturalKey(Object value) throws BusinessException;
    List<EntityType> search(Map<String,Object> filter) throws BusinessException;
    List<EntityType> search(Map<String,Object> filter,List<Order> orders) throws BusinessException;
    Object namedSearch(String namedSearch, Map<String, Object> filter) throws BusinessException;
    Page<EntityType> pageableSearch(Map<String,Object> filter,int pageNumber,int pageSize) throws BusinessException;
    Page<EntityType> pageableSearch(Map<String,Object> filter,List<Order> orders,int pageNumber,int pageSize) throws BusinessException; 
}
