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

        resize(20, 20); // on resize l'image en 20*20
    }

    private void resize(int width, int height) {
        ImageProcessor ip = img.getProcessor();
        ip.setInterpolate(true);
        ip = ip.resize(width, height);
        img.setProcessor(null, ip);
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

    public void setFeatureHProfile() {
        vect = new ArrayList<>();
        for(int i = 0; i < img.getHeight(); i++)
        {
            double value = 0.d;
            for(int j = 0; j < img.getWidth(); j++)
            {
                value =+ img.getPixel(j, i)[0];
            }
            vect.add(value/img.getWidth());
        }
    }

    public void setFeatureVProfile() {
        vect = new ArrayList<>();
        for(int i = 0; i < img.getWidth(); i++)
        {
            double value = 0.d;
            for(int j = 0; j < img.getHeight(); j++)
            {
                value =+ img.getPixel(i, j)[0];
            }
            vect.add(value/img.getHeight());
        }
    }

    public void setFeatureHVProfile() {
        ArrayList<Double> tempVect;
        setFeatureHProfile();
        tempVect = vect;
        setFeatureVProfile();
        tempVect.addAll(vect);
        vect = tempVect;
    }

    public void setFeatureIso() {
        vect = new ArrayList<>();
        double rapportIso = perimetre()/(4 * Math.PI * surface());
        vect.add(rapportIso);
    }

    public int surface() {
        int coloredPixel = 0;
        for(int i = 0; i < img.getWidth(); i++)
        {
            for(int j = 0; j < img.getHeight(); j++)
            {
                if(img.getPixel(i, j)[0] < 127)
                {
                    coloredPixel++;
                }
            }
        }
        return coloredPixel;
    }

    public int perimetre() {
        int blackPixel = 0;
        for(int i = 0; i < img.getWidth(); i++)
        {
            for(int j = 0; j < img.getHeight(); j++)
            {
                if(img.getPixel(i, j)[0] < 128 && ((img.getPixel(i+1, j)[0] > 127) || (img.getPixel(i, j+1)[0] > 127) || (img.getPixel(i-1, j)[0] > 127) || (img.getPixel(i, j-1)[0] > 127)))
                {
                    // Si le voisin est noir et qu'il a au moins 1 voisin blanc, on le compte
                    blackPixel++;
                }
            }
        }
        return blackPixel;
    }

    public void setFeatureZoning() {

        /* Ancienne méthode -> inefficace car créait 16 éléments au lieu de 9 (on ne sait pas pourquoi)

        for(int i = 0; i < img.getWidth(); i+= img.getWidth()/3)
        {
            for(int j = 0; j < img.getHeight(); j+= img.getHeight()/3)
            {
                img.setRoi(i, j, img.getWidth()/3, img.getHeight()/3);
                ImagePlus imagePlusTemp = img.duplicate();
                OCRImage ocrImageTemp = new OCRImage(imagePlusTemp, '?', null);
                vect.add(ocrImageTemp.averageGs());
            }
        }
        */

        for (int div = 0; div < 9; div++) {
            Double sum = 0.0;
            for (int i = (div/3)*(img.getHeight()/3); i < (div/3)*(img.getHeight()/3)+img.getHeight()/3; i++)
            {
                for (int j = (div%3)*(img.getWidth()/3); j < (div%3)*(img.getWidth()/3)+img.getWidth()/3; j++)
                {
                    sum += img.getPixel(j, i)[0];
                }
            }
            vect.add(sum/((img.getWidth()/3)*(img.getHeight()/3)));
        }
    }

    // Filtres niveau de gris + profil horizontal-Vertical
    public void setFeatureGsAndHVProfile() {
        ArrayList<Double> tempVect;
        setFeatureGs();
        tempVect = vect;
        setFeatureHVProfile();
        tempVect.addAll(vect);
        vect = tempVect;
    }

    // Filtres niveau de gris + Isoperimetrie
    public void setFeatureGsAndIso() {
        ArrayList<Double> tempVect;
        setFeatureGs();
        tempVect = vect;
        setFeatureIso();
        tempVect.addAll(vect);
        vect = tempVect;
    }

    // Isoperimetrie + profil horizontal-Vertical
    public void setFeatureIsoAndHVProfile() {
        ArrayList<Double> tempVect;
        setFeatureIso();
        tempVect = vect;
        setFeatureHVProfile();
        tempVect.addAll(vect);
        vect = tempVect;
    }


    // Niveau de gris + Zoning
    public void setFeatureGSAndZoning() {
        ArrayList<Double> tempVect;
        setFeatureGs();
        tempVect = vect;
        setFeatureZoning();
        tempVect.addAll(vect);
        vect = tempVect;
    }

    //  Isoperimetrie + Zoning
    public void setFeatureIsoAndZoning() {
        ArrayList<Double> tempVect;
        setFeatureIso();
        tempVect = vect;
        setFeatureZoning();
        tempVect.addAll(vect);
        vect = tempVect;
    }

    // Zoning + Profil horizontal-Vertical
    public void setFeatureZoningAndHVProfile() {
        ArrayList<Double> tempVect;
        setFeatureZoning();
        tempVect = vect;
        setFeatureHVProfile();
        tempVect.addAll(vect);
        vect = tempVect;
    }

    // Niveau de gris + Zoning + Profil horizontal-Vertical
    public void setFeatureGSAndZoningAndHVProfile() {
        ArrayList<Double> tempVect;
        setFeatureGs();
        tempVect = vect;
        setFeatureZoning();
        tempVect.addAll(vect);
        setFeatureHVProfile();
        tempVect.addAll(vect);
        vect = tempVect;
    }


    // Niveau de gris + Zoning + Isoperimetrie
    public void setFeatureGSAndZoningAndIso() {
        ArrayList<Double> tempVect;
        setFeatureGs();
        tempVect = vect;
        setFeatureZoning();
        tempVect.addAll(vect);
        setFeatureIso();
        tempVect.addAll(vect);
        vect = tempVect;
    }



    // Niveau de gris + Isoperimetrie + Profil horizontal-Vertical
    public void setFeatureGsAndIsoAndHVProfile() {
        ArrayList<Double> tempVect;
        setFeatureGs();
        tempVect = vect;
        setFeatureIso();
        tempVect.addAll(vect);
        setFeatureHVProfile();
        tempVect.addAll(vect);
        vect = tempVect;
    }

    // Zoning + Isoperimetrie + Profil horizontal-Vertical
    public void setFeatureZoningAndIsoAndHVProfile() {
        ArrayList<Double> tempVect;
        setFeatureZoning();
        tempVect = vect;
        setFeatureIso();
        tempVect.addAll(vect);
        setFeatureHVProfile();
        tempVect.addAll(vect);
        vect = tempVect;
    }

    // Niveau de gris + Isoperimetrie + Profil horizontal-Vertical + Zoning
    public void setFeatureGsAndIsoAndHVProfileAndZoning() {
        ArrayList<Double> tempVect;
        setFeatureGs();
        tempVect = vect;
        setFeatureIso();
        tempVect.addAll(vect);
        setFeatureHVProfile();
        tempVect.addAll(vect);
        setFeatureZoning();
        tempVect.addAll(vect);
        vect = tempVect;
    }
}
