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

import es.logongas.ix3.security.services.authorization.AuthorizationType;
import java.util.List;

/**
 *
 * @author Lorenzo González
 */
public class Principal implements java.security.Principal {
    private int sid;
    private String name;
    private ACL acl;
    private List<Group> groups;

    public AuthorizationType authorized(SecureResource secureResource,Permission permission,Object arguments) {
        AuthorizationType authorizationType=getAcl().authorized(secureResource, permission, arguments);

        return authorizationType;
    }

    /**
     * @return the sid
     */
    public int getSid() {
        return sid;
    }

    /**
     * @param sid the sid to set
     */
    public void setSid(int sid) {
        this.sid = sid;
    }

    /**
     * @return the name
     */
    @Override
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
     * @return the acl
     */
    public ACL getAcl() {
        return acl;
    }

    /**
     * @param acl the acl to set
     */
    public void setAcl(ACL acl) {
        this.acl = acl;
    }

    /**
     * @return the groups
     */
    public List<Group> getGroups() {
        return groups;
    }

    /**
     * @param groups the groups to set
     */
    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }


}
