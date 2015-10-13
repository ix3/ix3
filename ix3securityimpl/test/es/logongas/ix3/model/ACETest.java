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

import es.logongas.ix3.security.model.Permission;
import es.logongas.ix3.security.model.User;
import es.logongas.ix3.security.model.SecureResourceType;
import es.logongas.ix3.security.model.ACEType;
import es.logongas.ix3.security.model.ACE;
import es.logongas.ix3.security.authorization.AuthorizationType;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Lorenzo González
 */
public class ACETest {

    public ACETest() {
    }

    @Test
    public void testAuthorized() {
        System.out.println("authorized Allow");
        SecureResourceType secureResourceType = new SecureResourceType(1, "URL", "URL");
        Permission permission = new Permission(1, "GET", "GET", secureResourceType);
        User user=new User(1, "Juan", "Juan García");
        Object arguments = new HashMap<String,Object>();
        ACE instance = new ACE(1, ACEType.Allow, permission, user, "/.*", null,null, 10,null);
        AuthorizationType expResult = AuthorizationType.AccessAllow;
        AuthorizationType result = instance.authorized(user,"/index.html", permission, arguments);
        assertEquals(expResult, result);
    }
    @Test
    public void testAuthorized2() {
        System.out.println("authorized Deny");
        SecureResourceType secureResourceType = new SecureResourceType(1, "URL", "URL");
        Permission permission = new Permission(1, "GET", "GET", secureResourceType);
        User user=new User(1, "Juan", "Juan García");
        Object arguments = new HashMap<String,Object>();
        ACE instance = new ACE(1, ACEType.Deny, permission, user, "/.*", null,null, 10,null);
        AuthorizationType expResult = AuthorizationType.AccessDeny;
        AuthorizationType result = instance.authorized(user,"/index.html", permission, arguments);
        assertEquals(expResult, result);
    }

    @Test
    public void testAuthorized3() {
        System.out.println("authorized Match recurso con 2 barras en la URL");
        SecureResourceType secureResourceType = new SecureResourceType(1, "URL", "URL");
        Permission permission = new Permission(1, "GET", "GET", secureResourceType);
        User user=new User(1, "Juan", "Juan García");
        Object arguments = new HashMap<String,Object>();
        ACE instance = new ACE(1, ACEType.Allow, permission, user, "/.*", null,null, 10,null);
        AuthorizationType expResult = AuthorizationType.AccessAllow;
        AuthorizationType result = instance.authorized(user,"/juan/index.html", permission, arguments);
        assertEquals(expResult, result);
    }

    @Test
    public void testAuthorized5() {
        System.out.println("authorized Permiso Distinto");
        SecureResourceType secureResourceType = new SecureResourceType(1, "URL", "URL");
        Permission permission = new Permission(1, "GET", "GET", secureResourceType);
        User user=new User(1, "Juan", "Juan García");
        Object arguments = new HashMap<String,Object>();
        ACE instance = new ACE(1, ACEType.Allow, permission, user, "/.*", null,null, 10,null);
        AuthorizationType expResult = AuthorizationType.Abstain;
        AuthorizationType result = instance.authorized(user,"/juan/index.html", new Permission(1, "POST", "POST", secureResourceType), arguments);
        assertEquals(expResult, result);
    }

    @Test
    public void testAuthorized6() {
        System.out.println("authorized El recurso no match");
        SecureResourceType secureResourceType = new SecureResourceType(1, "URL", "URL");
        Permission permission = new Permission(1, "GET", "GET", secureResourceType);
        User user=new User(1, "Juan", "Juan García");
        Object arguments = new HashMap<String,Object>();
        ACE instance = new ACE(1, ACEType.Allow, permission, user, "/.*/.*", null,null, 10,null);
        AuthorizationType expResult = AuthorizationType.Abstain;
        AuthorizationType result = instance.authorized(user,"/index.html", permission, arguments);
        assertEquals(expResult, result);
    }
    @Test
    public void testAuthorized7() {
        System.out.println("authorized El recurso si match");
        SecureResourceType secureResourceType = new SecureResourceType(1, "URL", "URL");
        Permission permission = new Permission(1, "GET", "GET", secureResourceType);
        User user=new User(1, "Juan", "Juan García");
        Object arguments = new HashMap<String,Object>();
        ACE instance = new ACE(1, ACEType.Allow, permission, user, "/.*/.*",null, null, 10,null);
        AuthorizationType expResult = AuthorizationType.AccessAllow;
        AuthorizationType result = instance.authorized(user,"/pepe/index.html", permission, arguments);
        assertEquals(expResult, result);
    }

