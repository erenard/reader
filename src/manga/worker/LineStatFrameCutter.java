/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package manga.worker;

import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import manga.model.*;
import manga.MaskedBufferedImage;
import manga.OptionManager;
import manga.gui.DebugFrame;

public class LineStatFrameCutter implements FrameFinder {

    private Page imageFile;

    @Override
    public List<Frame> getFrames(Page imageFile) {
        this.imageFile = imageFile;
        System.out.println("Loading : " + imageFile.getAbsolutePath());
        BufferedImage rawImage = imageFile.getImage();
        BufferedImage image = new BufferedImage(rawImage.getWidth(), rawImage.getHeight(), BufferedImage.TYPE_INT_RGB);
        ColorConvertOp xformOp = new ColorConvertOp(null);
        xformOp.filter(rawImage, image);
        long t = System.currentTimeMillis();
        // Contrast
        autoLevels(image);
        System.out.println("Levels in " + (System.currentTimeMillis() - t) + "ms.");
        List<MaskedBufferedImage> images = new ArrayList<>();
        images.add(new MaskedBufferedImage(image));
        t = System.currentTimeMillis();
        // D�coupage
        cutFrames(images, image.getWidth(), image.getHeight());
        if (images.isEmpty()) {
            images.add(new MaskedBufferedImage(image));
        }
        System.out.println("Cut in " + (System.currentTimeMillis() - t) + "ms.");
        return null;
    }

    private static final boolean DEBUG = Boolean.valueOf(OptionManager.getInstance().getProperty(OptionManager.DEBUG));
    private static final double PERFORMANCE_BOOST_RATIO = Integer.valueOf(OptionManager.getInstance().getProperty(OptionManager.SENSIVITY_DIVIDER));
    private static final int MAX_IMAGES_CUT = 15;

    private static void imageLineStats(MaskedBufferedImage image, double[] datas) {
        int imageWidth = image.getMask().getWidth();
        int imageHeight = image.getMask().getHeight();
        Raster raster = image.getMask().getRaster();
        double lastLine = 0;
        for (int y = 0; y < imageHeight; y++) {
            double line = 0;
            for (int x = 0; x < imageWidth; x++) {
                int[] iArray = new int[3];
                raster.getPixel(x, y, iArray);
                int i = iArray[0];
                line += i;
            }
            line /= imageWidth;
            line *= 100;
            datas[y] = Math.abs(lastLine - line);
            lastLine = line;
        }
    }

    private static void imageColumnStats(MaskedBufferedImage image, double[] datas) {
        int imageWidth = image.getMask().getWidth();
        int imageHeight = image.getMask().getHeight();
        Raster raster = image.getMask().getRaster();
        double lastColumn = 0;
        for (int x = 0; x < imageWidth; x++) {
            double column = 0;
            for (int y = 0; y < imageHeight; y++) {
                int[] iArray = new int[3];
                raster.getPixel(x, y, iArray);
                int i = iArray[0];
                column += i;
            }
            column /= imageWidth;
            column *= 100;
            datas[x] = Math.abs(lastColumn - column);
            lastColumn = column;
        }
    }

    private static void cutFrames(List<MaskedBufferedImage> images, int imageWidth, int imageHeight) {
        boolean loop = true;
        boolean vertical = true;
        while (loop) {
            List<MaskedBufferedImage> newImageList = new ArrayList<>();
            for (MaskedBufferedImage image : images) {
                List<MaskedBufferedImage> localList;
                if (vertical) {
                    localList = frameCutTopDown(image);
                } else {
                    localList = frameCutRightLeft(image);
                }
                if (localList.size() > MAX_IMAGES_CUT) {
                    loop = false;
                }
                newImageList.addAll(localList);
            }
            if (images.size() == newImageList.size()) {
                loop = false;
            }
            images.clear();
            images.addAll(newImageList);
            vertical = !vertical;
        }
        int minWidth = imageWidth / 10;
        int minHeight = imageHeight / 10;
        for (Iterator<MaskedBufferedImage> iterator = images.iterator(); iterator.hasNext();) {
            MaskedBufferedImage maskedBufferedImage = (MaskedBufferedImage) iterator.next();
            BufferedImage bufferedImage = maskedBufferedImage.getImage();
            if (bufferedImage.getWidth() < minWidth || bufferedImage.getHeight() < minHeight) {
                iterator.remove();
            }
        }
    }

