## Child objects as serialized properties ##

JCROM supports storing a child object as a serializes byte array. To do this, mark your field with @JcrSerializedProperty. The object type must implement java.io.Serializable. For example:

```
import org.jcrom.annotations.JcrName;
import org.jcrom.annotations.JcrPath;
import org.jcrom.annotations.JcrSerializedProperty;

public class MyClass {

    @JcrName private String name;
    @JcrPath private String path;
    @JcrSerializedProperty private MyChild child;

    ...
}
```

In the above case, JCROM will automatically serialize the child object, and store as a byte array in JCR. This of course works both ways, so when reading the object from JCR, JCROM will deserialize the byte array and add the resulting object to the child field.

This may also be useful for mapping a serializable object that you cannot annotate.

Go [back](UserGuide.md) to the overview.