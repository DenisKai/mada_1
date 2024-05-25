package elgamal;

public class ElGamalAppStarter {
    public static void main(String[] args) {
        ElGamal elGamal = new ElGamal();
        elGamal.encryptText("text.txt");
        elGamal.decrypt(false);
        elGamal.decrypt(true); // Aufgabe 5
    }
}
