package cpsr.planning.mcts;


import cpsr.environment.TrainingDataSet;
import cpsr.environment.components.Action;
import cpsr.environment.components.ActionObservation;
import cpsr.environment.components.Observation;
import cpsr.environment.simulation.ASimulator;
import cpsr.environment.simulation.domains.PoSys;
import cpsr.model.*;
import cpsr.stats.PSRObserver;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Random;


public class PoSysMCTSExperiment {
    public static final String DEF_DISCOUNT_FACTOR = "0.9",DEF_MIN_SING_VAL = "0.000000001";

    private ASimulator simulator;
    private ASimulator simulatorToRun;

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


    public PoSysMCTSExperiment(ASimulator pSimulatorToRun, ASimulator pSimulator, String pPSRConfigFile, String pPlanningConfigFile) {
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





    public void runExperiment() {
        APSR psrModel;
        String psrType;
        psrObs = new PSRObserver();
        simulator.setMaxRunLength(maxRunLength);

        double Rewrad[]=new double[220];

        TrainingDataSet trainDataSave = new TrainingDataSet(maxTestLen);

        trainDataSave.newDataBatch(10000);
        simulator.simulateTrainingRuns(1, trainDataSave);

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


            MCTS mcts=new MCTS(psrModel,1000.0,100,1,20,false,simulator);
            mcts.InitFastUCB(1000.0);



            for (int epsiode=0;epsiode<220;epsiode++){

                System.out.println("------------------------------------"+epsiode+"------------------------------");

                psrModel.build();

                int n=100;



                for (int runNum=0;runNum<n;runNum++) {
                    double undiscountedReturn = 0.0;
                    double discountedReturn = 0.0;


                    double discountTemp = 1.0;


                    boolean terminal=false;

                    simulatorToRun.resetToStar();

                    ArrayList<ActionObservation> runActObs = new ArrayList<ActionObservation>();
                    ArrayList<Double> runRewards = new ArrayList<Double>();

                    psrModel.resetPvToInitial();

                    for (int t = 0; t < 20; t++) {
                        //System.out.println("step"+"------------------------------------"+t+"------------------------------");
                        double reward;


                        double epsion=(199.0-epsiode)/235;

                        int action;

                        if (epsiode<10){
                            action=simulatorToRun.selectRandom(100);

                        } else{

                            action = mcts.SelectActionPSR();


                            if (rand.nextDouble() < epsion) {
                                action = simulatorToRun.selectRandom(action);
                            }
                        }

                        int observation = simulatorToRun.Execution(action);
                        reward = simulatorToRun.getReward(action, observation);
                        terminal = simulatorToRun.CheckTerminal(observation);


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
                        if (epsiode>=10) {
                            mcts.UpdatePSR(action, observation);
                        }
                    }

                    if (runNum==0) {
                        trainData.addRunData(runActObs, runRewards);//new training data


                    }
                    Rewrad[epsiode]+=discountedReturn;
                    mcts.resetMCTS();
                }
                System.out.println(Rewrad[epsiode]);




            }

        }


        for (int i=0;i<Rewrad.length;i++){
            System.out.println(Rewrad[i]);
        }
    }

    public static void main(String[] args) {
        PoSys poSys=new PoSys(10);
        PoSys poSysToRun=new PoSys(10);
        PoSysMCTSExperiment experiment1=new PoSysMCTSExperiment(poSysToRun,poSys,"/home/ubuntu/PSR-MCTS-Online-master/PSRConfigs/posys_3", "/home/ubuntu/PSR-MCTS-Online-master/PlanningConfigs/posys_3");
        experiment1.runExperiment();
    }
}


