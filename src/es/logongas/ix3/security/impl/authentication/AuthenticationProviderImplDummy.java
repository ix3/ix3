/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package es.logongas.ix3.security.impl.authentication;

import es.logongas.ix3.security.services.authentication.AuthenticationProvider;
import es.logongas.ix3.security.services.authentication.Credential;
import es.logongas.ix3.security.services.authentication.User;

/**
 *
 * @author Lorenzo González
 */
public class AuthenticationProviderImplDummy implements AuthenticationProvider {

    @Override
    public User authenticate(Credential credential) {
        if (credential instanceof CredentialImplLoginPassword) {
            CredentialImplLoginPassword credentialImplLoginPassword=(CredentialImplLoginPassword)credential;
            if (("admin".equals(credentialImplLoginPassword.getLogin())) && ("admin".equals(credentialImplLoginPassword.getPassword()))  ) {
                return new UserImpl("admin","Administrador");
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public User getUserByIdUser(String idUser) {
        if ("admin".equals(idUser)) {
            return new UserImpl("admin","Administrador");
        } else {
            return null;
        }
    }

}
