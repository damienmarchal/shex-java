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
package fr.univLille.cristal.shex.schema.parsing;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.github.jsonldjava.utils.JsonUtils;

import fr.univLille.cristal.shex.graph.TCProperty;
import fr.univLille.cristal.shex.schema.Label;
import fr.univLille.cristal.shex.schema.ShexSchema;
import fr.univLille.cristal.shex.schema.abstrsynt.Annotation;
import fr.univLille.cristal.shex.schema.abstrsynt.EachOf;
import fr.univLille.cristal.shex.schema.abstrsynt.EmptyShape;
import fr.univLille.cristal.shex.schema.abstrsynt.EmptyTripleExpression;
import fr.univLille.cristal.shex.schema.abstrsynt.NodeConstraint;
import fr.univLille.cristal.shex.schema.abstrsynt.OneOf;
import fr.univLille.cristal.shex.schema.abstrsynt.RepeatedTripleExpression;
import fr.univLille.cristal.shex.schema.abstrsynt.Shape;
import fr.univLille.cristal.shex.schema.abstrsynt.ShapeAnd;
import fr.univLille.cristal.shex.schema.abstrsynt.ShapeExpr;
import fr.univLille.cristal.shex.schema.abstrsynt.ShapeExprRef;
import fr.univLille.cristal.shex.schema.abstrsynt.ShapeNot;
import fr.univLille.cristal.shex.schema.abstrsynt.ShapeOr;
import fr.univLille.cristal.shex.schema.abstrsynt.TripleConstraint;
import fr.univLille.cristal.shex.schema.abstrsynt.TripleExpr;
import fr.univLille.cristal.shex.schema.abstrsynt.TripleExprRef;
import fr.univLille.cristal.shex.schema.concrsynt.Constraint;
import fr.univLille.cristal.shex.schema.concrsynt.DatatypeConstraint;
import fr.univLille.cristal.shex.schema.concrsynt.FacetNumericConstraint;
import fr.univLille.cristal.shex.schema.concrsynt.FacetStringConstraint;
import fr.univLille.cristal.shex.schema.concrsynt.IRIStemConstraint;
import fr.univLille.cristal.shex.schema.concrsynt.IRIStemRangeConstraint;
import fr.univLille.cristal.shex.schema.concrsynt.LanguageConstraint;
import fr.univLille.cristal.shex.schema.concrsynt.LanguageStemConstraint;
import fr.univLille.cristal.shex.schema.concrsynt.LanguageStemRangeConstraint;
import fr.univLille.cristal.shex.schema.concrsynt.LiteralStemConstraint;
import fr.univLille.cristal.shex.schema.concrsynt.LiteralStemRangeConstraint;
import fr.univLille.cristal.shex.schema.concrsynt.NodeKindConstraint;
import fr.univLille.cristal.shex.schema.concrsynt.ValueSetValueConstraint;
import fr.univLille.cristal.shex.schema.concrsynt.WildcardConstraint;
import fr.univLille.cristal.shex.util.Interval;


/** Convert a shex schema to its jsonld representation.
 * @author Jérémie Dusart
 *
 */
public class ShExJSerializer {
	
	public static void ToJson(ShexSchema schema, Path destination) throws JsonGenerationException, IOException {
		Object json = ToJson(schema.getRules());
		FileWriter fw = new FileWriter(destination.toFile());
		JsonUtils.writePrettyPrint(fw, json);
	}

	public static Object ToJson(Map<Label,ShapeExpr> rules) {
		Map<String,Object> result = new LinkedHashMap<String, Object>();
		result.put("@context","http://www.w3.org/ns/shex.jsonld");
		result.put("type", "Schema");
		List<Object> shapes = new ArrayList<Object>();
		for (ShapeExpr shape:rules.values())
			shapes.add(convertShapeExpr(shape));
		result.put("shapes", shapes);
		return result;
	}
	
	
	
	//--------------------------------------------------
	// Shape conversion
	//--------------------------------------------------

	protected static Object convertShapeExpr(ShapeExpr shape) {
		if (shape instanceof ShapeAnd)
			return convertShapeAnd((ShapeAnd) shape);
		if (shape instanceof ShapeOr)
			return convertShapeOr((ShapeOr) shape);
		if (shape instanceof ShapeNot)
			return convertShapeNot((ShapeNot) shape);
		if (shape instanceof ShapeExprRef)
			return convertShapeExprRef((ShapeExprRef) shape);
		if (shape instanceof Shape)
			return convertShape((Shape) shape);
		if (shape instanceof NodeConstraint)
			return convertNodeConstraint((NodeConstraint) shape);
		return null;
	}
	
