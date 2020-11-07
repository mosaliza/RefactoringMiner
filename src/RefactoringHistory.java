import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.refactoringminer.api.Refactoring;
@Entity
@Table(name = "refactorings")
public class RefactoringHistory {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;
	
    @Column(name = "repository")
    private String repository;

    public String getRepository() {
		return repository;
	}
	public void setRepository(String repository) {
		this.repository = repository;
	}
	@Column(name = "commit_id")
    private String commitId;
        
	
    @Column(name = "extract_operation")
	private int extract_operation;
    
    @Column(name = "rename_class")
    private int rename_class;
	
    @Column(name = "move_attribute")
    private int move_attribute;
	
    @Column(name = "move_rename_attribute")
    private int move_rename_attribute;
	
    @Column(name = "replace_attribute")
    private int replace_attribute;
    
    @Column(name = "rename_method")
    private int rename_method;
	
    @Column(name = "inline_operation")
    private int inline_operation;
    
    @Column(name = "move_operation")
    private int move_operation;
    
    @Column(name = "move_and_rename_operation")
    private int move_and_rename_operation;
	
    @Column(name = "pull_up_operation")
    private int pull_up_operation;
	
    @Column(name = "move_class")
    private int move_class;
	
    @Column(name = "move_rename_class")
    private int move_rename_class;
	
    @Column(name = "move_source_folder")
    private int move_source_folder;
	
    @Column(name = "pull_up_attribute")
    private int pull_up_attribute;
	
    @Column(name = "push_down_attribute")
    private int push_down_attribute;
	
    @Column(name = "push_down_operation")
    private int push_down_operation;
	
    @Column(name = "extract_interface")
    private int extract_interface;
	
    @Column(name = "extract_superclass")
    private int extract_superclass;
	
    @Column(name = "extract_subclass")
    private int extract_subclass;
	
    @Column(name = "extract_class")
    private int extract_class;
	
    @Column(name = "merge_operation")
    private int merge_operation;
	
    @Column(name = "extract_and_move_operation")
    private int extract_and_move_operation;
	
    @Column(name = "move_and_inline_operation")
    private int move_and_inline_operation;
	
    @Column(name = "convert_anonymous_class_to_type")
    private int convert_anonymous_class_to_type;
	
    @Column(name = "introduce_polymorphism")
    private int introduce_polymorphism;
	
    @Column(name = "rename_package")
    private int rename_package;
	
    @Column(name = "extract_variable")
    private int extract_variable;
	
    @Column(name = "extract_attribute")
    private int extract_attribute;
	
    @Column(name = "inline_variable")
    private int inline_variable;
	
    @Column(name = "rename_variable")
    private int rename_variable;
	
    @Column(name = "rename_parameter")
    private int rename_parameter;
	
    @Column(name = "rename_attribute")
    private int rename_attribute;
	
    @Column(name = "merge_variable")
    private int merge_variable;
	
    @Column(name = "merge_parameter")
    private int merge_parameter;
	
    @Column(name = "merge_attribute")
    private int merge_attribute;
	
    @Column(name = "split_variable")
    private int split_variable;
	    
    @Column(name = "split_parameter")
    private int split_parameter;
    
    @Column(name = "split_attribute")
    private int split_attribute;
	
    @Column(name = "replace_variable_with_attribute")
    private int replace_variable_with_attribute;
	
    @Column(name = "parametrize_variable")
    private int parametrize_variable;
	
    @Column(name = "change_return_type")
    private int change_return_type;
	
    @Column(name = "change_variable_type")
    private int change_variable_type;
	
    @Column(name = "change_parameter_type")
    private int change_parameter_type;
	
    @Column(name = "change_attribute_type")
    private int change_attribute_type;
	
    @Column(name = "add_method_annotation")
    private int add_method_annotation;
	
    @Column(name = "remove_method_annotation")
    private int remove_method_annotation;
	
    @Column(name = "modify_method_annotation")
    private int modify_method_annotation;
	
    @Column(name = "add_attribute_annotation")
    private int add_attribute_annotation;
	
    @Column(name = "remove_attribute_annotation")
    private int remove_attribute_annotation;
	
    @Column(name = "modify_attribute_annotation")
    private int modify_attribute_annotation;
	
    @Column(name = "add_class_annotation")
    private int add_class_annotation;
	
    @Column(name = "remove_class_annotation")
    private int remove_class_annotation;
	
