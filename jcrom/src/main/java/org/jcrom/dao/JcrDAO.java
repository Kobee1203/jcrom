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
 * @author Nicolas Dos Santos
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
    T create(T entity);

    /**
     * Creates a new JCR Node from the object supplied, under the parent
     * node with the path specified.
     * 
     * @param parentNodePath path to the parent node
     * @param entity the object to be mapped to a JCR node
     * @return the newly created Object
     */
    T create(String parentNodePath, T entity);

    /**
     * Updates an existing JCR Node with the values extracted
     * from the object supplied.
     * 
     * @param entity the object to be mapped to a JCR node
     * @return the name of the JCR Node that was updated
     */
    T update(T entity);

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
    T update(T entity, String childNameFilter, int maxDepth);

    /**
     * @deprecated This method is now deprecated. Use {@link #updateById(Object, String)} instead.
     * 
     * <p>
     * Updates an existing JCR Node with the values extracted
     * from the object supplied. The node to update is loaded using the 
     * UUID supplied.
     * </p>
     * 
     * @param entity the object to be mapped to a JCR node
     * @param uuid the JCR UUID of the node to update
     * @return the name of the node that was updated
     */
    @Deprecated
    T updateByUUID(T entity, String uuid);

    /**
     * Updates an existing JCR Node with the values extracted
     * from the object supplied. The node to update is loaded using the 
     * Identifier supplied.
     * 
     * @param entity the object to be mapped to a JCR node
     * @param id the Identifier of the node to update
     * @return the name of the node that was updated
     */
    T updateById(T entity, String id);

    /**
     * @deprecated This method is now deprecated. Use {@link #updateById(Object, String, String, int)} instead.
     * 
     * <p>
     * Updates an existing JCR Node with the values extracted
     * from the object supplied. The node to update is loaded using the
     * UUID supplied.
     * </p>
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
    @Deprecated
    T updateByUUID(T entity, String uuid, String childNameFilter, int maxDepth);

    /**
     * Updates an existing JCR Node with the values extracted
     * from the object supplied. The node to update is loaded using the
     * Identifier supplied.
     * 
     * @param entity the object to be mapped to a JCR node
     * @param id the Identifier of the node to update
     * @param childNameFilter comma separated list of names of child nodes to 
     * load ("*" loads all, "none" loads no children, and "-" at the beginning
     * makes it an exclusion filter)
     * @param maxDepth the maximum depth of loaded child nodes (0 means no 
     * child nodes are loaded, while a negative value means that no 
     * restrictions are set on the depth).
     * @return the name of the JCR Node that was updated
     */
    T updateById(T entity, String id, String childNameFilter, int maxDepth);

    /**
     * Move an entity to the parent with the path specified.
     * Handles checkout and checkin for mix:versionable parents.
     * 
     * @param entity
     * @param newParentPath
     */
    void move(T entity, String newParentPath);

    /**
     * Permanently remove the entity with the path supplied 
     * (from a @JcrPath field).
     * 
     * @param path the full path of the entity
     */
    void remove(String path);

    /**
     * @deprecated This method is now deprecated. Use {@link #removeById(String)} instead.
     * 
     * <p>Permanently remove the entity with the UUID supplied.</p>
     * 
     * @param uuid the JCR UUID of the entity
     */
    @Deprecated
    void removeByUUID(String uuid);

    /**
     * Permanently remove the entity with the Identifier supplied.
     * 
     * @param id the Identifier of the entity
     */
    void removeById(String id);

    /**
     * Check whether an entity with the path supplied exists in JCR.
     * 
     * @param path the path of the entity
     * @return true if the entity exists, else false
     */
    boolean exists(String path);

    /**
     * Get an entity from JCR by path (from a @JcrPath field).
     * 
     * @param path the full path of the entity to be loaded
     * @return an object instance mapped from the JCR node with the path
     * supplied, or null if no such node was found
     */
    T get(String path);

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
    T get(String path, String childNameFilter, int maxDepth);

    /**
     * Get all entities from JCR by path (from a @JcrPath field).
     * 
     * @param path the full path of the entities to be loaded
     * @return a list of object instances mapped from the JCR nodes with the path
     * supplied, or null if no such node was found
     */
    List<T> getAll(String path);

    /**
     * Get all entities from JCR by path (from a @JcrPath field).
     * Takes parameters that control the size and offset of the result.
     * 
     * @param startIndex the zero based index of the first item to return
     * @param resultSize the number of items to return
     * @return a list of object instances mapped from the JCR nodes with the path
     * supplied, or null if no such node was found
     */
    List<T> getAll(String path, long startIndex, long resultSize);

    /**
     * Get all entities from JCR by path (from a @JcrPath field).
     * 
     * @param path the full path of the entities to be loaded
     * @param childNameFilter comma separated list of names of child nodes to 
     * load ("*" loads all, "none" loads no children, and "-" at the beginning
     * makes it an exclusion filter)
     * @param maxDepth the maximum depth of loaded child nodes (0 means no 
     * child nodes are loaded, while a negative value means that no 
     * restrictions are set on the depth).
     * @return a list of object instances mapped from the JCR node with the path
     * supplied, or null if no such node was found
     */
    List<T> getAll(String path, String childNameFilter, int maxDepth);

    /**
     * Get all entities from JCR by path (from a @JcrPath field).
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
     * @return a list of object instances mapped from the JCR nodes with the path
     * supplied, or null if no such node was found
     */
    List<T> getAll(String path, String childNameFilter, int maxDepth, long startIndex, long resultSize);

    /**
     * @deprecated This method is now deprecated. Use {@link #loadById(String)} instead.
     * 
     * <p>Load an entity from JCR by UUID lookup.</p>
     * 
     * @param uuid the UUID generated by JCR
     * @return an object instance mapped from the JCR node with the uuid
     */
    @Deprecated
    T loadByUUID(String uuid);

    /**
     * Load an entity from JCR by Identifier lookup.
     * 
     * @param id the Identifier generated by JCR
     * @return an object instance mapped from the JCR node with the uuid
     */
    T loadById(String id);

    /**
     * @deprecated This method is now deprecated. Use {@link #loadById(String, String, int)} instead.
     * 
     * <p>Load an entity from JCR by UUID lookup.</p>
     * 
     * @param uuid the UUID generated by JCR
     * @return an object instance mapped from the JCR node with the uuid
     */
    @Deprecated
    T loadByUUID(String uuid, String childNameFilter, int maxDepth);

    /**
     * Load an entity from JCR by Identifier lookup.
     * 
     * @param id the Identifier generated by JCR
     * @return an object instance mapped from the JCR node with the id
     */
    T loadById(String id, String childNameFilter, int maxDepth);

    List<T> getVersionList(String path);

    List<T> getVersionList(String path, String childNameFilter, int maxDepth);

    List<T> getVersionList(String path, String childNameFilter, int maxDepth, long startIndex, long resultSize);

    /**
     * @deprecated This method is now deprecated. Use {@link #getVersionListById(String)} instead.
     */
    @Deprecated
    List<T> getVersionListByUUID(String uuid);

    List<T> getVersionListById(String id);

    /**
     * @deprecated This method is now deprecated. Use {@link #getVersionListById(String, String, int)} instead.
     */
    @Deprecated
    List<T> getVersionListByUUID(String uuid, String childNameFilter, int maxDepth);

    List<T> getVersionListById(String id, String childNameFilter, int maxDepth);

    /**
     * @deprecated This method is now deprecated. Use {@link #getVersionListById(String, String, int, long, long)} instead.
     */
    @Deprecated
    List<T> getVersionListByUUID(String uuid, String childNameFilter, int maxDepth, long startIndex, long resultSize);

    List<T> getVersionListById(String id, String childNameFilter, int maxDepth, long startIndex, long resultSize);

    long getVersionSize(String path);

    /**
     * @deprecated This method is now deprecated. Use {@link #getVersionSizeById(String)} instead.
     */
    @Deprecated
    long getVersionSizeByUUID(String uuid);

    long getVersionSizeById(String id);

    T getVersion(String path, String versionName);

    T getVersion(String path, String versionName, String childNameFilter, int maxDepth);

    /**
     * @deprecated This method is now deprecated. Use {@link #getVersionById(String, String)} instead.
     */
    @Deprecated
    T getVersionByUUID(String uuid, String versionName);

    T getVersionById(String id, String versionName);

    /**
     * @deprecated This method is now deprecated. Use {@link #getVersionById(String, String, String, int)} instead.
     */
    @Deprecated
    T getVersionByUUID(String uuid, String versionName, String childNameFilter, int maxDepth);

    T getVersionById(String id, String versionName, String childNameFilter, int maxDepth);

    void restoreVersion(String path, String versionName);

    /**
     * @deprecated This method is now deprecated. Use {@link #restoreVersionById(String, String)} instead.
     */
    @Deprecated
    void restoreVersionByUUID(String uuid, String versionName);

    void restoreVersionById(String id, String versionName);

    void restoreVersion(String path, String versionName, boolean removeExisting);

    /**
     * @deprecated This method is now deprecated. Use {@link #restoreVersionById(String, String, boolean)} instead.
     */
    @Deprecated
    void restoreVersionByUUID(String uuid, String versionName, boolean removeExisting);

    void restoreVersionById(String id, String versionName, boolean removeExisting);

    void removeVersion(String path, String versionName);

    /**
     * @deprecated This method is now deprecated. Use {@link #removeVersionById(String, String)} instead.
     */
    @Deprecated
    void removeVersionByUUID(String uuid, String versionName);

    void removeVersionById(String id, String versionName);

    /**
     * Get the number of entities.
     * 
     * @return the size of the list returned by findAll()
     */
    long getSize(String rootPath);

    /**
     * Find all entities represented by this DAO.
     * 
     * @return all entities represented by this DAO
     */
    List<T> findAll(String rootPath);

    /**
     * Find all entities represented by this DAO.
     * Takes parameters that control the size and offset of the result.
     * 
     * @param startIndex the zero based index of the first item to return
     * @param resultSize the number of items to return
     * @return all entities represented by this DAO
     */
    List<T> findAll(String rootPath, long startIndex, long resultSize);

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
    List<T> findAll(String rootPath, String childNameFilter, int maxDepth);

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
    List<T> findAll(String rootPath, String childNameFilter, int maxDepth, long startIndex, long resultSize);

}
