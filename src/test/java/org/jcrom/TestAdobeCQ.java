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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileReader;
import java.net.URL;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;

import org.apache.jackrabbit.commons.cnd.CndImporter;
import org.jcrom.util.PathUtils;
import org.junit.Test;

/**
 * @author Nicolas Dos Santos
 *
 */
public class TestAdobeCQ extends TestAbstract {

    @Override
    public void setUpRepository() throws Exception {
        super.setUpRepository();
        /*
        session.getWorkspace().getNamespaceRegistry().registerNamespace("sling", "http://sling.apache.org/jcr/sling/1.0");
        session.getWorkspace().getNamespaceRegistry().registerNamespace("cq", "http://www.day.com/jcr/cq/1.0");

        // Retrieve node type manager from the session
        NodeTypeManager nodeTypeManager = session.getWorkspace().getNodeTypeManager();

        // Create node type
        NodeTypeTemplate nodeType = nodeTypeManager.createNodeTypeTemplate();
        nodeType.setName("cq:PageContent");

        // Create a new property
        PropertyDefinitionTemplate customProperty = nodeTypeManager.createPropertyDefinitionTemplate();
        customProperty.setName("cq:template");
        customProperty.setRequiredType(PropertyType.STRING);
        // Add property to node type
        nodeType.getPropertyDefinitionTemplates().add(customProperty);

        nodeTypeManager.registerNodeType(nodeType, true);
        */
        URL cndResource = getClass().getResource("/nodetypes.cnd");
        FileReader cnd = null;
        try {
            cnd = new FileReader(new File(cndResource.toURI()));
            NodeType[] nodeTypes = CndImporter.registerNodeTypes(cnd, session);
            for (NodeType nt : nodeTypes) {
                System.out.println("Registered: " + nt.getName());
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        } finally {
            if (cnd != null) {
                cnd.close();
            }
        }
    }

    private PageContentNode createPageContentNode(String name, String title, String description, String resourceType, String[] tags) {
        PageContentNode pcn = new PageContentNode();
        pcn.setName(name);
        pcn.setTitle(title);
        pcn.setDescription(description);
        pcn.setResourceType(resourceType);
        pcn.setTags(tags);
        return pcn;
    }

    @Test
    public void testAdobeCQ() throws RepositoryException {
        Jcrom jcrom = new Jcrom();
        jcrom.map(PageContentNode.class);

        Node rootNode = session.getRootNode().addNode("root");

        String name = "Page Content Node 1";
        String title = "this is page content node 1";
        String description = "Description: page content node 1";
        String resourceType = "resourceType 1";
        String[] tags = new String[] { "new tag 1", "new tag 2" };
        PageContentNode pcn = createPageContentNode(name, title, description, resourceType, tags);

        Node newNode = jcrom.addNode(rootNode, pcn);
        assertNotNull(newNode);

        PageContentNode fromNode = jcrom.fromNode(PageContentNode.class, newNode);
        assertNotNull(fromNode);
        assertEquals(PathUtils.createValidName(name), fromNode.getName());
        assertEquals(title, fromNode.getTitle());
        assertEquals(description, fromNode.getDescription());
        assertEquals(resourceType, fromNode.getResourceType());
        assertNotNull(fromNode.getTags());
        assertEquals(tags.length, fromNode.getTags().length);
    }
}
