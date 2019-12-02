
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

// Реализация класслоадера который берёт классы/ресурсы из памяти
public class InMemoryClassLoader extends ClassLoader {
    private final Map<String, byte[]> resources;
    private final Map<String, Class<?>> loadedClasses = new HashMap<>(8192);
    private final URLStreamHandler handler = new InMemoryURLStreamHandler();

    public InMemoryClassLoader(Map<String, byte[]> resources) {
        this.resources = resources;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        // Если такой класс уже загружался - не надо определять его ещё раз
        Class<?> loaded = loadedClasses.get(name);
        if (loaded != null) {
            return loaded;
        }

        // Если нет - берём ресурс и превращаем его в класс, и запоминаем что этот класс мы уже загрузили
        byte[] bytes = resources.get(name.replace('.', '/') + ".class");
        if (bytes == null) {
            throw new ClassNotFoundException("Class not found in memory: " + name);
        }
        Class<?> clazz = defineClass(name, bytes, 0, bytes.length);
        loadedClasses.put(name, clazz);
        return clazz;
    }

    @Override
    protected URL findResource(String name) {
        byte[] resource = resources.get(name);
        try {
            // Если что-то просит URL того что находится в памяти, нужно дать URL с обработчиком потока
            // Который при попытке считать что-то по этому URL, вернёт то что в памяти
            return resource == null ? null : new URL("inmemory", null, -1, name, handler); // inmemory://name //порт тут по умолчанию
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected Enumeration<URL> findResources(String name) throws IOException {
        URL resource = findResource(name);
        return resource == null ? super.findResources(name) : Collections.enumeration(Collections.singletonList(resource)); // перечисление объектов URL,
                                                                                                        // представляющих все ресурсы с заданным именем.
    }

    static {
        registerAsParallelCapable(); // Класслоадер потокобезопасен, можно всё ускорить не блокируясь на загрузку каждого класса
    }

    // Реализация URLStreamHandler которая возвращает данные из памяти
    private final class InMemoryURLStreamHandler extends URLStreamHandler {
        @Override
        protected URLConnection openConnection(URL u) throws IOException {//соединение с объектом, на который ссылается аргумент
            if (!u.getProtocol().equals("inmemory")) {
                throw new IOException("Unknown protocol: " + u.getProtocol()); // Этот обработчик понимает только ссылки inmemory://
            }
            return new InMemoryURLConnection(u);
        }
    }

    // Реализация URLConnection которая возвращает данные из памяти
    private final class InMemoryURLConnection extends URLConnection {
        private final byte[] resource;
        private InputStream input;

        private InMemoryURLConnection(URL url) {
            super(url);
            resource = resources.get(url.getFile());
        }

        @Override
        public void connect() throws IOException {
            if (resource == null) { // Если такого ресурса не существует...
                throw new FileNotFoundException(url.getFile());
            }
            if (input == null) { // Подготовка InputStream
                input = new ByteArrayInputStream(resource);
            }
        }

        @Override
        public InputStream getInputStream() throws IOException {//чтение из открытого соединения
            connect();
            return input;
        }

        @Override
        public int getContentLength() {
            return resource == null ? -1 : resource.length;
        }
    }
}