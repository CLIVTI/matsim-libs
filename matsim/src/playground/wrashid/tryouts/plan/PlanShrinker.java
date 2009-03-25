package playground.wrashid.tryouts.plan;

import org.matsim.core.api.population.Population;
import org.matsim.core.config.Config;
import org.matsim.core.gbl.Gbl;
import org.matsim.core.network.MatsimNetworkReader;
import org.matsim.core.network.NetworkLayer;
import org.matsim.core.population.MatsimPopulationReader;
import org.matsim.core.population.PopulationImpl;
import org.matsim.core.population.PopulationReader;
import org.matsim.core.population.PopulationWriter;

public class PlanShrinker {

	/**
	 * @param args
	 *
	 */
	public static void main(final String[] args) {
		// TODO Auto-generated method stub


		// input plan defined in config file
		// output plan defined in last line
		// percentage defined in last line

		String outputPath="C:/data/workspaceYourKit6/matsim/output/";
		String configFile = outputPath +  "config.xml";

		Config config = Gbl.createConfig(new String[] {configFile});



		System.out.println("  reading the network...");
		NetworkLayer network = null;
		network = (NetworkLayer)Gbl.getWorld().createLayer(NetworkLayer.LAYER_TYPE, null);
		new MatsimNetworkReader(network).readFile(config.network().getInputFile());
		System.out.println("  done.");

		Population population = new PopulationImpl(PopulationImpl.USE_STREAMING);

		System.out.println("reading plans xml file... ");
		PopulationReader plansReader = new MatsimPopulationReader(population);
		plansReader.readFile(Gbl.getConfig().plans().getInputFile());
		population.printPlansCount();

		new PopulationWriter(population,outputPath+"plans1.xml","v4",0.1).write();

	}

}
