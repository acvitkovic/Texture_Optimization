package main.java.ui;

import javax.swing.JOptionPane;

public class CameraDistanceInput {
    public static double getCameraDistance(double cameraDistance) {
        while (true) {
            // Parse the user's input as a double
            
            // Show an input dialog with a text field for the user to enter the distance
            String input = JOptionPane.showInputDialog(null, "Enter the camera distance (in meters):");

            // Check if the user clicked OK or canceled
            if (input == null) {
                // User clicked cancel or closed the dialog, return a default value (e.g., 0)
                return cameraDistance;
            }

            try {
            	cameraDistance = Double.parseDouble(input);
                if (cameraDistance > 0) {
                	
                    return cameraDistance;
                } else {
                    // Show an error message if the user entered a non-positive number
                    JOptionPane.showMessageDialog(null, "Please enter a positive number.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException e) {
                // If the user entered an invalid number, show an error message
                JOptionPane.showMessageDialog(null, "Invalid input. Please enter a valid number.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
