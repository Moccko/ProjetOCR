package aimage;

import ij.ImagePlus;
import ij.process.ImageProcessor;

import javax.swing.*;
import java.awt.*;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;

public class OCRImage {
    private ImagePlus img; // contenu de l ’ image
    private char label; // correspond au label de l ’ image ( donne par le nom du fichier - - > 0 ,1 ,... ou 9)
    private String path; // path du fichier image
    private char decision; // affectation du label apres classification
    private ArrayList<Double> vect; // Vecteur de caracteristiques de l ’ image

    public OCRImage(ImagePlus img, char label, String path) {
        this.img = img;
        this.path = path;
        this.label = label;
        decision = '?';
        vect = new ArrayList<>();

        if(path == "testMatrice")
        {
            int val = new Random().nextInt(10);
            String i = Integer.toString(val);
            decision = i.charAt(0);
        }
    }

    public ImagePlus getImg() {
        return img;
    }

    public void setImg(ImagePlus img) {
        this.img = img;
    }

    public void setVect(int i, double val) {
        this.vect.set(i, val);
    }

    public Double getVect(int i) {
        return vect.get(i);
    }

    public char getLabel() {
        return label;
    }

    public void setLabel(char label) {
        this.label = label;
    }

    public char getDecision() {
        return decision;
    }

    public void setDecision(char decision) {
        this.decision = decision;
    }

    public ArrayList<Double> getVect() {
        return vect;
    }

    public void setVect(ArrayList<Double> vect) {
        this.vect = vect;
    }


    public double averageGs() {
        ImageProcessor imageProcessor = img.getProcessor();
        int height = imageProcessor.getHeight();
        int width = imageProcessor.getWidth();

        double sum = 0.d;

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                sum += img.getPixel(i, j)[0];
            }
        }

        return sum / (height*width);
    }

    public void setFeatureGs() {
        vect = new ArrayList<>();
        vect.add(averageGs());
    }
}
