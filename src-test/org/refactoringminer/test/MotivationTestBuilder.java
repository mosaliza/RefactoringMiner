package org.refactoringminer.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
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

import com.sun.javafx.applet.ExperimentalExtensions;

import gr.uom.java.xmi.diff.MotivationType;
import gr.uom.java.xmi.diff.MotivationFlag;
import org.refactoringminer.test.MotivationPopulator.Refactorings;
import org.refactoringminer.test.MotivationTestBuilder.ProjectMatcher.CommitMatcher.RefactoringMatcher;

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
	StringBuilder motivationsARM = new StringBuilder();
	StringBuilder faciliateExtensionAnalysis = new StringBuilder();
	StringBuilder decomposeToImproveRedabilityAnalysis = new StringBuilder();
	public MotivationTestBuilder(GitHistoryRefactoringMiner detector, String tempDir) {
		this.map = new HashMap<String, ProjectMatcher>();
		this.refactoringDetector = detector;
		this.tempDir = tempDir;
		this.verbose = false;
		this.aggregate = false;
	}
	
	public static String replaceIfExists(String s, String what, String with) {
		return s.contains(what) ? s.replace(what, with) : s;
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
			
			Set<String> extractMotivations = new HashSet<String>();
			for(MotivationType motivation : MotivationType.values()) {
				if(motivation.toString().startsWith("EM")) {		
					extractMotivations.add(motivation.getDescription());
				}
			}
			
			motivationsARM.append("Commit URL").append("|").append("Extract Refactoring Description").append("|");
			for(String motivation : extractMotivations) {
				motivationsARM.append(motivation).append("|");
			}
			motivationsARM.deleteCharAt(motivationsARM.length()-1);
			motivationsARM.append("\n");
			
			
			faciliateExtensionAnalysis.append("ResultType").append("|").append("Commit URL").append("|").append("Extract Refactoring Description")
			.append("|").append("Removed(T1)").append("|").append("Added(T2)").append("\n");
			
			decomposeToImproveRedabilityAnalysis.append("ResultType").append("|").append("Commit URL").append("|").append("Extract Refactoring Description")
			.append("|").append("Extracted Code Size").append("\n");
			
			for (ProjectMatcher m : map.values()) {
				//m.printResults();
				m.printRefactoringMatcherResults(extractMotivations);
			}
			//Writing FP motivations to CSV file for association rule mining.
			try (FileOutputStream oS = new FileOutputStream(new File("MotivationsARM.csv"))) {
				oS.write(motivationsARM.toString().getBytes());
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			//Writing FP and TP Removed and Added nodes to CSV file for Analysis.
			try (FileOutputStream oS = new FileOutputStream(new File("FacilitateExtensionAnalysis.csv"))) {
				oS.write(faciliateExtensionAnalysis.toString().getBytes());
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			//Writing Extracted Code Size for Decompose to improve Readability to CSV file for Analysis.
			try (FileOutputStream oS = new FileOutputStream(new File("DecomposeToImproveReadabilityAnalysis.csv"))) {
				oS.write(decomposeToImproveRedabilityAnalysis.toString().getBytes());
			} catch (IOException e) {
				e.printStackTrace();
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

		public Set<String> getRefactoringDescriptions(String commitID) {

			Set<String> refactoringDescriptions = new HashSet<String>();
			CommitMatcher commitMatcher = expected.get(commitID);
			for(String refactoringDescription : commitMatcher.expected1.keySet()) {
				refactoringDescriptions.add(refactoringDescription);
			}
			
			return refactoringDescriptions;
		}
		
		@Override
		public boolean skipCommit(String commitId) {
			if (this.ignoreNonSpecifiedCommits) {
				return !this.expected.containsKey(commitId);
			}
			return false;
		}
		
		Set<String> getDetectedRefactoringMotivations(String refactoringDescription , Map<Refactoring, List<MotivationType>> mapRefactoringMotivations){
			Set<String> motivationsDetected = new HashSet<String>();
			for (Refactoring refactoring : mapRefactoringMotivations.keySet()) {
				if(replaceIfExists(refactoring.toString(),"\t"," ").equals(refactoringDescription)) {			
					List<MotivationType> listMotivations = mapRefactoringMotivations.get(refactoring);
					for(MotivationType m: listMotivations) {
						motivationsDetected.add(m.getDescription());	
					}
					break;
				}
			}		
			return motivationsDetected;

		}
		
		
		
		@Override
		public void handle(String commitId, List<Refactoring> refactorings,
				Map<Refactoring, List<MotivationType>> mapRefactoringMotivations,
				Map<Refactoring, List<MotivationFlag>> mapMotivationFlag,
				Map<Refactoring, int[]> mapFacilitateExtensionT1T2,
				Map<Refactoring, String> mapDecomposeToImproveRedability) {
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
				Set<String> refactoringDetectedMotivations = new HashSet<String>();
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
				
				Set<String> refactoringDescriptions = getRefactoringDescriptions(commitId);
				for(String refactoringDescription : refactoringDescriptions) {
					refactoringDetectedMotivations = getDetectedRefactoringMotivations(refactoringDescription, mapRefactoringMotivations);
					// Computing the Test Results
					RefactoringMatcher refactoringMatcher = matcher.atRefactoring(refactoringDescription);
					
					//Setting the refactoringMatcher data for the removed and added in Facilitate Extension
					for(Refactoring ref : mapFacilitateExtensionT1T2.keySet()) {
						if(normalizeSingle(ref.toString()).equals(normalizeSingle(refactoringDescription))) {
							refactoringMatcher.facilitateExtensionT1T2 =  mapFacilitateExtensionT1T2.get(ref);
							break;
						}
					}
					
					//Setting the refactoringMatcher data for size of Extracted method for Decompose to Improve Readability
					for(Refactoring ref : mapDecomposeToImproveRedability.keySet()) {
						if(normalizeSingle(ref.toString()).equals(normalizeSingle(refactoringDescription))) {
							refactoringMatcher.decomposeToImproveReadabilityExtractedCodeSize =  mapDecomposeToImproveRedability.get(ref);
							break;
						}
					}
					
					
					for (Iterator<String> iter = refactoringMatcher.expectedMotivations.iterator(); iter.hasNext();) {
						String expectedMotivation = iter.next();
						if (refactoringDetectedMotivations.contains(expectedMotivation)) {
							iter.remove();
							refactoringDetectedMotivations.remove(expectedMotivation);
							this.truePositiveCount++;
							count(TP, expectedMotivation);
							matcher.truePositive.add(expectedMotivation);
							refactoringMatcher.truePositive.add(expectedMotivation);
							}
					}
					
					// count false positives
					for (Iterator<String> iter = refactoringMatcher.notExpected.iterator(); iter.hasNext();) {
						String notExpectedMotivations = iter.next();
						if (refactoringDetectedMotivations.contains(notExpectedMotivations)) {
							refactoringDetectedMotivations.remove(notExpectedMotivations);
							this.falsePositiveCount++;
							count(FP, notExpectedMotivations);
						} else {
							this.trueNegativeCount++;
							count(TN, notExpectedMotivations);
							iter.remove();
						}
					}
					// count false positives when using containsOnly
					if (refactoringMatcher.ignoreNonSpecified) {
						for (String motivation : refactoringDetectedMotivations) {
							matcher.unknown.add(motivation);
							refactoringMatcher.unknown.add(motivation);
							this.unknownCount++;
							count(UNK,motivation);
						}
					} else {
						for (String motivation : refactoringDetectedMotivations) {
							matcher.notExpected.add(motivation);
							refactoringMatcher.notExpected.add(motivation);
							this.falsePositiveCount++;
							count(FP, motivation);
						}
					}
					
					
				/*	expectedMotivations.stream().filter(s -> s!=null)
						.forEach(e ->{
							this.falseNegativeCount ++ ;
							count(FN,e);
						}); */
					
					// count false negatives
					for (String expectedButNotFound : refactoringMatcher.expectedMotivations) {
							this.falseNegativeCount++;
							count(FN, expectedButNotFound);			
					}
				}
				
		
				
				/*for (Iterator<String> iter = matcher.expectedMotivations.iterator(); iter.hasNext();) {
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
				} */
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
						if (matcher.notExpected.iterator().hasNext() && 
								!matcher.notExpected.iterator().next().equals("none")) {
							System.out.println(" false positives");
							for (String ref : matcher.notExpected) {
									System.out.println("  " + ref);
							}
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
		private void printRefactoringMatcherResults(Set<String> extractMotivations) {

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
							for(String  refactoringDescription : matcher.expected1.keySet()) {
								
								RefactoringMatcher refactoringMatcher = matcher.atRefactoring(refactoringDescription);

								System.out.println(refactoringDescription);

								if (verbose && !refactoringMatcher.truePositive.isEmpty()) {
									System.out.println(" true positives");
									for (String ref : refactoringMatcher.truePositive) {
										if(ref.equals("EM: Facilitate extension")) {
											System.out.println("  " + ref+ " "+ refactoringMatcher.facilitateExtensionT1T2[0]+","+refactoringMatcher.facilitateExtensionT1T2[1]);
											faciliateExtensionAnalysis.append("TP").append("|").append(commitUrl).append("|").append(refactoringDescription).append("|").
											append(refactoringMatcher.facilitateExtensionT1T2[0]).append("|").append(refactoringMatcher.facilitateExtensionT1T2[1]).append("\n");
											}
											else if(ref.equals("EM: Decompose method to improve readability")){
												System.out.println("  " + ref+ " "+ refactoringMatcher.decomposeToImproveReadabilityExtractedCodeSize);
												decomposeToImproveRedabilityAnalysis.append("TP").append("|").append(commitUrl).append("|").append(refactoringDescription).append("|").
												append(refactoringMatcher.decomposeToImproveReadabilityExtractedCodeSize).append("\n");
											}else {
												System.out.println("  " + ref);
											}
										}
									}
								
								motivationsARM.append("\"").append(commitUrl).append("\"").append("|").append(refactoringDescription).append("|");
								if (!refactoringMatcher.notExpected.isEmpty()) {
									if (refactoringMatcher.notExpected.iterator().hasNext() && 
											!refactoringMatcher.notExpected.iterator().next().equals("none")) {
										System.out.println(" false positives");
										for (String ref : refactoringMatcher.notExpected) {

											if (ref.equals("EM: Facilitate extension")) {
												System.out.println(
														"  " + ref + " " + refactoringMatcher.facilitateExtensionT1T2[0]
																+ "," + refactoringMatcher.facilitateExtensionT1T2[1]);
												faciliateExtensionAnalysis.append("FP").append("|").append(commitUrl)
														.append("|").append(refactoringDescription).append("|")
														.append(refactoringMatcher.facilitateExtensionT1T2[0])
														.append("|")
														.append(refactoringMatcher.facilitateExtensionT1T2[1])
														.append("\n");
											} else if (ref.equals("EM: Decompose method to improve readability")) {
												System.out.println("  " + ref + " "
														+ refactoringMatcher.decomposeToImproveReadabilityExtractedCodeSize);
												decomposeToImproveRedabilityAnalysis.append("FP").append("|")
														.append(commitUrl).append("|").append(refactoringDescription)
														.append("|")
														.append(refactoringMatcher.decomposeToImproveReadabilityExtractedCodeSize)
														.append("\n");
											} else {
												System.out.println("  " + ref);
											}
											// motivationsARM.append(ref).append("|");

										}
									}
								}
								
								for(String motivation : extractMotivations) {
									boolean motivationIsDetected = false;
									for(String  expectedMotivation : refactoringMatcher.notExpected) {
										if(expectedMotivation.equals(motivation)){
											motivationIsDetected = true;

										}
									}
									if(motivationIsDetected) {
										motivationsARM.append("1").append("|");
									}else {
										motivationsARM.append("0").append("|");
									}
								}
					
								
								if (!refactoringMatcher.expectedMotivations.isEmpty()) {
									System.out.println(" false negatives");
									for (String ref : refactoringMatcher.expectedMotivations) {
										System.out.println("  " + ref);
									}
								}
								if (!refactoringMatcher.unknown.isEmpty()) {
									System.out.println(" unknown");
									for (String ref : refactoringMatcher.unknown) {
										System.out.println("  " + ref);
									}
								}
								motivationsARM.deleteCharAt(motivationsARM.length()-1);
								motivationsARM.append("\n");
							}
							
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
			private Map<String, RefactoringMatcher> expected1 = new HashMap<>();
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
			public ProjectMatcher containsMotivation(String[] motivations,  String[] refactorings ) {
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
			// Refactoring Matcher Mock Up builder for Commits
			public RefactoringMatcher atRefactoring(String refactoringDescription) {
				RefactoringMatcher m = expected1.get(refactoringDescription);
				if (m == null) {
					m = new RefactoringMatcher();
					expected1.put(refactoringDescription, m);
				}
				return m;
			}
			public class RefactoringMatcher{
				private Set<String> expectedMotivations = new HashSet<String>();
				private int[] facilitateExtensionT1T2 = new int[2];
				private String decomposeToImproveReadabilityExtractedCodeSize ="";
				private Set<String> notExpected = new HashSet<String>();
				private Set<String> truePositive = new HashSet<String>();
				private Set<String> unknown = new HashSet<String>();
				private boolean ignoreNonSpecified = true;
				private RefactoringMatcher() {
				}
				
				public ProjectMatcher containsMotivation(String[] motivations ) {
					this.ignoreNonSpecified = false;
					this.expectedMotivations = new HashSet<String>();
					this.notExpected = new HashSet<String>();
					for (String m : motivations) {
						if( m != null) {
							expectedMotivations.add(m);
						}
					}					
					return ProjectMatcher.this;					
				}
			}
		}
	}
}
