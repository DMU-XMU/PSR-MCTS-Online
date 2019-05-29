package cpsr.planning.mcts;

import java.util.Vector;


public class Node {
    private int QNumChildren;
    private int VNumChildren;

    public Node(int QNumChildren, int VNumChildren) {
        this.QNumChildren = QNumChildren;
        this.VNumChildren = VNumChildren;
    }

    public class QNODE{
        public Value value;
        public int NumChildren=QNumChildren;
        public VNODE Child(int c){
            return Children.get(c);
        }

        public void setChild(int c,VNODE vnode){
            Children.setElementAt(vnode,c);
        }


        public void Initialise(){
            Children.setSize(NumChildren);
            for (int observation = 0; observation < NumChildren; observation++){
                Children.setElementAt(null,observation);
            }

        }
        private Vector<VNODE> Children;

        public QNODE() {
            Children = new Vector<>();
            value=new Value();
        }
    }




    public class VNODE{
        public Value value;
        public int NumChildren=VNumChildren;

        public void Initialise(){
            Children.setSize(NumChildren);

            for (int action = 0; action < NumChildren; action++){
                Children.setElementAt(new QNODE(),action);
                Children.get(action).Initialise();
            }


        }
        public void SetChildren(int count, double value){
            for (int action = 0; action < NumChildren; action++)
            {
                QNODE qnode = Children.get(action);
                qnode.value.Set(count, value);
            }
        }

        QNODE Child(int c){
            return Children.get(c);
        }

        private Vector<QNODE> Children;

        public VNODE() {
            Children = new Vector<>();
            value=new Value();
        }
    }
}
