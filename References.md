# References #

JCROM supports JCR references using the @JcrReference annotation. Both single references and list of references is supported:

```
...
    @JcrReference MyClass singleReference;
    @JcrReference List<MyClass> multipleReferences;
...
```

For example, if we wanted to reference an Author from a WeblogEntry (instead of storing it as a child node), we would annotate as follows:

```
import java.util.Date;
import java.util.List;
import org.jcrom.annotations.JcrChildNode;
import org.jcrom.annotations.JcrName;
import org.jcrom.annotations.JcrPath;
import org.jcrom.annotations.JcrProperty;
import org.jcrom.annotations.JcrReference;

public class WeblogEntry {
	
	@JcrName private String name;
	@JcrPath private String path;
	@JcrProperty private String title;
	@JcrProperty private String excerpt;
	@JcrProperty private String body;
	@JcrProperty private Date publishDate;

	@JcrProperty private List<String> tags;

	@JcrChildNode private List<Comment> comments;

	@JcrReference private Author author;	
	...
}
```

A few things to mention about the reference support:

  * The field type class of a reference field MUST have a @JcrUUID annotated field. In the above example, this means that the Author class must have a @JcrUUID field.
  * When JCROM maps a node to an object, and a @JcrReference is found, then that referenced node will be loaded, and mapped to an object. In the above example, that means that when a WeblogEntry is loaded from a node, the author will also be loaded and mapped by JCROM automatically.
  * When JCROM maps an object with a @JcrReference to a node, it does it in the following way:
    1. Retrieve the value of the @JcrUUID annotated field in the reference object.
    1. Use the UUID to load the node being referenced.
    1. Create a property referencing the loaded node.
  * The filtering and max depth arguments in the Jcrom methods apply in same way to references as child nodes.

Therefore, if we were creating a new WeblogEntry, it is enough for us to know the Author UUID:

```
...
String authorUUID = ...;
Author author = new Author();
author.setUuid(authorUUID);

WeblogEntry entry = new WeblogEntry();
entry.setAuthor(author);
...
```

## Weak references ##

Since release 2.0.1, JCROM also supports managing weak references. This is done by setting weak to true:

```
    @JcrReference(weak=true) private Author author;
```

## Loading references by path ##

Since release 1.3, JCROM also supports managing references by path rather than UUID. This is done by setting byPath to true:

```
    @JcrReference(byPath=true) private Author author;
```

This works similarly to the UUID references, but you use path in all cases where you would have used UUID, and the referenced object does not have to be mix:referenceable.

This is supported to allow for weak references, as described in [David's Model](http://wiki.apache.org/jackrabbit/DavidsModel#head-ed794ec9f4f716b3e53548be6dd91b23e5dd3f3a).

This means that you can delete the referenced object without worrying about references. In the case that JCROM does not find an object at the path referenced, it simply ignores it.

Go [back](UserGuide.md) to the overview.