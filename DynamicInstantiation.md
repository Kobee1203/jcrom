# Programming to interfaces: Dynamic instantiation #

New in release 1.2 is the ability to determine the class of the object to instantiate from a JCR property. This is especially important when programming to interfaces. In that case we have a child node as an interface, and then the actual implementation class used can depend on circumstances.

To use this feature, you need to do the following:

  1. Add a @JcrNode(classNameProperty="className") annotation to the interface.
  1. Map the implementing classes (not the interface!) in JCROM.
  1. Instantiate Jcrom with the dynamicInstantiation flag set to true.
  1. Use Jcrom to map to/from the interface.

For example, let's say we have a Shape interface:

```
import org.jcrom.JcrEntity;
import org.jcrom.annotations.JcrNode;

@JcrNode(classNameProperty="className")
public interface Shape extends JcrEntity {

	public double getArea();
}
```

Note that we have added a @JcrNode annotation, where we specify that the name of the
class should be stored in a JCR property on the node (we've set the name of the
property to "className"). If your node is of type nt:unstructured (default) then
there are no problems here, but if not, then you may need to create a mixin type that
supports the className property.

Also note that the interface does not need to extend the JcrEntity interface. This is just a convenience interface that has getters and setters for name and path.

Then we can have classes that implement the Shape interface:

```
import org.jcrom.AbstractJcrEntity;
import org.jcrom.annotations.JcrProperty;

public class Circle extends AbstractJcrEntity implements Shape {

	@JcrProperty private double radius;
	
	public Circle() {
	}
	
	public Circle( double radius ) {
		super();
		this.radius = radius;
	}
	
	public double getArea() {
		return Math.PI * (radius * radius);
	}

	public double getRadius() {
		return radius;
	}
}
```

and
```
import org.jcrom.AbstractJcrEntity;
import org.jcrom.annotations.JcrProperty;

public class Rectangle extends AbstractJcrEntity implements Shape {

	@JcrProperty private double height;
	@JcrProperty private double width;
	
	public Rectangle() {
	}
	
	public Rectangle( double height, double width ) {
		super();
		this.height = height;
		this.width = width;
	}
	
	public double getArea() {
		return height * width;
	}

	public double getHeight() {
		return height;
	}

	public double getWidth() {
		return width;
	}
}
```

Then these classes can be mapped as follows:

```
	Jcrom jcrom = new Jcrom(true, true);
	jcrom.map(Circle.class)
		.map(Rectangle.class);
		
	Shape circle = new Circle(5);
	circle.setName("circle");
		
	Shape rectangle = new Rectangle(5,5);
	rectangle.setName("rectangle");
		
	Node rootNode = session.getRootNode().addNode("dynamicInstTest");
	Node circleNode = jcrom.addNode(rootNode, circle);
	Node rectangleNode = jcrom.addNode(rootNode, rectangle);
	session.save();
		
	Shape circleFromNode = jcrom.fromNode(Shape.class, circleNode);
	Shape rectangleFromNode = jcrom.fromNode(Shape.class, rectangleNode);
```

Note that we use a different Jcrom constructor the above example:

```
Jcrom jcrom = new Jcrom(true, true);
```

This specifies that the Jcrom instance will clean node names (first parameter), and
use dynamic instantiation (second parameter). When using interfaces, the dynamic instantiation MUST be turned on using this parameter, or else the mapping will not work.

Note that this can also be used for mapping child nodes to interfaces. For example:

```
import java.util.ArrayList;
import java.util.List;
import org.jcrom.annotations.JcrChildNode;

public class ShapeContainer extends AbstractJcrEntity {

	@JcrChildNode private List<Shape> shapes;
	
	public ShapeContainer() {
		super();
		this.shapes = new ArrayList<Shape>();
	}

	public List<Shape> getShapes() {
		return shapes;
	}
	
	public void addShape( Shape shape ) {
		shapes.add(shape);
	}

	public void setShapes(List<Shape> shapes) {
		this.shapes = shapes;
	}

}
```

The ShapeContainer has a list of Shapes. JCROM will automatically store the class name of the implementing Shape class in a JCR property, and then use this to dynamically instantiate the correct implementation when creating a Shape object from a JCR node.

Go [back](UserGuide.md) to the overview.