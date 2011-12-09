/* *********************************************************************** *
 * project: org.matsim.*
 * OnlyTimeDependentTravelCostCalculator.java
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

package org.matsim.core.router.costcalculators;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.router.util.PersonalizableTravelCost;
import org.matsim.core.router.util.PersonalizableTravelTime;
import org.matsim.core.router.util.TravelMinCost;
import org.matsim.core.trafficmonitoring.FreeSpeedTravelTimeCalculator;
import org.matsim.core.utils.misc.Time;

/**
 *  A Travel Cost Calculator that uses the travel times as travel costs.
 *  
 * @author cdobler
 */
public class OnlyTimeDependentTravelCostCalculator implements TravelMinCost, PersonalizableTravelCost {

	private static final Logger log = Logger.getLogger(OnlyTimeDependentTravelCostCalculator.class);
	
	protected final PersonalizableTravelTime travelTime;

	public OnlyTimeDependentTravelCostCalculator(final PersonalizableTravelTime travelTime) {
		if (travelTime == null) {
			log.warn("TimeCalculator is null so FreeSpeedTravelTimes will be calculated!");
			this.travelTime = new FreeSpeedTravelTimeCalculator();
		} else this.travelTime = travelTime;
	}

	@Override
	public double getLinkGeneralizedTravelCost(final Link link, final double time) {
		return this.travelTime.getLinkTravelTime(link, time);
	}

	@Override
	public double getLinkMinimumTravelCost(final Link link) {
		return this.travelTime.getLinkTravelTime(link, Time.UNDEFINED_TIME);
	}
	
	@Override
	public void setPerson(Person person) {
		travelTime.setPerson(person);
	}
}