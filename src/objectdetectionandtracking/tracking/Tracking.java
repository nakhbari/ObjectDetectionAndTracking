package objectdetectionandtracking.tracking;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import objectdetectionandtracking.graphics.ImageFilteringPanel;
import objectdetectionandtracking.graphics.VideoDisplayPanel;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
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
	private static final int VIDEO_FRAME_HEIGHT = 720;
	private static final int VIDEO_FRAME_WIDTH = 1280;
	private static final int FILTER_FRAME_HEIGHT = 500;
	private static final int FILTER_FRAME_WIDTH = 250;
	// Constrains the number of objects able to be detected (including noise)
	private static final int MAX_NUM_OBJECTS = 4;
	// Minimum valid object area in pixel x pixel
	private static final int MIN_OBJECT_AREA = 20 * 20;
	// Maximum object area is to be a percentage of the frame's area
	private static final int MAX_OBJECT_AREA = (int) ((VIDEO_FRAME_HEIGHT * VIDEO_FRAME_WIDTH) * 0.67);;

	public static class PositionVector {
		int x;
		int y;

		public PositionVector() {
			x = 0;
			y = 0;
		}
	}

	public static void main(String[] args) {

		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		// Create the Frame/Window to display video
		VideoDisplayPanel normalPanel = new VideoDisplayPanel();
		JFrame normalFrame = new JFrame("Normal");
		normalFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		normalFrame.setSize(VIDEO_FRAME_WIDTH, VIDEO_FRAME_HEIGHT);
		normalFrame.add(normalPanel);
		normalFrame.setVisible(true);

		// HSV filtered content wil be displayed here
		VideoDisplayPanel hsvFilteredPanel = new VideoDisplayPanel();
		JFrame HSVFilteredframe = new JFrame("HSV Filtered");
		HSVFilteredframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		HSVFilteredframe.setSize(VIDEO_FRAME_WIDTH, VIDEO_FRAME_HEIGHT);
		HSVFilteredframe.add(hsvFilteredPanel);
		HSVFilteredframe.setVisible(true);

		// Create frame to manipulate image level thresholding
		ImageFilteringPanel slider = new ImageFilteringPanel();
		JFrame sliderFrame = new JFrame("HSV Thresholds");
		sliderFrame.setSize(FILTER_FRAME_WIDTH, FILTER_FRAME_HEIGHT);
		sliderFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		sliderFrame.add(slider);
		sliderFrame.setVisible(true);

		// Open connection to webcam
		VideoCapture camera = new VideoCapture(0);
		camera.open(0);

		// Matrix that represents the individual images
		Mat originalImage = new Mat();
		Mat hsvImage = new Mat();

		while (true) {

			// Read in the Webcam Frame
			camera.read(originalImage);

			if (!originalImage.empty()) {

				// Convert matrix from RGB to HSV
				Imgproc.cvtColor(originalImage, hsvImage, Imgproc.COLOR_BGR2HSV);

				// Threshold the hsv image to clarify the picture
				Core.inRange(hsvImage, slider.getScalarLow(),
						slider.getScalarHigh(), hsvImage);

				// Clarify the image
				hsvImage = morphOps(hsvImage);

				// Find object centroid position
				PositionVector position = getObjectsCentroid(hsvImage);

				// Draw circle where the centroid is
				normalPanel.addCircle(position.x, position.y);
				hsvFilteredPanel.addCircle(position.x, position.y);

				// Display image
				normalPanel.setImageBuffer(toBufferedImage(originalImage));
				hsvFilteredPanel.setImageBuffer(toBufferedImage(hsvImage));
			} else {
				System.out.println(" --(!) No captured frame -- Break!");
				break;
			}
		}
	}

	private static Mat morphOps(Mat hsv) {

		// create structuring element that will be used to "dilate" and "erode"
		// image. the element chosen here is a 3px by 3px rectangle
		Mat erodeElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT,
				new Size(3, 3));

		// dilate with larger element so make sure object is nicely visible
		Mat dilateElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT,
				new Size(8, 8));

		Imgproc.erode(hsv, hsv, erodeElement);
		Imgproc.erode(hsv, hsv, erodeElement);
		Imgproc.dilate(hsv, hsv, dilateElement);
		Imgproc.dilate(hsv, hsv, dilateElement);

		return hsv;

	}

	// Convert matrix into an image
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

	// Find the object and return the position of the centroid
	public static PositionVector getObjectsCentroid(Mat hsvImage) {
		// Temp mat
		Mat image = new Mat();
		hsvImage.copyTo(image);
		PositionVector position = new PositionVector();
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
						position.x = (int) (moment.get_m10() / area);
						position.y = (int) (moment.get_m01() / area);
						refArea = area;
						objectFound = true;
					} else {
						objectFound = false;
					}

				}
				// let user know you found an object
				if (objectFound) {
					System.out.println("Object Found at Coordinate ("
							+ position.x + "," + position.y + ")");
				}

			}

		}

		return position;
	}
}