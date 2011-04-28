/*******************************************************************************
 * Copyright (c) 2011 Guillaume Hillairet.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Guillaume Hillairet - initial API and implementation
 *******************************************************************************/
package com.emftriple.query;

import org.eclipse.emf.common.util.URI;

public class SparqlString implements ETripleQuery {

	private final String query;

	public SparqlString(String query) {
		this.query = query;
	}
	
	@Override
	public String get() {
		return query;
	}
	
	@Override
	public URI toURI(URI resourceURI) {
		String query = get().replaceAll(" ", "%20").replaceAll("#", "%23");
		return URI.createURI(resourceURI+"&query="+query);
	}
}