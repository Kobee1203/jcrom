## Child nodes ##

Often we have to deal with an object structure that is not just a flat list of fields, but rather a tree of objects. For example, our WeblogEntry class that we described above, might have an Author. For this purpose, JCROM has the @JcrChildNode annotation. With this annotation, we can specify that our class has a child field which itself needs to be mapped to a JCR node:

```
import java.util.Date;
import java.util.List;
import org.jcrom.annotations.JcrChildNode;
import org.jcrom.annotations.JcrName;
import org.jcrom.annotations.JcrPath;
import org.jcrom.annotations.JcrProperty;

public class WeblogEntry {
	
	@JcrName private String name;
	@JcrPath private String path;
	@JcrProperty private String title;
	@JcrProperty private String excerpt;
	@JcrProperty private String body;
	@JcrProperty private Date publishDate;

	@JcrProperty private List<String> tags;

	@JcrChildNode private Author author;
	
	/** Default Constructor */
	public WeblogEntry() {
	}
	
	...
}
```

We have to make sure that classes marked as child nodes are correctly annotated as well. So in this case we need to annotate the Author class accordingly:

```
import org.jcrom.annotations.JcrName;
import org.jcrom.annotations.JcrPath;
import org.jcrom.annotations.JcrProperty;

public class Author {
	@JcrName private String username;
	@JcrPath private String path;
	@JcrProperty private String firstName;
	@JcrProperty private String lastName;
	@JcrProperty private String emailAddress;
	
	/** Default Constructor */
	public Author() {
	}
}
```

In a production environment, we would probably reference the author rather than including it, but this will do fine for demonstration purposes.

JCROM also supports mapping a list of children. This must be done using a `java.util.List` parameterized with the annotated child class. For example, our WeblogEntry class might need a list of comments. We would add this in the following way:

```
import java.util.Date;
import java.util.List;
import org.jcrom.annotations.JcrChildNode;
import org.jcrom.annotations.JcrName;
import org.jcrom.annotations.JcrPath;
import org.jcrom.annotations.JcrProperty;

public class WeblogEntry {
	
	@JcrName private String name;
	@JcrPath private String path;
	@JcrProperty private String title;
	@JcrProperty private String excerpt;
	@JcrProperty private String body;
	@JcrProperty private Date publishDate;

	@JcrProperty private List<String> tags;

	@JcrChildNode private Author author;
	@JcrChildNode private List<Comment> comments;
	
	/** Default Constructor */
	public WeblogEntry() {
	}

	...
}
```

And of course we would have to annotate the Comment class accordingly:

```
import java.util.Date;
import org.jcrom.annotations.JcrName;
import org.jcrom.annotations.JcrPath;
import org.jcrom.annotations.JcrProperty;

public class Comment {

	@JcrName private String name;
	@JcrPath private String path;
	@JcrProperty private String title;
	@JcrProperty private String body;
	@JcrProperty private Date publishDate;

	/** Default Constructor */
	public Comment() {
	}

	...
}
```

The child nodes can have their own children if needed, and in fact there is no difference in the annotation of the parent or children classes. For example, each Comment should have an author, but I'll leave it like this for now.

JCROM creates a container node for each child node. The name of this container node is by default the field name, but if you need to customise the name, you can do so by specifying a name parameter on the @JcrChildNode annotation:

```
import org.jcrom.annotations.JcrChildNode;
import org.jcrom.annotations.JcrName;
import org.jcrom.annotations.JcrPath;
import org.jcrom.annotations.JcrProperty;

public class MyClass {

    @JcrName private String name;
    @JcrPath private String path;
    @JcrProperty(name = "my:singleValue") private int mySingleValue;
    @JcrChildNode(name = "my:child") private Child child;

    /** Default Constructor */
    public MyClass() {
    }

    ...
}
```

To visualise how this container node mapping works, consider the following example. Let's say we have a WeblogEntry with the title "Hello world!", written by an author with the username "john", and with two comments. The node structure might then look like this (ignoring properties):

```
/Weblog/Hello_world
/Weblog/Hello_world/author/john
/Weblog/Hello_world/comments/I_like_your_post
/Weblog/Hello_world/comments/Me_too
```

In some cases, where you have a one-to-one mapping to the child node, creating a child container node is unnecessary. In such cases, we can use the "createContainerNode" property of @JcrChildNode. If this is set to false (defaults to true), then a container node will not be created for the child node. This also means that the name of the object will be ignored, and the field name (or the name specified in @JcrChildNode) will be used instead.

For example, you could have a User object with an Address object like this:

```
public class User {
...
   @JcrChildNode(createContainerNode=false) Address address;
...
}
```

This is a perfect example, since it does not make sense to give a name to the
address. The path to the Address could map to something like /Users/ogg/address.

Note that this will only work for single child nodes, not Lists or Maps, for reasons
that are hopefully obvious.

Go [back](UserGuide.md) to the overview.