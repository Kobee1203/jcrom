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

/**
 * The base interface for JCR Data Access Objects.
 * Normally, the interface will be implemented for each root object being
 * mapped to a Java Content Repository (JCR).
 * It is intended that such implementations will manage the JCR root path
 * for the entity being mapped. JCR session strategy is also to be 
 * declared by implementations.
 * <br/><br/>
 * Implementations should encapsulate exceptions in JcrMappingException, which
 * is a RuntimeException.
 * 
 * @author Olafur Gauti Gudmundsson
 */
public interface JcrDAO<T> {
	
	/**
	 * Creates a new JCR Node from the object supplied.
	 * The path to the parent node will be retrieved from the entity path.
	 * A path of "/" means that the entity will be created on root level.
	 * 
	 * @param entity the object to be mapped to a JCR node
	 * @return the newly created Object
	 */
	public T create( T entity );
	
	/**
	 * Creates a new JCR Node from the object supplied, under the parent
	 * node with the path specified.
	 * 
	 * @param parentNodePath path to the parent node
	 * @param entity the object to be mapped to a JCR node
	 * @return the newly created Object
	 */
	public T create( String parentNodePath, T entity );
	
	/**
	 * Updates an existing JCR Node with the values extracted
	 * from the object supplied.
	 * 
	 * @param entity the object to be mapped to a JCR node
	 * @return the name of the JCR Node that was updated
	 */
	public String update( T entity );
		
	/**
	 * Updates an existing JCR Node with the values extracted
	 * from the object supplied.
	 * 
	 * @param entity the object to be mapped to a JCR node
	 * @param childNameFilter comma separated list of names of child nodes to 
	 * load ("*" loads all, "none" loads no children, and "-" at the beginning
	 * makes it an exclusion filter)
	 * @param maxDepth the maximum depth of loaded child nodes (0 means no 
	 * child nodes are loaded, while a negative value means that no 
	 * restrictions are set on the depth).
	 * @return the name of the JCR Node that was updated
	 */
	public String update( T entity, String childNameFilter, int maxDepth );
	
	/**
	 * Updates an existing JCR Node with the values extracted
	 * from the object supplied. The node to update is loaded using the 
	 * UUID supplied.
	 * 
	 * @param entity the object to be mapped to a JCR node
	 * @param uuid the JCR UUID of the node to update
	 * @return the name of the node that was updated
	 */
	public String updateByUUID( T entity, String uuid );
	
	/**
	 * Updates an existing JCR Node with the values extracted
	 * from the object supplied. The node to update is loaded using the
	 * UUID supplied.
	 * 
	 * @param entity the object to be mapped to a JCR node
	 * @param uuid the UUID of the node to update
	 * @param childNameFilter comma separated list of names of child nodes to 
	 * load ("*" loads all, "none" loads no children, and "-" at the beginning
	 * makes it an exclusion filter)
	 * @param maxDepth the maximum depth of loaded child nodes (0 means no 
	 * child nodes are loaded, while a negative value means that no 
	 * restrictions are set on the depth).
	 * @return the name of the JCR Node that was updated
	 */
	public String updateByUUID( T entity, String uuid, String childNameFilter, int maxDepth );
    
    /**
     * Move an entity to the parent with the path specified.
     * Handles checkout and checkin for mix:versionable parents.
     * 
     * @param entity
     * @param newParentPath
     */
    public void move( T entity, String newParentPath );
		
	/**
	 * Permanently remove the entity with the path supplied 
	 * (from a @JcrPath field).
	 * 
	 * @param path the full path of the entity
	 */
	public void remove( String path );
	
	/**
	 * Permanently remove the entity with the UUID supplied.
	 * 
	 * @param uuid the JCR UUID of the entity
	 */
	public void removeByUUID( String uuid );

	/**
	 * Check whether an entity with the path supplied exists in JCR.
	 * 
	 * @param path the path of the entity
	 * @return true if the entity exists, else false
	 */
	public boolean exists( String path );
		
	/**
	 * Get an entity from JCR by path (from a @JcrPath field).
	 * 
	 * @param path the full path of the entity to be loaded
	 * @return an object instance mapped from the JCR node with the path
	 * supplied, or null if no such node was found
	 */
	public T get( String path );
	
