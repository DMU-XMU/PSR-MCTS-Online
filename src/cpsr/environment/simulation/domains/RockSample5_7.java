package cpsr.environment.simulation.domains;

import cpsr.environment.components.Action;
import cpsr.environment.components.Observation;
import cpsr.environment.simulation.BSimulator;


import java.util.Vector;


public class RockSample5_7 extends BSimulator {

    public RockSample5_7(long seed, int maxRunLength) {
        super(seed, maxRunLength);
    }

    public RockSample5_7(long seed) {
        super(seed);
    }

    @Override
    public String getName() {
        return "RockSample";
    }

    @Override
    protected void initRun() {
        //System.out.println("init");
        Size=5;
        grid=new Grid(5,5);

        Coord[] rocks={new Coord(1,0),new Coord(2,1),new Coord(1,2),new Coord(2,2),
                new Coord(4,2),new Coord(0,3),new Coord(3,4)};
        rockPos=new Vector<>();
        rockPos.setSize(numRocks);
        halfEfficiencyDistance=20;
        //startPos=new Coord(0,3);
        agentPos=new Coord(0,2);
        grid.SetAllValues(-1);
        for (int i=0;i<numRocks;i++){
            grid.setValue(rocks[i],i);
            rockPos.set(i,rocks[i]);

        }
        inTerminalState=false;

        isCollected=new boolean[numRocks];
        for (int i=0;i<isCollected.length;i++){
            isCollected[i]=false;
        }

        for (int i=0;i<isChecked.length;i++){
            isChecked[i]=2;
        }


        Valuable=new int[numRocks];
        for (int i=0;i<Valuable.length;i++){

            Valuable[i]=rando.nextInt(2);

        }



    }

    @Override
    protected int getNumberOfActions() {
        return numRocks+5;
    }

    @Override
    protected int getNumberOfObservations() {
        return 7;
    }

    @Override
    protected boolean inTerminalState() {
        return inTerminalState;
    }

    @Override
    protected void executeAction(Action act) {
        isLegal=true;
        int actId=act.getID();
        if (actId<sample){
            switch (actId){
                case 0:{
                    if (agentPos.Y+1<Size){
                        agentPos.Y++;
                        currentObs=0;
                    }else {
                        isLegal=false;
                        currentObs=6;
                    }
                    break;
                }
                case 1:{
                    if (agentPos.X+1<Size){
                        agentPos.X++;
                        currentObs=0;
                    }else {
                        currentObs=6;
                        inTerminalState=true;
                    }
                    break;
                }
                case 2:{
                    if (agentPos.Y-1>=0){
                        agentPos.Y--;
                        currentObs=0;
                    }else {
                        isLegal=false;
                        currentObs=6;
                    }
                    break;
                }
                case 3:{
                    if (agentPos.X-1>=0){
                        agentPos.X--;
                        currentObs=0;
                    }else {
                        isLegal=false;
                        currentObs=6;
                    }
                    break;
                }
            }
        }
        if (actId==sample){
            int rock=grid.getGrid(grid.index(agentPos));
            if (rock>=0&&isCollected[rock]==false){
                isCollected[rock]=true;
                if (Valuable[rock]==1){
                    currentObs=5;
                }else if (Valuable[rock]==0){
                    currentObs=4;
                }
            }else {
                isLegal=false;
                currentObs=3;
            }
        }
        if (actId>sample) {
            int rock = actId - sample - 1;
            if (rock>=0&&isCollected[rock] == false) {
                double distance = Coord.EuclideanDistance(agentPos, rockPos.get(rock));
                double efficiency = (1 + Math.pow(2, -(distance / halfEfficiencyDistance))) * 0.5;
                if (rando.nextDouble() < efficiency) {
                    currentObs = Valuable[rock] + 1;

                } else {
                    currentObs = 2 - Valuable[rock];

                }
                isChecked[rock]=isChecked[rock]-1;
            }else {
                currentObs=3;
                isLegal=false;
            }
        }


    }

    @Override
    protected double getCurrentReward() {
        return 0;
    }

    @Override
    protected Observation getCurrentObservation() {
        return new Observation(currentObs);
    }

    @Override
    public int getNumAct() {
        return getNumberOfActions();
    }

