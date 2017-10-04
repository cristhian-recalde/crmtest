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
package com.trilogy.app.crm.provision.corba;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.omg.CORBA.ORB;

import com.trilogy.app.crm.provision.corba.ECareServicePackage.ServiceError;
import com.trilogy.app.crm.support.CorbaSupportHelper;
import com.trilogy.util.corba.CorbaClientProxy;
import com.trilogy.util.corba.CorbaClientException;

/**
 * @author lxia
 */
public class EcareServiceTestClient {
	
	
	public static void main(String[] args) throws Exception
	{

		if (args.length != 4)
		{
			System.out.println("Usage:");
			System.out.println("\tjava EcareServiceTestClient <hostname> <port> <username> <passwd>");
		}

		//EcareServiceTestClient client = new EcareServiceTestClient(); 
		
		try
		{
			ECareService service = getConnection(args[0], Integer.parseInt(args[1]), args[2], args[3]); 
			BufferedReader bin = new BufferedReader(new InputStreamReader(System.in));

			int selection;
			for (;;)
			{
				selection = selectMenu(bin);
				if (selection == 10)
				{
					break;
				}
				switch (selection)
				{
					case 0 :
						try {
						System.out.println("account number");					
						String acct = bin.readLine(); 
						System.out.println("msisdn");
						String msisdn = bin.readLine(); 
						System.out.println("adjustment Type "); 
						int type =  Integer.parseInt( bin.readLine() );
						System.out.println("amount");
						long amount = Long.parseLong(bin.readLine()); 
				        		service.acctAdjust(acct,msisdn,type, amount,new Date().toString(),"nope");
				        		System.out.println("done");
						}catch ( ServiceError error){
							System.out.println(error.reason); 
						}catch (Exception e){
							e.printStackTrace(); 
						}
						break; 
					case 1 :
						try {
						System.out.println("msisdn");
						String msisdn = bin.readLine(); 
						System.out.println("price plan "); 
						int plan =  Integer.parseInt( bin.readLine() );
						//System.out.println( "result : " + service.changePlan(msisdn, plan)); 
						//}catch ( ServiceError error){
						//	System.out.println(error.reason); 
						}catch (Exception e){
							e.printStackTrace(); 
						} 
						break;
					case 2 :
						try {
						System.out.println("msisdn");
						String msisdn = bin.readLine(); 
						System.out.println("price plane " + service.getPlan( msisdn)); 
						}catch ( ServiceError error){
							System.out.println(error.reason); 
						}catch (Exception e){
							e.printStackTrace(); 
						}
						break;
					case 3 :
						try {
						System.out.println("msisdn");
						String msisdn = bin.readLine(); 
						System.out.println("adjustment Type "); 
						int type =  Integer.parseInt( bin.readLine() );	
                        				System.out.println("csr input for adjustment amount "); 
                       					String csrInputAmt=bin.readLine(); 
						System.out.println("adjustment amount");
						long amount = Long.parseLong(bin.readLine()); 
						System.out.println("adjustment Type for service fee "); 
						int svcType =  Integer.parseInt( bin.readLine() );
                        				System.out.println("csr input for service fee "); 
                        				String csrInputSvcFee=bin.readLine();
						System.out.println("service fee amount");
						long svcAmount = Long.parseLong(bin.readLine()); 
						System.out.println(" result " + service.acctAdjustWithSvcFee(msisdn, 
								type, csrInputAmt, amount, svcType, csrInputSvcFee, svcAmount, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())));  
						}catch ( ServiceError error){
							System.out.println(error.reason); 
						}catch (Exception e){
							e.printStackTrace(); 
						}
						break;
 					case 4:
 						try {
 						System.out.println("msisdn");
 						SubscriberInfo subinf = service.getSub(bin.readLine()); 
 						System.out.println("Subscriber " + subinf.MSISDN + 
 								" state = " + subinf.state.value() +  						
 				         		" first name = " + subinf.firstName +
 								" last name = "+ subinf.lastName + 
 								" subscriber type = " + subinf.subscriberType.value() + 
 								" subscriber startdate = " + subinf.startDate +
 								" spid = " + subinf.spid);
 						}catch ( ServiceError error){
							System.out.println(error.reason); 
						}catch (Exception e){
							e.printStackTrace(); 
						}
 						break; 
					case 5:
						try {
 						System.out.println("msisdn");
 				       		System.out.println("Account state" + service.getAccountStateByMsisdn(bin.readLine()).value());
						}catch ( ServiceError error){
							System.out.println(error.reason); 
						}catch (Exception e){
							e.printStackTrace(); 
						}
						break;
					case 6 :
						try {
						System.out.println("msisdn");
						String msisdn = bin.readLine(); 
						
						System.out.println("adjustment Type "); 
						int type =  Integer.parseInt( bin.readLine() );
						
                       					 System.out.println("csr input for adjustment amount "); 
                        					String csrInputAmt=bin.readLine();
                        
						System.out.println("adjustment amount");
						long amount = Long.parseLong(bin.readLine());
						
						System.out.println("adjustment Type for service fee "); 
						int svcType =  Integer.parseInt( bin.readLine() );
						
                        				System.out.println("csr input for service fee "); 
                       					 String csrInputSvcFee=bin.readLine();
                        
						System.out.println("service fee amount");
						long svcAmount = Long.parseLong(bin.readLine());
						
						System.out.println("expiry days ext");
						int expiryDaysExt = Integer.parseInt(bin.readLine());
						
						
//						String msisdn = "8056250058";   //prepaid
//						int type=21000;//csr adjustment  credit
//						String csrInputAmt="30 to adj" ;
//						long amount=3000;
//						int svcType=25000;  //csr adjustment debit
//						String csrInputSvcFee="10 for csr";
//						int svcAmount = 1000;
//						int expiryDaysExt = 3;
						
						//System.out.println(" result " + service.acctAdjustEx(msisdn,
						//type, csrInputAmt, amount, svcType, csrInputSvcFee, svcAmount, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()),
						//expiryDaysExt,
						//0,
						//""));
						
						}catch (Exception e){
							e.printStackTrace(); 
						}
						break;
					case 7 :
						try {
						System.out.println("msisdn");
						String msisdn = bin.readLine(); 
						System.out.println("price plan "); 
						int plan =  Integer.parseInt( bin.readLine() );
						System.out.println("marketing campaign id");
						String marketingCampaignId = bin.readLine();
						System.out.println("startDate");
						String startDate = bin.readLine();
						System.out.println("endDate");
						String endDate = bin.readLine();
						//System.out.println( "result : " + service.changePlanEx(msisdn, plan,startDate,endDate,marketingCampaignId)); 
						//}catch ( ServiceError error){
						//	System.out.println(error.reason); 
						}catch (Exception e){
							e.printStackTrace(); 
						} 
						break;

					case 8:
						try {
							System.out.println("msisdn");
							String msisdn = bin.readLine(); 
							System.out.println("Subscriber Category");
							long category =  Long.parseLong( bin.readLine() );
							service.changeSubscriberCategory(msisdn, category);
 				        
						//}catch ( ServiceError error){
						//	System.out.println(error.reason); 
						}catch (Exception e){
							e.printStackTrace(); 
						}
						break;
				}
			}
		}
		catch (Exception e)
		{ 
			e.printStackTrace(); 
		}

