## Using filters ##

There are times when you don't want to load/update some/any child nodes, references or properties. For example, we may want to list all the weblog entries, but without loading the comments for each entry. JCROM supports this with a NodeFilter parameter that can be used in the Jcrom.fromNode() and Jcrom.updateNode() methods.

The NodeFilter constructor takes three arguments:

  * _fieldNameFilter_ - this is a comma separated string with the names of children to include or exclude.

  * "comments" will include only the comments children
  * "-comments" will include everything but the comments children
  * star will include all children
  * "none" will exclude all children

  * _maxDepth_ - the maximum depth of included child nodes (0 means no child nodes are loaded, while a negative value means that no restrictions are set on the depth).

  * _filterDepth_ - the depth down to which the filter applies. If we are deeper than this, then the filter will be ignored. This is useful for example when you want to filter immediate children, but then load everything below unfiltered.

So, to load a weblog entry without the comments, we would do:

```
Session session = ...;
String weblogEntryName = ...;

try {
    Node weblogEntryNode = session.getRootNode()
        .getNode("Weblog").getNode(weblogEntryName);
    NodeFilter nodeFilter = new NodeFilter("-comments", NodeFilter.DEPTH_INFINITE);
    WeblogEntry weblogEntry 
        = jcrom.fromNode(WeblogEntry.class, weblogEntryNode, nodeFilter);
} finally {
    session.logout();
}
```

In much the same way, sometimes we do not want to update the child nodes when updating a parent. For example, if we need to update the body property of the WeblogEntry, then there is no need to update all the comment child nodes. Therefore, the Jcrom.updateNode() methods accept the filter arguments as well.

Note that the field name filter will not only apply to immediate children, but down the whole tree.

Since release 2.0.1, JCROM also supports filters for JCR properties. To do this, you must prefix the property name with "prop:" (a constant exists, NodeFilter.PROPERTY\_PREFIX).

Unlike child nodes that are not loaded if maxDepth = 0, the properties of the current node are loaded.

```
Session session = ...;
String weblogEntryName = ...;

try {
    Node weblogEntryNode = session.getRootNode()
        .getNode("Weblog").getNode(weblogEntryName);
    // Exclude title property
    NodeFilter nodeFilter = new NodeFilter("-" +  NodeFilter.PROPERTY_PREFIX + "title", NodeFilter.DEPTH_INFINITE);
    WeblogEntry weblogEntry 
        = jcrom.fromNode(WeblogEntry.class, weblogEntryNode, nodeFilter);
} finally {
    session.logout();
}
```

Go [back](UserGuide.md) to the overview.