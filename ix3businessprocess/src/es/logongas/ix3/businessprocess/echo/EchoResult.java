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
package es.logongas.ix3.businessprocess.echo;

import java.util.Date;

/**
 *
 * @author logongas
 */
public class EchoResult {
        private final long id;
        private final Date date;

        public EchoResult(long id, Date date) {
            this.id = id;
            if (date==null) {
                this.date = new Date();
            } else {
                this.date = date;
            }
        }

        /**
         * @return the id
         */
        public long getId() {
            return id;
        }


        /**
         * @return the date
         */
        public Date getDate() {
            return date;
        }


        @Override
        public String toString() {
            return id + "-" + date.getTime();
        }

}
