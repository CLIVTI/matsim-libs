/*
 * *********************************************************************** *
 * project: org.matsim.*
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2020 by the members listed in the COPYING,        *
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
 * *********************************************************************** *
 */

package org.matsim.contrib.drt.optimizer.rebalancing.demandestimator;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.events.PersonDepartureEvent;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.drt.analysis.zonal.DrtZonalSystem;
import org.matsim.contrib.drt.analysis.zonal.DrtZone;
import org.matsim.contrib.drt.optimizer.rebalancing.RebalancingParams;
import org.matsim.contrib.drt.run.DrtConfigGroup;
import org.matsim.testcases.fakes.FakeLink;

/**
 * @author michalm (Michal Maciejewski)
 */
public class PreviousIterationDRTDemandEstimatorTest {

	private static final String DRT_SPEEDUP = "drt_speedup";

	private final Link link1 = new FakeLink(Id.createLinkId("link_1"));
	private final Link link2 = new FakeLink(Id.createLinkId("link_2"));

	private final DrtZone zone1 = DrtZone.createDummyZone("zone_1", List.of(link1), new Coord());
	private final DrtZone zone2 = DrtZone.createDummyZone("zone_2", List.of(link2), new Coord());
	private final DrtZonalSystem zonalSystem = new DrtZonalSystem(Map.of(zone1.getId(), zone1, zone2.getId(), zone2));

	@Test
	public void noDepartures() {
		PreviousIterationDRTDemandEstimator estimator = createEstimator(1800);

		//no events in previous iterations
		estimator.reset(1);

		assertDemand(estimator, 0, zone1, 0);
		assertDemand(estimator, 2000, zone1, 0);
		assertDemand(estimator, 4000, zone1, 0);
		assertDemand(estimator, 0, zone2, 0);
		assertDemand(estimator, 2000, zone2, 0);
		assertDemand(estimator, 4000, zone2, 0);
	}

	@Test
	public void drtDepartures() {
		PreviousIterationDRTDemandEstimator estimator = createEstimator(1800);

		//time bin 0-1800
		estimator.handleEvent(departureEvent(100, link1, TransportMode.drt));
		estimator.handleEvent(departureEvent(200, link1, DRT_SPEEDUP));
		estimator.handleEvent(departureEvent(500, link2, TransportMode.drt));
		estimator.handleEvent(departureEvent(1500, link1, TransportMode.drt));
		//time bin 1800-3600
		estimator.handleEvent(departureEvent(2500, link1, DRT_SPEEDUP));
		//time bin 3600-5400
		estimator.handleEvent(departureEvent(4000, link2, TransportMode.drt));
		//time bin 5400-7200
		estimator.handleEvent(departureEvent(7000, link1, TransportMode.drt));
		estimator.handleEvent(departureEvent(7100, link2, DRT_SPEEDUP));
		estimator.reset(1);

		//time bin 0-1800
		assertDemand(estimator, 0, zone1, 3);
		assertDemand(estimator, 0, zone2, 1);
		//time bin 1800-3600
		assertDemand(estimator, 1800, zone1, 1);
		assertDemand(estimator, 1800, zone2, 0);
		//time bin 3600-5400
		assertDemand(estimator, 3600, zone1, 0);
		assertDemand(estimator, 3600, zone2, 1);
		//time bin 5400-7200
		assertDemand(estimator, 5400, zone1, 1);
		assertDemand(estimator, 5400, zone2, 1);
		//time bin 7200-9000
		assertDemand(estimator, 7200, zone1, 0);
		assertDemand(estimator, 7200, zone2, 0);
	}

	@Test
	public void nonDrtDepartures() {
		PreviousIterationDRTDemandEstimator estimator = createEstimator(1800);

		estimator.handleEvent(departureEvent(100, link1, "mode X"));
		estimator.handleEvent(departureEvent(200, link2, TransportMode.car));
		estimator.reset(1);

		assertDemand(estimator, 0, zone1, 0);
		assertDemand(estimator, 0, zone2, 0);
	}

	@Test
	public void currentCountsAreCopiedToPreviousAfterReset() {
		PreviousIterationDRTDemandEstimator estimator = createEstimator(1800);

		estimator.handleEvent(departureEvent(100, link1, DRT_SPEEDUP));
		estimator.handleEvent(departureEvent(200, link2, TransportMode.drt));

		assertDemand(estimator, 0, zone1, 0);
		assertDemand(estimator, 0, zone2, 0);

		estimator.reset(1);

		assertDemand(estimator, 0, zone1, 1);
		assertDemand(estimator, 0, zone2, 1);
	}

	@Test
	public void timeBinsAreRespected() {
		PreviousIterationDRTDemandEstimator estimator = createEstimator(1800);

		estimator.handleEvent(departureEvent(100, link1, DRT_SPEEDUP));
		estimator.handleEvent(departureEvent(2200, link2, TransportMode.drt));
		estimator.reset(1);

		assertDemand(estimator, 0, zone1, 1);
		assertDemand(estimator, 1799, zone1, 1);
		assertDemand(estimator, 1800, zone1, 0);

		assertDemand(estimator, 1799, zone2, 0);
		assertDemand(estimator, 1800, zone2, 1);
		assertDemand(estimator, 3599, zone2, 1);
		assertDemand(estimator, 3600, zone2, 0);
	}

	@Test
	public void noTimeLimitIsImposed() {
		PreviousIterationDRTDemandEstimator estimator = createEstimator(1800);

		estimator.handleEvent(departureEvent(10000000, link1, DRT_SPEEDUP));
		estimator.reset(1);

		assertDemand(estimator, 10000000, zone1, 1);
	}

	private PreviousIterationDRTDemandEstimator createEstimator(int timeBinSize) {
		RebalancingParams rebalancingParams = new RebalancingParams();
		rebalancingParams.setInterval(timeBinSize);

		DrtConfigGroup drtConfigGroup = new DrtConfigGroup();
		drtConfigGroup.setDrtSpeedUpMode(DRT_SPEEDUP);
		drtConfigGroup.addParameterSet(rebalancingParams);

		return new PreviousIterationDRTDemandEstimator(zonalSystem, drtConfigGroup);
	}

	private PersonDepartureEvent departureEvent(double time, Link link, String mode) {
		return new PersonDepartureEvent(time, null, link.getId(), mode);
	}

	private void assertDemand(PreviousIterationDRTDemandEstimator estimator, double time, DrtZone zone,
			double expectedDemand) {
		assertThat(estimator.getExpectedDemandForTimeBin(time).applyAsDouble(zone)).isEqualTo(expectedDemand);
	}
}
