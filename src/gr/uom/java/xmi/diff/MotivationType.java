package gr.uom.java.xmi.diff;

import org.refactoringminer.api.RefactoringType;

public enum MotivationType {
	
	
	EM_REUSABLE_METHOD("EM: Extract reusable method"),
	EM_INTRODUCE_ALTERNATIVE_SIGNATURE("EM: Introduce alternative method signature"),
	EM_DECOMPOSE_TO_IMPROVE_READABILITY("EM: Decompose method to improve readability"),
	EM_FACILITATE_EXTENSION("EM: Facilitate extension"),
	EM_REMOVE_DUPLICATION("EM: Remove duplication"),
	EM_REPLACE_METHOD_PRESERVING_BACKWARD_COMPATIBILITY("EM: Replace method keeping backward compatibility"),
	EM_IMPROVE_TESTABILITY("EM: Improve testability"),
	EM_ENABLE_OVERRIDING("EM: Enable overriding"),
	EM_ENABLE_RECURSION("EM: Enable recursion"),
	EM_INTRODUCE_FACTORY_METHOD("EM: Introduce factory method"),
	EM_INTRODUCE_ASYNC_OPERATION("EM: Introduce async operation"),
	
	MC_MOVE_CLASS_TO_APPROPRIATE_CONTAINER("MC: Move class to appropriate container"),
	MC_INTRODUCE_SUB_PACKAGE("MC: Introduce sub-package"),
	MC_CONVERT_TO_TOP_LEVEL_CONTAINER("MC: Convert to top-level container"),
	MC_REMOVE_INNER_CLASS_FROM_DEPRECATED_CONTAINER("MC: Remove inner classes from deprecated container"),
	MC_REMOVE_FROM_PUBLIC_API("MC: Remove from public API"),
	MC_CONVERT_TO_INNER_CLASS("MC: Convert to inner class"),
	MC_ELIMINATE_DEPENDENCIES("MC: Isolate dependencies"),
	MC_ELIMINATE_REDUNDANT_SUB_PACKAGE("MC: Eliminate redundant sub-package"),
	MC_BACKWARD_COMPATIBILITY("MC: Backward compatibility"),
	
	MA_MOVE_ATTRIBUTE_TO_APPROPRIATE_CLASS("MA: Move attribute to appropriate class"),
	MA_REMOVE_DUPLICATION("MA: Remove duplication"),
	
	RP_IMPROVE_PACKAGE_NAME("RP: Improve package name"),
	RP_ENFORCE_NAMING_CONSISTENCY("RP: Enforce naming consistency"),
	RP_MOVE_PACKAGE_TO_APPROPRIATE_CONTAINER("RP: Move Package to appropriate container"),
	
	MM_MOVE_METHOD_TO_APPROPRIATE_CLASS("MM: Move method to appropriate class"),
	MM_MOVE_METHOD_TO_ENABLE_REUSE("MM: Move method to enable reuse"),
	MM_ELIMINATE_DEPENDENCIES("MM: Isolate dependencies"),
	MM_REMOVE_DUPLICATION("MM: Remove duplication"),
	MM_ENABLE_OVERRIDING("MM: Enable overriding"),
	
	IM_ELIMINATE_UNNECESSARY_METHOD("IM: Eliminate unnecessary method"),
	IM_CALLER_BECOMES_TRIVIAL("IM: Caller becomes trivial"),
	IM_IMPROVE_READABILITY("IM: Improve readability"),
	
	ES_EXTRACT_COMMON_STATE_OR_BEHAVIOUR("ES: Extract common state/behavior"),
	ES_ELIMINATE_DEPENDENCIES("ES: Isolate dependencies"),
	ES_DECOMPOSE_CLASS("ES: Decompose class"),
	
	PUM_MOVE_UP_COMMON_METHODS("UM: Move up common methods"),
	
	PUA_MOVE_UP_COMMON_ATTRIBUTES("UA: Move up common attributes"),
	
	EI_FACILITATE_EXTENSION("EI: Facilitate extension"),
	EI_ENABLE_DEPENDENCY_INJECTION("EI: Enable dependency injection"),
	EI_ELIMINATE_DEPENDENCIES("EI: Avoid dependency"),
	
	PDA_SPECIALIZED_IMPLEMENTATION("DA: Specialized implementation"),
	PDA_ELIMINATE_DEPENDENCIES("DA: Isolate dependencies"),
	
	PDM_SPECIALIZED_IMPLEMENTATION("DM: Specialized implementation");
	
	private String description;
	
    private MotivationType(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}
	
	public static MotivationType extractFromDescription(String motivationDescription) {
		for (MotivationType motivationType : MotivationType.values()) {
			if (motivationDescription.startsWith(motivationType.getDescription())) {
				return motivationType;
			}
		}
		throw new RuntimeException("Unknown Motivation type: " + motivationDescription);
	}
}
