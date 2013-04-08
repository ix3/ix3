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
package es.logongas.ix3.persistence.services.dao;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Interfaz generico para todos los DAO
 * @author Lorenzo González
 * @param EntityType Tipo de la propia entidad
 * @param PrimaryKeyType Tipo de la clave primaria de la entidad
 */
public interface GenericDAO<EntityType,PrimaryKeyType extends Serializable> {
    EntityType create() throws BusinessException;
    void insert(EntityType entity) throws BusinessException;
    EntityType read(PrimaryKeyType primaryKey) throws BusinessException;
    boolean update(EntityType entity) throws BusinessException;
    boolean delete(PrimaryKeyType primaryKey) throws BusinessException;
    EntityType readByNaturalKey(Object value) throws BusinessException;
    List<EntityType> search(Map<String,Object> filter) throws BusinessException;
}
