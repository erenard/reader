package manga.worker;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import manga.model.Frame;

public class HoughTransform {

    // The size of the neighbourhood in which to search for other local maxima 
    final int neighbourhoodSize = 2;

    // How many discrete values of theta shall we check? 
    private final double thetaMin;
    private final double thetaMax;
    private final double thetaStep;
    private final int thetaCount = 25;

    // the input image
    private final BufferedImage sobel;

    // the width and height of the image 
    private final Frame region;
    
    private final Orientation orientation;

    // the hough array 
    protected int[][] houghArray;

    // the coordinates of the centre of the image 
    protected float centerX, centerY;

    // the height of the hough array 
    protected int houghHeight;

    // double the hough height (allows for negative numbers) 
    protected int doubleHeight;

    // the number of points that have been added 
    protected int numPoints;

    // cache of values of sin and cos for different theta values. Has a significant performance improvement. 
    private final double[] thetas = new double[thetaCount];
    private final double[] sinCache = new double[thetaCount];
    private final double[] cosCache = new double[thetaCount];

    /**
     * Initialises the hough transform. The dimensions of the input image are
     * needed in order to initialise the hough array.
     * @param image sobel output
     * @param region region to process
     * @param orientation process orientation
     */
    public HoughTransform(BufferedImage image, Frame region, Orientation orientation) {
        this.sobel = image;
        this.region = region;
        this.orientation = orientation;
        this.thetaMin = orientation.angleMin;
        this.thetaMax = orientation.angleMax;
        this.thetaStep = (thetaMax - thetaMin) / (thetaCount - 1);
        
        // Calculate the maximum height the hough array needs to have 
        houghHeight = (int) (Math.sqrt(2) * Math.max(region.height, region.width)) / 2;

        // Double the height of the hough array to cope with negative r values 
        doubleHeight = 2 * houghHeight;

        // Create the hough array 
        houghArray = new int[thetaCount][doubleHeight];

        // Find edge points and vote in array 
        centerX = region.width / 2;
        centerY = region.height / 2;

        // Count how many points there are 
        numPoints = 0;

        // cache the values of sin and cos for faster processing 
        for (int t = 0; t < thetaCount; t++) {
            double thetaDegree = thetaMin + t * thetaStep;
            double realTheta = Math.PI * thetaDegree / 180;
            thetas[t] = realTheta;
            sinCache[t] = Math.sin(realTheta);
            cosCache[t] = Math.cos(realTheta);
        }
        addPoints();
    }

    private boolean isPoint(int x, int y) {
        return (sobel.getRGB(x, y) & 0xff) != 0;
    }
    
    /**
     * Adds points from an image. The image is assumed to be greyscale black and
     * white, so all pixels that are not black are counted as edges. The image
     * should have the same dimensions as the one passed to the constructor.
     */
    private void addPoints() {
        // Now find edge points and update the hough array 
        for (int xx = region.x; xx < region.x + region.width; xx++) {
            for (int yy = region.y; yy < region.y + region.height; yy++) {
                // Find non-black pixels
                if (isPoint(xx, yy)) {
                    addPoint(xx - region.x, yy - region.y);
                }
            }
        }
    }

    /**
     * Adds a single point to the hough transform. You can use this method
     * directly if your data isn't represented as a buffered image.
     */
    private void addPoint(int x, int y) {
        // Go through each value of theta 
        for (int t = 0; t < thetaCount; t++) {
            //for (int t : thetas) {

            //Work out the r values for each theta step 
            int r = (int) (((x - centerX) * cosCache[t]) + ((y - centerY) * sinCache[t]));

            // this copes with negative values of r 
            r += houghHeight;

            if (r < 0 || r >= doubleHeight) {
                continue;
            }

            // Increment the hough array 
            houghArray[t][r]++;

        }

        numPoints++;
    }

