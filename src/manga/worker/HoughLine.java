package manga.worker;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

public class HoughLine {

    protected double theta;
    protected double r;

    /**
     * Initialises the hough line
     * @param theta orientation
     * @param r distance between the center of the region, and the line
     */
    public HoughLine(double theta, double r) {
        this.theta = theta;
        this.r = r;
    }

    /**
     * Draws the line on the image of your choice with the RGB colour of your
     * choice.
     */
    public void draw(BufferedImage image, Rectangle region, int color) {

        //int height = region.height;
        //int width = region.width;

        // During processing h_h is doubled so that -ve r values 
        int houghHeight = (int) (Math.sqrt(2) * Math.max(region.height, region.width)) / 2;

        // Find edge points and vote in array 
        float centerX = region.x + region.width / 2;
        float centerY = region.y + region.height / 2;

        // Draw edges in output array 
        double tsin = Math.sin(theta);
        double tcos = Math.cos(theta);

        if (theta < Math.PI * 0.25 || theta > Math.PI * 0.75) {
            // Draw vertical-ish lines 
            for (int y = region.y; y < (region.y + region.height); y++) {
                int x = (int) ((((r - houghHeight) - ((y - centerY) * tsin)) / tcos) + centerX);
                if (x < (region.x + region.width) && x >= region.x) {
                    image.setRGB(x, y, color);
                }
            }
        } else {
            // Draw horizontal-sh lines 
            for (int x = region.y; x < (region.x + region.width); x++) {
                int y = (int) ((((r - houghHeight) - ((x - centerX) * tcos)) / tsin) + centerY);
                if (y < (region.y + region.height) && y >= region.y) {
                    image.setRGB(x, y, color);
                }
            }
        }
    }

    public int getCoord(Rectangle region) {

        // During processing h_h is doubled so that -ve r values 
        int houghHeight = (int) (Math.sqrt(2) * Math.max(region.height, region.width)) / 2;

        // Find edge points and vote in array 
        float centerX = region.x + region.width / 2;
        float centerY = region.y + region.height / 2;

        // Draw edges in output array 
        double tsin = Math.sin(theta);
        double tcos = Math.cos(theta);

        if (theta < Math.PI * 0.25 || theta > Math.PI * 0.75) {
            // Draw vertical-ish lines 
            return (int) ((((r - houghHeight) - (centerY * tsin)) / tcos) + centerX);
        } else {
            // Draw horizontal-sh lines 
            return (int) ((((r - houghHeight) - (centerX * tcos)) / tsin) + centerY);
        }
    }

    public static int getCoord(double r, double theta, Rectangle region) {

        // During processing h_h is doubled so that -ve r values 
        int houghHeight = (int) (Math.sqrt(2) * Math.max(region.height, region.width)) / 2;

        // Find edge points and vote in array 
        float centerX = region.x + region.width / 2;
        float centerY = region.y + region.height / 2;

        // Draw edges in output array 
        double tsin = Math.sin(theta);
        double tcos = Math.cos(theta);

        if (theta < Math.PI * 0.25 || theta > Math.PI * 0.75) {
            // Draw vertical-ish lines 
            return (int) ((((r - houghHeight) - (centerY * tsin)) / tcos) + centerX);
        } else {
            // Draw horizontal-sh lines 
            return (int) ((((r - houghHeight) - (centerX * tcos)) / tsin) + centerY);
        }
    }
}
