package ohhell.network;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MessageProcessor implements AsyncMessageClient.MessageProcessor {

    final ArrayList<String> msgs = new ArrayList<>();

    @Override
    public void processMessage(String msg) {
        msgs.add(msg);
    }

    public List<String> getMessages() {
        return msgs;
    }
}

class AsyncMessageClientTest {

    private final static int PORT = 7002;
    private EchoServer echoServer;
    private AsyncMessageClient client;
    private MessageProcessor msgProcessor;

    @BeforeEach
    void init() {
        echoServer = new EchoServer(PORT);
        msgProcessor = new MessageProcessor();
        client = new AsyncMessageClient("localhost", PORT, msgProcessor);
    }

    private boolean waitForInputs( int numMsgs, int timeout) {
        int totalTime = 0;
        while( totalTime < timeout ) {
            try {
                client.processRead(100);
                if (msgProcessor.getMessages().size() >= numMsgs) {
                    return true;
                }
            } catch (IOException e) {
                System.err.println(e.getLocalizedMessage());
                return false;
            }
            totalTime += 100;
        }
        return false;
    }

    @Test
    void client() {
        echoServer.start();

        try {
            client.connect(1000);
            client.sendMessage( "Message 1", 100);
            assertTrue(waitForInputs(1, 1000));
            List<String> msgs = msgProcessor.getMessages();
            assertEquals(1, msgs.size());
            assertEquals("Message 1", msgs.get(0));
            client.sendMessage( "Message 2", 100);
            assertTrue(waitForInputs(2, 1000));
            assertEquals(2, msgs.size());
            assertEquals("Message 2", msgs.get(1));

            client.sendMessage( "Message 3", 100);
            client.sendMessage( "Message 4", 100);
            assertTrue(waitForInputs(4, 1000));
            assertEquals(4, msgs.size());
            assertEquals("Message 3", msgs.get(2));
            assertEquals("Message 4", msgs.get(3));

            client.sendMessage("\0", 1000);
            client.close();
        } catch (IOException e) {
            fail(e.getLocalizedMessage());
        }

        assertTrue(echoServer.wait(2000));
    }
}