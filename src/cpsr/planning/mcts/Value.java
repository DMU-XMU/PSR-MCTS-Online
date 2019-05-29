package cpsr.planning.mcts;


public class Value {
    private int Count;
    private double Total;
    public void Set(int count,double value){
        Count=count;
        Total=value*count;
    }

    public void Add(double totalReward){
        Count+=1;
        Total+=totalReward;
    }

    public void Add(double totalReward,double weight){
        Count+=weight;
        Total+=totalReward*weight;
    }
    public double GetValue(){
        return Count==0?Total:Total/Count;
    }
    public int GetCount(){
        return Count;
    }

    public Value() {
        Count=0;
        Total=0.0;
    }
}
