  package gr.uom.java.xmi.diff;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.Annotation;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringType;

import com.sun.javafx.css.Declaration;

import gr.uom.java.xmi.LocationInfo;
import gr.uom.java.xmi.LocationInfo.CodeElementType;
import gr.uom.java.xmi.UMLAnnotation;
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
import gr.uom.java.xmi.decomposition.LeafMapping;
import gr.uom.java.xmi.decomposition.ObjectCreation;
import gr.uom.java.xmi.decomposition.OperationBody;
import gr.uom.java.xmi.decomposition.OperationInvocation;
import gr.uom.java.xmi.decomposition.StatementObject;
import gr.uom.java.xmi.decomposition.TernaryOperatorExpression;
import gr.uom.java.xmi.decomposition.UMLOperationBodyMapper;
import gr.uom.java.xmi.decomposition.VariableDeclaration;
import gr.uom.java.xmi.decomposition.replacement.MethodInvocationReplacement;

public class MotivationExtractor {
	private UMLModelDiff modelDiff;
	private List<Refactoring> refactorings;
	private Map<RefactoringType, List<Refactoring>> mapClassifiedRefactorings;
	private Map<Refactoring, List<MotivationType>> mapRefactoringMotivations;
	private Map<Refactoring, List<MotivationFlag>> mapMotivationFlags;
	private List<ExtractOperationRefactoring> removeDuplicationFromSingleMethodRefactorings = new ArrayList<ExtractOperationRefactoring>();
	private List<ExtractOperationRefactoring> oneLineRemoveDuplications = new ArrayList<ExtractOperationRefactoring>();
	private List<ExtractOperationRefactoring> decomposeToImproveReadabilityFromSingleMethodRefactorings = new ArrayList<ExtractOperationRefactoring>();
	private int decomposeToImproveReadabilityFromMultipleMethodRefactorings = 0;
	private List<ExtractOperationRefactoring> decomposeToImproveReadabilityFromSingleMethodByHavingCallToExtractedMethodInReturn = new ArrayList<ExtractOperationRefactoring>();
	private List<ExtractOperationRefactoring> facilitateExtensionRefactoringsWithExtrensionInParent = new ArrayList<ExtractOperationRefactoring>();

