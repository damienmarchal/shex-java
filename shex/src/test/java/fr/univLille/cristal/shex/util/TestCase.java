/*******************************************************************************
 * Copyright (C) 2018 Université de Lille - Inria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.univLille.cristal.shex.util;

import java.util.Set;

import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.model.vocabulary.RDFS;

import fr.univLille.cristal.shex.schema.Label;

public 	class TestCase {
	private static final RDFFactory RDF_FACTORY = RDFFactory.getInstance();
	private static final IRI RDF_TYPE = RDF_FACTORY.createIRI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
	private static final IRI TEST_NAME_IRI = RDF_FACTORY.createIRI("http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#name");
	private static final IRI ACTION_PROPERTY = RDF_FACTORY.createIRI("http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#action");
	private static final IRI SCHEMA_PROPERTY = RDF_FACTORY.createIRI("http://www.w3.org/ns/shacl/test-suite#schema");
	private static final IRI DATA_PROPERTY = RDF_FACTORY.createIRI("http://www.w3.org/ns/shacl/test-suite#data");
	private static final IRI SHAPE_PROPERTY = RDF_FACTORY.createIRI("http://www.w3.org/ns/shacl/test-suite#shape");
	private static final IRI FOCUS_PROPERTY = RDF_FACTORY.createIRI("http://www.w3.org/ns/shacl/test-suite#focus");
	private static final IRI TEST_TRAIT_IRI = RDF_FACTORY.createIRI("http://www.w3.org/ns/shacl/test-suite#trait");

	public final Resource testKind;
	public final String testName;
	public final Resource schemaFileName;
	public final Resource dataFileName;
	public Label shapeLabel;
	public Value focusNode;
	public final String testComment;
	public final Set<Value> traits;

	public TestCase(Model manifest, Resource testNode) {
		try {
			Resource actionNode = Models.getPropertyResource(manifest, testNode, ACTION_PROPERTY).get();
			traits = manifest.filter(testNode, TEST_TRAIT_IRI, null).objects();
			schemaFileName = Models.getPropertyIRI(manifest, actionNode, SCHEMA_PROPERTY).get();  
			dataFileName = Models.getPropertyIRI(manifest, actionNode, DATA_PROPERTY).get();
			if (Models.getPropertyResource(manifest, actionNode, SHAPE_PROPERTY).isPresent()) {
				Resource labelRes = Models.getPropertyResource(manifest, actionNode, SHAPE_PROPERTY).get();
				if (labelRes instanceof BNode)
					shapeLabel = new Label((BNode)labelRes);
				else
					shapeLabel = new Label((IRI)labelRes);

				focusNode = Models.getProperty(manifest, actionNode, FOCUS_PROPERTY).get();
			}
			testComment = Models.getPropertyString(manifest, testNode, RDFS.COMMENT).get();
			testName = Models.getPropertyString(manifest, testNode, TEST_NAME_IRI).get();
			testKind = Models.getPropertyIRI(manifest, testNode, RDF_TYPE).get();
		} catch (Exception e) {
			System.out.println(" Error on test case " + testNode);
			throw e;
		}
	}

	@Override
	public String toString() {
		String info = "";
		info += testName + "\n";
		info += testKind.toString() + "\n";
		info += "Comment    : " + testComment + "\n";
		info += "Schema file: " + schemaFileName + "\n";
		info += "Data file  : " + dataFileName + "\n";
		info += "Focus : " + focusNode + "\n";
		info += "Shape : " + shapeLabel + "\n";
		return info;
	}		

	public boolean isWellDefined () {
		return schemaFileName != null && dataFileName != null && shapeLabel != null && focusNode != null;
	}
}
