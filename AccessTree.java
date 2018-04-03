import java.util.*;

/*
  The root is the object which the tree has information about access. T is the data
  type of the node. The node will be either a user, a group or a file. The edges
  connecting the root to the children are permission relation between the root and child. If the root
  and child have many permissions then there are several edges between the two.
*/
class AccessTree<T>{

    private Node<? extends AccessObject> root;

    public AccessTree(AccessObject dataType){
      root = new Node<AccessObject>(dataType);
    }

    public Node<? extends AccessObject> getRoot(){
      return this.root;
    }

    public ArrayList<Node<? extends AccessObject>> getChildren(){
      return this.root.getChildren();
    }

    public void addFileChild(File file, Permission permission){
        Node<File> fileNode = new Node<File>(file);
        Edge edge = new Edge(root, fileNode, permission);

        root.addChild(fileNode, edge);
    }

    /*
        Adding a new group has to add all the permissions that a group access tree has
    */
    public void addGroupChild(Group group, Permission permission){
        //All files of the group
        ArrayList<Node<? extends AccessObject>> groupChildren = group.getAccessTree().getChildren();
        for(int i = 0; i < groupChildren.size(); i++){
          Node<? extends AccessObject> tempNode = groupChildren.get(i);
          addGroupHelper(tempNode, group, permission);
        }
    }

    private void addGroupHelper(Node<? extends AccessObject> childNode, Group group, Permission permission){
        //if the root already contains the object we dont want to add another node.
        //instead we just need to add a new edge if the permission doesnt already exist for that file
        Node<Group> groupNode = new Node<Group>(group);
        Edge newEdge = new Edge(this.root, groupNode, permission);
        root.addChild(childNode, newEdge);
        newEdge.print();

        ArrayList<Group> childGroups = group.getChildrenGroups();
        while(childGroups.size() > 0){
          Group aChildGroup = childGroups.get(0);
          childGroups.remove(0);
          addGroupHelper(childNode, aChildGroup, permission);
        }

    }

    public void addUserChild(User user, Permission permission){
      Node<User> newNode = new Node<User>(user);
      Edge newEdge = new Edge(root, newNode, permission);
      root.addChild(newNode, newEdge);
    }

    public void print(){
      System.out.println();
      System.out.println("--------------------------------------------------------------------");
      System.out.println("Root: " + root.getData().getName());
      ArrayList<Edge> edges = root.getEdges();
      for(int i = 0; i < edges.size(); i++){
        edges.get(i).print();
      }
      System.out.println("--------------------------------------------------------------------");
      System.out.println();
      System.out.println();

    }





}
