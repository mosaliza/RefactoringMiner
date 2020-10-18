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
    
    @Column(name = "em_has_added_parameter")
    private boolean emHasAddedParameter = false;

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
            case EM_HAS_ADDED_PARAMETERS:
                emHasAddedParameter = true;
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
