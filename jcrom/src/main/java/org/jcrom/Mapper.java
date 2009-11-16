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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;
import javax.jcr.version.Version;
import net.sf.cglib.proxy.LazyLoader;
import org.jcrom.annotations.JcrBaseVersionCreated;
import org.jcrom.annotations.JcrBaseVersionName;
import org.jcrom.annotations.JcrCheckedout;
import org.jcrom.annotations.JcrChildNode;
import org.jcrom.annotations.JcrCreated;
import org.jcrom.annotations.JcrFileNode;
import org.jcrom.annotations.JcrName;
import org.jcrom.annotations.JcrNode;
import org.jcrom.annotations.JcrParentNode;
import org.jcrom.annotations.JcrPath;
import org.jcrom.annotations.JcrProperty;
import org.jcrom.annotations.JcrReference;
import org.jcrom.annotations.JcrSerializedProperty;
import org.jcrom.annotations.JcrUUID;
import org.jcrom.annotations.JcrVersionCreated;
import org.jcrom.annotations.JcrVersionName;
import org.jcrom.util.NodeFilter;
import org.jcrom.util.PathUtils;
import org.jcrom.util.ReflectionUtils;

/**
 * This class handles the heavy lifting of mapping a JCR node to a JCR entity object, and vice versa.
 * 
 * @author Olafur Gauti Gudmundsson
 */
class Mapper {

    static final String DEFAULT_FIELDNAME = "fieldName";

    /** Set of classes that have been validated for mapping by this mapper */
    private final CopyOnWriteArraySet<Class> mappedClasses = new CopyOnWriteArraySet<Class>();
    /** Specifies whether to clean up the node names */
    private final boolean cleanNames;
    /** Specifies whether to retrieve mapped class name from node property */
    private final boolean dynamicInstantiation;

    private final PropertyMapper propertyMapper;

    private final ReferenceMapper referenceMapper;

    private final FileNodeMapper fileNodeMapper;

    private final ChildNodeMapper childNodeMapper;

    private final Jcrom jcrom;

    private final ThreadLocal<Map<HistoryKey, Object>> history = new ThreadLocal<Map<HistoryKey, Object>>();

    /**
     * Create a Mapper for a specific class.
     * 
     * @param entityClass
     *            the class that we will me mapping to/from
     */
    Mapper(boolean cleanNames, boolean dynamicInstantiation, Jcrom jcrom) {
        this.cleanNames = cleanNames;
        this.dynamicInstantiation = dynamicInstantiation;
        this.jcrom = jcrom;
        this.propertyMapper = new PropertyMapper(this);
        this.referenceMapper = new ReferenceMapper(this);
        this.fileNodeMapper = new FileNodeMapper(this);
        this.childNodeMapper = new ChildNodeMapper(this);
    }

    void clearHistory() {
        history.remove();
    }

    boolean isMapped(Class c) {
        return mappedClasses.contains(c);
    }

    void addMappedClass(Class c) {
        mappedClasses.add(c);
    }

    CopyOnWriteArraySet<Class> getMappedClasses() {
        return mappedClasses;
    }

    boolean isCleanNames() {
        return cleanNames;
    }

    boolean isDynamicInstantiation() {
        return dynamicInstantiation;
    }

