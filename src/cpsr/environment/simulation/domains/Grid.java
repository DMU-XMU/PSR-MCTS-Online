package cpsr.environment.simulation.domains;



import java.util.Vector;

public class Grid {
    private int XSize,YSize;
    private Vector<Integer> grid;




    public Grid(int XSize, int YSize) {
        this.XSize = XSize;
        this.YSize = YSize;
        grid=new Vector<>();
        grid.setSize(XSize*YSize);
    }



    public int getGrid(int index){
        return grid.get(index);
    }
    public void setGrid(int index,int value){
        grid.set(index,value);

    }

    public int index(Coord coord){
        return XSize*coord.Y+coord.X;
    }

    public int index(int x,int y){
        return XSize*y+x;
    }


    public void SetAllValues(int value){
        for (int x=0;x<XSize;x++){
            for (int y=0;y<YSize;y++){
                //System.out.println(index(x,y));
                grid.setElementAt(value,index(x,y));
            }
        }
    }
    public void setValue(Coord coord,int value){
        grid.set(index(coord),value);
    }












}
