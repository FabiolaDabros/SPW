package sample;

import java.util.ArrayList;
import java.util.List;

public class ExponentialMovingAverage {
    private double alpha;
    private Double oldValue;

    public ExponentialMovingAverage(double alpha) {
        this.alpha = alpha;
    }

    public List<Double> getEMA(List<Double> data1){
        //List<Double> ema_data = new ArrayList<>(data.size());

        List<Double> data = new ArrayList<>();
        for(int i=0;i<data1.size();++i) {
            data.set(i,average(data1.get(i)));
        }
        return data;
    }

    public double average(double value) {
        if (oldValue == null) {
            oldValue = value;
            return value;
        }
        double newValue = oldValue + alpha * (value - oldValue);
        oldValue = newValue;
        return newValue;
    }
}
