/* *********************************************************************** *
 * project: org.matsim.*
 * PlansAnalyzer.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2008 by the members listed in the COPYING,        *
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

package playground.anhorni.locationchoice.analysis;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.matsim.api.basic.v01.population.BasicPlanElement;
import org.matsim.core.api.population.Activity;
import org.matsim.core.api.population.Person;
import org.matsim.core.api.population.Plan;
import org.matsim.core.api.population.Population;
import org.matsim.core.gbl.Gbl;
import org.matsim.core.network.MatsimNetworkReader;
import org.matsim.core.network.NetworkLayer;
import org.matsim.core.population.MatsimPopulationReader;
import org.matsim.core.population.PopulationImpl;
import org.matsim.core.population.PopulationReader;
import org.matsim.core.utils.io.IOUtils;
import org.matsim.core.utils.misc.Counter;


/**
 * @author anhorni
 */

public class ActChainsPlansAnalyzer {

	private Population plans=null;
	private NetworkLayer network=null;
	private final static Logger log = Logger.getLogger(ActChainsPlansAnalyzer.class);


	/**
	 * @param
	 *  - path of the plans file
	 */
	public static void main(final String[] args) {

		if (args.length < 1 || args.length > 1 ) {
			System.out.println("Too few or too many arguments. Exit");
			System.exit(1);
		}
		String plansfilePath = args[0];
		log.info(plansfilePath);

		String networkfilePath="./input/network.xml";

		ActChainsPlansAnalyzer analyzer = new ActChainsPlansAnalyzer();
		analyzer.init(plansfilePath, networkfilePath);
		analyzer.analyze();
	}

	private void init(final String plansfilePath, final String networkfilePath) {

		this.network = (NetworkLayer)Gbl.getWorld().createLayer(NetworkLayer.LAYER_TYPE,null);
		new MatsimNetworkReader(this.network).readFile(networkfilePath);
		log.info("network reading done");

		this.plans=new PopulationImpl(false);
		final PopulationReader plansReader = new MatsimPopulationReader(this.plans);
		plansReader.readFile(plansfilePath);
		log.info("plans reading done");
	}


	private void analyze() {

		try {
			final String header="Person_id\tnumberOfSL";
			final BufferedWriter out = IOUtils.getBufferedWriter("./output/actchainsplananalysis.txt");
			out.write(header);
			out.newLine();


			Iterator<Person> person_iter = this.plans.getPersons().values().iterator();
			Counter counter = new Counter(" person # ");
			while (person_iter.hasNext()) {
				Person person = person_iter.next();
				counter.incCounter();
				Plan selectedPlan = person.getSelectedPlan();
				final List<? extends BasicPlanElement> actslegs = selectedPlan.getPlanElements();

				int countSL = 0;
				for (int j = 0; j < actslegs.size(); j=j+2) {
					final Activity act = (Activity)actslegs.get(j);
					if (act.getType().startsWith("s") || act.getType().startsWith("l")) {
						countSL++;
					}
					else if (act.getType().startsWith("h") || act.getType().startsWith("w")||
							act.getType().startsWith("e")) {
						if (countSL > 0) {
							out.write(person.getId().toString()+"\t"+String.valueOf(countSL)+"\n");
							countSL = 0;
						}
					}
				}
			}
			out.close();
		}
		catch (final IOException e) {
			Gbl.errorMsg(e);
		}
	}
}

