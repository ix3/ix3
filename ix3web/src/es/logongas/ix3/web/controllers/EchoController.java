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
package es.logongas.ix3.web.controllers;

import es.logongas.ix3.core.BusinessException;
import es.logongas.ix3.dao.NativeDAO;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 *
 * @author logongas
 */
@Controller
public class EchoController extends AbstractRESTController {

    private static final Log log = LogFactory.getLog(EchoController.class);

    @Autowired
    private NativeDAO nativeDAO;

    @RequestMapping(value = {"/$echo/{id}"}, method = RequestMethod.GET, produces = "application/json")
    public void echo(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, final @PathVariable("id") int id) {
        restMethod(httpServletRequest, httpServletResponse, null, new Command() {

            @Override
            public CommandResult run(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Map<String, Object> arguments) throws Exception, BusinessException {

                List<Object> resultado = nativeDAO.createNativeQuery("select now() from dual", (List<Object>) null);
                Date date = (Date) resultado.get(0);

                EchoResult echoResult = new EchoResult(id, date);

                return new CommandResult(EchoResult.class, echoResult);

            }
        });
    }

    public class EchoResult {

        private int id;
        private Date date;

        public EchoResult(int id, Date date) {
            this.id = id;
            this.date = date;
        }

        /**
         * @return the id
         */
        public int getId() {
            return id;
        }

        /**
         * @param id the id to set
         */
        public void setId(int id) {
            this.id = id;
        }

        /**
         * @return the date
         */
        public Date getDate() {
            return date;
        }

        /**
         * @param date the date to set
         */
        public void setDate(Date date) {
            this.date = date;
        }

        @Override
        public String toString() {
            return id + "-" + date.getTime();
        }

    }

}
