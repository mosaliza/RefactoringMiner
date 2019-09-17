package org.refactoringminer.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jgit.lib.Repository;
import org.junit.Assert;
import org.refactoringminer.api.GitHistoryRefactoringMiner;
import org.refactoringminer.api.GitService;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringHandler;
import org.refactoringminer.api.RefactoringType;
import org.refactoringminer.rm1.GitHistoryRefactoringMinerImpl;
import org.refactoringminer.util.GitServiceImpl;

import gr.uom.java.xmi.diff.MotivationType;
import org.refactoringminer.test.MotivationPopulator.Refactorings;

public class MotivationTestBuilder {

	private final String tempDir;
	private final Map<String, ProjectMatcher> map;
	private final GitHistoryRefactoringMiner refactoringDetector;
	private boolean verbose;
	private boolean aggregate;
	private int commitsCount;
	private int errorCommitsCount;
	private Counter c;// = new Counter();
	private Map<MotivationType, Counter> cMap;
	private static final int TP = 0;
	private static final int FP = 1;
	private static final int FN = 2;
	private static final int TN = 3;
	private static final int UNK = 4;

	private BigInteger refactoringFilter;

	public MotivationTestBuilder(GitHistoryRefactoringMiner detector, String tempDir) {
		this.map = new HashMap<String, ProjectMatcher>();
		this.refactoringDetector = detector;
		this.tempDir = tempDir;
		this.verbose = false;
		this.aggregate = false;
	}

	public MotivationTestBuilder(GitHistoryRefactoringMiner detector, String tempDir, BigInteger refactorings) {
		this(detector, tempDir);

		this.refactoringFilter = refactorings;
	}

	public MotivationTestBuilder verbose() {
		this.verbose = true;
		return this;
	}

	public MotivationTestBuilder withAggregation() {
		this.aggregate = true;
		return this;
	}

	private static class Counter {
		int[] c = new int[5];
	}

	private void count(int type, String motivationDescription) {
		c.c[type]++;
		MotivationType motivationType = MotivationType.extractFromDescription(motivationDescription);
		Counter motivationTypeCounter = cMap.get(motivationType);
		if (motivationTypeCounter == null) {
			motivationTypeCounter = new Counter();
			cMap.put(motivationType, motivationTypeCounter);
			}
		motivationTypeCounter.c[type]++;
	}

	private int get(int type) {
		return c.c[type];
	}

	private int get(int type, Counter counter) {
		return counter.c[type];
	}

