package objectdetectionandtracking.tracking;

import org.opencv.core.Scalar;

public class ScalarRange {
	private Scalar min;
	private Scalar max;

	public ScalarRange(Scalar min, Scalar max) {
		this.min = min;
		this.max = max;
	}
	
	public ScalarRange() {
		this.min = new Scalar(0,0,0);
		this.max = new Scalar(0,0,0);
	}


	public void setMin(Scalar min) {
		this.min = min;
	}

	public void setMax(Scalar max) {
		this.max = max;
	}

	public Scalar getMin() {
		return this.min;
	}

	public Scalar getMax() {
		return this.max;
	}
}
