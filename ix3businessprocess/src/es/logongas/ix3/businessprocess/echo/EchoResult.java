/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
