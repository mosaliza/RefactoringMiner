import gr.uom.java.xmi.diff.MotivationType;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
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
import java.util.stream.Collectors;

public class MainLargeScaleTest {
    private static String FOLDER_TO_CLONE = "H:\\Projects\\";
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

        new MainLargeScaleTest().buildSessionFactory().start(repositories);
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
            try (Git git = new Git(repository)) {
                PullCommand pull = git.pull();
                String treeName = "refs/heads/master"; // tag or branch
                for (RevCommit commit : git.log().add(repository.resolve(treeName)).call()) {
                    System.out.println(commit.getName());
                    writeToFile(projectDirectory + "/analysis/processed.csv", String.format("%s,%d", commit.getName(), System.currentTimeMillis()) + System.lineSeparator(), StandardOpenOption.APPEND);
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
                        public void handle(String commitId, List<Refactoring> refactorings, Map<Refactoring, List<MotivationType>> mapRefactoringMotivations, Map<Refactoring, int[]> mapFacilitateExtensionT1T2, Map<Refactoring, String> mapDecomposeToImproveRedability) {
                            List<ExtractMethodMotivation> results = mapRefactoringMotivations.entrySet().stream()
                                    .filter(entry -> isExtractOperationRefactoring(entry.getKey().getRefactoringType()))
                                    .map(entry -> {
                                        ExtractMethodMotivation extractMethodMotivation = new ExtractMethodMotivation(new HashSet<>(entry.getValue()));
                                        extractMethodMotivation.setRepository(repositoryWebURL);
                                        extractMethodMotivation.setCommitId(commitId);
                                        extractMethodMotivation.setRefactoringDesc(entry.getKey().toString());
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

                    }, 10000);
                }
            }
        }
    }

    private MainLargeScaleTest buildSessionFactory() {
        // Creating Configuration Instance & Passing Hibernate Configuration File
        Configuration configObj = new Configuration();
        configObj.configure("hibernate.cfg.xml");
        configObj.addAnnotatedClass(ExtractMethodMotivation.class);

        // Since Hibernate Version 4.x, ServiceRegistry Is Being Used
        ServiceRegistry serviceRegistryObj = new StandardServiceRegistryBuilder().applySettings(configObj.getProperties()).build();

        // Creating Hibernate SessionFactory Instance
        sessionFactoryObj = configObj.buildSessionFactory(serviceRegistryObj);
        return this;
    }
}
