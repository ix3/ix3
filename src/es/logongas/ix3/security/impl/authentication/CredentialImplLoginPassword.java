/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package es.logongas.ix3.security.impl.authentication;

import es.logongas.ix3.security.services.authentication.Credential;

/**
 *
 * @author Lorenzo González
 */
public class CredentialImplLoginPassword implements Credential {
    private String login;
    private String password;

    public CredentialImplLoginPassword(String login, String password) {
        this.login = login;
        this.password = password;
    }



    /**
     * @return the login
     */
    public String getLogin() {
        return login;
    }

    /**
     * @param login the login to set
     */
    public void setLogin(String login) {
        this.login = login;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }
}
