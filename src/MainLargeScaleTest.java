import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.internal.core.util.ExtendedAnnotation;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.refactoringminer.api.GitHistoryRefactoringMiner;
import org.refactoringminer.api.GitService;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringHandler;
import org.refactoringminer.api.RefactoringType;
import org.refactoringminer.rm1.GitHistoryRefactoringMinerImpl;
import org.refactoringminer.util.GitServiceImpl;

import gr.uom.java.xmi.diff.MotivationType;

public class MainLargeScaleTest {

	public static void main(String[] args) throws Exception {
		GitService gitService = new GitServiceImpl();
		GitHistoryRefactoringMiner miner = new GitHistoryRefactoringMinerImpl();
		Repository repo = gitService.cloneIfNotExists("tmp1/bitcoinj", "https://github.com/bitcoinj/bitcoinj.git");
		StringBuilder sb = new StringBuilder();
		sb.append(ExtractMethodMotivation.EM_HEADER).append("\n");
		List<ExtractMethodMotivation> listExtractMethodMotivations = new ArrayList<ExtractMethodMotivation>();

		RevWalk revWalk = gitService.createAllRevsWalk(repo, "master");
		Iterator<RevCommit> i = revWalk.iterator();
		while (i.hasNext()) {
			RevCommit commit = i.next();
			miner.detectAtCommit("https://github.com/bitcoinj/bitcoinj.git", commit.name(), new RefactoringHandler() {
				@Override
				public void handle(String commitId, List<Refactoring> refactorings,
						Map<Refactoring, List<MotivationType>> mapRefactoringMotivations,
						Map<Refactoring, int[]> mapFacilitateExtensionT1T2,
						Map<Refactoring, String> mapDecomposeToImproveRedability) {
					for (Refactoring ref : refactorings) {
						if (ref.getRefactoringType().equals(RefactoringType.EXTRACT_AND_MOVE_OPERATION)
								|| ref.getRefactoringType().equals(RefactoringType.EXTRACT_OPERATION)) {
							for (Refactoring refactoing : mapRefactoringMotivations.keySet()) {
								if (refactoing.toString().equals(ref.toString())) {
									System.out.println(ref);
									ExtractMethodMotivation extractMethodMotivation = new ExtractMethodMotivation(ref);
									List<MotivationType> motivations = mapRefactoringMotivations.get(refactoing);
									extractMethodMotivation.addMotivations(motivations);
									listExtractMethodMotivations.add(extractMethodMotivation);
									for (MotivationType motivation : motivations) {
										System.out.println(motivation.toString());
									}
								}
							}
						}
					}
				}
			}, 1000);
		}
		for (ExtractMethodMotivation extractMethodMotivation : listExtractMethodMotivations) {
			sb.append(extractMethodMotivation.getRefactoring().toString() + "|"
					+ extractMethodMotivation.isReusableMethod() + "|"
					+ extractMethodMotivation.isIntroduceAlternativeMethodSingature() + "|"
					+ extractMethodMotivation.isDecomposeToImproveReadability() + "|"
					+ extractMethodMotivation.isFacilitateExtension() + "|"
					+ extractMethodMotivation.isRemoveDuplication() + "|"
					+ extractMethodMotivation.isReplaceMethodPreservingBackwardCompatibility() + "|"
					+ extractMethodMotivation.isImproveTestability() + "|"
					+ extractMethodMotivation.isEnableOverriding() + "|" + extractMethodMotivation.isEnableRecursion()
					+ "|" + extractMethodMotivation.isIntroduceFactoryMethod() + "|"
					+ extractMethodMotivation.isIntroduceAsyncMethod() + "\n");
		}

		try {
			FileWriter out = new FileWriter(new File("motivations.csv"));
			out.write(sb.toString());
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
