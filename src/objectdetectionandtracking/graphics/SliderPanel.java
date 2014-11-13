package objectdetectionandtracking.graphics;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.opencv.core.Scalar;

public class SliderPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JSlider[] high = new JSlider[3];
	private JSlider[] low = new JSlider[3];

	public SliderPanel() {
		initialize();
	}

	private void initialize() {
		if (high.length == low.length) {
			String[] values = {"Hue", "Saturation", "Value"};
			
			
			for (int i = 0; i < high.length; i++) {
				if (i == 0) {
					high[i] = new JSlider(0, 179);
					low[i] = new JSlider(0, 179);
				} else {
					high[i] = new JSlider(0, 255);
					low[i] = new JSlider(0, 255);
				}
				high[i].createStandardLabels(50);
				high[i].setMajorTickSpacing(50);
				high[i].setPaintTicks(true);
				high[i].setPaintLabels(true);
				high[i].setValue(high[i].getMaximum());
				high[i].addChangeListener(new ChangeListener() {

					@Override
					public void stateChanged(ChangeEvent e) {
						PrintValues();

					}
				});


				low[i].createStandardLabels(50);
				low[i].setPaintLabels(true);
				low[i].setMajorTickSpacing(50);
				low[i].setValue(high[i].getMinimum());
				low[i].setPaintTicks(true);
				low[i].addChangeListener(new ChangeListener() {

					@Override
					public void stateChanged(ChangeEvent e) {
						PrintValues();

					}
				});
				
				this.add(new JLabel(values[i] + " low"));
				this.add(low[i]);
				this.add(new JLabel(values[i] + " high"));
				this.add(high[i]);
			}
		}
	}

	public Scalar getScalarHigh() {
		return new Scalar(high[0].getValue(), high[1].getValue(),
				high[2].getValue());
	}

	public Scalar getScalarLow() {
		return new Scalar(low[0].getValue(), low[1].getValue(),
				low[2].getValue());
	}

	private void PrintValues() {
		System.out.println("Hue Range: (" + low[0].getValue() + ","
				+ high[0].getValue() + ") Saturation Range: (" + low[1].getValue() + ","
				+ high[1].getValue() + ") Value Range: (" + low[2].getValue() + ","
				+ high[2].getValue() + ")");
	}

}
