package com.trilogy.app.crm.bas.directDebit;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.trilogy.app.crm.CoreCrmLicenseConstants;
import com.trilogy.app.crm.calculation.support.BankSupport;
import com.trilogy.app.crm.bas.SpidAwareAgent;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.DirectDebitConstants;
import com.trilogy.app.crm.bean.DirectDebitRecord;
import com.trilogy.app.crm.bean.DirectDebitRecordHome;
import com.trilogy.app.crm.bean.DirectDebitRecordXInfo;
import com.trilogy.app.crm.bean.GeneralConfig;
import com.trilogy.app.crm.bean.bank.Bank;
import com.trilogy.app.crm.bean.bank.DDROutputWriterType;
import com.trilogy.app.crm.bean.bank.DDROutputWriterTypeXInfo;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.LicensingSupportHelper;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.LTE;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;

public class CreateDirectDebitRequestService 
extends SpidAwareAgent
implements DirectDebitOutputWriterFactory
{
	public static final String AGENT_NAME = "DirectDebitOutBoundRequest";
	
	public static final String AGENT_DISCRIPTION = "Create Direct Debit Out Bound file";
	
	public CreateDirectDebitRequestService()
	{
		this.setTaskName(AGENT_NAME);
	}
	
	public void processSpid(final Context ctx, final CRMSpid spid)
	{
	        final Home home = (Home) ctx.get(DirectDebitRecordHome.class);

	        And and = new And(); 
	        
	        and.add(new EQ(DirectDebitRecordXInfo.SPID, Integer.valueOf(spid.getId()))); 
	        and.add(new EQ(DirectDebitRecordXInfo.STATE, Long.valueOf(DirectDebitConstants.DD_STATE_CREATE))); 
	        and.add(new LTE(DirectDebitRecordXInfo.REQUEST_DATE, CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(new Date()))); 
	        GeneralConfig config = (GeneralConfig) ctx.get(GeneralConfig.class); 
	        int threadCount = 5; 
	        
	        if (config != null)
	        {
	        	threadCount = config.getDDROutBoundThreads();
	        }
	        
	        EnhancedParallVisitor visitor = new EnhancedParallVisitor(threadCount, new DirectDebitRecordOutputVisitor(this) ); 
	        try
	        {
	            home.where(ctx, and).forEach(ctx, visitor);
	        }
	        catch (final HomeException e)
	        {
	         
	        }finally
	        {
	        	 try
	             {
	        		 visitor.shutdown(EnhancedParallVisitor.TIME_OUT_FOR_SHUTTING_DOWN);
	             }
	             catch (final Exception e)
	             {
	                 LogSupport.minor(ctx, this, "exception catched during wait for completion of all Direct debit threads", e);
	             }
	        }
	        
	        closeWriters();
	}
	

	
	
	private void closeWriters()
	{
		for(DirectDebitOutputWriter writer : ddrWriters.values() )
		{
			writer.close(); 
		}
		
		this.ddrWriters = new HashMap<String, DirectDebitOutputWriter>(); 
	}


	@Override
	synchronized public DirectDebitOutputWriter getWriter(Context ctx,
			DirectDebitRecord record) 
	{
		String key = record.getPMethodBankID()+ "-" +record.getSpid(); 
		
		if (ddrWriters.containsKey(key))
		{
			return ddrWriters.get(key);
		}
		
		DirectDebitOutputWriter writer = createWriter(ctx, record);
		if (writer != null)
		{
			ddrWriters.put(key, writer); 
		}

		return writer;
	}
	
	
	private DirectDebitOutputWriter createWriter(Context ctx, DirectDebitRecord record)
	{
		
		try
		{
			
			Bank bank = BankSupport.getBank(ctx, record.getPMethodBankID(), record.getSpid());
			
			if (bank == null)
			{
				throw new Exception("can not find bank " + record.getPMethodBankID()); 
			}
			DDROutputWriterType type = HomeSupportHelper.get(ctx).findBean(ctx, DDROutputWriterType.class, 
				new EQ(DDROutputWriterTypeXInfo.ID, Integer.valueOf(bank.getOutBoundFileWriter())));
			DirectDebitOutputWriter writer = (DirectDebitOutputWriter)XBeans.instantiate(type.getDDRWriterClassName(), ctx);
			
			if (writer != null)
			{
				writer.init(ctx, bank.getOutBoundDirectory(),bank.getBankCode(), bank.getOutBoundFileExtension(), record.getSpid()); 
			}
			
			return writer; 
		} catch (Exception e)
		{
			new MajorLogMsg(this, "fail to get direct debit writer for bank " + record.getPMethodBankID(), e).log(ctx); 
		}
		return null; 
	}
	
	
	public boolean isLicensed(Context ctx)
	{
	    return LicensingSupportHelper.get(ctx).isLicensed(ctx, CoreCrmLicenseConstants.DIRECTDEBIT_LICENSE); 
	}
	
	public Map<String, DirectDebitOutputWriter> ddrWriters = new HashMap<String, DirectDebitOutputWriter>(); 
}
