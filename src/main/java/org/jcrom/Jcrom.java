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
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;

import org.jcrom.annotations.JcrNode;
import org.jcrom.callback.JcromCallback;
import org.jcrom.type.DefaultTypeHandler;
import org.jcrom.type.JavaFXTypeHandler;
import org.jcrom.type.TypeHandler;
import org.jcrom.util.NodeFilter;
import org.jcrom.util.ReflectionUtils;

/**
 * This is the main entry class for JCROM. To use JCROM, you create an instance of this class, and add the classes you
 * want to be mapped to it. Jcrom will validate that the classes you add can be mapped to/from JCR.
 * <p>
 * Instances of this class are thread-safe.
 * </p>
 * 
 * @author Olafur Gauti Gudmundsson
 * @author Nicolas Dos Santos
 */
public class Jcrom {

    private final Mapper mapper;
    private final Validator validator;

    private AnnotationReader annotationReader;

    private static final ThreadLocal<Session> currentSession = new ThreadLocal<Session>();

    private SessionFactory sessionFactory;

    /**
     * Create a new Jcrom instance that cleans node names, but with dynamic instantiation turned off.
     */
    public Jcrom() {
        this(true);
    }

    /**
     * Create a new Jcrom instance with dynamic instantiation turned off.
     * 
     * @param cleanNames
     *            specifies whether to clean names of new nodes, that is, replace illegal characters and spaces
     *            automatically
     */
    public Jcrom(boolean cleanNames) {
        this(cleanNames, false);
    }

    /**
     * Create a new Jcrom instance.
     * 
     * @param cleanNames specifies whether to clean names of new nodes, that is, replace illegal characters and spaces automatically
     * @param dynamicInstantiation if set to true, then Jcrom will try to retrieve the name of the class to instantiate from a node
     *            property (see @JcrNode(classNameProperty)).
     */
    public Jcrom(boolean cleanNames, boolean dynamicInstantiation) {
        this(cleanNames, dynamicInstantiation, new HashSet<Class<?>>());
    }

    /**
     * Create a new Jcrom instance with name cleaning set to true, and dynamic instantiation off.
     * 
     * @param classesToMap a set of classes to map by this instance
     */
    public Jcrom(Set<Class<?>> classesToMap) {
        this(true, false, classesToMap);
    }

    /**
     * Create a new Jcrom instance with dynamic instantiation turned off.
     * 
     * @param cleanNames specifies whether to clean names of new nodes, that is, replace illegal characters and spaces automatically
     * @param classesToMap a set of classes to map by this instance
     */
    public Jcrom(boolean cleanNames, Set<Class<?>> classesToMap) {
        this(cleanNames, false, classesToMap);
    }

    /**
     * Create a new Jcrom instance.
     * 
     * @param cleanNames specifies whether to clean names of new nodes, that is, replace illegal characters and spaces automatically
     * @param dynamicInstantiation if set to true, then Jcrom will try to retrieve the name of the class to instantiate from a node property (see @JcrNode(classNameProperty)).
     * @param classesToMap a set of classes to map by this instance
     */
    public Jcrom(boolean cleanNames, boolean dynamicInstantiation, Set<Class<?>> classesToMap) {
        this(cleanNames, dynamicInstantiation, classesToMap, getTypeHandler());
    }

    private static TypeHandler getTypeHandler() {
        Class<?> clazz = null;
        try {
            // Try to find a Java FX class. If found, uses the JavaFXTypeHandler class
            clazz = Class.forName("javafx.beans.property.ObjectProperty");
        } catch (Exception e) {
        }
        return clazz == null ? new DefaultTypeHandler() : new JavaFXTypeHandler();
    }

    /**
     * Create a new Jcrom instance.
     * 
     * @param cleanNames specifies whether to clean names of new nodes, that is, replace illegal characters and spaces automatically
     * @param dynamicInstantiation if set to true, then Jcrom will try to retrieve the name of the class to instantiate from a node property (see @JcrNode(classNameProperty)).
     * @param classesToMap a set of classes to map by this instance
     * @param typeHandler Implementation of {@link TypeHandler} to handle additional types. If <code>null</code>, Defaults {@link DefaultTypeHandler} 
     */
    public Jcrom(boolean cleanNames, boolean dynamicInstantiation, Set<Class<?>> classesToMap, TypeHandler typeHandler) {
        TypeHandler th = typeHandler != null ? typeHandler : new DefaultTypeHandler();
        this.mapper = new Mapper(cleanNames, dynamicInstantiation, th, this);
        this.validator = new Validator(th, this);
        this.annotationReader = new ReflectionAnnotationReader();
        for (Class<?> c : classesToMap) {
            map(c);
        }
    }