    @Test
    public void testAuthorizedScript1() {
        System.out.println("authorized Script a true");
        SecureResourceType secureResourceType = new SecureResourceType(1, "URL", "URL");
        Permission permission = new Permission(1, "GET", "GET", secureResourceType);
        User user=new User(1, "Juan", "Juan García");
        Map<String,Object> arguments = new HashMap<String,Object>();
        arguments.put("user","pepe");
        ACE instance = new ACE(1, ACEType.Allow, permission, user, "/.*/.*", "return arguments.get('user')=='pepe'",null, 10,null);
        AuthorizationType expResult = AuthorizationType.AccessAllow;
        AuthorizationType result = instance.authorized(user,"/pepe/index.html", permission, arguments);
        assertEquals(expResult, result);
    }

    @Test
    public void testAuthorizedScript2() {
        System.out.println("authorized Script  a false");
        SecureResourceType secureResourceType = new SecureResourceType(1, "URL", "URL");
        Permission permission = new Permission(1, "GET", "GET", secureResourceType);
        User user=new User(1, "Juan", "Juan García");
        Map<String,Object> arguments = new HashMap<String,Object>();
        arguments.put("user","pepe");
        ACE instance = new ACE(1, ACEType.Allow, permission, user, "/.*/.*", "return arguments.get('user')=='juan'",null, 10,null);
        AuthorizationType expResult = AuthorizationType.Abstain;
        AuthorizationType result = instance.authorized(user,"/pepe/index.html", permission, arguments);
        assertEquals(expResult, result);
    }

    @Test
    public void testAuthorizedScript3() {
        System.out.println("authorized Script  usando el Identity");
        SecureResourceType secureResourceType = new SecureResourceType(1, "URL", "URL");
        Permission permission = new Permission(1, "GET", "GET", secureResourceType);
        User user=new User(1, "Juan", "Juan García");
        Map<String,Object> arguments = new HashMap<String,Object>();
        arguments.put("user","pepe");
        ACE instance = new ACE(1, ACEType.Allow, permission, user, "/.*/.*", "return identity.getLogin()=='Juan'",null, 10,null);
        AuthorizationType expResult = AuthorizationType.AccessAllow;
        AuthorizationType result = instance.authorized(user,"/pepe/index.html", permission, arguments);
        assertEquals(expResult, result);
    }

    @Test
    public void testAuthorizedScript4() {
        System.out.println("authorized Script  usando el secureResourceName");
        SecureResourceType secureResourceType = new SecureResourceType(1, "URL", "URL");
        Permission permission = new Permission(1, "GET", "GET", secureResourceType);
        User user=new User(1, "Juan", "Juan García");
        Map<String,Object> arguments = new HashMap<String,Object>();
        arguments.put("user","pepe");
        ACE instance = new ACE(1, ACEType.Allow, permission, user, "/.*/.*", "return secureResourceName=='/pepe/index.html'",null, 10,null);
        AuthorizationType expResult = AuthorizationType.AccessAllow;
        AuthorizationType result = instance.authorized(user,"/pepe/index.html", permission, arguments);
        assertEquals(expResult, result);
    }

