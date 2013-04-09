/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package es.logongas.ix3.security.impl.authentication;

import es.logongas.ix3.security.services.authentication.User;

/**
 *
 * @author Lorenzo González
 */
public class UserImpl implements User {
    String login;
    String name;

    public UserImpl(String login, String name) {
        this.login = login;
        this.name = name;
    }


    @Override
    public String getIdUser() {
        return login;
    }

    @Override
    public String getName() {
        return name;
    }
}
