/**
 * This file is part of the JCROM project.
 * Copyright (C) 2008-2013 - All rights reserved.
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

import static org.jcrom.util.JavaFXUtils.getObject;
import static org.jcrom.util.JavaFXUtils.getType;
import static org.jcrom.util.JavaFXUtils.isList;
import static org.jcrom.util.JavaFXUtils.isMap;
import static org.jcrom.util.JavaFXUtils.setObject;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javafx.beans.property.ObjectProperty;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.ValueFactory;
import javax.jcr.nodetype.NodeType;

import org.jcrom.annotations.JcrFileNode;
import org.jcrom.annotations.JcrNode;
import org.jcrom.util.NodeFilter;
import org.jcrom.util.ReflectionUtils;
import org.jcrom.util.io.IOUtils;

/**
 * This class handles mappings of type @JcrFileNode
 * 
 * @author Olafur Gauti Gudmundsson
 * @author Nicolas Dos Santos
 */
class FileNodeMapper {

    private final Mapper mapper;

    public FileNodeMapper(Mapper mapper) {
        this.mapper = mapper;
    }

    private String getNodeName(Field field) {
        JcrFileNode jcrFileNode = mapper.getJcrom().getAnnotationReader().getAnnotation(field, JcrFileNode.class);
        String name = field.getName();
        if (!jcrFileNode.name().equals(Mapper.DEFAULT_FIELDNAME)) {
            name = jcrFileNode.name();
        }
        return name;
    }

    private Node createFileFolderNode(JcrNode jcrNode, String containerName, Node parentNode, Mapper mapper) throws RepositoryException {

        if (!parentNode.hasNode(mapper.getCleanName(containerName))) {
            if (jcrNode != null && (jcrNode.nodeType().equals("nt:unstructured") || jcrNode.nodeType().equals(NodeType.NT_UNSTRUCTURED))) {
                return parentNode.addNode(mapper.getCleanName(containerName));
            } else {
                // assume it is an nt:file or extension of that, 
                // so we create an nt:folder
                return parentNode.addNode(mapper.getCleanName(containerName), NodeType.NT_FOLDER);
            }
        } else {
            return parentNode.getNode(mapper.getCleanName(containerName));
        }
    }

    private <T extends JcrFile> void setFileNodeProperties(Node contentNode, T file) throws RepositoryException, IOException {
        contentNode.setProperty(Property.JCR_MIMETYPE, file.getMimeType());
        contentNode.setProperty(Property.JCR_LAST_MODIFIED, file.getLastModified());
        if (file.getEncoding() != null) {
            contentNode.setProperty(Property.JCR_ENCODING, file.getEncoding());
        }

        // add the file data
        JcrDataProvider dataProvider = file.getDataProvider();
        if (dataProvider != null && !dataProvider.isPersisted()) {
            ValueFactory valueFactory = contentNode.getSession().getValueFactory();
            if (dataProvider.isFile() && dataProvider.getFile() != null) {
                //contentNode.setProperty("jcr:data", new FileInputStream(dataProvider.getFile()));
                Binary binary = valueFactory.createBinary(new FileInputStream(dataProvider.getFile()));
                contentNode.setProperty(Property.JCR_DATA, binary);
            } else if (dataProvider.isBytes() && dataProvider.getBytes() != null) {
                //contentNode.setProperty("jcr:data", new ByteArrayInputStream(dataProvider.getBytes()));
                Binary binary = valueFactory.createBinary(new ByteArrayInputStream(dataProvider.getBytes()));
                contentNode.setProperty(Property.JCR_DATA, binary);
            } else if (dataProvider.isStream() && dataProvider.getInputStream() != null) {
                try {
                    // contentNode.setProperty("jcr:data", dataProvider.getInputStream());
                    Binary binary = valueFactory.createBinary(dataProvider.getInputStream());
                    contentNode.setProperty(Property.JCR_DATA, binary);
                } catch (RepositoryException re) {
                    System.out.println("FILE NODE: " + contentNode.getPath() + " " + ((FileInputStream) dataProvider.getInputStream()).available());
                    throw re;
                }
            }
        }
    }

