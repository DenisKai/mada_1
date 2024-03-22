import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.Random;

public class RSA {
    private final int RSA_BIT_LENGTH = 2048;

    public void encrypt() throws IOException {
        // a
        Path path_pk = Path.of("./main_module/src/keys/pk.txt");
        String puk = Files.readString(path_pk);
        BigInteger n = new BigInteger((puk.split(",")[0]).replace("(", ""));
        BigInteger e = new BigInteger((puk.split(",")[1]).replace(")", ""));

        // b
        Path path_text = Path.of("./main_module/src/texts/text.txt");
        String text = Files.readString(path_text);
        BigInteger[] encrypted_chars = new BigInteger[text.length()];
        for (int i = 0; i < text.length(); i++) {
            char x = text.charAt(i);
            // b + c in fastExponation() implemented
            encrypted_chars[i] = fastExponation(x, n);
        }

        // d
        StringBuilder sb = new StringBuilder();
        sb.append(encrypted_chars[0]);
        for (int i = 1; i < encrypted_chars.length; i++) {
            sb.append(",").append(encrypted_chars[i]);
        }

        BufferedWriter writer_encrypted = new BufferedWriter(new FileWriter("main_module/src/texts/chiffre.txt"));
        writer_encrypted.write(sb.toString());
        writer_encrypted.close();
    }

    // Aufgabe 3
    public void decrypt() throws IOException {
        Path path_sk = Path.of("./main_module/src/keys/sk.txt");
        String puk = Files.readString(path_sk);
        BigInteger n = new BigInteger((puk.split(",")[0]).replace("(", ""));
        BigInteger d = new BigInteger((puk.split(",")[1]).replace(")", ""));

        Path path_e_text = Path.of("./main_module/src/texts/chiffre.txt");
        String[] text_array = Files.readString(path_e_text).split(",");
        for (int i = 0; i < text_array.length; i++) {
            BigInteger y = new BigInteger(text_array[i]);
            // TODO exponantion hie?

        }
    }

    public void generateKeyPairs() throws IOException {
        // Initialise p and q randomly
        BigInteger p = BigInteger.probablePrime(RSA_BIT_LENGTH, new SecureRandom());
        BigInteger q = BigInteger.probablePrime(RSA_BIT_LENGTH, new SecureRandom());

        // first part of both keys is n
        BigInteger n = p.multiply(q);

        BigInteger phi_n = (p.subtract(BigInteger.ONE)).multiply(q.subtract(BigInteger.ONE));

        BigInteger[] e_d = generateEAndD(phi_n);
        BigInteger e = e_d[0];
        BigInteger d = e_d[1];

        BufferedWriter writer_pk = new BufferedWriter(new FileWriter("main_module/src/keys/pk.txt"));
        writer_pk.write("(" + n + "," + e + ")");
        writer_pk.close();
        BufferedWriter writer_sk = new BufferedWriter(new FileWriter("main_module/src/keys/sk.txt"));
        writer_sk.write("(" + n + "," + d + ")");
        writer_sk.close();
    }

    // Implementation of extended euclidean algorithm
    // Returns an array of bigintegers (Size 2), first value being e, second d.
    private BigInteger[] generateEAndD(BigInteger phi_n) {
        BigInteger e = BigInteger.ZERO;
        BigInteger d = BigInteger.ZERO;
        BigInteger ggT_check = BigInteger.ZERO;

        // First loop is for choosing random e's until one is 'Teilerfremd' with phi_n
        // In other terms: ggT(phi_n,e) = 1
        while (!ggT_check.equals(BigInteger.ONE)) {
            // implementation of extended euclidean algorithm
            // Semantic difference to formula: a' is a, b' is b
            e = randomBigInteger(phi_n);
            BigInteger a = phi_n;
            BigInteger b = e;
            BigInteger x0 = BigInteger.ONE, y1 = BigInteger.ONE;
            BigInteger x1 = BigInteger.ZERO, y0 = BigInteger.ZERO;
            BigInteger q, r;

            // Keep algo running until b' == 0
            while (!b.equals(BigInteger.ZERO)) {
                BigInteger[] q_r = a.divideAndRemainder(b);
                q = q_r[0];
                r = q_r[1];
                a = b;
                b = r;

                // x0 and y0 have to be stored temporarily / copied, for calculating x1 and y1
                BigInteger x0_temp = x0;
                BigInteger y0_temp = y0;
                x0 = x1;
                y0 = y1;
                x1 = x0_temp.subtract(q.multiply(x1));
                y1 = y0_temp.subtract(q.multiply(y1));
            }


            ggT_check = a;
            d = y0;
        }
        return new BigInteger[]{e, d};
    }

    // Extended euclidean algorithm (eeA) needs a random e in range 0 to phi_n
    // Vorbedingung eeA: a,b Element Z, a >= b >= 0.
    // In diesem Fall: a = phi_n.
    private BigInteger randomBigInteger(BigInteger phi_n) {
        Random rnd = new SecureRandom();
        BigInteger e = new BigInteger(RSA_BIT_LENGTH, rnd);
        // As long e > phi_n or e < 0, generate a new one
        while (e.compareTo(phi_n) > 0 || e.compareTo(BigInteger.ZERO) < 0) {
            e = new BigInteger(RSA_BIT_LENGTH, rnd);
        }

        return e;
    }

    // c
    private BigInteger fastExponation(char x, BigInteger n) {
        int i = 0; // length e

        // Iterate over byte
        for (int j = 7; j >= 0; j--) {
            if (((x >> j) & 0b0000_0001) == 1) {
                // bitwise XOR does the trick
                i += (2 ^ j);
            }
        }
        i++; // l is offset by -1

        BigInteger h = BigInteger.ONE;
        BigInteger k = BigInteger.valueOf((byte) x); // b; ASCII is 0-127 = 128 => a byte with a most significant bit (Danke algd1 & Filip Schramka)

        while (i >= 0) {
            if (((x >> i) & 1) == 1) {
                h = h.multiply(k).mod(n);
            }

            k = k.multiply(k).mod(n);
            i--;
        }

        return h;
    }

    private char fastExponation(BigInteger y, BigInteger d, BigInteger n) {
        int i = 0;
        int h = 1;

        return 'a';
    }
}