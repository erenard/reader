package manga.worker;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

public class Sobel {

    private BufferedImage input;
    private int width;
    private int height;
    private BufferedImage output;

    public void writeDebug(String filename) {
        try {
            ImageIO.write(output, "BMP", new File(filename));
        } catch (IOException ex) {
            Logger.getLogger(Sobel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void luminance() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = input.getRGB(x, y);
                int R = (rgb >> 16) & 0xff;
                int G = (rgb >> 8) & 0xff;
                int B = rgb & 0xff;
                int luminance = (int) (0.2125 * R + 0.7154 * G + 0.0721 * B);
                int pixel = 0xFF000000 | (luminance << 16) | (luminance << 8) | luminance;
                output.setRGB(x, y, pixel);
            }
        }
    }

    public void edges(int threshold) {
        int[][] convolution_x = {{1, 0, -1}, {2, 0, -2}, {1, 0, -1}};
        int[][] convolution_y = {{1, 2, 1}, {0, 0, 0}, {-1, -2, -1}};
        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int Gx = 0;
                int Gy = 0;
                for (int yy = -1; yy < 2; yy++) {
                    for (int xx = -1; xx < 2; xx++) {
                        int luminance = input.getRGB(x + xx, y + yy) & 0xff;
                        Gx += luminance * convolution_x[xx + 1][yy + 1];
                        Gy += luminance * convolution_y[xx + 1][yy + 1];
                    }
                }
                //Gx = Gx > threshold ? 255 : 0;
                //Gy = Gy > threshold ? 255 : 0;
                int G = (int) (Math.sqrt(Gx * Gx + Gy * Gy));
                G = G > threshold ? 255 : 0;
                int pixel = 0xFF000000 | (G << 16) | (G << 8) | G;
                output.setRGB(x, y, pixel);
            }
        }
    }

    /* *********************/
    /* Getters and Setters */
    /* *********************/
    public BufferedImage getInput() {
        return input;
    }

    public void setInput(BufferedImage input) {
        this.input = input;
        this.width = input.getWidth();
        this.height = input.getHeight();
        this.output = new BufferedImage(width, height, input.getType());
    }

    public void switchIOs() {
        if (output != null) {
            input = output;
            output = new BufferedImage(width, height, input.getType());
        }
    }

    public BufferedImage getOutput() {
        return output;
    }
}
