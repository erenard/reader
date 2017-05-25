package manga.model;

import manga.gui.ImageFileFilter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Album implements FrameIterator<Page> {

    private final String commonPath;
    private final List<Page> pages = new ArrayList<>();
    int index = 0;
    final int size;

    /**
     *
     * @param file
     * @throws java.io.IOException
     */
    public Album(File file) throws IOException {
        if (file == null) {
            throw new IOException("file must exist");
        }
        if (file.isDirectory()) {
            openDirectory(file);
            this.commonPath = file.getAbsolutePath();
        } else if (file.getName().endsWith(".zip")) {
            openArchive(new ZipFile(file));
            this.commonPath = "";
        } else {
            pages.add(new Page(file));
            this.commonPath = "";
        }
        Collections.sort(pages);
        size = pages.size();
    }

    private void openDirectory(File directory) throws IOException {
        ImageFileFilter fileFilter = new ImageFileFilter();
        File [] files = directory.listFiles(fileFilter);
        Arrays.sort(files);
        for (File file : files) {
            if (file.isDirectory()) {
                openDirectory(file);
            } else if (fileFilter.acceptArchive(file.getAbsolutePath())) {
                openArchive(new ZipFile(file));
            } else if (fileFilter.acceptImage(file.getName())) {
                pages.add(new Page(file));
            }
        }
    }

    private void openArchive(ZipFile zipFile) throws IOException {
        Map<String, ZipEntry> zipEntryByName = new HashMap<>();
        @SuppressWarnings("unchecked")
        Enumeration<ZipEntry> zipEntries = (Enumeration<ZipEntry>) zipFile.entries();
        ImageFileFilter imageFileFilter = new ImageFileFilter();
        while (zipEntries.hasMoreElements()) {
            ZipEntry zipEntry = (ZipEntry) zipEntries.nextElement();
            String zipEntryName = zipEntry.getName();
            if (imageFileFilter.acceptImage(zipEntryName)) {
                zipEntryByName.put(zipEntryName, zipEntry);
            }
        }
        Set<String> zipEntryNames = zipEntryByName.keySet();
        for (String zipEntryName : zipEntryNames) {
            ZipEntry zipEntry = zipEntryByName.get(zipEntryName);
            if(imageFileFilter.acceptImage(zipEntryName)) {
                pages.add(new Page(zipFile, zipEntry));
            }
        }
    }

    public List<Page> getPages() {
        return pages;
    }

    public String getCommonPath() {
        return commonPath;
    }

    @Override
    public Page jumpTo(int i) {
        index = i;
        return pages.get(index);
    }

    @Override
    public Page next() {
        if(index != size - 1) {
            index++;
        } else {
            index = 0;
        }
        return pages.get(index);
    }

    @Override
    public Page previous() {
        if(index != 0) {
            index--;
        } else {
            index = size - 1;
        }
        return pages.get(index);
    }
}
