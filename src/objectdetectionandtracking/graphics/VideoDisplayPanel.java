package objectdetectionandtracking.graphics;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.Queue;

import javax.swing.JPanel;

import objectdetectionandtracking.tracking.PositionVector;

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
	private static final int DEFAULT_RADIUS_SIZE = 10;
	private long frameRate;
	private BufferedImage imageBuffer;
	private Queue<Circle> circles;
	private PositionVector positionA;
	private PositionVector positionB;

	public VideoDisplayPanel() {
		super();
		circles = new LinkedList<Circle>();
		frameRate = 0;
	}

	/**
	 * Gets called by repaint(), and adds the components
	 * to the panel (this)
	 */
	public void paint(Graphics graphicsContext) {
		if (imageBuffer != null) {

			// Draws the image
			graphicsContext.drawImage(this.imageBuffer, 0, 0, this);
		}

		// Copy the Queue to avoid iterating while changes are being made to it
		Queue<Circle> circlesCopy = new LinkedList<Circle>(circles);
		if (!circlesCopy.isEmpty()) {
			for (Circle c : circlesCopy) {
				// For each circle in the queue, draw the circle
				graphicsContext.setColor(Color.GREEN);
				graphicsContext.fillOval(c.x, c.y, c.radius, c.radius);
			}
		}
		graphicsContext.drawString("FPS: " + frameRate, 20, 20);
		
		if(positionA != null && positionB != null){
			graphicsContext.setColor(Color.RED);
			graphicsContext.drawLine(positionA.getX(), positionA.getY(), positionB.getX(), positionB.getY());
		}
	}

	/**
	 * Sets the panel's buffered image
	 * @param image
	 */
	public void setImageBuffer(BufferedImage image) {
		this.imageBuffer = image;

		// After setting a new image, repaint the panel
		repaint();
	}

	/**
	 * Adds circle to be drawn onto the buffered image
	 * @param x - X coordinate in pixels
	 * @param y - Y coordinate in pixels
	 */
	public void addCircle(int x, int y) {

		// Add a new circle to the array
		Circle circle = new Circle();
		circle.x = x;
		circle.y = y;
		circle.radius = DEFAULT_RADIUS_SIZE;

		circles.add(circle);

		// Remove circle if the maximum has been reached
		if (circles.size() > MAX_NUM_CIRCLES) {
			circles.remove();
		}
	}
	
	public void setFrameRate(long rate){
		this.frameRate = rate;
	}
	
	public void drawTrajectory(PositionVector positionA, PositionVector positionB){
		this.positionA = positionA;
		this.positionB = positionB;
	}
}
