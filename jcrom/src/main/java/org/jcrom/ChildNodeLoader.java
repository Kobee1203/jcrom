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
import javax.jcr.Session;
import net.sf.cglib.proxy.LazyLoader;
import org.jcrom.util.NodeFilter;

/**
 * Handles lazy loading of single child node.
 * 
 * @author Olafur Gauti Gudmundsson
 */
class ChildNodeLoader implements LazyLoader {
	
	private static final Logger logger = Logger.getLogger(ChildNodeLoader.class.getName());
	
	private final Class objectClass;
	private final Object parentObject;
	private final String containerPath;
	private final Session session;
	private final Mapper mapper;
	private final int depth;
	private final NodeFilter nodeFilter;
    private final boolean pathIsContainer;

	ChildNodeLoader( Class objectClass, Object parentObject, String containerPath, Session session, Mapper mapper,
			int depth, NodeFilter nodeFilter ) {
        
        this(objectClass, parentObject, containerPath, session, mapper, depth, nodeFilter, true);
    }
    
	ChildNodeLoader( Class objectClass, Object parentObject, String containerPath, Session session, Mapper mapper,
			int depth, NodeFilter nodeFilter, boolean pathIsContainer ) {
		this.objectClass = objectClass;
		this.parentObject = parentObject;
		this.containerPath = containerPath;
		this.session = session;
		this.mapper = mapper;
		this.depth = depth;
		this.nodeFilter = nodeFilter;
        this.pathIsContainer = pathIsContainer;
	}

	public Object loadObject() throws Exception {
		if ( logger.isLoggable(Level.FINE) ) {
			logger.fine("Lazy loading single child for " + containerPath);
		}
        Node node;
        if ( pathIsContainer ) {
            node = session.getRootNode().getNode(containerPath.substring(1)).getNodes().nextNode();
        } else {
            node = session.getRootNode().getNode(containerPath.substring(1));
        }
		return ChildNodeMapper.getSingleChild(objectClass, node, parentObject, mapper, depth, nodeFilter);
	}
}
