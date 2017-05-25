package manga.model;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.imageio.ImageIO;
import manga.worker.FrameFinder;
import manga.worker.Hough;

/**
 * Abstract the source of the image, beeing a ZipFile + ZipEntry or a File
 * Contains the frames
 */
public class Page implements Comparable<Page>, FrameIterator<Frame> {

    private ZipFile zipFile;
    private ZipEntry zipEntry;
    private File file;
    private final String name;
    private final String absolutePath;
    private final String path;
    private final BufferedImage image;
    public BufferedImage debug;
    private final List<Frame> frames = new ArrayList<>();
    int index = 0;
    final int size = 1;

    public String getAbsolutePath() {
        return absolutePath;
    }

    public String getName() {
        return name;
    }

    /**
     * File or directory constructor
     * @param file 
     * @throws java.io.IOException 
     */
    public Page(File file) throws IOException {
        this.file = file;
        this.name = file.getName();
        this.absolutePath = file.getAbsolutePath();
        this.path = this.absolutePath.substring(0, this.absolutePath.lastIndexOf(File.separator) + 1);
        this.image = ImageIO.read(new FileInputStream(file));
        findFrames();
    }

    /**
     * Zip File contructor
     * @param zipFile
     * @param zipEntry 
     * @throws java.io.IOException 
     */
    public Page(ZipFile zipFile, ZipEntry zipEntry) throws IOException {
        this.zipFile = zipFile;
        this.zipEntry = zipEntry;
        this.absolutePath = zipEntry.getName();
        String zipEntryName = zipEntry.getName();
        if (zipEntryName.contains(File.separator)) {
            this.path = zipEntryName.substring(0, zipEntryName.lastIndexOf("/") + 1);
            this.name = zipEntryName.substring(zipEntryName.lastIndexOf("/") + 1);
        } else {
            this.path = "";
            this.name = zipEntryName;
        }
        this.image = ImageIO.read(zipFile.getInputStream(zipEntry));
        findFrames();
    }

    private void findFrames() {
        this.frames.add(new Frame(this.name, 0, 0, this.image.getWidth(), this.image.getHeight()));
        FrameFinder frameFinder = new Hough();
        this.frames.addAll(frameFinder.getFrames(this));
    }
    
    public final InputStream getInputStream() throws IOException {
        if (file != null) {
            return new FileInputStream(file);
        } else {
            return zipFile.getInputStream(zipEntry);
        }
    }
    
    public BufferedImage getImage() {
        return image;
    }

    /**
     * Sort by absolute path
     * @param o
     * @return
     */
    @Override
    public int compareTo(Page o) {
        int comparison = path.compareTo(o.path);
        return comparison != 0 ? comparison : name.compareTo(o.name);
    }

    @Override
    public Frame next() {
        index = index == size - 1 ? 0 : index++;
        return frames.get(index);
    }

    @Override
    public Frame previous() {
        index = index == 0 ? size - 1 : index--;
        return frames.get(index);
    }

    @Override
    public Frame jumpTo(int index) {
        this.index = index;
        if(this.frames.size() == 1) {
            findFrames();
        }
        return this.frames.get(index);
    }

    public Collection<Frame> getFrames() {
        return frames;
    }

    public Frame getCurrentFrame() {
        return frames.get(index);
    }
}
