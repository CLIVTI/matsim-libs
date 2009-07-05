/* *********************************************************************** *
 * project: org.matsim.*
 * RunRetailerGA.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2009 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

package playground.jjoubert.RetailerGA;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import playground.jjoubert.DateString;

public class RunRetailerGA {

	
	
	public static void main(String[] args) {
		
		int genomeLength = 100;
		int populationSize = 100;
		int numberOfGenerations = 1000;
		double elites = 0.10;
		double mutants = 0.05;
		/*
		 * Crossover types implemented:
		 * 	1 - TODO Enhanced Edge Recombination
		 *  2 - Merged Crossover
		 *  3 - Partially Matched Crossover
		 */
		int crossoverType = 2;
		ArrayList<ArrayList<Double>> solutionProgress = new ArrayList<ArrayList<Double>>(numberOfGenerations);		
		
		/*
		 * Just for me to create a fictitious  solution
		 */
		MyPermutator p = new MyPermutator();
		ArrayList<Integer> first = p.permutate(genomeLength);
		
		MyFitnessFunction ff = new MyFitnessFunction(false, genomeLength);
		RetailerGA ga = new RetailerGA(populationSize, genomeLength, ff, first);
		ga.generateFirstGeneration();
		solutionProgress.add(ga.getStats());
		long tNow = 0;
		long total = 0;
		for(int i = 0; i < numberOfGenerations; i++){
			tNow = System.currentTimeMillis();
			ga.evolve(elites, mutants, crossoverType, ff.getPrecedenceVector());
			total += System.currentTimeMillis() - tNow;
			solutionProgress.add(ga.getStats());
		}
		double avgTime = ((double)total) / ((double)numberOfGenerations);
		
		/*
		 * Print out the last generation to the console.
		 */
//		String out = ga.toString();
//		System.out.printf(out);
		System.out.printf("Statistics for crossover type %d:\n", crossoverType);
		System.out.printf("\t                   Genome length:  %d\n", genomeLength);
		System.out.printf("\t                 Population size:  %d\n", populationSize);
		System.out.printf("\t           Number of generations:  %d\n", numberOfGenerations);
		System.out.printf("\t               Incumbent fitness:  %6.2f\n", ga.getIncumbent().getFitness());
		System.out.printf("\tAverage time per generation (ms):  %6.2f\n", avgTime);
		
		/*
		 * Print out the solution progress to a file for R-graph.
		 */
		DateString ds = new DateString();
		String fileName = "/Users/johanwjoubert/R-Source/Input/GA-Progress-" + ds.toString() + ".txt";		
		writeSolutionProgressToFile(solutionProgress, fileName);
	}

	/**
	 * Method to write the solution progress to file. Four fields are written for each generation:
	 * <ul>
	 * <li> Iteration number;
	 * <li> Best solution fitness (incumbent);
	 * <li> Average fitness of whole population; and
	 * <li> Worst fitness.
	 * </ul>
	 * @param solutionProgress the <code>ArrayList</code> of the generations' statistics; 
	 * @param fileName the <code>String</code> filename to which statistics are written.
	 */
	private static void writeSolutionProgressToFile(
			ArrayList<ArrayList<Double>> solutionProgress,
			String fileName) {
		try {
			BufferedWriter output = new BufferedWriter(new FileWriter(new File(fileName)));
			try{
				output.write("Iteration,Best,Average,Worst");
				output.newLine();
				int iteration = 0;
				for (ArrayList<Double> solution : solutionProgress) {
					output.write(String.valueOf(iteration));
					output.write(",");
					output.write(String.valueOf(solution.get(0)));
					output.write(",");
					output.write(String.valueOf(solution.get(1)));
					output.write(",");
					output.write(String.valueOf(solution.get(2)));
					output.newLine();
					iteration++;
				}				
			} finally{
				output.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}	

}
