import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class HuffmanCompression {


    public HuffmanCompression(int n){
        this.n = n;
    }
    int n;
    byte[] lastBytes = new byte[0];
    int paddingBits = 0;
    StringBuilder encodingTable = new StringBuilder();
    int encodedTableLength = 0;
    int encodedTablePadding = 0;


    public HashMap<String, Integer> countFrequency(String path) throws IOException {
        HashMap<String, Integer> hashMap = new HashMap<>();
        File file = new File(path);
        BufferedInputStream br = new BufferedInputStream(new FileInputStream(file));
        int read;
        StringBuilder stringBuilder = new StringBuilder();
        long counter = 0;
        while ((read = br.read()) != -1) {
            counter++;
            char c =  (char) ((byte) read);
            stringBuilder.append(c);
            if (counter % this.n == 0) {
                String key = stringBuilder.toString();
                //System.out.println(Arrays.toString(key.getBytes(StandardCharsets.UTF_8)));
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
        if (stringBuilder.length() != 0){
            this.lastBytes = new byte[stringBuilder.length()];
            for (int i = 0; i < stringBuilder.length(); i++) {
                this.lastBytes[i]  = (byte) stringBuilder.charAt(i);
            }
        }
        return hashMap;
    }

    public Node huffman(HashMap<String, Integer> hashMap){

        PriorityQueue<Node> priorityQueue = new PriorityQueue<>();
        for(Map.Entry<String, Integer> entry: hashMap.entrySet()) {
            LeafNode leafNode= new LeafNode();
            leafNode.data = entry.getKey();
            leafNode.freq = entry.getValue();
            priorityQueue.add(leafNode);
        }
        while (priorityQueue.size() != 1){
            Node parent = new Node();
            Node left =  priorityQueue.remove();
            Node right = priorityQueue.remove();
            parent.left = left;
            parent.right = right;
            parent.freq = left.freq + right.freq;
            priorityQueue.add(parent);

        }
        return priorityQueue.remove();
    }

    public void constructHashTable(Node root, String bits, HashMap<String, String> table, StringBuilder encodingTable){
        if(root instanceof LeafNode){
            String key = ((LeafNode) root).data;
            table.put(key, bits);
            encodingTable.append("1");
            encodingTable.append(key);
            return;
        }
        encodingTable.append("0");
        constructHashTable(root.left,bits.concat("0"), table,encodingTable);
        constructHashTable(root.right, bits.concat("1"), table,encodingTable);
    }

    public void writeCompressedData(BufferedInputStream br, FileOutputStream output, HashMap<String, String> hashMap) throws IOException {
        int read;
        StringBuilder stringBuilder = new StringBuilder();
        StringBuilder encodedData = new StringBuilder();
        int counter = 0;
        int maxLength = 80000;
        while ((read = br.read()) != -1) {
            counter++;
            char c =  (char) ((byte) read);
            stringBuilder.append(c);
            if (counter % this.n == 0) {
                String key = stringBuilder.toString();
                encodedData.append(hashMap.get(key));
                stringBuilder = new StringBuilder();
                if(encodedData.length() >= maxLength){
                    byte[] buffer = constructBuffer(encodedData, maxLength);
                    output.write(buffer);
                    output.flush();
                }
            }
        }

        int paddingbits = 8 - encodedData.length() % 8;
        while (paddingbits--!= 0) {
            encodedData.append("0");
        }
        byte[] buffer = constructBuffer(encodedData, encodedData.length());
        output.write(buffer);
    }


    public byte[] constructBuffer(StringBuilder encodedData , int maxLength) {
        //System.out.println(maxLength/8);
        byte[] buffer = new byte[maxLength/8];
        int bufferIndx = 0;
        int i = 0;
        while (bufferIndx != buffer.length){
            String strbyte = encodedData.substring(i, i+8);
            i+=8;
            buffer[bufferIndx] = (byte)Integer.parseInt(strbyte, 2);
           // System.out.println(strbyte + " " + buffer[bufferIndx]);
            bufferIndx++;
        }
        encodedData = encodedData.delete(0, i);
        return  buffer;
    }


    public int countPaddingBits(HashMap<String, Integer> freqMap , HashMap<String, String> bitMap){
        int bitsNum = 0;

        for(Map.Entry<String, Integer> entry: freqMap.entrySet()) {
            bitsNum += entry.getValue() * bitMap.get(entry.getKey()).length();
        }


      //  System.out.println(bitsNum / 8 + "fileSize");
        return 8 - bitsNum % 8;
    }

    public byte[] compressTable(StringBuilder encodingTable){
        StringBuilder binaryTable = new StringBuilder();
        int i = 0;
        //long t = System.currentTimeMillis();
        while ( i != encodingTable.length()){
            char c =encodingTable.charAt(i);
            binaryTable.append(c);
            //writeBytes of treeNode
            if(c == '1'){
                for(int j = 0; j < this.n; j++) {
                    i++;
                    c = encodingTable.charAt(i);

                    String cByte= Integer.toBinaryString((byte)c & 0xFF);
                    int zeroBits = 8 - cByte.length();
                    System.out.println(cByte + " " + zeroBits + " " + (byte)c);
                    while (zeroBits-- != 0) {
                        System.out.println("kkk");
                        binaryTable.append("0");
                    }
                    binaryTable.append(cByte);



                   // System.out.println(cByte);

                    //System.out.println(c);
                    //String cByte = String.format("%8s", Integer.toBinaryString((byte)c & 0xFF)).replace(' ', '0');
                    //System.out.println(c + " " + cByte);

                   // binaryTable.append(cByte);
                   // System.out.println(cByte);
                }
            }
            i++;
        }

        int paddingbits = 8 - binaryTable.length() % 8;
        this.encodedTablePadding = paddingbits;
        while (paddingbits--!= 0) {
            binaryTable.append("0");
        }

        byte[] b =  constructBuffer(binaryTable, binaryTable.length());
        this.encodedTableLength = b.length;
        return b;
    }

    private void writeHeaderInfo(FileOutputStream output) throws IOException {
        int indx = 0;
        byte[] info = new byte[8 + this.lastBytes.length ];
        info[indx++] =  (byte) this.n;
        info[indx++] = (byte) this.lastBytes.length;
        for(int i = 0; i < this.lastBytes.length; i++){
            info[indx++] = this.lastBytes[i];
        }
        info[indx++] = (byte) this.paddingBits;
        byte[] length =  ByteBuffer.allocate(4).putInt(this.encodedTableLength).array();
        for (int i = 0; i < 4; i++)
            info[indx++] = length[i];
        info[indx++] = (byte) this.encodedTablePadding;
       // System.out.println(Arrays.toString(info));
        output.write(info);
    }

    public void compress(String path) throws IOException {
        HashMap<String, Integer> frequencyTable = countFrequency(path);
        Node tree =huffman(frequencyTable);
        HashMap<String, String> hashMap = new HashMap<>();
        StringBuilder encodedTree = new StringBuilder();
        constructHashTable(tree, "", hashMap, encodedTree);
        this.paddingBits = countPaddingBits(frequencyTable, hashMap);
        File file = new File(path);
        BufferedInputStream br = new BufferedInputStream(new FileInputStream(file));
        FileOutputStream output = new   FileOutputStream(file.getName()+ ".hc");
        byte[] byteTree = compressTable(encodedTree);
        writeHeaderInfo(output);
        output.write(byteTree);
        writeCompressedData(br, output, hashMap);
        br.close();
        output.close();

    }



    public static void main(String[] args) throws IOException {


        HuffmanCompression huf = new HuffmanCompression(5);
        String path = "C:\\Users\\maria\\Downloads\\file1.pdf";

        long x= System.currentTimeMillis();
        huf.compress(path);
        System.out.println(System.currentTimeMillis() - x);

        byte z = 5;
        //System.out.println(huf.convertToBinaryString(z));


        /*
        HashMap<String, Integer> hashMap = huf.countFrequency(path);

        Node root = huf.huffman(hashMap);
        HashMap<String, String> hashMap1 = new HashMap<>();
        StringBuilder builder = new StringBuilder();
        huf.constructHashTable(root, "", hashMap1);
      //  System.out.println(builder);
      //
        //  System.out.println(huf.encodingTable);

        //Node t = huf.reconstructTable(builder);
        HashMap<String, String> hh = new HashMap<>();
        StringBuilder jjj = new StringBuilder();
        //huf.constructHashTable(t, "", hh, jjj);
        System.out.println(System.currentTimeMillis()-x);

        for(Map.Entry<String, String> entry: hh.entrySet()) {
            System.out.println(entry);
        }
        for(Map.Entry<String, String> entry: hashMap1.entrySet()) {
            System.out.println(entry);
        }
        System.out.println(builder);





        int y = huf.countPaddingBits(hashMap, hashMap1);
        huf.compress(hashMap1, path,  y);
        /*

        //System.out.println(builder);

        //System.out.println("Padd" + y);

       // huf.compress(hashMap1, path, 1, y, );
        //huf.decompress("output.txt");



        for(Map.Entry<String, String> entry: hashMap1.entrySet()) {
            System.out.println(entry);
        }


        BufferedInputStream brr = new BufferedInputStream(new FileInputStream(path));
        int read;
        StringBuilder s = new StringBuilder();
        while ((read = brr.read()) != -1) {
            String b1 = String.valueOf((char)read);
            s.append(hashMap1.get(b1));


        }
        System.out.println("c" + s);
        HashMap<String, String> map = new HashMap<>();

        File file = new File("output.txt");

        long l = file.length();
        System.out.println(l + "file");

        //byte b1 = -127;

        //String s1 = String.format("%8s", Integer.toBinaryString(b1 & 0xFF)).replace(' ', '0');
     //   System.out.println((byte)Integer.parseInt("10000001", 2));



        for(Map.Entry<String, String> entry: hashMap1.entrySet()) {
            map.put(entry.getValue(), entry.getKey());
        }

        //decompress

        BufferedInputStream br = new BufferedInputStream(new FileInputStream(file));
        int r;
        StringBuilder stringBuilder = new StringBuilder();
        while ((r = br.read()) != -1) {
            byte b1 = (byte) r;
            String s1 = String.format("%8s", Integer.toBinaryString(b1 & 0xFF)).replace(' ', '0');

            stringBuilder.append(s1);
        }
        String la = "";
        if(huf.lastBytes.length != 0){
            int length = stringBuilder.length();
            la = stringBuilder.substring(length - 8*huf.lastBytes.length, length);

        }
        System.out.println(stringBuilder);
        StringBuilder key = new StringBuilder();
        String content = "";
        for(int i = 0; i < stringBuilder.length() && i <= huf.bitsNum; i++){
            key.append(stringBuilder.charAt(i));
            String e = map.get(key.toString());
            if(e != null) {
                content += e;
                System.out.println(key);
                key = new StringBuilder();
            }
        }
        System.out.println("Content: "+ content);
        //System.out.println((char) Integer.parseInt(la, 2));
















        /*




        for(Map.Entry<String, String> entry: hashMap1.entrySet()) {
            System.out.println(entry);

        }

         */








/*
        FileInputStream inputStream = null;

        inputStream = new FileInputStream(path);
        Scanner sc = new Scanner(inputStream, "UTF-8");
        while (sc.hasNextLine()) {
            String line = sc.nextLine();
             System.out.println(line);
        }

 */



        //System.out.println(System.currentTimeMillis() - x);


/*
        for(Map.Entry<String, Integer> entry: hashMap.entrySet()) {
            System.out.println(entry);

        }





        /*

        File file = new File(
                "C:\\Users\\maria\\Downloads\\DiscordSetup.exe");

        BufferedReader br = new BufferedReader(new FileReader(file));

        String st;
        char[] c = new char[3];
        int read;
        while ((read = br.read(c) ) != -1){
            String data = new String(c, 0, read);
            System.out.println(data + " " + data.length());

        }
        */









/*



        HuffmanCompression huf = new HuffmanCompression();
        byte[] arr ={'a', 'b', 'c', 'c', 'c', 'd', 'a'};
        HashMap<String, Integer> hashMap = new HashMap<>();
        for(int i = 0; i < arr.length; i += 1) {

            byte[] bytes = Arrays.copyOfRange(arr, i, i+1);

            String key = new String(bytes, StandardCharsets.UTF_8);

            System.out.println(key);

            Integer freq = hashMap.get(key);
            if(freq != null)
                hashMap.put(key, freq + 1);
            else
                hashMap.put(key, 1);

        }

        PriorityQueue<Node> priorityQueue = new PriorityQueue<>();

        for(Map.Entry<String, Integer> entry: hashMap.entrySet()) {
            LeafNode leafNode= new LeafNode();
            leafNode.data = entry.getKey();
            leafNode.freq = entry.getValue();

            System.out.println(entry);

           priorityQueue.add(leafNode);

        }
        while (priorityQueue.size() != 1){
            Node parent = new Node();
            Node left =  priorityQueue.remove();
            Node right = priorityQueue.remove();
            parent.left = left;
            parent.right = right;
            parent.freq = left.freq + right.freq;
            System.out.println(left.freq + " " + right.freq);
            priorityQueue.add(parent);
        }
        Node root = priorityQueue.remove();

        HashMap<String, String> hashMap1 = new HashMap<>();
        huf.constructHashTable(root, "", hashMap1);

        StringBuilder stringBuilder = new StringBuilder();

        for(Map.Entry<String, String> entry: hashMap1.entrySet()) {
            System.out.println(entry);

        }

        for(int i = 0; i < arr.length; i++){
            byte[] bytes = Arrays.copyOfRange(arr, i, i+1);
            String key = new String(bytes, StandardCharsets.UTF_8);
            String s = hashMap1.get(key);
            stringBuilder.append(s);

        }

        System.out.println(stringBuilder);
        byte b = Byte.parseByte(stringBuilder.substring(0, 7), 2);
        System.out.println(b);

        File fileOne=new File("fileone.txt");
        FileOutputStream fos=new FileOutputStream(fileOne);
        ObjectOutputStream oos=new ObjectOutputStream(fos);

        oos.writeObject(hashMap1);
        oos.flush();
        fos.write(arr);
        oos.close();
        fos.close();

        File toRead=new File("fileone.txt");
        FileInputStream fis=new FileInputStream(toRead);
        ObjectInputStream ois=new ObjectInputStream(fis);

        HashMap<String,String> mapInFile=(HashMap<String,String>)ois.readObject();


        for(Map.Entry<String, String> entry: mapInFile.entrySet()) {
            System.out.println(entry);

        }
        int ch;
        while ((ch=fis.read()) != -1)
            System.out.print((char)ch);


        //Node root = new Node();
        //priorityQueue.add(root);

         */




    }










}
