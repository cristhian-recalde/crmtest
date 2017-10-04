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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Vector;

import electric.registry.Registry;
import electric.util.holder.intOut;

/**
 * Code ported by amit.baid@redknee.com
 * @author shailesh.kushwaha@redknee.com
 */
public class TestSubscriberInterfaceClient implements Runnable
{

    /**
     * @param url
     */
    public TestSubscriberInterfaceClient(String url)
    {
        this.url = url;
    }

    /**
     * @param url
     * @param choice
     * @param username
     * @param pass
     * @param msisdn
     */
    public TestSubscriberInterfaceClient(String url, String choice, String username, String pass, String msisdn)
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
            provisioner = (SubscriberInterface) Registry.bind(url, SubscriberInterface.class);
            if ("1".equals(choice))
            {
                getPPlanList();
            }
            else if ("2".equals(choice))
            {
                getSubPricePlan();
            }
            else if ("3".equals(choice))
            {
                switchPPlan();
            }
            else if ("4".equals(choice))
            {
                getSubAuxSvcList();
            }
            else if ("5".equals(choice))
            {
                provSubAuxSvc();
            }
            else if ("6".equals(choice))
            {
                deProvSubAuxSvc();
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
            new TestSubscriberInterfaceClient(args[0]).interactive();
        }
        else if (args.length == 2)
        {
            TestSubscriberInterfaceClient client1 = new TestSubscriberInterfaceClient(args[0]);
            client1.getInfo();

            Collection c = new Vector();
            int count = Integer.parseInt(args[1]);

            for (int i = 0; i < count; ++i)
            {
                TestSubscriberInterfaceClient client = new TestSubscriberInterfaceClient(
                        args[0], client1.choice, client1.username,
                        client1.password, client1.msisdn);
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
            provisioner = (SubscriberInterface) Registry.bind(url, SubscriberInterface.class);

            do
            {
                getInfo();

                if ("1".equals(choice))
                {
                    getPPlanList();
                }
                else if ("2".equals(choice))
                {
                    getSubPricePlan();
                }
                else if ("3".equals(choice))
                {
                    switchPPlan();
                }
                else if ("4".equals(choice))
                {
                    getSubAuxSvcList();
                }
                else if ("5".equals(choice))
                {
                    provSubAuxSvc();
                }
                else if ("6".equals(choice))
                {
                    deProvSubAuxSvc();
                }
                else if ("7".equals(choice))
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

    public void switchPPlan()
    {
        try
        {
            intOut retCode = new intOut();
            try
            {
                provisioner.switchSubPricePlan(username, password, spid,
                        msisdn, imsi, Long.valueOf(newPricePlanId).longValue(), retCode);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            System.out.println("\n\tResult code ==> " + retCode.value
                    + "\t** Return Message: \" " + mapReturnCode(retCode.value)
                    + " \"");
            System.out.println("\n\n");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void provSubAuxSvc()
    {
        try
        {

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
            intOut retCode = new intOut();
            try
            {
                provisioner.provSubAuxSvcList(username, password, spid, msisdn,
                        imsi, auxServiceId, startDate_, endDate_, numPayments, retCode);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            System.out.println("\n\tResult code ==> " + retCode.value
                    + "\t** Return Message: \" " + mapReturnCode(retCode.value)
                    + " \"");
            System.out.println("\n\n");
        }
        catch (Exception e)
        {

            e.printStackTrace();
        }
    }

    public void deProvSubAuxSvc()
    {
        try
        {
            intOut retCode = new intOut();
            try
            {
                provisioner.deProvSubAuxSvcList(username, password, spid,
                        msisdn, imsi, auxServiceId, retCode);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            System.out.println("\n\tResult code ==> " + retCode.value
                    + "\t** Return Message: \" " + mapReturnCode(retCode.value)
                    + " \"");
            System.out.println("\n\n");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void getSubAuxSvcList()
    {
        try
        {
            intOut retCode = new intOut();
            try
            {
                ArrayList v = provisioner.getSubAuxSvcList(username, password,
                        spid, msisdn, imsi, retCode);
                if (v == null)
                {
                    System.out.println("\n\n\t No Aux Service found ");
                }
                else
                {
                    System.out.println("\n\n\t Size of Auxilary Service "
                            + v.size());
                    System.out.println("\n\n\t List = " + v.toString());
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            System.out.println("\n\tResult code ==> " + retCode.value
                    + "\t** Return Message: \" " + mapReturnCode(retCode.value)
                    + " \"");
            System.out.println("\n\n");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void getPPlanList()
    {
        try
        {
            intOut retCode = new intOut();
            try
            {
                ArrayList v = provisioner.getPricePlanList(username, password,
                        spid, retCode);
                if (v == null)
                {
                    System.out.println("\n\n\t No Price Plans selected ");
                }
                else
                {
                    System.out.println("\n\n\t Size of plans " + v.size());
                    System.out.println("\n\n\t List = " + v.toString());
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            System.out.println("\n\tResult code ==> " + retCode.value
                    + "\t** Return Message: \" " + mapReturnCode(retCode.value)
                    + " \"");
            System.out.println("\n\n");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void getSubPricePlan()
    {
        try
        {
            intOut retCode = new intOut();
            try
            {
                if (retCode.value == 0)
                {
                    System.out.println("\n\n\t Subscriber pricePlan ID = "
                            + provisioner.getSubPricePlan(username, password, spid,
                            msisdn, imsi, retCode));
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            System.out.println("\n\tResult code ==> " + retCode.value
                    + "\t** Return Message: \" " + mapReturnCode(retCode.value)
                    + " \"");
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
            case SubscriberInterface.SUCCESSFUL:
                msg = "Success !!";
                break;
            case SubscriberInterface.ENDDATE_LESS_THAN_STARTDATE:
                msg = "End Date is less than the Start Date, please correct the dates";
                break;
            case SubscriberInterface.FAILED_PROVISIONING:
                msg = "Provisioning Failed";
                break;
            case SubscriberInterface.INTERNAL_ERROR:
                msg = "Internal Error occured";
                break;
            case SubscriberInterface.INVALID_MSISDN:
                msg = "Invalid Msidn provided for provisioning";
                break;
            case SubscriberInterface.INVALID_PARAMETERS:
                msg = "Invalid Parameters provided";
                break;
            case SubscriberInterface.INVALID_PRICE_PLAN:
                msg = "Invalid Price Plan Id provided";
                break;
            case SubscriberInterface.STARTDATE_LESS_THAN_CURRENTDATE:
                msg = "Start date is before current date";
                break;
            case SubscriberInterface.INVALID_SUBSCRIBER_STATE:
                msg = "Invalid Subscriber state";
                break;
            case SubscriberInterface.INVALID_AUX_MSISDN_ASSOCIATION:
                msg = "Invalid auxiliary service ID and msisdn association";
                break;
            case SubscriberInterface.INVALID_AUX_SERVICE_ID:
                msg = "Invalid auxiliary service ID";
                break;
            case SubscriberInterface.INVALID_IMSI:
                msg = "Invalid IMSI";
                break;
            case SubscriberInterface.INVALID_SPID:
                msg = "Invalid SPID";
                break;
            case SubscriberInterface.LOGIN_FAILED:
                msg = "Login failed";
                break;
            case SubscriberInterface.INVALID_PAYMENT_NUMBER:
                msg = "Invalid payment number ";
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
        System.out.println("1. Get Price Plans");
        System.out.println("2. Get Subscriber Price Plan");
        System.out.println("3. Switch Subscriber Price Plan");
        System.out.println("4. Get Subscriber Auxilary Service List");
        System.out.println("5. Provision Subscriber Auxilary Service List");
        System.out.println("6. Deprovision Subscriber Auxilary Service List");
        System.out.println("7. Quit");
        System.out.print("\nEnter choice: ");
        choice = input.readLine().trim();

        if ("7".equals(choice))
        {
            System.exit(0);
        }

        if ("1".equals(choice))
        {
            System.out.print("Username: ");
            this.username = input.readLine();

            System.out.print("Password: ");
            this.password = input.readLine();

            System.out.print("SPID: ");
            this.spid = Long.valueOf(input.readLine()).longValue();
        }
        else if ("2".equals(choice))
        {
            System.out.print("Username: ");
            this.username = input.readLine();

            System.out.print("Password: ");
            this.password = input.readLine();

            System.out.print("SPID: ");
            this.spid = Long.valueOf(input.readLine()).longValue();

            System.out.print("MSISDN: ");
            this.msisdn = input.readLine().trim();

            System.out.print("IMSI: ");
            this.imsi = input.readLine();
        }
        else if ("3".equals(choice))
        {
            System.out.print("Username: ");
            this.username = input.readLine();

            System.out.print("Password: ");
            this.password = input.readLine();

            System.out.print("SPID: ");
            this.spid = Long.valueOf(input.readLine()).longValue();

            System.out.print("MSISDN: ");
            this.msisdn = input.readLine().trim();

            System.out.print("IMSI: ");
            this.imsi = input.readLine();

            System.out.print("price Plan ID : ");
            this.newPricePlanId = input.readLine().trim();

            if (newPricePlanId == null || newPricePlanId != null
                    && newPricePlanId.length() <= 0)
            {
                System.out.print("\t Price Plan Id cannot be null, Please try again");
                getInfo();
            }
        }
        else if ("4".equals(choice))
        {
            System.out.print("Username: ");
            this.username = input.readLine();

            System.out.print("Password: ");
            this.password = input.readLine();

            System.out.print("SPID: ");
            this.spid = Long.valueOf(input.readLine()).longValue();

            System.out.print("MSISDN: ");
            this.msisdn = input.readLine().trim();

            System.out.print("IMSI: ");
            this.imsi = input.readLine();
        }
        else if ("5".equals(choice))
        {
            System.out.print("Username: ");
            this.username = input.readLine();

            System.out.print("Password: ");
            this.password = input.readLine();

            System.out.print("SPID: ");
            this.spid = Long.valueOf(input.readLine()).longValue();

            System.out.print("MSISDN: ");
            this.msisdn = input.readLine().trim();

            System.out.print("IMSI: ");
            this.imsi = input.readLine();

            System.out.print("Auxilary Service ID: ");
            this.auxServiceId = Long.valueOf(input.readLine()).longValue();

            System.out.print("StareDate: dd/MM/yy: ");
            this.startDate = input.readLine().trim();

            if (!checkDate(startDate.trim()))
            {
                System.out.print("Invalid Start Date given.");
                System.out.print(" Result code ==>" + SubscriberInterface.STARTDATE_OR_ENDDATE_NULL);
                getInfo();
            }

            System.out.print("EndDate: dd/MM/yy: ");
            this.endDate = input.readLine().trim();

            if (!checkDate(endDate.trim()))
            {
                System.out.print("Invalid End Date given.");
                System.out.print(" Result code ==>" + SubscriberInterface.STARTDATE_OR_ENDDATE_NULL);
                getInfo();
            }
            System.out.print("Num of Payments: ");
            this.numPayments = Long.valueOf(input.readLine()).longValue();
        }
        else if ("6".equals(choice))
        {
            System.out.print("Username: ");
            this.username = input.readLine();

            System.out.print("Password: ");
            this.password = input.readLine();

            System.out.print("SPID: ");
            this.spid = Long.valueOf(input.readLine()).longValue();

            System.out.print("MSISDN: ");
            this.msisdn = input.readLine().trim();

            System.out.print("IMSI: ");
            this.imsi = input.readLine();

            System.out.print("Auxilary Service ID: ");
            this.auxServiceId = Long.valueOf(input.readLine()).longValue();
        }
    }

    private boolean checkDate(String date)
    {
        try
        {
            if (date != null && date.length() == 8)
            {
                StringBuilder sb = new StringBuilder();
                String day = sb.append(date.charAt(0)).append(date.charAt(1))
                        .toString().trim();
                sb = new StringBuilder();
                String mon = sb.append(date.charAt(3)).append(date.charAt(4))
                        .toString().trim();
                sb = new StringBuilder();
                String yy = sb.append(date.charAt(6)).append(date.charAt(7))
                        .toString().trim();

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

    SubscriberInterface provisioner = null;

    BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

    String url = "";

    String choice = "";

    String username = "";

    String password = "";

    String msisdn = "";

    String newPricePlanId = "";

    String startDate = "";

    String endDate = "";

    long spid = 1;

    String imsi = "";

    long auxServiceId;

    long numPayments;
}
