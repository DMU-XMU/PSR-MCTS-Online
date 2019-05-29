package cpsr.planning.mcts;


import cpsr.environment.components.Action;
import cpsr.environment.components.ActionObservation;
import cpsr.environment.components.Observation;
import cpsr.environment.simulation.BSimulator;
import cpsr.model.APSR;
import cpsr.model.Predictor;

import java.util.Random;
import java.util.Vector;


public class RockMCTS {

    public RockMCTS(APSR psrModel, double explorationConstant, int numSimulations, int expandCount, int maxDepth, boolean reuseTree, BSimulator simulator) {
        this.psrModel = psrModel;
        ExplorationConstant = explorationConstant;
        NumSimulations = numSimulations;
        ExpandCount = expandCount;
        MaxDepth = maxDepth;
        ReuseTree = reuseTree;
        this.simulator = simulator;




        actNum=simulator.getNumAct();
        obsNum=simulator.getNumObs();
        predictor=new Predictor(psrModel);
        random=new Random(1);
        history=new History();
        nodeCreate=new Node(obsNum,actNum);
        Root=ExpandNode();


    }

    Random random;


    APSR psrModel;
    Predictor predictor;




    private double ExplorationConstant;
    private int NumSimulations;
    private int ExpandCount;
    private int MaxDepth;
    private boolean ReuseTree;


    int step=0;
    private Node nodeCreate;
    private Node.VNODE Root;
    private History history;
    private int TreeDepth, PeakTreeDepth;






    static int UCB_N = 10000, UCB_n = 5000;
    static double UCB[][]=new double[UCB_N][UCB_n];
    static boolean InitialisedFastUCB;
    static Vector<Integer> besta=new Vector<>();


    private Statistic StatTreeDepth=new Statistic();
    private Statistic StatRolloutDepth=new Statistic();
    private Statistic StatTotalReward=new Statistic();


    private BSimulator simulator;

    int actNum;
    int obsNum;



    int SelectActionPSR(){
        UCTSearch();
        step++;
        simulator.getStateToUse();
        return GreedyUCB(Root,false);
    }


    public void UCTSearch(){
        clearStatistics();
        int historyDepth=history.Size();
        for (int n=0;n<NumSimulations;n++){


            simulator.getStateToUse();
            //Tree phase

            psrModel.setPvToCurrentState();

            TreeDepth=0;
            PeakTreeDepth=0;

            double totalReward = SimulateVPSR(Root);
            StatTotalReward.Add(totalReward);
            StatTreeDepth.Add(PeakTreeDepth);
            history.Truncate(historyDepth);
        }
    }


    double SimulateVPSR(Node.VNODE vnode){
        //System.out.println("SimulateVPSR");
        int action=GreedyUCB(vnode,true);
        PeakTreeDepth=TreeDepth;
        if (TreeDepth>=MaxDepth)
            return 0;
        Node.QNODE qnode=vnode.Child(action);
        double totalReward=SimulateQPSR(qnode,action);
        vnode.value.Add(totalReward);
        return totalReward;

    }
    double SimulateQPSR(Node.QNODE qnode, int action){
        //System.out.println("SimulateQPSR");
        int observation;
        double immediateReward, delayedReward = 0;
        boolean terminal;

        observation=predictor.getObservation(action);
        terminal=simulator.CheckTerminal(observation);
        immediateReward=simulator.getReward(action,observation);



        simulator.StateToUseuUpdate(action);


        if(simulator.agentPosCopy.X>=simulator.Size&&observation==0){

            terminal=true;

        }



        psrModel.update(new ActionObservation(new Action(action,actNum-1),new Observation(observation,obsNum-1)));

        history.Add(action,observation);


        Node.VNODE vnode=qnode.Child(observation);


        if ((vnode==null)&&(terminal==false)&&(qnode.value.GetCount()>=ExpandCount)){
            vnode=ExpandNode();
            qnode.setChild(observation,vnode);
        }

        if (!terminal){
            TreeDepth++;
            if (vnode!=null){
                delayedReward = SimulateVPSR(qnode.Child(observation));
            }else {
                delayedReward = RolloutPSR();
            }
            TreeDepth--;
        }

        double totalReward=immediateReward+simulator.getDiscount()*delayedReward;
        qnode.value.Add(totalReward);
        return totalReward;
    }

