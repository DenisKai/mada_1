package huffman;

import java.io.IOException;

public class HuffmanAppStarter {
    public static void main(String[] args) throws IOException {
        // tests for encoding and decoding
        Huffman.encode("/text.txt", "test");
        Huffman.decode(
                Huffman.TEST_OUTPUT_FILE_NAME,
                Huffman.TEST_ENCODING_TABLE_FILE_NAME,
                "test");

        // decode task in 11.
        Huffman.decode(
                "/output-mada.dat",
                "/dec_tab-mada.txt",
                "task");

        // 10. test the Programm with a few texts and report how much space is saved.
        evaluateSpaceSavingWithHuffman();
    }

    public static void evaluateSpaceSavingWithHuffman() throws IOException {
        var folderName = "size-test";
        for (int i = 0; i < 5; i++) {
            var huffman = Huffman.encode("/" + i + ".txt", folderName);
            huffman.addEncodingSizeDifferenceToFile(i, "size-difference.txt");
        }
    }
}
