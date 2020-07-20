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
package es.logongas.ix3.rule;

/**
 *
 * @author logongas
 */
public class RuleGroupPredefined {
    public static class PreInsert {}
    public static class PreUpdate {}
    public static class PreDelete {}
    public static class PreInsertOrUpdate {}
    public static class PreInsertOrUpdateOrDelete {}
    public static class PreUpdateOrDelete {}
    
    
    public static class PostCreate {}
    public static class PostRead {}
    public static class PostInsert {}
    public static class PostUpdate {}
    public static class PostDelete {}    
    public static class PostInsertOrUpdate {}    
    public static class PostInsertOrUpdateOrDelete {}    
    public static class PostUpdateOrDelete {}    
}
