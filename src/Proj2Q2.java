import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;


public class Proj2Q2 extends JPanel {
	private final int SIZE = 8; // block size that we are working with
	private JPanel leftPanel, rightPanel, buttonPanel;
	private JLabel leftLabel, rightLabel;
	private JButton fileButton, compressButton, quantizeButton, exitButton, saveButton;
	private ImageIcon leftIm,rightIm;
	private File f;
	private BufferedImage originalImage, quantizedImage, compressedImage;
	private Image leftI, rightI = null;
	private MatrixOp mp;
	private boolean imageLaid, imageQ, imageC;
	private int imageCounter = 1;
	private int fileCounter = 1;
	
	private double[][] yMatrix1, uMatrix1, vMatrix1;

	
	// Reference for converting color space from RGB to YUV and back
	private double[][] conversionMatrix = {{0.299, 0.587, 0.114},
										   {-0.299, -0.587, 0.886},
										   {0.701, -0.587, -0.114}};
	
	private double[][] conversionMatrixInverse = {{1.0, 0, 1.0},
												  {1.0, -0.194, -0.509},
												  {1.0, 1.0, 0}};
										
	
	

	public Proj2Q2() {
		mp = new MatrixOp();
		
		fileButton = new JButton("Select file");
		fileButton.setFocusable(false);
		
		compressButton = new JButton("Compress");
		compressButton.setFocusable(false);
		
		exitButton = new JButton("Quit");
		exitButton.setFocusable(false);
		
		quantizeButton = new JButton("Quantize");
		quantizeButton.setFocusable(false);
		
		saveButton = new JButton("Save Image");
		saveButton.setFocusable(false);
		
		leftPanel = new JPanel();
		leftPanel.setBounds(0, 100, 400, 500);
		leftPanel.setBackground(Color.black);
		
		leftLabel = new JLabel();
		leftPanel.add(leftLabel);
		
		rightPanel = new JPanel();
		rightPanel.setBounds(600, 100, 400 , 500);
		rightPanel.setBackground(Color.black);
		
		rightLabel = new JLabel();
		rightPanel.add(rightLabel);
		
		buttonPanel = new JPanel();
		buttonPanel.setBounds(250, 20, 500, 50);
		buttonPanel.setBackground(Color.black);
		buttonPanel.setLayout(new GridLayout(1,5));
		buttonPanel.add(fileButton);
		buttonPanel.add(quantizeButton);
		buttonPanel.add(compressButton);
		buttonPanel.add(saveButton);
		buttonPanel.add(exitButton);
		
		
		fileButton.addActionListener(new OpenFileListener());
		exitButton.addActionListener(new ExitListener());
		compressButton.addActionListener(new CompressListener());
		quantizeButton.addActionListener(new QuantizeListener());
		saveButton.addActionListener(new SaveListener());
		
		add(buttonPanel);
		add(leftPanel);
		add(rightPanel);
		
		
		setLayout(null);
		setBackground(Color.black);
		setPreferredSize(new Dimension(1000, 600));
		setVisible(true);
	}
	
	public void layImageLeft(Image i) {
		leftIm = new ImageIcon(i);
		leftLabel.setIcon(leftIm);
		leftPanel.revalidate();
		leftPanel.repaint();
	}
	
	public void layImageRight(Image i) {
		rightIm = new ImageIcon(i);
		rightLabel.setIcon(rightIm);
		rightPanel.revalidate();
		rightPanel.repaint();
	}
	
	public BufferedImage getQuantizedImage(BufferedImage im) {
		BufferedImage output = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_INT_RGB);
		
		yMatrix1 = new double[im.getWidth()][im.getHeight()];
		uMatrix1 = new double[im.getWidth()][im.getHeight()];
		vMatrix1 = new double[im.getWidth()][im.getHeight()];
		
