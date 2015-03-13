## Files ##

JCROM offers a few different strategies when mapping binary/file content. You can do this directly using a byte[.md](.md) or java.io.InputStream field:

```
import org.jcrom.annotations.JcrChildNode;
import org.jcrom.annotations.JcrName;
import org.jcrom.annotations.JcrPath;
import org.jcrom.annotations.JcrProperty;

public class MyClass {

    @JcrName private String name;
    @JcrPath private String path;
    @JcrProperty private byte[] myImage;

    ...
}
```

But the recommended way is to map to a nt:file node, and JCROM supports this with the JcrFile class. To use the JcrFile class, the file node child needs to be annotated with @JcrFileNode. Let's assume we allow users to upload a single image with each WeblogEntry. We can implement this as follows:

```
import java.util.Date;
import java.util.List;
import org.jcrom.JcrFile;
import org.jcrom.annotations.JcrChildNode;
import org.jcrom.annotations.JcrFileNode;
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

	@JcrFileNode private JcrFile image;
	
	...
}
```

JCROM will automatically create an nt:folder container node for the file, and map the JcrFile instance to an nt:file node within the folder. This is the recommended way of storing files in JCR.

The JcrFile node has fields that correspond (and map) to the nt:file properties and child nodes, such as last modified date, mime type, and encoding (optional). For accessing the file content, JcrFile has a JcrDataProvider field. The JcrDataProvider interface looks like this:

```
package org.jcrom;

import java.io.File;
import java.io.InputStream;

public interface JcrDataProvider {

	public enum TYPE {FILE, BYTES, STREAM}
	
	public TYPE getType();
	public boolean isFile();
	public boolean isBytes();
	public boolean isStream();
	public File getFile();
	public byte[] getBytes();
	public InputStream getInputStream();
	public void writeToFile(File file) throws IOException;
	public long getContentLength();
}
```

JCROM provides an implementation of this interface, which is used when reading a file node from JCR, and you can also use it when adding content to a JcrFile instance. For example, if we want to upload a file from a source to which we have an input stream:

```
...
WeblogEntry weblogEntry = ...;

String imageName = ...;
String imageMimeType = ...;
Calendar imageLastModified = ...;
InputStream imageStream = ...;

JcrFile image = new JcrFile();
image.setName(imageName);
image.setMimeType(imageMimeType);
image.setLastModified(imageLastModified);
image.setDataProvider( new JcrDataProviderImpl(imageStream) );

weblogEntry.setImage(image);
...
```

JcrDataUtils (before release 2.0, static methods in JcrFile) has static methods that can reduce the lines of code you write. For example, to create a JcrFile instance that has a data provider using a `java.io.File`, you can simply do:

```
String imageName = ...;
String imageMimeType = ...;
File imageFile = ...;

JcrFile jcrFile = JcrFile.fromFile(imageName, imageFile, imageMimeType);
```

Since release 2.0, new static methods were added in JcrDataUtils :

```
public static JcrFile fromFile(String name, File file) // Detects the mime type with Apache Tika library

public static JcrFile fromFile(String name, File file, String mimeType, String encoding)

public static JcrFile fromInputStream(String name, InputStream input) // Detects the mime type with Apache Tika library

public static JcrFile fromInputStream(String name, InputStream input, String mimeType)

public static JcrFile fromInputStream(String name, InputStream input, String mimeType, String encoding)

public static JcrFile fromByteArray(String name, byte[] bytes) // Detects the mime type with Apache Tika library

public static JcrFile fromByteArray(String name, byte[] bytes, String mimeType)

public static JcrFile fromByteArray(String name, byte[] bytes, String mimeType, String encoding)

public static String toString(JcrDataProvider dataProvider, String encoding) // Converts JcrDataProvider content to String

public static byte[] toByteArray(JcrDataProvider dataProvider) // Converts JcrDataProvider content to byte[]

public static InputStream toStream(JcrDataProvider dataProvider) // Converts JcrDataProvider content to InputStream
```

If you have more complex requirements for accessing the file content when mapping to JCR, you can create your own implementation of JcrDataProvider, and add that to your JcrFile instance.

Often we need to store some custom metadata on our file nodes. For example, we may want to store information about the photographer with the image. To achieve this we must subclass JcrFile, and add the fields for the custom metadata:

```
import org.jcrom.annotations.JcrNode;
import org.jcrom.annotations.JcrProperty;

@JcrNode(nodeType = "nt:unstructured")
public class Image extends JcrFile {

    @JcrProperty private String photographer;

    public Image() {
        super();
    }
   
    ...
}
```

Note that we have to specify the nt:unstructured node type. The reason for this is that JcrFile will automatically map to the nt:file node type, but that node type does not allow our photographer property. Therefore we have specified the nt:unstructured node type. A better solution would be to create our own node type that would be a sub-type of nt:file, with our custom metadata properties specified, and use that for the mapping.

JCROM supports updating metdata only on file nodes. For example, you may just want to update the name of the photographer, leaving the file itself intact. To do this, just make sure that the data provider on JcrFile is **`null`**.

JCROM supports mapping a list of JcrFile nodes, using a `java.util.List` (much in the same way as for properties and children).

Go [back](UserGuide.md) to the overview.