	protected static Object convertShapeAnd(ShapeAnd shape) {
		Map<String,Object> result = new LinkedHashMap<String, Object>();
		if (! shape.getId().isGenerated())
			result.put("id", shape.getId().stringValue());
		result.put("type", "ShapeAnd");
		List<Object> subExprs = new ArrayList<Object>();
		for (ShapeExpr sub:shape.getSubExpressions())
			subExprs.add(convertShapeExpr(sub));
		result.put("shapeExprs", subExprs);	
		return result;
	}
	
	protected static Object convertShapeOr(ShapeOr shape) {
		Map<String,Object> result = new LinkedHashMap<String, Object>();
		if (! shape.getId().isGenerated())
			result.put("id", shape.getId().stringValue());
		result.put("type", "ShapeOr");
		List<Object> subExprs = new ArrayList<Object>();
		for (ShapeExpr sub:shape.getSubExpressions())
			subExprs.add(convertShapeExpr(sub));
		result.put("shapeExprs", subExprs);	
		return result;
	}
	
	protected static Object convertShapeNot(ShapeNot shape) {
		Map<String,Object> result = new LinkedHashMap<String, Object>();
		if (! shape.getId().isGenerated())
			result.put("id", shape.getId().stringValue());
		result.put("type", "ShapeNot");
		result.put("shapeExpr", convertShapeExpr(shape.getSubExpression()));	
		return result;
	}
	
	protected static Object convertShapeExprRef(ShapeExprRef shape) {
		return shape.getLabel().stringValue();
	}
	
	protected static Object convertShape(Shape shape) {
		Map<String,Object> result = new LinkedHashMap<String, Object>();
		if (! shape.getId().isGenerated())
			result.put("id", shape.getId().stringValue());
		result.put("type", "Shape");
		if (shape.isClosed())
			result.put("closed", true);
		if (shape.getExtraProperties().size()>0) {
			List<Object> extra = new ArrayList<Object>();
			for (TCProperty tcp:shape.getExtraProperties()) {
				extra.add(tcp.getIri().stringValue());
			}
			result.put("extra", extra);
		}
		if (!(shape.getTripleExpression() instanceof EmptyTripleExpression))
			result.put("expression", convertTripleExpr(shape.getTripleExpression()));
		if (shape.getAnnotations()!=null && shape.getAnnotations().size()>0) {
			result.put("annotations", convertAnnotations(shape.getAnnotations()));
		}
		return result;
	}

	protected static Object convertNodeConstraint(NodeConstraint shape) {
		Map<String,Object> result = new LinkedHashMap<String, Object>();
		if (! shape.getId().isGenerated())
			result.put("id", shape.getId().stringValue());
		result.put("type", "NodeConstraint");
		
		List<Constraint> constraints = shape.getConstraints();
		for (Constraint constraint:constraints) {
			if (constraint.equals(NodeKindConstraint.Blank))
				result.put("nodeKind", "bnode");
			if (constraint.equals(NodeKindConstraint.AllIRI))
				result.put("nodeKind", "iri");
			if (constraint.equals(NodeKindConstraint.AllLiteral))
				result.put("nodeKind", "literal");
			if (constraint.equals(NodeKindConstraint.AllNonLiteral))
				result.put("nodeKind", "nonliteral");
			if (constraint instanceof DatatypeConstraint)
				result.put("datatype",((DatatypeConstraint) constraint).getDatatypeIri().stringValue());
			if (constraint instanceof FacetNumericConstraint)
				convertNumericFacet((FacetNumericConstraint) constraint, result);
			if (constraint instanceof FacetStringConstraint)
				convertStringFacet((FacetStringConstraint) constraint, result);
			if (constraint instanceof ValueSetValueConstraint)
				result.put("values", convertValueSetValueConstraint((ValueSetValueConstraint) constraint));
			
		}
		
		return result;
	}
	
	
	//--------------------------------------------------
	// Constraint conversion
	//--------------------------------------------------
	
	protected static void convertNumericFacet(FacetNumericConstraint facet, Map<String,Object> res) {
		if (facet.getMinincl() != null)
			res.put("mininclusive", facet.getMinincl());
		if (facet.getMinexcl() != null)
			res.put("minexclusive", facet.getMinexcl());
		if (facet.getMaxincl() != null)
			res.put("maxinclusive", facet.getMaxincl());
		if (facet.getMaxexcl() != null)
			res.put("maxexclusive", facet.getMaxexcl());
		if (facet.getTotalDigits() != null)
			res.put("totaldigits", facet.getTotalDigits());
		if (facet.getFractionDigits() != null)
			res.put("fractiondigits", facet.getFractionDigits());
	}
		
