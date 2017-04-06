package aimage;

import java.util.ArrayList;

/**
 * Created by roman on 31/03/17.
 */
public class CalculMath {

    public static double distEucli(ArrayList<Double> vect1, ArrayList<Double> vect2) {
        double sum = 0.d;
        for (int i = 0; i < vect1.size(); i++) {
            double x = (vect2.get(i) - vect1.get(i));
            sum += Math.pow(x, 2.d);
        }
        return Math.sqrt(sum);
    }

    public static int PPV(ArrayList<Double> vect, ArrayList<ArrayList<Double>> tabVect) {
        int index = -1;
        double dist = Double.MAX_VALUE;
        double dist1 = 0.d;

        for (int i = 0; i < tabVect.size(); i++) {
            if(vect != tabVect.get(i))
                dist1 = distEucli(vect, tabVect.get(i));
            if (dist1 < dist) {
                dist = dist1;
                index = i;
            }
        }
        return index;
    }
}