	public final ProjectMatcher project(String cloneUrl, String branch) {
		ProjectMatcher projectMatcher = this.map.get(cloneUrl);
		if (projectMatcher == null) {
			projectMatcher = new ProjectMatcher(cloneUrl, branch);
			this.map.put(cloneUrl, projectMatcher);
		}
		return projectMatcher;
	}	
	public void assertExpectations() throws Exception {
		c = new Counter();
		cMap = new HashMap<MotivationType, Counter>();
		commitsCount = 0;
		errorCommitsCount = 0;
		GitService gitService = new GitServiceImpl();
		
		
		GitHistoryRefactoringMinerImpl refactoringDetectorImpl = (GitHistoryRefactoringMinerImpl)refactoringDetector;
		StringBuilder JSON = new StringBuilder();
		JSON.append("{").append("\n");
		JSON.append("\"").append("commits").append("\"").append(": ");
		JSON.append("[");
		for (ProjectMatcher m : map.values()) {
			String folder = tempDir + "/"
					+ m.cloneUrl.substring(m.cloneUrl.lastIndexOf('/') + 1, m.cloneUrl.lastIndexOf('.'));
			try (Repository rep = gitService.cloneIfNotExists(folder,
				m.cloneUrl/* , m.branch */)) {
				if (m.ignoreNonSpecifiedCommits) {
					// It is faster to only look at particular commits
					for (String commitId : m.getCommits()) {
						refactoringDetector.detectAtCommit(rep, commitId, m);
						//refactoringDetector.detectAtCommit(m.cloneUrl, commitId, m, 100);
					}	
					
				} else {
					// Iterate over each commit
					//refactoringDetector.detectAll(rep, m.branch, m);
				}
			}
		}
		refactoringDetectorImpl.getStringJSON().deleteCharAt(refactoringDetectorImpl.getStringJSON().toString().length()-1);
		refactoringDetectorImpl.getStringJSON().append("]");
		refactoringDetectorImpl.getStringJSON().append("}");
		refactoringDetectorImpl.getStringJSON().append("\n");
		JSON.append(refactoringDetectorImpl.getStringJSON());
		try (FileOutputStream oS = new FileOutputStream(new File("Refactorings.json"))) {
			oS.write(JSON.toString().getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println(String.format("Commits: %d  Errors: %d", commitsCount, errorCommitsCount));

		String mainResultMessage = buildResultMessage(c);
		System.out.println("Total  " + mainResultMessage);
		for (MotivationType motivType : MotivationType.values()) {
			Counter motivationTypeCounter = cMap.get(motivType);
			if (motivationTypeCounter != null) {
				System.out
						.println(String.format("%-50s", motivType.toString()) + buildResultMessage(motivationTypeCounter));
			}
		}

		boolean success = get(FP) == 0 && get(FN) == 0 && get(TP) > 0;
		if (!success || verbose) {
			for (ProjectMatcher m : map.values()) {
				m.printResults();
			}
		}
		Assert.assertTrue(mainResultMessage, success);
	}

	private String buildResultMessage(Counter c) {
		double precision = ((double) get(TP, c) / (get(TP, c) + get(FP, c)));
		double recall = ((double) get(TP, c)) / (get(TP, c) + get(FN, c));
		String mainResultMessage = String.format(
				"TP: %2d  FP: %2d  FN: %2d  TN: %2d  Unk.: %2d  Prec.: %.3f  Recall: %.3f", get(TP, c), get(FP, c),
				get(FN, c), get(TN, c), get(UNK, c), precision, recall);
		return mainResultMessage;
	}

	private List<String> normalize(String refactoring) {
		RefactoringType refType = RefactoringType.extractFromDescription(refactoring);
		refactoring = normalizeSingle(refactoring);
		if (aggregate) {
			refactoring = refType.aggregate(refactoring);
		} else {
			int begin = refactoring.indexOf("from classes [");
			if (begin != -1) {
				int end = refactoring.lastIndexOf(']');
				String types = refactoring.substring(begin + "from classes [".length(), end);
				String[] typesArray = types.split(", ");
				List<String> refactorings = new ArrayList<String>();
				for (String type : typesArray) {
					refactorings.add(refactoring.substring(0, begin) + "from class " + type);
				}
				return refactorings;
			}
		}
		return Collections.singletonList(refactoring);
	}

	/**
	 * Remove generics type information.
	 */
	private static String normalizeSingle(String refactoring) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < refactoring.length(); i++) {
			char c = refactoring.charAt(i);
			if (c == '\t') {
				c = ' ';
			}
			sb.append(c);
		}
		return sb.toString();
	}

	/*
	 * private static String normalizeMotivation(String motivation) { String
	 * normalizedString = ""; StringBuilder sb = new StringBuilder(); for (int i =
	 * 0; i < motivation.length(); i++) { char c = motivation.charAt(i); if (c ==
	 * '_') { c = ' '; } if(c == ':') { c = ' ' ; } sb.append(c); } normalizedString
	 * = sb.toString().replace("  ", " "); return normalizedString.toLowerCase(); }
	 */
	public class ProjectMatcher extends RefactoringHandler {

		private final String cloneUrl;
		public String getCloneUrl() {
			return cloneUrl;
		}
		private final String branch;
		private Map<String, CommitMatcher> expected = new HashMap<>();
		private boolean ignoreNonSpecifiedCommits = true;
		private int truePositiveCount = 0;
		private int falsePositiveCount = 0;
		private int falseNegativeCount = 0;
		private int trueNegativeCount = 0;
		private int unknownCount = 0;
		// private int errorsCount = 0;

		private ProjectMatcher(String cloneUrl, String branch) {
			this.cloneUrl = cloneUrl;
			this.branch = branch;
		}

		public ProjectMatcher atNonSpecifiedCommitsContainsNothing() {
			this.ignoreNonSpecifiedCommits = false;
			return this;
		}

