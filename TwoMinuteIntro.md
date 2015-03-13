# Annotate your Java classes #

Let's say you have two simple Java classes:

```
import java.util.Date;

public class Contact {

    private String username;
    private String fullName;
    private String emailAddress;

    private Address address;

    // ... getters and setters
}
```

and

```
public class Address {

    private String name;
    private String street;
    private String city;
    private String postCode;
    private String country;

    // ... getters and setters
}
```

Then you can add JCROM annotations to these classes, telling JCROM which fields it should map to JCR nodes and properties. The classes then become:

```
import org.jcrom.annotations.JcrName;
import org.jcrom.annotations.JcrProperty;
import org.jcrom.annotations.JcrChildNode;

public class Contact {

    @JcrName private String username;
    @JcrPath private String path;
    @JcrProperty private String fullName;
    @JcrProperty private String emailAddress;

    @JcrChildNode private Address address;

    // ... getters and setters
}
```

and

```
import org.jcrom.annotations.JcrName;
import org.jcrom.annotations.JcrProperty;

public class Address {

    @JcrName private String name;
    @JcrPath private String path;
    @JcrProperty private String street;
    @JcrProperty private String city;
    @JcrProperty private String postCode;
    @JcrProperty private String country;

    // ... getters and setters
}
```

# Create a Jcrom instance and add the classes #

Next, you create an instance of org.jcrom.Jcrom, and add the classes you have annotated:

```
import org.jcrom.Jcrom;
...
Jcrom jcrom = new Jcrom();
jcrom.map(Contact.class);
...
```

Note that mapping the Contact class will automatically map the Address class, since it is referenced as a child node from Contact.

# Map to/from JCR nodes #

Now you can use the Jcrom instance to map Contacts and Addresses to/from JCR nodes. To create a JCR node from a Contact instance:

```
Contact contact = new Contact();
contact.setUsername("john");
contact.setFullName("John Doe");
contact.setEmailAddress("john.doe@somehost.com");

Address address = new Address();
address.setName("John's home address");
address.setStreet("123 Some street");
address.setCity("Some city");
address.setPostCode("123 456");
address.setCountry("Some country");
contact.setAddress(address);

Jcrom jcrom = ...;
Session session = ...;
try {
    Node parentNode = session.getRootNode().addNode("Contacts");
    jcrom.addNode(parentNode, contact);
    session.save();
} finally {
    session.logout();
}
```

And to load a Contact instance from a JCR node:

```
Jcrom jcrom = ...;
Session session = ...;
String username = ...;
try {
    Node contactNode = session.getRootNode().getNode("Contacts").getNode(username);
    Contact contact = jcrom.fromNode(Contact.class, contactNode);
} finally {
    session.logout();
}
```

# DAO Support #

Take advantage of the org.jcrom.dao.AbstractJcrDAO class to create DAO objects:

```
import javax.jcr.Session;
import org.jcrom.Jcrom;
import org.jcrom.dao.AbstractJcrDAO;

public class ContactDAO extends AbstractJcrDAO<Contact> {
        
        public ContactDAO( Session session, Jcrom jcrom ) {
                super(Contact.class, session, jcrom);
        }
}
```

This gives us a much cleaner approach:

```
Jcrom jcrom = ...;
Session session = ...;
String username = ...;
Contact contact = ...;
String parentPath = "/Contacts"; // this parent node must exist

ContactDAO contactDAO = new ContactDAO(session, jcrom);
contactDAO.create(parentPath, contact);

Contact anotherContact = contactDAO.get(contact.getPath());
List<Contact> allContacts = contactDAO.findAll(parentPath);
```

In a web application environment, we would probably inject the Session and the Jcrom instance into the DAO, and then inject the DAO into a controller, so the controller would never directly deal with Jcrom or the Session.

For more detailed description on how to use JCROM, refer to the [User Guide](UserGuide.md).