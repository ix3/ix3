/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package es.logongas.ix3.security.services.authorization;

/**
 * Si se concede o no acceso a un recurso
 * @author Lorenzo González
 */
public enum AuthorizationType {
    /*
     * Se permite el acceso al recurso
     */
    AccessAllow,
    /*
     * Se deniega el acceso al recurso
     */
    AccessDeny,
    /*
     * No se dice nada sobre si se puede o no acceder al recurso
     * Aunque el proveeder conoce el tipo de recurso
     */
    Abstain,
    /*
     * El proveedor no conoce el tipo de recurso
     */
    ResourceUnknown
}
