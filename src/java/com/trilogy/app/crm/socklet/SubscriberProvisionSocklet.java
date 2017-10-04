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
package com.trilogy.app.crm.socklet;

import java.io.*;
import java.net.Socket;
import java.util.*;

import com.trilogy.framework.core.socket.Socklet;
import com.trilogy.framework.core.socket.Socklets;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAwareSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.socklet.AddSubscriberCommand;

/**
 * @author ali
 */
public class SubscriberProvisionSocklet extends ContextAwareSupport implements Socklet{

    protected String sockletCommand=null;

    protected Map commands=new HashMap();
    
    protected AddSubscriberCommand addCommand=new AddSubscriberCommand();
    protected UpdateSubscriberCommand updateCommand=new UpdateSubscriberCommand();
    protected DeleteSubscriberCommand deleteCommand=new DeleteSubscriberCommand();

    private void initCommands()
    {
        commands.put("A", addCommand); 
        commands.put("U", updateCommand); 
        commands.put("D", deleteCommand);
    }
    public SubscriberProvisionSocklet()
    {
        super();
        
        initCommands();
    }

    public SubscriberProvisionSocklet(Context ctx)
    {
        super();
        setContext(ctx);
        
        initCommands();
    }
    
    public SubscriberProvisionSocklet(String _command)
    {
        super();
        sockletCommand=_command;
        
        initCommands();
    }

    public SubscriberProvisionSocklet(Context ctx,String _command)
    {
        super();
        setContext(ctx);
        sockletCommand=_command;
    }

    /**
     * @see com.redknee.framework.core.socket.Socklet#service(com.redknee.framework.xhome.context.Context, java.lang.String, java.io.PrintWriter)
     */
    public void service(Context _ctx, String args, PrintWriter out)
    {
        // there is actually a shell command. seems like it's comming from shell.
        try
        {
            Socket client = Socklets.getSocket(_ctx);
            InputStream pin = client.getInputStream();
            PrintStream pout = new PrintStream(client.getOutputStream());
        
            /*
             * Arguments are setup as: 
             *  A,BAN,SPID,FirstName,LastName,SubscriberType,MSISDN,faxMSISDN,dataMSISDN,state,startDate,endDate,birthdate,deposit,lastdepositdate,creditlimit,maxBalance,maxRecharge,reactivationFee,postpaidsupportMSISDN,chargePPSM,expirydate,priceplan,services,d.o.b.,packageID,address1,address2,address3,city,province,country,dealercode,idtype1,id#1,idtype2,id#2,discountclass,initialbalance,IMSI
             *  U,MSISDN,firstname,lastname,state,priceplan,services,address,address1,address2,address3,city,province,country,dealercode,idtype1,id#1,idtype2,id#2,discountclass
             *  D,MSISDN
             */
            String command = args.substring(0, 1);  //Command: A, U, D
            String commandArguments = args.substring(2);
        
            SubscriberProvisioningCommand cmd=getCommand(command);
            if(cmd!=null)
            {
                cmd.setOut(pout);
                cmd.setErr(pout);
                cmd.setIn(pin);
                
                Runnable executor = new SubscriberProvisioningExecutor(_ctx, cmd, commandArguments);
                executor.run();

            }

        }
        catch (IOException e)
        {
            new MinorLogMsg(this,e.getMessage(),e).log(_ctx);
        }
    }//service

    /**
     * @param command
     * @return the class that handles a command
     */
    private SubscriberProvisioningCommand getCommand(String command)
    {
    	SubscriberProvisioningCommand cmd=(SubscriberProvisioningCommand) commands.get(command.toUpperCase());

        return cmd;
    }

    
}
