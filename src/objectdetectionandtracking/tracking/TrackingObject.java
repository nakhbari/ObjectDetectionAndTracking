package objectdetectionandtracking.tracking;

import java.awt.Color;

import objectdetectionandtracking.util.Vector2;

import org.opencv.core.Scalar;

public class TrackingObject {
	private Vector2 position;
	protected Color color;
	protected Scalar HSVmin, HSVmax;

	public TrackingObject() {
		position = new Vector2(0, 0);
		color = Color.BLACK;
	}

	public TrackingObject(float x, float y) {
		position = new Vector2(x, y);
		color = Color.BLACK;
	}

	public float getX() {
		return position.x;
	}

	public float getY() {
		return position.y;
	}

	public void setX(float x) {
		position.x = x;
	}

	public void setY(float y) {
		position.y = y;
	}

	public void setPosition(Vector2 position) {
		this.position = position;
	}

	public Color getColor() {
		return color;
	}

	public Scalar getHSVmin() {
		return HSVmin;

	}

	public Scalar getHSVmax() {
		return HSVmax;
	}
}
