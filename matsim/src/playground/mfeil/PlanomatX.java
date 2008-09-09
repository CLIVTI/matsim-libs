/**
 * 
 */
package playground.mfeil;

import org.matsim.network.NetworkLayer;
import org.matsim.planomat.costestimators.LegTravelTimeEstimator;
import org.matsim.population.Plan;
import org.matsim.population.algorithms.PlanAlgorithm;
import org.matsim.planomat.*;
import org.matsim.router.PlansCalcRouteLandmarks;
import org.matsim.router.util.PreProcessLandmarks;
import org.matsim.router.util.TravelCost;
import org.matsim.router.util.TravelTime;
import org.matsim.population.Act;
import org.matsim.scoring.PlanScorer;
import org.matsim.scoring.ScoringFunctionFactory;
import java.util.ArrayList;

/**
 * @author Matthias Feil
 * PlanomatX will be the class where to implement the Tabu Search. Currently, work focus is on the definition of 
 * the neighbourhood. Changing the order of activities already works. Next is to integrate the TS mechanisms.
 */
public class PlanomatX implements org.matsim.population.algorithms.PlanAlgorithm { 
	
	private final int neighbourhoodSize, maxIterations;
	private final double weightChangeOrder, weightChangeNumber;// weightChangeType;
	private final PlanAlgorithm planomatAlgorithm;
	private final PlansCalcRouteLandmarks router;
	private final PlanScorer scorer;
	
	
	
	//////////////////////////////////////////////////////////////////////
	// Constructor
	//////////////////////////////////////////////////////////////////////
		
	public PlanomatX (LegTravelTimeEstimator legTravelTimeEstimator, NetworkLayer network, TravelCost costCalculator,
			TravelTime timeCalculator, PreProcessLandmarks commonRouterDatafinal, ScoringFunctionFactory factory) {

		planomatAlgorithm = new PlanOptimizeTimes (legTravelTimeEstimator);
		router = new PlansCalcRouteLandmarks (network, commonRouterDatafinal, costCalculator, timeCalculator);
		scorer = new PlanScorer (factory);
		neighbourhoodSize = 5;//TODO @MF: variables to be configured externally, sum must be smaller or equal than 1.0
		weightChangeOrder = 0.8; 
		weightChangeNumber = 0.2;
		//weightChangeType = 0.0;
		maxIterations = 1;
	}
	
		
	//////////////////////////////////////////////////////////////////////
	// run() method
	//////////////////////////////////////////////////////////////////////
	
	
	
