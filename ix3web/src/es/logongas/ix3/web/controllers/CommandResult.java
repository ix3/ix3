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

/**
 *
 * @author logongas
 */
public class CommandResult {
    private Class resultClass;
    private Object result;
    private int httpSuccessStatus;
    private boolean cache;
    
    

    public CommandResult(Class resultClass, Object result) {
        this.resultClass = resultClass;
        this.result = result;
        this.httpSuccessStatus=200;
        this.cache=false;
    }
    
    public CommandResult(Class resultClass, Object result, int httpSuccessStatus) {
        this.resultClass = resultClass;
        this.result = result;
        this.httpSuccessStatus=httpSuccessStatus;
        this.cache=false;
    } 
    
    public CommandResult(Class resultClass, Object result, boolean cache) {
        this.resultClass = resultClass;
        this.result = result;
        this.httpSuccessStatus=200;
        this.cache = cache;
    }
    
    public CommandResult(Class resultClass, Object result, int httpSuccessStatus, boolean cache) {
        this.resultClass = resultClass;
        this.result = result;
        this.httpSuccessStatus=httpSuccessStatus;  
        this.cache = cache;
    }   
    
    /**
     * @return the resultClass
     */
    public Class getResultClass() {
        return resultClass;
    }

    /**
     * @param resultClass the resultClass to set
     */
    public void setResultClass(Class resultClass) {
        this.resultClass = resultClass;
    }

    /**
     * @return the result
     */
    public Object getResult() {
        return result;
    }

    /**
     * @param result the result to set
     */
    public void setResult(Object result) {
        this.result = result;
    }

    /**
     * @return the cache
     */
    public boolean isCache() {
        return cache;
    }

    /**
     * @param cache the cache to set
     */
    public void setCache(boolean cache) {
        this.cache = cache;
    }

    /**
     * @return the httpSuccessStatus
     */
    public int getHttpSuccessStatus() {
        return httpSuccessStatus;
    }

    /**
     * @param httpSuccessStatus the httpSuccessStatus to set
     */
    public void setHttpSuccessStatus(int httpSuccessStatus) {
        this.httpSuccessStatus = httpSuccessStatus;
    }
    
    

    
    
    
}
