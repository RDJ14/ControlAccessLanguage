import java.util.*;


class Group extends AccessObject{

    public String groupName;
    private ArrayList<Group> parentGroups;
    private ArrayList<Group> childGroups;
    private ArrayList<User> users;
    private AccessTree<Group> accessTree;

    public Group(String groupName){
      this.groupName = groupName;
      parentGroups = new ArrayList<Group>();
      childGroups = new ArrayList<Group>();
      accessTree = new AccessTree<Group>(this);
      users = new ArrayList<User>();
    }

    public String getName(){
      return this.groupName;
    }

    public ArrayList<User> getUsers(){
      return this.users;
    }

    public void addChildGroup(Group group){
      if(!childGroups.contains(group)){
        childGroups.add(group);
      }

    }

    public void addParentGroup(Group group){
      parentGroups.add(group);
      ArrayList<Node<? extends AccessObject>> accessNodes = accessTree.getRoot().getChildren();
      for(int i = 0; i < accessNodes.size(); i++){
        accessTree.addFileChild(accessNodes.get(i));
      }
    }

    public void addUser(User user){
      users.add(user);
    }

    public void addFileAccess(File file, Permission permission){
      accessTree.addFileChild(file, permission);
      for(int i = 0; i < parentGroups.size(); i++){
        parentGroups.get(i).getAccessTree().addFileChild(file, permission);
      }
    }

    public boolean hasFileAccess(File file, Permission permission){
      return accessTree.hasFileAccess(file, permission);
    }

    public AccessTree<Group> getAccessTree(){
      return this.accessTree;
    }

    public void printFileAccess(){
      accessTree.printFileAccess();
    }

    public void printFileAccess(Permission permission){
      accessTree.printFileAccess(permission);
    }

    public Node<Group> getRootOfTree(){
      return (Node<Group>) this.accessTree.getRoot();
    }

    public ArrayList<Group> getChildrenGroups(){
      return this.childGroups;
    }

    public ArrayList<Group> getParentGroups(){
      return this.parentGroups;
    }
    @Override
    public boolean equals(Object obj){
      if(obj == null) return false;
      if (!Group.class.isAssignableFrom(obj.getClass())) return false;

      final Group other = (Group) obj;
      if((this.groupName == null) ? (other.groupName == null) : !this.groupName.equals(other.groupName)){
        return false;
      }
      return true;
    }

    public void print(){
      System.out.println(groupName + ": ");
      System.out.println("  Users: ");
      for(int i = 0; i < users.size(); i++){
        System.out.print("    ");
        users.get(i).print();
      }
      System.out.println("  Parent Groups: ");
      for (int i = 0; i < parentGroups.size(); i++) {
        System.out.println("    " + parentGroups.get(i).getName());
      }
      System.out.println("  Child Groups: ");
      for (int i = 0; i < childGroups.size(); i++) {
        System.out.println("    " + childGroups.get(i).getName());
      }
      System.out.println("  File Access: ");
      ArrayList<Node<? extends AccessObject>> accessNodes = accessTree.getRoot().getChildren();
      for(int i = 0; i < accessNodes.size(); i++){
        System.out.println("    " +accessNodes.get(i).getData().getName() + " :");
        ArrayList<Edge> edges = accessNodes.get(i).getEdges();
        for(int j = 0; j < edges.size(); j++){
          System.out.println("      " + edges.get(j).getPermission());
        }
      }

    }

}
