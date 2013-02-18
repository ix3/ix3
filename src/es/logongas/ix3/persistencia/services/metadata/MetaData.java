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
package es.logongas.ix3.persistencia.services.metadata;

import java.util.List;
import java.util.Map;

/**
 * Metadatos sobre la entidad
 * @author Lorenzo Gonzalez
 */
public interface MetaData {
    /**
     * La clase Java correspondiente a esta entidad
     * @return La clase Java
     */
    Class getType();
    /**
     * La Metadatas de todas las propiedades de esta entidad
     * @return Map con el nombre de cada propiedad y sis metadatos
     */
    Map<String,MetaData> getPropertiesMetaData();
    /**
     * El nombre de la clave primaria de la entidad
     * @return Nombre de la clave primaria de la entidad o null si no tiene clave primaria esta entidad
     */
    String getPrimaryKeyPropertyName();
    /**
     * Lista de claves naturales de una entidad
     * @return Claves naturales de una entidad. Si no tiene ninguna se retornará una lista vacia
     */
    List<String> getNaturalKeyPropertiesName();
    /**
     * Si esta propiedad es una colección
     * @return <code>true</code> si es una colección
     */
    boolean isCollection();
}
