package main.java.math;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ObjSurfaceAreaCalculator {


	public static double calculateSurfaceArea(File objFile) {
	    List<double[]> vertices = new ArrayList<>();
	    List<int[]> faces = new ArrayList<>();

	    try (BufferedReader reader = new BufferedReader(new FileReader(objFile))) {
	        String line;
	        while ((line = reader.readLine()) != null) {
	            if (line.startsWith("v ")) {
	                String[] parts = line.split("\\s+");
	                double x = Double.parseDouble(parts[1]);
	                double y = Double.parseDouble(parts[2]);
	                double z = Double.parseDouble(parts[3]);
	                vertices.add(new double[]{x, y, z});
	            } else if (line.startsWith("f ")) {
	                String[] parts = line.split("\\s+");
	                int[] faceVertices = new int[parts.length - 1];
	                for (int i = 0; i < faceVertices.length; i++) {
	                    String vertexData = parts[i + 1].split("/")[0];
	                    faceVertices[i] = Integer.parseInt(vertexData) - 1;
	                }
	                faces.add(faceVertices);
	            }
	        }
	    } catch (IOException e) {
	        e.printStackTrace();
	        return 0.0; // Return 0.0 in case of an error or empty file
	    }

	    // Triangulate the mesh and calculate surface area
        double totalSurfaceArea = 0.0;
        for (int[] face : faces) {
            if (face.length >= 3) {
                // Triangulate the polygon with more than 3 vertices
                List<int[]> triangles = triangulatePolygon(vertices, face);
                for (int[] triangle : triangles) {
                    double[] v1 = vertices.get(triangle[0]);
                    double[] v2 = vertices.get(triangle[1]);
                    double[] v3 = vertices.get(triangle[2]);
                    totalSurfaceArea += calculateTriangleArea(v1, v2, v3);
                }
            }
        }
        
        return totalSurfaceArea/4;
    }

    public static List<int[]> triangulatePolygon(List<double[]> vertices, int[] polygon) {
        List<int[]> triangles = new ArrayList<>();
        int n = polygon.length;

        if (n == 3) {
            // The polygon is already a triangle, no need to triangulate
            triangles.add(polygon);
            return triangles;
        }

        // Simple triangulation without ear checking
        for (int i = 1; i < n - 1; i++) {
            triangles.add(new int[]{polygon[0], polygon[i], polygon[i + 1]});
        }

        return triangles;
    }

    public static double calculateTriangleArea(double[] v1, double[] v2, double[] v3) {
        // Calculate the vectors representing two sides of the triangle
        double[] side1 = new double[]{v2[0] - v1[0], v2[1] - v1[1], v2[2] - v1[2]};
        double[] side2 = new double[]{v3[0] - v1[0], v3[1] - v1[1], v3[2] - v1[2]};

        // Calculate the cross product of the two sides (vector normal to the triangle)
        double[] crossProduct = new double[]{
                side1[1] * side2[2] - side1[2] * side2[1],
                side1[2] * side2[0] - side1[0] * side2[2],
                side1[0] * side2[1] - side1[1] * side2[0]
        };

        // Calculate the magnitude of the cross product vector (twice the triangle area)
        double area = 0.5 * Math.sqrt(crossProduct[0] * crossProduct[0]
                + crossProduct[1] * crossProduct[1]
                + crossProduct[2] * crossProduct[2]);

        return area;
        
     
    }

}
