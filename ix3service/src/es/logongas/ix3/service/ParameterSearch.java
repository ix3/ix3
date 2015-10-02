/*
 * Copyright 2015 Lorenzo.
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

import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.METHOD;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * Esta anotación se aplica a los métodos que heredan de la clase "CRUDService" y sirve para decir que un método realmente es de tipo "Search".
 * En ese caso podrá se llamado desde la capa de presentación.
 * El método debe tener alguno de los siguientes parámetros:
 * <ul>
 *  <li>es.logongas.ix3.core.PageRequest</li>
 *  <li>List<es.logongas.ix3.core.Order></li>
 *  <li>es.logongas.ix3.dao.SearchResponse</li>
 * </ul>
 * Además puede tener cualquier otro parámetro que necesite.
 * 
 * Esta anotacion realmente indica que este método es una consulta "predefinida  y especifica" y que cada es un valor que necesita para hacer la consulta a "base de datos" 
 * @author logongas
 */
@Documented
@Target({METHOD})
@Retention(RUNTIME)
public @interface ParameterSearch {
    /**
     * Lista de los nombres de los parametros.
     * Se usa para que pueda llamarse al método por el nombre de los parámetros.
     * Es util para que se pueda llamar directamente desde el método "search"
     * @return 
     */
    String[] parameterNames() default {};    
}
