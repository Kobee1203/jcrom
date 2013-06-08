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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;

import org.junit.Test;
import org.modeshape.test.ModeShapeSingleUseTest;

/**
 *
 * @author Nicolas Dos Santos
 */
public class TestModeShape extends ModeShapeSingleUseTest {

    @Test
    public void testGetNodesMethod() throws Exception {
        Node jcrRootNode = ((Session) session).getRootNode();
        Node rootNode = jcrRootNode.addNode("mapSuperclassTest");
        Node newNode = rootNode.addNode("newNode");
        NodeIterator nodeIterator = rootNode.getNodes("myMap"); // Gets all child nodes that match 'myMap'
        assertTrue(nodeIterator.hasNext()); // It's not correct ! there should be an empty iterator !
        assertEquals(newNode.getName(), nodeIterator.nextNode().getName()); // The next node is 'newNode'
        session.save();
        nodeIterator = rootNode.getNodes("myMap");
        assertFalse(nodeIterator.hasNext()); // It's correct after saving
    }

    @Test
    public void mapsInSuperclass() throws Exception {

        Jcrom jcrom = new Jcrom(true, true);
        jcrom.map(EntityParent.class);
        jcrom.map(EntityChild.class);

        // create person
        Person person = new Person();
        person.setName("nico");
        person.setAge(30);
        person.setPhones(Arrays.asList(new String[] { "0123456789" }));

        Map<String, String> myMap = new HashMap<String, String>();
        myMap.put("key1", "value1");
        myMap.put("key2", "value2");

        EntityChild entity = new EntityChild();
        entity.setName("myChild");
        entity.setMyMap(myMap);
        entity.setPerson(person);

        Node jcrRootNode = ((Session) session).getRootNode();
        Node rootNode = jcrRootNode.addNode("mapSuperclassTest");
        Node newNode = jcrom.addNode(rootNode, entity);

        Node containerNode = newNode.addNode("person2", "{http://www.jcp.org/jcr/nt/1.0}unstructured");

        session.save();

        OutputStream out = new FileOutputStream(new File("target/jcr-export.xml"));
        session.exportSystemView(jcrRootNode.getPath(), out, false, false);

        EntityChild entityFromJcr = jcrom.fromNode(EntityChild.class, newNode);
        assertNotNull(entityFromJcr);
        assertNotNull(entityFromJcr.getMyMap());
        assertEquals(2, entityFromJcr.getMyMap().size());
        assertNotNull(entityFromJcr.getPerson());
        assertEquals(30, entityFromJcr.getPerson().getAge());
        assertEquals("nico", entityFromJcr.getPerson().getName());
    }
}
