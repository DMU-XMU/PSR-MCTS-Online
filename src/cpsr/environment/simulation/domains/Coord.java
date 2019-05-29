package cpsr.environment.simulation.domains;

public class Coord {
    public int X,Y;

    public Coord(int x, int y) {
        X = x;
        Y = y;
    }


    public static double EuclideanDistance(Coord lhs,Coord rhs){
        return Math.sqrt((lhs.X-rhs.X)*(lhs.X-rhs.X)+(lhs.Y-rhs.Y)*(lhs.Y-rhs.Y));
    }


}
