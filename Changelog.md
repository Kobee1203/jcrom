# Release 2.2.0 (04.01.2014) #

This release includes:
  * [Custom Field Converters](Properties.md), to customize the conversion from an entity attribute�value to JCR property representation and conversely
  * [JavaFX properties support](JavaFXPropertiesSupport.md), to map Java objects with JavaFX properties directly to/from a JCR repository

Issues resolved: http://code.google.com/p/jcrom/issues/list?can=1&q=label%3AMilestone-Release2.1.0

# Release 2.1.0 (22.06.2013) #

This release includes fixes, new unit tests to check compatibility with ModeShape, refactoring, and a new feature, [JCROM Callbacks](JcromCallback.md).

Issues resolved: http://code.google.com/p/jcrom/issues/list?can=1&q=label%3AMilestone-Release2.1.0

# Release 2.0.1 (07.05.2013) #

This release includes fixes, improvements and new features, such as support for weak references, mapping protected properties, support for matching properties in NodeFilter.

Issues resolved: http://code.google.com/p/jcrom/issues/list?can=1&q=label%3AMilestone-Release2.0.1

# Release 2.0.0 (25.09.2012) #

This release includes fixes and improvements in handling versions and recursive references, and a couple of enhancements.

Issues resolved: http://code.google.com/p/jcrom/issues/list?can=1&q=label%3AMilestone-Release2.0.0

# Release 1.3.2 (27.01.2009) #

This release includes fixes and improvements in handling versions and recursive references, and a couple of enhancements.

Issues resolved: http://code.google.com/p/jcrom/issues/list?can=1&q=label%3AMilestone-Release1.3.2

# Release 1.3.1 (25.09.2008) #

This is a small maintenance release, with a number of bug fixes and a few enhancements.

Issues resolved: http://code.google.com/p/jcrom/issues/list?can=1&q=label%3AMilestone-Release1.3.1

# Release 1.3 (10.05.2008) #

This is the biggest release yet. Lots of new features! Highlights include lazy loading of child nodes and references, and dynamic maps of properties, child nodes, and references.

Issues resolved: http://code.google.com/p/jcrom/issues/list?q=label%3AMilestone-Release1.3&can=1

# Release 1.2 (03.04.2008) #

This release has many new features, such as support for java.util.Map as a child node, array support for properties, dynamic instantiation of objects from nodes (for programming to interfaces) and more.

Issues resolved: http://code.google.com/p/jcrom/issues/list?q=label%3AMilestone-Release1.2&can=1

# Release 1.1 (23.02.2008) #

Some bugfixes, but also enhancements such as support for references, and versioning support in the DAO classes. There is a bit of broken backwards compatibility. It is now mandatory to have a @JcrPath annotated field. Also, in the DAO classes: name-based methods are now path-based, and because of the removal of the static root path, the finder methods and the AbstractJcrDAO constructors have changed.

Issues resolved: http://code.google.com/p/jcrom/issues/list?q=label%3AMilestone-Release1.1&can=1

# Release 1.0 (04.02.2008) #

This is the initial release of JCROM.