package gr.uom.java.xmi.diff;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringType;import com.sun.javafx.collections.MapAdapterChange;

import gr.uom.java.xmi.LocationInfo.CodeElementType;
import gr.uom.java.xmi.UMLAnonymousClass;
import gr.uom.java.xmi.UMLClass;
import gr.uom.java.xmi.UMLGeneralization;
import gr.uom.java.xmi.UMLJavadoc;
import gr.uom.java.xmi.UMLOperation;
import gr.uom.java.xmi.UMLParameter;
import gr.uom.java.xmi.UMLTagElement;
import gr.uom.java.xmi.UMLType;
import gr.uom.java.xmi.decomposition.AbstractExpression;
import gr.uom.java.xmi.decomposition.AbstractStatement;
import gr.uom.java.xmi.decomposition.AnonymousClassDeclarationObject;
import gr.uom.java.xmi.decomposition.CompositeStatementObject;
import gr.uom.java.xmi.decomposition.ObjectCreation;
import gr.uom.java.xmi.decomposition.OperationBody;
import gr.uom.java.xmi.decomposition.OperationInvocation;
import gr.uom.java.xmi.decomposition.StatementObject;
import gr.uom.java.xmi.decomposition.UMLOperationBodyMapper;
import gr.uom.java.xmi.decomposition.VariableDeclaration;

public class MotivationExtractor {
	private UMLModelDiff modelDiff;
	private List<Refactoring> refactorings;
	private Map<RefactoringType, List<Refactoring>> mapClassifiedRefactorings;
	private Map<Refactoring , List<MotivationType>> mapRefactoringMotivations;
	
	public MotivationExtractor(UMLModelDiff modelDiff, List<Refactoring> refactorings) {
		this.modelDiff = modelDiff;
		this.refactorings = refactorings;
		this.mapClassifiedRefactorings = new HashMap<RefactoringType, List<Refactoring>>();
		this.mapRefactoringMotivations = new HashMap<Refactoring, List<MotivationType>>();
		classifyRefactoringsByType(refactorings);		
	}
	
	public void detectAllRefactoringMotivations() {		
		for(RefactoringType type : mapClassifiedRefactorings.keySet() ){
			detectMotivataion(type);
		}
	}

	public Map<Refactoring, List<MotivationType>> getMapRefactoringMotivations() {
		return mapRefactoringMotivations;
	}

	private void detectMotivataion(RefactoringType type) {	
		
		List<Refactoring> listRef = mapClassifiedRefactorings.get(type);
		switch (type) {
		case EXTRACT_OPERATION :
			detectExtractOperationMotivation(listRef);
			break;
		case MOVE_CLASS:
			detectMoveClassMotivation(listRef);
			break;
		case MOVE_ATTRIBUTE:
			
			break;
		case RENAME_PACKAGE:
			
			break;
		case MOVE_OPERATION:
			
			break;
		case INLINE_OPERATION:
			
			break;
		case PULL_UP_OPERATION:
			
			break;
		case PULL_UP_ATTRIBUTE:
			
			break;
		case EXTRACT_SUPERCLASS:
			
			break;				
		case PUSH_DOWN_OPERATION:
			
			break;
		case PUSH_DOWN_ATTRIBUTE:						
			
			break;
		case EXTRACT_INTERFACE:

		default:
			break;
		}	
	}

	private void detectMoveClassMotivation(List<Refactoring> listRef) {
		for(Refactoring ref : listRef){
			if(IsMoveClassToAppropriateContainer(ref)) {
				setRefactoringMotivation(MotivationType.MC_MOVE_CLASS_TO_APPROPRIATE_CONTAINER, ref);
			}
		}
	}


	private boolean IsMoveClassToAppropriateContainer(Refactoring ref) {
		if(ref instanceof MoveClassRefactoring) {
		}
		return false;
	}

	private void detectExtractOperationMotivation(List<Refactoring> listRef) {		
		
		//Motivation Detection algorithms that depends on other refactorings of the same type
		isDecomposeMethodToImroveReadability(listRef);
		isMethodExtractedToRemoveDuplication(listRef);
		
		//Motivation Detection algorithms that can detect the motivation independently for each refactoring
		for(Refactoring ref : listRef){
			if(isExtractReusableMethod(ref)) {
				setRefactoringMotivation(MotivationType.EM_REUSABLE_METHOD, ref);
			}
			if(isIntroduceAlternativeMethodSignature(ref)) {
				setRefactoringMotivation(MotivationType.EM_INTRODUCE_ALTERNATIVE_SIGNATURE, ref);
			}
			if(isReplaceMethodPreservingBackwardCompatibility(ref)){
				setRefactoringMotivation(MotivationType.EM_REPLACE_METHOD_PRESERVING_BACKWARD_COMPATIBILITY, ref);
			}
			if(IsExtractedToImproveTestability(ref)) {
				setRefactoringMotivation(MotivationType.EM_IMPROVE_TESTABILITY, ref);
			}
			if(IsExtractedtoEnableRecursion(ref)) {
				setRefactoringMotivation(MotivationType.EM_ENABLE_RECURSION, ref);
			}
			if(IsExtractedToEnableOverriding(ref)) {
				setRefactoringMotivation(MotivationType.EM_ENABLE_OVERRIDING, ref);
			}
			if(IsExtractedToIntroduceFactoryMethod(ref)) {
				setRefactoringMotivation(MotivationType.EM_INTRODUCE_FACTORY_METHOD, ref);
			}
			if(IsExtractedtoIntroduceAsyncOperation(ref)) {
				setRefactoringMotivation(MotivationType.EM_INTRODUCE_ASYNC_OPERATION, ref);
			}
			
			if(isExtractFacilitateExtension(ref,listRef)){		
				setRefactoringMotivation(MotivationType.EM_FACILITATE_EXTENSION, ref);
			}		
		}
		
		postProcessingForIsExtractFacilitateExtension(listRef);
		
		//Print All detected refactorings
		printDetectedRefactoringMotivations();			
	}
	
	private void postProcessingForIsExtractFacilitateExtension(List<Refactoring> listRef) {
		//Post Processing  for facilitate extension
		/*Removing facilitate extension from the cases where multiple extract operations with the same extracted operation
		 * (LIKE THE CASE OF REMOVE DUPLICATION) exists and at least one of them has no new added code .
		 *  we will omit the facilitate extension from all the other extract operations
		 *  of that group as well. Example: j2objc fa3e6fa */
		boolean extracteOperationwithoutAddedCodeInExtractedMethodExists = false;
		List<ExtractOperationRefactoring> listExtractOperationstoRemoveDupliction = new ArrayList<ExtractOperationRefactoring>();
		for(Refactoring refactoring : listRef) {
			if(refactoring instanceof ExtractOperationRefactoring) {
				if(mapRefactoringMotivations.containsKey(refactoring)){
					if(mapRefactoringMotivations.get(refactoring).contains(MotivationType.EM_REMOVE_DUPLICATION)){
						listExtractOperationstoRemoveDupliction.add((ExtractOperationRefactoring)refactoring);	
					}
				}	
			}
		}
		for(ExtractOperationRefactoring refactoring : listExtractOperationstoRemoveDupliction) {
			List<MotivationType> extractRefactoringMotivations = mapRefactoringMotivations.get(refactoring);
			if(!extractRefactoringMotivations.contains(MotivationType.EM_FACILITATE_EXTENSION)) {
				extracteOperationwithoutAddedCodeInExtractedMethodExists = true;
				break;
			}
		}
		if(extracteOperationwithoutAddedCodeInExtractedMethodExists) {
			for(Refactoring refactoring : listExtractOperationstoRemoveDupliction) {
				if(mapRefactoringMotivations.get(refactoring).contains(MotivationType.EM_FACILITATE_EXTENSION)) {
					mapRefactoringMotivations.get(refactoring).remove(MotivationType.EM_FACILITATE_EXTENSION);
				}
			}
		}
	}
			
