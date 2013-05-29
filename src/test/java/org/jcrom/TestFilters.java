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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.jcrom.util.NameFilter;
import org.jcrom.util.NodeFilter;
import org.junit.Test;

/**
 *
 * @author Olafur Gauti Gudmundsson
 */
public class TestFilters {

    @Test
    public void testInclusionFilter() {

        NameFilter incFilter = new NameFilter("comments,images");
        assertTrue(incFilter.isIncluded("comments"));
        assertTrue(incFilter.isIncluded("images"));
        assertFalse(incFilter.isIncluded("something"));

        NameFilter allowAll = new NameFilter(NodeFilter.INCLUDE_ALL);
        assertTrue(allowAll.isIncluded("something"));
    }

    @Test
    public void testExclusionFilter() {

        NameFilter excFilter = new NameFilter("-comments,images");
        assertTrue(excFilter.isIncluded("something"));
        assertFalse(excFilter.isIncluded("comments"));
        assertFalse(excFilter.isIncluded("images"));

        NameFilter allowNone = new NameFilter(NodeFilter.EXCLUDE_ALL);
        assertFalse(allowNone.isIncluded("something"));
        assertFalse(allowNone.isIncluded("none"));
    }
}
