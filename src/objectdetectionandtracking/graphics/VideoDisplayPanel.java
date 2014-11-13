package objectdetectionandtracking.graphics;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.Queue;

import javax.swing.JPanel;

/**
 * Graphics panel that controls the video feed. Allows for new frames to be
 * added and for circles to be superimposed onto the image.
 * 
 * @author Nima Akhbari
 */

public class VideoDisplayPanel extends JPanel {

	private static class Circle {
		public int x, y, radius;

		public Circle() {
			x = 0;
			y = 0;
			radius = 0;
		}
	}

	private static final long serialVersionUID = 1;
	private static final int MAX_NUM_CIRCLES = 10;
	private static final int RADIUS_SIZE = 10;
	private BufferedImage imgBuffer;
	private Queue<Circle> circles;

	public VideoDisplayPanel() {
		super();
		circles = new LinkedList<Circle>();
	}

	// Gets called by repaint()
	public void paint(Graphics graphicsContext) {
		if (imgBuffer != null) {

			// Draws the image
			graphicsContext.drawImage(this.imgBuffer, 0, 0, this);
		}

		if (!circles.isEmpty()) {
			for (Circle c : circles) {
				// For each circle in the queue, draw the circle
				graphicsContext.fillOval(c.x, c.y, c.radius, c.radius);
			}
		}
	}

	public void setImageBuffer(BufferedImage img) {
		this.imgBuffer = img;

		// After setting a new image, repaint the panel
		repaint();
	}

	public void addCircle(int x, int y) {

		// Add a new circle to the array
		Circle circle = new Circle();
		circle.x = x;
		circle.y = y;
		circle.radius = RADIUS_SIZE;

		circles.add(circle);

		// Remove circle if the maximum has been reached
		if (circles.size() > MAX_NUM_CIRCLES) {
			circles.remove();
		}
	}
}
