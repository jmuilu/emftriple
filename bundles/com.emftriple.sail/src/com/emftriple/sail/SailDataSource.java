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
package com.emftriple.sail;

import info.aduna.iteration.CloseableIteration;

import org.openrdf.model.Graph;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.impl.EmptyBindingSet;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.sparql.SPARQLParser;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;

import com.emftriple.datasources.AbstractDataSource;
import com.emftriple.datasources.IDataSource;
import com.emftriple.datasources.IResultSet;
import com.emftriple.sail.util.SailResultSet;

/**
 * 
 * @author <a href="mailto:g.hillairet at gmail.com">Guillaume Hillairet</a>
 * @since 0.6.0
 */
public class SailDataSource 
	extends AbstractDataSource<Graph, Statement, Value, URI, Literal>
	implements IDataSource<Graph, Statement, Value, URI, Literal> {

	public static final Object OPTION_SAIL_OBJECT = "OPTION_SAIL_OBJECT";

	protected SailConnection connection;

	private Sail sail;

	public SailDataSource(Sail sail) {
		this.sail = sail;
		
		try {
			this.connection = sail.getConnection();
		} catch (SailException e) {
			e.printStackTrace();
		}
//		connect();
	}
	
	@Override
	public void add(Iterable<Statement> triples, String namedGraphURI) {
		for (Statement stmt: triples) {
			try {
				sail.getConnection().addStatement(stmt.getSubject(), stmt.getPredicate(), stmt.getObject(), 
						sail.getValueFactory().createURI(namedGraphURI));
			} catch (SailException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void remove(Iterable<Statement> triples, String namedGraphURI) {
		checkIsConnected();
		
		for (Statement stmt: triples)
			try {
				connection.removeStatements(stmt.getSubject(), stmt.getPredicate(), stmt.getObject(),
						new ValueFactoryImpl().createURI(namedGraphURI));
			} catch (SailException e) {
				try {
					connection.rollback();
				} catch (SailException e1) {
					e1.printStackTrace();
				}
			}
	}

	@Override
	public void connect() {
		if (!isConnected()) {
			setConnected(true);

			try {
				if (!sail.isWritable()) {
					sail.shutDown();
				}
			} catch (SailException e) {
				e.printStackTrace();
			}
			try {
				sail.initialize();
			} catch (SailException e) {
				e.printStackTrace();
			}
			try {
				connection = sail.getConnection();
			} catch (SailException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void disconnect() {
		setConnected(false);

		try {
			connection.close();
			sail.shutDown();
		} catch (SailException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean askQuery(String query, String graph) {
//		return askQuery(query);
		throw new UnsupportedOperationException();
	}

	@Override
	public IResultSet<Value, URI, Literal> selectQuery(String query, String graph) {
		checkIsConnected();
		SPARQLParser parser = new SPARQLParser();
		CloseableIteration<? extends BindingSet, QueryEvaluationException> sparqlResults = null;

		ParsedQuery parsedQuery = null;
		try {
			parsedQuery = parser.parseQuery(query, null);
		} catch (MalformedQueryException e) {
			e.printStackTrace();
		}
		try {
			sparqlResults = sail.getConnection().evaluate(
					parsedQuery.getTupleExpr(), 
					parsedQuery.getDataset(), 
					new EmptyBindingSet(), 
					false);
		} catch (SailException e) {
			e.printStackTrace();
		}

		return new SailResultSet(sparqlResults);
	}

	@Override
	public boolean supportsTransaction() {
		return true;
	}

	protected boolean containsGraph(String graph) {
		try {
			for (CloseableIteration<? extends Resource, SailException> res = connection.getContextIDs(); res.hasNext();) {
				Resource r = res.next();
				if (r.stringValue().equals(graph.toString()))
					return true;
			}
		} catch (SailException e) {
			e.printStackTrace();
		}

		return false;
	}

	private final void checkIsConnected() {
		try {
			if (connection == null || !connection.isOpen() || !isConnected()) {
				connect();
			}
		} catch (SailException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean supportsNamedGraph() {
		return true;
	}

	@Override
	public boolean isMutable() {
		return true;
	}

	@Override
	public boolean supportsUpdateQuery() {
		return false;
	}

	@Override
	public void constructQuery(String aQuery, String graphURI, Graph aGraph) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void describeQuery(String aQuery, String graphURI, Graph aGraph) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Graph getGraph(String graphURI) {
		
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void delete(String graphURI) {
		try {
			connection.clear();
		} catch (SailException e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (SailException e1) {
				e1.printStackTrace();
			}
		}
	}

	@Override
	public Graph constructQuery(String query, String graphURI) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Graph describeQuery(String query, String graphURI) {
		// TODO Auto-generated method stub
		return null;
	}
}
