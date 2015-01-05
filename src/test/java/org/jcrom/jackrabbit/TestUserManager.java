/**
 * This file is part of the JCROM project.
 * Copyright (C) 2008-2015 - All rights reserved.
 * Authors: Olafur Gauti Gudmundsson, Nicolas Dos Santos
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jcrom.jackrabbit;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.SimpleCredentials;
import javax.jcr.Value;
import javax.jcr.ValueFactory;
import javax.jcr.security.AccessControlEntry;
import javax.jcr.security.AccessControlException;
import javax.jcr.security.AccessControlList;
import javax.jcr.security.AccessControlManager;
import javax.jcr.security.Privilege;

import org.apache.jackrabbit.api.JackrabbitSession;
import org.apache.jackrabbit.api.security.JackrabbitAccessControlEntry;
import org.apache.jackrabbit.api.security.JackrabbitAccessControlList;
import org.apache.jackrabbit.api.security.JackrabbitAccessControlManager;
import org.apache.jackrabbit.api.security.JackrabbitAccessControlPolicy;
import org.apache.jackrabbit.api.security.principal.PrincipalManager;
import org.apache.jackrabbit.api.security.user.AuthorizableExistsException;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.jackrabbit.core.security.principal.EveryonePrincipal;
import org.apache.jackrabbit.core.security.principal.PrincipalImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * http://wiki.apache.org/jackrabbit/AccessControl
 */
public class TestUserManager extends TestAbstract {

    @Before
    public void setUp() throws Exception {
        super.setUpRepository();
        session = repo.login(new SimpleCredentials("admin", "admin".toCharArray()));
    }

    @After
    public void tearDown() throws Exception {
        super.tearDownRepository();
    }

    @Ignore
    @Test
    public void testGrantallRightsToEveryone() throws AccessControlException, RepositoryException {
        String path = "/some/path";

        AccessControlManager aMgr = session.getAccessControlManager();

        // create a privilege set with jcr:all
        Privilege[] privileges = new Privilege[] { aMgr.privilegeFromName(Privilege.JCR_ALL) };
        AccessControlList acl;
        try {
            // get first applicable policy (for nodes w/o a policy)
            acl = (AccessControlList) aMgr.getApplicablePolicies(path).nextAccessControlPolicy();
        } catch (NoSuchElementException e) {
            // else node already has a policy, get that one
            acl = (AccessControlList) aMgr.getPolicies(path)[0];
        }
        // remove all existing entries
        for (AccessControlEntry e : acl.getAccessControlEntries()) {
            acl.removeAccessControlEntry(e);
        }
        // add a new one for the special "everyone" principal
        acl.addAccessControlEntry(EveryonePrincipal.getInstance(), privileges);

        // the policy must be re-set
        aMgr.setPolicy(path, acl);

        // and the session must be saved for the changes to be applied
        session.save();
    }

    @Ignore
    @Test
    public void testSettingPrincipalBasedACL() throws RepositoryException {
        // usual entry point into the Jackrabbit API
        JackrabbitSession js = (JackrabbitSession) session;

        // get user/principal for whom to read/set ACLs

        // Note: the ACL security API works using Java Principals as high-level abstraction and does not
        // assume the users are actually stored in the JCR with the Jackrabbit UserManagement; an example
        // are external users provided by a custom LoginModule via LDAP
        PrincipalManager pMgr = js.getPrincipalManager();
        Principal principal = pMgr.getPrincipal(session.getUserID());

        // alternatively: get the current user as Authorizable from the user management
        // (there is a one-to-one mapping between Authorizables and Principals)
        User user = ((User) js.getUserManager().getAuthorizable(session.getUserID()));
        Principal principal2 = user.getPrincipal();

        // get the Jackrabbit access control manager
        JackrabbitAccessControlManager acMgr = (JackrabbitAccessControlManager) session.getAccessControlManager();

        JackrabbitAccessControlPolicy[] ps = acMgr.getPolicies(principal); // or getApplicablePolicies()
        JackrabbitAccessControlList list = (JackrabbitAccessControlList) ps[0];

        // list entries
        JackrabbitAccessControlEntry[] entries = (JackrabbitAccessControlEntry[]) list.getAccessControlEntries();
        JackrabbitAccessControlEntry entry = entries[0];

        // remove entry
        list.removeAccessControlEntry(entry);

        // add entry
        Privilege[] privileges = new Privilege[] { acMgr.privilegeFromName(Privilege.JCR_READ) };
        Map<String, Value> restrictions = new HashMap<String, Value>();
        ValueFactory vf = session.getValueFactory();
        restrictions.put("rep:nodePath", vf.createValue("/some/path", PropertyType.PATH));
        restrictions.put("rep:glob", vf.createValue("*"));
        list.addEntry(principal, privileges, true /* allow or deny */, restrictions);

        // reorder entries
        // list.orderBefore(entry, entry2);

        // finally set policy again & save
        acMgr.setPolicy(list.getPath(), list);
        session.save();
    }

    public boolean createUser(String name, String pass) throws AuthorizableExistsException, RepositoryException {

        // usual entry point into the Jackrabbit API
        JackrabbitSession js = (JackrabbitSession) session;
        UserManager um = js.getUserManager();

        PrincipalImpl p = new PrincipalImpl(name);
        String usersPath = "/" + name;
        User u = um.createUser(name, pass, p, null);
        u.setProperty("homeFolder", session.getValueFactory().createValue(usersPath));
        // "HOME" folder for the brand new user
        // createUsersFolder(name, session);
        // Assign permissions to the "HOME" folder of the just created user
        // assignInitialPermissions(u, u.getPrincipal(), usersPath, session);
        session.save();
        return true;
    }
}
