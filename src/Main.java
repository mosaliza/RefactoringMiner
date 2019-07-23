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
				"tmp1/java-algorithms-implementation",
				"https://github.com/phishman3579/java-algorithms-implementation.git");
		/*miner.detectAtCommit("https://github.com/luontola/retrolambda.git",
				"46b0d84de9c309bca48a99e572e6611693ed5236", new RefactoringHandler() {
					@Override
					public void handle(String commitId, List<Refactoring> refactorings) {
						for(Refactoring ref : refactorings) {
							System.out.println(ref);
						}
					}
				},100);*/
		
		miner.detectAtCommit(repo,"f2385a56e6aa040ea4ff18a23ce5b63a4eeacf29", new RefactoringHandler() {
			@Override 
			public void handle(String commitData, List<Refactoring> refactorings) {
				for(Refactoring ref : refactorings) {
					System.out.println(ref);
				}
			}
		});
	}
}
