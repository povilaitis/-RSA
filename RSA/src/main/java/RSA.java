import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public class RSA extends JFrame implements ActionListener {
    private JTextField pTextField;
    private JTextField qTextField;
    private JTextArea xTextArea;
    private JTextArea resultTextArea;
    private JButton encryptButton;
    private JButton decryptButton;

    private int p;
    private int q;
    private int n;
    private int phiN;
    private int e;


    public RSA() {
        setTitle("RSA Encryption System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        pTextField = new JTextField();
        qTextField = new JTextField();
        xTextArea = new JTextArea();
        encryptButton = new JButton("Encrypt");
        decryptButton = new JButton("Decrypt");
        resultTextArea = new JTextArea();
        resultTextArea.setEditable(false);


        JPanel inputPanel = new JPanel(new GridLayout(4, 2));
        inputPanel.add(new JLabel("p:"));
        inputPanel.add(pTextField);
        inputPanel.add(new JLabel("q:"));
        inputPanel.add(qTextField);
        inputPanel.add(new JLabel("Text x:"));
        inputPanel.add(xTextArea);
        inputPanel.add(encryptButton);
        inputPanel.add(decryptButton);

        add(inputPanel, BorderLayout.NORTH);
        add(new JScrollPane(resultTextArea), BorderLayout.CENTER);

        encryptButton.addActionListener(this);
        decryptButton.addActionListener(this);

        pack();
        setLocationRelativeTo(null);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == encryptButton) {
            encrypt();
        } else if (e.getSource() == decryptButton) {
            decrypt();
        }
    }


    private void encrypt() {
        try {
            p = Integer.parseInt(pTextField.getText());
            q = Integer.parseInt(qTextField.getText());
            String x = xTextArea.getText();

            n = p * q;
            phiN = (p - 1) * (q - 1);
            e = findCoprime(phiN);

            byte[] xBytes = x.getBytes(StandardCharsets.US_ASCII);
            String y = "";

            for (int i = 0; i < xBytes.length; i++) {
                int byteValue = xBytes[i] & 0xFF;
                String encryptedValue = Integer.toHexString(modExp(byteValue, e, n));
                y += encryptedValue;
            }

            BufferedWriter writer = new BufferedWriter(new FileWriter("encrypted.txt"));
            writer.write("message: " + y + "\n" + "public key: " + n + " " + e);
            writer.close();

            resultTextArea.append("Encrypted text: " + y + "\n");
            resultTextArea.append("Public key (n, e): (" + n + ", " + e + ")\n");
        } catch (NumberFormatException | IOException ex) {
            resultTextArea.append("Error: Incorrect input data\n");
        }
    }

    public static int modExp(int x, int e, int n) {
        int result = 1;
        while (e > 0) {
            if (e % 2 == 1) {
                result = (result * x) % n;
            }
            x = (x * x) % n;
            e = e / 2;
        }
        return result;
    }

    private void decrypt() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader("encrypted.txt"));
            String line;
            String y = null;
            int publicKeyN = -1;
            int publicKeyE = -1;

            while ((line = reader.readLine()) != null) {
                String[] splitLine = line.split(": ");
                if (splitLine.length < 2) {
                    throw new IOException("Invalid input format");
                }
                String key = splitLine[0];
                String value = splitLine[1];
                if (key.equals("message")) {
                    y = value;
                } else if (key.equals("public key")) {
                    String[] publicKeyValues = value.split(" ");
                    if (publicKeyValues.length != 2) {
                        throw new IOException("Invalid public key format");
                    }
                    publicKeyN = Integer.parseInt(publicKeyValues[0]);
                    publicKeyE = Integer.parseInt(publicKeyValues[1]);
                }
            }

            if (y == null || publicKeyN == -1 || publicKeyE == -1) {
                throw new IOException("Missing message or public key");
            }

            int p = -1;
            int q = -1;
            for (int i = 2; i <= Math.sqrt(publicKeyN); i++) {
                if (publicKeyN % i == 0) {
                    p = i;
                    q = publicKeyN / i;
                    break;
                }
            }
            if (p == -1 || q == -1) {
                throw new IOException("Unable to factorize n");
            }

            int phiN = (p - 1) * (q - 1);
            int d = modularInverse(publicKeyE, phiN);

            if (y.length() % 2 != 0) {
                throw new IOException("Invalid message length");
            }

            byte[] decryptedBytes = new byte[y.length() / 2];
            for (int i = 0; i < decryptedBytes.length; i++) {
                String hexByte = y.substring(i * 2, (i + 1) * 2);
                int encryptedByte = Integer.parseInt(hexByte, 16);
                int decryptedByte = modExp(encryptedByte, d, publicKeyN);
                decryptedBytes[i] = (byte) decryptedByte;
            }

            String x = new String(decryptedBytes, StandardCharsets.US_ASCII);
            resultTextArea.append("Decrypted text: " + x + "\n");

            BufferedWriter writer = new BufferedWriter(new FileWriter("decrypted.txt"));
            writer.write("message: " + x);
            writer.close();

        } catch (IOException ex) {
            resultTextArea.append("Error: " + ex.getMessage() + "\n");
        }
    }




    private int modularInverse(int a, int m) {
        int m0 = m;
        int y = 0, x = 1;
        if (m <= 1) return 0;

        while (a > 1) {
            int q = a / m;
            int t = m;
            m = a % m;
            a = t;
            t = y;
            y = x - q * y;
            x = t;
        }
        if (x < 0) x += m0;
        return x;
    }

    private int findCoprime(int phiN) {
        int e;
        do {
            e = (int) (Math.random() * (phiN - 3) + 3);  //e iesko
        } while (gcd(e, phiN) != 1);
        return e;
    }

    private int gcd(int a, int b) {
        if (b == 0) {
            return a;
        } else {
            return gcd(b, a % b);
        }
    }


    public static void main(String[] args) {
        RSA rsa = new RSA();
        rsa.setVisible(true);
    }


}
