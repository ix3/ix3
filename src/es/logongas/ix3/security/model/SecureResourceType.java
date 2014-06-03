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
import org.hibernate.Hibernate;

/**
 * Tipo de recurso securizado. Sus valores suelen ser "TABLE","PRINTER","CLASS","URL" , etc.
 * @author Lorenzo González
 */
public class SecureResourceType {
    private int idSecureResourceType;
    private String name;
    private String description;
    private Set<Permission> permissions=new HashSet<Permission>();
    private Set<SecureResource> secureResources=new HashSet<SecureResource>();

    public SecureResourceType() {
    }

    public SecureResourceType(int idSecureResourceType, String name, String description) {
        this.idSecureResourceType = idSecureResourceType;
        this.name = name;
        this.description = description;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (Hibernate.getClass(this) != Hibernate.getClass(obj)) {
            return false;
        }

        Permission permission = (Permission) obj;
        Object obj1_prop1 = getName();
        Object obj2_prop1 = permission.getName();


        if ((obj1_prop1 == null) || (obj2_prop1 == null) ) {
            if ((obj1_prop1 == null) && (obj2_prop1 == null)) {
                return true;
            } else {
                return false;
            }
        } else if (obj1_prop1.equals(obj2_prop1) == true) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        Object obj1_prop1 = name;
        int resultado;

        resultado = (obj1_prop1 == null ? 0 : obj1_prop1.hashCode());

        return resultado;
    }

    @Override
    public String toString() {
        return description;
    }


    /**
     * @return the idSecureResourceType
     */
    public int getIdSecureResourceType() {
        return idSecureResourceType;
    }

    /**
     * @param idSecureResourceType the idSecureResourceType to set
     */
    public void setIdSecureResourceType(int idSecureResourceType) {
        this.idSecureResourceType = idSecureResourceType;
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
     * @return the permissions
     */
    public Set<Permission> getPermissions() {
        return permissions;
    }

    /**
     * @param permissions the permissions to set
     */
    public void setPermissions(Set<Permission> permissions) {
        this.permissions = permissions;
    }

    /**
     * @return the secureResources
     */
    public Set<SecureResource> getSecureResources() {
        return secureResources;
    }

    /**
     * @param secureResources the secureResources to set
     */
    public void setSecureResources(Set<SecureResource> secureResources) {
        this.secureResources = secureResources;
    }
}
