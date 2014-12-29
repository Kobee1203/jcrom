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
package org.jcrom;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.jcrom.annotations.JcrChildNode;
import org.jcrom.type.TypeHandler;
import org.jcrom.util.NodeFilter;
import org.jcrom.util.ReflectionUtils;

/**
 * This class handles mappings of type @JcrChildNode
 * 
 * @author Olafur Gauti Gudmundsson
 * @author Nicolas Dos Santos
 */
class ChildNodeMapper {

    private static final String POLICY_NODE_NAME = "rep:policy";

    private final Mapper mapper;

    private final TypeHandler typeHandler;

    public ChildNodeMapper(Mapper mapper) {
        this.mapper = mapper;
        this.typeHandler = this.mapper.getTypeHandler();
    }

    private String getNodeName(Field field) {
        JcrChildNode jcrChildNode = mapper.getJcrom().getAnnotationReader().getAnnotation(field, JcrChildNode.class);
        String name = field.getName();
        if (!jcrChildNode.name().equals(Mapper.DEFAULT_FIELDNAME)) {
            name = jcrChildNode.name();
        }
        return name;
    }

    private void removeChildren(Node containerNode) throws RepositoryException {
        NodeIterator nodeIterator = containerNode.getNodes();
        while (nodeIterator.hasNext()) {
            Node currentNode = nodeIterator.nextNode();
            // ignore the policy node
            if (!currentNode.getName().equals(POLICY_NODE_NAME)) {
                currentNode.remove();
            }
        }
    }

    private Node createChildNodeContainer(Node node, String containerName, JcrChildNode jcrChildNode, Mapper mapper) throws RepositoryException {

        if (!node.hasNode(mapper.getCleanName(containerName))) {
            Node containerNode = node.addNode(mapper.getCleanName(containerName), jcrChildNode.containerNodeType());

            // add annotated mixin types
            if (jcrChildNode != null && jcrChildNode.containerMixinTypes() != null) {
                for (String mixinType : jcrChildNode.containerMixinTypes()) {
                    if (containerNode.canAddMixin(mixinType)) {
                        containerNode.addMixin(mixinType);
                    }
                }
            }
            return containerNode;
        } else {
            return node.getNode(mapper.getCleanName(containerName));
        }
    }

    private void addSingleChildToNode(Field field, JcrChildNode jcrChildNode, Object obj, String nodeName, Node node, Mapper mapper, int depth, NodeFilter nodeFilter) throws IllegalAccessException, RepositoryException, IOException {

        if (jcrChildNode.createContainerNode()) {
            Node childContainer = createChildNodeContainer(node, nodeName, jcrChildNode, mapper);
            if (!childContainer.hasNodes()) {
                Object object = typeHandler.getObject(field, obj);
                if (object != null) {
                    // add the node if it does not exist
                    mapper.addNode(childContainer, object, null, null);
                }
            } else {
                Object object = typeHandler.getObject(field, obj);
                if (object != null) {
                    Object childObj = object;
                    mapper.updateNode(childContainer.getNodes().nextNode(), childObj, childObj.getClass(), nodeFilter, depth + 1, null);
                } else {
                    // field is now null, so we remove the child node
                    removeChildren(childContainer);
                }
            }
        } else {
            // don't create a container node for this child,
            // use the field name for the node instead
            if (!node.hasNode(nodeName)) {
                Object object = typeHandler.getObject(field, obj);
                if (object != null) {
                    Object childObj = object;
                    mapper.setNodeName(childObj, nodeName);
                    mapper.addNode(node, childObj, null, null);
                }
            } else {
                Object object = typeHandler.getObject(field, obj);
                if (object != null) {
                    Object childObj = object;
                    mapper.setNodeName(childObj, nodeName);
                    mapper.updateNode(node.getNode(nodeName), childObj, childObj.getClass(), nodeFilter, depth + 1, null);
                } else {
                    NodeIterator nodeIterator = node.getNodes(nodeName);
                    while (nodeIterator.hasNext()) {
                        nodeIterator.nextNode().remove();
                    }
                }
            }
        }
    }

