/**
 * This file is part of the JCROM project.
 * Copyright (C) 2008-2014 - All rights reserved.
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
package org.jcrom.jackrabbit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.awt.Color;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.jcrom.Jcrom;
import org.jcrom.entities.EntityWithConverter;
import org.junit.Test;

/**
 * 
 * @author Nicolas Dos Santos
 */
public class TestConverter extends TestAbstract {

    @Test
    public void testConverter() throws RepositoryException {
        Jcrom jcrom = new Jcrom(true, true);
        jcrom.map(EntityWithConverter.class);

        Node rootNode = session.getRootNode().addNode("converterTest");

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

        Node newNode = jcrom.addNode(rootNode, entity);
        session.save();

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
    }
}
