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

    public void addFileChild(Node<? extends AccessObject> addNode){
      File file = (File) addNode.getData();
      ArrayList<Edge> edges = addNode.getEdges();
      for(int i = 0; i < edges.size(); i++){
        Permission permission = edges.get(i).getPermission();
        addFileChild(file, permission);
      }
    }

    public void addFileChild(File file, Permission permission){
        Node<File> fileNode = null;
        boolean existingNode = false;
        for(int i = 0; i < root.getChildren().size(); i++){
          if(root.getChildren().get(i).getData().equals(file)){
            fileNode = (Node<File>) root.getChildren().get(i);
            existingNode = true;
          }
        }
        if(!existingNode){
          fileNode = new Node<File>(file);
        }

        Edge edge = new Edge(root, fileNode, permission);
        root.addChild(fileNode, edge);
        fileNode.addEdge(edge);
    }

    /*
        Adding a new group has to add all the permissions that a group access tree has
    */
    public void addGroupChild(Group group, Permission permission){
        //All files of the group
        Node<Group> groupNode = null;
        boolean existingNode = false;
        for(int i = 0; i < root.getChildren().size(); i++){
          if(root.getChildren().get(i).getData().equals(group)){
            groupNode = (Node<Group>) root.getChildren().get(i);
            existingNode = true;
          }
        }
        if(!existingNode){
          groupNode = new Node<Group>(group);
        }
        Edge newEdge = new Edge(this.root, groupNode, permission);
        root.addChild(groupNode, newEdge);
        groupNode.addEdge(newEdge);
        ArrayList<Group> parentGroups = group.getParentGroups();
        ArrayList<Group> parentCopy = new ArrayList<Group>();
        for(int i = 0; i < parentGroups.size(); i++){
          parentCopy.add(parentGroups.get(i));
        }
        while(parentCopy.size() > 0){
          Group aParentGroup = parentCopy.get(0);
          parentCopy.remove(0);
          addGroupChild(aParentGroup, permission);
        }
    }


    public void addUserChild(User user, Permission permission){
      Node<User> newNode = null;
      boolean existingNode = false;
      for(int i = 0; i < root.getChildren().size(); i++){
        if(root.getChildren().get(i).getData().equals(user)){
          newNode = (Node<User>) root.getChildren().get(i);
          existingNode = true;
        }
      }
      if(!existingNode){
        newNode = new Node<User>(user);
      }
      Edge newEdge = new Edge(root, newNode, permission);
      root.addChild(newNode, newEdge);
      newNode.addEdge(newEdge);
    }

    public void printFileAccess(){
      ArrayList<Node<? extends AccessObject>> accessNodes = root.getChildren();
      for(int i = 0; i < accessNodes.size(); i++){
        Node<? extends AccessObject> node = accessNodes.get(i);
        System.out.println("File: " +node.getData().getName());
        ArrayList<Edge> edges = node.getEdges();
        for(int j = 0; j < edges.size(); j++){
          System.out.println("  " + edges.get(j).getPermission());
        }
      }
    }
    public void printFileAccess(Permission permission){
      ArrayList<Node<? extends AccessObject>> accessNodes = root.getChildren();
      for(int i = 0; i < accessNodes.size(); i++){
        Node<? extends AccessObject> node = accessNodes.get(i);
        ArrayList<Edge> edges = node.getEdges();
        for(int j = 0; j < edges.size(); j++){
          if(edges.get(j).getPermission() == permission)
            System.out.println("File: " +node.getData().getName());
        }
      }
    }

    public void printUserAccess(Permission permission){
      ArrayList<Node<? extends AccessObject>> accessNodes = root.getChildren();
      for(int i = 0; i < accessNodes.size(); i++){
        Node<? extends AccessObject> node = accessNodes.get(i);
        if(User.class.isAssignableFrom(node.getData().getClass())){
            ArrayList<Edge> edges = node.getEdges();
            for(int j = 0; j < edges.size(); j++){
              if(edges.get(j).getPermission() == permission)
                System.out.println("User: " +node.getData().getName());
            }
        }
      }
    }

    public void printGroupAccess(Permission permission){
      ArrayList<Node<? extends AccessObject>> accessNodes = root.getChildren();
      for(int i = 0; i < accessNodes.size(); i++){
        Node<? extends AccessObject> node = accessNodes.get(i);
        if(Group.class.isAssignableFrom(node.getData().getClass())){
            ArrayList<Edge> edges = node.getEdges();
            for(int j = 0; j < edges.size(); j++){
              if(edges.get(j).getPermission() == permission)
                System.out.println("Group: " +node.getData().getName());
            }
        }
      }
    }

    public boolean hasFileAccess(File file, Permission permission){
      ArrayList<Node<? extends AccessObject>> accessNodes = root.getChildren();
      for(int i = 0; i < accessNodes.size(); i++){
        Node<? extends AccessObject> node = accessNodes.get(i);
        if(node.getData().equals(file)){
          ArrayList<Edge> edges = node.getEdges();
          for(int j = 0; j < edges.size(); j++){
            Edge edge = edges.get(j);
            if(edge.getPermission() == permission){
              return true;
            }
          }
        }
      }
      return false;
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
