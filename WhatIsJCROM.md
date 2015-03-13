# What is JCROM? #

JCROM is an acronym that stands for Java Content Repository Object Mapper. It is a simple and lightweight annotation-based framework for mapping plain old java objects (POJOs) to/from nodes in a Java Content Repository. Before you can appreciate what it does for you, it is important to understand what a Java Content Repository (JCR) is.

## Java Content Repository (the JCR part of JCROM) ##

The [JCR specification (JSR-170)](http://jcp.org/en/jsr/detail?id=170) is one of the most exciting things to come out in the Java world lately. The JCR API is a standard, implementation independent, way to access content bi-directionally on a granular level to a content repository. It take the best of both the database and file system worlds, and adds a layer of extra services on top of that (versioning, full-text search, multi-valued properties, observations, etc.).

Although the spec has been out for a while (since 2005), JCR is now really starting to gain momentum with good open source implementations like [Jackrabbit](http://jackrabbit.apache.org).

## Object Mapping (the OM part of JCROM) ##

We generally model our domain objects as Java classes. If we're storing information about hotels, we're likely to create a Hotel class, and add fields like title, description, address, rating, pictures, etc. It is easy to instantiate Java classes and add data to the fields, but when it comes to actually persisting the data we usually map the object to an external data source (such as database, xml files, or JCR). This often involves a lot of error prone boiler plate code (e.g. mapping from JDBC result set, or XML elements). The same applies, of course, when we need to load the data from the external data source into the Java object: more boiler plate code.

We already have good object mapping frameworks for JDBC, like Hibernate and the new Enterprise JavaBeans spec. There are also lots of solutions for mapping to/from XML. The vision of JCROM is to provide the same for JCR.

To see how JCROM can make things easier, consider the following example. Let's say we have a very simple Hotel class:

```
public class Hotel {

	private String name;
	private String description;
	private String phoneNumber;
	private int rating;
	
	private List<Restaurant> restaurants;
	
	// ... getters and setters
}
```

As you can see, each hotel has a list of restaurants. The Restaurant object is very simple as well:

```
public class Restaurant {
	
	private String name;
	private String description;
	private String phoneNumber;
	
	// ... getters and setters
}
```

To map a Hotel instance with restaurants to a JCR repository, we would need to do something like this (let's assume the Hotel object has been initialised with all relevant data):

```
Hotel hotel = ...;
Session session = ...;
try {
	Node hotelNode = session.getRootNode().getNode("Hotels").addNode(hotel.getName());
	hotelNode.setProperty("description", hotel.getDescription());
	hotelNode.setProperty("phoneNumber", hotel.getPhoneNumber());
	hotelNode.setProperty("rating", hotel.getRating());
	
	Node restaurantContainer = hotelNode.addNode("restaurants");
	for ( Restaurant r : hotel.getRestaurants() ) {
		Node rNode = restaurantContainer.addNode(r.getName());
		rNode.setProperty("description", r.getDescription());
		rNode.setProperty("phoneNumber", r.getPhoneNumber());
	}
	session.save();
} finally {
	session.logout();
}
```

And if we wanted to load the Hotel back from the JCR, we'd have to do something like this:

```
String hotelName = ...;
Session session = ...;
try {
	Node hotelNode = session.getRootNode().getNode("Hotels").getNode(hotelName);

	Hotel hotel = new Hotel();
	hotel.setName( node.getName() );
	hotel.setDescription( node.getProperty("description").getString() );
	hotel.setRating( node.getProperty("rating").getInt() );
	
	NodeIterator restaurantIterator = hotelNode.getNodes("restaurants");
	while ( restaurantIterator.hasNext() ) {
		Node rNode = restaurantIterator.nextNode();
		Restaurant restaurant = new Restaurant();
		restaurant.setName(rNode.getName());
		restaurant.setDescription(rNode.getProperty("description").getString());
		restaurant.setPhoneNumber(rNode.getProperty("phoneNumber").getString());
		hotel.addRestaurant(restaurant);
	}
	
} finally {
	session.logout();
}
```

This code is far from beautiful, and it gets messier with every field or child you add. Now, let's assume that we have annotated the Hotel and Restaurant classes with JCROM. We then create a new JCROM instance and map the objects we need:

```
Jcrom jcrom = new Jcrom();
jcrom.map(Hotel.class); // this will map Restaurant as well
```

Now that we have initialised Jcrom, we can map a Hotel instance to JCR:

```
Hotel hotel = ...;
Session session = ...;
try {
	Node parentNode = session.getRootNode().getNode("Hotels");
	jcrom.addNode(parentNode, hotel);
	
	session.save();
} finally {
	session.logout();
}
```

And in the same way we can load it from JCR:

```
String hotelName = ...;
Session session = ...;
try {
	Node hotelNode = session.getRootNode().getNode("Hotels").getNode(hotelName);
	Hotel hotel = jcrom.fromNode(Hotel.class, hotelNode);
} finally {
	session.logout();
}
```

Pretty neat, huh? JCROM handles the mapping of nodes and child nodes, casting and assignment. And that's not all. The DAO support can abstract the JCR concepts even further, but more about that later in this guide.

## Summary ##

To summarise:
  * annotation based (needs Java 1.5)
  * lightweight, minimal external [dependencies](Dependencies.md)
  * simple to learn, easy to use (and test)
  * removes error prone boiler plate code
  * works with any JCR implementation
  * works well with Guice and Spring
  * vision: do for JCR what Hibernate did for JDBC

What JCROM does not do at the moment:
  * support mapping of legacy objects via XML

I find it easiest to explain by example. Therefore, this user guide is written more in a tutorial style than as a reference.

# Before we begin #

To use JCROM, you'll need the JCROM jar in your classpath. There are a few ways you can achieve this:

  * Download it directly from [the download page](https://code.google.com/p/jcrom/downloads/list), or [here for JCROM 2.2.0](https://jcrom.googlecode.com/svn/repo/releases/org/jcrom/jcrom/2.2.0/jcrom-2.2.0.jar) (note that you will need to download the external [dependencies](Dependencies.md) as well)
  * If you use Maven2 to manage your project, you can reference JCROM as a dependency:
```
  <dependency>
	<groupId>org.jcrom</groupId>
	<artifactId>jcrom</artifactId>
	<version>2.2.0</version>
  </dependency>
```
  * Finally, you can check out from subversion and build it yourself. You will need [Maven2](http://maven.apache.org) for this. After you've checked out the project, cd into the jcrom folder, and run "mvn install".

Go [back](UserGuide.md) to the overview.