import java.util.*;


class File extends AccessObject{

  public String fileName;
  private AccessTree<File> accessTree;

  public File(String fileName){
    this.fileName = fileName;
    accessTree = new AccessTree<File>(this);
  }

  public String getName(){
    return this.fileName;
  }

  public void addUserAccess(User user, Permission permission){
    this.accessTree.addUserChild(user, permission);
  }

  public void addGroupAccess(Group group, Permission permission){
    this.accessTree.addGroupChild(group, permission);
  }

  public Node<File> getRootOfTree(){
    return (Node<File>) this.accessTree.getRoot();
  }

  public AccessTree<File> getAccessTree(){
    return accessTree;
  }

  @Override
  public boolean equals(Object obj){
    if(obj == null) return false;
    if (!File.class.isAssignableFrom(obj.getClass())) return false;

    final File other = (File) obj;
    if((this.fileName == null) ? (other.fileName == null) : !this.fileName.equals(other.fileName)){
      return false;
    }
    return true;
  }
}
