import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FacilitateErrors{
  private static String FOLDER_ERROR= "tmp1\\111\\";

 public static void main(String[] args) throws IOException {
     final List<String> commit_messages = getCommitMessages(FOLDER_ERROR + "commit-messages-facilitate.csv");
     List<String> errors = new ArrayList<String>();
     for(String message : commit_messages) {
    	 message = message.toLowerCase();
    	 if(message.contains("fix") ||message.contains("error") ||message.contains("bug")
    			 ||message.contains("bugs") || message.contains("errors")
    			 || message.contains("fixed")
    			 || message.contains("fixes")
    			 || message.contains("resolve")
    			 || message.contains("solve")
    			 || message.contains("issue")
    			 || message.contains("prevent")) {
    		errors.add(message);
    	 }
     }
     System.out.println("All:"+ commit_messages.size());
     System.out.print("Error:"+errors.size());
}
 private static List<String> getCommitMessages(String pathStr) throws IOException {
     Path path = Paths.get(pathStr);
     if (!path.toFile().exists())
         return Collections.emptyList();
     List<String> allLines =  Files.readAllLines(path);
     return allLines;
 }

 
}