	private boolean IsExtractedToIntroduceFactoryMethod(Refactoring ref) {
		if(ref instanceof ExtractOperationRefactoring) {
			ExtractOperationRefactoring extractOpRefactoring = (ExtractOperationRefactoring) ref;
			UMLOperation extractedOperation = extractOpRefactoring.getExtractedOperation();
			List<VariableDeclaration> listVariableDeclerations = extractedOperation.getAllVariableDeclarations();
			List<ObjectCreation> listReturnTypeObjectsCreatedInReturnStatement = new ArrayList<ObjectCreation>();
			UMLParameter returnParameter =  extractedOperation.getReturnParameter();
			UMLType returnParameterType = returnParameter.getType();
			List<String> returnStatementVariables = new ArrayList<String>();
			Map<String ,List<ObjectCreation>> returnStatementobjectCreationsMap = new HashMap<String,List<ObjectCreation>>();
			for(AbstractStatement statement : extractedOperation.getBody().getCompositeStatement().getStatements()) {
				if(statement.getLocationInfo().getCodeElementType().equals(CodeElementType.RETURN_STATEMENT)) {
					returnStatementVariables = statement.getVariables();
					returnStatementobjectCreationsMap = statement.getCreationMap();
					for(String objectCreationString : returnStatementobjectCreationsMap.keySet() ) {
						List<ObjectCreation> listObjectCreation = returnStatementobjectCreationsMap.get(objectCreationString);
						for(ObjectCreation objectCreation: listObjectCreation) {
							if(objectCreation.getType().equalClassType(returnParameterType) ||
									returnParameterType.equalsWithSubType(objectCreation.getType())) {
								listReturnTypeObjectsCreatedInReturnStatement.add(objectCreation);
							}
						}
					}
					if(listReturnTypeObjectsCreatedInReturnStatement.size() == 1) {
						return true;
					}
				}
			}
			//Find All the Variable Declerations(that are Object Creation) with same Type as return parameter
			Map<String ,List<ObjectCreation>> abstractExpressionObjectCreationsMap = new HashMap<String,List<ObjectCreation>>();
			List<ObjectCreation> abstractExpressionObjectCreations = new ArrayList<ObjectCreation>();
			List<VariableDeclaration> listObjectCreationVariableDeclerationsWithReturnType = new ArrayList<VariableDeclaration>();
			Map<UMLType,List<String>> variableTypeNameMap = new HashMap<UMLType, List<String>>();
			for(VariableDeclaration variableDecleration: listVariableDeclerations){
				if(variableDecleration.getInitializer() != null) {
					AbstractExpression abstractExpression = variableDecleration.getInitializer();
					abstractExpressionObjectCreationsMap = abstractExpression.getCreationMap();
					for(String objectCreationString : abstractExpressionObjectCreationsMap.keySet()) {
						abstractExpressionObjectCreations = abstractExpressionObjectCreationsMap.get(objectCreationString);
						for(ObjectCreation objectCreation: abstractExpressionObjectCreations) {
							if(objectCreation.getType().equalClassType(returnParameterType) ||
									returnParameterType.equalsWithSubType(objectCreation.getType())) {
								listObjectCreationVariableDeclerationsWithReturnType.add(variableDecleration);
							}
						}	
						for(VariableDeclaration objectCreationVariableDecleration :listObjectCreationVariableDeclerationsWithReturnType){
							if(variableTypeNameMap.containsKey(variableDecleration.getType())) {
								variableTypeNameMap.get(objectCreationVariableDecleration.getType()).add(objectCreationVariableDecleration.getVariableName());
							}else {
								List<String> variableNames  = new ArrayList<String>();
								variableNames.add(objectCreationVariableDecleration.getVariableName());
								variableTypeNameMap.put(objectCreationVariableDecleration.getType(), variableNames);						
							}
						}
					}			
				}
			}
			//Check if return statement returns a Variable Declerations(that are Object Creation) with Return parameter type
			for(UMLType type : variableTypeNameMap.keySet()) {
				List<String> variableNames = variableTypeNameMap.get(type);
				for(String variableName : variableNames) {
					if( returnStatementVariables.size() == 1 && returnStatementVariables.contains(variableName)){
						return true;
					}	
				}
			}
		}
		return false;
	}

	private boolean IsExtractedtoIntroduceAsyncOperation(Refactoring ref) {

		if(ref instanceof ExtractOperationRefactoring){
			ExtractOperationRefactoring extractOpRefactoring = (ExtractOperationRefactoring)ref;
			UMLOperation sourceOperationAfterExtraction = extractOpRefactoring.getSourceOperationAfterExtraction();
			UMLOperation extractedOperation = extractOpRefactoring.getExtractedOperation();
			OperationBody sourceBody = sourceOperationAfterExtraction.getBody();
			CompositeStatementObject sourceCompositeStatement = sourceBody.getCompositeStatement();
			for(AbstractStatement statement : sourceCompositeStatement.getStatements()) {
				if(statement.getTypes().contains("Runnable")){								
					List<AnonymousClassDeclarationObject> anonymousClassDeclerations = statement.getAnonymousClassDeclarations();
					for(AnonymousClassDeclarationObject decleration: anonymousClassDeclerations) {
						Map<String,List<OperationInvocation>> declerationMethodInvocationMap = decleration.getMethodInvocationMap();
						for(String methodInvocation : declerationMethodInvocationMap.keySet()) {
							List<OperationInvocation> invocations = declerationMethodInvocationMap.get(methodInvocation);
							for(OperationInvocation invocation : invocations) {
								if(invocation.matchesOperation(extractedOperation, sourceOperationAfterExtraction.variableTypeMap(), modelDiff)){
									return true;
								}
							}	
						}
					}
				}
			}
		}
		
		return false;
	}
	

	private boolean IsExtractedToEnableOverriding(Refactoring ref) {
		List<UMLOperation> operationsOverridingeExtractedOperations = new ArrayList<UMLOperation>();
		if(ref instanceof ExtractOperationRefactoring){
			ExtractOperationRefactoring extractOpRefactoring = (ExtractOperationRefactoring)ref;
			UMLOperation extractedOperation = extractOpRefactoring.getExtractedOperation();
			UMLClassBaseDiff extractedOperationClassDiff = modelDiff.getUMLClassDiff(extractedOperation.getClassName());
			if(extractedOperationClassDiff != null) {
				UMLClass extractedOperationNextClass = extractedOperationClassDiff.getNextClass();
				for(UMLClassDiff classDiff : modelDiff.getCommonClassDiffList()) {
					UMLClass nextClass = classDiff.getNextClass();
					isOperationOverridenInClass(extractedOperation, extractedOperationNextClass, nextClass,
							operationsOverridingeExtractedOperations);
				}
				for(UMLClass addedClass : modelDiff.getAddedClasses()) {
					isOperationOverridenInClass(extractedOperation, extractedOperationNextClass, addedClass,
							operationsOverridingeExtractedOperations);	
				}
			}
			UMLJavadoc javaDoc = extractedOperation.getJavadoc();
			boolean javaDocContainsOverridingKeyword = false;
			if(javaDoc != null && (javaDoc.contains("overriding") ||javaDoc.contains("override")||javaDoc.contains("subclass"))) {
				javaDocContainsOverridingKeyword = true;
			}
			if((operationsOverridingeExtractedOperations.size()>0) || javaDocContainsOverridingKeyword) {
				return true;
			}
		}
		return false;
	}

