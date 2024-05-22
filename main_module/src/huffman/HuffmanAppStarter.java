package huffman;

import java.io.IOException;

public class HuffmanAppStarter {
    public static void main(String[] args) throws IOException {
        // tests for encoding and decoding
        Huffman.encode("text.txt", "test");
        Huffman.decode(
                Huffman.TEST_OUTPUT_FILE_NAME,
                Huffman.TEST_ENCODING_TABLE_FILE_NAME,
                "test");

        // decode task in 11.
        Huffman.decode(
                "/output-mada.dat",
                "/dec_tab-mada.txt",
                "task");

        // TODO! 10. Testen Sie Ihr Programm an ein paar Textdateien und geben Sie an,
        // wie viel Platz gespart wird.
    }
}
