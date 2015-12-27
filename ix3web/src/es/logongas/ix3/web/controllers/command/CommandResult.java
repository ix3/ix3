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
package es.logongas.ix3.web.controllers.command;

import es.logongas.ix3.web.json.beanmapper.BeanMapper;

/**
 *
 * @author logongas
 */
public class CommandResult {
    final private Class resultClass;
    final private Object result;
    final private int httpSuccessStatus;
    final private boolean cache;
    final private BeanMapper beanMapper;
    final private MimeType mimeType;

    public CommandResult() {
        this.resultClass = null;
        this.result = null;
        this.httpSuccessStatus = 204; //No-content
        this.cache = false;
        this.beanMapper = null;
        this.mimeType = MimeType.JSON;
    }

    
    
    
    public CommandResult(Object result) {
        Class resultClass;
        if (result!=null) {
            resultClass = result.getClass();
        } else {
            resultClass=null;
        }
        
        int httpSuccessStatus;
        if (result!=null) {
            httpSuccessStatus=200;
        } else {
            httpSuccessStatus=204;
        }        
        
        this.result = result;
        this.resultClass = resultClass;
        this.httpSuccessStatus=httpSuccessStatus;
        this.cache=false; 
        this.beanMapper=null;
        this.mimeType = MimeType.JSON;
    }
    

    public CommandResult(Class resultClass, Object result) {
        if ((result!=null) && (resultClass==null)) {
            resultClass = result.getClass();
        }   
        
        int httpSuccessStatus;
        if (result!=null) {
            httpSuccessStatus=200;
        } else {
            httpSuccessStatus=204;
        }        
        
        this.resultClass = resultClass;
        this.result = result;
        this.httpSuccessStatus=httpSuccessStatus;
        this.cache=false;
        this.beanMapper=null;
        this.mimeType = MimeType.JSON;
        
    }
    
    public CommandResult(Class resultClass, Object result, int httpSuccessStatus) {
        if (httpSuccessStatus<=0) {
            throw new RuntimeException("El argumento httpSuccessStatus no puede ser 0 o negativo");
        }          
        
        if ((result!=null) && (resultClass==null)) {
            resultClass = result.getClass();
        }        
        this.resultClass = resultClass;
        this.result = result;
        this.httpSuccessStatus=httpSuccessStatus;
        this.cache=false;
        this.beanMapper=null;
        this.mimeType = MimeType.JSON;
    } 
    
    public CommandResult(Class resultClass, Object result, boolean cache) {
        if ((result!=null) && (resultClass==null)) {
            resultClass = result.getClass();
        }        
        this.resultClass = resultClass;
        this.result = result;
        this.httpSuccessStatus=200;
        this.cache = cache;
        this.beanMapper=null;
        this.mimeType = MimeType.JSON;
    }
    
    public CommandResult(Class resultClass, Object result, int httpSuccessStatus, boolean cache,BeanMapper beanMapper, MimeType mimeType) {
        
        if (httpSuccessStatus<=0) {
            throw new RuntimeException("El argumento httpSuccessStatus no puede ser 0 o negativo");
        } 
        if (mimeType==null) {
            throw new RuntimeException("El argumento mimeType no puede ser null");
        }
        
        if ((result!=null) && (resultClass==null)) {
            resultClass = result.getClass();
        }       
        
        this.resultClass = resultClass;
        this.result = result;
        this.httpSuccessStatus=httpSuccessStatus;  
        this.cache = cache;
        this.beanMapper=beanMapper;
        this.mimeType = mimeType;
    }   
    
    /**
     * @return the resultClass
     */
    public Class getResultClass() {
        return resultClass;
    }


    /**
     * @return the result
     */
    public Object getResult() {
        return result;
    }


    /**
     * @return the cache
     */
    public boolean isCache() {
        return cache;
    }


    /**
     * @return the httpSuccessStatus
     */
    public int getHttpSuccessStatus() {
        return httpSuccessStatus;
    }


    /**
     * @return the beanMapper
     */
    public BeanMapper getBeanMapper() {
        return beanMapper;
    }


    /**
     * @return the mimeType
     */
    public MimeType getMimeType() {
        return mimeType;
    }

    
}
