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
 * This is the main entry class for JCROM.
 * To use JCROM, you create an instance of this class, and add the classes
 * you want to be mapped to it. Jcrom will validate that the classes you add
 * can be mapped to/from JCR.
 * 
 * @author Olafur Gauti Gudmundsson
 */
public class Jcrom {

	private Map<Class<? extends JcrEntity>, Mapper> mappedClasses;
	
	public Jcrom() {
		mappedClasses = new HashMap<Class<? extends JcrEntity>, Mapper>();
	}
	
	/**
	 * Add a class that this instance can map to/from JCR nodes. This method
	 * will validate the class, and all mapped JcrEntity implementations
	 * referenced from this class.
	 * 
	 * @param entityClass the class that will be mapped
	 */
	public void addMappedClass( Class<? extends JcrEntity> entityClass ) {
		if ( !isMapped(entityClass) ) {
			Set<Class<? extends JcrEntity>> validClasses = Validator.validate(entityClass);
			for ( Class<? extends JcrEntity> c : validClasses ) {
				mappedClasses.put(c, new Mapper(c));
			}
		}
	}
	
	/**
	 * Get a set of all classes that are mapped by this instance.
	 *
	 * @return all classes that are mapped by this instance
	 */
	public Set<Class<? extends JcrEntity>> getMappedClasses() {
		return Collections.unmodifiableSet(mappedClasses.keySet());
	}
	
	/**
	 * Check whether a specific class is mapped by this instance.
	 * 
	 * @param entityClass the class we want to check
	 * @return true if the class is mapped, else false
	 */
	public boolean isMapped( Class<? extends JcrEntity> entityClass ) {
		return mappedClasses.containsKey(entityClass);
	}
	
	/**
	 * Maps the node supplied to an instance of the entity class. Loads all
	 * child nodes, to infinite depth.
	 * 
	 * @param entityClass the class of the entity to be instantiated from the node
	 * @param node the JCR node from which to create the object
	 * @return an instance of the JCR entity class, mapped from the node
	 * @throws java.lang.Exception
	 */
	public <T extends JcrEntity> T fromNode( Class<? extends JcrEntity> entityClass, Node node ) throws Exception {
		return (T)mappedClasses.get(entityClass).fromNode(node, "*", -1);
	}
	
	/**
	 * Maps the node supplied to an instance of the entity class.
	 * 
	 * @param entityClass the class of the entity to be instantiated from the node
	 * @param node the JCR node from which to create the object
	 * @param childNodeFilter comma separated list of names of child nodes to load 
	 * ("*" loads all, while "none" loads no children)
	 * @param maxDepth the maximum depth of loaded child nodes (0 means no child nodes are loaded,
	 * while a negative value means that no restrictions are set on the depth).
	 * @return an instance of the JCR entity class, mapped from the node
	 * @throws java.lang.Exception
	 */
	public <T extends JcrEntity> T fromNode( Class<? extends JcrEntity> entityClass, Node node, String childNodeFilter, int maxDepth ) throws Exception {
		return (T)mappedClasses.get(entityClass).fromNode(node, childNodeFilter, maxDepth);
	}
	
	/**
	 * Maps the entity supplied to a JCR node, and adds that node as a child
	 * to the parent node supplied.
	 * 
	 * @param parentNode the parent node to which the entity node will be added
	 * @param entity the entity to be mapped to the JCR node
	 * @return the newly created JCR node
	 * @throws java.lang.Exception
	 */
	public Node addNode( Node parentNode, JcrEntity entity ) throws Exception {
		return addNode(parentNode, entity, null);
	}
	
	/**
	 * Maps the entity supplied to a JCR node, and adds that node as a child
	 * to the parent node supplied.
	 * 
	 * @param parentNode the parent node to which the entity node will be added
	 * @param entity the entity to be mapped to the JCR node
	 * @param mixinTypes an array of mixin type that will be added to the new node
	 * @return the newly created JCR node
	 * @throws java.lang.Exception
	 */
	public Node addNode( Node parentNode, JcrEntity entity, String[] mixinTypes ) throws Exception {
		return mappedClasses.get(entity.getClass()).addNode(parentNode, entity, mixinTypes);
	}
	
	/**
	 * Update an existing JCR node with the entity supplied.
	 * 
	 * @param node the JCR node to be updated
	 * @param entity the entity that will be mapped to the existing node
	 * @return the name of the updated node
	 * @throws java.lang.Exception
	 */
	public String updateNode( Node node, JcrEntity entity ) throws Exception {
		return updateNode(node, entity, "*", -1);
	}
	
	/**
	 * Update an existing JCR node with the entity supplied.
	 * 
	 * @param node the JCR node to be updated
	 * @param entity the entity that will be mapped to the existing node
	 * @param childNodeFilter comma separated list of names of child nodes to update 
	 * ("*" updates all, while "none" updates no children)
	 * @param maxDepth the maximum depth of updated child nodes (0 means no child nodes are updated,
	 * while a negative value means that no restrictions are set on the depth).
	 * @return the name of the updated node
	 * @throws java.lang.Exception
	 */
	public String updateNode( Node node, JcrEntity entity, String childNodeFilter, int maxDepth ) throws Exception {
		return mappedClasses.get(entity.getClass()).updateNode(node, entity, childNodeFilter, maxDepth);
	}
}
