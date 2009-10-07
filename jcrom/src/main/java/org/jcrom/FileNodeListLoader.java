/**
 * Copyright (C) Olafur Gauti Gudmundsson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

import net.sf.cglib.proxy.LazyLoader;

import org.jcrom.annotations.JcrFileNode;
import org.jcrom.util.NodeFilter;

/**
 * Handles lazy loading of file node lists.
 * 
 * @author Olafur Gauti Gudmundsson
 */
class FileNodeListLoader implements LazyLoader {

    private static final Logger logger = Logger.getLogger(FileNodeListLoader.class.getName());

    private final Class objectClass;
    private final Node fileContainer;
    private final Object parentObject;
    private final JcrFileNode jcrFileNode;
    private final int depth;
    private final NodeFilter nodeFilter;
    private final Mapper mapper;

    FileNodeListLoader(Class objectClass, Node fileContainer, Object parentObject, JcrFileNode jcrFileNode, int depth,
            NodeFilter nodeFilter, Mapper mapper) {
        this.objectClass = objectClass;
        this.parentObject = parentObject;
        this.jcrFileNode = jcrFileNode;
        this.fileContainer = fileContainer;
        this.mapper = mapper;
        this.depth = depth;
        this.nodeFilter = nodeFilter;
    }

    public Object loadObject() throws Exception {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Lazy loading file list for " + fileContainer.getPath());
        }
        return mapper.getFileNodeMapper().getFileList(objectClass, fileContainer, parentObject, jcrFileNode, depth,
                nodeFilter, mapper);
    }
}
