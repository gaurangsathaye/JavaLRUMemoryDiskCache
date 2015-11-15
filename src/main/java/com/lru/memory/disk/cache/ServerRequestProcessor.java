package com.lru.memory.disk.cache;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author sathayeg
 */
public class ServerRequestProcessor implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(ServerRequestProcessor.class);

    private final Socket socket;
    private final ServerCache cache;

    ServerRequestProcessor(Socket socket, ServerCache standAloneServerCache) {
        this.socket = socket;
        this.cache = standAloneServerCache;
    }

    @Override
    public void run() {
        try {
            proc();
        } catch (Exception e) {
            log.error("Error processing", e);
        }
    }

    private void proc() {
        InputStream is = null;
        InputStreamReader isr = null;
        BufferedReader br = null;

        OutputStream os = null;
        PrintWriter pw = null;

        AutoCloseable closeables[] = {is, isr, br, os, pw, socket};

        try {
            is = socket.getInputStream();
            isr = new InputStreamReader(is);
            br = new BufferedReader(isr);

            String request = "";
            StringBuilder sb = new StringBuilder();
            do {
                request = br.readLine();
                sb.append(request);
                if (request.contains("<end>")) {
                    break;
                }
            } while ((null != request));

            os = socket.getOutputStream();
            pw = new PrintWriter(os, true);

            String response = "From server, request: " + sb.toString();

            pw.println(response);
            os.flush();
            pw.flush();
        } catch (Exception e) {
            log.error("Unable to process request", e);
        } finally {
            Utl.closeAll(closeables);
        }
    }

    static String readInput(BufferedReader br) throws IOException {
        String request = "";
        StringBuilder sb = new StringBuilder();
        do {
            request = br.readLine();
            sb.append(request);
            if (request.contains("<end>")) {
                break;
            }
        } while ((null != request));
        return sb.toString();
    }

}