    private <T extends JcrFile> void addFileNode(JcrNode jcrNode, Node parentNode, T file, Mapper mapper) throws IllegalAccessException, RepositoryException, IOException {
        Node fileNode;
        if (jcrNode == null || (jcrNode.nodeType().equals("nt:unstructured") || jcrNode.nodeType().equals(NodeType.NT_UNSTRUCTURED))) {
            fileNode = parentNode.addNode(mapper.getCleanName(file.getName()));
        } else {
            fileNode = parentNode.addNode(mapper.getCleanName(file.getName()), jcrNode.nodeType());
        }

        // add annotated mixin types
        if ((jcrNode != null) && (jcrNode.mixinTypes() != null)) {
            for (final String mixinType : jcrNode.mixinTypes()) {
                if (fileNode.canAddMixin(mixinType)) {
                    fileNode.addMixin(mixinType);
                }
            }
        }

        // update the object name and path
        file.setName(fileNode.getName());
        file.setPath(fileNode.getPath());
        // Update the object identifier
        mapper.setId(file, fileNode.getIdentifier());
        if (fileNode.hasProperty(Property.JCR_UUID)) {
            //mapper.setUUID(file, fileNode.getUUID());
            mapper.setUUID(file, fileNode.getIdentifier());
        }
        //Node contentNode = fileNode.addNode(Property.JCR_CONTENT, NodeType.NT_RESOURCE);
        //setFileNodeProperties(contentNode, file);

        mapper.addNode(fileNode, file, null, false, null);
    }

    <T extends JcrFile> void addFileNode(Node fileNode, T file, Mapper mapper) throws IllegalAccessException, RepositoryException, IOException {
        // update the object name and path
        file.setName(fileNode.getName());
        file.setPath(fileNode.getPath());
        // Update the object identifier
        mapper.setId(file, fileNode.getIdentifier());
        if (fileNode.hasProperty(Property.JCR_UUID)) {
            //mapper.setUUID(file, fileNode.getUUID());
            mapper.setUUID(file, fileNode.getIdentifier());
        }
        Node contentNode;
        if (fileNode.hasNode(Property.JCR_CONTENT)) {
            contentNode = fileNode.getNode(Property.JCR_CONTENT);
        } else {
            contentNode = fileNode.addNode(Property.JCR_CONTENT, NodeType.NT_RESOURCE);
        }
        setFileNodeProperties(contentNode, file);
    }

    private <T extends JcrFile> void updateFileNode(Node fileNode, T file, NodeFilter nodeFilter, int depth, Mapper mapper) throws RepositoryException, IllegalAccessException, IOException {
        Node contentNode = fileNode.getNode(Property.JCR_CONTENT);
        setFileNodeProperties(contentNode, file);

        mapper.updateNode(fileNode, file, file.getClass(), nodeFilter, depth + 1, null);
    }

    private void removeChildren(Node containerNode) throws RepositoryException {
        NodeIterator nodeIterator = containerNode.getNodes();
        while (nodeIterator.hasNext()) {
            nodeIterator.nextNode().remove();
        }
    }

    private void addSingleFileToNode(Field field, Object obj, String nodeName, Node node, Mapper mapper, int depth, NodeFilter nodeFilter) throws IllegalAccessException, RepositoryException, IOException {

        JcrNode fileJcrNode = null;
        if (ObjectProperty.class.isAssignableFrom(field.getType())) {
            fileJcrNode = ReflectionUtils.getJcrNodeAnnotation(ReflectionUtils.getObjectPropertyGeneric(obj, field));
        } else {
            fileJcrNode = ReflectionUtils.getJcrNodeAnnotation(field.getType());
        }
        Node fileContainer = createFileFolderNode(fileJcrNode, nodeName, node, mapper);

        if (!fileContainer.hasNodes()) {
            if (getObject(field, obj) != null) {
                JcrFile file = (JcrFile) getObject(field, obj);
                if (file != null) {
                    addFileNode(fileJcrNode, fileContainer, file, mapper);
                }
            }
        } else {
            if (getObject(field, obj) != null) {
                JcrFile file = (JcrFile) getObject(field, obj);
                if (file != null) {
                    updateFileNode(fileContainer.getNodes().nextNode(), file, nodeFilter, depth, mapper);
                }
            } else {
                // field is now null, so we remove the files
                removeChildren(fileContainer);
            }
        }
    }

