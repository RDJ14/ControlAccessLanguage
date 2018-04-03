import java.util.*;

class Node<T extends AccessObject>{

      private T data;
      private ArrayList<Edge> edges;
      private ArrayList<Node<? extends AccessObject>> childNodes;

      public Node() {
          this(null); // Call next constructor
      }

      public Node(T dataPortion) {
          data = dataPortion;
          edges = new ArrayList<Edge>();
          childNodes = new ArrayList<Node<? extends AccessObject>>();

      }

      public T getData() {
          return data;
      }

      public ArrayList<Edge> getEdges(){
        return edges;
      }

      public void setData(T newData) {
          data = newData;
      }

      public ArrayList<Node<? extends AccessObject>> getChildren() {
          return childNodes;
      }

      public void addChild(Node<? extends AccessObject> newChild, Edge edge){
        if(!childNodes.contains(newChild)){
          this.childNodes.add(newChild);
        }  
        addEdge(edge);
      }

      public void addChild(Node<? extends AccessObject> newChild) {

      }

      public void addEdge(Edge edge){
          if(!edges.contains(edge)){
            edges.add(edge);
          }
      }
      public ArrayList<Edge> getEdgesOfNode(Node<? extends AccessObject> otherEnd){
        ArrayList<Edge> returnData = new ArrayList<Edge>();
        for(int i = 0; i < edges.size(); i++){
          Edge edge = edges.get(i);
          ArrayList<Node<? extends AccessObject>> endpoints = edge.getEndpoints();
          Node<? extends AccessObject> end1 = endpoints.get(0);
          Node<? extends AccessObject> end2 = endpoints.get(1);

          if(otherEnd.equals(end1) && this.equals(end2)){
            returnData.add(edge);
          } else if(this.equals(end1) && otherEnd.equals(end2)){
            returnData.add(edge);
          }
        }
        return returnData;
      }
}
