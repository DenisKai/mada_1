package huffman;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.PriorityQueue;

public class Huffman {

    private final String TEST_TEXT_FILE = "./main_module/src/huffman/texts/text.txt";
    private final String TEST_DECODING_TABLE_FILE = "./main_module/src/huffman/texts/dec_tab.txt";
    private final String TEST_OUTPUT_FILE = "./main_module/src/huffman/texts/output.dat";

    private String inputText = "";

    private HashMap<Integer, TreeEntry> characterMap = new HashMap<>();

    public Huffman() {
        super();
    }

    public void registerOccurrenceOfCharactersInFile() throws IOException {
        // 1.
        Path path_text = Path.of(TEST_TEXT_FILE);
        inputText = Files.readString(path_text);

        // 2.
        for (char c : inputText.toCharArray()) {
            var key = (int) c;

            var valueTree = characterMap.get(key);
            if (valueTree == null) {
                valueTree = new TreeEntry();
                valueTree.ascii = key;
                valueTree.character = c;
                characterMap.put(key, valueTree);
            }

            valueTree.occurrence += 1;
            valueTree.calculateProbability();
        }

        for (var c : characterMap.entrySet()) {
            System.out.println(c.getKey() + " " + c.getValue().occurrence);
        }
    }

    public void createTree() {
        // 3.
        var root = createHuffmanTree();
        root.writeCode(root, "0");
    }

    public void saveEncodingTable() throws IOException {
        // 4.
        BufferedWriter writer_pk = new BufferedWriter(new FileWriter(TEST_DECODING_TABLE_FILE));

        var encoding = "";
        for (var node : characterMap.entrySet()) {
            encoding += (node.getKey() + ":" + node.getValue().code + "-");
        }

        writer_pk.write(encoding.substring(0, encoding.length() - 1));
        writer_pk.close();
    }

    public void encodeInputText() throws IOException {
        // 5.
        var bitString = "";
        for (char c : inputText.toCharArray()) {
            bitString += characterMap.get((int) c).code;
        }

        // 6.
        bitString += "1";
        while (bitString.length() % 8 != 0) {
            bitString += "0";
        }

        System.out.println(bitString);

        // 7.
        var workingString = bitString;
        var length = bitString.length() / 8;
        var byteArray = new byte[length];
        for (int i = 0; i < length; i++) {
            var next = workingString.substring(0, 8);
            workingString = workingString.substring(8);
            byte x = (byte) Integer.parseInt(next, 2);
            System.out.println(next + " -> " + x);
            byteArray[i] = x;
        }

        System.out.println(byteArray);

        // 8.
        saveByteArrayToOutputFile(byteArray);
    }

    private TreeEntry createHuffmanTree() {
        var queue = new PriorityQueue<TreeEntry>();
        for (var node : characterMap.values()) {
            queue.add(node);
        }

        // starting with the smallest probability (as defined in the compareTo Method of
        // the Queue)
        while (queue.size() > 1) {
            // get right and left, remove from stack
            var left = queue.remove();
            var right = queue.remove();
            // add as merged node
            queue.add(new TreeEntry(left, right));
        }

        return queue.peek();
    }

    private void saveByteArrayToOutputFile(byte[] byteArray) throws IOException {
        FileOutputStream fos = new FileOutputStream(TEST_OUTPUT_FILE);
        fos.write(byteArray);
        fos.close();
    }

    private void readByteArrayFromOutputFile() throws IOException {
        File file = new File(TEST_OUTPUT_FILE);
        byte[] bFile = new byte[(int) file.length()];
        FileInputStream fis = new FileInputStream(file);
        fis.read(bFile);
        fis.close();
    }

    private class TreeEntry implements Comparable<TreeEntry> {
        public char character = ' ';
        public int ascii;
        public Integer occurrence = 0;
        public Float probability;
        public TreeEntry parent;
        public TreeEntry leftChild;
        public TreeEntry rightChild;
        public String code;

        public TreeEntry() {
        }

        public TreeEntry(TreeEntry leftChild, TreeEntry rightChild) {
            this.occurrence = leftChild.occurrence + rightChild.occurrence;
            this.leftChild = leftChild;
            this.rightChild = rightChild;

            leftChild.parent = this;
            rightChild.parent = this;

            calculateProbability();
        }

        public void writeCode(TreeEntry v, String code) {
            if (v != null) {
                v.code = code;
                System.out.println(v.character + " " + v.code);
                writeCode(v.leftChild, code + "0");
                writeCode(v.rightChild, code + "1");
            }
        }

        public void calculateProbability() {
            probability = (float) ((100 * occurrence / inputText.length()));
        }

        @Override
        public int compareTo(TreeEntry o) {
            return this.probability.compareTo(o.probability);
        }
    }
}
