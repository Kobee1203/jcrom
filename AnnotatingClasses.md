# Annotating your Java classes #

To use JCROM, you need to be able to annotate the class you want to map. All the JCROM annotations are on the field level, except for one that is on the class level. I'll introduce the most important ones, and then give a table of all the annotations. Let's assume that we have the following Java class:

```
import java.util.Date;

public class WeblogEntry {

	private String title;
	private String excerpt;
	private String body;
	private Date publishDate;
	
	/** Default Constructor */
	public WeblogEntry() {
	}

	// ... getters and setters
}
```

We have four simple fields here. Let's look at the class after annotating it with JCROM annotations:

```
import java.util.Date;
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
	
	/** Default Constructor */
	public WeblogEntry() {
	}

	public void setTitle( String title ) {
		this.title = title;
		this.name = title;
	}
	
	// ... other getters and setters
}
```

Two things to notice here. First of all, we've added a new field, "name", with the annotation @JcrName. The @JcrName annotation is mandatory: each class that is to be mapped using JCROM must have a String field that is annotated with @JcrName. This will be mapped to the node name, and will therefore represent the last token in the path to the node. The reason we've created another field, rather than use the title field, is that JCROM will clean the @JcrName field when mapping to a node name ("Hello, world!" would become "Hello\_world"). We therefore store the original title in the title field, and the clean version in the name field (note that in the title setter we set the name as well). We've also added a "path" field, annotated with @JcrPath. This is also mandatory. The path field is a JCR read-only field that will contain the full path to the JCR node storing the object.

Note that the name cleanup can be turned off by using the Jcrom( boolean cleanName ) constructor.

The second thing to notice, is that we have annotated the other fields using @JcrProperty. JCROM will map all fields annotated with @JcrProperty to a property on the node. For a list of valid property types, see the table later in this chapter. @JcrProperty can be also be used for multi-value properties, but such fields need to be mapped as java.util.List, parameterized with a valid field type (more on this in the annotation reference below).

In general, to annotate your object for JCROM, you do the following:

  1. Annotate a String field with @JcrName. This will store the node name. Mandatory.
  1. Annotate a String field with @JcrPath. This will store the full node path. Mandatory.
  1. Annotate the class with @JcrNode if you need to specify a custom node type to map to.
  1. Annotate fields with @JcrProperty. Multi-valued fields should be represented as a java.util.List with a valid property parameter.
  1. Annotate child objects with @JcrChildNode. The child objects must be annotated with JCROM annotations. Multiple children are represented as a java.util.List, parameterized with the child object type.
  1. Annotate parent object references with @JcrParentNode.
  1. Annotate file objects with @JcrFileNode.
  1. Annotate files for storing read-only JCR information with @JcrUUID, @JcrCreated.

## List of annotations ##

| **Annotation**  | **Description** |
|:----------------|:----------------|
| @JcrName      | This annotation is used to mark a field that contains the JCR node name. **Mandatory** |
| @JcrPath      | This annotation is used to mark a field that should store a JCR path read from a node. **Mandatory** |
| @JcrNode      | This annotation is applied to types (classes or interfaces) that implement JcrEntity, and provides the ability to specify what JCR node type to use when creating a JCR node from the object being mapped, mixin types, and more. |
| @JcrProperty  | This annotation is used mark fields that should be mapped to JCR node properties. |
| @JcrUUID     |  This annotation is used to mark a field that should store a JCR UUID read from a node. If the node does not have such a UUID, then the field will be left empty.<br />**Deprecated as of JCR 2.0**, @JcrIdentifier should be used instead. |
| @JcrIdentifier| This annotation is used to mark a field that should store a JCR Identifier read from a node. If the node does not have such a Identifier, then the field will be left empty.<br />**Since release 2.0**. |
| @JcrChildNode | This annotation is used to mark fields that are to be mapped as JCR child nodes. It can be applied to fields whos type has been mapped to Jcrom, or to a java.util.List that is parameterized with a mapped implementation.<br />Note that JCROM creates a container node for all child nodes. The child node is then created with the name retrieved via calling JcrEntity.getName() on the child object. |
| @JcrParentNode| This annotation is added to fields that implement JcrEntity, and are a reference to a parent object. Such fields are ignored when mapping to a JCR node, but when loading an object from a node, then field annotated with @JcrParentNode will be populated with the mapped parent object (if it was mapped). |
| @JcrFileNode  | This annotation is used to mark fields that are to be mapped as JCR file nodes. It can be applied to fields whos type is a JcrFile (or extension of that), or to a java.util.List that is parameterized with a JcrFile (or extension).<br />Note that JCROM creates a container node for all file nodes. The file node is then created with the name retrieved via calling JcrEntity.getName() on the child object. |
| @JcrCreated   | This annotation is used to mark a field that should store a JCR creation date read from a node. If the node does not have a creation date, then the field will be left empty. The jcr:created property is inherited from the nt:hierarchyNode, so unless your node type extends that type there is little use of using this annotation. |
| @JcrBaseVersionCreated |This annotation is used to mark a field that should store a JCR base version creation date read from the node.<br />Note: this will only be relevant for nodes that use the `mix:versionable` mixin type. Also note that this will be empty when iterating over older versions.|
| @JcrBaseVersionName | This annotation is used to mark a field that should store a JCR base version name read from the node.<br />Note: this will only be relevant for nodes that use the `mix:versionable` mixin type. Also note that this will be empty when iterating over older versions. |
| @JcrVersionCreated| This annotation is used to mark a field that should store a JCR version creation date read from the node.<br />Note: this will only be relevant for nodes that use the `mix:versionable` mixin type. Also note that this will be empty unless iterating over older versions. |
| @JcrVersionName| This annotation is used to mark a field that should store a JCR version name read from the node.<br />Note: this will only be relevant for nodes that use the `mix:versionable` mixin type. Also note that this will be empty unless iterating over older versions. |
| @JcrReference | This annotation is used to mark fields that are to be mapped as JCR reference properties. |
| @JcrCheckedout| This annotation is used to mark a field that should store whether a JCR node is checked out.<br />Note: this will only be relevant for nodes that use the mix:versionable mixin type. |
| @JcrSerializedProperty| This annotation is used mark fields that should be serialized to a byte array and then mapped to JCR node property. |

## Notes ##
  * JCROM uses reflection to mapping Java classes. It must Always define the default constructor.
  * JCROM uses field level annotation, not method level. The fields can be private. Therefore there is no requirement for getter and setter methods.
  * JCROM handles inheritance, so annotations from superclasses are automatically detected.

Go [back](UserGuide.md) to the overview.