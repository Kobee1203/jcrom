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
package org.jcrom.modeshape;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.UUID;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;

import org.jcrom.JcrMappingException;
import org.jcrom.Jcrom;
import org.jcrom.annotations.JcrNode;
import org.jcrom.callback.DefaultJcromCallback;
import org.jcrom.entities.Parent;
import org.jcrom.util.JcrUtils;
import org.jcrom.util.PathUtils;
import org.junit.Test;
import org.modeshape.test.ModeShapeSingleUseTest;

/**
 * 
 * @author Nicolas Dos Santos
 */
public class TestJcromCallback extends ModeShapeSingleUseTest {

    private Parent createParent(String name) {
        Parent parent = new Parent();
        parent.setTitle(name);
        parent.setBirthDay(new Date());
        parent.setDrivingLicense(true);
        parent.setFingers(10);
        parent.setHairs(0L);
        parent.setHeight(1.80);
        parent.setWeight(83.54);
        parent.setNickName("Daddy");

        parent.addTag("father");
        parent.addTag("parent");
        parent.addTag("male");

        return parent;
    }

    @Test
    public void testJcromCallback() throws RepositoryException {
        Jcrom jcrom = new Jcrom();
        jcrom.map(Parent.class);

        Node rootNode = ((Session) session).getRootNode().addNode("root");

        String name = "John Doe";
        String uuid = UUID.randomUUID().toString();

        // --------------------------------
        // Create a node without callback
        // --------------------------------
        Parent parent = createParent(name);
        parent.setUuid(uuid);
        parent.setPath(name);

        Node newNode = jcrom.addNode(rootNode, parent);
        assertNotNull(newNode);

        Parent fromNode = jcrom.fromNode(Parent.class, newNode);
        assertNotNull(fromNode);
        assertEquals(PathUtils.createValidName(name), fromNode.getName());
        assertNotSame(uuid, fromNode.getUuid()); // UUID not same

        // --------------------------------
        // Create a node with callback
        // --------------------------------
        parent = createParent(name);
        parent.setUuid(uuid);
        parent.setPath(name);

        newNode = jcrom.addNode(rootNode, parent, null, new DefaultJcromCallback(jcrom) {
            @Override
            public Node doAddNode(Node parentNode, String nodeName, JcrNode jcrNode, Object entity) throws RepositoryException {
                if (!("org.modeshape.jcr.JcrNode".equals(parentNode.getClass().getCanonicalName())) || !(entity instanceof Parent)) {
                    return super.doAddNode(parentNode, nodeName, jcrNode, entity);
                }
                System.out.println("add node in callback");
                try {
                    Parent parentEntity = (Parent) entity;
                    Method m = parentNode.getClass().getSuperclass().getDeclaredMethod("canAddNode", String.class, String.class);
                    m.setAccessible(true);
                    boolean canAddNode = (Boolean) m.invoke(parentNode, parentEntity.getPath(), null);
                    assertTrue(canAddNode);
                } catch (Exception e) {
                    e.printStackTrace();
                    fail(e.getMessage());
                }
                return super.doAddNode(parentNode, nodeName, jcrNode, entity);
            }
        });
        assertNotNull(newNode);

        fromNode = jcrom.fromNode(Parent.class, newNode);
        assertNotNull(fromNode);
        assertEquals(PathUtils.createValidName(name), fromNode.getName());

        Node updateNode = jcrom.updateNode(newNode, fromNode, null, new DefaultJcromCallback(jcrom) {
            @Override
            public void doComplete(Object entity, Node node) throws JcrMappingException, RepositoryException {
                if (!(entity instanceof Parent)) {
                    super.doComplete(entity, node);
                }
                System.out.println("complete entity in callback");
                Parent parent = (Parent) entity;
                parent.setFingers(5);
                Value value = JcrUtils.createValue(int.class, 5, node.getSession().getValueFactory());
                node.setProperty("fingers", value);
            }
        });
        assertNotNull(updateNode);

        fromNode = jcrom.fromNode(Parent.class, updateNode);
        assertNotNull(fromNode);
        assertEquals(PathUtils.createValidName(name), fromNode.getName());
        assertEquals(5, fromNode.getFingers()); // update in callback: 5 instead of the defauolt value 10
    }
}
