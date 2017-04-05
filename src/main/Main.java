package main;

import aimage.CalculMath;
import aimage.OCRImage;
import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageConverter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;

public class Main {

    private static ImagePlus img;
    ArrayList<OCRImage> listImg = new ArrayList<>();

    public static void main(String[] args) {
        logOCR("confusion-matrix.txt");
        ArrayList<Double> tab0 = new ArrayList<>();
        tab0.add(1.0);
        tab0.add(1.0);
        ArrayList<Double> tab1 = new ArrayList<>();
        tab1.add(5.0);
        tab1.add(-1.0);
        ArrayList<Double> tab2 = new ArrayList<>();
        tab2.add(2.0);
        tab2.add(1.0);
        ArrayList<Double> tab3 = new ArrayList<>();
        tab3.add(-1.0);
        tab3.add(0.0);
        ArrayList<ArrayList<Double>> myList = new ArrayList<>();
        myList.add(tab1);
        myList.add(tab2);
        myList.add(tab3);
        IJ.showMessage(" dist = " + CalculMath.PPV(tab0, myList, 0));
    }

    private static void logOCR(String pathOut) {
        Date date = new Date();
        pathOut += "Test OCR effectués le " + date.toString() + "\n";
        int[][] matriceConfusion = new int[10][10];

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                int temp = i + j;
                char a = (char) temp;
                OCRImage tmpOCR = new OCRImage(img, a, "chemin");
                if (i >= 1 && j >= 1) {
                    matriceConfusion[i][j] = Character.getNumericValue(tmpOCR.getDecision());
                } else if (j == 0) {
                    matriceConfusion[i][0] = i;
                } else if (i == 0) {
                    matriceConfusion[0][j] = j;
                }
            }
        }

        StringBuilder s = new StringBuilder("OCR test performed on " + new Date() + "\n");
        s.append("   ");
        for (int i = 0; i < 10; i++) {
            s.append("  ").append(i).append("  ");
        }
        s.append("\n");
        s.append("---");
        for (int i = 0; i < 10; i++) {
            s.append("-----");
        }
        s.append("\n");
        for (int i = 0; i < 10; i++) {
            s.append(i).append("| ");
            for (int j = 0; j < 10; j++) {
                s.append("  0  ");
            }
            s.append("\n");
        }
        s.append("---");
        for (int i = 0; i < 10; i++) {
            s.append("-----");
        }
        s.append("\n");

        Charset charset = Charset.forName("UTF-8");
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(pathOut), charset)) {
            writer.write(s.toString());
        } catch (IOException x) {
            System.err.format("IOException: %s%n", x);
        }
    }

    public void createListeImage(String path, ArrayList<OCRImage> listeImg) {
        File[] files = new File(path).listFiles();
        assert files != null;
        if (files.length != 0) {
            for (File file : files) {
                ImagePlus tempImg = new ImagePlus(file.getAbsolutePath());
                new ImageConverter(tempImg).convertToGray8();
                listeImg.add(new OCRImage(tempImg,
                        file.getName().substring(0, 1).charAt(0),
                        file.getAbsolutePath()));
            }
        }
    }

    public void setFeatureGsVect() {
        for (OCRImage image : listImg) {
            image.setFeatureGs();
        }
    }
}