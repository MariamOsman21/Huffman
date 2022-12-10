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
        FileOutputStream output = new FileOutputStream(name);

        extractHeaderInfo(br);
        StringBuilder tree = readTree(br);
        Node root = reconstructTree(tree);
        HashMap<String, String> table = new HashMap<>();

        constructHashTable(root, "", table);

        int maxLength = 80000;
        StringBuilder fileData = new StringBuilder();
        int read;
        int read2 = br.read();
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
           // String s1 = String.format("%8s", Integer.toBinaryString((byte) read & 0xFF)).replace(' ', '0');
           // System.out.println(dataLength);
            fileData.append(cByte);
            if(read2 == -1) {
               // System.out.println(this.paddingBits);
                fileData.delete(fileData.length() -  this.paddingBits, fileData.length());
               // System.out.println(fileData);
               System.out.println("hhh");
                break;
            }
            if(fileData.length() == maxLength){
                writeBuffer(fileData, table, output);
            }
        }
      //  System.out.println(fileData);


        writeBuffer(fileData, table, output);
        if(lastBytes.length != 0) {
            System.out.println("anahena");
            output.write(lastBytes);
            output.flush();
        }
        output.close();
        br.close();
    }


    public void writeBuffer(StringBuilder encodedData, HashMap<String, String> table, FileOutputStream output) throws IOException {
        StringBuilder key = new StringBuilder();
        int indx = 0;
        byte[] buffer = new byte[8000];
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
            String cByte= Integer.toBinaryString((byte)b & 0xFF);
            int zeroBits = 8 - cByte.length();
           // System.out.println(cByte + " " + zeroBits + " " + (byte)c);
            while (zeroBits-- != 0) {
                encodedTable.append("0");
            }
            encodedTable.append(cByte);
          //  String s1 = String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
           // encodedTable.append(s1);
        }
      //  System.out.println(this.tablePaddingBits +"dddddddddddddddddddddddd");
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
        //System.out.println(encodingTable);
        if(encodingTable.charAt(this.indx) == '1'){
            this.indx++;
           // encodingTable.delete(0, 1);
            LeafNode node = new LeafNode();
            //System.out.println();
            node.data  = getString(encodingTable.substring(this.indx, this.indx +8*n));
            this.indx += 8 *this.n;
            //
            //System.out.println(node.data);
            //encodingTable.delete(0, 8*n);
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

        for(int i = 0; i < binaryString.length(); i+=8)
        {
            byte c = (byte) Integer.parseInt(binaryString.substring(i, i+8), 2);
          //  System.out.println(binaryString);
            data.append((char)c);
           // System.out.println(data + " " + c);
        }
       //System.out.println("d" + Arrays.toString(data.toString().getBytes(StandardCharsets.UTF_8)));

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


        HuffmanDecompression decompression = new HuffmanDecompression();
        String path = "file1.pdf.hc";

        decompression.decompress(path);

        File file = new File("file1.pdf");
        File file2 = new File("C:\\Users\\maria\\Downloads\\file1.pdf");
        //Use SHA-1 algorithm

        MessageDigest shaDigest = MessageDigest.getInstance("SHA-256");

        String shaChecksum = getFileChecksum(shaDigest, file);
        String s = getFileChecksum(shaDigest, file2);
        System.out.println(shaChecksum + " " + s);
        /*

        FileInputStream fr = new FileInputStream("file1.pdf");
        FileInputStream fr2 = new FileInputStream("C:\\Users\\maria\\Downloads\\file1.pdf");
        byte[] arr = fr.readAllBytes();
        byte[] arr2 = fr2.readAllBytes();

        for (int i = 0; i < arr.length; i++){
            if (arr[i] != arr2[i]) {
                System.out.println(arr[i] + " " + arr2[i]);
            }

        }


         */












    }




}
