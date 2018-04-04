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
  static ArrayList<File> _files            = new ArrayList<File>();
  static ArrayList<User> _users            = new ArrayList<User>();
  static ArrayList<Group> _groups          = new ArrayList<Group>();
  static ArrayList<User> _trustedPermUsers = new ArrayList<User>();

  public static void argumentError(String lineError, int expected, int actual){
    System.out.println("Error: " + lineError);
    System.out.println("Invalid number of arguments. There should be " + expected +", but there are: " + actual);
    System.exit(1);
  }

  public static void invalidPermissionError(String lineError, String invalidPermission){
    System.out.println("Error: " +lineError);
    System.out.println("The permission " + invalidPermission + " is not a valid permission");
    System.exit(1);
  }

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
    return lines;
  }

  public static void handleLine(String line){

    String[] splitLine = line.split(":(?=-)");
    if(splitLine.length != 2){
      return;
    }

    String lhs = splitLine[0];
    String rhs = splitLine[1].replaceAll("\\s+","");
    rhs = rhs.substring(1, rhs.length());

    String[] splitLeftHand = lhs.split("\\(");
    String[] splitRightHand = {};
    if(!rhs.equals(";")){
      splitRightHand = rhs.split("(?<=\\)),");
    }

    String type = splitLeftHand[0].replaceAll("\\s+","");
    String arguments = splitLeftHand[1].replaceAll("\\s+","");
    arguments = arguments.substring(0, arguments.length() - 1);

    // String rightType = splitRightHand[0];
    // String rightArguments  = splitRightHand[1];
    // rightArguments = rightArguments.substring(0, rightArguments.length() - 2);

    if(type.equals("PKD")){
        String[] argSplit = arguments.split(",");
        if(argSplit.length != 2){
          argumentError(line, 2, argSplit.length);
        }
        User newUser = new User(argSplit[0].replaceAll("\\s+",""), argSplit[1].replaceAll("\\s+",""));
        _users.add(newUser);
    }
    else if(type.equals("Group")){
         String[] argSplit = arguments.split(",");
        if(argSplit.length != 2){
          argumentError(line, 2, argSplit.length);
        }
        String groupName = argSplit[0].replaceAll("\\s+","");
        String userName = argSplit[1].replaceAll("\\s+","");
        Group group;
        int index = -1;
        for(int i = 0; i < _groups.size(); i++){
          if(_groups.get(i).getName().equals(groupName)){
            index = i;
          }
        }
        if(index != -1){
          group = _groups.get(index);
        } else{
          group = new Group(groupName);
          _groups.add(group);
        }
        int userIndex = -1;
        for(int i = 0; i < _users.size(); i++){
          if(_users.get(i).getName().equals(userName)){
            userIndex = i;
          }
        }
        if(userIndex == -1){
          System.out.println("Error: " +line);
          System.out.println("There is no user with the userName: " +userName);
          System.exit(1);
        }
        User userToAdd = _users.get(userIndex);
        group.addUser(userToAdd);
    }
    else if(type.equals("Perms")){
        String[] argSplit = arguments.split(",");
        if(argSplit.length != 3){
          argumentError(line, 3, argSplit.length);
        }
        String userName = argSplit[0];
        String fileName = argSplit[1];
        String permissionString = argSplit[2];
        if(splitRightHand.length == 0){
          Permission permission;
          if(permissionString.charAt(0) == '"'){
            permissionString = permissionString.substring(1, permissionString.length() - 1);
            try{
              permission = Permission.valueOf(permissionString);
            } catch(Exception e){
              invalidPermissionError(line, permissionString);
            }
          } else{
            System.out.println("Error: " + line);
            System.out.println("When assigning a permission, the permission needs to be surrounded by \" \" ");
            System.exit(1);
          }
          int userIndex = -1;
          for(int i = 0; i < _users.size(); i++){
            if(_users.get(i).getName().equals(userName)){
              userIndex = i;
            }
          }
          if(userIndex == -1){
            System.out.println("Error: " +line);
            System.out.println("There is no user with the userName: " +userName);
            System.exit(1);
          }
          User userToAdd = _users.get(userIndex);

          int fileIndex = -1;
          for(int i = 0; i < _files.size(); i++){
            if(_files.get(i).getName().equals(fileName)){
              fileIndex = i;
            }
          }
          File file;
          if(fileIndex == -1){
            
          }
        } else{

        }

    } else if(type.equals("SubGroup")){

    }


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
    for(int i = 0; i < _users.size(); i++){
      _users.get(i).print();
    }
    for(int i = 0; i < _groups.size(); i++){
      _groups.get(i).print();
    }

  }

}
