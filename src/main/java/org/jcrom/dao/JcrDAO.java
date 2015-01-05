/**
 * This file is part of the JCROM project.
 * Copyright (C) 2008-2015 - All rights reserved.
 * Authors: Olafur Gauti Gudmundsson, Nicolas Dos Santos
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
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

import org.jcrom.annotations.JcrNode;
import org.jcrom.callback.JcromCallback;
import org.jcrom.util.NodeFilter;

/**
 * The base interface for JCR Data Access Objects.
 * Normally, the interface will be implemented for each root object being mapped to a Java Content Repository (JCR).
 * It is intended that such implementations will manage the JCR root path for the entity being mapped.
 * JCR session strategy is also to be declared by implementations.
 * <br/><br/>
 * Implementations should encapsulate exceptions in JcrMappingException, which is a RuntimeException.
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
     * Creates a new JCR Node from the object supplied.
     * The path to the parent node will be retrieved from the entity path.
     * A path of "/" means that the entity will be created on root level.
     * 
     * @param entity the object to be mapped to a JCR node
     * @param action callback object that specifies the Jcrom actions: 
     *     <ul>
     *       <li>{@link JcromCallback#doAddNode(Node, String, JcrNode, Object)},</li>
     *       <li>{@link JcromCallback#doAddMixinTypes(Node, String[], JcrNode, Object)},</li>
     *       <li>{@link JcromCallback#doAddClassNameToProperty(Node, JcrNode, Object)},</li>
     *       <li>{@link JcromCallback#doComplete(Object, Node)},</li>
     *     </ul>
     * @return the newly created Object
     */
    T create(T entity, JcromCallback action);

    /**
     * Creates a new JCR Node from the object supplied, under the parent node with the path specified.
     * 
     * @param parentNodePath path to the parent node
     * @param entity the object to be mapped to a JCR node
     * @return the newly created Object
     */
    T create(String parentNodePath, T entity);

    /**
     * Creates a new JCR Node from the object supplied, under the parent node with the path specified.
     * 
     * @param parentNodePath path to the parent node
     * @param entity the object to be mapped to a JCR node
     * @param action callback object that specifies the Jcrom actions: 
     *     <ul>
     *       <li>{@link JcromCallback#doAddNode(Node, String, JcrNode, Object)},</li>
     *       <li>{@link JcromCallback#doAddMixinTypes(Node, String[], JcrNode, Object)},</li>
     *       <li>{@link JcromCallback#doAddClassNameToProperty(Node, JcrNode, Object)},</li>
     *       <li>{@link JcromCallback#doComplete(Object, Node)},</li>
     *     </ul>
     * @return the newly created Object
     * @since 2.1.0
     */
    T create(String parentNodePath, T entity, JcromCallback action);

    /**
     * Updates an existing JCR Node with the values extracted from the object supplied.
     * 
     * @param entity the object to be mapped to a JCR node
     * @return the updated Object
     */
    T update(T entity);

    /**
     * Updates an existing JCR Node with the values extracted from the object supplied.
     * 
     * @param entity the object to be mapped to a JCR node
     * @param action callback object that specifies the Jcrom actions: 
     *     <ul>
     *       <li>{@link JcromCallback#doUpdateClassNameToProperty(Node, JcrNode, Object)},</li>
     *       <li>{@link JcromCallback#doMoveNode(Node, Node, String, JcrNode, Object)},</li>
     *       <li>{@link JcromCallback#doComplete(Object, Node)},</li>
     *     </ul>
     * @return the updated Object
     * @since 2.1.0
     */
    T update(T entity, JcromCallback action);

    /**
     * @deprecated This method is now deprecated. Use {@link #update(Object, NodeFilter)} instead.
     * 
     * <p>Updates an existing JCR Node with the values extracted from the object supplied.</p> 
     * 
     * @param entity the object to be mapped to a JCR node
     * @param childNameFilter comma separated list of names of child nodes to load ("*" loads all, "none" loads no children, and "-" at the beginning makes it an exclusion filter)
     * @param maxDepth the maximum depth of loaded child nodes (0 means no child nodes are loaded, while a negative value means that no restrictions are set on the depth).
     * @return the updated Object
     */
    @Deprecated
    T update(T entity, String childNameFilter, int maxDepth);

    /**
     * Update an existing JCR node with the entity supplied.
     * 
     * @param entity the entity that will be mapped to the existing node
     * @param nodeFilter the NodeFilter to apply when updating child nodes and references
     * @return the updated Object
     * @since 2.1.0
     */
    T update(T entity, NodeFilter nodeFilter);

    /**
     * Update an existing JCR node with the entity supplied.
     * 
     * @param entity the entity that will be mapped to the existing node
     * @param nodeFilter the NodeFilter to apply when updating child nodes and references
     * @param action callback object that specifies the Jcrom actions: 
     *     <ul>
     *       <li>{@link JcromCallback#doUpdateClassNameToProperty(Node, JcrNode, Object)},</li>
     *       <li>{@link JcromCallback#doMoveNode(Node, Node, String, JcrNode, Object)},</li>
     *       <li>{@link JcromCallback#doComplete(Object, Node)},</li>
     *     </ul>
     * @return the updated Object
     */
    T update(T entity, NodeFilter nodeFilter, JcromCallback action);

    /**
     * @deprecated This method is now deprecated. Use {@link #updateById(Object, String)} instead.
     * 
     * <p>
     * Updates an existing JCR Node with the values extracted from the object supplied.
     * The node to update is loaded using the UUID supplied.
     * </p>
     * 
     * @param entity the object to be mapped to a JCR node
     * @param uuid the JCR UUID of the node to update
     * @return the updated Object
     */
    @Deprecated
    T updateByUUID(T entity, String uuid);

    /**
     * Updates an existing JCR Node with the values extracted from the object supplied.
     * The node to update is loaded using the Identifier supplied.
     * 
     * @param entity the object to be mapped to a JCR node
     * @param id the Identifier of the node to update
     * @return the updated Object
     */
    T updateById(T entity, String id);

    /**
     * Updates an existing JCR Node with the values extracted from the object supplied.
     * The node to update is loaded using the Identifier supplied.
     * 
     * @param entity the object to be mapped to a JCR node
     * @param id the Identifier of the node to update
     * @return the updated Object
     * @since 2.1.0
     */
    T updateById(T entity, String id, JcromCallback action);

    /**
     * @deprecated This method is now deprecated. Use {@link #updateById(Object, String, NodeFilter)} instead.
     * 
     * <p>
     * Updates an existing JCR Node with the values extracted from the object supplied.
     * The node to update is loaded using the UUID supplied.
     * </p>
     * 
     * @param entity the object to be mapped to a JCR node
     * @param uuid the UUID of the node to update
     * @param childNameFilter comma separated list of names of child nodes to load ("*" loads all, "none" loads no children, and "-" at the beginning makes it an exclusion filter)
     * @param maxDepth the maximum depth of loaded child nodes (0 means no child nodes are loaded, while a negative value means that no restrictions are set on the depth).
     * @return the updated Object
     */
    @Deprecated
    T updateByUUID(T entity, String uuid, String childNameFilter, int maxDepth);

    /**
     * @deprecated This method is now deprecated. Use {@link #updateById(Object, String, NodeFilter)} instead.
     * 
     * <p>
     * Updates an existing JCR Node with the values extracted from the object supplied.
     * The node to update is loaded using the Identifier supplied.
     * </p>
     * 
     * @param entity the object to be mapped to a JCR node
     * @param id the Identifier of the node to update
     * @param childNameFilter comma separated list of names of child nodes to load ("*" loads all, "none" loads no children, and "-" at the beginning makes it an exclusion filter)
     * @param maxDepth the maximum depth of loaded child nodes (0 means no child nodes are loaded, while a negative value means that no restrictions are set on the depth).
     * @return the updated Object
     */
    @Deprecated
    T updateById(T entity, String id, String childNameFilter, int maxDepth);

    /**
     * Updates an existing JCR Node with the values extracted from the object supplied.
     * The node to update is loaded using the Identifier supplied.
     * 
     * @param entity the object to be mapped to a JCR node
     * @param id the Identifier of the node to update
     * @param nodeFilter the NodeFilter to apply when updating child nodes and references
     * @return the updated Object
     * @since 2.1.0
     */
    T updateById(T entity, String id, NodeFilter nodeFilter);

    /**
     * Updates an existing JCR Node with the values extracted from the object supplied.
     * The node to update is loaded using the Identifier supplied.
     * 
     * @param entity the object to be mapped to a JCR node
     * @param id the Identifier of the node to update
     * @param nodeFilter the NodeFilter to apply when updating child nodes and references
     * @param action callback object that specifies the Jcrom actions: 
     *     <ul>
     *       <li>{@link JcromCallback#doUpdateClassNameToProperty(Node, JcrNode, Object)},</li>
     *       <li>{@link JcromCallback#doMoveNode(Node, Node, String, JcrNode, Object)},</li>
     *       <li>{@link JcromCallback#doComplete(Object, Node)},</li>
     *     </ul>
     * @return the updated Object
     */
    T updateById(T entity, String id, NodeFilter nodeFilter, JcromCallback action);

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
     * @return an object instance mapped from the JCR node with the path supplied, or null if no such node was found
     */
    T get(String path);

    /**
     * @deprecated This method is now deprecated. Use {@link #get(String, NodeFilter)} instead.
     * 
     * <p>Get an entity from JCR by path (from a @JcrPath field).</p>
     * 
     * @param path the full path of the entity to be loaded
     * @param childNameFilter comma separated list of names of child nodes to load ("*" loads all, "none" loads no children, and "-" at the beginning makes it an exclusion filter)
     * @param maxDepth the maximum depth of loaded child nodes (0 means no child nodes are loaded, while a negative value means that no restrictions are set on the depth).
     * @return an object instance mapped from the JCR node with the path supplied, or null if no such node was found
     */
    @Deprecated
    T get(String path, String childNameFilter, int maxDepth);

    /**
     * Get an entity from JCR by path (from a @JcrPath field).
     * 
     * @param path path the full path of the entity to be loaded
     * @param nodeFilter the NodeFilter to apply when loading child nodes and references
     * @return an object instance mapped from the JCR node with the path supplied, or null if no such node was found
     * @since 2.1.0
     */
    T get(String path, NodeFilter nodeFilter);

    /**
     * Get all entities from JCR by path (from a @JcrPath field).
     * 
     * @param path the full path of the entities to be loaded
     * @return a list of object instances mapped from the JCR nodes with the path supplied, or null if no such node was found
     */
    List<T> getAll(String path);

    /**
     * Get all entities from JCR by path (from a @JcrPath field).
     * Takes parameters that control the size and offset of the result.
     * 
     * @param startIndex the zero based index of the first item to return
     * @param resultSize the number of items to return
     * @return a list of object instances mapped from the JCR nodes with the path supplied, or null if no such node was found
     */
    List<T> getAll(String path, long startIndex, long resultSize);

    /**
     * @deprecated This method is now deprecated. Use {@link #getAll(String, NodeFilter)} instead.
     * 
     * <p>Get all entities from JCR by path (from a @JcrPath field).</p>
     * 
     * @param path the full path of the entities to be loaded
     * @param childNameFilter comma separated list of names of child nodes to load ("*" loads all, "none" loads no children, and "-" at the beginning makes it an exclusion filter)
     * @param maxDepth the maximum depth of loaded child nodes (0 means no child nodes are loaded, while a negative value means that no restrictions are set on the depth).
     * @return a list of object instances mapped from the JCR node with the path supplied, or null if no such node was found
     */
    @Deprecated
    List<T> getAll(String path, String childNameFilter, int maxDepth);

    /**
     * Get all entities from JCR by path (from a @JcrPath field).
     * 
     * @param path the full path of the entities to be loaded
     * @param nodeFilter the NodeFilter to apply when loading child nodes and references
     * @return a list of object instances mapped from the JCR node with the path supplied, or null if no such node was found
     * @since 2.1.0
     */
    List<T> getAll(String path, NodeFilter nodeFilter);

    /**
     * @deprecated This method is now deprecated. Use {@link #getAll(String, NodeFilter, long, long)} instead.
     * 
     * <p>
     * Get all entities from JCR by path (from a @JcrPath field).
     * Takes parameters that control the size and offset of the result, and filter which child nodes to load.
     * </p>
     * 
     * @param path the full path of the entities to be loaded
     * @param childNameFilter comma separated list of names of child nodes to load ("*" loads all, "none" loads no children, and "-" at the beginning makes it an exclusion filter)
     * @param maxDepth the maximum depth of loaded child nodes (0 means no child nodes are loaded, while a negative value means that no restrictions are set on the depth).
     * @param startIndex the zero based index of the first item to return
     * @param resultSize the number of items to return
     * @return a list of object instances mapped from the JCR nodes with the path supplied, or null if no such node was found
     */
    @Deprecated
    List<T> getAll(String path, String childNameFilter, int maxDepth, long startIndex, long resultSize);

    /**
     * Get all entities from JCR by path (from a @JcrPath field).
     * Takes parameters that control the size and offset of the result, and filter which child nodes to load.
     * 
     * @param path the full path of the entities to be loaded
     * @param nodeFilter the NodeFilter to apply when loading child nodes and references
     * @param startIndex the zero based index of the first item to return
     * @param resultSize the number of items to return
     * @return a list of object instances mapped from the JCR nodes with the path supplied, or null if no such node was found
     * @since 2.1.0
     */
    List<T> getAll(String path, NodeFilter nodeFilter, long startIndex, long resultSize);

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
     * @deprecated This method is now deprecated. Use {@link #loadById(String, NodeFilter)} instead.
     * 
     * <p>Load an entity from JCR by UUID lookup.</p>
     * 
     * @param uuid the UUID generated by JCR
     * @param childNameFilter comma separated list of names of child nodes to load ("*" loads all, "none" loads no children, and "-" at the beginning makes it an exclusion filter)
     * @param maxDepth the maximum depth of loaded child nodes (0 means no child nodes are loaded, while a negative value means that no restrictions are set on the depth).
     * @return an object instance mapped from the JCR node with the uuid
     */
    @Deprecated
    T loadByUUID(String uuid, String childNameFilter, int maxDepth);

    /**
     * @deprecated This method is now deprecated. Use {@link #loadById(String, NodeFilter)} instead.
     * 
     * <p>Load an entity from JCR by UUID lookup.</p>
     * 
     * @param id the Identifier generated by JCR
     * @return an object instance mapped from the JCR node with the id
     */
    @Deprecated
    T loadById(String id, String childNameFilter, int maxDepth);

    /**
     * Load an entity from JCR by UUID lookup.
     * 
     * @param id the Identifier generated by JCR
     * @param nodeFilter the NodeFilter to apply when loading child nodes and references
     * @return an object instance mapped from the JCR node with the id
     * @since 2.1.0
     */
    T loadById(String id, NodeFilter nodeFilter);

    /**
     * Get all versions by path.
     * 
     * @param path the full path of the entity to be loaded
     * @return a list of object instances mapped from the JCR nodes with the path supplied, or null if no such node was found
     */
    List<T> getVersionList(String path);

    /**
     * @deprecated This method is now deprecated. Use {@link #getVersionList(String, NodeFilter)} instead.
     */
    @Deprecated
    List<T> getVersionList(String path, String childNameFilter, int maxDepth);

    /**
     * Get all versions by path.
     * 
     * @param path the full path of the entity to be loaded
     * @param nodeFilter the NodeFilter to apply when loading child nodes and references
     * @return a list of object instances mapped from the JCR nodes with the path supplied, or null if no such node was found
     * @since 2.1.0
     */
    List<T> getVersionList(String path, NodeFilter nodeFilter);

    /**
     * @deprecated This method is now deprecated. Use {@link #getVersionList(String, NodeFilter, long, long)} instead.
     */
    @Deprecated
    List<T> getVersionList(String path, String childNameFilter, int maxDepth, long startIndex, long resultSize);

    /**
     * Get all versions by path.
     * Takes parameters that control the size and offset of the result, and filter which child nodes to load.
     * 
     * @param path the full path of the entity to be loaded
     * @param nodeFilter the NodeFilter to apply when loading child nodes and references
     * @param startIndex the zero based index of the first item to return
     * @param resultSize the number of items to return
     * @return a list of object instances mapped from the JCR nodes with the path supplied, or null if no such node was found
     * @since 2.1.0
     */
    List<T> getVersionList(String path, NodeFilter nodeFilter, long startIndex, long resultSize);

    /**
     * @deprecated This method is now deprecated. Use {@link #getVersionListById(String)} instead.
     */
    @Deprecated
    List<T> getVersionListByUUID(String uuid);

    /**
     * Get all versions by Identifier lookup.
     * 
     * @param id the Identifier generated by JCR
     * @return a list of object instances mapped from the JCR nodes with the id supplied, or null if no such node was found
     */
    List<T> getVersionListById(String id);

    /**
     * @deprecated This method is now deprecated. Use {@link #getVersionListById(String, NodeFilter)} instead.
     */
    @Deprecated
    List<T> getVersionListByUUID(String uuid, String childNameFilter, int maxDepth);

    /**
     * @deprecated This method is now deprecated. Use {@link #getVersionListById(String, NodeFilter)} instead.
     */
    @Deprecated
    List<T> getVersionListById(String id, String childNameFilter, int maxDepth);

    /**
     * Get all versions by Identifier lookup.
     * 
     * @param id the Identifier generated by JCR
     * @param nodeFilter the NodeFilter to apply when loading child nodes and references
     * @return a list of object instances mapped from the JCR nodes with the id supplied, or null if no such node was found
     * @since 2.1.0
     */
    List<T> getVersionListById(String id, NodeFilter nodeFilter);

    /**
     * @deprecated This method is now deprecated. Use {@link #getVersionListById(String, NodeFilter, long, long)} instead.
     */
    @Deprecated
    List<T> getVersionListByUUID(String uuid, String childNameFilter, int maxDepth, long startIndex, long resultSize);

    /**
     * @deprecated This method is now deprecated. Use {@link #getVersionListById(String, NodeFilter, long, long)} instead.
     */
    @Deprecated
    List<T> getVersionListById(String id, String childNameFilter, int maxDepth, long startIndex, long resultSize);

    /**
     * Get all versions by Identifier lookup.
     * Takes parameters that control the size and offset of the result, and filter which child nodes to load.
     * 
     * @param id the Identifier generated by JCR
     * @param nodeFilter the NodeFilter to apply when loading child nodes and references
     * @param startIndex the zero based index of the first item to return
     * @param resultSize the number of items to return
     * @return a list of object instances mapped from the JCR nodes with the id supplied, or null if no such node was found
     * @since 2.1.0
     */
    List<T> getVersionListById(String id, NodeFilter nodeFilter, long startIndex, long resultSize);

    /**
     * Get the total number of versions by path.
     * 
     * @param path the full path of the entity to be loaded
     * @return the total number of versions
     */
    long getVersionSize(String path);

    /**
     * @deprecated This method is now deprecated. Use {@link #getVersionSizeById(String)} instead.
     */
    @Deprecated
    long getVersionSizeByUUID(String uuid);

    /**
     * Get the total number of versions by Identifier lookup.
     * 
     * @param id the Identifier generated by JCR
     * @return the total number of versions
     */
    long getVersionSizeById(String id);

    /**
     * Get one specific version by path and version name.
     * Takes parameters that control the size and offset of the result, and filter which child nodes to load.
     * 
     * @param path the full path of the entity to be loaded
     * @param versionName a version name
     * @return an object instance mapped from the JCR node with the path supplied, or null if no such node was found
     */
    T getVersion(String path, String versionName);

    /**
     * @deprecated This method is now deprecated. Use {@link #getVersion(String, String, NodeFilter)} instead.
     */
    @Deprecated
    T getVersion(String path, String versionName, String childNameFilter, int maxDepth);

    /**
     * Get one specific version by path and version name.
     * Takes parameters that control the size and offset of the result, and filter which child nodes to load.
     * 
     * @param path the full path of the entity to be loaded
     * @param versionName a version name
     * @param nodeFilter the NodeFilter to apply when loading child nodes and references
     * @return an object instance mapped from the JCR node with the path supplied, or null if no such node was found
     * @since 2.1.0
     */
    T getVersion(String path, String versionName, NodeFilter nodeFilter);

    /**
     * @deprecated This method is now deprecated. Use {@link #getVersionById(String, String)} instead.
     */
    @Deprecated
    T getVersionByUUID(String uuid, String versionName);

    /**
     * Get one specific version by Identifier and version name.
     * Takes parameters that control the size and offset of the result, and filter which child nodes to load.
     * 
     * @param id the Identifier generated by JCR
     * @param versionName a version name
     * @return an object instance mapped from the JCR node with the path supplied, or null if no such node was found
     */
    T getVersionById(String id, String versionName);

    /**
     * @deprecated This method is now deprecated. Use {@link #getVersionById(String, String, NodeFilter)} instead.
     */
    @Deprecated
    T getVersionByUUID(String uuid, String versionName, String childNameFilter, int maxDepth);

    /**
     * @deprecated This method is now deprecated. Use {@link #getVersionById(String, String, NodeFilter)} instead.
     */
    @Deprecated
    T getVersionById(String id, String versionName, String childNameFilter, int maxDepth);

    /**
     * Get one specific version by Identifier and version name.
     * Takes parameters that control the size and offset of the result, and filter which child nodes to load.
     * 
     * @param id the Identifier generated by JCR
     * @param versionName a version name
     * @param nodeFilter the NodeFilter to apply when loading child nodes and references
     * @return an object instance mapped from the JCR node with the path supplied, or null if no such node was found
     * @since 2.1.0
     */
    T getVersionById(String id, String versionName, NodeFilter nodeFilter);

    /**
     * Restore a specific version by path and version name.
     * 
     * @param path the full path of the entity to be loaded
     * @param versionName a version name
     */
    void restoreVersion(String path, String versionName);

    /**
     * @deprecated This method is now deprecated. Use {@link #restoreVersionById(String, String)} instead.
     */
    @Deprecated
    void restoreVersionByUUID(String uuid, String versionName);

    /**
     * Restore a specific version by id and version name.
     * 
     * @param id the Identifier generated by JCR
     * @param versionName a version name
     */
    void restoreVersionById(String id, String versionName);

    /**
     * Restore a specific version by path and version name.
     * 
     * @param path the full path of the entity to be loaded
     * @param versionName a version name
     * @param removeExisting a boolean flag that governs what happens in case of an identifier collision.
     */
    void restoreVersion(String path, String versionName, boolean removeExisting);

    /**
     * @deprecated This method is now deprecated. Use {@link #restoreVersionById(String, String, boolean)} instead.
     */
    @Deprecated
    void restoreVersionByUUID(String uuid, String versionName, boolean removeExisting);

    /**
     * Restore a specific version by id and version name.
     * 
     * @param id the Identifier generated by JCR
     * @param versionName a version name
     * @param removeExisting a boolean flag that governs what happens in case of an identifier collision.
     */
    void restoreVersionById(String id, String versionName, boolean removeExisting);

    /**
     * Remove a specific version by path and version name.
     * 
     * @param path the full path of the entity to be loaded
     * @param versionName a version name
     */
    void removeVersion(String path, String versionName);

    /**
     * @deprecated This method is now deprecated. Use {@link #removeVersionById(String, String)} instead.
     */
    @Deprecated
    void removeVersionByUUID(String uuid, String versionName);

    /**
     * Remove a specific version by id and version name.
     * 
     * @param id the Identifier generated by JCR
     * @param versionName a version name
     */
    void removeVersionById(String id, String versionName);

    /**
     * Get the number of entities.
     * 
     * @param rootPath root path of the found entities
     * @return the size of the list returned by findAll()
     */
    long getSize(String rootPath);

    /**
     * Find all entities represented by this DAO.
     * 
     * @param rootPath root path of the found entities
     * @return all entities represented by this DAO
     */
    List<T> findAll(String rootPath);

    /**
     * Find all entities represented by this DAO.
     * Takes parameters that control the size and offset of the result.
     * 
     * @param rootPath root path of the found entities
     * @param startIndex the zero based index of the first item to return
     * @param resultSize the number of items to return
     * @return all entities represented by this DAO
     */
    List<T> findAll(String rootPath, long startIndex, long resultSize);

    /**
     * @deprecated This method is now deprecated. Use {@link #findAll(String, NodeFilter)} instead.
     * 
     * <p>Find all entities represented by this DAO.</p>
     * 
     * @param rootPath root path of the found entities
     * @param childNameFilter comma separated list of names of child nodes to load ("*" loads all, "none" loads no children, and "-" at the beginning makes it an exclusion filter)
     * @param maxDepth the maximum depth of loaded child nodes (0 means no child nodes are loaded, while a negative value means that no restrictions are set on the depth).
     * @return all entities represented by this DAO
     */
    @Deprecated
    List<T> findAll(String rootPath, String childNameFilter, int maxDepth);

    /**
     * Find all entities represented by this DAO.
     * 
     * @param rootPath root path of the found entities
     * @param nodeFilter the NodeFilter to apply when loading child nodes and references
     * @return all entities represented by this DAO
     * @since 2.1.0
     */
    List<T> findAll(String rootPath, NodeFilter nodeFilter);

    /**
     * @deprecated This method is now deprecated. Use {@link #findAll(String, NodeFilter, long, long)} instead.
     * 
     * <p>
     * Find all entities represented by this DAO.
     * Takes parameters that control the size and offset of the result, and filter which child nodes to load.
     * </p>
     * 
     * @param rootPath root path of the found entities
     * @param childNameFilter comma separated list of names of child nodes to load ("*" loads all, "none" loads no children, and "-" at the beginning makes it an exclusion filter)
     * @param maxDepth the maximum depth of loaded child nodes (0 means no child nodes are loaded, while a negative value means that no restrictions are set on the depth).
     * @param startIndex the zero based index of the first item to return
     * @param resultSize the number of items to return
     * @return all entities represented by this DAO
     */
    @Deprecated
    List<T> findAll(String rootPath, String childNameFilter, int maxDepth, long startIndex, long resultSize);

    /**
     * Find all entities represented by this DAO.
     * Takes parameters that control the size and offset of the result, and filter which child nodes to load.
     * 
     * @param rootPath root path of the found entities
     * @param nodeFilter the NodeFilter to apply when loading child nodes and references
     * @param startIndex the zero based index of the first item to return
     * @param resultSize the number of items to return
     * @return all entities represented by this DAO
     * @since 2.1.0
     */
    List<T> findAll(String rootPath, NodeFilter nodeFilter, long startIndex, long resultSize);

}
