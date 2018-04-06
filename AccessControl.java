import java.util.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.io.*;
import java.text.*;

enum Permission{
  read,
  write,
  execute
}
enum QueryError{
  invalidStart,
  canKeyWord,
  whatKeyWord,
  isKeyWords,
  variableNotQuoted,
  noUser,
  noFile,
  noGroup,
  noPermission,
  invalidForm
}
public class AccessControl{
  static ArrayList<File> _files            = new ArrayList<File>();
  static ArrayList<User> _users            = new ArrayList<User>();
  static ArrayList<Group> _groups          = new ArrayList<Group>();
  static ArrayList<User> _trustedPermUsers = new ArrayList<User>();
  static User _keyDirectoryOwner           = null;

  //For When testing is run
  static ArrayList<String> _queries = new ArrayList<String>();

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

  public static Permission getPermission(String permissionName){
    Permission permission = null;
    try{
      return Permission.valueOf(permissionName);
    } catch(Exception e){
      return null;
    }
  }

  public static File getFile(String fileName){
    File file = null;
    fileName = fileName.toUpperCase();
    for(int i = 0; i < _files.size(); i++){
      String other = _files.get(i).getName().toUpperCase();
      if(other.equals(fileName)){
        return _files.get(i);
      }
    }
    return null;
  }

  public static User getUser(String userName){
    User user = null;
    userName = userName.toUpperCase();
    for(int i = 0; i < _users.size(); i++){
      String name = _users.get(i).getName().toUpperCase();
      if(name.equals(userName)){
        return _users.get(i);
      }
    }
    return null;
  }

