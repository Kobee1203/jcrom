package org.jcrom;

import java.io.File;
import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import org.apache.jackrabbit.core.TransientRepository;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Olafur Gauti Gudmundsson
 */
public class TestLazyLoading {

	private Repository repo;
	private Session session;
	
	@Before
	public void setUpRepository() throws Exception {
		repo = (Repository) new TransientRepository();
		session = repo.login(new SimpleCredentials("a", "b".toCharArray()));
	}
	
	@After
	public void tearDownRepository() throws Exception {
		session.logout();
		deleteDir(new File("repository"));
		new File("repository.xml").delete();
		new File("derby.log").delete();
	}
	
    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
    
        // The directory is now empty so delete it
        return dir.delete();
    }
	
	@Test
	public void testLazyLoading() throws Exception {
		
		Jcrom jcrom = new Jcrom(true, true);
		jcrom.map(Tree.class).map(LazyObject.class);
		
		TreeNode homeNode = new TreeNode();
		homeNode.setName("home");
		
		TreeNode newsNode = new TreeNode();
		newsNode.setName("news");
		
		TreeNode productsNode = new TreeNode();
		productsNode.setName("products");
		
		TreeNode templateNode = new TreeNode();
		templateNode.setName("template");
		
		homeNode.addChild(newsNode);
		homeNode.addChild(productsNode);
		
		LazyInterface lazyObject1 = new LazyObject();
		lazyObject1.setName("one");
		lazyObject1.setString("a");
		
		LazyInterface lazyObject2 = new LazyObject();
		lazyObject2.setName("two");
		lazyObject2.setString("b");
		
		Tree tree = new Tree();
		tree.setName("Tree");
		tree.setPath("/");
		tree.addChild(homeNode);
		tree.setTemplateNode(templateNode);
		
		tree.setLazyObject(lazyObject1);
		tree.addLazyObject(lazyObject1);
		tree.addLazyObject(lazyObject2);
		
		Node treeRootNode = jcrom.addNode(session.getRootNode(), tree);

		Tree fromNode = jcrom.fromNode(Tree.class, treeRootNode);		
		assertTrue( fromNode.getChildren().size() == tree.getChildren().size() );
		assertTrue( fromNode.getTemplateNode().getName().equals(templateNode.getName()) );
		
		assertTrue( fromNode.getLazyObject().getString().equals(lazyObject1.getString()) );
		assertTrue( fromNode.getLazyObjects().size() == tree.getLazyObjects().size() );
		assertTrue( fromNode.getLazyObjects().get(1).getString().equals(lazyObject2.getString()) );
        
        assertTrue( fromNode.getStartNode() == null );
		
		TreeNode homeFromNode = fromNode.getChildren().get(0);
		assertTrue( homeFromNode.getChildren().size() == homeNode.getChildren().size() );
		assertTrue( homeFromNode.getChildren().get(0).getName().equals(newsNode.getName()) );
        
		// add references
		fromNode.addFavourite(newsNode);
		fromNode.setStartNode(productsNode);
		
		jcrom.updateNode(treeRootNode, fromNode);
		
		Tree modifiedFromNode = jcrom.fromNode(Tree.class, treeRootNode);
		assertTrue( modifiedFromNode.getFavourites().size() == fromNode.getFavourites().size() );
		assertTrue( modifiedFromNode.getStartNode().getName().equals(productsNode.getName()) );
        assertTrue( modifiedFromNode.getStartNode().getChildren().size() == productsNode.getChildren().size() );
	}

}
