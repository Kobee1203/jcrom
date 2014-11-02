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
import static org.junit.Assert.fail;

import java.io.File;
import java.net.URL;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeTypeDefinition;
import javax.jcr.nodetype.NodeTypeManager;

import org.jcrom.Jcrom;
import org.jcrom.entities.PageContentNode;
import org.jcrom.util.PathUtils;
import org.junit.Test;
import org.modeshape.common.collection.SimpleProblems;
import org.modeshape.jcr.CndImporter;
import org.modeshape.jcr.ExecutionContext;
import org.modeshape.test.ModeShapeSingleUseTest;

/**
 * 
 * @author Nicolas Dos Santos
 */
public class TestAdobeCQ extends ModeShapeSingleUseTest {

    @Override
    public void beforeEach() throws Exception {
        super.beforeEach();

        URL cndResource = getClass().getResource("/nodetypes-modeshape.cnd");
        File cnd = null;
        try {
            cnd = new File(cndResource.toURI());

            SimpleProblems problems = new SimpleProblems();
            ExecutionContext context = new ExecutionContext();
            context.getNamespaceRegistry().register("sling", "http://sling.apache.org/jcr/sling/1.0");
            context.getNamespaceRegistry().register("cq", "http://www.day.com/jcr/cq/1.0");

            // Set up the importer ...
            CndImporter importer = new CndImporter(context, true);
            importer.importFrom(cnd, problems);

            List<NodeTypeDefinition> ntds = importer.getNodeTypeDefinitions();
            // Retrieve node type manager from the session
            NodeTypeManager nodeTypeManager = ((Session) session).getWorkspace().getNodeTypeManager();
            nodeTypeManager.registerNodeTypes(ntds.toArray(new NodeTypeDefinition[] {}), true);
            for (NodeTypeDefinition ntd : ntds) {
                System.out.println("Registered: " + ntd.getName());
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
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

        Node rootNode = ((Session) session).getRootNode().addNode("root");

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