    /**
     * Once points have been added in some way this method extracts the lines
     * and returns them as a Vector of HoughLine objects, which can be used to
     * draw on the
     *
     * @param threshold
     * @return 
     */
    public SortedSet<Integer> getLines(double threshold) {

        // Initialise the vector of lines that we'll return 
        List<HoughLine> lines = new ArrayList<>(32);

        // Only proceed if the hough array is not empty 
        if (numPoints == 0) {
            return new TreeSet<>();
        }

        // Search for local peaks above threshold to draw 
        for (int t = 0; t < thetaCount; t++) {
            loop:
            for (int r = neighbourhoodSize; r < doubleHeight - neighbourhoodSize; r++) {

                // Only consider points above threshold 
                if (houghArray[t][r] > threshold) {

                    int peak = houghArray[t][r];

                    // Check that this peak is indeed the local maxima 
                    for (int dx = -neighbourhoodSize; dx <= neighbourhoodSize; dx++) {
                        for (int dy = -neighbourhoodSize; dy <= neighbourhoodSize; dy++) {
                            int dt = t + dx;
                            int dr = r + dy;
                            if (dt < 0) {
                                dt = dt + thetaCount;
                            } else if (dt >= thetaCount) {
                                dt = dt - thetaCount;
                            }
                            if (houghArray[dt][dr] > peak) {
                                // found a bigger point nearby, skip 
                                continue loop;
                            }
                        }
                    }

                    // calculate the true value of theta 
                    double theta = thetas[t];

                    // add the line to the vector 
                    lines.add(new HoughLine(theta, r));

                }
            }
        }
        writeDebug(lines);
        SortedSet<Integer> lineCoordinates = new TreeSet<>();
        lines.stream().forEach(line -> lineCoordinates.add(line.getCoord(region)));
        return lineCoordinates;
    }

    /**
     * Once points have been added in some way this method extracts the lines
     * and returns them as a Vector of HoughLine objects, which can be used to
     * draw on the
     *
     * @param threshold
     * @return 
     */
    public SortedSet<Integer> getLineCoordinates(double threshold) {
        // Only proceed if the hough array is not empty 
        if (numPoints == 0) {
            return new TreeSet<>();
        }
        SortedSet<Integer> lineCoordinates = new TreeSet<>();
        SortedMap<Integer, HoughLine> houghLineByCoordinate = new TreeMap<>();
        rLoop: for(int r = 0; r < doubleHeight; r++) {
            for(int t = 0; t < thetaCount; t++) {
                // Only consider points above threshold 
                if (houghArray[t][r] > threshold) {
                    HoughLine houghLine = new HoughLine(orientation.theta, r);
                    Integer coordinate = houghLine.getCoord(region);
                    lineCoordinates.add(coordinate);
                    houghLineByCoordinate.put(coordinate, houghLine);
                    System.out.println(coordinate);
                    continue rLoop;
                }
            }
        }
        //
        SortedSet<Integer> result = new TreeSet<>();
        // Group of lines detection
        Integer firstGroupLine = null;
        for(Integer line : lineCoordinates) {
            if(firstGroupLine == null && lineCoordinates.contains(line + 1)) {
                firstGroupLine = line;
            } else if(firstGroupLine != null && !lineCoordinates.contains(line + 1)) {
                result.add(firstGroupLine + (line - firstGroupLine) / 2);
                firstGroupLine = null;
            } else if(firstGroupLine == null && !lineCoordinates.contains(line + 1)) {
                result.add(line);
            }
        }
        //DEBUG
        writeDebug(houghLineByCoordinate.values());
        //
        return result;
    }

    /**
     * Gets the highest value in the hough array
     * @return highest value in the hough array
     */
    public int getHighestValue() {
        int max = 0;
        for (int t = 0; t < thetaCount; t++) {
            for (int r = 0; r < doubleHeight; r++) {
                if (houghArray[t][r] > max) {
                    max = houghArray[t][r];
                }
            }
        }
        return max;
    }

    /**
     * Gets the hough array as an image, in case you want to have a look at it.
     * @return image of the hough array
     */
    public BufferedImage getHoughArrayImage() {
        int max = getHighestValue();
        BufferedImage image = new BufferedImage(thetaCount, doubleHeight, BufferedImage.TYPE_INT_ARGB);
        for (int t = 0; t < thetaCount; t++) {
            for (int r = 0; r < doubleHeight; r++) {
                double value = 255 * ((double) houghArray[t][r]) / max;
                int v = 255 - (int) value;
                int c = new Color(v, v, v).getRGB();
                image.setRGB(t, r, c);
            }
        }
        return image;
    }

