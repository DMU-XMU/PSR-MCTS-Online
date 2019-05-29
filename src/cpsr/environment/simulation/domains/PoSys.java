package cpsr.environment.simulation.domains;





import cpsr.environment.components.Action;
import cpsr.environment.components.Observation;
import cpsr.environment.simulation.ASimulator;

public class PoSys extends ASimulator {
    protected static int comNum=3;
    protected static int state[]=new int[]{1,1,1};
    protected static int currentObs;
    protected static int currentReward;



    public PoSys(long seed, int maxRunLength)
    {
        super(seed, maxRunLength);
    }

    public PoSys(long seed)
    {
        super(seed);
    }

    @Override
    protected void initRun() {
        state= new int[]{1, 1, 1};
    }

    @Override
    protected int getNumberOfActions() {
        return comNum*2+1;
    }

    @Override
    protected int getNumberOfObservations() {
        return (comNum+1)*3;
    }

    @Override
    public int getNumAct() {
        return comNum*2+1;
    }

    @Override
    public int getNumObs() {
        return (comNum+1)*3;
    }

    @Override
    public boolean CheckTerminal(int obs) {
        return false;
    }

    @Override
    protected boolean inTerminalState() {
        return false;
    }

    @Override
    protected void executeAction(Action act) {
        for (int i=0;i<state.length;i++){
            if (rando.nextDouble()>0.9){
                state[i]=0;
            }
        }


        int moveDir=act.getID();

        if (moveDir==0){
            int count=0;
            for (int i=0;i<state.length;i++){
                if (state[i]==0){
                    count=count+1;
                }
            }
            currentObs=count;

        }else if (moveDir>0&&moveDir<comNum+1){
            int count=0;
            for (int i=0;i<state.length;i++){
                if (state[i]==0){
                    count++;
                }
            }
            if (state[moveDir-1]==1){
                currentObs=count+2*(comNum+1);
            }else {
                currentObs=count+(comNum+1);
            }
        } else if (moveDir>comNum){
            state[moveDir-comNum-1]=1;
            int count=0;
            for (int i=0;i<state.length;i++){
                if (state[i]==0){
                    count++;
                }
            }
            currentObs=count;

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
    public double getReward(int action, int observation) {
        double reward=0.0;
        int downc=observation%(comNum+1);
        reward=reward-downc*10;
        if(action>0&&action<(comNum+1))
        {
            reward=reward-1.0;
        }else if(action>comNum)
        {
            reward=reward-20.0;
        }
        return reward;
    }

    @Override
    public String getName() {
        return "PoSys";
    }

}
