## Parent nodes ##

If a child object needs to reference a parent object, then we can use the @JcrParentNode annotation inside the child object. For example, if a Comment needs a reference to its parent WeblogEntry object:


```
import java.util.Date;
import org.jcrom.annotations.JcrName;
import org.jcrom.annotations.JcrPath;
import org.jcrom.annotations.JcrParentNode;
import org.jcrom.annotations.JcrProperty;

public class Comment {

	@JcrName private String name;
	@JcrPath private String path;
	@JcrProperty private String title;
	@JcrProperty private String body;
	@JcrProperty private Date publishDate;

	@JcrParentNode private WeblogEntry weblogEntry;
	...
}
```

JCROM will notice this annotation when creating a WeblogEntry instance from a JCR node, and inject the parent WeblogEntry into each Comment. Note that JCROM will ignore this annotation if the parent object is not being loaded.

Go [back](UserGuide.md) to the overview.