package gr.uom.java.xmi.diff;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringType;

import gr.uom.java.xmi.UMLClass;
import gr.uom.java.xmi.UMLOperation;
import gr.uom.java.xmi.decomposition.OperationInvocation;

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
				
	private void detectMotivataion(RefactoringType type) {	
		
		switch (type) {
		case EXTRACT_OPERATION :
			List<Refactoring> listRef = mapClassifiedRefactorings.get(type);
			detectExtractOperationMotivation(listRef);
			break;
		case MOVE_CLASS:
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

	public void detectExtractOperationMotivation(List<Refactoring> refList) {		
		
		//Motivation Detection algorithms that depends on other refactorings of the same type
		isDecomposeMethodToImroveReadability(refList);
		isMethodExtractedToRemoveDuplication(refList);
		
		//Motivation Detection algorithms that can detect the motivation independently for each refactoring
		for(Refactoring ref : refList){
			if(isExtractReusableMethod(ref))
			setRefactoringMotivation(MotivationType.EM_REUSABLE_METHOD, ref);								
		}
		//Print All detected refactorings
		printDetectedRefactoringMotivations();			
	}

	private void printDetectedRefactoringMotivations() {
		// TODO Auto-generated method stub	
	}

	private boolean isExtractReusableMethod(Refactoring ref) {		
		
			if(ref instanceof ExtractOperationRefactoring) {
				ExtractOperationRefactoring extractOp = (ExtractOperationRefactoring)ref;
				UMLOperation extractedOperation = extractOp.getExtractedOperation();
				UMLOperation sourceOperationAfterRefactoring = extractOp.getSourceOperationAfterExtraction();
				UMLClassBaseDiff classDiff = modelDiff.getUMLClassDiff(sourceOperationAfterRefactoring.getClassName());
				int extractOperationInvocationCount = 0 ;
				
				if(classDiff != null) {
					UMLClass classAfterRefactoring = classDiff.getNextClass();
					for(UMLOperation operation : classAfterRefactoring.getOperations()) {
						//check if other operation in class after refactoring, call the extracted method
						if(!operation.equals(sourceOperationAfterRefactoring) && !operation.equals(extractedOperation)) {
							List<OperationInvocation> invocations = operation.getAllOperationInvocations();
							for(OperationInvocation invocation : invocations) {
								if(invocation.matchesOperation(extractedOperation)) {
									extractOperationInvocationCount++;
									System.out.println();
								}
							}
						}
					}
				}
				/* DETECTION RULE:
				 * IF Invocations to Extracted method from source method after Extraction is more than one OR 
				 *  there are other Invocations from other methods to extracted operation*/
				if(extractOp.getExtractedOperationInvocations().size()>1 || extractOperationInvocationCount != 0) 
				{
					/*AND if the extraction operation is not detected as duplicated removal before
					 * (All Extract Operations that tend to remove duplication are also are reused.)*/
					List<MotivationType> listMotivations = mapRefactoringMotivations.get(ref);
					if(!listMotivations.contains(MotivationType.EM_REMOVE_DUPLICATION)){
						//System.out.print("Motivation: Extract Reusable Method");
						return true;
					}
				}	
			}
			
			return false;	
	}
	
	public boolean isDecomposeMethodToImroveReadability(List<Refactoring> refList) {
		
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
					//System.out.println(ref);
				}
				return true;
			}
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
		
		for(UMLOperation extractOperation : sourceOperationMapWithExtractedOperationAsKey.keySet()){
			List<ExtractOperationRefactoring> listSourceOperations = sourceOperationMapWithExtractedOperationAsKey.get(extractOperation);	
			/*DETECTION RULE: if multiple source operations(The Extract Refactorings that contain them)
			 *  have the same extractedOperation the extract operations motivations is Remove Duplication*/
			if(listSourceOperations.size() > 1){
				for(ExtractOperationRefactoring extractOp : listSourceOperations){
					setRefactoringMotivation(MotivationType.EM_REMOVE_DUPLICATION, extractOp);
				}
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
}