
import java.awt.GridLayout;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

public class Login {

    public static void main(String[] args) {
       /* boolean isDebug = java.lang.management.ManagementFactory.getRuntimeMXBean().//отладка?
        getInputArguments().toString().indexOf("-agentlib:jdwp") > 0;
        boolean isDebugDist = java.lang.management.ManagementFactory.getRuntimeMXBean().//отладка?
                getInputArguments().toString().indexOf("-Xrunjdwp") > 0;
        if (isDebug == true || isDebugDist == true) {
            System.out.println("dfhgdfgg");
            System.exit(0);
        }*/
        if (LoaderLib.checkDebug1())
            System.exit(0);
        MainWin mainwin = new MainWin();
        mainwin.setVisible(true);
        mainwin.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}


class LogCheck {
    public static void check(String password) throws Exception {
        byte[] passwordBytes = password.getBytes(StandardCharsets.UTF_8);
        byte[] keyBytes = MessageDigest.getInstance("SHA-256").digest(passwordBytes);
        SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");

        byte[] encrypted;
        try (InputStream input = Login.class.getResourceAsStream("/GameEncrypted.jar")) {
            encrypted = input.readAllBytes();
        }

        // берём первые 16 байт и превращаем в nonce
        GCMParameterSpec nonce = new GCMParameterSpec(128, encrypted, 0, 16); // 128=16*8(бит)

        // можно расшифровывать
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, key, nonce); // Инициализируем шифратор с заданным ключом и nonce
        cipher.updateAAD(nonce.getIV()); // Также ПРОВЕРЯЕМ что nonce правильный (доп. защита от неправильно введёного пароля); При неправильном пароле расшифруется что-то другое
        byte[] decryptedBytes = cipher.doFinal(encrypted, 16, encrypted.length - 16); //мы расшифровали эти байты (все, кроме первых 16, которые nonce)
        // Теперь эти расшифрованные байты надо запустить
        // Считаем все ресурсы из расшифрованного JAR-файла в Map
        // Ключ - путь к ресурсу, значение - его байты
        Map<String, byte[]> resources = new HashMap<>(100);//вместимость
        try (ZipInputStream input = new ZipInputStream(new ByteArrayInputStream(decryptedBytes), StandardCharsets.UTF_8)) {
            ZipEntry entry;//для getNextEntry -  отдельная запись в архиве
            while ((entry = input.getNextEntry()) != null) {
                if (!entry.isDirectory()) { // Суперпримитивное чтение JAR-файла. Папки игнорируем, ресурсы полностью читаем
                    resources.put(entry.getName(), input.readAllBytes());
                }
            }
        }

        // Ресурсы считали. Теперь запускаем программу внутри своего собственного ClassLoader'а
        InMemoryClassLoader loader = new InMemoryClassLoader(resources); // Создание своего classloader'а с расшифрованными ресурсами
        Class<?> clazz = loader.findClass("GameWindow"); // Получаем главный класс программы
        String[] args = {};
        String[] argsStripped = Arrays.copyOfRange(args, 0, args.length); // В программу передаём аргументы
        Method main = clazz.getDeclaredMethod("main", String[].class); // Получаем метод main чтобы его вызвать

        System.out.println("Запуск зашифрованной программы!!!");
        main.invoke(null, new Object[] {null});
    }

    public static String what(String p) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            byte[] aa = { -7, 28, -119, -61, 12, 100, 114, -70, -16, -22, -88, -119, 5, -19, 65, -87 };//password
            byte[] bb = { -58, -103, 39, -28, -91, 79, 47, -95, 106, 5, -120, -14, -128, 81, 77, -32 };//Submit
            cipher.init(Cipher.DECRYPT_MODE, Globals.aesKey);

            String decryptedaa = new String(cipher.doFinal(aa));
            String decryptedbb = new String(cipher.doFinal(bb));
            switch (p) {
                case "dsbkjgfbsj":
                    return decryptedaa;
                case "slhrucnsoi":
                    return decryptedbb;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}

class Globals {

    public static String key = "Bar12345Bar12345";
    public static Key aesKey = new SecretKeySpec(key.getBytes(), "AES");
}

class MainWin extends JFrame {

    private JPasswordField pwdtxt;

    public MainWin() {
        setSize(250, 200);
        pwdtxt = new JPasswordField();
        String t = LogCheck.what("dsbkjgfbsj");
        JLabel pwdlbl = new JLabel(t);
        t = LogCheck.what("slhrucnsoi");
        JButton submitbtn = new JButton(t);
        JPanel formPanel = new JPanel(new GridLayout(5, 1));
        formPanel.add(pwdlbl);
        formPanel.add(pwdtxt);
        formPanel.add(submitbtn);
        add(formPanel);
        submitbtn.addActionListener(arg0 -> {
            if (LoaderLib.checkDebug2())
                System.exit(0);
            String password = new String(pwdtxt.getPassword());
            try {
                LogCheck.check(password);
            } catch (Exception t1) {
                JOptionPane.showMessageDialog(null, "Wrong password");
                t1.printStackTrace();
            }
        });
    }
}
