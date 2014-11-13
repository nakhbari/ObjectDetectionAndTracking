package objectdetectionandtracking.graphics;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.opencv.core.Scalar;

/**
 * Graphics panel that displays sliders so that an HSV image levels can be
 * mutated, based on the Hue, Saturation and Value.
 * 
 * @author Nima Akhbari
 */
public class ImageFilteringPanel extends JPanel {

	// Constants
	private static final long serialVersionUID = 1L;
	private static final int NUM_TICKS_SPACING = 50;
	// Sliders for the high and lows of the Hue, Saturation and Value
	private JSlider[] high = new JSlider[3];
	private JSlider[] low = new JSlider[3];

	// Ranges for the sliders
	private int[] lowRange = { 0, 0, 0 };
	private int[] highRange = { 179, 255, 255 };
	private String[] levelType = { "Hue", "Saturation", "Value" };

	public ImageFilteringPanel() {
		initialize();
	}

	private void initialize() {

		if (high.length == low.length) {
			for (int i = 0; i < high.length; i++) {

				// initialize the slider to it's range
				high[i] = new JSlider(lowRange[i], highRange[i]);
				low[i] = new JSlider(lowRange[i], highRange[i]);

				// format the labeling and ticks
				high[i].createStandardLabels(NUM_TICKS_SPACING);
				high[i].setMajorTickSpacing(NUM_TICKS_SPACING);
				high[i].setPaintTicks(true);
				high[i].setPaintLabels(true);
				high[i].setValue(high[i].getMaximum());
				high[i].addChangeListener(new ChangeListener() {

					@Override
					public void stateChanged(ChangeEvent e) {
						// If the slider is moved, print out the values
						PrintValues();

					}
				});

				// format the labeling and ticks
				low[i].createStandardLabels(NUM_TICKS_SPACING);
				low[i].setMajorTickSpacing(NUM_TICKS_SPACING);
				low[i].setPaintLabels(true);
				low[i].setPaintTicks(true);
				low[i].setValue(low[i].getMinimum());
				low[i].addChangeListener(new ChangeListener() {

					@Override
					public void stateChanged(ChangeEvent e) {
						// If the slider is moved, print out the values
						PrintValues();

					}
				});

				// Display a label and then the respective slider
				this.add(new JLabel(levelType[i] + " low"));
				this.add(low[i]);
				this.add(new JLabel(levelType[i] + " high"));
				this.add(high[i]);
			}
		}
	}

	// Returns values of the High levels
	public Scalar getScalarHigh() {
		return new Scalar(high[0].getValue(), high[1].getValue(),
				high[2].getValue());
	}

	// Returns values of the Low levels
	public Scalar getScalarLow() {
		return new Scalar(low[0].getValue(), low[1].getValue(),
				low[2].getValue());
	}

	// Prints all the level values
	private void PrintValues() {
		System.out.println("Hue Range: (" + low[0].getValue() + ","
				+ high[0].getValue() + ") Saturation Range: ("
				+ low[1].getValue() + "," + high[1].getValue()
				+ ") Value Range: (" + low[2].getValue() + ","
				+ high[2].getValue() + ")");
	}

}
