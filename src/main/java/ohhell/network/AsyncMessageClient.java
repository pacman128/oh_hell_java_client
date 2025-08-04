package ohhell.network;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.logging.Logger;

/**
 * Async message client.
 *
 * Messages are delimited by line feeds (\n)
 */
public class AsyncMessageClient implements AutoCloseable{
    private final static Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass().getPackage().getName());

    /**
     * Processor of messages.
     */
    private MessageProcessor msgProcessor;
    /**
     * Raw async client
     */
    private final AsyncClient asyncClient;
    /**
     * Character decoder
     */
    private final CharsetDecoder decoder;
    /**
     * Buffer for byte data.
     */
    private final ByteBuffer buffer = ByteBuffer.allocate(4096);
    /**
     * Buffer for character data
     */
    private final CharBuffer charBuf = CharBuffer.allocate(4096);

    /**
     * Default timeout (ms)
     */
    private long defaultTimeout = 500;

    /**
     * Interface for message processors.
     */
    public interface MessageProcessor {
        /**
         * Process a message
         * @param msg Message to process
         */
        void processMessage(String msg);
    }

    /**
     * Create a client
     * @param host Address of server
     * @param port Port of server
     * @param msgProcessor Message processor
     */
    public AsyncMessageClient(String host, int port, MessageProcessor msgProcessor) {
        this.msgProcessor = msgProcessor;
        asyncClient = new AsyncClient(host, port, this::processBuffer);
        decoder = Charset.defaultCharset().newDecoder();
    }

    /**
     * Set the message processor
     * @param msgProcessor Processor for messages
     */
    public void setMsgProcessor( MessageProcessor msgProcessor) {
        this.msgProcessor = msgProcessor;
    }

    public void setDefaultTimeout( long timeout) {
        defaultTimeout = timeout;
    }

    /**
     * Connect to server
     * @param timeout Timeout(ms)
     * @throws IOException On error
     */
    public void connect(long timeout) throws IOException {
        asyncClient.connect(timeout);
    }

    /**
     * Send message to server
     * @param msg Message to send
     * @param timeout Timeout (ms)
     * @throws IOException On error
     */
    public void sendMessage(String msg, long timeout) throws IOException {
        logger.info(String.format("Sending message: '%s'", msg));
        asyncClient.write((msg + "\n").getBytes(Charset.defaultCharset()), timeout);
    }

    /**
     * Send message with default timeout
     * @param msg Message to send
     * @throws IOException On error
     */
    public void sendMessage(String msg) throws IOException {
        sendMessage(msg, defaultTimeout);
    }

    /**
     * Process data received from server. This needs to be called periodically.
     * @param timeout Timeout (ms)
     * @throws IOException On error
     */
    public void processRead(long timeout) throws IOException {
        asyncClient.processRead(timeout);
    }

    @Override
    public void close() throws IOException {
        asyncClient.close();
    }

    /**
     * Process raw data from server
     * @param buf Buffer with raw data
     */
    private void processBuffer(ByteBuffer buf) {
        // Copy data from server into buffer
        buffer.put(buf);
        buffer.flip();

        // Send raw data to decoder
        decoder.decode(buffer, charBuf, false);

        // Remove used data from buffer and flip charBuf to ready for reading
        buffer.compact();
        charBuf.flip();

        // Process data in charBuf to find messages
        boolean charsLeft = false; // Are there partially processed chars in charBuf
        while( charBuf.remaining() > 0) {
            // Mark position for possible reset later
            charBuf.mark();

            // Try to find a complete message
            StringBuilder msg = new StringBuilder();
            int numChars = charBuf.remaining();
            for( int i=0; i < numChars; i++) {
                char ch = charBuf.get();
                if (ch == '\n') {
                    // Found end of message, send the message to processor
                    msgProcessor.processMessage(msg.toString());
                    charsLeft = false;
                    break;
                } else {
                    // Add char to msg and set flag for partially processed chars
                    charsLeft = true;
                    msg.append(ch);
                }
            }
        }

        if (charsLeft) {
            // Some characters were partially processed, reset buffer to process them again later
            charBuf.reset();
        }

        // Removed processed characters
        charBuf.compact();
    }
}