	protected static void convertStringFacet(FacetStringConstraint facet, Map<String,Object> res) {
		if (facet.getLength()!=null)
			res.put("length", facet.getLength());
		if (facet.getMinlength()!=null)
			res.put("minlength", facet.getMinlength());
		if (facet.getMaxlength()!=null)
			res.put("maxlength", facet.getMaxlength());
		if (facet.getPatternString()!=null)
			res.put("pattern", facet.getPatternString());
		if (facet.getFlags()!=null)
			res.put("flags", facet.getFlags());
	}
	
	protected static Object convertValueSetValueConstraint(ValueSetValueConstraint constraint) {
		List<Object> result = new ArrayList<Object>();
		for (Value val:constraint.getExplicitValues()) {
			result.add(convertValue(val));
		}
		
		for (Constraint cons:constraint.getConstraintsValue()) {
			if (cons instanceof LanguageConstraint)
				result.add(convertLanguageConstraint((LanguageConstraint) cons));
			if (cons instanceof LanguageStemConstraint)
				result.add(convertLanguageStemConstraint((LanguageStemConstraint) cons));
			if (cons instanceof LanguageStemRangeConstraint)
				result.add(convertLanguageStemRangeConstraint((LanguageStemRangeConstraint) cons));
			if (cons instanceof IRIStemConstraint)
				result.add(convertIRIStemConstraint((IRIStemConstraint) cons));
			if (cons instanceof IRIStemRangeConstraint)
				result.add(convertIRIStemRangeConstraint((IRIStemRangeConstraint) cons));
			if (cons instanceof LiteralStemConstraint)
				result.add(convertLiteralStemConstraint((LiteralStemConstraint) cons));
			if (cons instanceof LiteralStemRangeConstraint)
				result.add(convertLiteralStemRangeConstraint((LiteralStemRangeConstraint) cons));
		}

		return result;
	}

	protected static Object convertLanguageConstraint(LanguageConstraint cons) {
		Map<String,String> result = new LinkedHashMap<>();
		result.put("type", "Language");
		result.put("langTag", cons.getLangTag());
		return result;
	}

	protected static Object convertLanguageStemConstraint(LanguageStemConstraint cons) {
		Map<String,String> result = new LinkedHashMap<>();
		result.put("type", "LanguageStem");
		result.put("stem", cons.getLangStem());
		return result;
	}

	protected static Object convertLanguageStemRangeConstraint(LanguageStemRangeConstraint cons) {
		Map<String,Object> result = new LinkedHashMap<>();
		result.put("type", "LanguageStemRange");
		if (!(cons.getStem() instanceof WildcardConstraint))
			result.put("stem", ((LanguageStemConstraint) cons.getStem()).getLangStem());
		else {
			Map<String,Object> tmp = new LinkedHashMap<>();
			tmp.put("type", "Wildcard");
			result.put("stem",tmp);
		}
		List<Object> exclusions = (List) convertValueSetValueConstraint(cons.getExclusions());
		if (exclusions.size()>0)
			result.put("exclusions", exclusions);
		return result;
	}

	protected static Object convertIRIStemConstraint(IRIStemConstraint cons) {
		Map<String,String> result = new LinkedHashMap<>();
		result.put("type", "IriStem");
		result.put("stem", cons.getIriStem());
		return result;
	}

	protected static Object convertIRIStemRangeConstraint(IRIStemRangeConstraint cons) {
		Map<String,Object> result = new LinkedHashMap<>();
		result.put("type", "IriStemRange");
		if (!(cons.getStem() instanceof WildcardConstraint))
			result.put("stem", ((IRIStemConstraint) cons.getStem()).getIriStem());
		else {
			Map<String,Object> tmp = new LinkedHashMap<>();
			tmp.put("type", "Wildcard");
			result.put("stem",tmp);
		}
		List<Object> exclusions = (List) convertValueSetValueConstraint(cons.getExclusions());
		if (exclusions.size()>0)
			result.put("exclusions", exclusions);
		return result;
	}

	protected static Object convertLiteralStemConstraint(LiteralStemConstraint cons) {
		Map<String,String> result = new LinkedHashMap<>();
		result.put("type", "LiteralStem");
		result.put("stem", cons.getLitStem());
		return result;
	}

	protected static Object convertLiteralStemRangeConstraint(LiteralStemRangeConstraint cons) {
		Map<String,Object> result = new LinkedHashMap<>();
		result.put("type", "LiteralStemRange");
		if (!(cons.getStem() instanceof WildcardConstraint))
			result.put("stem", ((LiteralStemConstraint) cons.getStem()).getLitStem());
		else {
			Map<String,Object> tmp = new LinkedHashMap<>();
			tmp.put("type", "Wildcard");
			result.put("stem",tmp);
		}
		List<Object> exclusions = (List) convertValueSetValueConstraint(cons.getExclusions());
		if (exclusions.size()>0)
			result.put("exclusions", exclusions);
		return result;
	}

	
	