    private static List<MaskedBufferedImage> frameCutTopDown(MaskedBufferedImage maskedBufferedImage) {
        int imageHeight = maskedBufferedImage.getImage().getHeight();
        int imageWidth = maskedBufferedImage.getImage().getWidth();
        double variations[] = new double[imageHeight];
        imageLineStats(maskedBufferedImage, variations);
        BufferedImage mask = maskedBufferedImage.getMask();
        double moyenneVariations = 0;
        for (double variation : variations) {
            moyenneVariations += variation;
        }
        moyenneVariations /= variations.length;
        moyenneVariations *= PERFORMANCE_BOOST_RATIO;
        if (DEBUG) {
            DebugFrame debugFrame = new DebugFrame(DebugFrame.LINES);
            debugFrame.setDatas(variations);
            debugFrame.setSensivity(moyenneVariations);
            debugFrame.setMask(maskedBufferedImage);
            debugFrame.setVisible(true);
        }

        List<MaskedBufferedImage> images = new ArrayList<>();
        SortedSet<Integer> extremums = new TreeSet<>();
        extremums.add(0);
        extremums.add(imageHeight - 1);
        for (int y = 0; y < imageHeight; y++) {
            // Detection des extremums
            if (variations[y] > moyenneVariations) {
                extremums.add(y);
            }
        }
        Raster raster = mask.getData();
        int starting = 0;
        SortedMap<Integer, Boolean> ordres = new TreeMap<>();
        for (Integer integer : extremums) {
            if (starting != integer) {
                boolean isAFrame = false;
//				int frameBorderX = 0;
                column:
                for (int x = 0; x < imageWidth; x++) {
                    for (int y = starting; y < integer - 1; y++) {
                        int[] iArray = new int[3];
                        raster.getPixel(x, y, iArray);
                        if (iArray[0] != 0) {
                            continue column;
                        }
                    }
//					frameBorderX = x;
                    isAFrame = true;
                    break column;
                }

//				if(isAFrame) {
//					column : for(int x = imageWidth - 1; x > frameBorderX; x--) {
//						for(int y = starting; y < integer - 1; y++) {
//							int [] iArray = new int[3];
//							raster.getPixel(x, y, iArray);
//							if(iArray[0] != 0) {
//								continue column;
//							}
//						}
//						isAFrame = true;
//						break column;
//					}
//				}
                ordres.put(starting, isAFrame);
                starting = integer;
            }
        }
        Set<Integer> keys = ordres.keySet();
        starting = 0;
        boolean cutting = false;
        for (Integer integer : keys) {
            if (!cutting && ordres.get(integer)) {
                cutting = true;
                starting = integer;
            } else if (cutting) {
                if (!ordres.get(integer)) {
                    MaskedBufferedImage tile = maskedBufferedImage.getSubimage(0, starting, maskedBufferedImage.getImage().getWidth(), integer - starting);
//					System.out.println("Image : " + starting + " jusqu'a " + integer + ", hauteur:" + (integer - starting));
                    images.add(tile);
                    cutting = false;
                }
            }
        }
        if (cutting) {
            MaskedBufferedImage tile = maskedBufferedImage.getSubimage(0, starting, maskedBufferedImage.getImage().getWidth(), imageHeight - 1 - starting);
//			System.out.println("Image : " + starting + " jusqu'a " + (imageHeight - 1) + ", hauteur:" + (imageHeight - 1 - starting));
            images.add(tile);
            cutting = false;
        }
        return images;
    }