				System.out.println("Logging out...");
				System.exit(0);

	}
	

	public static int selectMenu(BufferedReader bin) throws IOException {

		System.out.println("Please enter your choice:");
		System.out.println("0. acctAdjust");
		System.out.println("1. changPlan");
		System.out.println("2. getPlan");
		System.out.println("3. acctAdjustmentWithSvcFee");
		System.out.println("4. getSub");
		System.out.println("5. getAccountStatebyMsisdn");
		System.out.println("6. acctAdjustEx");
		System.out.println("7. changePlanEx");
		System.out.println("8. changeCategory");


		String input = bin.readLine();

		int retVal = 0;
		try {
			retVal = Integer.parseInt(input);
		}
		catch (Exception e) {
			System.out.println("Invalid input : " + input);
			System.out.println("Please try again.");
		}
		return retVal;

	}	
	
	
	static public ECareService getConnection(String hostname, int port, String user, String pwd)
	{
	      org.omg.CORBA.Object objServant = null;

	      CorbaClientProxy corbaProxy_ = null; 
	      
          ORB orb = ORB.init(new String[] { }, null);
	      corbaProxy_ = new CorbaClientProxy(
	                  hostname,
	                  port,
					  "NameService",
	                  "Redknee/App/Crm/ecareServiceFactory",
	                  true,
	                  5000,
	                  60000, null);
	            
	 
	      objServant = corbaProxy_.instance();
	      if (objServant != null)
	      {
	         try
	         {
	            // attempt to derive SubProvision
	            return ECareServiceFactoryHelper.narrow(objServant).createEcareService(
	                  user,
	                  pwd);
	         }
	         catch (Exception e)
	         {
	         }
	      }
	      
	      return null;
     }	   
}