    private void updateFileList(List<?> children, Node fileContainer, JcrNode fileJcrNode, Mapper mapper, int depth, NodeFilter nodeFilter) throws IllegalAccessException, RepositoryException, IOException {

        if (children != null && !children.isEmpty()) {
            if (fileContainer.hasNodes()) {
                // children exist, we must update
                NodeIterator childNodes = fileContainer.getNodes();
                while (childNodes.hasNext()) {
                    Node child = childNodes.nextNode();
                    JcrFile childEntity = (JcrFile) mapper.findEntityByPath(children, child.getPath());
                    if (childEntity == null) {
                        // this child was not found, so we remove it
                        child.remove();
                    } else {
                        updateFileNode(child, childEntity, nodeFilter, depth, mapper);
                    }
                }
                // we must add new children, if any
                for (Object child : children) {
                    String childPath = mapper.getNodePath(child);
                    if (childPath == null || childPath.equals("") || !fileContainer.hasNode(mapper.getCleanName(mapper.getNodeName(child)))) {
                        addFileNode(fileJcrNode, fileContainer, (JcrFile) child, mapper);
                    }
                }
            } else {
                // no children exist, we add
                for (int i = 0; i < children.size(); i++) {
                    addFileNode(fileJcrNode, fileContainer, (JcrFile) children.get(i), mapper);
                }
            }
        } else {
            // field list is now null (or empty), so we remove the file nodes
            removeChildren(fileContainer);
        }
    }

    private void addMultipleFilesToNode(Field field, Object obj, String nodeName, Node node, Mapper mapper, int depth, NodeFilter nodeFilter) throws IllegalAccessException, RepositoryException, IOException {

        JcrNode fileJcrNode = ReflectionUtils.getJcrNodeAnnotation(ReflectionUtils.getParameterizedClass(field.getGenericType()));
        Node fileContainer = createFileFolderNode(fileJcrNode, nodeName, node, mapper);

        List<?> children = (List<?>) getObject(field, obj);
        updateFileList(children, fileContainer, fileJcrNode, mapper, depth, nodeFilter);
    }

    private void addMapOfFilesToNode(Field field, Object obj, String nodeName, Node node, Mapper mapper, int depth, NodeFilter nodeFilter) throws IllegalAccessException, RepositoryException, IOException {

        Class<?> fileClass;
        if (isList(ReflectionUtils.getParameterizedClass(field.getGenericType(), 1))) {
            fileClass = ReflectionUtils.getTypeArgumentOfParameterizedClass(field.getGenericType(), 1, 0);
        } else {
            fileClass = ReflectionUtils.getParameterizedClass(field.getGenericType(), 1);
        }

        JcrNode fileJcrNode = ReflectionUtils.getJcrNodeAnnotation(fileClass);
        String cleanName = mapper.getCleanName(nodeName);
        Node fileContainer = node.hasNode(cleanName) ? node.getNode(cleanName) : node.addNode(cleanName); // this is just a nt:unstructured node

        Map<?, ?> children = (Map<?, ?>) field.get(obj);
        if (children != null && !children.isEmpty()) {
            Class<?> paramClass = ReflectionUtils.getParameterizedClass(field.getGenericType(), 1);
            if (fileContainer.hasNodes()) {
                // nodes already exist, we need to update
                Map<String, String> mapWithCleanKeys = new HashMap<String, String>();
                Iterator<?> it = children.keySet().iterator();
                while (it.hasNext()) {
                    String key = (String) it.next();
                    String cleanKey = mapper.getCleanName(key);
                    if (fileContainer.hasNode(cleanKey)) {
                        if (isList(paramClass)) {
                            // update the file list
                            List<?> childList = (List<?>) children.get(key);
                            Node listContainer = createFileFolderNode(fileJcrNode, cleanKey, fileContainer, mapper);
                            updateFileList(childList, listContainer, fileJcrNode, mapper, depth, nodeFilter);
                        } else {
                            // update the file
                            updateFileNode(fileContainer.getNode(cleanKey), (JcrFile) children.get(key), nodeFilter, depth, mapper);
                        }
                    } else {
                        // this child does not exist, so we add it
                        addMapFile(paramClass, fileJcrNode, fileContainer, children, key, mapper);
                    }
                    mapWithCleanKeys.put(cleanKey, "1");
                }

                // remove nodes that no longer exist
                NodeIterator childNodes = fileContainer.getNodes();
                while (childNodes.hasNext()) {
                    Node child = childNodes.nextNode();
                    if (!mapWithCleanKeys.containsKey(child.getName())) {
                        child.remove();
                    }
                }
            } else {
                // no children exist, we simply add all
                Iterator<?> it = children.keySet().iterator();
                while (it.hasNext()) {
                    String key = (String) it.next();
                    addMapFile(paramClass, fileJcrNode, fileContainer, children, key, mapper);
                }
            }

        } else {
            // field list is now null (or empty), so we remove the file nodes
            removeChildren(fileContainer);
        }
    }

