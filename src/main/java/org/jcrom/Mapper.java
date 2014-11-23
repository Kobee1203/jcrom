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
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;
import javax.jcr.version.Version;
import javax.jcr.version.VersionManager;

import net.sf.cglib.proxy.LazyLoader;

import org.jcrom.annotations.JcrBaseVersionCreated;
import org.jcrom.annotations.JcrBaseVersionName;
import org.jcrom.annotations.JcrCheckedout;
import org.jcrom.annotations.JcrChildNode;
import org.jcrom.annotations.JcrCreated;
import org.jcrom.annotations.JcrFileNode;
import org.jcrom.annotations.JcrIdentifier;
import org.jcrom.annotations.JcrName;
import org.jcrom.annotations.JcrNode;
import org.jcrom.annotations.JcrParentNode;
import org.jcrom.annotations.JcrPath;
import org.jcrom.annotations.JcrProperty;
import org.jcrom.annotations.JcrProtectedProperty;
import org.jcrom.annotations.JcrReference;
import org.jcrom.annotations.JcrSerializedProperty;
import org.jcrom.annotations.JcrUUID;
import org.jcrom.annotations.JcrVersionCreated;
import org.jcrom.annotations.JcrVersionName;
import org.jcrom.callback.DefaultJcromCallback;
import org.jcrom.callback.JcromCallback;
import org.jcrom.type.TypeHandler;
import org.jcrom.util.JcrUtils;
import org.jcrom.util.NodeFilter;
import org.jcrom.util.PathUtils;
import org.jcrom.util.ReflectionUtils;

/**
 * This class handles the heavy lifting of mapping a JCR node to a JCR entity object, and vice versa.
 *
 * @author Olafur Gauti Gudmundsson
 * @author Nicolas Dos Santos
 */
class Mapper {

    static final String DEFAULT_FIELDNAME = "fieldName";

    /** Set of classes that have been validated for mapping by this mapper */
    private final CopyOnWriteArraySet<Class<?>> mappedClasses = new CopyOnWriteArraySet<Class<?>>();
    /** Specifies whether to clean up the node names */
    private final boolean cleanNames;
    /** Specifies whether to retrieve mapped class name from node property */
    private final boolean dynamicInstantiation;
    /** */
    private final TypeHandler typeHandler;

    private final PropertyMapper propertyMapper;

    private final ReferenceMapper referenceMapper;

    private final FileNodeMapper fileNodeMapper;

    private final ChildNodeMapper childNodeMapper;

    private final Jcrom jcrom;

    private final ThreadLocal<Map<HistoryKey, Object>> history = new ThreadLocal<Map<HistoryKey, Object>>();

    /**
     * Create a Mapper for a specific class.
     * 
     * @param cleanNames specifies whether to clean names of new nodes, that is, replace illegal characters and spaces automatically
     * @param dynamicInstantiation if set to true, then Jcrom will try to retrieve the name of the class to instantiate from a node property (see @JcrNode(classNameProperty)).
     * @param typeHandler {@link TypeHandler}
     * @param jcrom 
     */
    Mapper(boolean cleanNames, boolean dynamicInstantiation, TypeHandler typeHandler, Jcrom jcrom) {
        this.cleanNames = cleanNames;
        this.dynamicInstantiation = dynamicInstantiation;
        this.typeHandler = typeHandler;
        this.jcrom = jcrom;
        this.propertyMapper = new PropertyMapper(this);
        this.referenceMapper = new ReferenceMapper(this);
        this.fileNodeMapper = new FileNodeMapper(this);
        this.childNodeMapper = new ChildNodeMapper(this);
    }

    void clearHistory() {
        history.remove();
    }

    boolean isMapped(Class<?> c) {
        return mappedClasses.contains(c);
    }

    void addMappedClass(Class<?> c) {
        mappedClasses.add(c);
    }

    CopyOnWriteArraySet<Class<?>> getMappedClasses() {
        return mappedClasses;
    }

    boolean isCleanNames() {
        return cleanNames;
    }

    boolean isDynamicInstantiation() {
        return dynamicInstantiation;
    }

    TypeHandler getTypeHandler() {
        return typeHandler;
    }

