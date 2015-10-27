/*
 * Copyright 2015 logongas.
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
package es.logongas.ix3.web.controllers.endpoint;

import es.logongas.ix3.web.json.beanmapper.BeanMapper;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.util.AntPathMatcher;

/**
 * Definicion de un endpoint. Es decir indica a ix3 de todos las "URL" y "Metodos" que tiene. Cuales de "muestran" al exterior y como transformar los modelos al exterior.
 *
 * @author logongas
 */
public class EndPoint {

    final private String path;
    final private BeanMapper beanMapper;
    final private String method;
    final private static AntPathMatcher antPathMatcher = new AntPathMatcher();

    public static EndPoint createEndPoint(String path, String method, BeanMapper beanMapper) {
        return new EndPoint(path, method, beanMapper);
    }

    public static EndPoint createEndPointCrud(String prefixPath, Class entityClass) {
        return new EndPoint(prefixPath + "/" + entityClass.getSimpleName() + "/**", null, new BeanMapper(entityClass));
    }

    public static EndPoint createEndPointCrud(String prefixPath, BeanMapper beanMapper) {
        return new EndPoint(prefixPath + "/" + beanMapper.getEntityClass().getSimpleName() + "/**", null, beanMapper);
    }

    private EndPoint(String path, String method, BeanMapper beanMapper) {
        if ((path == null) || (path.trim().length() == 0)) {
            throw new RuntimeException("El path  no puede estar vacio");
        }

        this.path = path;
        this.method = method;
        this.beanMapper = beanMapper;
    }

    public boolean matches(String path, String method) {
        if (antPathMatcher.match(this.path, path)) {
            if ((this.method == null) || (this.method.equals("*")) || (this.method.equals(method))) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public static List<EndPoint> getMatchEndPoint(List<EndPoint> endPoints, String path, String method) {
        List<EndPoint> matchEndPoints = new ArrayList<EndPoint>();

        for (EndPoint endPoint : endPoints) {
            if (endPoint.matches(path, method)) {
                matchEndPoints.add(endPoint);
            }
        }

        return matchEndPoints;
    }

    public static EndPoint getBestEndPoint(List<EndPoint> endPoints, String path, String method) {
        Comparator<String> comparatorPath = antPathMatcher.getPatternComparator(path);
        Comparator<String> comparatorMethod = antPathMatcher.getPatternComparator(method);

        EndPoint bestEndPoint = null;

        for (EndPoint newEndPoint : endPoints) {
            if (bestEndPoint == null) {
                //Si no hay ninguno seguro que este es el mejor endPoint
                bestEndPoint = newEndPoint;
            } else {
                int valuePath = comparatorPath.compare(bestEndPoint.getPath(), newEndPoint.getPath());
                if (valuePath < 0) {
                    //El mejor sigue siendo el actual
                    bestEndPoint = bestEndPoint;
                } else if (valuePath > 0) {
                    bestEndPoint = newEndPoint;
                } else if (valuePath == 0) {
                    //Si son iguales por path veamos ahora por método.

                    int valueMethod = comparatorMethod.compare(bestEndPoint.getMethod(), newEndPoint.getMethod());
                    if ((bestEndPoint.getMethod() == null) && (newEndPoint.getMethod() == null)) {
                        throw new RuntimeException("EndPoits repetidos:" + bestEndPoint.toString() + " y " + newEndPoint.toString());
                    } else if ((bestEndPoint.getMethod() == null) && (newEndPoint.getMethod() != null)) {
                        bestEndPoint=newEndPoint;
                    } else if ((bestEndPoint.getMethod() != null) && (newEndPoint.getMethod() == null)) {
                        bestEndPoint = bestEndPoint;
                    } else if ((bestEndPoint.getMethod() != null) && (newEndPoint.getMethod() != null)) {
                        
                        if (bestEndPoint.getMethod().equals(newEndPoint.getMethod())) {
                            throw new RuntimeException("EndPoits repetidos:" + bestEndPoint.toString() + " y " + newEndPoint.toString());
                        } else if (bestEndPoint.getMethod().equals("*")) {
                            bestEndPoint=newEndPoint;
                        } else if (newEndPoint.getMethod().equals("*")) {
                            bestEndPoint=bestEndPoint;
                        } else {
                           throw new RuntimeException("Error de logioca:" + bestEndPoint.toString() + " y " + newEndPoint.toString());
                        }
                        
                    } else {
                        throw new RuntimeException("Error de logioca:" + valueMethod);
                    }

                } else {
                    throw new RuntimeException("Error de logioca:" + valuePath);
                }
            }
        }

        return bestEndPoint;
    }

    /**
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * @return the method
     */
    public String getMethod() {
        return method;
    }

    /**
     * @return the beanMapper
     */
    public BeanMapper getBeanMapper() {
        return beanMapper;
    }

    @Override
    public String toString() {
        return this.getPath() + "-" + this.getMethod();
    }
}
