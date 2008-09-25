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

import java.io.IOException;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import org.jcrom.util.NodeFilter;

/**
 * This is the main entry class for JCROM.
 * To use JCROM, you create an instance of this class, and add the classes
 * you want to be mapped to it. Jcrom will validate that the classes you add
 * can be mapped to/from JCR.
 * 
 * <p>Instances of this class are thread-safe.</p>
 * 
 * @author Olafur Gauti Gudmundsson
 */
public class Jcrom {

	private final Mapper mapper;

	/**
	 * Create a new Jcrom instance that cleans node names, but with dynamic
	 * instantiation turned off.
	 */
	public Jcrom() {
		this(true);
	}

	/**
	 * Create a new Jcrom instance with dynamic instantiation turned off.
	 * 
	 * @param cleanNames specifies whether to clean names of new nodes, that is,
	 * replace illegal characters and spaces automatically
	 */
	public Jcrom(boolean cleanNames) {
		this(cleanNames, false);
	}

	/**
	 * Create a new Jcrom instance.
	 * 
	 * @param cleanNames specifies whether to clean names of new nodes, that is,
	 * replace illegal characters and spaces automatically
	 * @param dynamicInstantiation if set to true, then Jcrom will try to retrieve
	 * the name of the class to instantiate from a node property
	 * (see @JcrNode(classNameProperty)).
	 */
	public Jcrom(boolean cleanNames, boolean dynamicInstantiation) {
		this(cleanNames, dynamicInstantiation, new HashSet<Class>());
	}

	/**
	 * Create a new Jcrom instance with name cleaning set to true,
	 * and dynamic instantiation off.
	 * 
	 * @param classesToMap a set of classes to map by this instance
	 */
	public Jcrom(Set<Class> classesToMap) {
		this(true, false, classesToMap);
	}

	/**
	 * Create a new Jcrom instance with dynamic instantiation turned off.
	 * 
	 * @param cleanNames specifies whether to clean names of new nodes, that is,
	 * replace illegal characters and spaces automatically
	 * @param classesToMap a set of classes to map by this instance
	 */
	public Jcrom(boolean cleanNames, Set<Class> classesToMap) {
		this(cleanNames, false, classesToMap);
	}

	/**
	 * Create a new Jcrom instance.
	 * 
	 * @param cleanNames specifies whether to clean names of new nodes, that is,
	 * replace illegal characters and spaces automatically
	 * @param dynamicInstantiation if set to true, then Jcrom will try to retrieve
	 * the name of the class to instantiate from a node property
	 * (see @JcrNode(classNameProperty)).
	 * @param classesToMap a set of classes to map by this instance
	 */
	public Jcrom(boolean cleanNames, boolean dynamicInstantiation, Set<Class> classesToMap) {
		this.mapper = new Mapper(cleanNames, dynamicInstantiation);
		for (Class c : classesToMap) {
			map(c);
		}
	}

	/**
	 * Add a class that this instance can map to/from JCR nodes. This method
	 * will validate the class, and all mapped JcrEntity implementations
	 * referenced from this class.
	 * 
	 * @param entityClass the class that will be mapped
	 * @return the Jcrom instance
	 */
	public synchronized Jcrom map(Class entityClass) {
		if ( !mapper.isMapped(entityClass) ) {
			Set<Class> validClasses = Validator.validate(entityClass, mapper.isDynamicInstantiation());
			for (Class c : validClasses) {
				mapper.addMappedClass(c);
			}
		}
		return this;
	}

	/**
	 * Get a set of all classes that are mapped by this instance.
	 *
	 * @return all classes that are mapped by this instance
	 */
	public Set<Class> getMappedClasses() {
		return Collections.unmodifiableSet(mapper.getMappedClasses());
	}

	/**
	 * Check whether a specific class is mapped by this instance.
	 * 
	 * @param entityClass the class we want to check
	 * @return true if the class is mapped, else false
	 */
	public boolean isMapped(Class entityClass) {
		return mapper.isMapped(entityClass);
	}

	public String getName(Object object) throws JcrMappingException {
		try {
			return Mapper.getNodeName(object);
		} catch (IllegalAccessException e) {
			throw new JcrMappingException("Could not get node name from object", e);
		}
	}

	public String getPath(Object object) throws JcrMappingException {
		try {
			return Mapper.getNodePath(object);
		} catch (IllegalAccessException e) {
			throw new JcrMappingException("Could not get node path from object", e);
		}
	}

	public void setBaseVersionInfo(Object object, String name, Calendar created) throws JcrMappingException {
		try {
			Mapper.setBaseVersionInfo(object, name, created);
		} catch (IllegalAccessException e) {
			throw new JcrMappingException("Could not set base version info on object", e);
		}
	}

	/**
	 * Maps the node supplied to an instance of the entity class. Loads all
	 * child nodes, to infinite depth.
	 * 
	 * @param entityClass the class of the entity to be instantiated from the node
	 * (in the case of dynamic instantiation, the instance class may be read from
	 * the document, but will be cast to this class)
	 * @param node the JCR node from which to create the object
	 * @return an instance of the JCR entity class, mapped from the node
	 * @throws JcrMappingException
	 */
	public <T> T fromNode(Class<T> entityClass, Node node) throws JcrMappingException {
		return (T) fromNode(entityClass, node, "*", -1);
	}

