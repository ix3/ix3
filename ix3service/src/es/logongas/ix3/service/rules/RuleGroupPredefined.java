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
package es.logongas.ix3.service.rules;

/**
 *
 * @author logongas
 */
public class RuleGroupPredefined {
    public static class PreRead {}
    public static class PreInsert {}
    public static class PreUpdate {}
    public static class PreDelete {}
    public static class PreInsertOrUpdate {}
    public static class PreInsertOrUpdateOrDelete {}
    
    
    public static class PostRead {}
    public static class PostInsert {}
    public static class PostUpdate {}
    public static class PostDelete {}    
    public static class PostInsertOrUpdate {}    
    public static class PostInsertOrUpdateOrDelete {}    
}
