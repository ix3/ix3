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

import es.logongas.ix3.core.annotations.ValuesList;

/**
 * Un recurso que puede ser "securizado"
 * @author Lorenzo González
 */
public class SecureResource {
    private int idSecureResource;
    private String name;
    private String description;
    @ValuesList(shortLength = true)
    private SecureResourceType secureResourceType;

    public SecureResource() {
    }

    public SecureResource(int idSecureResource, String name, String description, SecureResourceType secureResourceType) {
        this.idSecureResource = idSecureResource;
        this.name = name;
        this.description = description;
        this.secureResourceType = secureResourceType;
    }



    /**
     * @return the idSecureResource
     */
    public int getIdSecureResource() {
        return idSecureResource;
    }

    /**
     * @param idSecureResource the idSecureResource to set
     */
    public void setIdSecureResource(int idSecureResource) {
        this.idSecureResource = idSecureResource;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the secureResourceType
     */
    public SecureResourceType getSecureResourceType() {
        return secureResourceType;
    }

    /**
     * @param secureResourceType the secureResourceType to set
     */
    public void setSecureResourceType(SecureResourceType secureResourceType) {
        this.secureResourceType = secureResourceType;
    }
}
