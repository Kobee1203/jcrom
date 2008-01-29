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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.jcr.Node;

/**
 *
 * @author Olafur Gauti Gudmundsson
 */
public class Jcrom {

	private Map<Class<? extends JcrEntity>, Mapper> mappedClasses;
	
	public Jcrom() {
		mappedClasses = new HashMap<Class<? extends JcrEntity>, Mapper>();
	}
	
	public void addMappedClass( Class<? extends JcrEntity> entityClass ) {
		if ( !isMapped(entityClass) ) {
			Set<Class<? extends JcrEntity>> validClasses = Validator.validate(entityClass);
			for ( Class<? extends JcrEntity> c : validClasses ) {
				mappedClasses.put(c, new Mapper(c));
			}
		}
	}
	
	public Set<Class<? extends JcrEntity>> getMappedClasses() {
		return Collections.unmodifiableSet(mappedClasses.keySet());
	}
	
	public boolean isMapped( Class<? extends JcrEntity> entityClass ) {
		return mappedClasses.containsKey(entityClass);
	}
	
	public <T extends JcrEntity> T fromNode( Class<? extends JcrEntity> entityClass, Node node ) throws Exception {
		return (T)mappedClasses.get(entityClass).fromNode(node, "*", -1);
	}
	
	public <T extends JcrEntity> T fromNode( Class<? extends JcrEntity> entityClass, Node node, String childNodeFilter, int maxDepth ) throws Exception {
		return (T)mappedClasses.get(entityClass).fromNode(node, childNodeFilter, maxDepth);
	}
	
	public Node addNode( Node parentNode, JcrEntity entity ) throws Exception {
		return addNode(parentNode, entity, null);
	}
	
	public Node addNode( Node parentNode, JcrEntity entity, String[] mixinTypes ) throws Exception {
		return mappedClasses.get(entity.getClass()).addNode(parentNode, entity, mixinTypes);
	}
	
	public String updateNode( Node node, JcrEntity entity ) throws Exception {
		return updateNode(node, entity, "*", -1);
	}
	
	public String updateNode( Node node, JcrEntity entity, String childNodeFilter, int maxDepth ) throws Exception {
		return mappedClasses.get(entity.getClass()).updateNode(node, entity, childNodeFilter, maxDepth);
	}
}
