import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class HuffmanMain {
    /*
    “I acknowledge that I am aware of the academic integrity guidelines of this course,
    and that I worked on this assignment independently without any unauthorized help”
     */
    public static void main(String[] args) throws IOException {

        if(args[0].equals("c")){
            String path = args[1];
            int n = Integer.parseInt(args[2]);
            long time1 = System.currentTimeMillis();
            HuffmanCompression huffmanCompression = new HuffmanCompression(n);
            String compPath = huffmanCompression.compress(path);
            long time2 = System.currentTimeMillis();

            Path filepath = Paths.get(path);
            long orignalFileSize = Files.size(filepath);

            Path compressedPath = Paths.get(compPath);
             long compressedFileSize = Files.size(compressedPath);

            System.out.println("Compression Time: " + (time2 - time1) / (double) 1000 + " seconds");
            System.out.println("Compression Ratio: " + (compressedFileSize/ (double) orignalFileSize));
        }
        if(args[0].equals("d")){
            long time1 = System.currentTimeMillis();
            HuffmanDecompression huffmanDecompression = new HuffmanDecompression();
            huffmanDecompression.decompress(args[1]);
            long time2 = System.currentTimeMillis();
            System.out.println("Decompression Time: " + (time2 - time1) / (double) 1000 + " seconds");
        }


    }
}
