package huffman;

import java.io.IOException;

public class HuffmanAppStarter {
    public static void main(String[] args) throws IOException {
        var huffman = new Huffman();
        huffman.registerOccurrenceOfCharactersInFile();
        huffman.createTree();
        huffman.saveEncodingTable();
        huffman.encodeInputText();
    }
}
