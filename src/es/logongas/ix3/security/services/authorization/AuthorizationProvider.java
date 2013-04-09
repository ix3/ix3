/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package es.logongas.ix3.security.services.authorization;

import es.logongas.ix3.security.services.authentication.User;


/**
 *
 * @author Lorenzo González
 */
public interface AuthorizationProvider {
    AuthorizationType authorized(User user,ResourceType resourceType,AccessType accessType, Object resource);
}
