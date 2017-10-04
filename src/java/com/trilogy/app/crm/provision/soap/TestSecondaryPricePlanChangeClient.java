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
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Vector;

import electric.registry.Registry;
import electric.util.holder.intOut;

/**
 * Code ported by amit.baid@redknee.com
 * @author shailesh.kushwaha@redknee.com
 */
public class TestSecondaryPricePlanChangeClient implements Runnable
{

    public TestSecondaryPricePlanChangeClient(String url)
    {
        this.url = url;
    }

    public TestSecondaryPricePlanChangeClient(String url, String choice, String username, String pass, String msisdn)
    {
        this.url = url;
        this.choice = choice;
        this.username = username;
        this.password = pass;
        this.msisdn = msisdn;
    }

    public void run()
    {
        try
        {
            provisioner = (PricePlanProvisionInterface) Registry.bind(url, PricePlanProvisionInterface.class);
            if ("1".equals(choice))
            {
                provisionPPlan();
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

    public static void main(String[] args) throws Exception
    {
        if (args.length == 1)
        {
            new TestSecondaryPricePlanChangeClient(args[0]).interactive();
        }
        else if (args.length == 2)
        {
            TestSecondaryPricePlanChangeClient client1 = new TestSecondaryPricePlanChangeClient(args[0]);
            client1.getInfo();

            Collection c = new Vector();
            int count = Integer.parseInt(args[1]);

            for (int i = 0; i < count; ++i)
            {
                TestSecondaryPricePlanChangeClient client = new TestSecondaryPricePlanChangeClient(args[0],
                        client1.choice, client1.username, client1.password, client1.msisdn);
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

    public void interactive()
    {
        try
        {
            provisioner = (PricePlanProvisionInterface) Registry.bind(url, PricePlanProvisionInterface.class);

            do
            {
                getInfo();

                if ("1".equals(choice))
                {
                    provisionPPlan();
                }
                else if ("2".equals(choice))
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

    public void provisionPPlan()
    {
        try
        {
            intOut retCode = new intOut();
            int ppId = Integer.parseInt(pricePlanId);
            Date startDate_ = new Date();
            Date endDate_ = new Date();
            SimpleDateFormat sf = new SimpleDateFormat("dd/MM/yy");
            try
            {
                startDate_ = sf.parse(startDate.trim());
                endDate_ = sf.parse(endDate.trim());
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            System.out.println("To send msisdn " + msisdn + " ppId " + ppId + " startDate_ " + startDate_
                    + " endDate_ " + endDate_);
            try
            {
                provisioner.provisionSecondaryPricePlan(msisdn, ppId, startDate_, endDate_, retCode);
            }
            catch (SoapServiceException e)
            {

                e.printStackTrace();
            }

            System.out.println("\n\tResult code ==> " + retCode.value + "\t** Return Message: \" "
                    + mapReturnCode(retCode.value) + " \"");
            System.out.println("\n\n");
        }
        catch (Exception e)
        {

            e.printStackTrace();
        }
    }

    private String mapReturnCode(int retCode)
    {
        String msg = "";
        switch (retCode)
        {
            case PricePlanProvisionInterface.SUCCESSFUL:
                msg = "Successfully Provisioned !!";
                break;
            case PricePlanProvisionInterface.ENDDATE_LESS_THAN_STARTDATE:
                msg = "End Date is less than the Start Date, please correct the dates";
                break;
            case PricePlanProvisionInterface.FAILED_PROVISIONING:
                msg = "Provisioning of the Secondary Price Plan Failed";
                break;
            case PricePlanProvisionInterface.INTERNAL_ERROR:
                msg = "Internal Error occured";
                break;
            case PricePlanProvisionInterface.INVALID_MSISDN:
                msg = "Invalid Msidn provided for provisioning";
                break;
            case PricePlanProvisionInterface.INVALID_PARAMETERS:
                msg = "Invalid Parameters provided";
                break;
            case PricePlanProvisionInterface.INVALID_PRICE_PLAN:
                msg = "Invalid Price Plan Id provided";
                break;
            case PricePlanProvisionInterface.STARTDATE_LESS_THAN_CURRENTDATE:
                msg = "Start date of Sec PP is before current date";
                break;
            case PricePlanProvisionInterface.INVALID_SUBSCRIBER_STATE:
                msg = "Deactivated Subscriber";
                break;
            default:
                msg = "Some other error";
                break;
        }

        return msg;
    }

    public void getInfo() throws IOException
    {

        System.out.println("\n\n");
        System.out.println("PricePlan Provisioner Client");
        System.out.println("=============================");
        System.out.println("1. Provision Secondary Price Plan");
        System.out.println("2. Quit");
        System.out.print("\nEnter choice: ");
        choice = input.readLine().trim();

        if ("2".equals(choice))
        {
            System.exit(0);
        }

        System.out.print("Username: ");
        this.username = input.readLine();

        System.out.print("Password: ");
        this.password = input.readLine();

        System.out.print("MSISDN: ");
        this.msisdn = input.readLine().trim();

        System.out.print("price Plan ID : ");
        this.pricePlanId = input.readLine().trim();
        if (pricePlanId == null ||
                pricePlanId != null && pricePlanId.length() <= 0)
        {
            System.out.print("\t Price Plan Id cannot be null, Please try again");
            getInfo();
        }

        System.out.print("StareDate: dd/MM/yy: ");
        this.startDate = input.readLine().trim();

        if (!checkDate(startDate.trim()))
        {
            System.out.print("Invalid Start Date given.");
            getInfo();
        }

        System.out.print("EndDate: dd/MM/yy: ");
        this.endDate = input.readLine().trim();

        if (!checkDate(endDate.trim()))
        {
            System.out.print("Invalid End Date given.");
            getInfo();
        }
    }

    private boolean checkDate(String date)
    {
        try
        {
            if (date != null && date.length() == 8)
            {
                StringBuilder sb = new StringBuilder();
                String day = sb.append(date.charAt(0)).append(date.charAt(1)).toString().trim();
                sb = new StringBuilder();
                String mon = sb.append(date.charAt(3)).append(date.charAt(4)).toString().trim();
                sb = new StringBuilder();
                String yy = sb.append(date.charAt(6)).append(date.charAt(7)).toString().trim();

                if (Integer.parseInt(day) > 0 && Integer.parseInt(day) <= 31)
                {
                    if (Integer.parseInt(mon) > 0 && Integer.parseInt(mon) < 13)
                    {
                        return true;
                    }
                    else
                    {
                        return false;
                    }
                }
            }
        }
        catch (Exception e)
        {
            System.out.println("Invalid Date specified");

            return false;
        }
        return false;
    }

    public static void printUsage()
    {
        System.out.println("usage: SubscriberProvisionClient url");
        System.out.println(" or SubscriberProvisionClient url numberofThread");
        System.out.println(" if the numberofThread is not specified, the client will run in interative mode");
    }

    PricePlanProvisionInterface provisioner = null;
    BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
    String url = "";
    String choice = "";
    String username = "";
    String password = "";
    String msisdn = "";
    String pricePlanId = "";
    String startDate = "";
    String endDate = "";
}
