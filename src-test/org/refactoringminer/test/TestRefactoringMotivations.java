package org.refactoringminer.test;


import org.junit.Test;
import org.refactoringminer.rm1.GitHistoryRefactoringMinerImpl;
import org.refactoringminer.test.MotivationPopulator.Refactorings;
import org.refactoringminer.test.MotivationPopulator.Systems;


public class TestRefactoringMotivations {
	
	@Test
	public void testAllMotivationsDetectors() throws Exception {
		MotivationTestBuilder test = new MotivationTestBuilder(new GitHistoryRefactoringMinerImpl(), "tmp1", Refactorings.All.getValue());
		MotivationPopulator.feedRefactoringsInstances(Refactorings.All.getValue(), Systems.FSE.getValue(), test);
		test.assertExpectations();
	}

}
