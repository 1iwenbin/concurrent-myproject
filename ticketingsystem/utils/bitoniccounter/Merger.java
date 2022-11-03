package ticketingsystem.utils.bitoniccounter;

public class Merger {
    private Merger[] half;
    private Balancer[] layer;
    private final int width;

    public Merger(int width) {
        this.width = width;
        layer = new Balancer[width / 2];
        for (int i = 0 ; i < layer.length ; i ++) {
            layer[i] = new Balancer();
        }
        if (width > 2) {
            half = new Merger[] { new Merger(width / 2), new Merger(width / 2) };
        }
    }

    public int traverse(int input) {
        int out = 0;
        if (width <= 2) {
            return layer[0].traverse();
        }
        if (input < width / 2) {
            out = half[input % 2].traverse(input / 2);
        } else {
            out = half[1 - (input % 2)].traverse(input / 2);
        }
        return (2 * out) + layer[out].traverse();
    }
}
