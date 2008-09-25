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
package org.jcrom.util;

import java.io.Serializable;

/**
 *
 * @author Olafur Gauti Gudmundsson
 */
public class NodeFilter implements Serializable {
    
    public static final int DEPTH_INFINITE = -1;
    
    public static final String INCLUDE_ALL = "*";
    public static final String EXCLUDE_ALL = "none";

    protected final NameFilter nameFilter;
    protected final int maxDepth;
    protected final int filterDepth;
    
    /**
     * 
	 * @param fieldNameFilter comma separated list of names of child nodes / references to load 
	 * ("*" loads all, while "none" loads none, and a prefix of "-" excludes the named nodes)
	 * @param maxDepth the maximum depth of loaded child nodes (0 means no child nodes are loaded,
	 * while a negative value means that no restrictions are set on the depth).
     * @param filterDepth
     */
    public NodeFilter( String fieldNameFilter, int maxDepth, int filterDepth ) {
        this.nameFilter = new NameFilter(fieldNameFilter);
        this.maxDepth = maxDepth;
        this.filterDepth = filterDepth;
    }
    
    public NodeFilter( String fieldNameFilter, int maxDepth ) {
        this(fieldNameFilter, maxDepth, DEPTH_INFINITE);
    }
    
    public NodeFilter( String fieldNameFilter ) {
        this(fieldNameFilter, DEPTH_INFINITE, DEPTH_INFINITE);
    }
    
    public NodeFilter( int maxDepth ) {
        this(INCLUDE_ALL, maxDepth, DEPTH_INFINITE);
    }
    
    public boolean isIncluded( String name, int depth ) {
        if ( filterDepth > DEPTH_INFINITE && depth > filterDepth ) {
            return true;
        } else {
            return (maxDepth == DEPTH_INFINITE || depth < maxDepth) && nameFilter.isIncluded(name);
        }
    }
    
    public boolean isDepthIncluded( int depth ) {
        if ( filterDepth > DEPTH_INFINITE && depth > filterDepth ) {
            return true;
        } else {
            return maxDepth == DEPTH_INFINITE || depth < maxDepth;
        }
    }
    
    public boolean isNameIncluded( String name ) {
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
