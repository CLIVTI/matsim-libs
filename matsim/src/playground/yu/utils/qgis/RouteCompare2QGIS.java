/**
 *
 */
package playground.yu.utils.qgis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.geotools.factory.FactoryRegistryException;
import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.DefaultAttributeTypeFactory;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureTypeBuilder;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.SchemaException;
import org.geotools.referencing.CRS;
import org.jfree.util.Log;
import org.matsim.api.basic.v01.Id;
import org.matsim.core.api.population.Population;
import org.matsim.core.gbl.Gbl;
import org.matsim.core.network.MatsimNetworkReader;
import org.matsim.core.network.NetworkLayer;
import org.matsim.core.population.MatsimPopulationReader;
import org.matsim.core.population.PopulationImpl;
import org.matsim.core.utils.gis.ShapeFileWriter;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import playground.yu.analysis.RouteSummaryTest.RouteSummary;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

/**
 * @author yu
 * 
 */
public class RouteCompare2QGIS extends Route2QGIS {
	private final Map<List<Id>, Integer> routeCountersB;

	public RouteCompare2QGIS(Population population,
			final CoordinateReferenceSystem crs, final String outputDir,
			final NetworkLayer network,
			final Map<List<Id>, Integer> routeCountersA,
			final Map<List<Id>, Integer> routeCountersB) {
		super(population, crs, outputDir, network, routeCountersA);
		this.routeCountersB = routeCountersB;
	}

	@Override
	protected void initFeatureType() {
		AttributeType[] attrRoute = new AttributeType[5];
		attrRoute[0] = DefaultAttributeTypeFactory.newAttributeType(
				"MultiPolygon", MultiPolygon.class, true, null, null, this
						.getCrs());
		attrRoute[1] = AttributeTypeFactory.newAttributeType("ROUTEFLOWA",
				Double.class);
		attrRoute[2] = AttributeTypeFactory.newAttributeType("ROUTEFLOWB",
				Double.class);
		attrRoute[3] = AttributeTypeFactory.newAttributeType("DIFF_B-A",
				Double.class);
		attrRoute[4] = AttributeTypeFactory.newAttributeType("DIFF_SIGN",
				Double.class);
		try {
			this.setFeatureTypeRoute(FeatureTypeBuilder.newFeatureType(
					attrRoute, "route"));
		} catch (FactoryRegistryException e) {
			e.printStackTrace();
		} catch (SchemaException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected Feature getRouteFeature(final List<Id> routeLinkIds) {
		Integer routeFlowsA = this.routeCounters.get(routeLinkIds);
		Integer routeFlowsB = this.routeCountersB.get(routeLinkIds);
		if (routeFlowsA != null || routeFlowsB != null) {
			if (routeFlowsA == null)
				routeFlowsA = new Integer(0);
			if (routeFlowsB == null)
				routeFlowsB = new Integer(0);
			if ((routeFlowsA.intValue() > 1 || routeFlowsB.intValue() > 1)
					&& (routeFlowsA.intValue() != routeFlowsB.intValue())) {
				Coordinate[] coordinates = new Coordinate[(routeLinkIds.size() + 1) * 2 + 1];
				Double diff = routeFlowsB.doubleValue()
						- routeFlowsA.doubleValue();
				Double absDiff = Math.abs(diff);
				double width = 10.0 * Math.min(250.0, absDiff);
				coordinates = calculateCoordinates(coordinates, width,
						routeLinkIds);
				try {
					return getFeatureTypeRoute()
							.create(
									new Object[] {
											new MultiPolygon(
													new Polygon[] { new Polygon(
															getGeofac()
																	.createLinearRing(
																			coordinates),
															null, getGeofac()) },
													this.getGeofac()),
											new Double(routeFlowsA
													.doubleValue()),
											new Double(routeFlowsB
													.doubleValue()), absDiff,
											new Double(diff / absDiff) });
				} catch (IllegalAttributeException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	@Override
	protected void writeRoutes() throws IOException {
		ArrayList<Feature> fts = new ArrayList<Feature>();
		Set<List<Id>> totalKeys = new HashSet<List<Id>>();
		totalKeys.addAll(this.routeCounters.keySet());
		totalKeys.addAll(this.routeCountersB.keySet());
		for (List<Id> routeLinkIds : totalKeys) {
			Feature ft = getRouteFeature(routeLinkIds);
			if (ft != null)
				fts.add(ft);
		}
		ShapeFileWriter.writeGeometries(fts, getOutputDir() + "/routes.shp");
	}

	/**
	 * @param args
	 */
	public static void main(final String[] args) {
		final String networkFilename = args[0];
		final String populationFilenameA = args[1];
		final String populationFilenameB = args[2];
		final String outputDir = args[3];

		Gbl.createConfig(null);

		NetworkLayer network = new NetworkLayer();
		new MatsimNetworkReader(network).readFile(networkFilename);
		// ------------------------RouteSummaryA--------------------------------
		Population populationA = new PopulationImpl();

		RouteSummary rsA = new RouteSummary(outputDir + "/routeCompareA.txt.gz");

		System.out.println("-->reading plansfile: " + populationFilenameA);
		new MatsimPopulationReader(populationA, network)
				.readFile(populationFilenameA);

		rsA.run(populationA);
		rsA.write();
		rsA.end();
		// ------------------------RouteSummaryB---------------------------------
		Population populationB = new PopulationImpl();

		RouteSummary rsB = new RouteSummary(outputDir + "/routeCompareB.txt.gz");

		System.out.println("-->reading plansfile: " + populationFilenameB);
		new MatsimPopulationReader(populationB, network)
				.readFile(populationFilenameB);

		rsB.run(populationB);
		rsB.write();
		rsB.end();
		// ----------------------------------------------------------------------
		CoordinateReferenceSystem crs;
		try {
			crs = CRS.parseWKT(ch1903);
			RouteCompare2QGIS r2q = new RouteCompare2QGIS(null, crs, outputDir,
					network, rsA.getRouteCounters(), rsB.getRouteCounters());
			r2q.setWriteActs(false);
			r2q.setWriteLegs(false);
			r2q.setWriteRoutes(true);
			r2q.write();
		} catch (FactoryException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			Log.error(e.getMessage(), e);
		}
	}

}