    /**
     * Add a class that this instance can map to/from JCR nodes. This method will validate the class, and all mapped
     * JcrEntity implementations referenced from this class.
     * 
     * @param entityClass the class that will be mapped
     * @return the Jcrom instance
     */
    public synchronized Jcrom map(Class<?> entityClass) {
        if (!mapper.isMapped(entityClass)) {
            Set<Class<?>> validClasses = validator.validate(entityClass, mapper.isDynamicInstantiation());
            for (Class<?> c : validClasses) {
                mapper.addMappedClass(c);
            }
        }
        return this;
    }

    /**
     * Tries to map all classes in the package specified. Fails if one of the classes is not valid for mapping.
     * 
     * @param packageName the name of the package to process
     * @return the Jcrom instance
     */
    public synchronized Jcrom mapPackage(String packageName) {
        return mapPackage(packageName, false);
    }

    /**
     * Tries to map all classes in the package specified.
     * 
     * @param packageName the name of the package to process
     * @param ignoreInvalidClasses specifies whether to ignore classes in the package that cannot be mapped
     * @return the Jcrom instance
     */
    public synchronized Jcrom mapPackage(String packageName, boolean ignoreInvalidClasses) {
        try {
            for (Class<?> c : ReflectionUtils.getClasses(packageName)) {
                try {
                    // Ignore Enum because these are not entities
                    // Can be useful if there is an inner Enum
                    if (!c.isEnum()) {
                        map(c);
                    }
                } catch (JcrMappingException ex) {
                    if (!ignoreInvalidClasses) {
                        throw ex;
                    }
                }
            }
            return this;
        } catch (IOException ioex) {
            throw new JcrMappingException("Could not get map classes from package " + packageName, ioex);
        } catch (ClassNotFoundException cnfex) {
            throw new JcrMappingException("Could not get map classes from package " + packageName, cnfex);
        }
    }

    /**
     * Get a set of all classes that are mapped by this instance.
     * 
     * @return all classes that are mapped by this instance
     */
    public Set<Class<?>> getMappedClasses() {
        return Collections.unmodifiableSet(mapper.getMappedClasses());
    }

    /**
     * Check whether a specific class is mapped by this instance.
     * 
     * @param entityClass the class we want to check
     * @return true if the class is mapped, else false
     */
    public boolean isMapped(Class<?> entityClass) {
        return mapper.isMapped(entityClass);
    }

    public String getName(Object object) throws JcrMappingException {
        try {
            return mapper.getNodeName(object);
        } catch (IllegalAccessException e) {
            throw new JcrMappingException("Could not get node name from object", e);
        }
    }

    public String getPath(Object object) throws JcrMappingException {
        try {
            return mapper.getNodePath(object);
        } catch (IllegalAccessException e) {
            throw new JcrMappingException("Could not get node path from object", e);
        }
    }

    public Object getParentObject(Object childObject) throws JcrMappingException {
        try {
            return mapper.getParentObject(childObject);
        } catch (IllegalAccessException e) {
            throw new JcrMappingException("Could not get parent object with Annotation JcrParentNode from child object", e);
        }
    }

    public String getChildContainerPath(Object childObject, Object parentObject, Node parentNode) {
        try {
            return mapper.getChildContainerNodePath(childObject, parentObject, parentNode);
        } catch (IllegalAccessException e) {
            throw new JcrMappingException("Could not get child object with Annotation @JcrChildNode and with the type '" + childObject.getClass() + "' from parent object", e);
        } catch (RepositoryException e) {
            throw new JcrMappingException("Could not get child object with Annotation @JcrChildNode and with the type '" + childObject.getClass() + "' from parent object", e);
        }
    }

    public void setBaseVersionInfo(Object object, String name, Calendar created) throws JcrMappingException {
        try {
            mapper.setBaseVersionInfo(object, name, created);
        } catch (IllegalAccessException e) {
            throw new JcrMappingException("Could not set base version info on object", e);
        }
    }

    /**
     * Maps the node supplied to an instance of the entity class. Loads all child nodes, to infinite depth.
     * 
     * @param entityClass the class of the entity to be instantiated from the node (in the case of dynamic instantiation, the instance class may be read from the document, but will be cast to this class)
     * @param node the JCR node from which to create the object
     * @return an instance of the JCR entity class, mapped from the node
     * @throws JcrMappingException
     */
    public <T> T fromNode(Class<T> entityClass, Node node) throws JcrMappingException {
        return fromNode(entityClass, node, null);
    }

