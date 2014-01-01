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
package es.logongas.ix3.persistence.services.metadata;

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
     * Si la carga de la colección es perezosa
     *
     * @return Retorna <code>true</code> si la carga de la colección es perezosa
     */
    boolean isCollectionLazy();

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
    String getCaption();

    /**
     * Si esta propiedad es requerida
     * Sera null si hace rererencia a una clase en vez de a una propiedad de una clase.
     * @return Retorna <code>true</code> si la propiedad es requerida.
     */
    boolean isRequired();

    /**
     * Para valores numéricos, el valor mínimo que tendrá.Por defecto vale Long.MIN_VALUE si no se indica ningún valor.
     * @return Retorna el valro mínimo que puede tener un valor. 
     */
    long getMinimum();
    
    /**
     * Para valores numéricos, el valor máximo que tendrá.Por defecto vale Long.MAX_VALUE si no se indica ningún valor.
     * @return Retorna el valor mínimo que puede tener un valor. 
     */
    long getMaximum();

    /**
     * Para valores de tipo String, indica la longitud mínima del String.Por defecto vale 0 si no se indica ningún valor.
     * @return La longitud mínima del String
     */
    int getMinLength();

    /**
     * Para valores de tipo String, indica la longitud máxima del String.Por defecto vale Integer.MAX_VALUE si no se indica ningún valor.
     * @return La longitud máxima del String
     */
    int getMaxLength();
    
    /**
     * Para valores de tipo String, indica una expresión regular que debe cumplir el campo.Por defecto vale null si no se indica ningún valor.
     * @return Una expresion regular
     */
    String getPattern();
    
    /**
     * Para valores de tipo String, indica un formato especifico que debe cumplir el campo.Por defecto vale null si no se indica ningún valor.
     * @return El formato especifico
     */
    Format getFormat();
    
}
