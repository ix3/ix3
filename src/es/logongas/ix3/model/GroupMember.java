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
package es.logongas.ix3.model;

/**
 * Relación entre los "Principal" que están incluidos en un grupo
 * @author Lorenzo González
 */
public class GroupMember {
    private int idGroupMember;
    private Group group;
    private Principal principal;

    public GroupMember() {
    }

    public GroupMember(int idGroupMember, Group group, Principal principal) {
        this.idGroupMember = idGroupMember;
        this.group = group;
        this.principal = principal;
    }



    /**
     * @return the idGroupMember
     */
    public int getIdGroupMember() {
        return idGroupMember;
    }

    /**
     * @param idGroupMember the idGroupMember to set
     */
    public void setIdGroupMember(int idGroupMember) {
        this.idGroupMember = idGroupMember;
    }

    /**
     * @return the group
     */
    public Group getGroup() {
        return group;
    }

    /**
     * @param group the group to set
     */
    public void setGroup(Group group) {
        this.group = group;
    }

    /**
     * @return the principal
     */
    public Principal getPrincipal() {
        return principal;
    }

    /**
     * @param principal the principal to set
     */
    public void setPrincipal(Principal principal) {
        this.principal = principal;
    }
}
