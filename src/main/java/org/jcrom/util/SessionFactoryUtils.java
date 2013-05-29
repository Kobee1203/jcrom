/**
 * This file is part of the JCROM project.
 * Copyright (C) 2008-2013 - All rights reserved.
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
package org.jcrom.util;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.jcrom.JcrMappingException;
import org.jcrom.SessionFactory;

/**
 * Helper class featuring methods for JCR Session handling.
 * 
 * @author Nicolas Dos Santos
 */
public final class SessionFactoryUtils {

	private SessionFactoryUtils() {
	}
	
    /**
     * Get a JCR Session from the SessionFactory
     * @param sessionFactory JCR Repository to create session with
     * @throws RepositoryException
     * @return {@link Session}
     */
    public static Session getSession(SessionFactory sessionFactory) throws JcrMappingException {
        try {
            if (sessionFactory == null) {
                throw new IllegalArgumentException("No sessionFactory specified");
            }

            Session session = sessionFactory.getSession();

            return session;
        } catch (RepositoryException ex) {
            throw new JcrMappingException("Could not open Jcr Session", ex);
        }
    }

    /**
     * Close the given JCR Session
     * @param session the Session to close
     */
    public static void releaseSession(Session session) {
        if (session == null) {
            return;
        }
        session.logout();
    }

}