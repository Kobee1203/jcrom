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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import javax.jcr.Node;

import org.jcrom.annotations.JcrChildNode;
import org.jcrom.annotations.JcrFileNode;
import org.jcrom.util.ReflectionUtils;
import org.junit.Test;

/**
 *
 * @author Olafur Gauti Gudmundsson
 * @author Nicolas Dos Santos
 */
public class TestReflection {

    @Test
    public void listClassesInPackage() throws Exception {

        Set<Class<?>> classes = ReflectionUtils.getClasses("org.jcrom.annotations");
        assertEquals(19, classes.size());
        assertTrue(classes.contains(JcrChildNode.class));
        assertTrue(classes.contains(JcrFileNode.LoadType.class));

        Set<Class<?>> jcrClasses = ReflectionUtils.getClasses("javax.jcr");

        for (Class<?> c : jcrClasses) {
            System.out.println(c.getName());
        }

        assertTrue(jcrClasses.contains(Node.class));

        Set<Class<?>> classesToMap = ReflectionUtils.getClasses("org.jcrom.dao");
        assertEquals(11, classesToMap.size());
    }
}
