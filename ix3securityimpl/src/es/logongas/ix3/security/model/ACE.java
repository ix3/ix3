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
import es.logongas.ix3.security.authorization.AuthorizationType;
import es.logongas.ix3.util.ScriptEvaluator;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 *
 * @author Lorenzo González
 */
public class ACE {

    private int idACE;
    private ACEType aceType;
    private Permission permission;
    @ValuesList()
    private Identity identity;
    private String secureResourceRegExp;
    private Pattern secureResourcePattern;
    private String conditionalScript;
    private String conditionalExpression;
    private Integer priority;
    private String description;
    
    private static final ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
    private static final ScriptEngine scriptEngine = scriptEngineManager.getEngineByName("JavaScript");
    private static final ScriptEvaluator scriptEvaluator = new ScriptEvaluator(scriptEngine);

    private static final ExpressionParser expressionParser = new SpelExpressionParser();

    protected final Log log = LogFactory.getLog(getClass());

    public ACE() {
    }

    public ACE(int idACE, ACEType aceType, Permission permission, Identity identity, String secureResourceRegExp, String conditionalScript, String conditionalExpression, Integer priority, String description) {
        this.idACE = idACE;
        this.aceType = aceType;
        this.permission = permission;
        this.identity = identity;
        this.secureResourceRegExp = secureResourceRegExp;
        this.secureResourcePattern=Pattern.compile("^" + this.secureResourceRegExp + "$");
        this.conditionalScript = conditionalScript;
        this.conditionalExpression = conditionalExpression;
        this.priority = priority;
        this.description = description;
    }

    

    @Override
    public String toString() {
        return idACE + "-" + aceType + " - " + permission + " - " + secureResourceRegExp;
    }

