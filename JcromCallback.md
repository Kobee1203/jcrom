# Jcrom Callbacks #

It can be useful in some cases to modify the default behavior of JCROM to add or update a node.

Since release 2.1.0, JCROM Callbacks have emerged to change the default actions to add a node, to update a node, add mixin types...

To do this, you must use the methods from the classes [Jcrom](http://jcrom.googlecode.com/svn/trunk/apidocs/org/jcrom/Jcrom.html) and [AbstractDao](http://jcrom.googlecode.com/svn/trunk/apidocs/org/jcrom/dao/AbstractJcrDAO.html) (for [Dao support](DaoSupport.md)) with a JcromCallback object as a parameter.

The easiest way is to extend the DefaultJcromCallback class, which contains the default implementation, and override methods to be modified.

```
jcrom.addNode(rootNode, parent, null, new DefaultJcromCallback(jcrom) {
    @Override
    public Node doAddNode(Node parentNode, String nodeName, JcrNode jcrNode, Object entity) throws RepositoryException {
       if (!(parentNode instanceof NodeImpl) && !(entity instanceof Parent)) {
           return super.doAddNode(parentNode, nodeName, jcrNode, entity);
       }
       NodeImpl parentNodeImpl = (NodeImpl) parentNode;
       Parent parentEntity = (Parent) entity;
       return parentNodeImpl.addNodeWithUuid(nodeName, parentEntity.getUuid());
    }
});
```

## Add a node ##

When adding a node, the methods that will be invoked are the following:

  * JcromCallback.doAddNode(Node, String, JcrNode, Object): Method invoked when a new node is added.
  * JcromCallback.doAddMixinTypes(Node, String[.md](.md), JcrNode, Object): Method invoked when mixin types are added to a new node.
  * JcromCallback.doAddClassNameToProperty(Node, JcrNode, Object): Method invoked when the class name is added to property for a newly created JCR node.
  * JcromCallback.doComplete(Object, Node): Method called after the node is completely added or updated. This method allows to perform other actions after creating or updating a node.

## Update a node ##

When updating a node, the methods that will be invoked are the following:

  * JcromCallback.doUpdateClassNameToProperty(Node, JcrNode, Object): Method invoked when the class name is updated to property for a node updated.
  * JcromCallback.doMoveNode(Node, Node, String, JcrNode, Object): Method invoked if the node name has changed and the node must be moved.
  * JcromCallback.doComplete(Object, Node): Method called after the node is completely added or updated. This method allows to perform other actions after creating or updating a node.

Go [back](UserGuide.md) to the overview.