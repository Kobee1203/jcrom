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
package org.jcrom.util;

import java.util.HashSet;
import java.util.Set;

/**
 * This class represents a simple filter that can be used to filter names.
 * The filter is either an inclusion or exclusion filter. The filter is 
 * constructed with a comma separated list of names, e.g. "photos,comments" 
 * (reads: include photos and comments).
 * An exclusion filter is built by prefixing the list with a minus sign, e.g.
 * "-photos" (reads: include everything apart from photos).
 * 
 * The isIncluded() method can then be used to check if a specific name
 * gets through the filter or not.
 * 
 * Two special values are recognized: "*" means isIncluded() always returns
 * true, and "none" means isIncluded() will always return false.
 * 
 * @author Olafur Gauti Gudmundsson
 */
public class NameFilter {

    private final String filterStr;
    private final boolean exclusion;
    private final boolean all;
    private final Set<String> names;

    public NameFilter(String filterStr) {
        this.filterStr = filterStr;
        names = new HashSet<String>();
        if (filterStr == null) {
            exclusion = false;
            all = true;
        } else {

            filterStr = filterStr.trim();
            if (filterStr.startsWith("-")) {
                filterStr = filterStr.substring(1);
                // exclusion filter
                exclusion = true;
                all = false;
                addToSet(filterStr);
            } else if (filterStr.equals(NodeFilter.EXCLUDE_ALL)) {
                exclusion = true;
                all = true;
            } else {
                // inclusion filter
                exclusion = false;
                if (filterStr.equals(NodeFilter.INCLUDE_ALL)) {
                    all = true;
                } else {
                    all = false;
                    addToSet(filterStr);
                }
            }
        }
    }

    public String getFilterStr() {
        return filterStr;
    }

    private void addToSet(String filterStr) {
        String[] nameArray = filterStr.split(",");
        for (String nodeName : nameArray) {
            names.add(nodeName.trim());
        }
    }

    /**
     * Check whether the name supplied gets through the filter.
     * 
     * @param name the name to check
     * @return true if the name gets through the filter, else false
     */
    public boolean isIncluded(String name) {
        if (exclusion) {
            if (all) {
                return false;
            } else {
                return !names.contains(name);
            }
        } else {
            if (all) {
                return true;
            } else {
                return names.contains(name);
            }
        }
    }
}
