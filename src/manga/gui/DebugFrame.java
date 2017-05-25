package manga.gui;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

import javax.swing.JFrame;

import manga.MaskedBufferedImage;

public class DebugFrame extends JFrame {

    private static final long serialVersionUID = 1L;
    private static int numero = 0;
    public static final String COLUMNS = "Columns";
    public static final String LINES = "Lines";
    private boolean rotate = false;
    private boolean debugMask = true;
    private DebugPanel linePanel;

    public void setDatas(double[] datas) {
        linePanel.setDatas(datas);
        setSize(datas.length + 10, 300);
    }

    public DebugFrame(String title) {
        if (LINES.equals(title)) {
            rotate = true;
        }
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        linePanel = new DebugPanel();
        getContentPane().add(linePanel);
        numero++;
        setTitle(numero + " " + title);
    }

    public void setSensivity(double moyenneVariations) {
        linePanel.setSensivity(moyenneVariations);
    }

    public void setMask(MaskedBufferedImage maskedBufferedImage) {
        BufferedImage mask = maskedBufferedImage.getMask();
        linePanel.setMask(mask);
        int imageWidth = mask.getWidth();
        int imageHeight = mask.getHeight();
        BufferedImage newMask = null;
        if (rotate) {
            newMask = new BufferedImage(imageHeight, imageWidth, BufferedImage.TYPE_INT_RGB);
        } else {
            newMask = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
        }
        WritableRaster writableRaster = newMask.getRaster();
        Raster raster = mask.getRaster();
        for (int y = 0; y < imageHeight; y++) {
            for (int x = 0; x < imageWidth; x++) {
                int[] iArray = {0, 0, 0};
                raster.getPixel(x, y, iArray);
                if (debugMask) {
                    for (int i = 0; i < 3; i++) {
                        iArray[i] *= 255;
                    }
                }
                if (rotate) {
                    writableRaster.setPixel(y, x, iArray);
                } else {
                    writableRaster.setPixel(x, y, iArray);
                }
            }
        }
        linePanel.setMask(newMask);
    }
}
