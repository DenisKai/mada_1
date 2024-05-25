package elgamal;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.concurrent.ThreadLocalRandom;

// secret Key: (G, g, b)
// public Key: (G, g, g^b)
// G = Gruppe, g = Erzeuger, b Element von Z*_n
public class ElGamal {

    private final String FOLDER_PATH = "./main_module/src/elgamal/";
    private final String HEX_NUMBER_FILE = "texts/hex-number.txt";
    private final String ENCRYPTED_TEXT = "texts/chiffre.txt";
    private final String DECRYPTED_TEXT = "texts/text-d.txt";
    private final String PUBLIC_KEY = "keys/pk.txt";
    private final String SECRET_KEY = "keys/sk.txt";

    private BigInteger n = new BigInteger("-1");
    private final BigInteger g = new BigInteger("2"); // aus Aufgabenstellung

    public ElGamal() {
        getNFromHexFile();
        generateKeys();
    }

    // 1: Hex to BigInteger
    private void getNFromHexFile() {
        try {
            Path p = Path.of(FOLDER_PATH + HEX_NUMBER_FILE);
            String hexString = new String(Files.readAllBytes(p));

            hexString = hexString.replace("\s", "")
                    .replace("\r", "")
                    .replace("\n", "");
            this.n = new BigInteger(hexString, 16);
            System.out.println("n is: " + n);
        } catch (IOException e) {
            System.out.println("Couldn't read hex-number file.");
            e.printStackTrace();
        }
    }

    // 2: Generate Private and Public key
    private void generateKeys() {
        SecureRandom rnd = new SecureRandom();
        BigInteger b;

        // 2.1 get random b from Z*_n
        do {
            b = new BigInteger(n.bitLength(), rnd);
        } while (b.compareTo(n) >= 0 || b.compareTo(BigInteger.ZERO) <= 0);

        generateSecretKey(b);
        generatePublicKey(b);
    }

    // 2.2 save secret key (G, g, b) (in this case just b) to file
    private void generateSecretKey(BigInteger b) {
        try (PrintWriter pw = new PrintWriter(FOLDER_PATH + SECRET_KEY)) {
            pw.print(b.toString());
            pw.flush();
        } catch (IOException e) {
            System.out.println("Couldn't write secret key.");
            e.printStackTrace();
        }
    }

    // 2.3 save public key (G, g, g^b) (in this case just g^b) to file
    private void generatePublicKey(BigInteger b) {
        // mod n needs to be put at the end
        BigInteger g_b = g.modPow(b, n);

        try (PrintWriter pw = new PrintWriter(FOLDER_PATH + PUBLIC_KEY)) {
            pw.print(g_b);
            pw.flush();
        } catch (IOException e) {
            System.out.println("Couldn't write public key.");
            e.printStackTrace();
        }
    }

    // 3: Encrypt
    // provide file in texts folder
    public void encryptText(String file) {
        // a read public key
        BigInteger g_b = BigInteger.ZERO;
        try {
            Path p = Path.of(FOLDER_PATH + PUBLIC_KEY);
            g_b = new BigInteger(Files.readString(p));
        } catch (IOException e) {
            System.out.println("Couldn't read public key file.");
            e.printStackTrace();
        }

        // b convert each character to ascii
        byte[] ascii_text = new byte[0];
        try {
            Path p = Path.of(FOLDER_PATH + "texts/" + file);
            ascii_text = new String(Files.readAllBytes(p)).getBytes();
        } catch (IOException e) {
            System.out.println("Couldn't read text file.");
            e.printStackTrace();
        }

        // encryption with (g^a, (g^b)^a * x)
        // a is chosen randomly
        BigInteger a = BigInteger.valueOf(ThreadLocalRandom.current().nextInt());
        BigInteger y_1 = g.modPow(a, n);
        BigInteger g_b_a = g_b.modPow(a, n);

        //flush existing chiffre
        try {
            FileWriter fileWriter = new FileWriter(FOLDER_PATH + ENCRYPTED_TEXT);
            fileWriter.write("");
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // encrypt each character
        for (int i = 0; i < ascii_text.length; i++) {
            try {
                BigInteger y_2 = g_b_a.multiply(new BigInteger(String.valueOf(ascii_text[i]))).mod(n); // g^b^a * x

                BufferedWriter writer = new BufferedWriter(new FileWriter(FOLDER_PATH + ENCRYPTED_TEXT, true));
                writer.append("(" + y_1 + "," + y_2 + ");");
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 4: Decrypt chiffre text
    public void decrypt(boolean example) {
        // get private key
        BigInteger b = BigInteger.ZERO;
        try {
            Path p = Path.of(FOLDER_PATH + SECRET_KEY);
            if (example) {
                p = Path.of(FOLDER_PATH + "keys/sk_bsp.txt");
            }
            b = new BigInteger(Files.readString(p));
        } catch (IOException e) {
            System.out.println("Couldn't read secret key file.");
            e.printStackTrace();
        }

        // read chiffre
        String chiffre_text = "";
        try {
            Path p_chiffre = Path.of(FOLDER_PATH + ENCRYPTED_TEXT);
            if (example) {
                p_chiffre = Path.of(FOLDER_PATH + "texts/chiffre_bsp.txt");
            }
            chiffre_text = Files.readString(p_chiffre);
        } catch (IOException e) {
            System.out.println("Couldn't read chiffre file.");
            e.printStackTrace();
        }

        String[] chiffre_characters = chiffre_text.split(";");
        StringBuilder decrypted_text = new StringBuilder();
        for (int i = 0; i < chiffre_characters.length; i++) {
            String[] values = chiffre_characters[i].replace("(", "")
                    .replace(")", "")
                    .split(",");

            BigInteger g_a = new BigInteger(values[0]);
            BigInteger x = new BigInteger(values[1]);

            // x * ((g^a)^b)^-1
            BigInteger decrypted_char = x.multiply(g_a.modPow(b.negate(), n)).mod(n);
            decrypted_text.append((char) decrypted_char.intValue());
        }

        System.out.println(decrypted_text);
        // write decrypted text into file
        try {
            Path decrypt_file = Path.of(FOLDER_PATH + DECRYPTED_TEXT);
            if (example) {
                decrypt_file = Path.of(FOLDER_PATH + "texts/" + "text-d_bsp.txt");
            }

            Files.write(decrypt_file, decrypted_text.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