    /**
     * @deprecated This method is now deprecated. Use {@link #fromNode(Class, Node, NodeFilter)} instead.
     * 
     * <p>Maps the node supplied to an instance of the entity class.</p>
     * 
     * @param entityClass the class of the entity to be instantiated from the node (in the case of dynamic instantiation, the instance class may be read from the document, but will be cast to this class)
     * @param node the JCR node from which to create the object
     * @param childNodeFilter comma separated list of names of child nodes to load ("*" loads all, while "none" loads no children)
     * @param maxDepth the maximum depth of loaded child nodes (0 means no child nodes are loaded, while a negative value means that no restrictions are set on the depth).
     * @return an instance of the JCR entity class, mapped from the node
     * @throws JcrMappingException
     */
    @Deprecated
    public <T> T fromNode(Class<T> entityClass, Node node, String childNodeFilter, int maxDepth) throws JcrMappingException {
        return fromNode(entityClass, node, new NodeFilter(childNodeFilter, maxDepth));
    }

    /**
     * Maps the node supplied to an instance of the entity class.
     * 
     * @param entityClass the class of the entity to be instantiated from the node (in the case of dynamic instantiation, the instance class may be read from the document, but will be cast to this class)
     * @param node the JCR node from which to create the object
     * @param nodeFilter the NodeFilter to apply when loading child nodes and references
     * @return an instance of the JCR entity class, mapped from the node
     * @throws JcrMappingException
     */
    @SuppressWarnings("unchecked")
    public <T> T fromNode(Class<T> entityClass, Node node, NodeFilter nodeFilter) throws JcrMappingException {
        if (!mapper.isDynamicInstantiation() && !mapper.isMapped(entityClass)) {
            throw new JcrMappingException("Trying to map to an unmapped class: " + entityClass.getName());
        }
        try {
            return (T) mapper.fromNodeWithParent(entityClass, node, nodeFilter);
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
        } finally {
            mapper.clearHistory();
        }
    }

    /**
     * Maps the entity supplied to a JCR node, and adds that node as a child to the parent node supplied.
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
     * Maps the entity supplied to a JCR node, and adds that node as a child to the parent node supplied.
     * 
     * @param parentNode the parent node to which the entity node will be added
     * @param entity the entity to be mapped to the JCR node
     * @param mixinTypes an array of mixin type that will be added to the new node
     * @return the newly created JCR node
     * @throws JcrMappingException
     */
    public Node addNode(Node parentNode, Object entity, String[] mixinTypes) throws JcrMappingException {
        return addNode(parentNode, entity, mixinTypes, null);
    }

    /**
     * Maps the entity supplied to a JCR node, and adds that node as a child to the parent node supplied.
     * 
     * @param parentNode the parent node to which the entity node will be added
     * @param entity the entity to be mapped to the JCR node
     * @param mixinTypes an array of mixin type that will be added to the new node
     * @param action callback object that specifies the Jcrom actions: 
     *     <ul>
     *       <li>{@link JcromCallback#doAddNode(Node, String, JcrNode, Object)},</li>
     *       <li>{@link JcromCallback#doAddMixinTypes(Node, String[], JcrNode, Object)},</li>
     *       <li>{@link JcromCallback#doAddClassNameToProperty(Node, JcrNode, Object)},</li>
     *       <li>{@link JcromCallback#doComplete(Object, Node)},</li>
     *     </ul>
     * @return the newly created JCR node
     * @throws JcrMappingException
     * @since 2.1.0
     */
    public Node addNode(Node parentNode, Object entity, String[] mixinTypes, JcromCallback action) throws JcrMappingException {
        if (!mapper.isMapped(entity.getClass())) {
            throw new JcrMappingException("Trying to map an unmapped class: " + entity.getClass().getName());
        }
        try {
            return mapper.addNode(parentNode, entity, mixinTypes, action);
        } catch (RepositoryException e) {
            throw new JcrMappingException("Could not create node from object", e);
        } catch (IllegalAccessException e) {
            throw new JcrMappingException("Could not create node from object", e);
        } catch (IOException e) {
            throw new JcrMappingException("Could not create node from object", e);
        } finally {
            mapper.clearHistory();
        }
    }