		for(int i = 0; i < im.getWidth() / 8; i++) {
			for(int j = 0; j < im.getHeight() / 8; j++) {
				double[][] y = new double[SIZE][SIZE];
				double[][] u = new double[SIZE][SIZE];
				double[][] v = new double[SIZE][SIZE];
				
				// Get color values block by block
				for(int k = 0; k < SIZE; k++) {
					for(int l = 0; l < SIZE; l++) {
						int row = SIZE * i + k;
						int col = SIZE * j + l;
						
						int rgb = im.getRGB(row, col);
						int r = (rgb >> 16) & 0xFF;
						int g = (rgb >> 8) & 0xFF;
						int b = rgb & 0xFF;
						
						
						float yy = (float) (0.299 * r + 0.587 * g + 0.114 * b);
						float uu = (float) (-0.299 * r - 0.587 * g + 0.886 * b);
						float vv = (float) (0.701 * r - 0.587 * g - 0.114 * b);
						
						y[k][l] = yy;
						u[k][l] = uu;
						v[k][l] = vv;
					}
				}
					// DCT then quantize
					double[][] yyy = mp.DCT(y);
					mp.quantize(yyy);
					double[][] uuu = mp.DCT(u);
					mp.quantize(uuu);
					double[][] vvv = mp.DCT(v);
					mp.quantize(vvv);

	
				
				// Plug values back into block
				for(int k = 0; k < SIZE; k++) {
					for(int l = 0; l < SIZE; l++) {
						int row = SIZE * i + k;
						int col = SIZE * j + l;
						
						yMatrix1[row][col] = yyy[k][l];
						uMatrix1[row][col] = uuu[k][l];
						vMatrix1[row][col] = vvv[k][l];
					
						// Convert YUV to RGB
						int returnedR = (int) (1.0 * yyy[k][l] + 0 * uuu[k][l] + 1.0 * vvv[k][l]);
						int returnedG = (int) (1.0 * yyy[k][l] - 0.194 * uuu[k][l] - 0.509 * vvv[k][l]);
						int returnedB = (int) (1.0 * yyy[k][l] + 1.0 * uuu[k][l] - 0 * vvv[k][l]);
						
						
						// Bounds checking if color < 0 or > 255
						if(returnedR < 0) 
							returnedR = 0;
						
						if(returnedG < 0)
							returnedG = 0;
						
						if(returnedB < 0)
							returnedB = 0;
						
						if(returnedR > 255)
							returnedR = 255;
						
						if(returnedG > 255)
							returnedG = 255;
						
						if(returnedB > 255)
							returnedB = 255;
					
						
						// Place RGB back into image
						output.setRGB(row, col, (returnedR << 16) + (returnedG << 8) + returnedB);
					}
				}
			}
		}
		
		/* GETTING QUANTIZATION MATRIX OF YUV CHANNELS INTO TEXT FILE
		 * Uncomment if you want the quantization matrix into text file
		 * for the YUV channels
		*/
		
		/*matrixToTextFile(yMatrix1); Y channel
		matrixToTextFile(uMatrix1); U channel
		matrixToTextFile(vMatrix1); V channel*/
		
