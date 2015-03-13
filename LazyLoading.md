# Lazy loading #

Since release 1.3, JCROM supports lazy loading for child nodes, file nodes, and references.

To turn on lazy loading for child nodes, just do:

```
@JcrChildNode(lazy=true)
```

For files:

```
@JcrFileNode(lazy=true)
```

And similarly for references:

```
@JcrReference(lazy=true)
```

Note that this applies both to single children/files/references and lists of
children/files/references.

Also note that the depth and naming [filters](UsingFilters.md) can be used alongside lazy loading. So if a lazy loaded child node is under the max depth, it will be lazily loaded, but the loading of it will still maintain the max depth and naming filters applied to the parent.

Also note that lazy loading works with [dynamic maps](DynamicMaps.md) as well. In the case of dynamic maps, each value in the Map is lazy loaded (rather than loading the Map itself lazily).

Go [back](UserGuide.md) to the overview.