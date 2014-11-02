/**
 * This file is part of the JCROM project.
 * Copyright (C) 2008-2014 - All rights reserved.
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

import java.io.File;
import java.net.URL;
import java.util.logging.LogManager;

import javax.jcr.Repository;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import org.apache.jackrabbit.core.TransientRepository;
import org.junit.After;
import org.junit.Before;

/**
 * 
 * @author Olafur Gauti Gudmundsson
 * @author Nicolas Dos Santos
 */
public class TestAbstract {

    protected Repository repo;
    protected Session session;
    protected String userID = "admin";
    protected char[] password = "admin".toCharArray();

    @Before
    public void setUpRepository() throws Exception {
        repo = new TransientRepository();
        session = repo.login(new SimpleCredentials(userID, password));

        ClassLoader loader = getClass().getClassLoader();
        URL url = loader.getResource("logger.properties");
        if (url == null) {
            url = loader.getResource("/logger.properties");
        }
        LogManager.getLogManager().readConfiguration(url.openStream());
    }

    @After
    public void tearDownRepository() throws Exception {
        session.logout();
        deleteDir(new File("repository"));
        new File("repository.xml").delete();
        new File("derby.log").delete();
    }

    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (String element : children) {
                boolean success = deleteDir(new File(dir, element));
                if (!success) {
                    return false;
                }
            }
        }

        // The directory is now empty so delete it
        return dir.delete();
    }
}
