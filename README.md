# RefactoringMiner
RefactoringMiner is a library/API written in Java that can detect refactorings applied in the history of a Java project.

Currently, it supports the detection of the following refactorings:

1. Extract Method
2. Inline Method
3. Rename Method
4. Move Method/Attribute
5. Pull Up Method/Attribute
6. Push Down Method/Attribute
7. Extract Class/Subclass/Superclass/Interface
8. Move Class
9. Rename Class
10. Move and Rename Class
11. Extract and Move Method
12. Move Source Folder
13. Change Package (Move, Rename, Split, Merge)
14. Extract Variable
15. Inline Variable
16. Parameterize Variable
17. Rename Variable/Parameter
18. Rename Attribute
19. Move and Rename Attribute
20. Replace Variable with Attribute
21. Replace Attribute (with Attribute)
22. Merge Variable/Parameter
23. Merge Attribute
24. Split Variable/Parameter
25. Split Attribute

In order to build the project, run `./gradlew jar` (or `gradlew jar`, in Windows) in the project's root directory.
Alternatively, you can generate a complete distribution zip including all runtime dependencies running `./gradlew distZip`.

You can also work with the project with Eclipse IDE. First, run `./gradlew eclipse` to generate Eclipse project metadata files. Then, import it into Eclipse using the *Import Existing Project* feature.

## Research ##
If you are using RefactoringMiner in your research, please cite the following paper:

Nikolaos Tsantalis, Matin Mansouri, Laleh Eshkevari, Davood Mazinanian, and Danny Dig, "[Accurate and Efficient Refactoring Detection in Commit History](https://users.encs.concordia.ca/~nikolaos/publications/ICSE_2018.pdf)," *40th International Conference on Software Engineering* (ICSE 2018), Gothenburg, Sweden, May 27 - June 3, 2018.

RefactoringMiner has been used in the following studies:
1. Danilo Silva, Nikolaos Tsantalis, and Marco Tulio Valente, "[Why We Refactor? Confessions of GitHub Contributors](https://doi.org/10.1145/2950290.2950305)," *24th ACM SIGSOFT International Symposium on the Foundations of Software Engineering* (FSE 2016), Seattle, WA, USA, November 13-18, 2016.
2. Diego Cedrim, Alessandro Garcia, Melina Mongiovi, Rohit Gheyi, Leonardo Sousa, Rafael de Mello, Baldoino Fonseca, Márcio Ribeiro, and Alexander Chávez, "[Understanding the impact of refactoring on smells: a longitudinal study of 23 software projects](https://doi.org/10.1145/3106237.3106259)," *11th Joint Meeting on Foundations of Software Engineering* (ESEC/FSE 2017), Paderborn, Germany, September 4-8, 2017.
3. Alexander Chávez, Isabella Ferreira, Eduardo Fernandes, Diego Cedrim, and Alessandro Garcia, "[How does refactoring affect internal quality attributes?: A multi-project study](https://doi.org/10.1145/3131151.3131171)," *31st Brazilian Symposium on Software Engineering* (SBES 2017), Fortaleza, CE, Brazil, September 20-22, 2017.
4. Navdeep Singh, and Paramvir Singh, "[How Do Code Refactoring Activities Impact Software Developers' Sentiments? - An Empirical Investigation Into GitHub Commits](https://doi.org/10.1109/APSEC.2017.79)," *24th Asia-Pacific Software Engineering Conference* (APSEC 2017), Nanjing, Jiangsu, China, December 4-8, 2017.
5. Mehran Mahmoudi, and Sarah Nadi, "[The Android Update Problem: An Empirical Study](https://doi.org/10.1145/3196398.3196434)," *15th International Conference on Mining Software Repositories* (MSR 2018), Gothenburg, Sweden, May 28-29, 2018.
6. Anthony Peruma, Mohamed Wiem Mkaouer, Michael J. Decker, and Christian D. Newman, "[An empirical investigation of how and why developers rename identifiers](https://doi.org/10.1145/3242163.3242169)," *2nd International Workshop on Refactoring* (IWoR 2018), Montpellier, France, September 4, 2018.
7. Patanamon Thongtanunam, Weiyi Shang, and Ahmed E. Hassan, "[Will this clone be short-lived? Towards a better understanding of the characteristics of short-lived clones](https://doi.org/10.1007/s10664-018-9645-2)," Empirical Software Engineering, pp. 1-36, 2018.
8. Isabella Ferreira, Eduardo Fernandes, Diego Cedrim, Anderson Uchôa, Ana Carla Bibiano, Alessandro Garcia, João Lucas Correia, Filipe Santos, Gabriel Nunes, Caio Barbosa, Baldoino Fonseca, and Rafael de Mello, "[The buggy side of code refactoring: understanding the relationship between refactorings and bugs](https://doi.org/10.1145/3183440.3195030)," *40th International Conference on Software Engineering: Companion Proceedings* (ICSE 2018), Gothenburg, Sweden, May 27-June 3, 2018.
9. Matheus Paixao, "[Software Restructuring: Understanding Longitudinal Architectural Changes and Refactoring](http://discovery.ucl.ac.uk/10060511/)," Ph.D. thesis, Computer Science Department, University College London, July 2018.
10. Mehran Mahmoudi, Sarah Nadi, and Nikolaos Tsantalis, "[Are Refactorings to Blame? An Empirical Study of Refactorings in Merge Conflicts](https://doi.org/10.1109/SANER.2019.8668012)," *26th IEEE International Conference on Software Analysis, Evolution and Reengineering* (SANER 2019), Hangzhou, China, February 24-27, 2019.
11. Bin Lin, Csaba Nagy, Gabriele Bavota and Michele Lanza, "[On the Impact of Refactoring Operations on Code Naturalness](https://doi.org/10.1109/SANER.2019.8667992)," *26th IEEE International Conference on Software Analysis, Evolution and Reengineering* (SANER 2019), Hangzhou, China, February 24-27, 2019.
12. Sarah Fakhoury, Devjeet Roy, Sk. Adnan Hassan, and Venera Arnaoudova, "[Improving Source Code Readability: Theory and Practice](https://doi.org/10.1109/ICPC.2019.00014)," *27th IEEE/ACM International Conference on Program Comprehension* (ICPC 2019), Montreal, QC, Canada, May 25-26, 2019.
13. Carmine Vassallo, Giovanni Grano, Fabio Palomba, Harald C. Gall, and Alberto Bacchelli, "[A large-scale empirical exploration on refactoring activities in open source software projects](https://doi.org/10.1016/j.scico.2019.05.002)," *Science of Computer Programming*, 2019.