	private void isOperationOverridenInClass(UMLOperation extractedOperation, UMLClass extractedOperationNextClass,
			UMLClass nextClass, List<UMLOperation> operationsOverridingeExtractedOperations) {
		List<UMLAnonymousClass> listAnonymousUmlClasses = nextClass.getAnonymousClassList();
		if(nextClass.isSubTypeOf(extractedOperationNextClass)){
			for(UMLOperation operation : nextClass.getOperations()) {
				if(operation.equalSignature(extractedOperation))
					operationsOverridingeExtractedOperations.add(operation);
			}
		}
		for(UMLAnonymousClass anonymousClass : listAnonymousUmlClasses) {
			for(UMLOperation operation : anonymousClass.getOperations()) {
				if(operation.equalSignature(extractedOperation))
					operationsOverridingeExtractedOperations.add(operation);
			}
		}
	}

	private boolean IsExtractedToImproveTestability(Refactoring ref) {
		List<UMLOperation> operationsTestingExtractedOperation = new ArrayList<UMLOperation>();
		if(ref instanceof ExtractOperationRefactoring) {
			ExtractOperationRefactoring extractOpRefactoring = (ExtractOperationRefactoring)ref;
			UMLOperation extractedOperation = extractOpRefactoring.getExtractedOperation();
			for(UMLClassDiff classDiff : modelDiff.getCommonClassDiffList()) {
				UMLClass nextClass = classDiff.getNextClass();
				isOperationTestedInClass(nextClass, extractedOperation, operationsTestingExtractedOperation , extractOpRefactoring);
			}
			for(UMLClass addedClass : modelDiff.getAddedClasses()) {
				isOperationTestedInClass(addedClass, extractedOperation, operationsTestingExtractedOperation, extractOpRefactoring);
			}
		}
		if(operationsTestingExtractedOperation.size() > 0)
			return true;
		return false;
	}

	private void isOperationTestedInClass(UMLClass nextClass, UMLOperation extractedOperation,
			List<UMLOperation> operationsTestingExtractedOperation , ExtractOperationRefactoring extractOpRefactoring) {
		String extractedOperationClassName = extractOpRefactoring.getExtractedOperation().getClassName();
		if(!nextClass.getName().equals(extractedOperationClassName)) {
			for(UMLOperation operation : nextClass.getOperations()) {
				if(operation.hasTestAnnotation() || operation.getName().startsWith("test") || nextClass.isTestClass()) {
					for(OperationInvocation invocation : operation.getAllOperationInvocations()) {
						if(invocation.matchesOperation(extractedOperation, operation.variableTypeMap(), modelDiff)) {
							operationsTestingExtractedOperation.add(operation);
						}
					}
				}
			}	
		}
	}

	private boolean IsExtractedtoEnableRecursion(Refactoring ref) {
		if(ref instanceof ExtractOperationRefactoring) {
			ExtractOperationRefactoring extractOpRefactoring = (ExtractOperationRefactoring) ref;
			UMLOperation extractedOperation = extractOpRefactoring.getExtractedOperation();
			UMLClassBaseDiff classDiff = modelDiff.getUMLClassDiff(extractedOperation.getClassName());
			int countExtractedOperationRecursiveInvocations = 0;
			if(classDiff != null) {
				UMLClass classAfterRefactoring = classDiff.getNextClass();
				for(UMLOperation operation : classAfterRefactoring.getOperations()) {
					if(operation.equals(extractedOperation)) {
						List<OperationInvocation> listInvokations = operation.getAllOperationInvocations();
						for( OperationInvocation invocation : listInvokations) {
							boolean noExpression = invocation.getExpression() == null;
							boolean thisExpression = invocation.getExpression() != null && invocation.getExpression().equals("this");
							boolean noOrThisExpresion = noExpression || thisExpression;
							if(invocation.matchesOperation(extractedOperation, operation.variableTypeMap(),modelDiff) && noOrThisExpresion){
								countExtractedOperationRecursiveInvocations++;
							}
						}
					}
				}
			}
			if(countExtractedOperationRecursiveInvocations > 0) {
				return true;
			}
		}
		return false;
	}

	private boolean isReplaceMethodPreservingBackwardCompatibility(Refactoring ref) {
		if( ref instanceof ExtractOperationRefactoring){
			ExtractOperationRefactoring extractOpRefactoring = (ExtractOperationRefactoring)ref;
			UMLOperation sourceOpAfterExtraction = extractOpRefactoring.getSourceOperationAfterExtraction();;	
			OperationBody sourceOpBodyAfterExtraction = sourceOpAfterExtraction.getBody();
			int countStatements = sourceOpBodyAfterExtraction.statementCount();
			if(countStatements == 1){
				if(isExtractOperationForBackwardCompatibility(ref)) {
					return true;
				}

			}else{
				//Temporary Variables should be excluded.
				if(isUmlOperationStatementsAllTempVariables(sourceOpAfterExtraction)) {
					if(isExtractOperationForBackwardCompatibility(ref)) {
						return true;
					}
				}
			}
		}
		return false;
	}
	private boolean isExtractOperationForBackwardCompatibility(Refactoring ref) {
		if(ref instanceof ExtractOperationRefactoring) {
			ExtractOperationRefactoring extractOpRefactoring = (ExtractOperationRefactoring)ref;
			UMLOperation extractedOperation = extractOpRefactoring.getExtractedOperation();
			UMLOperation sourceOpAfterExtraction = extractOpRefactoring.getSourceOperationAfterExtraction();
			List<OperationInvocation> listExtractedOpInvokations = extractOpRefactoring.getExtractedOperationInvocations();
			boolean isBackwardCompatible = !sourceOpAfterExtraction.equalParameters(extractedOperation) ||
					!extractedOperation.getName().equals(sourceOpAfterExtraction.getName());
			String extractedOperationAccessModifier = extractedOperation.getVisibility();
			String sourceOperationAfterExtractionAccessModifier = sourceOpAfterExtraction.getVisibility();
			boolean isSourceOperationAfterExtractionAndExtractedOperationModifiersPrivate = 
					(extractedOperationAccessModifier.equals("private") && sourceOperationAfterExtractionAccessModifier.equals("private")) ? true: false;
			/*DETECTION RULE: Check IF the method parameters OR name has changed AND if source Operation after extraction is a delegate AND
			* If the source operation after Extraction and extracted operation are non privatre
			*/
			if(isBackwardCompatible && (listExtractedOpInvokations.size() == 1) /*&& isSourceOperationAfterExtractionAndExtractedOperationModifiersPrivate*/) {
				if(isUmlOperationWithDeprecatedAnnotation(sourceOpAfterExtraction)) {
					return true;	
				}else {
					if(!isMotivationDetected(ref, MotivationType.EM_INTRODUCE_ALTERNATIVE_SIGNATURE)) {
						return true;
					}
				}
			}
		}
		return false;
	}
	private boolean isUmlOperationWithDeprecatedAnnotation(UMLOperation umlOperation) {
		boolean isDeprecatedInClass = false;
		boolean isDeprecatedInImplementedInterface = false;
		//Check the operation annotation in operation Class
		List<Annotation> sourceOperationAnnotations = umlOperation.getAnnotations();
		for(Annotation annotation : sourceOperationAnnotations) {
				if(annotation.getTypeName().toString().equals("Deprecated")) {
					isDeprecatedInClass = true;
					break;
				}
			}
		//Check the operation annotation in implemented interface
		UMLClassBaseDiff umlClassDiff = modelDiff.getUMLClassDiff(umlOperation.getClassName());
		if(umlClassDiff != null) {
			UMLClass nextClass = umlClassDiff.getNextClass();
			List<UMLType> extractedOperationImplementedInterfaces = nextClass.getImplementedInterfaces();
			for(UMLType implementedInterface : extractedOperationImplementedInterfaces) {
				UMLClassBaseDiff interfaceUmlClassDiff = modelDiff.getUMLClassDiff(implementedInterface);
				if(interfaceUmlClassDiff != null) {
					UMLClass interfaceClass = interfaceUmlClassDiff.getNextClass();
					if(interfaceClass != null) {
						for( UMLOperation operation : interfaceClass.getOperations()){
							if(operation.equalSignature(umlOperation)) {
								for(Annotation annotation : operation.getAnnotations()) {
									if(annotation.getTypeName().toString().equals("Deprecated")) {
										isDeprecatedInImplementedInterface = true;
										break;
									}
								}
							}
							if(isDeprecatedInImplementedInterface) {
								break;
							}
						}			
					}
				}
			}
		}
		
		return (isDeprecatedInClass || isDeprecatedInImplementedInterface)? true:false;

	}
	

