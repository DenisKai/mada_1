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

    private final String FOLDER_PATH = "./main_module/src/huffman/texts/";
    public static final String TEST_ENCODING_TABLE_FILE_NAME = "/dec_tab.txt";
    public static final String TEST_OUTPUT_FILE_NAME = "/output.dat";
    private final String TEST_DECOMPRESS_FILE_NAME = "/decompress.txt";

    private final String ENCODING_CHAR_SET_SEPARATOR = "-";
    private final String ENCODING_CHAR_SEPARATOR = ":";

    private String inputText = "";
    private HashMap<Integer, TreeEntry> characterMap = new HashMap<>();
    private HashMap<String, TreeEntry> codeMap = new HashMap<>();

    public Huffman() {
        super();
    }

    public static Huffman encode(String inputFileName, String outputFolderName) throws IOException {
        var huffman = new Huffman();
        huffman.readInputFile(inputFileName);
        huffman.createEncodingStrategy(outputFolderName);
        huffman.encodeInputText(outputFolderName);

        return huffman;
    }

    public static Huffman decode(String encodedDatFileName, String encodingTableFileName, String outputFolderName)
            throws IOException {
        var huffman = new Huffman();
        huffman.recreateDecodingStrategy(outputFolderName, encodingTableFileName);
        var byteArray = huffman.readByteArrayFromOutputFile(encodedDatFileName, outputFolderName);
        huffman.decodeInputText(byteArray, outputFolderName);

        return huffman;
    }

    private void readInputFile(String inputFileName) throws IOException {
        // 1. read ascii input file
        Path path_text = Path.of(FOLDER_PATH + inputFileName);
        inputText = Files.readString(path_text);

        registerOccurrenceOfCharactersInFile();
    }

    private void createEncodingStrategy(String outputFolderName) throws IOException {
        // 3. create huffman encoding tree
        var root = createHuffmanTree();
        // write codes according to tree
        root.writeCode(root, "0");

        // 4. save encoding tree to file
        var encodingFile = getFilePath(outputFolderName, TEST_ENCODING_TABLE_FILE_NAME);
        BufferedWriter writer_pk = new BufferedWriter(new FileWriter(encodingFile));

        var encodingStringBuilder = new StringBuilder();
        for (var node : characterMap.entrySet()) {
            encodingStringBuilder.append(
                    node.getKey()
                            + ENCODING_CHAR_SEPARATOR
                            + node.getValue().code
                            + ENCODING_CHAR_SET_SEPARATOR);
        }

        writer_pk.write(encodingStringBuilder.substring(0, encodingStringBuilder.length() - 1));
        writer_pk.close();
    }

    private void encodeInputText(String outputFolderName) throws IOException {
        // 5. convert input file to bit string according to huffman
        var bitString = "";
        for (char c : inputText.toCharArray()) {
            bitString += characterMap.get((int) c).code;
        }

        // 6. expand bit string according to instructions
        bitString += "1";
        while (bitString.length() % 8 != 0) {
            bitString += "0";
        }

        System.out.println("encoded bit string:");
        System.out.println(bitString);

        // 7. create byte array
        var workingString = bitString;
        var length = bitString.length() / 8;
        var byteArray = new byte[length];
        for (int i = 0; i < length; i++) {
            var next = workingString.substring(0, 8);
            workingString = workingString.substring(8);
            byte b = (byte) Integer.parseInt(next, 2);
            System.out.println(next + " -> " + Byte.toUnsignedInt(b));
            byteArray[i] = b;
        }

        // 8. save byte array in .dat file
        saveByteArrayToOutputFile(byteArray, outputFolderName);
    }

    private void registerOccurrenceOfCharactersInFile() {
        // 2. register characters in text with occurrence and meta data
        for (char c : inputText.toCharArray()) {
            var key = (int) c;

            var entry = characterMap.get(key);
            if (entry == null) {
                entry = new TreeEntry(c, key);
                characterMap.put(key, entry);
            }

            entry.occurrence += 1;
            entry.calculateProbability();
        }
    }

    private TreeEntry createHuffmanTree() {
        var queue = new PriorityQueue<TreeEntry>();
        for (var node : characterMap.values()) {
            queue.add(node);
        }

        // starting with the smallest probability (as defined in the compareTo-Method
        // required by the Queue)
        while (queue.size() > 1) {
            // get right and left, remove from stack to then merge into one
            var left = queue.remove();
            var right = queue.remove();
            // add as merged node
            queue.add(new TreeEntry(left, right));
        }

        // return last remaining element (root)
        return queue.peek();
    }

    private void saveByteArrayToOutputFile(byte[] byteArray, String outputFolderName) throws IOException {
        // code according to hint from 8.
        var byteArrayFile = getFilePath(outputFolderName, TEST_OUTPUT_FILE_NAME);
        FileOutputStream fos = new FileOutputStream(byteArrayFile);
        fos.write(byteArray);
        fos.close();
    }

    private byte[] readByteArrayFromOutputFile(String outputFileName, String outputFolderName) throws IOException {
        // code according to hint from 8.
        File file = new File(getFilePath(outputFolderName, outputFileName));
        byte[] bFile = new byte[(int) file.length()];
        FileInputStream fis = new FileInputStream(file);
        fis.read(bFile);
        fis.close();

        return bFile;
    }

    private void recreateDecodingStrategy(String outputFolderName, String encodingTableFileName) throws IOException {
        // read huffman encoding table from file
        Path path_text = Path.of(getFilePath(outputFolderName, encodingTableFileName));
        var encodingText = Files.readString(path_text);
        var characterSets = encodingText.split(ENCODING_CHAR_SET_SEPARATOR);

        // create map by code with character from ascii value
        for (String characterSet : characterSets) {
            var chars = characterSet.split(ENCODING_CHAR_SEPARATOR);
            var entry = new TreeEntry(chars[0], chars[1]);
            codeMap.put(entry.code, entry);
            entry.printEncoding();
        }
    }

    private void decodeInputText(byte[] byteArray, String outputFolderName) throws IOException {
        var bitStringBuilder = new StringBuilder();
        for (byte b : byteArray) {
            var i = Integer.toBinaryString(Byte.toUnsignedInt(b));
            var s = String.format("%8s", i).replace(' ', '0');
            bitStringBuilder.append(s);
        }

        // find index of manually added 1000... to remove those again
        var indexToStrip = bitStringBuilder.lastIndexOf("1");
        var strippedBitString = bitStringBuilder.substring(0, indexToStrip);

        System.out.println("to decode bit string:");
        System.out.println(strippedBitString);

        // decode string bit by bit
        var decodingStringBuilder = new StringBuilder();

        var workingString = strippedBitString;
        var lastFoundIndex = 0;
        for (int i = 0; i <= workingString.length(); i++) {
            var next = workingString.substring(lastFoundIndex, i);
            if (codeMap.containsKey(next)) {
                var entry = codeMap.get(next);
                decodingStringBuilder.append(entry.character);
                System.out.println(entry.code + " -> " + entry.character);
                lastFoundIndex = i;
            }
        }

        var decodedString = decodingStringBuilder.toString();
        System.out.println(decodedString);

        // write decoded / decompressed result
        var decodedFile = getFilePath(outputFolderName, TEST_DECOMPRESS_FILE_NAME);
        BufferedWriter writer_pk = new BufferedWriter(new FileWriter(decodedFile));

        writer_pk.write(decodedString);
        writer_pk.close();
    }

    private String getFilePath(String outputFolderName, String fileName) {
        return FOLDER_PATH + "/" + outputFolderName + fileName;
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

        public TreeEntry(char character, int ascii) {
            this.character = character;
            this.ascii = ascii;
        }

        public TreeEntry(TreeEntry leftChild, TreeEntry rightChild) {
            this.occurrence = leftChild.occurrence + rightChild.occurrence;
            this.leftChild = leftChild;
            this.rightChild = rightChild;

            leftChild.parent = this;
            rightChild.parent = this;

            calculateProbability();
        }

        public TreeEntry(String ascii, String code) {
            this.ascii = Integer.parseInt(ascii);
            this.code = code;
            this.character = (char) this.ascii;
        }

        public void writeCode(TreeEntry v, String code) {
            if (v != null) {
                v.code = code;
                v.printEncoding();
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

        public void printEncoding() {
            System.out.println(this.character + " " + this.code);
            ;
        }
    }
}