    @Test
    public void testAuthorizedScript5() {
        System.out.println("authorized Script  usando el ACE");
        SecureResourceType secureResourceType = new SecureResourceType(1, "URL", "URL");
        Permission permission = new Permission(1, "GET", "GET", secureResourceType);
        User user=new User(1, "Juan", "Juan García");
        Map<String,Object> arguments = new HashMap<String,Object>();
        arguments.put("user","pepe");
        ACE instance = new ACE(4, ACEType.Allow, permission, user, "/.*/.*", "return ace.getIdACE()==4",null, 10,null);
        AuthorizationType expResult = AuthorizationType.AccessAllow;
        AuthorizationType result = instance.authorized(user,"/pepe/index.html", permission, arguments);
        assertEquals(expResult, result);
    }
    @Test
    public void testAuthorizedScript6() {
        System.out.println("authorized Script  usando múltiple lineas");
        SecureResourceType secureResourceType = new SecureResourceType(1, "URL", "URL");
        Permission permission = new Permission(1, "GET", "GET", secureResourceType);
        User user=new User(1, "Juan", "Juan García");
        Map<String,Object> arguments = new HashMap<String,Object>();
        arguments.put("user","pepe");
        ACE instance = new ACE(4, ACEType.Allow, permission, user, "/.*/.*", "var a=4; if (a==4) { return true; } else { return false; }",null, 10,null);
        AuthorizationType expResult = AuthorizationType.AccessAllow;
        AuthorizationType result = instance.authorized(user,"/pepe/index.html", permission, arguments);
        assertEquals(expResult, result);
    }
    
    
    
    
    @Test
    public void testAuthorizedExpression1() {
        System.out.println("authorized Expression a true");
        SecureResourceType secureResourceType = new SecureResourceType(1, "URL", "URL");
        Permission permission = new Permission(1, "GET", "GET", secureResourceType);
        User user=new User(1, "Juan", "Juan García");
        Map<String,Object> arguments = new HashMap<String,Object>();
        arguments.put("user","pepe");
        ACE instance = new ACE(1, ACEType.Allow, permission, user, "/.*/.*", null,"arguments.get('user')=='pepe'", 10,null);
        AuthorizationType expResult = AuthorizationType.AccessAllow;
        AuthorizationType result = instance.authorized(user,"/pepe/index.html", permission, arguments);
        assertEquals(expResult, result);
    }

    @Test
    public void testAuthorizedExpression2() {
        System.out.println("authorized Expression  a false");
        SecureResourceType secureResourceType = new SecureResourceType(1, "URL", "URL");
        Permission permission = new Permission(1, "GET", "GET", secureResourceType);
        User user=new User(1, "Juan", "Juan García");
        Map<String,Object> arguments = new HashMap<String,Object>();
        arguments.put("user","pepe");
        ACE instance = new ACE(1, ACEType.Allow, permission, user, "/.*/.*",null, "arguments.get('user')=='juan'", 10,null);
        AuthorizationType expResult = AuthorizationType.Abstain;
        AuthorizationType result = instance.authorized(user,"/pepe/index.html", permission, arguments);
        assertEquals(expResult, result);
    }

    @Test
    public void testAuthorizedExpression3() {
        System.out.println("authorized Expression  usando el Identity");
        SecureResourceType secureResourceType = new SecureResourceType(1, "URL", "URL");
        Permission permission = new Permission(1, "GET", "GET", secureResourceType);
        User user=new User(1, "Juan", "Juan García");
        Map<String,Object> arguments = new HashMap<String,Object>();
        arguments.put("user","pepe");
        ACE instance = new ACE(1, ACEType.Allow, permission, user, "/.*/.*",null, "identity.getLogin()=='Juan'", 10,null);
        AuthorizationType expResult = AuthorizationType.AccessAllow;
        AuthorizationType result = instance.authorized(user,"/pepe/index.html", permission, arguments);
        assertEquals(expResult, result);
    }

    @Test
    public void testAuthorizedExpression4() {
        System.out.println("authorized Expression  usando el secureResourceName");
        SecureResourceType secureResourceType = new SecureResourceType(1, "URL", "URL");
        Permission permission = new Permission(1, "GET", "GET", secureResourceType);
        User user=new User(1, "Juan", "Juan García");
        Map<String,Object> arguments = new HashMap<String,Object>();
        arguments.put("user","pepe");
        ACE instance = new ACE(1, ACEType.Allow, permission, user, "/.*/.*",null, "secureResourceName=='/pepe/index.html'", 10,null);
        AuthorizationType expResult = AuthorizationType.AccessAllow;
        AuthorizationType result = instance.authorized(user,"/pepe/index.html", permission, arguments);
        assertEquals(expResult, result);
    }

    @Test
    public void testAuthorizedExpression5() {
        System.out.println("authorized Expression  usando el ACE");
        SecureResourceType secureResourceType = new SecureResourceType(1, "URL", "URL");
        Permission permission = new Permission(1, "GET", "GET", secureResourceType);
        User user=new User(1, "Juan", "Juan García");
        Map<String,Object> arguments = new HashMap<String,Object>();
        arguments.put("user","pepe");
        ACE instance = new ACE(4, ACEType.Allow, permission, user, "/.*/.*",null, "ace.getIdACE()==4", 10,null);
        AuthorizationType expResult = AuthorizationType.AccessAllow;
        AuthorizationType result = instance.authorized(user,"/pepe/index.html", permission, arguments);
        assertEquals(expResult, result);
    }

    
    
}

