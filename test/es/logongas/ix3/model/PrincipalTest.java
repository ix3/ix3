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

import es.logongas.ix3.security.services.authorization.AuthorizationType;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Lorenzo González
 */
public class PrincipalTest {

    private List<ACE> allow=new ArrayList<ACE>();
    private List<ACE> deny=new ArrayList<ACE>();

    public PrincipalTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        allow=new ArrayList<ACE>();
        allow.add(new ACE());
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testAuthorized() {
        System.out.println("authorized");
        SecureResourceType secureResourceType = null;
        String secureResource = "";
        Permission permission = null;
        Object arguments = null;
        Principal instance = new Principal();
        AuthorizationType expResult = null;
        AuthorizationType result = instance.authorized(secureResource, permission, arguments);
        assertEquals(expResult, result);
        fail("The test case is a prototype.");
    }

    @Test
    public void testGetSid() {
        System.out.println("getSid");
        Principal instance = new Principal();
        int expResult = 0;
        int result = instance.getSid();
        assertEquals(expResult, result);
        fail("The test case is a prototype.");
    }

    @Test
    public void testSetSid() {
        System.out.println("setSid");
        int sid = 0;
        Principal instance = new Principal();
        instance.setSid(sid);
        fail("The test case is a prototype.");
    }

    @Test
    public void testGetLogin() {
        System.out.println("getLogin");
        Principal instance = new Principal();
        String expResult = "";
        String result = instance.getLogin();
        assertEquals(expResult, result);
        fail("The test case is a prototype.");
    }

    @Test
    public void testSetLogin() {
        System.out.println("setLogin");
        String login = "";
        Principal instance = new Principal();
        instance.setLogin(login);
        fail("The test case is a prototype.");
    }

    @Test
    public void testGetName() {
        System.out.println("getName");
        Principal instance = new Principal();
        String expResult = "";
        String result = instance.getName();
        assertEquals(expResult, result);
        fail("The test case is a prototype.");
    }

    @Test
    public void testSetName() {
        System.out.println("setName");
        String name = "";
        Principal instance = new Principal();
        instance.setName(name);
        fail("The test case is a prototype.");
    }

    @Test
    public void testGetAcl() {
        System.out.println("getAcl");
        Principal instance = new Principal();
        Set expResult = null;
        Set result = instance.getAcl();
        assertEquals(expResult, result);
        fail("The test case is a prototype.");
    }

    @Test
    public void testSetAcl() {
        System.out.println("setAcl");
        Set<ACE> acl = null;
        Principal instance = new Principal();
        instance.setAcl(acl);
        fail("The test case is a prototype.");
    }

    @Test
    public void testGetMemberOf() {
        System.out.println("getMemberOf");
        Principal instance = new Principal();
        Set expResult = null;
        Set result = instance.getMemberOf();
        assertEquals(expResult, result);
        fail("The test case is a prototype.");
    }

    @Test
    public void testSetMemberOf() {
        System.out.println("setMemberOf");
        Set<GroupMember> memberOf = null;
        Principal instance = new Principal();
        instance.setMemberOf(memberOf);
        fail("The test case is a prototype.");
    }
}
