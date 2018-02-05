package UIDelegate;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import Model.ModelCalculator;
/**
 * The MandelbrotSetExplorer class contains method to establish the GUI and interact with users.
 * 	example usage:
 * 		new MandelbrotSetExplorer();
 * @author 170024030
 *
 */
public class MandelbrotSetExplorer extends JComponent{
	
	private static final long serialVersionUID = 1L;
	public static final int WIDTH = 800;
	public static final int HEIGHT = 800;
	public static final int MENU_WIDTH = 90;
	public static final int WHEEL_ZOOM_IN_RATIO = 100;
	private ArrayList<int[]> pointList = new ArrayList<int[]>();
	private int[] mouseLocation = new int[2];
	private BufferedImage image;
	private boolean released = false;
	private int startCoor[] = new int[2];
	private int endCoor[] = new int[2];
	private JMenuBar menu = new JMenuBar();
	private JFrame frame;
	private JToolBar toolbar;
	private boolean flagUndo = false;
	private JTextField textfield;
	
	
	ModelCalculator modelCalculator;
	
	/**
	 * Constructor. set size of the image, setup all components.
	 */
	public MandelbrotSetExplorer() {
		image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		frame = new JFrame("Mandelbrot Set Explorer");
		this.modelCalculator = new ModelCalculator(WIDTH, HEIGHT);
		setup();
	}
	
	
	
	@Override
	/**
	 * Set the size of the frame.
	 */
	public void addNotify() {
		setPreferredSize(new Dimension(WIDTH, HEIGHT));
	}

	@Override
	/**
	 * Override paint method.
	 * Paint the image generated from the Model part.
	 * Paint the square used to indicate the part to zoom in.
	 * @param g
	 */
	public void paint(Graphics g) {
		modelCalculator.updateImage();
		image = modelCalculator.getImage();
		Graphics2D g2d = (Graphics2D) g;
		g2d.drawImage(image, 0, 0, null);
		//painting the box generated by the location where mouse clicked (x1, y1) and dragged to (x2, y2).
		if (!released) {
			if (!pointList.isEmpty()) {
				int x1 = pointList.get(0)[0];
				int y1 = pointList.get(0)[1] - MENU_WIDTH;
				int x2 = pointList.get(pointList.size() - 1)[0];
				int y2 = pointList.get(pointList.size() - 1)[1] - MENU_WIDTH;
				Square s = new Square(x1, x2, y1, y2);
				g2d.drawRect((int) s.getxMin(), (int) s.getyMin(), (int) s.getSide(), (int) s.getSide());
			}
		}
	}

	/**
	 * Setup all components of the main frame including
	 * 			the menu set,
	 * 			the tool bar,
	 * 			mouse listeners,
	 * 			and the main canvas of Mandelbrot Set
	 */
	private void setup() {
		setupMenu();
		setupToolbar();
        addMouseListeners();
        Container contentPane = frame.getContentPane();
	    contentPane.add(toolbar, BorderLayout.NORTH);
	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(true);
		frame.getContentPane().add(this);
		frame.pack();
		frame.setVisible(true);
    }
	
