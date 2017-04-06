package main;

import aimage.CalculMath;
import aimage.OCRImage;
import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;

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
    private static ArrayList<OCRImage> listImg;
    private static ArrayList<ArrayList<Double>> listImgVect;

    public static void main(String[] args) {
        listImg = new ArrayList<>();
        listImgVect = new ArrayList<>();

        /****** TESTS ******/
        /*logOCRTest("confusion-matrix-test.txt");
        distTest();
        GSTest();*/


        /****** REAL CODE ******/

        /** Vecteur Niveau de gris **/
        setFeatureVect("GS");

        /** Vecteur Horizontal **/
        setFeatureVect("VP");

        /** Vecteur Vertical **/
        setFeatureVect("HP");

        /** Vecteur Horizontal-Vertical **/
        setFeatureVect("HVP");

        /** Vecteur Isoperimetrique **/
        setFeatureVect("I");

        /** Vecteur Niveau de gris + Horizontal-Vertical **/
        setFeatureVect("GS+HVP");

        /** Vecteur Niveau de gris + Isoperimetrie **/
        setFeatureVect("GS+I");

        /** Vecteur Isoperimetrie + Horizontal-Vertical **/
        setFeatureVect("I+HVP");

        /** Vecteur Niveau de gris +  Isoperimetrie + Horizontal-Vertical **/
        setFeatureVect("GS+I+HVP");
    }

    private static void GSTest() {
        img = new ImagePlus("src/baseProjetOCR/0_1.png");
        OCRImage ocrImage = new OCRImage(img, (char) 0, "path");
        System.out.println("Niveau de gris = " + ocrImage.averageGs());
    }

    private static void distTest() {
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
        IJ.showMessage(" dist = " + CalculMath.PPV(tab0, myList));
    }

    private static void logOCRTest(String pathOut) {
        int[][] matriceConfusion = new int[10][10];

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                int temp = i + j;
                char a = (char) temp;
                OCRImage tmpOCR = new OCRImage(img, a, "testMatrice");
                matriceConfusion[i][j] = Character.getNumericValue(tmpOCR.getDecision());
            }
        }

        structFile(matriceConfusion, pathOut);
    }

    private static void logOCR(String pathOut) {
        Date date = new Date();
        int[][] matriceConfusion = new int[10][10];


        for (int label = 0; label < 10; label++) {
            for (int nbDecision = 0; nbDecision < 10; nbDecision++) {
                matriceConfusion[label][nbDecision] = 0;
            }
        }

        for(OCRImage image : listImg)
        {
            int label = Character.getNumericValue(image.getImg().getTitle().substring(0, 1).charAt(0));
            int decision = Character.getNumericValue(image.getDecision());
            matriceConfusion[label][decision]++;
        }

        structFile(matriceConfusion, pathOut);
    }

    private static void structFile(int[][] matriceConfusion, String pathOut) {
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
                s.append("  " + matriceConfusion[i][j] + "  ");
                //s.append("  0  ");
            }
            s.append("\n");
        }
        s.append("---");
        for (int i = 0; i < 10; i++) {
            s.append("-----");
        }
        s.append("\n Le taux de reconnaissance est de " + countSuccess(matriceConfusion) + "%");

        Charset charset = Charset.forName("UTF-8");
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(pathOut), charset)) {
            writer.write(s.toString());
        } catch (IOException x) {
            System.err.format("IOException: %s%n", x);
        }
    }

    public static void createListImage(String path) {
        listImg = new ArrayList<>();
        listImgVect = new ArrayList<>();
        File[] files = new File(path).listFiles();
        assert files != null;
        if (files.length != 0) {
            for (File file : files) {
                ImagePlus tempImg = new ImagePlus(file.getAbsolutePath());
                new ImageConverter(tempImg).convertToGray8();
                listImg.add(new OCRImage(tempImg,
                        file.getName().substring(0, 1).charAt(0),
                        file.getAbsolutePath()));
            }
        }
    }

    private static int countSuccess(int[][] matriceConfusion) {
        int countSuccess = 0;
        for (int i = 0; i < 10; i++)
        {
           countSuccess += matriceConfusion[i][i];
        }

        return countSuccess;
    }


    /**
     * 1 - Create images
     * 2 - Set the appropriate filter of every image in their vector
     * 3 - Set their decision with their vector
     * 4 - Generate the file that contains the matrix
     */
    private static void setFeatureVect(String filters) {
        createListImage("src/baseProjetOCR");
        for (OCRImage image : listImg) {

            switch (filters) {
                case "GS":
                    image.setFeatureGs();
                    break;
                case "HP":
                    image.setFeatureHProfile();
                    break;
                case "VP":
                    image.setFeatureVProfile();
                    break;
                case "HVP":
                    image.setFeatureHVProfile();
                    break;
                case "I":
                    image.setFeatureIso();
                    break;
                case "GS+HVP":
                    image.setFeatureGsAndHVProfile();
                    break;
                case "GS+I":
                    image.setFeatureGsAndIso();
                    break;
                case "I+HVP":
                    image.setFeatureIsoAndHVProfile();
                    break;
                case "GS+I+HVP":
                    image.setFeatureGsAndIsoAndHVProfile();
                    break;
            }
            listImgVect.add(image.getVect());
        }
        setImageDecision();
        String pathOut = "Confusion_Matrices/";
        switch (filters) {
            case "GS":
                pathOut += "Single_Filter/GreyScale.txt";
                break;
            case "HP":
                pathOut += "Single_Filter/HorizontalProfile.txt";
                break;
            case "VP":
                pathOut += "Single_Filter/VerticalProfile.txt";
                break;
            case "HVP":
                pathOut += "Single_Filter/HorizontalVerticalProfile.txt";
                break;
            case "I":
                pathOut += "Single_Filter/Isoperimetry.txt";
                break;
            case "GS+HVP":
                pathOut += "Many_Filters/GreyScale-and-HorizontalVerticalProfile.txt";
                break;
            case "GS+I":
                pathOut += "Many_Filters/GreyScale-and-Isoperimetry.txt";
                break;
            case "I+HVP":
                pathOut += "Many_Filters/Isoperimetry-and-HorizontalVerticalProfile.txt";
                break;
            case "GS+I+HVP":
                pathOut += "Many_Filters/GreyScale-and-Isoperimetry-and-HorizontalVerticalProfile.txt";
                break;
        }
        logOCR(pathOut);
    }

    /**
     * Set the decision of every image
     */
    private static void setImageDecision() {
        for (OCRImage image : listImg) {
            int decision = CalculMath.PPV(image.getVect(), listImgVect);
            image.setDecision(listImg.get(decision).getLabel());
        }
    }

    /**
     * Get the grey scale of every image
     */
    private static void getGsVectValue() {
        for (OCRImage image : listImg) {
            System.out.println("Image : " + image.getImg().getTitle() + " / Grey Scale : " + image.getVect(0));
        }
    }

    private static void getVectValue(OCRImage image) {
        System.out.println("Image : " + image.getImg().getTitle());
        for (int i  = 0; i < image.getVect().size(); i++)
        {
            System.out.println(" Vector valor for [" + i + "] : " + image.getVect(i));
        }
    }

    /**
     * Get the decision of every image
     */
    private static void getImageDecsision() {
        for (OCRImage image : listImg) {
            System.out.println("Image : " + image.getImg().getTitle() + " / Decision : " + image.getDecision());
        }
    }
}

