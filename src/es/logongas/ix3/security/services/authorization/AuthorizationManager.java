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

import es.logongas.ix3.model.Permission;
import es.logongas.ix3.model.User;

/**
 *
 * @author Lorenzo González
 */
public interface AuthorizationManager {
    boolean authorized(User user,String secureResource,Permission permission,Object arguments);
    boolean authorized(User user,String secureResource,String resourceTypeName,String permissionName,Object arguments);
}
