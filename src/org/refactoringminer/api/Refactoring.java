package org.refactoringminer.api;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;

import com.fasterxml.jackson.core.io.JsonStringEncoder;
import com.fasterxml.jackson.core.util.BufferRecyclers;
public interface Refactoring extends Serializable, CodeRangeProvider {

	public RefactoringType getRefactoringType();
	
	public String getName();

	public String toString();
	
	public List<String> getInvolvedClassesBeforeRefactoring();
	
	public List<String> getInvolvedClassesAfterRefactoring();
	
	default public String toJSON() {
		StringBuilder sb = new StringBuilder();
		JsonStringEncoder encoder = BufferRecyclers.getJsonStringEncoder();
		sb.append("{").append("\n");
		sb.append("\t").append("\"").append("type").append("\"").append(": ").append("\"").append(getName()).append("\"").append(",").append("\n");
		sb.append("\t").append("\"").append("description").append("\"").append(": ").append("\"");
		encoder.quoteAsString(toString().replace('\t', ' '), sb);
		sb.append("\"").append(",").append("\n");
		sb.append("\t").append("\"").append("leftSideLocations").append("\"").append(": ").append(leftSide()).append(",").append("\n");
		sb.append("\t").append("\"").append("rightSideLocations").append("\"").append(": ").append(rightSide()).append("\n");
		sb.append("}");
		return sb.toString();
	}
}