package ohhell;

import java.io.InputStream;

public class Util {

    // get a file from the resources folder
    // works everywhere, IDEA, unit test and JAR file.
    public static InputStream getFileFromResourceAsStream(Class<?> clazz, String fileName) {

        // The class loader that loaded the class
        ClassLoader classLoader = clazz.getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(fileName);

        // the stream holding the file content
        if (inputStream == null) {
            throw new IllegalArgumentException("file not found! " + fileName);
        } else {
            return inputStream;
        }

    }


    public static void main(String [] args) {
        try {
            InputStream strm = Util.getFileFromResourceAsStream(Util.class,"images/back.png");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