		public CommitMatcher atCommit(String commitId) {
			CommitMatcher m = expected.get(commitId);
			if (m == null) {
				m = new CommitMatcher();
				expected.put(commitId, m);
			}
			return m;
		}

		public Set<String> getCommits() {
			return expected.keySet();
		}
		@Override
		public boolean skipCommit(String commitId) {
			if (this.ignoreNonSpecifiedCommits) {
				return !this.expected.containsKey(commitId);
			}
			return false;
		}

		@Override
		public void handle(String commitId, List<Refactoring> refactorings,
				Map<Refactoring, List<MotivationType>> mapRefactoringMotivations) {
			refactorings= filterRefactoring(refactorings);
			CommitMatcher matcher;
			commitsCount++;
			//String commitId = curRevision.getId().getName();
			if (expected.containsKey(commitId)) {
				matcher = expected.get(commitId);
			} else if (!this.ignoreNonSpecifiedCommits) {
				matcher = this.atCommit(commitId);
				matcher.containsOnly();
			} else {
				// ignore this commit
				matcher = null;
			}
			if (matcher != null) {
				matcher.analyzed = true;
				Set<String> refactoringsFound = new HashSet<String>();
				Set<String> motivationsFound = new HashSet<String>();
				Set<String> motivationRefactorings = new HashSet<String>();
				Set<String> expectedMotivations = new HashSet<String>();
				for (Refactoring ref : mapRefactoringMotivations.keySet()) {
					List<MotivationType> listMotivations = mapRefactoringMotivations.get(ref);
					for(MotivationType m: listMotivations) {
						motivationsFound.add(m.getDescription());	
					}	
				}
				for (Refactoring refactoring : refactorings) {
					refactoringsFound.addAll(normalize(refactoring.toString()));
				}
				
				// count true positives
				for (Iterator<String> iter = matcher.motivationRefactorings.iterator(); iter.hasNext();) {
					  motivationRefactorings.add(iter.next());
				}
				for (Iterator<String> iter = matcher.expectedMotivations.iterator(); iter.hasNext();) {
					 expectedMotivations.add(iter.next());
				}
				for (Iterator<String> iter = matcher.expectedMotivations.iterator(); iter.hasNext();) {
					String expectedMotivation = iter.next();
					if (motivationsFound.contains(expectedMotivation)) {
						iter.remove();
						motivationsFound.remove(expectedMotivation);
						this.truePositiveCount++;
						count(TP, expectedMotivation);
						matcher.truePositive.add(expectedMotivation); 			 
						}
				}
				
				// count false positives
				for (Iterator<String> iter = matcher.notExpected.iterator(); iter.hasNext();) {
					String notExpectedMotivations = iter.next();
					if (motivationsFound.contains(notExpectedMotivations)) {
						motivationsFound.remove(notExpectedMotivations);
						this.falsePositiveCount++;
						count(FP, notExpectedMotivations);
					} else {
						this.trueNegativeCount++;
						count(TN, notExpectedMotivations);
						iter.remove();
					}
				}
				// count false positives when using containsOnly
				if (matcher.ignoreNonSpecified) {
					for (String motivation : motivationsFound) {
						matcher.unknown.add(motivation);
						this.unknownCount++;
						count(UNK,motivation);
					}
				} else {
					for (String motivation : motivationsFound) {
						matcher.notExpected.add(motivation);
						this.falsePositiveCount++;
						count(FP, motivation);
					}
				}

				// count false negatives
				for (String expectedButNotFound : matcher.expectedMotivations) {
					this.falseNegativeCount++;
					count(FN, expectedButNotFound);
				}
			}
			
		}
			

		private List<Refactoring> filterRefactoring(List<Refactoring> refactorings) {
			List<Refactoring> filteredRefactorings = new ArrayList<>();

			for (Refactoring refactoring : refactorings) {
				BigInteger value = Enum.valueOf(Refactorings.class, refactoring.getName().replace(" ", "")).getValue();
				if (value.and(refactoringFilter).compareTo(BigInteger.ZERO) == 1) {
					filteredRefactorings.add(refactoring);
				}
			}
			
			return filteredRefactorings;
		}

