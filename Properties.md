# Properties #

We can map fields to JCR node properties. There are two categories of properties.

## Custom properties ##

You can map your Java fields to custom JCR properties using the @JcrProperty annotation. The following table shows which field types are valid for @JcrProperty annotation, and what JCR type they are mapped to:

| **Java type** | **JCR type** |
|:--------------|:-------------|
| String | STRING |
| Boolean, boolean | BOOLEAN |
| Double, double | DOUBLE |
| Integer, int | DOUBLE |
| Long, long | LONG |
| byte[.md](.md) | BINARY |
| java.io.InputStream | BINARY |
| java.util.Date | DATE |
| java.util.Calendar | DATE |
| java.sql.Timestamp | DATE |
| java.util.Locale | STRING |
| enum | STRING |

If we try to map a class with a @JcrProperty annotated field of type other than the above, we will get an exception telling us that the type is invalid.

We can map multi-valued properties in one of two ways:

  1. using an array of the property type
  1. using a java.util.List, as long as the List is parameterized with one of the Java types in the table above.

For example, our WeblogEntry class might need a list of tags:

```
import java.util.Date;
import java.util.List;
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
	
	...
}
```

The name of the JCR property will by default be the same as the name of the Java field. If you need to override this (for example to use namespaces), you can do so using the name parameter on the @JcrProperty annotation:

```
import org.jcrom.annotations.JcrName;
import org.jcrom.annotations.JcrPath;
import org.jcrom.annotations.JcrProperty;

public class MyClass {

    @JcrName private String name;
    @JcrPath private String path;
    @JcrProperty(name = "my:singleValue") private int mySingleValue;

    ...
}
```

## Custom converter ##

Since release 2.2.0, JCROM allows to customize the conversion from an entity attribute value to JCR property representation and conversely.
You must to implement the Converter interface as the following example:

```
public class ColorConverter implements Converter<Color, String> {

  @Override
  public String convertToJcrProperty(Color color) { ... }

  @Override
  public Color convertToEntityAttribute(String colorString) { ... }
}
```

You must then use the converter parameter on the @JcrProperty annotation:

```
public class EntityWithConverter extends AbstractJcrEntity {

  @JcrProperty(converter = ColorConverter.class)
  private Color color;
  
  ...
  
}
```

### Enum properties ###

JCROM supports mapping enums in the following way:

```
...
    public enum Suit { CLUBS, DIAMONDS, HEARTS, SPADES }
    
    @JcrProperty private Suit suit;
    @JcrProperty private Suit[] suitAsArray;
    @JcrProperty private List<Suit> suitAsList;
...
```

## Named read-only JCR properties ##

Depending on the node types and/or mixin types you use when mapping a Java object to JCR, the JCR will create default properties. JCROM provides a set of annotations to map to those read-only properties. This is useful if you want to use these properties in your Java code:

  * _@JcrUUID_ - If the node has a generated UUID stored in a jcr:uuid property (e.g. it is of the mixin type mix:referenceable), then that value will be read into the field annotated with this annotation. This must be a String field. <br />**Deprecated as of JCR 2.0**, @JcrIdentifier should be used instead.
  * _@JcrIdentifier_ - If the node has a generated Identifier stored in a jcr:identifier property (e.g. it is of the mixin type mix:referenceable), then that value will be read into the field annotated with this annotation. This must be a String field. **Since release 2.0**
  * _@JcrCreated_ - If the node has a creation date stored in a jcr:created property (e.g. it is of a type that extends nt:hierarchyNode), then that value will be read into the field annotated with this annotation.
  * _@JcrCheckedout_ - For a versioned node, stores whether the node is checked out. Must be on a boolean field.
  * _@JcrBaseVersionName_ - For a versioned node, stores the name of the base version.
  * _@JcrBaseVersionCreated_ - For a versioned node, stores the creation date of the base version.
  * _@JcrVersionName_ - For a node version, stores the name of that version.
  * _@JcrVersionCreated_ - For a node version, stores the creation date of that version.

Go [back](UserGuide.md) to the overview.