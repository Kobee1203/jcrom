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

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import org.jcrom.annotations.JcrFileNode;
import org.jcrom.annotations.JcrNode;
import org.jcrom.util.NameFilter;
import org.jcrom.util.ReflectionUtils;

/**
 * This class handles mappings of type @JcrFileNode
 *
 * @author Olafur Gauti Gudmundsson
 */
class FileNodeMapper {

	private static String getNodeName(Field field) {
		JcrFileNode jcrFileNode = field.getAnnotation(JcrFileNode.class);
		String name = field.getName();
		if ( !jcrFileNode.name().equals(Mapper.DEFAULT_FIELDNAME) ) {
			name = jcrFileNode.name();
		}
		return name;
	}
	
	private static Node createFileFolderNode( JcrNode jcrNode, String nodeName, Node parentNode, Mapper mapper ) throws RepositoryException {
		if ( jcrNode != null && jcrNode.nodeType().equals("nt:unstructured") ) {
			return parentNode.addNode(mapper.getCleanName(nodeName));
		} else {
			// assume it is an nt:file or extension of that, 
			// so we create an nt:folder
			return parentNode.addNode(mapper.getCleanName(nodeName), "nt:folder");
		}
	}
	
	private static <T extends JcrFile> void setFileNodeProperties( Node contentNode, T file ) throws RepositoryException, IOException {
		contentNode.setProperty("jcr:mimeType", file.getMimeType());
		contentNode.setProperty("jcr:lastModified", file.getLastModified());
		if ( file.getEncoding() != null ) {
			contentNode.setProperty("jcr:encoding", file.getEncoding());
		}
		
		// add the file data
		JcrDataProvider dataProvider = file.getDataProvider();
		if ( dataProvider != null ) {
			if ( dataProvider.getType() == JcrDataProvider.TYPE.FILE && dataProvider.getFile() != null ) {
				contentNode.setProperty("jcr:data", new FileInputStream(dataProvider.getFile()));
			} else if ( dataProvider.getType() == JcrDataProvider.TYPE.BYTES && dataProvider.getBytes() != null ) {
				contentNode.setProperty("jcr:data", new ByteArrayInputStream(dataProvider.getBytes()));
			} else if ( dataProvider.getType() == JcrDataProvider.TYPE.STREAM && dataProvider.getInputStream() != null ) {
				contentNode.setProperty("jcr:data", dataProvider.getInputStream());
			}
		}
	}
	
	private static <T extends JcrFile> void addFileNode( JcrNode jcrNode, Node parentNode, T file, Mapper mapper ) 
			throws IllegalAccessException, RepositoryException, IOException {
		Node fileNode;
		if ( jcrNode == null || jcrNode.nodeType().equals("nt:unstructured") ) {
			fileNode = parentNode.addNode(mapper.getCleanName(file.getName()));
		} else {
			fileNode = parentNode.addNode(mapper.getCleanName(file.getName()), jcrNode.nodeType());
		}
		Node contentNode = fileNode.addNode("jcr:content", "nt:resource");
		setFileNodeProperties(contentNode, file);
		
		mapper.addNode(fileNode, file, null, false);
	}
	
	private static <T extends JcrFile> void updateFileNode( Node fileNode, T file, NameFilter childNameFilter, 
			int maxDepth, int depth, Mapper mapper ) 
			throws RepositoryException, IllegalAccessException, IOException {
		Node contentNode = fileNode.getNode("jcr:content");
		setFileNodeProperties(contentNode, file);
	
		mapper.updateNode(fileNode, file, file.getClass(), childNameFilter, maxDepth, depth+1);
	}
	
	private static void removeNodes( Node node, String childNodeName ) throws RepositoryException {
		NodeIterator nodeIterator = node.getNodes(childNodeName);
		while ( nodeIterator.hasNext() ) {
			nodeIterator.nextNode().remove();
		}
	}
	
	private static void addSingleFileToNode( Field field, Object obj, String nodeName, Node node, Mapper mapper, 
			int depth, int maxDepth, NameFilter nameFilter ) 
			throws IllegalAccessException, RepositoryException, IOException {
		
		if ( !node.hasNode(nodeName) ) {
			if ( field.get(obj) != null ) {
				JcrNode fileJcrNode = ReflectionUtils.getJcrNodeAnnotation(field.getType());
				Node fileContainer = createFileFolderNode(fileJcrNode, nodeName, node, mapper);
				addFileNode(fileJcrNode, fileContainer, (JcrFile)field.get(obj), mapper);
			}
		} else {
			if ( field.get(obj) != null ) {
				updateFileNode(node.getNode(nodeName).getNodes().nextNode(), (JcrFile)field.get(obj), nameFilter, maxDepth, depth, mapper);
			} else {
				// field is now null, so we remove the child node
				removeNodes(node, nodeName);
			}
		}
	}
	