    public AuthorizationType authorized(Identity identity, String secureResourceName, Permission permission, Object arguments) {
        AuthorizationType authorizationType;

        if (this.permission.equals(permission)) {
            Matcher matcher=secureResourcePattern.matcher(secureResourceName);
            if (matcher.matches()) {             
                if ((existsConditionalExpression() == false) && (existsConditionalScript() == false)) {
                    authorizationType = aceTypeToAuthorizationType(aceType);
                } else if ((existsConditionalExpression() == true) && (existsConditionalScript() == false)) {
                    List<String> matcherGroups=getMatcherGroups(matcher);
                    if (evaluateConditionalExpression(this, identity, secureResourceName, arguments,matcherGroups) == true) {
                        authorizationType = aceTypeToAuthorizationType(aceType);
                    } else {
                        authorizationType = AuthorizationType.Abstain;
                    }
                } else if ((existsConditionalExpression() == false) && (existsConditionalScript() == true)) {
                    List<String> matcherGroups=getMatcherGroups(matcher);
                    if (evaluateConditionalScript(this, identity, secureResourceName, arguments,matcherGroups) == true) {
                        authorizationType = aceTypeToAuthorizationType(aceType);
                    } else {
                        authorizationType = AuthorizationType.Abstain;
                    }
                } else if ((existsConditionalExpression() == true) && (existsConditionalScript() == true)) {
                    throw new RuntimeException("No es posible indicar un Script y una expresion para el ACE:" + this.getIdACE());
                } else {
                    throw new RuntimeException("Error de lógica para el ACE:" + this.getIdACE());
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

    private List<String> getMatcherGroups(Matcher matcher) {
        List<String> matcherGroups=new ArrayList<String>();
        
        for(int i=0;i<=matcher.groupCount();i++) {
            matcherGroups.add(matcher.group(i));
        }
        
        return matcherGroups;
    }
    
    private boolean evaluateConditionalScript(ACE ace, Identity identity, String secureResourceName, Object arguments, List<String> mg) {
        if ((ace.getConditionalScript() == null) || (ace.getConditionalScript().trim().length() == 0)) {
            throw new RuntimeException("No podemos evaluar un Script vacio del ACE:" + ace.getIdACE());
        }

        String functionName = "conditionalScript_" + ace.getIdACE() + "_" + Math.abs(ace.getConditionalScript().hashCode());

        //Si aun no se ha compilado el Script lo hacemos ahora
        if (((Boolean) scriptEvaluator.evaluate("typeof " + functionName + "=='function'")) == false) {
            scriptEvaluator.evaluate("function " + functionName + "(ace,identity,secureResourceName,arguments,mg) {" + ace.getConditionalScript() + "}");
            log.debug("Compilando código del ACE " + ace.getIdACE());
        }

        Object result = scriptEvaluator.invokeFunction(functionName, ace, identity, secureResourceName, arguments,mg);
        if (result == null) {
            throw new RuntimeException("El Script no puede retornar null en el ACE:" + ace.getIdACE());
        }
        if (!(result instanceof Boolean)) {
            throw new RuntimeException("El Script no es un boolean en el ACE:" + ace.getIdACE());
        }

        return (Boolean) result;
    }

    private boolean evaluateConditionalExpression(ACE ace, Identity identity, String secureResourceName, Object arguments,List<String> mg) {
        Object result;

        //Si no hay Script retornamos 'null'
        if ((ace.getConditionalExpression() == null) || (ace.getConditionalExpression().trim().length() == 0)) {
            throw new RuntimeException("No podemos evaluar una expresion vacia del ACE:" + ace.getIdACE());
        }

        EvaluationContext context = new StandardEvaluationContext(new ConditionalExpressionSpelContext(ace, identity, secureResourceName, arguments,mg));
        result = expressionParser.parseExpression(ace.getConditionalExpression()).getValue(context, Object.class);
        if (result == null) {
            throw new RuntimeException("La expresion no puede retornar null en el ACE:" + ace.getIdACE());
        }
        if (!(result instanceof Boolean)) {
            throw new RuntimeException("La expresion no es un boolean en el ACE:" + ace.getIdACE());
        }

        return (Boolean) result;
    }

    protected boolean existsConditionalExpression() {
        if ((this.getConditionalExpression() == null) || (this.getConditionalExpression().trim().length() == 0)) {
            return false;
        } else {
            return true;
        }
    }

    protected boolean existsConditionalScript() {
        if ((this.getConditionalScript() == null) || (this.getConditionalScript().trim().length() == 0)) {
            return false;
        } else {
            return true;
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
        this.secureResourcePattern=Pattern.compile("^" + this.secureResourceRegExp + "$");
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
     * @return the conditionalExpression
     */
    public String getConditionalExpression() {
        return conditionalExpression;
    }

    /**
     * @param conditionalExpression the conditionalExpression to set
     */
    public void setConditionalExpression(String conditionalExpression) {
        this.conditionalExpression = conditionalExpression;
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
     * Contiene los datos a los que tiene acceso una "conditionalExpresion"
     */
    private class ConditionalExpressionSpelContext {

        public ACE ace;
        public Identity identity;
        public String secureResourceName;
        public Object arguments;
        public List<String> mg;

        public ConditionalExpressionSpelContext(ACE ace, Identity identity, String secureResourceName, Object arguments, List<String> mg) {
            this.ace = ace;
            this.identity = identity;
            this.secureResourceName = secureResourceName;
            this.arguments = arguments;
            this.mg = mg;
        }



        /**
         * @return the ace
         */
        public ACE getAce() {
            return ace;
        }

        /**
         * @param ace the ace to set
         */
        public void setAce(ACE ace) {
            this.ace = ace;
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
         * @return the secureResourceName
         */
        public String getSecureResourceName() {
            return secureResourceName;
        }

        /**
         * @param secureResourceName the secureResourceName to set
         */
        public void setSecureResourceName(String secureResourceName) {
            this.secureResourceName = secureResourceName;
        }

        /**
         * @return the arguments
         */
        public Object getArguments() {
            return arguments;
        }

        /**
         * @param arguments the arguments to set
         */
        public void setArguments(Object arguments) {
            this.arguments = arguments;
        }

        /**
         * @return the mg
         */
        public List<String> getMg() {
            return mg;
        }

        /**
         * @param mg the mg to set
         */
        public void setMg(List<String> mg) {
            this.mg = mg;
        }

    }

}
