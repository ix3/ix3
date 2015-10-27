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

import java.util.List;
import java.util.Map;

/**
 * Metadatos sobre la entidad
 *
 * @author Lorenzo Gonzalez
 */
public interface MetaData {

    /**
     * La clase Java correspondiente a esta entidad
     *
     * @return La clase Java
     */
    Class getType();

    /**
     * El MetaTypo de esta entidad.
     *
     * @return El MetaTypo de esta entidad.
     */
    MetaType getMetaType();

    /**
     * La Metadatas de todas las propiedades de esta entidad
     *
     * @return Map con el nombre de cada propiedad y sus metadatos
     */
    Map<String, MetaData> getPropertiesMetaData();
    
    /**
     * La Metadatas de todas las propiedades de esta entidad
     *
     * @param propertyName El nombre de la propiedad de la que se obtiene los metadatos
     * @return Map con el nombre de cada propiedad y sus metadatos
     */
    MetaData getPropertyMetaData(String propertyName);    

    /**
     * El nombre de la clave primaria de la entidad
     *
     * @return Nombre de la clave primaria de la entidad o null si no tiene
     * clave primaria esta entidad
     */
    String getPrimaryKeyPropertyName();

    /**
     * Lista de claves naturales de una entidad
     *
     * @return Claves naturales de una entidad. Si no tiene ninguna se retornará
     * una lista vacia
     */
    List<String> getNaturalKeyPropertiesName();

    /**
     * Si esta propiedad es una colección
     *
     * @return <code>true</code> si es una colección
     */
    boolean isCollection();

    /**
     * El tipo de colección de la entidad
     *
     * @return El tipo de colección
     */
    CollectionType getCollectionType();

    /**
     * El nombre de la propiedad.
     * Sera null si hace rererencia a una clase en vez de a una propiedad de una clase.
     * @return El nombre de la propiedad.
     */
    String getPropertyName();
    
    /**
     * El label de la propiedad.Es decir un nombre para ser mostrado al usuario.
     * Sera null si hace rererencia a una clase en vez de a una propiedad de una clase.
     * @return El label de la propiedad.
     */
    String getLabel();

    /**
     * Obtiene las restricciones de la propiedad
     * @return 
     */
    Constraints getConstraints();
    
    /**
     * El path de esta propopiedad desde el inicio de la entidad
     * @return El path de esta propopiedad desde el inicio de la entidad
     */
    String getPropertyPath();
}
