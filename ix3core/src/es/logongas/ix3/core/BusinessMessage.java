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

package es.logongas.ix3.core;

public class BusinessMessage {
    private final String propertyName;
    private final String message;
    private final Class<? extends BusinessMessageUID> businessMessageUID;

    public BusinessMessage(String propertyName, String message) {
        this.propertyName = propertyName;
        this.message = message;
        this.businessMessageUID=BusinessMessageUID.class;
    }

    public BusinessMessage(String message) {
        this.propertyName = null;
        this.message = message;
        this.businessMessageUID=BusinessMessageUID.class;        
    }

    
    public BusinessMessage(String propertyName, String message,Class<? extends BusinessMessageUID> businessMessageUID) {
        this.propertyName = propertyName;
        this.message = message;
        this.businessMessageUID=businessMessageUID;
    }

    public BusinessMessage(String message,Class<? extends BusinessMessageUID> businessMessageUID) {
        this.propertyName = null;
        this.message = message;
        this.businessMessageUID=businessMessageUID;        
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
    
    public String getUID() {
        String uid=businessMessageUID.getCanonicalName();
        
        if (uid==null) {
            uid=businessMessageUID.getName();
        }
        
        return uid;
    }    


}
