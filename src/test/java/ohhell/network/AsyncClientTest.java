package ohhell.network;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
/*
 * Copyright (c) 2013, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

class InputProcessor implements AsyncClient.InputProcessor {

    private final ArrayList<byte []> inputs = new ArrayList<>();

    private int totalBytes = 0;

    @Override
    public void processInput(ByteBuffer buffer) {
        byte [] input = new byte[buffer.remaining()];
        buffer.get(input);
        inputs.add(input);
        totalBytes += input.length;
    }

    public List<byte[]> getInputs() {
        return inputs;
    }

    public int getTotalBytes() {
        return totalBytes;
    }

    public byte[] getCombinedInputs() {
        // How to functionally compute the total bytes from the list
        // int totalBytes = inputs.stream().mapToInt(ary -> ary.length).sum();
        byte [] totalInputs = new byte[totalBytes];
        int destIndex = 0;
        for( byte [] input: inputs) {
            System.arraycopy(input, 0, totalInputs, destIndex, input.length);
            destIndex += input.length;
        }
        return totalInputs;
    }
}
class AsyncClientTest {
    private final static int PORT = 7002;

    private EchoServer echoServer;
    private AsyncClient client;
    private InputProcessor inputProcessor;

    @org.junit.jupiter.api.BeforeEach
    void init() {
        echoServer = new EchoServer(PORT);
        inputProcessor = new InputProcessor();
        client = new AsyncClient("localhost", PORT, inputProcessor);
    }

    private boolean waitForInputs( int inputSize, int timeout) {
        int totalTime = 0;
        while( totalTime < timeout ) {
            try {
                client.processRead(100);
                if (inputProcessor.getTotalBytes() >= inputSize) {
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
            client.write( new byte[] {1,2,3,4}, 100);
            assertTrue(waitForInputs(4, 1000));
            assertArrayEquals(new byte[] {1,2,3,4}, inputProcessor.getCombinedInputs());
            client.write( new byte[] {10, -10, 11, -11, -12}, 100);
            assertTrue(waitForInputs(5, 1000));
            assertArrayEquals(new byte[] {1, 2, 3, 4, 10, -10, 11, -11, -12}, inputProcessor.getCombinedInputs());

            client.write(new byte[] {0}, 1000);
            client.close();
        } catch (IOException e) {
            fail(e.getLocalizedMessage());
        }

        assertTrue(echoServer.wait(2000));
    }
}