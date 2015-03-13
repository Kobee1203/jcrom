

**JCROM** (pronounced "Jack-rom") stands for Java Content Repository Object Mapper. It is a lightweight framework for mapping Java objects to/from a Java Content Repository (JCR). This is commonly called Object Content Mapping.

JCROM relies on annotations only, so there are no XML configuration files involved.

JCROM ensures that your objects are mapped to the JCR according to best practices, meanwhile abstracting the gritty details and making your code clean and readable.

  * annotation based (needs Java 1.5)
  * lightweight, minimal external [dependencies](Dependencies.md)
  * simple to learn, easy to use (and test)
  * removes error prone boiler plate code
  * works with any JCR implementation
  * works well with [Guice](UsingWithGuice.md) and [Spring](UsingWithSpring.md)
  * vision: do for JCR what Hibernate did for JDBC

You can look at the [documentation](UserGuide.md) for more explanations.


---


If you have ideas or suggestions to improve JCROM, please enter a new topic in the [User Mailing List](http://groups.google.com/group/jcrom).

If you have a bug with JCROM, please enter your issue on the [issues list](https://code.google.com/p/jcrom/issues/list) page.


---


**JCROM 2.2.0 is released**

This release includes:
  * [Custom Field Converters](Properties#Custom_converter.md), to customize the conversion from an entity attribute value to JCR property representation and conversely
  * [JavaFX properties support](JavaFXPropertiesSupport.md), to map Java objects with JavaFX properties directly to/from a JCR repository

You can look at the [resolved issues](http://code.google.com/p/jcrom/issues/list?can=1&q=label%3AMilestone-Release2.2.0).

[Download JAR](https://jcrom.googlecode.com/svn/repo/releases/org/jcrom/jcrom/2.2.0/jcrom-2.2.0.jar)

**JCROM 2.1.0 is out**

This release includes fixes, new unit tests to check compatibility with ModeShape, refactoring, and a new feature, [JCROM Callbacks](JcromCallback.md).

You can look at the [resolved issues](http://code.google.com/p/jcrom/issues/list?can=1&q=label%3AMilestone-Release2.1.0).

**JCROM 2.0.1 is out**

This release includes fixes, improvements and new features, such as support for weak references, mapping protected properties, support for matching properties in NodeFilter.

You can look at the [resolved issues](http://code.google.com/p/jcrom/issues/list?can=1&q=label%3AMilestone-Release2.0.1).

**JCROM 2.0 is out!**

Many changes have taken place, including:

  * Support for JCR 2.0
  * Improved AbstractJcrDAO class
  * Using [Spring Extension JCR](http://se-jcr.sourceforge.net/guide.html) with JCROM (transactions, JcrTemplate ...). See the documentation on [Spring](UsingWithSpring.md)
  * And many other things...