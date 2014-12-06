package objectdetectionandtracking.graphics;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.swing.JPanel;

import objectdetectionandtracking.tracking.TrackingObject;

/**
 * Graphics panel that controls the video feed. Allows for new frames to be
 * added and for tracked object positions to be superimposed onto the image.
 * 
 * @author Nima Akhbari
 */

public class VideoDisplayPanel extends JPanel {

	private static final long serialVersionUID = 1;
	private static final int MAX_NUM_OBJECTS = 10;
	private static final int DEFAULT_RADIUS_SIZE = 10;
	private long frameRate;
	private BufferedImage imageBuffer;
	private Queue<TrackingObject> objects;

	public VideoDisplayPanel() {
		super();
		objects = new LinkedList<TrackingObject>();
		frameRate = 0;
	}

	/**
	 * Gets called by repaint(), and adds the components to the panel (this)
	 */
	public void paint(Graphics graphicsContext) {
		if (imageBuffer != null) {

			// Draws the image
			graphicsContext.drawImage(this.imageBuffer, 0, 0, this);
		}

		// Copy the Queue to avoid iterating while changes are being made to it
		Queue<TrackingObject> objectsCopy = new LinkedList<TrackingObject>(
				objects);
		if (!objectsCopy.isEmpty()) {
			for (TrackingObject obj : objectsCopy) {
				// For each circle in the queue, draw the circle
				graphicsContext.setColor(obj.getColor());
				graphicsContext.fillOval((int) obj.getX(), (int) obj.getY(),
						DEFAULT_RADIUS_SIZE, DEFAULT_RADIUS_SIZE);
			}
		}

		graphicsContext.setColor(Color.CYAN);
		graphicsContext.drawString("FPS: " + frameRate, 20, 20);
	}

	/**
	 * Sets the panel's buffered image
	 * 
	 * @param image
	 */
	public void setImageBuffer(BufferedImage image) {
		this.imageBuffer = image;

		// After setting a new image, repaint the panel
		repaint();
	}

	/**
	 * Adds circle to be drawn onto the buffered image
	 * 
	 * @param x
	 *            - X coordinate in pixels
	 * @param y
	 *            - Y coordinate in pixels
	 */
	public void addObject(TrackingObject obj) {

		// Add a new object to the array
		objects.add(obj);

		// Remove objects if the maximum has been reached
		if (objects.size() > MAX_NUM_OBJECTS) {
			objects.remove();
		}
	}

	public void setObjects(List<TrackingObject> objs) {
		// set a new objects as the array
		objects.clear();

		if (objs != null) {

			for (TrackingObject obj : objs) {
				objects.add(obj);
			}
		}
	}

	public void setFrameRate(long rate) {
		this.frameRate = rate;
	}

}
