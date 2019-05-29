package cpsr.planning.mcts;

import java.util.Vector;


public class History {
    public class ENTRY{
        ENTRY(){}
        ENTRY(int action,int obs){
            Action=action;
            Observation=obs;
        }
        int Action;
        int Observation;
    }
    public void Add(int action,int obs){
        History.add(new ENTRY(action,obs));
    }

    public void Pop(){
        History.remove(History.size()-1);
    }

    public void  Truncate(int t)
    {
        History.setSize(t);
    }

    public int Size(){
        return History.size();
    }

    public void Clear(){
        History.clear();
    }


    private Vector<ENTRY> History;

    public History() {
        History = new Vector<>();
    }
}
