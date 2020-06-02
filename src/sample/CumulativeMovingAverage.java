package sample;

import java.util.ArrayList;
import java.util.List;

public class CumulativeMovingAverage {
    int n = 0;
    double average = 0.0;

    public List<Double> getCMA(List<Double> data1){
        System.out.println("siiize " +data1.size());
        List<Double> data = new ArrayList<Double>(data1.size());
        for (int i=0;i<data1.size();i++) {
            data.add(i,add(data1.get(i)));
        }
        return data;
    }

    public double add(double x) {
        return average += (x - average) / ++n;
    }

}