	public void run (Plan plan){
		
		//////////////////////////////////////////////////////////////////////
		// New section TS iterations (under construction)
		//////////////////////////////////////////////////////////////////////
		
		
		int currentIteration;
		
		PlanomatXPlan [] neighbourhood = new PlanomatXPlan [neighbourhoodSize+1];
		int neighbourhoodInitialisation;
		for (neighbourhoodInitialisation = 0; neighbourhoodInitialisation < neighbourhood.length; neighbourhoodInitialisation++){
			neighbourhood[neighbourhoodInitialisation] = new PlanomatXPlan (plan.getPerson());
			neighbourhood[neighbourhoodInitialisation].copyPlan(plan);
	
		}
		
		int [] notNewInNeighbourhood = new int [neighbourhoodSize];
		int [] tabuInNeighbourhood = new int [neighbourhoodSize];
		ArrayList<PlanomatXPlan> nonTabuNeighbourhood = new ArrayList<PlanomatXPlan>();
		
		for (currentIteration = 1; currentIteration<maxIterations+1;currentIteration++){
			
			this.createNeighbourhood(neighbourhood);	
			int warningNoNew = this.checkForNoNewSolutions(neighbourhood, notNewInNeighbourhood);
			if (warningNoNew==1) {
				System.out.println("No new solutions availabe for person "+plan.getPerson().getId()+" at iteration "+currentIteration);
				break; 
			}
			int warningTabu = this.checkForTabuSolutions(neighbourhood, notNewInNeighbourhood, tabuInNeighbourhood);
			if (warningTabu==1) {
				System.out.println("No non-tabu solutions availabe for person "+plan.getPerson().getId()+" at iteration "+currentIteration);
				break; 
			}
			
			for (int x=0; x<neighbourhoodSize;x++){
				if(tabuInNeighbourhood[x]==0){
					// Scoring
					
					neighbourhood[x].setScore(scorer.getScore(neighbourhood[x]));
					//double scoreTwo = neighbourhood[x].getScore();
					//System.out.println("Person: "+neighbourhood[x].getPerson().getId()+", Scoring danach: "+scoreTwo);
					nonTabuNeighbourhood.add(0, neighbourhood[x]);
					System.out.println(0+". nonTabuNeighbourhood: "+nonTabuNeighbourhood.get(0).getActsLegs());
				}
			}
			
			java.util.Collections.sort(nonTabuNeighbourhood);
			for (int x = 0; x<nonTabuNeighbourhood.size();x++)System.out.println(x+". nonTabuNeighbourhood: "+nonTabuNeighbourhood.get(x));
			
			if (currentIteration==maxIterations) {
				System.out.println("Tabu Search regularly finished for person "+plan.getPerson().getId()+" at iteration "+currentIteration);
			//	if (nonTabuNeighbourhood.size()>0){
					if (nonTabuNeighbourhood.get(nonTabuNeighbourhood.size()-1).getScore()>plan.getScore()){
						System.out.println("Bin in der Schreib-Schleife.");
						ArrayList<Object> al = plan.getActsLegs();
					
						for (int i = 1; i<al.size()-1;i++){
							al.remove(i);
							al.add(i, nonTabuNeighbourhood.get(nonTabuNeighbourhood.size()-1).getActsLegs().get(i));	
						}
					}
			//	}
			
			}
		}
		
		//Write final solution back to plan if it is better than the base plan.
		//if (nonTabuNeighbourhood.size()>0){
		//	if (nonTabuNeighbourhood.get(nonTabuNeighbourhood.size()-1).getScore()>plan.getScore()){
			
		//		ArrayList<Object> al = plan.getActsLegs();
			
		//		for (int i = 1; i<al.size()-1;i++){
		//			al.remove(i);
		//			al.add(i, nonTabuNeighbourhood.get(nonTabuNeighbourhood.size()-1).getActsLegs().get(i));	
		//		}
		//	}
		//}
		System.out.println("Person: "+plan.getPerson().getId()+", Finaler Plan: "+plan);
	}
				
	//////////////////////////////////////////////////////////////////////
	// Neighbourhood definition (under construction)
	//////////////////////////////////////////////////////////////////////
	
	public void createNeighbourhood (PlanomatXPlan [] neighbourhood) {
		int z;
		int x = 2;
		for (z = 1; z<(int)(neighbourhoodSize*weightChangeOrder); z++){
			x =this.changeOrder(neighbourhood[z], x);
		}
	
		for (z = (int) (neighbourhoodSize*weightChangeOrder); z<((int)(neighbourhoodSize*weightChangeOrder)+(int)(neighbourhoodSize*weightChangeNumber)); z++){
			neighbourhood[z]=this.changeNumber(neighbourhood[z]);
		}
	
		for (z = (int) (neighbourhoodSize*weightChangeOrder)+(int)(neighbourhoodSize*weightChangeNumber); z<neighbourhoodSize; z++){
			neighbourhood[z]=this.changeType(neighbourhood[z]);
		}
		
		//java.util.Arrays.sort(neighbourhood,0,5);
				
	}
	
		
		//for (z = 0; z<neighbourhoodSize; z++) System.out.println(z+". Scoring danach: "+neighbourhood [z].getScore());
		//int size =0;
		//for (Object o : plan.getActsLegs()){
			//if (o.getClass().equals(Act.class)){
				//plan.removeAct(size);
				//size=size+1;
			//}
			//else {
				//plan.removeLeg(size);
				//size=size+1;
			//}
			