    @Column(name = "modify_class_annotation")
    private int modify_class_annotation;
	
    @Column(name = "add_parameter_annotation")
    private int add_parameter_annotation;
	
    @Column(name = "remove_parameter_annotation")
    private int remove_parameter_annotation;
	
    @Column(name = "modify_parameter_annotation")
    private int modify_parameter_annotation;
	
    @Column(name = "add_parameter")
    private int add_parameter;
	
    @Column(name = "remove_parameter")
    private int remove_parameter;
	
    @Column(name = "reorder_parameter")
    private int reorder_parameter;
	
    @Column(name = "add_variable_annotation")
    private int add_variable_annotation;
	
    @Column(name = "remove_variable_annotation")
    private int remove_variable_annotation;
	
    @Column(name = "modify_variable_annotation")
    private int modify_variable_annotation;
	
    

	
	public RefactoringHistory(String commitID , String repository){
		this.commitId = commitID;
		this.repository = repository;
	
	}
	public void addRefactoringType(Refactoring refactoring){
		
		switch (refactoring.getRefactoringType()) {
		case RENAME_CLASS:
			rename_class++; 
			break;
		case MOVE_CLASS:
			move_class++;
			break;
		case MOVE_SOURCE_FOLDER:
			move_source_folder++;
			break;
		case RENAME_METHOD:
			rename_method++;
			break;
		case EXTRACT_OPERATION:
			extract_operation++;
			break;
		case INLINE_OPERATION:
			inline_operation++;
			break;
		case MOVE_OPERATION:
			move_operation++;
			break;
		case PULL_UP_OPERATION:
			pull_up_operation++;
			break;
		case PUSH_DOWN_OPERATION:
			push_down_operation++;
			break;
		case MOVE_ATTRIBUTE:
			move_attribute++;
			break;
		case MOVE_RENAME_ATTRIBUTE:
			move_rename_attribute++;
			break;
		case REPLACE_ATTRIBUTE:
			replace_attribute++;
			break;
		case PULL_UP_ATTRIBUTE:
			pull_up_attribute++;
			break;
		case PUSH_DOWN_ATTRIBUTE:
			push_down_attribute++;
			break;
		case EXTRACT_SUPERCLASS:
			extract_superclass++;
			break;
		case EXTRACT_CLASS:
			extract_class++;
			break;
		case EXTRACT_AND_MOVE_OPERATION:
			extract_and_move_operation++;
			break;
		case MOVE_RENAME_CLASS:
			move_rename_class++;
			break;
		case RENAME_PACKAGE:
			rename_package++;
			break;
		case EXTRACT_VARIABLE:
			extract_variable++;
			break;
		case INLINE_VARIABLE:
			inline_variable++;
			break;
		case RENAME_VARIABLE:
			rename_variable++;
			break;
		case RENAME_PARAMETER:
			rename_parameter++;
			break;
		case RENAME_ATTRIBUTE:
			rename_attribute++;
			break;
		case REPLACE_VARIABLE_WITH_ATTRIBUTE:
			replace_variable_with_attribute++;
			break;
		case PARAMETERIZE_VARIABLE:
			parametrize_variable++;
			break;
		case MERGE_VARIABLE:
			merge_variable++;
			break;
		case MERGE_PARAMETER:
			merge_parameter++;
			break;
		case MERGE_ATTRIBUTE:
			merge_attribute++;
			break;
		case SPLIT_ATTRIBUTE:
			split_attribute++;
			break;
		case SPLIT_VARIABLE:
			split_variable++;
			break;
		case SPLIT_PARAMETER:
			split_parameter++;
			break;
		case CHANGE_RETURN_TYPE:
			change_return_type++;
			break;
		case CHANGE_VARIABLE_TYPE:
			change_variable_type++;
			break;
		case CHANGE_PARAMETER_TYPE:
			change_parameter_type++;
			break;
		case CHANGE_ATTRIBUTE_TYPE:
			change_attribute_type++;
			break;
		case EXTRACT_ATTRIBUTE:
			extract_attribute++;
			break;
		case MOVE_AND_RENAME_OPERATION:
			move_and_rename_operation++;
			break;
		case MOVE_AND_INLINE_OPERATION:
			move_and_inline_operation++;
			break;
		case ADD_METHOD_ANNOTATION:
			add_method_annotation++;
			break;
		case REMOVE_METHOD_ANNOTATION:
			remove_method_annotation++;
			break;
		case MODIFY_METHOD_ANNOTATION:
			modify_method_annotation++;
			break;
		case ADD_ATTRIBUTE_ANNOTATION:
			add_attribute_annotation++;
			break;
		case REMOVE_ATTRIBUTE_ANNOTATION:
			remove_attribute_annotation++;
			break;
		case MODIFY_ATTRIBUTE_ANNOTATION:
			modify_attribute_annotation++;
			break;
		case ADD_CLASS_ANNOTATION:
			add_class_annotation++;
			break;
		case REMOVE_CLASS_ANNOTATION:
			remove_class_annotation++;
			break;
		case MODIFY_CLASS_ANNOTATION:
			modify_class_annotation++;
			break;
		case ADD_PARAMETER:
			add_parameter++;
			break;
		case REMOVE_PARAMETER:
			remove_parameter++;
			break;
		case REORDER_PARAMETER:
			reorder_parameter++;
			break;
		case ADD_PARAMETER_ANNOTATION:
			add_parameter_annotation++;
			break;
		case REMOVE_PARAMETER_ANNOTATION:
			remove_parameter_annotation++;
			break;
		case MODIFY_PARAMETER_ANNOTATION:
			modify_parameter_annotation++;
			break;
		case ADD_VARIABLE_ANNOTATION:
			add_variable_annotation++;
			break;
		case REMOVE_VARIABLE_ANNOTATION:
			remove_variable_annotation++;
			break;
		case MODIFY_VARIABLE_ANNOTATION:
			modify_variable_annotation++;
			break;
						
		default:
			break;
		}	
	}
	
