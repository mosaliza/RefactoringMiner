import java.util.List;

import org.refactoringminer.api.Refactoring;

import gr.uom.java.xmi.diff.MotivationType;

public class ExtractMethodMotivation {
	private Refactoring refactoring;
	private boolean reusableMethod;
	public boolean isReusableMethod() {
		return reusableMethod;
	}
	public boolean isIntroduceAlternativeMethodSingature() {
		return introduceAlternativeMethodSingature;
	}
	public boolean isDecomposeToImproveReadability() {
		return decomposeToImproveReadability;
	}
	public boolean isFacilitateExtension() {
		return facilitateExtension;
	}
	public boolean isRemoveDuplication() {
		return removeDuplication;
	}
	public boolean isReplaceMethodPreservingBackwardCompatibility() {
		return replaceMethodPreservingBackwardCompatibility;
	}
	public boolean isImproveTestability() {
		return improveTestability;
	}
	public boolean isEnableOverriding() {
		return enableOverriding;
	}
	public boolean isEnableRecursion() {
		return enableRecursion;
	}
	public boolean isIntroduceFactoryMethod() {
		return introduceFactoryMethod;
	}
	public boolean isIntroduceAsyncMethod() {
		return introduceAsyncMethod;
	}

	private boolean introduceAlternativeMethodSingature;
	private boolean decomposeToImproveReadability;
	private boolean facilitateExtension;
	private boolean removeDuplication;
	private boolean replaceMethodPreservingBackwardCompatibility;
	private boolean improveTestability;
	private boolean enableOverriding;
	private boolean enableRecursion;
	private boolean introduceFactoryMethod;
	private boolean introduceAsyncMethod;
	
	public ExtractMethodMotivation(Refactoring refactoring) {
		this.setRefactoring(refactoring);
		this.setReusableMethod(false);
		this.setIntroduceAlternativeMethodSingature(false);
		this.setDecomposeToImproveReadability(false);
		this.setFacilitateExtension(false);
		this.setRemoveDuplication(false);
		this.setReplaceMethodPreservingBackwardCompatibility(false);
		this.setImproveTestability(false);
		this.setEnableOverriding(false);
		this.setEnableRecursion(false);
		this.setIntroduceFactoryMethod(false);
		this.setIntroduceAsyncMethod(false);
	}
	public Refactoring getRefactoring() {
		return refactoring;
	}
	public void setRefactoring(Refactoring refactoring) {
		this.refactoring = refactoring;
	}
	public void setReusableMethod(boolean reusableMethod) {
		this.reusableMethod = reusableMethod;
	}
	public void setIntroduceAlternativeMethodSingature(boolean introduceAlternativeMethodSingature) {
		this.introduceAlternativeMethodSingature = introduceAlternativeMethodSingature;
	}
	public void setDecomposeToImproveReadability(boolean decomposeToImproveReadability) {
		this.decomposeToImproveReadability = decomposeToImproveReadability;
	}
	public void setFacilitateExtension(boolean facilitateExtension) {
		this.facilitateExtension = facilitateExtension;
	}
	public void setRemoveDuplication(boolean removeDuplication) {
		this.removeDuplication = removeDuplication;
	}
	public void setReplaceMethodPreservingBackwardCompatibility(boolean replaceMethodPreservingBackwardCompatibility) {
		this.replaceMethodPreservingBackwardCompatibility = replaceMethodPreservingBackwardCompatibility;
	}
	public void setImproveTestability(boolean improveTestability) {
		this.improveTestability = improveTestability;
	}
	public void setEnableOverriding(boolean enableOverriding) {
		this.enableOverriding = enableOverriding;
	}
	public void setEnableRecursion(boolean enableRecursion) {
		this.enableRecursion = enableRecursion;
	}
	public void setIntroduceFactoryMethod(boolean introduceFactoryMethod) {
		this.introduceFactoryMethod = introduceFactoryMethod;
	}
	public void setIntroduceAsyncMethod(boolean introduceAsyncMethod) {
		this.introduceAsyncMethod = introduceAsyncMethod;
	}	
	
	public static String EM_HEADER =  new String("Refactoring Description|"
			+ "EM_REUSABLE_METHOD|"
			+ "EM_INTRODUCE_ALTERNATIVE_SIGNATURE|"
			+ "EM_DECOMPOSE_TO_IMPROVE_READABILITY|"
			+ "EM_FACILITATE_EXTENSION|"
			+ "EM_REMOVE_DUPLICATION|"
			+ "EM_REPLACE_METHOD_PRESERVING_BACKWARD_COMPATIBILITY|"
			+ "EM_IMPROVE_TESTABILITY|"
			+ "EM_ENABLE_OVERRIDING|"
			+ "EM_ENABLE_RECURSIO|"
			+ "EM_INTRODUCE_FACTORY_METHOD|"
			+ "EM_INTRODUCE_ASYNC_OPERATION") ;

	public void addMotivations(List<MotivationType> motivations) {
		for(MotivationType motivation : motivations) {
			this.addMotivation(motivation);
		}
	}
	private void addMotivation(MotivationType motivation) {
		switch (motivation) {
		case EM_REUSABLE_METHOD:
			reusableMethod = true;
			break;
		case EM_INTRODUCE_ALTERNATIVE_SIGNATURE:
			introduceAlternativeMethodSingature = true;
			break;
		case EM_DECOMPOSE_TO_IMPROVE_READABILITY:
			decomposeToImproveReadability = true;
			break;
		case EM_FACILITATE_EXTENSION:
			facilitateExtension = true;
			break;
		case EM_REMOVE_DUPLICATION:
			removeDuplication = true;
			break;
		case EM_REPLACE_METHOD_PRESERVING_BACKWARD_COMPATIBILITY:
			replaceMethodPreservingBackwardCompatibility = true;
			break;
		case EM_IMPROVE_TESTABILITY:
			improveTestability = true;
			break;
		case EM_ENABLE_OVERRIDING:
			enableOverriding = true;
			break;
		case EM_ENABLE_RECURSION:
			enableRecursion = true;
			break;
		case EM_INTRODUCE_FACTORY_METHOD:
			introduceFactoryMethod = true;
			break;
		case EM_INTRODUCE_ASYNC_OPERATION:
			introduceAsyncMethod = true;
			break;	
		default:
			break;
		}
	}
}
