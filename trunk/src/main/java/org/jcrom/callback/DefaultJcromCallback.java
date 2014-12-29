/**
 * This file is part of the JCROM project.
 * Copyright (C) 2008-2014 - All rights reserved.
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
package org.jcrom.callback;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;

import org.jcrom.JcrMappingException;
import org.jcrom.Jcrom;
import org.jcrom.annotations.JcrNode;

/**
 * The default implementation of the interface {@link JcromCallback}.<br/>
 * It is recommended to extend this class in order to only override the methods to modify.<br/>
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
 * @see JcromCallback
 * @author Nicolax Dos Santos
 */
public class DefaultJcromCallback implements JcromCallback {

    private final Jcrom jcrom;

    public DefaultJcromCallback(Jcrom jcrom) {
        this.jcrom = jcrom;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Node doAddNode(Node parentNode, String nodeName, JcrNode jcrNode, Object entity) throws JcrMappingException, RepositoryException {
        Node node;
        // check if we should use a specific node type
        if (jcrNode == null || (jcrNode.nodeType().equals("nt:unstructured") || jcrNode.nodeType().equals(NodeType.NT_UNSTRUCTURED))) {
            node = parentNode.addNode(nodeName);
        } else {
            node = parentNode.addNode(nodeName, jcrNode.nodeType());
        }
        return node;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doAddMixinTypes(Node node, String[] mixinTypes, JcrNode jcrNode, Object entity) throws JcrMappingException, RepositoryException {
        // add the mixin types
        if (mixinTypes != null) {
            for (String mixinType : mixinTypes) {
                if (node.canAddMixin(mixinType)) {
                    node.addMixin(mixinType);
                }
            }
        }
        // add annotated mixin types
        if (jcrNode != null && jcrNode.mixinTypes() != null) {
            for (String mixinType : jcrNode.mixinTypes()) {
                if (node.canAddMixin(mixinType)) {
                    node.addMixin(mixinType);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doComplete(Object entity, Node node) throws JcrMappingException, RepositoryException {
        // DO NOTHING
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doAddClassNameToProperty(Node node, JcrNode jcrNode, Object entity) throws JcrMappingException, RepositoryException {
        // map the class name to a property
        node.setProperty(jcrNode.classNameProperty(), entity.getClass().getCanonicalName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doUpdateClassNameToProperty(Node node, JcrNode jcrNode, Object entity) throws JcrMappingException, RepositoryException {
        // map the class name to a property
        node.setProperty(jcrNode.classNameProperty(), entity.getClass().getCanonicalName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doMoveNode(Node parentNode, Node node, String nodeName, JcrNode jcrNode, Object entity) throws JcrMappingException, RepositoryException {
        if (parentNode.getPath().equals("/")) {
            // special case: moving a root node
            node.getSession().move(node.getPath(), parentNode.getPath() + nodeName);
        } else {
            node.getSession().move(node.getPath(), parentNode.getPath() + "/" + nodeName);
        }
    }
}
