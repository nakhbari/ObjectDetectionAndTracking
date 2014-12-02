package objectdetectionandtracking.tracking;

/**
 * Position Vector used to keep track of the x y coordinates of any object.
 * 
 * @author Nima Akhbari
 * 
 */
public class PositionVector {
	private int x;
	private int y;

	public PositionVector() {
		this.x = 0;
		this.y = 0;
	}

	public PositionVector(PositionVector position) {
		this.x = position.getX();
		this.y = position.getY();
	}

	public int getX() {
		return this.x;
	}

	public int getY() {
		return this.y;
	}

	public void setX(int x) {
		this.x = x;
	}

	public void setY(int y) {
		this.y = y;
	}

	public double getSlope(PositionVector previousPosition) {
		int nominator = this.y - previousPosition.getY();
		int denominator = this.x - previousPosition.getX();
		if (denominator != 0) {
			return nominator / denominator;
		} else {
			return Double.NaN;
		}
	}
}
