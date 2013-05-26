/**
 * Copyright (C) 2008-2013 by JCROM Authors (Olafur Gauti Gudmundsson, Nicolas Dos Santos) - All rights reserved.
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
package org.jcrom.callback;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.jcrom.JcrMappingException;
import org.jcrom.Jcrom;
import org.jcrom.annotations.JcrNode;

/**
 * The interface for Jcrom callbacks.<br/>
 * This interface defines the methods to be implemented that can change the default actions to 
 * add a node to update a node, add mixin types...<br/>
 * It is recommended to extend the default implementation, {@link DefaultJcromCallback}, in order to only override the methods to modify.<br/>
 * For example:<br/>
 * <pre>
 * jcrom.addNode(rootNode, parent, null, new DefaultJcromCallback(jcrom) {
 *     {@literal @Override}
 *     public Node doAddNode(Node parentNode, String nodeName, JcrNode jcrNode, Object entity) throws RepositoryException {
 *         if (!(parentNode instanceof NodeImpl) && !(entity instanceof Parent)) {
 *             return super.doAddNode(parentNode, nodeName, jcrNode, entity);
 *         }
 *         NodeImpl parentNodeImpl = (NodeImpl) parentNode;
 *         Parent parentEntity = (Parent) entity;
 *         return parentNodeImpl.addNodeWithUuid(nodeName, parentEntity.getUuid());
 *     }
 * });
 * </pre>
 * 
 * @author Nicolas Dos Santos
 */
public interface JcromCallback {

    /**
     * Method invoked when a new node is added.
     * 
     * @param parentNode parent {@link Node}
     * @param nodeName node name, cleaned if enabled in {@link Jcrom}
     * @param jcrNode {@link JcrNode} annotation specified on the Entity Class
     * @param entity the entity to be mapped to the JCR node
     * @return the newly created JCR node
     * @throws JcrMappingException 
     * @throws RepositoryException 
     */
    Node doAddNode(Node parentNode, String nodeName, JcrNode jcrNode, Object entity) throws JcrMappingException, RepositoryException;

    /**
     * Method invoked when mixin types are added to a new node.
     * 
     * @param node new added node
     * @param mixinTypes an array of mixin type that will be added to the new node
     * @param jcrNode {@link JcrNode} annotation specified on the Entity Class
     * @param entity the entity to be mapped to the JCR node
     * @throws JcrMappingException 
     * @throws RepositoryException 
     */
    void doAddMixinTypes(Node node, String[] mixinTypes, JcrNode jcrNode, Object entity) throws JcrMappingException, RepositoryException;

    /**
     * Method invoked when you the class name is added to property for a newly created JCR node.
     * 
     * @param node the newly created JCR node
     * @param jcrNode {@link JcrNode} annotation specified on the Entity Class
     * @param entity the entity to be mapped to the JCR node
     * @throws JcrMappingException 
     * @throws RepositoryException 
     */
    void doAddClassNameToProperty(Node node, JcrNode jcrNode, Object entity) throws JcrMappingException, RepositoryException;

    /**
     * Method invoked when you update the class name to property for a node updated.
     * 
     * @param node the newly created JCR node
     * @param jcrNode {@link JcrNode} annotation specified on the Entity Class
     * @param entity the entity to be mapped to the JCR node
     * @throws JcrMappingException 
     * @throws RepositoryException 
     */
    void doUpdateClassNameToProperty(Node node, JcrNode jcrNode, Object entity) throws JcrMappingException, RepositoryException;

    /**
     * Method invoked if the node name has changed and the node must be moved.
     * 
     * @param parentNode parent {@link Node}
     * @param node the node to move
     * @param nodeName node name, cleaned if enabled in {@link Jcrom}
     * @param jcrNode {@link JcrNode} annotation specified on the Entity Class
     * @param entity the entity to be mapped to the JCR node
     * @throws JcrMappingException 
     * @throws RepositoryException 
     */
    void doMoveNode(Node parentNode, Node node, String nodeName, JcrNode jcrNode, Object entity) throws JcrMappingException, RepositoryException;

    /**
     * Method called after the node is completely added or updated. This method allows to perform other actions after creating or updating a node.
     * 
     * @param entity the entity object used to create or update the node.
     * @param node node created or updated
     * @throws JcrMappingException 
     * @throws RepositoryException 
     */
    void doComplete(Object entity, Node node) throws JcrMappingException, RepositoryException;
}
