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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
	
	private static Node createFileFolderNode( JcrNode jcrNode, String containerName, Node parentNode, Mapper mapper ) throws RepositoryException {
		
        if ( !parentNode.hasNode(mapper.getCleanName(containerName)) ) {
            if ( jcrNode != null && jcrNode.nodeType().equals("nt:unstructured") ) {
                return parentNode.addNode(mapper.getCleanName(containerName));
            } else {
                // assume it is an nt:file or extension of that, 
                // so we create an nt:folder
                return parentNode.addNode(mapper.getCleanName(containerName), "nt:folder");
            }
        } else {
            return parentNode.getNode(mapper.getCleanName(containerName));
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
        file.setName(fileNode.getName());
        file.setPath(fileNode.getPath());
        if ( fileNode.hasProperty("jcr:uuid") ) {
            Mapper.setUUID(file, fileNode.getUUID());
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
	
	private static void removeChildren( Node containerNode ) throws RepositoryException {
		NodeIterator nodeIterator = containerNode.getNodes();
		while ( nodeIterator.hasNext() ) {
			nodeIterator.nextNode().remove();
		}
	}
	
	private static void addSingleFileToNode( Field field, Object obj, String nodeName, Node node, Mapper mapper, 
			int depth, int maxDepth, NameFilter nameFilter ) 
			throws IllegalAccessException, RepositoryException, IOException {
		
        JcrNode fileJcrNode = ReflectionUtils.getJcrNodeAnnotation(field.getType());
        Node fileContainer = createFileFolderNode(fileJcrNode, nodeName, node, mapper);
        
		if ( !fileContainer.hasNodes() ) {
			if ( field.get(obj) != null ) {
				addFileNode(fileJcrNode, fileContainer, (JcrFile)field.get(obj), mapper);
			}
		} else {
			if ( field.get(obj) != null ) {
				updateFileNode(fileContainer.getNodes().nextNode(), (JcrFile)field.get(obj), nameFilter, maxDepth, depth, mapper);
			} else {
				// field is now null, so we remove the files
				removeChildren(fileContainer);
			}
		}
	}
    
    private static void updateFileList( List children, Node fileContainer, JcrNode fileJcrNode, Mapper mapper, int depth, int maxDepth, NameFilter nameFilter ) 
            throws IllegalAccessException, RepositoryException, IOException {

		if ( children != null && !children.isEmpty() ) {
			if ( fileContainer.hasNodes() ) {
				// children exist, we must update
				NodeIterator childNodes = fileContainer.getNodes();
				while ( childNodes.hasNext() ) {
					Node child = childNodes.nextNode();
					JcrFile childEntity = (JcrFile)Mapper.findEntityByPath(children, child.getPath());
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
                    String childPath = Mapper.getNodePath(child);
					if ( childPath == null || childPath.equals("") || !fileContainer.hasNode(mapper.getCleanName(Mapper.getNodeName(child))) ) {
                        addFileNode(fileJcrNode, fileContainer, (JcrFile)child, mapper);
					}
				}
			} else {
				// no children exist, we add
				for ( int i = 0; i < children.size(); i++ ) {
					addFileNode(fileJcrNode, fileContainer, (JcrFile)children.get(i), mapper);
				}
			}
		} else {
			// field list is now null (or empty), so we remove the file nodes
			removeChildren(fileContainer);
		}
    }
    
	
	private static void addMultipleFilesToNode( Field field, Object obj, String nodeName, Node node, Mapper mapper, 
			int depth, int maxDepth, NameFilter nameFilter ) 
			throws IllegalAccessException, RepositoryException, IOException {
		
        JcrNode fileJcrNode = ReflectionUtils.getJcrNodeAnnotation(ReflectionUtils.getParameterizedClass(field));
		Node fileContainer = createFileFolderNode(fileJcrNode, nodeName, node, mapper);
        
        List children = (List)field.get(obj);
        updateFileList(children, fileContainer, fileJcrNode, mapper, depth, maxDepth, nameFilter);
	}
    
    
	private static void addMapOfFilesToNode( Field field, Object obj, String nodeName, Node node, Mapper mapper, 
			int depth, int maxDepth, NameFilter nameFilter ) 
			throws IllegalAccessException, RepositoryException, IOException {
        
        Class fileClass;
        if ( ReflectionUtils.implementsInterface(ReflectionUtils.getParameterizedClass(field,1), List.class) ) {
            fileClass = ReflectionUtils.getTypeArgumentOfParameterizedClass(field, 1, 0);
        } else {
            fileClass = ReflectionUtils.getParameterizedClass(field,1);
        }
        
        JcrNode fileJcrNode = ReflectionUtils.getJcrNodeAnnotation(fileClass);
        String cleanName = mapper.getCleanName(nodeName);
		Node fileContainer = node.hasNode(cleanName) ? node.getNode(cleanName) : node.addNode(cleanName); // this is just a nt:unstructured node
        
        Map children = (Map)field.get(obj);
        if ( children != null && !children.isEmpty() ) {
            Class paramClass = ReflectionUtils.getParameterizedClass(field,1);
            if ( fileContainer.hasNodes() ) {
                // nodes already exist, we need to update
                Map mapWithCleanKeys = new HashMap();
                Iterator it = children.keySet().iterator();
                while ( it.hasNext() ) {
                    String key = (String)it.next();
                    String cleanKey = mapper.getCleanName(key);
                    if ( fileContainer.hasNode(cleanKey) ) {
                        if ( ReflectionUtils.implementsInterface(paramClass, List.class) ) {
                            // update the file list
                            List childList = (List) children.get(key);
                            Node listContainer = createFileFolderNode(fileJcrNode, cleanKey, fileContainer, mapper);
                            updateFileList(childList, listContainer, fileJcrNode, mapper, depth, maxDepth, nameFilter);
                        } else {
                            // update the file
                            updateFileNode(fileContainer.getNode(cleanKey), (JcrFile)children.get(key), nameFilter, maxDepth, depth, mapper);
                        }
                    } else {
                        // this child does not exist, so we add it
                        addMapFile(paramClass, fileJcrNode, fileContainer, children, key, mapper);
                    }
                    mapWithCleanKeys.put(cleanKey, "1");
                }
                
                // remove nodes that no longer exist
				NodeIterator childNodes = fileContainer.getNodes();
				while ( childNodes.hasNext() ) {
					Node child = childNodes.nextNode();
                    if ( !mapWithCleanKeys.containsKey(child.getName()) ) {
                        child.remove();
                    }
                }
            } else {
                // no children exist, we simply add all
                Iterator it = children.keySet().iterator();
                while ( it.hasNext() ) {
                    String key = (String)it.next();
                    addMapFile(paramClass, fileJcrNode, fileContainer, children, key, mapper);
                }
            }
            
		} else {
			// field list is now null (or empty), so we remove the file nodes
			removeChildren(fileContainer);
        }
    }
    
    private static void addMapFile( Class paramClass, JcrNode fileJcrNode, Node fileContainer, Map childMap, String key, Mapper mapper ) 
            throws IllegalAccessException, RepositoryException, IOException {
        
        if ( ReflectionUtils.implementsInterface(paramClass, List.class) ) {
            List childList = (List) childMap.get(key);
            Node listContainer = createFileFolderNode(fileJcrNode, mapper.getCleanName(key), fileContainer, mapper);
            for ( int i = 0; i < childList.size(); i++ ) {
                addFileNode(fileJcrNode, listContainer, (JcrFile)childList.get(i), mapper);
            }
        } else {
            addFileNode(fileJcrNode, fileContainer, (JcrFile)childMap.get(key), mapper);
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
            } else if ( ReflectionUtils.implementsInterface(field.getType(), Map.class) ) {
                // dynamic map of file nodes
                addMapOfFilesToNode(field, obj, nodeName, node, mapper, depth, maxDepth, nameFilter);
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
    
    static List getFileList( Class childObjClass, Node fileContainer, Object obj, JcrFileNode jcrFileNode, int depth, int maxDepth, NameFilter nameFilter, Mapper mapper ) 
            throws ClassNotFoundException, InstantiationException, RepositoryException, IllegalAccessException, IOException {
        List children = new ArrayList();
        NodeIterator iterator = fileContainer.getNodes();
        while ( iterator.hasNext() ) {
            JcrFile fileObj = (JcrFile)childObjClass.newInstance();
            mapNodeToFileObject(jcrFileNode, fileObj, iterator.nextNode(), nameFilter, maxDepth, obj, depth, mapper);
            children.add(fileObj);
        }
        return children;
    }
    
    static JcrFile getSingleFile( Class childObjClass, Node fileContainer, Object obj, JcrFileNode jcrFileNode, int depth, int maxDepth, NameFilter nameFilter, Mapper mapper ) 
            throws ClassNotFoundException, InstantiationException, RepositoryException, IllegalAccessException, IOException {
        JcrFile fileObj = (JcrFile)childObjClass.newInstance();
        mapNodeToFileObject(jcrFileNode, fileObj, fileContainer.getNodes().nextNode(), nameFilter, maxDepth, obj, depth, mapper);
        return fileObj;
    }
    
    private static Map getFileMap( Field field, Node fileContainer, JcrFileNode jcrFileNode, Object obj, int depth, int maxDepth, NameFilter nameFilter, Mapper mapper ) 
            throws ClassNotFoundException, InstantiationException, RepositoryException, IllegalAccessException, IOException {
        
        Class mapParamClass = ReflectionUtils.getParameterizedClass(field,1);
        Map children = new HashMap();
        NodeIterator iterator = fileContainer.getNodes();
        while ( iterator.hasNext() ) {
            Node childNode = iterator.nextNode();
            if ( ReflectionUtils.implementsInterface(mapParamClass, List.class) ) {
                Class childObjClass = ReflectionUtils.getTypeArgumentOfParameterizedClass(field, 1, 0);
                if ( jcrFileNode.lazy() ) {
                    // lazy loading
                    children.put(childNode.getName(), ProxyFactory.createFileNodeListProxy(childObjClass, fileContainer, obj, jcrFileNode, depth, maxDepth, nameFilter, mapper));
                } else {
                    children.put(childNode.getName(), getFileList(childObjClass, childNode, obj, jcrFileNode, depth, maxDepth, nameFilter, mapper));
                }
            } else {
                if ( jcrFileNode.lazy() ) {
                    // lazy loading
                    children.put(childNode.getName(), ProxyFactory.createFileNodeProxy(mapParamClass, fileContainer, obj, jcrFileNode, depth, maxDepth, nameFilter, mapper));
                } else {
                    children.put(childNode.getName(), getSingleFile(mapParamClass, fileContainer, obj, jcrFileNode, depth, maxDepth, nameFilter, mapper));
                }
            }
        }
        return children;
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
				List children;
				Class childObjClass = ReflectionUtils.getParameterizedClass(field);
                if ( jcrFileNode.lazy() ) {
                    // lazy loading
                    children = ProxyFactory.createFileNodeListProxy(childObjClass, fileContainer, obj, jcrFileNode, depth, maxDepth, nameFilter, mapper);
                } else {
                    // eager loading
                    children = getFileList(childObjClass, fileContainer, obj, jcrFileNode, depth, maxDepth, nameFilter, mapper);
                }
				field.set(obj, children);
                
            } else if ( ReflectionUtils.implementsInterface(field.getType(), Map.class) ) {
                // dynamic map of child nodes
                // lazy loading is applied to each value in the Map
                field.set(obj, getFileMap(field, fileContainer, jcrFileNode, obj, depth, maxDepth, nameFilter, mapper));
                
			} else {
				// instantiate the field class
                if ( fileContainer.hasNodes() ) {
                    if ( jcrFileNode.lazy() ) {
                        // lazy loading
                        field.set(obj, ProxyFactory.createFileNodeProxy(field.getType(), fileContainer, obj, jcrFileNode, depth, maxDepth, nameFilter, mapper));
                    } else {
                        // eager loading
                        field.set(obj, getSingleFile(field.getType(), fileContainer, obj, jcrFileNode, depth, maxDepth, nameFilter, mapper));
                    }
                }
			}
		}
	}
}
