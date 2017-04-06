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
    private static ArrayList<OCRImage> listImg;
    private static ArrayList<ArrayList<Double>> listImgVect;

    public static void main(String[] args) {
        listImg = new ArrayList<>();
        listImgVect = new ArrayList<>();

        /** TESTS **/
        logOCRTest("confusion-matrix-test.txt");
        distTest();
        GSTest();


        /** REAL CODE **/
        createListImage("src/baseProjetOCR", listImg);
        setFeatureGsVect();
        setImageDecision();
        logOCR("confusion-matrix.txt");

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

    public static void createListImage(String path, ArrayList<OCRImage> listeImg) {
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

    private static int countSuccess(int[][] matriceConfusion)
    {
        int countSuccess = 0;
        for (int i = 0; i < 10; i++)
        {
           countSuccess += matriceConfusion[i][i];
        }

        return countSuccess;
    }


    /**
     * Get the grey scale of every image
     */
    private static void setFeatureGsVect() {
        for (OCRImage image : listImg) {
            image.setFeatureGs();
            listImgVect.add(image.getVect());
        }
    }

    /**
     * Set the decision of every image
     */
    private static void setImageDecision() {
        for (OCRImage image : listImg) {
            char temp = Character.forDigit(0, 10);
            int calcul = CalculMath.PPV(image.getVect(), listImgVect);

            if(calcul < 10)
                temp = Character.forDigit(0, 10);
            else if(calcul < 20)
                temp = Character.forDigit(1, 10);
            else if(calcul < 30)
                temp = Character.forDigit(2, 10);
            else if(calcul < 40)
                temp = Character.forDigit(3, 10);
            else if(calcul < 50)
                temp = Character.forDigit(4, 10);
            else if(calcul < 60)
                temp = Character.forDigit(5, 10);
            else if(calcul < 70)
                temp = Character.forDigit(6, 10);
            else if(calcul < 80)
                temp = Character.forDigit(7, 10);
            else if(calcul < 90)
                temp = Character.forDigit(8, 10);
            else if(calcul < 100)
                temp = Character.forDigit(9, 10);

            image.setDecision(temp);
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

    /**
     * Get the decision of every image
     */
    private static void getImageDecsision() {
        for (OCRImage image : listImg) {
            System.out.println("Image : " + image.getImg().getTitle() + " / Decision : " + image.getDecision());
        }
    }


}

