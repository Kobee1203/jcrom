/**
 * This file is part of the JCROM project.
 * Copyright (C) 2008-2015 - All rights reserved.
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
package org.jcrom.modeshape;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.awt.Color;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;

import org.jcrom.Jcrom;
import org.jcrom.entities.EntityWithConverter;
import org.jcrom.entities.EntityWithConverterChild;
import org.junit.Test;
import org.modeshape.test.ModeShapeSingleUseTest;

/**
 * 
 * @author Nicolas Dos Santos
 */
public class TestConverter extends ModeShapeSingleUseTest {

    @Test
    public void testConverter() throws RepositoryException {
        Jcrom jcrom = new Jcrom(true, true);
        jcrom.map(EntityWithConverter.class);

        Node rootNode = ((Session) session).getRootNode().addNode("converterTest");

        // Create entity
        EntityWithConverter entity = new EntityWithConverter();
        entity.setName("entity_converter");
        entity.setColor(new Color(50, 100, 150, 200));
        entity.setColors(Arrays.asList(new Color(0, 0, 0, 0), new Color(255, 255, 255, 255), new Color(50, 100, 150, 200)));
        try {
            entity.setUrl(new URL("https://code.google.com/p/jcrom/"));
            Map<String, URL> map = new HashMap<String, URL>();
            map.put("Google", new URL("http://www.google.fr"));
            map.put("Jcrom", new URL("https://code.google.com/p/jcrom/"));
            map.put("Joys of Code", new URL("http://thejoysofcode.tumblr.com/"));
            entity.setUrlMap(map);
        } catch (MalformedURLException e) {
            fail(e.toString());
        }
        entity.setExpression("*");
        List<EntityWithConverterChild> children = Arrays.asList(new EntityWithConverterChild("entity_converter_child1", "*"));
        entity.setChildren(children);

        // Create new node
        Node newNode = jcrom.addNode(rootNode, entity);
        session.save();

        // Check values of the new node
        assertEquals("50:100:150:200", newNode.getProperty("color").getString());
        Value[] values = newNode.getProperty("colors").getValues();
        assertNotNull(values);
        assertEquals(3, values.length);
        assertEquals("0:0:0:0", values[0].getString());
        assertEquals("255:255:255:255", values[1].getString());
        assertEquals("50:100:150:200", values[2].getString());
        assertEquals("https://code.google.com/p/jcrom/", newNode.getProperty("url").getString());
        NodeIterator nodeIterator = newNode.getNodes("urlMap");
        Node nextNode = nodeIterator.nextNode();
        assertEquals("http://www.google.fr", nextNode.getProperty("Google").getString());
        assertEquals("https://code.google.com/p/jcrom/", nextNode.getProperty("Jcrom").getString());
        assertEquals("http://thejoysofcode.tumblr.com/", nextNode.getProperty("Joys of Code").getString());
        assertEquals("#ALL#", newNode.getProperty("expression").getString());
        assertEquals("#ALL#", newNode.getNodes("children").nextNode().getNodes("entity_converter_child1").nextNode().getProperty("expression").getString());

        // Get entity from the new node
        EntityWithConverter fromNode = jcrom.fromNode(EntityWithConverter.class, newNode);
        assertEquals(new Color(50, 100, 150, 200), fromNode.getColor());
        assertNotNull(fromNode.getColors());
        assertEquals(3, fromNode.getColors().size());
        assertEquals(new Color(0, 0, 0, 0), fromNode.getColors().get(0));
        assertEquals(new Color(255, 255, 255, 255), fromNode.getColors().get(1));
        assertEquals(new Color(50, 100, 150, 200), fromNode.getColors().get(2));
        try {
            assertEquals(new URL("https://code.google.com/p/jcrom/"), fromNode.getUrl());
            Map<String, URL> urlMap = fromNode.getUrlMap();
            assertNotNull(urlMap);
            assertEquals(3, urlMap.size());
            assertEquals(new URL("http://www.google.fr"), urlMap.get("Google"));
            assertEquals(new URL("https://code.google.com/p/jcrom/"), urlMap.get("Jcrom"));
            assertEquals(new URL("http://thejoysofcode.tumblr.com/"), urlMap.get("Joys of Code"));
        } catch (MalformedURLException e) {
            fail(e.toString());
        }
        assertEquals("*", fromNode.getExpression());
        assertEquals("*", fromNode.getChildren().get(0).getExpression());

        // Update entity
        fromNode.setColor(new Color(25, 75, 125, 150));
        fromNode.getColors().add(new Color(25, 75, 125, 150));
        try {
            fromNode.setUrl(new URL("https://code.google.com/"));
            fromNode.getUrlMap().remove("Google");
            fromNode.getUrlMap().put("Google Code", new URL("https://code.google.com/"));
        } catch (MalformedURLException e) {
            fail(e.toString());
        }
        fromNode.setExpression("**");
        fromNode.getChildren().add(new EntityWithConverterChild("entity_converter_child2", "**"));

        // Update values of the node
        Node updateNode = jcrom.updateNode(newNode, fromNode);

        // Check values of the new node
        assertEquals("25:75:125:150", updateNode.getProperty("color").getString());
        values = updateNode.getProperty("colors").getValues();
        assertNotNull(values);
        assertEquals(4, values.length);
        assertEquals("0:0:0:0", values[0].getString());
        assertEquals("255:255:255:255", values[1].getString());
        assertEquals("50:100:150:200", values[2].getString());
        assertEquals("25:75:125:150", values[3].getString());
        assertEquals("https://code.google.com/", updateNode.getProperty("url").getString());
        nodeIterator = updateNode.getNodes("urlMap");
        nextNode = nodeIterator.nextNode();
        assertEquals("https://code.google.com/p/jcrom/", nextNode.getProperty("Jcrom").getString());
        assertEquals("http://thejoysofcode.tumblr.com/", nextNode.getProperty("Joys of Code").getString());
        assertEquals("https://code.google.com/", nextNode.getProperty("Google Code").getString());
        assertEquals("#ALL##ALL#", updateNode.getProperty("expression").getString());
        assertEquals("#ALL#", updateNode.getNodes("children").nextNode().getNodes("entity_converter_child1").nextNode().getProperty("expression").getString());
        assertEquals("#ALL##ALL#", updateNode.getNodes("children").nextNode().getNodes("entity_converter_child2").nextNode().getProperty("expression").getString());

        // Get entity from the new node
        fromNode = jcrom.fromNode(EntityWithConverter.class, updateNode);
        assertEquals(new Color(25, 75, 125, 150), fromNode.getColor());
        assertNotNull(fromNode.getColors());
        assertEquals(4, fromNode.getColors().size());
        assertEquals(new Color(0, 0, 0, 0), fromNode.getColors().get(0));
        assertEquals(new Color(255, 255, 255, 255), fromNode.getColors().get(1));
        assertEquals(new Color(50, 100, 150, 200), fromNode.getColors().get(2));
        assertEquals(new Color(25, 75, 125, 150), fromNode.getColors().get(3));
        try {
            assertEquals(new URL("https://code.google.com/"), fromNode.getUrl());
            Map<String, URL> urlMap = fromNode.getUrlMap();
            assertNotNull(urlMap);
            assertEquals(3, urlMap.size());
            assertEquals(new URL("https://code.google.com/p/jcrom/"), urlMap.get("Jcrom"));
            assertEquals(new URL("http://thejoysofcode.tumblr.com/"), urlMap.get("Joys of Code"));
            assertEquals(new URL("https://code.google.com/"), urlMap.get("Google Code"));
        } catch (MalformedURLException e) {
            fail(e.toString());
        }
        assertEquals("**", fromNode.getExpression());
        assertEquals(2, fromNode.getChildren().size());
        assertEquals("*", fromNode.getChildren().get(0).getExpression());
        assertEquals("**", fromNode.getChildren().get(1).getExpression());
    }
}
