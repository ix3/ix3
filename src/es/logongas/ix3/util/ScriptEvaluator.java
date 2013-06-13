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
package es.logongas.ix3.util;

import java.util.List;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;

/**
 * Simplificación del uso de la clase ScriptEngine en entornos multithread.
 *
 * @author Lorenzo González
 */
public class ScriptEvaluator {

    private final ScriptEngine scriptEngine;

    public ScriptEvaluator(ScriptEngine scriptEngine) {
        this.scriptEngine = scriptEngine;
    }

    /**
     * Llama a una función de ScriptEngine
     *
     * @param functionName Nombre de la función
     * @param args Argumentos de la función
     * @return Lo que retorna el llamar a la función
     */
    public Object invokeFunction(String functionName,Object... args) {
        if (scriptEngine instanceof Invocable) {
            Invocable invocable = (Invocable) scriptEngine;
            try {
                if (isSafeMultiThread()) {
                    return invocable.invokeFunction(functionName, args);
                } else {
                    synchronized (scriptEngine) {
                        return invocable.invokeFunction(functionName, args);
                    }
                }
            } catch (Exception ex) {
                throw new RuntimeException("Fallo al invocar la funcion:"+functionName,ex);
            }
        } else {
            throw new RuntimeException("El scriptEngine no permite invocar funciones:"+functionName);
        }
    }

    /**
     * Evalua una función dentro de ScriptEngine
     *
     * @param script El código fuente de Script a evaluar (ejecutar)
     * @return Lo que retorna el Script
     */
    public Object evaluate(String script) {
        try {
            if (isSafeMultiThread()) {
                return scriptEngine.eval(script);
            } else {
                synchronized (scriptEngine) {
                    return scriptEngine.eval(script);
                }
            }
        } catch (ScriptException ex) {
            throw new RuntimeException(script,ex);
        }
    }

    /**
     * Si el scriptEngine es seguro en entornos de multithread
     *
     * @return
     */
    private boolean isSafeMultiThread() {
        ScriptEngineFactory scriptEngineFactory = scriptEngine.getFactory();
        String threading = (String) scriptEngineFactory.getParameter("THREADING");

        if (threading == null) {
            return false;
        } else if (threading.equals("MULTITHREADED")) {
            return true;
        } else if (threading.equals("THREAD-ISOLATED")) {
            return true;
        } else if (threading.equals("STATELESS")) {
            return true;
        } else {
            //No sabemos nada sobre el tipo de multithread.
            //debe ser una propiedad nueva de Java.
            return false;
        }
    }
}
