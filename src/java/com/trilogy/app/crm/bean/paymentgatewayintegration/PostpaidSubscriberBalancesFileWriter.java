package com.trilogy.app.crm.bean.paymentgatewayintegration;

import java.io.File;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Map;

import com.trilogy.app.crm.bean.SubscriberBalances;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * 
 * 
 * @author vijay.gote
 *
 * @since 9_7_2 
 */

public class PostpaidSubscriberBalancesFileWriter
{

	public PostpaidSubscriberBalancesFileWriter()
	{

	}

	synchronized public void init(Context ctx, String path, String fileName)  
			throws Exception
	{
		if (this.output == null )
		{	
			this.lineCount = 0;
			this.fileName  = fileName;
			this.output = new PrintWriter(new File(path + File.separator + this.fileName)); 
		}	
	}

	synchronized public void close() 
	{
		if (output != null)
		{	
			output.flush(); 
			output.close();
			output = null; 
		}	
	}


	synchronized public void printLine(Context ctx, Map<String, SubscriberBalances> subscriberBalancesMap)
	{
		if (output == null )
		{
			LogSupport.major(ctx, this, "PostpaidSubscriberBalancesFileWriter is found to be null.");
			return; 
		}
		
		String headerString = subscriberId + "," + realTimeBalance + "," + amountOwing;
		output.println(headerString);
		
		synchronized (this.output)
		{
			Iterator iterator = subscriberBalancesMap.keySet().iterator();
			while(iterator.hasNext()) 
			{
				SubscriberBalances subscriberBalance = new SubscriberBalances();
				String subscriberId = (String)iterator.next();
				subscriberBalance = (SubscriberBalances)subscriberBalancesMap.get(subscriberId);
				String outputString = subscriberId +","+subscriberBalance.getRealTimeBalance()+","+subscriberBalance.getAmountOwing()+",";
				output.println(outputString);
			}
		}
		lineCount++;
	}
	
	String fileName = ""; 
	PrintWriter output = null; 
	long lineCount = 0; 
	private static final String subscriberId = "Subscriber ID";
	private static final String realTimeBalance = "Real Time Balance";
	private static final String amountOwing = "Amount Owing";
	
}