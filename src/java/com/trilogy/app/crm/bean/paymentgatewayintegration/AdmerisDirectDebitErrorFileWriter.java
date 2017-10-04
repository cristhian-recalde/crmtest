package com.trilogy.app.crm.bean.paymentgatewayintegration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.MajorLogMsg;

public class AdmerisDirectDebitErrorFileWriter 
{
	public AdmerisDirectDebitErrorFileWriter(String path, String fileName)
	{
		this.errorDirectory = path;
		this.fileName  = fileName;
	}

	synchronized private void init(Context ctx )  
			throws FileNotFoundException
			{
			this.errorCount = 0;
			this.output = new PrintWriter(new File(this.errorDirectory + File.separator + this.fileName)); 
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
	
	public synchronized void writeToErrorFile(Context ctx, String record, String errorMessage) 
	{
		if(output == null)
		{
			try 
			{
				init(ctx);
			} catch (FileNotFoundException e) {

				new MajorLogMsg(DirectDebitOutboundFileProcessorLifecycleAgent.class, "FileNotFoundException encounterd while trying to write the record : " + record + " to the error file :" + fileName , e).log(ctx);
				return;
			}
		}
		
		output.println(record);
		output.println("#ERROR -> " + errorMessage);
		errorCount++;

	}


	public long getErrorCount() {
		return errorCount;
	}


	String fileName = ""; 
	PrintWriter output = null; 
	long errorCount = 0; 
	String errorDirectory = ""; 




}
