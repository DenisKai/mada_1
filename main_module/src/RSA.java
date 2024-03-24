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

    private final String PUBLIC_KEY_FILE = "./main_module/src/keys/pk_bsp.txt";
    private final String PRIVATE_KEY_FILE = "./main_module/src/keys/sk_bsp.txt";
    private final String TEST_TEXT_FILE = "./main_module/src/texts/text.txt";
    private final String ENCRYPTED_TEXT_FILE = "./main_module/src/texts/chiffre_bsp.txt";
    private final String DECRYPTED_TEXT_FILE = "./main_module/src/texts/text_bsp-d.txt";

    private final String PRIVATE_KEY_FROM_TASK_FILE = "./main_module/src/keys/sk.txt";
    private final String CHIFFRE_FROM_TASK_FILE = "./main_module/src/texts/chiffre.txt";
    private final String CHIFFRE_FROM_TASK_DECRYPTED_TEXT_FILE = "./main_module/src/texts/text-d.txt";

    public void generateKeyPairs() throws IOException {
        // Initialise p and q randomly
        BigInteger p = BigInteger.probablePrime(RSA_BIT_LENGTH, new SecureRandom());
        BigInteger q = BigInteger.probablePrime(RSA_BIT_LENGTH, new SecureRandom());
        while (p == q) {
            // making sure p and q are unequal
            q = q.nextProbablePrime();
        }

        // first part of both keys is n (p * q)
        BigInteger n = p.multiply(q);

        // phi(n) = (p - 1) * (q - 1)
        BigInteger phi_n = (p.subtract(BigInteger.ONE)).multiply(q.subtract(BigInteger.ONE));

        BigInteger[] e_d = generateEAndD(phi_n);
        BigInteger e = e_d[0];
        BigInteger d = e_d[1];

        BufferedWriter writer_pk = new BufferedWriter(new FileWriter(PUBLIC_KEY_FILE));
        writer_pk.write("(" + n + "," + e + ")");
        writer_pk.close();
        BufferedWriter writer_sk = new BufferedWriter(new FileWriter(PRIVATE_KEY_FILE));
        writer_sk.write("(" + n + "," + d + ")");
        writer_sk.close();
    }

    public String encrypt() throws IOException {
        // a
        String puk = Files.readString(Path.of(PUBLIC_KEY_FILE));
        BigInteger n = new BigInteger((puk.split(",")[0]).replace("(", ""));
        BigInteger e = new BigInteger((puk.split(",")[1]).replace(")", ""));

        // b
        Path path_text = Path.of(TEST_TEXT_FILE);
        String text = Files.readString(path_text);
        BigInteger[] encrypted_chars = new BigInteger[text.length()];
        for (int i = 0; i < text.length(); i++) {
            char x = text.charAt(i);
            // b; ASCII is 0-127 = 128 => a byte with a most significant bit (Danke algd1 &
            // Filip Schramka)
            var xAsBigInteger = BigInteger.valueOf((byte) x);

            // b + c
            encrypted_chars[i] = fastExponationWithBinaryDepiction(xAsBigInteger, e, n);
        }

        // d
        StringBuilder sb = new StringBuilder();
        sb.append(encrypted_chars[0]);
        for (int i = 1; i < encrypted_chars.length; i++) {
            sb.append(",").append(encrypted_chars[i]);
        }

        var encryptedText = sb.toString();

        BufferedWriter writer_encrypted = new BufferedWriter(new FileWriter(ENCRYPTED_TEXT_FILE));
        writer_encrypted.write(encryptedText);
        writer_encrypted.close();

        return encryptedText;
    }

    // Aufgabe 3
    public String decrypt(boolean decryptMadaTask) throws IOException {
        Path path_sk = Path.of(PRIVATE_KEY_FILE);
        if (decryptMadaTask) {
            path_sk = Path.of(PRIVATE_KEY_FROM_TASK_FILE);
        }

        String puk = Files.readString(path_sk);
        BigInteger n = new BigInteger((puk.split(",")[0]).replace("(", ""));
        BigInteger d = new BigInteger((puk.split(",")[1]).replace(")", ""));

        Path path_e_text = Path.of(ENCRYPTED_TEXT_FILE);
        if (decryptMadaTask) {
            path_e_text = Path.of(CHIFFRE_FROM_TASK_FILE);
        }

        String[] text_array = Files.readString(path_e_text).split(",");

        Integer[] decrypted_chars = new Integer[text_array.length];
        for (int i = 0; i < text_array.length; i++) {
            BigInteger y = new BigInteger(text_array[i]); // consists of y = (x ^ e) mod n
            var decryptedBigInt = fastExponationWithBinaryDepiction(y, d, n); // x = (y ^ d) % n;

            decrypted_chars[i] = decryptedBigInt.intValue(); // if we trust below 128
            System.out.println(Character.toString(decrypted_chars[i]));
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < decrypted_chars.length; i++) {
            sb.append(Character.toString(decrypted_chars[i]));
        }

        var decryptedText = sb.toString();
        var fileName = decryptMadaTask ? CHIFFRE_FROM_TASK_DECRYPTED_TEXT_FILE : DECRYPTED_TEXT_FILE;
        BufferedWriter writer_decrypted = new BufferedWriter(new FileWriter(fileName));
        writer_decrypted.write(decryptedText);
        writer_decrypted.close();

        return decryptedText;
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
                q = q_r[0]; // quotient
                r = q_r[1]; // remainder
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

            // if y0 is negative, need to make sure it's not by adding phi_n
            while (d.compareTo(BigInteger.ZERO) < 0) {
                d = d.add(phi_n);
            }
        }
        return new BigInteger[] { e, d };
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
    private BigInteger fastExponationWithBinaryDepiction(BigInteger basis, BigInteger exponent, BigInteger n) {
        var exponentAsBinary = exponent.toString(2); // big int allows binary depiction with this method

        // "Wir gehen von hinten nach vorne über die Binärdarstellung, quadrieren
        // jedesmal und multiplizieren, falls das Bit 1 ist."
        int i = exponentAsBinary.length() - 1; // position of least significant bit

        // initializing variables
        BigInteger h = BigInteger.ONE;
        BigInteger k = basis;

        while (i >= 0) {
            if (exponentAsBinary.charAt(i) == '1') {
                // if bit is set, extend h (result)
                h = h.multiply(k).mod(n);
            }

            k = k.multiply(k).mod(n);
            i--;
        }

        System.out.println(String.format("%d ^ %d mod %d = %d", basis, exponent, n, h));

        return h;
    }
}