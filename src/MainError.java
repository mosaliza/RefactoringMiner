import gr.uom.java.xmi.decomposition.AnonymousClassDeclarationObject;
import gr.uom.java.xmi.diff.MotivationFlag;
import gr.uom.java.xmi.diff.MotivationType;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.refactoringminer.api.*;
import org.refactoringminer.rm1.GitHistoryRefactoringMinerImpl;
import org.refactoringminer.util.GitServiceImpl;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MainError {
    private static String FOLDER_TO_CLONE = "tmp1\\";
    private static String FOLDER_ERROR= "tmp1\\111\\";
    private SessionFactory sessionFactoryObj;

    public static void main(String[] args) throws Exception {
        List<String> repositories = new ArrayList<>();
        for (String arg : args) {
            if (arg == null || arg.isEmpty())
                continue;
            if (arg.startsWith("-PF")) {
                FOLDER_TO_CLONE = arg.replace("-PF", "");
            }
            if (arg.startsWith("-RP")) {
                repositories.add(arg.replace("-RP", ""));
            }
        }

        new MainError().buildSessionFactory().start(repositories);
    }

    private static Set<String> getProcessed(String pathStr) throws IOException {
        Path path = Paths.get(pathStr);
        if (!path.toFile().exists())
            return Collections.emptySet();
        return Files.readAllLines(path)
                .stream()
                .filter(String::isEmpty)
                .map(row -> row.split(",")[0])
                .collect(Collectors.toSet());
    }
    
    private static Set<String> getErrors(String pathStr) throws IOException {
        Path path = Paths.get(pathStr);
        if (!path.toFile().exists())
            return Collections.emptySet();
        List<String> allLines =  Files.readAllLines(path);
        Set<String> allCommits = new HashSet<String>();
        for(String commit : allLines) {
        	commit = commit.split(",")[0];
        	if(!commit.isEmpty()) {
        		allCommits.add(commit);	
        	}	
        }
        return allCommits;
    }

    private static void writeToFile(String pathString, String content, StandardOpenOption standardOpenOption) {
        try {
            Path path = Paths.get(pathString);
            if (!path.toFile().exists())
                Files.createFile(path);
            Files.write(path, content.getBytes(), standardOpenOption);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
    
    private static List<String> readFromFile(String pathString, StandardOpenOption standardOpenOption) {
		List<String> file = new ArrayList<String>();
    	try {
            Path path = Paths.get(pathString);
            if (!path.toFile().exists()) {
            	file =  Files.readAllLines(path);
            }
            	
        } catch (Exception exception) {
            exception.printStackTrace();
        }
		return file;        
    }

    private static void createDirectory(String analysisDirectory) throws IOException {
        Path path = Paths.get(analysisDirectory);
        File directory = path.toFile();

        if (!directory.exists()) {
            Files.createDirectory(path);
        }
    }

    private void start(List<String> repositories) throws Exception {
        for (String repository : repositories)
            start(repository);
    }

    private void start(final String repositoryWebURL) throws Exception {
        String repositoryName = repositoryWebURL.replace("https://github.com/", "").replace(".git", "").replace("/", "\\");
        String projectDirectory = FOLDER_TO_CLONE + repositoryName;
        GitService gitService = new GitServiceImpl();
        GitHistoryRefactoringMiner miner = new GitHistoryRefactoringMinerImpl();


        try (Repository repository = gitService.cloneIfNotExists(projectDirectory, repositoryWebURL)) {
            String analysisDirectory = projectDirectory + "\\analysis";
            createDirectory(analysisDirectory);
            final Set<String> processed = getProcessed(projectDirectory + "/analysis/processed.csv");
            final Set<String> errors = getErrors(FOLDER_ERROR + "commit-messages-facilitate.csv");
            try (Git git = new Git(repository)) {
                PullCommand pull = git.pull();
                String treeName = "refs/heads/master"; // tag or branch
                
              //  for(String commitString : errors) {
                String commitString = new String("6341f5e7c7405afe8ff9204f4dcc1f4652300d01");
                 	ObjectId commitId = ObjectId.fromString(commitString);
                    RevCommit commit = git.getRepository().parseCommit(commitId);
                    System.out.println(commit);                   
                    writeToFile(projectDirectory + "/analysis/processed.csv", String.format("%s,%d", commit.getName() , System.currentTimeMillis()) + System.lineSeparator(), StandardOpenOption.APPEND);
                    
                    miner.detectAtCommit(repository, commit.getName(), new RefactoringHandler() {
                        @Override
                        public boolean skipCommit(String commitId) {
                            if (processed.contains(commitId))
                                return true;

                            return super.skipCommit(commitId);
                        }

                        @Override
                        public void handleException(String commitId, Exception e) {
                            try {
                                String uuid = UUID.randomUUID().toString();
                                writeToFile(projectDirectory + "/analysis/error.csv", String.format("%s,%s", commitId, uuid) + System.lineSeparator(), StandardOpenOption.APPEND);
                                PrintWriter pw = new PrintWriter(new File(String.format("%s/analysis/error-%s.log", projectDirectory, uuid)));
                                e.printStackTrace(pw);
                                pw.close();
                            } catch (Exception exception) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void handle(String commitId, List<Refactoring> refactorings, Map<Refactoring, List<MotivationType>> mapRefactoringMotivations,Map<Refactoring, List<MotivationFlag>> mapMotivationFlags, Map<Refactoring, int[]> mapFacilitateExtensionT1T2, Map<Refactoring, String> mapDecomposeToImproveRedability) {

                        	RefactoringHistory refactoringHistory = new RefactoringHistory(commitId , repositoryWebURL);
                        	for(Refactoring ref : refactorings) {
                        		refactoringHistory.addRefactoringType(ref);
                        	}
                        	List<ExtractMethodMotivation> results = mapRefactoringMotivations.entrySet().stream()
                                    .filter(entry -> isExtractOperationRefactoring(entry.getKey().getRefactoringType()))
                                    .map(entry -> {
                                        ExtractMethodMotivation extractMethodMotivation = new ExtractMethodMotivation(new HashSet<>(entry.getValue()),new HashSet<>(mapMotivationFlags.get(entry.getKey())));
                                        extractMethodMotivation.setRepository(repositoryWebURL);
                                        extractMethodMotivation.setCommitId(commitId);
                                        extractMethodMotivation.setCommitTime(commit.getCommitTime());
                                        extractMethodMotivation.setCommitMessage(commit.getShortMessage());
                                        extractMethodMotivation.setCommitAuthor(commit.getAuthorIdent().getName());
                                        extractMethodMotivation.setCommitAuthorDate(commit.getAuthorIdent().getWhen());
                                        extractMethodMotivation.setCommiterName(commit.getCommitterIdent().getName());
                                        extractMethodMotivation.setCommiterDate(commit.getCommitterIdent().getWhen());
                                        extractMethodMotivation.setRefactoringDesc(entry.getKey().toString());
                                        extractMethodMotivation.setRefactoringType(entry.getKey().getRefactoringType().toString());
                                        extractMethodMotivation.setRefactoringJSON(entry.getKey().toJSON());
                                        return extractMethodMotivation;
                                    })
                                    .collect(Collectors.toList());
                            if (!results.isEmpty()) {
                                Session sessionObj = null;
                                try {
                                    sessionObj = sessionFactoryObj.openSession();
                                    sessionObj.beginTransaction();

                                    for (ExtractMethodMotivation result : results) {
                                        sessionObj.save(result);
                                    }
                                    sessionObj.save(refactoringHistory);

                                    // Committing The Transactions To The Database
                                    sessionObj.getTransaction().commit();
                                    if (results.size() > 1)
                                        System.out.println(String.format("%d motivations are added to DB", results.size()));
                                    else
                                        System.out.println(String.format("%d motivation is added to DB", results.size()));
                                } catch (Exception exception) {
                                    if (sessionObj != null && null != sessionObj.getTransaction()) {
                                        sessionObj.getTransaction().rollback();
                                    }
                                    handleException(commitId, exception);
                                } finally {
                                    if (sessionObj != null) {
                                        sessionObj.close();
                                    }
                                }
                            }

                        }

                        private final boolean isExtractOperationRefactoring(RefactoringType refactoringType) {
                            switch (refactoringType) {
                                case EXTRACT_AND_MOVE_OPERATION:
                                case EXTRACT_OPERATION:
                                    return true;
                                default:
                                    return false;
                            }
                        }

                    }, 100000);
               // }
            }
        }
    }

    private MainError buildSessionFactory() {
        // Creating Configuration Instance & Passing Hibernate Configuration File
        Configuration configObj = new Configuration();
        configObj.configure("hibernate.cfg.xml");
        configObj.addAnnotatedClass(ExtractMethodMotivation.class);
        configObj.addAnnotatedClass(RefactoringHistory.class);

        // Since Hibernate Version 4.x, ServiceRegistry Is Being Used
        ServiceRegistry serviceRegistryObj = new StandardServiceRegistryBuilder().applySettings(configObj.getProperties()).build();

        // Creating Hibernate SessionFactory Instance
        sessionFactoryObj = configObj.buildSessionFactory(serviceRegistryObj);
        return this;
    }
}
