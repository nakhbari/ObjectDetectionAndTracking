package objectdetectionandtracking.tracking;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.swing.JFrame;

import objectdetectionandtracking.graphics.ImageFilteringPanel;
import objectdetectionandtracking.graphics.VideoDisplayPanel;
import objectdetectionandtracking.util.Vector2;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

/**
 * Accesses the computer's webcam and display the video feed in both RGB and
 * HSV. Allows the HSV images to be filtered via sliders. After a reasonably
 * sized object has been detected the centroid of it will be detected and marked
 * on both videos.
 * 
 * @author Nima Akhbari
 */
public class Tracking {

	// Constants
	private static final int VIDEO_FRAME_HEIGHT = 480;
	private static final int VIDEO_FRAME_WIDTH = 720;
	private static final int FILTER_FRAME_HEIGHT = 500;
	private static final int FILTER_FRAME_WIDTH = 250;
	// Constrains the number of objects able to be detected (including noise)
	private static final int MAX_NUM_OBJECTS = 20;
	// Minimum valid object area in pixel x pixel
	private static final int MIN_OBJECT_AREA = 10 * 10;
	// Maximum object area is to be a percentage of the frame's area
	private static final int MAX_OBJECT_AREA = (int) ((VIDEO_FRAME_HEIGHT * VIDEO_FRAME_WIDTH) * 0.67);;
	private static MouseListener mouseListener;
	private static Mat hsvImage;
	private static ImageFilteringPanel slider;
	private static VideoDisplayPanel normalPanel;
	private static final int NUM_CHANNELS = 3;

	/**
	 * Entry point for the program
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		long lastLoopTime = System.nanoTime();
		long lastFpsTime = 0;
		long fps = 0;
		boolean filterCalibration = true;
		boolean useCameraFeed = true;

		initializeMouseListener();
		List<TrackingObject> objects = new ArrayList<TrackingObject>();
		TrackingObject objectToTrack;

		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		// Create the Frame/Window to display video
		normalPanel = new VideoDisplayPanel();
		JFrame normalFrame = new JFrame("Normal");
		normalFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		normalFrame.setSize(VIDEO_FRAME_WIDTH, VIDEO_FRAME_HEIGHT);
		normalFrame.add(normalPanel);
		normalFrame.addMouseListener(mouseListener);
		normalFrame.setVisible(true);

		// HSV filtered content wil be displayed here
		VideoDisplayPanel hsvFilteredPanel = new VideoDisplayPanel();
		JFrame HSVFilteredframe = new JFrame("HSV Filtered");
		HSVFilteredframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		HSVFilteredframe.setSize(VIDEO_FRAME_WIDTH, VIDEO_FRAME_HEIGHT);
		HSVFilteredframe.add(hsvFilteredPanel);

		// Create frame to manipulate image level thresholding
		slider = new ImageFilteringPanel();
		JFrame sliderFrame = new JFrame("HSV Thresholds");
		sliderFrame.setSize(FILTER_FRAME_WIDTH, FILTER_FRAME_HEIGHT);
		sliderFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		sliderFrame.add(slider);

		if (filterCalibration) {
			sliderFrame.setVisible(true);
			HSVFilteredframe.setVisible(true);
		}

		// Open connection to webcam
		VideoCapture camera;
		if (useCameraFeed) {
			camera = new VideoCapture(0);
			if (!camera.isOpened())
				System.out.println("Can't open camera");
			camera.open(0);
		} else {
			camera = new VideoCapture(
					"/Users/nima/Documents/4TB6 Code/ObjectDetectionAndTracking/Gameplay Video Footage (Yellow Puck) 480p.mp4");
		}

		// Matrix that represents the individual images
		Mat originalImage = new Mat();
		System.out.println("type " + originalImage.type());
		hsvImage = new Mat();
		Mat thresholdedImage = new Mat();
		int objectcount = 0;

		while (true) {

			// Determine how long it's been since last update; this will be used
			// to calculate
			// how far entities should move this loop
			long currentTime = System.nanoTime();
			long updateLengthTime = currentTime - lastLoopTime;
			lastLoopTime = currentTime;

			// Update frame counter
			lastFpsTime += updateLengthTime;
			fps++;

			// Update FPS counter and remaining game time if a second has passed
			// since last recorded
			if (lastFpsTime >= 1000000000) {
				normalPanel.setFrameRate(fps);
				hsvFilteredPanel.setFrameRate(fps);
				lastFpsTime = 0;
				fps = 0;
			}

			// Read in the Webcam Frame
			camera.read(originalImage);

			if (!originalImage.empty()) {

				// Convert matrix from RGB to HSV
				Imgproc.cvtColor(originalImage, hsvImage, Imgproc.COLOR_BGR2HSV);

				if (filterCalibration) {
					objectToTrack = new TrackingObject();

					// Threshold the hsv image to filter the picture
					Core.inRange(hsvImage, slider.getScalarLow(),
							slider.getScalarHigh(), thresholdedImage);

					// reduce the noise in the image
					reduceNoise(thresholdedImage);

					// Find list of objects
					findObjects(thresholdedImage, objectToTrack, objects);

					// Draw circle where the centroid is
					// normalPanel.setObjects(objects);
					hsvFilteredPanel.setObjects(objects);
					objects.clear();

					hsvFilteredPanel
							.setImageBuffer(toBufferedImage(thresholdedImage));

				} else {
					objectToTrack = new Puck();

					// Search the hsvImage for the objectToTrack
					getObjects(objects, objectToTrack, thresholdedImage);

					// Draw circle where the centroid is
					normalPanel.setObjects(objects);

					if (objects.size() > 10) {
						objectcount += objects.size();
						objects.clear();
					}

				}

				// Display image
				normalPanel.setImageBuffer(toBufferedImage(originalImage));

			} else {
				System.out.println(objectcount + objects.size());
				System.out.println(" --(!) No captured frame -- Break!");
				break;
			}
		}
	}

	/**
	 * Searches the given hsvImage for the objectToTrack and adds it to the list
	 * of objects
	 * 
	 * @param objects
	 *            - list of TrackingObjects
	 * @param objectToTrack
	 *            - The desired object to be tracked
	 * @param hsvImage
	 *            - The HSV image
	 */
	private static void getObjects(List<TrackingObject> objects,
			TrackingObject objectToTrack, Mat hsvImage) {
		Mat hsvImageThresholded = new Mat();

		// Threshold the hsv image to filter for Pucks
		Core.inRange(hsvImage, objectToTrack.HSVmin, objectToTrack.HSVmax,
				hsvImageThresholded);

		// reduce the noise in the image
		reduceNoise(hsvImageThresholded);

		// Find list of Pucks
		findObjects(hsvImageThresholded, objectToTrack, objects);

	}

