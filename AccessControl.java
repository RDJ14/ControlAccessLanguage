import java.util.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.io.*;

enum Permission{
  read,
  write,
  execute
}

public class AccessControl{
  ArrayList<File> _files = new ArrayList<File>();
  ArrayList<User> _users = new ArrayList<User>();
  ArrayList<Group> _groups = new ArrayList<Group>();


  public static List<String> fileParser(String fileName){
    List<String> lines = Collections.emptyList();
    try
    {
      lines = Files.readAllLines(Paths.get(fileName), StandardCharsets.UTF_8);
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    for(int i = 0; i < lines.size(); i++){
      System.out.println(lines.get(i));
    }
    return lines;
  }

  public static void handleLine(String line){

    String[] splitLine = line.split(":(?=-)");
    if(splitLine.length != 2){
      return;
    }
    System.out.println("Split 0: " + splitLine[0]);
    System.out.println("Split 1: " + splitLine[1]);
  }

  public static void main(String[] args) {
    String fileName = "";
    if(args.length != 1){
      System.out.println("You must enter a policy file as an argument");
      System.exit(1);
    }
    else{
      fileName = args[0];
    }
    List<String> fileLines = fileParser(fileName);
    for(int i = 0; i < fileLines.size(); i++){
      handleLine(fileLines.get(i));
    }

  }

}
