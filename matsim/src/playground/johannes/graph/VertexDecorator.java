/* *********************************************************************** *
 * project: org.matsim.*
 * AbstractVertexDecorator.java
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

/**
 * 
 */
package playground.johannes.graph;


/**
 * @author illenberger
 *
 */
public class VertexDecorator<V extends Vertex> extends SparseVertex {

	private V delegate;
	
	protected VertexDecorator(V delegate) {
		super();
		this.delegate = delegate;
	}
	
	public V getDelegate() {
		return delegate;
	}
}