    Class<?> getClassForName(String className) {
        return getClassForName(className, null);
    }

    Class<?> getClassForName(String className, Class<?> defaultClass) {
        for (Class<?> c : mappedClasses) {
            if (className.equals(c.getCanonicalName())) {
                return c;
            }
        }
        try {
            return Class.forName(className, true, Thread.currentThread().getContextClassLoader());
        } catch (ClassNotFoundException ex) {
            return defaultClass;
        }
    }

    String getCleanName(String name) {
        if (name == null) {
            throw new JcrMappingException("Node name is null");
        }
        if (cleanNames) {
            return PathUtils.createValidName(name);
        } else {
            return name;
        }
    }

    Object findEntityByPath(List<?> entities, String path) throws IllegalAccessException {
        for (Object entity : entities) {
            if (path.equals(getNodePath(entity))) {
                return entity;
            }
        }
        return null;
    }

    private Field findAnnotatedField(Object obj, Class<? extends Annotation> annotationClass) {
        for (Field field : ReflectionUtils.getDeclaredAndInheritedFields(obj.getClass(), false)) {
            if (jcrom.getAnnotationReader().isAnnotationPresent(field, annotationClass)) {
                field.setAccessible(true);
                return field;
            }
        }
        return null;
    }

    Field findPathField(Object obj) {
        return findAnnotatedField(obj, JcrPath.class);
    }

    Field findParentField(Object obj) {
        return findAnnotatedField(obj, JcrParentNode.class);
    }

    Field findNameField(Object obj) {
        return findAnnotatedField(obj, JcrName.class);
    }

    /**
     * @deprecated This method is now deprecated because {@link JcrUUID} annotation is deprecated.<br/>
     * {@link #findIdField(Object)} with {@link JcrIdentifier} annotation should be used instead.
     */
    @Deprecated
    Field findUUIDField(Object obj) {
        return findAnnotatedField(obj, JcrUUID.class);
    }

    Field findIdField(Object obj) {
        return findAnnotatedField(obj, JcrIdentifier.class);
    }

    String getNodeName(Object object) throws IllegalAccessException {
        Field field = findNameField(object);
        return (String) typeHandler.getObject(field, object);
    }

    String getNodePath(Object object) throws IllegalAccessException {
        Field field = findPathField(object);
        return (String) typeHandler.getObject(field, object);
    }

    Object getParentObject(Object childObject) throws IllegalAccessException {
        Field parentField = findParentField(childObject);
        return parentField != null ? typeHandler.getObject(parentField, childObject) : null;
    }

    String getChildContainerNodePath(Object childObject, Object parentObject, Node parentNode) throws IllegalAccessException, RepositoryException {
        return childNodeMapper.getChildContainerNodePath(childObject, parentObject, parentNode);
    }

    /**
     * @deprecated This method is now deprecated because {@link #findUUIDField(Object)} annotation is deprecated.<br/>
     * {@link #getNodeId(Object)} should be used instead.
     */
    @Deprecated
    String getNodeUUID(Object object) throws IllegalAccessException {
        return (String) findUUIDField(object).get(object);
    }

    String getNodeId(Object object) throws IllegalAccessException {
        Field idField = findIdField(object);
        return idField != null ? (String) typeHandler.getObject(idField, object) : getNodeUUID(object);
    }

    static boolean hasMixinType(Node node, String mixinType) throws RepositoryException {
        for (NodeType nodeType : node.getMixinNodeTypes()) {
            if (nodeType.getName().equals(mixinType)) {
                return true;
            }
        }
        return false;
    }

    void setBaseVersionInfo(Object object, String name, Calendar created) throws IllegalAccessException {
        Field baseName = findAnnotatedField(object, JcrBaseVersionName.class);
        if (baseName != null) {
            baseName.set(object, name);
        }
        Field baseCreated = findAnnotatedField(object, JcrBaseVersionCreated.class);
        if (baseCreated != null) {
            if (baseCreated.getType() == Date.class) {
                baseCreated.set(object, created.getTime());
            } else if (baseCreated.getType() == Timestamp.class) {
                baseCreated.set(object, new Timestamp(created.getTimeInMillis()));
            } else if (baseCreated.getType() == Calendar.class) {
                baseCreated.set(object, created);
            }
        }
    }

