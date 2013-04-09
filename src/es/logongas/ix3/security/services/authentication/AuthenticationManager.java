/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package es.logongas.ix3.security.services.authentication;

/**
 *
 * @author Lorenzo González
 */
public interface AuthenticationManager {
    User authenticate(Credential credential);
    User getUserByIdUser(String idUser);
}
