package com.trilogy.app.crm.bulkprovisioning.prbtconnection;

import java.io.IOException;

/**
 * This interface is used for PRBT Telnet connection managment
 * @author ksivasubramaniam
 *
 */
public interface PRBTTelnetConnection
{
    /** 
     * 
     * @return
     */
    public boolean open();
    
    /**
     * 
     */
    public void close();
    
    /**
     * 
     * @return
     */
    public boolean isConnected();
    
    /**
     * Closes current connection and creates new connection
     */
    public void forceReconnect();
    
    /**
     * 
     * @param command
     * @return
     * @throws IOException
     */
    public String send(final String command) throws IOException;
}
