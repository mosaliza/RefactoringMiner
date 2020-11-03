package gr.uom.java.xmi.diff;

public enum MotivationFlag {
	EM_INVOCATION_IN_REMOVE_DUPLICATION("Extract Method Invocation is in the SOAE of RemoveDuplication Refactoirng"),	
	EM_INVOCATION_IN_TEST_OPERATION("Extract Method Invocation is in test Opeataion"),
	SOAE_IS_TEST_OPERATION("Source Operation After Extraction is a Test Operation"),
	EM_INVOCATION_EQUAL_MAPPING("Equal Mapping of invocations in SOBE AND SOAE"),
	EM_EQUAL_SINATURE_INVOCATIONS("Invocations to an operation with an equal signature to Extracted Mewthod"),
	EM_NESTED_INVOCATIONS("Invocation to an Extracted Method from within a Nested Extrcted Method"),
	EM_SOAE_INVOCATIONS("Invocations to the Extracted Method from Source Opereration After Extraction"),
	EM_NONE_SOAE_INVOCATIONS("Invocations to the Extracted Method from None_SOAE methods"),
	
	EM_SOAE_EQUAL_PARAMETER_TYPES("Extracted Method has Euqal Paramater Types with Source Operation After Extraction"),
	SOAE_IS_DELEGATE_TO_EM("Extracted Method is a delegate to the Source Operation After Extraction"),
	EM_HAS_ADDED_PARAMETERS("Extracted Operation has more parameters than the Source Operation"),
	
	EM_DECOMPOS_SINGLE_METHOD("Single Method Decomposition to Improve Readability"),
	EM_DECOMPOSE_MULTIPLE_METHODS("Multiple Method Decomposition to Improve Readability"),
	EM_WITH_SAME_SOURCE_OPERATION("Extract Methods Have same source operation"),
	EM_DISTINCT("Extract Methods are Distinct"),
	EM_INVOCATION_EDIT_DISTANCE_THRESHOLD_SM("Extract Method Invocation Edit Distance in Single Decomposition"),
	EM_INVOCATION_EDIT_DISTANCE_THRESHOLD_MM("Extract Method Invocation Edit Distance in Multiple Decomposition"),
	EM_ALL_SOURCE_OPERATION_NODES_MAPPED("All the nodes in the source operation are mapped to the Extracted Operation"),
	EM_GETTER_SETTER("Extract Method is a Getter/Setter Method"),
	EM_MAPPING_COMPOSITE_NODES("Number of Composite Nodes in the Mapping"),
	EM_COMPOSITE_EXPRESSION_INVOCATIONS("Invocations to the Extracted Method from the Composite expression"),
	EM_COMPOSITE_EXPRESSION_CALLVAR("callVar to the Extract Method exists in the Composite expression"),
	EM_RETURN_STATEMENT_INVOCATIONS("Invocations to the Extracted Method from the Return Statement"),

	EM_MAPPING_FRAGMENT2_TERNARY("Ternary operator is used in the fragment 2 of the Extract Method Mapping"),
	SOAE_NOT_MAPPED_T2_UNFILTERED("Unfiltered notMapped T2 nodes in the Source Operation After Extraction"),
	EM_NOTMAPPED_T2_UNFILTERED("Unfiltered notMapped T2 nodes in the Extracted Operation"),
	SOAE_NOTMAPPED_T2_FILTERED("Filtered notMapped T2 nodes in the Source Operation After Extraction"),
	EM_NOTMAPPED_T2_FILTERED("Filtered notMapped T2 nodes in the Extracted Operation"),
	EM_T2_IN_T1("NotMapped T2 Nodes exist in NotMapped T2 Nodes in the Extracted Method"),
	SOAE_T2_IN_T1("NotMapped T2 Nodes exist in NotMapped T2 Nodes in the Source Operation After Extraction"),
	EM_T2_IN_MAPPING("T2 NotMapped Nodes are in the Extract Method(child) mapping"),
	SOAE_T2_IN_MAPPING("T2 NotMapped Nodes are in the Source Operation After Extraction mappings"),
	EM_T2_IE_IN_EM_PARAMETERS("Extracted Method T2 NotMapped Nodes Invocation Expressions are in the Extracted Method Parameters"),
	SOAE_T2_IE_IN_EM_PARAMETERS("Source Operation After Extraction T2 NotMapped Nodes Invocation Expressions are in the Extracted Method Parameters"),
	EM_T2_NEUTRAL("Extracted Method T2 NotMapped Nodes are Neutral"),
	SOAE_T2_NEUTRAL("Source Operation After Extraction T2 NotMapped Nodes are Neutral"),
	SOAE_T2_DV_IN_EM_PARAMETERS("Source Operation After Extraction Declared Variable are in the Extracted Method Parameters"),
	
	EM_SAME_EXTRACTED_OPERATIONS("Extract Method operations with the same extracted operations"),
	
	//EM_SOAE_EQUAL_PARAMETER_TYPES, SOAE_IS_DELEGATE_TO_EM are extracted for Backward compatibility as well.
	EM_SOAE_EQUAL_NAMES("Extracted Method has equal names with the Source Operation After Extraction"),
	SOAE_DEPRECATED("Source Operation After Extraction is Deprecated"),
	SOAE_PRIVATE("Source Operation After Extraction is Private"),
	SOAE_PROTECTED("Source Operation After Extraction is Protected"),
	
	//EM_INVOCATION_IN_TEST_OPERATION is extracted for Improve Testability as well.
	EM_TEST_INVOCATION_CLASS_EQUAL_TO_EM_CLASS("Extract Method test invocation class is equal to the Extract Method class"),
	EM_TEST_INVOCATION_IN_ADDED_NODE("Extract Method test invocation is in added nodes"),
	
	EM_EQUAL_OPERATION_SIGNATURE_IN_SUBTYPE("Equal Operation Signature in SubType of the Extracted Method exists"),
	EM_OVERRIDING_KEYWORD_IN_COMMENT("Extract Method has overriding keywords in its comment"),
	
	SOBE_RECURSIVE("Source Operation Before Extraction is recursive"),
	EM_RECURSIVE("Extracted Method is recursive"),
	
	EM_HAS_RETURN_STATEMENTS("Extract Method has return statements"),
	EM_RETURN_STATEMENT_NEW_KEYWORDS("New keywords in the return statement"),
	EM_RETURN_EQUAL_NEW_RETURN("Extract Method return type equals the object creation type in the return statement"),
	EM_VARS_FACTORY_METHOD_RELATED("Extract Method Variables are related to object creation for factory method"),
	SOBE_FACTORY_METHOD("Source Operation Before Extraction is Factory Method"),

	SOAE_ANONYMOUS_CLASS_RUNNABLE_EM_INVOCATION("Source Operation After Extraction has an anonymous class and runnable type that has invocation to Extracted Method");
	
	private String description;
	private int motivationValue;
	
    private MotivationFlag(String description) {
		this.description = description;
	}
    private MotivationFlag(int motivationVal) {
		this.motivationValue = motivationVal;
	}

	public int getMotivationValue() {
		return motivationValue;
	}
	public MotivationFlag  setMotivationValue(int motivationValue) {
		 this.motivationValue = motivationValue;
		 return this;
	}
	
	public String getDescription() {
		return description;
	}
}
