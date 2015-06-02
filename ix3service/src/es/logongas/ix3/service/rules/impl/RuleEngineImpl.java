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
import es.logongas.ix3.util.ReflectionUtil;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.util.ReflectionUtils;

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

        List<Field> fields=getRuleFields(rulesObject);
        for (Field field : fields) {
            RuleContextImplDelegate ruleContextImplDelegate=new RuleContextImplDelegate(ruleContext, field.getName());
            
            if (ruleContextImplDelegate.getEntity()!=null) {
                this.fireConstraintRules(ruleContextImplDelegate.getEntity(), ruleContextImplDelegate, groups);
            }
        }        
        
        
        
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

        List<Field> fields=getRuleFields(rulesObject);
        for (Field field : fields) {
            RuleContextImplDelegate ruleContextImplDelegate=new RuleContextImplDelegate(ruleContext, field.getName());
            
            if (ruleContextImplDelegate.getEntity()!=null) {
                this.fireActionRules(ruleContextImplDelegate.getEntity(), ruleContextImplDelegate, groups);
            }
        }          
        
        
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
                    Throwable cause = ex.getCause();
                    if (cause instanceof BusinessException) {
                        BusinessException businessException = (BusinessException) cause;

                        throw businessException;
                    } else {
                        throw new RuntimeException(cause);
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
    
    
    private List<Field> getRuleFields(Object rulesObject) {
        final List<Field> fields=new ArrayList<Field>();
        
        if (rulesObject==null) {
            return fields;
        }
        
        ReflectionUtils.doWithFields(rulesObject.getClass(), new ReflectionUtils.FieldCallback() {

            @Override
            public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
                if (field.getAnnotation(Valid.class)!=null) {
                    fields.add(field);
                }
            }
        });
        
        
        return fields;
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