    private void addMultipleChildrenToNode(Field field, JcrChildNode jcrChildNode, Object obj, String nodeName, Node node, Mapper mapper, int depth, NodeFilter nodeFilter) throws IllegalAccessException, RepositoryException, IOException {

        Node childContainer = createChildNodeContainer(node, nodeName, jcrChildNode, mapper);
        List<?> children = (List<?>) typeHandler.getObject(field, obj);
        if (children != null && !children.isEmpty()) {
            if (childContainer.hasNodes()) {
                // children exist, we must update
                NodeIterator childNodes = childContainer.getNodes();
                while (childNodes.hasNext()) {
                    Node child = childNodes.nextNode();
                    Object childEntity = mapper.findEntityByPath(children, child.getPath());
                    if (childEntity == null) {
                        // this child was not found, so we remove it
                        child.remove();
                    } else {
                        mapper.updateNode(child, childEntity, childEntity.getClass(), nodeFilter, depth + 1, null);
                    }
                }
                // we must add new children, if any
                for (int i = 0; i < children.size(); i++) {
                    Object child = children.get(i);
                    String childPath = mapper.getNodePath(child);
                    if (childPath == null || childPath.equals("") || !childContainer.hasNode(mapper.getCleanName(mapper.getNodeName(child)))) {
                        mapper.addNode(childContainer, child, null, null);
                    }
                }
            } else {
                // no children exist, we add
                for (int i = 0; i < children.size(); i++) {
                    mapper.addNode(childContainer, children.get(i), null, null);
                }
            }
        } else {
            // field list is now null (or empty), so we remove the child nodes
            removeChildren(childContainer);
        }
    }

    /**
     * Maps a Map<String,Object> or Map<String,List<Object>> to a JCR Node.
     */
    private void addMapOfChildrenToNode(Field field, JcrChildNode jcrChildNode, Object obj, String nodeName, Node node, Mapper mapper, int depth, NodeFilter nodeFilter) throws IllegalAccessException, RepositoryException, IOException {

        Node childContainer = createChildNodeContainer(node, nodeName, jcrChildNode, mapper);
        Map<?, ?> childMap = (Map<?, ?>) typeHandler.getObject(field, obj);
        if (childMap != null && !childMap.isEmpty()) {
            Class<?> paramClass = ReflectionUtils.getParameterizedClass(field.getGenericType(), 1);
            if (childContainer.hasNodes()) {
                // nodes already exist, we need to update
                Map<String, String> mapWithCleanKeys = new HashMap<String, String>();
                for (Map.Entry<?, ?> entry : childMap.entrySet()) {
                    String key = (String) entry.getKey();
                    String cleanKey = mapper.getCleanName(key);
                    if (childContainer.hasNode(cleanKey)) {
                        if (typeHandler.isList(paramClass)) {
                            // lists are hard to update, so we just recreate it
                            childContainer.getNode(cleanKey).remove();
                            Node listContainer = childContainer.addNode(cleanKey);
                            List<?> childList = (List<?>) entry.getValue();
                            for (int i = 0; i < childList.size(); i++) {
                                mapper.addNode(listContainer, childList.get(i), null, null);
                            }
                        } else {
                            // update the child
                            mapper.updateNode(childContainer.getNode(cleanKey), entry.getValue(), paramClass, nodeFilter, depth + 1, null);
                        }
                    } else {
                        // this child does not exist, so we add it
                        addMapChild(paramClass, childContainer, childMap, key, cleanKey, mapper);
                    }
                    mapWithCleanKeys.put(cleanKey, "1");
                }

                // remove nodes that no longer exist
                NodeIterator childNodes = childContainer.getNodes();
                while (childNodes.hasNext()) {
                    Node child = childNodes.nextNode();
                    if (!mapWithCleanKeys.containsKey(child.getName())) {
                        child.remove();
                    }
                }
            } else {
                // no children exist, we simply add all
                for (Object k : childMap.keySet()) {
                    String key = (String) k;
                    String cleanKey = mapper.getCleanName(key);
                    addMapChild(paramClass, childContainer, childMap, key, cleanKey, mapper);
                }
            }
        } else {
            // map is now null (or empty), so we remove the child nodes
            removeChildren(childContainer);
        }
    }

