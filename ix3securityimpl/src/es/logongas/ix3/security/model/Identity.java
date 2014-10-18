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

import es.logongas.ix3.core.annotations.Label;
import es.logongas.ix3.security.authorization.AuthorizationType;
import java.io.Serializable;
import java.util.Set;
import org.hibernate.validator.constraints.NotBlank;

/**
 *
 * @author Lorenzo González
 */
public class Identity implements es.logongas.ix3.security.authentication.Principal {

    protected int idIdentity;
    @NotBlank
    @Label("Identificador")
    protected String login;
    @NotBlank
    @Label("Nombre")
    protected String name;
    protected Set<ACE> acl;
    protected Set<GroupMember> memberOf;

    public Identity() {
    }

    public Identity(int idIdentity, String login, String name) {
        this.idIdentity = idIdentity;
        this.login = login;
        this.name = name;
    }

    public AuthorizationType authorized(String secureResource, Permission permission, Object arguments) {
        return doAuthorized(this, secureResource, permission, arguments);
    }

    protected AuthorizationType doAuthorized(Identity rootIdentity, String secureResource, Permission permission, Object arguments) {

        //Los ACE deben estar ordenador por prioridad
        if (acl != null) {
            for (ACE ace : acl) {
                AuthorizationType authorizationType = ace.authorized(rootIdentity, secureResource, permission, arguments);

                if (authorizationType == AuthorizationType.AccessAllow) {
                    return authorizationType;
                } else if (authorizationType == AuthorizationType.AccessDeny) {
                    return authorizationType;
                }
            }
        }

        //Los Grupos deben estar ordenador por prioridad
        if (memberOf != null) {
            for (GroupMember groupMember : memberOf) {
                AuthorizationType authorizationType = groupMember.getGroup().doAuthorized(rootIdentity, secureResource, permission, arguments);

                if (authorizationType == AuthorizationType.AccessAllow) {
                    return authorizationType;
                } else if (authorizationType == AuthorizationType.AccessDeny) {
                    return authorizationType;
                }
            }
        }

        return AuthorizationType.Abstain;
    }

    @Override
    public Serializable getSid() {
        return idIdentity;
    }

    /**
     * @return the idIdentity
     */
    public int getIdIdentity() {
        return idIdentity;
    }

    /**
     * @param idIdentity the IdIdentity to set
     */
    public void setIdIdentity(int idIdentity) {
        this.idIdentity = idIdentity;
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
    public Set<ACE> getAcl() {
        return acl;
    }

    /**
     * @param acl the acl to set
     */
    public void setAcl(Set<ACE> acl) {
        this.acl = acl;
    }

    /**
     * @return the memberOf
     */
    public Set<GroupMember> getMemberOf() {
        return memberOf;
    }

    /**
     * @param memberOf the memberOf to set
     */
    public void setMemberOf(Set<GroupMember> memberOf) {
        this.memberOf = memberOf;
    }

    @Override
    public String toString() {
        return getName();
    }
}
