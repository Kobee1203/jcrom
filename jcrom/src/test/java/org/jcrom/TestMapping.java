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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.LogManager;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.Repository;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.Value;
import org.apache.jackrabbit.core.TransientRepository;
import org.jcrom.JcrDataProvider.TYPE;
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
		repo = (Repository) new TransientRepository();
		session = repo.login(new SimpleCredentials("a", "b".toCharArray()));
        
		ClassLoader loader = TestMapping.class.getClassLoader();
		URL url = loader.getResource("logger.properties");
		if ( url == null ) {
			url = loader.getResource("/logger.properties");
		}
        LogManager.getLogManager().readConfiguration(url.openStream());
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
	
	static JcrFile createFile( String name ) {
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
	
	private Photo createPhoto( String name ) throws Exception {
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
		
		// also read the file to byte array and store it that way
		jcrFile.setFileBytes(readBytes(new FileInputStream(imageFile)));
		
		return jcrFile;
	}
	
	private byte[] readBytes( InputStream in ) throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
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
		return out.toByteArray();
	}
	
	static void printNode( Node node, String indentation ) throws Exception {
		System.out.println();
		System.out.println(indentation + "------- NODE -------");
		System.out.println(indentation + "Path: " + node.getPath());
		
		System.out.println(indentation + "------- Properties: ");
		PropertyIterator propertyIterator = node.getProperties();
		while ( propertyIterator.hasNext() ) {
			Property p = propertyIterator.nextProperty();
			if ( !p.getName().equals("jcr:data") && !p.getName().equals("jcr:mixinTypes") && !p.getName().equals("fileBytes") ) {
				System.out.print(indentation + p.getName() + ": ");
                if ( p.getDefinition().getRequiredType() == PropertyType.BINARY ) {
                    System.out.print( "binary, (length:" + p.getLength() + ") ");
                } else if ( !p.getDefinition().isMultiple() ) {
                    System.out.print(p.getString());
                } else {
                    for ( Value v : p.getValues() ) {
                        System.out.print(v.getString() + ", ");
                    }
                }
                System.out.println();
			}
		}
	
		NodeIterator nodeIterator = node.getNodes();
		while ( nodeIterator.hasNext() ) {
			printNode(nodeIterator.nextNode(), indentation + "\t");
		}
	}
    
    @Test
    public void testEnums() throws Exception {
        Jcrom jcrom = new Jcrom();
        jcrom.map(EnumEntity.class);
        
        EnumEntity.Suit[] suitArray = new EnumEntity.Suit[2];
        suitArray[0] = EnumEntity.Suit.HEARTS;
        suitArray[1] = EnumEntity.Suit.CLUBS;
        
        EnumEntity enumEntity = new EnumEntity();
        enumEntity.setName("MySuit");
        enumEntity.setSuit(EnumEntity.Suit.DIAMONDS);
        enumEntity.setSuitAsArray(suitArray);
        enumEntity.addSuitToList(EnumEntity.Suit.SPADES);
        
        Node rootNode = session.getRootNode().addNode("enumTest");
        Node newNode = jcrom.addNode(rootNode, enumEntity);
        session.save();
        
        EnumEntity fromNode = jcrom.fromNode(EnumEntity.class, newNode);
        
        assertEquals(fromNode.getSuit(), enumEntity.getSuit());
        assertTrue(fromNode.getSuitAsArray().length == enumEntity.getSuitAsArray().length);
        assertTrue(fromNode.getSuitAsArray()[0].equals(enumEntity.getSuitAsArray()[0]));
        assertTrue(fromNode.getSuitAsList().size() == enumEntity.getSuitAsList().size());
        assertTrue(fromNode.getSuitAsList().get(0).equals(enumEntity.getSuitAsList().get(0)));
    }
	
	@Test(expected = JcrMappingException.class)
	public void mapInvalidObject() throws Exception {
		Jcrom jcrom = new Jcrom();
		jcrom.map(InvalidEntity.class);
	}
	
	@Test
	public void serializedProperties() throws Exception {
		
		Jcrom jcrom = new Jcrom();
		jcrom.map(EntityWithSerializedProperties.class);
		
		EntityWithSerializedProperties entity = new EntityWithSerializedProperties();
		entity.setName("withSerializedProperties");
		
		Parent parent = createParent("John");
		entity.setParent(parent);
		
		Node rootNode = session.getRootNode().addNode("mapChildTest");
		Node newNode = jcrom.addNode(rootNode, entity);
		session.save();
		
		EntityWithSerializedProperties entityFromJcr = jcrom.fromNode(EntityWithSerializedProperties.class, newNode);
		
		assertTrue( entityFromJcr.getParent().getName().equals(entity.getParent().getName()) );
		assertTrue( entityFromJcr.getParent().getBirthDay().equals(entity.getParent().getBirthDay()) );
		assertTrue( entityFromJcr.getParent().getHeight() == entity.getParent().getHeight() );
	}
	
	@Test
	public void mapsAsChildren() throws Exception {
		
		Jcrom jcrom = new Jcrom();
		jcrom.map(EntityWithMapChildren.class);
		
		Integer[] myIntArr1 = {1,2,3};
		Integer[] myIntArr2 = {4,5,6};
		int[] myIntArr3 = {7,8,9};
		
		int myInt1 = 1;
		int myInt2 = 2;
		
		String[] myStringArr1 = {"a","b","c"};
		String[] myStringArr2 = {"d","e","f"};
		String[] myStringArr3 = {"h","i","j"};
		
		String myString1 = "string1";
		String myString2 = "string2";
		
		Locale locale = Locale.ITALIAN;
		Locale[] locales = {Locale.FRENCH, Locale.GERMAN};
		
		EntityWithMapChildren entity = new EntityWithMapChildren();
		entity.setName("mapEntity");
		entity.addIntegerArray("myIntArr1", myIntArr1);
		entity.addIntegerArray("myIntArr2", myIntArr2);
		entity.setMultiInt(myIntArr3);
		entity.addStringArray("myStringArr1", myStringArr1);
		entity.addStringArray("myStringArr2", myStringArr2);
		entity.setMultiString(myStringArr3);
		entity.addString("myString1", myString1);
		entity.addString("myString2", myString2);
		entity.addInteger("myInt1", myInt1);
		entity.addInteger("myInt2", myInt2);
		entity.setLocale(locale);
		entity.setMultiLocale(locales);
		
		Node rootNode = session.getRootNode().addNode("mapChildTest");
		Node newNode = jcrom.addNode(rootNode, entity);
		session.save();
		
		EntityWithMapChildren entityFromJcr = jcrom.fromNode(EntityWithMapChildren.class, newNode);
		
		assertTrue( entityFromJcr.getIntegers().equals(entity.getIntegers()) );
		assertTrue( entityFromJcr.getStrings().equals(entity.getStrings()) );
		assertTrue( entityFromJcr.getMultiInt().length == entity.getMultiInt().length );
		assertTrue( entityFromJcr.getMultiInt()[1] == myIntArr3[1] );
		assertTrue( entityFromJcr.getMultiString().length == entity.getMultiString().length );
		assertTrue( entityFromJcr.getIntegerArrays().size() == entity.getIntegerArrays().size() );
		assertTrue( entityFromJcr.getIntegerArrays().get("myIntArr1").length == myIntArr1.length );
		assertTrue( entityFromJcr.getIntegerArrays().get("myIntArr2").length == myIntArr2.length );
		assertTrue( entityFromJcr.getIntegerArrays().get("myIntArr1")[1] == myIntArr1[1] );
		assertTrue( entityFromJcr.getStringArrays().size() == entity.getStringArrays().size() );
		assertTrue( entityFromJcr.getStringArrays().get("myStringArr1").length == myStringArr1.length );
		assertTrue( entityFromJcr.getStringArrays().get("myStringArr2").length == myStringArr2.length );
		assertTrue( entityFromJcr.getStringArrays().get("myStringArr1")[1].equals(myStringArr1[1]) );
		
		assertTrue( entityFromJcr.getLocale().equals(locale) );
		assertTrue( entityFromJcr.getMultiLocale().length == entity.getMultiLocale().length );
		assertTrue( entityFromJcr.getMultiLocale()[1].equals(locales[1]) );
	}
	
	@Test
	public void dynamicInstantiation() throws Exception {
		
		Jcrom jcrom = new Jcrom(true, true);
		jcrom.map(Circle.class)
				.map(Rectangle.class)
				.map(ShapeParent.class);
		
		Shape circle = new Circle(5);
		circle.setName("circle");
		
		Shape rectangle = new Rectangle(5,5);
		rectangle.setName("rectangle");
		
		Node rootNode = session.getRootNode().addNode("dynamicInstTest");
		Node circleNode = jcrom.addNode(rootNode, circle);
		Node rectangleNode = jcrom.addNode(rootNode, rectangle);
		session.save();
		
		Shape circleFromNode = jcrom.fromNode(Shape.class, circleNode);
		Shape rectangleFromNode = jcrom.fromNode(Shape.class, rectangleNode);
		
		assertTrue( circleFromNode.getArea() == circle.getArea() );
		assertTrue( rectangleFromNode.getArea() == rectangle.getArea() );
		
		// now try it with a parent with interface based child nodes
		Shape circle1 = new Circle(1);
		circle1.setName("circle1");
		Shape circle2 = new Circle(2);
		circle2.setName("circle2");
		Shape rectangle1 = new Rectangle(2,2);
		rectangle1.setName("rectangle");
		
		ShapeParent shapeParent = new ShapeParent();
		shapeParent.setName("ShapeParent");
		shapeParent.addShape(circle1);
		shapeParent.addShape(rectangle1);
		shapeParent.setMainShape(circle2);
		
		Node shapeParentNode = jcrom.addNode(rootNode, shapeParent);
		session.save();
		
		ShapeParent fromNode = jcrom.fromNode(ShapeParent.class, shapeParentNode);
		
		assertTrue( fromNode.getMainShape().getArea() == shapeParent.getMainShape().getArea() );
		assertTrue( fromNode.getShapes().size() == shapeParent.getShapes().size() );
		assertTrue( fromNode.getShapes().get(0).getArea() == shapeParent.getShapes().get(0).getArea() );
		assertTrue( fromNode.getShapes().get(1).getArea() == shapeParent.getShapes().get(1).getArea() );
	}
	
	@Test
	public void references() throws Exception {
		
		Jcrom jcrom = new Jcrom(true,true);
		jcrom.map(ReferenceContainer.class);
        jcrom.map(Rectangle.class);
		
		// create the entity we will reference
		ReferencedEntity reference = new ReferencedEntity();
		reference.setName("myReference");
		reference.setBody("myBody");
		
		ReferencedEntity reference2 = new ReferencedEntity();
		reference2.setName("Reference2");
		reference2.setBody("Body of ref 2.");
        
        Rectangle rectangle = new Rectangle(2, 3);
        rectangle.setName("rectangle");
		
		Node rootNode = session.getRootNode().addNode("referenceTest");
		jcrom.addNode(rootNode, reference);
		jcrom.addNode(rootNode, reference2);
        jcrom.addNode(rootNode, rectangle);
		session.save();
		
		// note that the ReferenceContainer and ReferencedEntity classes both have
		// mixin types defined in their @JcrNode annotation
		
		ReferenceContainer refContainer = new ReferenceContainer();
		refContainer.setName("refContainer");
		refContainer.setReference(reference);
		refContainer.addReference(reference);
		refContainer.addReference(reference2);
        
        refContainer.setReferenceByPath(reference);
        refContainer.addReferenceByPath(reference);
        refContainer.addReferenceByPath(reference2);
        
        refContainer.setShape(rectangle);
		
		Node refNode = jcrom.addNode(rootNode, refContainer);
		
		ReferenceContainer fromNode = jcrom.fromNode(ReferenceContainer.class, refNode);
		
		assertTrue(fromNode.getReference() != null);
		assertTrue(fromNode.getReference().getName().equals(reference.getName()));
		assertTrue(fromNode.getReference().getBody().equals(reference.getBody()));
		
		assertTrue(fromNode.getReferences().size() == 2);
		assertTrue(fromNode.getReferences().get(1).getName().equals(reference2.getName()));
		assertTrue(fromNode.getReferences().get(1).getBody().equals(reference2.getBody()));
        
		assertTrue(fromNode.getReferenceByPath() != null);
		assertTrue(fromNode.getReferenceByPath().getName().equals(reference.getName()));
		assertTrue(fromNode.getReferenceByPath().getBody().equals(reference.getBody()));
        
		assertTrue(fromNode.getReferencesByPath().size() == 2);
		assertTrue(fromNode.getReferencesByPath().get(1).getName().equals(reference2.getName()));
		assertTrue(fromNode.getReferencesByPath().get(1).getBody().equals(reference2.getBody()));
        
        assertTrue(fromNode.getShape().getArea() == rectangle.getArea());
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
        
        VersionedEntity child1 = new VersionedEntity();
        child1.setName("child1");
        child1.setBody("child1Body");
        
        VersionedEntity child2 = new VersionedEntity();
        child2.setName("child2");
        child2.setBody("child2Body");
        
        entity.addVersionedChild(child1);
        entity.addVersionedChild(child2);
        
        Child child3 = createChild("John");
        entity.addUnversionedChild(child3);
		
		versionedDao.create(entity);
		
		// change it
		entity.setBody("Second");
        entity.getUnversionedChildren().get(0).setNickName("Kalli");
		versionedDao.update(entity);
        
		// change it again
		entity.setBody("SecondSecond");
		versionedDao.update(entity);
			
		assertEquals(3, versionedDao.getVersionSize(entity.getPath()));
		
		// load entity
		VersionedEntity loadedEntity = versionedDao.get(entity.getPath());
		
		assertEquals("1.2", loadedEntity.getBaseVersionName());
		assertEquals("1.2", loadedEntity.getVersionName());
        assertTrue(loadedEntity.getUnversionedChildren().size() == entity.getUnversionedChildren().size());
		assertTrue(versionedDao.getVersionList(entity.getPath()).size() == versionedDao.getVersionSize(entity.getPath()));

        VersionedEntity middleVersion = versionedDao.getVersion(entity.getPath(), "1.2");
        assertNotNull(middleVersion);
        
		// restore
		versionedDao.restoreVersion(entity.getPath(), "1.0");
		
		loadedEntity = versionedDao.get(entity.getPath());
		assertTrue(loadedEntity.getBaseVersionName().equals("1.0"));
        Child loadedChild3 = loadedEntity.getUnversionedChildren().get(0);
        assertEquals("Baby",loadedChild3.getNickName()); 
		
		loadedEntity.setBody("Third");
		versionedDao.update(loadedEntity);
		
		VersionedEntity oldEntity = versionedDao.getVersion(entity.getPath(), "1.0");
		assertTrue(oldEntity != null);
		assertTrue(oldEntity.getBody().equals("First"));
		
		List<VersionedEntity> versions = versionedDao.getVersionList(entity.getPath());
		for ( VersionedEntity version : versions ) {
			System.out.println("Version [" + version.getVersionName() + "] [" + version.getBody() + "], base [" + version.getBaseVersionName() + "] [" + version.getBaseVersionCreated() + "]");
		}
        
        // move
        VersionedEntity anotherEntity = new VersionedEntity();
        anotherEntity.setName("anotherEntity");
        anotherEntity.setBody("anotherBody");
        
        versionedDao.create(rootNode.getPath(), anotherEntity);
        
        VersionedEntity childEntity = loadedEntity.getVersionedChildren().get(0);
        
        versionedDao.move(childEntity, anotherEntity.getPath() + "/versionedChildren");
        
        assertTrue( versionedDao.exists(anotherEntity.getPath() + "/versionedChildren/" + childEntity.getName()));
        
        // remove child
        versionedDao.remove(loadedEntity.getVersionedChildren().get(1).getPath());
        
        assertFalse( versionedDao.exists(loadedEntity.getVersionedChildren().get(1).getPath()) );
        
        versionedDao.remove(loadedEntity.getPath());
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
        
        dad.setAdoptedChild(createChild("AdoptedChild1"));
        dad.addChild(createChild("Child1"));
        dad.addChild(createChild("Child2"));
        
		Parent mom = createParent("Jane");
		mom.setPath(rootNode.getPath()); // mom is created directly under root
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
        
        // test second level update
        loadedParent.getAdoptedChild().setNickName("testing");
        loadedParent.getChildren().get(0).setNickName("hello");
        parentDao.update(loadedParent, "*", 2);
        
        Parent updatedParent = parentDao.get(dad.getPath());
        assertEquals(loadedParent.getAdoptedChild().getNickName(), updatedParent.getAdoptedChild().getNickName());
        assertEquals(loadedParent.getChildren().get(0).getNickName(), updatedParent.getChildren().get(0).getNickName());
		
		List<Parent> parents = parentDao.findAll(rootNode.getPath());
		assertTrue(parents.size() == 2);

		List<Parent> parentsWithLicense = parentDao.findByLicense();
		assertTrue(parentsWithLicense.size() == 1);

		parentDao.remove(dad.getPath());
		assertFalse(parentDao.exists(dad.getPath()));
		
		parentDao.remove(loadedMom.getPath());
		assertFalse(parentDao.exists(mom.getPath()));
		
		// test a root level node
		Parent rootDad = createParent("John Smith");
		parentDao.create("/", rootDad);
		
		assertTrue( rootDad.getPath().equals("/"+rootDad.getName()) );
		
		// rename a root level node
		rootDad.setName("John Smythe");
		parentDao.update(rootDad);
		
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
		parentFromNode.getPassportPhoto().getDataProvider().writeToFile(imageFileFromNode);
		
		assertTrue(parentFromNode.getPassportPhoto().getFileBytes().length == photo.getFileBytes().length);

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
        
        parentFromNode.getFiles().remove(1);
        parentFromNode.getFiles().add(1, createFile("jcr_image5.jpg"));
        parentFromNode.addFile(createFile("jcr_image6.jpg"));
        jcrom.updateNode(newNode, parentFromNode);
        
        Parent updatedParent = jcrom.fromNode(Parent.class, newNode);
        assertTrue(updatedParent.getFiles().size() == parentFromNode.getFiles().size());
		
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
    
    /**
     * Thanks to Decebal Suiu for contributing this test case.
     * @throws Exception 
     */
	@Test
	public void testEmptyList() throws Exception {
        Jcrom jcrom = new Jcrom();
        jcrom.map(Person.class);

        // create person
		Person person = new Person();
		person.setName("peter");
		person.setAge(20);
		person.setPhones(Arrays.asList(new String[] {"053453553"}));

		// add person in jcr
		Node node = jcrom.addNode(session.getRootNode(), person);
		Person personFromJcr = jcrom.fromNode(Person.class, node);
		assertEquals(1, personFromJcr.getPhones().size());

		// update person in jcr
		person.setPhones(new ArrayList<String>()); // reset the phones
		jcrom.updateNode(node, person);

		// retrieve updated person from jcr
		personFromJcr = jcrom.fromNode(Person.class, node);
		assertFalse(personFromJcr.getPhones().size() == 1); // <<< FAILED
	}
    
    /**
     * Thanks to Danilo Barboza for contributing this test case.
     * @throws Exception 
     */
    @Test
    public void testCustomJcrFile() throws Exception {
		Jcrom jcrom = new Jcrom();
		jcrom.map(CustomJCRFile.class);
        
        session.getRootNode().addNode("customs");
        CustomJCRFileDAO dao = new CustomJCRFileDAO(session, jcrom);

		File imageFile = new File("src/test/resources/ogg.jpg");

        CustomJCRFile custom = new CustomJCRFile();
		custom.setPath("customs");
		custom.setMetadata("My Metadata!");
		custom.setEncoding("UTF-8");
		custom.setMimeType("image/jpg");
		custom.setLastModified(Calendar.getInstance());
		JcrDataProvider dataProvider = new JcrDataProviderImpl(TYPE.FILE, imageFile);
		custom.setDataProvider(dataProvider);
		custom.setName(imageFile.getName());

		dao.create(custom);
        
        CustomJCRFile customFromJcr = dao.get(custom.getPath());
        
        assertEquals(custom.getName(), customFromJcr.getName());
        assertEquals(custom.getEncoding(), customFromJcr.getEncoding());
        assertEquals(custom.getMimeType(), customFromJcr.getMimeType());
        assertEquals(custom.getMetadata(), customFromJcr.getMetadata());
        
        customFromJcr.setEncoding("ISO-8859-1");
        customFromJcr.setMimeType("image/gif");
        customFromJcr.setMetadata("Updated metadata");
        customFromJcr.setDataProvider(null); // not going to update the file
        
        dao.update(customFromJcr);
        
        CustomJCRFile updatedFromJcr = dao.get(customFromJcr.getPath());
        
        assertEquals(customFromJcr.getEncoding(), updatedFromJcr.getEncoding());
        assertEquals(customFromJcr.getMimeType(), updatedFromJcr.getMimeType());
        assertEquals(customFromJcr.getMetadata(), updatedFromJcr.getMetadata());
    }
    
    @Test
    public void testNoChildContainerNode() throws Exception {
        Jcrom jcrom = new Jcrom();
        jcrom.map(UserProfile.class);
        
        Node rootNode = session.getRootNode().addNode("noChildTest");
        
        UserProfile userProfile = new UserProfile();
        userProfile.setName("john");
        
        Address address = new Address();
        address.setStreet("Some street");
        address.setPostCode("101");
        userProfile.setAddress(address);
        
        Node userProfileNode = jcrom.addNode(rootNode, userProfile);
        
        printNode(userProfileNode, "");
        
        UserProfile fromJcr = jcrom.fromNode(UserProfile.class, userProfileNode);
        
        assertEquals(address.getName(), "address");
        assertEquals(address.getStreet(), fromJcr.getAddress().getStreet());
        assertEquals(address.getPostCode(), fromJcr.getAddress().getPostCode());
        
        fromJcr.getAddress().setStreet("Another street");
        
        jcrom.updateNode(userProfileNode, fromJcr);
        
        UserProfile updatedFromJcr = jcrom.fromNode(UserProfile.class, userProfileNode);
        
        assertEquals(fromJcr.getAddress().getStreet(), updatedFromJcr.getAddress().getStreet());
        assertEquals(fromJcr.getAddress().getPostCode(), updatedFromJcr.getAddress().getPostCode());
    }
    
    @Test
    public void testSecondLevelFileUpdate() throws Exception {
        
        Jcrom jcrom = new Jcrom();
        jcrom.map(GrandParent.class)
                .map(Photo.class);
        
        GrandParent grandParent = new GrandParent();
        grandParent.setName("Charles");
        
        Parent parent = createParent("William");

		Photo photo = createPhoto("jcr_passport.jpg");
		parent.setPassportPhoto(photo);
        
        parent.setJcrFile(createFile("jcr_image.jpg"));
        
        grandParent.setChild(parent);
        
        Node rootNode = session.getRootNode().addNode("root");
        
        Node newNode = jcrom.addNode(rootNode, grandParent);
        
        GrandParent fromNode = jcrom.fromNode(GrandParent.class, newNode);
        fromNode.getChild().getPassportPhoto().setName("bobby.xml");
        fromNode.getChild().getPassportPhoto().setPhotographer("Bobbs");
        
        fromNode.getChild().setName("test");
        fromNode.getChild().setTitle("Something");
        fromNode.getChild().getJcrFile().setName("bob.xml");
        
        jcrom.updateNode(newNode, photo);
    }

}
