import java.io.IOException;

public class main {
    public static void main(String[] args) throws IOException {
        RSA rsa = new RSA();
        // Aufgabe 1
        rsa.generateKeyPairs();

        // Aufgabe 2
        rsa.encrypt();
    }
}