		return output;
	}
	
	
	public BufferedImage compress(BufferedImage im) {
		BufferedImage output = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_INT_RGB);
		
		for(int i = 0; i < im.getWidth() / 8; i++) {
			for(int j = 0; j < im.getHeight() / 8; j++) {
				
				double[][] y = new double[SIZE][SIZE];
				double[][] u = new double[SIZE][SIZE];
				double[][] v = new double[SIZE][SIZE];
				
				// Get color values block by block
				for(int k = 0; k < SIZE; k++) {
					for(int l = 0; l < SIZE; l++) {
						int row = SIZE * i + k;
						int col = SIZE * j + l;
						
						int rgb = im.getRGB(row, col);
						int r = (rgb >> 16) & 0xFF;
						int g = (rgb >> 8) & 0xFF;
						int b = rgb & 0xFF;
						
						
						float yy = (float) (0.299 * r + 0.587 * g + 0.114 * b);
						float uu = (float) (-0.299 * r - 0.587 * g + 0.886 * b);
						float vv = (float) (0.701 * r - 0.587 * g - 0.114 * b);
						
						y[k][l] = yy;
						u[k][l] = uu;
						v[k][l] = vv;
					}
				}
				
				// DCT then quantize
				double[][] yyy = mp.DCT(y);
				mp.quantize(yyy);
				mp.dequantize(yyy);
				double[][] uuu = mp.DCT(u);
				mp.quantize(uuu);
				mp.dequantize(uuu);
				double[][] vvv = mp.DCT(v);
				mp.quantize(vvv);
				mp.dequantize(vvv);
				
				// Inverse transform
				double[][] yyyy = mp.inverseTransform(yyy);
				double[][] uuuu = mp.inverseTransform(uuu);
				double[][] vvvv = mp.inverseTransform(vvv);
				

				
				
				
				// Plug values back into block
				for(int k = 0; k < SIZE; k++) {
					for(int l = 0; l < SIZE; l++) {
						int row = SIZE * i + k;
						int col = SIZE * j + l;
						
					
						// Convert YUV to RGB
						int returnedR = (int) (1.0 * yyyy[k][l] + 0 * uuuu[k][k] + 1.0 * vvvv[k][l]);
						int returnedG = (int) (1.0 * yyyy[k][l] - 0.194 * uuuu[k][l] - 0.509 * vvvv[k][l]);
						int returnedB = (int) (1.0 * yyyy[k][l] + 1.0 * uuuu[k][l] - 0 * vvvv[k][l]);					
						
						// Bounds checking if color < 0 or > 255
						if(returnedR < 0) 
							returnedR = 0;
						
						if(returnedG < 0)
							returnedG = 0;
						
						if(returnedB < 0)
							returnedB = 0;
						
						if(returnedR > 255)
							returnedR = 255;
						
						if(returnedG > 255)
							returnedG = 255;
						
						if(returnedB > 255)
							returnedB = 255;
					
						
						// Place RGB back into image
						output.setRGB(row, col, (returnedR << 16) + (returnedG << 8) + returnedB);
					}
				}
			}
		}
		return output;
	}
	
	private class QuantizeListener implements ActionListener {	
		public void actionPerformed(ActionEvent e) {
			if(e.getSource() == quantizeButton & imageLaid) {
				quantizedImage = getQuantizedImage(originalImage);
				rightI = quantizedImage.getScaledInstance(rightPanel.getWidth(),rightPanel.getHeight(), Image.SCALE_SMOOTH);
				layImageRight(rightI);
				
				imageQ = true;
				imageC = false;
			}
			
		}
		
	}
	
	private class OpenFileListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if(e.getSource() == fileButton) {
				JFileChooser fc = new JFileChooser();
				
				// Set directory
				//fc.setCurrentDirectory(new File("C:\\Users\\[YourUserName]\\Desktop"));
				int response = fc.showOpenDialog(null); 
				
				if(response == fc.APPROVE_OPTION) 
					f = new File(fc.getSelectedFile().getAbsolutePath());
	
				
				try {
					originalImage = ImageIO.read(f);
				}catch (IOException ee) {
					ee.printStackTrace();
				}
				
				leftI = originalImage.getScaledInstance(leftPanel.getWidth(), leftPanel.getHeight(),
						Image.SCALE_SMOOTH);
				layImageLeft(leftI);
				
				imageLaid = true;
			}		
	
		}
	}
	

	
	private class CompressListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if(e.getSource() == compressButton && imageLaid) {
				compressedImage = compress(originalImage);
				rightI = compressedImage.getScaledInstance(rightPanel.getWidth(), rightPanel.getHeight(), Image.SCALE_SMOOTH);
				layImageRight(rightI);
				
				imageC = true;
				imageQ = false;	
			}
		}
	
	}
	
	private class SaveListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if(e.getSource() == saveButton && imageLaid) {
				if(imageC) {
					File saveCompress = new File("bmp" + imageCounter + "_compressed_image.bmp");
					try {
						ImageIO.write(compressedImage, "bmp", saveCompress);
						imageCounter++;
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
				
				if(imageQ) {
					File saveQuantize = new File("bmp" + imageCounter + "_quantized_image.bmp");
					try {
						ImageIO.write(quantizedImage, "bmp", saveQuantize);
						imageCounter++;
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			
			}
		}
		
	}
	
	private class ExitListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			System.exit(0);
		}
	
	}
	
	public void matrixToTextFile(double[][] input) {
		File output = new File("bmp" + fileCounter + "_channel_" + fileCounter + "_qmatrix.txt");
		
		try {
			FileWriter fw = new FileWriter(output);
			
			for(int i = 0; i < input.length; i++) {
				fw.write("[" + i + "] ");
				for(int j = 0; j < input[i].length; j++) {
					fw.write(String.valueOf((int)input[i][j] + " "));
				}
				fw.write(";\n");
			}
			fileCounter++;
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.setTitle("Image Compression");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);;
		frame.add(new Proj2Q2());
		frame.pack();
		frame.setVisible(true);
	}
		
}
