/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package es.logongas.ix3.security.impl.authentication;

import es.logongas.ix3.security.services.authentication.AuthenticationManager;
import es.logongas.ix3.security.services.authentication.AuthenticationProvider;
import es.logongas.ix3.security.services.authentication.Credential;
import es.logongas.ix3.security.services.authentication.User;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 *
 * @author Lorenzo González
 */
public class AuthenticationManagerImpl implements AuthenticationManager {

    List<AuthenticationProvider> authenticationProviders=new ArrayList<>();

    @Override
    public User authenticate(Credential credential) {
        User user=null;


        for(Object authenticationProvider:authenticationProviders) {
            user=((AuthenticationProvider)authenticationProvider).authenticate(credential);
            if (user!=null) {
                break;
            }
        }

        return user;
    }

    @Override
    public User getUserByIdUser(String idUser) {
        User user=null;

        for(Object authenticationProvider:authenticationProviders) {
            user=((AuthenticationProvider)authenticationProvider).getUserByIdUser(idUser);
            if (user!=null) {
                break;
            }
        }

        return user;
    }

    public void setAuthenticationProviders(List<AuthenticationProvider> authenticationProviders) {
        this.authenticationProviders=authenticationProviders;
    }

}