    public List<Rectangle> getFrames(double ratio) {
        SortedSet<Integer> coords;
        {
            double threshold = 0;
            switch (orientation) {
                case HORIZONTAL:
                    threshold = region.width * ratio;
                    coords = getLineCoordinates(threshold);
                    coords.add(region.y);
                    coords.add(region.y + region.height - 1);
                    break;
                case VERTICAL:
                    threshold = region.height * ratio;
                    coords = getLineCoordinates(threshold);
                    coords.add(region.x);
                    coords.add(region.x + region.width - 1);
                    break;
                default:
                    throw new IllegalStateException();
            }
        }
        Iterator<Integer> iterator = coords.iterator();
        List<Rectangle> rectangles = new ArrayList<>();
        int lastCoord = iterator.next();
        while(iterator.hasNext()) {
            int coord = iterator.next();
            Rectangle result = null;
            if(orientation.equals(Orientation.HORIZONTAL)) {
                int height = coord - lastCoord;
                if(height < 3) {
                    continue;
                }
                Frame localRegion = new Frame(region.getTitle(), region.x, lastCoord, region.width, height);
                HoughTransform houghTransform = new HoughTransform(sobel, localRegion, Orientation.VERTICAL);
                SortedSet<Integer> plops = houghTransform.getLineCoordinates(height * .95);
                if(plops.size() > 1 && !plops.first().equals(plops.last())) {
                    result = new Rectangle(plops.first(), localRegion.y, plops.last() - plops.first(), localRegion.height);
                }
            } else {
                int width = coord - lastCoord;
                if(width < 3) {
                    continue;
                }
                Frame localRegion = new Frame(region.getTitle(), lastCoord, region.y, width, region.height);
                HoughTransform houghTransform = new HoughTransform(sobel, localRegion, Orientation.HORIZONTAL);
                SortedSet<Integer> plops = houghTransform.getLineCoordinates(width * .95);
                if(plops.size() > 1) {
                    result = new Rectangle(localRegion.x, plops.first(), localRegion.width, plops.last() - plops.first());
                }
            }
            if(result != null) {
                rectangles.add(result);
            }
            lastCoord = coord;
        }
        return groupRectangles(rectangles);
    }

    private List<Rectangle> groupRectangles(List<Rectangle> rectangles) {
        Iterator<Rectangle> it = rectangles.iterator();
        if(it.hasNext()) {
            Rectangle lastRectangle = it.next();
            while(it.hasNext()) {
                Rectangle rectangle = it.next();
                switch(orientation) {
                    case HORIZONTAL:
                        if(lastRectangle.y + lastRectangle.height == rectangle.y) {
                            lastRectangle.height += rectangle.height;
                            it.remove();
                        } else {
                            lastRectangle = rectangle;
                        }
                        break;
                    case VERTICAL:
                        if(lastRectangle.x + lastRectangle.width == rectangle.x) {
                            lastRectangle.width += rectangle.width;
                            it.remove();
                        } else {
                            lastRectangle = rectangle;
                        }
                        break;
                }
            }
        }
        return rectangles;
    }
    
    private void writeDebug(Collection<HoughLine> lines) {
        if(lines == null || lines.isEmpty())
            return;
        try {
            BufferedImage image = new BufferedImage(sobel.getWidth(), sobel.getHeight(), sobel.getType());
            sobel.copyData(image.getRaster());
            lines.stream().forEach((line) -> {line.draw(image, region, Color.LIGHT_GRAY.getRGB());});
            ImageIO.write(image, "BMP", new File(toString()));
        } catch (IOException ex) {
            Logger.getLogger(Sobel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(region.getTitle());
        sb.append("_");
        sb.append(orientation);
        sb.append("_");
        switch(orientation) {
            case VERTICAL:
                sb.append(region.y);
                sb.append("-");
                sb.append(region.y + region.height);
                break;
            case HORIZONTAL:
                sb.append(region.x);
                sb.append("-");
                sb.append(region.x + region.width);
                break;
        }
        sb.append(".bmp");
        return sb.toString();
    }
}
