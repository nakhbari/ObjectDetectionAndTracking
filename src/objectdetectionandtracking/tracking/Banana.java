package objectdetectionandtracking.tracking;

import java.awt.Color;

import org.opencv.core.Scalar;

public class Banana extends TrackingObject {
	public Banana() {
		super();
		color = Color.YELLOW;
		HSVmin = new Scalar(19, 120, 130);
		HSVmax = new Scalar(140, 255, 213);
	}

	public Banana(float x, float y) {
		super(x, y);
		color = Color.YELLOW;
	}

}
