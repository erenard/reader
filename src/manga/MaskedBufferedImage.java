package manga;


import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;


public class MaskedBufferedImage {

	private BufferedImage image;
	private BufferedImage mask;
	
	public BufferedImage getMask() {
		if(mask == null)
			mask = imageMask(image);
		return mask;
	}

	public BufferedImage getImage() {
		return image;
	}
	private void setMask(BufferedImage mask) {
		this.mask = mask;
	}

	public MaskedBufferedImage(BufferedImage image) {
		this.image = image;
	}

	private static BufferedImage imageMask(BufferedImage image) {
		int imageWidth = image.getWidth();
		int imageHeight = image.getHeight();
		boolean whiteBackground = true;
		//Le fond noir n'est pas pris en charge pour l'instant
		BufferedImage mask = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
		WritableRaster writableRaster = mask.getRaster();
		Raster raster = image.getRaster();
		for(int y = 0; y < imageHeight; y++) {
			for(int x = 0; x < imageWidth; x++) {
				int[] subPixels = new int[3];
				raster.getPixel(x, y, subPixels);
				boolean isNotBackground;
				if(whiteBackground)
					isNotBackground = subPixels[0] < 240 || subPixels[1] < 240 || subPixels[2] < 240;
				else
					isNotBackground = subPixels[0] > 15 || subPixels[1] > 15 || subPixels[2] > 15;
				int i = (isNotBackground ? 0 : 1);
				int [] iArray = {i, i, i};
				writableRaster.setPixel(x, y, iArray);
			}
		}
		return mask;
	}
	
	public MaskedBufferedImage getSubimage(int x, int y, int w, int h) {
		BufferedImage subImage = image.getSubimage(x, y, w, h);
		BufferedImage subMask  = mask.getSubimage(x, y, w, h);
		MaskedBufferedImage returned = new MaskedBufferedImage(subImage);
		returned.setMask(subMask);
		return returned;
	}
}
