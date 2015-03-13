# Dynamic Maps #

Since release 1.3, JCROM supports mapping a dynamic Map of properties, child nodes, files, and references. By "dynamic" I mean that the names and types of child nodes and references do not have to be specified at compile time. Instead, JCROM will map the objects dynamically at runtime.

[Lazy loading](LazyLoading.md) works in intelligent way for maps; each value of the map is lazy loaded (when lazy loading is turned on) rather than the whole map being lazy loaded.

Note that this relies on using nt:unstructured node type for the Map container.

Also note, that to use this feature, [dynamic instantiation](DynamicInstantiation.md) MUST be turned on, and each entity stored in the map must have @JcrNode(classNameProperty="className"). This is required to allow JCROM to determine the class to instantiate at runtime.

```
Jcrom jcrom = new Jcrom(true, true);
jcrom.map(Child.class);

@JcrNode(classNameProperty = "className")
public abstract class Child implements Serializable {

    @JcrPath protected String path;
    @JcrName protected String name;
    
    ...
}
```

Also note that the key is the name of the child node. If the value is an object with a field annotated with @JcrName, the value of this field will be overwritten by the value of the key.

If you enable to clean names of new nodes, you should use `PathUtils.createValidName(String)` method: `parent.getMap().put(mapper.getCleanName(child.getName()), child);`

## Map for properties ##

JCROM can map a java.util.Map of properties. JCROM will create an nt:unstructured node for the Map, and store each name-value pair in the Map as a property on that node. The Map must be parameterized with keys as java.lang.String, and values as a valid property type (or an array of such). Example:

```
    @JcrProperty private Map<String,String> properties;
    @JcrProperty private Map<String,Integer[]> integers;
```

## Map for child nodes ##

JCROM can map a java.util.Map of child nodes. The key type must be java.lang.String, and the value type must be java.lang.Object or a java.util.List of java.lang.Object. Example:

```
@JcrChildNode Map<String,Object> singleChildNodes;
@JcrChildNode Map<String,List<Object>> multiChildNodes;
```

## Map for file nodes ##

JCROM can map a java.util.Map of file nodes. The key type must be java.lang.String, and the value type must be JcrFile (or subclass) or a java.util.List of JcrFile (or subclass). Example:

```
@JcrFileNode private Map<String,JcrFile> files;
@JcrFileNode private Map<String,List<JcrFile>> fileLists;
```

## Map for references ##

JCROM can map a java.util.Map of references. The key type must be java.lang.String, and the value type must be java.lang.Object or a java.util.List of java.lang.Object. Example:

```
@JcrReference Map<String,Object> singleReferences;
@JcrReference Map<String,List<Object>> multiReferences;
```

Go [back](UserGuide.md) to the overview.