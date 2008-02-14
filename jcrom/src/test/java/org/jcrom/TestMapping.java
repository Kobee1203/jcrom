/**
 * Copyright (C) Olafur Gauti Gudmundsson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jcrom;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.Repository;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import org.apache.jackrabbit.core.TransientRepository;
import org.junit.After;
import org.junit.Before;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author Olafur Gauti Gudmundsson
 */
public class TestMapping {
	
	private Repository repo;
	private Session session;
	
	@Before
	public void setUpRepository() throws Exception {
		repo = new TransientRepository();
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

	
	private Parent createParent( String name ) {
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
	
	private Child createChild( String name ) {
		Child child = new Child();
		child.setTitle(name);
		child.setBirthDay(new Date());
		child.setDrivingLicense(false);
		child.setFingers(11);
		child.setHairs(130000L);
		child.setHeight(1.93);
		child.setWeight(90.45);
		child.setNickName("Baby");
		return child;
	}
	
	private GrandChild createGrandChild( String name ) {
		GrandChild grandChild = new GrandChild();
		grandChild.setTitle(name);
		grandChild.setBirthDay(new Date());
		grandChild.setDrivingLicense(false);
		grandChild.setFingers(12);
		grandChild.setHairs(130000L);
		grandChild.setHeight(1.37);
		grandChild.setWeight(42.17);
		grandChild.setNickName("Zima");
		return grandChild;
	}
	
	private JcrFile createFile( String name ) {
		JcrFile jcrFile = new JcrFile();
		jcrFile.setName(name);
		jcrFile.setMimeType("image/jpeg");
		
		File imageFile = new File("src/test/resources/ogg.jpg");
		
		Calendar lastModified = Calendar.getInstance();
		lastModified.setTimeInMillis(imageFile.lastModified());
		jcrFile.setLastModified(lastModified);
		
		JcrDataProviderImpl dataProvider = new JcrDataProviderImpl(JcrDataProvider.TYPE.FILE, imageFile);
		jcrFile.setDataProvider(dataProvider);
		
		return jcrFile;
	}
	
	private Photo createPhoto( String name ) {
		Photo jcrFile = new Photo();
		jcrFile.setName(name);
		jcrFile.setMimeType("image/jpeg");
		jcrFile.setOriginalFilename(name);
		
		File imageFile = new File("src/test/resources/ogg.jpg");
		
		Calendar lastModified = Calendar.getInstance();
		lastModified.setTimeInMillis(imageFile.lastModified());
		jcrFile.setLastModified(lastModified);
		
		jcrFile.setFileSize(imageFile.length());
		jcrFile.setPhotographer("Testino");
		jcrFile.setChild(createParent("Kate"));
		
		JcrDataProviderImpl dataProvider = new JcrDataProviderImpl(JcrDataProvider.TYPE.FILE, imageFile);
		jcrFile.setDataProvider(dataProvider);
		
		return jcrFile;
	}
	
	private void printNode( Node node, String indentation ) throws Exception {
		System.out.println();
		System.out.println(indentation + "------- NODE -------");
		System.out.println(indentation + "Path: " + node.getPath());
		
		System.out.println(indentation + "------- Properties: ");
		PropertyIterator propertyIterator = node.getProperties();
		while ( propertyIterator.hasNext() ) {
			Property p = propertyIterator.nextProperty();
			if ( !p.getName().equals("jcr:data") && !p.getName().equals("jcr:mixinTypes") ) {
				System.out.println(indentation + p.getName() + ": " + p.getString());
			}
		}
	
		NodeIterator nodeIterator = node.getNodes();
		while ( nodeIterator.hasNext() ) {
			printNode(nodeIterator.nextNode(), indentation + "\t");
		}
	}
	
	@Test(expected = JcrMappingException.class)
	public void mapInvalidObject() throws Exception {
		Jcrom jcrom = new Jcrom();
		jcrom.map(InvalidEntity.class);
	}

	@Test
	public void versioningDAO() throws Exception {
		
		Jcrom jcrom = new Jcrom();
		jcrom.map(VersionedEntity.class);
		
		// create the root
		Node rootNode = session.getRootNode().addNode("content").addNode("versionedEntities");
		VersionedDAO versionedDao = new VersionedDAO(session, jcrom);
		
		VersionedEntity entity = new VersionedEntity();
		entity.setTitle("MyEntity");
		entity.setBody("First");
		entity.setPath(rootNode.getPath());
		
		versionedDao.create(entity);
		
		// change it
		entity.setBody("Second");
		versionedDao.update(entity);
			
		assertTrue(versionedDao.getVersionSize(entity.getPath()) == 2);
		
		// load entity
		VersionedEntity loadedEntity = versionedDao.get(entity.getPath());
		
		assertTrue(loadedEntity.getBaseVersionName().equals("1.1"));
		assertTrue(loadedEntity.getVersionName().equals("1.1"));
		assertTrue(versionedDao.getVersionList(entity.getPath()).size() == versionedDao.getVersionSize(entity.getPath()));
				
		// restore
		versionedDao.restoreVersion(entity.getPath(), "1.0");
		
		loadedEntity = versionedDao.get(entity.getPath());
		assertTrue(loadedEntity.getBaseVersionName().equals("1.0"));
		
		loadedEntity.setBody("Third");
		versionedDao.update(loadedEntity);
		
		VersionedEntity oldEntity = versionedDao.getVersion(entity.getPath(), "1.0");
		assertTrue(oldEntity != null);
		assertTrue(oldEntity.getBody().equals("First"));
		
		List<VersionedEntity> versions = versionedDao.getVersionList(entity.getPath());
		for ( VersionedEntity version : versions ) {
			System.out.println("Version [" + version.getVersionName() + "] [" + version.getBody() + "], base [" + version.getBaseVersionName() + "] [" + version.getBaseVersionCreated() + "]");
		}
	}
	
	@Test
	public void testDAOs() throws Exception {
		
		Jcrom jcrom = new Jcrom();
		jcrom.map(Parent.class);
		
		// create the root
		Node rootNode = session.getRootNode().addNode("content").addNode("parents");
		ParentDAO parentDao = new ParentDAO(session, jcrom);

		Parent dad = createParent("John Bobs");
		dad.setDrivingLicense(false);
		dad.setPath(rootNode.getPath());
		Parent mom = createParent("Jane");
		mom.setPath(rootNode.getPath());
		assertFalse(parentDao.exists(dad.getPath() + "/" + dad.getName()));

		parentDao.create(dad);
		parentDao.create(mom);

		session.save();
		
		assertTrue(parentDao.exists(dad.getPath()));

		Parent loadedParent = parentDao.get(dad.getPath());
		assertTrue(loadedParent.getNickName().equals(dad.getNickName()));
		
		Parent uuidParent = parentDao.loadByUUID(loadedParent.getUuid());
		assertTrue(uuidParent.getNickName().equals(dad.getNickName()));

		Parent loadedMom = parentDao.get(mom.getPath());
		assertTrue(loadedMom.getNickName().equals(mom.getNickName()));
		
		List<Parent> parents = parentDao.findAll(rootNode.getPath());
		assertTrue(parents.size() == 2);

		List<Parent> parentsWithLicense = parentDao.findByLicense();
		assertTrue(parentsWithLicense.size() == 1);

		parentDao.delete(dad.getPath());
		assertFalse(parentDao.exists(dad.getPath()));
		
		parentDao.delete(loadedMom.getPath());
		assertFalse(parentDao.exists(mom.getPath()));
	}
	
	/*
	@Test
	public void versioning() throws Exception {
		
		Node node = session.getRootNode().addNode("versionTest");
		node.addMixin("mix:versionable");
		node.setProperty("title", "version 1");
		session.save();
		node.checkin();
		
		node.checkout();
		node.setProperty("title", "version 2");
		session.save();
		node.checkin();
		
		node.checkout();
		node.setProperty("title", "version 3");
		session.save();
		node.checkin();
		
		// print version history
		VersionHistory history = session.getRootNode().getNode("versionTest").getVersionHistory();
		VersionIterator iterator = history.getAllVersions();
		iterator.skip(1);
		while ( iterator.hasNext() ) {
			Version version = iterator.nextVersion();
			System.out.println("Version [" + version.getName() + "], [" + version.getPrimaryNodeType().getName() + "], created " + version.getCreated().getTime());
			
			NodeIterator nodeIterator = version.getNodes();
			while ( nodeIterator.hasNext() ) {
				Node versionNode = nodeIterator.nextNode();
				System.out.println("\tTitle: " + versionNode.getProperty("title").getString());
				
				if ( versionNode.getParent().isNodeType("nt:version") ) {
					Version parentVersion = (Version) versionNode.getParent();
					System.out.println("\tVersion name: " + parentVersion.getName() + " " + parentVersion.getCreated().getTime());
				}
			}
		}
		
		// print base version
		System.out.println("Base version name: " + session.getRootNode().getNode("versionTest").getBaseVersion().getName());
		
		// restore
		node.checkout();
		node.restore("1.0", true);
		
		System.out.println("Base version name (after restore): " + session.getRootNode().getNode("versionTest").getBaseVersion().getName());
	}
	*/

	
	@Test
	public void mapObjectsToNodesAndBack() throws Exception {
		
		Parent parent = createParent("John Mugabe");
		
		// adopt a child
		Child adoptedChild = createChild("Mubi");
		parent.setAdoptedChild(adoptedChild);
				
		// add children
		parent.addChild(createChild("Jane"));
		parent.addChild(createChild("Julie"));
		
		Child child = createChild("Robert");
		parent.addChild(child);
		
		// add grand children
		child.setAdoptedGrandChild(createGrandChild("Jim"));
		child.addGrandChild(createGrandChild("Adam"));
		
		// add a passport photo
		Photo photo = createPhoto("jcr_passport.jpg");
		parent.setPassportPhoto(photo);
		
		// multiple files
		for ( int i = 0; i < 3; i++ ) {
			parent.addFile(createFile("jcr_image" + i + ".jpg"));
		}
		
		// map to node
		Jcrom jcrom = new Jcrom();
		jcrom.map(Parent.class); // this should automatically map the other classes, since they are referenced

		Node rootNode = session.getRootNode().addNode("root");

		// map the object to node
		String[] mixinTypes = {"mix:referenceable"};
		Node newNode = jcrom.addNode(rootNode, parent, mixinTypes);

		String uuid = newNode.getUUID();
		assertTrue( newNode.getUUID() != null && newNode.getUUID().length() > 0 );
		assertTrue( newNode.getProperty("birthDay").getDate().getTime().equals(parent.getBirthDay()) );
		assertTrue( newNode.getProperty("weight").getDouble() == parent.getWeight() );
		assertTrue( (int)newNode.getProperty("fingers").getDouble() == parent.getFingers() );
		assertTrue( new Boolean(newNode.getProperty("drivingLicense").getBoolean()).equals(new Boolean(parent.isDrivingLicense())) );
		assertTrue( newNode.getProperty("hairs").getLong() == parent.getHairs() );
		assertTrue( newNode.getNode("children").getNodes().nextNode().getName().equals("Jane") );
		assertTrue( newNode.getProperty("tags").getValues().length == 3 );
		assertTrue( newNode.getProperty("tags").getValues()[0].getString().equals("father") );

		// map back to object
		Parent parentFromNode = jcrom.fromNode(Parent.class, newNode);

		// check the file
		File imageFileFromNode = new File("target/ogg_copy.jpg");
		write(parentFromNode.getPassportPhoto().getDataProvider().getInputStream(), imageFileFromNode);

		// check the list of files
		assertTrue( parentFromNode.getFiles().size() == 3 );

		// validate properties
		assertTrue( parentFromNode.getUuid().equals(uuid) );
		assertTrue( parentFromNode.getNickName().equals(parent.getNickName()) );
		assertTrue( parentFromNode.getBirthDay().equals(parent.getBirthDay()) );
		assertTrue( parentFromNode.getWeight() == parent.getWeight() );
		assertTrue( parentFromNode.getFingers() == parent.getFingers() );
		assertTrue( new Boolean(parentFromNode.isDrivingLicense()).equals(new Boolean(parent.isDrivingLicense())) );
		assertTrue( parentFromNode.getHairs() == parent.getHairs() );
		assertTrue( parentFromNode.getTags().size() == 3 );

		// validate children
		assertTrue( parentFromNode.getAdoptedChild() != null && parentFromNode.getAdoptedChild().getName().equals(adoptedChild.getName()) );
		assertTrue( parentFromNode.getChildren().size() == 3 );
		assertTrue( parentFromNode.getChildren().get(2).getTitle().equals(child.getTitle()) );
		assertTrue( parentFromNode.getChildren().get(2).getAdoptedGrandChild().getTitle().equals("Jim") );
		assertTrue( parentFromNode.getChildren().get(2).getGrandChildren().size() == 1 );
		assertTrue( parentFromNode.getChildren().get(2).getGrandChildren().get(0).getTitle().equals("Adam") );

		// make sure that the JcrParentNode has been properly mapped
		assertTrue( ((Parent)parentFromNode.getAdoptedChild().getParent()).getTitle().equals(parent.getTitle()) );
		assertTrue( ((Parent)parentFromNode.getChildren().get(0).getParent()).getTitle().equals(parent.getTitle()) );
		assertTrue( parentFromNode.getChildren().get(2).getAdoptedGrandChild().getParent().getTitle().equals(child.getTitle()) );
		assertTrue( parentFromNode.getChildren().get(2).getGrandChildren().get(0).getParent().getTitle().equals(child.getTitle()) );

		// update the object
		parent.setNickName("Father");
		parent.setBirthDay(new Date());
		parent.setWeight(87.5);
		parent.setFingers(9);
		parent.setDrivingLicense(false);
		parent.setHairs(2);
		
		parent.getTags().remove(0);
		parent.addTag("test1");
		parent.addTag("test2");

		parent.getAdoptedChild().setFingers(10);
		
		// update photo metadata
		parent.getPassportPhoto().setPhotographer("Johnny Bob");
		parent.getPassportPhoto().setDataProvider(null);

		// update the node
		jcrom.updateNode(newNode, parent);
		
		//printNode(newNode, "");
		parentFromNode = jcrom.fromNode(Parent.class, newNode);
		System.out.println("Updated photographer: " + parentFromNode.getPassportPhoto().getPhotographer());
		System.out.println("InputStream is null: " + (parentFromNode.getPassportPhoto().getDataProvider().getInputStream()==null));
		

		// validate that the node is updated
		assertTrue( newNode.getProperty("birthDay").getDate().getTime().equals(parent.getBirthDay()) );
		assertTrue( newNode.getProperty("weight").getDouble() == parent.getWeight() );
		assertTrue( (int)newNode.getProperty("fingers").getDouble() == parent.getFingers() );
		assertTrue( new Boolean(newNode.getProperty("drivingLicense").getBoolean()).equals(new Boolean(parent.isDrivingLicense())) );
		assertTrue( newNode.getProperty("hairs").getLong() == parent.getHairs() );
		assertTrue( (int)newNode.getNode("adoptedChild").getNodes().nextNode().getProperty("fingers").getDouble() == parent.getAdoptedChild().getFingers() );
		assertTrue( parentFromNode.getTags().equals(parent.getTags()) );
		
		// move the node
		parent.setTitle("Mohammed");
		parent.setNickName("Momo");
		jcrom.updateNode(newNode, parent);

		assertFalse( rootNode.hasNode("John_Mugabe") );
		assertTrue( rootNode.hasNode("Mohammed") );
		assertTrue( rootNode.getNode("Mohammed").getProperty("nickName").getString().equals("Momo") );

		// update a child node directly
		Node adoptedChildNode = rootNode.getNode(parent.getName()).getNode("adoptedChild").getNodes().nextNode();
		adoptedChild.setNickName("Mubal");
		jcrom.updateNode(adoptedChildNode, adoptedChild);

		assertTrue( rootNode.getNode(parent.getName()).getNode("adoptedChild").getNodes().nextNode().getProperty("nickName").getString().equals(adoptedChild.getNickName()) );
	}
	
    void write(InputStream in, File dst) throws IOException {
        OutputStream out = new FileOutputStream(dst);
		try {
			// Transfer bytes from in to out
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
		} finally {
			in.close();
	        out.close();
		}
    }

}
