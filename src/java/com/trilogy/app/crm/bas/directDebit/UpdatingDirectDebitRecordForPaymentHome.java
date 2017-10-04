package com.trilogy.app.crm.bas.directDebit;

import java.util.Collection;

import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.DirectDebitConstants;
import com.trilogy.app.crm.bean.DirectDebitRecord;
import com.trilogy.app.crm.bean.DirectDebitRecordXInfo;
import com.trilogy.app.crm.bean.Transaction;
import com.trilogy.app.crm.support.AdjustmentTypeSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.LT;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.MinorLogMsg;

public class UpdatingDirectDebitRecordForPaymentHome 
extends HomeProxy 
{
	public UpdatingDirectDebitRecordForPaymentHome(Home home)
	{
		super(home); 
	}
	
	@Override
	public Object create(Context ctx, Object obj) throws HomeException,
			HomeInternalException 
	{
		Transaction ret = (Transaction) super.create(ctx, obj);
		
		if ( isInDirectDebitCategory(ctx,  ret) )
		{	
			updateDirectDebitRecord(ctx, ret); 
		}
		
		return ret; 
	}

	
	private boolean isInDirectDebitCategory(Context ctx,  Transaction trans)
	{	
		
		 return AdjustmentTypeSupportHelper.get(ctx).isInCategory(ctx, trans.getAdjustmentType(),		 
	            AdjustmentTypeEnum.DirectDebitPayment); 

	}
	
	private void updateDirectDebitRecord(Context ctx,  Transaction trans)
	{
		
		DirectDebitRecord record = findRecord(ctx, trans);
		
		if (record != null)
		{
			record.setReceivedAmount(trans.getAmount());
			record.setReceiveDate(trans.getTransDate()); 
			record.setReceipt(trans.getExtTransactionId()); 
			//record.setPMethodBankID(trans.getLocationCode()); 
			record.setReasonCode(DirectDebitConstants.DD_REASON_CODE_SUCCESS); 
			record.setState(DirectDebitConstants.DD_STATE_SUCCESS);
			record.setTransactionID(trans.getReceiptNum()); 
			
			try 
			{
				HomeSupportHelper.get(ctx).storeBean(ctx, record);
			} catch (Exception e)
			{
				new MinorLogMsg(this, "fail to update direct Debit record " + record.getId(), e).log(ctx); 
			}
		}
	}
	
	
	private DirectDebitRecord findRecord(Context ctx,  Transaction trans)
	{
		DirectDebitRecord ret = null; 
		
		And and = new And(); 
		
		and.add(new EQ(DirectDebitRecordXInfo.BAN, trans.getBAN()));
		and.add(new EQ(DirectDebitRecordXInfo.BILL_AMOUNT, Long.valueOf(trans.getAmount()*-1))); 
		and.add(new EQ(DirectDebitRecordXInfo.STATE, Long.valueOf(DirectDebitConstants.DD_STATE_PENDING)));
		and.add(new LT(DirectDebitRecordXInfo.POST_DATE, trans.getTransDate())); 
		
		try 
		{
			Collection<DirectDebitRecord> c = HomeSupportHelper.get(ctx).getBeans(ctx, DirectDebitRecord.class, and);
			
			for(DirectDebitRecord record : c)
			{
				if (ret == null || ret.getPostDate().after( record.getPostDate()))
				{
					ret = record; 
				}
			}
			
		} catch (Exception e)
		{
			new MinorLogMsg(this, "fail to find direct Debit record for trans to " + trans.getBAN(), e).log(ctx); 
		}
		
		return ret; 
	}
}
