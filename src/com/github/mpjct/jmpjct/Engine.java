package com.github.mpjct.jmpjct;

/*
 * Base proxy code. This should really just move data back and forth
 * Calling plugins as needed
 */

import java.net.Socket;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.BufferedInputStream;
import java.util.ArrayList;
import org.apache.log4j.Logger;
import com.github.mpjct.jmpjct.plugin.Base;
import com.github.mpjct.jmpjct.mysql.proto.Handshake;
import com.github.mpjct.jmpjct.mysql.proto.HandshakeResponse;
import com.github.mpjct.jmpjct.mysql.proto.Flags;

public class Engine implements Runnable {
    public Logger logger = Logger.getLogger("Engine");
    
    public int port = 0;
    
    public Socket clientSocket = null;
    public InputStream clientIn = null;
    public OutputStream clientOut = null;
    
    // Plugins
    public ArrayList<Base> plugins = new ArrayList<Base>();
    
    // Packet Buffer. ArrayList so we can grow/shrink dynamically
    public ArrayList<byte[]> buffer = new ArrayList<byte[]>();
    public int offset = 0;
    
    // Stop the thread?
    public boolean running = true;

    // What sorta of result set should we expect?
    public int expectedResultSet = Flags.RS_OK;
    
    // Connection info
    public Handshake handshake = null;
    public HandshakeResponse authReply = null;
    
    public String schema = "";
    public String query = "";
    public byte command = 0; //context query type
    public long statusFlags = 0;
    public long sequenceId = 0;
    
    // Buffer or directly pass though the data
    public boolean bufferResultSet = true;
    public boolean packResultSet = true;
    
    // Modes
    public int mode = Flags.MODE_INIT;
    
    // Allow plugins to muck with the modes
    public int nextMode = Flags.MODE_INIT;
    
    public Engine(int port, Socket clientSocket, ArrayList<Base> plugins) throws IOException {
        this.port = port;
        this.plugins = plugins;
        
        this.clientSocket = clientSocket;
        this.clientSocket.setPerformancePreferences(0, 2, 1);
        this.clientSocket.setTcpNoDelay(true);
        this.clientSocket.setTrafficClass(0x10);
        this.clientSocket.setKeepAlive(true);
        
        this.clientIn = new BufferedInputStream(this.clientSocket.getInputStream(), 16384);
        this.clientOut = this.clientSocket.getOutputStream();
    }

    public void run() {
        
    }
    
    public void buffer_result_set() {
        if (!this.bufferResultSet)
            this.bufferResultSet = true;
    }
    
    public void halt() {
        this.logger.trace("Halting!");
        this.running = false;
    }
    
    public void clear_buffer() {
        this.logger.trace("Clearing Buffer.");
        this.offset = 0;
        
        // With how ehcache works, if we clear the buffer via .clear(), it also
        // clears the cached value. Create a new ArrayList and count on java
        // cleaning up after ourselves.
        this.buffer = new ArrayList<byte[]>();
    }
}
