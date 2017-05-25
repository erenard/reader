package manga.gui;
import java.io.File;

import javax.swing.filechooser.FileFilter;

public class ImageFileFilter extends FileFilter implements java.io.FileFilter {

	@Override
	public boolean accept(File pathname) {
		String path = pathname.getAbsolutePath().toLowerCase();
		return pathname.isDirectory() || acceptArchive(path) || acceptImage(path);
	}

	@Override
	public String getDescription() {
		return "Images (PNG, JPEG, JPG, GIF, BMP), Archives (Zip & GZip) or Directory";
	}
	
	public boolean acceptArchive(String path) {
		return path.endsWith(".zip")
                || path.endsWith(".gz")
                || path.endsWith(".gzip");
	}
        
        public boolean acceptImage(String path) {
		return path.endsWith(".bmp")
                || path.endsWith(".jpg")
                || path.endsWith(".jpeg")
                || path.endsWith(".gif")
                || path.endsWith(".png");
        }
}
