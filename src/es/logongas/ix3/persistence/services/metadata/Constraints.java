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

package es.logongas.ix3.persistence.services.metadata;

/**
 * Restricciones de una propiedad de una clase.
 * @author Lorenzo Gonzalez
 */
public interface Constraints {
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

    ValuesList getValuesList();

}
