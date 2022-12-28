import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class HuffmanCompression {


    public HuffmanCompression(int n) {
        this.n = n;
    }

    int n;
    byte[] lastBytes = new byte[0];
    int paddingBits = 0;
    int encodedTableLength = 0;
    int encodedTablePadding = 0;
    int MAXBITSNO = 8 * 1024;


    public HashMap<String, Integer> countFrequency(String path) throws IOException {
        HashMap<String, Integer> hashMap = new HashMap<>();
        File file = new File(path);
        BufferedInputStream br = new BufferedInputStream(new FileInputStream(file));
        int read;
        StringBuilder stringBuilder = new StringBuilder();
        int counter = 0;
        while ((read = br.read()) != -1) {
            counter++;
            char c = (char) ((byte) read);
            stringBuilder.append(c);
            if (counter == this.n) {
                counter = 0;
                String key = stringBuilder.toString();
                Integer freq = hashMap.get(key);
                if (freq != null)
                    hashMap.put(key, freq + 1);
                else
                    hashMap.put(key, 1);
                stringBuilder = new StringBuilder();
            }

        }
        br.close();
        //Save remainder bytes
        if (stringBuilder.length() != 0) {
            this.lastBytes = new byte[stringBuilder.length()];
            for (int i = 0; i < stringBuilder.length(); i++) {
                this.lastBytes[i] = (byte) stringBuilder.charAt(i);
            }
        }
        return hashMap;

    }

    public Node huffman(HashMap<String, Integer> hashMap) {

        PriorityQueue<Node> priorityQueue = new PriorityQueue<>();
        for (Map.Entry<String, Integer> entry : hashMap.entrySet()) {
            LeafNode leafNode = new LeafNode();
            leafNode.data = entry.getKey();
            leafNode.freq = entry.getValue();
            priorityQueue.add(leafNode);
        }
        while (priorityQueue.size() != 1) {
            Node parent = new Node();
            Node left = priorityQueue.remove();
            Node right = priorityQueue.remove();
            parent.left = left;
            parent.right = right;
            parent.freq = left.freq + right.freq;
            priorityQueue.add(parent);

        }
        return priorityQueue.remove();
    }

    public void constructHashTable(Node root, String bits, HashMap<String, String> table, StringBuilder encodingTable) {
        if (root instanceof LeafNode) {
            String key = ((LeafNode) root).data;
            table.put(key, bits);
            encodingTable.append("1");
            for (int j = 0; j < this.n; j++) {
                char c = key.charAt(j);
                String cByte = Integer.toBinaryString((byte) c & 0xFF);
                int zeroBits = 8 - cByte.length();
                while (zeroBits-- != 0) {
                    encodingTable.append("0");
                }
                encodingTable.append(cByte);
            }

            return;
        }
        encodingTable.append("0");
        constructHashTable(root.left, bits.concat("0"), table, encodingTable);
        constructHashTable(root.right, bits.concat("1"), table, encodingTable);
    }

    public void writeCompressedData(BufferedInputStream br, FileOutputStream output, HashMap<String, String> hashMap) throws IOException {
        int read;
        StringBuilder stringBuilder = new StringBuilder();
        StringBuilder encodedData = new StringBuilder();
        int counter = 0;
        while ((read = br.read()) != -1) {
            counter++;
            char c = (char) ((byte) read);
            stringBuilder.append(c);
            if (counter % this.n == 0) {
                String key = stringBuilder.toString();
                encodedData.append(hashMap.get(key));
                stringBuilder = new StringBuilder();
                if (encodedData.length() >= MAXBITSNO) {
                    byte[] buffer = constructBuffer(encodedData, MAXBITSNO);
                    output.write(buffer);
                    output.flush();
                }
            }
        }

        int paddingbits = 8 - encodedData.length() % 8;
        while (paddingbits-- != 0) {
            encodedData.append("0");
        }
        byte[] buffer = constructBuffer(encodedData, encodedData.length());
        output.write(buffer);
    }


    public byte[] constructBuffer(StringBuilder encodedData, int maxLength) {
        byte[] buffer = new byte[maxLength / 8];
        int bufferIndx = 0;
        int i = 0;
        while (bufferIndx != buffer.length) {
            String strbyte = encodedData.substring(i, i + 8);
            i += 8;
            buffer[bufferIndx] = (byte) Integer.parseInt(strbyte, 2);
            bufferIndx++;
        }
        encodedData = encodedData.delete(0, i);
        return buffer;
    }


    public int countPaddingBits(HashMap<String, Integer> freqMap, HashMap<String, String> bitMap) {
        int bitsNum = 0;
        for (Map.Entry<String, Integer> entry : freqMap.entrySet()) {
            bitsNum += entry.getValue() * bitMap.get(entry.getKey()).length();
        }
        return 8 - bitsNum % 8;
    }

    public byte[] compressTree(StringBuilder encodedTree) {
        int paddingbits = 8 - encodedTree.length() % 8;
        this.encodedTablePadding = paddingbits;
        while (paddingbits-- != 0) {
            encodedTree.append("0");
        }
        byte[] b = constructBuffer(encodedTree, encodedTree.length());
        this.encodedTableLength = b.length;
        return b;
    }

    private void writeHeaderInfo(FileOutputStream output) throws IOException {
        int indx = 0;
        byte[] info = new byte[11 + this.lastBytes.length];
        byte[] nBytes = ByteBuffer.allocate(4).putInt(this.n).array();
        for (int i = 0; i < 4; i++)
            info[indx++] = nBytes[i];
        info[indx++] = (byte) this.lastBytes.length;
        for (int i = 0; i < this.lastBytes.length; i++) {
            info[indx++] = this.lastBytes[i];
        }
        info[indx++] = (byte) this.paddingBits;
        byte[] length = ByteBuffer.allocate(4).putInt(this.encodedTableLength).array();
        for (int i = 0; i < 4; i++)
            info[indx++] = length[i];
        info[indx++] = (byte) this.encodedTablePadding;
        output.write(info);
    }

    public String compress(String path) throws IOException {
        HashMap<String, Integer> frequencyTable = countFrequency(path);
        Node tree = huffman(frequencyTable);
        HashMap<String, String> hashMap = new HashMap<>();

        StringBuilder encodedTree = new StringBuilder();
        constructHashTable(tree, "", hashMap, encodedTree);

        this.paddingBits = countPaddingBits(frequencyTable, hashMap);
        File file = new File(path);
        BufferedInputStream br = new BufferedInputStream(new FileInputStream(file));
        //outfile naming
        String outName = "19017274." + this.n + "." + file.getName() + ".hc";
        Path filePath = Paths.get(file.getParent(), outName);
        FileOutputStream output = new FileOutputStream(filePath.toString());
        byte[] byteTree = compressTree(encodedTree);
        writeHeaderInfo(output);
        output.write(byteTree);
        writeCompressedData(br, output, hashMap);
        br.close();
        output.close();
        return filePath.toString();
    }
}