	/**
	 * Reduces the noise in the image by shrinking the groups of pixels to
	 * eliminate outliers, and then expand the pixels to restore the shrinked
	 * pixels.
	 * 
	 * @param image
	 *            - Image to be clarified
	 */
	private static void reduceNoise(Mat image) {

		// create structuring element that will be used to "dilate" and "erode"
		// image. the element chosen here is a 3px by 3px rectangle
		Mat erodeElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT,
				new Size(1, 1));

		// dilate with larger element so make sure object is nicely visible
		Mat dilateElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT,
				new Size(12, 12));

		// Erode will shrink the grouping of pixels
		Imgproc.erode(image, image, erodeElement);
		Imgproc.erode(image, image, erodeElement);
		Imgproc.erode(image, image, erodeElement);

		// // Dilate will expand the grouping of pixels
		Imgproc.dilate(image, image, dilateElement);
		Imgproc.dilate(image, image, dilateElement);
		// Imgproc.dilate(image, image, dilateElement);

	}

	/**
	 * Convert matrix into an image
	 * 
	 * @param m
	 *            - matrix to be converted
	 * @return Converted BufferedImage
	 */
	public static BufferedImage toBufferedImage(Mat m) {
		int type = BufferedImage.TYPE_BYTE_GRAY;
		if (m.channels() > 1) {
			type = BufferedImage.TYPE_3BYTE_BGR;
		}
		int bufferSize = m.channels() * m.cols() * m.rows();
		byte[] b = new byte[bufferSize];
		m.get(0, 0, b); // get all the pixels
		BufferedImage image = new BufferedImage(m.cols(), m.rows(), type);
		final byte[] targetPixels = ((DataBufferByte) image.getRaster()
				.getDataBuffer()).getData();
		System.arraycopy(b, 0, targetPixels, 0, b.length);
		return image;
	}

	/**
	 * Find the object and return the position of the centroid
	 * 
	 * @param inputImage
	 *            - Image that will be scanned for objects
	 * @return Position of the centroid
	 * */
	public static void findObjects(Mat inputImage, TrackingObject obj,
			List<TrackingObject> objects) {

		// Temp mat, as to not override the input Mat
		Mat image = new Mat();
		inputImage.copyTo(image);

		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Mat hierarchy = new Mat();

		// Find the contours of the image and save them into contours and
		// hierarchy Where the hierarchy holds the relationship between the
		// current contour point and the next
		Imgproc.findContours(image, contours, hierarchy, Imgproc.RETR_CCOMP,
				Imgproc.CHAIN_APPROX_SIMPLE);

		boolean objectFound = false;
		double refArea = 0;
		// The number of items in the hierarchy is the number of contours
		if (hierarchy.total() > 0) {

			int numObjects = (int) hierarchy.total();

			if (numObjects < MAX_NUM_OBJECTS) {
				// Go through each contour the hierarchy is a one dimensional
				// matrix 1xN where N is the number of contours. each item is
				// array of this format [Next, Previous, First_Child, Parent],
				// so we only want the next contour
				for (int index = 0; index >= 0
						&& hierarchy.get(index, 0) != null; index = (int) hierarchy
						.get(index, 0)[0]) {

					Moments moment = Imgproc.moments(contours.get(index));

					double area = moment.get_m00();

					// if the area is less than 20 px by 20px then it is
					// probably just noise if the area is the same as the 3/2 of
					// the image size, probably just a bad filter we only want
					// the object with the largest area so we safe a reference
					// area each iteration and compare it to the area in the
					// next iteration.
					if (area > MIN_OBJECT_AREA && area < MAX_OBJECT_AREA
							&& area > refArea) {
						obj.setX((float) (moment.get_m10() / area));
						obj.setY((float) (moment.get_m01() / area));

						objects.add(obj);
						refArea = area;
						objectFound = true;

						System.out.println("Object Found at Coordinate ("
								+ obj.getX() + "," + obj.getY() + ")");
					} else {
						objectFound = false;
					}

				}

			}

		}
	}

	private static ScalarRange GetColorRangeHSV(Mat image, int x, int y) {
		if (image.channels() < NUM_CHANNELS)
			return null;

		int radius = 10;
		GaussianDistribution<Double> hueDistribution = new GaussianDistribution<>();
		GaussianDistribution<Double> valueDistribution = new GaussianDistribution<>();
		GaussianDistribution<Double> saturationDistribution = new GaussianDistribution<>();
		ScalarRange range = null;

		int initX = (x - radius >= 0) ? (x - radius) : 0;
		int initY = (y - radius >= 0) ? (y - radius) : 0;
		int endX = (x + radius <= image.cols()) ? (x + radius) : image.cols();
		int endY = (y + radius <= image.rows()) ? (y + radius) : image.rows();
		double[] color;

		for (int i = initX; i < endX; i++) {
			for (int j = initY; j < endY; j++) {
				color = image.get(j, i);
				hueDistribution.addData(color[0]);
				saturationDistribution.addData(color[1]);
				valueDistribution.addData(color[2]);
			}
		}
		System.out.println("Hue Mean: " + hueDistribution.getMean() + " Dev: "
				+ hueDistribution.getDevation());
		System.out.println("Sat Mean: " + saturationDistribution.getMean()
				+ " Dev: " + saturationDistribution.getDevation());
		System.out.println("Val Mean: " + valueDistribution.getMean()
				+ " Dev: " + valueDistribution.getDevation());
		range = new ScalarRange(
				new Scalar(
						(hueDistribution.getMean() - (0.5 * hueDistribution
								.getDevation())),
						(saturationDistribution.getMean() - (2 * saturationDistribution
								.getDevation())),
						(valueDistribution.getMean() - (2 * valueDistribution
								.getDevation()))),
				new Scalar(
						(hueDistribution.getMean() + (0.5 * hueDistribution
								.getDevation())),
						(saturationDistribution.getMean() + (2 * saturationDistribution
								.getDevation())),
						(valueDistribution.getMean() + (2 * valueDistribution
								.getDevation()))));
		return range;
	}

	public static void initializeMouseListener() {
		mouseListener = new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
				slider.setSliders(GetColorRangeHSV(hsvImage, e.getX(), e.getY()));
				normalPanel.addObject(new TrackingObject(e.getX(), e.getY()));
			}

			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub

			}
		};
	}
}