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

import javax.jcr.Node;
import net.sf.cglib.proxy.LazyLoader;
import org.jcrom.annotations.JcrFileNode;
import org.jcrom.util.NameFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles lazy loading of single file node.
 *
 * @author Olafur Gauti Gudmundsson
 */
public class FileNodeLoader implements LazyLoader {

    private static final Logger logger = LoggerFactory.getLogger(FileNodeLoader.class);
    
    private final Class objectClass;
    private final Node fileContainer;
    private final Object parentObject;
    private final JcrFileNode jcrFileNode;
    private final int depth;
    private final int maxDepth;
    private final NameFilter nameFilter;
    private final Mapper mapper;
    
    FileNodeLoader( Class objectClass, Node fileContainer, Object parentObject, JcrFileNode jcrFileNode, int depth, int maxDepth, NameFilter nameFilter, Mapper mapper ) {
		this.objectClass = objectClass;
		this.parentObject = parentObject;
        this.jcrFileNode = jcrFileNode;
		this.fileContainer = fileContainer;
		this.mapper = mapper;
		this.depth = depth;
		this.maxDepth = maxDepth;
		this.nameFilter = nameFilter;
    }
    
	public Object loadObject() throws Exception {
		if ( logger.isDebugEnabled() ) {
			logger.debug("Lazy loading file node for " + fileContainer.getPath());
		}
        return FileNodeMapper.getSingleFile(objectClass, fileContainer, parentObject, jcrFileNode, depth, maxDepth, nameFilter, mapper);
    }
}
