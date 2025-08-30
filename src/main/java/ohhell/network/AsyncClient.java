package ohhell.network;

import ohhell.Util;

import java.lang.invoke.MethodHandles;
import java.util.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.logging.*;

/**
 * Async TCP client implementation.
 */
public class AsyncClient implements AutoCloseable{
    /**
     * Server host to connect to.
     */
    private final String host;
    /**
     * Server port to connect to.
     */
    private final int port;
    /**
     * Buffer for incoming data.
     */
    private ByteBuffer buf;
    /**
     * Selector for async operations.
     */
    private Selector selector;
    /**
     * Socket channel to server.
     */
    private SocketChannel channel;
    /**
     * Processor for data received.
     */
    private final InputProcessor inputProcessor;
    /**
     * Logger.
     */
    private final static Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass().getPackage().getName());

    public static class NoConnectionException extends IOException {
        public NoConnectionException() {
            super("No Connection");
        }
    }

    /**
     * Interface for input processors.
     */
    public interface InputProcessor {
        /**
         * Process input data,
         * Buffer has been flipped before passed to method. It will be cleared after
         * the call.
         * @param buffer Buffer with input data
         */
        void processInput(ByteBuffer buffer);
    }

    /**
     * Create a new client.
     *
     * @param host Server address to connect to
     * @param port Server port to connect to
     * @param inputProcessor Processor for data received
     */
    public AsyncClient(String host, int port, InputProcessor inputProcessor) {
        this.host = host;
        this.port = port;
        this.inputProcessor = inputProcessor;
    }

    /**
     * Connect to the server.
     * @param timeout Timeout (ms)
     * @throws IOException On error
     */
    public void connect(long timeout) throws IOException {
        selector = Selector.open();
        channel = SocketChannel.open();
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_CONNECT);
        channel.connect(new InetSocketAddress(host, port));
        buf = ByteBuffer.allocate(1*1024);
        long sleep = Math.min(timeout, 1000);
        while(timeout > 0) {
            if (selector.select(sleep) < 1) {
                timeout-=sleep;
                continue;
            }
            Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
            while(keys.hasNext()) {
                SelectionKey key = keys.next();
                keys.remove();
                if (!key.isValid() || !key.isConnectable()){
                    continue;
                }
                SocketChannel channel = (SocketChannel)key.channel();
                if (channel.isConnectionPending()) {
                    channel.finishConnect();
                    channel.configureBlocking(false);
                    return; // we are ready to receive bytes
                }
            }
        }
        // TODO: Just return boolean
        throw new IOException("Connection timed out");
    }

    @Override
    public void close() throws IOException {
        if ( channel!=null ) {
            channel.close();
            channel = null;
        }
        if ( selector!=null ) {
            selector.close();
            selector = null;
        }
        buf=null;
    }

    /**
     * Write data to server.
     *
     * @param bytes Bytes to send to server
     * @param timeout Timeout (ms)
     * @throws IOException On error
     */
    public void write(byte[] bytes, long timeout) throws IOException {
        ByteBuffer outBuffer = ByteBuffer.wrap(bytes);
        channel.register(selector, SelectionKey.OP_WRITE);
        long timeLeft = timeout;
        long sleep = Math.min(timeout, 1000);
        while(timeLeft > 0) {
            if (selector.select(sleep) < 1) {
                timeLeft -= sleep;
                continue;
            }
            Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
            while(keys.hasNext()) {
                SelectionKey key = keys.next();
                keys.remove();
                if (!key.isValid() || !key.isWritable()) {
                    continue;
                }
                SocketChannel channel = (SocketChannel)key.channel();
                logger.finer("write remaining=" + outBuffer.remaining());
                channel.write(outBuffer);
                logger.finer("write remaining=" + outBuffer.remaining());
                if (outBuffer.remaining() <= 0) {
                    return;
                }
            }
        }
        throw new IOException(String.format("Write timed out (timeout: %d)", timeout));
    }

    /**
     * Process data received from server.
     * This method needs to be called regular to handle data from server.
     * @param timeout Timeout (ms)
     * @throws IOException On error
     */
    public void processRead(long timeout) throws IOException {
        try {
            channel.register(selector, SelectionKey.OP_READ);
            long sleep = Math.min(timeout, 1000);
            while (timeout>0) {
                if (selector.select(sleep) < 1) {
                    timeout -= sleep;
                    continue;
                }
                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
                while(keys.hasNext()) {
                    SelectionKey key = keys.next();
                    keys.remove();
                    if ( !key.isValid() || !key.isReadable()) {
                        continue;
                    }
                    SocketChannel channel = (SocketChannel)key.channel();
                    buf.clear();
                    int len = channel.read(buf);
                    if (len == -1) {
                        logger.severe("Socket disconnected");
                        throw new NoConnectionException();
                    }
                    buf.flip();
                    inputProcessor.processInput(buf);
                    buf.clear();
                }
            }

        } catch( IOException e) {
            logger.severe(e.getLocalizedMessage());
            throw e;
        }
    }

    public static void main(String [] args) {

        Util.setLevel(Logger.getLogger("ohhell"), Level.FINER);
        try (
            AsyncClient client = new AsyncClient("127.0.0.1", 7001, (ByteBuffer buf) -> {
                logger.fine("Read " + buf.remaining() + " bytes");
                buf.clear();
            });
            ){
            client.connect(2000);
            client.write(new byte[2], 1000);
            for(int i=0; i < 3; i++) {
                client.processRead(1000);
            }
            client.write(new byte[5], 1000);
            for(int i=0; i < 3; i++) {
                client.processRead(1000);
            }

            Thread.sleep(1000);
        } catch( Exception e) {
            e.printStackTrace();
        }
    }
}