	private static void addMultipleFilesToNode( Field field, Object obj, String nodeName, Node node, Mapper mapper, 
			int depth, int maxDepth, NameFilter nameFilter ) 
			throws IllegalAccessException, RepositoryException, IOException {
		
		List children = (List)field.get(obj);
		if ( children != null && !children.isEmpty() ) {
			if ( node.hasNode(nodeName) ) {
				// children exist, we must update
				Node childContainer = node.getNode(nodeName);
				NodeIterator childNodes = childContainer.getNodes();
				while ( childNodes.hasNext() ) {
					Node child = childNodes.nextNode();
					JcrFile childEntity = (JcrFile)mapper.findEntityByName(children, child.getName());
					if ( childEntity == null ) {
						// this child was not found, so we remove it
						child.remove();
					} else {
						updateFileNode(child, childEntity, nameFilter, maxDepth, depth, mapper);
					}
				}
				// we must add new children, if any
				for ( int i = 0; i < children.size(); i++ ) {
					Object child = children.get(i);
					if ( !childContainer.hasNode(mapper.getCleanName(Mapper.getNodeName(child))) ) {
						mapper.addNode(childContainer, child, null);
					}
				}
			} else {
				// no children exist, we add
				JcrNode fileJcrNode = ReflectionUtils.getJcrNodeAnnotation(ReflectionUtils.getParameterizedClass(field));
				Node fileContainer = createFileFolderNode(fileJcrNode, nodeName, node, mapper);
				for ( int i = 0; i < children.size(); i++ ) {
					addFileNode(fileJcrNode, fileContainer, (JcrFile)children.get(i), mapper);
				}
			}
		} else {
			// field list is now null (or empty), so we remove the child nodes
			removeNodes(node, nodeName);
		}
	}
	
	private static void setFiles( Field field, Object obj, Node node, Mapper mapper, int depth, int maxDepth, NameFilter nameFilter ) 
			throws IllegalAccessException, RepositoryException, IOException {
		
		String nodeName = getNodeName(field);

		// make sure that this child is supposed to be updated
		if ( nameFilter == null || nameFilter.isIncluded(field.getName()) ) {
			if ( ReflectionUtils.implementsInterface(field.getType(), List.class) ) {
				// multiple file nodes in a List
				addMultipleFilesToNode(field, obj, nodeName, node, mapper, depth, maxDepth, nameFilter);
			} else {
				// single child
				addSingleFileToNode(field, obj, nodeName, node, mapper, depth, maxDepth, nameFilter);
			}
		}
	}
	
	private static <T extends JcrFile> void mapNodeToFileObject( JcrFileNode jcrFileNode, T fileObj, Node fileNode, 
			NameFilter nameFilter, int maxDepth, Object parentObject, int depth, Mapper mapper ) 
			throws ClassNotFoundException, InstantiationException, RepositoryException, IllegalAccessException, IOException {
		
		Node contentNode = fileNode.getNode("jcr:content");
		fileObj.setName(fileNode.getName());
		fileObj.setPath(fileNode.getPath());
		fileObj.setMimeType( contentNode.getProperty("jcr:mimeType").getString() );
		fileObj.setLastModified( contentNode.getProperty("jcr:lastModified").getDate() );
		if ( contentNode.hasProperty("jcr:encoding") ) {
			fileObj.setEncoding(contentNode.getProperty("jcr:encoding").getString());
		}
		
		// file data
		if ( jcrFileNode.loadType() == JcrFileNode.LoadType.BYTES ) {
			JcrDataProviderImpl dataProvider = new JcrDataProviderImpl(JcrDataProvider.TYPE.BYTES, Mapper.readBytes(contentNode.getProperty("jcr:data").getStream()));
			fileObj.setDataProvider(dataProvider);
		} else if ( jcrFileNode.loadType() == JcrFileNode.LoadType.STREAM ) {
			JcrDataProviderImpl dataProvider = new JcrDataProviderImpl(JcrDataProvider.TYPE.STREAM, contentNode.getProperty("jcr:data").getStream());
			fileObj.setDataProvider(dataProvider);
		}
		
		// if this is a JcrFile subclass, it may contain custom properties and 
		// child nodes that need to be mapped
		mapper.mapNodeToClass(fileObj, fileNode, nameFilter, maxDepth, parentObject, depth+1);
	}
	
	static void addFiles( Field field, Object obj, Node node, Mapper mapper ) 
			throws IllegalAccessException, RepositoryException, IOException {
		
		setFiles(field, obj, node, mapper, -1, -1, null);
	}
	
	static void updateFiles( Field field, Object obj, Node node, Mapper mapper, int depth, int maxDepth, NameFilter nameFilter ) 
			throws IllegalAccessException, RepositoryException, IOException {
		
		setFiles(field, obj, node, mapper, depth, maxDepth, nameFilter);
	}
	
	static void getFilesFromNode( Field field, Node node, Object obj, int depth, int maxDepth, NameFilter nameFilter, Mapper mapper ) 
			throws ClassNotFoundException, InstantiationException, RepositoryException, IllegalAccessException, IOException {
		
		String nodeName = getNodeName(field);
		JcrFileNode jcrFileNode = field.getAnnotation(JcrFileNode.class);

		if ( node.hasNode(nodeName) && nameFilter.isIncluded(field.getName()) ) {
			// file nodes are always stored inside a folder node
			Node fileContainer = node.getNode(nodeName);
			if ( ReflectionUtils.implementsInterface(field.getType(), List.class) ) {
				// we can expect more than one child object here
				List children = new ArrayList();
				Class childObjClass = ReflectionUtils.getParameterizedClass(field);
				NodeIterator iterator = fileContainer.getNodes();
				while ( iterator.hasNext() ) {
					JcrFile fileObj = (JcrFile)childObjClass.newInstance();
					mapNodeToFileObject(jcrFileNode, fileObj, iterator.nextNode(), nameFilter, maxDepth, obj, depth, mapper);
					children.add(fileObj);
				}
				field.set(obj, children);

			} else {
				// instantiate the field class
				JcrFile fileObj = (JcrFile)field.getType().newInstance();
				mapNodeToFileObject(jcrFileNode, fileObj, fileContainer.getNodes().nextNode(), nameFilter, maxDepth, obj, depth, mapper);
				field.set(obj, fileObj);
			}
		}
	}
}