    void setNodeName(Object object, String name) throws IllegalAccessException {
        Field field = findNameField(object);
        typeHandler.setObject(field, object, name);
    }

    void setNodePath(Object object, String path) throws IllegalAccessException {
        Field field = findPathField(object);
        typeHandler.setObject(field, object, path);
    }

    /**
     * @deprecated This method is now deprecated because {@link #findUUIDField(Object)} annotation is deprecated.<br/>
     * {@link #setId(Object, String)} should be used instead.
     */
    @Deprecated
    void setUUID(Object object, String uuid) throws IllegalAccessException {
        Field uuidField = findUUIDField(object);
        if (uuidField != null) {
            typeHandler.setObject(uuidField, object, uuid);
        }
    }

    void setId(Object object, String id) throws IllegalAccessException {
        Field idField = findIdField(object);
        if (idField != null) {
            typeHandler.setObject(idField, object, id);
        }
    }

    /**
     * Check if this node has a child version history reference. If so, then return the referenced node, else return the
     * node supplied.
     *
     * @param node
     * @return
     * @throws javax.jcr.RepositoryException
     */
    Node checkIfVersionedChild(Node node) throws RepositoryException {
        if (node.hasProperty(Property.JCR_CHILD_VERSION_HISTORY)) {
            //Node versionNode = node.getSession().getNodeByUUID(node.getProperty("jcr:childVersionHistory").getString());
            Node versionNode = getNodeById(node, node.getProperty(Property.JCR_CHILD_VERSION_HISTORY).getString());
            NodeIterator it = versionNode.getNodes();
            while (it.hasNext()) {
                Node n = it.nextNode();
                if ((!n.getName().equals("jcr:rootVersion") && !n.getName().equals(Node.JCR_ROOT_VERSION)) && n.isNodeType(NodeType.NT_VERSION) && n.hasNode(Node.JCR_FROZEN_NODE) && node.getPath().indexOf("/" + n.getName() + "/") != -1) {
                    return n.getNode(Node.JCR_FROZEN_NODE);
                }
            }
            return node;
        } else {
            return node;
        }
    }

    Object findParentObjectFromNode(Node node) throws RepositoryException, IllegalAccessException, ClassNotFoundException, InstantiationException, IOException {
        Object parentObj = null;
        Node parentNode = node.getParent();
        while (parentNode != null) {
            Class<?> parentClass = findClassFromNode(Object.class, parentNode);
            if (parentClass != null && !parentClass.equals(Object.class)) {
                // Gets parent object without children
                parentObj = fromNode(parentClass, parentNode, new NodeFilter(NodeFilter.INCLUDE_ALL, 0));
                break;
            }
            try {
                parentNode = parentNode.getParent();
            } catch (Exception ignore) {
                parentNode = null;
            }
        }
        return parentObj;
    }

    Class<?> findClassFromNode(Class<?> defaultClass, Node node) throws RepositoryException, IllegalAccessException, ClassNotFoundException, InstantiationException {
        if (dynamicInstantiation) {
            // first we try to locate the class name from node property
            String classNameProperty = "className";
            JcrNode jcrNode = ReflectionUtils.getJcrNodeAnnotation(defaultClass);
            if (jcrNode != null && !jcrNode.classNameProperty().equals("none")) {
                classNameProperty = jcrNode.classNameProperty();
            }

            if (node.hasProperty(classNameProperty)) {
                String className = node.getProperty(classNameProperty).getString();
                Class<?> c = getClassForName(className, defaultClass);
                if (isMapped(c)) {
                    return c;
                } else {
                    throw new JcrMappingException("Trying to instantiate unmapped class: " + c.getName());
                }
            } else {
                // use default class
                return defaultClass;
            }
        } else {
            // use default class
            return defaultClass;
        }
    }

    Object createInstanceForNode(Class<?> objClass, Node node) throws RepositoryException, IllegalAccessException, ClassNotFoundException, InstantiationException {
        return findClassFromNode(objClass, node).newInstance();
    }