	private int countSingleMethodRemoveDuplications = 0;
	private Map<Refactoring, int[]> mapFacilitateExtensionT1T2 = new HashMap<Refactoring, int[]>();
	private Map<Refactoring, String> mapDecomposeToImproveRedability = new HashMap<Refactoring, String>();

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
		this.mapMotivationFlags = new HashMap<Refactoring, List<MotivationFlag>>();
		classifyRefactoringsByType(refactorings);
	}

	public void detectAllRefactoringMotivations() {
		for (RefactoringType type : mapClassifiedRefactorings.keySet()) {
			detectMotivataion(type);
		}
	}

	public Map<Refactoring, List<MotivationType>> getMapRefactoringMotivations() {
		return mapRefactoringMotivations;
	}

	public Map<Refactoring, List<MotivationFlag>> getMapMotivationFlags() {
		return mapMotivationFlags;
	}

	private void detectExtractMethodWithLoggingSatement(List<Refactoring> listRef) {
		for (Refactoring ref : listRef) {
			if (ref instanceof ExtractOperationRefactoring) {
				ExtractOperationRefactoring extractOpRef = (ExtractOperationRefactoring) ref;
				UMLOperation extractedOperation = extractOpRef.getExtractedOperation();
				isExtractedOperationConsistLoggingStatement(extractedOperation);
			}
		}
	}

	private boolean isExtractedOperationConsistLoggingStatement(UMLOperation extractedOperation) {
		List<StatementObject> extractedMethodLeaves = extractedOperation.getBody().getCompositeStatement().getLeaves();
		for (StatementObject statement : extractedMethodLeaves) {
			int i = 0;
		}
		return false;
	}

	private void detectMotivataion(RefactoringType type) {

		List<Refactoring> listRef = mapClassifiedRefactorings.get(type);
		switch (type) {
		case EXTRACT_OPERATION:
			detectExtractOperationMotivation(listRef);
			// Add Refactorings without motivation
			for (Refactoring ref : listRef) {
				if (mapRefactoringMotivations.isEmpty()) {
					setRefactoringMotivation(MotivationType.NONE, ref);
				} else {
					List<MotivationType> motivationTypes = mapRefactoringMotivations.get(ref);
					if (motivationTypes == null || motivationTypes.size() == 0) {
						setRefactoringMotivation(MotivationType.NONE, ref);
					}
				}
			}
			setExtractMethodInitialMotivationFlags(listRef);
			detectExtractMethodWithLoggingSatement(listRef);
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
			detectInlineOperationMotivation(listRef);
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

	private void setExtractMethodInitialMotivationFlags(List<Refactoring> listRef) {
		for (Refactoring ref : listRef) {
			if (ref.getRefactoringType().equals(RefactoringType.EXTRACT_OPERATION)
					|| ref.getRefactoringType().equals(RefactoringType.EXTRACT_AND_MOVE_OPERATION)) {
				ExtractOperationRefactoring extractOpRef = (ExtractOperationRefactoring) ref;
				if (isExtractedOperationWithAddedParameters(extractOpRef.getExtractedOperation(),
						extractOpRef.getSourceOperationAfterExtraction())) {
					setMotivationFlag(MotivationFlag.EM_HAS_ADDED_PARAMETERS, ref);
				}
			}
		}
	}

	private void detectInlineOperationMotivation(List<Refactoring> listRef) {
		for (Refactoring ref : listRef) {
			if (isInlineMethodToImproveReadability(ref)) {
			    setRefactoringMotivation(MotivationType.IM_IMPROVE_READABILITY, ref);
			}
			if (isInlineMethodCallerBecomesTrivial(ref)) {
				if (!isMotivationDetected(ref, MotivationType.IM_IMPROVE_READABILITY)) {
					setRefactoringMotivation(MotivationType.IM_CALLER_BECOMES_TRIVIAL, ref);
				}
			}
			if (isInlineMethodToEliminateUnncessaryMethod(ref)) {
				if (!isMotivationDetected(ref, MotivationType.IM_IMPROVE_READABILITY) &&
						!isMotivationDetected(ref, MotivationType.IM_CALLER_BECOMES_TRIVIAL)) {
					setRefactoringMotivation(MotivationType.IM_ELIMINATE_UNNECESSARY_METHOD, ref);
				}
			}
			

		}
	}

	private boolean isInlineMethodToImproveReadability(Refactoring ref) {
		InlineOperationRefactoring inlineOperationRef = (InlineOperationRefactoring) ref;
		Set<AbstractCodeMapping> mappings = inlineOperationRef.getBodyMapper().getMappings();
		boolean hasTestAnnotation = inlineOperationRef.getTargetOperationAfterInline().hasTestAnnotation();
		boolean inlinedOperationAndTargetBeforeInlineHasEqualNames = inlineOperationRef.getTargetOperationBeforeInline()
				.getName().equalsIgnoreCase(inlineOperationRef.getInlinedOperation().getName()) ? true : false;
		if ((inlineOperationRef.getInlinedOperation().getBody().statementCount() == 1|| hasTestAnnotation) && !inlinedOperationAndTargetBeforeInlineHasEqualNames) {
			if(!isInlinedStatementIsInTargetBeforeInlineExpression(inlineOperationRef)) {
				return true;
			}
		}
		return false;
	}
	


	private boolean isInlinedStatementIsInTargetBeforeInlineExpression(InlineOperationRefactoring inlineOperationRef) {
		CompositeStatementObject targetOperationBeforeInlineBody  = inlineOperationRef.getTargetOperationBeforeInline().getBody().getCompositeStatement();
		Set<CompositeStatementObject> targetBeforeInlineComposites = getAllInnerNodes(targetOperationBeforeInlineBody);
		for(CompositeStatementObject composite : targetBeforeInlineComposites) {
			List<AbstractExpression> expressions = composite.getExpressions();
			UMLOperation inlinedOperation = inlineOperationRef.getInlinedOperation();
			if(inlinedOperation.statementCount() == 1) {
			  List<StatementObject> inlinedOperationsStatement = inlinedOperation.getBody().getCompositeStatement().getLeaves();
			  for(AbstractExpression expression : expressions) {
				  
				  if(inlinedOperationsStatement.get(0).getString().contains(expression.getString())){
					  return true;
				  }
				  if(inlinedOperationsStatement.get(0).getString().contains(  expression.toString().replace(" == null", ""))){
					  return true;
				  }
				
			  }
			}
		}
		return false;
	}

	private boolean isInlineMethodToEliminateUnncessaryMethod(Refactoring ref) {
		if (ref instanceof InlineOperationRefactoring) {
			InlineOperationRefactoring inlineOperationRef = (InlineOperationRefactoring) ref;
			if (isInlinedOperationUnnecessary(inlineOperationRef)) {
				return true;
			}
		}
		return false;
	}

	private boolean isInlinedOperationUnnecessary(InlineOperationRefactoring inlineOperationRef) {
		UMLOperation inlinedOperation = inlineOperationRef.getInlinedOperation();
		boolean oneLineTargetOperationBeforeInline = inlineOperationRef.getTargetOperationBeforeInline().statementCount() == 1 ?true:false;
		int inlinedOperationStatementCounts = inlinedOperation.statementCount();
		boolean inlinedOperationNodesRemoved = false;
		boolean targetOperationBeforeInlineNodesRemoved = false;		
		inlinedOperationNodesRemoved = isOperationUnecessaryNodesRemovedForInline(inlinedOperation, inlineOperationRef);
		targetOperationBeforeInlineNodesRemoved = isOperationUnecessaryNodesRemovedForInline(inlinedOperation, inlineOperationRef);

		if ((inlinedOperationNodesRemoved || targetOperationBeforeInlineNodesRemoved)) {
			return true;
		}
		return false;
	}

	private boolean isOperationUnecessaryNodesRemovedForInline(UMLOperation operation, InlineOperationRefactoring inlineOperationRef) {
		boolean inlineOperationNodesRemoved = false;
		List<StatementObject> targetOperationAfterInlineLeaves = inlineOperationRef.getTargetOperationAfterInline()
				.getBody().getCompositeStatement().getLeaves();
		
		Set<CompositeStatementObject> innerNodes = getAllInnerNodes(operation.getBody().getCompositeStatement());

		Set<CompositeStatementObject> targetOperationAfterInlineInnerNodes = getAllInnerNodes(
				inlineOperationRef.getTargetOperationAfterInline().getBody().getCompositeStatement());
		
		
		for (CompositeStatementObject innerNode : innerNodes) {
			if (!targetOperationAfterInlineInnerNodes.contains(innerNode)) {
				  inlineOperationNodesRemoved = true;
			}
		}
		for (StatementObject statement : operation.getBody().getCompositeStatement().getLeaves()) {
			if (!targetOperationAfterInlineLeaves.contains(statement)) {
				  inlineOperationNodesRemoved = true;
			}
		}
		if(inlineOperationNodesRemoved) {
			return true;
		}else {
			return false;
		}
	}

	private boolean isInlineMethodCallerBecomesTrivial(Refactoring ref) {
		if (ref instanceof InlineOperationRefactoring) {
			InlineOperationRefactoring inlineOperationRef = (InlineOperationRefactoring) ref;
			int targetOperationBeforeInlineStatementCount = 0;
			UMLOperation targetOperationAfterInline = inlineOperationRef.getTargetOperationAfterInline();
			UMLOperation targetOperationBeforeInline = inlineOperationRef.getTargetOperationBeforeInline();
			targetOperationBeforeInlineStatementCount = targetOperationBeforeInline.getBody().getCompositeStatement()
					.statementCount();
			CompositeStatementObject targetBody = targetOperationAfterInline.getBody().getCompositeStatement();
			List<StatementObject> targetOperationLeaves = targetBody.getLeaves();

			for (StatementObject leaf : targetOperationLeaves) {
				if (!isTargetOperationLeavesInAddedT2OrBodyMappings(leaf, inlineOperationRef)) {
					return false;
				}
			}

			for (CompositeStatementObject innerNode : getAllInnerNodes(targetBody)) {
				if (!isTargetOperationInnerNodesInAddedT2OrBodyMappings(innerNode, inlineOperationRef)) {
					return false;
				}
			}
			if (targetOperationBeforeInlineStatementCount == 1) {
				return false;
			}
		}
		return true;
	}

	private boolean isTargetOperationLeavesInAddedT2OrBodyMappings(StatementObject statement,
			InlineOperationRefactoring inlineOperationRef) {
		boolean leafInAddedT2Leaves = isInlineTargetClassLeafInAddedT2Leaves(statement.getString(), inlineOperationRef);
		boolean leafInBodyMapping = isStatementInlineBodyMappings(statement.getString(), inlineOperationRef);

		if (leafInAddedT2Leaves || leafInBodyMapping) {
			return true;
		}
		return false;
	}

	private boolean isTargetOperationInnerNodesInAddedT2OrBodyMappings(CompositeStatementObject statement,
			InlineOperationRefactoring inlineOperationRef) {
		if (isInlineTargetClassInnerNodeInAddedT2InnerNodes(statement.getString(), inlineOperationRef)
				|| isStatementInlineBodyMappings(statement.getString(), inlineOperationRef)) {
			return true;
		}
		return false;
	}

	boolean isInlineTargetClassLeafInAddedT2Leaves(String leafString, InlineOperationRefactoring inlineOperationRef) {
		List<StatementObject> addedT2Leaves = inlineOperationRef.getBodyMapper().getNonMappedLeavesT2();
		for (StatementObject statement : addedT2Leaves) {
			if (statement.getString().equals(leafString)) {
				return true;
			}
		}
		return false;
	}

	boolean isInlineTargetClassInnerNodeInAddedT2InnerNodes(String innerNodeString,
			InlineOperationRefactoring inlineOperationRef) {
		List<CompositeStatementObject> addedT2InnerNodes = inlineOperationRef.getBodyMapper()
				.getNonMappedInnerNodesT2();
		for (CompositeStatementObject composite : addedT2InnerNodes) {
			if (composite.getString().equals(innerNodeString)) {
				return true;
			}
		}
		return false;
	}

	private boolean isStatementInlineBodyMappings(String statementString,
			InlineOperationRefactoring inlineOperationRef) {
		Set<AbstractCodeMapping> codeMappings = inlineOperationRef.getBodyMapper().getMappings();
		for (AbstractCodeMapping mapping : codeMappings) {
			if (mapping.getFragment2().getString().equals(statementString)) {
				return true;
			}
		}
		return false;
	}

	private void detectMoveClassMotivation(List<Refactoring> listRef) {
		for (Refactoring ref : listRef) {
			if (IsMoveClassToAppropriateContainer(ref)) {
				setRefactoringMotivation(MotivationType.MC_MOVE_CLASS_TO_APPROPRIATE_CONTAINER, ref);
			}
		}
	}

	private boolean IsMoveClassToAppropriateContainer(Refactoring ref) {
		if (ref instanceof MoveClassRefactoring) {
		}
		return false;
	}

	private void detectExtractOperationMotivation(List<Refactoring> listRef) {
		// Motivation Detection algorithms that depends on other refactorings of the
		// same type
		isDecomposeMethodToImroveReadability(listRef);
		postProcessingForIsDecomposeMethodToImroveReadability(listRef);
		isMethodExtractedToRemoveDuplication(listRef);
		// Motivation Detection algorithms that can detect the motivation independently
		// for each refactoring
		for (Refactoring ref : listRef) {
			if (isExtractFacilitateExtension(ref, listRef)) {
				setRefactoringMotivation(MotivationType.EM_FACILITATE_EXTENSION, ref);
			}
			if (isIntroduceAlternativeMethodSignature(ref)) {
				if (!isMotivationDetected(ref, MotivationType.EM_REMOVE_DUPLICATION)) {
					setRefactoringMotivation(MotivationType.EM_INTRODUCE_ALTERNATIVE_SIGNATURE, ref);
					removeRefactoringMotivation(MotivationType.EM_FACILITATE_EXTENSION, ref);
					if (decomposeToImproveReadabilityFromSingleMethodRefactorings.size() > 0) {
						removeRefactoringMotivation(MotivationType.EM_DECOMPOSE_TO_IMPROVE_READABILITY, ref);
					}
				}
			}
			if (!isMotivationDetected(ref, MotivationType.EM_REMOVE_DUPLICATION)) {
				if (isExtractedToImproveTestability(ref)) {
					setRefactoringMotivation(MotivationType.EM_IMPROVE_TESTABILITY, ref);
					// e.g. : JetBrains/intellij-community:7ed3f2
					removeRefactoringMotivation(MotivationType.EM_DECOMPOSE_TO_IMPROVE_READABILITY, ref);
					removeRefactoringMotivation(MotivationType.EM_FACILITATE_EXTENSION, ref);
				}
			}
			// Check if facilitate extension happened in source operation after extraction
			// (parent)
			if (facilitateExtensionRefactoringsWithExtrensionInParent.size() == 0) {
				if (isReplaceMethodPreservingBackwardCompatibility(ref)) {
					if (!isMotivationDetected(ref, MotivationType.EM_REMOVE_DUPLICATION)) {
						setRefactoringMotivation(MotivationType.EM_REPLACE_METHOD_PRESERVING_BACKWARD_COMPATIBILITY,
								ref);
						removeRefactoringMotivation(MotivationType.EM_FACILITATE_EXTENSION, ref);
						removeRefactoringMotivation(MotivationType.EM_IMPROVE_TESTABILITY, ref);
						removeRefactoringMotivation(MotivationType.EM_INTRODUCE_ALTERNATIVE_SIGNATURE, ref);
					}
				}
			}
			if (isExtractReusableMethod(ref, listRef)) {
				if (!isMotivationDetected(ref, MotivationType.EM_REPLACE_METHOD_PRESERVING_BACKWARD_COMPATIBILITY)
						&& !isMotivationDetected(ref, MotivationType.EM_INTRODUCE_ALTERNATIVE_SIGNATURE)) {
					setRefactoringMotivation(MotivationType.EM_REUSABLE_METHOD, ref);
					// removeRefactoringMotivation(MotivationType.EM_FACILITATE_EXTENSION, ref);
					if (decomposeToImproveReadabilityFromMultipleMethodRefactorings == 0
							&& decomposeToImproveReadabilityFromSingleMethodRefactorings.size() > 0) {
						removeRefactoringMotivation(MotivationType.EM_DECOMPOSE_TO_IMPROVE_READABILITY, ref);
					}
				}
			}
			if (isExtractedtoEnableRecursion(ref)) {
				setRefactoringMotivation(MotivationType.EM_ENABLE_RECURSION, ref);
				// removeRefactoringMotivation(MotivationType.EM_FACILITATE_EXTENSION, ref);
			}
			if (isExtractedtoIntroduceAsyncOperation(ref)) {
				setRefactoringMotivation(MotivationType.EM_INTRODUCE_ASYNC_OPERATION, ref);
			}
			if (!isMotivationDetected(ref, MotivationType.EM_DECOMPOSE_TO_IMPROVE_READABILITY)
					&& !isMotivationDetected(ref, MotivationType.EM_REPLACE_METHOD_PRESERVING_BACKWARD_COMPATIBILITY)) {
				if (isExtractedToIntroduceFactoryMethod(ref)) {
					setRefactoringMotivation(MotivationType.EM_INTRODUCE_FACTORY_METHOD, ref);
					removeRefactoringMotivation(MotivationType.EM_FACILITATE_EXTENSION, ref);
					removeRefactoringMotivation(MotivationType.EM_REMOVE_DUPLICATION, ref);
					// Removing Reusable method when it is Introduce Factory method and Introduce
					// alternative method signature.
					if (isMotivationDetected(ref, MotivationType.EM_INTRODUCE_ALTERNATIVE_SIGNATURE)) {
						removeRefactoringMotivation(MotivationType.EM_REUSABLE_METHOD, ref);
					}
				}
			}
			if (isExtractedToEnableOverriding(ref)) {
				if (!isMotivationDetected(ref, MotivationType.EM_REMOVE_DUPLICATION)
						&& decomposeToImproveReadabilityFromSingleMethodRefactorings.size() == 0) {
					setRefactoringMotivation(MotivationType.EM_ENABLE_OVERRIDING, ref);
					removeRefactoringMotivation(MotivationType.EM_REUSABLE_METHOD, ref);
					removeRefactoringMotivation(MotivationType.EM_FACILITATE_EXTENSION, ref);
					removeRefactoringMotivation(MotivationType.EM_INTRODUCE_FACTORY_METHOD, ref);
					removeRefactoringMotivation(MotivationType.EM_DECOMPOSE_TO_IMPROVE_READABILITY, ref);
				}
			}
		}

		postProcessingForIsExtractFacilitateExtension(listRef);
		for (Refactoring ref : listRef) {
			if (isMotivationDetected(ref, MotivationType.EM_FACILITATE_EXTENSION)) {
				removeRefactoringMotivation(MotivationType.EM_REMOVE_DUPLICATION, ref);
			}
		}

		// Print All detected refactorings
		printDetectedRefactoringMotivations();
	}

	private void postProcessingForIsDecomposeMethodToImroveReadability(List<Refactoring> listRef) {
		/*
		 * Checking source operation after Extraction calls to the extracted method to
		 * see if they improve readability in case of calls inside expressions or inside
		 * return statements we consider the extraction is improving the readability
		 * Example: mockito:2d036 , jedis:d4b4a , cassandra:9a3fa
		 * ,JetBrains/intellij-community/commit/7dd55
		 */
		for (Refactoring ref : listRef) {
			if (isExtractedOperationInvokationsToImproveReadability(ref)) {
				if (getDecomposeNormalizedEditDistance((ExtractOperationRefactoring) ref) > 0.55) {
					codeAnalysisDecomposeToImproveRedability(Arrays.asList((ExtractOperationRefactoring) ref));
					decomposeToImproveReadabilityFromSingleMethodRefactorings.add((ExtractOperationRefactoring) ref);
					setRefactoringMotivation(MotivationType.EM_DECOMPOSE_TO_IMPROVE_READABILITY, ref);
					setMotivationFlag(MotivationFlag.EM_INVOCATION_EDIT_DISTANCE_THRESHOLD_SM, ref);
					setMotivationFlag(MotivationFlag.EM_DECOMPOS_SINGLE_METHOD, ref);
				}
			}
		}

		/*
		 * Getter and setter methods are excluded from the decompose to improve
		 * readability case. Example: drools:1bf28 setExpiringHandle
		 */
		for (Refactoring ref : listRef) {
			ExtractOperationRefactoring extractOpRef = (ExtractOperationRefactoring) ref;
			UMLOperation extractedOperation = extractOpRef.getExtractedOperation();
			if ((extractedOperation.isGetter() || extractedOperation.isSetter())) {
				setMotivationFlag(MotivationFlag.EM_GETTER_SETTER, extractOpRef);
				removeRefactoringMotivation(MotivationType.EM_DECOMPOSE_TO_IMPROVE_READABILITY, ref);
				removeMotivationFlag(MotivationFlag.EM_DECOMPOS_SINGLE_METHOD, extractOpRef);
			}
		}
	}

	private void postProcessingForIsExtractFacilitateExtension(List<Refactoring> listRef) {
		// Post Processing for facilitate extension
		/*
		 * Removing facilitate extension from the cases where multiple extract
		 * operations with the same extracted operation (LIKE THE CASE OF REMOVE
		 * DUPLICATION) exists and at least one of them has no new added code . we will
		 * omit the facilitate extension from all the other extract operations of that
		 * group as well. Example: j2objc fa3e6fa
		 */

		Map<String, List<ExtractOperationRefactoring>> listRefGroupedByExtractedOperationNames = listRef.stream()
				.map(x -> (ExtractOperationRefactoring) x)
				.collect(Collectors.groupingBy(x -> x.getExtractedOperation().getName()));

		for (String extractedMethodgroupName : listRefGroupedByExtractedOperationNames.keySet()) {
			boolean noFacilitateExtension = false;
			for (ExtractOperationRefactoring extractOpRef : listRefGroupedByExtractedOperationNames
					.get(extractedMethodgroupName)) {
				if (!isMotivationDetected(extractOpRef, MotivationType.EM_FACILITATE_EXTENSION)) {
					noFacilitateExtension = true;
					break;
				}
			}
			if (noFacilitateExtension) {
				for (ExtractOperationRefactoring extractOpRef : listRefGroupedByExtractedOperationNames
						.get(extractedMethodgroupName)) {
					removeRefactoringMotivation(MotivationType.EM_FACILITATE_EXTENSION, extractOpRef);
				}
			}
		}
	}

	private boolean isExtractedToIntroduceFactoryMethod(Refactoring ref) {
		if (ref instanceof ExtractOperationRefactoring) {
			ExtractOperationRefactoring extractOpRefactoring = (ExtractOperationRefactoring) ref;
			UMLOperation extractedOperation = extractOpRefactoring.getExtractedOperation();
			UMLOperation sourceOperationBeforeExtraction = extractOpRefactoring.getSourceOperationBeforeExtraction();
			boolean sobe_factory_method = isUMLOperationFactoryMethod(sourceOperationBeforeExtraction,
					extractOpRefactoring);
			setMotivationFlag(MotivationFlag.SOBE_FACTORY_METHOD, extractOpRefactoring);
			if (isUMLOperationFactoryMethod(extractedOperation, extractOpRefactoring) && !sobe_factory_method) {
				return true;
			}
		}
		return false;
	}

	private boolean isUMLOperationFactoryMethod(UMLOperation umlOperation, ExtractOperationRefactoring extractOp) {

		List<VariableDeclaration> listVariableDeclerations = umlOperation.getAllVariableDeclarations();
		List<ObjectCreation> listReturnTypeObjectsCreatedInReturnStatement = new ArrayList<ObjectCreation>();
		UMLParameter returnParameter = umlOperation.getReturnParameter();
		if (returnParameter == null) {
			return false;
		}
		UMLType returnParameterType = returnParameter.getType();
		List<String> returnStatementVariables = new ArrayList<String>();
		Map<String, List<ObjectCreation>> returnStatementobjectCreationsMap = new HashMap<String, List<ObjectCreation>>();
		for (AbstractStatement statement : umlOperation.getBody().getCompositeStatement().getStatements()) {
			if (statement.getLocationInfo().getCodeElementType().equals(CodeElementType.RETURN_STATEMENT)) {
				if (umlOperation.equalSignature(extractOp.getExtractedOperation())) {
					setMotivationFlag(MotivationFlag.EM_HAS_RETURN_STATEMENTS, extractOp);
				}
				returnStatementVariables = statement.getVariables();
				returnStatementobjectCreationsMap = statement.getCreationMap();
				int em_return_statement_new_keywords = returnStatementobjectCreationsMap.size();
				if (umlOperation.equalSignature(extractOp.getExtractedOperation())) {
					setMotivationFlag(MotivationFlag.EM_RETURN_STATEMENT_NEW_KEYWORDS
							.setMotivationValue(em_return_statement_new_keywords), extractOp);
				}
				for (String objectCreationString : returnStatementobjectCreationsMap.keySet()) {
					// if(!isStatementInMappings(objectCreationString, extractOpRefactoring)) {
					List<ObjectCreation> listObjectCreation = returnStatementobjectCreationsMap
							.get(objectCreationString);
					for (ObjectCreation objectCreation : listObjectCreation) {
						String statementWithoutObjectCreation = statement.toString().replace(objectCreationString, "")
								.replace(";", "");
						boolean isOnlyObjectCreation = statementWithoutObjectCreation.trim().equals("return") ? true
								: false;
						boolean em_return_equal_new_return = objectCreation.getType().equalClassType(
								returnParameterType) || returnParameterType.equalsWithSubType(objectCreation.getType());
						if (umlOperation.equalSignature(extractOp.getExtractedOperation())) {
							setMotivationFlag(MotivationFlag.EM_RETURN_EQUAL_NEW_RETURN, extractOp);
						}
						if (em_return_equal_new_return || isOnlyObjectCreation) {
							listReturnTypeObjectsCreatedInReturnStatement.add(objectCreation);
						}
					}
					// }
				}
				if (listReturnTypeObjectsCreatedInReturnStatement.size() == 1) {
					return true;
				}
			}
		}
		// Find All the Variable Declerations(that are Object Creation) with same Type
		// as return parameter
		Map<String, List<ObjectCreation>> abstractExpressionObjectCreationsMap = new HashMap<String, List<ObjectCreation>>();
		List<ObjectCreation> abstractExpressionObjectCreations = new ArrayList<ObjectCreation>();
		List<VariableDeclaration> listObjectCreationVariableDeclerationsWithReturnType = new ArrayList<VariableDeclaration>();
		Map<UMLType, List<String>> variableTypeNameMap = new HashMap<UMLType, List<String>>();
		for (VariableDeclaration variableDecleration : listVariableDeclerations) {
			if (variableDecleration.getInitializer() != null) {
				AbstractExpression abstractExpression = variableDecleration.getInitializer();
				abstractExpressionObjectCreationsMap = abstractExpression.getCreationMap();
				for (String objectCreationString : abstractExpressionObjectCreationsMap.keySet()) {
					// ObjectCreation statement should not be in the mappings
					// if(!isStatementInMappings(objectCreationString, extractOpRefactoring)) {
					abstractExpressionObjectCreations = abstractExpressionObjectCreationsMap.get(objectCreationString);
					for (ObjectCreation objectCreation : abstractExpressionObjectCreations) {
						if (objectCreation.getType().equalClassType(returnParameterType)
								|| returnParameterType.equalsWithSubType(objectCreation.getType())) {
							listObjectCreationVariableDeclerationsWithReturnType.add(variableDecleration);
						}
					}
					for (VariableDeclaration objectCreationVariableDecleration : listObjectCreationVariableDeclerationsWithReturnType) {
						if (variableTypeNameMap.containsKey(variableDecleration.getType())) {
							variableTypeNameMap.get(objectCreationVariableDecleration.getType())
									.add(objectCreationVariableDecleration.getVariableName());
						} else {
							List<String> variableNames = new ArrayList<String>();
							variableNames.add(objectCreationVariableDecleration.getVariableName());
							variableTypeNameMap.put(objectCreationVariableDecleration.getType(), variableNames);
						}
					}
					// }
				}
			}
		}
		// Check if return statement returns a Variable Declerations(that are Object
		// Creation) with Return parameter type
		for (UMLType type : variableTypeNameMap.keySet()) {
			List<String> variableNames = variableTypeNameMap.get(type);
			for (String variableName : variableNames) {
				if (returnStatementVariables.size() == 1 && returnStatementVariables.contains(variableName)) {
					setMotivationFlag(MotivationFlag.EM_OBJECT_CREATION_VARIABLE_RETURNED, extractOp);
					// Check if all statements in the Extracted Operation are object creation
					// related
					if (isAllStatementsObjectCreationRelated(umlOperation,
							listObjectCreationVariableDeclerationsWithReturnType)) {
						if (umlOperation.equalSignature(extractOp.getExtractedOperation())) {
							setMotivationFlag(MotivationFlag.EM_VARS_FACTORY_METHOD_RELATED, extractOp);
						}
						return true;
					}
				}
			}
		}

		return false;
	}

	private boolean isStatementInMappings(String statementString, ExtractOperationRefactoring extractOpRef) {
		Set<AbstractCodeMapping> abstractCodeMappings = extractOpRef.getBodyMapper().getMappings();
		for (AbstractCodeMapping abstractCodeMapping : abstractCodeMappings) {
			AbstractCodeFragment abstractCodeFragment = abstractCodeMapping.getFragment2();
			if (abstractCodeFragment.toString().contains(statementString)) {
				return true;
			}
		}
		return false;
	}

	private boolean isAllStatementsObjectCreationRelated(UMLOperation extractedOperation,
			List<VariableDeclaration> returnTypeObjectCreationVariableDeclerations) {

		List<VariableDeclaration> allVariableDeclerations = extractedOperation.getBody().getAllVariableDeclarations();
		// List<String> allObjectCreationRelatedVariables = new ArrayList<String>();
		Set<String> allObjectCreationRelatedVariables = new HashSet<String>();
		List<String> allObjectStateSettingVariables = new ArrayList<String>();
		Set<String> allFactoryMethodRelatedVariables = new HashSet<String>();

		if (returnTypeObjectCreationVariableDeclerations.size() == 1) {
			AbstractExpression returnTypeObjectCreationExpression = returnTypeObjectCreationVariableDeclerations.get(0)
					.getInitializer();
			String objectCreationVariable = returnTypeObjectCreationVariableDeclerations.get(0).getVariableName();
			allObjectStateSettingVariables = getAllObjectStateSettingVariables(extractedOperation,
					objectCreationVariable);
			allObjectCreationRelatedVariables = getAllObjectCreationRelatedVariables(returnTypeObjectCreationExpression,
					allVariableDeclerations);
			allFactoryMethodRelatedVariables.addAll(allObjectCreationRelatedVariables);
			allFactoryMethodRelatedVariables.addAll(allObjectStateSettingVariables);
			allFactoryMethodRelatedVariables.addAll(getAllObjectCreationRelatedCompositeStatementVariables(
					extractedOperation, allFactoryMethodRelatedVariables));
			for (VariableDeclaration variableDecleration : allVariableDeclerations) {
				if (!variableDecleration.equals(returnTypeObjectCreationVariableDeclerations.get(0))) {
					// Check if there is a variable declaration that variable is not part of Object
					// creation
					if (!allFactoryMethodRelatedVariables.contains(variableDecleration.getVariableName())) {
						return false;
					}
				}
			}
		}
		return true;
	}

	private Set<String> getAllObjectCreationRelatedCompositeStatementVariables(UMLOperation operation,
			Set<String> relatedVariables) {
		List<CompositeStatementObject> compositeStatements = operation.getBody().getCompositeStatement()
				.getInnerNodes();
		for (CompositeStatementObject compositeStatement : compositeStatements) {
			List<String> variables = compositeStatement.getVariables();
			List<AbstractExpression> abstractExpressions = compositeStatement.getExpressions();
			for (String variable : variables) {
				if (relatedVariables.contains(variable)) {
					for (AbstractExpression expression : abstractExpressions) {
						relatedVariables.addAll(expression.getVariables());
					}
				}
			}
		}
		return relatedVariables;
	}

	private List<String> getAllObjectStateSettingVariables(UMLOperation operation, String ObjectCreatianiVariableName) {
		List<String> allObjectStateSettingVariables = new ArrayList<String>();
		ObjectCreatianiVariableName += ".";
		for (StatementObject statement : operation.getBody().getCompositeStatement().getLeaves()) {
			if (statement.toString().startsWith(ObjectCreatianiVariableName)) {
				allObjectStateSettingVariables.addAll(statement.getVariables());
			}
		}
		return allObjectStateSettingVariables;
	}

	private Set<String> getAllObjectCreationRelatedVariables(AbstractExpression returnTypeObjectCreationExpression,
			List<VariableDeclaration> allVariableDeclerations) {
		Set<String> returnTypeObjectCreationExpressionVariables = new HashSet<String>();
		Set<String> otherDependentObjectCreationVariables = new HashSet<String>();
		returnTypeObjectCreationExpressionVariables.addAll(returnTypeObjectCreationExpression.getVariables());
		otherDependentObjectCreationVariables.addAll(getObjectCreationRelatedVariables(
				returnTypeObjectCreationExpressionVariables, allVariableDeclerations));
		returnTypeObjectCreationExpressionVariables.addAll(otherDependentObjectCreationVariables);
		return returnTypeObjectCreationExpressionVariables;
	}

	private Set<String> getObjectCreationRelatedVariables(Set<String> variables,
			List<VariableDeclaration> allVariableDeclerations) {
		int intialVariableSize = variables.size();
		int newVariableVariableSize = 0;
		for (VariableDeclaration variableDecleration : allVariableDeclerations) {
			String variableName = variableDecleration.getVariableName();
			if (variables.contains(variableName)) {
				AbstractExpression variableInitializer = variableDecleration.getInitializer();
				if (variableInitializer != null) {
					variables.addAll(variableInitializer.getVariables());
				}
			}
		}
		newVariableVariableSize = variables.size();
		if (newVariableVariableSize > intialVariableSize) {

			variables.addAll(getObjectCreationRelatedVariables(variables, allVariableDeclerations));
		}
		return variables;

	}

	private boolean isExtractedtoIntroduceAsyncOperation(Refactoring ref) {

		if (ref instanceof ExtractOperationRefactoring) {
			ExtractOperationRefactoring extractOpRefactoring = (ExtractOperationRefactoring) ref;
			UMLOperation sourceOperationAfterExtraction = extractOpRefactoring.getSourceOperationAfterExtraction();
			UMLOperation extractedOperation = extractOpRefactoring.getExtractedOperation();
			OperationBody sourceBody = sourceOperationAfterExtraction.getBody();
			CompositeStatementObject sourceCompositeStatement = sourceBody.getCompositeStatement();
			for (AbstractStatement statement : sourceCompositeStatement.getStatements()) {
				if (statement.getTypes().contains("Runnable")) {
					setMotivationFlag(MotivationFlag.SOAE_STATEMENTS_CONTAIN_RUNNABLE_TYPE, extractOpRefactoring);
					List<AnonymousClassDeclarationObject> anonymousClassDeclerations = statement
							.getAnonymousClassDeclarations();
					for (AnonymousClassDeclarationObject decleration : anonymousClassDeclerations) {
						Map<String, List<OperationInvocation>> declerationMethodInvocationMap = decleration
								.getMethodInvocationMap();
						for (String methodInvocation : declerationMethodInvocationMap.keySet()) {
							List<OperationInvocation> invocations = declerationMethodInvocationMap
									.get(methodInvocation);
							for (OperationInvocation invocation : invocations) {
								if (invocation.matchesOperation(extractedOperation,
										sourceOperationAfterExtraction.variableDeclarationMap(), modelDiff)) {
									setMotivationFlag(MotivationFlag.SOAE_ANONYMOUS_CLASS_RUNNABLE_EM_INVOCATION,
											extractOpRefactoring);
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
		if (ref instanceof ExtractOperationRefactoring) {
			ExtractOperationRefactoring extractOpRefactoring = (ExtractOperationRefactoring) ref;
			UMLOperation extractedOperation = extractOpRefactoring.getExtractedOperation();
			UMLClassBaseDiff extractedOperationClassDiff = modelDiff.getUMLClassDiff(extractedOperation.getClassName());
			if (extractedOperationClassDiff != null) {
				UMLClass extractedOperationNextClass = extractedOperationClassDiff.getNextClass();
				for (UMLClassDiff classDiff : modelDiff.getCommonClassDiffList()) {
					UMLClass nextClass = classDiff.getNextClass();
					isOperationOverridenInClass(extractedOperation, extractedOperationNextClass, nextClass,
							operationsOverridingeExtractedOperations, extractOpRefactoring);
				}
				for (UMLClass addedClass : modelDiff.getAddedClasses()) {
					isOperationOverridenInClass(extractedOperation, extractedOperationNextClass, addedClass,
							operationsOverridingeExtractedOperations, extractOpRefactoring);
				}
			}
			/*
			 * DETECTION RULE: 1-Check if any subclasses is overriding the extracted
			 * operation OR 2-Check the UML operation comments to see if it contains any
			 * keywords about overriding.
			 */
			if (isUmlOperationCommentUsingOverridingKeywords(extractedOperation)) {
				setMotivationFlag(MotivationFlag.EM_OVERRIDING_KEYWORD_IN_COMMENT, extractOpRefactoring);
			}
			if ((operationsOverridingeExtractedOperations.size() > 0)
					|| isUmlOperationCommentUsingOverridingKeywords(extractedOperation)) {
				return true;
			}
		}
		return false;
	}

	private void isOperationOverridenInClass(UMLOperation extractedOperation, UMLClass extractedOperationNextClass,
			UMLClass nextClass, List<UMLOperation> operationsOverridingeExtractedOperations, Refactoring extractOp) {
		List<UMLAnonymousClass> listAnonymousUmlClasses = nextClass.getAnonymousClassList();
		if (nextClass.isSubTypeOf(extractedOperationNextClass)) {
			for (UMLOperation operation : nextClass.getOperations()) {
				if (operation.equalSignature(extractedOperation)) {
					operationsOverridingeExtractedOperations.add(operation);
					setMotivationFlag(MotivationFlag.EM_EQUAL_OPERATION_SIGNATURE_IN_SUBTYPE, extractOp);
				}
			}
		}
		for (UMLAnonymousClass anonymousClass : listAnonymousUmlClasses) {
			for (UMLOperation operation : anonymousClass.getOperations()) {
				if (operation.equalSignature(extractedOperation))
					operationsOverridingeExtractedOperations.add(operation);
			}
		}
	}

	private boolean isUmlOperationCommentUsingOverridingKeywords(UMLOperation operation) {
		UMLJavadoc javaDoc = operation.getJavadoc();
		if (javaDoc != null) {
			for (UMLTagElement tagElement : javaDoc.getTags()) {
				// Only process general purpose unnamed tags
				if (tagElement.getTagName() == null) {
					for (String fragment : tagElement.getFragments()) {
						if (fragment.toLowerCase().contains("override") || fragment.toLowerCase().contains("overriding")
								|| fragment.toLowerCase().contains("overriden")
								|| fragment.toLowerCase().contains("subclass")) {
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
		if (ref instanceof ExtractOperationRefactoring) {
			ExtractOperationRefactoring extractOpRefactoring = (ExtractOperationRefactoring) ref;
			UMLOperation extractedOperation = extractOpRefactoring.getExtractedOperation();
			for (UMLClassDiff classDiff : modelDiff.getCommonClassDiffList()) {
				UMLClass nextClass = classDiff.getNextClass();
				isOperationTestedInClass(nextClass, extractedOperation, operationsTestingExtractedOperation,
						extractOpRefactoring, classDiff);
			}
			for (UMLClass addedClass : modelDiff.getAddedClasses()) {
				isOperationTestedInClass(addedClass, extractedOperation, operationsTestingExtractedOperation,
						extractOpRefactoring, null);
			}
		}
		if (operationsTestingExtractedOperation.size() > 0) {
			return true;
		}
		return false;
	}

	private void isOperationTestedInClass(UMLClass nextClass, UMLOperation extractedOperation,
			List<UMLOperation> operationsTestingExtractedOperation, ExtractOperationRefactoring extractOpRefactoring,
			UMLClassDiff classDiff) {
		String extractedOperationClassName = extractOpRefactoring.getExtractedOperation().getClassName();
		if (!nextClass.getName().equals(extractedOperationClassName)) {
			for (UMLOperation operation : nextClass.getOperations()) {
				if (operation.hasTestAnnotation() || operation.getName().startsWith("test")
						|| nextClass.isTestClass()) {
					setMotivationFlag(MotivationFlag.EM_TEST_INVOCATION_CLASS_EQUAL_TO_EM_CLASS, extractOpRefactoring);
					setMotivationFlag(MotivationFlag.EM_INVOCATION_IN_TEST_OPERATION, extractOpRefactoring);
					for (OperationInvocation invocation : operation.getAllOperationInvocations()) {
						if (invocation.matchesOperation(extractedOperation, operation.variableDeclarationMap(),
								modelDiff)) {
							if (classDiff == null) {
								setMotivationFlag(MotivationFlag.EM_TEST_INVOCATION_IN_ADDED_NODE,
										extractOpRefactoring);
								// classDiff is null when we have an added class and therefore invocation is in
								// added operation.
								operationsTestingExtractedOperation.add(operation);
							} else {
								// In the case of modified classes
								// 1st: Check added operations in modified class
								if (classDiff.addedOperations.contains(operation)) {
									operationsTestingExtractedOperation.add(operation);
									setMotivationFlag(MotivationFlag.EM_TEST_INVOCATION_IN_ADDED_NODE,
											extractOpRefactoring);

								}
								// 2md:Check edited operations for elements that call extracted operation
								List<UMLOperationBodyMapper> umlBodyMappers = classDiff.getOperationBodyMapperList();
								for (UMLOperationBodyMapper bodyMapper : umlBodyMappers) {
									if (bodyMapper.getOperation2().equalSignature(operation)) {
										if (bodyMapper.nonMappedElementsT2CallingAddedOperation(
												Arrays.asList(extractedOperation)) > 0) {
											// Added Element T2 in Test Operation is calling extracted operation
											operationsTestingExtractedOperation.add(operation);
											setMotivationFlag(MotivationFlag.EM_TEST_INVOCATION_IN_ADDED_NODE,
													extractOpRefactoring);
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}

	private boolean isExtractedtoEnableRecursion(Refactoring ref) {
		if (ref instanceof ExtractOperationRefactoring) {
			ExtractOperationRefactoring extractOpRefactoring = (ExtractOperationRefactoring) ref;
			UMLOperation sourceOperationBeforeExtraction = extractOpRefactoring.getSourceOperationBeforeExtraction();
			UMLOperation extractedOperation = extractOpRefactoring.getExtractedOperation();
			boolean sobe_recursive = isUmlOperationRecursive(sourceOperationBeforeExtraction);
			boolean em_recursive = isUmlOperationRecursive(extractedOperation);
			if (sobe_recursive) {
				setMotivationFlag(MotivationFlag.SOBE_RECURSIVE, extractOpRefactoring);
			}
			if (em_recursive) {
				setMotivationFlag(MotivationFlag.EM_RECURSIVE, extractOpRefactoring);
			}
			if (!sobe_recursive && em_recursive) {
				return true;
			}
		}
		return false;
	}

	private boolean isUmlOperationRecursive(UMLOperation operation) {
		List<OperationInvocation> listInvokations = operation.getAllOperationInvocations();
		List<OperationInvocation> recursiveInvokations = new ArrayList<OperationInvocation>();
		for (OperationInvocation invocation : listInvokations) {
			boolean noExpression = invocation.getExpression() == null;
			boolean thisExpression = invocation.getExpression() != null && invocation.getExpression().equals("this");
			boolean noOrThisExpresion = noExpression || thisExpression;
			if (invocation.matchesOperation(operation, operation.variableDeclarationMap(), modelDiff)
					&& noOrThisExpresion) {
				recursiveInvokations.add(invocation);
			}
		}
		if (recursiveInvokations.size() > 0) {
			return true;
		}
		return false;
	}

	private boolean isReplaceMethodPreservingBackwardCompatibility(Refactoring ref) {
		if (ref instanceof ExtractOperationRefactoring) {
			ExtractOperationRefactoring extractOpRefactoring = (ExtractOperationRefactoring) ref;
			UMLOperation sourceOpAfterExtraction = extractOpRefactoring.getSourceOperationAfterExtraction();
			;
			OperationBody sourceOpBodyAfterExtraction = sourceOpAfterExtraction.getBody();
			int countStatements = sourceOpBodyAfterExtraction.statementCount();

			if ((countStatements == 1) && getAllOperationInvocationToExtractedMethod(sourceOpAfterExtraction,
					extractOpRefactoring.getExtractedOperation()).size() == 1) {
				setMotivationFlag(MotivationFlag.SOAE_IS_DELEGATE_TO_EM, extractOpRefactoring);
			}
			if (countStatements == 1) {
				if (isExtractOperationForBackwardCompatibility(ref)) {
					return true;
				}
			} else {
				// Temporary Variables should be excluded.
				if (isUmlOperationStatementsAllTempVariables(sourceOpAfterExtraction, extractOpRefactoring)) {
					if (isExtractOperationForBackwardCompatibility(ref)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private boolean isExtractOperationForBackwardCompatibility(Refactoring ref) {
		if (ref instanceof ExtractOperationRefactoring) {
			ExtractOperationRefactoring extractOpRefactoring = (ExtractOperationRefactoring) ref;
			UMLOperation extractedOperation = extractOpRefactoring.getExtractedOperation();
			UMLOperation sourceOpAfterExtraction = extractOpRefactoring.getSourceOperationAfterExtraction();
			List<OperationInvocation> listExtractedOpInvokations = extractOpRefactoring
					.getExtractedOperationInvocations();

			boolean em_soae_equal_parameter_types = sourceOpAfterExtraction.equalParameterTypes(extractedOperation);
			setMotivationFlag(MotivationFlag.EM_SOAE_EQUAL_PARAMETER_TYPES, extractOpRefactoring);
			boolean em_soae_equal_names = extractedOperation.getName().equals(sourceOpAfterExtraction.getName());
			setMotivationFlag(MotivationFlag.EM_SOAE_EQUAL_NAMES, extractOpRefactoring);
			boolean isBackwardCompatible = (!em_soae_equal_parameter_types || !em_soae_equal_names);
			String extractedOperationAccessModifier = extractedOperation.getVisibility();
			String sourceOperationAfterExtractionAccessModifier = sourceOpAfterExtraction.getVisibility();

			boolean soae_protected = sourceOperationAfterExtractionAccessModifier.equals("protected");
			boolean soae_private = sourceOperationAfterExtractionAccessModifier.equals("private");
			setMotivationFlag(MotivationFlag.SOAE_PROTECTED, extractOpRefactoring);
			setMotivationFlag(MotivationFlag.SOAE_PRIVATE, extractOpRefactoring);
			boolean isSourceOperationAfterExtractionAndExtractedOperationModifiersProtectedOrPrivate = soae_protected
					|| soae_private ? true : false;
			/*
			 * DETECTION RULE: Check IF the method parameters OR name has changed AND if
			 * source Operation after extraction is a delegate AND also check if it
			 * contains @deprecated in annotations or JavaDoc
			 */
			if (isBackwardCompatible && (listExtractedOpInvokations.size() == 1)
					&& !isSourceOperationAfterExtractionAndExtractedOperationModifiersProtectedOrPrivate) {
				if (isUmlOperationWithDeprecatedAnnotation(sourceOpAfterExtraction)
						|| isUmlOperationJavaDocContainsTagName(sourceOpAfterExtraction, "@deprecated")) {
					setMotivationFlag(MotivationFlag.SOAE_DEPRECATED, extractOpRefactoring);
					if (isMotivationDetected(ref, MotivationType.EM_INTRODUCE_ALTERNATIVE_SIGNATURE)) {
						removeRefactoringMotivation(MotivationType.EM_INTRODUCE_ALTERNATIVE_SIGNATURE, ref);
					}
					return true;
				} else {
					// In this case introducing an alternative method has priority over backward
					// compatibility if it is previously detected
					if (isMotivationDetected(ref, MotivationType.EM_INTRODUCE_ALTERNATIVE_SIGNATURE)) {
						return false;
					} else {
						return true;
					}
				}
			}
		}
		return false;
	}

	private boolean isUmlOperationJavaDocContainsTagName(UMLOperation operation, String tagName) {
		UMLJavadoc javaDoc = operation.getJavadoc();
		if (javaDoc != null) {
			for (UMLTagElement tagElements : javaDoc.getTags()) {
				if (tagElements.getTagName() != null) {
					if (tagElements.getTagName().toLowerCase().equals("@deprecated")) {
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
		// Check the operation annotation in operation Class
		List<UMLAnnotation> sourceOperationAnnotations = umlOperation.getAnnotations();
		for (UMLAnnotation annotation : sourceOperationAnnotations) {
			if (annotation.getTypeName().toString().equals("Deprecated")) {
				isDeprecatedInClass = true;
				break;
			}
		}
		// Check the operation annotation in implemented interface
		UMLClassBaseDiff umlClassDiff = modelDiff.getUMLClassDiff(umlOperation.getClassName());
		if (umlClassDiff != null) {
			UMLClass nextClass = umlClassDiff.getNextClass();
			List<UMLType> extractedOperationImplementedInterfaces = nextClass.getImplementedInterfaces();
			for (UMLType implementedInterface : extractedOperationImplementedInterfaces) {
				UMLClassBaseDiff interfaceUmlClassDiff = modelDiff.getUMLClassDiff(implementedInterface);
				if (interfaceUmlClassDiff != null) {
					UMLClass interfaceClass = interfaceUmlClassDiff.getNextClass();
					if (interfaceClass != null) {
						for (UMLOperation operation : interfaceClass.getOperations()) {
							if (operation.equalSignature(umlOperation)) {
								for (UMLAnnotation annotation : operation.getAnnotations()) {
									if (annotation.getTypeName().toString().equals("Deprecated")) {
										isDeprecatedInImplementedInterface = true;
										break;
									}
								}
							}
							if (isDeprecatedInImplementedInterface) {
								break;
							}
						}
					}
				}
			}
		}

		return (isDeprecatedInClass || isDeprecatedInImplementedInterface) ? true : false;

	}

	private boolean isIntroduceAlternativeMethodSignature(Refactoring ref) {

		if (ref instanceof ExtractOperationRefactoring) {
			ExtractOperationRefactoring extractOpRefactoring = (ExtractOperationRefactoring) ref;
			UMLOperation sourceOpAfterExtraction = extractOpRefactoring.getSourceOperationAfterExtraction();
			UMLOperation extractedOperation = extractOpRefactoring.getExtractedOperation();
			isExtractedOperationWithAddedParameters(extractedOperation, sourceOpAfterExtraction);
			OperationBody sourceOpBodyAfterExtraction = sourceOpAfterExtraction.getBody();
			int countStatements = sourceOpBodyAfterExtraction.statementCount();
			if ((countStatements == 1) && getAllOperationInvocationToExtractedMethod(sourceOpAfterExtraction,
					extractOpRefactoring.getExtractedOperation()).size() == 1) {
				setMotivationFlag(MotivationFlag.SOAE_IS_DELEGATE_TO_EM, extractOpRefactoring);
			}
			if (countStatements == 1) {
				if (isExtractOperationToIntroduceAlternativeMethod(ref)) {
					return true;
				}

			} else {
				// Excluding cases with more than one invocation in source operation After
				// extraction to extracted method
				List<OperationInvocation> countExtractedOperationInvocations = getAllOperationInvocationToExtractedMethod(
						sourceOpAfterExtraction, extractOpRefactoring.getExtractedOperation());
				if (countExtractedOperationInvocations.size() > 1) {
					return false;
				}
				// Temporary Variables should be excluded.
				if (isUmlOperationStatementsAllTempVariables(sourceOpAfterExtraction, extractOpRefactoring)) {
					if (isExtractOperationToIntroduceAlternativeMethod(ref)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private boolean isExtractedOperationWithAddedParameters(UMLOperation extractedOperation,
			UMLOperation sourceOpAfterExtraction) {
		if (extractedOperation.getParametersWithoutReturnType().size() > sourceOpAfterExtraction
				.getParametersWithoutReturnType().size()) {
			return true;
		}
		return false;
	}

	private boolean isExtractOperationToIntroduceAlternativeMethod(Refactoring ref) {

		if (ref instanceof ExtractOperationRefactoring) {
			ExtractOperationRefactoring extractOpRefactoring = (ExtractOperationRefactoring) ref;
			UMLOperation extractedOperation = extractOpRefactoring.getExtractedOperation();
			UMLOperation sourceOpAfterExtraction = extractOpRefactoring.getSourceOperationAfterExtraction();
			List<OperationInvocation> listExtractedOpInvokations = extractOpRefactoring
					.getExtractedOperationInvocations();
			boolean isEqualParameters = sourceOpAfterExtraction.equalParameterTypes(extractedOperation);
			setMotivationFlag(MotivationFlag.EM_SOAE_EQUAL_PARAMETER_TYPES, extractOpRefactoring);
			// boolean isEqualNames =
			// extractedOperation.getName().equals(sourceOpAfterExtraction.getName());
			// boolean isEqualParametersDifferentNames = isEqualParameters && !isEqualNames
			// ;
			boolean isToIntroduceAlternativeMethod = !isEqualParameters ? true : false;
			/*
			 * DETECTION RULE: Check IF the method parameters has changed AND if source
			 * Operation after extraction is a delegate
			 */
			if (isToIntroduceAlternativeMethod && (listExtractedOpInvokations.size() == 1)) {
				// if(!isMotivationDetected(extractOpRefactoring,
				// MotivationType.EM_REPLACE_METHOD_PRESERVING_BACKWARD_COMPATIBILITY)) {
				return true;

				// }
			}
		}
		return false;
	}

	private boolean isUmlOperationStatementsAllTempVariables(UMLOperation sourceOperationAfterExtraction,
			ExtractOperationRefactoring extractOpRefactoring) {
		CompositeStatementObject compositeStatement = sourceOperationAfterExtraction.getBody().getCompositeStatement();
		List<AbstractStatement> abstractStatements = compositeStatement.getStatements();
		Set<CodeElementType> codeElementTypeSet = new HashSet<CodeElementType>();
		List<AbstractStatement> nonTempAbstractStatements = new ArrayList<AbstractStatement>();
		List<StatementObject> statementsCallingExtractedOperation = new ArrayList<StatementObject>();
		statementsCallingExtractedOperation.addAll(getStatementsCallingExtractedOperation(extractOpRefactoring,
				CodeElementType.VARIABLE_DECLARATION_STATEMENT));
		statementsCallingExtractedOperation.addAll(
				getStatementsCallingExtractedOperation(extractOpRefactoring, CodeElementType.EXPRESSION_STATEMENT));
		codeElementTypeSet.add(CodeElementType.VARIABLE_DECLARATION_STATEMENT);
		// codeElementTypeSet.add(CodeElementType.RETURN_STATEMENT);//Considering return
		// statements as Temp
		for (AbstractStatement statement : abstractStatements) {
			CodeElementType statementType = statement.getLocationInfo().getCodeElementType();
			boolean statementVariableIncludesParameterNames = isStatementUsingUMLOperationParametersNames(statement,
					sourceOperationAfterExtraction);
			boolean statementContainsInvocationToExtractedMethod = false;
			for (StatementObject statementObject : statementsCallingExtractedOperation) {
				if (statement.equals(statementObject)) {
					statementContainsInvocationToExtractedMethod = true;
					break;
				}
			}
			if (!codeElementTypeSet.contains(statementType)
					&& !statementContainsInvocationToExtractedMethod /* && !statementVariableIncludesParameterNames */ ) {
				nonTempAbstractStatements.add(statement);
			} else {
				setMotivationFlag(MotivationFlag.EM_SOAE_EQUAL_PARAMETER_TYPES, extractOpRefactoring);
			}
		}
		if (nonTempAbstractStatements.size() == 0) {
			return true;
		} else {
			return false;
		}
	}

	private boolean isStatementUsingUMLOperationParametersNames(AbstractStatement statement, UMLOperation operation) {
		List<String> sourceOperationAfterExtarctionParameterNames = new ArrayList<String>();
		List<UMLParameter> sourceOperationAfterExtarctionParameters = operation.getParametersWithoutReturnType();
		for (UMLParameter parameter : sourceOperationAfterExtarctionParameters) {
			sourceOperationAfterExtarctionParameterNames.add(parameter.getName());
		}
		List<String> variableNames = statement.getVariables();
		for (String variable : variableNames) {
			if (sourceOperationAfterExtarctionParameterNames.contains(variable)) {
				return true;
			}
		}
		return false;
	}

	private List<OperationInvocation> getAllOperationInvocationToExtractedMethod(
			UMLOperation sourceOperationAfterExtraction, UMLOperation extractedOperation) {
		List<OperationInvocation> allExtractedMethodInvocations = new ArrayList<OperationInvocation>();
		for (OperationInvocation invocation : sourceOperationAfterExtraction.getAllOperationInvocations()) {
			if (invocation.matchesOperation(extractedOperation)) {
				allExtractedMethodInvocations.add(invocation);
			}
		}
		return allExtractedMethodInvocations;
	}

	private boolean isStatementHavingInvocationsToExtractedOperation(AbstractStatement statement,
			UMLOperation extractedOperation) {
		for (String invocationString : statement.getMethodInvocationMap().keySet()) {
			for (OperationInvocation invocation : statement.getMethodInvocationMap().get(invocationString)) {
				if (invocation.matchesOperation(extractedOperation)) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean isExtractFacilitateExtension(Refactoring ref, List<Refactoring> refList) {
		if (ref instanceof ExtractOperationRefactoring) {
			ExtractOperationRefactoring extractOperationRefactoring = (ExtractOperationRefactoring) ref;
			UMLOperationBodyMapper umlBodyMapper = extractOperationRefactoring.getBodyMapper();
			UMLOperation extractedOperation = extractOperationRefactoring.getExtractedOperation();
			UMLOperation sourceOperationAfterExtrction = extractOperationRefactoring
					.getSourceOperationAfterExtraction();
			int countChildNonMappedLeavesAndInnerNodesT2 = 0;
			int countParentNonMappedLeavesAndInnerNodesT2 = 0;
			if (isExtractedMethodMappingAddedTernaryOperator(extractOperationRefactoring)) {
				setMotivationFlag(MotivationFlag.EM_MAPPING_FRAGMENT2_TERNARY, extractOperationRefactoring);
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
			List<CompositeStatementObject> parentListNotMappedInnerNodesT2 = umlBodyMapper.getParentMapper()
					.getNonMappedInnerNodesT2();

			int em_NotMapped_T2_unfiltered = listNotMappedLeavesT2.size() + listNotMappedInnerNodesT2.size();
			int soae_NotMapped_T2_unfiltered = parentListNotMappedLeavesT2.size()
					+ parentListNotMappedInnerNodesT2.size();
			setMotivationFlag(MotivationFlag.EM_NOTMAPPED_T2_UNFILTERED.setMotivationValue(em_NotMapped_T2_unfiltered),
					extractOperationRefactoring);
			setMotivationFlag(
					MotivationFlag.SOAE_NOT_MAPPED_T2_UNFILTERED.setMotivationValue(soae_NotMapped_T2_unfiltered),
					extractOperationRefactoring);

			Set<StatementObject> setParentMarkedT2Leaves = new HashSet<StatementObject>();
			Set<CompositeStatementObject> setParentMarkedT2InnerNodes = new HashSet<CompositeStatementObject>();
			Set<StatementObject> setChildMarkedT2Leaves = new HashSet<StatementObject>();
			Set<CompositeStatementObject> setChildMarkedT2InnerNodes = new HashSet<CompositeStatementObject>();

			/*
			 * Lists for detecting and excluding the cases in which the T2 node
			 * (parent/child - leaf/InnerNode) includes statements that are in T1 by
			 * error(Deleted nodes) Example: Facebook/buck:f26d23 , structr:6c5905
			 */
			List<StatementObject> listNotMappedleafNodesT1 = umlBodyMapper.getNonMappedLeavesT1();
			List<CompositeStatementObject> listNotMappedInnerNodesT1 = umlBodyMapper.getNonMappedInnerNodesT1();
			List<StatementObject> parentListNotMappedleafNodesT1 = umlBodyMapper.getParentMapper()
					.getNonMappedLeavesT1();
			List<CompositeStatementObject> parentListNotMappedInnerNodesT1 = umlBodyMapper.getParentMapper()
					.getNonMappedInnerNodesT1();
			List<CompositeStatementObject> listParentT2InnerNodeInT1InnerNodes = new ArrayList<CompositeStatementObject>();
			List<StatementObject> listParentT2LeafNodeInT1LeafNodes = new ArrayList<StatementObject>();
			List<StatementObject> listParentLeafWithInvocationsExpressionsInExtractOperationInvocationParameters = new ArrayList<StatementObject>();
			List<StatementObject> listParentLeafWithDeclaredVariableInExtractOperationInvocationParameters = new ArrayList<StatementObject>();
			List<CompositeStatementObject> listChildT2InnerNodeInT1InnerNodes = new ArrayList<CompositeStatementObject>();
			List<StatementObject> listChildT2LeafNodeInT1LeafNodes = new ArrayList<StatementObject>();

			List<StatementObject> listParentT2LeafNodesInMappings = new ArrayList<StatementObject>();
			List<StatementObject> listChildT2LeafNodesInChildMappings = new ArrayList<StatementObject>();
			List<StatementObject> listChildT2LeafNodesInParentMappings = new ArrayList<StatementObject>();
			Set<AbstractCodeMapping> parentMappings = umlBodyMapper.getParentMapper().getMappings();
			Set<AbstractCodeMapping> childMappings = umlBodyMapper.getMappings();

			boolean hasRecursiveLeave = false;

			List<String> addedOperationNames = getOperationNames(OperationType.ADDED);
			List<String> allOperationNames = getOperationNames(OperationType.ALL);

			List<AbstractStatement> allT1Nodes = getAllT1NodesInModelDiff();
			// Processing Parent (Source Operation After Extraction) T2
			// Inner(Composite)/Leaf Nodes to filter out marked nodes
			for (CompositeStatementObject notMappedCompositeNode : parentListNotMappedInnerNodesT2) {

				if (isCompositeNodeExpressionContainingInvokationsToExtractedMethods(notMappedCompositeNode, refList)) {
					listParentT2CompositesWithInvokationsToExtractedMethodInExpression.add(notMappedCompositeNode);
				}
				if (isNeutralNodeForFacilitateExtension(notMappedCompositeNode, refList, sourceOperationAfterExtrction,
						addedOperationNames, allOperationNames)) {
					listParentNeutralInnerNodes.add(notMappedCompositeNode);
				}
				if (isParentT2InnerNodeinT1InnerNodes(notMappedCompositeNode, parentListNotMappedInnerNodesT1,
						allT1Nodes)) {
					listParentT2InnerNodeInT1InnerNodes.add(notMappedCompositeNode);
				}
			}
			setParentMarkedT2InnerNodes.addAll(listParentT2CompositesWithInvokationsToExtractedMethodInExpression);
			setParentMarkedT2InnerNodes.addAll(listParentNeutralInnerNodes);
			setParentMarkedT2InnerNodes.addAll(listParentT2InnerNodeInT1InnerNodes);

			for (StatementObject notMappedNode : parentListNotMappedLeavesT2) {
				if (isLeafNodeContainingInvokationsToExtractedMethods(notMappedNode, refList)) {
					if (!isLeafNodeHavingExtraCalls(notMappedNode,
							getExtractedMethodInvocationsInStatement(notMappedNode, refList))) {
						listParentNotMappedLeavesWithInvokationsToExtractedMethod.add(notMappedNode);
					}
				}
				if (isNeutralNodeForFacilitateExtension(notMappedNode, refList, sourceOperationAfterExtrction,
						addedOperationNames, allOperationNames)) {
					listParentNeutralLeaves.add(notMappedNode);
				}
				if (isParentT2LeafNodeinT1leafNodes(notMappedNode, parentListNotMappedleafNodesT1, allT1Nodes)) {
					listParentT2LeafNodeInT1LeafNodes.add(notMappedNode);
				}
				if (isParentLeafNodeExtraInvocationsExpressionsInExtractedMethodParameters(notMappedNode,
						extractedOperation, sourceOperationAfterExtrction, refList)) {
					listParentLeafWithInvocationsExpressionsInExtractOperationInvocationParameters.add(notMappedNode);
					setMotivationFlag(MotivationFlag.SOAE_T2_IE_IN_EM_PARAMETERS, extractOperationRefactoring);

				}
				if (isParentLeafNodeDeclaredVariableInExtractedMethodParameters(notMappedNode, extractedOperation,
						sourceOperationAfterExtrction, refList)) {
					listParentLeafWithDeclaredVariableInExtractOperationInvocationParameters.add(notMappedNode);
					setMotivationFlag(MotivationFlag.SOAE_T2_DV_IN_EM_PARAMETERS, extractOperationRefactoring);
				}
				if (isParentT2LeafNodeInParentMappings(notMappedNode, parentMappings)) {
					listParentT2LeafNodesInMappings.add(notMappedNode);
					setMotivationFlag(MotivationFlag.SOAE_T2_IN_MAPPING, extractOperationRefactoring);
				}
			}
			setParentMarkedT2Leaves.addAll(listParentNotMappedLeavesWithInvokationsToExtractedMethod);
			setParentMarkedT2Leaves.addAll(listParentNeutralLeaves);
			setParentMarkedT2Leaves.addAll(listParentT2LeafNodeInT1LeafNodes);
			setParentMarkedT2Leaves
					.addAll(listParentLeafWithInvocationsExpressionsInExtractOperationInvocationParameters);
			setParentMarkedT2Leaves.addAll(listParentT2LeafNodesInMappings);
			setParentMarkedT2Leaves.addAll(listParentLeafWithDeclaredVariableInExtractOperationInvocationParameters);

			int soae_T2_in_T1 = listParentT2LeafNodeInT1LeafNodes.size() + listParentT2InnerNodeInT1InnerNodes.size();
			if (soae_T2_in_T1 > 0) {
				setMotivationFlag(MotivationFlag.SOAE_T2_IN_T1, extractOperationRefactoring);
			}
			int soae_T2_neutral = listParentNeutralLeaves.size() + listParentNeutralInnerNodes.size();
			if (soae_T2_neutral > 0) {
				setMotivationFlag(MotivationFlag.SOAE_T2_NEUTRAL, extractOperationRefactoring);
			}
			int soae_T2_em_invocations = listParentNotMappedLeavesWithInvokationsToExtractedMethod.size()
					+ listParentT2CompositesWithInvokationsToExtractedMethodInExpression.size();
			if (soae_T2_em_invocations > 0) {
				setMotivationFlag(MotivationFlag.SOAE_T2_EM_INVOCATIONS, extractOperationRefactoring);
			}

			// Processing Child (Extracted Operation) T2 Inner(Composite)/Leaf Nodes to
			// filter out marked nodes
			for (CompositeStatementObject notMappedCompositeNode : listNotMappedInnerNodesT2) {

				if (isCompositeNodeExpressionContainingInvokationsToExtractedMethods(notMappedCompositeNode, refList)) {
					listChildT2CompositesWithInvokationsToExtractedMethodInExpression.add(notMappedCompositeNode);
				}
				if (isNeutralNodeForFacilitateExtension(notMappedCompositeNode, refList, extractedOperation,
						addedOperationNames, allOperationNames)) {
					listChildNeutralInnerNodes.add(notMappedCompositeNode);
				}
				if (isChildT2InnerNodeinT1InnerNodes(notMappedCompositeNode, listNotMappedInnerNodesT1, allT1Nodes)) {
					listChildT2InnerNodeInT1InnerNodes.add(notMappedCompositeNode);
				}
			}
			setChildMarkedT2InnerNodes.addAll(listChildT2CompositesWithInvokationsToExtractedMethodInExpression);
			setChildMarkedT2InnerNodes.addAll(listChildNeutralInnerNodes);
			setChildMarkedT2InnerNodes.addAll(listChildT2InnerNodeInT1InnerNodes);

			for (StatementObject notMappedNode : listNotMappedLeavesT2) {
				if (isLeafNodeContainingInvokationsToExtractedMethods(notMappedNode, refList)) {
					if (!isLeafNodeHavingExtraCalls(notMappedNode,
							getExtractedMethodInvocationsInStatement(notMappedNode, refList))) {
						listChildNotMappedLeavesWithInvokationsToExtractedMethod.add(notMappedNode);
					}
				}
				if (isLeafNodeExtraInvocationsRecursive(notMappedNode, extractedOperation)) {
					listChildNotMappedLeavesWithRecursive.add(notMappedNode);
					hasRecursiveLeave = true;
				}
				if (isLeafNodeExtraInvocationsExpressionsInOperationParameters(notMappedNode, extractedOperation)) {
					listChildNotMappedLeavesWithInvocationsExpressionsInOperationParameters.add(notMappedNode);
					setMotivationFlag(MotivationFlag.EM_T2_IE_IN_EM_PARAMETERS, extractOperationRefactoring);

				}
				if (isNeutralNodeForFacilitateExtension(notMappedNode, refList, extractedOperation, addedOperationNames,
						allOperationNames)) {
					listChildNeutralLeaves.add(notMappedNode);
				}
				if (isChildT2LeafNodeinT1leafNodes(notMappedNode, listNotMappedleafNodesT1, allT1Nodes)) {
					listChildT2LeafNodeInT1LeafNodes.add(notMappedNode);
				}
				if (isChildT2LeafNodeInChildMappings(notMappedNode, childMappings)) {
					listChildT2LeafNodesInChildMappings.add(notMappedNode);
					setMotivationFlag(MotivationFlag.EM_T2_IN_MAPPING, extractOperationRefactoring);
				}
				if (isChildT2LeafNodeInParentMappings(notMappedNode, parentMappings)) {
					listChildT2LeafNodesInParentMappings.add(notMappedNode);
					setMotivationFlag(MotivationFlag.EM_T2_IN_MAPPING, extractOperationRefactoring);
				}
			}
			int em_T2_in_T1 = listChildT2LeafNodeInT1LeafNodes.size() + listChildT2InnerNodeInT1InnerNodes.size();
			if (em_T2_in_T1 > 0) {
				setMotivationFlag(MotivationFlag.EM_T2_IN_T1, extractOperationRefactoring);
			}
			int em_T2_neutral = listChildNeutralLeaves.size() + listChildNeutralInnerNodes.size();
			if (em_T2_neutral > 0) {
				setMotivationFlag(MotivationFlag.EM_T2_NEUTRAL, extractOperationRefactoring);
			}
			int em_T2_em_invocations = listChildNotMappedLeavesWithInvokationsToExtractedMethod.size()
					+ listChildT2CompositesWithInvokationsToExtractedMethodInExpression.size();
			if (em_T2_em_invocations > 0) {
				setMotivationFlag(MotivationFlag.EM_T2_EM_INVOCATIONS, extractOperationRefactoring);
			}

			setChildMarkedT2Leaves.addAll(listChildNotMappedLeavesWithInvokationsToExtractedMethod);
			setChildMarkedT2Leaves.addAll(listChildNotMappedLeavesWithRecursive);
			// setChildMarkedT2Leaves.addAll(listChildNotMappedLeavesWithInvocationsExpressionsInOperationParameters);
			setChildMarkedT2Leaves.addAll(listChildNeutralLeaves);
			setChildMarkedT2Leaves.addAll(listChildT2LeafNodeInT1LeafNodes);
			setChildMarkedT2Leaves.addAll(listChildT2LeafNodesInChildMappings);
			setChildMarkedT2Leaves.addAll(listChildT2LeafNodesInParentMappings);

			// Filtering parent nodes that are not in extracted method scope
			List<AbstractStatement> parentStatementsInExtractedScope = getParentExtraNodesInExtractedScope(
					extractOperationRefactoring, parentListNotMappedInnerNodesT2, setParentMarkedT2InnerNodes,
					parentListNotMappedLeavesT2, setParentMarkedT2Leaves);

			Set<CompositeStatementObject> setParentMarkedT2InnerNodesBeforeScopeFilter = new HashSet<CompositeStatementObject>();
			setParentMarkedT2InnerNodesBeforeScopeFilter.addAll(setParentMarkedT2InnerNodes);

			for (AbstractStatement statement : parentListNotMappedInnerNodesT2) {
				if (!parentStatementsInExtractedScope.contains(statement)) {
					setParentMarkedT2InnerNodes.add((CompositeStatementObject) statement);
				}
			}
			List<CompositeStatementObject> compositeWithParentInExtractedScope = new ArrayList<CompositeStatementObject>();
			for (CompositeStatementObject composite : setParentMarkedT2InnerNodes) {
				CompositeStatementObject nonBlockParent = getNonBlockParentOfAbstractStatement(composite);
				if (parentStatementsInExtractedScope.contains(nonBlockParent)) {
					compositeWithParentInExtractedScope.add(composite);
				}
			}
			// Remove composites with parents in Extract method scope if they were not
			// marked before.
			for (CompositeStatementObject composite : compositeWithParentInExtractedScope) {
				if (!setParentMarkedT2InnerNodesBeforeScopeFilter.contains(composite)) {
					setParentMarkedT2InnerNodes.remove(composite);
				}
			}
			for (AbstractStatement statement : parentListNotMappedLeavesT2) {
				if (!parentStatementsInExtractedScope.contains(statement)) {
					setParentMarkedT2Leaves.add((StatementObject) statement);
				}
			}

			// Computing filtered nodes (Nodes that facilitate extension)

			List<AbstractStatement> filteredChildNonMappedLeavesAndInnerNodesT2 = new ArrayList<AbstractStatement>();
			List<AbstractStatement> filteredParentNonMappedLeavesAndInnerNodesT2 = new ArrayList<AbstractStatement>();

			int filterdListNotMappedInnerNodesT2 = listNotMappedInnerNodesT2.size() - setChildMarkedT2InnerNodes.size();
			int filteredListNotMappedLeavesT2 = listNotMappedLeavesT2.size() - setChildMarkedT2Leaves.size();
			countChildNonMappedLeavesAndInnerNodesT2 = filterdListNotMappedInnerNodesT2 + filteredListNotMappedLeavesT2;

			filteredChildNonMappedLeavesAndInnerNodesT2.addAll(listNotMappedInnerNodesT2);
			filteredChildNonMappedLeavesAndInnerNodesT2.addAll(listNotMappedLeavesT2);
			filteredChildNonMappedLeavesAndInnerNodesT2.removeAll(setChildMarkedT2InnerNodes);
			filteredChildNonMappedLeavesAndInnerNodesT2.removeAll(setChildMarkedT2Leaves);

			int filterdParentListNotMappedInnerNodesT2 = parentListNotMappedInnerNodesT2.size()
					- setParentMarkedT2InnerNodes.size();
			int filteredParentListNotMappedLeavesT2 = parentListNotMappedLeavesT2.size()
					- setParentMarkedT2Leaves.size();
			countParentNonMappedLeavesAndInnerNodesT2 = filterdParentListNotMappedInnerNodesT2
					+ filteredParentListNotMappedLeavesT2;
			filteredParentNonMappedLeavesAndInnerNodesT2.addAll(parentListNotMappedInnerNodesT2);
			filteredParentNonMappedLeavesAndInnerNodesT2.addAll(parentListNotMappedLeavesT2);
			filteredParentNonMappedLeavesAndInnerNodesT2.removeAll(setParentMarkedT2InnerNodes);
			filteredParentNonMappedLeavesAndInnerNodesT2.removeAll(setParentMarkedT2Leaves);

			// CODE ANALYSYS
			codeAnalysisFaciliateExtension(ref, countChildNonMappedLeavesAndInnerNodesT2,
					countParentNonMappedLeavesAndInnerNodesT2, listNotMappedleafNodesT1, listNotMappedInnerNodesT1,
					parentListNotMappedleafNodesT1, parentListNotMappedInnerNodesT1);
			// DETECTION RULE: Detect if Some statements(InnerNode or Leave) added either
			// ExtractedOperation or
			// Source Operation After Extraction

			if (countParentNonMappedLeavesAndInnerNodesT2 > 0) {
				// Checking that the extension in the parent is in the "extraction scope" in
				// source operation after extraction
				if (parentStatementsInExtractedScope.size() > 0) {
					facilitateExtensionRefactoringsWithExtrensionInParent.add(extractOperationRefactoring);
				} else {
					if (countChildNonMappedLeavesAndInnerNodesT2 == 0) {
						return false;
					}
				}
			}
			if (hasRecursiveLeave) {
				int countFilteredComposites = 0;
				for (AbstractStatement statement : filteredChildNonMappedLeavesAndInnerNodesT2) {
					if (statement instanceof CompositeStatementObject) {
						countFilteredComposites++;
					}
				}
				// Exclude all composite leaves in child(extracted operation) when there is a
				// recursive leave
				countChildNonMappedLeavesAndInnerNodesT2 -= countFilteredComposites;
			}
			int soae_NotMapped_T2_filtered = countParentNonMappedLeavesAndInnerNodesT2;
			int em_NotMapped_T2_filtered = countChildNonMappedLeavesAndInnerNodesT2;
			setMotivationFlag(MotivationFlag.EM_NOTMAPPED_T2_FILTERED.setMotivationValue(em_NotMapped_T2_filtered),
					extractOperationRefactoring);
			setMotivationFlag(MotivationFlag.SOAE_NOTMAPPED_T2_FILTERED.setMotivationValue(soae_NotMapped_T2_filtered),
					extractOperationRefactoring);
			if (countChildNonMappedLeavesAndInnerNodesT2 > 0 || countParentNonMappedLeavesAndInnerNodesT2 > 0) {
				// if(!isMotivationDetected(ref,
				// MotivationType.EM_INTRODUCE_ALTERNATIVE_SIGNATURE) &&
				// !isMotivationDetected(ref,
				// MotivationType.EM_REPLACE_METHOD_PRESERVING_BACKWARD_COMPATIBILITY)) {
				return true;
				// }
			}
		}
		return false;
	}

	private List<AbstractStatement> getAllT1NodesInModelDiff() {
		List<AbstractStatement> allT1Nodes = new ArrayList<AbstractStatement>();
		for (UMLClassDiff classDiff : modelDiff.getCommonClassDiffList()) {
			if (classDiff != null) {
				for (UMLOperationBodyMapper bodyMapper : classDiff.getOperationBodyMapperList()) {
					if (bodyMapper != null) {
						allT1Nodes.addAll(bodyMapper.getNonMappedLeavesT1());
						allT1Nodes.addAll(bodyMapper.getNonMappedInnerNodesT1());
					}
				}
			}
		}
		return allT1Nodes;
	}

	private boolean isParentT2LeafNodeInParentMappings(StatementObject notMappedNode,
			Set<AbstractCodeMapping> parentMappings) {

		for (AbstractCodeMapping parentMapping : parentMappings) {
			if (notMappedNode.getString().equals(parentMapping.getFragment2().getString())) {
				return true;
			}
		}
		return false;
	}

	private boolean isChildT2LeafNodeInChildMappings(StatementObject notMappedNode,
			Set<AbstractCodeMapping> childMappings) {
		for (AbstractCodeMapping childMapping : childMappings) {
			if (notMappedNode.getString().equals(childMapping.getFragment2().getString())) {
				return true;
			}
		}
		return false;
	}

	private boolean isChildT2LeafNodeInParentMappings(StatementObject notMappedNode,
			Set<AbstractCodeMapping> parentMappings) {
		for (AbstractCodeMapping parentMapping : parentMappings) {
			if (notMappedNode.getString().equals(parentMapping.getFragment2().getString())) {
				return true;
			}
			List<String> fragment2Variables = parentMapping.getFragment2().getVariables();
			List<String> childLeafVariables = notMappedNode.getVariables();
			if (fragment2Variables.size() > 0 && childLeafVariables.size() > 0) {
				if (fragment2Variables.containsAll(childLeafVariables)) {
					if (notMappedNode.getVariableDeclarations().size() > 0
							&& parentMapping.getFragment2().getVariableDeclarations().size() > 0) {
						if (notMappedNode.getVariableDeclarations().get(0).getVariableName().equals(
								parentMapping.getFragment2().getVariableDeclarations().get(0).getVariableName())) {
							return true;
						}
					} else {
						if (notMappedNode.getMethodInvocationMap().size() == parentMapping.getFragment2()
								.getMethodInvocationMap().size()) {
							return true;
						}
					}
				}
				List<String> commonVariables = new ArrayList<String>();
				List<String> differentVariables = new ArrayList<String>();
				for (String childLeafVariable : childLeafVariables) {
					if (fragment2Variables.contains(childLeafVariable)) {
						commonVariables.add(childLeafVariable);
					} else {
						differentVariables.add(childLeafVariable);
					}
				}
				if (differentVariables.size() * 2 < commonVariables.size()) {
					if (notMappedNode.getVariableDeclarations().size() > 0
							&& parentMapping.getFragment2().getVariableDeclarations().size() > 0) {
						if (notMappedNode.getVariableDeclarations().get(0).getVariableName().equals(
								parentMapping.getFragment2().getVariableDeclarations().get(0).getVariableName())) {
							return true;
						}
					} else {
						if (notMappedNode.getMethodInvocationMap().size() == parentMapping.getFragment2()
								.getMethodInvocationMap().size()) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	private List<AbstractStatement> getParentExtraNodesInExtractedScope(
			ExtractOperationRefactoring extractOpRefactoring,
			List<CompositeStatementObject> parentListNotMappedInnerNodesT2,
			Set<CompositeStatementObject> setParentMarkedT2InnerNodes,
			List<StatementObject> parentListNotMappedLeavesT2, Set<StatementObject> setParentMarkedT2Leaves) {
		List<AbstractStatement> statementsInSameScopeAsExtractedMethod = new ArrayList<AbstractStatement>();
		List<StatementObject> newParentListNotMappedLeavesT2 = new ArrayList<StatementObject>();
		newParentListNotMappedLeavesT2.addAll(parentListNotMappedLeavesT2);
		List<CompositeStatementObject> newParentListNotMappedInnerNodesT2 = new ArrayList<CompositeStatementObject>();
		newParentListNotMappedInnerNodesT2.addAll(parentListNotMappedInnerNodesT2);
		// newParentListNotMappedLeavesT2.removeAll(setParentMarkedT2Leaves);
		// newParentListNotMappedInnerNodesT2.removeAll(setParentMarkedT2InnerNodes);
		Set<AbstractCodeMapping> abstractMappings = extractOpRefactoring.getBodyMapper().getMappings();
		Set<CompositeStatementObject> codeFragment1sParents = new HashSet<CompositeStatementObject>();
		List<CompositeStatementObject> compositesInSameScopeAsExtractedCode = new ArrayList<CompositeStatementObject>();
		List<StatementObject> leavesInSameScopeAsExtractedCode = new ArrayList<StatementObject>();
		for (AbstractCodeMapping abstractCodeMapping : abstractMappings) {
			codeFragment1sParents.add(getNonBlockParentOfAbstractCodeFragment(abstractCodeMapping.getFragment1()));
		}
		for (CompositeStatementObject compositeStatement : newParentListNotMappedInnerNodesT2) {
			CompositeStatementObject compositeStatementParent = getAbstractStatementNonBlockParent(compositeStatement);
			for (CompositeStatementObject parent : codeFragment1sParents) {
				if ((parent == null && compositeStatementParent == null)) {
					// parents are either source operation before/after extraction
					compositesInSameScopeAsExtractedCode.add(compositeStatement);
					// return true;
				} else if (parent != null && compositeStatementParent != null) {
					if (parent.toString().equals(compositeStatementParent.toString())) {
						compositesInSameScopeAsExtractedCode.add(compositeStatement);
					}
				}
			}
		}
		for (StatementObject statementObject : newParentListNotMappedLeavesT2) {
			CompositeStatementObject statementObjectParent = getAbstractStatementNonBlockParent(statementObject);
			for (CompositeStatementObject parent : codeFragment1sParents) {
				if ((parent == null && statementObjectParent == null)) {
					leavesInSameScopeAsExtractedCode.add(statementObject);
				} else if (parent != null && statementObjectParent != null) {
					if (parent.toString().equals(statementObjectParent.toString())) {
						leavesInSameScopeAsExtractedCode.add(statementObject);
					}
				}
			}
		}
		statementsInSameScopeAsExtractedMethod.addAll(compositesInSameScopeAsExtractedCode);
		statementsInSameScopeAsExtractedMethod.addAll(leavesInSameScopeAsExtractedCode);

		return statementsInSameScopeAsExtractedMethod;
	}

	private CompositeStatementObject getAbstractStatementNonBlockParent(AbstractStatement abstractStatement) {
		CompositeStatementObject parent = abstractStatement.getParent();
		while (parent.getLocationInfo().getCodeElementType().equals(CodeElementType.BLOCK)) {
			if (parent.getParent() != null) {
				parent = parent.getParent();
			} else {
				return null;
			}
		}
		return parent;
	}

	private CompositeStatementObject getNonBlockParentOfAbstractCodeFragment(AbstractCodeFragment fragment) {
		CompositeStatementObject parent = fragment.getParent();
		if (parent == null) {
			return null;
		}
		while (parent.getLocationInfo().getCodeElementType().equals(CodeElementType.BLOCK)) {
			if (parent.getParent() != null) {
				parent = parent.getParent();
			} else {
				return null;
			}
		}

		return parent;
	}

	private CompositeStatementObject getNonBlockParentOfAbstractStatement(AbstractStatement statement) {
		CompositeStatementObject parent = statement.getParent();
		if (parent == null) {
			return null;
		}
		while (parent.getLocationInfo().getCodeElementType().equals(CodeElementType.BLOCK)) {
			if (parent.getParent() != null) {
				parent = parent.getParent();
			} else {
				return null;
			}
		}

		return parent;
	}

	private boolean isExtractedMethodMappingAddedTernaryOperator(ExtractOperationRefactoring extrctOpRefactoring) {
		Set<AbstractCodeMapping> codeMappings = extrctOpRefactoring.getBodyMapper().getMappings();
		for (AbstractCodeMapping abstractCodeMapping : codeMappings) {
			List<TernaryOperatorExpression> fragment1TernaryOperatorExpressions = abstractCodeMapping.getFragment1()
					.getTernaryOperatorExpressions();
			List<TernaryOperatorExpression> fragment2TernaryOperatorExpressions = abstractCodeMapping.getFragment2()
					.getTernaryOperatorExpressions();
			if (fragment2TernaryOperatorExpressions.size() > 0 && fragment1TernaryOperatorExpressions.size() == 0) {
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
		// CODE ANALYSIS
		int[] addedRemovedCount = new int[2];
		addedRemovedCount[0] = listNotMappedleafNodesT1.size() + listNotMappedInnerNodesT1.size()
				+ parentListNotMappedleafNodesT1.size() + parentListNotMappedInnerNodesT1.size();
		addedRemovedCount[1] = countChildNonMappedLeavesAndInnerNodesT2 + countParentNonMappedLeavesAndInnerNodesT2;
		mapFacilitateExtensionT1T2.put(ref, addedRemovedCount);
	}

	private boolean isInvocationExpressionsInOperationVariableNames(AbstractStatement statement,
			UMLOperation extarctedOperation) {
		List<String> invocationExpressionInVariableNames = new ArrayList<String>();
		List<VariableDeclaration> allVariableDeclarations = extarctedOperation.getAllVariableDeclarations();
		List<String> allVariableNames = new ArrayList<String>();
		for (VariableDeclaration decleration : allVariableDeclarations) {
			allVariableNames.add(decleration.getVariableName());
		}
		for (String invocationString : statement.getMethodInvocationMap().keySet()) {
			List<OperationInvocation> operationInvocations = statement.getMethodInvocationMap().get(invocationString);
			for (OperationInvocation invocation : operationInvocations) {
				if (allVariableNames.contains(invocation.getExpression())) {
					invocationExpressionInVariableNames.add(invocation.getExpression());
				}
			}
		}
		if (invocationExpressionInVariableNames.size() > 0) {
			return true;
		}
		return false;
	}

	private boolean isLeafNodeExtraInvocationsRecursive(StatementObject notMappedNode,
			UMLOperation extractedOperation) {
		// checking extra invocations for extension
		List<OperationInvocation> recursiveInvocations = new ArrayList<OperationInvocation>();
		if (notMappedNode.getMethodInvocationMap().size() == 0) {
			return false;
		}
		for (String invocationString : notMappedNode.getMethodInvocationMap().keySet()) {
			List<OperationInvocation> operationInvocations = notMappedNode.getMethodInvocationMap()
					.get(invocationString);
			for (OperationInvocation invocation : operationInvocations) {
				if (invocation.matchesOperation(extractedOperation, extractedOperation.variableDeclarationMap(),
						modelDiff)) {
					recursiveInvocations.add(invocation);
				}
			}
			if (recursiveInvocations.size() > 0) {
				return true;
			}
		}
		return false;
	}

	private boolean isParentLeafNodeDeclaredVariableInExtractedMethodParameters(StatementObject notMappedNode,
			UMLOperation extractedOperation, UMLOperation sourceOperationAfterExtraction, List<Refactoring> refList) {
		List<String> extractMethodInvocationArguments = new ArrayList<String>();
		if (!notMappedNode.getLocationInfo().getCodeElementType()
				.equals(CodeElementType.VARIABLE_DECLARATION_STATEMENT)) {
			return false;
		}
		for (OperationInvocation invocation : sourceOperationAfterExtraction.getAllOperationInvocations()) {
			for (Refactoring ref : refList) {
				ExtractOperationRefactoring extractOpRef = (ExtractOperationRefactoring) ref;
				if (invocation.matchesOperation(extractOpRef.getExtractedOperation(),
						sourceOperationAfterExtraction.variableDeclarationMap(), modelDiff)) {
					extractMethodInvocationArguments.addAll(invocation.getArguments());
					break;
				}
			}
		}
		for (VariableDeclaration declaration : notMappedNode.getVariableDeclarations()) {
			if (extractMethodInvocationArguments.contains(declaration.getVariableName())) {
				return true;
			}
		}
		return false;
	}

	private boolean isParentLeafNodeExtraInvocationsExpressionsInExtractedMethodParameters(
			StatementObject notMappedNode, UMLOperation extractedOperation, UMLOperation sourceOperationAfterExtraction,
			List<Refactoring> refList) {
		List<OperationInvocation> invocationExpressionInExtractedMethodInvocationParameters = new ArrayList<OperationInvocation>();
		List<String> extractMethodInvocationArguments = new ArrayList<String>();
		if (notMappedNode.getMethodInvocationMap().size() == 0) {
			return false;
		}
		for (OperationInvocation invocation : sourceOperationAfterExtraction.getAllOperationInvocations()) {
			for (Refactoring ref : refList) {
				ExtractOperationRefactoring extractOpRef = (ExtractOperationRefactoring) ref;
				if (invocation.matchesOperation(extractOpRef.getExtractedOperation(),
						sourceOperationAfterExtraction.variableDeclarationMap(), modelDiff)) {
					extractMethodInvocationArguments.addAll(invocation.getArguments());
					break;
				}
			}
		}

		for (String invocationString : notMappedNode.getMethodInvocationMap().keySet()) {
			List<OperationInvocation> operationInvocations = notMappedNode.getMethodInvocationMap()
					.get(invocationString);
			for (OperationInvocation invocation : operationInvocations) {
				if (extractMethodInvocationArguments.contains(invocation.getExpression())) {
					invocationExpressionInExtractedMethodInvocationParameters.add(invocation);
				}
			}
			if (invocationExpressionInExtractedMethodInvocationParameters.size() > 0) {
				return true;
			}
		}
		return false;
	}

	private boolean isLeafNodeExtraInvocationsExpressionsInOperationParameters(StatementObject notMappedNode,
			UMLOperation extractedOperation) {
		// checking extra invocations for extension
		List<OperationInvocation> invocationOfParametersList = new ArrayList<OperationInvocation>();
		if (notMappedNode.getMethodInvocationMap().size() == 0) {
			return false;
		}
		for (String invocationString : notMappedNode.getMethodInvocationMap().keySet()) {
			List<OperationInvocation> operationInvocations = notMappedNode.getMethodInvocationMap()
					.get(invocationString);
			for (OperationInvocation invocation : operationInvocations) {
				if (extractedOperation.getParameterNameList().contains(invocation.getExpression())) {
					invocationOfParametersList.add(invocation);
				}
			}
			if (invocationOfParametersList.size() == operationInvocations.size()) {
				return true;
			}
		}
		return false;
	}

	private boolean isLeafNodeHavingExtraCalls(StatementObject notMappedNode,
			List<OperationInvocation> extractedMethodInvocations) {

		boolean extraCallsExist = notMappedNode.getMethodInvocationMap().size() > 1;
		List<OperationInvocation> extractedMethodSubsumedInvocations = new ArrayList<OperationInvocation>();
		for (String invocationString : notMappedNode.getMethodInvocationMap().keySet()) {
			for (OperationInvocation invocation : notMappedNode.getMethodInvocationMap().get(invocationString)) {
				if (!extractedMethodInvocations.contains(invocation)) {
					for (OperationInvocation extractedInvocation : extractedMethodInvocations) {
						if (extractedInvocation.getLocationInfo().subsumes(invocation.getLocationInfo())) {
							extractedMethodSubsumedInvocations.add(invocation);
						}
					}
				}
			}
		}
		return extraCallsExist && (extractedMethodSubsumedInvocations.size() == 0);
	}

	private boolean isLeafNodeContainingInvokationsToExtractedMethods(StatementObject notMappedNode,
			List<Refactoring> refList) {
		for (Refactoring refactoring : refList) {
			if (refactoring instanceof ExtractOperationRefactoring) {
				ExtractOperationRefactoring extractOperationRefactoring = (ExtractOperationRefactoring) refactoring;
				for (OperationInvocation invocation : extractOperationRefactoring.getExtractedOperationInvocations()) {
					if (notMappedNode.getLocationInfo().subsumes(invocation.getLocationInfo())) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private List<OperationInvocation> getExtractedMethodInvocationsInStatement(StatementObject notMappedNode,
			List<Refactoring> refList) {
		List<OperationInvocation> extractedMehtodInvocations = new ArrayList<OperationInvocation>();
		for (Refactoring refactoring : refList) {
			if (refactoring instanceof ExtractOperationRefactoring) {
				ExtractOperationRefactoring extractOperationRefactoring = (ExtractOperationRefactoring) refactoring;
				for (OperationInvocation invocation : extractOperationRefactoring.getExtractedOperationInvocations()) {
					if (notMappedNode.getLocationInfo().subsumes(invocation.getLocationInfo())) {
						extractedMehtodInvocations.add(invocation);
					}
				}
			}
		}
		return extractedMehtodInvocations;
	}

	private boolean isCompositeNodeExpressionContainingInvokationsToExtractedMethods(
			CompositeStatementObject notMappedCompositeNode, List<Refactoring> refList) {
		for (AbstractExpression expression : notMappedCompositeNode.getExpressions()) {
			/*
			 * The loop around different Extract refactoring helps omit all the call to
			 * different extracted operations from the source operation after extraction
			 */
			for (Refactoring refactoring : refList) {
				if (refactoring instanceof ExtractOperationRefactoring) {
					ExtractOperationRefactoring extractOperationRefactoring = (ExtractOperationRefactoring) refactoring;
					for (OperationInvocation invocation : extractOperationRefactoring
							.getExtractedOperationInvocations()) {
						if (expression.getLocationInfo().subsumes(invocation.getLocationInfo())) {
							if (expression.getMethodInvocationMap().size() == 1) {
								return true;
							}
						}
					}
				}
			}
		}
		return false;
	}

	private boolean isParentT2InnerNodeinT1InnerNodes(AbstractStatement parentT2InnerNode,
			List<CompositeStatementObject> parentT1InnerNodes, List<AbstractStatement> allT1Nodes) {
		List<String> statementTokens = getAbstractStatementTokens(parentT2InnerNode);
		for (CompositeStatementObject notMappedCompositeNode : parentT1InnerNodes) {
			if (notMappedCompositeNode.getString().equals(parentT2InnerNode.toString())) {
				return true;
			}
			if (notMappedCompositeNode.getString().indexOf(parentT2InnerNode.toString()) >= 0) {
				return true;
			}
			if (notMappedCompositeNode.getString().startsWith(parentT2InnerNode.toString())) {
				return true;
			}
			for (String token : statementTokens) {
				if (notMappedCompositeNode.getString().indexOf(token) >= 0) {
					return true;
				}
			}
		}
		for (AbstractStatement notMappedCompositeNode : allT1Nodes) {
			if (notMappedCompositeNode.getString().equals(parentT2InnerNode.toString())) {
				return true;
			}
		}
		return false;
	}

	private boolean isParentT2LeafNodeinT1leafNodes(AbstractStatement parentT2Leave,
			List<StatementObject> parentLeavesT1, List<AbstractStatement> allT1Nodes) {
		List<String> statementTokens = getAbstractStatementTokens(parentT2Leave);
		for (StatementObject notMappedNode : parentLeavesT1) {
			if (notMappedNode.getString().equals(parentT2Leave.toString())) {
				return true;
			}
			if (notMappedNode.getString().indexOf(parentT2Leave.toString()) >= 0) {
				return true;
			}
			if (notMappedNode.getString().startsWith(parentT2Leave.toString())) {
				return true;
			}
			for (String token : statementTokens) {
				if (notMappedNode.getString().indexOf(token) >= 0) {
					return true;
				}
			}
			List<String> argumentsInT1 = new ArrayList<String>();
			for (String token : getStatementInvocationArguments(parentT2Leave)) {
				if (notMappedNode.getString().indexOf(token) >= 0) {
					argumentsInT1.add(token);
				}
			}
			if (argumentsInT1.size() > 0) {
				return true;
			}
		}
		for (AbstractStatement notMappedNode : allT1Nodes) {
			if (notMappedNode.getString().equals(parentT2Leave.toString())) {
				return true;
			}
		}

		return false;
	}

	private boolean isChildT2InnerNodeinT1InnerNodes(AbstractStatement childT2InnerNode,
			List<CompositeStatementObject> childT1InnerNodes, List<AbstractStatement> allT1Nodes) {
		List<String> statementTokens = getAbstractStatementTokens(childT2InnerNode);
		for (CompositeStatementObject notMappedCompositeNode : childT1InnerNodes) {
			if (notMappedCompositeNode.getString().equals(childT2InnerNode.toString())) {
				return true;
			}
			if (notMappedCompositeNode.getString().indexOf(childT2InnerNode.toString()) >= 0) {
				return true;
			}
			if (notMappedCompositeNode.getString().startsWith(childT2InnerNode.toString())) {
				return true;
			}
			for (String token : statementTokens) {
				if (notMappedCompositeNode.getString().indexOf(token) >= 0) {
					return true;
				}
			}
		}
		for (AbstractStatement notMappedCompositeNode : allT1Nodes) {
			if (notMappedCompositeNode.getString().equals(childT2InnerNode.toString())) {
				return true;
			}
		}

		return false;
	}

	private boolean isChildT2LeafNodeinT1leafNodes(AbstractStatement childT2Leaf, List<StatementObject> childLeavesT1,
			List<AbstractStatement> allT1Nodes) {

		List<String> statementTokens = getAbstractStatementTokens(childT2Leaf);
		for (StatementObject notMappedNode : childLeavesT1) {
			if (notMappedNode.getString().equals(childT2Leaf.toString())) {
				return true;
			}
			if (notMappedNode.getString().indexOf(childT2Leaf.toString()) >= 0) {
				return true;
			}
			if (notMappedNode.getString().startsWith(childT2Leaf.toString())) {
				return true;
			}
			List<String> tokensInT1 = new ArrayList<String>();
			for (String token : statementTokens) {
				if (notMappedNode.getString().indexOf(token) >= 0) {
					tokensInT1.add(token);
				}
			}
			if (tokensInT1.size() == statementTokens.size()) {
				return true;
			}
			List<String> argumentsInT1 = new ArrayList<String>();
			for (String token : getStatementInvocationArguments(childT2Leaf)) {
				if (notMappedNode.getString().indexOf(token) >= 0) {
					argumentsInT1.add(token);
				}
			}
			if (argumentsInT1.size() > 0) {
				return true;
			}
		}
		for (AbstractStatement notMappedNode : allT1Nodes) {
			if (notMappedNode.getString().equals(childT2Leaf.toString())) {
				return true;
			}
		}
		return false;
	}

	private List<String> getAbstractStatementTokens(AbstractStatement statement) {
		List<String> statementTokens = new ArrayList<String>();
		// statementTokens.addAll(getStatementInvocationNames(statement));
		statementTokens.addAll(getStatementInvocationExpressions(statement));
		// statementTokens.addAll(getStatementInvocationArguments(statement));
		return statementTokens;
	}

	private boolean isNeutralNodeForFacilitateExtension(AbstractStatement statement, List<Refactoring> refList,
			UMLOperation statementOperation, List<String> addedOperationNames, List<String> allOperationNames) {
		Set<CodeElementType> neutralCodeElements = new HashSet<CodeElementType>();
		neutralCodeElements.add(CodeElementType.RETURN_STATEMENT);
		neutralCodeElements.add(CodeElementType.BLOCK);
		CodeElementType elementType = statement.getLocationInfo().getCodeElementType();
		int extractedOperationInvocationCountInStatement = 0;
		int otherOperationInvocationCountInStatement = 0;

		if (neutralCodeElements.contains(elementType)) {
			return true;
		} else {
			Map<String, List<OperationInvocation>> mapStatementInvokations = statement.getMethodInvocationMap();
			if (mapStatementInvokations.isEmpty()) {
				// There is no invokations in the variable declaration statement
				return true;
			} else {
				for (String invokationString : mapStatementInvokations.keySet()) {
					List<OperationInvocation> statementInvokations = mapStatementInvokations.get(invokationString);
					if (isInvocationToExtractedOperation(statementInvokations, statementOperation, refList)) {
						extractedOperationInvocationCountInStatement++;
					} else {
						otherOperationInvocationCountInStatement++;
					}
				}
				if (elementType.equals(CodeElementType.IF_STATEMENT)
						|| elementType.equals(CodeElementType.EXPRESSION_STATEMENT)
						|| elementType.equals(CodeElementType.VARIABLE_DECLARATION_STATEMENT)) {
					if (extractedOperationInvocationCountInStatement == 0
							&& otherOperationInvocationCountInStatement == 0) {
						return true;
					}
					if (extractedOperationInvocationCountInStatement == 0
							&& otherOperationInvocationCountInStatement > 0) {
						if (elementType.equals(CodeElementType.VARIABLE_DECLARATION_STATEMENT)
								|| elementType.equals(CodeElementType.EXPRESSION_STATEMENT)) {
							if (isStatementInvocationExpressionReturned(statement, statementOperation)) {
								return true;
							}
							if (isInvocationExpressionsInOperationVariableNames(statement, statementOperation)) {
								return false;
							}
							if (isStatementInvocationsInAddedOperations(statement, statementOperation,
									addedOperationNames)) {
								return false;
							}
							if (!isStatementInvocationsInAllOperationNames(statement, allOperationNames)) {
								return false;
							}
							return true;
						}
					}
				} else {
					if (extractedOperationInvocationCountInStatement == 0) {
						if (extractedOperationInvocationCountInStatement == 0
								&& otherOperationInvocationCountInStatement > 0) {
							if (!isStatementInvocationsInAllOperationNames(statement, allOperationNames)) {
								return false;
							}
							if (isStatementInvocationsInAddedOperations(statement, statementOperation,
									addedOperationNames)) {
								return false;
							}
						}
						return true;
					}
				}
			}
		}
		return false;
	}

	private boolean isStatementInvocationExpressionReturned(AbstractStatement statement,
			UMLOperation statementOperation) {
		List<String> statementInvocations = getStatementInvocationExpressions(statement);
		for (StatementObject leaf : statementOperation.getBody().getCompositeStatement().getLeaves()) {
			if (leaf.getLocationInfo().getCodeElementType().equals(CodeElementType.RETURN_STATEMENT)) {
				for (String variable : leaf.getVariables()) {
					if (statementInvocations.contains(variable)) {
						return true;
					}
				}
			}
		}
		if (statementInvocations.contains("")) {
			return true;
		}
		return false;
	}

	private boolean isStatementInvocationsInAllOperationNames(AbstractStatement statement,
			List<String> allOperationNames) {
		List<String> methodNamesInvokedInStatement = getStatementInvocationNames(statement);

		List<String> invorcationsInAllOperations = new ArrayList<String>();
		for (String methodInvocation : methodNamesInvokedInStatement) {
			if (allOperationNames.contains(methodInvocation)) {
				invorcationsInAllOperations.add(methodInvocation);
			}
		}
		if (invorcationsInAllOperations.size() == methodNamesInvokedInStatement.size()) {
			return true;
		}
		return false;
	}

	private boolean isStatementInvocationsInAddedOperations(AbstractStatement statement,
			UMLOperation statementOperation, List<String> addedOperationNames) {
		List<String> addedOperationsInVariableDeclarionOrParameterClasses = new ArrayList<String>();
		List<String> invocationClassNames = new ArrayList<String>();
		List<String> methodNamesInvokedInStatement = getStatementInvocationNames(statement);
		// Adding Classes of the variable declarations or parameter Class names with
		// same class Type of invocation expression
		for (String invocationString : statement.getMethodInvocationMap().keySet()) {
			for (OperationInvocation invocation : statement.getMethodInvocationMap().get(invocationString)) {
				if (invocation.getExpression() == null || invocation.getExpression().equals("this")) {
					invocationClassNames.add(statementOperation.getClassName());
				}
				for (VariableDeclaration declaration : statementOperation.getAllVariableDeclarations()) {
					if (declaration.getVariableName().equals(invocation.getExpression())) {
						invocationClassNames.add(declaration.getType().getClassType());
					}
				}
				List<UMLParameter> statementOperationParameters = statementOperation.getParameters();
				for (UMLParameter parameter : statementOperationParameters) {
					if (parameter.getName().equals(invocation.getExpression())) {
						invocationClassNames.add(parameter.getType().getClassType());
					}
				}
			}
		}
		for (String invocationClassName : invocationClassNames) {
			UMLClassBaseDiff umlClassBaseDiff = modelDiff.getUMLClassDiff(invocationClassName);
			if (umlClassBaseDiff != null) {
				for (UMLOperation operation : umlClassBaseDiff.getAddedOperations()) {
					addedOperationsInVariableDeclarionOrParameterClasses.add(operation.getName());
				}
			}
		}

		addedOperationNames.addAll(addedOperationsInVariableDeclarionOrParameterClasses);

		List<String> invorcationsInAddedOperations = new ArrayList<String>();
		for (String methodInvocation : methodNamesInvokedInStatement) {
			if (addedOperationNames.contains(methodInvocation)) {
				invorcationsInAddedOperations.add(methodInvocation);
			}
		}

		if (invorcationsInAddedOperations.size() == methodNamesInvokedInStatement.size()) {
			return true;
		}
		return false;
	}

	private List<String> getStatementInvocationNames(AbstractStatement statement) {

		List<String> methodNamesInvokedInStatement = new ArrayList<String>();
		for (String invocationString : statement.getMethodInvocationMap().keySet()) {
			List<OperationInvocation> operationInvocations = statement.getMethodInvocationMap().get(invocationString);
			for (OperationInvocation invocation : operationInvocations) {
				methodNamesInvokedInStatement.add(invocation.getMethodName());
			}
		}
		return methodNamesInvokedInStatement;
	}

	private List<String> getStatementInvocationExpressions(AbstractStatement statement) {

		List<String> statementInvocationExpressions = new ArrayList<String>();
		for (String invocationString : statement.getMethodInvocationMap().keySet()) {
			List<OperationInvocation> operationInvocations = statement.getMethodInvocationMap().get(invocationString);
			for (OperationInvocation invocation : operationInvocations) {
				if (invocation.getExpression() != null) {
					statementInvocationExpressions.add(invocation.getExpression());
				}
			}
		}
		return statementInvocationExpressions;
	}

	private List<String> getStatementInvocationArguments(AbstractStatement statement) {

		List<String> statementInvocationArguments = new ArrayList<String>();
		for (String invocationString : statement.getMethodInvocationMap().keySet()) {
			List<OperationInvocation> operationInvocations = statement.getMethodInvocationMap().get(invocationString);
			for (OperationInvocation invocation : operationInvocations) {
				statementInvocationArguments.addAll(invocation.getArguments());
			}
		}
		return statementInvocationArguments;
	}

	private enum OperationType {
		ADDED, ALL;
	}

	private List<String> getOperationNames(OperationType operationType) {

		List<String> operationNames = new ArrayList<String>();
		List<String> addedClassesOperationNames = new ArrayList<String>();
		List<String> commonClassesAddedOperationNames = new ArrayList<String>();
		List<String> commonClassesAllOperationNames = new ArrayList<String>();
		for (UMLClass addedClass : modelDiff.getAddedClasses()) {
			for (UMLOperation operation : addedClass.getOperations()) {
				addedClassesOperationNames.add(operation.getName());
			}
		}
		for (UMLClassDiff classDiff : modelDiff.getCommonClassDiffList()) {
			if (classDiff != null) {
				for (UMLOperation operartion : classDiff.getAddedOperations()) {
					commonClassesAddedOperationNames.add(operartion.getName());
				}
			}
		}
		for (UMLClassDiff classDiff : modelDiff.getCommonClassDiffList()) {
			if (classDiff != null) {
				for (UMLOperation operartion : classDiff.getNextClass().getOperations()) {
					commonClassesAllOperationNames.add(operartion.getName());
				}
			}
		}

		switch (operationType) {
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

	private boolean isInvocationToExtractedOperation(List<OperationInvocation> statementInvokations,
			UMLOperation statementOperation, List<Refactoring> refList) {
		List<OperationInvocation> invokationToExtractedOperation = new ArrayList<OperationInvocation>();
		for (OperationInvocation invokation : statementInvokations) {
			for (Refactoring ref : refList) {
				ExtractOperationRefactoring extractOperationRefactoring = (ExtractOperationRefactoring) ref;
				if (invokation.matchesOperation(extractOperationRefactoring.getExtractedOperation(),
						statementOperation.variableDeclarationMap(), modelDiff)) {
					invokationToExtractedOperation.add(invokation);
				}
			}
		}
		return invokationToExtractedOperation.size() > 0 ? true : false;
	}

	private boolean isExtractReusableMethod(Refactoring ref, List<Refactoring> refList) {
		if (ref instanceof ExtractOperationRefactoring) {
			countSingleMethodRemoveDuplications = 0;
			ExtractOperationRefactoring extractOpRefactoring = (ExtractOperationRefactoring) ref;
			UMLOperation extractedOperation = extractOpRefactoring.getExtractedOperation();
			// Removing duplication from a single method.
			Map<String, List<ExtractOperationRefactoring>> groupedByToStringSingleMthods = removeDuplicationFromSingleMethodRefactorings
					.stream().collect(Collectors.groupingBy(x -> x.toString()));
			if (groupedByToStringSingleMthods.containsKey(extractOpRefactoring.toString())) {
				countSingleMethodRemoveDuplications = groupedByToStringSingleMthods.get(extractOpRefactoring.toString())
						.size();
			}
			if (countSingleMethodRemoveDuplications > 0) {
				countSingleMethodRemoveDuplications--;
			}
			Map<UMLOperation, List<OperationInvocation>> mapExtraExtractedOperationInvokationsInClasses = new HashMap<UMLOperation, List<OperationInvocation>>();
			for (UMLClassDiff classDiff : modelDiff.getCommonClassDiffList()) {
				if (classDiff != null) {
					UMLClass nextClass = classDiff.getNextClass();
					mapExtraExtractedOperationInvokationsInClasses.putAll(
							extractedOperationInvocationsCountInClass(extractOpRefactoring, nextClass, refList));
				}
			}
			for (UMLClass addedClass : modelDiff.getAddedClasses()) {
				mapExtraExtractedOperationInvokationsInClasses
						.putAll(extractedOperationInvocationsCountInClass(extractOpRefactoring, addedClass, refList));
			}
			// Check the mappings of invocations to Extracted method in Original and next
			// class
			if (isExtractedMethodInvocationsEqualInOriginalAndNextClass(ref)) {
				setMotivationFlag(MotivationFlag.EM_INVOCATION_EQUAL_MAPPING, extractOpRefactoring);
				return false;
			}

			List<UMLOperation> listMatchedOperationsWithExtractedOperationInOtherClasses = getAllMatchedOperationsInOtherClasses(
					extractedOperation);
			// In case when there are no other operations in other classes matches extracted
			// operation.
			if (listMatchedOperationsWithExtractedOperationInOtherClasses.size() == 0) {
				if (reusabilityRulesValidation(extractOpRefactoring, mapExtraExtractedOperationInvokationsInClasses)) {
					return true;
				}
			} else {
				setMotivationFlag(MotivationFlag.EM_EQUAL_SINATURE_INVOCATIONS, extractOpRefactoring);
				Map<String, List<ExtractOperationRefactoring>> groupedByToString = refList.stream() // For each
																									// refactoring
						// Cast to ExtractOperationRefactoring
						.map(x -> (ExtractOperationRefactoring) x)
						// Group by the toString() value
						.collect(Collectors.groupingBy(x -> x.toString()));

				// Checking for cases where all extract operations are unique
				if (groupedByToString.entrySet().stream().allMatch(x -> x.getValue().size() == 1)) {
					/*
					 * When there are matching Operations with the same name as extracted operation
					 * in other classes. e.g. intellij-community:10f769a exists, A UMLOperation with
					 * same name as extracted method exists in
					 * com.intellij.execution.junit.JUnit4Framework class
					 */
					Map<UMLOperation, List<OperationInvocation>> mapExtraExtractedOperationInvokationsInOtherClassesWhenMatchingOperationExists = getExtraInvocationsToExtractedMethodWhenMatchingOperationExists(
							extractedOperation, mapExtraExtractedOperationInvokationsInClasses);
					// When Invocation in other classes to extracted operation exists e.g:
					// alluxio:ed966510
					if (mapExtraExtractedOperationInvokationsInOtherClassesWhenMatchingOperationExists.size() > 0) {
						return true;
					} else {
						// When Invocation to extracted method in other classes does not exist but in
						// extracted method class there are extra calls to extracted method.
						for (UMLOperation operation : mapExtraExtractedOperationInvokationsInClasses.keySet()) {
							for (OperationInvocation invokation : mapExtraExtractedOperationInvokationsInClasses
									.get(operation)) {
								if (invokation.getExpression() == null || invokation.getExpression().equals("this")) {
									if (operation.getClassName().equals(extractedOperation.getClassName())) {
										if ((extractOpRefactoring.getExtractedOperationInvocations().size()
												- countSingleMethodRemoveDuplications) > 1) {
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

	private boolean isExtractedMethodInvocationsEqualInOriginalAndNextClass(Refactoring ref) {
		Map<UMLOperation, List<OperationInvocation>> extractedOperationInvokationsInNextClasses = new HashMap<UMLOperation, List<OperationInvocation>>();
		Map<UMLOperation, List<OperationInvocation>> sourceOperationInvokationsInOriginalClass = new HashMap<UMLOperation, List<OperationInvocation>>();
		if (ref instanceof ExtractOperationRefactoring) {
			ExtractOperationRefactoring extractOpRefactoring = (ExtractOperationRefactoring) ref;
			UMLOperation extractedOperation = extractOpRefactoring.getExtractedOperation();
			UMLOperation sourceOperationBeforeExtraction = extractOpRefactoring.getSourceOperationBeforeExtraction();
			extractedOperationInvokationsInNextClasses = getAllExtractedOperationInvocationsInNextClasses(
					extractedOperation);
			sourceOperationInvokationsInOriginalClass = getAllSourceOperationBeforeExtractionInvocationsInOriginalClass(
					sourceOperationBeforeExtraction);
			if (isMotivationDetected(extractOpRefactoring,
					MotivationType.EM_REPLACE_METHOD_PRESERVING_BACKWARD_COMPATIBILITY)) {
				int nextInvocationCount = extractedOperationInvokationsInNextClasses.values().size();
				int OriginalInvocationCount = sourceOperationInvokationsInOriginalClass.values().size();
				// Reduce one from the size of right side because of the delegate method extra
				// call.
				if (OriginalInvocationCount == nextInvocationCount - 1) {
					return true;
				}
			}
		}
		return false;
	}

	private Map<UMLOperation, List<OperationInvocation>> getAllExtractedOperationInvocationsInNextClasses(
			UMLOperation extractedOperation) {
		Map<UMLOperation, List<OperationInvocation>> extractedOperationInvokationsInNextClasses = new HashMap<UMLOperation, List<OperationInvocation>>();
		for (UMLClassDiff classDiff : modelDiff.getCommonClassDiffList()) {
			if (classDiff != null) {
				UMLClass nextClass = classDiff.getNextClass();
				for (UMLOperation operation : nextClass.getOperations()) {
					extractedOperationInvokationsInNextClasses
							.putAll(countOperationAInvokationsInOperationB(extractedOperation, operation));
				}
				for (UMLAnonymousClass nextClassAnonymousClass : nextClass.getAnonymousClassList()) {
					for (UMLOperation nextClassAnonymousClassOperation : nextClassAnonymousClass.getOperations()) {
						extractedOperationInvokationsInNextClasses.putAll(countOperationAInvokationsInOperationB(
								extractedOperation, nextClassAnonymousClassOperation));
					}
				}
			}
		}
		for (UMLClass addedClass : modelDiff.getAddedClasses()) {
			for (UMLOperation operation : addedClass.getOperations()) {
				extractedOperationInvokationsInNextClasses
						.putAll(countOperationAInvokationsInOperationB(extractedOperation, operation));
			}
			for (UMLAnonymousClass addedClassAnonymousClass : addedClass.getAnonymousClassList()) {
				for (UMLOperation addedClassAnonymousClassOperation : addedClassAnonymousClass.getOperations()) {
					extractedOperationInvokationsInNextClasses.putAll(countOperationAInvokationsInOperationB(
							extractedOperation, addedClassAnonymousClassOperation));
				}
			}
		}
		return extractedOperationInvokationsInNextClasses;
	}

	private Map<UMLOperation, List<OperationInvocation>> getAllSourceOperationBeforeExtractionInvocationsInOriginalClass(
			UMLOperation sourceOperationBeforeExtraction) {
		Map<UMLOperation, List<OperationInvocation>> sourceOperationInvokationsInOriginalClass = new HashMap<UMLOperation, List<OperationInvocation>>();
		for (UMLClassDiff classDiff : modelDiff.getCommonClassDiffList()) {
			UMLClass originalClass = classDiff.getOriginalClass();
			for (UMLOperation operation : originalClass.getOperations()) {
				sourceOperationInvokationsInOriginalClass
						.putAll(countOperationAInvokationsInOperationB(sourceOperationBeforeExtraction, operation));
			}
		}
		return sourceOperationInvokationsInOriginalClass;
	}

	private Map<UMLOperation, List<OperationInvocation>> getExtraInvocationsToExtractedMethodWhenMatchingOperationExists(
			UMLOperation extractedOperation,
			Map<UMLOperation, List<OperationInvocation>> mapExtraExtractedOperationInvokationsInClasses) {
		// Find extra invocations to extracted method when A matching operation to
		// Extracted method exists.
		Map<UMLOperation, List<OperationInvocation>> mapExtraInvocationsWithUmlOperationAsKeyWhenExtractMethodMatchingOperationExists = new HashMap<UMLOperation, List<OperationInvocation>>();
		String extractedOperationQualifiedClassName = extractedOperation.getNonQualifiedClassName();
		for (UMLOperation invocationOperation : mapExtraExtractedOperationInvokationsInClasses.keySet()) {
			List<OperationInvocation> invocationsInOtherClassesContainingExtractedMethodClassNameOrVariableTypeInExpression = new ArrayList<OperationInvocation>();
			for (OperationInvocation invocation : mapExtraExtractedOperationInvokationsInClasses
					.get(invocationOperation)) {
				String invocationExpression = invocation.getExpression();
				if (invocationExpression != null && !invocationExpression.equals("this")) {
					for (String variableString : invocationOperation.variableDeclarationMap().keySet()) {
						Set<VariableDeclaration> variableDeclaration = invocationOperation.variableDeclarationMap()
								.get(variableString);
						// Check to see if the expression type matches the extracted operation class
						if (invocationExpression.contains(extractedOperationQualifiedClassName)
								|| extractedOperationQualifiedClassName
										.equals(variableDeclaration.iterator().next().getType().getClassType())) {
							invocationsInOtherClassesContainingExtractedMethodClassNameOrVariableTypeInExpression
									.add(invocation);
						}
					}
				}
			}
			if (invocationsInOtherClassesContainingExtractedMethodClassNameOrVariableTypeInExpression.size() > 0) {
				if (mapExtraInvocationsWithUmlOperationAsKeyWhenExtractMethodMatchingOperationExists
						.containsKey(invocationOperation)) {
					mapExtraInvocationsWithUmlOperationAsKeyWhenExtractMethodMatchingOperationExists
							.get(invocationOperation)
							.addAll(invocationsInOtherClassesContainingExtractedMethodClassNameOrVariableTypeInExpression);
				} else {
					mapExtraInvocationsWithUmlOperationAsKeyWhenExtractMethodMatchingOperationExists.put(
							invocationOperation,
							invocationsInOtherClassesContainingExtractedMethodClassNameOrVariableTypeInExpression);
				}
			}
		}
		return mapExtraInvocationsWithUmlOperationAsKeyWhenExtractMethodMatchingOperationExists;
	}

	private boolean reusabilityRulesValidation(ExtractOperationRefactoring extractOpRefactoring,
			Map<UMLOperation, List<OperationInvocation>> mapExtraExtractedOperationInvokationsInClasses) {
		UMLOperation sourceOperationAfterExtraction = extractOpRefactoring.getSourceOperationAfterExtraction();
		List<OperationInvocation> sourceOperationAfterExtractionInvokations = sourceOperationAfterExtraction
				.getAllOperationInvocations();
		UMLOperation extractedOperation = extractOpRefactoring.getExtractedOperation();

		/*
		 * Rule Exception : Extract is not reusable when there is only one call to
		 * extracted operation from source after extraction and Call is outside source
		 * operation and outside extractOperationInvocationCount is one. Example(Nested
		 * Extract Refactorings): Checkstyle :5a9b7 , JGroups:f1533
		 */
		int extractedOperationCallsInsideSourceOperationAfterExtraction = 0;
		for (OperationInvocation invokation : sourceOperationAfterExtractionInvokations) {
			if (invokation.matchesOperation(extractedOperation, sourceOperationAfterExtraction.variableDeclarationMap(),
					modelDiff)) {
				extractedOperationCallsInsideSourceOperationAfterExtraction++;
			}
		}
		if (((extractOpRefactoring.getExtractedOperationInvocations().size() == 1)
				|| (extractOpRefactoring.getExtractedOperationInvocations().size() == 0))
				&& (extractedOperationCallsInsideSourceOperationAfterExtraction == 0)) {
			if (mapExtraExtractedOperationInvokationsInClasses.size() == 1) {
				setMotivationFlag(MotivationFlag.EM_NESTED_INVOCATIONS, extractOpRefactoring);
				return false;
			}
		}
		/*
		 * GENERAL DETECTION RULE: IF Invocations to Extracted method from source method
		 * after Extraction is more than one OR there are other Invocations from other
		 * methods to extracted operation. Invocations inside test methods will not be
		 * considered as reusable calls.
		 */
		int extarctOpRefactoringCallstoExtractedOperation = extractOpRefactoring.getExtractedOperationInvocations()
				.size();

		int em_soae_invocations = extarctOpRefactoringCallstoExtractedOperation - countSingleMethodRemoveDuplications;
		setMotivationFlag(MotivationFlag.EM_SOAE_INVOCATIONS.setMotivationValue(em_soae_invocations),
				extractOpRefactoring);

		int em_not_soae_invocations = mapExtraExtractedOperationInvokationsInClasses.size();
		setMotivationFlag(MotivationFlag.EM_NONE_SOAE_INVOCATIONS.setMotivationValue(em_not_soae_invocations),
				extractOpRefactoring);

		if ((extarctOpRefactoringCallstoExtractedOperation - countSingleMethodRemoveDuplications) > 1
				|| mapExtraExtractedOperationInvokationsInClasses.size() > 0) {
			return true;
		}
		return false;
	}

	private Map<UMLOperation, List<OperationInvocation>> extractedOperationInvocationsCountInClass(
			ExtractOperationRefactoring extractOpRefactoring, UMLClass nextClass, List<Refactoring> refList) {
		Map<UMLOperation, List<OperationInvocation>> mapAllExtraOperationInvokations = new HashMap<UMLOperation, List<OperationInvocation>>();
		for (UMLOperation operation : nextClass.getOperations()) {
			mapAllExtraOperationInvokations
					.putAll(computeAllReusedInvokationsToExtractedMethod(operation, extractOpRefactoring, refList));
			for (UMLAnonymousClass anonymousClass : nextClass.getAnonymousClassList()) {
				for (UMLOperation anonymousClassOperation : anonymousClass.getOperations()) {
					mapAllExtraOperationInvokations.putAll(computeAllReusedInvokationsToExtractedMethod(
							anonymousClassOperation, extractOpRefactoring, refList));
				}
			}
		}
		return mapAllExtraOperationInvokations;
	}

	private Map<UMLOperation, List<OperationInvocation>> computeAllReusedInvokationsToExtractedMethod(
			UMLOperation operation, ExtractOperationRefactoring extractOpRefactoring, List<Refactoring> refList) {
		Map<UMLOperation, List<OperationInvocation>> mapExtraOperationInvokations = new HashMap<UMLOperation, List<OperationInvocation>>();
		UMLOperation extractedOperation = extractOpRefactoring.getExtractedOperation();
		UMLOperation sourceOperationAfterExtration = extractOpRefactoring.getSourceOperationAfterExtraction();
		boolean considerCallsFromTestMethodsAsReuse = (sourceOperationAfterExtration.hasTestAnnotation()
				|| sourceOperationAfterExtration.getName().startsWith("test")) ? true : false;
		/*
		 * In the cases when extracted operation is extracted from a test method (is
		 * part of the test code), extra calls from test methods to extracted operation
		 * are considered as reuse.
		 */
		if (considerCallsFromTestMethodsAsReuse) {
			setMotivationFlag(MotivationFlag.SOAE_IS_TEST_OPERATION, extractOpRefactoring);
			if (!operation.equals(sourceOperationAfterExtration) && !operation.equals(extractedOperation)
					&& !sourceOperationAfterExtration.getLocationInfo().subsumes(operation.getLocationInfo())) {
				mapExtraOperationInvokations
						.putAll(computeReusedInvokationsToExtractedMethod(operation, extractOpRefactoring, refList));
			}
		} else {
			if (operation.hasTestAnnotation() || operation.getName().startsWith("test")) {
				setMotivationFlag(MotivationFlag.EM_INVOCATION_IN_TEST_OPERATION, extractOpRefactoring);
			}
			if (!operation.equals(sourceOperationAfterExtration) && !operation.equals(extractedOperation)
					&& !sourceOperationAfterExtration.getLocationInfo().subsumes(operation.getLocationInfo())
					&& !operation.hasTestAnnotation() && !operation.getName().startsWith("test")) {
				mapExtraOperationInvokations
						.putAll(computeReusedInvokationsToExtractedMethod(operation, extractOpRefactoring, refList));
			}
		}
		return mapExtraOperationInvokations;
	}

	private Map<UMLOperation, List<OperationInvocation>> computeReusedInvokationsToExtractedMethod(
			UMLOperation operation, ExtractOperationRefactoring extractOpRefactoring, List<Refactoring> refList) {
		Map<UMLOperation, List<OperationInvocation>> mapExtraInvokations = new HashMap<UMLOperation, List<OperationInvocation>>();
		UMLOperation extractedOperation = extractOpRefactoring.getExtractedOperation();

		if (isMotivationDetected(extractOpRefactoring, MotivationType.EM_REMOVE_DUPLICATION) && refList.size() > 1) {
			/*
			 * When we check extra calls to detect Reuse if there has been a Remove
			 * Duplication motivation from multiple source methods we ignore extra
			 * invokations from "same remove duplication"
			 * "source operations after extraction" Extract method's.
			 */
			if (!isOperationEqualToSourceOperationAfterExtractionOfSameRemoveDuplicationGroupExtractRefactorings(
					operation, refList, extractOpRefactoring)) {
				mapExtraInvokations.putAll(countOperationAInvokationsInOperationB(extractedOperation, operation));
			} else {
				setMotivationFlag(MotivationFlag.EM_INVOCATION_IN_REMOVE_DUPLICATION, extractOpRefactoring);
			}
		} else {
			if (!oneLineRemoveDuplications.contains(extractOpRefactoring)) {
				mapExtraInvokations.putAll(countOperationAInvokationsInOperationB(extractedOperation, operation));
			}
		}

		return mapExtraInvokations;
	}

	private boolean isOperationEqualToSourceOperationAfterExtractionOfSameRemoveDuplicationGroupExtractRefactorings(
			UMLOperation operation, List<Refactoring> refList, Refactoring exRefactoring) {
		ExtractOperationRefactoring mainExtractOperationRefactoring = (ExtractOperationRefactoring) exRefactoring;
		UMLOperation mainExtractedOperation = mainExtractOperationRefactoring.getExtractedOperation();
		for (Refactoring ref : refList) {
			if (ref instanceof ExtractOperationRefactoring) {
				ExtractOperationRefactoring otherExtractOperationRefactoring = (ExtractOperationRefactoring) ref;
				UMLOperation otherExtractedOperation = otherExtractOperationRefactoring.getExtractedOperation();
				if (isMotivationDetected(otherExtractOperationRefactoring, MotivationType.EM_REMOVE_DUPLICATION)
						&& mainExtractedOperation.equals(otherExtractedOperation)) {
					if (operation.equals(otherExtractOperationRefactoring.getSourceOperationAfterExtraction())) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private Map<UMLOperation, List<OperationInvocation>> countOperationAInvokationsInOperationB(UMLOperation operationA,
			UMLOperation operationB) {
		Map<UMLOperation, List<OperationInvocation>> mapOperationAInvokationsInOperationB = new HashMap<UMLOperation, List<OperationInvocation>>();
		List<OperationInvocation> invocations = operationB.getAllOperationInvocations();
		for (OperationInvocation invocation : invocations) {
			if (invocation.matchesOperation(operationA, operationB.variableDeclarationMap(), modelDiff)) {
				if (mapOperationAInvokationsInOperationB.containsKey(operationB)) {
					mapOperationAInvokationsInOperationB.get(operationB).add(invocation);
				} else {
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
		if (umlClassDiff != null) {
			UMLClass umlClass = umlClassDiff.getNextClass();
			for (UMLClassDiff classDiff : modelDiff.getCommonClassDiffList()) {
				if (classDiff != null) {
					UMLClass nextCommonClass = classDiff.getNextClass();
					if (nextCommonClass.matchOperation(umlOperation) != null && !nextCommonClass.equals(umlClass)) {
						listEqualOperations.add(nextCommonClass.matchOperation(umlOperation));
					}
				}
				for (UMLClass addedClass : modelDiff.getAddedClasses()) {
					if (addedClass.matchOperation(umlOperation) != null && !addedClass.equals(umlClass)) {
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
			if (ref instanceof ExtractOperationRefactoring) {
				ExtractOperationRefactoring extractOpRefactoring = (ExtractOperationRefactoring) ref;
				UMLOperation sourceOperation = extractOpRefactoring.getSourceOperationBeforeExtraction();
				if (extractOperationMapWithSourceOperationAsKey.containsKey(sourceOperation)) {
					extractOperationMapWithSourceOperationAsKey.get(sourceOperation).add(extractOpRefactoring);
				} else {
					List<ExtractOperationRefactoring> list = new ArrayList<ExtractOperationRefactoring>();
					list.add(extractOpRefactoring);
					extractOperationMapWithSourceOperationAsKey.put(sourceOperation, list);
				}
			}
		}
		int countDecomposeMethodToImproveReadability = 0;
		for (UMLOperation key : extractOperationMapWithSourceOperationAsKey.keySet()) {
			List<ExtractOperationRefactoring> list = extractOperationMapWithSourceOperationAsKey.get(key);
			/*
			 * DETECTION RULE: if multiple extract operations have the same source Operation
			 * the extract operations motivations is Decompose to Improve Readability
			 */

			if (list.size() > 1) {
				if (!isExtractMethodRefactoringsEqual(list)) {
					for (ExtractOperationRefactoring ref : list) {
						setMotivationFlag(MotivationFlag.EM_DISTINCT, ref);
						setMotivationFlag(MotivationFlag.EM_WITH_SAME_SOURCE_OPERATION, ref);
					}
					// Compute the Decompose Extract methods that with with Edit distance more than
					// 15%
					int countDecompose = 0;
					for (ExtractOperationRefactoring ref : list) {
						double editDistance = getDecomposeNormalizedEditDistance(ref);
						if (editDistance > 0.55) {
							setMotivationFlag(MotivationFlag.EM_INVOCATION_EDIT_DISTANCE_THRESHOLD_MM, ref);
							countDecompose++;
						}
					}
					if (countDecompose > 1) {
						// CODE ANALYSYS
						codeAnalysisDecomposeToImproveRedability(list);
						for (ExtractOperationRefactoring ref : list) {
							// Set Motivation for each refactoring with the same source
							setRefactoringMotivation(MotivationType.EM_DECOMPOSE_TO_IMPROVE_READABILITY, ref);
							setMotivationFlag(MotivationFlag.EM_DECOMPOSE_MULTIPLE_METHODS, ref);
							countDecomposeMethodToImproveReadability++;
						}
					}
				} else {
					for (ExtractOperationRefactoring ref : list) {
						setMotivationFlag(MotivationFlag.EM_WITH_SAME_SOURCE_OPERATION, ref);
					}
				}
			}
		}

		if (countDecomposeMethodToImproveReadability >= 2) {
			decomposeToImproveReadabilityFromMultipleMethodRefactorings = countDecomposeMethodToImproveReadability;
			return true;
		}

		return false;
	}

	private double getDecomposeNormalizedEditDistance(ExtractOperationRefactoring extractOperationRef) {
		List<ExtractOperationRefactoring> excludedExtractOperations = new ArrayList<ExtractOperationRefactoring>();
		double normalizedEditDistance = 1.0;
		Set<AbstractCodeMapping> mappings = extractOperationRef.getBodyMapper().getMappings();
		// if there is only one leaf mapping compute the normalized distance between
		// invocation string and fragment 1
		// Otherwise return 1
		if (mappings.size() == 1) {
			if (mappings.iterator().next() instanceof LeafMapping) {
				String fragment1 = mappings.iterator().next().getFragment1().getString().toLowerCase();
				String invocationStatement = new String();
				String invocationExpression = new String();
				List<OperationInvocation> extractedOperationInvocations = extractOperationRef
						.getExtractedOperationInvocations();
				LocationInfo extractedOperationInvocationLocation = extractedOperationInvocations.get(0)
						.getLocationInfo();
				UMLOperation sourceOperationAfterExtraction = extractOperationRef.getSourceOperationAfterExtraction();
				List<StatementObject> sourceOperationAfterExtractionLeaves = sourceOperationAfterExtraction.getBody()
						.getCompositeStatement().getLeaves();

				List<AbstractExpression> allCompositeExpressions = getAllCompositeStatementObjectExpressionsWithInvokationsToExtractedOperation(
						sourceOperationAfterExtraction.getBody().getCompositeStatement(),
						extractOperationRef.getExtractedOperation(), sourceOperationAfterExtraction);

				for (StatementObject leaf : sourceOperationAfterExtractionLeaves) {
					if (leaf.getLocationInfo().subsumes(extractedOperationInvocationLocation)) {
						invocationStatement = leaf.getString();
						break;
					}
				}
				for (AbstractExpression expression : allCompositeExpressions) {
					if (expression.getLocationInfo().subsumes(extractedOperationInvocationLocation)) {
						invocationExpression = expression.getString();
						break;
					}
				}

				List<String> invocations = new ArrayList<String>();
				if (!invocationStatement.isEmpty()) {
					invocations.add(invocationStatement);
				}
				if (!invocationExpression.isEmpty()) {
					invocations.add(invocationExpression);
				}

				if (invocations.size() > 0) {
					int distance = StringDistance.editDistance(fragment1, invocations.get(0));
					normalizedEditDistance = (double) distance
							/ (double) Math.max(fragment1.length(), invocationStatement.length());
				}
			}
		}
		return normalizedEditDistance;
	}

	private boolean isExtractMethodRefactoringsEqual(List<ExtractOperationRefactoring> extractOperationRefactorings) {
		Set<UMLOperation> setSourceOperationsBeforeExtraction = new HashSet<>();
		Set<UMLOperation> setExtractedOperations = new HashSet<>();
		Set<UMLOperation> setSourceOperationsAfterExtraction = new HashSet<>();
		Set<String> setSourceFiles = new HashSet<>();
		Set<String> setClassesNames = new HashSet<>();
		for (ExtractOperationRefactoring extractOpRefactroing : extractOperationRefactorings) {
			setSourceOperationsBeforeExtraction.add(extractOpRefactroing.getSourceOperationBeforeExtraction());
			setSourceOperationsAfterExtraction.add(extractOpRefactroing.getSourceOperationAfterExtraction());
			setExtractedOperations.add(extractOpRefactroing.getExtractedOperation());
			setClassesNames.add(extractOpRefactroing.getExtractedOperation().getClassName());
			setClassesNames.add(extractOpRefactroing.getSourceOperationBeforeExtraction().getClassName());

			UMLClassBaseDiff umlClass = modelDiff
					.getUMLClassDiff(extractOpRefactroing.getExtractedOperation().getClassName());
			if (umlClass != null) {
				UMLClass nextClass = umlClass.getNextClass();
				UMLClass OriginalClass = umlClass.getOriginalClass();
				setSourceFiles.add(nextClass.getSourceFile());
				setSourceFiles.add(OriginalClass.getSourceFile());
			}
		}
		if (setExtractedOperations.size() == 1 && setSourceOperationsBeforeExtraction.size() == 1
				&& setSourceOperationsAfterExtraction.size() == 1 && setClassesNames.size() == 1
				&& setSourceFiles.size() == 1) {
			return true;
		}

		return false;

	}

	private void codeAnalysisDecomposeToImproveRedability(List<ExtractOperationRefactoring> list) {

		for (ExtractOperationRefactoring extractOpRefactoring : list) {
			String sizeExtractedMethod = Integer.toString(extractOpRefactoring.getBodyMapper().getMappings().size());
			mapDecomposeToImproveRedability.put(extractOpRefactoring, sizeExtractedMethod);
		}
	}

	private boolean isExtractedOperationInvokationsToImproveReadability(Refactoring ref) {
		if (ref instanceof ExtractOperationRefactoring) {
			ExtractOperationRefactoring extractOperationRef = (ExtractOperationRefactoring) ref;
			UMLOperation sourceOperationAfterExtraction = extractOperationRef.getSourceOperationAfterExtraction();
			UMLOperation extratedOperation = extractOperationRef.getExtractedOperation();
			List<StatementObject> listReturnStatementswithCallsToExtractedOperation = getStatementsCallingExtractedOperation(
					extractOperationRef, CodeElementType.RETURN_STATEMENT);
			List<StatementObject> listVariableDeclarationStatementsWithCallsToExtractedOperation = getStatementsCallingExtractedOperation(
					extractOperationRef, CodeElementType.VARIABLE_DECLARATION_STATEMENT);
			List<StatementObject> listExpressioNStatementsWithCallsToExtractedOperation = getStatementsCallingExtractedOperation(
					extractOperationRef, CodeElementType.EXPRESSION_STATEMENT);
			List<AbstractExpression> expressionsUsingVariableInitializedWithExtracedOperationInvocation = new ArrayList<AbstractExpression>();
			CompositeStatementObject sourceOperationAfterExtractionBody = sourceOperationAfterExtraction.getBody()
					.getCompositeStatement();
			for (StatementObject statement : listVariableDeclarationStatementsWithCallsToExtractedOperation) {
				for (VariableDeclaration declaration : statement.getVariableDeclarations()) {
					expressionsUsingVariableInitializedWithExtracedOperationInvocation.addAll(
							getAllCompositeStatementObjectExpressionsUsingVariable(sourceOperationAfterExtractionBody,
									declaration.getVariableName()));
				}
			}
			// if notMapped T1 statements are 0 and everything is mapped there is no
			// decomposition
			if (isEverySourceOperationNodeMapped(extractOperationRef)) {
				setMotivationFlag(MotivationFlag.EM_ALL_SOURCE_OPERATION_NODES_MAPPED, extractOperationRef);
				return false;
			}
			// Checking the structure of the mappings for nested composite statements
			if (listVariableDeclarationStatementsWithCallsToExtractedOperation.size() > 0
					|| listExpressioNStatementsWithCallsToExtractedOperation.size() > 0) {
				if (isMappingComplicatedStructure(extractOperationRef)) {
					return true;
				}
			}
			/*
			 * Check all expressions of the source operation after extraction to see if
			 * there is any calls to extracted operation Check if any expression exists with
			 * Variables initialized with invokations to the extracted operation Check if
			 * any return statements exists with calls to the extracted operation
			 */
			if (listReturnStatementswithCallsToExtractedOperation.size() > 0) {
				decomposeToImproveReadabilityFromSingleMethodByHavingCallToExtractedMethodInReturn
						.add((ExtractOperationRefactoring) ref);
			}
			List<AbstractExpression> expressionsInCompositesWithCallsToExtractedMethod = getAllCompositeStatementObjectExpressionsWithInvokationsToExtractedOperation(
					sourceOperationAfterExtractionBody, extratedOperation, sourceOperationAfterExtraction);

			if (expressionsInCompositesWithCallsToExtractedMethod.size() > 0) {
				int em_composite_expression_invocations = expressionsInCompositesWithCallsToExtractedMethod.size();
				setMotivationFlag(MotivationFlag.EM_COMPOSITE_EXPRESSION_INVOCATIONS
						.setMotivationValue(em_composite_expression_invocations), extractOperationRef);

			}
			if (expressionsUsingVariableInitializedWithExtracedOperationInvocation.size() > 0) {
				setMotivationFlag(MotivationFlag.EM_COMPOSITE_EXPRESSION_CALLVAR, extractOperationRef);

			}
			if ((listReturnStatementswithCallsToExtractedOperation.size() > 0)
					&& (sourceOperationAfterExtraction.getBody().statementCount() > 1)) {
				int em_return_statement_invocations = listReturnStatementswithCallsToExtractedOperation.size();
				setMotivationFlag(MotivationFlag.EM_RETURN_STATEMENT_INVOCATIONS
						.setMotivationValue(em_return_statement_invocations), extractOperationRef);
			}

			if (expressionsInCompositesWithCallsToExtractedMethod.size() > 0
					|| /* listExpressioNStatementsWithCallsToExtractedOperation.size() > 0 || */
					expressionsUsingVariableInitializedWithExtracedOperationInvocation.size() > 0
					|| ((listReturnStatementswithCallsToExtractedOperation.size() > 0)
							&& (sourceOperationAfterExtraction.getBody().statementCount() > 1))) {
				return true;
			}

			OperationBody sourceOperationBody = extractOperationRef.getSourceOperationAfterExtraction().getBody();
			CompositeStatementObject compositeStatement = sourceOperationBody.getCompositeStatement();
			// if(isCompositeStatementWithLeavesCallingExtractedOepration(compositeStatement
			// , CodeElementType.CATCH_CLAUSE , extractOperationRef)) {
			// return true;
			// }
		}
		return false;
	}

	private boolean isEverySourceOperationNodeMapped(ExtractOperationRefactoring extractOperationRef) {
		Set<AbstractCodeMapping> mappings = extractOperationRef.getBodyMapper().getMappings();
		CompositeStatementObject sourceOperationBeforeExtractionBody = extractOperationRef
				.getSourceOperationBeforeExtraction().getBody().getCompositeStatement();
		CompositeStatementObject extractedOperationBody = extractOperationRef.getExtractedOperation().getBody()
				.getCompositeStatement();
		Set<String> mappingNodes = new HashSet<String>();
		Set<String> sourceOperationBeforeExtractionNodes = new HashSet<String>();
		Set<String> extractedOperationNodes = new HashSet<String>();
		for (AbstractCodeMapping mapping : mappings) {
			mappingNodes.add(mapping.getFragment2().toString());
		}

		for (StatementObject sourceLeave : sourceOperationBeforeExtractionBody.getLeaves()) {
			sourceOperationBeforeExtractionNodes.add(sourceLeave.toString());
		}
		for (CompositeStatementObject innerNode : sourceOperationBeforeExtractionBody.getInnerNodes()) {
			sourceOperationBeforeExtractionNodes.add(innerNode.toString());
		}

		for (StatementObject sourceLeave : extractedOperationBody.getLeaves()) {
			extractedOperationNodes.add(sourceLeave.toString());
		}
		for (CompositeStatementObject innerNode : extractedOperationBody.getInnerNodes()) {
			extractedOperationNodes.add(innerNode.toString());
		}
		List<String> identicalNodesInSourceAndExtractedOperation = new ArrayList<String>();
		for (String extractedOperationNode : extractedOperationNodes) {
			if (sourceOperationBeforeExtractionNodes.contains(extractedOperationNode)) {
				identicalNodesInSourceAndExtractedOperation.add(extractedOperationNode);
			}
		}
		// Removing identical nodes from mappings and source operation before extraction
		mappingNodes.removeAll(identicalNodesInSourceAndExtractedOperation);
		sourceOperationBeforeExtractionNodes.removeAll(identicalNodesInSourceAndExtractedOperation);

		int notMappedLeavesT1Size = extractOperationRef.getBodyMapper().getNonMappedLeavesT1().size();
		int notMappedInnerNodesT1Size = extractOperationRef.getBodyMapper().getNonMappedInnerNodesT1().size();

		if (notMappedLeavesT1Size == 0 && notMappedInnerNodesT1Size == 0) {
			if (sourceOperationBeforeExtractionNodes.size() == mappingNodes.size()) {
				return true;
			}
		}
		return false;
	}

	boolean isMappingComplicatedStructure(ExtractOperationRefactoring extractOperationRef) {
		Set<AbstractCodeMapping> codeMappings = extractOperationRef.getBodyMapper().getMappings();
		Set<CodeElementType> compositeCodeElements = new HashSet<CodeElementType>();
		compositeCodeElements.add(CodeElementType.FOR_STATEMENT);
		compositeCodeElements.add(CodeElementType.ENHANCED_FOR_STATEMENT);
		compositeCodeElements.add(CodeElementType.IF_STATEMENT);
		for (AbstractCodeMapping mapping : codeMappings) {
			AbstractCodeFragment fragment1 = mapping.getFragment1();
			CodeElementType fragment1Type = fragment1.getLocationInfo().getCodeElementType();
			if (compositeCodeElements.contains(fragment1Type)) {
				List<CompositeStatementObject> fragment1CompositeParents = new ArrayList<CompositeStatementObject>();
				fragment1CompositeParents = isAnyNonBlockParentComposite(fragment1, compositeCodeElements);
				List<CompositeStatementObject> compositeStatementsInMappingRange = getCompositeStatementsInMappingRange(
						fragment1CompositeParents, codeMappings);
				List<CompositeStatementObject> compositeStatementsInMappings = new ArrayList<CompositeStatementObject>();

				for (CompositeStatementObject composite : compositeStatementsInMappingRange) {
					if (isCompositeNodeInSourceOperation(composite, extractOperationRef)) {
						compositeStatementsInMappings.add(composite);
					}
				}
				if (fragment1CompositeParents != null && compositeStatementsInMappings != null) {
					if (compositeStatementsInMappings.size() > 0) {
						setMotivationFlag(MotivationFlag.EM_MAPPING_COMPOSITE_NODES
								.setMotivationValue(compositeStatementsInMappings.size()), extractOperationRef);
						return true;
					}
				}
			}
		}
		return false;
	}

	private boolean isCompositeNodeInSourceOperation(CompositeStatementObject compositeStatement,
			ExtractOperationRefactoring extractOperationRef) {
		CompositeStatementObject sourceOperationBeforeExtraction = extractOperationRef
				.getSourceOperationBeforeExtraction().getBody().getCompositeStatement();
		Set<CompositeStatementObject> allInnerNodes = new HashSet<CompositeStatementObject>();
		allInnerNodes.addAll(getAllInnerNodes(sourceOperationBeforeExtraction));
		if (allInnerNodes.contains(compositeStatement)) {
			return true;
		}
		return false;
	}

	private Set<CompositeStatementObject> getAllInnerNodes(CompositeStatementObject composite) {
		Set<CompositeStatementObject> allInnerNodes = new HashSet<CompositeStatementObject>();
		allInnerNodes.addAll(composite.getInnerNodes());
		for (CompositeStatementObject innerNode : allInnerNodes) {
			if (composite.getLocationInfo().getCodeElementType().equals(CodeElementType.BLOCK)) {
				break;
			} else {
				allInnerNodes.addAll(getAllInnerNodes(innerNode));
			}
		}
		return allInnerNodes;

	}

	private List<CompositeStatementObject> getCompositeStatementsInMappingRange(
			List<CompositeStatementObject> compositeStatements, Set<AbstractCodeMapping> mappings) {
		List<CompositeStatementObject> compositeStatementsInMappings = new ArrayList<CompositeStatementObject>();
		CodeRange mappingsCodeRange = getMappingsCodeRange(mappings);
		if (compositeStatements != null) {
			for (CompositeStatementObject composite : compositeStatements) {
				if (mappingsCodeRange.getStartLine() <= composite.getLocationInfo().getEndLine()
						&& mappingsCodeRange.getEndLine() >= composite.getLocationInfo().getEndLine()) {
					compositeStatementsInMappings.add(composite);
				}
			}
		}
		return compositeStatementsInMappings;
	}

	private CodeRange getMappingsCodeRange(Set<AbstractCodeMapping> mappings) {

		List<Integer> startLines = new ArrayList<Integer>();
		List<Integer> endLines = new ArrayList<Integer>();
		List<Integer> startColumns = new ArrayList<Integer>();
		List<Integer> endColumns = new ArrayList<Integer>();
		for (AbstractCodeMapping abstractMapping : mappings) {
			startLines.add(abstractMapping.getFragment1().getLocationInfo().codeRange().getStartLine());
			endLines.add(abstractMapping.getFragment1().getLocationInfo().codeRange().getEndLine());
			startColumns.add(abstractMapping.getFragment1().getLocationInfo().codeRange().getStartColumn());
			endColumns.add(abstractMapping.getFragment1().getLocationInfo().codeRange().getEndColumn());
		}
		startLines.sort(Collections.reverseOrder());
		endLines.sort(Collections.reverseOrder());
		startColumns.sort(Collections.reverseOrder());
		endColumns.sort(Collections.reverseOrder());

		return new CodeRange("", startLines.get(startLines.size() - 1), endLines.get(0),
				startColumns.get(startColumns.size() - 1), endColumns.get(0), CodeElementType.LIST_OF_STATEMENTS);
	}

	private List<CompositeStatementObject> isAnyNonBlockParentComposite(AbstractCodeFragment abstractCodeFragment,
			Set<CodeElementType> compositeCodeElements) {
		CompositeStatementObject nonBlockParent = getNonBlockParentOfAbstractCodeFragment(abstractCodeFragment);
		List<CompositeStatementObject> compositeParents = new ArrayList<CompositeStatementObject>();
		if (nonBlockParent != null) {
			if (compositeCodeElements.contains(nonBlockParent.getLocationInfo().getCodeElementType())) {
				compositeParents.add(nonBlockParent);
				CompositeStatementObject composite = getNonBlockParentOfAbstractStatement(nonBlockParent);
				if (composite != null) {
					compositeParents.add(composite);
					if (isAnyNonBlockParentComposite(composite, compositeCodeElements) != null)
						compositeParents.addAll(isAnyNonBlockParentComposite(composite, compositeCodeElements));
				}
			}
		}
		if (compositeParents.size() > 0) {
			return compositeParents;
		}
		return null;
	}

	private boolean isCompositeStatementWithLeavesCallingExtractedOepration(CompositeStatementObject compositeStatement,
			CodeElementType codeElementType, ExtractOperationRefactoring extractOperationRef) {
		if (compositeStatement.getLocationInfo().getCodeElementType().equals(codeElementType)) {
			if (isLeavesContainingCallsToExtractedMethod(compositeStatement, extractOperationRef)) {
				return true;
			}
		} else {
			List<CompositeStatementObject> innerNodes = compositeStatement.getInnerNodes();
			for (CompositeStatementObject composite : innerNodes) {
				if (composite.getLocationInfo().getCodeElementType().equals(codeElementType)) {
					if (isLeavesContainingCallsToExtractedMethod(composite, extractOperationRef)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private boolean isLeavesContainingCallsToExtractedMethod(CompositeStatementObject compositeStatement,
			ExtractOperationRefactoring extractOperationRef) {
		List<StatementObject> listStatementObjects = compositeStatement.getLeaves();
		for (StatementObject statement : listStatementObjects) {
			for (OperationInvocation invokation : extractOperationRef.getExtractedOperationInvocations()) {
				if (statement.getLocationInfo().subsumes(invokation.getLocationInfo())) {
					return true;
				}
			}
		}
		return false;
	}

	private List<StatementObject> getStatementsCallingExtractedOperation(
			ExtractOperationRefactoring extractOperationRef, CodeElementType codeElementType) {
		List<StatementObject> statementswithCallsToExtractedOperation = new ArrayList<StatementObject>();
		OperationBody sourceOperationBody = extractOperationRef.getSourceOperationAfterExtraction().getBody();
		CompositeStatementObject compositeStatement = sourceOperationBody.getCompositeStatement();
		// Check statements to see if they have calls to extracted operation
		List<StatementObject> listStatementObjects = compositeStatement.getLeaves();
		for (StatementObject statement : listStatementObjects) {
			CodeElementType type = statement.getLocationInfo().getCodeElementType();
			if (type.equals(codeElementType)) {
				for (OperationInvocation invokation : extractOperationRef.getExtractedOperationInvocations()) {
					if (statement.getLocationInfo().subsumes(invokation.getLocationInfo())
							&& invokation.getExpression() == null) {
						// if(isAllStatementInvocationsToExtractedOperation(statement ,
						// extractOperationRef.getExtractedOperation())) {
						statementswithCallsToExtractedOperation.add(statement);
						break;
					}
				}
			}
		}
		return statementswithCallsToExtractedOperation;
	}

	private boolean isAllStatementInvocationsToExtractedOperation(StatementObject statement,
			UMLOperation extractOperation) {
		for (String invocationString : statement.getMethodInvocationMap().keySet()) {
			for (OperationInvocation invocation : statement.getMethodInvocationMap().get(invocationString)) {
				if (!invocation.matchesOperation(extractOperation)) {
					return false;
				}
			}
		}
		return true;
	}

	private List<AbstractExpression> getAllCompositeStatementObjectExpressionsWithInvokationsToExtractedOperation(
			CompositeStatementObject compositeStatement, UMLOperation invokedOperation,
			UMLOperation sourceOperationAfterExtraction) {
		List<AbstractExpression> listAbstractExpressions = compositeStatement.getExpressions();
		List<AbstractExpression> listExpressionsWithCallToExtractedOperation = new ArrayList<AbstractExpression>();
		Map<String, List<OperationInvocation>> mapMerthodInvokations = new HashMap<String, List<OperationInvocation>>();
		for (AbstractExpression expression : listAbstractExpressions) {
			mapMerthodInvokations = expression.getMethodInvocationMap();
			for (String invokationString : mapMerthodInvokations.keySet()) {
				List<OperationInvocation> listInvokations = mapMerthodInvokations.get(invokationString);
				for (OperationInvocation invokation : listInvokations) {
					if (invokation.matchesOperation(invokedOperation,
							sourceOperationAfterExtraction.variableDeclarationMap(), modelDiff)) {
						listExpressionsWithCallToExtractedOperation.add(expression);
					}
				}
			}
		}
		List<AbstractStatement> listStatements = compositeStatement.getStatements();
		for (AbstractStatement statement : listStatements) {
			if (statement instanceof CompositeStatementObject) {
				CompositeStatementObject composite = (CompositeStatementObject) statement;
				listExpressionsWithCallToExtractedOperation
						.addAll(getAllCompositeStatementObjectExpressionsWithInvokationsToExtractedOperation(composite,
								invokedOperation, sourceOperationAfterExtraction));
			}
		}

		return listExpressionsWithCallToExtractedOperation;
	}

	private List<AbstractExpression> getAllCompositeStatementObjectExpressionsUsingVariable(
			CompositeStatementObject compositeStatement, String variableName) {
		List<AbstractExpression> listAbstractExpressions = compositeStatement.getExpressions();
		List<AbstractExpression> listExpressionsUsingVariable = new ArrayList<AbstractExpression>();
		for (AbstractExpression expression : listAbstractExpressions) {
			if (expression.getVariables().contains(variableName)) {
				listExpressionsUsingVariable.add(expression);
			}
		}
		List<AbstractStatement> listStatements = compositeStatement.getStatements();
		for (AbstractStatement statement : listStatements) {
			if (statement instanceof CompositeStatementObject) {
				CompositeStatementObject composite = (CompositeStatementObject) statement;
				listExpressionsUsingVariable
						.addAll(getAllCompositeStatementObjectExpressionsUsingVariable(composite, variableName));
			}
		}

		return listExpressionsUsingVariable;
	}

	private boolean isMethodExtractedToRemoveDuplication(List<Refactoring> refList) {
		Map<UMLOperation, List<ExtractOperationRefactoring>> sourceOperationMapWithExtractedOperationAsKey = new HashMap<>();

		for (Refactoring ref : refList) {
			if (ref instanceof ExtractOperationRefactoring) {
				ExtractOperationRefactoring extractOpRefactoring = (ExtractOperationRefactoring) ref;
				UMLOperation extractedOperation = extractOpRefactoring.getExtractedOperation();
				if (sourceOperationMapWithExtractedOperationAsKey.containsKey(extractedOperation))
					sourceOperationMapWithExtractedOperationAsKey.get(extractedOperation).add(extractOpRefactoring);
				else {
					List<ExtractOperationRefactoring> listExtractOperation = new ArrayList<ExtractOperationRefactoring>();
					listExtractOperation.add(extractOpRefactoring);
					sourceOperationMapWithExtractedOperationAsKey.put(extractedOperation, listExtractOperation);
				}
			}
		}
		List<ExtractOperationRefactoring> allRemoveDuplicationExtractRefactorings = new ArrayList<ExtractOperationRefactoring>();
		for (UMLOperation extractOperation : sourceOperationMapWithExtractedOperationAsKey.keySet()) {
			List<ExtractOperationRefactoring> listSourceOperations = sourceOperationMapWithExtractedOperationAsKey
					.get(extractOperation);
			/*
			 * DETECTION RULE: if multiple source operations(Or the Extract Refactorings
			 * that contain them) have the same extractedOperation the extract operations
			 * motivations is Remove Duplication
			 */
			if (listSourceOperations.size() > 1) {
				if (isRemoveDuplicationOneLine(listSourceOperations)) {
					oneLineRemoveDuplications.addAll(listSourceOperations);
					continue;
				}
				if (isExtractMethodRefactoringsEqual(listSourceOperations)) {
					removeDuplicationFromSingleMethodRefactorings.addAll(listSourceOperations);
				} else {
					for (ExtractOperationRefactoring ref : listSourceOperations) {
						setMotivationFlag(MotivationFlag.EM_DISTINCT, ref);
					}
				}
				for (ExtractOperationRefactoring extractOp : listSourceOperations) {
					setMotivationFlag(MotivationFlag.EM_SAME_EXTRACTED_OPERATIONS, extractOp);
					setRefactoringMotivation(MotivationType.EM_REMOVE_DUPLICATION, extractOp);
					if (isMotivationDetected(extractOp, MotivationType.EM_DECOMPOSE_TO_IMPROVE_READABILITY)
							&& (!decomposeToImproveReadabilityFromSingleMethodRefactorings.contains(extractOp)
									|| decomposeToImproveReadabilityFromSingleMethodByHavingCallToExtractedMethodInReturn
											.contains(extractOp))) {
						removeRefactoringMotivation(MotivationType.EM_DECOMPOSE_TO_IMPROVE_READABILITY, extractOp);
					}
					allRemoveDuplicationExtractRefactorings.add(extractOp);
				}
			}
		}
		if (allRemoveDuplicationExtractRefactorings.size() >= 2) {
			return true;
		}
		return false;
	}

	private boolean isRemoveDuplicationOneLine(List<ExtractOperationRefactoring> extractOperations) {
		for (ExtractOperationRefactoring extractOperation : extractOperations) {
			int em_mapping_size = extractOperation.getBodyMapper().getMappings().size();
			setMotivationFlag(MotivationFlag.EM_MAPPING_SIZE.setMotivationValue(em_mapping_size), extractOperation);
		}
		int em_num_methods_used_in_duplication_removal = extractOperations.size();
		for (ExtractOperationRefactoring extractOperation : extractOperations) {
			setMotivationFlag(MotivationFlag.EM_NUM_METHODS_USED_IN_DUPLICATION_REMOVAL
					.setMotivationValue(em_num_methods_used_in_duplication_removal), extractOperation);
		}
		if (extractOperations.size() < 3) {
			for (ExtractOperationRefactoring extractOperation : extractOperations) {
				Set<AbstractCodeMapping> abstractCodeMappings = extractOperation.getBodyMapper().getMappings();
				if (abstractCodeMappings.size() != 1) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	private List<ExtractOperationRefactoring> getRepetativeExtractOperations(
			List<ExtractOperationRefactoring> listExtractOperations) {
		List<ExtractOperationRefactoring> listRepetativeExtractOperations = new ArrayList<ExtractOperationRefactoring>();
		for (int i = 0; i < listExtractOperations.size(); i++) {
			for (int j = i + 1; j < listExtractOperations.size(); j++) {
				if (listExtractOperations.get(i).toString().equals(listExtractOperations.get(j).toString())) {
					listRepetativeExtractOperations.add(listExtractOperations.get(i));
				}
			}
		}
		return listRepetativeExtractOperations;
	}

	private boolean isMotivationDetected(Refactoring ref, MotivationType type) {

		List<MotivationType> listMotivations = mapRefactoringMotivations.get(ref);
		if (listMotivations == null) {
			return false;
		} else {
			if (listMotivations.contains(type)) {
				return true;
			}
		}
		return false;
	}

	private void setRefactoringMotivation(MotivationType motivationType, Refactoring ref) {
		if (mapRefactoringMotivations.containsKey(ref) && motivationType != MotivationType.NONE) {
			mapRefactoringMotivations.get(ref).add(motivationType);
		} else {
			List<MotivationType> listMotivations = new ArrayList<MotivationType>();
			listMotivations.add(motivationType);
			mapRefactoringMotivations.put(ref, listMotivations);
		}
	}

	private void setMotivationFlag(MotivationFlag motivationFlag, Refactoring ref) {
		if (mapMotivationFlags.containsKey(ref)) {
			mapMotivationFlags.get(ref).add(motivationFlag);
		} else {
			List<MotivationFlag> listMotivationFlags = new ArrayList<MotivationFlag>();
			listMotivationFlags.add(motivationFlag);
			mapMotivationFlags.put(ref, listMotivationFlags);
		}
	}

	private boolean removeRefactoringMotivation(MotivationType motivationType, Refactoring ref) {
		if (mapRefactoringMotivations.containsKey(ref)) {
			if (!mapRefactoringMotivations.get(ref).isEmpty()
					&& mapRefactoringMotivations.get(ref).contains(motivationType)) {
				if (mapRefactoringMotivations.get(ref).remove(motivationType)) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean removeMotivationFlag(MotivationFlag motivationFlag, Refactoring ref) {
		if (mapMotivationFlags.containsKey(ref)) {
			if (!mapMotivationFlags.get(ref).isEmpty() && mapMotivationFlags.get(ref).contains(motivationFlag)) {
				if (mapMotivationFlags.get(ref).remove(motivationFlag)) {
					return true;
				}
			}
		}
		return false;
	}

	public void classifyRefactoringsByType(List<Refactoring> refactorings) {
		for (Refactoring refactoring : refactorings) {
			RefactoringType type = refactoring.getRefactoringType();
			if (type.equals(RefactoringType.EXTRACT_AND_MOVE_OPERATION)) {
				type = RefactoringType.EXTRACT_OPERATION;
			}
			if (mapClassifiedRefactorings.containsKey(type)) {
				mapClassifiedRefactorings.get(type).add(refactoring);
			} else {
				List<Refactoring> listRefactoring = new ArrayList<Refactoring>();
				listRefactoring.add(refactoring);
				mapClassifiedRefactorings.put(type, listRefactoring);
			}
		}
	}

	private void printDetectedRefactoringMotivations() {
		// TODO Auto-generated method stub
	}

}