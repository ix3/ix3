/*
 * Copyright 2015 logongas.
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
package es.logongas.ix3.web.controllers.helper;

import es.logongas.ix3.web.json.JsonFactory;
import es.logongas.ix3.web.util.ExceptionManager;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Clase que centraliza las excepciones de Spring
 * @author logongas
 */
@ControllerAdvice
public class ExceptionControllerAdvice {

    @Autowired
    protected JsonFactory jsonFactory;

    @ExceptionHandler(Exception.class)
    public void exception(Exception ex, HttpServletResponse httpServletResponse) {
        ExceptionManager.exceptionToHttpResponse(ex, httpServletResponse, jsonFactory);
    }


}
