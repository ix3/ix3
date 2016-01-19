/*
 * Copyright 2015 Lorenzo Gonzalez.
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
package es.logongas.ix3.businessprocess.security;

/**
 * Es lo que se pasa como argumento en <code>AuthorizationManager.authorized</code> en el "post-execute" de una función de BusinessProcess
 * De esa forma tenemos tanto los argumentos de entrada como el resultado
 * @author logongas
 */
public class PostArguments {
    public Object inputArguments;
    public Object result;

    public PostArguments(Object inputArguments, Object result) {
        this.inputArguments = inputArguments;
        this.result = result;
    }

    
    /**
     * @return the parameters
     */
    public Object getInputArguments() {
        return inputArguments;
    }


    /**
     * @return the result
     */
    public Object getResult() {
        return result;
    }

    
}
