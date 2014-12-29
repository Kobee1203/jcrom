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
package org.jcrom;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

/**
 * Session Factory interface. This interface describes a simplfied contract for retrieving a session.
 * 
 * @author Nicolas Dos Santos
 */
public interface SessionFactory {

    /**
     * Returns a Session using the credentials and workspace on this SessionFactory implementation. The session
     * factory doesn't allow specification of a different workspace name because:
     * <p>
     *" Each Session object is associated one-to-one with a Workspace object. The Workspace object represents
     * a `view` of an actual repository workspace entity as seen through the authorization settings of its
     * associated Session." (quote from javax.jcr.Session javadoc).
     * </p>
     * @return the session.
     * @throws RepositoryException
     */
    Session getSession() throws RepositoryException;

}