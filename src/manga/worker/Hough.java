package manga.worker;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

import java.util.List;
import manga.model.*;

public class Hough implements FrameFinder {

    @Override
    public List<Frame> getFrames(Page page) {

        BufferedImage image = page.getImage();
        
        Sobel sobel = new Sobel();
        sobel.setInput(image);
        sobel.luminance();
        sobel.switchIOs();
        sobel.edges(Math.round(255 * .95f));
        //sobel.writeDebug("edges.bmp");

        File directory = new File(".");
        String [] bmpFilenames = directory.list((File dir, String name) -> name.endsWith(".bmp"));
        for(String bmpFilename : bmpFilenames) {
            File bmpFile = new File(bmpFilename);
            bmpFile.delete();
        }
        
        // create a hough transform object with the right dimensions
        Frame mainFrame = page.getCurrentFrame();
        return findFrame(sobel.getOutput(), mainFrame, Orientation.HORIZONTAL, 0);
    }

    private List<Frame> findFrame(BufferedImage sobel, Frame frame, Orientation orientation, int depth) {
        if(depth > 0) {
            List<Frame> frames = new ArrayList<>();
            frames.add(frame);
            return frames;
        }
        HoughTransform h = new HoughTransform(sobel, frame, orientation);
        List<Frame> frames = new ArrayList<>();
        int index = 0;
        for(Rectangle rectangle : h.getFrames(depth == 0 ? .5 : .95)) {
            frames.addAll(findFrame(sobel, new Frame(frame.getTitle() + "_" + index++, rectangle), orientation.toggle(), depth + 1));
        }
        return frames;
    }
}