    Class getClassForName( String className ) {
        for ( Class c : mappedClasses ) {
            if ( className.equals(c.getCanonicalName()) ) {
                return c;
            }
        }
        try {
            return Class.forName(className);
        } catch ( ClassNotFoundException ex ) {
            return null;
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

    Object findEntityByPath(List entities, String path) throws IllegalAccessException {
        for (int i = 0; i < entities.size(); i++) {
            Object entity = (Object) entities.get(i);
            if (path.equals(getNodePath(entity))) {
                return entity;
            }
        }
        return null;
    }

    private Field findAnnotatedField(Object obj, Class annotationClass) {
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

    Field findNameField(Object obj) {
        return findAnnotatedField(obj, JcrName.class);
    }

    Field findUUIDField(Object obj) {
        return findAnnotatedField(obj, JcrUUID.class);
    }

    String getNodeName(Object object) throws IllegalAccessException {
        return (String) findNameField(object).get(object);
    }

    String getNodePath(Object object) throws IllegalAccessException {
        return (String) findPathField(object).get(object);
    }

    String getNodeUUID(Object object) throws IllegalAccessException {
        return (String) findUUIDField(object).get(object);
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
        findNameField(object).set(object, name);
    }

    void setNodePath(Object object, String path) throws IllegalAccessException {
        findPathField(object).set(object, path);
    }

    void setUUID(Object object, String uuid) throws IllegalAccessException {
        Field uuidField = findUUIDField(object);
        if (uuidField != null) {
            uuidField.set(object, uuid);
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
        if (node.hasProperty("jcr:childVersionHistory")) {
            Node versionNode = node.getSession().getNodeByUUID(node.getProperty("jcr:childVersionHistory").getString());
            NodeIterator it = versionNode.getNodes();
            while (it.hasNext()) {
                Node n = it.nextNode();
                if (!n.getName().equals("jcr:rootVersion") && n.isNodeType("nt:version") && n.hasNode("jcr:frozenNode")
                        && node.getPath().indexOf("/" + n.getName() + "/") != -1) {
                    return n.getNode("jcr:frozenNode");
                }
            }
            return node;
        } else {
            return node;
        }
    }

    Class findClassFromNode(Class defaultClass, Node node) throws RepositoryException, IllegalAccessException,
            ClassNotFoundException, InstantiationException {
        if (dynamicInstantiation) {
            // first we try to locate the class name from node property
            String classNameProperty = "className";
            JcrNode jcrNode = ReflectionUtils.getJcrNodeAnnotation(defaultClass);
            if (jcrNode != null && !jcrNode.classNameProperty().equals("none")) {
                classNameProperty = jcrNode.classNameProperty();
            }

            if (node.hasProperty(classNameProperty)) {
                String className = node.getProperty(classNameProperty).getString();
                Class c = Class.forName(className, true, Thread.currentThread().getContextClassLoader());
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

    Object createInstanceForNode(Class objClass, Node node) throws RepositoryException, IllegalAccessException,
            ClassNotFoundException, InstantiationException {

        return findClassFromNode(objClass, node).newInstance();
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
    Object fromNode(Class entityClass, Node node, NodeFilter nodeFilter) throws ClassNotFoundException,
            InstantiationException, RepositoryException, IllegalAccessException, IOException {
        history.set(new HashMap<HistoryKey, Object>());
        Object obj = createInstanceForNode(entityClass, node);
        if (ReflectionUtils.extendsClass(obj.getClass(), JcrFile.class)) {
            // special handling of JcrFile objects
            fileNodeMapper.mapSingleFile((JcrFile) obj, node, null, 0, nodeFilter, this);
        }
        mapNodeToClass(obj, node, nodeFilter, null, 0);
        history.remove();
        return obj;
    }

    /**
     * @param node
     * @param entity
     * @param childNodeFilter
     * @param maxDepth
     * @throws java.lang.Exception
     */
    String updateNode(Node node, Object entity, NodeFilter nodeFilter) throws RepositoryException,
            IllegalAccessException, IOException {
        return updateNode(node, entity, entity.getClass(), nodeFilter, 0);
    }

    String updateNode(Node node, Object obj, Class objClass, NodeFilter nodeFilter, int depth)
            throws RepositoryException, IllegalAccessException, IOException {

        obj = clearCglib(obj);

        // map the class name to a property
        JcrNode jcrNode = ReflectionUtils.getJcrNodeAnnotation(objClass);
        if (jcrNode != null && !jcrNode.classNameProperty().equals("none")) {
            // check if the class of the object has changed
            if ( node.hasProperty(jcrNode.classNameProperty()) ) {
                String oldClassName = node.getProperty(jcrNode.classNameProperty()).getString();
                if ( !oldClassName.equals(obj.getClass().getCanonicalName()) ) {
                    // different class, so we should remove the properties of the old class
                    Class oldClass = getClassForName(oldClassName);
                    if ( oldClass != null ) {
                        Class newClass = obj.getClass();

                        Set<Field> oldFields = new HashSet<Field>();
                        oldFields.addAll(Arrays.asList(ReflectionUtils.getDeclaredAndInheritedFields(oldClass, true)));
                        oldFields.removeAll(Arrays.asList(ReflectionUtils.getDeclaredAndInheritedFields(newClass, true)));

                        // remove the old fields
                        for ( Field field : oldFields ) {
                            if ( node.hasProperty(field.getName()) ) {
                                node.getProperty(field.getName()).remove();
                            }
                        }
                    }
                }
            }
            node.setProperty(jcrNode.classNameProperty(), obj.getClass().getCanonicalName());
        }

        // special handling of JcrFile objects
        if (ReflectionUtils.extendsClass(obj.getClass(), JcrFile.class) && depth == 0) {
            fileNodeMapper.addFileNode(node, (JcrFile) obj, this);
        }

        for (Field field : ReflectionUtils.getDeclaredAndInheritedFields(objClass, true)) {
            field.setAccessible(true);

            if (jcrom.getAnnotationReader().isAnnotationPresent(field, JcrProperty.class)) {
                propertyMapper.mapFieldToProperty(field, obj, node, this);

            } else if (jcrom.getAnnotationReader().isAnnotationPresent(field, JcrSerializedProperty.class)) {
                propertyMapper.mapSerializedFieldToProperty(field, obj, node);

            } else if (jcrom.getAnnotationReader().isAnnotationPresent(field, JcrChildNode.class)
                    && nodeFilter.isDepthIncluded(depth)) {
                // child nodes
                childNodeMapper.updateChildren(field, obj, node, depth, nodeFilter, this);

            } else if (jcrom.getAnnotationReader().isAnnotationPresent(field, JcrReference.class)) {
                // references
                referenceMapper.updateReferences(field, obj, node, nodeFilter);

            } else if (jcrom.getAnnotationReader().isAnnotationPresent(field, JcrFileNode.class)
                    && nodeFilter.isDepthIncluded(depth)) {
                // file nodes
                fileNodeMapper.updateFiles(field, obj, node, this, depth, nodeFilter);
            }
        }

        // if name is different, then we move the node
        if (!node.getName().equals(getCleanName(getNodeName(obj)))) {
            if (node.getParent().getPath().equals("/")) {
                // special case: moving a root node
                node.getSession().move(node.getPath(), node.getParent().getPath() + getCleanName(getNodeName(obj)));
            } else {
                node.getSession().move(node.getPath(),
                        node.getParent().getPath() + "/" + getCleanName(getNodeName(obj)));
            }

            // update the object name and path
            setNodeName(obj, node.getName());
            setNodePath(obj, node.getPath());
        }

        return node.getName();
    }

    /**
     * @param parentNode
     * @param entity
     * @throws java.lang.Exception
     */
    Node addNode(Node parentNode, Object entity, String[] mixinTypes) throws IllegalAccessException,
            RepositoryException, IOException {
        return addNode(parentNode, entity, mixinTypes, true);
    }

    Node addNode(Node parentNode, Object entity, String[] mixinTypes, boolean createNode)
            throws IllegalAccessException, RepositoryException, IOException {

        entity = clearCglib(entity);

        // create the child node
        Node node;
        JcrNode jcrNode = ReflectionUtils.getJcrNodeAnnotation(entity.getClass());
        if (createNode) {
            // check if we should use a specific node type
            if (jcrNode == null || jcrNode.nodeType().equals("nt:unstructured")) {
                node = parentNode.addNode(getCleanName(getNodeName(entity)));
            } else {
                node = parentNode.addNode(getCleanName(getNodeName(entity)), jcrNode.nodeType());
            }
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

            // update the object name and path
            setNodeName(entity, node.getName());
            setNodePath(entity, node.getPath());
            if (node.hasProperty("jcr:uuid")) {
                setUUID(entity, node.getUUID());
            }

        } else {
            node = parentNode;
        }

        // map the class name to a property
        if (jcrNode != null && !jcrNode.classNameProperty().equals("none")) {
            node.setProperty(jcrNode.classNameProperty(), entity.getClass().getCanonicalName());
        }

        // special handling of JcrFile objects
        if (ReflectionUtils.extendsClass(entity.getClass(), JcrFile.class)) {
            fileNodeMapper.addFileNode(node, (JcrFile) entity, this);
        }

        for (Field field : ReflectionUtils.getDeclaredAndInheritedFields(entity.getClass(), true)) {
            field.setAccessible(true);

            if (jcrom.getAnnotationReader().isAnnotationPresent(field, JcrProperty.class)) {
                propertyMapper.mapFieldToProperty(field, entity, node, this);

            } else if (jcrom.getAnnotationReader().isAnnotationPresent(field, JcrSerializedProperty.class)) {
                propertyMapper.mapSerializedFieldToProperty(field, entity, node);

            } else if (jcrom.getAnnotationReader().isAnnotationPresent(field, JcrChildNode.class)) {
                childNodeMapper.addChildren(field, entity, node, this);

            } else if (jcrom.getAnnotationReader().isAnnotationPresent(field, JcrReference.class)) {
                referenceMapper.addReferences(field, entity, node);

            } else if (jcrom.getAnnotationReader().isAnnotationPresent(field, JcrFileNode.class)) {
                fileNodeMapper.addFiles(field, entity, node, this);
            }
        }
        return node;
    }

    private boolean isVersionable(Node node) throws RepositoryException {
        for (NodeType mixinType : node.getMixinNodeTypes()) {
            if (mixinType.getName().equals("mix:versionable")) {
                return true;
            }
        }
        return false;
    }

    Object mapNodeToClass(Object obj, Node node, NodeFilter nodeFilter, Object parentObject, int depth)
            throws ClassNotFoundException, InstantiationException, RepositoryException, IllegalAccessException,
            IOException {

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

            if (jcrom.getAnnotationReader().isAnnotationPresent(field, JcrProperty.class)) {
                propertyMapper.mapPropertyToField(obj, field, node);

            } else if (jcrom.getAnnotationReader().isAnnotationPresent(field, JcrSerializedProperty.class)) {
                propertyMapper.mapSerializedPropertyToField(obj, field, node);

            } else if (jcrom.getAnnotationReader().isAnnotationPresent(field, JcrUUID.class)) {
                if (node.hasProperty("jcr:uuid")) {
                    field.set(obj, node.getUUID());
                }

            } else if (jcrom.getAnnotationReader().isAnnotationPresent(field, JcrBaseVersionName.class)) {
                if (isVersionable(node)) {
                    field.set(obj, node.getBaseVersion().getName());
                }

            } else if (jcrom.getAnnotationReader().isAnnotationPresent(field, JcrBaseVersionCreated.class)) {
                if (isVersionable(node)) {
                    field.set(obj, propertyMapper.getValue(field.getType(), node.getSession().getValueFactory()
                            .createValue(node.getBaseVersion().getCreated())));
                }

            } else if (jcrom.getAnnotationReader().isAnnotationPresent(field, JcrVersionName.class)) {
                if (node.getParent() != null && node.getParent().isNodeType("nt:version")) {
                    field.set(obj, node.getParent().getName());
                } else if (isVersionable(node)) {
                    // if we're not browsing version history, then this must be the base version
                    field.set(obj, node.getBaseVersion().getName());
                }

            } else if (jcrom.getAnnotationReader().isAnnotationPresent(field, JcrVersionCreated.class)) {
                if (node.getParent() != null && node.getParent().isNodeType("nt:version")) {
                    Version version = (Version) node.getParent();
                    field.set(obj, propertyMapper.getValue(field.getType(), node.getSession().getValueFactory()
                            .createValue(version.getCreated())));
                } else if (isVersionable(node)) {
                    // if we're not browsing version history, then this must be the base version
                    field.set(obj, propertyMapper.getValue(field.getType(), node.getSession().getValueFactory()
                            .createValue(node.getBaseVersion().getCreated())));
                }

            } else if (jcrom.getAnnotationReader().isAnnotationPresent(field, JcrCheckedout.class)) {
                field.set(obj, node.isCheckedOut());

            } else if (jcrom.getAnnotationReader().isAnnotationPresent(field, JcrCreated.class)) {
                if (node.hasProperty("jcr:created")) {
                    field
                            .set(obj, propertyMapper.getValue(field.getType(), node.getProperty("jcr:created")
                                    .getValue()));
                }

            } else if (jcrom.getAnnotationReader().isAnnotationPresent(field, JcrParentNode.class)) {
                if (parentObject != null && field.getType().isInstance(parentObject)) {
                    field.set(obj, parentObject);
                }

            } else if (jcrom.getAnnotationReader().isAnnotationPresent(field, JcrChildNode.class)
                    && nodeFilter.isDepthIncluded(depth)) {
                childNodeMapper.getChildrenFromNode(field, node, obj, depth, nodeFilter, this);

            } else if (jcrom.getAnnotationReader().isAnnotationPresent(field, JcrReference.class)) {
                referenceMapper.getReferencesFromNode(field, node, obj, depth, nodeFilter, this);

            } else if (jcrom.getAnnotationReader().isAnnotationPresent(field, JcrFileNode.class)
                    && nodeFilter.isDepthIncluded(depth)) {
                fileNodeMapper.getFilesFromNode(field, node, obj, depth, nodeFilter, this);

            } else if (jcrom.getAnnotationReader().isAnnotationPresent(field, JcrPath.class)) {
                field.set(obj, node.getPath());

            }
        }
        return obj;
    }

    static byte[] readBytes(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } finally {
            in.close();
            out.close();
        }
        return out.toByteArray();
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
                if (field.get(obj) != null) {
                    return field.get(obj);
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
                    return ((LazyLoader) field.get(obj)).loadObject();
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
