/*
 * Copyright 2013 Lorenzo.
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
package es.logongas.ix3.persistence.services.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Indica la lista de posibles valores de una columna. Se gasta para hacer un
 * combo en el interfaz de usuario.
 *
 * @author Lorenzo Gonzalez
 */
@Documented
@Target({METHOD, FIELD})
@Retention(RUNTIME)
public @interface ValuesList {

    /**
     * Indica si la lista contiene "pocos" elementos.
     * La norma a seguir si debe valer <code>true</code> o <code>false</code> se basa en que los elementos pueden ser enviados al cliente.
     * Si la cantidad de datos es excesiva nunca debería ser <code>true</code>.
     * @return Si vale <code>true</code> la lista contiene pocos elementos.
     */
    boolean shortLength() default false;
    
    /**
     * Nombre de la entidad de cuyo DAO obtener obtener los datos.Por defecto es
     * del tipo de la propiedad o del tipo que retorna el método
     *
     * @return
     */
    Class entity() default DEFAULT.class;

    /**
     * Lista de propiedades de las que depende el valor de esta
     *
     * @return
     */
    String[] dependProperties() default {};

    /**
     * En caso de querer consultas que no sea simplemente la lista de todas las
     * entidades, aqui pondremos el nombre de un método DAO de la entidad
     * correspondiente
     *
     * @return
     */
    String namedSearch() default "";
    
    static final class DEFAULT {} ;
}
