package UIDelegate;

/**
 * The Square class contains five attributes as the four boundaries of a square and the square's side length.
 * The constructor below takes in two pairs of Cartesian coordinates and calculate the four attributes.
 * @author 170024030
 *
 */
public class Square {
	private double xMin;
	private double yMin;
	private double xMax;
	private double yMax;
	private double side;

	public double getxMin() {
		return xMin;
	}

	public double getyMin() {
		return yMin;
	}

	public double getxMax() {
		return xMax;
	}

	public double getyMax() {
		return yMax;
	}

	public double getSide() {
		return side;
	}
	
	/**
	 * This method generates a square that starts at the first point, 
	 * 										points to the second point's direction
	 * 										and has the side length of the longer projection.
	 * @param x1 x-coordinate of the first point.
	 * @param x2 x-coordinate of the second point.
	 * @param y1 y-coordinate of the first point.
	 * @param y2 y-coordinate of the second point.
	 */
	public Square(double x1, double x2, double y1, double y2) {
		side = Math.max(Math.abs(x1 - x2), Math.abs(y1 - y2));
		if(x1 < x2 && y1 < y2) {
			xMin = x1;
			yMin = y1;
			xMax = x1 + side;
			yMax = y1 + side;
		}
		else {
			if(x1 < x2 && y1 >= y2) {
				xMin = x1;
				yMax = y1;
				xMax = x1 + side;
				yMin = y1 - side;
			}
			else {
				if (x1 >= x2 && y1 < y2) {
					xMax = x1;
					yMin = y1;
					xMin = x1 - side;
					yMax = y1 + side;
				}
				else {
					if (x1 >= x2 && y2 >= y2) {
						xMax = x1;
						yMax = y1;
						xMin = x1 - side;
						yMin = y1 - side;
					}
				}
			}
		}
	}
}