	/**
	 * Get an entity from JCR by path (from a @JcrPath field).
	 * 
	 * @param path the full path of the entity to be loaded
	 * @param childNameFilter comma separated list of names of child nodes to 
	 * load ("*" loads all, "none" loads no children, and "-" at the beginning
	 * makes it an exclusion filter)
	 * @param maxDepth the maximum depth of loaded child nodes (0 means no 
	 * child nodes are loaded, while a negative value means that no 
	 * restrictions are set on the depth).
	 * @return an object instance mapped from the JCR node with the path
	 * supplied, or null if no such node was found
	 */
	public T get( String path, String childNodeFilter, int maxDepth );
	
	/**
	 * Load an entity from JCR by UUID lookup.
	 * 
	 * @param uuid the UUID generated by JCR
	 * @return an object instance mapped from the JCR node with the uuid
	 */
	public T loadByUUID( String uuid );
	
	/**
	 * Load an entity from JCR by UUID lookup.
	 * 
	 * @param uuid the UUID generated by JCR
	 * @return an object instance mapped from the JCR node with the uuid
	 */
	public T loadByUUID( String uuid, String childNodeFilter, int maxDepth );
	
	public List<T> getVersionList( String path );
	public List<T> getVersionList( String path, String childNameFilter, int maxDepth );
	public List<T> getVersionList( String path, String childNameFilter, int maxDepth, long startIndex, long resultSize );
	
	public List<T> getVersionListByUUID( String uuid );
	public List<T> getVersionListByUUID( String uuid, String childNameFilter, int maxDepth );
	public List<T> getVersionListByUUID( String uuid, String childNameFilter, int maxDepth, long startIndex, long resultSize );
	
	public long getVersionSize( String path );
	public long getVersionSizeByUUID( String uuid );
	
	public T getVersion( String path, String versionName );
	public T getVersion( String path, String versionName, String childNodeFilter, int maxDepth );
	
	public T getVersionByUUID( String uuid, String versionName );
	public T getVersionByUUID( String uuid, String versionName, String childNodeFilter, int maxDepth );
	
	public void restoreVersion( String path, String versionName );
	public void restoreVersionByUUID( String uuid, String versionName );
	
	public void removeVersion( String path, String versionName );
	public void removeVersionByUUID( String uuid, String versionName );
	
	/**
	 * Get the number of entities.
	 * 
	 * @return the size of the list returned by findAll()
	 */
	public long getSize( String rootPath );
	
	/**
	 * Find all entities represented by this DAO.
	 * 
	 * @return all entities represented by this DAO
	 */
	public List<T> findAll( String rootPath );
	
	/**
	 * Find all entities represented by this DAO.
	 * Takes parameters that control the size and offset of the result.
	 * 
	 * @param startIndex the zero based index of the first item to return
	 * @param resultSize the number of items to return
	 * @return all entities represented by this DAO
	 */
	public List<T> findAll( String rootPath, long startIndex, long resultSize );
	
	/**
	 * Find all entities represented by this DAO.
	 * 
	 * @param childNameFilter comma separated list of names of child nodes to 
	 * load ("*" loads all, "none" loads no children, and "-" at the beginning
	 * makes it an exclusion filter)
	 * @param maxDepth the maximum depth of loaded child nodes (0 means no 
	 * child nodes are loaded, while a negative value means that no 
	 * restrictions are set on the depth).
	 * @return all entities represented by this DAO
	 */
	public List<T> findAll( String rootPath, String childNameFilter, int maxDepth );
	
	/**
	 * Find all entities represented by this DAO.
	 * Takes parameters that control the size and offset of the result, and
	 * filter which child nodes to load.
	 * 
	 * @param childNameFilter comma separated list of names of child nodes to 
	 * load ("*" loads all, "none" loads no children, and "-" at the beginning
	 * makes it an exclusion filter)
	 * @param maxDepth the maximum depth of loaded child nodes (0 means no 
	 * child nodes are loaded, while a negative value means that no 
	 * restrictions are set on the depth).
	 * @param startIndex the zero based index of the first item to return
	 * @param resultSize the number of items to return
	 * @return all entities represented by this DAO
	 */
	public List<T> findAll( String rootPath, String childNameFilter, int maxDepth, long startIndex, long resultSize );

}
