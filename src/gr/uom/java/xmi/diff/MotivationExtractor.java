package gr.uom.java.xmi.diff;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.Annotation;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringType;

import gr.uom.java.xmi.LocationInfo;
import gr.uom.java.xmi.LocationInfo.CodeElementType;
import gr.uom.java.xmi.UMLAnonymousClass;
import gr.uom.java.xmi.UMLClass;
import gr.uom.java.xmi.UMLJavadoc;
import gr.uom.java.xmi.UMLOperation;
import gr.uom.java.xmi.UMLParameter;
import gr.uom.java.xmi.UMLTagElement;
import gr.uom.java.xmi.UMLType;
import gr.uom.java.xmi.decomposition.AbstractCodeFragment;
import gr.uom.java.xmi.decomposition.AbstractCodeMapping;
import gr.uom.java.xmi.decomposition.AbstractExpression;
import gr.uom.java.xmi.decomposition.AbstractStatement;
import gr.uom.java.xmi.decomposition.AnonymousClassDeclarationObject;
import gr.uom.java.xmi.decomposition.CompositeStatementObject;
import gr.uom.java.xmi.decomposition.ObjectCreation;
import gr.uom.java.xmi.decomposition.OperationBody;
import gr.uom.java.xmi.decomposition.OperationInvocation;
import gr.uom.java.xmi.decomposition.StatementObject;
import gr.uom.java.xmi.decomposition.TernaryOperatorExpression;
import gr.uom.java.xmi.decomposition.UMLOperationBodyMapper;
import gr.uom.java.xmi.decomposition.VariableDeclaration;

public class MotivationExtractor {
	private UMLModelDiff modelDiff;
	private List<Refactoring> refactorings;
	private Map<RefactoringType, List<Refactoring>> mapClassifiedRefactorings;
	private Map<Refactoring , List<MotivationType>> mapRefactoringMotivations;
	private List<ExtractOperationRefactoring> removeDuplicationFromSingleMethodRefactorings = new ArrayList<ExtractOperationRefactoring>();
	private List<ExtractOperationRefactoring> decomposeToImproveReadabilityFromSingleMethodRefactorings = new ArrayList<ExtractOperationRefactoring>();
	private List<ExtractOperationRefactoring> decomposeToImproveReadabilityFromSingleMethodByHavingCallToExtractedMethodInReturn = new ArrayList<ExtractOperationRefactoring>();
	private List<ExtractOperationRefactoring> facilitateExtensionRefactoringsWithExtrensionInParent = new ArrayList<ExtractOperationRefactoring>();

	

	
	private int countSingleMethodRemoveDuplications = 0;
	private Map<Refactoring, int[] > mapFacilitateExtensionT1T2  = new HashMap<Refactoring, int[]>() ;
	private Map<Refactoring, String> mapDecomposeToImproveRedability =  new HashMap<Refactoring,String>();
	public Map<Refactoring, String> getMapDecomposeToImproveRedability() {
		return mapDecomposeToImproveRedability;
		
	}

