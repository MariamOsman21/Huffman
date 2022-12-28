import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HuffmanDecompression {

    int n = 1;
    int paddingBits;
    int tableLength;
    int tablePaddingBits;
    byte[] lastBytes;
    int MAXBITSNO = 8*1024;
    int MAXBUFFER = 1024;


    public void decompress(String path) throws IOException {
        File file = new File(path);
        BufferedInputStream br = new BufferedInputStream(new FileInputStream(file));
        String name = file.getName();
        name = name.substring(0, name.lastIndexOf('.'));
        String outName = "extracted." + name;
        Path filePath = Paths.get(file.getParent(),outName);
        BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(filePath.toString()));
        extractHeaderInfo(br);
        StringBuilder tree = readTree(br);
        Node root = reconstructTree(tree);
        StringBuilder fileData = new StringBuilder();
        int read;
        int read2 = br.read();
        Node itr = root;

        while (true){
            read = read2;
            read2 = br.read();
            String cByte= Integer.toBinaryString((byte)read & 0xFF);
            int zeroBits = 8 - cByte.length();
            while (zeroBits-- != 0) {
                fileData.append("0");
            }
            fileData.append(cByte);
            if(read2 == -1) {
               fileData.delete(fileData.length() -  this.paddingBits, fileData.length());
                break;
            }
            if(fileData.length() >= MAXBITSNO){

                itr = writeBuffer(fileData, root, itr, output);
            }
        }

        writeBuffer(fileData, root, itr, output);
        if(lastBytes.length != 0) {
            output.write(lastBytes);
            output.flush();
        }
        output.close();
        br.close();
    }


    public Node writeBuffer(StringBuilder encodedData, Node root, Node itr, BufferedOutputStream output) throws IOException {
        int indx = 0;
        byte[] buffer = new byte[this.MAXBUFFER];
        int bufferIndx = 0;
        if(itr == null)
            itr = root;

        for(int i = 0; i < encodedData.length(); i++){
            if(encodedData.charAt(i)== '0')
                itr = itr.left;
            else
                itr = itr.right;
            if(itr instanceof LeafNode){
                String value = ((LeafNode) itr).data;
                itr = root;
                indx += i;
                for (int j = 0; j < this.n; j++){

                    if(bufferIndx == buffer.length){
                        bufferIndx = 0;
                        output.write(buffer);
                    }
                    buffer[bufferIndx++] = (byte) value.charAt(j);
                }

            }
        }
        byte[] remBuffer = new byte[bufferIndx];
        for (int i = 0; i < bufferIndx; i++){
            remBuffer[i] = buffer[i];
        }
        output.write(remBuffer);
        output.flush();
        encodedData.delete(0, indx);
        return itr;

    }

    public void extractHeaderInfo(BufferedInputStream br) throws IOException {

        byte[] nBytes = new byte[4];
        for(int i = 0; i < 4; i++)
            nBytes[i] = (byte) br.read();
        ByteBuffer bBuffer = ByteBuffer.wrap(nBytes);
        this.n = bBuffer.getInt();
        int lastBytesLength = br.read();
        this.lastBytes = new byte[lastBytesLength];
        for(int i = 0; i < lastBytesLength; i++)
            this.lastBytes[i] = (byte) br.read() ;

        this.paddingBits = br.read();

        byte[] tableLengthBytes = new byte[4];
        for(int i = 0; i < 4; i++)
            tableLengthBytes[i] = (byte) br.read();


        ByteBuffer byteBuffer = ByteBuffer.wrap(tableLengthBytes);
        this.tableLength = byteBuffer.getInt();
        this.tablePaddingBits = br.read();
    }

    public StringBuilder readTree(BufferedInputStream br) throws IOException {
        StringBuilder encodedTable = new StringBuilder();
        for(int i = 0; i < this.tableLength; i++){
            byte b = (byte) br.read();
            String cByte= Integer.toBinaryString(b & 0xFF);
            int zeroBits = 8 - cByte.length();
            while (zeroBits-- != 0) {
                encodedTable.append("0");
            }
            encodedTable.append(cByte);

        }
        encodedTable = encodedTable.delete(encodedTable.length()- this.tablePaddingBits, encodedTable.length());
        return encodedTable;
    }

    int indx = 0;
    public Node reconstructTree(StringBuilder encodingTable){
        if(encodingTable.charAt(this.indx) == '1'){
            this.indx++;
            LeafNode node = new LeafNode();
            node.data  = getString(encodingTable.substring(this.indx, this.indx +8*n));
            this.indx += 8 *this.n;
            return node;
        }
        this.indx++;
        Node node = new Node();
        node.left = reconstructTree(encodingTable);
        node.right = reconstructTree(encodingTable);
        return node;

    }

    public String getString(String binaryString){
        StringBuilder data = new StringBuilder();

        for(int i = 0; i < binaryString.length(); i+=8) {
            byte c = (byte) Integer.parseInt(binaryString.substring(i, i+8), 2);
            data.append((char)c);
        }
        return data.toString();
    }

}
