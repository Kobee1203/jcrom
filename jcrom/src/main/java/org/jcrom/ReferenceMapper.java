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

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.NamespaceRegistry;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;

import org.jcrom.annotations.JcrReference;
import org.jcrom.util.NodeFilter;
import org.jcrom.util.PathUtils;
import org.jcrom.util.ReflectionUtils;

/**
 * This class handles mappings of type @JcrReference
 * 
 * @author Olafur Gauti Gudmundsson
 * @author Nicolas Dos Santos
 */
class ReferenceMapper {

    private final Mapper mapper;

    public ReferenceMapper(Mapper mapper) {
        this.mapper = mapper;
    }

    private String getPropertyName(Field field) {
        JcrReference jcrReference = mapper.getJcrom().getAnnotationReader().getAnnotation(field, JcrReference.class);
        String name = field.getName();
        if (!jcrReference.name().equals(Mapper.DEFAULT_FIELDNAME)) {
            name = jcrReference.name();
        }
        return name;
    }

    private List<Value> getReferenceValues(List<?> references, Session session, JcrReference jcrReference) throws IllegalAccessException, RepositoryException {
        List<Value> refValues = new ArrayList<Value>();
        for (Object reference : references) {
            if (jcrReference.byPath()) {
                String referencePath = mapper.getNodePath(reference);
                if (referencePath != null && !referencePath.equals("")) {
                    if (session.getRootNode().hasNode(PathUtils.relativePath(referencePath))) {
                        refValues.add(session.getValueFactory().createValue(referencePath));
                    }
                }
            } else {
                String referenceId = mapper.getNodeId(reference);
                if (referenceId != null && !referenceId.equals("")) {
                    //Node referencedNode = session.getNodeByUUID(referenceUUID);
                    Node referencedNode = PathUtils.getNodeById(referenceId, session);
                    Value value;
                    if (jcrReference.weak()) {
                        value = session.getValueFactory().createValue(referencedNode, true);
                    } else {
                        value = session.getValueFactory().createValue(referencedNode);
                    }
                    refValues.add(value);
                }
            }
        }
        return refValues;
    }

    private void addSingleReferenceToNode(Field field, Object obj, String propertyName, Node node) throws IllegalAccessException, RepositoryException {
        // extract the Identifier from the object, load the node, and add a reference to it
        JcrReference jcrReference = mapper.getJcrom().getAnnotationReader().getAnnotation(field, JcrReference.class);
        Object referenceObject = field.get(obj);
        if (referenceObject != null) {
            referenceObject = mapper.clearCglib(field.get(obj));
        }

        if (referenceObject != null) {
            mapSingleReference(jcrReference, referenceObject, node, propertyName);
        } else {
            // remove the reference
            node.setProperty(propertyName, (Value) null);
        }
    }

    private void addMultipleReferencesToNode(Field field, Object obj, String propertyName, Node node) throws IllegalAccessException, RepositoryException {

        JcrReference jcrReference = mapper.getJcrom().getAnnotationReader().getAnnotation(field, JcrReference.class);
        List<?> references = (List<?>) field.get(obj);
        if (node.hasProperty(propertyName) && !node.getProperty(propertyName).getDefinition().isMultiple()) {
            node.setProperty(propertyName, (Value) null);
            //node.save();
            node.getSession().save();
        }
        if (references != null && !references.isEmpty()) {
            List<Value> refValues = getReferenceValues(references, node.getSession(), jcrReference);
            if (!refValues.isEmpty()) {
                node.setProperty(propertyName, refValues.toArray(new Value[refValues.size()]));
            } else if (node.hasProperty(propertyName)) {
                node.setProperty(propertyName, (Value[]) null);
            }
        } else if (node.hasProperty(propertyName)) {
            node.setProperty(propertyName, (Value[]) null);
        }
    }

    private void mapSingleReference(JcrReference jcrReference, Object referenceObject, Node containerNode, String propertyName) throws IllegalAccessException, RepositoryException {

        if (jcrReference.byPath()) {
            String referencePath = mapper.getNodePath(referenceObject);
            if (referencePath != null && !referencePath.equals("")) {
                if (containerNode.getSession().getRootNode().hasNode(PathUtils.relativePath(referencePath))) {
                    containerNode.setProperty(propertyName, containerNode.getSession().getValueFactory().createValue(referencePath));
                }
            }
        } else {
            String referenceId = mapper.getNodeId(referenceObject);
            if (referenceId != null && !referenceId.equals("")) {
                //Node referencedNode = containerNode.getSession().getNodeByUUID(referenceUUID);
                Node referencedNode = PathUtils.getNodeById(referenceId, containerNode.getSession());
                if (jcrReference.weak()) {
                    containerNode.setProperty(propertyName, containerNode.getSession().getValueFactory().createValue(referencedNode, true));
                } else {
                    containerNode.setProperty(propertyName, referencedNode);
                }
            } else {
                // remove the reference
                containerNode.setProperty(propertyName, (Value) null);
            }
        }
    }

