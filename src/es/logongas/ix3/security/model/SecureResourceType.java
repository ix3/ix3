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
package es.logongas.ix3.security.model;

import java.util.HashSet;
import java.util.Set;

/**
 * Tipo de recurso securizado. Sus valores suelen ser "TABLE","PRINTER","CLASS","URL" , etc.
 * @author Lorenzo González
 */
public class SecureResourceType {
    int idSecureResourceType;
    String name;
    Set<Permission> permissions=new HashSet<Permission>();
    Set<SecureResource> secureResources=new HashSet<SecureResource>();
}
