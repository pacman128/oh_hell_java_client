package ohhell;

import java.io.InputStream;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Util {

    /**
     * Set the logging level of at a particular logger
     * @param pLogger Logger to set
     * @param pLevel Level to set to
     */
    public static void setLevel(Logger pLogger, Level pLevel) {
        // See https://stackoverflow.com/a/6308286/1366027
        // First set the level of the root level handlers
        var rootLogger = Logger.getLogger("");
        Handler[] handlers = rootLogger.getHandlers();
        for (Handler h : handlers) {
            h.setLevel(pLevel);
        }

        // Then set the level of the lower level logger
        pLogger.setLevel(pLevel);
    }

    /**
     * Set the root logging level
     * @param pLevel Level to set
     */
    public static void setLevel(Level pLevel) {
        setLevel(Logger.getLogger(""), pLevel);
    }


    /**
     *  Get a InputStream to a file from the resources folder
     *  Works everywhere, IDEA, unit test and JAR file.
     *
     * @param clazz Class to get ClassLoader from
     * @param fileName Name of file to get
     * @return InputStream to file
     */
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
