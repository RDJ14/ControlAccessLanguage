import java.util.*;

class User extends AccessObject{

  public String username;
  private String key;
  private ArrayList<Group> groups;
  private AccessTree<User> accessTree;

  public User(String username, String key){
    this.username = username;
    this.key = key;
    groups = new ArrayList<Group>();
    this.accessTree = new AccessTree<User>(this);
  }

  public String getName(){
    return this.username;
  }

  public String getKey(){
    return this.key;
  }

  public boolean hasFileAccess(File file, Permission permission){
    return accessTree.hasFileAccess(file, permission);
  }

  public void printFileAccess(){
    accessTree.printFileAccess();
  }

  public void printFileAccess(Permission permission){
    accessTree.printFileAccess(permission);
  }

  public ArrayList<Group> getGroups(){
    return this.groups;
  }

  public void addGroup(Group aGroup){
    this.groups.add(aGroup);
  }

  public void addFileAccess(File file, Permission permission){
    this.accessTree.addFileChild(file, permission);
  }

  public Node<User> getRootOfTree(){
    return (Node<User>)this.accessTree.getRoot();
  }

  public AccessTree<User> getAccessTree(){
    return accessTree;
  }

  @Override
  public boolean equals(Object obj){
    if(obj == null) return false;
    if (!User.class.isAssignableFrom(obj.getClass())) return false;

    final User other = (User) obj;
    if((this.username == null) ? (other.username == null) : !this.username.toUpperCase().equals(other.username.toUpperCase())){
      return false;
    }
    if((this.key == null) ? (other.getKey() == null) : !this.key.equals(other.getKey())){
      return false;
    }
    return true;
  }

  public void print(){
    System.out.println("Username: " +username + " " + "Key: " +key);
  }

}
