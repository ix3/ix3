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

import es.logongas.ix3.core.annotations.ValuesList;
import es.logongas.ix3.security.services.authorization.AuthorizationType;
import es.logongas.ix3.util.ScriptEvaluator;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author Lorenzo González
 */
public class ACE {

    private int idACE;
    private ACEType aceType;
    @ValuesList(dependProperties = "aceType")
    private Permission permission;
    @ValuesList()
    private Identity identity;
    private String secureResourceRegExp;
    private String conditionalScript;
    private Integer priority;


    private static final ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
    private static final ScriptEngine scriptEngine = scriptEngineManager.getEngineByName("JavaScript");
    private static final ScriptEvaluator scriptEvaluator = new ScriptEvaluator(scriptEngine);

    protected final Log log = LogFactory.getLog(getClass());

    public ACE() {
    }

    public ACE(int idACE, ACEType aceType, Permission permission, Identity identity, String secureResourceRegExp, String conditionalScript, Integer priority) {
        this.idACE = idACE;
        this.aceType = aceType;
        this.permission = permission;
        this.identity = identity;
        this.secureResourceRegExp = secureResourceRegExp;
        this.conditionalScript = conditionalScript;
        this.priority = priority;
    }

    @Override
    public String toString() {
        String cs = "";
        if (conditionalScript != null) {
            cs = " WHERE (" + conditionalScript + ")";
        }
        return aceType + " - " + permission + " => " + secureResourceRegExp + cs;
    }

    public AuthorizationType authorized(Identity identity, String secureResourceName, Permission permission, Object arguments) {
        AuthorizationType authorizationType;

        if (this.permission.equals(permission)) {
            if (secureResourceName.matches(secureResourceRegExp)) {
                if (evaluateConditionalScript(this, identity, secureResourceName, arguments)==true) {
                    authorizationType = aceTypeToAuthorizationType(aceType);
                } else {
                    authorizationType = AuthorizationType.Abstain;
                }
            } else {
                authorizationType = AuthorizationType.Abstain;
            }
        } else {
            authorizationType = AuthorizationType.Abstain;
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
                throw new RuntimeException("El tipo de ACE es desconocido:" + aceType);
        }
    }

    private boolean evaluateConditionalScript(ACE ace, Identity identity, String secureResourceName, Object arguments) {
        Object result;


        //Si no hay Script retirnamos 'true'
        if ((conditionalScript==null) || (conditionalScript.trim().length()==0)) {
            return true;
        }

        String functionName = "conditionalScript_" + ace.getIdACE()+"_"+Math.abs(conditionalScript.hashCode());

        //Si aun no se ha compilado el Script lo hacemos ahora
        if (((Boolean) scriptEvaluator.evaluate("typeof " + functionName + "=='function'")) == false) {
            scriptEvaluator.evaluate("function " + functionName + "(ace,identity,secureResourceName,arguments) {" + ace.getConditionalScript() + "}");
            log.debug("Compilando código del ACE " +  ace.getIdACE()+ ":"+ace.getConditionalScript());
        }

        result =scriptEvaluator.invokeFunction(functionName, ace, identity, secureResourceName, arguments);
        if (result==null) {
            throw new RuntimeException("El método no puede retornal null:"+conditionalScript);
        }
        if (!(result instanceof Boolean)) {
            throw new RuntimeException("El método no ha retornado un boolean:"+conditionalScript + " ," + result + " , " + result.getClass().getName());
        }

        return (Boolean)result;
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
     * @return the identity
     */
    public Identity getIdentity() {
        return identity;
    }

    /**
     * @param identity the identity to set
     */
    public void setIdentity(Identity identity) {
        this.identity = identity;
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
    public Integer getPriority() {
        return priority;
    }

    /**
     * @param priority the priority to set
     */
    public void setPriority(Integer priority) {
        this.priority = priority;
    }
}
