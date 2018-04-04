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
  static User _keyDirectoryOwner           = null;
  public static void argumentError(String lineError, String policy, int expected, int actual){
    System.out.println("Error: " + lineError);
    System.out.println("Invalid number of arguments. There should be " + expected +" arguments for policy " + policy +", but there are: " + actual);
    System.exit(1);
  }

  public static void invalidPermissionError(String lineError, String invalidPermission){
    System.out.println("Error: " +lineError);
    System.out.println("The permission " + invalidPermission + " is not a valid permission");
    System.exit(1);
  }

  public static void invalidUserError(String lineError, String invalidUserName){
    System.out.println("Error: " +lineError);
    System.out.println("There is no user with the userName: " +invalidUserName);
    System.exit(1);
  }

  public static void invalidFileError(String lineError, String invalidFileName){
    System.out.println("Error: " +lineError);
    System.out.println("There is no file with the name: " +invalidFileName);
    System.exit(1);
  }

  public static File getFile(String fileName){
    File file = null;
    for(int i = 0; i < _files.size(); i++){
      if(_files.get(i).getName().equals(fileName)){
        return _files.get(i);
      }
    }
    return null;
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


    if(type.equals("PKD")){
      if(splitRightHand.length == 0){
        String[] argSplit = arguments.split(",");
        if(argSplit.length != 2){
          argumentError(line, "PKD", 2, argSplit.length);
        }
        User newUser = new User(argSplit[0].replaceAll("\\s+",""), argSplit[1].replaceAll("\\s+",""));
        _users.add(newUser);
      } else if(splitRightHand.length == 1){
        String[] splitDollar = splitRightHand[0].split("$");
        if(splitDollar.length != 2){
          System.out.println("Error: " +line);
          System.out.print("When assigning a PKD to a specific user key the ");
          System.out.println("argument must be of the form: userkey$PKD(user, key)");
          System.exit(1);
        }
        String userKey = splitDollar[1];
        String[] rhsParams = splitDollar[1].split("\\(");
        String rType = rhsParams[0].replaceAll("\\s+","");
        String rArguments = rhsParams[1].replaceAll("\\s+","");
        if(!rType.equals("PKD")){
          System.out.println("Error: " +line);
          System.out.print("When assigning a PKD to a specific user key the ");
          System.out.println("argument must be of the form: userkey$PKD(user, key)");
          System.exit(1);
        }

        String[] argSplit = rArguments.split(",");
        if(argSplit.length != 2){
          argumentError(line, "PKD", 2, argSplit.length);
        }
        for(int i = 0; i < _users.size(); i++){
          if(userKey.equals(_users.get(i).getKey())){
            _keyDirectoryOwner = _users.get(i);
            break;
          }
        }
        if(_keyDirectoryOwner == null){
          System.out.println("There is no user with the key: " +userKey);
          System.exit(1);
        }
      }
    }
    else if(type.equals("Group")){
         String[] argSplit = arguments.split(",");
        if(argSplit.length != 2){
          argumentError(line, "Group", 2, argSplit.length);
        }
        String groupName = argSplit[0].replaceAll("\\s+","");
        String userName = argSplit[1].replaceAll("\\s+","");
        Group group;
        int index = -1;
        for(int i = 0; i < _groups.size(); i++){
          if(_groups.get(i).getName().equals(groupName)){
            index = i;
            break;
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
            break;
          }
        }
        if(userIndex == -1){
          System.out.println("Error: " +line);
          System.out.println("There is no user with the userName: " +userName);
          System.exit(1);
        }
        User userToAdd = _users.get(userIndex);
        userToAdd.addGroup(group);
        group.addUser(userToAdd);
    }
    else if(type.equals("Perms")){
        String[] argSplit = arguments.split(",");
        if(argSplit.length != 3){
          argumentError(line, "Perms", 3, argSplit.length);
        }
        String userName = argSplit[0];
        String fileName = argSplit[1];
        String permissionString = argSplit[2];
        if(splitRightHand.length == 0){
          Permission permission = null;
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
              break;
            }
          }
          if(userIndex == -1){
            invalidUserError(line, userName);
          }
          User userToAdd = _users.get(userIndex);

          int fileIndex = -1;
          for(int i = 0; i < _files.size(); i++){
            if(_files.get(i).getName().equals(fileName)){
              fileIndex = i;
              break;
            }
          }
          File file;
          if(fileIndex == -1){
            file = new File(fileName);
            _files.add(file);
          } else{
            file = _files.get(fileIndex);
          }
          file.addUserAccess(userToAdd, permission);
          userToAdd.addFileAccess(file, permission);
        }
        else{
          if(splitRightHand.length == 1){
            String[] rhsParams = splitRightHand[0].split("\\(");
            String rType = rhsParams[0].replaceAll("\\s+","");
            String rArguments = rhsParams[1].replaceAll("\\s+","");
            if(rType.equals("Group")){

              Permission permission = null;
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

              String[] rArgSplit = rArguments.split(",");
              if(rArgSplit.length != 2){
                argumentError(line, "Group", 2, rArgSplit.length);
              }
              String groupName = rArgSplit[0];
              String iterator = rArgSplit[1];
              iterator = iterator.substring(0, iterator.length() - 2);
              if(!iterator.equals(userName)){
                System.out.println("Error: " +line);
                System.out.println("The first argument of Perms and the second argument of Group should both be user");
                System.exit(1);
              }

              int fileIndex = -1;
              for(int i = 0; i < _files.size(); i++){
                if(_files.get(i).getName().equals(fileName)){
                  fileIndex = i;
                  break;
                }
              }
              File file;
              if(fileIndex == -1){
                file = new File(fileName);
                _files.add(file);
              } else{
                file = _files.get(fileIndex);
              }

              Group group = null;
              int index = -1;
              for(int i = 0; i < _groups.size(); i++){
                if(_groups.get(i).getName().equals(groupName)){
                  index = i;
                  break;
                }
              }
              if(index != -1){
                group = _groups.get(index);
              } else{
                System.out.println("Error: " +line);
                System.out.println("There is no group named " + groupName);
                System.exit(1);
              }
              ArrayList<User> groupUsers = group.getUsers();
              for(int i = 0; i < groupUsers.size(); i++){
                String groupUserName = groupUsers.get(i).getName();
                int userIndex = -1;
                for(int j = 0; j < _users.size(); j++){
                  if(_users.get(j).getName().equals(groupUserName)){
                    userIndex = j;
                    break;
                  }
                }
                if(userIndex == -1){
                  System.out.println("Error: " +line);
                  System.out.println("There is no user with the userName: " +groupUserName);
                  System.exit(1);
                }
                User userToAdd = _users.get(userIndex);
                file.addUserAccess(userToAdd, permission);
                userToAdd.addFileAccess(file, permission);
              }
              file.addGroupAccess(group, permission);
              group.addFileAccess(file, permission);
            }
            else if(rType.equals("PKD")){
              String[] rArgSplit = rArguments.split(",");
              String userKey  = rArgSplit[1];
              boolean foundUser = false;
              for(int i = 0; i < _users.size(); i++){
                if(userName.equals(_users.get(i).getName())){
                  _trustedPermUsers.add(_users.get(i));
                  foundUser = true;
                  break;
                }
              }
              if(!foundUser){
                System.out.println("Error: " +line);
                System.out.println("No user of the name: " +userName);
                System.exit(1);
              }
            }
          }
          else if(splitRightHand.length == 2){
            //splitRightHand[0] should be PKD(user, key);
            String[] rhsParams1 = splitRightHand[0].split("\\(");

            String rType1 = rhsParams1[0].replaceAll("\\s+","");  //PKD
            String rArguments1 = rhsParams1[1].replaceAll("\\s+",""); // user, key

            //splitRightHand[1] should be key$perm(user, file, permission);
            String[] rhsDollar = splitRightHand[1].split("\\$");
            String key = rhsDollar[0].replaceAll("\\s+","");
            String rhsPerm = rhsDollar[1].replaceAll("\\s+","");

            String[] rhsParams2 = rhsPerm.split("\\(");
            String rType2 = rhsParams2[0].replaceAll("\\s+",""); //Perm
            String rArguments2 = rhsParams2[1].replaceAll("\\s+",""); // user, file, permission

            String[] argSplitFirst = rArguments1.split(",");
            String[] argSplitSecond = rArguments2.split(",");

            System.out.println(rArguments1);
            if(argSplitFirst.length != 2){
              argumentError(line, "PKD", 2, argSplitFirst.length);
            }
            if(argSplitSecond.length != 3){
              argumentError(line, "Perms", 3, argSplitSecond.length);
            }
            //        String userName = argSplit[0];
            //        String fileName = argSplit[1];
            //        String permissionString = argSplit[2];
            String trustedUsername       = argSplitFirst[0];
            String trustedKey            = argSplitFirst[1];
            User trustedUser = null;
            User userToAdd = null;
            for(int i = 0; i < _users.size(); i++){
              if(trustedUsername.equals(_users.get(i).getName())){
                trustedUser = _users.get(i);
              } else if(userName.equals(_users.get(i).getName())){
                userToAdd = _users.get(i);
              }
            }
            if(trustedUser == null){
              invalidUserError(line, trustedUsername);
            }
            if(userToAdd == null){
              invalidUserError(line, userName);
            }

            String permUser = argSplitSecond[0];
            String fileNameToCheck = argSplitSecond[1];
            String permStringToCheck = argSplitSecond[2].replaceAll("\\s+","");;

            File fileToCheck = getFile(fileNameToCheck);
            if(fileToCheck == null){
              invalidFileError(line, fileNameToCheck);
            }
            Permission permToCheck = null;
            if(permStringToCheck.charAt(0) == '"'){
              permStringToCheck = permStringToCheck.substring(1, permStringToCheck.length() - 3);
            }

            try{
              permToCheck = Permission.valueOf(permStringToCheck);
            } catch(Exception e){
              invalidPermissionError(line, permStringToCheck);
            }
            if(trustedUser.hasFileAccess(fileToCheck, permToCheck)){
              Permission permission = null;
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
              int fileIndex = -1;
              for(int i = 0; i < _files.size(); i++){
                if(_files.get(i).getName().equals(fileName)){
                  fileIndex = i;
                  break;
                }
              }
              File file;
              if(fileIndex == -1){
                file = new File(fileName);
                _files.add(file);
              } else{
                file = _files.get(fileIndex);
              }
              file.addUserAccess(userToAdd, permission);
              userToAdd.addFileAccess(file, permission);
            }
          } else{
            System.out.println("Error: " +line);
            System.out.println("There is no recogized function for Perms with > 2 right hand arguments");
            System.exit(1);
          }
        }

    }
    else if(type.equals("SubGroup")){
     String[] argSplit = arguments.split(",");
     if(argSplit.length != 2){
       argumentError(line, "SubGroup", 2, argSplit.length);
     }
     String parentGroupName = argSplit[0].replaceAll("\\s+","");
     String childGroupName = argSplit[1].replaceAll("\\s+","");
     Group parentGroup;
     Group childGroup;

     int parentIndex = -1;
     for(int i = 0; i < _groups.size(); i++){
       if(_groups.get(i).getName().equals(parentGroupName)){
         parentIndex = i;
         break;
       }
     }
     if(parentIndex == -1){
       System.out.println("Error: " +line);
       System.out.println("There is no group with the name " +parentGroupName);
       System.exit(1);
     }

     int childIndex = -1;
     for(int i = 0; i < _groups.size(); i++){
       if(_groups.get(i).getName().equals(childGroupName)){
         childIndex = i;
         break;
       }
     }
     if(childIndex == -1){
       System.out.println("Error: " +line);
       System.out.println("There is no group with the name " +childGroupName);
       System.exit(1);
     }

     childGroup = _groups.get(childIndex);
     parentGroup = _groups.get(parentIndex);

     parentGroup.addChildGroup(childGroup);
     childGroup.addParentGroup(parentGroup);

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
    for(int i = 0; i < _groups.size(); i++){
      _groups.get(i).print();
    }
    System.out.println();
    for(int i = 0; i < _files.size(); i++){
      _files.get(i).print();
    }


  }

}
