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
package org.jcrom;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.jcrom.dao.AbstractJcrDAO;
import org.jcrom.util.SessionFactoryUtils;

public class TreeDAO extends AbstractJcrDAO<Tree> {

    private boolean flag;

    private Session currentSession;

    public TreeDAO(Jcrom jcrom) throws RepositoryException {
        super(jcrom);
    }

    @Override
    protected Session getSession() {
        if (!flag) {
            Session session = super.getSession();
            currentSession = session != null ? session : SessionFactoryUtils.getSession(getJcrom().getSessionFactory());
            flag = true;
        }
        return currentSession;
    }

    protected void releaseSession(Session session) {
        SessionFactoryUtils.releaseSession(session);
        currentSession = null;
        flag = false;
    }

    @Override
    public Tree create(Tree entity) {
        Session session = getSession();
        Tree result = super.create(entity);
        releaseSession(session);
        return result;
    }

    @Override
    public Tree loadById(String id) {
        Session session = getSession();
        Tree result = super.loadById(id);
        releaseSession(session);
        return result;
    }

    @Override
    public Tree update(Tree entity) {
        Session session = getSession();
        Tree result = super.update(entity);
        releaseSession(session);
        return result;
    }
}