    private void addMapFile(Class<?> paramClass, JcrNode fileJcrNode, Node fileContainer, Map<?, ?> childMap, String key, Mapper mapper) throws IllegalAccessException, RepositoryException, IOException {

        if (isList(paramClass)) {
            List<?> childList = (List<?>) childMap.get(key);
            Node listContainer = createFileFolderNode(fileJcrNode, mapper.getCleanName(key), fileContainer, mapper);
            for (int i = 0; i < childList.size(); i++) {
                addFileNode(fileJcrNode, listContainer, (JcrFile) childList.get(i), mapper);
            }
        } else {
            addFileNode(fileJcrNode, fileContainer, (JcrFile) childMap.get(key), mapper);
        }
    }

    private void setFiles(Field field, Object obj, Node node, Mapper mapper, int depth, NodeFilter nodeFilter) throws IllegalAccessException, RepositoryException, IOException {

        String nodeName = getNodeName(field);

        // make sure that this child is supposed to be updated
        if (nodeFilter == null || nodeFilter.isIncluded(field.getName(), depth)) {
            if (isList(field.getType())) {
                // multiple file nodes in a List
                addMultipleFilesToNode(field, obj, nodeName, node, mapper, depth, nodeFilter);
            } else if (isMap(field.getType())) {
                // dynamic map of file nodes
                addMapOfFilesToNode(field, obj, nodeName, node, mapper, depth, nodeFilter);
            } else {
                // single child
                addSingleFileToNode(field, obj, nodeName, node, mapper, depth, nodeFilter);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends JcrFile> void mapNodeToFileObject(JcrFileNode jcrFileNode, T fileObj, Node fileNode, NodeFilter nodeFilter, Object parentObject, int depth, Mapper mapper) throws ClassNotFoundException, InstantiationException, RepositoryException, IllegalAccessException, IOException {

        Node contentNode = fileNode.getNode(Property.JCR_CONTENT);
        fileObj.setName(fileNode.getName());
        fileObj.setPath(fileNode.getPath());
        fileObj.setMimeType(contentNode.getProperty(Property.JCR_MIMETYPE).getString());
        fileObj.setLastModified(contentNode.getProperty(Property.JCR_LAST_MODIFIED).getDate());
        if (contentNode.hasProperty(Property.JCR_ENCODING)) {
            fileObj.setEncoding(contentNode.getProperty(Property.JCR_ENCODING).getString());
        }

        // file data
        if (nodeFilter.isIncluded("jcr:data", depth)) {
            if (jcrFileNode == null || jcrFileNode.loadType() == JcrFileNode.LoadType.STREAM) {
                // InputStream is = contentNode.getProperty(Property.JCR_DATA).getBinary().getStream();
                // JcrDataProviderImpl dataProvider = new JcrDataProviderImpl(is, contentNode.getProperty(Property.JCR_DATA).getLength());
                Binary binary = contentNode.getProperty(Property.JCR_DATA).getBinary();
                JcrDataProvider dataProvider = new JcrDataProviderImpl(binary);
                fileObj.setDataProvider(dataProvider);
            } else if (jcrFileNode.loadType() == JcrFileNode.LoadType.BYTES) {
                InputStream is = contentNode.getProperty(Property.JCR_DATA).getBinary().getStream();
                JcrDataProviderImpl dataProvider = new JcrDataProviderImpl(IOUtils.toByteArray(is));
                fileObj.setDataProvider(dataProvider);
            }
        }

        // if this is a JcrFile subclass, it may contain custom properties and 
        // child nodes that need to be mapped
        fileObj = (T) mapper.mapNodeToClass(fileObj, fileNode, nodeFilter, parentObject, depth + 1);
    }

    void addFiles(Field field, Object obj, Node node, Mapper mapper) throws IllegalAccessException, RepositoryException, IOException {
        setFiles(field, obj, node, mapper, NodeFilter.DEPTH_INFINITE, null);
    }

    void updateFiles(Field field, Object obj, Node node, Mapper mapper, int depth, NodeFilter nodeFilter) throws IllegalAccessException, RepositoryException, IOException {
        setFiles(field, obj, node, mapper, depth, nodeFilter);
    }

    @SuppressWarnings("unchecked")
    List<JcrFile> getFileList(Class<?> childObjClass, Node fileContainer, Object obj, JcrFileNode jcrFileNode, int depth, NodeFilter nodeFilter, Mapper mapper) throws ClassNotFoundException, InstantiationException, RepositoryException, IllegalAccessException, IOException {
        List<JcrFile> children = jcrFileNode.listContainerClass().newInstance();
        NodeIterator iterator = fileContainer.getNodes();
        while (iterator.hasNext()) {
            JcrFile fileObj = (JcrFile) childObjClass.newInstance();
            mapNodeToFileObject(jcrFileNode, fileObj, iterator.nextNode(), nodeFilter, obj, depth, mapper);
            children.add(fileObj);
        }
        return children;
    }

    void mapSingleFile(JcrFile fileObj, Node fileNode, Object obj, int depth, NodeFilter nodeFilter, Mapper mapper) throws ClassNotFoundException, InstantiationException, RepositoryException, IllegalAccessException, IOException {
        mapNodeToFileObject(null, fileObj, fileNode, nodeFilter, obj, depth, mapper);
    }

    JcrFile getSingleFile(Class<?> childObjClass, Node fileContainer, Object obj, JcrFileNode jcrFileNode, int depth, NodeFilter nodeFilter, Mapper mapper) throws ClassNotFoundException, InstantiationException, RepositoryException, IllegalAccessException, IOException {
        JcrFile fileObj = (JcrFile) childObjClass.newInstance();
        mapNodeToFileObject(jcrFileNode, fileObj, fileContainer.getNodes().nextNode(), nodeFilter, obj, depth, mapper);
        return fileObj;
    }

    // Add file properties to another node, e.g. reference node
    static void addFileProperties(JcrFile fileObj, Node fileNode, JcrFileNode jcrFileNode, int depth, NodeFilter nodeFilter) throws ClassNotFoundException, InstantiationException, RepositoryException, IllegalAccessException, IOException {

        Node contentNode = fileNode.getNode("jcr:content");
        fileObj.setName(fileNode.getName());
        //fileObj.setPath(fileNode.getPath());
        fileObj.setMimeType(contentNode.getProperty("jcr:mimeType").getString());
        fileObj.setLastModified(contentNode.getProperty("jcr:lastModified").getDate());
        if (contentNode.hasProperty("jcr:encoding")) {
            fileObj.setEncoding(contentNode.getProperty("jcr:encoding").getString());
        }

        // file data
        if (nodeFilter.isIncluded("jcr:data", depth)) {
            if (jcrFileNode == null || jcrFileNode.loadType() == JcrFileNode.LoadType.STREAM) {
                // InputStream is = contentNode.getProperty(Property.JCR_DATA).getBinary().getStream();
                // JcrDataProviderImpl dataProvider = new JcrDataProviderImpl(is, contentNode.getProperty(Property.JCR_DATA).getLength());
                Binary binary = contentNode.getProperty(Property.JCR_DATA).getBinary();
                JcrDataProvider dataProvider = new JcrDataProviderImpl(binary);
                fileObj.setDataProvider(dataProvider);
            } else if (jcrFileNode.loadType() == JcrFileNode.LoadType.BYTES) {
                InputStream is = contentNode.getProperty(Property.JCR_DATA).getBinary().getStream();
                JcrDataProviderImpl dataProvider = new JcrDataProviderImpl(IOUtils.toByteArray(is));
                fileObj.setDataProvider(dataProvider);
            }
        }

    }

    @SuppressWarnings("unchecked")
    private Map<?, ?> getFileMap(Field field, Node fileContainer, JcrFileNode jcrFileNode, Object obj, int depth, NodeFilter nodeFilter, Mapper mapper) throws ClassNotFoundException, InstantiationException, RepositoryException, IllegalAccessException, IOException {

        Class<?> mapParamClass = ReflectionUtils.getParameterizedClass(field.getGenericType(), 1);
        Map<Object, Object> children = jcrFileNode.mapContainerClass().newInstance();
        NodeIterator iterator = fileContainer.getNodes();
        while (iterator.hasNext()) {
            Node childNode = iterator.nextNode();
            if (isList(mapParamClass)) {
                Class<?> childObjClass = ReflectionUtils.getTypeArgumentOfParameterizedClass(field.getGenericType(), 1, 0);
                if (jcrFileNode.lazy()) {
                    // lazy loading
                    children.put(childNode.getName(), ProxyFactory.createFileNodeListProxy(childObjClass, obj, fileContainer.getPath(), fileContainer.getSession(), mapper, depth, nodeFilter, jcrFileNode));
                } else {
                    children.put(childNode.getName(), getFileList(childObjClass, childNode, obj, jcrFileNode, depth, nodeFilter, mapper));
                }
            } else {
                if (jcrFileNode.lazy()) {
                    // lazy loading
                    children.put(childNode.getName(), ProxyFactory.createFileNodeProxy(mapParamClass, obj, fileContainer.getPath(), fileContainer.getSession(), mapper, depth, nodeFilter, jcrFileNode));
                } else {
                    children.put(childNode.getName(), getSingleFile(mapParamClass, fileContainer, obj, jcrFileNode, depth, nodeFilter, mapper));
                }
            }
        }
        return children;
    }

    void getFilesFromNode(Field field, Node node, Object obj, int depth, NodeFilter nodeFilter, Mapper mapper) throws ClassNotFoundException, InstantiationException, RepositoryException, IllegalAccessException, IOException {

        String nodeName = getNodeName(field);
        JcrFileNode jcrFileNode = mapper.getJcrom().getAnnotationReader().getAnnotation(field, JcrFileNode.class);

        if (node.hasNode(nodeName) && nodeFilter.isIncluded(field.getName(), depth)) {
            // file nodes are always stored inside a folder node
            Node fileContainer = node.getNode(nodeName);
            if (isList(getType(field, obj))) {
                // we can expect more than one child object here
                List<?> children;
                Class<?> childObjClass = ReflectionUtils.getParameterizedClass(field.getGenericType());
                if (jcrFileNode.lazy()) {
                    // lazy loading
                    children = ProxyFactory.createFileNodeListProxy(childObjClass, obj, fileContainer.getPath(), node.getSession(), mapper, depth, nodeFilter, jcrFileNode);
                } else {
                    // eager loading
                    children = getFileList(childObjClass, fileContainer, obj, jcrFileNode, depth, nodeFilter, mapper);
                }
                setObject(field, obj, children);

            } else if (isMap(field.getType())) {
                // dynamic map of child nodes
                // lazy loading is applied to each value in the Map
                setObject(field, obj, getFileMap(field, fileContainer, jcrFileNode, obj, depth, nodeFilter, mapper));

            } else {
                // instantiate the field class
                if (fileContainer.hasNodes()) {
                    Object file = null;
                    Class type = getType(field, obj);
                    if (jcrFileNode.lazy()) {
                        // lazy loading
                        file = ProxyFactory.createFileNodeProxy(type, obj, fileContainer.getPath(), node.getSession(), mapper, depth, nodeFilter, jcrFileNode);
                    } else {
                        // eager loading
                        file = getSingleFile(type, fileContainer, obj, jcrFileNode, depth, nodeFilter, mapper);
                    }
                    setObject(field, obj, file);
                }
            }
        }
    }
}
