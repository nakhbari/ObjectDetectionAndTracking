package objectdetectionandtracking.tracking;

import java.util.ArrayList;
import java.util.List;

public class GaussianDistribution<T extends Number> {
	private List<T> x;
	private double x_squared_sum;
	private double x_sum;
	private int n;

	public GaussianDistribution() {
		x = new ArrayList<>();
		x_sum = 0;
		x_squared_sum = 0;
		n = 0;
	}

	public void addData(T data) {
		x.add(data);
		x_sum += data.doubleValue();
		x_squared_sum += (Math.pow(data.doubleValue(), 2));
		n++;
	}

	public double getDevation() {
		return Math.sqrt((x_squared_sum - (Math.pow(x_sum, 2) / n)) / (n - 1));
	}

	public double getMean() {
		return x_sum / n;
	}
}
