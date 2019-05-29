package cpsr.planning.mcts;



public class Statistic {
    private int Count;
    private double Mean;
    private double Variance;
    private double Min,Max;

    public Statistic(double mean,int count) {
        Count = count;
        Mean = mean;
    }

    public Statistic() {
        Count=0;
        Mean=0;
        Variance=0;
        Min=Double.MAX_VALUE;
        Max=-(Double.MAX_VALUE);
    }
    public void Add(double val){
        double meanOld=Mean;
        int countOld=Count;
        Count=Count+1;
        Mean+=(val-Mean)/Count;
        Variance= (countOld * (Variance + meanOld * meanOld) + val * val) / Count - Mean * Mean;
        if (val > Max)
            Max = val;
        if (val < Min)
            Min = val;
    }

    public void clear(){
        Count=0;
        Mean=0;
        Variance=0;
        Min=Double.MAX_VALUE;
        Max=-(Double.MAX_VALUE);
    }

    public int getCount(){
        return Count;
    }
    public double getTotal(){
        return Mean * Count;
    }
    public double getMean(){
        return Mean;
    }
    public double getStdDev(){
        return Math.sqrt(Variance);
    }
    public double getStdErr(){
        return Math.sqrt(Variance/Count);
    }

    public double getMax() {
        return Max;
    }

    public double getMin() {
        return Min;
    }
}
