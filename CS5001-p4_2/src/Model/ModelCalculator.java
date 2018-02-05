package Model;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.imageio.ImageIO;

import UIDelegate.Square;
/**
 * The ModelCalculator class implements several method to update the Mandelbrot data and generate image from the data.
 * @author 170024030
 *
 */
public class ModelCalculator {
	//real horizontal
	public static final double INITIAL_MIN_REAL = -2.0;
	public static final double INITIAL_MAX_REAL = 0.7;
	//imaginary vertical
	public static final double INITIAL_MIN_IMAGINARY = -1.25;
	public static final double INITIAL_MAX_IMAGINARY = 1.25;
	public static final String INITIAL_COLOR_SCHEME = "BnW";
	public static final int INITIAL_MAX_ITERATIONS = 50;
	private BufferedImage image;	//the image generated from Mandelbrot data
	private int stateIndex;	//the index of current state
	private int width;	//the width of the image
	private int height;	//the height of the image
	private ArrayList<MandelbrotParam> data;	//the list used to implement redo and undo
	private ObjectInputStream ois;
	private ObjectOutputStream oos;
	
	/**
	 * The constructor.
	 * @param width
	 * @param height
	 */
	public ModelCalculator(int width, int height) {
		this.width = width;
		this.height = height;
		this.stateIndex = 0;
		this.data = new ArrayList<MandelbrotParam>() {
			private static final long serialVersionUID = 1L;
		{
			add(new MandelbrotParam(INITIAL_MIN_REAL, 
					INITIAL_MAX_REAL, 
					INITIAL_MIN_IMAGINARY, 
					INITIAL_MAX_IMAGINARY, 
					INITIAL_COLOR_SCHEME, 
					INITIAL_MAX_ITERATIONS));
		}};
		this.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	}

	/**
	 * getter of Index
	 * @return
	 */
	public int getStateIndex() {
		return stateIndex;
	}

	/**
	 * setter of Index
	 * @param stateIndex
	 */
	public void setStateIndex(int stateIndex) {
		this.stateIndex = stateIndex;
	}
	
	/**
	 * getter of the data list
	 * @return
	 */
	public ArrayList<MandelbrotParam> getData() {
		return data;
	}