	//--------------------------------------------------
	// Triple conversion
	//--------------------------------------------------

	protected static Object convertTripleExpr(TripleExpr triple) {
		if (triple instanceof EachOf)
			return convertEachOf((EachOf) triple);
		if (triple instanceof OneOf)
			return convertOneOf((OneOf) triple);
		if (triple instanceof TripleExprRef)
			return convertTripleExprRef((TripleExprRef) triple);
		if (triple instanceof RepeatedTripleExpression)
			return convertRepeatedTripleExpression((RepeatedTripleExpression) triple);
		if (triple instanceof TripleConstraint)
			return convertTripleConstraint((TripleConstraint) triple);
		return null;
	}
	
	protected static Object convertEachOf(EachOf triple) {
		Map<String,Object> result = new LinkedHashMap<String, Object>();
		if (! triple.getId().isGenerated())
			result.put("id", triple.getId().stringValue());
		result.put("type", "EachOf");
		List<Object> subExprs = new ArrayList<>();
		for (TripleExpr sub:triple.getSubExpressions()) {
			subExprs.add(convertTripleExpr(sub));
		}
		result.put("expressions", subExprs);
		if (triple.getAnnotations()!=null && triple.getAnnotations().size()>0) {
			result.put("annotations", convertAnnotations(triple.getAnnotations()));
		}
		return result;		
	}

	protected static Object convertOneOf(OneOf triple) {
		Map<String,Object> result = new LinkedHashMap<String, Object>();
		if (! triple.getId().isGenerated())
			result.put("id", triple.getId().stringValue());
		result.put("type", "OneOf");
		List<Object> subExprs = new ArrayList<>();
		for (TripleExpr sub:triple.getSubExpressions()) {
			subExprs.add(convertTripleExpr(sub));
		}
		result.put("expressions", subExprs);
		if (triple.getAnnotations()!=null && triple.getAnnotations().size()>0) {
			result.put("annotations", convertAnnotations(triple.getAnnotations()));
		}
		return result;	
	}

	protected static Object convertTripleExprRef(TripleExprRef triple) {
		return triple.getLabel().stringValue();
	}
	
	protected static Object convertRepeatedTripleExpression(RepeatedTripleExpression triple) {
		Map<String,Object> conv = (Map<String, Object>) convertTripleExpr(triple.getSubExpression());
		Interval card = triple.getCardinality();
		conv.put("min", card.min);
		if (card.isUnbound())
			conv.put("max", -1);
		else
			conv.put("max", card.max);		
		return conv;
	}
	
	protected static Object convertTripleConstraint(TripleConstraint triple) {
		Map<String,Object> result = new LinkedHashMap<String, Object>();
		
		if (! triple.getId().isGenerated())
			result.put("id", triple.getId().stringValue());
		
		result.put("type", "TripleConstraint");
		
		if (! triple.getProperty().isForward())
			result.put("inverse", true);
		result.put("predicate", triple.getProperty().getIri().stringValue());
		
		if (! triple.getShapeExpr().equals(EmptyShape.Shape)) 
			result.put("valueExpr", convertShapeExpr(triple.getShapeExpr()));
		
		if (triple.getAnnotations()!=null && triple.getAnnotations().size()>0) 
			result.put("annotations", convertAnnotations(triple.getAnnotations()));
		
		return result;
	}
	
	
	
	//--------------------------------------------------
	// Utils conversion
	//--------------------------------------------------

	protected static Object convertAnnotations(List<Annotation> annotations) {
		List<Object> result = new ArrayList<Object>();
		for (Annotation ann:annotations){
			Map<String,Object> tmp = new LinkedHashMap<>();
			tmp.put("type", "Annotation");
			tmp.put("predicate", ann.getPredicate().stringValue());
			if (ann.getObjectValue() instanceof IRI)
				tmp.put("object", ann.getObjectValue().stringValue());
			else {
				Map<String,Object> tmp2 = new LinkedHashMap<>();
				tmp2.put("value", ann.getObjectValue().stringValue());
				tmp.put("object", tmp2);
			}
			result.add(tmp);			
		}
		return result;
	}
	
	protected static Object convertValue(Value v) {
		if (v instanceof Literal) {
			Literal lv = (Literal) v;
			Map<String,Object> result = new LinkedHashMap<String, Object>();
			result.put("value", lv.stringValue());
			result.put("type", lv.getDatatype().toString());
			if (lv.getLanguage().isPresent())
				result.put("language", lv.getLanguage().get());
			return result;
		}
		return v.stringValue();
	}
	
	
}
