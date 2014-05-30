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

package es.logongas.ix3.core.annotations.impl;

import es.logongas.ix3.core.annotations.Time;
import java.util.GregorianCalendar;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Valida que el Date esté en la época 0 , es decir el 1 de Enero de 1970
 * Es decir que solo haya columnas de "Tiempo".
 * Además comprueba que los milisegundos sean 0.Para de esa forma comparar mejor las fechas.
 * @author Lorenzo
 */
public class TimeValidator implements ConstraintValidator<Time, java.util.Date> {

    @Override
    public void initialize(Time date) {
    }

    @Override
    public boolean isValid(java.util.Date date, ConstraintValidatorContext cvc) {
        if (date==null) {
            return true;
        }
        
        GregorianCalendar gc=new GregorianCalendar();
        gc.setTime(date);
        
        int year=gc.get(GregorianCalendar.YEAR);
        int month=gc.get(GregorianCalendar.MONTH);
        int day=gc.get(GregorianCalendar.DAY_OF_MONTH);
        int mili=gc.get(GregorianCalendar.MILLISECOND);
        
        if ((year==1970) && (month==0) && (day==1) && (mili==0)) {
            return true;
        } else {
            return false;
        }
        
    }
    
}
