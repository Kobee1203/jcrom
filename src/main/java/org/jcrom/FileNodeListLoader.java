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

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jcr.Node;
import javax.jcr.Session;

import org.jcrom.annotations.JcrFileNode;
import org.jcrom.util.NodeFilter;
import org.jcrom.util.PathUtils;

/**
 * Handles lazy loading of file node lists.
 * 
 * @author Olafur Gauti Gudmundsson
 * @author Nicolas Dos Santos
 */
class FileNodeListLoader extends AbstractLazyLoader {

    private static final Logger logger = Logger.getLogger(FileNodeListLoader.class.getName());

    private final Class<?> objectClass;
    private final String fileContainerPath;
    private final Object parentObject;
    private final JcrFileNode jcrFileNode;
    private final int depth;
    private final NodeFilter nodeFilter;

    FileNodeListLoader(Class<?> objectClass, Object parentObject, String fileContainerPath, Session session, Mapper mapper, int depth, NodeFilter nodeFilter, JcrFileNode jcrFileNode) {
        super(session, mapper);
        this.objectClass = objectClass;
        this.parentObject = parentObject;
        this.jcrFileNode = jcrFileNode;
        this.fileContainerPath = fileContainerPath;
        this.depth = depth;
        this.nodeFilter = nodeFilter;
    }

    @Override
    protected Object doLoadObject(Session session, Mapper mapper) throws Exception {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Lazy loading file list for " + fileContainerPath);
        }
        Node fileContainer = PathUtils.getNode(fileContainerPath, session);
        return mapper.getFileNodeMapper().getFileList(objectClass, fileContainer, parentObject, jcrFileNode, depth, nodeFilter, mapper);
    }
}
