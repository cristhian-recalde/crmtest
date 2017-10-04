/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s).  A complete listing of authors of this work is readily
 * available.  Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee.  No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the license agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */

package com.trilogy.app.crm.ssl;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.security.KeyStore;
import java.util.Hashtable;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.apache.axis.components.net.BooleanHolder;
import org.apache.axis.components.net.JSSESocketFactory;

/**
 * Class to renew SSL certificates without BSS restart.
 * 
 * @author ankit.nagpal
 */

public class InstallSocketFactory extends JSSESocketFactory {
    
    public static boolean initalizeFactory = false;

    public InstallSocketFactory(Hashtable attributes)
    {
        super(attributes);
    }
    
    protected void initFactory() throws IOException
    {
        try
        {
            InputStream in = new FileInputStream(System.getProperty("javax.net.ssl.trustStore"));
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(in, System.getProperty("javax.net.ssl.keyStorePassword").toCharArray());
            
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(ks, System.getProperty("javax.net.ssl.keyStorePassword").toCharArray());
            
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(ks);
            
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
            this.sslFactory = sslContext.getSocketFactory();
        }
        catch (Exception e)
        {
            // Will be caught while re-starting the server
        }
    }
    
    public Socket create(String host, int port, StringBuffer otherHeaders, BooleanHolder useFullURL) throws Exception
    {
        if(initalizeFactory)
        {
            this.initFactory();
            initalizeFactory = false;
        }
        return super.create(host, port, otherHeaders, useFullURL);
    }
}