## Node types ##

By default, JCROM maps objects to the nt:unstructured node type (the JcrFile class is an exception, more on that later!). This is handy for many cases, as it is a node type that allows any properties and child nodes. You can get up and running very quickly using this node type, and you can always add structure later.

However, if you have stricter requirements, and want to map your Java object to a custom node type, then you can use the @JcrNode annotation. This annotation is the only JCROM annotation that works on the type level (classes, interfaces). To map your class to a custom node type, simply annotate it as follows:

```
import org.jcrom.annotations.JcrName;
import org.jcrom.annotations.JcrPath;
import org.jcrom.annotations.JcrNode;

@JcrNode(nodeType = "my:type")
public class MyClass {
	@JcrName private String name;
	@JcrPath private String path;
	...
}
```

You can also use the @JcrNode annotation to specify mixin types to use when mapping an object to a JCR node:

```
import org.jcrom.annotations.JcrName;
import org.jcrom.annotations.JcrPath;
import org.jcrom.annotations.JcrNode;

@JcrNode(mixinTypes = {"mix:referenceable"})
public class MyClass {
	@JcrName private String name;
	@JcrPath private String path;
	...
}
```

Note that mixinTypes takes an array, so you can specify multiple mixin types.

Finally, you can use the @JcrNode annotation to store the full canonical name of the class being mapped in a JCR property. For example:

```
import org.jcrom.annotations.JcrName;
import org.jcrom.annotations.JcrPath;
import org.jcrom.annotations.JcrNode;

@JcrNode(classNameProperty = "className")
public class MyClass {
	@JcrName private String name;
	@JcrPath private String path;
	...
}
```

With the above annotation, JCROM would store the full name of the class in a property called "className" on the JCR node. This is important when using dynamic instantiation, which is described in a later section.

Go [back](UserGuide.md) to the overview.