    private void addMapChild(Class<?> paramClass, Node childContainer, Map<?, ?> childMap, String key, String cleanKey, Mapper mapper) throws IllegalAccessException, RepositoryException, IOException {
        if (typeHandler.isList(paramClass)) {
            List<?> childList = (List<?>) childMap.get(key);
            // create a container for the List
            Node listContainer = childContainer.addNode(cleanKey);
            for (int i = 0; i < childList.size(); i++) {
                mapper.addNode(listContainer, childList.get(i), null, null);
            }
        } else {
            mapper.setNodeName(childMap.get(key), cleanKey);
            mapper.addNode(childContainer, childMap.get(key), null, null);
        }
    }

    private void setChildren(Field field, Object obj, Node node, int depth, NodeFilter nodeFilter, Mapper mapper) throws IllegalAccessException, RepositoryException, IOException {

        JcrChildNode jcrChildNode = mapper.getJcrom().getAnnotationReader().getAnnotation(field, JcrChildNode.class);
        String nodeName = getNodeName(field);

        // make sure that this child is supposed to be updated
        if (nodeFilter == null || nodeFilter.isIncluded(field.getName(), depth)) {
            if (typeHandler.isList(field.getType())) {
                // multiple children in a List
                addMultipleChildrenToNode(field, jcrChildNode, obj, nodeName, node, mapper, depth, nodeFilter);
            } else if (typeHandler.isMap(field.getType())) {
                // multiple children in a Map
                addMapOfChildrenToNode(field, jcrChildNode, obj, nodeName, node, mapper, depth, nodeFilter);
            } else {
                // single child
                addSingleChildToNode(field, jcrChildNode, obj, nodeName, node, mapper, depth, nodeFilter);
            }
        }
    }

    void addChildren(Field field, Object entity, Node node, Mapper mapper) throws IllegalAccessException, RepositoryException, IOException {

        setChildren(field, entity, node, NodeFilter.DEPTH_INFINITE, null, mapper);
    }

    void updateChildren(Field field, Object obj, Node node, int depth, NodeFilter nodeFilter, Mapper mapper) throws IllegalAccessException, RepositoryException, IOException {

        setChildren(field, obj, node, depth, nodeFilter, mapper);
    }

    @SuppressWarnings("unchecked")
    List<?> getChildrenList(Class<?> childObjClass, Node childrenContainer, Object parentObj, Mapper mapper, int depth, NodeFilter nodeFilter, JcrChildNode jcrChildNode) throws ClassNotFoundException, InstantiationException, RepositoryException, IllegalAccessException, IOException {
        List<Object> children = jcrChildNode.listContainerClass().newInstance();
        NodeIterator iterator = childrenContainer.getNodes();
        while (iterator.hasNext()) {
            Node childNode = iterator.nextNode();
            // ignore the policy node when loading child nodes
            if (!childNode.getName().equals(POLICY_NODE_NAME)) {
                children.add(getSingleChild(childObjClass, childNode, parentObj, mapper, depth, nodeFilter));
            }
        }
        return children;
    }

