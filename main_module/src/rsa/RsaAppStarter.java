package rsa;

import java.io.IOException;

public class RsaAppStarter {
    public static void main(String[] args) throws IOException {
        RSA rsa = new RSA();
        // Aufgabe 1
        rsa.generateKeyPairs();

        // Aufgabe 2
        rsa.encrypt();

        // Aufgabe 3
        rsa.decrypt(false);

        // Aufgabe 4
        rsa.decrypt(true);
    }
}
