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

package es.logongas.ix3.persistence.services.dao;

import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.METHOD;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 *
 * @author Lorenzo
 */
@Documented
@Target({METHOD})
@Retention(RUNTIME)
public @interface NamedSearch {
    /**
     * Lista de los nombres de los parametros.
     * Se usa para que pueda llamarse al método por el nombre de los parámetros.
     * Es util para que se pueda llamar directamente desde el método "search"
     * @return 
     */
    String[] parameterNames() default {};
    
}