    /**
     * Transforms the node supplied to an instance of the entity class that this Mapper was created for.
     *
     * @param node
     *            the JCR node from which to create the object
     * @param nodeFilter
     *            the NodeFilter to be applied
     * @param action
     *            callback object that specifies the Jcr action
     * @return an instance of the JCR entity class, mapped from the node
     * @throws java.lang.Exception
     */
    Object fromNodeWithParent(Class<?> entityClass, Node node, NodeFilter nodeFilter) throws ClassNotFoundException, InstantiationException, RepositoryException, IllegalAccessException, IOException {
        history.set(new HashMap<HistoryKey, Object>());
        Object obj = createInstanceForNode(entityClass, node);

        Object parentObj = findParentObjectFromNode(node);

        if (nodeFilter == null) {
            nodeFilter = new NodeFilter(NodeFilter.INCLUDE_ALL, NodeFilter.DEPTH_INFINITE);
        }

        if (ReflectionUtils.extendsClass(obj.getClass(), JcrFile.class)) {
            // special handling of JcrFile objects
            fileNodeMapper.mapSingleFile((JcrFile) obj, node, parentObj, 0, nodeFilter, this);
        }
        mapNodeToClass(obj, node, nodeFilter, parentObj, 0);
        history.remove();
        return obj;
    }

    /**
     * Transforms the node supplied to an instance of the entity class that this Mapper was created for.
     *
     * @param node
     *            the JCR node from which to create the object
     * @param nodeFilter
     *            the NodeFilter to be applied
     * @return an instance of the JCR entity class, mapped from the node
     * @throws java.lang.Exception
     */
    Object fromNode(Class<?> entityClass, Node node, NodeFilter nodeFilter) throws ClassNotFoundException, InstantiationException, RepositoryException, IllegalAccessException, IOException {
        history.set(new HashMap<HistoryKey, Object>());
        Object obj = createInstanceForNode(entityClass, node);

        if (nodeFilter == null) {
            nodeFilter = new NodeFilter(NodeFilter.INCLUDE_ALL, NodeFilter.DEPTH_INFINITE);
        }

        if (ReflectionUtils.extendsClass(obj.getClass(), JcrFile.class)) {
            // special handling of JcrFile objects
            fileNodeMapper.mapSingleFile((JcrFile) obj, node, null, 0, nodeFilter, this);
        }
        mapNodeToClass(obj, node, nodeFilter, null, 0);
        history.remove();
        return obj;
    }

    /**
     * Transforms the entity supplied to a JCR node, and adds that node as a child to the parent node supplied.
     *
     * @param parentNode
     *            the parent node to which the entity node will be added
     * @param entity
     *            the entity to be mapped to the JCR node
     * @param mixinTypes
     *            an array of mixin type that will be added to the new node
     * @param action
     *            callback object that specifies the Jcrom actions:
     *            <ul>
     *              <li>{@link JcromCallback#doAddNode(Node, String, JcrNode, Object)},</li>
     *              <li>{@link JcromCallback#doAddMixinTypes(Node, String[], JcrNode, Object)},</li>
     *              <li>{@link JcromCallback#doComplete(Object, Node)},</li>
     *            </ul>
     * @return the newly created JCR node
     * @throws java.lang.Exception
     */
    Node addNode(Node parentNode, Object entity, String[] mixinTypes, JcromCallback action) throws IllegalAccessException, RepositoryException, IOException {
        return addNode(parentNode, entity, mixinTypes, true, action);
    }