	public String getCommitID() {
		return commitId;
	}
	public void setCommitID(String commitID) {
		this.commitId = commitID;
	}
	public int getExtract_operation() {
		return extract_operation;
	}
	public void setExtract_operation(int extract_operation) {
		this.extract_operation = extract_operation;
	}
	public int getRename_class() {
		return rename_class;
	}
	public void setRename_class(int rename_class) {
		this.rename_class = rename_class;
	}
	public int getMove_attribute() {
		return move_attribute;
	}
	public void setMove_attribute(int move_attribute) {
		this.move_attribute = move_attribute;
	}
	public int getMove_rename_attribute() {
		return move_rename_attribute;
	}
	public void setMove_rename_attribute(int move_rename_attribute) {
		this.move_rename_attribute = move_rename_attribute;
	}
	public int getReplace_attribute() {
		return replace_attribute;
	}
	public void setReplace_attribute(int replace_attribute) {
		this.replace_attribute = replace_attribute;
	}
	public int getRename_method() {
		return rename_method;
	}
	public void setRename_method(int rename_method) {
		this.rename_method = rename_method;
	}
	public int getInline_operation() {
		return inline_operation;
	}
	public void setInline_operation(int inline_operation) {
		this.inline_operation = inline_operation;
	}
	public int getMove_operation() {
		return move_operation;
	}
	public void setMove_operation(int move_operation) {
		this.move_operation = move_operation;
	}
	public int getMove_and_rename_operation() {
		return move_and_rename_operation;
	}
	public void setMove_and_rename_operation(int move_and_rename_operation) {
		this.move_and_rename_operation = move_and_rename_operation;
	}
	public int getPull_up_operation() {
		return pull_up_operation;
	}
	public void setPull_up_operation(int pull_up_operation) {
		this.pull_up_operation = pull_up_operation;
	}
	public int getMove_class() {
		return move_class;
	}
	public void setMove_class(int move_class) {
		this.move_class = move_class;
	}
	public int getMove_rename_class() {
		return move_rename_class;
	}
	public void setMove_rename_class(int move_rename_class) {
		this.move_rename_class = move_rename_class;
	}
	public int getMove_source_folder() {
		return move_source_folder;
	}
	public void setMove_source_folder(int move_source_folder) {
		this.move_source_folder = move_source_folder;
	}
	public int getPull_up_attribute() {
		return pull_up_attribute;
	}
	public void setPull_up_attribute(int pull_up_attribute) {
		this.pull_up_attribute = pull_up_attribute;
	}
	public int getPush_down_attribute() {
		return push_down_attribute;
	}
	public void setPush_down_attribute(int push_down_attribute) {
		this.push_down_attribute = push_down_attribute;
	}
	public int getExtract_interface() {
		return extract_interface;
	}
	public void setExtract_interface(int extract_interface) {
		this.extract_interface = extract_interface;
	}
	public int getExtract_superclass() {
		return extract_superclass;
	}
	public void setExtract_superclass(int extract_superclass) {
		this.extract_superclass = extract_superclass;
	}
	public int getExtract_class() {
		return extract_class;
	}
	public void setExtract_class(int extract_class) {
		this.extract_class = extract_class;
	}
	public int getMerge_operation() {
		return merge_operation;
	}
	public void setMerge_operation(int merge_operation) {
		this.merge_operation = merge_operation;
	}
	public int getExtract_and_move_operation() {
		return extract_and_move_operation;
	}
	public void setExtract_and_move_operation(int extract_and_move_operation) {
		this.extract_and_move_operation = extract_and_move_operation;
	}
	public int getMove_and_inline_operation() {
		return move_and_inline_operation;
	}
	public void setMove_and_inline_operation(int move_and_inline_operation) {
		this.move_and_inline_operation = move_and_inline_operation;
	}
	public int getConvert_anonymous_class_to_type() {
		return convert_anonymous_class_to_type;
	}
	public void setConvert_anonymous_class_to_type(int convert_anonymous_class_to_type) {
		this.convert_anonymous_class_to_type = convert_anonymous_class_to_type;
	}
	public int getIntroduce_polymorphism() {
		return introduce_polymorphism;
	}
	public void setIntroduce_polymorphism(int introduce_polymorphism) {
		this.introduce_polymorphism = introduce_polymorphism;
	}
	public int getRename_package() {
		return rename_package;
	}
	public void setRename_package(int rename_package) {
		this.rename_package = rename_package;
	}
	public int getExtract_variable() {
		return extract_variable;
	}
	public void setExtract_variable(int extract_variable) {
		this.extract_variable = extract_variable;
	}
	public int getExtract_attribute() {
		return extract_attribute;
	}
	public void setExtract_attribute(int extract_attribute) {
		this.extract_attribute = extract_attribute;
	}
	public int getInline_variable() {
		return inline_variable;
	}
	public void setInline_variable(int inline_variable) {
		this.inline_variable = inline_variable;
	}
	public int getRename_variable() {
		return rename_variable;
	}
	public void setRename_variable(int rename_variable) {
		this.rename_variable = rename_variable;
	}
	public int getRename_parameter() {
		return rename_parameter;
	}
	public void setRename_parameter(int rename_parameter) {
		this.rename_parameter = rename_parameter;
	}
	public int getRename_attribute() {
		return rename_attribute;
	}
	public void setRename_attribute(int rename_attribute) {
		this.rename_attribute = rename_attribute;
	}
	public int getMerge_variable() {
		return merge_variable;
	}
	public void setMerge_variable(int merge_variable) {
		this.merge_variable = merge_variable;
	}
	public int getMerge_parameter() {
		return merge_parameter;
	}
	public void setMerge_parameter(int merge_parameter) {
		this.merge_parameter = merge_parameter;
	}
	public int getMerge_attribute() {
		return merge_attribute;
	}
	public void setMerge_attribute(int merge_attribute) {
		this.merge_attribute = merge_attribute;
	}
	public int getSplit_variable() {
		return split_variable;
	}
	public void setSplit_variable(int split_variable) {
		this.split_variable = split_variable;
	}
	public int getSplit_parameter() {
		return split_parameter;
	}
	public void setSplit_parameter(int split_parameter) {
		this.split_parameter = split_parameter;
	}
	public int getSplit_attribute() {
		return split_attribute;
	}
	public void setSplit_attribute(int split_attribute) {
		this.split_attribute = split_attribute;
	}
	public int getReplace_variable_with_attribute() {
		return replace_variable_with_attribute;
	}
	public void setReplace_variable_with_attribute(int replace_variable_with_attribute) {
		this.replace_variable_with_attribute = replace_variable_with_attribute;
	}
	public int getParametrize_variable() {
		return parametrize_variable;
	}
	public void setParametrize_variable(int parametrize_variable) {
		this.parametrize_variable = parametrize_variable;
	}
	public int getChange_return_type() {
		return change_return_type;
	}
	public void setChange_return_type(int change_return_type) {
		this.change_return_type = change_return_type;
	}
	public int getChange_variable_type() {
		return change_variable_type;
	}
	public void setChange_variable_type(int change_variable_type) {
		this.change_variable_type = change_variable_type;
	}
	public int getChange_parameter_type() {
		return change_parameter_type;
	}
	public void setChange_parameter_type(int change_parameter_type) {
		this.change_parameter_type = change_parameter_type;
	}
	public int getChange_attribute_type() {
		return change_attribute_type;
	}
	public void setChange_attribute_type(int change_attribute_type) {
		this.change_attribute_type = change_attribute_type;
	}
	public int getAdd_method_annotation() {
		return add_method_annotation;
	}
	public void setAdd_method_annotation(int add_method_annotation) {
		this.add_method_annotation = add_method_annotation;
	}
	public int getRemove_method_annotation() {
		return remove_method_annotation;
	}
	public void setRemove_method_annotation(int remove_method_annotation) {
		this.remove_method_annotation = remove_method_annotation;
	}
	public int getModify_method_annotation() {
		return modify_method_annotation;
	}
	public void setModify_method_annotation(int modify_method_annotation) {
		this.modify_method_annotation = modify_method_annotation;
	}
	public int getAdd_attribute_annotation() {
		return add_attribute_annotation;
	}
	public void setAdd_attribute_annotation(int add_attribute_annotation) {
		this.add_attribute_annotation = add_attribute_annotation;
	}
	public int getRemove_attribute_annotation() {
		return remove_attribute_annotation;
	}
	public void setRemove_attribute_annotation(int remove_attribute_annotation) {
		this.remove_attribute_annotation = remove_attribute_annotation;
	}
	public int getModify_attirbute_annotation() {
		return modify_attribute_annotation;
	}
	public void setModify_attirbute_annotation(int modify_attirbute_annotation) {
		this.modify_attribute_annotation = modify_attirbute_annotation;
	}
	public int getAdd_class_annotation() {
		return add_class_annotation;
	}
	public void setAdd_class_annotation(int add_class_annotation) {
		this.add_class_annotation = add_class_annotation;
	}
	public int getRemove_class_annotation() {
		return remove_class_annotation;
	}
	public void setRemove_class_annotation(int remove_class_annotation) {
		this.remove_class_annotation = remove_class_annotation;
	}
	public int getModify_class_annotation() {
		return modify_class_annotation;
	}
	public void setModify_class_annotation(int modify_class_annotation) {
		this.modify_class_annotation = modify_class_annotation;
	}
	public int getAdd_parameter_annotation() {
		return add_parameter_annotation;
	}
	public void setAdd_parameter_annotation(int add_parameter_annotation) {
		this.add_parameter_annotation = add_parameter_annotation;
	}
	public int getRemove_parameter_annotation() {
		return remove_parameter_annotation;
	}
	public void setRemove_parameter_annotation(int remove_parameter_annotation) {
		this.remove_parameter_annotation = remove_parameter_annotation;
	}
	public int getModify_parameter_annotation() {
		return modify_parameter_annotation;
	}
	public void setModify_parameter_annotation(int modify_parameter_annotation) {
		this.modify_parameter_annotation = modify_parameter_annotation;
	}
	public int getAdd_parameter() {
		return add_parameter;
	}
	public void setAdd_parameter(int add_parameter) {
		this.add_parameter = add_parameter;
	}
	public int getRemove_parameter() {
		return remove_parameter;
	}
	public void setRemove_parameter(int remove_parameter) {
		this.remove_parameter = remove_parameter;
	}
	public int getReorder_parameter() {
		return reorder_parameter;
	}
	public void setReorder_parameter(int reorder_parameter) {
		this.reorder_parameter = reorder_parameter;
	}
	public int getAdd_variable_annotation() {
		return add_variable_annotation;
	}
	public void setAdd_variable_annotation(int add_variable_annotation) {
		this.add_variable_annotation = add_variable_annotation;
	}
	public int getRemove_variable_annotation() {
		return remove_variable_annotation;
	}
	public void setRemove_variable_annotation(int remove_variable_annotation) {
		this.remove_variable_annotation = remove_variable_annotation;
	}
	public int getModify_variable_annotation() {
		return modify_variable_annotation;
	}
	public void setModify_variable_annotation(int modify_variable_annotation) {
		this.modify_variable_annotation = modify_variable_annotation;
	}
	public int getPush_down_operation() {
		return push_down_operation;
	}
	public void setPush_down_operation(int push_down_operation) {
		this.push_down_operation = push_down_operation;
	}
	public int getExtract_subclass() {
		return extract_subclass;
	}
	public void setExtract_subclass(int extract_subclass) {
		this.extract_subclass = extract_subclass;
	}
}
