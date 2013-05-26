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
package org.jcrom.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;

/**
 *
 * @author Olafur Gauti Gudmundsson
 */
public class NodeFilter implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final int DEPTH_INFINITE = -1;

    public static final String INCLUDE_ALL = "*";
    public static final String EXCLUDE_ALL = "none";

    public static final String PROPERTY_PREFIX = "prop:";

    protected final NameFilter nameFilter;
    protected final int maxDepth;
    protected final int filterDepth;

    private final List<String> nodeNameStack = new ArrayList<String>();

    /**
     * 
     * @param fieldNameFilter comma separated list of names of child nodes / references to load 
     * ("*" loads all, while "none" loads none, and a prefix of "-" excludes the named nodes)
     * @param maxDepth the maximum depth of loaded child nodes (0 means no child nodes are loaded,
     * while a negative value means that no restrictions are set on the depth).
     * @param filterDepth the depth down to which the filter applies. If we are deeper than this, 
     * then the filter will be ignored. This is useful for example when you want to filter immediate children, 
     * but then load everything below unfiltered.
     */
    public NodeFilter(String fieldNameFilter, int maxDepth, int filterDepth) {
        this.nameFilter = new NameFilter(fieldNameFilter);
        this.maxDepth = maxDepth;
        this.filterDepth = filterDepth;
    }

    public NodeFilter(String fieldNameFilter, int maxDepth) {
        this(fieldNameFilter, maxDepth, DEPTH_INFINITE);
    }

    public NodeFilter(String fieldNameFilter) {
        this(fieldNameFilter, DEPTH_INFINITE, DEPTH_INFINITE);
    }

    public NodeFilter(int maxDepth) {
        this(INCLUDE_ALL, maxDepth, DEPTH_INFINITE);
    }

    public boolean isIncluded(String name, int depth) {
        // return isDepthIncluded(depth) && isNameIncluded(name);
        boolean isIncluded = isDepthIncluded(depth) && isNameIncluded(name);
        if (isIncluded) {
            int size = nodeNameStack.size();
            if (depth < size) {
                List<String> tmp = new ArrayList<String>();
                for (int i = 0; i < depth; i++) {
                    tmp.add(nodeNameStack.get(i));
                }
                tmp.add(name);
                nodeNameStack.clear();
                nodeNameStack.addAll(tmp);
            } else if (depth == size) {
                nodeNameStack.add(name);
            } else {
                throw new IndexOutOfBoundsException("Could not add node name: Index: " + depth + ", Size: " + size);
            }
        }
        return isIncluded;
    }

    public boolean isIncluded(String propName, Node parentNode, int depth) {
        boolean isIncluded = isDepthPropertyIncluded(depth) && isNameIncluded(propName);
        if (!isIncluded) {
            try {
                Node rootNode = parentNode.getSession().getRootNode();
                Node parent = parentNode;
                while (parent != null && !rootNode.getIdentifier().equals(parent.getIdentifier())) {
                    String parentName = parent.getName();
                    // Check whether one of parent node is included
                    for (String nodeName : nodeNameStack) {
                        if (nodeName.equals(parentName)) {
                            isIncluded = true;
                            break;
                        }
                    }
                    parent = parent.getParent();
                }
            } catch (Exception e) {
                System.err.println(e.getMessage());
                e.printStackTrace();
            }
        }
        return isIncluded;
    }

    public boolean isDepthIncluded(int depth) {
        if (filterDepth > DEPTH_INFINITE && depth > filterDepth) {
            return true;
        } else {
            return maxDepth == DEPTH_INFINITE || depth < maxDepth;
        }
    }

    public boolean isDepthPropertyIncluded(int depth) {
        if (filterDepth > DEPTH_INFINITE && depth >= filterDepth) {
            return true;
        } else {
            return maxDepth == DEPTH_INFINITE || depth <= maxDepth;
        }
    }

    public boolean isNameIncluded(String name) {
        return nameFilter.isIncluded(name);
    }

    public int getFilterDepth() {
        return filterDepth;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public NameFilter getNameFilter() {
        return nameFilter;
    }
}