    Node addNode(Node parentNode, Object entity, String[] mixinTypes, boolean createNode, JcromCallback action) throws IllegalAccessException, RepositoryException, IOException {
        entity = typeHandler.resolveAddEntity(entity);
        entity = clearCglib(entity);

        if (action == null) {
            action = new DefaultJcromCallback(jcrom);
        }

        // create the child node
        Node node;
        JcrNode jcrNode = typeHandler.getJcrNodeAnnotation(entity.getClass(), entity.getClass().getGenericSuperclass(), entity);
        if (createNode) {
            // add node
            String nodeName = getCleanName(getNodeName(entity));
            node = action.doAddNode(parentNode, nodeName, jcrNode, entity);

            // add mixin types
            action.doAddMixinTypes(node, mixinTypes, jcrNode, entity);

            // update the object id, name and path
            setId(entity, node.getIdentifier());
            setNodeName(entity, node.getName());
            setNodePath(entity, node.getPath());
            if (node.hasProperty(Property.JCR_UUID)) {
                // setUUID(entity, node.getUUID());
                setUUID(entity, node.getIdentifier());
            }
        } else {
            node = parentNode;
        }

        // add class name to property
        if (jcrNode != null && !jcrNode.classNameProperty().equals("none")) {
            action.doAddClassNameToProperty(node, jcrNode, entity);
        }

        // special handling of JcrFile objects
        if (ReflectionUtils.extendsClass(entity.getClass(), JcrFile.class)) {
            fileNodeMapper.addFileNode(node, (JcrFile) entity, this);
        }

        for (Field field : ReflectionUtils.getDeclaredAndInheritedFields(entity.getClass(), true)) {
            field.setAccessible(true);

            if (jcrom.getAnnotationReader().isAnnotationPresent(field, JcrProperty.class)) {
                propertyMapper.addProperty(field, entity, node, this);

            } else if (jcrom.getAnnotationReader().isAnnotationPresent(field, JcrSerializedProperty.class)) {
                propertyMapper.addSerializedProperty(field, entity, node);

            } else if (jcrom.getAnnotationReader().isAnnotationPresent(field, JcrChildNode.class)) {
                childNodeMapper.addChildren(field, entity, node, this);

            } else if (jcrom.getAnnotationReader().isAnnotationPresent(field, JcrReference.class)) {
                referenceMapper.addReferences(field, entity, node);

            } else if (jcrom.getAnnotationReader().isAnnotationPresent(field, JcrFileNode.class)) {
                fileNodeMapper.addFiles(field, entity, node, this);
            }
        }

        // complete the addition of the new node
        action.doComplete(entity, node);

        return node;
    }

    /**
     * Update an existing JCR node with the entity supplied.
     *
     * @param node
     *            the JCR node to be updated
     * @param entity
     *            the entity that will be mapped to the existing node
     * @param nodeFilter
     *            the NodeFilter to apply when updating child nodes and references
     * @param action
     *            callback object that specifies the Jcrom actions
     * @return the updated node
     * @throws java.lang.Exception
     */
    Node updateNode(Node node, Object entity, NodeFilter nodeFilter, JcromCallback action) throws RepositoryException, IllegalAccessException, IOException {
        return updateNode(node, entity, entity.getClass(), nodeFilter, 0, action);
    }

