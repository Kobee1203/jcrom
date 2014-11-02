/*
 * This file is part of the Weedow jcrom (R) project.
 * Copyright (c) 2010-2014 Weedow Software Corp.
 * Authors: Nicolas Dos Santos
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License version 3
 * as published by the Free Software Foundation with the addition of the
 * following permission added to Section 15 as permitted in Section 7(a):
 * FOR ANY PART OF THE COVERED WORK IN WHICH THE COPYRIGHT IS OWNED BY Weedow, 
 * Weedow DISCLAIMS THE WARRANTY OF NON INFRINGEMENT OF THIRD PARTY RIGHTS.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA, 02110-1301 USA, or download the license from the following URL:
 * http://www.weedow.com/terms-of-use/
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License.
 *
 * You can be released from the requirements of the license by purchasing
 * a commercial license. Buying such a license is mandatory as soon as you
 * develop commercial activities involving this program without disclosing
 * the source code of your own applications.
 * These activities include: offering paid services to customers as an ASP, 
 * serving this program in a web application, shipping this program with a closed
 * source product.
 *
 * For more information, please contact Weedow Software Corp. at this
 * address: nicolas.dossantos@gmail.com
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
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;

import org.jcrom.Jcrom;
import org.jcrom.entities.EntityWithConverter;
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