    @Override
    public int getNumObs() {
        return getNumberOfObservations();
    }

    @Override
    public double getReward(int action, int observation) {
        double reward=0.0;
        if (action==1&&observation==6){
            reward=10.0;
        }else if (action==sample){
            if (observation==4){
                reward=10.0;
            }else if (observation==5){
                reward=-10.0;
            }
        }

        return reward;
    }
    @Override
    public boolean checkLegal() {
        return isLegal;
    }

    @Override
    public boolean CheckTerminal(int obs) {
        if (obs==6) {
            return true;
        }else {
            return false;
        }
    }


    @Override
    public Vector<Integer> getLegalAction() {
        Vector<Integer> legalAcion=new Vector<>();
        if (agentPosCopy.Y+1<Size){
            legalAcion.add(0);
        }
        legalAcion.add(1);
        if (agentPosCopy.Y-1>=0){
            legalAcion.add(2);
        }
        if(agentPosCopy.X-1>=0){
            legalAcion.add(3);
        }
        int rock=grid.getGrid(grid.index(agentPosCopy));
        if (rock>=0&&isCollectedCopy[rock]==false){
            legalAcion.add(4);
        }
        for (int i=5;i<12;i++){
            int rocknum = i - sample - 1;
            if (isCollectedCopy[rocknum]==false) {
                legalAcion.add(i);
            }
        }
        return legalAcion;
    }


    @Override
    public void CopyState(BSimulator simulator) {
        this.agentPos.X=simulator.agentPos.X;
        this.agentPos.Y=simulator.agentPos.Y;

        for (int i=0;i<isCollected.length;i++){
            this.isCollected[i]=simulator.isCollected[i];
        }



    }


    @Override
    public void getStateToUse() {
        agentPosCopy.X=agentPos.X;
        agentPosCopy.Y=agentPos.Y;
        for (int i=0;i<isCollected.length;i++){
            isCollectedCopy[i]=isCollected[i];
        }


    }


    @Override
    public void StateToUseuUpdate(int act) {
        isLegal=true;
        int actId=act;
        if (actId<sample){
            switch (actId){
                case 0:{
                    if (agentPosCopy.Y+1<Size){
                        agentPosCopy.Y++;
                    }
                    break;
                }
                case 1:{
                    if (agentPosCopy.X+1<=Size){
                        agentPosCopy.X++;
                    }
                    break;
                }
                case 2:{
                    if (agentPosCopy.Y-1>=0){
                        agentPosCopy.Y--;
                    }
                    break;
                }
                case 3:{
                    if (agentPosCopy.X-1>=0){
                        agentPosCopy.X--;
                    }
                    break;
                }
            }
        }
        if (actId==sample){
            int rock=grid.getGrid(grid.index(agentPosCopy));
            if (rock>=0&&isCollectedCopy[rock]==false){
                isCollectedCopy[rock]=true;
            }
        }
    }


    boolean[] isCollectedCopy=new boolean[7];


    int[] isChecked=new int[7];





    public int sample=4;
    public Grid grid;
    public int numRocks=7;



    double halfEfficiencyDistance;
    public Vector<Coord> rockPos;

    public int[] Valuable;
    public boolean isLegal=true;

    protected static int currentObs;
    protected boolean inTerminalState;


    @Override
    public int getPreferAction() {
        int action=-1;

        Vector<Integer> preferAcion=new Vector<>();

        int rock=grid.getGrid(grid.index(agentPos));
        double r=rando.nextDouble();
        if (rock>=0&&isCollected[rock]==false&&(r<0.5||isChecked[rock]<2)){

                action = 4;

        }else {

            if (agentPos.Y+1<Size){
                preferAcion.add(0);
            }
            preferAcion.add(1);
            if (agentPos.Y-1>=0){
                preferAcion.add(2);
            }
            if(agentPos.X-1>=0){
                preferAcion.add(3);
            }





            for (int j=0;j<isCollected.length;j++){
                if (isCollected[j]==false&&isChecked[j]>0){
                    preferAcion.add(j+sample+1);
                }
            }
            int temp=rando.nextInt(preferAcion.size());
            action=preferAcion.get(temp);
        }
        return action;
    }







}