	/**
	 * Setup the menu set with the file pull-down menu which includes
	 * 			load data,
	 * 			save data,
	 * 			and save image.
	 */
	private void setupMenu() {
        JMenu file = new JMenu("File");
        JMenuItem load = new JMenuItem("Load from Data");
        load.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	//A file chooser is implemented to choose the data to load from existing file.
            	JFileChooser chooser = new JFileChooser();
                FileNameExtensionFilter filter = new FileNameExtensionFilter(
                        "SER files", "ser");
                chooser.setFileFilter(filter);
                int returnVal = chooser.showOpenDialog(null);
                if(returnVal == JFileChooser.APPROVE_OPTION) {
                    try {
                    	//invoke the loading method to update the model.
                    	modelCalculator.loadParam(chooser.getSelectedFile());
    					frame.repaint();
    				} catch (FileNotFoundException e1) {
    					JOptionPane.showMessageDialog(frame, "Ooops, File not found!");
    				} catch (IOException e1) {
    					JOptionPane.showMessageDialog(frame, "Ooops, this is not a valid file!");
    				} catch (ClassNotFoundException e1) {
    					JOptionPane.showMessageDialog(frame, "Ooops, the data file is broken!");
    				}
                }
            }
        });
        
        JMenuItem save = new JMenuItem("Save Data");
        save.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	try {
            		//invoke the saving method in the model part.
                	modelCalculator.saveParam();
				} catch (Exception e2) {
					JOptionPane.showMessageDialog(frame, "Ooops, IOException reported!");
				}
            }
        });
        
        JMenuItem saveImage = new JMenuItem("Save Image");
        saveImage.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e){
				try {
					modelCalculator.saveImage();
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(frame, "Ooops, IOException reported!");
				}
				
			}
		});
        
        //add menu set to frame
        file.add(load);
        file.add(save);
        file.add(saveImage);
        menu.add(file);
        frame.setJMenuBar(menu);
    }
	
	/**
	 * Setup the frame with all buttons including
	 * 			reset,
	 * 			change colour,	
	 * 			undo,
	 * 			redo,
	 * 			a text field to change max iteration,
	 * 			and a button to update with the given max iteration.	
	 */
	private void setupToolbar() {
		JButton resetButton = new JButton("Reset");
        resetButton.addActionListener(new ActionListener() {
        	/**
        	 * construct a new model calculator to reset the view and settings.
        	 */
            public void actionPerformed(ActionEvent e) {
                modelCalculator = new ModelCalculator(WIDTH, HEIGHT);
                textfield.setText("" + modelCalculator.getData().get(modelCalculator.getStateIndex()).getMaxIteration());
                frame.repaint();
            }
        });
		
		JButton changeColourButton = new JButton("Change Colour");
        changeColourButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	if (flagUndo) {
					modelCalculator.removeTail();
					flagUndo = false;
				}
            	modelCalculator.setNextState();
            	frame.repaint();
            }
        });
        
        JButton undoButton = new JButton("Undo");
        undoButton.addActionListener(new ActionListener() {
        	/**
        	 * Redo is implemented by reducing the stateIndex by one.
        	 */
            public void actionPerformed(ActionEvent e) {
                if (modelCalculator.getStateIndex() > 0) {
                	modelCalculator.setStateIndex(modelCalculator.getStateIndex() - 1);
                	frame.repaint();
                	textfield.setText("" + modelCalculator.getData().get(modelCalculator.getStateIndex()).getMaxIteration());
                }
                flagUndo = true;
            }
        });

        JButton redoButton = new JButton("Redo");
        redoButton.addActionListener(new ActionListener() {
        	/**
        	 * Redo is implemented by increasing the stateIndex by one.
        	 */
            public void actionPerformed(ActionEvent e) {
            	if (flagUndo && modelCalculator.getStateIndex() < modelCalculator.getData().size() - 1) {
            		modelCalculator.setStateIndex(modelCalculator.getStateIndex() + 1);
            		textfield.setText("" + modelCalculator.getData().get(modelCalculator.getStateIndex()).getMaxIteration());
            		frame.repaint();
                }
            }
        });

        JLabel currenIter = new JLabel("Current iteration: ");
        textfield = new JTextField("" + modelCalculator.getData().get(modelCalculator.getStateIndex()).getMaxIteration());
        textfield.addActionListener(new ActionListener() {
        	/**
        	 * Allow the user to apply change with hitting the enter key.
        	 */
        	public void actionPerformed(ActionEvent e) {
        		String text = textfield.getText();
                int maxIter = Integer.parseInt(text);
                if (flagUndo) {
					modelCalculator.removeTail();
					flagUndo = false;
				}
            	modelCalculator.setNextState(maxIter);
            	frame.repaint();
            }
        });
        
        JButton applyChange = new JButton("Apply Change");
        applyChange.addActionListener(new ActionListener() {
        	/**
        	 * Add a new state with given maxIter and former settings.
        	 */
            public void actionPerformed(ActionEvent e) {
            	String text = textfield.getText();
                int maxIter = Integer.parseInt(text);
                if (flagUndo) {
					modelCalculator.removeTail();
					flagUndo = false;
				}
            	modelCalculator.setNextState(maxIter);
            	frame.repaint();
            }
        });
        /**
         * Add all to the frame.
         */
        toolbar = new JToolBar();
        toolbar.add(resetButton);
        toolbar.add(changeColourButton);
        toolbar.add(undoButton);
        toolbar.add(redoButton);
        toolbar.add(currenIter);
        toolbar.add(textfield);
        toolbar.add(applyChange);
    }
	
	
	private void addMouseListeners() {
		frame.addMouseListener(new MouseListener () {
			@Override
			public void mouseClicked(MouseEvent e) {
				// TODO Auto-generated method stub
			}
			
			@Override
			/**
			 * give the coordinates of the first point when clicked.
			 */
			public void mousePressed(MouseEvent e) {
				released = false;
				startCoor = new int[]{e.getX(),e.getY()};
			}
	
			@Override
			/**
			 * if mouse is released, generate new Mandelbrot Set.
			 */
			public void mouseReleased(MouseEvent e) {
				endCoor = new int[]{e.getX(),e.getY()};
				double y1 = startCoor[0];
				double x1 = startCoor[1] - MENU_WIDTH;
				double y2 = endCoor[0];
				double x2 = endCoor[1] - MENU_WIDTH;
				zoom(x1, x2, y1, y2);
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub
			}
		});
		
		frame.addMouseMotionListener(new MouseMotionListener(){
			//save points to draw the square on the image.
			public void mouseDragged(MouseEvent e) {
				pointList.add(new int[]{e.getX(),e.getY()});
				frame.repaint();
			}
			public void mouseMoved(MouseEvent e) {
				mouseLocation = new int[]{e.getX(), e.getY()};
			}
		});
		
		frame.addMouseWheelListener(new MouseWheelListener() {
			//zoom in with wheels
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				int notches = e.getWheelRotation();
				if (notches < 0) {
					double x1 = 0 + (mouseLocation[1] - 0) / WHEEL_ZOOM_IN_RATIO;
					double y1 = 0 + (mouseLocation[0] - 0) / WHEEL_ZOOM_IN_RATIO;
					double x2 = WIDTH - (WIDTH - mouseLocation[1]) / WHEEL_ZOOM_IN_RATIO;
					double y2 = HEIGHT - (HEIGHT - mouseLocation[0]) / WHEEL_ZOOM_IN_RATIO;
		    		zoom(x1, x2, y1, y2);
				    } else {
				    	double x1 = 0 - (mouseLocation[1] - 0) / WHEEL_ZOOM_IN_RATIO;
						double y1 = 0 - (mouseLocation[0] - 0) / WHEEL_ZOOM_IN_RATIO;
						double x2 = WIDTH + (WIDTH - mouseLocation[1]) / WHEEL_ZOOM_IN_RATIO;
						double y2 = HEIGHT + (HEIGHT - mouseLocation[0]) / WHEEL_ZOOM_IN_RATIO;
			    		zoom(x1, x2, y1, y2);
				    }
			}
		});

	}
	
	/**
	 * The method implemented to zoom in with given square. 
	 * @param x1
	 * @param x2
	 * @param y1
	 * @param y2
	 */
	private void zoom(double x1, double x2, double y1, double y2) {
		Square s = new Square(x1, x2, y1, y2);
		if (flagUndo) {
			modelCalculator.removeTail();
			flagUndo = false;
		}
		//generate a new state of Mandelbrot set with the square to zoom in.
		if (x1 != x2 || y1 != y2) {
			modelCalculator.setNextState(s);
			
			frame.repaint();
			try {
				Thread.sleep(40);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			pointList = new ArrayList<int[]>();
		}
		released = true;
	}

}