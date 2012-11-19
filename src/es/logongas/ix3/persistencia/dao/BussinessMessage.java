/*
 * Copyright 2012 Lorenzo González.
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

package es.logongas.ix3.persistencia.dao;

public class BussinessMessage {
    private final String propertyName;
    private final String message;

    public BussinessMessage(String propertyName, String message) {
        this.propertyName = propertyName;
        this.message = message;
    }



    @Override
    public String toString() {
        if (propertyName!=null) {
            return "'"+propertyName+ "'-"+message;
        } else {
            return message;
        }
    }
    

    public String getPropertyName() {
        return propertyName;
    }


    public String getMessage() {
        return message;
    }


}
