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
package es.logongas.ix3.util;

import es.logongas.ix3.core.BusinessException;

/**
 *
 * @author logongas
 */
public class ExceptionUtil {

    /**
     * Si la excepcion contiene como causa (recursivamente) una BusinessException, retorna la BusinessException sino retorna null
     *
     * @param ex
     * @return
     */
    static public BusinessException getBusinessExceptionFromThrowable(Throwable ex) {

        if (ex instanceof BusinessException) {
            return (BusinessException) ex;
        } else if (ex.getCause() == null) {
            return null;
        } else if (ex == ex.getCause()) {
            return null;
        } else {
            Throwable causeException = getBusinessExceptionFromThrowable(ex.getCause());

            if (causeException instanceof BusinessException) {
                return (BusinessException) causeException;
            } else {
                return null;
            }
        }

    }
    
    /**
     * Obtiene la excepción original
     *
     * @param ex
     * @return
     */
    static public Throwable getOriginalExceptionFromThrowable(Throwable ex) {

        if (ex==null) {
            return null;
        }
        
        if (ex.getCause() == null) {
            return ex;
        } else if (ex == ex.getCause()) {
            return ex;
        } else {
            return getOriginalExceptionFromThrowable(ex.getCause());
        }

    }
    
}
