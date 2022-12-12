import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class HuffmanDecompression {

    int n = 1;
    int paddingBits;
    int tableLength;
    int tablePaddingBits;
    byte[] lastBytes;


    public void decompress(String path) throws IOException {

        File file = new File(path);

        BufferedInputStream br = new BufferedInputStream(new FileInputStream(file));
        String name = file.getName();
        name = name.substring(0, name.length() - 3);
        BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(name));

        extractHeaderInfo(br);
        StringBuilder tree = readTree(br);
        Node root = reconstructTree(tree);
        HashMap<String, String> table = new HashMap<>();
        long t = System.currentTimeMillis();

        constructHashTable(root, "", table);
        System.out.println(System.currentTimeMillis() - t);

        int maxLength = 8000;
        StringBuilder fileData = new StringBuilder();
        int read;
        int read2 = br.read();
        Node itr = root;
        //write last bytes
        //remove padding لسة عايزة تتظبط
        // table not constructed properly

        while (true){
            read = read2;
            read2 = br.read();
            String cByte= Integer.toBinaryString((byte)read & 0xFF);
            int zeroBits = 8 - cByte.length();
            // System.out.println(cByte + " " + zeroBits + " " + (byte)c);
            while (zeroBits-- != 0) {
                fileData.append("0");
            }
            fileData.append(cByte);
            if(read2 == -1) {
               fileData.delete(fileData.length() -  this.paddingBits, fileData.length());
                break;
            }
            if(fileData.length() >= maxLength){

                itr = getData(fileData, root, itr, output);

                //writeBuffer(fileData, table, output);
            }
        }

        getData(fileData, root, itr, output);


       // writeBuffer(fileData, table, output);
        if(lastBytes.length != 0) {
            output.write(lastBytes);
            output.flush();
        }
        output.close();
        br.close();
    }


    public void writeBuffer(StringBuilder encodedData, HashMap<String, String> table, BufferedOutputStream output) throws IOException {
        StringBuilder key = new StringBuilder();
        int indx = 0;
        byte[] buffer = new byte[1024];
        int bufferIndx = 0;
        for(int i = 0; i < encodedData.length(); i++){
            //System.out.println("hhhh");
            key.append(encodedData.charAt(i));
            String value = table.get(key.toString());
            if(value != null) {
                indx += key.length();

               // System.out.println(key);
                key = new StringBuilder();
                for(int j = 0; j < value.length(); j++) {
                    //System.out.println();
                    buffer[bufferIndx++]= (byte) value.charAt(j);

                    if(bufferIndx == buffer.length){
                        output.write(buffer);
                        bufferIndx = 0;
                    }
                }
            }
        }
        byte[] remBuffer = new byte[bufferIndx];
        for (int i = 0; i < bufferIndx; i++){
            remBuffer[i] = buffer[i];
        }
        output.write(remBuffer);
        encodedData.delete(0, indx);
    }


    public Node getData(StringBuilder encodedData, Node root, Node itr, BufferedOutputStream output) throws IOException {
        int indx = 0;
        byte[] buffer = new byte[1024];
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







    public void constructHashTable(Node root, String bits, HashMap<String, String> table){
        if(root instanceof LeafNode){
            String key = ((LeafNode) root).data;
            table.put(bits, key);
            return;
        }
        if(root != null) {
            constructHashTable(root.left, bits.concat("0"), table);
            constructHashTable(root.right, bits.concat("1"), table);
        }
    }


    public void extractHeaderInfo(BufferedInputStream br) throws IOException {
        this.n = br.read();
       // System.out.println(n);
        int lastBytesLength = br.read();
        this.lastBytes = new byte[lastBytesLength];
        //System.out.println(lastBytesLength);
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
/*
    public Node reconstructTree(StringBuilder encodingTable){
       // System.out.println(encodingTable);
        if(encodingTable.charAt(0) == '1'){
            encodingTable.delete(0, 1);
            LeafNode node = new LeafNode();
            node.data  = getString(encodingTable.substring(0, 8*n));
            //
            //System.out.println(node.data);
            encodingTable.delete(0, 8*n);
            return node;
        }
        encodingTable.delete(0, 1);
        Node node = new Node();
        node.left = reconstructTree(encodingTable);
        node.right = reconstructTree(encodingTable);
        return node;

    }

 */


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

    private static String getFileChecksum(MessageDigest digest, File file) throws IOException
    {
        //Get file input stream for reading the file content
        FileInputStream fis = new FileInputStream(file);

        //Create byte array to read data in chunks
        byte[] byteArray = new byte[1024];
        int bytesCount = 0;

        //Read file data and update in message digest
        while ((bytesCount = fis.read(byteArray)) != -1) {
            digest.update(byteArray, 0, bytesCount);
        };

        //close the stream; We don't need it now.
        fis.close();

        //Get the hash's bytes
        byte[] bytes = digest.digest();

        //This bytes[] has bytes in decimal format;
        //Convert it to hexadecimal format
        StringBuilder sb = new StringBuilder();
        for(int i=0; i< bytes.length ;i++)
        {
            sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
        }

        //return complete hash
        return sb.toString();
    }

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {

        long t = System.currentTimeMillis();
        /*

        HuffmanDecompression decompression = new HuffmanDecompression();
        String path = "file1.pdf.hc";

        decompression.decompress(path);
        System.out.println(System.currentTimeMillis() - t);

         */

        File file = new File("gbbct10.seq");
        File file2 = new File("C:\\Users\\maria\\Downloads\\gbbct10.seq");
        //Use SHA-1 algorithm



        MessageDigest shaDigest = MessageDigest.getInstance("SHA-256");

        String shaChecksum = getFileChecksum(shaDigest, file);
        String s = getFileChecksum(shaDigest, file2);

        if(s.equals(shaChecksum))
            System.out.println("heeeeeeeeeeeh");
        System.out.println(shaChecksum + " " + s);





    }




}
