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
package org.jcrom.dao;

import java.util.List;
import javax.jcr.Node;
import org.jcrom.JcrEntity;

/**
 * The base interface for JCR Data Access Objects.
 * Normally, the interface will be implemented for each root object being
 * mapped to a Java Content Repository (JCR).
 * It is intended that such implementations will manage the JCR root path
 * for the entity being mapped. JCR session strategy is also to be 
 * declared by implementations.
 * 
 * @author Olafur Gauti Gudmundsson
 */
public interface JcrDAO<T extends JcrEntity> {
	
	/**
	 * Creates a new JCR Node from the object supplied.
	 * 
	 * @param entity the object to be mapped to a JCR node
	 * @return the newly created Node
	 * @throws java.lang.Exception
	 */
	public Node create( T entity ) throws Exception;
	
	/**
	 * Updates an existing JCR Node with the values extracted
	 * from the object supplied.
	 * 
	 * @param entity the object to be mapped to a JCR node
	 * @return the name of the JCR Node that was updated
	 * @throws java.lang.Exception
	 */
	public String update( T entity ) throws Exception;
	
	/**
	 * Updates an existing JCR Node with the values extracted
	 * from the object supplied.
	 * 
	 * @param entity the object to be mapped to a JCR node
	 * @param childNodeFilter comma separated list of names of child nodes to 
	 * load ("*" loads all, "none" loads no children, and "-" at the beginning
	 * makes it an exclusion filter)
	 * @param maxDepth the maximum depth of loaded child nodes (0 means no 
	 * child nodes are loaded, while a negative value means that no 
	 * restrictions are set on the depth).
	 * @return the name of the JCR Node that was updated
	 * @throws java.lang.Exception
	 */
	public String update( T entity, String childNodeFilter, int maxDepth ) throws Exception;
	
	/**
	 * Permanently delete the entity with the name supplied.
	 * 
	 * @param name the name of the entity
	 * @throws java.lang.Exception
	 */
	public void delete( String name ) throws Exception;

	/**
	 * Check whether an entity with the name supplied exists in JCR.
	 * 
	 * @param name the name of the entity
	 * @return true if the entity exists, else false
	 * @throws java.lang.Exception
	 */
	public boolean exists( String name ) throws Exception;
	
	/**
	 * Get an entity from JCR.
	 * 
	 * @param name the name of the entity to be loaded
	 * @return an object instance mapped from the JCR node with the name
	 * supplied, or null if no such node was found
	 * @throws java.lang.Exception
	 */
	public T get( String name ) throws Exception;
	
	/**
	 * Get an entity from JCR.
	 * 
	 * @param name the name of the entity to be loaded
	 * @param childNodeFilter comma separated list of names of child nodes to 
	 * load ("*" loads all, "none" loads no children, and "-" at the beginning
	 * makes it an exclusion filter)
	 * @param maxDepth the maximum depth of loaded child nodes (0 means no 
	 * child nodes are loaded, while a negative value means that no 
	 * restrictions are set on the depth).
	 * @return an object instance mapped from the JCR node with the name
	 * supplied, or null if no such node was found
	 * @throws java.lang.Exception
	 */
	public T get( String name, String childNodeFilter, int maxDepth ) throws Exception;
	
	/**
	 * Find all entities represented by this DAO.
	 * 
	 * @return all entities represented by this DAO
	 * @throws java.lang.Exception
	 */
	public List<T> findAll() throws Exception;
	
	/**
	 * Find all entities represented by this DAO.
	 * 
	 * @param childNodeFilter comma separated list of names of child nodes to 
	 * load ("*" loads all, "none" loads no children, and "-" at the beginning
	 * makes it an exclusion filter)
	 * @param maxDepth the maximum depth of loaded child nodes (0 means no 
	 * child nodes are loaded, while a negative value means that no 
	 * restrictions are set on the depth).
	 * @return all entities represented by this DAO
	 * @throws java.lang.Exception
	 */
	public List<T> findAll( String childNameFilter, int maxDepth ) throws Exception;

}
