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
package es.logongas.ix3.web.util;

import es.logongas.ix3.core.BusinessException;
import es.logongas.ix3.security.authorization.BusinessSecurityException;
import es.logongas.ix3.web.json.JsonFactory;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Tratar las excepciones en la web
 * @author logongas
 */
public class ExceptionManager {

    private ExceptionManager() {
    }

    static public void exceptionToHttpResponse(Throwable throwable, HttpServletResponse httpServletResponse,JsonFactory jsonFactory) {
        try {
            Throwable realException = getRealException(throwable);

            if (realException instanceof BusinessSecurityException) {
                BusinessSecurityException businessSecurityException = (BusinessSecurityException) realException;
                Log log=LogFactory.getLog(ExceptionManager.class);
                log.info(businessSecurityException);
                
                httpServletResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
                httpServletResponse.setContentType("text/plain; charset=UTF-8");
                if (businessSecurityException.getBusinessMessages().size() > 0) {
                    httpServletResponse.getWriter().println(businessSecurityException.getBusinessMessages().get(0).getMessage());
                }
            } else if (realException instanceof BusinessException) {
                BusinessException businessException = (BusinessException) realException;
                
                httpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                httpServletResponse.setContentType("application/json; charset=UTF-8");
                httpServletResponse.getWriter().println(jsonFactory.getJsonWriter().toJson(businessException.getBusinessMessages()));
            } else {
                Log log=LogFactory.getLog(ExceptionManager.class);
                log.error("Falló la llamada al servidor:", realException);
                
                httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                httpServletResponse.setContentType("text/plain");
                realException.printStackTrace(httpServletResponse.getWriter());
            }
        } catch (IOException ioException) {
            Log log=LogFactory.getLog(ExceptionManager.class);
            log.error("Falló al devolver la excepción por la HttpResponse:", ioException);
            log.error("Excepcion original:", throwable);
        }

    }

    /**
     * Si la excepcion contiene como causa (recursivamente) una BusinessException, retorna la BusinessException sino retorna la propia excepción original
     *
     * @param ex
     * @return
     */
    static private Throwable getRealException(Throwable ex) {

        if (ex instanceof BusinessException) {
            return ex;
        } else if (ex.getCause() == null) {
            return ex;
        } else if (ex == ex.getCause()) {
            return ex;
        } else {
            Throwable causeException = getRealException(ex.getCause());

            if (causeException instanceof BusinessException) {
                return causeException;
            } else {
                return ex;
            }
        }

    }

    
    
}