	/**
	 * Maps the node supplied to an instance of the entity class.
	 * 
	 * @param entityClass the class of the entity to be instantiated from the node
	 * (in the case of dynamic instantiation, the instance class may be read from
	 * the document, but will be cast to this class)
	 * @param node the JCR node from which to create the object
	 * @param childNodeFilter comma separated list of names of child nodes to load 
	 * ("*" loads all, while "none" loads no children)
	 * @param maxDepth the maximum depth of loaded child nodes (0 means no child nodes are loaded,
	 * while a negative value means that no restrictions are set on the depth).
	 * @return an instance of the JCR entity class, mapped from the node
	 * @throws JcrMappingException
	 */
	public <T> T fromNode(Class<T> entityClass, Node node, String childNodeFilter, int maxDepth) throws JcrMappingException {
        return fromNode(entityClass, node, new NodeFilter(childNodeFilter, maxDepth));
	}
    
	/**
	 * Maps the node supplied to an instance of the entity class.
	 * 
	 * @param entityClass the class of the entity to be instantiated from the node
	 * (in the case of dynamic instantiation, the instance class may be read from
	 * the document, but will be cast to this class)
	 * @param node the JCR node from which to create the object
	 * @param nodeFilter the NodeFilter to apply when loading child nodes and references
	 * @return an instance of the JCR entity class, mapped from the node
	 * @throws JcrMappingException
	 */
    public <T> T fromNode(Class<T> entityClass, Node node, NodeFilter nodeFilter ) throws JcrMappingException {
		if ( !mapper.isDynamicInstantiation() && !mapper.isMapped(entityClass) ) {
			throw new JcrMappingException("Trying to map to an unmapped class: " + entityClass.getName());
		}
		try {
			return (T) mapper.fromNode(entityClass, node, nodeFilter);
		} catch (ClassNotFoundException e) {
			throw new JcrMappingException("Could not map Object from node", e);
		} catch (InstantiationException e) {
			throw new JcrMappingException("Could not map Object from node", e);
		} catch (RepositoryException e) {
			throw new JcrMappingException("Could not map Object from node", e);
		} catch (IllegalAccessException e) {
			throw new JcrMappingException("Could not map Object from node", e);
		} catch (IOException e) {
			throw new JcrMappingException("Could not map Object from node", e);
		}
    }

	/**
	 * Maps the entity supplied to a JCR node, and adds that node as a child
	 * to the parent node supplied.
	 * 
	 * @param parentNode the parent node to which the entity node will be added
	 * @param entity the entity to be mapped to the JCR node
	 * @return the newly created JCR node
	 * @throws JcrMappingException
	 */
	public Node addNode(Node parentNode, Object entity) throws JcrMappingException {
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
	 * @throws JcrMappingException
	 */
	public Node addNode(Node parentNode, Object entity, String[] mixinTypes) throws JcrMappingException {
		if ( !mapper.isMapped(entity.getClass()) ) {
			throw new JcrMappingException("Trying to map an unmapped class: " + entity.getClass().getName());
		}
		try {
			return mapper.addNode(parentNode, entity, mixinTypes);
		} catch (RepositoryException e) {
			throw new JcrMappingException("Could not create node from object", e);
		} catch (IllegalAccessException e) {
			throw new JcrMappingException("Could not create node from object", e);
		} catch (IOException e) {
			throw new JcrMappingException("Could not create node from object", e);
		}
	}

	/**
	 * Update an existing JCR node with the entity supplied.
	 * 
	 * @param node the JCR node to be updated
	 * @param entity the entity that will be mapped to the existing node
	 * @return the name of the updated node
	 * @throws JcrMappingException
	 */
	public String updateNode(Node node, Object entity) throws JcrMappingException {
		return updateNode(node, entity, new NodeFilter("*", -1));
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
	 * @throws JcrMappingException
	 */
	public String updateNode(Node node, Object entity, String childNodeFilter, int maxDepth) throws JcrMappingException {
        return updateNode(node, entity, new NodeFilter(childNodeFilter, maxDepth));
    }
    
	/**
	 * Update an existing JCR node with the entity supplied.
	 * 
	 * @param node the JCR node to be updated
	 * @param entity the entity that will be mapped to the existing node
	 * @param nodeFilter the NodeFilter to apply when updating child nodes and references
	 * @return the name of the updated node
	 * @throws JcrMappingException
	 */
    public String updateNode(Node node, Object entity, NodeFilter nodeFilter ) throws JcrMappingException {
        
		if ( !mapper.isMapped(entity.getClass()) ) {
			throw new JcrMappingException("Trying to map an unmapped class: " + entity.getClass().getName());
		}
		try {
			return mapper.updateNode(node, entity, nodeFilter);
		} catch (RepositoryException e) {
			throw new JcrMappingException("Could not update node from object", e);
		} catch (IllegalAccessException e) {
			throw new JcrMappingException("Could not update node from object", e);
		} catch (IOException e) {
			throw new JcrMappingException("Could not update node from object", e);
		}
	}
}
