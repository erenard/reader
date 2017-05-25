package manga;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

public class ImageUtil {
    
    public static Image optimizeForDisplay(Image image, Dimension dimension) {
        BufferedImage bufferedImage = (BufferedImage) image;
        int panelWidth = dimension.width;
        int panelHeight = dimension.height;
        float panelRatio = (float) panelWidth / (float) panelHeight;

        int imageWidth = bufferedImage.getWidth();
        int imageHeight = bufferedImage.getHeight();
        float imageRatio = (float) imageWidth / (float) imageHeight;

        AffineTransform tx = new AffineTransform();
        double scale = 1;
        if (imageRatio > panelRatio) {
            // Pleine largeur
            scale = (double) panelWidth / (double) imageWidth;
        } else {
            // Pleine hauteur
            scale = (double) panelHeight / (double) imageHeight;
        }
        tx.scale(scale, scale);
        AffineTransformOp op = new AffineTransformOp(tx,
                AffineTransformOp.TYPE_BILINEAR);
        return op.filter(bufferedImage, null);
    }

}
