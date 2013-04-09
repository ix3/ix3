/*
 * Copyright 2013 Lorenzo González.
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
package es.logongas.ix3.security.services.authorization;

/**
 * Si se concede o no acceso a un recurso
 * @author Lorenzo González
 */
public enum AuthorizationType {
    /*
     * Se permite el acceso al recurso
     */
    AccessAllow,
    /*
     * Se deniega el acceso al recurso
     */
    AccessDeny,
    /*
     * No se dice nada sobre si se puede o no acceder al recurso
     * Aunque el proveeder conoce el tipo de recurso
     */
    Abstain
}
