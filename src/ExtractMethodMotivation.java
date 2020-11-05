import com.vladmihalcea.hibernate.type.json.JsonStringType;

import gr.uom.java.xmi.diff.MotivationFlag;
import gr.uom.java.xmi.diff.MotivationType;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;

import java.util.Date;
import java.util.Set;

@Entity
@Table(name = "motivations")
@TypeDef(
        name = "json",
        typeClass = JsonStringType.class
)
public class ExtractMethodMotivation {
    public final static String EM_HEADER = "Refactoring Description|"
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
            + "EM_INTRODUCE_ASYNC_OPERATION";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "repository")
    private String repository;

    @Column(name = "commit_id")
    private String commitId;
    
	@Column(name = "commit_message")
    private String commitMessage;
    
    @Column(name = "committer_name")
    private String commiterName;
    
    @Column(name = "commit_time")
    private int commitTime;
    
    @Column(name = "committer_date")
    private Date commiterDate;
    
	@Column(name = "commit_author")
    private String commitAuthor;
    
    @Column(name = "author_date")
    private Date commitAuthorDate;
    
	@Column(name = "refactoring_type")
    private String refactoringType;


	@Column(name = "refactoring_desc", columnDefinition = "LONGTEXT")
    private String refactoringDesc;

    @Type(type = "json")
    @Column(name = "refactoring_json", columnDefinition = "json")
    private String refactoringJSON;

    @Column(name = "reusable_method")
    private boolean reusableMethod;

    @Column(name = "introduce_alternative_method_signature")
    private boolean introduceAlternativeMethodSignature;

    @Column(name = "decompose_to_improve_readability")
    private boolean decomposeToImproveReadability;

    @Column(name = "facilitate_extension")
    private boolean facilitateExtension;

    @Column(name = "remove_duplication")
    private boolean removeDuplication;

    @Column(name = "replace_method_preserving_backward_compatibility")
    private boolean replaceMethodPreservingBackwardCompatibility;

    @Column(name = "improve_testability")
    private boolean improveTestability;

    @Column(name = "enable_overriding")
    private boolean enableOverriding;

    @Column(name = "enable_recursion")
    private boolean enableRecursion;

    @Column(name = "introduce_factory_method")
    private boolean introduceFactoryMethod;

    @Column(name = "introduce_async_method")
    private boolean introduceAsyncMethod;
    
    //Reuse Features
    @Column(name = "em_invocation_in_removeDuplication")
    private boolean em_invocation_in_removeDuplication = false;
    
	@Column(name = "em_invocation_in_testOperation")
    private boolean em_invocation_in_testOperation = false;
    
    @Column(name = "soae_is_testOperation")
    private boolean soae_is_testOperation = false;
    
    @Column(name = "em_invocation_equal_mapping")
    private boolean em_invocation_equal_mapping = false;
    
    @Column(name = "em_equalSignature_invocations")
    private boolean em_equalSignature_invocations = false;
    
    @Column(name = "em_nested_invocations")
    private boolean em_nested_invocations = false;
    
    @Column(name = "em_soae_invocations")
    private int em_soae_invocations;
   
    @Column(name = "em_not_soae_invocations")
    private int em_not_soae_invocations;
   
    //Introduce Alternative Method Signature Features
    @Column(name = "em_has_added_parameter")
    private boolean emHasAddedParameter = false;
    
    @Column(name = "em_soae_equal_parameter_types")
    private boolean em_soae_equal_parameter_types = false;

    @Column(name = "soae_is_delegate_to_em")
    private boolean soae_is_delegate_to_em = false;
    
    @Column(name = "soae_is_all_temp_variables")
    private boolean soae_is_all_temp_variables = false;
    
	//Decompose Method to Improve Readability
    @Column(name = "em_decompose_single_method")
    private boolean em_decompose_single_method = false;
    
    @Column(name = "em_decompose_multiple_methods")
    private boolean em_decompose_multiple_methods = false;
    
    @Column(name = "em_with_same_source_operation")
    private boolean em_with_same_source_operation = false;
    
    @Column(name = "em_distinct")
    private boolean em_distinct = false;
    
    @Column(name = "em_invocation_edit_distance_threshold_SM")
    private boolean em_invocation_edit_distance_threshold_SM = false;
    
    @Column(name = "em_invocation_edit_distance_threshold_MM")
    private boolean em_invocation_edit_distance_threshold_MM = false;
    
    @Column(name = "em_all_source_operation_nodes_mapped")
    private boolean em_all_source_operation_nodes_mapped = false;
    
    @Column(name = "em_getter_setter")
    private boolean em_getter_setter = false;
    
    @Column(name = "em_mapping_composite_nodes")
    private int em_mapping_composite_nodes;
    
    @Column(name = "em_composite_expression_invocations")
    private int em_composite_expression_invocations;
    
    @Column(name = "em_composite_expression_callVar")
    private boolean em_composite_expression_callVar = false;
    
    @Column(name = "em_return_statement_invocations")
    private int em_return_statement_invocations;
    
    //Facilitate Extension
    @Column(name = "em_mapping_fragment2_ternary")
    private boolean em_mapping_fragment2_ternary;
    
    @Column(name = "soae_NotMapped_T2_unfiltered")
    private int soae_NotMapped_T2_unfiltered;
    
	@Column(name = "em_NotMapped_T2_filtered")
    private int em_NotMapped_T2_filtered;
    
    @Column(name = "em_NotMapped_T2_unfiltered")
    private int em_NotMapped_T2_unfiltered;
    
    @Column(name = "soae_NotMapped_T2_filtered")
    private int soae_NotMapped_T2_filtered;
    
    @Column(name = "em_T2_in_T1")
    private boolean em_T2_in_T1;
    
    @Column(name = "soae_T2_in_T1")
    private boolean soae_T2_in_T1;
    
    @Column(name = "em_T2_in_mapping")
    private boolean em_T2_in_mapping;
    
    @Column(name = "soae_T2_in_mapping")
    private boolean soae_T2_in_mapping;
    
    @Column(name = "em_T2_IE_in_em_parameters")
    private boolean em_T2_IE_in_em_parameters;
    
    @Column(name = "soae_T2_IE_in_em_parameters")
    private boolean soae_T2_IE_in_em_parameters;
    
    @Column(name = "em_T2_Neutral")
    private boolean em_T2_Neutral;
    
    @Column(name = "soae_T2_Neutral")
    private boolean soae_T2_Neutral;
    
    @Column(name = "soae_T2_DV_in_em_parameters")
    private boolean soae_T2_DV_in_em_parameters;
    
    @Column(name = "em_T2_em_invocations")
    private boolean em_T2_em_invocations;
    
    @Column(name = "soae_T2_em_invocations")
    private boolean soae_T2_em_invocations;

    
    //Remove Duplication
    @Column(name = "em_same_extracted_operations")
    private boolean em_same_extracted_operations;
    
    @Column(name = "em_mapping_size")
    private int em_mapping_size;
    
    @Column(name = "em_num_methods_used_in_duplication_removal")
    private int em_num_methods_used_in_duplication_removal;
    
    
	//Replace Method Preserving Backwards compatibility
    @Column(name = "em_soae_equal_names")
    private boolean em_soae_equal_names;
    
    @Column(name = "soae_deprecated")
    private boolean soae_deprecated;
    
    @Column(name = "soae_private")
    private boolean soae_private;
    
    @Column(name = "soae_protected")
    private boolean soae_protected;
    
    //Improve Testability
    @Column(name = "em_test_invocation_class_equal_to_emClass")
    private boolean em_test_invocation_class_equal_to_emClass;
    
    @Column(name = "em_test_invocation_in_added_node")
    private boolean em_test_invocation_in_added_node;
    
    //Enable Overriding
    @Column(name = "em_equal_operation_signature_in_subtype")
    private boolean em_equal_operation_signature_in_subtype;
    
    @Column(name = "em_overriding_keyword_in_comment")
    private boolean em_overriding_keyword_in_comment;
    
    
    //Enable Recursion
    
    @Column(name = "sobe_recursive")
    private boolean sobe_recursive;
    
    @Column(name = "em_recursive")
    private boolean em_recursive;
    
    //Introduce Factory Method
    @Column(name = "em_has_return_statement")
    private boolean em_has_return_statement;
    
    @Column(name = "em_return_statement_new_keywords")
    private int em_return_statement_new_keywords;
    
    @Column(name = "em_return_equal_new_return")
    private boolean em_return_equal_new_return;
    
    @Column(name = "em_object_creation_variable_returned")
    private boolean em_object_creation_variable_returned;
    
    @Column(name = "em_vars_factory_method_related")
    private boolean em_vars_factory_method_related;
    
    @Column(name = "sobe_factory_method")
    private boolean sobe_factory_method;
    
    @Column(name = "soae_anonymous_class_runnable_em_invocation")
    private boolean soae_anonymous_class_runnable_em_invocation;
    
    @Column(name = "soae_statements_contain_runnable_type")
    private boolean soae_statements_contain_runnable_type;
                 
	public ExtractMethodMotivation() {
    }

    public ExtractMethodMotivation(Set<MotivationType> motivations , Set<MotivationFlag> motivationFlags) {
        for (MotivationType motivation : motivations) {
            this.addMotivation(motivation);
        }
        for(MotivationFlag motivationFlag : motivationFlags) {
        	this.addMotivationFlag(motivationFlag);
        }
    }
    
	public int getEm_num_methods_used_in_duplication_removal() {
		return em_num_methods_used_in_duplication_removal;
	}

	public void setEm_num_methods_used_in_duplication_removal(int em_num_methods_used_in_duplication_removal) {
		this.em_num_methods_used_in_duplication_removal = em_num_methods_used_in_duplication_removal;
	}

	public boolean isSoae_statements_contain_runnable_type() {
		return soae_statements_contain_runnable_type;
	}

	public void setSoae_statements_contain_runnable_type(boolean soae_statements_contain_runnable_type) {
		this.soae_statements_contain_runnable_type = soae_statements_contain_runnable_type;
	}
	
    public boolean isEm_T2_em_invocations() {
		return em_T2_em_invocations;
	}

	public void setEm_T2_em_invocations(boolean em_T2_em_invocations) {
		this.em_T2_em_invocations = em_T2_em_invocations;
	}

	public boolean isSoae_T2_em_invocations() {
		return soae_T2_em_invocations;
	}

	public void setSoae_T2_em_invocations(boolean soae_T2_em_invocations) {
		this.soae_T2_em_invocations = soae_T2_em_invocations;
	}

	public int getEm_mapping_size() {
		return em_mapping_size;
	}

	public void setEm_mapping_size(int em_mapping_size) {
		this.em_mapping_size = em_mapping_size;
	}
	public boolean isEm_object_creation_variable_returned() {
		return em_object_creation_variable_returned;
	}

	public void setEm_object_creation_variable_returned(boolean em_object_creation_variable_returned) {
		this.em_object_creation_variable_returned = em_object_creation_variable_returned;
	}
    
   public boolean isSoae_is_all_temp_variables() {
		return soae_is_all_temp_variables;
	}

	public void setSoae_is_all_temp_variables(boolean soae_is_all_temp_variables) {
		this.soae_is_all_temp_variables = soae_is_all_temp_variables;
	}

	public int getEm_return_statement_new_keywords() {
		return em_return_statement_new_keywords;
	}
	
	public boolean isEm_has_return_statement() {
		return em_has_return_statement;
	}

	public void setEm_has_return_statement(boolean em_has_return_statement) {
		this.em_has_return_statement = em_has_return_statement;
	}

	public int isEm_return_statement_new_keywords() {
		return em_return_statement_new_keywords;
	}

	public void setEm_return_statement_new_keywords(int em_return_statement_new_keywords) {
		this.em_return_statement_new_keywords = em_return_statement_new_keywords;
	}

	public boolean isEm_return_equal_new_return() {
		return em_return_equal_new_return;
	}

	public void setEm_return_equal_new_return(boolean em_return_equal_new_return) {
		this.em_return_equal_new_return = em_return_equal_new_return;
	}

	public boolean isEm_vars_factory_method_related() {
		return em_vars_factory_method_related;
	}

	public void setEm_vars_factory_method_related(boolean em_vars_factory_method_related) {
		this.em_vars_factory_method_related = em_vars_factory_method_related;
	}

	public boolean isSobe_factory_method() {
		return sobe_factory_method;
	}

	public void setSobe_factory_method(boolean sobe_factory_method) {
		this.sobe_factory_method = sobe_factory_method;
	}

	public boolean isSoae_anonymous_class_runnable_em_invocation() {
		return soae_anonymous_class_runnable_em_invocation;
	}

	public void setSoae_anonymous_class_runnable_em_invocation(boolean soae_anonymous_class_runnable_em_invocation) {
		this.soae_anonymous_class_runnable_em_invocation = soae_anonymous_class_runnable_em_invocation;
	}
	public boolean isSobe_recursive() {
		return sobe_recursive;
	}

	public void setSobe_recursive(boolean sobe_recursive) {
		this.sobe_recursive = sobe_recursive;
	}

	public boolean isEm_recursive() {
		return em_recursive;
	}

	public void setEm_recursive(boolean em_recursive) {
		this.em_recursive = em_recursive;
	}

	public boolean isEm_equal_operation_signature_in_subtype() {
		return em_equal_operation_signature_in_subtype;
	}

	public void setEm_equal_operation_signature_in_subtype(boolean em_equal_operation_signature_in_subtype) {
		this.em_equal_operation_signature_in_subtype = em_equal_operation_signature_in_subtype;
	}

	public boolean isEm_overriding_keyword_in_comment() {
		return em_overriding_keyword_in_comment;
	}

	public void setEm_overriding_keyword_in_comment(boolean em_overriding_keyword_in_comment) {
		this.em_overriding_keyword_in_comment = em_overriding_keyword_in_comment;
	}
    
	public boolean isEm_test_invocation_class_equal_to_emClass() {
		return em_test_invocation_class_equal_to_emClass;
	}

	public void setEm_test_invocation_class_equal_to_emClass(boolean em_test_invocation_class_equal_to_emClass) {
		this.em_test_invocation_class_equal_to_emClass = em_test_invocation_class_equal_to_emClass;
	}

	public boolean isEm_test_invocation_in_added_node() {
		return em_test_invocation_in_added_node;
	}

	public void setEm_test_invocation_in_added_node(boolean em_test_invocation_in_added_node) {
		this.em_test_invocation_in_added_node = em_test_invocation_in_added_node;
	}
	
	public boolean isEm_soae_equal_names() {
		return em_soae_equal_names;
	}

	public void setEm_soae_equal_names(boolean em_soae_equal_names) {
		this.em_soae_equal_names = em_soae_equal_names;
	}

	public boolean isSoae_deprecated() {
		return soae_deprecated;
	}

	public void setSoae_deprecated(boolean soae_deprecated) {
		this.soae_deprecated = soae_deprecated;
	}

	public boolean isSoae_private() {
		return soae_private;
	}

	public void setSoae_private(boolean soae_private) {
		this.soae_private = soae_private;
	}

	public boolean isSoae_protected() {
		return soae_protected;
	}

	public void setSoae_protected(boolean soae_protected) {
		this.soae_protected = soae_protected;
	}
	
    public boolean isEm_same_extracted_operations() {
		return em_same_extracted_operations;
	}

	public void setEm_same_extracted_operations(boolean em_same_extracted_operations) {
		this.em_same_extracted_operations = em_same_extracted_operations;
	}
        
    public boolean getEm_mapping_fragment2_ternary() {
		return em_mapping_fragment2_ternary;
	}

	public void setEm_mapping_fragment2_ternary(boolean em_mapping_fragment2_ternary) {
		this.em_mapping_fragment2_ternary = em_mapping_fragment2_ternary;
	}

	public int getSoae_NotMapped_T2_unfiltered() {
		return soae_NotMapped_T2_unfiltered;
	}

	public void setSoae_NotMapped_T2_unfiltered(int soae_NotMapped_T2_unfiltered) {
		this.soae_NotMapped_T2_unfiltered = soae_NotMapped_T2_unfiltered;
	}

	public int getEm_NotMapped_T2_filtered() {
		return em_NotMapped_T2_filtered;
	}

	public void setEm_NotMapped_T2_filtered(int em_NotMapped_T2_filtered) {
		this.em_NotMapped_T2_filtered = em_NotMapped_T2_filtered;
	}

	public int getEm_NotMapped_T2_unfiltered() {
		return em_NotMapped_T2_unfiltered;
	}

	public void setEm_NotMapped_T2_unfiltered(int em_NotMapped_T2_unfiltered) {
		this.em_NotMapped_T2_unfiltered = em_NotMapped_T2_unfiltered;
	}

	public int getSoae_NotMapped_T2_filtered() {
		return soae_NotMapped_T2_filtered;
	}

	public void setSoae_NotMapped_T2_filtered(int soae_NotMapped_T2_filtered) {
		this.soae_NotMapped_T2_filtered = soae_NotMapped_T2_filtered;
	}

	public boolean getEm_T2_in_T1() {
		return em_T2_in_T1;
	}

	public void setEm_T2_in_T1(boolean em_T2_in_T1) {
		this.em_T2_in_T1 = em_T2_in_T1;
	}

	public boolean getSoae_T2_in_T1() {
		return soae_T2_in_T1;
	}

	public void setSoae_T2_in_T1(boolean soae_T2_in_T1) {
		this.soae_T2_in_T1 = soae_T2_in_T1;
	}

	public boolean getEm_T2_in_mapping() {
		return em_T2_in_mapping;
	}

	public void setEm_T2_in_mapping(boolean em_T2_in_mapping) {
		this.em_T2_in_mapping = em_T2_in_mapping;
	}

	public boolean getSoae_T2_in_mapping() {
		return soae_T2_in_mapping;
	}

	public void setSoae_T2_in_mapping(boolean soae_T2_in_mapping) {
		this.soae_T2_in_mapping = soae_T2_in_mapping;
	}

	public boolean getEm_T2_IE_in_em_parameters() {
		return em_T2_IE_in_em_parameters;
	}

	public void setEm_T2_IE_in_em_parameters(boolean em_T2_IE_in_em_parameters) {
		this.em_T2_IE_in_em_parameters = em_T2_IE_in_em_parameters;
	}

	public boolean getSoae_T2_IE_in_em_parameters() {
		return soae_T2_IE_in_em_parameters;
	}

	public void setSoae_T2_IE_in_em_parameters(boolean soae_T2_IE_in_em_parameters) {
		this.soae_T2_IE_in_em_parameters = soae_T2_IE_in_em_parameters;
	}

	public boolean getEm_T2_Neutral() {
		return em_T2_Neutral;
	}

	public void setEm_T2_Neutral(boolean em_T2_Neutral) {
		this.em_T2_Neutral = em_T2_Neutral;
	}

	public boolean getSoae_T2_Neutral() {
		return soae_T2_Neutral;
	}

	public void setSoae_T2_Neutral(boolean soae_T2_Neutral) {
		this.soae_T2_Neutral = soae_T2_Neutral;
	}

	public boolean getSoae_T2_DV_in_em_parameters() {
		return soae_T2_DV_in_em_parameters;
	}

	public void setSoae_T2_DV_in_em_parameters(boolean soae_T2_DV_in_em_parameters) {
		this.soae_T2_DV_in_em_parameters = soae_T2_DV_in_em_parameters;
	}
	
    public boolean isEm_decompose_single_method() {
		return em_decompose_single_method;
	}

	public void setEm_decompose_single_method(boolean em_decompose_single_method) {
		this.em_decompose_single_method = em_decompose_single_method;
	}

	public boolean isEm_decompose_multiple_methods() {
		return em_decompose_multiple_methods;
	}

	public void setEm_decompose_multiple_methods(boolean em_decompose_multiple_methods) {
		this.em_decompose_multiple_methods = em_decompose_multiple_methods;
	}

	public boolean isEm_with_same_source_operation() {
		return em_with_same_source_operation;
	}

	public void setEm_with_same_source_operation(boolean em_with_same_source_operation) {
		this.em_with_same_source_operation = em_with_same_source_operation;
	}

	public boolean isEm_distinct() {
		return em_distinct;
	}

	public void setEm_distinct(boolean em_distinct) {
		this.em_distinct = em_distinct;
	}

	public boolean isEm_invocation_edit_distance_threshold_SM() {
		return em_invocation_edit_distance_threshold_SM;
	}

	public void setEm_invocation_edit_distance_threshold_SM(boolean em_invocation_edit_distance_threshold_SM) {
		this.em_invocation_edit_distance_threshold_SM = em_invocation_edit_distance_threshold_SM;
	}

	public boolean isEm_invocation_edit_distance_threshold_MM() {
		return em_invocation_edit_distance_threshold_MM;
	}

	public void setEm_invocation_edit_distance_threshold_MM(boolean em_invocation_edit_distance_threshold_MM) {
		this.em_invocation_edit_distance_threshold_MM = em_invocation_edit_distance_threshold_MM;
	}

	public boolean isEm_all_source_operation_nodes_mapped() {
		return em_all_source_operation_nodes_mapped;
	}

	public void setEm_all_source_operation_nodes_mapped(boolean em_all_source_operation_nodes_mapped) {
		this.em_all_source_operation_nodes_mapped = em_all_source_operation_nodes_mapped;
	}

	public boolean isEm_getter_setter() {
		return em_getter_setter;
	}

	public void setEm_getter_setter(boolean em_getter_setter) {
		this.em_getter_setter = em_getter_setter;
	}

	public int getEm_mapping_composite_nodes() {
		return em_mapping_composite_nodes;
	}

	public void setEm_mapping_composite_nodes(int em_mapping_composite_nodes) {
		this.em_mapping_composite_nodes = em_mapping_composite_nodes;
	}

	public int getEm_composite_expression_invocations() {
		return em_composite_expression_invocations;
	}

	public void setEm_composite_expression_invocations(int em_composite_expression_invocations) {
		this.em_composite_expression_invocations = em_composite_expression_invocations;
	}

	public boolean isEm_composite_expression_callVar() {
		return em_composite_expression_callVar;
	}

	public void setEm_composite_expression_callVar(boolean em_composite_expression_callVar) {
		this.em_composite_expression_callVar = em_composite_expression_callVar;
	}

	public int getEm_return_statement_invocations() {
		return em_return_statement_invocations;
	}

	public void setEm_return_statement_invocations(int em_return_statement_invocations) {
		this.em_return_statement_invocations = em_return_statement_invocations;
	}
    
    public boolean isEm_soae_equal_parameter_types() {
		return em_soae_equal_parameter_types;
	}

	public void setEm_soae_equal_parameter_types(boolean em_soae_equal_parameter_types) {
		this.em_soae_equal_parameter_types = em_soae_equal_parameter_types;
	}

	public boolean isSoae_is_delegate_to_em() {
		return soae_is_delegate_to_em;
	}

	public void setSoae_is_delegate_to_em(boolean soae_is_delegate_to_em) {
		this.soae_is_delegate_to_em = soae_is_delegate_to_em;
	}
	public boolean isEm_invocation_in_removeDuplication() {
		return em_invocation_in_removeDuplication;
	}

	public void setEm_invocation_in_removeDuplication(boolean em_invocation_in_removeDuplication) {
		this.em_invocation_in_removeDuplication = em_invocation_in_removeDuplication;
	}

	public boolean isEm_invocation_in_testOperation() {
		return em_invocation_in_testOperation;
	}

	public void setEm_invocation_in_testOperation(boolean em_invocation_in_testOperation) {
		this.em_invocation_in_testOperation = em_invocation_in_testOperation;
	}

	public boolean isSoae_is_testOperation() {
		return soae_is_testOperation;
	}

	public void setSoae_is_testOperation(boolean soae_is_testOperation) {
		this.soae_is_testOperation = soae_is_testOperation;
	}

	public boolean isEm_invocation_equal_mapping() {
		return em_invocation_equal_mapping;
	}

	public void setEm_invocation_equal_mapping(boolean em_invocation_equal_mapping) {
		this.em_invocation_equal_mapping = em_invocation_equal_mapping;
	}

	public boolean isEm_equalSignature_invocations() {
		return em_equalSignature_invocations;
	}

	public void setEm_equalSignature_invocations(boolean em_equalSignature_invocations) {
		this.em_equalSignature_invocations = em_equalSignature_invocations;
	}

	public boolean isEm_nested_invocations() {
		return em_nested_invocations;
	}

	public void setEm_nested_invocations(boolean em_nested_invocations) {
		this.em_nested_invocations = em_nested_invocations;
	}

	public int getEm_soae_invocations() {
		return em_soae_invocations;
	}

	public void setEm_soae_invocations(int em_soae_invocations) {
		this.em_soae_invocations = em_soae_invocations;
	}

	public int getEm_not_soae_invocations() {
		return em_not_soae_invocations;
	}

	public void setEm_not_soae_invocations(int em_not_soae_invocations) {
		this.em_not_soae_invocations = em_not_soae_invocations;
	}

    public boolean isEmHasAddedParameter() {
		return emHasAddedParameter;
	}

	public void setEmHasAddedParameter(boolean emHasAddedParameter) {
		this.emHasAddedParameter = emHasAddedParameter;
	}

	public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getRepository() {
        return repository;
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }

    public String getCommitId() {
        return commitId;
    }

    public void setCommitId(String commitId) {
        this.commitId = commitId;
    }
    
    public String getCommitMessage() {
		return commitMessage;
	}

	public void setCommitMessage(String commitMessage) {
		this.commitMessage = commitMessage;
	}
    public String getCommiterName() {
		return commiterName;
	}

	public void setCommiterName(String commiterName) {
		this.commiterName = commiterName;
	}

	public Date getCommiterDate() {
		return commiterDate;
	}

	public void setCommiterDate(Date commiterDate) {
		this.commiterDate = commiterDate;
	}

	public Date getCommitAuthorDate() {
		return commitAuthorDate;
	}

	public void setCommitAuthorDate(Date commitAuthorDate) {
		this.commitAuthorDate = commitAuthorDate;
	}
	
	public int getCommitTime() {
		return commitTime;
	}

	public void setCommitTime(int commitTime) {
		this.commitTime = commitTime;
	}

	public String getCommitAuthor() {
		return commitAuthor;
	}

	public void setCommitAuthor(String commitAuthor) {
		this.commitAuthor = commitAuthor;
	}

    public String getRefactoringDesc() {
        return refactoringDesc;
    }
    	
    public String getRefactoringType() {
		return refactoringType;
	}

	public void setRefactoringType(String refactoringType) {
		this.refactoringType = refactoringType;
	}
	
    public void setRefactoringDesc(String refactoringDesc) {
        this.refactoringDesc = refactoringDesc;
    }

    public String getRefactoringJSON() {
        return refactoringJSON;
    }

    public void setRefactoringJSON(String refactoringJSON) {
        this.refactoringJSON = refactoringJSON;
    }

    public boolean isReusableMethod() {
        return reusableMethod;
    }

    public void setReusableMethod(boolean reusableMethod) {
        this.reusableMethod = reusableMethod;
    }

    public boolean isIntroduceAlternativeMethodSignature() {
        return introduceAlternativeMethodSignature;
    }

    public void setIntroduceAlternativeMethodSignature(boolean introduceAlternativeMethodSignature) {
        this.introduceAlternativeMethodSignature = introduceAlternativeMethodSignature;
    }

    public boolean isDecomposeToImproveReadability() {
        return decomposeToImproveReadability;
    }

    public void setDecomposeToImproveReadability(boolean decomposeToImproveReadability) {
        this.decomposeToImproveReadability = decomposeToImproveReadability;
    }

    public boolean isFacilitateExtension() {
        return facilitateExtension;
    }

    public void setFacilitateExtension(boolean facilitateExtension) {
        this.facilitateExtension = facilitateExtension;
    }

    public boolean isRemoveDuplication() {
        return removeDuplication;
    }

    public void setRemoveDuplication(boolean removeDuplication) {
        this.removeDuplication = removeDuplication;
    }

    public boolean isReplaceMethodPreservingBackwardCompatibility() {
        return replaceMethodPreservingBackwardCompatibility;
    }

    public void setReplaceMethodPreservingBackwardCompatibility(boolean replaceMethodPreservingBackwardCompatibility) {
        this.replaceMethodPreservingBackwardCompatibility = replaceMethodPreservingBackwardCompatibility;
    }

    public boolean isImproveTestability() {
        return improveTestability;
    }

    public void setImproveTestability(boolean improveTestability) {
        this.improveTestability = improveTestability;
    }

    public boolean isEnableOverriding() {
        return enableOverriding;
    }

    public void setEnableOverriding(boolean enableOverriding) {
        this.enableOverriding = enableOverriding;
    }

    public boolean isEnableRecursion() {
        return enableRecursion;
    }

    public void setEnableRecursion(boolean enableRecursion) {
        this.enableRecursion = enableRecursion;
    }

    public boolean isIntroduceFactoryMethod() {
        return introduceFactoryMethod;
    }

    public void setIntroduceFactoryMethod(boolean introduceFactoryMethod) {
        this.introduceFactoryMethod = introduceFactoryMethod;
    }

    public boolean isIntroduceAsyncMethod() {
        return introduceAsyncMethod;
    }

    public void setIntroduceAsyncMethod(boolean introduceAsyncMethod) {
        this.introduceAsyncMethod = introduceAsyncMethod;
    }
    private void addMotivationFlag(MotivationFlag motivationFlag) {
        switch (motivationFlag) {
          	//Reuse Features
            case EM_INVOCATION_IN_REMOVE_DUPLICATION:
            	em_invocation_in_removeDuplication = true;
            	break;
            case EM_INVOCATION_IN_TEST_OPERATION:
            	em_invocation_in_testOperation = true;
            	break;
            case SOAE_IS_TEST_OPERATION:
            	soae_is_testOperation = true;
            	break;
            case EM_INVOCATION_EQUAL_MAPPING:
            	em_invocation_equal_mapping = true;
            	break;
            case EM_EQUAL_SINATURE_INVOCATIONS:
            	em_equalSignature_invocations = true;
            	break;
            case EM_NESTED_INVOCATIONS:
            	em_nested_invocations = true;
            	break;
            case EM_SOAE_INVOCATIONS:
            	em_soae_invocations = motivationFlag.getMotivationValue();
            	break;
            case EM_NONE_SOAE_INVOCATIONS:
            	em_not_soae_invocations = motivationFlag.getMotivationValue();
            	break;
            //Introduce Alternative Method Signature Features
            case EM_HAS_ADDED_PARAMETERS:
                emHasAddedParameter = true;
                break;
            case EM_SOAE_EQUAL_PARAMETER_TYPES:
                em_soae_equal_parameter_types = true;
                break;
            case SOAE_IS_DELEGATE_TO_EM:
                soae_is_delegate_to_em = true;
                break;
             //Decompose to Improve Readability
            case EM_DECOMPOS_SINGLE_METHOD:
                em_decompose_single_method = true;
                break;
            case EM_DECOMPOSE_MULTIPLE_METHODS:
                em_decompose_multiple_methods = true;
                break;
            case EM_WITH_SAME_SOURCE_OPERATION:
                em_with_same_source_operation = true;
                break;
            case EM_DISTINCT:
                em_distinct = true;
                break;
            case EM_INVOCATION_EDIT_DISTANCE_THRESHOLD_SM:
                em_invocation_edit_distance_threshold_SM = true;
                break;
            case EM_INVOCATION_EDIT_DISTANCE_THRESHOLD_MM:
                em_invocation_edit_distance_threshold_MM = true;
                break;
            case EM_ALL_SOURCE_OPERATION_NODES_MAPPED:
                em_all_source_operation_nodes_mapped = true;
                break;
            case EM_GETTER_SETTER:
                em_getter_setter = true;
                break;
            case EM_MAPPING_COMPOSITE_NODES:
                em_mapping_composite_nodes = motivationFlag.getMotivationValue();
                break;
            case EM_COMPOSITE_EXPRESSION_INVOCATIONS:
            	em_composite_expression_invocations = motivationFlag.getMotivationValue();
                break;
            case EM_COMPOSITE_EXPRESSION_CALLVAR:
            	em_composite_expression_callVar = true;
                break;    
            case EM_RETURN_STATEMENT_INVOCATIONS:
            	em_return_statement_invocations = motivationFlag.getMotivationValue();
                break;  
            case EM_MAPPING_FRAGMENT2_TERNARY:
            	em_mapping_fragment2_ternary = true;
            	break;
            case SOAE_NOT_MAPPED_T2_UNFILTERED:
            	soae_NotMapped_T2_unfiltered = motivationFlag.getMotivationValue();
            	break;
            case EM_NOTMAPPED_T2_UNFILTERED:
            	em_NotMapped_T2_unfiltered = motivationFlag.getMotivationValue();
            	break;
            case SOAE_NOTMAPPED_T2_FILTERED:
            	soae_NotMapped_T2_filtered = motivationFlag.getMotivationValue();
            	break;
            case EM_NOTMAPPED_T2_FILTERED:
            	em_NotMapped_T2_filtered = motivationFlag.getMotivationValue();
            	break;
            case EM_T2_IN_T1:
            	em_T2_in_T1 = true;
            	break;
            case SOAE_T2_IN_T1:
            	soae_T2_in_T1 = true;
            	break;
            case EM_T2_IN_MAPPING:
            	em_T2_in_mapping = true;
            	break;
            case SOAE_T2_IN_MAPPING:
            	soae_T2_in_mapping = true;
            	break;
            case EM_T2_IE_IN_EM_PARAMETERS:
            	em_T2_IE_in_em_parameters = true;
            	break;
            case SOAE_T2_IE_IN_EM_PARAMETERS:
            	soae_T2_IE_in_em_parameters = true;
            	break;
            case EM_T2_NEUTRAL:
            	em_T2_Neutral = true;
            	break;
            case SOAE_T2_NEUTRAL:
            	soae_T2_Neutral = true;
            	break;
            case SOAE_T2_DV_IN_EM_PARAMETERS:
            	soae_T2_DV_in_em_parameters = true;
            	break; 
            case EM_SAME_EXTRACTED_OPERATIONS:
            	em_same_extracted_operations = true;
            	break;
            case EM_SOAE_EQUAL_NAMES:
            	em_soae_equal_names = true;
            	break;
            case SOAE_DEPRECATED:
            	soae_deprecated = true;
            	break;
            case SOAE_PROTECTED:
            	soae_protected = true;
            	break;
            case SOAE_PRIVATE:
            	soae_private = true;
            	break;
            case EM_TEST_INVOCATION_CLASS_EQUAL_TO_EM_CLASS:
            	em_test_invocation_class_equal_to_emClass = true;
            	break;
            case EM_TEST_INVOCATION_IN_ADDED_NODE:
            	em_test_invocation_in_added_node = true;
            	break;
            case EM_EQUAL_OPERATION_SIGNATURE_IN_SUBTYPE:
            	em_equal_operation_signature_in_subtype = true;
            	break;
            case EM_OVERRIDING_KEYWORD_IN_COMMENT:
            	em_overriding_keyword_in_comment = true;
            	break;
            case SOBE_RECURSIVE:
            	sobe_recursive = true;
                break;
            case EM_RECURSIVE:
            	em_recursive = true;
                break;
            case EM_HAS_RETURN_STATEMENTS:
            	em_has_return_statement = true;
            	break;
            case EM_RETURN_STATEMENT_NEW_KEYWORDS:
            	em_return_statement_new_keywords = motivationFlag.getMotivationValue();
            	break;
            case EM_RETURN_EQUAL_NEW_RETURN:
            	em_return_equal_new_return = true;
            	break;
            case EM_VARS_FACTORY_METHOD_RELATED:
            	em_vars_factory_method_related = true;
            	break;
            case SOBE_FACTORY_METHOD:
            	sobe_factory_method = true;
            	break;
            case SOAE_ANONYMOUS_CLASS_RUNNABLE_EM_INVOCATION:
            	soae_anonymous_class_runnable_em_invocation = true;
            	break;
            case SOAE_IS_ALL_TEMP_VARIABLES:
            	soae_is_all_temp_variables = true;
            	break;
            case EM_OBJECT_CREATION_VARIABLE_RETURNED:
            	em_object_creation_variable_returned = true;
            	break;
            case EM_T2_EM_INVOCATIONS:
            	em_T2_em_invocations = true;
            	break;
            case SOAE_T2_EM_INVOCATIONS:
            	soae_T2_em_invocations = true;
            	break;
            case EM_MAPPING_SIZE: 
            	em_mapping_size = motivationFlag.getMotivationValue();
            	break;
            case EM_NUM_METHODS_USED_IN_DUPLICATION_REMOVAL: 
            	em_num_methods_used_in_duplication_removal = motivationFlag.getMotivationValue();
            	break;
            case SOAE_STATEMENTS_CONTAIN_RUNNABLE_TYPE: 
            	soae_statements_contain_runnable_type = true;
            	break;
            	
            	           	
        }
    }
    
    private void addMotivation(MotivationType motivation) {
        switch (motivation) {
            case EM_REUSABLE_METHOD:
                reusableMethod = true;
                break;
            case EM_INTRODUCE_ALTERNATIVE_SIGNATURE:
                introduceAlternativeMethodSignature = true;
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
        }
    }

}
