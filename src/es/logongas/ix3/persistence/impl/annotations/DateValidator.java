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

package es.logongas.ix3.persistence.impl.annotations;

import es.logongas.ix3.persistence.services.annotations.Date;
import java.util.GregorianCalendar;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Comprueba que los campos referidos a hora, minutos, segundos y milisegundos, sean 0.
 * De esa forma solo hay información de la fecha.
 * @author Lorenzo
 */
public class DateValidator implements ConstraintValidator<Date, java.util.Date> {

    @Override
    public void initialize(Date date) {
    }

    @Override
    public boolean isValid(java.util.Date date, ConstraintValidatorContext cvc) {
        
        if (date==null) {
            return true;
        }
        
        GregorianCalendar gc=new GregorianCalendar();
        gc.setTime(date);
        
        int hour=gc.get(GregorianCalendar.HOUR_OF_DAY);
        int minute=gc.get(GregorianCalendar.MINUTE);
        int second=gc.get(GregorianCalendar.SECOND);
        int milisecond=gc.get(GregorianCalendar.MILLISECOND);
        
        if ((hour==0) && (minute==0) && (second==0) && (milisecond==0)) {
            return true;
        } else {
            return false;
        }
    }
    
}