    /**
     * Maps a Map<String,Object> or Map<String,List<Object>> to a JCR Node.
     */
    private void addMapOfReferencesToNode(Field field, Object obj, String containerName, Node node) throws IllegalAccessException, RepositoryException {

        JcrReference jcrReference = mapper.getJcrom().getAnnotationReader().getAnnotation(field, JcrReference.class);

        // remove previous references if they exist
        if (node.hasNode(containerName)) {
            node.getNode(containerName).remove();
        }

        // create a reference container
        Node referenceContainer = node.addNode(containerName);

        // map the references as properties on the container node
        Map<?, ?> referenceMap = (Map<?, ?>) field.get(obj);
        if (referenceMap != null && !referenceMap.isEmpty()) {
            Class<?> paramClass = ReflectionUtils.getParameterizedClass(field, 1);
            for (Map.Entry<?, ?> entry : referenceMap.entrySet()) {
                String key = (String) entry.getKey();
                if (ReflectionUtils.implementsInterface(paramClass, List.class)) {
                    List<?> references = (List<?>) entry.getValue();
                    List<Value> refValues = getReferenceValues(references, referenceContainer.getSession(), jcrReference);
                    if (!refValues.isEmpty()) {
                        referenceContainer.setProperty(key, refValues.toArray(new Value[refValues.size()]));
                    }

                } else {
                    Object referenceObject = entry.getValue();
                    mapSingleReference(jcrReference, referenceObject, referenceContainer, key);
                }
            }
        }
    }

    private void setReferenceProperties(Field field, Object obj, Node node, NodeFilter nodeFilter) throws IllegalAccessException, RepositoryException {

        String propertyName = getPropertyName(field);

        // make sure that the reference should be updated
        if (nodeFilter == null || nodeFilter.isNameIncluded(field.getName())) {
            if (ReflectionUtils.implementsInterface(field.getType(), List.class)) {
                // multiple references in a List
                addMultipleReferencesToNode(field, obj, propertyName, node);
            } else if (ReflectionUtils.implementsInterface(field.getType(), Map.class)) {
                // multiple references in a Map
                addMapOfReferencesToNode(field, obj, propertyName, node);
            } else {
                // single reference object
                addSingleReferenceToNode(field, obj, propertyName, node);
            }
        }
    }

    void addReferences(Field field, Object obj, Node node) throws IllegalAccessException, RepositoryException {
        setReferenceProperties(field, obj, node, null);
    }

    void updateReferences(Field field, Object obj, Node node, NodeFilter nodeFilter) throws IllegalAccessException, RepositoryException {
        setReferenceProperties(field, obj, node, nodeFilter);
    }

    private Node getSingleReferencedNode(JcrReference jcrReference, Value value, Session session) throws RepositoryException {
        if (jcrReference.byPath()) {
            if (session.getRootNode().hasNode(PathUtils.relativePath(value.getString()))) {
                return PathUtils.getNode(value.getString(), session);
            }
        } else {
            //return session.getNodeByUUID(value.getString());
            return PathUtils.getNodeById(value.getString(), session);
        }
        return null;
    }

    Object createReferencedObject(Field field, Value value, Object obj, Session session, Class<?> referenceObjClass, int depth, NodeFilter nodeFilter, Mapper mapper) throws ClassNotFoundException, InstantiationException, RepositoryException, IllegalAccessException, IOException {

        JcrReference jcrReference = mapper.getJcrom().getAnnotationReader().getAnnotation(field, JcrReference.class);
        Node referencedNode = null;

        if (jcrReference.byPath()) {
            if (session.getRootNode().hasNode(PathUtils.relativePath(value.getString()))) {
                referencedNode = PathUtils.getNode(value.getString(), session);
            }
        } else {
            //referencedNode = session.getNodeByUUID(value.getString());
            referencedNode = PathUtils.getNodeById(value.getString(), session);
        }

        if (referencedNode != null) {
            Object referencedObject = mapper.createInstanceForNode(referenceObjClass, referencedNode);
            if (nodeFilter.isIncluded(field.getName(), depth)) {
                // load and map the object, we don't send the current object as parent
                referencedObject = mapper.mapNodeToClass(referencedObject, referencedNode, nodeFilter, null, depth + 1);
            } else {
                if (jcrReference.byPath()) {
                    // just store the path
                    mapper.setNodePath(referencedObject, value.getString());
                } else {
                    // just store the Identifier
                    mapper.setUUID(referencedObject, value.getString());
                    mapper.setId(referencedObject, value.getString());
                }
            }
            return referencedObject;
        } else {
            return null;
        }
    }

