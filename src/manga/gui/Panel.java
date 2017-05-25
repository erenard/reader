package manga.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.util.Collection;

import javax.swing.JPanel;
import manga.model.*;

public class Panel extends JPanel {

    private static final long serialVersionUID = 1L;
    private Page page;
    
    public void setPage(Page page) {
        this.page = page;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (page != null) {
            //Image image = ImageUtil.optimizeForDisplay(page.getImage(), getSize());
            Image image = page.getImage();
            //int xDiff = getWidth() - image.getWidth(null);
            //int yDiff = getHeight() - image.getHeight(null);
            //g.drawImage(image, xDiff / 2, yDiff / 2, null);
            g.drawImage(image, 0, 0, null);
            Collection<Frame> frames = page.getFrames();
            if(frames != null) {
                g.setColor(Color.BLUE);
                for(Frame frame : frames) {
                    g.drawRect(frame.x, frame.y, frame.width, frame.height);
                    g.drawString(frame.getTitle(), frame.x + frame.width / 2, frame.y + frame.height / 2);
                }
            }
            /*
            Frame frame = page.getCurrentFrame();
            if(frame != null) {
		g.setColor(Color.RED);
		g.drawRect(frame.x, frame.y, frame.width, frame.height);
            }
            */
            if(page.debug != null) {
                g.drawImage(page.debug, 0, 0, null);
            }
        }
    }
}
