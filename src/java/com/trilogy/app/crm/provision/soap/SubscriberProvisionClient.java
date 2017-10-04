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
package com.trilogy.app.crm.provision.soap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Vector;

import electric.registry.Registry;
import electric.util.holder.intOut;

/**
 * Code ported by amit.baid@redknee.com
 * @author imahalingam
 */
public class SubscriberProvisionClient implements Runnable
{

    public SubscriberProvisionClient(String url)
    {
        this.url = url;
    }

    public SubscriberProvisionClient(String url, String choice, String username, String pass, String msisdn)
    {
        this.url = url;
        this.choice = choice;
        this.username = username;
        this.password = pass;
        this.msisdn = msisdn;
    }

    private void deactivateSub()
    {
        try
        {
            int resultCode = provisioner.deactivateSub(username, password, msisdn);

            System.out.println("\nresult code -> " + resultCode);
            System.out.println("\n");
        }
        catch (Throwable t)
        {
            t.printStackTrace();
        }
    }

    private void deleteSub()
    {
        try
        {
            int resultCode = provisioner.deleteSub(username, password, msisdn);

            System.out.println("\nresult code -> " + resultCode);
            System.out.println("\n");
        }
        catch (Throwable t)
        {
            t.printStackTrace();
        }
    }

    private void getSub()
    {
        SubscriberInfo subinfo;
        try
        {
            intOut retCode = new intOut();
            SubscriberInfo newSubInfo = provisioner.getSub(username, password, msisdn, retCode);

            System.out.println("\nresult code -> " + retCode.value);
            if (retCode.value == SubscriberProvisionInterface.LOGIN_FAILED)
            {
                System.out.println("\nLogin FAILED ! ");
            }
            else
            {
                System.out.println("\nSubscriber -> " + newSubInfo);
            }

            System.out.println("");
            System.out.println("\n");
        }
        catch (Throwable t)
        {
            t.printStackTrace();
        }
    }

    public void run()
    {
        try
        {
            provisioner = (SubscriberProvisionInterface) Registry.bind(url, SubscriberProvisionInterface.class);
            if ("1".equals(choice))
            {
                getSub();
            }
            else if ("2".equals(choice))
            {
                deactivateSub();
            }
            else if ("3".equals(choice))
            {
                deleteSub();
            }
            else
            {
                System.out.println("\nInvalid choice");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void getInfo() throws IOException
    {

        System.out.println("\n\n");
        System.out.println("Subscriber Provisioner Client");
        System.out.println("=============================");
        System.out.println("1. Get Subscriber");
        System.out.println("2. Deactivate Subscriber");
        System.out.println("3. Delete Subscriber");
        System.out.println("4. Quit");
        System.out.print("\nEnter choice: ");
        choice = input.readLine().trim();

        if ("4".equals(choice))
        {
            System.exit(0);
        }

        System.out.print("Username: ");
        username = input.readLine();
        System.out.print("Password: ");
        password = input.readLine();
        System.out.print("MSISDN: ");
        msisdn = input.readLine().trim();
    }

    public void interactive()
    {
        try
        {
            provisioner = (SubscriberProvisionInterface) Registry.bind(url, SubscriberProvisionInterface.class);

            do
            {
                getInfo();

                if ("1".equals(choice))
                {
                    getSub();
                }
                else if ("2".equals(choice))
                {
                    deactivateSub();
                }
                else if ("3".equals(choice))
                {
                    deleteSub();
                }
                else if ("4".equals(choice))
                {
                    return;
                }
                else
                {
                    System.out.println("\nInvalid choice '" + choice + "'");
                }
            } while (true);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception
    {
        if (args.length == 1)
        {
            new SubscriberProvisionClient(args[0]).interactive();
        }
        else if (args.length == 2)
        {
            SubscriberProvisionClient client1 = new SubscriberProvisionClient(args[0]);
            client1.getInfo();

            Collection c = new Vector();
            int count = Integer.parseInt(args[1]);

            for (int i = 0; i < count; ++i)
            {
                SubscriberProvisionClient client = new SubscriberProvisionClient(args[0], client1.choice,
                        client1.username, client1.password, client1.msisdn);
                c.add(client);
                client.run();
            }
        }
        else
        {
            printUsage();
            System.exit(0);
        }
    }

    public static void printUsage()
    {
        System.out.println("usage: SubscriberProvisionClient url");
        System.out.println(" or SubscriberProvisionClient url numberofThread");
        System.out.println(" if the numberofThread is not specified, the client will run in interative mode");
    }

    SubscriberProvisionInterface provisioner = null;
    BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
    String url = null;
    String choice = null;
    String username = null;
    String password = null;
    String msisdn = null;
}