    private static List<MaskedBufferedImage> frameCutRightLeft(MaskedBufferedImage maskedBufferedImage) {
        int imageHeight = maskedBufferedImage.getImage().getHeight();
        int imageWidth = maskedBufferedImage.getImage().getWidth();
        double variations[] = new double[imageWidth];
        imageColumnStats(maskedBufferedImage, variations);
        BufferedImage mask = maskedBufferedImage.getMask();
        double moyenneVariations = 0;
        for (double variation : variations) {
            moyenneVariations += variation;
        }
        moyenneVariations /= variations.length;
        moyenneVariations *= PERFORMANCE_BOOST_RATIO;
        if (DEBUG) {
            DebugFrame debugFrame = new DebugFrame(DebugFrame.COLUMNS);
            debugFrame.setDatas(variations);
            debugFrame.setSensivity(moyenneVariations);
            debugFrame.setMask(maskedBufferedImage);
            debugFrame.setVisible(true);
        }

        List<MaskedBufferedImage> images = new ArrayList<>();
        SortedSet<Integer> extremums = new TreeSet<>();
        extremums.add(0);
        extremums.add(imageWidth - 1);
        for (int x = 0; x < imageWidth; x++) {
            // Detection des extremums
            if (variations[x] > moyenneVariations) {
                extremums.add(x);
            }
        }
        Raster raster = mask.getData();
        int starting = 0;
        SortedMap<Integer, Boolean> ordres = new TreeMap<>();
        for (Integer extremum : extremums) {
            if (starting != extremum) {
                boolean isAFrame = false;
//				int frameBorderY = 0;
                //Boucle sur les colones
                line:
                for (int y = 0; y < imageHeight; y++) {
                    //Boucle sur les pixels de la colone
                    for (int x = starting; x < extremum - 1; x++) {
                        int[] iArray = new int[3];
                        raster.getPixel(x, y, iArray);
                        if (iArray[0] != 0) {
                            //Si un pixel n'est pas blanc on change de colone
                            continue line;
                        }
                    }
                    //Tout les pixels de la colonne �taient blanc
//					frameBorderY = y;
                    isAFrame = true;
                    break line;
                }

//				if(isAFrame) {
//					line : for(int y = imageHeight - 1; y > frameBorderY; y--) {
//						for(int x = starting; x < extremum - 1; x++) {
//							int [] iArray = new int[3];
//							raster.getPixel(x, y, iArray);
//							if(iArray[0] != 0) {
//								continue line;
//							}
//						}
//						isAFrame = true;
//						break line;
//					}
//				}
                ordres.put(starting, isAFrame);
                starting = extremum;
            }
        }
        Set<Integer> keys = ordres.keySet();
        starting = 0;
        boolean cutting = false;
        for (Integer integer : keys) {
            if (!cutting && ordres.get(integer)) {
                cutting = true;
                starting = integer;
            } else if (cutting) {
                if (!ordres.get(integer)) {
                    MaskedBufferedImage tile = maskedBufferedImage.getSubimage(starting, 0, integer - starting, imageHeight);
//					System.out.println("Image : " + starting + " jusqu'a " + integer + ", largeur:" + (integer - starting));
                    images.add(tile);
                    cutting = false;
                }
            }
        }
        if (cutting) {
            MaskedBufferedImage tile = maskedBufferedImage.getSubimage(starting, 0, imageWidth - 1 - starting, imageHeight);
//			System.out.println("Image : " + starting + " jusqu'a " + (imageWidth - 1) + ", largeur:" + (imageWidth - 1 - starting));
            images.add(tile);
            cutting = false;
        }
        //Inversion
        Collections.reverse(images);
        return images;
    }

    private static void autoLevels(BufferedImage bufferedImage) {
        int imageWidth = bufferedImage.getWidth();
        int imageHeight = bufferedImage.getHeight();
        WritableRaster raster = bufferedImage.getRaster();
        double[] a = new double[3];
        double[] b = new double[3];
        {
            int[] minBound = {255, 255, 255};
            int[] maxBound = {0, 0, 0};
            {
                int[][] histo = new int[3][256];
                for (int x = 0; x < imageWidth; x++) {
                    for (int y = 0; y < imageHeight; y++) {
                        int[] iArray = new int[3];
                        raster.getPixel(x, y, iArray);
                        for (int i = 0; i < 3; i++) {
                            histo[i][iArray[i]]++;
                        }
                    }
                }
                for (int h = 0; h < 256; h++) {
                    for (int i = 0; i < 3; i++) {
                        if (histo[i][h] != 0) {
                            minBound[i] = Math.min(minBound[i], h);
                            maxBound[i] = Math.max(maxBound[i], h);
                        }
                    }
                }
            }
            for (int i = 0; i < 3; i++) {
                if (minBound[i] == maxBound[i]) {
                    return;
                }
                a[i] = 255.0 / (double) (maxBound[i] - minBound[i]);
                b[i] = -1 * a[i] * minBound[i];
                if (a[i] != 1 && b[i] != 0) {
                    System.out.println(" " + i + " a=" + a[i] + " b=" + b[i]);
                }
            }
        }
        if (a[0] < 0 || a[1] < 0 || a[2] < 0) {
            System.out.println("a < 0");
            return;
        }
        if (a[0] != 1 || a[1] != 1 || a[2] != 1 || b[0] != 0 || b[1] != 0 || b[1] != 0) {
            for (int x = 0; x < imageWidth; x++) {
                for (int y = 0; y < imageHeight; y++) {
                    int[] iArray = new int[3];
                    raster.getPixel(x, y, iArray);
                    for (int i = 0; i < 3; i++) {
                        iArray[i] = (int) (b[i] + a[i] * iArray[i]);
                    }
                    raster.setPixel(x, y, iArray);
                }
            }
            bufferedImage.setData(raster);
        }
    }
}