    /**
     * Update an existing JCR node with the entity supplied.
     * 
     * @param node the JCR node to be updated
     * @param entity the entity that will be mapped to the existing node
     * @return the updated node
     * @throws JcrMappingException
     */
    public Node updateNode(Node node, Object entity) throws JcrMappingException {
        return updateNode(node, entity, null, null);
    }

    /**
     * @deprecated This method is now deprecated. Use {@link #updateNode(Node, Object, NodeFilter)} instead.
     * 
     * <p>Update an existing JCR node with the entity supplied.</p>
     * 
     * @param node the JCR node to be updated
     * @param entity the entity that will be mapped to the existing node
     * @param childNodeFilter comma separated list of names of child nodes to update ("*" updates all, while "none" updates no children)
     * @param maxDepth the maximum depth of updated child nodes (0 means no child nodes are updated, while a negative value means that no restrictions are set on the depth).
     * @return the updated node
     * @throws JcrMappingException
     */
    @Deprecated
    public Node updateNode(Node node, Object entity, String childNodeFilter, int maxDepth) throws JcrMappingException {
        return updateNode(node, entity, new NodeFilter(childNodeFilter, maxDepth), null);
    }

    /**
     * Update an existing JCR node with the entity supplied.
     * 
     * @param node the JCR node to be updated
     * @param entity the entity that will be mapped to the existing node
     * @param nodeFilter the NodeFilter to apply when updating child nodes and references
     * @return the updated node
     * @throws JcrMappingException
     */
    public Node updateNode(Node node, Object entity, NodeFilter nodeFilter) throws JcrMappingException {
        return updateNode(node, entity, nodeFilter, null);
    }

    /**
     * Update an existing JCR node with the entity supplied.
     * 
     * @param node the JCR node to be updated
     * @param entity the entity that will be mapped to the existing node
     * @param nodeFilter the NodeFilter to apply when updating child nodes and references
     * @param action callback object that specifies the Jcrom actions: 
     *     <ul>
     *       <li>{@link JcromCallback#doUpdateClassNameToProperty(Node, JcrNode, Object)},</li>
     *       <li>{@link JcromCallback#doMoveNode(Node, Node, String, JcrNode, Object)},</li>
     *       <li>{@link JcromCallback#doComplete(Object, Node)},</li>
     *     </ul>
     * @return the updated node
     * @throws JcrMappingException
     * @since 2.1.0
     */
    public Node updateNode(Node node, Object entity, NodeFilter nodeFilter, JcromCallback action) throws JcrMappingException {

        if (!mapper.isMapped(entity.getClass())) {
            throw new JcrMappingException("Trying to map an unmapped class: " + entity.getClass().getName());
        }
        try {
            return mapper.updateNode(node, entity, nodeFilter, action);
        } catch (RepositoryException e) {
            throw new JcrMappingException("Could not update node from object", e);
        } catch (IllegalAccessException e) {
            throw new JcrMappingException("Could not update node from object", e);
        } catch (IOException e) {
            throw new JcrMappingException("Could not update node from object", e);
        } finally {
            mapper.clearHistory();
        }
    }

    public static void setCurrentSession(Session session) {
        currentSession.set(session);
    }

    public static Session getCurrentSession() {
        return currentSession.get();
    }

    public void setAnnotationReader(AnnotationReader annotationReader) {
        this.annotationReader = annotationReader;
    }

    public AnnotationReader getAnnotationReader() {
        return annotationReader;
    }

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public void logNodeInfos(Node node) throws RepositoryException {
        for (PropertyIterator iter = node.getProperties(); iter.hasNext();) {
            Property p = iter.nextProperty();
            if (p.isMultiple()) {
                for (Value value : p.getValues()) {
                    System.out.println(p.getName() + " = " + value.getString());
                }
            } else {
                System.out.println(p.getName() + " = " + p.getValue().getString());
            }
        }
        for (PropertyIterator iter = node.getReferences(); iter.hasNext();) {
            Property p = iter.nextProperty();
            if (p.isMultiple()) {
                for (Value value : p.getValues()) {
                    System.out.println(p.getName() + " = " + value.getString());
                }
            } else {
                System.out.println(p.getName() + " = " + p.getValue().getString());
            }
        }
        for (PropertyIterator iter = node.getWeakReferences(); iter.hasNext();) {
            Property p = iter.nextProperty();
            if (p.isMultiple()) {
                for (Value value : p.getValues()) {
                    System.out.println(p.getName() + " = " + value.getString());
                }
            } else {
                System.out.println(p.getName() + " = " + p.getValue().getString());
            }
        }
    }
}
