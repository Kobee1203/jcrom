package org.jcrom;

import java.util.ArrayList;
import java.util.List;
import org.jcrom.annotations.JcrChildNode;

/**
 *
 * @author Olafur Gauti Gudmundsson
 */
public class ShapeParent extends AbstractJcrEntity {

	@JcrChildNode private Shape mainShape;
	@JcrChildNode private List<Shape> shapes;
	
	public ShapeParent() {
		super();
		this.shapes = new ArrayList<Shape>();
	}

	public Shape getMainShape() {
		return mainShape;
	}

	public void setMainShape(Shape mainShape) {
		this.mainShape = mainShape;
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
