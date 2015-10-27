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
package es.logongas.ix3.web.json.beanmapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Lista de propiedades a expandir
 *
 * @author logongas
 */
public class Expands extends ArrayList<String> {

    private final static Pattern expandPatternWithoutAsterisk = Pattern.compile("([_a-zA-Z0-9]+(\\.[_a-zA-Z0-9]+)*)(,([_a-zA-Z0-9]+(\\.[_a-zA-Z0-9]+)*))*");
    private final static Pattern expandPatternWithAsterisk = Pattern.compile("(([_a-zA-Z0-9]+(\\.[_a-zA-Z0-9]+)*)|\\*)(,(([_a-zA-Z0-9]+(\\.[_a-zA-Z0-9]+)*)|\\*))*");

    public static Expands createExpandsWithoutAsterisk(String commaSeparedExpands) {
        return createExpands(commaSeparedExpands, expandPatternWithoutAsterisk);
    }

    public static Expands createExpandsWithAsterisk(String commaSeparedExpands) {
        return createExpands(commaSeparedExpands, expandPatternWithAsterisk);
    }

    private static Expands createExpands(String commaSeparedExpands,Pattern pattern) {
        if ((commaSeparedExpands == null) || (commaSeparedExpands.trim().isEmpty())) {
            return new Expands();
        } else {
            if (pattern.matcher(commaSeparedExpands).matches() == false) {
                throw new RuntimeException("El parámetro expands no tiene el formato adecuado:" + commaSeparedExpands + " , " + pattern.pattern());
            }

            List<String> arrExpands = Arrays.asList(commaSeparedExpands.split(","));
            Expands expands = new Expands();
            expands.addAll(arrExpands);
            return expands;
        }
    }    
    
    public boolean isExpandProperty(String propertyName) {
        for (String expandProperty : this) {
            if ((expandProperty.trim()).startsWith(propertyName + ".") || (expandProperty.trim().equals(propertyName)) || (expandProperty.trim().equals("*"))) {
                return true;
            }
        }

        return false;
    }



}