  public static Group getGroup(String groupName){
    Group group = null;
    for(int i = 0; i < _groups.size(); i++){
      if(_groups.get(i).getName().toUpperCase().equals(groupName.toUpperCase())){
        return _groups.get(i);
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
      System.out.println("Error: There is no file with the file name: " +fileName);
      System.exit(1);
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
        String userName = argSplit[0].replaceAll("\\s+","");
        String key = argSplit[1].replaceAll("\\s+","");
        if(userName.charAt(0) == '\"') userName = userName.substring(1, userName.length());
        if(userName.charAt(userName.length() - 1) == '\"') userName = userName.substring(0, userName.length() - 1);
        User newUser = new User(userName, key);
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
        if(groupName.charAt(0) == '\"') groupName = groupName.substring(1, groupName.length());
        if(groupName.charAt(groupName.length() - 1) == '\"') groupName = groupName.substring(0, groupName.length() - 1);

        if(userName.charAt(0) == '\"') userName = userName.substring(1, userName.length());
        if(userName.charAt(userName.length() - 1) == '\"') userName = userName.substring(0, userName.length() - 1);

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
        if(userName.charAt(0) == '\"') userName = userName.substring(1, userName.length());
        if(userName.charAt(userName.length() - 1) == '\"') userName = userName.substring(0, userName.length() - 1);

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
              if(groupName.charAt(0) == '\"') groupName = groupName.substring(1, groupName.length());
              if(groupName.charAt(groupName.length() - 1) == '\"') groupName = groupName.substring(0, groupName.length() - 1);

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
              String userToTrust = rArgSplit[0];
              if(userToTrust.charAt(0) == '\"') userToTrust = userToTrust.substring(1, userToTrust.length());
              if(userToTrust.charAt(userToTrust.length() - 1) == '\"') userToTrust = userToTrust.substring(0, userToTrust.length() - 1);
              String userKey  = rArgSplit[1];
              boolean foundUser = false;
              for(int i = 0; i < _users.size(); i++){
                if(userToTrust.equals(_users.get(i).getName())){
                  _trustedPermUsers.add(_users.get(i));
                  foundUser = true;
                  break;
                }
              }
              if(!foundUser){
                System.out.println("Error: " +line);
                System.out.println("No user of the name: " +userToTrust);
                System.exit(1);
              }
            }
          }
          else if(splitRightHand.length == 2){
            //splitRightHand[0] should be PKD(user, key);
            String[] rhsParams1 = splitRightHand[0].split("\\(");

            String rType1 = rhsParams1[0].replaceAll("\\s+","");  //PKD
            String rArguments1 = rhsParams1[1].replaceAll("\\s+",""); // user, key

            //splitRightHand[1] should be "username"$policy(Arguments);
            String[] rhsDollar = splitRightHand[1].split("\\$");
            String key = rhsDollar[0].replaceAll("\\s+","");
            String rhsPerm = rhsDollar[1].replaceAll("\\s+","");

            String[] rhsParams2 = rhsPerm.split("\\(");
            String rType2 = rhsParams2[0].replaceAll("\\s+",""); //Perm or Attr
            String rArguments2 = rhsParams2[1].replaceAll("\\s+",""); // user, file, permission

            String[] argSplitFirst = rArguments1.split(",");
            String[] argSplitSecond = rArguments2.split(",");

            String trustedUsername       = argSplitFirst[0];
            String trustedKey            = argSplitFirst[1];
            if(trustedUsername.charAt(0) == '\"') trustedUsername = trustedUsername.substring(1, trustedUsername.length());
            if(trustedUsername.charAt(trustedUsername.length() - 1) == '\"') trustedUsername = trustedUsername.substring(0, trustedUsername.length() - 1);

            User trustedUser = null;
            User userToAdd = null;
            for(int i = 0; i < _users.size(); i++){
              boolean sameName = _users.get(i).getName().equals(userName);
              if(trustedUsername.equals(_users.get(i).getName())){
                trustedUser = _users.get(i);
              }
              if(sameName){
                userToAdd = _users.get(i);
              }
            }
            if(argSplitFirst.length != 2){
              argumentError(line, "PKD", 2, argSplitFirst.length);
            }

            if(trustedUser == null){
              invalidUserError(line, trustedUsername);
            }
            if(userToAdd == null){
              invalidUserError(line, userName);
            }
            if(rType2.equals("Perms")){
              if(argSplitSecond.length != 3){
                argumentError(line, "Perms", 3, argSplitSecond.length);
              }
              String permUser = argSplitSecond[0];
              String fileNameToCheck = argSplitSecond[1];
              String permStringToCheck = argSplitSecond[2].replaceAll("\\s+","");

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
            } else if(rType2.equals("Attr")){
              if(argSplitSecond.length != 2){
                argumentError(line, "Attr", 2, argSplitSecond.length);
              }
              String attrUser = argSplitSecond[0];
              String attribute = argSplitSecond[1].replaceAll("\\s+","");
              attribute = attribute.substring(0, attribute.length() - 2);
              if(attrUser.charAt(0) == '\"') attrUser = attrUser.substring(1, attrUser.length());
              if(attrUser.charAt(attrUser.length() - 1) == '\"') attrUser = attrUser.substring(0, attrUser.length() - 1);

              if(attribute.charAt(0) == '\"') attribute = attribute.substring(1, attribute.length());
              if(attribute.charAt(attribute.length() - 1) == '\"') attribute = attribute.substring(0, attribute.length() - 1);

              if(trustedUser.hasAtrribute(attribute)){
                if(permissionString.charAt(0) == '"'){
                  permissionString = permissionString.substring(1, permissionString.length() - 1);
                }
                Permission permission = getPermission(permissionString);
                if(permission == null){
                  invalidPermissionError(line, permissionString);
                }
                File file = getFile(fileName);
                if(file == null){
                  file = new File(fileName);
                  _files.add(file);
                }
                file.addUserAccess(userToAdd, permission);
                userToAdd.addFileAccess(file, permission);
              }

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
     if(parentGroupName.charAt(0) == '\"') parentGroupName = parentGroupName.substring(1, parentGroupName.length());
     if(parentGroupName.charAt(parentGroupName.length() - 1) == '\"') parentGroupName = parentGroupName.substring(0, parentGroupName.length() - 1);

     if(childGroupName.charAt(0) == '\"') childGroupName = childGroupName.substring(1, childGroupName.length());
     if(childGroupName.charAt(childGroupName.length() - 1) == '\"') childGroupName = childGroupName.substring(0, childGroupName.length() - 1);

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
    else if(type.equals("Attr")){
      String[] argSplit = arguments.split(",");
      if(argSplit.length != 2){
        argumentError(line, "Attr", 2, argSplit.length);
      }
      String userName = argSplit[0];
      String attribute = argSplit[1];
      if(userName.charAt(0) == '\"') userName = userName.substring(1, userName.length());
      if(userName.charAt(userName.length() - 1) == '\"') userName = userName.substring(0, userName.length() - 1);

      if(attribute.charAt(0) == '\"') attribute = attribute.substring(1, attribute.length());
      if(attribute.charAt(attribute.length() - 1) == '\"') attribute = attribute.substring(0, attribute.length() - 1);

      User user = getUser(userName);
      if(user == null){
        invalidUserError(line, userName);
      }
      user.addAttribute(attribute);
    }
    else{
      System.out.println("Error: " +line);
      System.out.println("No Policy function for " + type);
      System.exit(1);
    }
  }

  public static void proccessQuery(String query){
    query = query.toLowerCase();
    char firstChar = query.charAt(0);
    switch(firstChar){
      case 'c':
        String queryWord = query.substring(0, 3);
        if(!queryWord.equals("can")) {
          queryError(QueryError.invalidStart, query);
          return;
        }
        processCanQuery(query);
        break;
      case 'w':
        queryWord = query.substring(0, 4);
        if(!queryWord.equals("what")) {
          queryError(QueryError.invalidStart, query);
          return;
        }
        processWhatQuery(query);
        break;
      default:
        queryError(QueryError.invalidStart, query);
        return;
    }
  }

  public static void processCanQuery(String query){
    // get rid of CAN and whitespace
    String fullQuery = query;
    query = query.substring(4, query.length());
    char typeChar = query.charAt(0);
    if(typeChar == 'u'){
      String keyWord = query.substring(0, 4);
      if(keyWord.equals("user")){
        query = query.substring(5, query.length());
        if(query.charAt(0) != '\''){
          queryError(QueryError.variableNotQuoted, fullQuery);
          return;
        }

        String[] splitQuery = query.split("\'");
        if(splitQuery.length != 7){
          queryError(QueryError.variableNotQuoted, fullQuery);
          return;
        }
        String userName = splitQuery[1];
        String fileName = splitQuery[3];
        String permissionString = splitQuery[5];
        fileName = "\"" + fileName + "\"";


        User user = getUser(userName);
        if(user == null){
          queryError(QueryError.noUser, fullQuery);
          return;
        }
        File file = getFile(fileName);
        if(file == null){
          queryError(QueryError.noFile, fullQuery);
          return;
        }
        Permission permission;
        try{
          permission = Permission.valueOf(permissionString);
        } catch(Exception e){
          queryError(QueryError.noPermission, fullQuery);
          return;
        }
        boolean hasAccess = user.hasFileAccess(file, permission);
        if(hasAccess){
          System.out.println(">> Yes");
          System.out.println();
        }
        else{
          System.out.println(">> No");
          System.out.println();
        }
      } else {
        queryError(QueryError.canKeyWord, fullQuery);
        return;
      }
    }
    else if(typeChar == 'g'){
      String keyWord = query.substring(0, 5);
      if(keyWord.equals("group")){
        query = query.substring(6, query.length());
        if(query.charAt(0) != '\''){
          queryError(QueryError.variableNotQuoted, fullQuery);
          return;
        }
      } else{
        queryError(QueryError.canKeyWord, fullQuery);
        return;
        }
        String[] splitQuery = query.split("\'");
        if(splitQuery.length != 7){
          queryError(QueryError.variableNotQuoted, fullQuery);
          return;
        }
        String groupName = splitQuery[1];
        String fileName = splitQuery[3];
        String permissionString = splitQuery[5];
        fileName = "\"" + fileName + "\"";


        Group group = getGroup(groupName);
        if(group == null){
          queryError(QueryError.noGroup, fullQuery);
          return;
        }
        File file = getFile(fileName);
        if(file == null){
          queryError(QueryError.noFile, fullQuery);
          return;
        }
        Permission permission;
        try{
          permission = Permission.valueOf(permissionString);
        } catch(Exception e){
          queryError(QueryError.noPermission, fullQuery);
          return;
        }
        boolean hasAccess = group.hasFileAccess(file, permission);
        if(hasAccess){
          System.out.println(">> Yes");
          System.out.println();
        }
        else{
          System.out.println(">> No");
          System.out.println();
        }

      }
    else{
      queryError(QueryError.canKeyWord, fullQuery);
      return;
    }
  }

  public static void processWhatQuery(String query){
    String fullQuery = query;
    //remove what and whitespace
    query = query.substring(5, query.length());
    char typeChar = query.charAt(0);
    if(typeChar == 'f'){
      if(query.contains("files can user")) processWhatFilesUser(fullQuery);
      if(query.contains("files can group")) processWhatFilesGroup(fullQuery);
    }
    else if(typeChar == 'g'){
      if(query.contains("groups is") && query.contains("a member of?")){
        String[] variables = query.split("\'");
        if(variables.length == 3){
          String userName = variables[1];
          User user = getUser(userName);
          if(user == null){
            queryError(QueryError.noUser, fullQuery);
            return;
          }
          ArrayList<Group> groups = user.getGroups();
          for(int i = 0; i < groups.size(); i++){
            System.out.println(">> Group: " + groups.get(i).getName());
          }
          System.out.println();
        } else{
          queryError(QueryError.variableNotQuoted, fullQuery);
          return;
        }
      }
      else if(query.contains("groups have access to file")){
        String[] variables = query.split("\'");
        if(variables.length != 5){
          queryError(QueryError.variableNotQuoted, fullQuery);
          return;
        }
        String fileName = variables[1];
        fileName = "\"" + fileName + "\"";
        String permissionString = variables[3];
        File file = getFile(fileName);
        if(file == null){
          queryError(QueryError.noFile, fullQuery);
          return;
        }
        Permission permission = getPermission(permissionString);
        if(permission == null){
          queryError(QueryError.noPermission, fullQuery);
          return;
        }
        file.printGroupAccess(permission);
        System.out.println();

      }
    }
    else if(typeChar == 'u'){
      if(query.equals("users are trusted to alter permissions?")){
        if(_trustedPermUsers.size() == 0) System.out.println(">> None");
        for(int i = 0; i < _trustedPermUsers.size(); i++){
          System.out.println(">> User: " +_trustedPermUsers.get(i).getName());
        }
        System.out.println();
      } else if(query.contains("users have access to file")){
        String[] variables = query.split("\'");
        if(variables.length != 5){
          queryError(QueryError.variableNotQuoted, fullQuery);
          return;
        }
        String fileName = variables[1];
        fileName = "\"" + fileName + "\"";
        String permissionString = variables[3];
        File file = getFile(fileName);
        if(file == null){
          queryError(QueryError.noFile, fullQuery);
          return;
        }
        Permission permission = getPermission(permissionString);
        if(permission == null){
          queryError(QueryError.noPermission, fullQuery);
          return;
        }
        file.printUserAccess(permission);
        System.out.println();
      }
    }
    else if(typeChar == 's'){
      if(query.contains("subgroups does group")){
        String[] variables = query.split("\'");
        if(variables.length != 3){
          queryError(QueryError.variableNotQuoted, fullQuery);
          return;
        }
        String groupName = variables[1];
        Group group = getGroup(groupName);
        if(group == null){
          queryError(QueryError.noGroup, fullQuery);
          return;
        }
        ArrayList<Group> children = group.getChildrenGroups();
        if(children.size() == 0 ){
          System.out.println(">> None");
          System.out.println();
        }
        for(int i = 0; i < children.size(); i++){
          System.out.println("Sub Group: " + children.get(i).getName());
        }
        System.out.println();
      }
    }
  }

  public static void processWhatFilesUser(String query){
    String[] variables = query.split("\'");
    if(variables.length == 3){
      String userName = variables[1];
      User user = getUser(userName);
      if(user == null) {
        queryError(QueryError.noUser, query);
        return;
      }
      user.printFileAccess();
      System.out.println();
    } else if(variables.length == 5){
      String userName = variables[1];
      String permissionString = variables[3];
      User user = getUser(userName);
      Permission permission = getPermission(permissionString);
      if(user == null){
        queryError(QueryError.noUser, query);
        return;
      }
      if(permission == null){
        queryError(QueryError.noPermission, query);
        return;
      }
      user.printFileAccess(permission);
      System.out.println();

    } else{
      queryError(QueryError.invalidForm, query);
      return;
    }
  }

  public static void processWhatFilesGroup(String query){
    String[] variables = query.split("\'");
    if(variables.length == 3){
      String groupName = variables[1];
      Group group = getGroup(groupName);
      if(group == null) {
        queryError(QueryError.noGroup, query);
        return;
      }
      group.printFileAccess();
      System.out.println();
    } else if(variables.length == 5){
      String groupName = variables[1];
      String permissionString = variables[3];
      Group group = getGroup(groupName);
      Permission permission = getPermission(permissionString);
      if(group == null){
        queryError(QueryError.noUser, query);
        return;
      }
      if(permission == null){
        queryError(QueryError.noPermission, query);
        return;
      }
      group.printFileAccess(permission);
      System.out.println();

    } else{
      queryError(QueryError.invalidForm, query);
      return;
    }
  }

  public static void queryError(QueryError error, String query){
    if(error == QueryError.invalidStart)
      System.out.println("Queries must start with keyword 'IS', 'WHAT', or 'CAN'");
    if(error == QueryError.canKeyWord){
      System.out.println("There is no query of the form: " +query);
      System.out.println("                                   ^"        );
      System.out.println("Can must be followed by the keyword 'group' or 'user'");
      System.out.println();
    }
    if(error == QueryError.variableNotQuoted){
      System.out.println("All variables must be quoted in queries");
      System.out.println();
    }
    if(error == QueryError.noUser){
      System.out.println("There is no user with that name");
      System.out.println();
    }
    if(error == QueryError.noGroup){
      System.out.println("There is no group with that name");
      System.out.println();
    }
    if(error == QueryError.noFile){
      System.out.println("There is no file with that name");
      System.out.println();
    }
    if(error == QueryError.noPermission){
      System.out.println("There is no permission with that name");
      System.out.println();
    }

  }

  public static void main(String[] args) {
    String fileName = "";
    if(args.length != 1 && args.length != 2){
      System.out.println("You must enter a policy file as an argument");
      System.exit(1);
    }
    if(args.length == 1) fileName = args[0];
    else if(args.length == 2){
      fileName = args[0];
      if(args[1].equals("--test")){
        testProgram(fileName);
      } else{
        System.out.println("No valid parameter for file name" + args[2]);
      }
    }
    runProgram(fileName);

  }

  public static void runProgram(String fileName){
    List<String> fileLines = fileParser(fileName);
    for(int i = 0; i < fileLines.size(); i++){
      handleLine(fileLines.get(i));
    }
    Scanner sc = new Scanner(System.in);
    boolean quit = false;
    String query = "";
    while(!quit){
      System.out.println("Please enter a query (Enter 'help' to see query forms): ");
      System.out.print("> ");
      query = sc.nextLine();
      if(query.toLowerCase().equals("quit")){
        quit = true;
      } else if(query.toLowerCase().equals("help")) {
        System.out.println("_________________________________________________________________");
        System.out.println("All variables in queries must be surrounded by '' ");
        System.out.println("User Queries: ");
        System.out.println("  Can user 'u' access file 'f' with privilege 'p'?");
        System.out.println("  What files can user 'u' access?");
        System.out.println("  What files can user 'u' access with privilege 'p'?");
        System.out.println("  What groups is 'u' a member of?");
        System.out.println("  What users are trusted to alter permissions?");
        System.out.println("Group Queries: ");
        System.out.println("  What users are in group 'g'?");
        System.out.println("  Can group 'g' access file 'f' with privilege 'p'?");
        System.out.println("  What files can group 'g' access?");
        System.out.println("  What files can group 'g' access with privilege 'p'?");
        System.out.println("  What subgroups does group 'g' have?");
        System.out.println("  What are the parent groups of group 'g'?");
        System.out.println("File Queries: ");
        System.out.println("  What users have access to file 'f' with privilege 'p'?");
        System.out.println("  What groups have access to file 'f' with privilege 'p'?");
        System.out.println("_________________________________________________________________");
      }
      else {
        proccessQuery(query.toLowerCase());
      }
    }
  }

  public static void testProgram(String fileName){

    double startTime = System.nanoTime();

    List<String> fileLines = fileParser(fileName);
    for(int i = 0; i < fileLines.size(); i++){
      handleLine(fileLines.get(i));
    }

    double endTime = System.nanoTime();
    double duration = (endTime - startTime);
    double processTime = duration;
    for(int i = 0; i < _users.size(); i++){
      System.out.println("User: " +_users.get(i).getName());
    }
    double queryRuns = 100;
    initializeQuery();
    ArrayList<Double> times = new ArrayList<Double>();
    for(int i = 0; i < queryRuns; i++){
      String query = getNewQuery();
      System.out.println(query);
      startTime = System.nanoTime();
      proccessQuery(query);
      endTime = System.nanoTime();
      duration = (endTime - startTime);
      times.add(duration);
    }
    double averageTime = 0;
    for(int i =0; i < times.size(); i++){
      averageTime += times.get(i);
    }
    averageTime = (double) (averageTime/queryRuns);

    averageTime = (double) (averageTime/(double)1000000);
    processTime = (double) (processTime/1000000);
    DecimalFormat df = new DecimalFormat("#.#####");

    System.out.println("File Processed in: " +df.format(processTime) + " ms");
    System.out.println(queryRuns + " queries ran with an average time of " +df.format(averageTime)+ " ms");
    System.exit(1);
  }

  public static String getNewQuery(){
    Random rand = new Random();
    int index = rand.nextInt(_queries.size());
    String query = _queries.get(index);
    System.out.println("Starting query: " +query);
    if(query.contains(" \'u\' ")){
      index = rand.nextInt(_users.size());
      String username = _users.get(index).getName();
      username = "\'" + username + "\'";
      query = query.replace("\'u\'", username);
    }
    if(query.contains("\'g\'")){
      index = rand.nextInt(_groups.size());
      String groupName = _groups.get(index).getName();
      groupName = "\'" + groupName + "\'";
      query = query.replace("\'g\'", groupName);
    }
    if(query.contains("\'f\'")){
      index = rand.nextInt(_files.size());
      String fileName = _files.get(index).getName();
      fileName = fileName.substring(1, fileName.length() - 1);
      fileName = "\'" + fileName + "\'";
      query = query.replace("\'f\'", fileName);
    }
    if(query.contains("\'p\'")){
      index = rand.nextInt(Permission.values().length);
      String permission = Permission.values()[index].toString();
      permission = "\'" + permission + "\'";
      query = query.replace("\'p\'", permission);
    }
    return query;
  }
  public static void initializeQuery(){
    _queries.add("Can user 'u' access file 'f' with privilege 'p'?");
    _queries.add("What files can user 'u' access?");
    _queries.add("What files can user 'u' access with privilege 'p'?");
    _queries.add("What groups is 'u' a member of?");
    _queries.add("What users are trusted to alter permissions?");
    _queries.add("What users are in group 'g'?");
    _queries.add("Can group 'g' access file 'f' with privilege 'p'?");
    _queries.add("What files can group 'g' access?");
    _queries.add("What files can group 'g' access with privilege 'p'?");
    _queries.add("What subgroups does group 'g' have?");
    _queries.add("What are the parent groups of group 'g'?");
    _queries.add("What users have access to file 'f' with privilege 'p'?");
    _queries.add("What groups have access to file 'f' with privilege 'p'?");
  }
}
