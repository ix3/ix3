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
        if (priority>o.priority) {
            return -1;
        } else if (priority<o.priority) {
            return 1;
        } else if (priority==o.priority) {
            if ((aceType==ACEType.Deny) && (o.aceType==ACEType.Allow)) {
                return -1;
            } else if ((aceType==ACEType.Allow) && (o.aceType==ACEType.Deny)) {
                return 1;
            } else {
                return 0;
            }
        } else {
            throw new RuntimeException("Error de lógica:"+priority + "," + o.priority);
        }
    }
}
