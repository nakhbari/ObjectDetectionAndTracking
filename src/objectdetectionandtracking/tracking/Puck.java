package objectdetectionandtracking.tracking;

import java.awt.Color;

import org.opencv.core.Scalar;

public class Puck extends TrackingObject {
	public Puck() {
		super();
		color = Color.GREEN;
		HSVmin = new Scalar(49, 42, 37);
		HSVmax= new Scalar(88, 229, 157 );
	}

	public Puck(float x, float y) {
		super(x, y);
		color = Color.GREEN;
	}
}
