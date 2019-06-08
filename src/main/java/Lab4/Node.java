package Lab4;

import java.util.ArrayList;
import java.util.HashMap;


public class Node {
    private String nodeName;
    private ArrayList<String> data;
    private Node parent;
    private ArrayList<Node> children;
    private HashMap<String, Node> childrenDictionary;

    public Node(String name) {
        this.nodeName = name;
        this.data = new ArrayList<String>();
        this.parent = null;
        this.children = new ArrayList<>();
        this.childrenDictionary = new HashMap<>();


    }

    public Node(String name, Node parent) {
        this.nodeName = name;
        this.data = new ArrayList<String>();
        this.setParent(parent);
        this.children = new ArrayList<>();
        this.childrenDictionary = new HashMap<>();
    }

    public Node(String name, ArrayList<String> data) {
        this.nodeName = name;
        this.data = data;
        this.parent = null;
        this.children = new ArrayList<>();
        this.childrenDictionary = new HashMap<>();

    }

    public Node(String name, Node parent, ArrayList<String> data) {
        this.nodeName = name;
        this.data = data;
        this.setParent(parent);
        this.children = new ArrayList<>();
        this.childrenDictionary = new HashMap<>();

    }

    public String getNodeName() {
        return this.nodeName;
    }

    public void setNodeData(ArrayList<String> data) {
        this.data = data;
    }


    public Node getParent() {
        return this.parent;
    }


    public ArrayList<Node> getChildren() {
        return this.children;
    }


    public Node getFirstChild() {
        return this.children.get(0);
    }


    public Node getLastChild() {
        return this.children.get(children.size() - 1);
    }


    public Node getPreviousSibling() {
        for (int i = 0; i < this.getParent().getChildren().size(); i++) {
            if (this.getParent().getChildren().get(i).getNodeName().equals(this.nodeName) && (i - 1) >= 0) {
                return this.getParent().getChildren().get(i - 1);
            }
        }
        return null;
    }


    public Node getNextSibling() {
        for (int i = 0; i < this.getParent().getChildren().size(); i++) {
            if (this.getParent().getChildren().get(i).getNodeName().equals(this.nodeName) && (i + 1) < this.getParent().getChildren().size()) {
                return this.getParent().getChildren().get(i + 1);
            }
        }
        return null;
    }


    public void insertBefore(Node newChild, Node refChild) throws Exception {
        int index = -1;
        boolean found = false;
        for (int i = 0; i < this.getChildren().size(); i++) {
            if (this.getChildren().get(i).getNodeName().equals(refChild.getNodeName())) {
                index = i;
                found = true;
                break;
            }
        }

        if (found) {
            if (index == 0) {
                this.getChildren().add(0, newChild);
            } else {
                this.getChildren().add(index - 1, newChild);

            }
            childrenDictionary.put(newChild.getNodeName(), newChild);
        } else {
            throw new Exception("Reference Node Not Found");
        }

    }

    public void insertAfter(Node newChild, Node refChild) throws Exception {
        int index = -1;
        boolean found = false;
        for (int i = 0; i < this.getChildren().size(); i++) {
            if (this.getChildren().get(i).getNodeName().equals(refChild.getNodeName())) {
                index = i;
                found = true;
                break;
            }
        }

        if (found) {
            if (index == this.getChildren().size()) {
                this.getChildren().add(newChild);
            } else {
                this.getChildren().add(index + 1, newChild);

            }
            childrenDictionary.put(newChild.getNodeName(), newChild);
        } else {
            throw new Exception("Reference Node Not Found");
        }

    }

    public Node replaceChild(Node newChild, Node oldChild) throws Exception {
        int index = -1;
        boolean found = false;
        for (int i = 0; i < this.getChildren().size(); i++) {
            if (this.getChildren().get(i).getNodeName().equals(oldChild.getNodeName())) {
                index = i;
                found = true;
                break;
            }
        }

        if (found) {
            Node old = this.getChildren().get(index);
            this.getChildren().set(index, newChild);
            childrenDictionary.put(newChild.getNodeName(), newChild);
            childrenDictionary.remove(oldChild.getNodeName());

            return old;
        } else {
            throw new Exception("Old Child Not Found");
        }
    }

    public Node removeChild(Node oldChild) throws Exception {
        int index = -1;
        boolean found = false;
        for (int i = 0; i < this.getChildren().size(); i++) {
            if (this.getChildren().get(i).getNodeName().equals(oldChild.getNodeName())) {
                index = i;
                found = true;
                break;
            }
        }

        if (found) {
            Node old = this.getChildren().get(index);
            this.getChildren().remove(index);
            childrenDictionary.remove(oldChild.getNodeName());

            return old;
        } else {
            throw new Exception("Old Child Not Found");
        }
    }


    public void appendChild(Node newChild) {
        this.getChildren().add(newChild);
    }

    public void appendData(String newData) {
        this.getData().add(newData);
    }

    public void appendDataList(ArrayList<String> newData) {
        this.getData().addAll(newData);
    }

    public void setData(ArrayList<String> newData) {
        this.data = newData;
    }

    public void setParent(Node parent) {
        this.parent = parent;
        parent.childrenDictionary.put(this.getNodeName(), this);
        parent.appendChild(this);
//        System.out.print("CHECK " + parent.getChildren() );


    }

    public boolean hasChildNodes() {
        return !this.getChildren().isEmpty();
    }


    public boolean equals(Node node, Node refNode) {
        if (node.getNodeName().equals(refNode.getNodeName()) && node.getChildren().equals(refNode.getChildren()) && node.getParent().equals(refNode.getParent()) && node.getData().equals(refNode.getData())) {
            return true;
        } else {

            return false;
        }
    }

    public ArrayList<String> getData() {
        return data;
    }

    public static void printGraph(Node startingNode) {
        printNode(startingNode);
        for (int i = 0; i < startingNode.getChildren().size(); i++) {
            System.out.println("++++++++++++++++++++++++++++++++");
            printGraph(startingNode.getChildren().get(i));
        }
    }

    public static void printNode(Node node) {
        System.out.println("Node Name: " + node.getNodeName());

        if (node.getParent() == null) {
            System.out.println("Node Parent: No Parent");
        } else {
            System.out.println("Node Parent: " + node.getParent().getNodeName());
        }

        if (node.getData().size() == 0) {
            System.out.println("Node Data: No Data");
        } else {
            System.out.println("Node Data: " + node.data);
        }


        String children = "";
//        System.out.println("CHILD "+ node.getChildren());
        for (int i = 0; i < node.getChildren().size(); i++) {
            children += node.getChildren().get(i).getNodeName()+ ", ";
        }

        if (children.equals("")) {
            System.out.println("Node Children: No Children");
        } else {
            System.out.println("Node Children: " + children.substring(0,children.length()-2));
        }

    }

    public String toString(){
        return this.getNodeName();
    }


}
