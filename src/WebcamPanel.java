import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.Queue;

import javax.swing.JPanel;

public class WebcamPanel extends JPanel {
	static class Circle
	  {
	    public int x, y, radius;
	    
	    public Circle(){
	    	x = 0;
	    	y = 0;
	    	radius = 0;
	    }
	  }
	/**
	 * 
	 */
	private static final long serialVersionUID = 1;
	private static final int MAX_NUM_CIRCLES = 10;
	private static final int RADIUS_SIZE = 10;
	private BufferedImage imgBuffer;
	
	private Queue<Circle> circles ;

	public WebcamPanel() {
		super();
		circles = new LinkedList<Circle>();
	}

	public void paint(Graphics graphicsContext) {
		if (imgBuffer != null) {
			graphicsContext.drawImage(this.imgBuffer, 0, 0, this);
		}
		if(!circles.isEmpty()){
			for (Circle c : circles){
				graphicsContext.fillOval(c.x, c.y, c.radius, c.radius);
			}
		}
	}

	public void setImageBuffer(BufferedImage img) {
		this.imgBuffer = img;
		repaint();
	}

	public void addCircle(int x, int y){
		Circle circle = new Circle();
		circle.x = x;
		circle.y = y;
		circle.radius = RADIUS_SIZE;
		
		circles.add(circle);
		
		if(circles.size() > MAX_NUM_CIRCLES){
			circles.remove();
		}
	}
}