    Node updateNode(Node node, Object entity, Class<?> entityClass, NodeFilter nodeFilter, int depth, JcromCallback action) throws RepositoryException, IllegalAccessException, IOException {

        entity = clearCglib(entity);

        if (nodeFilter == null) {
            nodeFilter = new NodeFilter(NodeFilter.INCLUDE_ALL, NodeFilter.DEPTH_INFINITE);
        }

        if (action == null) {
            action = new DefaultJcromCallback(jcrom);
        }

        // map the class name to a property
        JcrNode jcrNode = ReflectionUtils.getJcrNodeAnnotation(entityClass);
        if (jcrNode != null && !jcrNode.classNameProperty().equals("none")) {
            // check if the class of the object has changed
            if (node.hasProperty(jcrNode.classNameProperty())) {
                String oldClassName = node.getProperty(jcrNode.classNameProperty()).getString();
                if (!oldClassName.equals(entity.getClass().getCanonicalName())) {
                    // different class, so we should remove the properties of the old class
                    Class<?> oldClass = getClassForName(oldClassName);
                    if (oldClass != null) {
                        Class<?> newClass = entity.getClass();

                        Set<Field> oldFields = new HashSet<Field>();
                        oldFields.addAll(Arrays.asList(ReflectionUtils.getDeclaredAndInheritedFields(oldClass, true)));
                        oldFields.removeAll(Arrays.asList(ReflectionUtils.getDeclaredAndInheritedFields(newClass, true)));

                        // remove the old fields
                        for (Field field : oldFields) {
                            if (node.hasProperty(field.getName())) {
                                node.getProperty(field.getName()).remove();
                            }
                        }
                    }
                }
            }
            action.doUpdateClassNameToProperty(node, jcrNode, entity);
        }

        // special handling of JcrFile objects
        if (ReflectionUtils.extendsClass(entity.getClass(), JcrFile.class) && depth == 0) {
            fileNodeMapper.addFileNode(node, (JcrFile) entity, this);
        }

        for (Field field : ReflectionUtils.getDeclaredAndInheritedFields(entityClass, true)) {
            field.setAccessible(true);

            if (jcrom.getAnnotationReader().isAnnotationPresent(field, JcrProperty.class) && nodeFilter.isDepthPropertyIncluded(depth)) {
                propertyMapper.updateProperty(field, entity, node, depth, nodeFilter, this);

            } else if (jcrom.getAnnotationReader().isAnnotationPresent(field, JcrSerializedProperty.class) && nodeFilter.isDepthPropertyIncluded(depth)) {
                propertyMapper.updateSerializedProperty(field, entity, node, depth, nodeFilter);

            } else if (jcrom.getAnnotationReader().isAnnotationPresent(field, JcrChildNode.class) && nodeFilter.isDepthIncluded(depth)) {
                // child nodes
                childNodeMapper.updateChildren(field, entity, node, depth, nodeFilter, this);

            } else if (jcrom.getAnnotationReader().isAnnotationPresent(field, JcrReference.class)) {
                // references
                referenceMapper.updateReferences(field, entity, node, nodeFilter);

            } else if (jcrom.getAnnotationReader().isAnnotationPresent(field, JcrFileNode.class) && nodeFilter.isDepthIncluded(depth)) {
                // file nodes
                fileNodeMapper.updateFiles(field, entity, node, this, depth, nodeFilter);
            }
        }

        // if name is different, then we move the node
        if (!node.getName().equals(getCleanName(getNodeName(entity)))) {
            boolean isVersionable = JcrUtils.hasMixinType(node, "mix:versionable") || JcrUtils.hasMixinType(node, NodeType.MIX_VERSIONABLE);
            Node parentNode = node.getParent();

            if (isVersionable) {
                if (JcrUtils.hasMixinType(parentNode, "mix:versionable") || JcrUtils.hasMixinType(parentNode, NodeType.MIX_VERSIONABLE)) {
                    JcrUtils.checkout(parentNode);
                }
            }

            // move node
            String nodeName = getCleanName(getNodeName(entity));
            action.doMoveNode(parentNode, node, nodeName, jcrNode, entity);

            if (isVersionable) {
                if ((JcrUtils.hasMixinType(parentNode, "mix:versionable") || JcrUtils.hasMixinType(parentNode, NodeType.MIX_VERSIONABLE)) && parentNode.isCheckedOut()) {
                    // Save session changes before checking-in the parent node
                    node.getSession().save();
                    JcrUtils.checkin(parentNode);
                }
            }

            // update the object name and path
            setNodeName(entity, node.getName());
            setNodePath(entity, node.getPath());
        }

        // complete the update of the node
        action.doComplete(entity, node);

        return node;
    }

    private boolean isVersionable(Node node) throws RepositoryException {
        for (NodeType mixinType : node.getMixinNodeTypes()) {
            if (mixinType.getName().equals("mix:versionable") || mixinType.getName().equals(NodeType.MIX_VERSIONABLE)) {
                return true;
            }
        }
        return false;
    }