	private boolean isIntroduceAlternativeMethodSignature(Refactoring ref) {
		
		if( ref instanceof ExtractOperationRefactoring){
			ExtractOperationRefactoring extractOpRefactoring = (ExtractOperationRefactoring)ref;
			UMLOperation sourceOpAfterExtraction = extractOpRefactoring.getSourceOperationAfterExtraction();;	
			OperationBody sourceOpBodyAfterExtraction = sourceOpAfterExtraction.getBody();
			int countStatements = sourceOpBodyAfterExtraction.statementCount();
			if(countStatements == 1){
				if(isExtractOperationToIntroduceAlternativeMethod(ref)) {
					return true;
				}

			}else{
				//Temporary Variables should be excluded.
				if(isUmlOperationStatementsAllTempVariables(sourceOpAfterExtraction)) {
					if(isExtractOperationToIntroduceAlternativeMethod(ref)) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	private boolean isExtractOperationToIntroduceAlternativeMethod(Refactoring ref){
		
		if(ref instanceof ExtractOperationRefactoring) {
			ExtractOperationRefactoring extractOpRefactoring = (ExtractOperationRefactoring)ref;
			UMLOperation extractedOperation = extractOpRefactoring.getExtractedOperation();
			UMLOperation sourceOpAfterExtraction = extractOpRefactoring.getSourceOperationAfterExtraction();
			List<OperationInvocation> listExtractedOpInvokations = extractOpRefactoring.getExtractedOperationInvocations();
			boolean isToIntroduceAlternativeMethod = (!sourceOpAfterExtraction.equalParameters(extractedOperation))?true:false;
			/*DETECTION RULE: Check IF the method parameters has changed AND if source Operation after extraction is a delegate 
			*/
			if(isToIntroduceAlternativeMethod && (listExtractedOpInvokations.size() == 1)) {
				return true;
			}
		}
		return false;
	}
	
	private boolean isUmlOperationStatementsAllTempVariables(UMLOperation operation) {
		CompositeStatementObject compositeStatement = operation.getBody().getCompositeStatement();
		List<AbstractStatement> abstractStatements = compositeStatement.getStatements();
		Set<CodeElementType> codeElementTypeSet = new HashSet<CodeElementType>();
		List<AbstractStatement> nonTempAbstractStatements = new ArrayList<AbstractStatement>();
		//codeElementTypeSet.add(CodeElementType.SINGLE_VARIABLE_DECLARATION);
		//codeElementTypeSet.add(CodeElementType.VARIABLE_DECLARATION_EXPRESSION);
		codeElementTypeSet.add(CodeElementType.VARIABLE_DECLARATION_STATEMENT);
		codeElementTypeSet.add(CodeElementType.RETURN_STATEMENT);//Considering return statements as Temp
		for(AbstractStatement statement : abstractStatements) {
			CodeElementType statementType = statement.getLocationInfo().getCodeElementType();
			if(!codeElementTypeSet.contains(statementType)) {
				nonTempAbstractStatements.add(statement);														
			}
		}
		if(nonTempAbstractStatements.size() == 0 ) {
			return true;
		}else {
			return false;
		}
	}
	private boolean isExtractFacilitateExtension(Refactoring ref , List<Refactoring> refList){
		if( ref instanceof ExtractOperationRefactoring){
			ExtractOperationRefactoring extractOperationRefactoring = (ExtractOperationRefactoring)ref;
			UMLOperationBodyMapper umlBodyMapper = extractOperationRefactoring.getBodyMapper();
			UMLOperation extractedOperation = extractOperationRefactoring.getExtractedOperation();
			UMLOperation sourceOperationAfterExtrction = extractOperationRefactoring.getExtractedOperation();
			int countChildNonMappedLeavesAndInnerNodesT2 = 0;
			int countParentNonMappedLeavesAndInnerNodesT2 = 0;
			
			List<CompositeStatementObject> listParentT2CompositesWithInvokationsToExtractedMethodInExpression = new ArrayList<CompositeStatementObject>();
			List<CompositeStatementObject> listParentNeutralInnerNodes = new ArrayList<CompositeStatementObject>();
			List<StatementObject> listParentNotMappedLeavesWithInvokationsToExtractedMethod = new ArrayList<StatementObject>();
			List<StatementObject> listParentNeutralLeaves = new ArrayList<StatementObject>();
			List<StatementObject> listChildNeutralLeaves = new ArrayList<StatementObject>();
			List<CompositeStatementObject> listChildNeutralInnerNodes = new ArrayList<CompositeStatementObject>();
			List<CompositeStatementObject> listChildT2CompositesWithInvokationsToExtractedMethodInExpression = new ArrayList<CompositeStatementObject>();
			List<StatementObject> listChildNotMappedLeavesWithInvokationsToExtractedMethod = new ArrayList<StatementObject>();

			List<StatementObject> listNotMappedLeavesT2 = umlBodyMapper.getNonMappedLeavesT2();
			List<CompositeStatementObject> listNotMappedInnerNodesT2 = umlBodyMapper.getNonMappedInnerNodesT2();
			List<StatementObject> parentListNotMappedLeavesT2 = umlBodyMapper.getParentMapper().getNonMappedLeavesT2();
			List<CompositeStatementObject> parentListNotMappedInnerNodesT2 = umlBodyMapper.getParentMapper().getNonMappedInnerNodesT2();
			
			Set<StatementObject> setParentMarkedT2Leaves = new HashSet<StatementObject>();
			Set<CompositeStatementObject> setParentMarkedT2InnerNodes = new HashSet<CompositeStatementObject>();
			Set<StatementObject> setChildMarkedT2Leaves = new HashSet<StatementObject>();
			Set<CompositeStatementObject> setChildMarkedT2InnerNodes = new HashSet<CompositeStatementObject>();
			
			/* Lists for detecting and excluding the cases in which the T2 node (parent/child - leaf/InnerNode) includes statements that are in T1 by error(Deleted nodes)
			 *  Example: Facebook/buck:f26d23 , structr:6c5905
			 */
			List<StatementObject> listNotMappedleafNodesT1 = umlBodyMapper.getNonMappedLeavesT1();
			List<CompositeStatementObject> listNotMappedInnerNodesT1 = umlBodyMapper.getNonMappedInnerNodesT1();
			List<StatementObject> parentListNotMappedleafNodesT1 = umlBodyMapper.getParentMapper().getNonMappedLeavesT1();
			List<CompositeStatementObject> parentListNotMappedInnerNodesT1 = umlBodyMapper.getParentMapper().getNonMappedInnerNodesT1();
			List<CompositeStatementObject> listParentT2InnerNodeInT1InnerNodes = new ArrayList<CompositeStatementObject>();
			List<StatementObject> listParentT2LeafNodeInT1LeafNodes = new ArrayList<StatementObject>();
			List<CompositeStatementObject> listChildT2InnerNodeInT1InnerNodes = new ArrayList<CompositeStatementObject>();
			List<StatementObject> listChildT2LeafNodeInT1LeafNodes = new ArrayList<StatementObject>();
			
			//Processing Parent (Source Operation After Extraction) T2 Inner(Composite)/Leaf Nodes to filter out  marked nodes
			for(CompositeStatementObject  notMappedCompositeNode :  parentListNotMappedInnerNodesT2) {
				
				if(isCompositeNodeExpressionContainingInvokationsToExtractedMethods(notMappedCompositeNode , refList)) {
					listParentT2CompositesWithInvokationsToExtractedMethodInExpression.add(notMappedCompositeNode);
				}
				if(IsNeutralNodeForFacilitateExtension(notMappedCompositeNode , refList , sourceOperationAfterExtrction)) {
					listParentNeutralInnerNodes.add(notMappedCompositeNode);
				}	
				if(isParentT2InnerNodeinT1InnerNodes(notMappedCompositeNode.toString(), parentListNotMappedInnerNodesT1)) {
					listParentT2InnerNodeInT1InnerNodes.add(notMappedCompositeNode);
				}
			}
			setParentMarkedT2InnerNodes.addAll(listParentT2CompositesWithInvokationsToExtractedMethodInExpression);
			setParentMarkedT2InnerNodes.addAll(listParentNeutralInnerNodes);
			setParentMarkedT2InnerNodes.addAll(listParentT2InnerNodeInT1InnerNodes);
			
			for(StatementObject  notMappedNode :  parentListNotMappedLeavesT2) {
	
				if(isLeafNodeContainingInvokationsToExtractedMethods(notMappedNode, refList)) {
					listParentNotMappedLeavesWithInvokationsToExtractedMethod.add(notMappedNode);
				}
				if(IsNeutralNodeForFacilitateExtension(notMappedNode , refList , sourceOperationAfterExtrction)) {
					listParentNeutralLeaves.add(notMappedNode);
				}
				if(isParentT2LeafNodeinT1leafNodes(notMappedNode.toString(), parentListNotMappedleafNodesT1)) {
					listParentT2LeafNodeInT1LeafNodes.add(notMappedNode);
				}
			}
			setParentMarkedT2Leaves.addAll(listParentNotMappedLeavesWithInvokationsToExtractedMethod);
			setParentMarkedT2Leaves.addAll(listParentNeutralLeaves);
			setParentMarkedT2Leaves.addAll(listParentT2LeafNodeInT1LeafNodes);
			
			//Processing Child (Extracted Operation) T2 Inner(Composite)/Leaf Nodes to filter out  marked nodes
			for(CompositeStatementObject  notMappedCompositeNode : listNotMappedInnerNodesT2) {
				
				if(isCompositeNodeExpressionContainingInvokationsToExtractedMethods(notMappedCompositeNode , refList)) {
					listChildT2CompositesWithInvokationsToExtractedMethodInExpression.add(notMappedCompositeNode);
				}
				if(IsNeutralNodeForFacilitateExtension(notMappedCompositeNode ,refList, extractedOperation)) {
					listChildNeutralInnerNodes.add(notMappedCompositeNode);
				}
				if(isChildT2InnerNodeinT1InnerNodes(notMappedCompositeNode.toString(), listNotMappedInnerNodesT1)) {
					listChildT2InnerNodeInT1InnerNodes.add(notMappedCompositeNode);
				}
			}
			setChildMarkedT2InnerNodes.addAll(listChildT2CompositesWithInvokationsToExtractedMethodInExpression);
			setChildMarkedT2InnerNodes.addAll(listChildNeutralInnerNodes);
			setChildMarkedT2InnerNodes.addAll(listChildT2InnerNodeInT1InnerNodes);
			
			for(StatementObject  notMappedNode :  listNotMappedLeavesT2) {
				if(isLeafNodeContainingInvokationsToExtractedMethods(notMappedNode, refList)) {
					listChildNotMappedLeavesWithInvokationsToExtractedMethod.add(notMappedNode);
				}
				if(IsNeutralNodeForFacilitateExtension(notMappedNode, refList, extractedOperation)){
					listChildNeutralLeaves.add(notMappedNode);
				}
				if(isChildT2LeafNodeinT1leafNodes(notMappedNode.toString(), listNotMappedleafNodesT1)) {
					listChildT2LeafNodeInT1LeafNodes.add(notMappedNode);
				}
			}
			setChildMarkedT2Leaves.addAll(listChildNotMappedLeavesWithInvokationsToExtractedMethod);
			setChildMarkedT2Leaves.addAll(listChildNeutralLeaves);
			setChildMarkedT2Leaves.addAll(listChildT2LeafNodeInT1LeafNodes);
			
			//Computing  filtered nodes (Nodes that facilitate extension)
			int filterdListNotMappedInnerNodesT2 = listNotMappedInnerNodesT2.size()-setChildMarkedT2InnerNodes.size();
			int filteredListNotMappedLeavesT2 = listNotMappedLeavesT2.size()-setChildMarkedT2Leaves.size();
			countChildNonMappedLeavesAndInnerNodesT2 =  filterdListNotMappedInnerNodesT2+filteredListNotMappedLeavesT2;
			
			int filterdParentListNotMappedInnerNodesT2 = parentListNotMappedInnerNodesT2.size()-setParentMarkedT2InnerNodes.size();
            int filteredParentListNotMappedLeavesT2 = parentListNotMappedLeavesT2.size()-setParentMarkedT2Leaves.size();
			 countParentNonMappedLeavesAndInnerNodesT2 = filterdParentListNotMappedInnerNodesT2 + filteredParentListNotMappedLeavesT2;
		 	 
			 //DETECTION RULE: Detect if Some statements(InnerNode or Leave) added either ExtractedOperation or
			//Source Operation After Extraction
			if( countChildNonMappedLeavesAndInnerNodesT2 > 0 || countParentNonMappedLeavesAndInnerNodesT2 > 0) {
				if(!isMotivationDetected(ref, MotivationType.EM_INTRODUCE_ALTERNATIVE_SIGNATURE) &&
						!isMotivationDetected(ref, MotivationType.EM_REPLACE_METHOD_PRESERVING_BACKWARD_COMPATIBILITY)) {
					return true;
				}
			}
		}
		return false;
	}
	private boolean isLeafNodeContainingInvokationsToExtractedMethods(StatementObject notMappedNode,List<Refactoring> refList) {
		for(Refactoring refactoring : refList) {
			if(refactoring instanceof ExtractOperationRefactoring) {
				ExtractOperationRefactoring extractOperationRefactoring = (ExtractOperationRefactoring)refactoring;
				for(OperationInvocation invocation : extractOperationRefactoring.getExtractedOperationInvocations()) {
					if(notMappedNode.getLocationInfo().subsumes(invocation.getLocationInfo())){
						return true;
					}
				}
			}
		}
		return false;
	}
	private boolean isCompositeNodeExpressionContainingInvokationsToExtractedMethods(CompositeStatementObject notMappedCompositeNode , 
			List<Refactoring> refList) {
		for(AbstractExpression expression: notMappedCompositeNode.getExpressions()) {
			/*The loop around different Extract refactoring helps omit all the call to different extracted operations
				from the source operation after extraction*/
			for(Refactoring refactoring : refList) {
				if(refactoring instanceof ExtractOperationRefactoring) {
					ExtractOperationRefactoring extractOperationRefactoring = (ExtractOperationRefactoring)refactoring;
					for(OperationInvocation invocation : extractOperationRefactoring.getExtractedOperationInvocations()) {
						if(expression.getLocationInfo().subsumes(invocation.getLocationInfo())) {
							return true;								
						}			
					}
				}
			}
		}
		return false;
	}
	private boolean isParentT2InnerNodeinT1InnerNodes(String strParentT2InnerNode,List<CompositeStatementObject> parentT1InnerNodes){

		for(CompositeStatementObject  notMappedCompositeNode :  parentT1InnerNodes) {
			if(notMappedCompositeNode.getString().equals(strParentT2InnerNode)) {
				return true;
			}
		}
		return false;	
	}
	private boolean isParentT2LeafNodeinT1leafNodes(String strParentT2Leave,List<StatementObject> parentLeavesT1){
		for(StatementObject  notMappedNode :  parentLeavesT1) {
			if(notMappedNode.getString().equals(strParentT2Leave)) {
				return true;
			}
		}
		return false;
	}
	private boolean isChildT2InnerNodeinT1InnerNodes(String strChildT2InnerNode,List<CompositeStatementObject> childT1InnerNodes){
		for(CompositeStatementObject  notMappedCompositeNode :  childT1InnerNodes) {
			if(notMappedCompositeNode.getString().equals(strChildT2InnerNode)) {
				return true;
			}
		}
		return false;	
	}
	private boolean isChildT2LeafNodeinT1leafNodes(String strChildT2Leaf,List<StatementObject> childLeavesT1){
		for(StatementObject  notMappedNode :  childLeavesT1) {
			if(notMappedNode.getString().equals(strChildT2Leaf)) {
				return true;
			}
		}
		return false;
	}
	
	private boolean IsNeutralNodeForFacilitateExtension(AbstractStatement statement , List<Refactoring> refList , UMLOperation statementOperation){
		Set<CodeElementType> neutralCodeElements = new HashSet<CodeElementType>();
		neutralCodeElements.add(CodeElementType.VARIABLE_DECLARATION_STATEMENT);
		neutralCodeElements.add(CodeElementType.RETURN_STATEMENT);
		neutralCodeElements.add(CodeElementType.BLOCK);
		List<OperationInvocation> invokationsToExtractedMethods = new ArrayList<OperationInvocation>();
		List<OperationInvocation> statementInvokationsOtherThanExtractedOperationsCalls = new ArrayList<OperationInvocation>(); 
		CodeElementType elementType  = statement.getLocationInfo().getCodeElementType();
		for(CodeElementType type: neutralCodeElements) {
			if(type.equals(elementType)) {
				if(elementType.equals(CodeElementType.VARIABLE_DECLARATION_STATEMENT)) {
					Map<String, List<OperationInvocation>> mapStatementInvokations = statement.getMethodInvocationMap();
					if(mapStatementInvokations.isEmpty()) {
						//There is no invokations in the variable declaration statement
						return true;
					} else {
						for(String invokationString : mapStatementInvokations.keySet()) {
							List<OperationInvocation> statementInvokations = mapStatementInvokations.get(invokationString);
							for (OperationInvocation invokation: statementInvokations) {
								for(Refactoring ref : refList) {
									ExtractOperationRefactoring extractOperationRefactoring = (ExtractOperationRefactoring)ref;
									if(invokation.matchesOperation(extractOperationRefactoring.getExtractedOperation(), statementOperation.variableTypeMap(),modelDiff)){
										invokationsToExtractedMethods.add(invokation);
									}else {
										statementInvokationsOtherThanExtractedOperationsCalls.add(invokation);
									}
								}
							}
						}
						if(invokationsToExtractedMethods.isEmpty()) {
							//There are invokations other than the ones to the Extracted methods 
							//which can be considered as extension.
							if(statementInvokationsOtherThanExtractedOperationsCalls.isEmpty()) {
								return true;
							}else {
								return false;
							}
						}else {
							return false;
						}
					}

				}
				return true;
			}
		}
		return false;
		}

	private boolean isExtractReusableMethod(Refactoring ref) {		
		
		if(ref instanceof ExtractOperationRefactoring) {
			ExtractOperationRefactoring extractOpRefactoring = (ExtractOperationRefactoring)ref;
			UMLOperation sourceOperationAfterExtraction = extractOpRefactoring.getSourceOperationAfterExtraction();
			UMLOperation extractedOperation = extractOpRefactoring.getExtractedOperation();
			List<OperationInvocation> sourceOperationAfterExtractionInvokations = sourceOperationAfterExtraction.getAllOperationInvocations();
			int extractOperationInvocationCount = 0 ;
			for(UMLClassDiff classDiff : modelDiff.getCommonClassDiffList()) {
				if(classDiff != null) {
					UMLClass nextClass = classDiff.getNextClass();
					extractOperationInvocationCount += extractedOperationInvocationsCountInClass(extractOpRefactoring, nextClass);
					}
				}
			for(UMLClass addedClass : modelDiff.getAddedClasses()) {
				extractOperationInvocationCount += extractedOperationInvocationsCountInClass(extractOpRefactoring, addedClass);
				}
			
			int extractedOperationCallsInsideSourceOperationAfterExtraction = 0 ;
			for(OperationInvocation invokation : sourceOperationAfterExtractionInvokations) {
				if(invokation.matchesOperation(extractedOperation,sourceOperationAfterExtraction.variableTypeMap(), modelDiff)) {
					extractedOperationCallsInsideSourceOperationAfterExtraction++;
				}
			}
			/*Rule Exception: Extract is not reusable when there is only one call to extracted operation from source after extraction
			* and Call is outside source operation and outside extractOperationInvocationCount is one.
			* Example(Nested Extract Refactorings): Checkstyle :5a9b7 , JGroups:f1533
			*/
			if((extractOpRefactoring.getExtractedOperationInvocations().size() == 1) 
					&& (extractedOperationCallsInsideSourceOperationAfterExtraction == 0 )) {
				if(extractOperationInvocationCount == 1) {
					return false;
				}
			}
			/* GENERAL DETECTION RULE:
			 * IF Invocations to Extracted method from source method after Extraction is more than one OR 
			 *  there are other Invocations from other methods to extracted operation.
			 *  Invocations inside test methods will be considered as reusable calls. */

			if(extractOpRefactoring.getExtractedOperationInvocations().size()>1 || extractOperationInvocationCount > 0 ) {
				/*AND if the extraction operation is not detected as duplicated removal before
				 * (All Extract Operations that tend to remove duplication are also reused.)*/
				if(!isMotivationDetected(ref, MotivationType.EM_REMOVE_DUPLICATION)) {
					return true;
				}
			}		
		}
		return false;	
	}

	private int extractedOperationInvocationsCountInClass(ExtractOperationRefactoring extractOpRefactoring, UMLClass nextClass) {
		int countInvocations = 0;
		for(UMLOperation operation : nextClass.getOperations()) {
			UMLOperation extractedOperation = extractOpRefactoring.getExtractedOperation();
			UMLOperation sourceOperationAfterExtration = extractOpRefactoring.getSourceOperationAfterExtraction();
			//check if other operations in  class calls the extracted method
			if(!operation.equals(sourceOperationAfterExtration) && !operation.equals(extractedOperation) &&
					!operation.hasTestAnnotation() && !operation.getName().startsWith("test")) {
				List<OperationInvocation> invocations = operation.getAllOperationInvocations();
				for(OperationInvocation invocation : invocations) {
					if(invocation.matchesOperation(extractedOperation,operation.variableTypeMap(), modelDiff)) {
						List<UMLOperation> listEqualOperations = getAllEqualOperations(extractedOperation);
						if(listEqualOperations.size() == 0) {
							countInvocations++;	
						}
					}
				}
			}
		}
		return countInvocations;
	}
	
	private List<UMLOperation> getAllEqualOperations(UMLOperation umlOperation) {
		List<UMLOperation> listEqualOperations = new ArrayList<UMLOperation>();
		UMLClassBaseDiff umlClassDiff = modelDiff.getUMLClassDiff(umlOperation.getClassName());
		UMLClass umlClass = umlClassDiff.getNextClass();
		for(UMLClassDiff classDiff : modelDiff.getCommonClassDiffList()) {
			if(classDiff != null) {
				UMLClass nextCommonClass = classDiff.getNextClass();
				if(nextCommonClass.matchOperation(umlOperation) != null && !nextCommonClass.equals(umlClass)) {
						listEqualOperations.add(umlOperation);	
				}
			}
			for(UMLClass addedClass : modelDiff.getAddedClasses() ) {
					if(addedClass.matchOperation(umlOperation) != null && !addedClass.equals(umlClass)) {
						listEqualOperations.add(umlOperation);
				}
			}
		}
		return listEqualOperations;
	}
	
	private boolean isDecomposeMethodToImroveReadability(List<Refactoring> refList) {
		
		Map<UMLOperation, List<ExtractOperationRefactoring>> extractOperationMapWithSourceOperationAsKey = new HashMap<>();
		for (Refactoring ref : refList) {
			if(ref instanceof ExtractOperationRefactoring) {
				ExtractOperationRefactoring extractOpRefactoring = (ExtractOperationRefactoring)ref;
				UMLOperation sourceOperation = extractOpRefactoring.getSourceOperationBeforeExtraction();
				if(extractOperationMapWithSourceOperationAsKey.containsKey(sourceOperation)) {
					extractOperationMapWithSourceOperationAsKey.get(sourceOperation).add(extractOpRefactoring);
				}
				else {
					List<ExtractOperationRefactoring> list = new ArrayList<ExtractOperationRefactoring>();
					list.add(extractOpRefactoring);
					extractOperationMapWithSourceOperationAsKey.put(sourceOperation, list);
				}
			}
		}
		int countDecomposeMethodToImproveReadability = 0;
		for(UMLOperation key : extractOperationMapWithSourceOperationAsKey.keySet()) {
			List<ExtractOperationRefactoring> list = extractOperationMapWithSourceOperationAsKey.get(key);
			/*DETECTION RULE: if multiple extract operations have the same source Operation
			 *  the extract operations motivations is Decompose to Improve Readability
			 */
			if(list.size() > 1) {
				for(ExtractOperationRefactoring ref : list) {
					//Set Motivation for each refactoring with the same source Operation
					setRefactoringMotivation(MotivationType.EM_DECOMPOSE_TO_IMPROVE_READABILITY,  ref);
					countDecomposeMethodToImproveReadability++;
				}
			}
		}
		if(countDecomposeMethodToImproveReadability >= 2) {
			return true;
		}
		/*Checking source operation after Extraction calls to the extracted method to see if they improve readability
		/in case of calls inside expressions or inside return statements we consider the extraction is improving the readability
		 * Example: mockito:2d036 , jedis:d4b4a , cassandra:9a3fa ,JetBrains/intellij-community/commit/7dd55
		 */
		for (Refactoring ref : refList) {
			if(isExtractedOperationInvokationsToImproveReadability(ref)) {
				setRefactoringMotivation(MotivationType.EM_DECOMPOSE_TO_IMPROVE_READABILITY,  ref);
				return true;
			}
		}
		return false;	
	}
	private boolean isExtractedOperationInvokationsToImproveReadability(Refactoring ref) {
		if(ref instanceof ExtractOperationRefactoring) {
			ExtractOperationRefactoring extractOperationRef = (ExtractOperationRefactoring)ref;
			UMLOperation sourceOperationAfterExtraction = extractOperationRef.getSourceOperationAfterExtraction();
			UMLOperation extratedOperation = extractOperationRef.getExtractedOperation();
			List<StatementObject> listReturnStatementswithCallsToExtractedOperation = getStatementsCallingExtractedOperation(
					extractOperationRef, CodeElementType.RETURN_STATEMENT);
			List<StatementObject> listVariableDeclarationStatementsWithCallsToExtractedOperation = getStatementsCallingExtractedOperation(
					extractOperationRef, CodeElementType.VARIABLE_DECLARATION_STATEMENT);
			List<AbstractExpression> expressionsUsingVariableInitializedWithExtracedOperationInvocation = new ArrayList<AbstractExpression>();
			CompositeStatementObject sourceOperationAfterExtractionBody = sourceOperationAfterExtraction.getBody().getCompositeStatement();
			for(StatementObject statement : listVariableDeclarationStatementsWithCallsToExtractedOperation) {
				for(VariableDeclaration declaration : statement.getVariableDeclarations()) {
					expressionsUsingVariableInitializedWithExtracedOperationInvocation.addAll(getAllCompositeStatementObjectExpressionsUsingVariable(sourceOperationAfterExtractionBody, declaration.getVariableName()));
				}
			}
			/*Check all expressions of the source operation after extraction to see if there is any calls to extracted operation
			 *Check if any expression exists with Variables initialized with invokations to the extracted operation
			 *Check if any return statements exists with calls to the extracted operation
			 */
			if(getAllCompositeStatementObjectExpressionsWithInvokationsToExtractedOperation(sourceOperationAfterExtractionBody, extratedOperation, sourceOperationAfterExtraction).size() > 0 ||
					expressionsUsingVariableInitializedWithExtracedOperationInvocation.size() > 0 ||
					listReturnStatementswithCallsToExtractedOperation.size() > 0) {
				return true;
			}
		}	
		return false;
	}

	private List<StatementObject> getStatementsCallingExtractedOperation(ExtractOperationRefactoring extractOperationRef, CodeElementType codeElementType) {
		List<StatementObject> statementswithCallsToExtractedOperation = new ArrayList<StatementObject>();
		OperationBody sourceOperationBody = extractOperationRef.getSourceOperationAfterExtraction().getBody();
		CompositeStatementObject compositeStatement =  sourceOperationBody.getCompositeStatement();
		//Check statements to see if they have calls to extracted operation
		List<StatementObject> listStatementObjects =  compositeStatement.getLeaves();
		for(StatementObject statement : listStatementObjects) {
			CodeElementType type = statement.getLocationInfo().getCodeElementType();
			if(type.equals(codeElementType)) {
				for(OperationInvocation invokation : extractOperationRef.getExtractedOperationInvocations()) {
					if(statement.getLocationInfo().subsumes(invokation.getLocationInfo())){
						statementswithCallsToExtractedOperation.add(statement);
						break;
					}	
				}
			}
		}
		return statementswithCallsToExtractedOperation;
	}
	
	private List<AbstractExpression> getAllCompositeStatementObjectExpressionsWithInvokationsToExtractedOperation(CompositeStatementObject compositeStatement ,
			UMLOperation invokedOperation , UMLOperation sourceOperationAfterExtraction){
		List<AbstractExpression> listAbstractExpressions = compositeStatement.getExpressions();
		List<AbstractExpression> listExpressionsWithCallToExtractedOperation = new ArrayList<AbstractExpression>();
		Map<String, List<OperationInvocation>> mapMerthodInvokations = new HashMap<String, List<OperationInvocation>>();
		for(AbstractExpression expression : listAbstractExpressions) {
			mapMerthodInvokations = expression.getMethodInvocationMap();
			for(String invokationString : mapMerthodInvokations.keySet()) {
				List<OperationInvocation> listInvokations = mapMerthodInvokations.get(invokationString);
				for(OperationInvocation invokation : listInvokations) {
					if(invokation.matchesOperation(invokedOperation , sourceOperationAfterExtraction.variableTypeMap(), modelDiff)) {
						listExpressionsWithCallToExtractedOperation.add(expression);
					}
				}
			}
		}
		List<AbstractStatement> listStatements = compositeStatement.getStatements();
		for(AbstractStatement statement : listStatements) {
			if(statement instanceof CompositeStatementObject) {
				CompositeStatementObject composite = (CompositeStatementObject)statement;
				listExpressionsWithCallToExtractedOperation.addAll(getAllCompositeStatementObjectExpressionsWithInvokationsToExtractedOperation(composite , invokedOperation , sourceOperationAfterExtraction));
			}
		}
		
		return listExpressionsWithCallToExtractedOperation;
	}

	private List<AbstractExpression> getAllCompositeStatementObjectExpressionsUsingVariable(CompositeStatementObject compositeStatement ,
			String variableName){
		List<AbstractExpression> listAbstractExpressions = compositeStatement.getExpressions();
		List<AbstractExpression> listExpressionsUsingVariable = new ArrayList<AbstractExpression>();
		for(AbstractExpression expression : listAbstractExpressions) {
			if(expression.getVariables().contains(variableName)) {
				listExpressionsUsingVariable.add(expression);
			}
		}
		List<AbstractStatement> listStatements = compositeStatement.getStatements();
		for(AbstractStatement statement : listStatements) {
			if(statement instanceof CompositeStatementObject) {
				CompositeStatementObject composite = (CompositeStatementObject)statement;
				listExpressionsUsingVariable.addAll(getAllCompositeStatementObjectExpressionsUsingVariable(composite , variableName));
			}
		}
		
		return listExpressionsUsingVariable;
	}
	
	private boolean isMethodExtractedToRemoveDuplication(List<Refactoring> refList) {
		Map<UMLOperation, List<ExtractOperationRefactoring>> sourceOperationMapWithExtractedOperationAsKey = new HashMap<>();
		
		for (Refactoring ref : refList){
			if(ref instanceof ExtractOperationRefactoring) {
				ExtractOperationRefactoring extractOpRefactoring = (ExtractOperationRefactoring) ref;
				UMLOperation extractedOperation = extractOpRefactoring.getExtractedOperation();
				if(sourceOperationMapWithExtractedOperationAsKey.containsKey(extractedOperation))
					sourceOperationMapWithExtractedOperationAsKey.get(extractedOperation).add(extractOpRefactoring);
				else{
					List<ExtractOperationRefactoring> listExtractOperation = new ArrayList<ExtractOperationRefactoring>();
					listExtractOperation.add(extractOpRefactoring);
					sourceOperationMapWithExtractedOperationAsKey.put(extractedOperation, listExtractOperation);
				}
			}			
		}
		int countRemoveDuplicationExtractRefactorings = 0;
		for(UMLOperation extractOperation : sourceOperationMapWithExtractedOperationAsKey.keySet()){
			List<ExtractOperationRefactoring> listRepetativeExtractOperations = new ArrayList<ExtractOperationRefactoring>();
			List<ExtractOperationRefactoring> listSourceOperations = sourceOperationMapWithExtractedOperationAsKey.get(extractOperation);	
			/*DETECTION RULE: if multiple source operations(The Extract Refactorings that contain them)
			 *  have the same extractedOperation the extract operations motivations is Remove Duplication*/
			if(listSourceOperations.size() > 1){
				for (int i = 0; i < listSourceOperations.size(); i++) {
					for (int j = i+1; j <listSourceOperations.size() ; j++) {
						if(listSourceOperations.get(i).toString().equals(listSourceOperations.get(j).toString())){
							listRepetativeExtractOperations.add(listSourceOperations.get(i));
						}
					}
				}
				//Removing the repetitive source methods. Example: hazelcast 679d38d
				if(listRepetativeExtractOperations.size() >= 1) {
						listSourceOperations.removeAll(listRepetativeExtractOperations);	
				}
				if(listSourceOperations.size() > 1) {
					for(ExtractOperationRefactoring extractOp : listSourceOperations){
						setRefactoringMotivation(MotivationType.EM_REMOVE_DUPLICATION, extractOp);
						countRemoveDuplicationExtractRefactorings++;
					}
				}
			}				
		}
		if(countRemoveDuplicationExtractRefactorings >= 2) {
			return true;
		}
		return false;
	}
	
	private boolean isMotivationDetected(Refactoring ref, MotivationType type){
		
		List<MotivationType> listMotivations = mapRefactoringMotivations.get(ref);
		if(listMotivations == null) {
			return false;
		}
		else {
			if(listMotivations.contains(type)){
				return true;
			}
		}	
		return false;
	}
	private void setRefactoringMotivation(MotivationType motivationType, Refactoring ref) {
		if (mapRefactoringMotivations.containsKey(ref)){
			mapRefactoringMotivations.get(ref).add(motivationType);
		}
		else {
			List<MotivationType> listMotivations = new ArrayList<MotivationType>();
			listMotivations.add(motivationType);
			mapRefactoringMotivations.put(ref, listMotivations);
		}
	}
	
	public void classifyRefactoringsByType(List<Refactoring> refactorings) {
		for(Refactoring refactoring : refactorings) {
			RefactoringType type = refactoring.getRefactoringType();
			if(mapClassifiedRefactorings.containsKey(type)){
				mapClassifiedRefactorings.get(type).add(refactoring);
			}
			else {
				List<Refactoring> listRefactoring = new ArrayList<Refactoring>();
				listRefactoring.add(refactoring);
				mapClassifiedRefactorings.put(type,listRefactoring);
			}
		}
	}
	
	private void printDetectedRefactoringMotivations() {
		// TODO Auto-generated method stub	
	}
}