import java.util.*;


public class Edge{

  public Node<? extends AccessObject> endpoint1;
  public Node<? extends AccessObject> endpoint2;
  private Permission permission;

  public Edge(Node<? extends AccessObject> endpoint1, Node<? extends AccessObject> endpoint2, Permission permission){
    this.endpoint1 = endpoint1;
    this.endpoint2 = endpoint2;
    this.permission = permission;
  }


  public ArrayList<Node<? extends AccessObject>> getEndpoints(){
    ArrayList<Node<? extends AccessObject>> ends = new ArrayList<Node<? extends AccessObject>>();
    ends.add(endpoint1);
    ends.add(endpoint2);
    return ends;
  }

  public Node<? extends AccessObject> getOtherEnd(Node<AccessObject> end){
    if(end.equals(endpoint1)){
      return endpoint2;
    }
    return endpoint1;
  }

  public Permission getPermission(){
    return permission;
  }

  @Override
  public boolean equals(Object obj){
    if(obj == null) return false;
    if (!Edge.class.isAssignableFrom(obj.getClass())) return false;

    final Edge other = (Edge) obj;
    if((this.endpoint1).equals(other.endpoint1) || (this.endpoint1).equals(other.endpoint2)){
      if((this.endpoint2).equals(other.endpoint1) || (this.endpoint2).equals(other.endpoint2)) {
        if(this.getPermission() == other.getPermission()){
          return true;
        }
      }
    }
    return false;
  }

  public void print(){
    System.out.println("Edge: " +endpoint1.getData().getName() + "--  " +permission + "  --" + endpoint2.getData().getName());
  }

}