## Contributors ##
The code in package **gr.uom.java.xmi.*** has been developed by [Nikolaos Tsantalis](https://github.com/tsantalis).

The code in package **org.refactoringminer.*** has been developed by [Danilo Ferreira e Silva](https://github.com/danilofes).

## API usage guidelines ##

RefactoringMiner can automatically detect refactorings in the entire history of 
git repositories, between specified commits or tags, or at specified commits.

In the code snippet below we demonstrate how to print all refactorings performed
in the toy project https://github.com/danilofes/refactoring-toy-example.git.

```java
GitService gitService = new GitServiceImpl();
GitHistoryRefactoringMiner miner = new GitHistoryRefactoringMinerImpl();

Repository repo = gitService.cloneIfNotExists(
    "tmp/refactoring-toy-example",
    "https://github.com/danilofes/refactoring-toy-example.git");

miner.detectAll(repo, "master", new RefactoringHandler() {
  @Override
  public void handle(RevCommit commitData, List<Refactoring> refactorings) {
    System.out.println("Refactorings at " + commitData.getId().getName());
    for (Refactoring ref : refactorings) {
      System.out.println(ref.toString());
    }
  }
});
```

You can also analyze between commits using `detectBetweenCommits` or between tags using `detectBetweenTags`. RefactoringMiner will start from commit or tag as specified and iterate backwards. If the end commit or end tag is not specified, RefactoringMiner will detect until the first beginning.

```java
// first commit: 819b202bfb09d4142dece04d4039f1708735019b
// last commit: d4bce13a443cf12da40a77c16c1e591f4f985b47
// detectBetweenCommits() will process merge commits, while detectAll() skips merge commits
miner.detectBetweenCommits(repo, 
    "819b202bfb09d4142dece04d4039f1708735019b", "d4bce13a443cf12da40a77c16c1e591f4f985b47",
    new RefactoringHandler() {
  @Override
  public void handle(RevCommit commitData, List<Refactoring> refactorings) {
    System.out.println("Refactorings at " + commitData.getId().getName());
    for (Refactoring ref : refactorings) {
      System.out.println(ref.toString());
    }
  }
});
```

```java
// first tag: 1.0
// last tag: 1.1
// detectBetweenTags() will process merge commits, while detectAll() skips merge commits
miner.detectBetweenTags(repo, "1.0", "1.1", new RefactoringHandler() {
  @Override
  public void handle(RevCommit commitData, List<Refactoring> refactorings) {
    System.out.println("Refactorings at " + commitData.getId().getName());
    for (Refactoring ref : refactorings) {
      System.out.println(ref.toString());
    }
  }
});
```

It is possible to analyze a specifc commit using `detectAtCommit` instead of `detectAll`. The commit
is identified by its SHA key, such as in the example below:

```java
miner.detectAtCommit(repo, "https://github.com/danilofes/refactoring-toy-example.git",
    "05c1e773878bbacae64112f70964f4f2f7944398", new RefactoringHandler() {
  @Override
  public void handle(RevCommit commitData, List<Refactoring> refactorings) {
    System.out.println("Refactorings at " + commitData.getId().getName());
    for (Refactoring ref : refactorings) {
      System.out.println(ref.toString());
    }
  }
});
```
You can get the churn of a specific commit using `churnAtCommit` as follows:
```java
Churn churn = miner.churnAtCommit(repo, "05c1e773878bbacae64112f70964f4f2f7944398", handler);
```

There is also a lower level API that works comparing the source code from two
folders that contain the code before and after the code changes:  

```java
// Assuming you have a List<String> of the changed/added/removed file paths from version1 to version2
// filename format example: /src/gr/uom/java/xmi/UMLModelASTReader.java

File rootFolder1 = new File("/path/to/version1/");
File rootFolder2 = new File("/path/to/version2/");
List<String> filePaths1 = new ArrayList<String>();
filePaths1.add("/src/package/Foo.java");
List<String> filePaths2 = new ArrayList<String>();
filePaths2.add("/src/package/Foo.java");

UMLModel model1 = new UMLModelASTReader(rootFolder1, filePaths1).getUmlModel();
UMLModel model2 = new UMLModelASTReader(rootFolder2, filePaths2).getUmlModel();
UMLModelDiff modelDiff = model1.diff(model2);
List<Refactoring> refactorings = modelDiff.getRefactorings();
```

Finally, if you don't want to clone locally the repository, you can use the following code snippet:

```java
GitHistoryRefactoringMiner miner = new GitHistoryRefactoringMinerImpl();
miner.detectAtCommit("https://github.com/danilofes/refactoring-toy-example.git",
    "36287f7c3b09eff78395267a3ac0d7da067863fd", new RefactoringHandler() {
  @Override
  public void handle(RevCommit commitData, List<Refactoring> refactorings) {
    for (Refactoring ref : refactorings) {
      System.out.println(ref.toString());
    }
  }
}, 10);
```
Please make sure to provide valid GitHub credentials in the `github-credentials.properties` file.

## Location information for the detected refactorings ##
All classes implementing the `Refactoring` interface include refactoring-specific location information.
For example, `ExtractOperationRefactoring` offers the following methods:

1. `getSourceOperationCodeRangeBeforeExtraction()` : Returns the code range of the source method in the **parent** commit
2. `getSourceOperationCodeRangeAfterExtraction()` : Returns the code range of the source method in the **child** commit
3. `getExtractedOperationCodeRange()` : Returns the code range of the extracted method in the **child** commit
4. `getExtractedCodeRangeFromSourceOperation()` : Returns the code range of the extracted code fragment from the source method in the **parent** commit
5. `getExtractedCodeRangeToExtractedOperation()` : Returns the code range of the extracted code fragment to the extracted method in the **child** commit
6. `getExtractedOperationInvocationCodeRange()` : Returns the code range of the invocation to the extracted method inside the source method in the **child** commit

Each method returns a `CodeRange` object including the following properties:
```java
String filePath
int startLine
int endLine
int startColumn
int endColumn
```

## Statement matching information for the detected refactorings ##
All method-related refactoring (Extract/Inline/Move/Rename/ExtractAndMove Operation) objects come with a `UMLOperationBodyMapper` object, which can be obtained by calling method `getBodyMapper()` on the refactoring object.

Let's consider the Extract Method refactoring in commit [JetBrains/intellij-community@7ed3f27](https://github.com/JetBrains/intellij-community/commit/7ed3f273ab0caf0337c22f0b721d51829bb0c877)

![example|1665x820](https://user-images.githubusercontent.com/1483516/52974463-b0240000-338f-11e9-91e2-966f20be2514.png)

#1. You can use the following code snippet to obtain the **newly added statements** in the extracted method:
```java
ExtractOperationRefactoring refactoring = ...;
UMLOperationBodyMapper mapper = refactoring.getBodyMapper();
List<StatementObject> newLeaves = mapper.getNonMappedLeavesT2(); //newly added leaf statements
List<CompositeStatementObject> newComposites = mapper.getNonMappedInnerNodesT2(); //newly added composite statements
List<StatementObject> deletedLeaves = mapper.getNonMappedLeavesT1(); //deleted leaf statements
List<CompositeStatementObject> deletedComposites = mapper.getNonMappedInnerNodesT1(); //deleted composite statements
```
For the Extract Method Refactoring example shown above `mapper.getNonMappedLeavesT2()` returns the following statements:
```java
final String url = pageNumber == 0 ? "courses" : "courses?page=" + String.valueOf(pageNumber);
final CoursesContainer coursesContainer = getFromStepic(url,CoursesContainer.class);
return coursesContainer.meta.containsKey("has_next") && coursesContainer.meta.get("has_next") == Boolean.TRUE;
```
#2. You can use the following code snippet to obtain the **matched statements** between the original and the extracted methods:
```java
ExtractOperationRefactoring refactoring = ...;
UMLOperationBodyMapper mapper = refactoring.getBodyMapper();
for(AbstractCodeMapping mapping : mapper.getMappings()) {
  AbstractCodeFragment fragment1 = mapping.getFragment1();
  AbstractCodeFragment fragment2 = mapping.getFragment2();
  Set<Replacement> replacements = mapping.getReplacements();
  for(Replacement replacement : replacements) {
    String valueBefore = replacement.getBefore();
    String valueAfter = replacement.getAfter();
    ReplacementType type = replacement.getType();
  }
}
```
For the Extract Method Refactoring example shown above `mapping.getReplacements()` returns the following AST node replacement for the pair of matched statements:
```java
final List<CourseInfo> courseInfos = getFromStepic("courses",CoursesContainer.class).courses;
final List<CourseInfo> courseInfos = coursesContainer.courses;
```
**Replacement**: `getFromStepic("courses",CoursesContainer.class)` -> `coursesContainer`

**ReplacementType**: VARIABLE_REPLACED_WITH_METHOD_INVOCATION

#3. You can use the following code snippet to obtain the **overlapping refactorings** in the extracted method:
```java
ExtractOperationRefactoring refactoring = ...;
UMLOperationBodyMapper mapper = refactoring.getBodyMapper();
Set<Refactoring> overlappingRefactorings = mapper.getRefactorings();
```
For the Extract Method Refactoring example shown above `mapper.getRefactorings()` returns the following refactoring:

**Extract Variable** `coursesContainer : CoursesContainer` in method
`private addCoursesFromStepic(result List<CourseInfo>, pageNumber int) : boolean`
from class `com.jetbrains.edu.stepic.EduStepicConnector`

because variable `coursesContainer = getFromStepic(url,CoursesContainer.class)` has been extracted from the following statement of the original method by replacing string literal `"courses"` with variable `url`:
```java
final List<CourseInfo> courseInfos = getFromStepic("courses",CoursesContainer.class).courses;
```

## Running from the command line ##

When you build a distributable application with `./gradlew distZip`, you can run Refactoring Miner as a command line application. Extract the file under `build/distribution/RefactoringMiner.zip` in the desired location, and cd into the `bin` folder (or include it in your path). Then, run `RefactoringMiner -h` to show its usage:

    > RefactoringMiner -h

	-h															Show tips
	-a <git-repo-folder> <branch>								Detect all refactorings at <branch> for <git-repo-folder>. If <branch> is not specified, commits from all branches are analyzed.
	-bc <git-repo-folder> <start-commit-sha1> <end-commit-sha1>	Detect refactorings Between <start-commit-sha1> and <end-commit-sha1> for project <git-repo-folder>
	-bt <git-repo-folder> <start-tag> <end-tag>					Detect refactorings Between <start-tag> and <end-tag> for project <git-repo-folder>
	-c <git-repo-folder> <commit-sha1>							Detect refactorings at specified commit <commit-sha1> for project <git-repo-folder>
	-gc <git-URL> <commit-sha1> <timeout>				Detect refactorings at specified commit <commit-sha1> for project <git-URL> within the given <timeout> in seconds. All required information is obtained directly from GitHub using the credentials in github-credentials.properties
	
For example, suppose that you run:

    > git clone https://github.com/danilofes/refactoring-toy-example.git refactoring-toy-example
    > RefactoringMiner -c refactoring-toy-example 36287f7c3b09eff78395267a3ac0d7da067863fd

The output would be:

    4 refactorings found in commit 36287f7c3b09eff78395267a3ac0d7da067863fd:
      Pull Up Attribute     private age : int from class org.animals.Labrador to class org.animals.Dog
      Pull Up Attribute     private age : int from class org.animals.Poodle to class org.animals.Dog
      Pull Up Method        public getAge() : int from class org.animals.Labrador to public getAge() : int from class org.animals.Dog
      Pull Up Method        public getAge() : int from class org.animals.Poodle to public getAge() : int from class org.animals.Dog

When you run Refactoring with `-a`, `-bc`, `-bt`, after all commits are analyzed, a result `csv` file which use semicolon `;` as delimiter will be generated in the repository directory.

If you don't want to clone locally the repository, run:

    > RefactoringMiner -gc https://github.com/danilofes/refactoring-toy-example.git 36287f7c3b09eff78395267a3ac0d7da067863fd 10

and you will get the output in JSON format:

    {
	"commits": [{
		"repository": "https://github.com/danilofes/refactoring-toy-example.git",
		"sha1": "36287f7c3b09eff78395267a3ac0d7da067863fd",
		"url": "https://github.com/danilofes/refactoring-toy-example/commit/36287f7c3b09eff78395267a3ac0d7da067863fd",
		"refactorings": [{
				"type": "Pull Up Attribute",
				"description": "Pull Up Attribute private age : int from class org.animals.Labrador to class org.animals.Dog",
				"leftSideLocations": [{
					"filePath": "src/org/animals/Labrador.java",
					"startLine": 5,
					"endLine": 5,
					"startColumn": 14,
					"endColumn": 21,
					"codeElementType": "FIELD_DECLARATION",
					"description": "original attribute declaration",
					"codeElement": "age : int"
				}],
				"rightSideLocations": [{
					"filePath": "src/org/animals/Dog.java",
					"startLine": 5,
					"endLine": 5,
					"startColumn": 14,
					"endColumn": 21,
					"codeElementType": "FIELD_DECLARATION",
					"description": "pulled up attribute declaration",
					"codeElement": "age : int"
				}]
			},
			{
				"type": "Pull Up Attribute",
				"description": "Pull Up Attribute private age : int from class org.animals.Poodle to class org.animals.Dog",
				"leftSideLocations": [{
					"filePath": "src/org/animals/Poodle.java",
					"startLine": 5,
					"endLine": 5,
					"startColumn": 14,
					"endColumn": 21,
					"codeElementType": "FIELD_DECLARATION",
					"description": "original attribute declaration",
					"codeElement": "age : int"
				}],
				"rightSideLocations": [{
					"filePath": "src/org/animals/Dog.java",
					"startLine": 5,
					"endLine": 5,
					"startColumn": 14,
					"endColumn": 21,
					"codeElementType": "FIELD_DECLARATION",
					"description": "pulled up attribute declaration",
					"codeElement": "age : int"
				}]
			},
			{
				"type": "Pull Up Method",
				"description": "Pull Up Method public getAge() : int from class org.animals.Labrador to public getAge() : int from class org.animals.Dog",
				"leftSideLocations": [{
					"filePath": "src/org/animals/Labrador.java",
					"startLine": 7,
					"endLine": 9,
					"startColumn": 2,
					"endColumn": 3,
					"codeElementType": "METHOD_DECLARATION",
					"description": "original method declaration",
					"codeElement": "public getAge() : int"
				}],
				"rightSideLocations": [{
					"filePath": "src/org/animals/Dog.java",
					"startLine": 7,
					"endLine": 9,
					"startColumn": 2,
					"endColumn": 3,
					"codeElementType": "METHOD_DECLARATION",
					"description": "pulled up method declaration",
					"codeElement": "public getAge() : int"
				}]
			},
			{
				"type": "Pull Up Method",
				"description": "Pull Up Method public getAge() : int from class org.animals.Poodle to public getAge() : int from class org.animals.Dog",
				"leftSideLocations": [{
					"filePath": "src/org/animals/Poodle.java",
					"startLine": 7,
					"endLine": 9,
					"startColumn": 2,
					"endColumn": 3,
					"codeElementType": "METHOD_DECLARATION",
					"description": "original method declaration",
					"codeElement": "public getAge() : int"
				}],
				"rightSideLocations": [{
					"filePath": "src/org/animals/Dog.java",
					"startLine": 7,
					"endLine": 9,
					"startColumn": 2,
					"endColumn": 3,
					"codeElementType": "METHOD_DECLARATION",
					"description": "pulled up method declaration",
					"codeElement": "public getAge() : int"
				}]
			}
		]
	}]
	}