    Object mapNodeToClass(Object obj, Node node, NodeFilter nodeFilter, Object parentObject, int depth) throws ClassNotFoundException, InstantiationException, RepositoryException, IllegalAccessException, IOException {

        if (!ReflectionUtils.extendsClass(obj.getClass(), JcrFile.class)) {
            // this does not apply for JcrFile extensions
            setNodeName(obj, node.getName());
        }

        // construct history key
        HistoryKey key = new HistoryKey();
        key.path = node.getPath();
        if (nodeFilter.getMaxDepth() == NodeFilter.DEPTH_INFINITE) {
            // then use infinite depth as key depth
            key.depth = NodeFilter.DEPTH_INFINITE;
        } else {
            // calculate key depth from max depth - current depth
            key.depth = nodeFilter.getMaxDepth() - depth;
        }

        // now check the history key
        if (history.get() == null) {
            history.set(new HashMap<HistoryKey, Object>());
        }
        if (history.get().containsKey(key)) {
            return history.get().get(key);
        } else {
            history.get().put(key, obj);
        }

        for (Field field : ReflectionUtils.getDeclaredAndInheritedFields(obj.getClass(), false)) {
            field.setAccessible(true);

            if (jcrom.getAnnotationReader().isAnnotationPresent(field, JcrProperty.class) && nodeFilter.isDepthPropertyIncluded(depth)) {
                propertyMapper.mapPropertyToField(obj, field, node, depth, nodeFilter);
            } else if (jcrom.getAnnotationReader().isAnnotationPresent(field, JcrSerializedProperty.class) && nodeFilter.isDepthPropertyIncluded(depth)) {
                propertyMapper.mapSerializedPropertyToField(obj, field, node, depth, nodeFilter);
            } else if (jcrom.getAnnotationReader().isAnnotationPresent(field, JcrProtectedProperty.class)) {
                propertyMapper.mapProtectedPropertyToField(obj, field, node);
            } else if (jcrom.getAnnotationReader().isAnnotationPresent(field, JcrUUID.class)) {
                if (node.hasProperty(Property.JCR_UUID)) {
                    // field.set(obj, node.getUUID());
                    typeHandler.setObject(field, obj, node.getIdentifier());
                }
            } else if (jcrom.getAnnotationReader().isAnnotationPresent(field, JcrIdentifier.class)) {
                typeHandler.setObject(field, obj, node.getIdentifier());
            } else if (jcrom.getAnnotationReader().isAnnotationPresent(field, JcrBaseVersionName.class)) {
                if (isVersionable(node)) {
                    // Version baseVersion = node.getBaseVersion();
                    Version baseVersion = getVersionManager(node).getBaseVersion(node.getPath());
                    typeHandler.setObject(field, obj, baseVersion.getName());
                }
            } else if (jcrom.getAnnotationReader().isAnnotationPresent(field, JcrBaseVersionCreated.class)) {
                if (isVersionable(node)) {
                    // Version baseVersion = node.getBaseVersion();
                    Version baseVersion = getVersionManager(node).getBaseVersion(node.getPath());
                    typeHandler.setObject(field, obj, typeHandler.getValue(field.getType(), null, typeHandler.createValue(Calendar.class, baseVersion.getCreated(), node.getSession().getValueFactory()), null));
                }
            } else if (jcrom.getAnnotationReader().isAnnotationPresent(field, JcrVersionName.class)) {
                if (node.getParent() != null && node.getParent().isNodeType(NodeType.NT_VERSION)) {
                    typeHandler.setObject(field, obj, node.getParent().getName());
                } else if (isVersionable(node)) {
                    // if we're not browsing version history, then this must be the base version
                    //Version baseVersion = node.getBaseVersion();
                    Version baseVersion = getVersionManager(node).getBaseVersion(node.getPath());
                    typeHandler.setObject(field, obj, baseVersion.getName());
                }
            } else if (jcrom.getAnnotationReader().isAnnotationPresent(field, JcrVersionCreated.class)) {
                if (node.getParent() != null && node.getParent().isNodeType(NodeType.NT_VERSION)) {
                    Version version = (Version) node.getParent();
                    typeHandler.setObject(field, obj, typeHandler.getValue(field.getType(), null, typeHandler.createValue(Calendar.class, version.getCreated(), node.getSession().getValueFactory()), null));
                } else if (isVersionable(node)) {
                    // if we're not browsing version history, then this must be the base version
                    //Version baseVersion = node.getBaseVersion();
                    Version baseVersion = getVersionManager(node).getBaseVersion(node.getPath());
                    typeHandler.setObject(field, obj, typeHandler.getValue(field.getType(), null, typeHandler.createValue(Calendar.class, baseVersion.getCreated(), node.getSession().getValueFactory()), null));
                }
            } else if (jcrom.getAnnotationReader().isAnnotationPresent(field, JcrCheckedout.class)) {
                typeHandler.setObject(field, obj, node.isCheckedOut());
            } else if (jcrom.getAnnotationReader().isAnnotationPresent(field, JcrCreated.class)) {
                if (node.hasProperty(Property.JCR_CREATED)) {
                    typeHandler.setObject(field, obj, typeHandler.getValue(field.getType(), null, node.getProperty(Property.JCR_CREATED).getValue(), null));
                }
            } else if (jcrom.getAnnotationReader().isAnnotationPresent(field, JcrParentNode.class)) {
                if (parentObject != null && typeHandler.getType(field.getType(), field.getGenericType(), obj).isInstance(parentObject)) {
                    typeHandler.setObject(field, obj, parentObject);
                }
            } else if (jcrom.getAnnotationReader().isAnnotationPresent(field, JcrChildNode.class) && nodeFilter.isDepthIncluded(depth)) {
                childNodeMapper.getChildrenFromNode(field, node, obj, depth, nodeFilter, this);
            } else if (jcrom.getAnnotationReader().isAnnotationPresent(field, JcrReference.class)) {
                referenceMapper.getReferencesFromNode(field, node, obj, depth, nodeFilter, this);
            } else if (jcrom.getAnnotationReader().isAnnotationPresent(field, JcrFileNode.class) && nodeFilter.isDepthIncluded(depth)) {
                fileNodeMapper.getFilesFromNode(field, node, obj, depth, nodeFilter, this);
            } else if (jcrom.getAnnotationReader().isAnnotationPresent(field, JcrPath.class)) {
                typeHandler.setObject(field, obj, node.getPath());
            }
        }
        return obj;
    }

