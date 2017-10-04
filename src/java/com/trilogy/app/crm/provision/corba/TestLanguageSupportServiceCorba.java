/*
 * Copyright (c) 1999-2005 REDKNEE.com. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * REDKNEE.com. ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the
 * terms of the license agreement you entered into with REDKNEE.com.
 *
 * REDKNEE.COM MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE
 * SUITABILITY OF THE SOFTWARE, EITHCDR EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE IMPLIED WARRANTIES OF MCDRCHANTABILITY, FITNESS FOR
 * A PARTICULAR PURPOSE, OR NON-INFRINGEMENT. REDKNEE.COM SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFCDRED BY LICENSEE AS A RESULT OF USING,
 * MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DCDRIVATIVES.
 */
package com.trilogy.app.crm.provision.corba;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;

import com.trilogy.service.corba.CorbaClientProperty;
import com.trilogy.util.corba.CorbaClientException;

import com.trilogy.app.crm.provision.corba.LanguageSupportServicePackage.*;

import org.omg.CORBA.StringHolder;

import java.io.*;

/**
 * @author lzou
 * @date   Jan.31, 2005
 *
 *  this class is a CORBA client simulator used by CRM QA group
 */
public class TestLanguageSupportServiceCorba
{
    public final static void main(String[] argv) throws Exception
    {
        if (argv.length < 2)
		{
			System.out.println("Usage:");
			System.out.println("\tjava TestLanguageSupportServiceCorba <nameHostname> <portNumber>");
            System.exit(1);
		}

        Context ctx = new ContextSupport();
        TestLanguageSupportServiceCorba corba = new TestLanguageSupportServiceCorba();

        CorbaClientProperty prop=new CorbaClientProperty();
        prop.setNameServiceName("name");
        prop.setNameServiceContextName(CORBA_CONTEXT_NAME);
        prop.setUsername(CORBA_PROP_USERNAME);
        prop.setPassword(CORBA_PROP_PWD);

        LanguageSupportServiceClient client=  null;
        LanguageSupportService service     =  null;

        String host   = argv[0];
        int port      = Integer.parseInt(argv[1]);
        
        prop.setNameServiceHost(host);
        prop.setNameServicePort(port);

        ctx.put(CorbaClientProperty.class,prop);
       
        client      =  new LanguageSupportServiceClient(ctx);
        service     =  client.getService();

        if ( service == null )
        {
            System.out.println("Cann't get LanguageSupportService servent.");
            System.exit(1);
        }

        BufferedReader bin = new BufferedReader(new InputStreamReader(System.in));

        int selection;

		for (;;)
		{
			try
			{
			    selection = selectMenu(bin);

			    if (selection == 3)
			    {
				    System.exit(1);
			    }

			    switch (selection)
			    {
				    case 1 :
					    corba.testGet(service, bin);
					    break;

                    case 2 :
                        corba.testSet(service, bin);
                        break;
                        
				    default:
					    System.out.println("Unknown option. Pleae try again");
			    }
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
    }
    
    public static int selectMenu(BufferedReader bin) 
        throws IOException
    {
        System.out.println("Please Enter Your Choice:");
        System.out.println("1. Test getLanguagePrompt Method.");
        System.out.println("2. Test setLanguagePrompt Method. ");
        System.out.println("3. End Test.");

        String input = bin.readLine();
        
        int retVal = 0;

		try
		{
			retVal = Integer.parseInt(input);
		}
		catch (Exception e)
		{
			System.out.println("Invalid input : " + input);
			System.out.println("Please try again.");
		}
		return retVal;


    }
    public void testGet(LanguageSupportService service, BufferedReader bin)
        throws IOException
    {
        System.out.println("Please enter msisdn:");
        String msisdn = bin.readLine();

        try
        {
            StringHolder langId = new StringHolder();
            int resultCode = service.getLanguagePrompt( msisdn, langId);
            System.out.println("resultCode: " + resultCode + "   returned Language ID:  " + langId.value );
        }
        catch(ServiceError error)
        {
            System.out.println("Error:  " + error );
        }
    }
    
    public void testSet(LanguageSupportService service,  BufferedReader bin) 
        throws IOException
    {
        System.out.println("Please enter msisdn:");
        String msisdn = bin.readLine();

        System.out.println("Please enter new LanguageId:");
        String newLangId = bin.readLine(); 

        try
        {
            StringHolder langId = new StringHolder();
            int resultCode = service.getLanguagePrompt( msisdn, langId);
            System.out.println("Before Set: Subscriber: " + msisdn + "'s langId is: " + langId.value);
            resultCode = service.setLanguagePrompt(msisdn, newLangId);
            resultCode = service.getLanguagePrompt(msisdn, langId);
            System.out.println("After Set: resultCode: " + resultCode + "   /   returned Language ID:  " + langId.value );
        }
        catch(com.redknee.app.crm.provision.corba.LanguageSupportServicePackage.ServiceError error)
        {
            System.out.println("Error:  " + error );
        }

    }
    
   public static final String CORBA_CONTEXT_NAME    = "Redknee/App/Crm/languageSupportServiceFactory";
   public static final String CORBA_NAMINGSERVICE_NAME 
                                                    = "name";
   public static final String CORBA_PROP_USERNAME   = "rkadm";
   public static final String CORBA_PROP_PWD        = "rkadm";
}