    List<?> getReferenceList(Field field, String propertyName, Class<?> referenceObjClass, Node node, Object obj, int depth, NodeFilter nodeFilter, Mapper mapper) throws ClassNotFoundException, InstantiationException, RepositoryException, IllegalAccessException, IOException {

        List<Object> references = new ArrayList<Object>();
        if (node.hasProperty(propertyName)) {

            Value[] refValues;

            if (node.getProperty(propertyName).getDefinition().isMultiple()) {
                refValues = node.getProperty(propertyName).getValues();
            } else {
                refValues = new Value[] { node.getProperty(propertyName).getValue() };
            }

            for (Value value : refValues) {
                Object referencedObject = createReferencedObject(field, value, obj, node.getSession(), referenceObjClass, depth, nodeFilter, mapper);
                references.add(referencedObject);
            }

        }

        return references;

    }

    Map<String, Object> getReferenceMap(Field field, String containerName, Class<?> mapParamClass, Node node, Object obj, int depth, NodeFilter nodeFilter, Mapper mapper, JcrReference jcrReference) throws ClassNotFoundException, InstantiationException, RepositoryException, IllegalAccessException, IOException {

        Map<String, Object> references = new HashMap<String, Object>();
        if (node.hasNode(containerName)) {
            Node containerNode = node.getNode(containerName);
            PropertyIterator propertyIterator = containerNode.getProperties();
            while (propertyIterator.hasNext()) {
                Property p = propertyIterator.nextProperty();
                if (!p.getName().startsWith("jcr:") && !p.getName().startsWith(NamespaceRegistry.NAMESPACE_JCR)) {
                    if (ReflectionUtils.implementsInterface(mapParamClass, List.class)) {
                        if (jcrReference.lazy()) {
                            references.put(p.getName(), ProxyFactory.createReferenceListProxy(Object.class, obj, containerNode.getPath(), p.getName(), node.getSession(), mapper, depth, nodeFilter, field));
                        } else {
                            references.put(p.getName(), getReferenceList(field, p.getName(), Object.class, containerNode, obj, depth, nodeFilter, mapper));
                        }
                    } else {
                        if (jcrReference.lazy()) {
                            Node referencedNode = getSingleReferencedNode(jcrReference, p.getValue(), node.getSession());
                            references.put(p.getName(), ProxyFactory.createReferenceProxy(mapper.findClassFromNode(Object.class, referencedNode), obj, containerNode.getPath(), p.getName(), node.getSession(), mapper, depth, nodeFilter, field));
                        } else {
                            references.put(p.getName(), createReferencedObject(field, p.getValue(), obj, containerNode.getSession(), Object.class, depth, nodeFilter, mapper));
                        }
                    }
                }
            }
        }
        return references;
    }

    void getReferencesFromNode(Field field, Node node, Object obj, int depth, NodeFilter nodeFilter, Mapper mapper) throws ClassNotFoundException, InstantiationException, RepositoryException, IllegalAccessException, IOException {

        String propertyName = getPropertyName(field);
        JcrReference jcrReference = mapper.getJcrom().getAnnotationReader().getAnnotation(field, JcrReference.class);

        if (ReflectionUtils.implementsInterface(field.getType(), List.class)) {
            // multiple references in a List
            Class<?> referenceObjClass = ReflectionUtils.getParameterizedClass(field);
            if (jcrReference.lazy()) {
                // lazy loading
                field.set(obj, ProxyFactory.createReferenceListProxy(referenceObjClass, obj, node.getPath(), propertyName, node.getSession(), mapper, depth, nodeFilter, field));
            } else {
                // eager loading
                field.set(obj, getReferenceList(field, propertyName, referenceObjClass, node, obj, depth, nodeFilter, mapper));
            }

        } else if (ReflectionUtils.implementsInterface(field.getType(), Map.class)) {
            // multiple references in a Map
            // lazy loading is applied to each value in the Map
            Class<?> mapParamClass = ReflectionUtils.getParameterizedClass(field, 1);
            field.set(obj, getReferenceMap(field, propertyName, mapParamClass, node, obj, depth, nodeFilter, mapper, jcrReference));

        } else {
            // single reference
            if (node.hasProperty(propertyName)) {
                Class<?> referenceObjClass = field.getType();
                if (jcrReference.lazy()) {
                    field.set(obj, ProxyFactory.createReferenceProxy(referenceObjClass, obj, node.getPath(), propertyName, node.getSession(), mapper, depth, nodeFilter, field));
                } else {
                    field.set(obj, createReferencedObject(field, node.getProperty(propertyName).getValue(), obj, node.getSession(), referenceObjClass, depth, nodeFilter, mapper));
                }
            }
        }
    }
}
