package org.jcrom;

/**
 *
 * @author Olafur Gauti Gudmundsson
 */
public class Square extends Rectangle {

    public Square() {}

	public Square( double height, double width ) {
		super(height, width);
		if ( height != width ) {
            throw new IllegalArgumentException("This is not a square!");
        }
	}
}
