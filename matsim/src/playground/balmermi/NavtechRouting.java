/* *********************************************************************** *
 * project: org.matsim.*
 * NavtechRouting.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2007 by the members listed in the COPYING,        *
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

package playground.balmermi;

import org.matsim.core.api.population.Population;
import org.matsim.core.gbl.Gbl;
import org.matsim.core.network.MatsimNetworkReader;
import org.matsim.core.network.NetworkLayer;
import org.matsim.core.population.MatsimPopulationReader;
import org.matsim.core.population.PopulationImpl;
import org.matsim.core.population.PopulationReader;
import org.matsim.core.population.PopulationWriter;
import org.matsim.population.algorithms.XY2Links;
import org.matsim.world.MatsimWorldReader;

public class NavtechRouting {

	//////////////////////////////////////////////////////////////////////
	// xy2links
	//////////////////////////////////////////////////////////////////////

	public static void xy2links(final String[] args) {

		System.out.println("RUN: xy2links");

		Gbl.createConfig(args);

		System.out.println("  reading world xml file... ");
		final MatsimWorldReader worldReader = new MatsimWorldReader(Gbl.getWorld());
		worldReader.readFile(Gbl.getConfig().world().getInputFile());
		System.out.println("  done.");

		System.out.println("  reading the network...");
		NetworkLayer network = null;
		network = (NetworkLayer)Gbl.getWorld().createLayer(NetworkLayer.LAYER_TYPE,null);
		new MatsimNetworkReader(network).readFile(Gbl.getConfig().network().getInputFile());
		System.out.println("  done.");

		System.out.println("  setting up plans objects...");
		Population plans = new PopulationImpl(PopulationImpl.USE_STREAMING);
		PopulationWriter plansWriter = new PopulationWriter(plans);
		PopulationReader plansReader = new MatsimPopulationReader(plans);
		System.out.println("  done.");

		System.out.println("  adding plans algorithm... ");
		plans.addAlgorithm(new XY2Links(network));
//		plans.addAlgorithm(new PlansCalcRoute(network, new FreespeedTravelTimeCost()));
//		plans.addAlgorithm(new PlansCalcTravelDistance());
//		plans.addAlgorithm(new PlansWriteTableForLoechl());
		System.out.println("  done.");

		System.out.println("  reading, processing, writing plans...");
		plans.addAlgorithm(plansWriter);
		plansReader.readFile(Gbl.getConfig().plans().getInputFile());
		plans.printPlansCount();
		plansWriter.write();
		System.out.println("  done.");

		System.out.println("RUN: xy2links finished.");
		System.out.println();
	}

	//////////////////////////////////////////////////////////////////////
	// main
	//////////////////////////////////////////////////////////////////////

	public static void main(final String[] args) {
		Gbl.startMeasurement();
		Gbl.printElapsedTime();

		xy2links(args);

		Gbl.printElapsedTime();
	}
}
