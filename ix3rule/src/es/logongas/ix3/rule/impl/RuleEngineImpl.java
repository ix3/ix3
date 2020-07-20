/*
 * ix3 Copyright 2020 Lorenzo González.
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
package es.logongas.ix3.rule.impl;

import es.logongas.ix3.core.BusinessException;
import es.logongas.ix3.core.BusinessMessage;
import es.logongas.ix3.rule.ActionRule;
import es.logongas.ix3.rule.ConstraintRule;
import es.logongas.ix3.rule.RuleContext;
import es.logongas.ix3.rule.RuleEngine;
import es.logongas.ix3.util.ExceptionUtil;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

            if (isExecuteConstrainRule(constraintRule, groups)) {
                try {
                    int numArgs = method.getParameterTypes().length;
                    boolean result;
                    if (numArgs == 0) {
                        result = (Boolean) method.invoke(rulesObject);
                    } else if (numArgs == 1) {
                        Class argumenType = method.getParameterTypes()[0];

                        if (RuleContext.class.isAssignableFrom(argumenType) == false) {
                            throw new RuntimeException("El método " + method.getName() + " debe tener el único argumento del tipo:" + RuleContext.class.getName());
                        }

                        result = (Boolean) method.invoke(rulesObject, ruleContext);

                    } else {
                        throw new RuntimeException("El método " + method.getName() + " solo puede tener 0 o 1 argumentos pero tiene:" + numArgs);
                    }

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
                    throw new RuntimeException(ex);
                } catch (InvocationTargetException ex) {
                    BusinessException businessException = ExceptionUtil.getBusinessExceptionFromThrowable(ex);
                    if (businessException != null) {
                        throw businessException;
                    } else {
                        throw new RuntimeException(ex);
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

            if (isExecuteActionRule(actionRule, groups)) {
                try {
                    int numArgs = method.getParameterTypes().length;
                    if (numArgs == 0) {
                        method.invoke(rulesObject);
                    } else if (numArgs == 1) {
                        Class argumenType = method.getParameterTypes()[0];

                        if (RuleContext.class.isAssignableFrom(argumenType) == false) {
                            throw new RuntimeException("El método " + method.getName() + " debe tener el único argumento del tipo:" + RuleContext.class.getName());
                        }

                        method.invoke(rulesObject, ruleContext);

                    } else {
                        throw new RuntimeException("El método " + method.getName() + " solo puede tener 0 o 1 argumentos pero tiene:" + numArgs);
                    }

                } catch (IllegalAccessException | IllegalArgumentException ex) {
                    throw new RuntimeException(ex);
                } catch (InvocationTargetException ex) {
                    BusinessException businessException = ExceptionUtil.getBusinessExceptionFromThrowable(ex);
                    if (businessException!=null) {
                        throw businessException;
                    } else {
                        throw new RuntimeException(ex);
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

    private boolean isExecuteConstrainRule(ConstraintRule constraintRule, Class<?>[] groups) {

        if (constraintRule.disabled() == true) {
            return false;
        }

        if (isExecuteRuleByGroup(constraintRule.groups(), groups) == false) {
            return false;
        }

        return true;

    }

    private boolean isExecuteActionRule(ActionRule actionRule, Class<?>[] groups) {

        if (actionRule.disabled() == true) {
            return false;
        }

        if (isExecuteRuleByGroup(actionRule.groups(), groups) == false) {
            return false;
        }

        return true;

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
