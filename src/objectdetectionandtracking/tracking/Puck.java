package objectdetectionandtracking.tracking;

import java.awt.Color;

import org.opencv.core.Scalar;

public class Puck extends TrackingObject {
	public Puck() {
		super();
		color = Color.GREEN;
		HSVmin = new Scalar(20, 65, 160);
		HSVmax = new Scalar(45, 208, 196);
	}

	public Puck(float x, float y) {
		super(x, y);
		color = Color.GREEN;
	}
}
