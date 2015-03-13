# Mapping objects to/from JCR #

Once you've annotated your objects, most of the hard work is done. Now all you need to do is create an instance of org.jcrom.Jcrom, add the classes you want to map, and then you can start mapping between nodes and objects.

## Create a Jcrom instance ##

The first thing you need to do is create a Jcrom instance, and tell it which classes you want to map. It is recommended that you create this instance once, and reuse it.

```
import org.jcrom.Jcrom;
...
Jcrom jcrom = new Jcrom();
jcrom.map(WeblogEntry.class);
...
```

Each class that you map will be validated, and a JcrMappinException will be thrown if the class is not valid for mapping. All child classes will be detected, validated, and mapped automatically. Therefore it is no need to map the Comment or Author classes above, since they are referenced as children from the WeblogEntry class.

You can also tell Jcrom to scan a package, and map all classes found in that package:

```
...
jcrom.mapPackage("my.package.with.only.jcr.entities");
jcrom.mapPackage("my.mixed.package", true);
...
```

The mapPackage() method optionally takes a second argument, a boolean that specifies whether to ignore invalid classes. This is useful if the package contains some classes that should not be mapped.

## Mapping an object to a JCR node ##

Let's say we have an instance of a Java object, and want to map it to a JCR node. We can choose to either create a new node for our object, or update an existing one. The decision is left to the user, JCROM does not try to determine whether to add a node or update.

So if we wanted to create a new WeblogEntry, we would do:

```
Session session = ...;
Author me = ...;

WeblogEntry weblogEntry = new WeblogEntry();
weblogEntry.setTitle("Hello world!");
weblogEntry.setPublishDate(new Date());
weblogEntry.setBody("This is my first weblog entry!");
weblogEntry.setAuthor(me);

try {
    Node parentNode = session.getRootNode().getNode("Weblog"); 
    jcrom.addNode(parentNode, weblogEntry);
    session.save();
} finally {
    session.logout();
}
```

If you need to pass in mixin types when adding the node, then use another version of addNode():

```
...
String[] mixinTypes = {"mix:referenceable"};
jcrom.addNode(parentNode, weblogEntry, mixinTypes);
...
```

And then if we later want to add a comment to that WeblogEntry:

```
Session session = ...;

Comment comment = new Comment();
comment.setTitle("Nice post!");
comment.setBody("This is my first comment.");
comment.setPublishDate(new Date());
weblogEntry.addComment(comment);

try {
    jcrom.updateNode(weblogEntryNode, weblogEntry);
    session.save();
} finally {
    session.logout();
}
```

JCROM offers good control over which child nodes are updated during a call to updateNode(), more about that in the "Using filters" section.

## Creating an object instance from a JCR node ##

To create an instance of a Java object from a JCR node, we can simply do:

```
Session session = ...;
String weblogEntryName = ...;

try {
    Node weblogEntryNode = session.getRootNode()
        .getNode("Weblog").getNode(weblogEntryName);
    WeblogEntry weblogEntry = jcrom.fromNode(WeblogEntry.class, weblogEntryNode);
} finally {
    session.logout();
}
```

Go [back](UserGuide.md) to the overview.