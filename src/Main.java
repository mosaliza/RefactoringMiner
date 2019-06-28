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
				"tmp/bitcoinj",
				"https://github.com/bitcoinj/bitcoinj.git");
		miner.detectAtCommit("https://github.com/bitcoinj/bitcoinj.git",
				"12602650ce99f34cb530fc24266c23e39733b0bb", new RefactoringHandler() {
					@Override
					public void handle(RevCommit commitData, List<Refactoring> refactorings) {
						for(Refactoring ref : refactorings) {
							System.out.println(ref);
						}
					}
				},100);
		
		/*miner.detectAtCommit(repo,"https://github.com/CyanogenMod/android_frameworks_base.git",
				"4587c32ab8a1c8e2169e4f93491a8c927216a6ab", new RefactoringHandler() {
			@Override
			public void handle(RevCommit commitData, List<Refactoring> refactorings) {
				for(Refactoring ref : refactorings) {
					System.out.println(ref);
				}
			}
		});*/
	}
}
