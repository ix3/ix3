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

/**
 *
 * @author Lorenzo González
 */
public class ACE implements Comparable<ACE> {
    private int idACE;
    private ACEType aceType;
    private Permission permission;
    private Principal principal;
    private String secureResourceRegExp;
    private String conditionalScript;
    private int priority;

    @Override
    public int compareTo(ACE o) {
        if (getPriority()>o.getPriority()) {
            return -1;
        } else if (getPriority()<o.getPriority()) {
            return 1;
        } else if (getPriority()==o.getPriority()) {
            if ((getAceType()==ACEType.Deny) && (o.getAceType()==ACEType.Allow)) {
                return -1;
            } else if ((getAceType()==ACEType.Allow) && (o.getAceType()==ACEType.Deny)) {
                return 1;
            } else {
                return 0;
            }
        } else {
            throw new RuntimeException("Error de lógica:"+getPriority() + "," + o.getPriority());
        }
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
}