		//}
		
		
		//System.out.println("Plan nach Umschreiben: "+plan.getActsLegs());
		
		//plan.copyPlan(neighbourhood[0]);

			
	
	
	public int changeOrder (PlanomatXPlan basePlan, int x){
		
		//System.out.println("x ist: "+x);
		
		ArrayList<Object> actslegs = basePlan.getActsLegs();
		if (actslegs.size()<=5){	//If true the plan has not enough activities to change their order.
			return x;
		}
		else {
			for (int loopCounter = x; loopCounter <= actslegs.size()-4; loopCounter=loopCounter+2){ //Go through the "inner" acts only
				x=x+2;
				
				//Activity swapping
				
				Act act2 = (Act)(actslegs.get(loopCounter));
				Act act4 = (Act)(actslegs.get(loopCounter+4));
				if (act2.getType()!=act4.getType()){
					//System.out.println("Hab was gefunden!");
					System.out.println("Person "+basePlan.getPerson().getId()+", Plan davor: "+actslegs);
					Act act1 = (Act)(actslegs.get(loopCounter-2));
					Act act3 = (Act)(actslegs.get(loopCounter+2));
					if (act1.getType()!=act3.getType()){
						double scoreOne = basePlan.getScore();
						System.out.println("Person: "+basePlan.getPerson().getId()+", Scoring davor: "+scoreOne);
						Act actHelp = new Act ((Act)(actslegs.get(loopCounter)));
						//actHelp.setStartTime(actslegs.get(loopCounter+2).getStartTime());
						actslegs.set(loopCounter, actslegs.get(loopCounter+2));
						actslegs.set(loopCounter+2, actHelp);
					
						System.out.println("Person "+basePlan.getPerson().getId()+", Plan danach: "+basePlan.getActsLegs());
						
						// Routing
					
						this.router.run(basePlan);
						System.out.println("Person "+basePlan.getPerson().getId()+", Neuer Plan :"+actslegs);
						
						//Optimizing the start times
						
						//this.planomatAlgorithm.run (basePlan); //Calling standard Planomat to optimise start times and mode choice
						//System.out.println("Neuer Plan nach Planomat: "+actslegs);
						
						// Scoring
						
						//basePlan.setScore(scorer.getScore(basePlan));
						//double scoreTwo = basePlan.getScore();
						//System.out.println("Person: "+basePlan.getPerson().getId()+", Scoring danach: "+scoreTwo);
						break;
					}
					
				}
			}		
			return x;
		}
	}
	
	public PlanomatXPlan changeNumber (PlanomatXPlan basePlan){
		//System.out.println("Aufruf Methode changeNumber.");
		return basePlan;
	}
	
	public PlanomatXPlan changeType (PlanomatXPlan basePlan){
		//System.out.println("Aufruf Methode changeType.");
		return basePlan;
	}
	
	public int checkForNoNewSolutions (PlanomatXPlan[] neighbourhood, int[] notNewInNeighbourhood){
		int warningInner = 1;
		int warningOuter = 1;
		for (int x=0; x<notNewInNeighbourhood.length;x++){
			if (neighbourhood[x].getActsLegs().toString().equals(neighbourhood[neighbourhood.length-1].getActsLegs().toString())){
				notNewInNeighbourhood[x]=1;
			}
			else {
				notNewInNeighbourhood[x]=0;
				warningInner = 0;
			}
			if (warningInner==0) warningOuter = 0;
		}
		return warningOuter;
	}
	public int checkForTabuSolutions (PlanomatXPlan[] neighbourhood, int[] notNewInNeighbourhood, int[] tabuInNeighbourhood){
		int warningInner = 1;
		int warningOuter = 1;
		for (int x=0; x<tabuInNeighbourhood.length;x++){
			if (notNewInNeighbourhood[x]==1) tabuInNeighbourhood[x] = 1;
			else {
				tabuInNeighbourhood[x] = 0;
				warningInner = 0;
			}
			if (warningInner==0) warningOuter = 0;
		}
		return warningOuter;
	}
}
	
