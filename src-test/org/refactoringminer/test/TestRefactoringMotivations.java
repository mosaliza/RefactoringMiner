package org.refactoringminer.test;

import java.math.BigInteger;

import org.junit.Test;
import org.refactoringminer.rm1.GitHistoryRefactoringMinerImpl;
import org.refactoringminer.test.MotivationPopulator.Refactorings;
import org.refactoringminer.test.MotivationPopulator.Systems;

public class TestRefactoringMotivations {

	@Test
	public void testAllMotivationsDetectors() throws Exception {
		BigInteger flag = Refactorings.ExtractMethod.getValue()
				.add(Refactorings.MoveClass.getValue())
				.add(Refactorings.MoveAttribute.getValue())
				.add(Refactorings.ChangePackage.getValue())
				.add(Refactorings.MoveMethod.getValue())
				.add(Refactorings.InlineMethod.getValue())
				.add(Refactorings.PullUpMethod.getValue())
				.add(Refactorings.PullUpAttribute.getValue())
				.add(Refactorings.ExtractSuperclass.getValue())
				.add(Refactorings.PushDownMethod.getValue())
				.add(Refactorings.PushDownAttribute.getValue())
				.add(Refactorings.ExtractInterface.getValue())
				.add(Refactorings.ExtractAndMoveMethod.getValue());
		MotivationTestBuilder test = new MotivationTestBuilder(new GitHistoryRefactoringMinerImpl(), "tmp1",
				 /*Refactorings.All.getValue()*/flag);
		MotivationPopulator.feedRefactoringsInstances(/*Refactorings.All.getValue()*/ flag, Systems.FSE.getValue(),
				test);
		test.verbose();
		test.assertExpectations();
	}

}
