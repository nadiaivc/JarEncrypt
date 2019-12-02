
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class ClassEncryption {

    public static void main(String[] args) throws Exception{

        String JarIn = "C:\\Users\\Win10\\IdeaProjects\\Game\\out\\artifacts\\Game_jar\\Game.jar";
        String JarOut ="C:\\Users\\Win10\\IdeaProjects\\Game\\out\\artifacts\\GameEncrypted.jar";
        String password ="correct";

        // Превращаем пароль (произвольной длины) в ключ (256 бит, 32 байта)
        // Так совпало, что длина хэша SHA-256, равна длине ключа AES256-GCM))))
        byte[] passwordBytes = password.getBytes(StandardCharsets.UTF_8); // Превращаем пароль из строки в байты UTF-8
        byte[] keyBytes = MessageDigest.getInstance("SHA-256").digest(passwordBytes); // Хэшируем байты пароля, на выходе имеем всегда 32 байта
        SecretKeySpec key = new SecretKeySpec(keyBytes, "AES"); // Так как это джава, тут любая вещь должна быть превращена в объект :/


        // Также для AES-GCM нужен nonce, его можно сгенерировать на рандом
        byte[] nonceBytes = new byte[16]; // 128 бит
        new SecureRandom().nextBytes(nonceBytes);
        GCMParameterSpec nonce = new GCMParameterSpec(nonceBytes.length * 8, nonceBytes); // Тож превращаем в объект; *8 превращает количество байтов в биты

        // Само шифрование
         // В качестве дополнительной информацию которую надо ПРОВЕРИТЬ, указываем тоже nonce

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key, nonce); // Инициализируем шифратор с заданным ключом и nonce
        cipher.updateAAD(nonceBytes);
        Path inputFile = Paths.get(JarIn);
        Path outputFile = Paths.get(JarOut);
        byte[] plainBytes = Files.readAllBytes(inputFile); // Читаем файл в массив байтов
        byte[] encryptedBytes = cipher.doFinal(plainBytes); // В общем-то, шифруем!

            // Запись в файл, ничего интересного. Нам надо записать И nonce, И зашифрованное содержимое
        byte[] outputFileBytes = new byte[nonceBytes.length + encryptedBytes.length];
        System.arraycopy(nonceBytes, 0, outputFileBytes, 0, nonceBytes.length); // Помещаем nonce в начало файла
        System.arraycopy(encryptedBytes, 0, outputFileBytes, nonceBytes.length, encryptedBytes.length); // ... И сразу после него, зашифрованное содержимое
        Files.write(outputFile, outputFileBytes, StandardOpenOption.CREATE, StandardOpenOption.WRITE); // Просто запись байтов в файл

            // Сообщение о том как всё классно
        System.out.println("Файл " + inputFile + " зашифрован паролем " + password + " в файл " + outputFile);

    }

}
