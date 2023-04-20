import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;


public class RSA extends JFrame {


    private JTextField pTextField;
    private JTextField qTextField;
    private JTextArea xTextArea;
    private JTextArea resultTextArea;

    private int p, q,n, phiN, e;


    public RSA() {
        setTitle("RSA");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());


        JPanel inputPanel = new JPanel(new GridLayout(4, 3));
        inputPanel.setPreferredSize(new Dimension(300, 400));

        pTextField = new JTextField();
        qTextField = new JTextField();
        xTextArea = new JTextArea();
        JButton encryptButton = new JButton("Encrypt");
        JButton decryptButton = new JButton("Decrypt");

        encryptButton.setPreferredSize(new Dimension(150, 150));
        decryptButton.setPreferredSize(new Dimension(150, 150));

        encryptButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                encrypt();
            }
        });
        decryptButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                decrypt();
            }
        });

        resultTextArea = new JTextArea();
        resultTextArea.setEditable(false);


        inputPanel.add(new JLabel("p:"));
        inputPanel.add(pTextField);
        inputPanel.add(new JLabel("q:"));
        inputPanel.add(qTextField);
        inputPanel.add(new JLabel("User message:"));
        inputPanel.add(xTextArea);
        inputPanel.add(encryptButton);
        inputPanel.add(decryptButton);

        add(inputPanel, BorderLayout.NORTH);
        add(new JScrollPane(resultTextArea), BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
    }

    private void encrypt() {
        try {
            p = Integer.parseInt(pTextField.getText());
            q = Integer.parseInt(qTextField.getText());
            String x = xTextArea.getText();

            n = p * q;
            phiN = (p - 1) * (q - 1);
            e = findCoprime(phiN);

            byte[] xBytes = x.getBytes(StandardCharsets.UTF_8);
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


    private int modExp(int x, int e, int n) {
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
            while ((line = reader.readLine()) != null) {
                String[] splitLine = line.split(": ");
                if (splitLine.length < 2) {
                    throw new IOException("Invalid input format");
                }
                String key = splitLine[0];
                String value = splitLine[1];
                if (key.equals("message")) {
                    String y = value;
                    int publicKeyN = -1;
                    int publicKeyE = -1;
                    if ((line = reader.readLine()) != null) {
                        splitLine = line.split(": ");
                        if (splitLine.length < 2) {
                            throw new IOException("Invalid input format");
                        }
                        key = splitLine[0];
                        value = splitLine[1];
                        if (key.equals("public key")) {
                            String[] publicKeyValues = value.split(" ");
                            if (publicKeyValues.length != 2) {
                                throw new IOException("Invalid public key format");
                            }
                            publicKeyN = Integer.parseInt(publicKeyValues[0]);
                            publicKeyE = Integer.parseInt(publicKeyValues[1]);
                        }
                    }

                    if (publicKeyN == -1 || publicKeyE == -1) {
                        throw new IOException("Missing public key");
                    }

                    // factorize n to find p and q
                    int p = -1;
                    int q = -1;
                    for (int i = 2; i < publicKeyN; i++) {
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
                    int d = findModularInverse(publicKeyE, phiN);

                    byte[] decryptedBytes = new byte[y.length() / 2];

                    for (int i = 0; i < y.length(); i += 2) {
                        String encryptedValueStr = y.substring(i, i + 2);
                        int encryptedValue = Integer.parseInt(encryptedValueStr, 16);
                        int decryptedValue = modExp(encryptedValue, d, publicKeyN);
                        decryptedBytes[i / 2] = (byte) decryptedValue;
                    }

                    String x = new String(decryptedBytes, StandardCharsets.UTF_8);

                    BufferedWriter writer = new BufferedWriter(new FileWriter("decrypted.txt"));
                    writer.write("message: " + x + "\n" + "private key: " + publicKeyN + " " + d);
                    writer.close();

                    resultTextArea.append("Decrypted text: " + x + "\n");
                    resultTextArea.append("Private key ( n, d): (" + n + ", " + d + ")\n");
                }
            }
            reader.close();
        } catch (NumberFormatException ex) {
            resultTextArea.append("Error: Incorrect input data\n");
        } catch (IOException ex) {
            resultTextArea.append("Error: " + ex.getMessage() + "\n");
        }
    }




    private int findModularInverse(int a, int m) {
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

    private int findCoprime(int n) {

        int e = 1;
        BigInteger bigN = BigInteger.valueOf(n);
        while (BigInteger.valueOf(e).gcd(bigN).intValue() != 1) {
            e++;
        }
        return e;
    }

    public static void main(String[] args) {
        RSA rsa = new RSA();
        rsa.setVisible(true);
    }


}