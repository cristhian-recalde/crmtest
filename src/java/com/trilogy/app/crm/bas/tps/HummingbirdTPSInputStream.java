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
package com.trilogy.app.crm.bas.tps;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.trilogy.app.crm.bean.TPSConfig;
import com.trilogy.app.crm.tps.pipe.TPSPipeConstant;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

/**
 * @author lxia
 */
public class HummingbirdTPSInputStream {
	Context context; 
	InputStream in; 
	String last_line = null; 
	String prefix = ""; 
	protected int lineCount = 0;
	
	// FIX ME: WHAT THE HECK IS THIS  --KEN 
	private final DateFormat dataformat_ = new SimpleDateFormat("yyMMdd");
	public final static String DATA_FORMAT = "yyMMdd";

    public HummingbirdTPSInputStream(Context ctx, InputStream inputStream)
    {
        super();
        
        //FIX ME: WHAT THE HECK IS THIS  --KEN 
        dataformat_.setLenient(false); 
        this.context = ctx; 
        this.reader = 
            new CommentSkippingReader(new BufferedReader(new InputStreamReader(inputStream)));
    	this.in = inputStream; 
    }

	/**
	 *  Verify the file header
 	 */

	public boolean verifyHeader() {
		return true; 
	}

	/**
	 * close the stream
	 *
	 */
	public void close() 
	throws IOException 
	{ 
		in.close(); 
	}
	
	/**
	 * read a TPS record from input stream
	 * @param ctx
	 * 		   a Context
	 * @return
	 * @throws Exception
	 */
	public TPSRecord readTps(Context ctx) throws Exception{
		
		
		String line = reader.readLine();
				
		if ( line == null )
			throw new EOFException("the end of file"); 
		lineCount ++;

		
		ctx.put(TPS_RAW_RECORD_ENTRY, line); 
		TPSRecord tps = new TPSRecord();
		tps.setTpsInitial(prefix); 
		tps.setRawline(line); 
		ctx.put(TPSRecord.class, tps); 
				
					
		int pstart = 0; 
		try {
		
			int externalTransactionLength= getTPSExternalTransactionLength(ctx);
			int accountNumberLength = getTPSAccountNumberLength(ctx);
			
			
			int tpsDynamicLength = externalTransactionLength + accountNumberLength;
            // Check to see that the record has the correct number of characters (taking into account that the PAYMENT_Type and void_id field is optional)
            // if the number of characters is in between the two expected number of characters then we will simply ignore it
            if (line.trim().length() < TPS_TOTAL_STATIC_CHARS + tpsDynamicLength  || line.trim().length() > TPS_TOTAL_STATIC_CHARS + tpsDynamicLength + 
            		TPS_FIELD_PAYMENT_TYPE + TPS_FIELD_VOID_ID)
            {
                throw new InvalidTPSRecordException("Invalid record length at line number [lineCount=" + lineCount + ",lineLength=" + line.trim().length() + "]");
            }
			
			tps.setCashierNum(line.substring( pstart, pstart + TPS_FIELD_CASHIER_NUMBER).trim() );
 			pstart += TPS_FIELD_CASHIER_NUMBER; 
		
  			tps.setPaymentDate( formatDate( line.substring( pstart, pstart+ TPS_FIELD_PAYMENT_DATE).trim())); 
 			pstart += TPS_FIELD_PAYMENT_DATE;
		
 			String txt = line.substring(pstart, pstart+accountNumberLength).trim(); 
 			if ( !txt.startsWith("S"))
 				tps.setAccountNum( txt);
 			else 
 				tps.setTelephoneNum(txt.substring(1)); 
			pstart += accountNumberLength;

			tps.setAmount( Integer.parseInt ( line.substring( pstart, pstart+ TPS_FIELD_AMOUNT).trim())); 
			pstart += TPS_FIELD_AMOUNT; 		

			tps.setTransactionNum( line.substring(pstart, pstart+externalTransactionLength).trim());
			pstart += externalTransactionLength;

			if ( line.trim().length() >= TPS_TOTAL_STATIC_CHARS + tpsDynamicLength + TPS_FIELD_PAYMENT_TYPE + TPS_FIELD_VOID_ID ){

				tps.setPaymentType(line.substring(pstart, pstart + TPS_FIELD_PAYMENT_TYPE).trim());
				pstart += TPS_FIELD_PAYMENT_TYPE; 
  			
				String voidString = line.substring(pstart, pstart + TPS_FIELD_VOID_ID);
				if (voidString.equalsIgnoreCase("Y"))
				{
					tps.setVoidFlag(true);
				}
				else
				{
					tps.setVoidFlag(false);
				}
				pstart += TPS_FIELD_VOID_ID;
  			  			
			}
  			
		} catch ( Exception e){
			throw new InvalidTPSRecordException(e.getMessage()); 
		}
   		return tps; 
 	}	   
 
 	/**
 	 * format a date from a string
 	 * @param d
 	 * 		  String
 	 * @return Date
 	 * @throws Exception
 	 */
	public Date formatDate(String d) throws Exception
	{
	   synchronized (dataformat_)
	   {
		  return dataformat_.parse(d);
	   }
	}	 


	private boolean checkIfLastLine(String line, int curCount)
	{
		try
		{
			int count = Integer.parseInt(line.substring(0,4));
			if (count == curCount)
			{
				return true;
			}
			
			return false;
		}
		catch (NumberFormatException ne)
		{
			return false;
		}
	}
	
    public static int getTPSExternalTransactionLength(Context ctx) throws HomeException
    {
        TPSConfig config = (TPSConfig) ctx.get(TPSConfig.class);
        if (config == null)
        {// Won't happen
            throw new HomeException("TPS Config not found in COntext.");
        }
        else
        {
            return config.getTPSExternalTransactionLength();
        }
    }
    
    public static int getTPSAccountNumberLength(Context ctx) throws HomeException
    {
        TPSConfig config = (TPSConfig) ctx.get(TPSConfig.class);
        if (config == null)
        {// Won't happen
            throw new HomeException("TPS Config not found in COntext.");
        }
        else
        {
            return config.getTPSAccountNumberLength();
        }
    }
	 
    public static final int TPS_RECORD_LENGTH_FULL = 45;
    public static final int TPS_RECORD_LENGTH_OPTIONAL = 41; 

    
    private CommentSkippingReader reader;
	public static final int TPS_FIELD_CASHIER_NUMBER		= 3; 
	public static final int TPS_FIELD_PAYMENT_DATE		 	= 6;
	public static final int TPS_FIELD_ACCOUNT_NUMBER		= 12;
	public static final int TPS_FIELD_AMOUNT				= 10;
	public static final int TPS_FIELD_PAYMENT_NUMBER		= 10;
	public static final int TPS_FIELD_PAYMENT_TYPE			= 3; 
   	public static final int TPS_FIELD_VOID_ID				= 1; 
	//public static final int TPS_FIELD_EXTRA
   	
    public static final int TPS_TOTAL_STATIC_CHARS = 
    		TPS_FIELD_CASHIER_NUMBER
    		+ TPS_FIELD_PAYMENT_DATE
    		+ TPS_FIELD_AMOUNT; 

    public static final String TPS_RAW_RECORD_ENTRY = "TPS_RAW_RECORD_ENTRY"; 

}
