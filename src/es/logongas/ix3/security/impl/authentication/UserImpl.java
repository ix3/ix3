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
package es.logongas.ix3.security.impl.authentication;

import es.logongas.ix3.security.services.authentication.*;
import es.logongas.ix3.security.services.authorization.Permission;
import java.util.HashSet;
import java.util.Set;

/**
 * Datos de un usuario
 * @author Lorenzo González
 */
public class UserImpl implements User {
    private int idUser;
    private String login;
    private String nombre;
    private String ape1;
    private String ape2;
    private Set<Permission> permissions=new HashSet<Permission>();
    private Set<Role> roles=new HashSet<Role>();

    public UserImpl(int idUser, String login, String nombre, String ape1, String ape2) {
        this.idUser = idUser;
        this.login = login;
        this.nombre = nombre;
        this.ape1 = ape1;
        this.ape2 = ape2;
    }



    public boolean getContainPermission(Permission permission) {
        if (permissions.contains(permission)) {
            return true;
        } else {
            for(Role role:roles) {
                boolean containPermission=role.getContainPermission(permission);

                if (containPermission==true) {
                    return true;
                }
            }
        }

        return false;
    }





    /**
     * @return the login
     */
    public String getLogin() {
        return login;
    }

    /**
     * @param login the login to set
     */
    public void setLogin(String login) {
        this.login = login;
    }

    /**
     * @return the nombre
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * @param nombre the nombre to set
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /**
     * @return the ape1
     */
    public String getApe1() {
        return ape1;
    }

    /**
     * @param ape1 the ape1 to set
     */
    public void setApe1(String ape1) {
        this.ape1 = ape1;
    }

    /**
     * @return the ape2
     */
    public String getApe2() {
        return ape2;
    }

    /**
     * @param ape2 the ape2 to set
     */
    public void setApe2(String ape2) {
        this.ape2 = ape2;
    }

    /**
     * @return the permissions
     */
    public Set<Permission> getPermissions() {
        return permissions;
    }


    /**
     * @return the roles
     */
    public Set<Role> getRoles() {
        return roles;
    }


    /**
     * @return the idUser
     */
    @Override
    public int getIdUser() {
        return idUser;
    }

    /**
     * @param idUser the idUser to set
     */
    public void setIdUser(int idUser) {
        this.idUser = idUser;
    }




}
