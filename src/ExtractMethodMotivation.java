import com.vladmihalcea.hibernate.type.json.JsonStringType;
import gr.uom.java.xmi.diff.MotivationType;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
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

    public ExtractMethodMotivation() {
    }

    public ExtractMethodMotivation(Set<MotivationType> motivations) {
        for (MotivationType motivation : motivations) {
            this.addMotivation(motivation);
        }
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

    public String getRefactoringDesc() {
        return refactoringDesc;
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