    double RolloutPSR(){
        //System.out.println("RolloutPSR");
        double totalReward = 0.0;
        double discount = 1.0;
        boolean terminal = false;
        int numSteps;
        int action;
        for (numSteps = 0; numSteps + TreeDepth < MaxDepth && !terminal; ++numSteps){
            int observation;
            double reward;


            legalAction=simulator.getLegalAction();
            action=simulator.selectLegalRandom(legalAction,100);

            observation=predictor.getObservation(action);
            terminal=simulator.CheckTerminal(observation);
            reward=simulator.getReward(action,observation);




            simulator.StateToUseuUpdate(action);


            if(simulator.agentPosCopy.X>=simulator.Size&&observation==0){

                terminal=true;

            }


            psrModel.update(new ActionObservation(new Action(action,actNum-1),new Observation(observation,obsNum-1)));

            history.Add(action, observation);
            totalReward += reward * discount;
            discount *= simulator.getDiscount();

        }
        StatRolloutDepth.Add(numSteps);
        return totalReward;
    }

    void UpdatePSR(int action,int observation){
        history.Add(action,observation);
        psrModel.updateSaving(new ActionObservation(new Action(action,actNum-1),new Observation(observation,obsNum-1)));
        Node.QNODE qnode=Root.Child(action);
        Node.VNODE vnode=qnode.Child(observation);


        Root=ExpandNode();

    }




    Node.VNODE ExpandNode(){
        Node.VNODE vnode=nodeCreate.new VNODE();
        vnode.Initialise();
        vnode.value.Set(0,0);
        vnode.SetChildren(0,0);

        return vnode;

    }

    public void resetMCTS(){
        Root=ExpandNode();

    }





    public void clearStatistics(){
        StatTreeDepth.clear();
        StatRolloutDepth.clear();
        StatTotalReward.clear();

    }



    //UCB
    public void InitFastUCB(double exploration){
        for (int N = 0; N < UCB_N; ++N){
            for (int n = 0; n < UCB_n; ++n){
                if (n == 0)
                    UCB[N][n] = Double.MAX_VALUE;
                else
                    UCB[N][n] = exploration * Math.sqrt(Math.log((double)N + 1) / (double) n);
            }
        }

        InitialisedFastUCB = true;


    }

    public double FastUCB(int N, int n, double logN){
        if (InitialisedFastUCB && N < UCB_N && n < UCB_n)
            return UCB[N][n];

        if (n == 0)
            return Double.MAX_VALUE;
        else
            return ExplorationConstant * Math.sqrt( logN / (double) n);
    }

    public int GreedyUCB(Node.VNODE vnode,boolean ucb){


        legalAction=simulator.getLegalAction();


        besta.clear();
        double bestq=-(Double.MAX_VALUE);
        int N=vnode.value.GetCount();
        double logN=Math.log((double)N+1.0);


        for (int action=0;action<legalAction.size();action++){
            double q;
            int n;
            Node.QNODE qnode=vnode.Child(legalAction.get(action));
            q=qnode.value.GetValue();
            n=qnode.value.GetCount();

            if (ucb){
                q+=FastUCB(N,n,logN);
            }


            if (q>=bestq){
                if (q>bestq) {
                    besta.clear();
                }

                bestq=q;

                besta.add(legalAction.get(action));

            }
        }
    if (besta.isEmpty()){
        System.out.println("no action");
    }

    int i=random.nextInt(besta.size());

    return besta.get(i);

    }




    Vector<Integer> legalAction;




}