    @SuppressWarnings("unchecked")
    private Map<?, ?> getChildrenMap(Class<?> mapParamClass, Node childrenContainer, Object parentObj, Mapper mapper, int depth, NodeFilter nodeFilter, JcrChildNode jcrChildNode) throws ClassNotFoundException, InstantiationException, RepositoryException, IllegalAccessException, IOException {

        Map<Object, Object> children = jcrChildNode.mapContainerClass().newInstance();
        NodeIterator iterator = childrenContainer.getNodes();
        while (iterator.hasNext()) {
            Node childNode = iterator.nextNode();
            if (typeHandler.isList(mapParamClass)) {
                // each value in the map is a list of child nodes
                if (jcrChildNode.lazy()) {
                    // lazy loading
                    children.put(childNode.getName(), ProxyFactory.createChildNodeListProxy(Object.class, parentObj, childNode.getPath(), childNode.getSession(), mapper, depth, nodeFilter, jcrChildNode));
                } else {
                    // eager loading
                    children.put(childNode.getName(), getChildrenList(Object.class, childNode, parentObj, mapper, depth, nodeFilter, jcrChildNode));
                }
            } else {
                // each value in the map is a child node
                if (jcrChildNode.lazy()) {
                    // lazy loading
                    children.put(childNode.getName(), ProxyFactory.createChildNodeProxy(mapper.findClassFromNode(Object.class, childNode), parentObj, childNode.getPath(), childNode.getSession(), mapper, depth, nodeFilter, false));
                } else {
                    // eager loading
                    children.put(childNode.getName(), getSingleChild(Object.class, childNode, parentObj, mapper, depth, nodeFilter));
                }
            }
        }
        return children;
    }

    Object getSingleChild(Class<?> childObjClass, Node childNode, Object obj, Mapper mapper, int depth, NodeFilter nodeFilter) throws ClassNotFoundException, InstantiationException, RepositoryException, IllegalAccessException, IOException {
        childNode = mapper.checkIfVersionedChild(childNode);
        Object childObj = mapper.createInstanceForNode(childObjClass, childNode);
        childObj = mapper.mapNodeToClass(childObj, childNode, nodeFilter, obj, depth + 1);
        return childObj;
    }

    void getChildrenFromNode(Field field, Node node, Object obj, int depth, NodeFilter nodeFilter, Mapper mapper) throws ClassNotFoundException, InstantiationException, RepositoryException, IllegalAccessException, IOException {

        String nodeName = getNodeName(field);
        JcrChildNode jcrChildNode = mapper.getJcrom().getAnnotationReader().getAnnotation(field, JcrChildNode.class);

        boolean childHasNodes = node.hasNode(nodeName) && (node.getNode(nodeName).hasNodes() || node.getNode(nodeName).hasProperty(Property.JCR_CHILD_VERSION_HISTORY));

        if (node.hasNode(nodeName) && (childHasNodes || (!jcrChildNode.createContainerNode() && !typeHandler.isList(field.getType()) && !typeHandler.isMap(field.getType()))) && nodeFilter.isIncluded(nodeName, depth)) {

            // child nodes are almost always stored inside a container node
            Node childrenContainer = node.getNode(nodeName);
            childrenContainer = mapper.checkIfVersionedChild(childrenContainer);
            if (typeHandler.isList(field.getType())) {
                // we can expect more than one child object here
                Class<?> childObjClass = ReflectionUtils.getParameterizedClass(field.getGenericType());
                List<?> children;
                if (jcrChildNode.lazy()) {
                    // lazy loading
                    children = ProxyFactory.createChildNodeListProxy(childObjClass, obj, childrenContainer.getPath(), node.getSession(), mapper, depth, nodeFilter, jcrChildNode);
                } else {
                    // eager loading
                    children = getChildrenList(childObjClass, childrenContainer, obj, mapper, depth, nodeFilter, jcrChildNode);
                }
                typeHandler.setObject(field, obj, children);
            } else if (typeHandler.isMap(field.getType())) {
                // dynamic map of child nodes
                // lazy loading is applied to each value in the Map
                Class<?> mapParamClass = ReflectionUtils.getParameterizedClass(field.getGenericType(), 1);
                Map<?, ?> childrenMap = getChildrenMap(mapParamClass, childrenContainer, obj, mapper, depth, nodeFilter, jcrChildNode);
                typeHandler.setObject(field, obj, childrenMap);
            } else {
                // instantiate the field class
                Class<?> childObjClass = typeHandler.getType(field.getType(), field.getGenericType(), obj);
                if (childrenContainer.hasNodes() || !jcrChildNode.createContainerNode()) {
                    if (jcrChildNode.lazy()) {
                        // lazy loading
                        typeHandler.setObject(field, obj, ProxyFactory.createChildNodeProxy(childObjClass, obj, childrenContainer.getPath(), node.getSession(), mapper, depth, nodeFilter, jcrChildNode.createContainerNode()));
                    } else {
                        // eager loading
                        if (jcrChildNode.createContainerNode()) {
                            typeHandler.setObject(field, obj, getSingleChild(childObjClass, childrenContainer.getNodes().nextNode(), obj, mapper, depth, nodeFilter));
                        } else {
                            typeHandler.setObject(field, obj, getSingleChild(childObjClass, childrenContainer, obj, mapper, depth, nodeFilter));
                        }
                    }
                } else {
                    // Issue 87: Child nodes not set to empty list/null
                    Object fieldValue = typeHandler.getObject(field, obj);
                    if (fieldValue != null) {
                        if (fieldValue instanceof Collection<?>) {
                            ((Collection<?>) fieldValue).clear();
                        } else if (fieldValue instanceof Map<?, ?>) {
                            ((Map<?, ?>) fieldValue).clear();
                        } else {
                            typeHandler.setObject(field, obj, null);
                        }
                    }
                }
            }
        }
    }

