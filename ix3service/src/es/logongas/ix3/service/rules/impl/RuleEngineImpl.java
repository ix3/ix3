/*
 * Copyright 2015 Lorenzo Gonzalez.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package es.logongas.ix3.service.rules.impl;

import es.logongas.ix3.core.BusinessException;
import es.logongas.ix3.core.BusinessMessage;
import es.logongas.ix3.service.rules.ActionRule;
import es.logongas.ix3.service.rules.ConstraintRule;
import es.logongas.ix3.service.rules.RuleContext;
import es.logongas.ix3.service.rules.RuleEngine;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;

/**
 *
 * @author logongas
 * @param <T>
 */
public class RuleEngineImpl<T> implements RuleEngine<T> {

    private static final ExpressionParser expressionParser = new SpelExpressionParser();
    private static final TemplateParserContext templateParserContext = new TemplateParserContext("${", "}");

    
    @Override
    public void fireConstraintRules(Object rulesObject, RuleContext<T> ruleContext, Class<?>... groups) throws BusinessException {
        List<Method> methods = getRuleMethods(rulesObject, ConstraintRule.class);
        List<BusinessMessage> businessMessages = new ArrayList<BusinessMessage>();

        Collections.sort(methods, new Comparator<Method>() {

            @Override
            public int compare(Method method1, Method method2) {
                ConstraintRule constraintRule1 = method1.getAnnotation(ConstraintRule.class);
                ConstraintRule constraintRule2 = method2.getAnnotation(ConstraintRule.class);

                if (constraintRule1.priority() == constraintRule2.priority()) {
                    return 0;
                } else if (constraintRule1.priority() > constraintRule2.priority()) {
                    return 1;
                } else if (constraintRule1.priority() < constraintRule2.priority()) {
                    return -1;
                } else {
                    throw new RuntimeException("Error de lógica");
                }

            }

        });

        for (Method method : methods) {
            ConstraintRule constraintRule = method.getAnnotation(ConstraintRule.class);

            if (isExecuteRuleByGroup(constraintRule.groups(), groups)) {
                try {
                    boolean result = (Boolean) method.invoke(rulesObject, ruleContext);

                    if (result == false) {

                        BusinessMessage businessMessage = getBusinessMessage(constraintRule, ruleContext);
                        businessMessages.add(businessMessage);

                        if (constraintRule.stopOnFail() == true) {
                            break;
                        }
                    }

                } catch (IllegalAccessException ex) {
                    throw new RuntimeException(ex);
                } catch (IllegalArgumentException ex) {
                    Logger.getLogger(RuleEngineImpl.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InvocationTargetException ex) {
                    Throwable cause = ex.getCause();
                    if (cause instanceof BusinessException) {
                        BusinessException businessException = (BusinessException) cause;

                        throw businessException;
                    }
                }

            }
        }

        if (businessMessages.size() > 0) {
            throw new BusinessException(businessMessages);
        }

    }
    
    @Override
    public void fireActionRules(Object rulesObject, RuleContext<T> ruleContext, Class<?>... groups) throws BusinessException {
        List<Method> methods = getRuleMethods(rulesObject, ActionRule.class);

        Collections.sort(methods, new Comparator<Method>() {

            @Override
            public int compare(Method method1, Method method2) {
                ActionRule actionRule1 = method1.getAnnotation(ActionRule.class);
                ActionRule actionRule2 = method2.getAnnotation(ActionRule.class);

                if (actionRule1.priority() == actionRule2.priority()) {
                    return 0;
                } else if (actionRule1.priority() > actionRule2.priority()) {
                    return 1;
                } else if (actionRule1.priority() < actionRule2.priority()) {
                    return -1;
                } else {
                    throw new RuntimeException("Error de lógica");
                }

            }

        });

        for (Method method : methods) {
            ActionRule actionRule = method.getAnnotation(ActionRule.class);

            if (isExecuteRuleByGroup(actionRule.groups(), groups)) {
                try {
                    method.invoke(rulesObject, ruleContext);
                } catch (IllegalAccessException | IllegalArgumentException ex) {
                    throw new RuntimeException(ex);
                } catch (InvocationTargetException ex) {
                    Throwable cause = ex.getCause();
                    if (cause instanceof BusinessException) {
                        BusinessException businessException = (BusinessException) cause;

                        throw businessException;
                    }
                }
            }
        }

    }

    private List<Method> getRuleMethods(Object rulesObject, Class annotationClass) {
        Map<String, Method> ruleMethods = new HashMap<String, Method>();
        Method[] methods;

        if (rulesObject == null) {
            throw new RuntimeException("El objeto con la regla no puede ser null");
        }

        methods = rulesObject.getClass().getMethods();

        for (Method method : methods) {
            if (method.isAnnotationPresent(annotationClass) == true) {
                if (ruleMethods.containsKey(method.getName()) == false) {
                    ruleMethods.put(method.getName(), method);
                }
            }
        }

        methods = rulesObject.getClass().getDeclaredMethods();

        for (Method method : methods) {
            if (method.isAnnotationPresent(annotationClass) == true) {
                if (ruleMethods.containsKey(method.getName()) == false) {
                    method.setAccessible(true);
                    ruleMethods.put(method.getName(), method);
                }
            }
        }

        return new ArrayList<Method>(ruleMethods.values());
    }

    private BusinessMessage getBusinessMessage(ConstraintRule constraintRule, RuleContext<T> ruleContext) {
        String message = expressionParser.parseExpression(constraintRule.message(), templateParserContext).getValue(ruleContext, String.class);
        String filedName = constraintRule.fieldName();

        return new BusinessMessage(filedName, message);
    }

    private boolean isExecuteRuleByGroup(Class<?>[] constraintGroups, Class<?>[] executeGroups) {
        if ((constraintGroups == null) || (constraintGroups.length == 0)) {
            return true;
        } else if ((executeGroups == null) || (executeGroups.length == 0)) {
            return true;
        } else {

            boolean exists = false;
            for (Class constraintGroup : constraintGroups) {
                for (Class executeGroup : executeGroups) {

                    if ((executeGroup != null) && (constraintGroup != null) && (executeGroup.equals(constraintGroup))) {
                        exists = true;
                        break;
                    }

                }
            }

            return exists;
        }
    }

}
