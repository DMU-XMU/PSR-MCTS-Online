package cpsr.planning.mcts;


import cpsr.environment.TrainingDataSet;
import cpsr.environment.components.Action;
import cpsr.environment.components.ActionObservation;
import cpsr.environment.components.Observation;
import cpsr.environment.simulation.BSimulator;
import cpsr.environment.simulation.domains.RockSample5_5;
import cpsr.model.APSR;

import cpsr.model.TPSR_Improve;
import cpsr.stats.PSRObserver;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;


public class RockSample5_5MCTSExperiment {
    public static final String DEF_DISCOUNT_FACTOR = "0.9", DEF_MIN_SING_VAL = "0.000000001";

    private BSimulator simulator;
    private BSimulator simulatorToRun;

    private int svdDim, maxTestLen, maxRunLength, maxHistLen;

    private double discount, minSingVal;



    private Properties psrProperties, plannerProperties;


    private PSRObserver psrObs;


    private Random rand=new Random(10);


    class Result {
        Statistic Time;
        Statistic Reward;
        Statistic DiscountedReturn;
        Statistic UndiscountedReturn;

        public Result() {
            Time = new Statistic();
            Reward = new Statistic();
            DiscountedReturn = new Statistic();
            UndiscountedReturn = new Statistic();
        }
    }


    public RockSample5_5MCTSExperiment(BSimulator pSimulatorToRun, BSimulator pSimulator, String pPSRConfigFile, String pPlanningConfigFile) {
        psrProperties = new Properties();
        plannerProperties = new Properties();

        simulator = pSimulator;
        simulatorToRun = pSimulatorToRun;

        Result result = new Result();


        try {
            psrProperties.load(new FileReader(pPSRConfigFile));
            plannerProperties.load(new FileReader(pPlanningConfigFile));

            //getting PSR parameters.
            svdDim = Integer.parseInt(psrProperties.getProperty("SVD_Dimension", "-1"));
            maxTestLen = Integer.parseInt(psrProperties.getProperty("Max_Test_Length", "-1"));
            maxHistLen = Integer.parseInt(psrProperties.getProperty("Max_History_Length", "-1"));
            minSingVal = Double.parseDouble(psrProperties.getProperty("Min_Singular_Val", DEF_MIN_SING_VAL));


            //getting planning parameters
            discount = Double.parseDouble(plannerProperties.getProperty("Discount_Factor", DEF_DISCOUNT_FACTOR));
            maxRunLength = Integer.parseInt(plannerProperties.getProperty("Max_Run_Length", "-1"));
            if (maxRunLength == -1) maxRunLength = Integer.MAX_VALUE;

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        if (svdDim == -1) {
            throw new IllegalArgumentException("Missing required parameter");
        }
    }


    private APSR initPSRModel(String psrType, TrainingDataSet trainData) {
        APSR psr = null;
        if (maxHistLen != -1) {
            psr = new TPSR_Improve(trainData, minSingVal, svdDim, maxHistLen);
        } else {
            psr = new TPSR_Improve(trainData, minSingVal, svdDim);
        }


        return psr;
    }









    public void runExperimentUpdate() {

        APSR psrModel;
        String psrType;
        psrObs = new PSRObserver();
        simulator.setMaxRunLength(maxRunLength);

        double Rewrad[]=new double[10];
        double UDRewrad[]=new double[10];

        TrainingDataSet trainDataSave = new TrainingDataSet(maxTestLen);
        trainDataSave.newDataBatch(100000);
        simulator.simulateTrainingRuns(40, trainDataSave);


        simulator.resetToStar();

        for (int run=0;run<1;run++){
            System.out.println("RUN------------------------------------"+run+"------------------------------");

            TrainingDataSet trainData=CloneUtils.clone(trainDataSave);


            psrType = "TPSR";


            System.out.println(psrType);
            psrModel = initPSRModel(psrType, trainData);
            psrModel.addPSRObserver(psrObs);
            System.out.println("building model");

            psrModel.build();
            System.out.println("built");


            RockMCTS mcts=new RockMCTS(psrModel,20.0,1000,1,20,false,simulator);
            mcts.InitFastUCB(20.0);



            for (int epsiode=0;epsiode<10;epsiode++){

                System.out.println("------------------------------------"+epsiode+"------------------------------");

                if (epsiode>0) {
                    psrModel.build();
                }


                int n=1000;

                double sum=0.0;
                for (int runNum=0;runNum<n;runNum++) {

                    System.out.println("epsiode"+epsiode+"run"+runNum);
                    double undiscountedReturn = 0.0;
                    double discountedReturn = 0.0;



                    double discountTemp = 1.0;


                    boolean terminal=false;

                    simulatorToRun.resetToStar();

                    ArrayList<ActionObservation> runActObs = new ArrayList<ActionObservation>();
                    ArrayList<Double> runRewards = new ArrayList<Double>();

                    psrModel.resetPvToInitial();

                    for (int t = 0; t < 30; t++) {
                        //System.out.println("step"+"------------------------------------"+t+"------------------------------");
                        double reward;


                        int action;


                        simulator.CopyState(simulatorToRun);



                        double epsion=(0.8-0.2*epsiode);

                        action = mcts.SelectActionPSR();


                        if (rand.nextDouble() < epsion) {

                                action=simulatorToRun.getPreferAction();

                        }


                        int observation = simulatorToRun.Execution(action);

                        reward = simulatorToRun.getReward(action, observation);
                        terminal = simulatorToRun.CheckTerminal(observation);



                        if(terminal){
                            System.out.println("out");
                        }


                        runActObs.add(new ActionObservation(new Action(action, simulatorToRun.getNumAct() - 1),
                                new Observation(observation, simulatorToRun.getNumObs() - 1)));
                        runRewards.add(reward);
                        trainData.addRunDataForTraining(runActObs);


                        undiscountedReturn += reward;
                        discountedReturn += reward * discountTemp;
                        discountTemp *= discount;


                        if (terminal) {
                            break;
                        }
                        if (epsiode >= 0) {
                            mcts.UpdatePSR(action, observation);
                        }


                    }

                    if (runNum<40) {
                        trainData.addRunData(runActObs, runRewards);//new training data

                    }
                    System.out.println(undiscountedReturn);
                    Rewrad[epsiode]+=discountedReturn;
                    UDRewrad[epsiode]+=undiscountedReturn;
                    sum+=undiscountedReturn;
                    System.out.println(sum/(runNum+1));

                    mcts.resetMCTS();
                }
                System.out.println(Rewrad[epsiode]);
                System.out.println(UDRewrad[epsiode]);




            }

        }



         for (int i=0;i<Rewrad.length;i++){
             System.out.println(Rewrad[i]);
         }
        System.out.println("-----------------------------------------------------------");

        for (int i=0;i<UDRewrad.length;i++){
            System.out.println(UDRewrad[i]);
        }
    }







    public static void main(String[] args) {
        RockSample5_5 rockSample=new RockSample5_5(10);
        RockSample5_5 rockSampleToRun=new RockSample5_5(10);
        RockSample5_5MCTSExperiment experiment1=new RockSample5_5MCTSExperiment(rockSampleToRun,rockSample,"/home/ubuntu/PSR-MCTS-Online-master/PSRConfigs/rocksample5_5", "/home/ubuntu/PSR-MCTS-Online-master/PlanningConfigs/rocksample5_5");
        experiment1.runExperimentUpdate();
    }
}
