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
package es.logongas.ix3.dao.metadata;


/**
 *
 * @author Lorenzo González
 */
public interface MetaDataFactory {
    /**
     * Obtiene los metadatos de una clase de negocio
     * @param entityClass El tipo de la clase de negocio.
     * @return Retorna los metadatos o null si el tipo de la clase que se ha pasado no es una clase de negocio
     */
    MetaData getMetaData(Class entityClass);
    /**
     * Obtiene los metadatos de una clase de negocio
     * @param entityName El nombre de la clase de negocio. No se debe incluir el paquete Java
     * @return Retorna los metadatos o null si la clase que se ha pasado no es una clase de negocio
     */
    MetaData getMetaData(String entityName);
    /**
     * Obtiene los metadatos de una clase de negocio
     * @param obj La clase de negocio.
     * @return Retorna los metadatos o null si la clase que se ha pasado no es una clase de negocio
     */
    MetaData getMetaData(Object obj);    
}
