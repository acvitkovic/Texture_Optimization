package main.java.ui;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.basic.BasicButtonUI;

import main.java.math.ObjSurfaceAreaCalculator;

import java.awt.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.prefs.Preferences;

public class MainWindow extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public JButton import2DButton;
	public JButton import3DButton;
	public JButton optimize2DButton;
	public JButton export2DButton;

	private File selectedImageFile;
	private String objFileContent;
	public BufferedImage originalImage;
	public BufferedImage scaledImage;

	public static int imageResolutionX;
	public static int imageResolutionY;
	public static double surfaceArea;
	public static int newImageResolutionX;
	public static int newImageResolutionY;

	public static int startLinearDistance = 5;
	public static float exponentialFactor = 0.1f;
	public static int programResolution = 4096;
	public static float newResolutionFactor = 1;

	// buttonGroup for the options
	private ButtonGroup buttonGroup;
	public JButton selectedButton;
	public static String optionString = "";
	public static double cameraDistance;
	public JButton optionsButton = new JButton("Camera Options");

	public JButton option1 = new JButton(
			"<html>1st Person" + "<br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br></html>");
	public JButton option2 = new JButton(
			"<html>3rd Person" + "<br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br></html>");
	public JButton option3 = new JButton(
			"<html>ARPG" + "<br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br></html>");
	public JButton option4 = new JButton(
			"<html>RTS/TBS" + "<br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br></html>");
	public JButton option5 = new JButton("<html>Custom Camera Distance"
			+ "<br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br></html>");

	// Session stored variable used to remember where we were at in the fileChooser
	private static String lastUsedDirectory2D = null;
	private static String lastUsedDirectory3D = null;

	// Get the screen dimensions
	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	int screenWidth = screenSize.width;
	int screenHeight = screenSize.height;

	// Set the desired percentage size
	double widthPercentage = 0.5; // 50% of the screen width
	double heightPercentage = 0.4; // 40% of the screen height

	// Calculate the desired width and height based on percentages
	int desiredScreenWidth = (int) (screenWidth * widthPercentage);
	int desiredScreenHeight = (int) (screenHeight * heightPercentage);

	public MainWindow() {
		// Set the window title
		setTitle("Texture Optimizer");

		// Set the size of the JFrame
		setSize(desiredScreenWidth, desiredScreenHeight);

		// Center the window on the screen
		setLocationRelativeTo(null);

		// Create the main panel
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());

		// Create the buttons
		import2DButton = new JButton("Import 2D Map");
		import3DButton = new JButton("Import 3D Model");
		optimize2DButton = new JButton("Optimize 2D Map");
		export2DButton = new JButton("Export 2D Map");

		optimize2DButton.setEnabled(false);
		export2DButton.setEnabled(false);

		import2DButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Open a file chooser dialog
				JFileChooser fileChooser = new JFileChooser();
				// Limit file chooser to PNG and JPG images only
				FileNameExtensionFilter filter = new FileNameExtensionFilter("Image Files", "png", "jpg");
				fileChooser.setFileFilter(filter);

				// Set the current directory to the last used directory (if available)
				if (lastUsedDirectory2D != null) {
					fileChooser.setCurrentDirectory(new File(lastUsedDirectory2D));
				}

				// Show the dialog and get the user's choice
				int returnVal = fileChooser.showOpenDialog(MainWindow.this);

				// Check if the user chose a file
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					// The selected PNG image file is now saved in the 'selectedImageFile' variable
					selectedImageFile = fileChooser.getSelectedFile();

					// Check to enable the Optimize Texture button
					if (selectedImageFile != null && objFileContent != null && cameraDistance != 0) {
						optimize2DButton.setEnabled(true);
					}
					// Save the last used directory for the next time the file chooser is opened
					lastUsedDirectory2D = selectedImageFile.getParent();

					try {
						// Read the image to get its dimensions
						originalImage = ImageIO.read(selectedImageFile);
						imageResolutionX = originalImage.getWidth();
						imageResolutionY = originalImage.getHeight();

						// Update the Button name to display the selected file and its dimensions
						import2DButton.setText("<html>Imported 2D Map: <br>" + selectedImageFile.getName()
								+ "<br><br>Image Resolution:<br>" + imageResolutionX + " x " + imageResolutionY
								+ "</html>");
					} catch (Exception ex) {
						// Error reading the image file
						ex.printStackTrace();
					}
				}
			}
		});

		import3DButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Open a file chooser dialog
				JFileChooser fileChooser = new JFileChooser();
				// Limit file chooser to .obj files only
				FileNameExtensionFilter filter = new FileNameExtensionFilter("OBJ Files", "obj");
				fileChooser.setFileFilter(filter);

				if (lastUsedDirectory3D != null) {
					fileChooser.setCurrentDirectory(new File(lastUsedDirectory3D));
				}

				// Show the dialog and get the user's choice
				int returnVal = fileChooser.showOpenDialog(MainWindow.this);

				// Check if the user chose a file
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File selectedFile = fileChooser.getSelectedFile();

					// Load the .obj file and save its content as a string
					objFileContent = loadObjFile(selectedFile);

					// Check to enable the Optimize Texture button
					if (selectedImageFile != null && objFileContent != null && cameraDistance != 0) {
						optimize2DButton.setEnabled(true);
					}

					lastUsedDirectory3D = selectedFile.getParent();
					// System.out.println(objFileContent);

					// Calculate the surface area
					try {
						surfaceArea = ObjSurfaceAreaCalculator.calculateSurfaceArea(selectedFile);
					} catch (FileNotFoundException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					// System.out.println("Surface Area: " + surfaceArea);

					// Update the Button name to more easily display what file was picked
					import3DButton.setText("<html>Imported 3D Model: <br>" + selectedFile.getName()
							+ "<br><br>Surface Area: " + String.format("%.3f", surfaceArea) + " m²</html>");

				}
			}

		});

		optimize2DButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				newImageResolutionX = 4096;
				newImageResolutionY = 4096;
				

				if (cameraDistance <= 5) {
					// Linear downscaling, fast
					for (int i = 0; i <= cameraDistance; i++) {
						newImageResolutionX = (int) (newImageResolutionX / 1.25);
						newImageResolutionY = (int) (newImageResolutionY / 1.25);
						// System.out.println(newImageResolutionX);
						// System.out.println(newImageResolutionY);
					}

				}
				if (cameraDistance > 5 && cameraDistance <= 20) {
					// Linear downscaling, slow
					for (int i = -1; i <= cameraDistance; i++) {
						newImageResolutionX = (int) (newImageResolutionX / 1.2);
						newImageResolutionY = (int) (newImageResolutionY / 1.2);
						// System.out.println(newImageResolutionX);
						// System.out.println(newImageResolutionY);
					}
				}
				if (cameraDistance > 20) {
					// Linear downscaling, the slowest
					for (int i = -7; i <= cameraDistance; i++) {
						newImageResolutionX = (int) (newImageResolutionX / 1.1);
						newImageResolutionY = (int) (newImageResolutionY / 1.1);
						// System.out.println(newImageResolutionX);
						// System.out.println(newImageResolutionY);
					}
					if (cameraDistance > 35) {
						for (int i = -7; i < cameraDistance; i++) {
							newImageResolutionX = (int) (newImageResolutionX / 1.04);
							newImageResolutionX += 6;
							newImageResolutionY = (int) (newImageResolutionY / 1.04);
							newImageResolutionY += 6;
						}
					}
				}
				if (newImageResolutionX < 32 || newImageResolutionY < 32) {
					// Increment by one in case the previous divisions lower the number to 0
					newImageResolutionX += 1;
					newImageResolutionY += 1;
					while (newImageResolutionX < 32 || newImageResolutionY < 32) {
						newImageResolutionX *= 2;
						newImageResolutionY *= 2;
					}
					// System.out.println(newImageResolutionX);
					// System.out.println(newImageResolutionY);
				}

				// The +1 helps resolve texture sizes for singular flat planes and objects
				// For models smaller than 1m squared
				if (surfaceArea > 1) {
					newImageResolutionX = (int) (newImageResolutionX * (surfaceArea));
					newImageResolutionY = (int) (newImageResolutionY * (surfaceArea));
				} else {
					newImageResolutionX = (int) (newImageResolutionX * (surfaceArea + 1));
					newImageResolutionY = (int) (newImageResolutionY * (surfaceArea + 1));
				}

				// System.out.println(newImageResolutionX);
				// System.out.println(newImageResolutionY);

				// Only check of 1 variable is enough as we scale both of them equally.
				if (newImageResolutionX > 4096) {
					newImageResolutionX = 4096;
					newImageResolutionY = 4096;
				}
				if (newImageResolutionX > imageResolutionX) {
					newImageResolutionX = imageResolutionX;
					newImageResolutionY = imageResolutionY;
				}

				export2DButton.setEnabled(true);
				
				

				optimize2DButton
						.setText("<html>New Resolution: <br>" + newImageResolutionX + " x " + newImageResolutionY);
			}
		});

		export2DButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Add code here to handle exporting the down scaled image
				scaledImage = scaleImage(originalImage, newImageResolutionX, newImageResolutionY);

				// Show a file chooser dialog for exporting the image
				JFileChooser fileChooser = new JFileChooser();
				FileNameExtensionFilter filter = new FileNameExtensionFilter("PNG Images", "png"); // Change filter to
																									// PNG
				fileChooser.setFileFilter(filter);

				// Set the initial directory to the last saved location, if available
				String lastSavedDirectory = System.getProperty("user.home"); // Default to user's home directory
				Preferences prefs = Preferences.userRoot().node(getClass().getName());
				String lastDirectory = prefs.get("lastDirectory", null);
				if (lastDirectory != null) {
					File lastDir = new File(lastDirectory);
					if (lastDir.exists() && lastDir.isDirectory()) {
						lastSavedDirectory = lastDirectory;
					}
				}
				fileChooser.setCurrentDirectory(new File(lastSavedDirectory));

				int result = fileChooser.showSaveDialog(null);
				if (result == JFileChooser.APPROVE_OPTION) {
					File selectedFile = fileChooser.getSelectedFile();
					String filePath = selectedFile.getAbsolutePath();

					// Check if the file name already has the .png extension
					if (!filePath.toLowerCase().endsWith(".png")) {
						filePath += ".png"; // Append the .png extension
					}

					// Export the scaled image as a PNG file
					try {
						ImageIO.write(scaledImage, "png", new File(filePath)); // Change format to "png"
						JOptionPane.showMessageDialog(null, "Image exported successfully.", "Success",
								JOptionPane.INFORMATION_MESSAGE);

						// Save the last directory location
						prefs.put("lastDirectory", selectedFile.getParent());
					} catch (Exception ex) {
						JOptionPane.showMessageDialog(null, "Error exporting image: " + ex.getMessage(), "Error",
								JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});

		// Create a separate panel for the buttons
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(1, 4, 10, 10));
		buttonPanel.add(import2DButton);
		buttonPanel.add(import3DButton);
		buttonPanel.add(optimize2DButton);
		buttonPanel.add(export2DButton);

		optionsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Open the options dialog
				openOptionsDialog();

			}
		});

		// Add the button panel to the main panel at the CENTER
		mainPanel.add(buttonPanel, BorderLayout.CENTER);

		// Add the "Options" button to the main panel at the SOUTH
		mainPanel.add(optionsButton, BorderLayout.SOUTH);

		// Add the main panel to the frame
		add(mainPanel);

		// Set the default close operation to exit the application
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	// Other methods
	private void openOptionsDialog() {
		// Create a new JDialog for the options
		JDialog optionsDialog = new JDialog(this, "Camera Options", true); // 'this' refers to the MainWindow as the
																			// parent
		// Create a ButtonGroup to group the buttons together
		buttonGroup = new ButtonGroup();

		// Set the size of the dialog
		optionsDialog.setSize(desiredScreenWidth, desiredScreenHeight);

		// Center the dialog on the screen relative to the main window
		;
		optionsDialog.setLocationRelativeTo(this);

		// Create the "Apply" button for the options dialog
		JButton applyButton = new JButton("Apply");

		// Add the buttons to the group
		buttonGroup.add(option1);
		buttonGroup.add(option2);
		buttonGroup.add(option3);
		buttonGroup.add(option4);
		buttonGroup.add(option5);

		ImageIcon icon1 = createImageIcon("/Resources/1stPerson.png");
		ImageIcon icon2 = createImageIcon("/Resources/3rdPerson.png");
		ImageIcon icon3 = createImageIcon("/Resources/ARPG.png");
		ImageIcon icon4 = createImageIcon("/Resources/RTS.png");
		ImageIcon icon5 = createImageIcon("/Resources/Custom.png");

		// Create a custom UI for the button to set the background image
		option1.setUI(new BasicButtonUI() {
			@Override
			public void paint(Graphics g, JComponent c) {
				Graphics2D g2 = (Graphics2D) g.create();
				// Set the alpha (transparency) value (0.0f: fully transparent, 1.0f: fully
				// opaque)
				float alpha = 0.5f;
				AlphaComposite alphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
				g2.setComposite(alphaComposite);

				// Paint the background image with transparency
				g2.drawImage(icon1.getImage(), 0, 0, option1.getWidth(), option1.getHeight(), null);

				// Paint the button's text (centered)
				alphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1);
				g2.setComposite(alphaComposite);
				super.paint(g2, c);

				g2.dispose();
			}
		});
		option2.setUI(new BasicButtonUI() {
			@Override
			public void paint(Graphics g, JComponent c) {
				Graphics2D g2 = (Graphics2D) g.create();
				// Set the alpha (transparency) value (0.0f: fully transparent, 1.0f: fully
				// opaque)
				float alpha = 0.5f;
				AlphaComposite alphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
				g2.setComposite(alphaComposite);

				// Paint the background image with transparency
				g2.drawImage(icon2.getImage(), 0, 0, option2.getWidth(), option2.getHeight(), null);

				// Paint the button's text (centered)
				alphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1);
				g2.setComposite(alphaComposite);
				super.paint(g2, c);

				g2.dispose();
			}
		});
		option3.setUI(new BasicButtonUI() {
			@Override
			public void paint(Graphics g, JComponent c) {
				Graphics2D g2 = (Graphics2D) g.create();
				// Set the alpha (transparency) value (0.0f: fully transparent, 1.0f: fully
				// opaque)
				float alpha = 0.5f;
				AlphaComposite alphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
				g2.setComposite(alphaComposite);

				// Paint the background image with transparency
				g2.drawImage(icon3.getImage(), 0, 0, option3.getWidth(), option3.getHeight(), null);

				// Paint the button's text (centered)
				alphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1);
				g2.setComposite(alphaComposite);
				super.paint(g2, c);

				g2.dispose();
			}
		});

		option4.setUI(new BasicButtonUI() {
			@Override
			public void paint(Graphics g, JComponent c) {
				Graphics2D g2 = (Graphics2D) g.create();
				// Set the alpha (transparency) value (0.0f: fully transparent, 1.0f: fully
				// opaque)
				float alpha = 0.5f;
				AlphaComposite alphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
				g2.setComposite(alphaComposite);

				// Paint the background image with transparency
				g2.drawImage(icon4.getImage(), 0, 0, option4.getWidth(), option4.getHeight(), null);

				// Paint the button's text (centered)
				alphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1);
				g2.setComposite(alphaComposite);
				super.paint(g2, c);

				g2.dispose();
			}
		});

		option5.setUI(new BasicButtonUI() {
			@Override
			public void paint(Graphics g, JComponent c) {
				Graphics2D g2 = (Graphics2D) g.create();
				// Set the alpha (transparency) value (0.0f: fully transparent, 1.0f: fully
				// opaque)
				float alpha = 0.5f;
				AlphaComposite alphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
				g2.setComposite(alphaComposite);

				// Paint the background image with transparency
				g2.drawImage(icon5.getImage(), 0, 0, option5.getWidth(), option5.getHeight(), null);

				// Paint the button's text (centered)
				alphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1);
				g2.setComposite(alphaComposite);
				super.paint(g2, c);

				g2.dispose();
			}
		});

		// Add an action listener to the "Apply" button
		applyButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				optionsDialog.setVisible(false);

			}
		});
		option1.addActionListener(new ButtonActionListener(this));
		option2.addActionListener(new ButtonActionListener(this));
		option3.addActionListener(new ButtonActionListener(this));
		option4.addActionListener(new ButtonActionListener(this));
		option5.addActionListener(new ButtonActionListener(this));

		// Create a panel to hold the buttons
		JPanel buttonPanel = new JPanel();

		buttonPanel.setLayout(new GridLayout(1, 4, 10, 10));

		buttonPanel.add(option1);
		buttonPanel.add(option2);
		buttonPanel.add(option3);
		buttonPanel.add(option4);
		buttonPanel.add(option5);

		// Add the button panel to the dialog's content pane
		optionsDialog.getContentPane().add(applyButton, BorderLayout.SOUTH);
		optionsDialog.getContentPane().add(buttonPanel, BorderLayout.CENTER);
		// Make the dialog visible
		optionsDialog.setVisible(true);

	}

	private String loadObjFile(File file) {
		StringBuilder content = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			String line;
			while ((line = reader.readLine()) != null) {
				content.append(line).append("\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return content.toString();
	}

	// Helper method to create ImageIcon from the resource path
	private ImageIcon createImageIcon(String path) {
		InputStream stream = getClass().getResourceAsStream(path);
		if (stream != null) {
			try {
				byte[] buffer = new byte[stream.available()];
				stream.read(buffer);
				return new ImageIcon(Toolkit.getDefaultToolkit().createImage(buffer));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	// Visual feedback for selecting buttons in the Options menu
	public void setSelectedButton(JButton button) {
		if (selectedButton != null) {
			selectedButton.setBackground(UIManager.getColor("Button.background"));
		}
		selectedButton = button;
		if (selectedButton != null) {
			selectedButton.setBackground(Color.YELLOW);
			if (selectedButton.getText().contains("1st Person")) {
				cameraDistance = 1;
				optionString = "1st Person";
				optionsButton.setText("Camera Option: " + optionString);

			}
			if (selectedButton.getText().contains("3rd Person")) {
				cameraDistance = 5;
				optionString = "3rd Person";
				optionsButton.setText("Camera Option: " + optionString);
			}
			if (selectedButton.getText().contains("ARPG")) {
				cameraDistance = 15;
				optionString = "ARPG";
				optionsButton.setText("Camera Option: " + optionString);
			}
			if (selectedButton.getText().contains("RTS")) {
				cameraDistance = 40;
				optionString = "RTS/TBS";
				optionsButton.setText("Camera Option: " + optionString);
			}
			if (selectedButton.getText().contains("Custom")) {
					cameraDistance = CameraDistanceInput.getCameraDistance(cameraDistance);
				
				optionString = "Custom Camera Distance";
					optionsButton.setText("Camera Option: " + optionString + " (" + cameraDistance + " meters)");
				}
			}
			// Check to enable the Optimize Texture button
			if (selectedImageFile != null && objFileContent != null && cameraDistance != 0) {
				optimize2DButton.setEnabled(true);
			}
		}



	public JButton getSelectedButton() {
		return selectedButton;
	}

	// Method called to scale the original 2D map
	public BufferedImage scaleImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
		BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics2D = resizedImage.createGraphics();
		graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		graphics2D.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
		graphics2D.dispose();
		return resizedImage;
	}

}