    String getChildContainerNodePath(Object childObject, Object parentObject, Node parentNode) throws IllegalAccessException, RepositoryException {
        List<String> childContainerPaths = getChildContainerNodePaths(childObject, parentObject, parentNode);
        if (childContainerPaths.size() > 1) {
            throw new IllegalAccessException("Multiple child objects found with type '" + childObject.getClass() + "' from parent object '" + parentObject.getClass() + "'");
        }
        return childContainerPaths.isEmpty() ? null : childContainerPaths.get(0);
    }

    List<String> getChildContainerNodePaths(Object childObject, Object parentObject, Node parentNode) throws IllegalAccessException, RepositoryException {
        Class<?> type = childObject.getClass();
        String childNodeName = mapper.getNodeName(childObject);

        List<String> containers = new ArrayList<String>();
        List<String> children = new ArrayList<String>();
        for (Field field : ReflectionUtils.getDeclaredAndInheritedFields(parentObject.getClass(), false)) {
            field.setAccessible(true);
            if (mapper.getJcrom().getAnnotationReader().isAnnotationPresent(field, JcrChildNode.class)) {
                Class<?> childObjClass;
                if (typeHandler.isList(field.getType())) {
                    childObjClass = ReflectionUtils.getParameterizedClass(field.getGenericType());
                } else if (typeHandler.isMap(field.getType())) {
                    childObjClass = ReflectionUtils.getParameterizedClass(field.getGenericType(), 1);
                } else {
                    childObjClass = field.getType();
                }

                if (childObjClass.isAssignableFrom(type)) {
                    JcrChildNode jcrChildNode = mapper.getJcrom().getAnnotationReader().getAnnotation(field, JcrChildNode.class);
                    String nodeName = getNodeName(field);
                    if (jcrChildNode.createContainerNode()) {
                        if (!parentNode.hasNode(nodeName)) {
                            throw new IllegalAccessException("The child container node not found with name '" + nodeName + "' from parent node '" + parentNode.getPath() + "'");
                        }
                        Node n = parentNode.getNode(nodeName);
                        containers.add(n.getPath());
                    } else {
                        children.add(nodeName);
                    }
                }
            }
        }

        // Check if there is child objects or the childNodeName is equal to one of the object names in the objects which is not creating a container node 
        if (containers.isEmpty() && !children.contains(childNodeName)) {
            throw new IllegalAccessException("Child object not found with name '" + childNodeName + "' and type '" + type + "' from parent object '" + parentObject.getClass() + "'");
        }

        return containers;
    }
}
