import java.util.List;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.refactoringminer.api.GitHistoryRefactoringMiner;
import org.refactoringminer.api.GitService;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringHandler;
import org.refactoringminer.rm1.GitHistoryRefactoringMinerImpl;
import org.refactoringminer.util.GitServiceImpl;

public class Main {

	public static void main(String[] args) throws Exception {
		GitService gitService = new GitServiceImpl();
		GitHistoryRefactoringMiner miner = new GitHistoryRefactoringMinerImpl();

		Repository repo = gitService.cloneIfNotExists(
				"tmp1/intellij-community",
				"https://github.com/JetBrains/intellij-community.git");
		/*miner.detectAtCommit("https://github.com/luontola/retrolambda.git",
				"46b0d84de9c309bca48a99e572e6611693ed5236", new RefactoringHandler() {
					@Override
					public void handle(String commitId, List<Refactoring> refactorings) {
						for(Refactoring ref : refactorings) {
							System.out.println(ref);
						}
					}
				},100);*/
		
		miner.detectAtCommit(repo,"a97341973c3b683d62d1422e5404ed5c7ccf45f8", new RefactoringHandler() {
			@Override 
			public void handle(String commitData, List<Refactoring> refactorings) {
				for(Refactoring ref : refactorings) {
					System.out.println(ref);
				}
			}
		});
	}
}
