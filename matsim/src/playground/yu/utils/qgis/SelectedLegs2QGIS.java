/**
 * 
 */
package playground.yu.utils.qgis;

import java.io.IOException;
import java.util.ArrayList;

import org.geotools.feature.Feature;
import org.jfree.util.Log;
import org.matsim.core.api.population.Leg;
import org.matsim.core.api.population.Plan;
import org.matsim.core.api.population.Population;
import org.matsim.core.basic.v01.BasicPlanImpl.LegIterator;
import org.matsim.core.gbl.Gbl;
import org.matsim.core.network.MatsimNetworkReader;
import org.matsim.core.network.NetworkLayer;
import org.matsim.core.population.MatsimPopulationReader;
import org.matsim.core.population.PopulationImpl;
import org.matsim.core.utils.geometry.geotools.MGC;
import org.matsim.core.utils.gis.ShapeFileWriter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * @author yu
 * 
 */
public class SelectedLegs2QGIS extends SelectedPlans2ESRIShape {

	/**
	 * @param population
	 * @param crs
	 * @param outputDir
	 */
	public SelectedLegs2QGIS(Population population,
			CoordinateReferenceSystem crs, String outputDir) {
		super(population, crs, outputDir);
	}

	@Override
	protected void writeLegs() throws IOException {
		String outputFile = this.getOutputDir() + "/legs.shp";
		ArrayList<Feature> fts = new ArrayList<Feature>();
		for (Plan plan : this.getOutputSamplePlans()) {
			if (plan.getFirstActivity().getEndTime() == 21600.0) {
				String id = plan.getPerson().getId().toString();
				LegIterator iter = plan.getIteratorLeg();
				if (iter.hasNext()) {
					Leg leg = (Leg) iter.next();
					if (leg.getRoute().getDistance() > 0) {
						fts.add(getLegFeature(leg, id));
					}
				}
			}

		}
		ShapeFileWriter.writeGeometries(fts, outputFile);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		final String populationFilename = "../runs_SVN/run674/it.1000/1000.plans.xml.gz";
		final String networkFilename = "../schweiz-ivtch-SVN/baseCase/network/ivtch-osm.xml";
		final String outputDir = "../runs_SVN/run674/it.1000/1000.analysis/";

		Gbl.createConfig(null);
		NetworkLayer network = new NetworkLayer();
		new MatsimNetworkReader(network).readFile(networkFilename);

		Population population = new PopulationImpl();
		new MatsimPopulationReader(population, network)
				.readFile(populationFilename);

		CoordinateReferenceSystem crs = MGC.getCRS("DHDN_GK4");
		SelectedPlans2ESRIShape sp = new SelectedPlans2ESRIShape(population,
				crs, outputDir);
		sp.setOutputSample(1);
		sp.setActBlurFactor(100);
		sp.setLegBlurFactor(100);
		sp.setWriteActs(false);
		sp.setWriteLegs(true);

		try {
			sp.write();
		} catch (IOException e) {
			Log.error(e.getMessage(), e);
		}
	}

}
