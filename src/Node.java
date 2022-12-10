public class Node implements Comparable<Node>{
    long  freq = 0;
    Node left;
    Node right;

    @Override
    public int compareTo(Node o) {
        return Long.compare(this.freq, o.freq);
    }
}

