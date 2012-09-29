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

import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jcr.Node;
import javax.jcr.Session;

import org.jcrom.util.NodeFilter;
import org.jcrom.util.PathUtils;

/**
 * Handles lazy loading of reference lists.
 * 
 * @author Olafur Gauti Gudmundsson
 * @author Nicolas Dos Santos
 */
public class ReferenceListLoader extends AbstractLazyLoader {

    private static final Logger logger = Logger.getLogger(ReferenceListLoader.class.getName());

    private final Class<?> objClass;
    private final Object parentObject;
    private final String nodePath;
    private final String propertyName;
    private final int depth;
    private final NodeFilter nodeFilter;
    private final Field field;

    ReferenceListLoader(Class<?> objClass, Object parentObject, String nodePath, String propertyName, Session session, Mapper mapper, int depth, NodeFilter nodeFilter, Field field) {
        super(session, mapper);
        this.objClass = objClass;
        this.parentObject = parentObject;
        this.nodePath = nodePath;
        this.propertyName = propertyName;
        this.depth = depth;
        this.nodeFilter = nodeFilter;
        this.field = field;
    }

    @Override
    protected Object doLoadObject(Session session, Mapper mapper) throws Exception {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Lazy loading reference list for " + nodePath + " " + propertyName);
        }
        Node node = PathUtils.getNode(nodePath, session);
        return mapper.getReferenceMapper().getReferenceList(field, propertyName, objClass, node, parentObject, depth, nodeFilter, mapper);
    }
}
