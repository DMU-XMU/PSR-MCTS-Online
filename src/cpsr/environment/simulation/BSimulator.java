package cpsr.environment.simulation;


import cpsr.environment.TrainingDataSet;
import cpsr.environment.components.Action;
import cpsr.environment.components.ActionObservation;
import cpsr.environment.components.Observation;
import cpsr.environment.simulation.domains.Coord;
import cpsr.planning.RandomPlanner;

import java.util.ArrayList;
import java.util.Random;
import java.util.Vector;

public abstract class BSimulator implements ISimulator
{

	//use arbitrary maximum run length limit.
	//this simply to prevent infinite loops in simulator
	//and also just practical since runs should not be any longer than this limit
	//if they are to be used with PSRs.
	public int DEFAULT_RUN_LEN_LIMIT = 100000000;

	protected Random rando;
	protected long seed;
	protected int maxRunLength;

	protected BSimulator(long seed, int maxRunLength)
	{
		if(maxRunLength > 1000000)
		{
			throw new IllegalArgumentException("Cannot have max run length greater than: " + DEFAULT_RUN_LEN_LIMIT);
		}

		this.rando = new Random(seed);
		this.seed = seed;
		this.maxRunLength = maxRunLength;
	}

	protected BSimulator(long seed)
	{
		this.rando = new Random(seed);
		this.maxRunLength = DEFAULT_RUN_LEN_LIMIT;
	}	





	
	@Override
	public void simulateTrainingRuns(int runs, TrainingDataSet trainData)
	{
		int currRun;
		double currReward;
		Observation currObs;
		Action currAction;

		RandomPlanner randPlanner = null;

		randPlanner = getRandomPlanner(rando.nextInt());


		for(currRun = 0; currRun < runs; currRun++)
		{
			currReward = 0.0;
			ArrayList<ActionObservation> runActObs = new ArrayList<ActionObservation>();
			ArrayList<Double> runRewards = new ArrayList<Double>();

			initRun();


			int counter = 0;
			while(!inTerminalState()  && counter < maxRunLength)
			{

				if (getPreferAction()==-1){
					currAction = randPlanner.getAction();
				}else {
					currAction=new Action(getPreferAction());
				}



				executeAction(currAction);
				if (!checkLegal()){
					continue;
				}
				currReward = getCurrentReward();
				currObs = getCurrentObservation();

				currObs.setMaxID(getNumberOfObservations()-1);
				currAction.setMaxID(getNumberOfActions()-1);
				
				runActObs.add(new ActionObservation(currAction, currObs));
				runRewards.add(currReward);
				
				trainData.addRunDataForTraining(runActObs);
				

					
				counter++;
			}
			trainData.addRunData(runActObs, runRewards);
		}
	}
		

	@Override
	public void setMaxRunLength(int pMaxRunLength) 
	{
		maxRunLength = pMaxRunLength;
	}
	
	@Override
	public RandomPlanner getRandomPlanner(int pSeed)
	{
		return new RandomPlanner(rando.nextInt(),0,getNumberOfActions());
	}

	protected abstract void initRun();

	public void resetToStar(){
		initRun();
	}

	public int Execution(int act){
		executeAction(new Action(act));
		return getCurrentObservation().getID();
	}

	public int selectRandom(int actionExcept){
		int action=-1;
		do {
			action=rando.nextInt(getNumAct());
		}while (action==actionExcept);

		return action;
	}

	public int selectLegalRandom(Vector<Integer> legalAction,int actionExcept){
		int action=-1;
		do{
			int rantemp=rando.nextInt(legalAction.size());
			action=legalAction.get(rantemp);
		}while (action==actionExcept);

		return action;
	}

	public abstract boolean checkLegal();

	protected abstract int getNumberOfActions();
	
	protected abstract int getNumberOfObservations();
	
	protected abstract boolean inTerminalState();
	
	protected abstract void executeAction(Action act);

	protected abstract double getCurrentReward();
	
	protected abstract Observation getCurrentObservation();

	public int getNumAct(){
		System.out.println("abstract");
		return 0;
	}
	public int getNumObs(){
		System.out.println("abstract");
		return 0;
	}
	public boolean CheckTerminal(int obs){
		System.out.println("abstract");
		return false;
	}
	public double getReward(int action,int observation){
		System.out.println("abstract");
		return 0;
	}
	public abstract Vector<Integer> getLegalAction();
	public abstract void CopyState(BSimulator simulator);
	public double getDiscount(){
		return 0.95;
	}

	public void getStateToUse() {
		System.out.println("Bsimulator");
	}

	public void StateToUseuUpdate(int act) {
		System.out.println("Bsimulator");
	}

	public int getPreferAction() {
		//System.out.println("Bsimulator");
		return -1;
	}



	public boolean[] isCollected;
	public Coord agentPos;
	public Coord agentPosCopy=new Coord(0,2);
	public int Size;


}