	/**
	 * getter of the current state.
	 * @return
	 */
	public MandelbrotParam getState() {
		return data.get(stateIndex);
	}
	/**
	 * update the image with current parameters
	 */
	public void updateImage() {
		MandelbrotParam mp = data.get(stateIndex);
		MandelbrotCalculator mandelCalc = new MandelbrotCalculator();
		int[][] mandelbrotData = mandelCalc.calcMandelbrotSet(width, height, 
				mp.getMinReal(), 
				mp.getMaxReal(), 
				mp.getMinImag(), 
				mp.getMaxImag(),
				mp.getMaxIteration(), 
				MandelbrotCalculator.DEFAULT_RADIUS_SQUARED);
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int val = mandelbrotData[y][x];
				int color = calculateColor(val, mp.getColorScheme());
				image.setRGB(x, y, color);
			}
		}
	}
	
	/**
	 * calculator of the colour with given colour scheme.
	 * all colour schemes except for the "BnW" are implemented by modifying the HSB values.
	 * for "blue", "red" and "green", the brightness is changed according to the Mandelbrot set.
	 * for "rainbow" the hue is varied accordingly.
	 * @param val
	 * @param colorScheme
	 * @return
	 */
	public int calculateColor(int val, String colorScheme) {
		int maxIter = data.get(stateIndex).getMaxIteration();
		if(val == maxIter) return 0x00000000;
		switch (colorScheme) {
		case "BnW":
			return 0xffffffff;
		case "Blue":
			return Color.HSBtoRGB(0.5f, 1f, ((float) val / maxIter)%1f);
		case "Red":
			return Color.HSBtoRGB(1f, 1f, ((float) val / maxIter)%1f);
		case "Green":
			return Color.HSBtoRGB(0.333f, 1f, ((float) val / maxIter)%1f);
		case "Rainbow":
			return Color.HSBtoRGB((1f - (float) val / (float)maxIter), 1f, 1f);
		}
		return 0xffffffff;
	}

	/**
	 * getter of the image
	 * @return
	 */
	public BufferedImage getImage() {
		return image;
	}
	
	/**
	 * this method is used to remove the parameters after the current state.
	 * the main reason for this is to let the system store subsequent data after undo.
	 */
	public void removeTail () {
		for (int i = data.size() - 1; i > stateIndex; i--) {
			data.remove(i);
		}
	}
	
	/**
	 * There are three version of this method to add a new state to the data list that takes in different parameters.
	 * The first one takes in a square from the former Mandelbrot Set image.
	 * The new Mandelbrot Set data is generated with the ranges for real and imaginary part from the square.
	 * Other parameters are inherited from the former state.
	 * @param s
	 */
	public void setNextState(Square s) {
		stateIndex++;
		MandelbrotParam last = data.get(stateIndex - 1);
		double minr = s.getyMin() / 800.0 * (last.getMaxReal() - last.getMinReal()) + last.getMinReal();
		double mini = s.getxMin() / 800.0 * (last.getMaxImag() - last.getMinImag()) + last.getMinImag();
		double maxr = s.getyMax() / 800.0 * (last.getMaxReal() - last.getMinReal()) + last.getMinReal();
		double maxi = s.getxMax() / 800.0 * (last.getMaxImag() - last.getMinImag()) + last.getMinImag();
		data.add(new MandelbrotParam(minr, 
				maxr, 
				mini, 
				maxi, 
				last.getColorScheme(), 
				last.getMaxIteration()));
	}
	
	/**
	 * The second state does not take in arguments.
	 * This is used to change the colour setting for the image.
	 * The colour setting sequence is built in the parameter class.
	 * Other parameters are inherited from the former state.
	 */
	public void setNextState() {
		stateIndex++;
		MandelbrotParam last = data.get(stateIndex - 1);
    	data.add(new MandelbrotParam(last.getMinReal(), 
    			last.getMaxReal(), 
    			last.getMinImag(), 
    			last.getMaxImag(), 
    			last.getNextColor(last.getColorScheme()), 
    			last.getMaxIteration()));
	}
	
	/**
	 * The third state takes in new max iteration number.
	 * This is used to update the max iteration number.
	 * Other parameters are inherited from the former state.
	 * @param maxIter
	 */
	public void setNextState(int maxIter) {
		stateIndex++;
		MandelbrotParam last = data.get(stateIndex - 1);
    	data.add(new MandelbrotParam(last.getMinReal(), 
    			last.getMaxReal(), 
    			last.getMinImag(), 
    			last.getMaxImag(), 
    			last.getColorScheme(), 
    			maxIter));
	}
	
	/**
	 * This method is used to load Mandelbrot set data from a saved file.
	 * @param f
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public void loadParam(File f) throws IOException, ClassNotFoundException {
			FileInputStream fis = new FileInputStream(f);
			ois = new ObjectInputStream(fis);
			MandelbrotParam mp = (MandelbrotParam) ois.readObject();
			data.add(mp);
			stateIndex++;
	}
	
	/**
	 * This method is used to save the current Mandelbrot set parameters.
	 * 		File name format:
	 * 				"MSData_yyyy-MM-dd_HH:mm:ss.ser"
	 * @throws IOException
	 */
	public void saveParam() throws IOException {
		String fileName = "MSData_";
		String exten = ".ser";
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
    	Date date = new Date();
		FileOutputStream fos = new FileOutputStream(fileName + dateFormat.format(date) + exten);
		oos = new ObjectOutputStream(fos);
		oos.writeObject(data.get(stateIndex));
	}
	
	/**
	 * This method is used to save the current image.
	 * 		File name format:
	 * 				"MSPic_yyyy-MM-dd_HH:mm:ss.png"
	 * @throws IOException
	 */
	public void saveImage() throws IOException {
		String fileName = "MSPic_";
		String exten = ".png";
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
    	Date date = new Date();
    	String time = dateFormat.format(date);
		File savedImage = new File(fileName + time + exten);
		ImageIO.write(image, "PNG", savedImage);
	}
}
