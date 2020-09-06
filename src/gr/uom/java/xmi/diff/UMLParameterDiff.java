package gr.uom.java.xmi.diff;

import java.util.LinkedHashSet;
import java.util.Set;

import org.refactoringminer.api.Refactoring;

import gr.uom.java.xmi.UMLAnnotation;
import gr.uom.java.xmi.UMLOperation;
import gr.uom.java.xmi.UMLParameter;
import gr.uom.java.xmi.decomposition.AbstractCodeMapping;
import gr.uom.java.xmi.decomposition.VariableDeclaration;
import gr.uom.java.xmi.decomposition.VariableReferenceExtractor;
import gr.uom.java.xmi.decomposition.replacement.Replacement;
import gr.uom.java.xmi.decomposition.replacement.Replacement.ReplacementType;

public class UMLParameterDiff {
	private UMLParameter removedParameter;
	private UMLParameter addedParameter;
	private UMLOperation removedOperation;
	private UMLOperation addedOperation;
	private boolean typeChanged;
	private boolean qualifiedTypeChanged;
	private boolean nameChanged;
	private Set<AbstractCodeMapping> mappings;
	private UMLAnnotationListDiff annotationListDiff;
	
	public UMLParameterDiff(UMLParameter removedParameter, UMLParameter addedParameter,
			UMLOperation removedOperation, UMLOperation addedOperation,
			Set<AbstractCodeMapping> mappings) {
		this.mappings = mappings;
		this.removedParameter = removedParameter;
		this.addedParameter = addedParameter;
		this.removedOperation = removedOperation;
		this.addedOperation = addedOperation;
		this.typeChanged = false;
		this.nameChanged = false;
		if(!removedParameter.getType().equals(addedParameter.getType()))
			typeChanged = true;
		else if(!removedParameter.getType().equalsQualified(addedParameter.getType()))
			qualifiedTypeChanged = true;
		if(!removedParameter.getName().equals(addedParameter.getName()))
			nameChanged = true;
		this.annotationListDiff = new UMLAnnotationListDiff(removedParameter.getAnnotations(), addedParameter.getAnnotations());
	}

	public UMLParameter getRemovedParameter() {
		return removedParameter;
	}

	public UMLParameter getAddedParameter() {
		return addedParameter;
	}

	public boolean isTypeChanged() {
		return typeChanged;
	}

	public boolean isQualifiedTypeChanged() {
		return qualifiedTypeChanged;
	}

	public boolean isNameChanged() {
		return nameChanged;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		if(typeChanged || nameChanged || qualifiedTypeChanged)
			sb.append("\t\t").append("parameter ").append(removedParameter).append(":").append("\n");
		if(typeChanged || qualifiedTypeChanged)
			sb.append("\t\t").append("type changed from " + removedParameter.getType() + " to " + addedParameter.getType()).append("\n");
		if(nameChanged)
			sb.append("\t\t").append("name changed from " + removedParameter.getName() + " to " + addedParameter.getName()).append("\n");
		for(UMLAnnotation annotation : annotationListDiff.getRemovedAnnotations()) {
			sb.append("\t").append("annotation " + annotation + " removed").append("\n");
		}
		for(UMLAnnotation annotation : annotationListDiff.getAddedAnnotations()) {
			sb.append("\t").append("annotation " + annotation + " added").append("\n");
		}
		for(UMLAnnotationDiff annotationDiff : annotationListDiff.getAnnotationDiffList()) {
			sb.append("\t").append("annotation " + annotationDiff.getRemovedAnnotation() + " modified to " + annotationDiff.getAddedAnnotation()).append("\n");
		}
		return sb.toString();
	}

	public Set<Refactoring> getRefactorings() {
		Set<Refactoring> refactorings = new LinkedHashSet<Refactoring>();
		VariableDeclaration originalVariable = getRemovedParameter().getVariableDeclaration();
		VariableDeclaration newVariable = getAddedParameter().getVariableDeclaration();
		Set<AbstractCodeMapping> references = VariableReferenceExtractor.findReferences(originalVariable, newVariable, mappings);
		RenameVariableRefactoring renameRefactoring = null;
		if(isNameChanged() && !inconsistentReplacement(originalVariable, newVariable)) {
			renameRefactoring = new RenameVariableRefactoring(originalVariable, newVariable, removedOperation, addedOperation, references);
			refactorings.add(renameRefactoring);
		}
		if((isTypeChanged() || isQualifiedTypeChanged()) && !inconsistentReplacement(originalVariable, newVariable)) {
			ChangeVariableTypeRefactoring refactoring = new ChangeVariableTypeRefactoring(originalVariable, newVariable, removedOperation, addedOperation, references);
			if(renameRefactoring != null) {
				refactoring.addRelatedRefactoring(renameRefactoring);
			}
			refactorings.add(refactoring);
		}
		for(UMLAnnotation annotation : annotationListDiff.getAddedAnnotations()) {
			AddVariableAnnotationRefactoring refactoring = new AddVariableAnnotationRefactoring(annotation, originalVariable, newVariable, removedOperation, addedOperation);
			refactorings.add(refactoring);
		}
		for(UMLAnnotation annotation : annotationListDiff.getRemovedAnnotations()) {
			RemoveVariableAnnotationRefactoring refactoring = new RemoveVariableAnnotationRefactoring(annotation, originalVariable, newVariable, removedOperation, addedOperation);
			refactorings.add(refactoring);
		}
		for(UMLAnnotationDiff annotationDiff : annotationListDiff.getAnnotationDiffList()) {
			ModifyVariableAnnotationRefactoring refactoring = new ModifyVariableAnnotationRefactoring(annotationDiff.getRemovedAnnotation(), annotationDiff.getAddedAnnotation(), originalVariable, newVariable, removedOperation, addedOperation);
			refactorings.add(refactoring);
		}
		return refactorings;
	}

	private boolean inconsistentReplacement(VariableDeclaration originalVariable, VariableDeclaration newVariable) {
		if(removedOperation.isStatic() || addedOperation.isStatic()) {
			for(AbstractCodeMapping mapping : mappings) {
				for(Replacement replacement : mapping.getReplacements()) {
					if(replacement.getType().equals(ReplacementType.VARIABLE_NAME)) {
						if(replacement.getBefore().equals(originalVariable.getVariableName()) && !replacement.getAfter().equals(newVariable.getVariableName())) {
							return true;
						}
						else if(!replacement.getBefore().equals(originalVariable.getVariableName()) && replacement.getAfter().equals(newVariable.getVariableName())) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}
}
