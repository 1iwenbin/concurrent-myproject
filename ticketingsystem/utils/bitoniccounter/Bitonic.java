package ticketingsystem.utils.bitoniccounter;

public class Bitonic {
    private Bitonic[] half;
    private final Merger merger;
    private final int width;


    public Bitonic(int width) {
        this.width = width;
        merger = new Merger(width);
        if (width > 2) {
            half = new Bitonic[] { new Bitonic(width / 2), new Bitonic(width / 2) };
        }
    }

    public int traverse(int input) {
        int out = 0;
        if (width > 2) {
            out = half[input / (width / 2)].traverse(input / 2);
        }
        return merger.traverse((input >= (width / 2) ?(width / 2) : 0) + out);
    }
}
