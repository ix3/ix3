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
package es.logongas.ix3.model;

import es.logongas.ix3.security.services.authorization.AuthorizationType;

/**
 *
 * @author Lorenzo González
 */
public class ACE  {
    private int idACE;
    private ACEType aceType;
    private Permission permission;
    private Principal principal;
    private SecureResourceType secureResourceType;
    private String secureResourceRegExp;
    private String conditionalScript;
    private int priority;

    @Override
    public String toString() {
        String cs="";
        if (conditionalScript!=null) {
            cs=" WHERE (" + conditionalScript + ")";
        }
        return aceType + " - " + permission + " => " + secureResourceRegExp  + cs;
    }

    public AuthorizationType authorized(SecureResourceType secureResourceType,String secureResource,Permission permission,Object arguments) {
        AuthorizationType authorizationType;

        if ((this.secureResourceType==secureResourceType) && (this.permission==permission)) {
            if (secureResource.matches(secureResourceRegExp)) {
                if (conditionalScript!=null) {
                    if (evaluateConditionalScript()==true) {
                        authorizationType=aceTypeToAuthorizationType(aceType);
                    } else {
                        authorizationType=AuthorizationType.Abstain;
                    }
                } else {
                    authorizationType=aceTypeToAuthorizationType(aceType);
                }
            } else {
               authorizationType=AuthorizationType.Abstain;
            }
        } else {
            authorizationType=AuthorizationType.Abstain;
        }

        return authorizationType;
    }

    private AuthorizationType aceTypeToAuthorizationType(ACEType aceType) {
        switch (aceType) {
            case Allow:
                return AuthorizationType.AccessAllow;
            case Deny:
                return AuthorizationType.AccessDeny;
            default:
                throw new RuntimeException("El tipo de ACE es desconocido:"+aceType);
        }
    }

    private boolean evaluateConditionalScript() {
        return true;
    }

    /**
     * @return the idACE
     */
    public int getIdACE() {
        return idACE;
    }

    /**
     * @param idACE the idACE to set
     */
    public void setIdACE(int idACE) {
        this.idACE = idACE;
    }

    /**
     * @return the aceType
     */
    public ACEType getAceType() {
        return aceType;
    }

    /**
     * @param aceType the aceType to set
     */
    public void setAceType(ACEType aceType) {
        this.aceType = aceType;
    }

    /**
     * @return the permission
     */
    public Permission getPermission() {
        return permission;
    }

    /**
     * @param permission the permission to set
     */
    public void setPermission(Permission permission) {
        this.permission = permission;
    }

    /**
     * @return the principal
     */
    public Principal getPrincipal() {
        return principal;
    }

    /**
     * @param principal the principal to set
     */
    public void setPrincipal(Principal principal) {
        this.principal = principal;
    }

    /**
     * @return the secureResourceRegExp
     */
    public String getSecureResourceRegExp() {
        return secureResourceRegExp;
    }

    /**
     * @param secureResourceRegExp the secureResourceRegExp to set
     */
    public void setSecureResourceRegExp(String secureResourceRegExp) {
        this.secureResourceRegExp = secureResourceRegExp;
    }

    /**
     * @return the conditionalScript
     */
    public String getConditionalScript() {
        return conditionalScript;
    }

    /**
     * @param conditionalScript the conditionalScript to set
     */
    public void setConditionalScript(String conditionalScript) {
        this.conditionalScript = conditionalScript;
    }

    /**
     * @return the priority
     */
    public int getPriority() {
        return priority;
    }

    /**
     * @param priority the priority to set
     */
    public void setPriority(int priority) {
        this.priority = priority;
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
