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
package org.jcrom;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jcr.Session;

import net.sf.cglib.proxy.LazyLoader;

import org.jcrom.util.SessionFactoryUtils;

/**
 * Abstract class used by lazy loading classes.
 * 
 * @author Nicolas Dos Santos
 */
abstract class AbstractLazyLoader implements LazyLoader {

    private static final Logger logger = Logger.getLogger(AbstractLazyLoader.class.getName());

    private final Session session;
    private final Mapper mapper;

    public AbstractLazyLoader(Session session, Mapper mapper) {
        this.session = session;
        this.mapper = mapper;
    }

    private Session getSession() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Getting the session");
        }
        Session sessionToUse = Jcrom.getCurrentSession() != null ? Jcrom.getCurrentSession() : session;
        if (sessionToUse == null || !sessionToUse.isLive()) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Creating a new session");
            }
            SessionFactory sessionFactory = mapper.getJcrom().getSessionFactory();
            sessionToUse = SessionFactoryUtils.getSession(sessionFactory);
        }
        return sessionToUse;
    }

    private void releaseSession(Session session) {
        if (session != null) {
            Session sessionToUse = Jcrom.getCurrentSession() != null ? Jcrom.getCurrentSession() : this.session;
            if (!sessionToUse.equals(session)) {
                SessionFactoryUtils.releaseSession(session);
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("Closing the newly created session");
                }
            }
        }
    }

    @Override
    public final Object loadObject() throws Exception {
        // Retrieve the session. If the session is closed, create a new session
        Session sessionToUse = getSession();
        // Load object
        Object obj = doLoadObject(sessionToUse, mapper);
        // Close only the newly created session
        releaseSession(sessionToUse);
        return obj;
    }

    protected abstract Object doLoadObject(Session session, Mapper mapper) throws Exception;
}
