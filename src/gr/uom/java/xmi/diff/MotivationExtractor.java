package gr.uom.java.xmi.diff;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringType;

import gr.uom.java.xmi.LocationInfo.CodeElementType;
import gr.uom.java.xmi.UMLClass;
import gr.uom.java.xmi.UMLOperation;
import gr.uom.java.xmi.UMLParameter;
import gr.uom.java.xmi.UMLType;
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
		// TODO Auto-generated method stub
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
			if(isExtractFacilitateExtension(ref)){
				setRefactoringMotivation(MotivationType.EM_FACILITATE_EXTENSION, ref);
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
			
		}
		//Print All detected refactorings
		printDetectedRefactoringMotivations();			
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
			//Find All the Variable Declerations with same Type as return parameter
			Map<UMLType,String> variableTypeNameMap = new HashMap<UMLType, String>();
			for(VariableDeclaration variableDecleration: listVariableDeclerations){
				if(variableDecleration.getType().equalClassType(returnParameterType)) {
					variableTypeNameMap.put(variableDecleration.getType(), variableDecleration.getVariableName()); 
				}
			}
			//Check if return statement returns a variable of Return parameter type
			for(UMLType type : variableTypeNameMap.keySet()) {
				if( returnStatementVariables.size() == 1 && returnStatementVariables.contains(variableTypeNameMap.get(type))){
					return true;
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
								if(invocation.matchesOperation(extractedOperation)){
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
		if(operationsOverridingeExtractedOperations.size() > 0)
			return true;

		}
		return false;
	}

	private void isOperationOverridenInClass(UMLOperation extractedOperation, UMLClass extractedOperationNextClass,
			UMLClass nextClass, List<UMLOperation> operationsOverridingeExtractedOperations) {
		if(nextClass.isSubTypeOf(extractedOperationNextClass)){
			for(UMLOperation operation : nextClass.getOperations()) {
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
				isOperationTestedInClass(nextClass, extractedOperation, operationsTestingExtractedOperation);
			}
			for(UMLClass addedClass : modelDiff.getAddedClasses()) {
				isOperationTestedInClass(addedClass, extractedOperation, operationsTestingExtractedOperation);
			}
		}
		if(operationsTestingExtractedOperation.size() > 0)
			return true;
		return false;
	}

	private void isOperationTestedInClass(UMLClass nextClass, UMLOperation extractedOperation,
			List<UMLOperation> operationsTestingExtractedOperation) {
		for(UMLOperation operation : nextClass.getOperations()) {
			if(operation.hasTestAnnotation() || nextClass.isTestClass()) {
				for(OperationInvocation invocation : operation.getAllOperationInvocations()) {
					if(invocation.matchesOperation(extractedOperation, operation.variableTypeMap(), modelDiff)) {
						operationsTestingExtractedOperation.add(operation);
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
							if(invocation.matchesOperation(extractedOperation) && noOrThisExpresion){
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
			UMLOperation extractedOperation = extractOpRefactoring.getExtractedOperation();
			UMLOperation sourceOpAfterExtraction = extractOpRefactoring.getSourceOperationAfterExtraction();
			List<OperationInvocation> listExtractedOpInvokations = extractOpRefactoring.getExtractedOperationInvocations();
			/*DETECTION RULE: Check if source Operation after extraction is a delegate AND
			* IF the method parameters OR name has changed
			*/
			if( listExtractedOpInvokations.size() == 1 && 
					!sourceOpAfterExtraction.equalParameters(extractedOperation) ||
					!extractedOperation.getName().equals(sourceOpAfterExtraction.getName())){
				OperationBody sourceOpBodyAfterExtraction = sourceOpAfterExtraction.getBody();
				int countStatements = sourceOpBodyAfterExtraction.statementCount();
					if(countStatements == 1){
					return true;
				}
				else{
				/*TODO: Also Check if the source method after extraction has the @Deprecated annotation
				 * Temporary Variables should be excluded.
				 */
				}
			}
		}
		return false;
	}
	
	private boolean isIntroduceAlternativeMethodSignature(Refactoring ref) {
		if( ref instanceof ExtractOperationRefactoring){
			ExtractOperationRefactoring extractOpRefactoring = (ExtractOperationRefactoring)ref;
			UMLOperation extractedOperation = extractOpRefactoring.getExtractedOperation();
			UMLOperation sourceOpAfterExtraction = extractOpRefactoring.getSourceOperationAfterExtraction();
			//OperationInvocation Invocation = sourceOpAfterExtraction.isDelegate();
			List<OperationInvocation> listExtractedOpInvokations = extractOpRefactoring.getExtractedOperationInvocations();
				if( listExtractedOpInvokations.size() == 1 && !sourceOpAfterExtraction.equalParameters(extractedOperation)){
					OperationBody sourceOpBodyAfterExtraction = sourceOpAfterExtraction.getBody();
					int countStatements = sourceOpBodyAfterExtraction.statementCount();
 					if(countStatements == 1){
						return true;
					}
					else{
						//TODO:Temporary Variables should be excluded
						CompositeStatementObject compositeStatement = sourceOpBodyAfterExtraction.getCompositeStatement();
						List <AbstractStatement> listStatements = compositeStatement.getStatements();
						for(AbstractStatement statement : listStatements) {
						}
					}
				}
			}			
		return false;
	}
	
	private boolean isExtractFacilitateExtension(Refactoring ref){
		if( ref instanceof ExtractOperationRefactoring){
			ExtractOperationRefactoring extractRefactoring = (ExtractOperationRefactoring)ref;
			UMLOperationBodyMapper umlBodyMapper = extractRefactoring.getBodyMapper();
			int countNonMappedLeavesAndInnerNodesT1 = 0;
			int countNonMappedLeavesAndInnerNodesT2 = 0;
			int countExtractMethodAddedNodes = 0;
			int countParentNonMappedLeavesAndInnerNodesT1 = 0;
			int countParentNonMappedLeavesAndInnerNodesT2 = 0;
			int countParentExtractMethodAddedNodes = 0;
			List<StatementObject> listNotMappedLeavesT1 = umlBodyMapper.getNonMappedLeavesT1();
			List<CompositeStatementObject> listNotMappedInnerNodesT1 = umlBodyMapper.getNonMappedInnerNodesT1();
			List<StatementObject> listNotMappedLeavesT2 = umlBodyMapper.getNonMappedLeavesT2();
			List<CompositeStatementObject> listNotMappedInnerNodesT2 = umlBodyMapper.getNonMappedInnerNodesT2();
			List<StatementObject> parentListNotMappedLeavesT1 = umlBodyMapper.getParentMapper().getNonMappedLeavesT1();
			List<CompositeStatementObject> parentListNotMappedInnerNodesT1  = umlBodyMapper.getParentMapper().getNonMappedInnerNodesT1();
			List<StatementObject> parentListNotMappedLeavesT2 = umlBodyMapper.getParentMapper().getNonMappedLeavesT2();
			List<CompositeStatementObject> parentListNotMappedInnerNodesT2 = umlBodyMapper.getParentMapper().getNonMappedInnerNodesT2();
			countNonMappedLeavesAndInnerNodesT1 = listNotMappedInnerNodesT1.size()+listNotMappedLeavesT1.size();
			countNonMappedLeavesAndInnerNodesT2 = listNotMappedInnerNodesT2.size()+listNotMappedLeavesT2.size();
			countExtractMethodAddedNodes = countNonMappedLeavesAndInnerNodesT1 + countNonMappedLeavesAndInnerNodesT2; 
			countParentNonMappedLeavesAndInnerNodesT1 = parentListNotMappedInnerNodesT1.size()+parentListNotMappedLeavesT1.size();
			countParentNonMappedLeavesAndInnerNodesT2 = parentListNotMappedInnerNodesT2.size()+parentListNotMappedLeavesT2.size();
			countParentExtractMethodAddedNodes = countParentNonMappedLeavesAndInnerNodesT1 + countParentNonMappedLeavesAndInnerNodesT2;
			//DETECTION RULE: Detect if Some statements(InnerNode or Leave) added either ExtractedOperation or
			//Source Operation After Extraction
			if( countExtractMethodAddedNodes > 0 || countParentExtractMethodAddedNodes > 0) {
				if(!isMotivationDetected(ref, MotivationType.EM_INTRODUCE_ALTERNATIVE_SIGNATURE) &&
						!isMotivationDetected(ref, MotivationType.EM_REPLACE_METHOD_PRESERVING_BACKWARD_COMPATIBILITY))
				return true;
			}
		}
		return false;
	}
	
	
	
	

	private boolean isExtractReusableMethod(Refactoring ref) {		
		
		if(ref instanceof ExtractOperationRefactoring) {
			ExtractOperationRefactoring extractOpRefactoring = (ExtractOperationRefactoring)ref;
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
			/* DETECTION RULE:
			 * IF Invocations to Extracted method from source method after Extraction is more than one OR 
			 *  there are other Invocations from other methods to extracted operation*/
			if(extractOpRefactoring.getExtractedOperationInvocations().size()>1 || extractOperationInvocationCount > 0) {
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
			if(!operation.equals(sourceOperationAfterExtration) && !operation.equals(extractedOperation)) {
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
				//System.out.println("MOTIVAION : DECOMPOSE METHOD FOR READABILITY");
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
		return false;	
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
			List<ExtractOperationRefactoring> listSourceOperations = sourceOperationMapWithExtractedOperationAsKey.get(extractOperation);	
			/*DETECTION RULE: if multiple source operations(The Extract Refactorings that contain them)
			 *  have the same extractedOperation the extract operations motivations is Remove Duplication*/
			if(listSourceOperations.size() > 1){
				for(ExtractOperationRefactoring extractOp : listSourceOperations){
					setRefactoringMotivation(MotivationType.EM_REMOVE_DUPLICATION, extractOp);
					countRemoveDuplicationExtractRefactorings++;
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