		@Override
		public void handleException(String commitId, Exception e) {
			if (expected.containsKey(commitId)) {
				CommitMatcher matcher = expected.get(commitId);
				matcher.error = e.toString();
			}
			errorCommitsCount++;
			// System.err.println(" error at commit " + commitId + ": " +
			// e.getMessage());
		}

		private void printResults() {
			// if (verbose || this.falsePositiveCount > 0 ||
			// this.falseNegativeCount > 0 || this.errorsCount > 0) {
			// System.out.println(this.cloneUrl);
			// }
			String baseUrl = this.cloneUrl.substring(0, this.cloneUrl.length() - 4) + "/commit/";
			for (Map.Entry<String, CommitMatcher> entry : this.expected.entrySet()) {
				String commitUrl = baseUrl + entry.getKey();
				CommitMatcher matcher = entry.getValue();
				if (matcher.error != null) {
					System.out.println("error at " + commitUrl + ": " + matcher.error);
				} else {
					if (verbose || !matcher.expectedMotivations.isEmpty() || !matcher.notExpected.isEmpty()
							|| !matcher.unknown.isEmpty()) {
						if (!matcher.analyzed) {
							System.out.println("at not analyzed " + commitUrl);
						} else {
							System.out.println("at " + commitUrl);
						}
					}
					if (verbose && !matcher.truePositive.isEmpty()) {
						System.out.println(" true positives");
						for (String ref : matcher.truePositive) {
							System.out.println("  " + ref);
						}
					}
					if (!matcher.notExpected.isEmpty()) {
						System.out.println(" false positives");
						for (String ref : matcher.notExpected) {
							System.out.println("  " + ref);
						}
					}
					if (!matcher.expectedMotivations.isEmpty()) {
						System.out.println(" false negatives");
						for (String ref : matcher.expectedMotivations) {
							System.out.println("  " + ref);
						}
					}
					if (!matcher.unknown.isEmpty()) {
						System.out.println(" unknown");
						for (String ref : matcher.unknown) {
							System.out.println("  " + ref);
						}
					}
				}
			}
		}

		// private void countFalseNegatives() {
		// for (Map.Entry<String, CommitMatcher> entry :
		// this.expected.entrySet()) {
		// CommitMatcher matcher = entry.getValue();
		// if (matcher.error == null) {
		// this.falseNegativeCount += matcher.expected.size();
		// }
		// }
		// }

		public class CommitMatcher {
			private Set<String> expected = new HashSet<String>();
			private Set<String> expectedMotivations = new HashSet<String>();
			private Set<String> motivationRefactorings = new HashSet<String>();
			private Set<String> notExpected = new HashSet<String>();
			private Set<String> truePositive = new HashSet<String>();
			private Set<String> unknown = new HashSet<String>();
			private boolean ignoreNonSpecified = true;
			private boolean analyzed = false;
			private String error = null;

			private CommitMatcher() {
			}

			public ProjectMatcher contains(String... refactorings) {
				for (String refactoring : refactorings) {
					expected.addAll(normalize(refactoring));
				}
				return ProjectMatcher.this;
			}
			public ProjectMatcher containsMotivation(String[] motivations, String[] refactorings ) {
				this.ignoreNonSpecified = false;
				this.expectedMotivations = new HashSet<String>();
				this.notExpected = new HashSet<String>();
				for (String m : motivations) {
					expectedMotivations.add(m);
				}
				for(String ref : refactorings) {
					motivationRefactorings.add(ref);
				}
				return ProjectMatcher.this;
			}
			public ProjectMatcher containsOnly(String... refactorings) {
				this.ignoreNonSpecified = false;
				this.expected = new HashSet<String>();
				this.notExpected = new HashSet<String>();
				for (String refactoring : refactorings) {
					expected.addAll(normalize(refactoring));
				}
				return ProjectMatcher.this;
			}

			public ProjectMatcher containsNothing() {
				return containsOnly();
			}

			public ProjectMatcher notContains(String... refactorings) {
				for (String refactoring : refactorings) {
					notExpected.addAll(normalize(refactoring));
				}
				return ProjectMatcher.this;
			}
		}
	}
}