    static VersionManager getVersionManager(Node node) throws RepositoryException {
        VersionManager versionMgr = node.getSession().getWorkspace().getVersionManager();
        return versionMgr;
    }

    static Node getNodeById(Node node, String id) throws RepositoryException {
        // return node.getSession().getNodeByUUID(uuid);
        return node.getSession().getNodeByIdentifier(id);
    }

    /**
     * This is a temporary solution to enable lazy loading of single child nodes and single references. The problem is
     * that Jcrom uses direct field modification, but CGLIB fails to cascade field changes between the enhanced class
     * and the lazy object.
     *
     * @param obj
     * @return
     * @throws java.lang.IllegalAccessException
     */
    Object clearCglib(Object obj) throws IllegalAccessException {
        for (Field field : ReflectionUtils.getDeclaredAndInheritedFields(obj.getClass(), true)) {
            field.setAccessible(true);
            if (field.getName().equals("CGLIB$LAZY_LOADER_0")) {
                Object object = typeHandler.getObject(field, obj);
                if (object != null) {
                    return object;
                } else {
                    // lazy loading has not been triggered yet, so
                    // we do it manually
                    return triggerLazyLoading(obj);
                }
            }
        }
        return obj;
    }

    Object triggerLazyLoading(Object obj) throws IllegalAccessException {
        for (Field field : ReflectionUtils.getDeclaredAndInheritedFields(obj.getClass(), false)) {
            field.setAccessible(true);
            if (field.getName().equals("CGLIB$CALLBACK_0")) {
                try {
                    return ((LazyLoader) typeHandler.getObject(field, obj)).loadObject();
                } catch (Exception e) {
                    throw new JcrMappingException("Could not trigger lazy loading", e);
                }
            }
        }
        return obj;
    }

    PropertyMapper getPropertyMapper() {
        return propertyMapper;
    }

    ReferenceMapper getReferenceMapper() {
        return referenceMapper;
    }

    FileNodeMapper getFileNodeMapper() {
        return fileNodeMapper;
    }

    ChildNodeMapper getChildNodeMapper() {
        return childNodeMapper;
    }

    Jcrom getJcrom() {
        return jcrom;
    }

    /**
     * Class for the history key. Contains the node path and the depth.
     * Thanks to Leander for supplying this fix.
     */
    private static class HistoryKey {

        private String path;
        private int depth;

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + depth;
            result = prime * result + ((path == null) ? 0 : path.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            HistoryKey other = (HistoryKey) obj;
            if (depth != other.depth) {
                return false;
            }
            if (path == null) {
                if (other.path != null) {
                    return false;
                }
            } else if (!path.equals(other.path)) {
                return false;
            }
            return true;
        }

    }
}
