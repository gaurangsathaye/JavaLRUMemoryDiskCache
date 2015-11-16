package com.lru.memory.disk.cache;

import com.lru.memory.disk.cache.exceptions.BadRequestException;
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

            String request = br.readLine();

            os = socket.getOutputStream();
            pw = new PrintWriter(os, true);

            String response = "From server, request: " + request;

            pw.println(response);
            os.flush();
            pw.flush();
        } catch (Exception e) {
            log.error("Unable to process request", e);
        } finally {
            Utl.closeAll(closeables);
        }
    }
    
    /*private String processRequest(String req) throws BadRequestException{
        if(Utl.areBlank(req)) throw new BadRequestException("request is blank", null);
    }*/
}