	public Map<Refactoring, int[]> getMapFacilitateExtensionT1T2() {
		return mapFacilitateExtensionT1T2;
	}

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
		for(Refactoring ref : listRef) {
			if(isExtractFacilitateExtension(ref,listRef)){
					setRefactoringMotivation(MotivationType.EM_FACILITATE_EXTENSION, ref);					
			}
			if(isIntroduceAlternativeMethodSignature(ref)) {
				if(!isMotivationDetected(ref, MotivationType.EM_REMOVE_DUPLICATION)) {
					setRefactoringMotivation(MotivationType.EM_INTRODUCE_ALTERNATIVE_SIGNATURE, ref);
					removeRefactoringMotivation(MotivationType.EM_FACILITATE_EXTENSION, ref);
				}
			}
			if(!isMotivationDetected(ref, MotivationType.EM_REMOVE_DUPLICATION)) {
				if(isExtractedToImproveTestability(ref)) {
					setRefactoringMotivation(MotivationType.EM_IMPROVE_TESTABILITY, ref);
					//e.g. : JetBrains/intellij-community:7ed3f2
					removeRefactoringMotivation(MotivationType.EM_DECOMPOSE_TO_IMPROVE_READABILITY, ref);
					removeRefactoringMotivation(MotivationType.EM_FACILITATE_EXTENSION, ref);
				}
			}
			//Check if facilitate extension happened in source operation after extraction (parent)
			if(facilitateExtensionRefactoringsWithExtrensionInParent.size() == 0) {
				if(isReplaceMethodPreservingBackwardCompatibility(ref)){
					if(!isMotivationDetected(ref, MotivationType.EM_REMOVE_DUPLICATION)) {
						setRefactoringMotivation(MotivationType.EM_REPLACE_METHOD_PRESERVING_BACKWARD_COMPATIBILITY, ref);
						removeRefactoringMotivation(MotivationType.EM_FACILITATE_EXTENSION, ref);
						removeRefactoringMotivation(MotivationType.EM_IMPROVE_TESTABILITY, ref);
						removeRefactoringMotivation(MotivationType.EM_INTRODUCE_ALTERNATIVE_SIGNATURE, ref);
					}
				}
			}
			if(isExtractReusableMethod(ref , listRef)) {
				if (!isMotivationDetected(ref, MotivationType.EM_REPLACE_METHOD_PRESERVING_BACKWARD_COMPATIBILITY)) {
					setRefactoringMotivation(MotivationType.EM_REUSABLE_METHOD, ref);
					//removeRefactoringMotivation(MotivationType.EM_FACILITATE_EXTENSION, ref);
				}
			}
			if(isExtractedtoEnableRecursion(ref)) {
				setRefactoringMotivation(MotivationType.EM_ENABLE_RECURSION, ref);
				//removeRefactoringMotivation(MotivationType.EM_FACILITATE_EXTENSION, ref);
			}
			if(isExtractedtoIntroduceAsyncOperation(ref)) {
				setRefactoringMotivation(MotivationType.EM_INTRODUCE_ASYNC_OPERATION, ref);
			}
			if(isExtractedToIntroduceFactoryMethod(ref)) {
				setRefactoringMotivation(MotivationType.EM_INTRODUCE_FACTORY_METHOD, ref);
				removeRefactoringMotivation(MotivationType.EM_FACILITATE_EXTENSION, ref);
				removeRefactoringMotivation(MotivationType.EM_REMOVE_DUPLICATION, ref);
				//Removing Reusable method when it is Introduce Factory method and Introduce alternative method signature.
				if(isMotivationDetected(ref, MotivationType.EM_INTRODUCE_ALTERNATIVE_SIGNATURE)) {
					removeRefactoringMotivation(MotivationType.EM_REUSABLE_METHOD, ref);
				}
			}
			if(isExtractedToEnableOverriding(ref)) {
				if(!isMotivationDetected(ref, MotivationType.EM_REMOVE_DUPLICATION)
						&& decomposeToImproveReadabilityFromSingleMethodRefactorings.size() == 0 ) {
					setRefactoringMotivation(MotivationType.EM_ENABLE_OVERRIDING, ref);
					removeRefactoringMotivation(MotivationType.EM_REUSABLE_METHOD, ref);
					removeRefactoringMotivation(MotivationType.EM_FACILITATE_EXTENSION, ref);
					removeRefactoringMotivation(MotivationType.EM_INTRODUCE_FACTORY_METHOD, ref);
					removeRefactoringMotivation(MotivationType.EM_DECOMPOSE_TO_IMPROVE_READABILITY, ref);
				}
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
		
		Map<String, List<ExtractOperationRefactoring>> listRefGroupedByExtractedOperationNames = 
				listRef.stream()
				.map(x->(ExtractOperationRefactoring)x)
				.collect(Collectors.groupingBy(x->x.getExtractedOperation().getName()));
		
		for(String extractedMethodgroupName : listRefGroupedByExtractedOperationNames.keySet()) {
			boolean noFacilitateExtension =false;
			for( ExtractOperationRefactoring extractOpRef : listRefGroupedByExtractedOperationNames.get(extractedMethodgroupName)) {
				if(!isMotivationDetected(extractOpRef, MotivationType.EM_FACILITATE_EXTENSION)) {
					noFacilitateExtension = true;
					 break;
				}
			}
			if(noFacilitateExtension) {
				for( ExtractOperationRefactoring extractOpRef : listRefGroupedByExtractedOperationNames.get(extractedMethodgroupName)) {
					removeRefactoringMotivation(MotivationType.EM_FACILITATE_EXTENSION, extractOpRef);
				}
			}
		}
	}
			
	private boolean isExtractedToIntroduceFactoryMethod(Refactoring ref) {
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
				if (statement.getLocationInfo().getCodeElementType().equals(CodeElementType.RETURN_STATEMENT)) {
					returnStatementVariables = statement.getVariables();
					returnStatementobjectCreationsMap = statement.getCreationMap();
					for(String objectCreationString : returnStatementobjectCreationsMap.keySet()) {
						//if(!isStatementInMappings(objectCreationString, extractOpRefactoring)) {
							List<ObjectCreation> listObjectCreation = returnStatementobjectCreationsMap.get(objectCreationString);
							for(ObjectCreation objectCreation: listObjectCreation) {
								String statementWithoutObjectCreation = statement.toString().replace(objectCreationString, "").replace(";","");
								boolean isOnlyObjectCreation = statementWithoutObjectCreation.trim().equals("return")?true:false;
								if(objectCreation.getType().equalClassType(returnParameterType) ||
										returnParameterType.equalsWithSubType(objectCreation.getType()) || isOnlyObjectCreation) {
									listReturnTypeObjectsCreatedInReturnStatement.add(objectCreation);
								}
							}	
						//}
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
						//ObjectCreation statement should not be in the mappings 
						//if(!isStatementInMappings(objectCreationString, extractOpRefactoring)) {
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
					//	}
					}			
				}
			}
			//Check if return statement returns a Variable Declerations(that are Object Creation) with Return parameter type
			for(UMLType type : variableTypeNameMap.keySet()) {
				List<String> variableNames = variableTypeNameMap.get(type);
				for(String variableName : variableNames) {
					if( returnStatementVariables.size() == 1 && returnStatementVariables.contains(variableName)){
						//Check if all statements in the Extracted Operation are object creation related
						if(isAllStatementsObjectCreationRelated(extractedOperation,listObjectCreationVariableDeclerationsWithReturnType))
						{
							return true;							
						}
					}	
				}
			}
		}
		return false;
	}
	private boolean isStatementInMappings(String statementString , ExtractOperationRefactoring  extractOpRef) {
		Set<AbstractCodeMapping> abstractCodeMappings = extractOpRef.getBodyMapper().getMappings();		
		for(AbstractCodeMapping abstractCodeMapping: abstractCodeMappings) {
			AbstractCodeFragment abstractCodeFragment = abstractCodeMapping.getFragment2();
			if(abstractCodeFragment.toString().contains(statementString)) {
				return true;
			}
		}
		return false;
	}
	private boolean isAllStatementsObjectCreationRelated (UMLOperation extractedOperation , List<VariableDeclaration> returnTypeObjectCreationVariableDeclerations ){
		
		List<VariableDeclaration> allVariableDeclerations = extractedOperation.getBody().getAllVariableDeclarations();
		List<String> allObjectCreationRelatedVariables = new ArrayList<String>();
		List<String> allObjectStateSettingVariables = new ArrayList<String>();
		Set<String> allFactoryMethodRelatedVariables = new HashSet<String>();
		
		if(returnTypeObjectCreationVariableDeclerations.size() == 1) {
			AbstractExpression returnTypeObjectCreationExpression = returnTypeObjectCreationVariableDeclerations.get(0).getInitializer();
			String objectCreationVariable = returnTypeObjectCreationVariableDeclerations.get(0).getVariableName();
			allObjectStateSettingVariables = getAllObjectStateSettingVariables(extractedOperation, objectCreationVariable);
			allObjectCreationRelatedVariables = getAllObjectCreationRelatedVariables(returnTypeObjectCreationExpression , allVariableDeclerations);
			allFactoryMethodRelatedVariables.addAll(allObjectCreationRelatedVariables);
			allFactoryMethodRelatedVariables.addAll(allObjectStateSettingVariables);
			allFactoryMethodRelatedVariables.addAll(getAllObjectCreationRelatedCompositeStatementVariables(extractedOperation,allFactoryMethodRelatedVariables));
			for(VariableDeclaration variableDecleration : allVariableDeclerations) {
				if(!variableDecleration.equals(returnTypeObjectCreationVariableDeclerations.get(0))) {
					//Check if there is a variable declaration that variable is not part of Object creation
					if(!allFactoryMethodRelatedVariables.contains(variableDecleration.getVariableName())) {
						return false;
					}
				}
			}
		}
		return true;
	}
	
	private Set<String> getAllObjectCreationRelatedCompositeStatementVariables(UMLOperation operation, Set<String> relatedVariables){
		List<CompositeStatementObject> compositeStatements = operation.getBody().getCompositeStatement().getInnerNodes();
		for(CompositeStatementObject compositeStatement : compositeStatements) {
			List<String> variables = compositeStatement.getVariables();
			List<AbstractExpression> abstractExpressions = compositeStatement.getExpressions();
			for(String variable: variables) {
				if(relatedVariables.contains(variable)) {
					for(AbstractExpression expression: abstractExpressions) {
						relatedVariables.addAll(expression.getVariables());
					}
				}	
			}
		}
		return relatedVariables ;
	}
	private List<String> getAllObjectStateSettingVariables(UMLOperation operation , String ObjectCreatianiVariableName){
		List<String> allObjectStateSettingVariables = new ArrayList<String>();
		ObjectCreatianiVariableName += ".";
		for(StatementObject statement : operation.getBody().getCompositeStatement().getLeaves()) {
			if(statement.toString().startsWith(ObjectCreatianiVariableName)) {
				allObjectStateSettingVariables.addAll(statement.getVariables());
			}
		}
		return allObjectStateSettingVariables;
	}
	private List<String> getAllObjectCreationRelatedVariables(AbstractExpression returnTypeObjectCreationExpression , List<VariableDeclaration> allVariableDeclerations){	
		List<String> returnTypeObjectCreationExpressionVariables = returnTypeObjectCreationExpression.getVariables();
		List<String> otherDependentObjectCreationVariables = getObjectCreationRelatedVariables(returnTypeObjectCreationExpressionVariables, allVariableDeclerations);
		returnTypeObjectCreationExpressionVariables.addAll(otherDependentObjectCreationVariables);
		return  returnTypeObjectCreationExpressionVariables ;
	}
	
	private List<String> getObjectCreationRelatedVariables(List<String> variables , List<VariableDeclaration> allVariableDeclerations){
		int intialVariableSize = variables.size();
		int newVariableVariableSize = 0;
		for(VariableDeclaration variableDecleration: allVariableDeclerations) {	
			String variableName = variableDecleration.getVariableName();
			if(variables.contains(variableName)){
				variables.addAll(variableDecleration.getInitializer().getVariables());
			}
		}
		newVariableVariableSize = variables.size();
		if(newVariableVariableSize> intialVariableSize) {
			variables.addAll(getObjectCreationRelatedVariables(variables ,allVariableDeclerations));
		}
		return  variables;
		
	}

	private boolean isExtractedtoIntroduceAsyncOperation(Refactoring ref) {

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
	
	private boolean isExtractedToEnableOverriding(Refactoring ref) {
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
			/* DETECTION RULE:
			 * 1-Check if any subclasses is overriding the extracted operation OR
			 * 2-Check the UML operation comments to see if it contains any keywords about overriding.
			 */
			if((operationsOverridingeExtractedOperations.size()>0) || isUmlOperationCommentUsingOverridingKeywords(extractedOperation)) {
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
	
	private boolean isUmlOperationCommentUsingOverridingKeywords(UMLOperation operation){
		UMLJavadoc javaDoc = operation.getJavadoc();
		if(javaDoc != null) {
			for(UMLTagElement tagElement : javaDoc.getTags()) {
				//Only process general purpose unnamed tags 
				 if(tagElement.getTagName() == null){
					 for(String fragment : tagElement.getFragments()) {
						 if (fragment.toLowerCase().contains("override") || fragment.toLowerCase().contains("overriding")||
								 fragment.toLowerCase().contains("overriden") || fragment.toLowerCase().contains("subclass")) { 
							 return true;
						 }
					 }
				 }
			}
		}
		return false;
	}
	
	private boolean isExtractedToImproveTestability(Refactoring ref) {
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

	private boolean isExtractedtoEnableRecursion(Refactoring ref) {
		if(ref instanceof ExtractOperationRefactoring) {
			ExtractOperationRefactoring extractOpRefactoring = (ExtractOperationRefactoring) ref;
			UMLOperation sourceOperationBeforeExtraction = extractOpRefactoring.getSourceOperationBeforeExtraction();
			UMLOperation extractedOperation = extractOpRefactoring.getExtractedOperation();
			if(!isUmlOperationRecursive(sourceOperationBeforeExtraction)) {
				if(isUmlOperationRecursive(extractedOperation)) {
					return true;
				}
			}
		}
		return false;
	}
	private boolean isUmlOperationRecursive(UMLOperation operation){
		List<OperationInvocation> listInvokations = operation.getAllOperationInvocations();
		List<OperationInvocation> recursiveInvokations = new ArrayList<OperationInvocation>();
		for( OperationInvocation invocation : listInvokations) {
			boolean noExpression = invocation.getExpression() == null;
			boolean thisExpression = invocation.getExpression() != null && invocation.getExpression().equals("this");
			boolean noOrThisExpresion = noExpression || thisExpression;
			if(invocation.matchesOperation(operation, operation.variableTypeMap(),modelDiff) && noOrThisExpresion){
				recursiveInvokations.add(invocation);
			}
		}
		if(recursiveInvokations.size()>0) {
			return true;
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
				if(isUmlOperationStatementsAllTempVariables(sourceOpAfterExtraction , extractOpRefactoring)) {
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
			boolean isSourceOperationAfterExtractionAndExtractedOperationModifiersProtectedOrPrivate = 
					sourceOperationAfterExtractionAccessModifier.equals("protected") || sourceOperationAfterExtractionAccessModifier.equals("private") ? true: false;
			/*DETECTION RULE: Check IF the method parameters OR name has changed AND if source Operation after extraction is a delegate
			 * AND also check if it contains @deprecated in annotations or JavaDoc 
			 */
			if(isBackwardCompatible && (listExtractedOpInvokations.size() == 1) && !isSourceOperationAfterExtractionAndExtractedOperationModifiersProtectedOrPrivate) {
				if(isUmlOperationWithDeprecatedAnnotation(sourceOpAfterExtraction) || isUmlOperationJavaDocContainsTagName(sourceOpAfterExtraction, "@deprecated")) {
					if(isMotivationDetected(ref, MotivationType.EM_INTRODUCE_ALTERNATIVE_SIGNATURE)) {
						removeRefactoringMotivation(MotivationType.EM_INTRODUCE_ALTERNATIVE_SIGNATURE, ref);
					}
					return true;	
				}else {
					// In this case introducing an alternative method has priority over backward compatibility if it is previously detected
					if(isMotivationDetected(ref, MotivationType.EM_INTRODUCE_ALTERNATIVE_SIGNATURE)) {
						return false;
					}else {
						return true;
					}
				} 
			}
		}
		return false;
	}
	private boolean isUmlOperationJavaDocContainsTagName(UMLOperation operation, String tagName) {
		UMLJavadoc javaDoc = operation.getJavadoc();
		if(javaDoc != null) {
			for(UMLTagElement tagElements : javaDoc.getTags()) {
				if(tagElements.getTagName() != null) {
					if(tagElements.getTagName().toLowerCase().equals("@deprecated")) {
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
				//Excluding cases with more than one invocation in source operation After extraction to extracted method
				List<OperationInvocation> countExtractedOperationInvocations = getAllOperationInvocationToExtractedMethod(sourceOpAfterExtraction, extractOpRefactoring.getExtractedOperation());
				if(countExtractedOperationInvocations.size() > 1) {
					return false;
				}
				//Temporary Variables should be excluded.
				if(isUmlOperationStatementsAllTempVariables(sourceOpAfterExtraction , extractOpRefactoring)) {
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
			boolean isEqualParameters = sourceOpAfterExtraction.equalParameters(extractedOperation);
			//boolean isEqualNames = extractedOperation.getName().equals(sourceOpAfterExtraction.getName());
			//boolean isEqualParametersDifferentNames = isEqualParameters && !isEqualNames ;
			boolean isToIntroduceAlternativeMethod = !isEqualParameters ?true:false;
			/*DETECTION RULE: Check IF the method parameters has changed AND if source Operation after extraction is a delegate 
			*/
			if(isToIntroduceAlternativeMethod && (listExtractedOpInvokations.size() == 1)) {
				//if(!isMotivationDetected(extractOpRefactoring, MotivationType.EM_REPLACE_METHOD_PRESERVING_BACKWARD_COMPATIBILITY)) {
					return true;

				//}
			}
		}
		return false;
	}
	
	private boolean isUmlOperationStatementsAllTempVariables(UMLOperation sourceOperationAfterExtraction , ExtractOperationRefactoring extracteOpRefactoring) {
		CompositeStatementObject compositeStatement = sourceOperationAfterExtraction.getBody().getCompositeStatement();
		List<AbstractStatement> abstractStatements = compositeStatement.getStatements();
		Set<CodeElementType> codeElementTypeSet = new HashSet<CodeElementType>();
		List<AbstractStatement> nonTempAbstractStatements = new ArrayList<AbstractStatement>();
		List<StatementObject> statementsCallingExtractedOperation = new ArrayList<StatementObject>();
		statementsCallingExtractedOperation.addAll(getStatementsCallingExtractedOperation(extracteOpRefactoring, CodeElementType.VARIABLE_DECLARATION_STATEMENT));	
		statementsCallingExtractedOperation.addAll(getStatementsCallingExtractedOperation(extracteOpRefactoring, CodeElementType.EXPRESSION_STATEMENT));
		codeElementTypeSet.add(CodeElementType.VARIABLE_DECLARATION_STATEMENT);
		//codeElementTypeSet.add(CodeElementType.RETURN_STATEMENT);//Considering return statements as Temp
		for(AbstractStatement statement : abstractStatements) {
			CodeElementType statementType = statement.getLocationInfo().getCodeElementType();
			boolean statementVariableIncludesParameterNames = isStatementUsingUMLOperationParametersNames(statement, sourceOperationAfterExtraction);
			boolean statementContainsInvocationToExtractedMethod = false;
			for( StatementObject statementObject : statementsCallingExtractedOperation) {
		    	if(statement.equals(statementObject)) {
		    		statementContainsInvocationToExtractedMethod = true;
		    		break;
		    	}
		    }			
			if(!codeElementTypeSet.contains(statementType) && !statementContainsInvocationToExtractedMethod /*&& !statementVariableIncludesParameterNames*/ ){
					nonTempAbstractStatements.add(statement);																			
				}
			}			
		if(nonTempAbstractStatements.size() == 0 ) {
			return true;
		}else {
			return false;
		}
	}
	
	private boolean isStatementUsingUMLOperationParametersNames(AbstractStatement statement , UMLOperation operation){			
			List<String> sourceOperationAfterExtarctionParameterNames = new ArrayList<String>();  
			List<UMLParameter> sourceOperationAfterExtarctionParameters = operation.getParametersWithoutReturnType();
			for(UMLParameter parameter : sourceOperationAfterExtarctionParameters) {
				sourceOperationAfterExtarctionParameterNames.add(parameter.getName());
			}
			List<String> variableNames = statement.getVariables();
			for (String variable : variableNames) {
				if(sourceOperationAfterExtarctionParameterNames.contains(variable)) {
					return true;
				}
			}
		return false;		
	}
	private List<OperationInvocation> getAllOperationInvocationToExtractedMethod(UMLOperation sourceOperationAfterExtraction , UMLOperation extractedOperation) {
		List<OperationInvocation> allExtractedMethodInvocations = new ArrayList<OperationInvocation>();
		for(OperationInvocation invocation : sourceOperationAfterExtraction.getAllOperationInvocations()) {
				if(invocation.matchesOperation(extractedOperation)) {
					allExtractedMethodInvocations.add(invocation);
				}
			}
		return allExtractedMethodInvocations; 
	}
	private boolean isStatementHavingInvocationsToExtractedOperation(AbstractStatement statement , UMLOperation extractedOperation) {
		for( String invocationString : statement.getMethodInvocationMap().keySet()) {
			for(OperationInvocation invocation : statement.getMethodInvocationMap().get(invocationString)) {
				if(invocation.matchesOperation(extractedOperation)) {
					return true;
				}
			}
		}
		return false; 
	}
		
	private boolean isExtractFacilitateExtension(Refactoring ref , List<Refactoring> refList){
		if( ref instanceof ExtractOperationRefactoring){
			ExtractOperationRefactoring extractOperationRefactoring = (ExtractOperationRefactoring)ref;
			UMLOperationBodyMapper umlBodyMapper = extractOperationRefactoring.getBodyMapper();
			UMLOperation extractedOperation = extractOperationRefactoring.getExtractedOperation();
			UMLOperation sourceOperationAfterExtrction = extractOperationRefactoring.getSourceOperationAfterExtraction();
			int countChildNonMappedLeavesAndInnerNodesT2 = 0;
			int countParentNonMappedLeavesAndInnerNodesT2 = 0;
			if(isExtractedMethodMappingAddedTernaryOperator(extractOperationRefactoring)) {
				return true;
			}
			List<CompositeStatementObject> listParentT2CompositesWithInvokationsToExtractedMethodInExpression = new ArrayList<CompositeStatementObject>();
			List<CompositeStatementObject> listParentNeutralInnerNodes = new ArrayList<CompositeStatementObject>();
			List<StatementObject> listParentNotMappedLeavesWithInvokationsToExtractedMethod = new ArrayList<StatementObject>();
			List<StatementObject> listParentNeutralLeaves = new ArrayList<StatementObject>();
			List<StatementObject> listChildNeutralLeaves = new ArrayList<StatementObject>();
			List<CompositeStatementObject> listChildNeutralInnerNodes = new ArrayList<CompositeStatementObject>();
			List<CompositeStatementObject> listChildT2CompositesWithInvokationsToExtractedMethodInExpression = new ArrayList<CompositeStatementObject>();
			List<StatementObject> listChildNotMappedLeavesWithInvokationsToExtractedMethod = new ArrayList<StatementObject>();
			List<StatementObject> listChildNotMappedLeavesWithRecursive = new ArrayList<StatementObject>();
			List<StatementObject> listChildNotMappedLeavesWithInvocationsExpressionsInOperationParameters = new ArrayList<StatementObject>();
			List<StatementObject> listChildNotMappedLeavesWithInvocationExpressionsInVariableNames = new ArrayList<StatementObject>();

			
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
			List<StatementObject> listParentLeafWithInvocationsExpressionsInExtractOperationInvocationParameters = new ArrayList<StatementObject>();
			List<CompositeStatementObject> listChildT2InnerNodeInT1InnerNodes = new ArrayList<CompositeStatementObject>();
			List<StatementObject> listChildT2LeafNodeInT1LeafNodes = new ArrayList<StatementObject>();
					
			List<String> addedOperationNames = getOperationNames(OperationType.ADDED);
			List<String> allOperationNames = getOperationNames(OperationType.ALL);
			
			//Processing Parent (Source Operation After Extraction) T2 Inner(Composite)/Leaf Nodes to filter out  marked nodes
			for(CompositeStatementObject  notMappedCompositeNode :  parentListNotMappedInnerNodesT2) {
				
				if(isCompositeNodeExpressionContainingInvokationsToExtractedMethods(notMappedCompositeNode , refList)) {
					listParentT2CompositesWithInvokationsToExtractedMethodInExpression.add(notMappedCompositeNode);
				}
				if(isNeutralNodeForFacilitateExtension(notMappedCompositeNode , refList , sourceOperationAfterExtrction, addedOperationNames, allOperationNames )) {
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
					if(!isLeafNodeHavingExtraCalls(notMappedNode , getExtractedMethodInvocationsInStatement(notMappedNode, refList))) {
					listParentNotMappedLeavesWithInvokationsToExtractedMethod.add(notMappedNode);
					}
				}
				if(isNeutralNodeForFacilitateExtension(notMappedNode , refList , sourceOperationAfterExtrction, addedOperationNames, allOperationNames )) {
					listParentNeutralLeaves.add(notMappedNode);
				}
				if(isParentT2LeafNodeinT1leafNodes(notMappedNode.toString(), parentListNotMappedleafNodesT1)) {
					listParentT2LeafNodeInT1LeafNodes.add(notMappedNode);
				}
				if(isParentLeafNodeExtraInvocationsExpressionsInExtractedMethodParameters(notMappedNode ,extractedOperation, sourceOperationAfterExtrction)) {
					listParentLeafWithInvocationsExpressionsInExtractOperationInvocationParameters.add(notMappedNode);
				}
			}
			setParentMarkedT2Leaves.addAll(listParentNotMappedLeavesWithInvokationsToExtractedMethod);
			setParentMarkedT2Leaves.addAll(listParentNeutralLeaves);
			setParentMarkedT2Leaves.addAll(listParentT2LeafNodeInT1LeafNodes);
			setParentMarkedT2Leaves.addAll(listParentLeafWithInvocationsExpressionsInExtractOperationInvocationParameters);

			//Processing Child (Extracted Operation) T2 Inner(Composite)/Leaf Nodes to filter out  marked nodes
			for(CompositeStatementObject  notMappedCompositeNode : listNotMappedInnerNodesT2) {
				
				if(isCompositeNodeExpressionContainingInvokationsToExtractedMethods(notMappedCompositeNode , refList)) {
					listChildT2CompositesWithInvokationsToExtractedMethodInExpression.add(notMappedCompositeNode);
				}
				if(isNeutralNodeForFacilitateExtension(notMappedCompositeNode ,refList, extractedOperation ,  addedOperationNames, allOperationNames)) {
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
					if(!isLeafNodeHavingExtraCalls(notMappedNode , getExtractedMethodInvocationsInStatement(notMappedNode, refList))) {
						listChildNotMappedLeavesWithInvokationsToExtractedMethod.add(notMappedNode);
					}
				}			
				if(isLeafNodeExtraInvocationsRecursive(notMappedNode, extractedOperation)) {
					listChildNotMappedLeavesWithRecursive.add(notMappedNode);
				}
				if(isLeafNodeExtraInvocationsExpressionsInOperationParameters(notMappedNode, extractedOperation)) {
					listChildNotMappedLeavesWithInvocationsExpressionsInOperationParameters.add(notMappedNode);
				}
				if(isNeutralNodeForFacilitateExtension(notMappedNode, refList, extractedOperation,  addedOperationNames, allOperationNames)){
					listChildNeutralLeaves.add(notMappedNode);
				}
				if(isChildT2LeafNodeinT1leafNodes(notMappedNode.toString(), listNotMappedleafNodesT1)) {
					listChildT2LeafNodeInT1LeafNodes.add(notMappedNode);
				}
			}
			setChildMarkedT2Leaves.addAll(listChildNotMappedLeavesWithInvokationsToExtractedMethod);
			setChildMarkedT2Leaves.addAll(listChildNotMappedLeavesWithRecursive);
			//setChildMarkedT2Leaves.addAll(listChildNotMappedLeavesWithInvocationsExpressionsInOperationParameters);
			setChildMarkedT2Leaves.addAll(listChildNeutralLeaves);
			setChildMarkedT2Leaves.addAll(listChildT2LeafNodeInT1LeafNodes);
			
			//Computing  filtered nodes (Nodes that facilitate extension)
			int filterdListNotMappedInnerNodesT2 = listNotMappedInnerNodesT2.size()-setChildMarkedT2InnerNodes.size();
			int filteredListNotMappedLeavesT2 = listNotMappedLeavesT2.size()-setChildMarkedT2Leaves.size();
			countChildNonMappedLeavesAndInnerNodesT2 =  filterdListNotMappedInnerNodesT2+filteredListNotMappedLeavesT2;
			
			int filterdParentListNotMappedInnerNodesT2 = parentListNotMappedInnerNodesT2.size()-setParentMarkedT2InnerNodes.size();
            int filteredParentListNotMappedLeavesT2 = parentListNotMappedLeavesT2.size()-setParentMarkedT2Leaves.size();
			 countParentNonMappedLeavesAndInnerNodesT2 = filterdParentListNotMappedInnerNodesT2 + filteredParentListNotMappedLeavesT2;
			 //CODE ANALYSYS
			 codeAnalysisFaciliateExtension(ref, countChildNonMappedLeavesAndInnerNodesT2,
					 countParentNonMappedLeavesAndInnerNodesT2, listNotMappedleafNodesT1, listNotMappedInnerNodesT1,
					 parentListNotMappedleafNodesT1, parentListNotMappedInnerNodesT1);
			 //DETECTION RULE: Detect if Some statements(InnerNode or Leave) added either ExtractedOperation or
			 //Source Operation After Extraction
			 
			 if(countParentNonMappedLeavesAndInnerNodesT2 > 0) {
				 //Checking that the extension in the parent is in the "extraction scope" in source operation after extraction
				if(isParentExtraNodeInExtractedScope(extractOperationRefactoring, parentListNotMappedInnerNodesT2,setParentMarkedT2InnerNodes,
						 parentListNotMappedLeavesT2, setParentMarkedT2Leaves )) {
					 facilitateExtensionRefactoringsWithExtrensionInParent.add(extractOperationRefactoring);
				}else {
					if(countChildNonMappedLeavesAndInnerNodesT2 == 0 ) {
						return false;
					}
				}
			 }
			 if( countChildNonMappedLeavesAndInnerNodesT2 > 0 || countParentNonMappedLeavesAndInnerNodesT2 > 0) {
				// if(!isMotivationDetected(ref, MotivationType.EM_INTRODUCE_ALTERNATIVE_SIGNATURE) && !isMotivationDetected(ref, MotivationType.EM_REPLACE_METHOD_PRESERVING_BACKWARD_COMPATIBILITY)) {	
					 return true;
				 //}	
			 }
		}
		return false;
	}
	private boolean isParentExtraNodeInExtractedScope (ExtractOperationRefactoring extractOpRefactoring,  List<CompositeStatementObject> parentListNotMappedInnerNodesT2 , Set<CompositeStatementObject> setParentMarkedT2InnerNodes ,
			List<StatementObject> parentListNotMappedLeavesT2 , Set<StatementObject> setParentMarkedT2Leaves ){
		parentListNotMappedLeavesT2.removeAll(setParentMarkedT2Leaves);
		Set<AbstractCodeMapping> abstractMappings = extractOpRefactoring.getBodyMapper().getMappings();
		Set<CompositeStatementObject> codeFragment1sParents = new HashSet<CompositeStatementObject>();
		for(AbstractCodeMapping abstractCodeMapping : abstractMappings) {
			codeFragment1sParents.add(getNonBlockParentOfAbstractCodeFragment(abstractCodeMapping.getFragment1()));
		}
			for(CompositeStatementObject compositeStatement: parentListNotMappedInnerNodesT2) {				
				CompositeStatementObject compositeStatementParent = getAbstractStatementNonBlockParent(compositeStatement);
				for(CompositeStatementObject parent : codeFragment1sParents) {
					if(parent == null && compositeStatementParent == null) {
						//parents are either source operation before/after extraction
						return true;
					}
					if (parent == null || compositeStatementParent == null) {
						break;
					}
					if(parent.toString().equals(compositeStatementParent.toString())) {
						return true;
					}
				}
			}
			for(StatementObject statementObject: parentListNotMappedLeavesT2) {
				CompositeStatementObject statementObjectParent = getAbstractStatementNonBlockParent(statementObject);
				for(CompositeStatementObject parent : codeFragment1sParents) {
					if(parent == null && statementObjectParent == null) {
						//parents are either source operation before/after extraction
						return true;
					}
					if (parent == null || statementObjectParent == null) {
						break;
					}
					if(parent.toString().equals(statementObjectParent.toString())) {
						return true;
					}
				}
			}	
		return false;
	}
	
	private CompositeStatementObject getAbstractStatementNonBlockParent(AbstractStatement abstractStatement) {
		 CompositeStatementObject parent = abstractStatement.getParent();
		while (parent.getLocationInfo().getCodeElementType().equals(CodeElementType.BLOCK)) {
			if(parent.getParent() != null) {
				parent = parent.getParent();	
			}else {
				return null;
			}
		}		
		return parent;
	}
	
	
	private CompositeStatementObject getNonBlockParentOfAbstractCodeFragment(AbstractCodeFragment fragment){
		 CompositeStatementObject parent = fragment.getParent();
		while (parent.getLocationInfo().getCodeElementType().equals(CodeElementType.BLOCK)) {
			if(parent.getParent() != null) {
				parent = parent.getParent();	
			}else {
				return null;
			}
		}
		
		return parent;
	}
	
	
	private boolean isExtractedMethodMappingAddedTernaryOperator(ExtractOperationRefactoring extrctOpRefactoring){
		Set<AbstractCodeMapping> codeMappings = extrctOpRefactoring.getBodyMapper().getMappings();
		for(AbstractCodeMapping abstractCodeMapping: codeMappings) {
			List<TernaryOperatorExpression> fragment1TernaryOperatorExpressions = abstractCodeMapping.getFragment1().getTernaryOperatorExpressions();
			List<TernaryOperatorExpression> fragment2TernaryOperatorExpressions = abstractCodeMapping.getFragment2().getTernaryOperatorExpressions();
			if(fragment2TernaryOperatorExpressions.size() > 0  && fragment1TernaryOperatorExpressions.size() == 0) {
				return true;
			}				
		}
		return false;
	}

	private void codeAnalysisFaciliateExtension(Refactoring ref, int countChildNonMappedLeavesAndInnerNodesT2,
			int countParentNonMappedLeavesAndInnerNodesT2, List<StatementObject> listNotMappedleafNodesT1,
			List<CompositeStatementObject> listNotMappedInnerNodesT1,
			List<StatementObject> parentListNotMappedleafNodesT1,
			List<CompositeStatementObject> parentListNotMappedInnerNodesT1) {
		//CODE ANALYSIS
		int[] addedRemovedCount = new int[2];
		addedRemovedCount[0] = listNotMappedleafNodesT1.size() + listNotMappedInnerNodesT1.size() 
		+ parentListNotMappedleafNodesT1.size() + parentListNotMappedInnerNodesT1.size();
		addedRemovedCount[1] = countChildNonMappedLeavesAndInnerNodesT2 + countParentNonMappedLeavesAndInnerNodesT2;
		mapFacilitateExtensionT1T2.put(ref,addedRemovedCount);
	}
	
	private boolean isInvocationExpressionsInOperationVariableNames(AbstractStatement statement , UMLOperation extarctedOperation) {
		List<String> invocationExpressionInVariableNames = new ArrayList<String>();
		List<VariableDeclaration> allVariableDeclarations = extarctedOperation.getAllVariableDeclarations();
		List<String> allVariableNames = new ArrayList<String>();
		for(VariableDeclaration decleration : allVariableDeclarations) {
			allVariableNames.add(decleration.getVariableName());
		}		
		for(String invocationString : statement.getMethodInvocationMap().keySet()) {
			List<OperationInvocation> operationInvocations = statement.getMethodInvocationMap().get(invocationString);
			for(OperationInvocation invocation : operationInvocations) {
				if(allVariableNames.contains(invocation.getExpression())){
					invocationExpressionInVariableNames.add(invocation.getExpression());
				}
			}
		}
		if(invocationExpressionInVariableNames.size() > 0) {
			return true;
		}
		return false;
	}
	private boolean isLeafNodeExtraInvocationsRecursive(StatementObject notMappedNode , UMLOperation extractedOperation){
		//checking extra invocations for extension
		List<OperationInvocation > recursiveInvocations = new ArrayList<OperationInvocation>();
		if(notMappedNode.getMethodInvocationMap().size() == 0) {
			return false;
		}
		for(String invocationString : notMappedNode.getMethodInvocationMap().keySet()) {
			List<OperationInvocation> operationInvocations = notMappedNode.getMethodInvocationMap().get(invocationString);
			for(OperationInvocation invocation : operationInvocations) {
				if(invocation.matchesOperation(extractedOperation, extractedOperation.variableTypeMap() , modelDiff)) {
					recursiveInvocations.add(invocation);
				}
			}
			if(recursiveInvocations.size() > 0) {
				return true;
			}
		}
		return false;
	}
	private boolean isParentLeafNodeExtraInvocationsExpressionsInExtractedMethodParameters(StatementObject notMappedNode ,UMLOperation extractedOperation, UMLOperation sourceOperationAfterExtraction) {
		List<OperationInvocation > invocationExpressionInExtractedMethodInvocationParameters = new ArrayList<OperationInvocation>();
		List<String> extractMethodInvocationArguments = new ArrayList<String>();
		if(notMappedNode.getMethodInvocationMap().size() == 0) {
			return false;
		}
		for(OperationInvocation invocation : sourceOperationAfterExtraction.getAllOperationInvocations()) {
			if(invocation.matchesOperation(extractedOperation,sourceOperationAfterExtraction.variableTypeMap(),modelDiff)) {
				extractMethodInvocationArguments.addAll(invocation.getArguments());
				break;
			}
		}

		for(String invocationString : notMappedNode.getMethodInvocationMap().keySet()) {
			List<OperationInvocation> operationInvocations = notMappedNode.getMethodInvocationMap().get(invocationString);
			for(OperationInvocation invocation : operationInvocations) {
				if(extractMethodInvocationArguments.contains(invocation.getExpression())) {
					invocationExpressionInExtractedMethodInvocationParameters.add(invocation);
				}
			}
			if( invocationExpressionInExtractedMethodInvocationParameters.size() > 0) {
				return true;
			}
		}
		return false;
	}
	private boolean isLeafNodeExtraInvocationsExpressionsInOperationParameters(StatementObject notMappedNode , UMLOperation extractedOperation){
		//checking extra invocations for extension
		List<OperationInvocation > invocationOfParametersList = new ArrayList<OperationInvocation>();
		if(notMappedNode.getMethodInvocationMap().size() == 0) {
			return false;
		}
		for(String invocationString : notMappedNode.getMethodInvocationMap().keySet()) {
			List<OperationInvocation> operationInvocations = notMappedNode.getMethodInvocationMap().get(invocationString);
			for(OperationInvocation invocation : operationInvocations) {
				if(extractedOperation.getParameterNameList().contains(invocation.getExpression())) {
					invocationOfParametersList.add(invocation);
				}
			}
			if(invocationOfParametersList.size() == operationInvocations.size()) {
				return true;
			}
		}
		return false;
	}
	private boolean isLeafNodeHavingExtraCalls(StatementObject notMappedNode , List<OperationInvocation> extractedMethodInvocations){
		
		boolean extraCallsExist = notMappedNode.getMethodInvocationMap().size() > 1;
		List<OperationInvocation> extractedMethodSubsumedInvocations = new ArrayList<OperationInvocation>();
		for(String invocationString :  notMappedNode.getMethodInvocationMap().keySet()) {
			for(OperationInvocation invocation: notMappedNode.getMethodInvocationMap().get(invocationString)) {
				if(!extractedMethodInvocations.contains(invocation)) {
					for(OperationInvocation extractedInvocation : extractedMethodInvocations) {
						if(extractedInvocation.getLocationInfo().subsumes(invocation.getLocationInfo())) {
							extractedMethodSubsumedInvocations.add(invocation);
						}
					}
				}
			}
		}
		return extraCallsExist && (extractedMethodSubsumedInvocations.size() == 0);
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
	private List<OperationInvocation> getExtractedMethodInvocationsInStatement(StatementObject notMappedNode,List<Refactoring> refList) {
		List<OperationInvocation> extractedMehtodInvocations = new ArrayList<OperationInvocation>();
		for(Refactoring refactoring : refList) {
			if(refactoring instanceof ExtractOperationRefactoring) {
				ExtractOperationRefactoring extractOperationRefactoring = (ExtractOperationRefactoring)refactoring;
				for(OperationInvocation invocation : extractOperationRefactoring.getExtractedOperationInvocations()) {
					if(notMappedNode.getLocationInfo().subsumes(invocation.getLocationInfo())){
						extractedMehtodInvocations.add(invocation);
					}
				}
			}
		}
		return extractedMehtodInvocations;
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
							if(expression.getMethodInvocationMap().size() == 1) {
								return true;									
							}
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
			if(notMappedNode.getString().indexOf(strParentT2Leave) >= 0) {
				return true;
			}
			if(notMappedNode.getString().startsWith(strParentT2Leave)) {
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
			if(notMappedNode.getString().indexOf(strChildT2Leaf) >= 0) {
				return true;
			}
			if(notMappedNode.getString().startsWith(strChildT2Leaf)) {
				return true;
			}
		}
		return false;
	}
	
	private boolean isNeutralNodeForFacilitateExtension(AbstractStatement statement , List<Refactoring> refList , UMLOperation statementOperation , 
			List<String> addedOperationNames , List<String> allOperationNames){
		Set<CodeElementType> neutralCodeElements = new HashSet<CodeElementType>();
		neutralCodeElements.add(CodeElementType.RETURN_STATEMENT);
		neutralCodeElements.add(CodeElementType.BLOCK);
		CodeElementType elementType  = statement.getLocationInfo().getCodeElementType();
		int  extractedOperationInvocationCountInStatement = 0;
		int  otherOperationInvocationCountInStatement = 0;
		
		if(neutralCodeElements.contains(elementType)) {
			return true;
		}else {
			Map<String, List<OperationInvocation>> mapStatementInvokations = statement.getMethodInvocationMap();
			if(mapStatementInvokations.isEmpty()) {
				//There is no invokations in the variable declaration statement
				return true;
			} else {				
				for(String invokationString : mapStatementInvokations.keySet()) {
					List<OperationInvocation> statementInvokations = mapStatementInvokations.get(invokationString);
					if(isInvocationToExtractedOperation(statementInvokations , statementOperation , refList)) {
						extractedOperationInvocationCountInStatement++;
					}else {
						otherOperationInvocationCountInStatement++;
					}
				}				
				if (elementType.equals(CodeElementType.IF_STATEMENT) ||
						elementType.equals(CodeElementType.EXPRESSION_STATEMENT)||
						elementType.equals(CodeElementType.VARIABLE_DECLARATION_STATEMENT)) {
					if(extractedOperationInvocationCountInStatement == 0 && otherOperationInvocationCountInStatement == 0) {
						return true;
						}
					if(extractedOperationInvocationCountInStatement == 0 && otherOperationInvocationCountInStatement > 0) {	
						if(elementType.equals(CodeElementType.VARIABLE_DECLARATION_STATEMENT)||elementType.equals(CodeElementType.EXPRESSION_STATEMENT)) {
							if(isInvocationExpressionsInOperationVariableNames(statement , statementOperation) ) {
								return false;
							}
							if(isStatementInvocationsInAddedOperations(statement , statementOperation , addedOperationNames)) {
								return false;
							}	
							if(!isStatementInvocationsInAllOperationNames(statement , allOperationNames)) {
								return false;
							}else {
								return true;
							}
						}
					}
				}else {
					if(extractedOperationInvocationCountInStatement == 0) {
						return true;
					}
				}
			}
		}
		return false;
	}
	private boolean isStatementInvocationsInAllOperationNames(AbstractStatement statement , List<String> allOperationNames) {
		List<String> methodNamesInvokedInStatement = getStatementInvocationNames(statement);

		
		List<String> invorcationsInAllOperations = new ArrayList<String>();
		for(String  methodInvocation : methodNamesInvokedInStatement) {
				if(allOperationNames.contains(methodInvocation)) {
					invorcationsInAllOperations.add(methodInvocation);
				}
			}		
		if(invorcationsInAllOperations.size() == methodNamesInvokedInStatement.size()) {
			return true;
		}	
		return false;
	}
	private boolean isStatementInvocationsInAddedOperations(AbstractStatement statement , UMLOperation statementOperation ,List<String> addedOperationNames) {
		List<String> addedOperationsInVariableDeclarionOrParameterClasses = new ArrayList<String>();
		List<String> invocationClassNames = new ArrayList<String>();
		List<String> methodNamesInvokedInStatement = getStatementInvocationNames(statement);
		//Adding Classes of the variable declarations or parameter Class names with same class Type of invocation expression
		for(String invocationString : statement.getMethodInvocationMap().keySet()) {
			for( OperationInvocation invocation : statement.getMethodInvocationMap().get(invocationString)) {
				if(invocation.getExpression() == null || invocation.getExpression().equals("this")) {
					invocationClassNames.add(statementOperation.getClassName());
				}
				for(VariableDeclaration declaration : statementOperation.getAllVariableDeclarations()) {
					if(declaration.getVariableName().equals(invocation.getExpression())) {
						invocationClassNames.add(declaration.getType().getClassType());
					}			
				}
				List<UMLParameter> statementOperationParameters = statementOperation.getParameters();
				for(UMLParameter parameter : statementOperationParameters) {
					if(parameter.getName().equals(invocation.getExpression())) {
						invocationClassNames.add(parameter.getType().getClassType());
					}
				}
			}
		}	
		for(String invocationClassName : invocationClassNames) {
			 UMLClassBaseDiff umlClassBaseDiff = modelDiff.getUMLClassDiff(invocationClassName);
			 if(umlClassBaseDiff != null) {
				 for(UMLOperation operation :umlClassBaseDiff.getAddedOperations()) {
					 addedOperationsInVariableDeclarionOrParameterClasses.add(operation.getName());
				 }
			 }
		}			
	
		addedOperationNames.addAll(addedOperationsInVariableDeclarionOrParameterClasses);
		
		List<String> invorcationsInAddedOperations = new ArrayList<String>();
		for(String  methodInvocation : methodNamesInvokedInStatement) {
				if(addedOperationNames.contains(methodInvocation)) {
					invorcationsInAddedOperations.add(methodInvocation);
				}
			}
		
		if(invorcationsInAddedOperations.size() == methodNamesInvokedInStatement.size()) {
			return true;
		}	
		return false;
	}
	private List<String> getStatementInvocationNames(AbstractStatement statement){
		
		List<String> methodNamesInvokedInStatement = new ArrayList<String>();
		for(String invocationString : statement.getMethodInvocationMap().keySet()) {
			List<OperationInvocation> operationInvocations = statement.getMethodInvocationMap().get(invocationString);
			for(OperationInvocation invocation : operationInvocations) {
				methodNamesInvokedInStatement.add(invocation.getMethodName());
			}
		}
		return methodNamesInvokedInStatement;
	}
	private enum OperationType{
		ADDED,
		ALL;
	}
	private List<String> getOperationNames(OperationType operationType){
		
		List<String> operationNames = new ArrayList<String>();
		List<String> addedClassesOperationNames = new ArrayList<String>();
		List<String> commonClassesAddedOperationNames = new ArrayList<String>();
		List<String> commonClassesAllOperationNames = new ArrayList<String>();
		for(UMLClass addedClass : modelDiff.getAddedClasses()) {
			for(UMLOperation operation : addedClass.getOperations()) {
				addedClassesOperationNames.add(operation.getName());
			}
		}
		for(UMLClassDiff classDiff : modelDiff.getCommonClassDiffList()) {
			if(classDiff != null) {
				for(UMLOperation operartion: classDiff.getAddedOperations()) {
					commonClassesAddedOperationNames.add(operartion.getName());
				}
			}
		}
		for(UMLClassDiff classDiff : modelDiff.getCommonClassDiffList()) {
			if(classDiff != null) {
				for(UMLOperation operartion: classDiff.getNextClass().getOperations()) {
					commonClassesAllOperationNames.add(operartion.getName());
				}
			}
		}
		
		switch(operationType) {
		case ADDED:
			operationNames.addAll(commonClassesAddedOperationNames);
			operationNames.addAll(addedClassesOperationNames);
			break;
		case ALL:
			operationNames.addAll(commonClassesAllOperationNames);
			operationNames.addAll(addedClassesOperationNames);
		default:
			break;
		}
	
		
		return operationNames;
	}
	
	private boolean isInvocationToExtractedOperation(List<OperationInvocation> statementInvokations , UMLOperation statementOperation , List<Refactoring> refList){
		List<OperationInvocation> invokationToExtractedOperation = new ArrayList<OperationInvocation>();
		for (OperationInvocation invokation: statementInvokations) {
			for(Refactoring ref : refList) {
				ExtractOperationRefactoring extractOperationRefactoring = (ExtractOperationRefactoring)ref;
				if( invokation.matchesOperation(extractOperationRefactoring.getExtractedOperation(), statementOperation.variableTypeMap(),modelDiff)){
					invokationToExtractedOperation.add(invokation);
				}
			}
		}
		return invokationToExtractedOperation.size() > 0 ? true : false;
	}
	private boolean isExtractReusableMethod(Refactoring ref ,List<Refactoring> refList) {		
		if(ref instanceof ExtractOperationRefactoring) {
			countSingleMethodRemoveDuplications = 0;
			ExtractOperationRefactoring extractOpRefactoring = (ExtractOperationRefactoring)ref;
			UMLOperation extractedOperation = extractOpRefactoring.getExtractedOperation();
			//Removing duplication from a single method.
			Map<String, List<ExtractOperationRefactoring>> groupedByToStringSingleMthods =
					removeDuplicationFromSingleMethodRefactorings.stream().collect(Collectors.groupingBy(x->x.toString()));
			if(groupedByToStringSingleMthods.containsKey(extractOpRefactoring.toString())) {
				countSingleMethodRemoveDuplications = groupedByToStringSingleMthods.get(extractOpRefactoring.toString()).size();
			}
			if(countSingleMethodRemoveDuplications > 0 ) {
				countSingleMethodRemoveDuplications --;
			}
			Map<UMLOperation, List<OperationInvocation>> mapExtraExtractedOperationInvokationsInClasses = new HashMap<UMLOperation, List<OperationInvocation>>() ;
			for(UMLClassDiff classDiff : modelDiff.getCommonClassDiffList()) {
				if(classDiff != null) {
					UMLClass nextClass = classDiff.getNextClass();
					mapExtraExtractedOperationInvokationsInClasses.putAll(extractedOperationInvocationsCountInClass(extractOpRefactoring, nextClass, refList));
				}
			}
			for(UMLClass addedClass : modelDiff.getAddedClasses()) {
				mapExtraExtractedOperationInvokationsInClasses.putAll(extractedOperationInvocationsCountInClass(extractOpRefactoring, addedClass , refList));
			}
			//Check the mappings of invocations to Extracted method in Original and next class
			if(isExtractedMethodInvocationsEqualInOriginalAndNextClass(ref)) {
				return false;
			}
			
			List<UMLOperation> listMatchedOperationsWithExtractedOperationInOtherClasses = getAllMatchedOperationsInOtherClasses(extractedOperation);
			//In case when there are no other operations in other classes matches extracted operation.
			if(listMatchedOperationsWithExtractedOperationInOtherClasses.size() == 0) {
				if(reusabilityRulesValidation(extractOpRefactoring, mapExtraExtractedOperationInvokationsInClasses)) {
					return true;
				}
			}else {
			
				Map<String, List<ExtractOperationRefactoring>> groupedByToString =
						refList.stream() // For each refactoring
						// Cast to ExtractOperationRefactoring
						.map(x->(ExtractOperationRefactoring)x)
						// Group by the toString() value
						.collect(Collectors.groupingBy(x->x.toString()));
				
				//Checking for cases where all extract operations are unique
				if(groupedByToString.entrySet().stream().allMatch(x-> x.getValue().size() == 1)) {
					/*When there are matching Operations with the same name as extracted operation in other classes.
					 * e.g. intellij-community:10f769a exists, A UMLOperation with same name as extracted method exists 
					 * in com.intellij.execution.junit.JUnit4Framework class
					 */
					Map<UMLOperation, List<OperationInvocation>> mapExtraExtractedOperationInvokationsInOtherClassesWhenMatchingOperationExists 
					= getExtraInvocationsToExtractedMethodWhenMatchingOperationExists(extractedOperation , mapExtraExtractedOperationInvokationsInClasses);
					// When Invocation in other classes to extracted operation exists e.g: alluxio:ed966510
					if(mapExtraExtractedOperationInvokationsInOtherClassesWhenMatchingOperationExists.size() > 0) {
						return true;
					}else {
						//When Invocation to extracted method in other classes does not exist but in extracted method class there are extra calls to extracted method.
						for(UMLOperation operation : mapExtraExtractedOperationInvokationsInClasses.keySet()) {
							for (OperationInvocation invokation: mapExtraExtractedOperationInvokationsInClasses.get(operation)) {
								if(invokation.getExpression() == null || invokation.getExpression().equals("this")) {
									if(operation.getClassName().equals(extractedOperation.getClassName())){
										if((extractOpRefactoring.getExtractedOperationInvocations().size() - countSingleMethodRemoveDuplications) > 1 ) {
											return true;
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return false;	
	}
	
	private boolean isExtractedMethodInvocationsEqualInOriginalAndNextClass(Refactoring ref){
		Map<UMLOperation, List<OperationInvocation>> extractedOperationInvokationsInNextClasses = new HashMap<UMLOperation, List<OperationInvocation>>();
		Map<UMLOperation, List<OperationInvocation>> sourceOperationInvokationsInOriginalClass = new HashMap<UMLOperation, List<OperationInvocation>>();
		if(ref instanceof ExtractOperationRefactoring) {
			ExtractOperationRefactoring extractOpRefactoring = (ExtractOperationRefactoring) ref;
			UMLOperation extractedOperation = extractOpRefactoring.getExtractedOperation();
			UMLOperation sourceOperationBeforeExtraction = extractOpRefactoring.getSourceOperationBeforeExtraction();
			extractedOperationInvokationsInNextClasses = getAllExtractedOperationInvocationsInNextClasses(extractedOperation);
			sourceOperationInvokationsInOriginalClass = getAllSourceOperationBeforeExtractionInvocationsInOriginalClass(sourceOperationBeforeExtraction);
			if(isMotivationDetected(extractOpRefactoring, MotivationType.EM_REPLACE_METHOD_PRESERVING_BACKWARD_COMPATIBILITY)) {
				int nextInvocationCount = extractedOperationInvokationsInNextClasses.values().size();
				int OriginalInvocationCount = sourceOperationInvokationsInOriginalClass.values().size();
				//Reduce one from the size of right side because of the delegate method extra call.
				if(OriginalInvocationCount == nextInvocationCount-1) {
					return true;
				}	
			}
		}				
		return false;
	}
	
	private Map<UMLOperation, List<OperationInvocation>> getAllExtractedOperationInvocationsInNextClasses(UMLOperation extractedOperation){
		Map<UMLOperation, List<OperationInvocation>> extractedOperationInvokationsInNextClasses = new HashMap<UMLOperation, List<OperationInvocation>>();
		for(UMLClassDiff classDiff : modelDiff.getCommonClassDiffList()) {
			if(classDiff != null) {
				UMLClass nextClass = classDiff.getNextClass();
				for(UMLOperation operation : nextClass.getOperations()) {
					extractedOperationInvokationsInNextClasses.putAll(countOperationAInvokationsInOperationB(extractedOperation , operation));
				}
			}
		}
		for(UMLClass addedClass : modelDiff.getAddedClasses()) {
			for(UMLOperation operation : addedClass.getOperations()) {
				extractedOperationInvokationsInNextClasses.putAll(countOperationAInvokationsInOperationB(extractedOperation , operation));
			}
		}		
		return extractedOperationInvokationsInNextClasses;
	}
	private  Map<UMLOperation, List<OperationInvocation>> getAllSourceOperationBeforeExtractionInvocationsInOriginalClass (UMLOperation sourceOperationBeforeExtraction){
		Map<UMLOperation, List<OperationInvocation>> sourceOperationInvokationsInOriginalClass = new HashMap<UMLOperation, List<OperationInvocation>>();
		for(UMLClassDiff classDiff : modelDiff.getCommonClassDiffList()) {
			UMLClass originalClass = classDiff.getOriginalClass();
			for(UMLOperation operation : originalClass.getOperations()) {
				sourceOperationInvokationsInOriginalClass.putAll(countOperationAInvokationsInOperationB(sourceOperationBeforeExtraction , operation));
			}
		}	
		return sourceOperationInvokationsInOriginalClass;
	}
	private Map<UMLOperation, List<OperationInvocation>> getExtraInvocationsToExtractedMethodWhenMatchingOperationExists (UMLOperation extractedOperation , 
			Map<UMLOperation, List<OperationInvocation>> mapExtraExtractedOperationInvokationsInClasses) {
		//Find extra invocations to extracted method when A matching operation to Extracted method exists.
		Map<UMLOperation, List<OperationInvocation>> mapExtraInvocationsWithUmlOperationAsKeyWhenExtractMethodMatchingOperationExists = new HashMap<UMLOperation, List<OperationInvocation>>();
		String extractedOperationQualifiedClassName  = extractedOperation.getNonQualifiedClassName();
		for(UMLOperation invocationOperation : mapExtraExtractedOperationInvokationsInClasses.keySet()) {
			List<OperationInvocation> invocationsInOtherClassesContainingExtractedMethodClassNameOrVariableTypeInExpression = new ArrayList<OperationInvocation>();
			for(OperationInvocation invocation : mapExtraExtractedOperationInvokationsInClasses.get(invocationOperation) ) {
				String invocationExpression = invocation.getExpression();
				if( invocationExpression != null && !invocationExpression.equals("this")){
					for(String variableString : invocationOperation.variableTypeMap().keySet()) {
						String tyoeClassName = invocationOperation.variableTypeMap().get(variableString).getClassType();
						// Check to see if the expression type matches the extracted operation class
						if( invocationExpression.contains(extractedOperationQualifiedClassName) || extractedOperationQualifiedClassName.equals(tyoeClassName)) {
							invocationsInOtherClassesContainingExtractedMethodClassNameOrVariableTypeInExpression.add(invocation);
						}
					}
				}
			}
			if(invocationsInOtherClassesContainingExtractedMethodClassNameOrVariableTypeInExpression.size()>0) {
				if(mapExtraInvocationsWithUmlOperationAsKeyWhenExtractMethodMatchingOperationExists.containsKey(invocationOperation)){
					mapExtraInvocationsWithUmlOperationAsKeyWhenExtractMethodMatchingOperationExists.get(invocationOperation).addAll(invocationsInOtherClassesContainingExtractedMethodClassNameOrVariableTypeInExpression);
				}else {
					mapExtraInvocationsWithUmlOperationAsKeyWhenExtractMethodMatchingOperationExists.put(invocationOperation, invocationsInOtherClassesContainingExtractedMethodClassNameOrVariableTypeInExpression);
				}
			}
		}
		return mapExtraInvocationsWithUmlOperationAsKeyWhenExtractMethodMatchingOperationExists;
	}

	private boolean reusabilityRulesValidation(ExtractOperationRefactoring extractOpRefactoring, Map<UMLOperation, List<OperationInvocation>> mapExtraExtractedOperationInvokationsInClasses) {
		UMLOperation sourceOperationAfterExtraction = extractOpRefactoring.getSourceOperationAfterExtraction();
		List<OperationInvocation> sourceOperationAfterExtractionInvokations = sourceOperationAfterExtraction.getAllOperationInvocations();
		UMLOperation extractedOperation = extractOpRefactoring.getExtractedOperation();

		/*Rule Exception : Extract is not reusable when there is only one call to extracted operation from source after extraction
		 * and Call is outside source operation and outside extractOperationInvocationCount is one.
		 * Example(Nested Extract Refactorings): Checkstyle :5a9b7 , JGroups:f1533
		 */
		int extractedOperationCallsInsideSourceOperationAfterExtraction = 0 ;
		for(OperationInvocation invokation : sourceOperationAfterExtractionInvokations) {
			if(invokation.matchesOperation(extractedOperation,sourceOperationAfterExtraction.variableTypeMap(), modelDiff)) {
				extractedOperationCallsInsideSourceOperationAfterExtraction++;
			}
		}
		if(((extractOpRefactoring.getExtractedOperationInvocations().size() == 1) || (extractOpRefactoring.getExtractedOperationInvocations().size() == 0)) 
				&& (extractedOperationCallsInsideSourceOperationAfterExtraction == 0 )) {
			if(mapExtraExtractedOperationInvokationsInClasses.size() == 1) {
				return false;
			}
		}
		/* GENERAL DETECTION RULE:
		 * IF Invocations to Extracted method from source method after Extraction is more than one OR 
		 *  there are other Invocations from other methods to extracted operation.
		 *  Invocations inside test methods will not be considered as reusable calls. */

		int extarctOpRefactoringCallstoExtractedOperation = extractOpRefactoring.getExtractedOperationInvocations().size();
		if((extarctOpRefactoringCallstoExtractedOperation - countSingleMethodRemoveDuplications) > 1 || mapExtraExtractedOperationInvokationsInClasses.size() > 0 ) {
			return true;
		}
		return false;
	}
	
	private Map<UMLOperation, List<OperationInvocation>> extractedOperationInvocationsCountInClass(ExtractOperationRefactoring extractOpRefactoring, UMLClass nextClass , List<Refactoring> refList) {
		Map<UMLOperation, List<OperationInvocation>> mapAllExtraOperationInvokations = new HashMap<UMLOperation, List<OperationInvocation>>();
		for(UMLOperation operation : nextClass.getOperations()) {
			mapAllExtraOperationInvokations.putAll(computeAllReusedInvokationsToExtractedMethod(operation,extractOpRefactoring,nextClass,refList));
		}
		return mapAllExtraOperationInvokations;	
	}
	
	private Map<UMLOperation, List<OperationInvocation>> computeAllReusedInvokationsToExtractedMethod(UMLOperation operation, ExtractOperationRefactoring extractOpRefactoring, 
			UMLClass nextClass , List<Refactoring> refList){
		Map<UMLOperation, List<OperationInvocation>> mapExtraOperationInvokations = new HashMap<UMLOperation, List<OperationInvocation>>();
		UMLOperation extractedOperation = extractOpRefactoring.getExtractedOperation();
		UMLOperation sourceOperationAfterExtration = extractOpRefactoring.getSourceOperationAfterExtraction();
		boolean considerCallsFromTestMethodsAsReuse = (sourceOperationAfterExtration.hasTestAnnotation() || sourceOperationAfterExtration.getName().startsWith("test")) ? true: false;
		/*In the cases when extracted operation is  extracted from a test method (is part of the test code), 
		extra calls from test methods to extracted operation are considered as reuse. */
		if(considerCallsFromTestMethodsAsReuse) {
			if(!operation.equals(sourceOperationAfterExtration) && !operation.equals(extractedOperation)) {
				mapExtraOperationInvokations.putAll(computeReusedInvokationsToExtractedMethod(operation, extractOpRefactoring, refList));
			}
		}else {
			if(!operation.equals(sourceOperationAfterExtration) && !operation.equals(extractedOperation) 
					&& !operation.hasTestAnnotation() && !operation.getName().startsWith("test")) {
				mapExtraOperationInvokations.putAll(computeReusedInvokationsToExtractedMethod(operation, extractOpRefactoring, refList));
			}
		}
		return mapExtraOperationInvokations;
	}
	private Map<UMLOperation, List<OperationInvocation>> computeReusedInvokationsToExtractedMethod(UMLOperation operation , ExtractOperationRefactoring extractOpRefactoring , 
			List<Refactoring> refList ){
		Map<UMLOperation, List<OperationInvocation>> mapExtraInvokations = new HashMap<UMLOperation, List<OperationInvocation>>();
		UMLOperation extractedOperation = extractOpRefactoring.getExtractedOperation();
		
		
		if(isMotivationDetected(extractOpRefactoring, MotivationType.EM_REMOVE_DUPLICATION) && refList.size() > 1) {
			/*When we check extra calls to detect Reuse if there has been a Remove Duplication motivation from multiple
			 *  source methods we ignore extra invokations from "same remove duplication" "source operations after extraction"  Extract method's. */
			if(!isOperationEqualToSourceOperationAfterExtractionOfSameRemoveDuplicationGroupExtractRefactorings(operation, refList , extractOpRefactoring)) {
				mapExtraInvokations.putAll(countOperationAInvokationsInOperationB(extractedOperation,operation));
			}
		}else {
			mapExtraInvokations.putAll(countOperationAInvokationsInOperationB(extractedOperation,operation));		 
		} 

		return mapExtraInvokations;
	}
	private boolean isOperationEqualToSourceOperationAfterExtractionOfSameRemoveDuplicationGroupExtractRefactorings(UMLOperation operation, List<Refactoring> refList 
			, Refactoring exRefactoring) {
		ExtractOperationRefactoring mainExtractOperationRefactoring = (ExtractOperationRefactoring)exRefactoring;
		UMLOperation  mainExtractedOperation = mainExtractOperationRefactoring.getExtractedOperation();
		for(Refactoring ref : refList) {
			if(ref instanceof ExtractOperationRefactoring) {
				ExtractOperationRefactoring otherExtractOperationRefactoring = (ExtractOperationRefactoring)ref;
				UMLOperation otherExtractedOperation = otherExtractOperationRefactoring.getExtractedOperation();
				if(isMotivationDetected(otherExtractOperationRefactoring, MotivationType.EM_REMOVE_DUPLICATION) && 
						mainExtractedOperation.equals(otherExtractedOperation)) {
					if (operation.equals(otherExtractOperationRefactoring.getSourceOperationAfterExtraction())){
						return true;
					}	
				}
			}
		}
		return false;
	}
	private Map<UMLOperation, List<OperationInvocation>>  countOperationAInvokationsInOperationB(UMLOperation operationA, UMLOperation operationB) {
		Map<UMLOperation, List<OperationInvocation>> mapOperationAInvokationsInOperationB = new HashMap<UMLOperation, List<OperationInvocation>>();
		List<OperationInvocation> invocations = operationB.getAllOperationInvocations();
		for(OperationInvocation invocation : invocations) {
			if(invocation.matchesOperation(operationA,operationB.variableTypeMap(), modelDiff)) {
				if(mapOperationAInvokationsInOperationB.containsKey(operationB)){
					mapOperationAInvokationsInOperationB.get(operationB).add(invocation);
				}else {
					List<OperationInvocation> operationAInvokationsInOperationB = new ArrayList<OperationInvocation>();
					operationAInvokationsInOperationB.add(invocation);
					mapOperationAInvokationsInOperationB.put(operationB, operationAInvokationsInOperationB);
				}
			}
		}
		return mapOperationAInvokationsInOperationB;
	}
	
	private List<UMLOperation> getAllMatchedOperationsInOtherClasses(UMLOperation umlOperation) {
		List<UMLOperation> listEqualOperations = new ArrayList<UMLOperation>();
		UMLClassBaseDiff umlClassDiff = modelDiff.getUMLClassDiff(umlOperation.getClassName());
		if(umlClassDiff != null) {
			UMLClass umlClass = umlClassDiff.getNextClass();
			for(UMLClassDiff classDiff : modelDiff.getCommonClassDiffList()) {
				if(classDiff != null) {
					UMLClass nextCommonClass = classDiff.getNextClass();
					if(nextCommonClass.matchOperation(umlOperation) != null && !nextCommonClass.equals(umlClass)) {
						listEqualOperations.add(nextCommonClass.matchOperation(umlOperation));	
					}
				}
				for(UMLClass addedClass : modelDiff.getAddedClasses() ) {
					if(addedClass.matchOperation(umlOperation) != null && !addedClass.equals(umlClass)) {
						listEqualOperations.add(addedClass.matchOperation(umlOperation));
					}
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
			 	if(!isExtractMethodRefactoringsEqual(list)) {
					//CODE ANALYSYS
					codeAnalysisDecomposeToImproveRedability(list);
					for(ExtractOperationRefactoring ref : list) {
						//Set Motivation for each refactoring with the same source 
						setRefactoringMotivation(MotivationType.EM_DECOMPOSE_TO_IMPROVE_READABILITY,  ref);
						countDecomposeMethodToImproveReadability++;	
					}	
			 	}
			}
		}				
		if(countDecomposeMethodToImproveReadability >= 2) {
			return true;
		}
		/*Checking source operation after Extraction calls to the extracted method to see if they improve readability
		 *in case of calls inside expressions or inside return statements we consider the extraction is improving the readability
		 * Example: mockito:2d036 , jedis:d4b4a , cassandra:9a3fa ,JetBrains/intellij-community/commit/7dd55
		 */
		int countDecomposeSingleMethodToImproveReadability = 0;
		for (Refactoring ref : refList) {
			if(isExtractedOperationInvokationsToImproveReadability(ref)) {
				codeAnalysisDecomposeToImproveRedability(Arrays.asList((ExtractOperationRefactoring)ref));
				decomposeToImproveReadabilityFromSingleMethodRefactorings.add((ExtractOperationRefactoring)ref);
				setRefactoringMotivation(MotivationType.EM_DECOMPOSE_TO_IMPROVE_READABILITY,  ref);
				countDecomposeSingleMethodToImproveReadability++;	
			}
		}
		if(countDecomposeSingleMethodToImproveReadability > 0 ) {
			return true;
		}
		return false;	
	}
	
	private boolean isExtractMethodRefactoringsEqual(List<ExtractOperationRefactoring> extractOperationRefactorings){
		Set<UMLOperation> setSourceOperationsBeforeExtraction = new HashSet<>();
		Set<UMLOperation> setExtractedOperations = new HashSet<>();
		Set<UMLOperation> setSourceOperationsAfterExtraction = new HashSet<>();
		Set<String> setSourceFiles = new HashSet<>();
		Set<String> setClassesNames = new HashSet<>();
		for(ExtractOperationRefactoring extractOpRefactroing : extractOperationRefactorings) {
			setSourceOperationsBeforeExtraction.add(extractOpRefactroing.getSourceOperationBeforeExtraction());
			setSourceOperationsAfterExtraction.add(extractOpRefactroing.getSourceOperationAfterExtraction());
			setExtractedOperations.add(extractOpRefactroing.getExtractedOperation());
			setClassesNames.add(extractOpRefactroing.getExtractedOperation().getClassName());
			setClassesNames.add(extractOpRefactroing.getSourceOperationBeforeExtraction().getClassName());
			
			UMLClassBaseDiff umlClass = modelDiff.getUMLClassDiff(extractOpRefactroing.getExtractedOperation().getClassName());
			if(umlClass != null) {
				UMLClass nextClass = umlClass.getNextClass();
				UMLClass OriginalClass = umlClass.getOriginalClass();
				setSourceFiles.add(nextClass.getSourceFile());
				setSourceFiles.add(OriginalClass.getSourceFile());
			}
		}
		if(setExtractedOperations.size() == 1 && setSourceOperationsBeforeExtraction.size() == 1 && 
				setSourceOperationsAfterExtraction.size() == 1 && 
				setClassesNames.size() == 1 && setSourceFiles.size()==1) {
			return true;
		}
		
		return false;

	}
	private void codeAnalysisDecomposeToImproveRedability(List<ExtractOperationRefactoring> list) {
		
		for(ExtractOperationRefactoring extractOpRefactoring : list) {
			String sizeExtractedMethod = Integer.toString(extractOpRefactoring.getBodyMapper().getMappings().size());		 
			mapDecomposeToImproveRedability.put(extractOpRefactoring, sizeExtractedMethod);
		}		
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
			if(listReturnStatementswithCallsToExtractedOperation.size() > 0) {
				decomposeToImproveReadabilityFromSingleMethodByHavingCallToExtractedMethodInReturn.add((ExtractOperationRefactoring)ref);
			}
			List<AbstractExpression> expressionsInCompositesWithCallsToExtractedMethod = getAllCompositeStatementObjectExpressionsWithInvokationsToExtractedOperation(sourceOperationAfterExtractionBody, extratedOperation, sourceOperationAfterExtraction);
			if(expressionsInCompositesWithCallsToExtractedMethod.size() > 0 ||
					expressionsUsingVariableInitializedWithExtracedOperationInvocation.size() > 0 || 
					((listReturnStatementswithCallsToExtractedOperation.size() > 0) && (sourceOperationAfterExtraction.getBody().statementCount() > 1))) {
				return true;
			}
			OperationBody sourceOperationBody = extractOperationRef.getSourceOperationAfterExtraction().getBody();
			CompositeStatementObject compositeStatement =  sourceOperationBody.getCompositeStatement();
			//if(isCompositeStatementWithLeavesCallingExtractedOepration(compositeStatement , CodeElementType.CATCH_CLAUSE , extractOperationRef)) {
			//	return true;
			//}
		}	
		return false;
	}
	private boolean isCompositeStatementWithLeavesCallingExtractedOepration(CompositeStatementObject compositeStatement, CodeElementType codeElementType , ExtractOperationRefactoring extractOperationRef){
		if(compositeStatement.getLocationInfo().getCodeElementType().equals(codeElementType)) {
			    if(isLeavesContainingCallsToExtractedMethod(compositeStatement, extractOperationRef)) {
			    	return true;
			    }
		}else {
			List<CompositeStatementObject> innerNodes = compositeStatement.getInnerNodes();
			for( CompositeStatementObject composite : innerNodes) {
				if(composite.getLocationInfo().getCodeElementType().equals(codeElementType)) {
					if(isLeavesContainingCallsToExtractedMethod(composite, extractOperationRef)) {
						return true;
					}	
				}
			}
		}
		return false;
	}
	
	private boolean isLeavesContainingCallsToExtractedMethod(CompositeStatementObject compositeStatement , ExtractOperationRefactoring extractOperationRef){
		List<StatementObject> listStatementObjects = compositeStatement.getLeaves();
		for(StatementObject statement : listStatementObjects) {
			for(OperationInvocation invokation : extractOperationRef.getExtractedOperationInvocations()) {
				if(statement.getLocationInfo().subsumes(invokation.getLocationInfo())){
					return true;
				}	
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
					if(statement.getLocationInfo().subsumes(invokation.getLocationInfo()) && invokation.getExpression() == null ){
						//if(isAllStatementInvocationsToExtractedOperation(statement , extractOperationRef.getExtractedOperation())) {
							statementswithCallsToExtractedOperation.add(statement);
							break;
						//}
					}	
				}
			}
		}
		return statementswithCallsToExtractedOperation;
	}
	
	private boolean isAllStatementInvocationsToExtractedOperation(StatementObject statement, UMLOperation extractOperation){
		for( String invocationString : statement.getMethodInvocationMap().keySet()) {
			for(OperationInvocation invocation : statement.getMethodInvocationMap().get(invocationString)) {
				if(!invocation.matchesOperation(extractOperation)) {
					return false;
				}
			}
		}
		return true;
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
		List<ExtractOperationRefactoring> allRemoveDuplicationExtractRefactorings = new ArrayList<ExtractOperationRefactoring>();
		for(UMLOperation extractOperation : sourceOperationMapWithExtractedOperationAsKey.keySet()){
			List<ExtractOperationRefactoring> listSourceOperations = sourceOperationMapWithExtractedOperationAsKey.get(extractOperation);
			/*DETECTION RULE: if multiple source operations(Or the Extract Refactorings that contain them)
			 *  have the same extractedOperation the extract operations motivations is Remove Duplication*/
			if(listSourceOperations.size() > 1){
				if(listSourceOperations.size() > 1) {
					if(isExtractMethodRefactoringsEqual(listSourceOperations)) {
						removeDuplicationFromSingleMethodRefactorings.addAll(listSourceOperations);
					}
					for(ExtractOperationRefactoring extractOp : listSourceOperations){
						setRefactoringMotivation(MotivationType.EM_REMOVE_DUPLICATION, extractOp);
						if(isMotivationDetected(extractOp , MotivationType.EM_DECOMPOSE_TO_IMPROVE_READABILITY) 
								&& (!decomposeToImproveReadabilityFromSingleMethodRefactorings.contains(extractOp)||
										decomposeToImproveReadabilityFromSingleMethodByHavingCallToExtractedMethodInReturn.contains(extractOp))) {
							removeRefactoringMotivation(MotivationType.EM_DECOMPOSE_TO_IMPROVE_READABILITY, extractOp);
						}
						allRemoveDuplicationExtractRefactorings.add(extractOp);	
					}
				}
			}				
		}
		if(allRemoveDuplicationExtractRefactorings.size() >= 2) {
			return true;
		}
		return false;
	}
	private List<ExtractOperationRefactoring> getRepetativeExtractOperations(List<ExtractOperationRefactoring> listExtractOperations){
		List<ExtractOperationRefactoring> listRepetativeExtractOperations = new ArrayList<ExtractOperationRefactoring>();
		for (int i = 0; i < listExtractOperations.size(); i++) {
			for (int j = i+1; j <listExtractOperations.size() ; j++) {
				if(listExtractOperations.get(i).toString().equals(listExtractOperations.get(j).toString())){
					listRepetativeExtractOperations.add(listExtractOperations.get(i));
				}
			}
		}
		return listRepetativeExtractOperations;
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
	private boolean removeRefactoringMotivation(MotivationType motivationType, Refactoring ref) {
		if (mapRefactoringMotivations.containsKey(ref)){
			if(!mapRefactoringMotivations.get(ref).isEmpty() && mapRefactoringMotivations.get(ref).contains(motivationType)){
				if(mapRefactoringMotivations.get(ref).remove(motivationType)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public void classifyRefactoringsByType(List<Refactoring> refactorings) {
		for(Refactoring refactoring : refactorings) {
			RefactoringType type = refactoring.getRefactoringType();
			if(type.equals(RefactoringType.EXTRACT_AND_MOVE_OPERATION)) {
				type = RefactoringType.EXTRACT_OPERATION;
			}
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