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
import net.sf.cglib.proxy.LazyLoader;
import org.jcrom.util.NameFilter;

/**
 * Handles lazy loading of single reference.
 * 
 * @author Olafur Gauti Gudmundsson
 */
class ReferenceLoader implements LazyLoader {
	
	private static final Logger logger = Logger.getLogger(ReferenceLoader.class.getName());
	
	private final Class objClass;
	private final Object parentObject;
	private final Session session;
	private final String nodePath;
	private final String propertyName;
	private final Mapper mapper;
	private final int depth;
	private final int maxDepth;
	private final NameFilter nameFilter;
	private final Field field;
	
	ReferenceLoader( Class objClass, Object parentObject, String nodePath, String propertyName, 
			Session session, Mapper mapper, int depth, int maxDepth, NameFilter nameFilter, Field field ) {
		this.objClass = objClass;
		this.parentObject = parentObject;
		this.nodePath = nodePath;
		this.propertyName = propertyName;
		this.session = session;
		this.mapper = mapper;
		this.depth = depth;
		this.maxDepth = maxDepth;
		this.nameFilter = nameFilter;
		this.field = field;
	}

	public Object loadObject() throws Exception {
		if ( logger.isLoggable(Level.FINE) ) {
			logger.fine("Lazy loading single reference for " + nodePath + " " + propertyName);
		}
		Node node = session.getRootNode().getNode(nodePath.substring(1));
		return ReferenceMapper.createReferencedObject(field, node.getProperty(propertyName).getValue(), parentObject, 
				session, objClass, depth, maxDepth, nameFilter, mapper);
	}
}
