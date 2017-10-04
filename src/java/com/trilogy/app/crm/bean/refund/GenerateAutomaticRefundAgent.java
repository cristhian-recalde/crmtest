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

package com.trilogy.app.crm.bean.refund;

import java.security.Principal;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;














import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.agent.CronConstant;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.app.crm.bean.core.AdjustmentType;
import com.trilogy.app.crm.bean.AccountReasonCodeMapping;
import com.trilogy.app.crm.bean.AccountReasonCodeMappingHome;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.bean.AdjustmentTypeActionEnum;
import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.GroupTypeEnum;
import com.trilogy.app.crm.bean.Invoice;
import com.trilogy.app.crm.bean.PayeeEnum;
import com.trilogy.app.crm.bean.RefundAdjustmentTypeMapping;
import com.trilogy.app.crm.bean.RefundAdjustmentTypeMappingHome;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.app.crm.bean.TransactionHome;
import com.trilogy.app.crm.bean.TransactionXInfo;
import com.trilogy.app.crm.calculation.support.InvoiceSupport;
import com.trilogy.app.crm.lifecycle.LifecycleAgentScheduledTask;
import com.trilogy.app.crm.log.ERLogger;
import com.trilogy.app.crm.support.AdjustmentTypeSupportHelper;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.CoreTransactionSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.app.crm.support.TransactionSupport;
import com.trilogy.app.crm.xhome.home.OcgTransactionException;
import com.trilogy.framework.core.cron.TaskEntry;
import com.trilogy.framework.core.cron.TaskHelper;
import com.trilogy.framework.lifecycle.LifecycleException;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.GTE;
import com.trilogy.framework.xhome.elang.Or;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * 
 * @author skambab
 *
 */
public class GenerateAutomaticRefundAgent extends LifecycleAgentScheduledTask {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public GenerateAutomaticRefundAgent(Context ctx, String str)
			throws AgentException {
		super(ctx, str);
		
	}
	private  Collection<AccountReasonCodeMapping> accountReasonCodeMappingCol;
	private Collection<RefundAdjustmentTypeMapping> refundAdjCollection;
	
	public void fetchRefundAdjustmentTypeCollection(Context ctx)
	{
		Home home = (Home)ctx.get(RefundAdjustmentTypeMappingHome.class);
		try
		{
			refundAdjCollection = home.selectAll();
		}catch (HomeException e)
		{
		
			e.printStackTrace();
		}
	}
	public void fetchAccountreasonCodeMapping(Context ctx)
	{
		Home home = (Home) ctx.get(AccountReasonCodeMappingHome.class);
		try
		{
			accountReasonCodeMappingCol= home.selectAll();
		} catch (HomeException e)
		{
			LogSupport.major(ctx, GenerateAutomaticRefundAgent.class, "Error occured while retieving the refund Adjustmenttype mapping objects");
			e.printStackTrace();
		}
	}
	
	
	
	@Override
	protected void start(Context ctx) throws LifecycleException, HomeException
	{
		fetchAccountreasonCodeMapping(ctx);
		fetchRefundAdjustmentTypeCollection(ctx);
		Calendar jobDate = Calendar.getInstance();
		jobrunningDate = jobDate.getTime();
		if(LogSupport.isDebugEnabled(ctx))
        {
            LogSupport.debug(ctx, this, "Job is Running for Date : " + jobDate.getTime());
        }
		TaskEntry task = TaskHelper.retrieve(ctx, CronConstant.GENERATE_AUTOMATIC_REFUNDS);
		LogSupport.debug(ctx, this, "Last Job run date : " + task.getLastRun());
		
		Home trnHome = (Home)ctx.get(TransactionHome.class);
		Home acctHome = (Home) ctx.get(AccountHome.class);			
		Or accFilter = new Or();
		accFilter.add(new EQ(AccountXInfo.STATE,AccountStateEnum.INACTIVE));
		accFilter.add(new EQ(AccountXInfo.STATE,AccountStateEnum.IN_COLLECTION));

		Collection<Account> accColl = acctHome.select(ctx, accFilter);
		try
		{
			if(!accColl.isEmpty())
			{
				for(Account acc : accColl)
				{
					account = acc;
					Invoice invoice = null;
					if(acc.getGroupType().compareTo(GroupTypeEnum.SUBSCRIBER)==0)
					{
						invoice = InvoiceSupport.getMostRecentInvoice(ctx, acc.getBAN());
					}else
					{
						if(LogSupport.isDebugEnabled(ctx))
						{
							LogSupport.debug(ctx, this, "Account group type is not subscriber" + acc);
						}
						
					}					

					if(invoice !=null)
					{
						String ban = invoice.getBAN();
						if(invoice.getTotalAmount()<0)
						{
							And trnFilter = new And();
							
							trnFilter.add(new EQ(TransactionXInfo.BAN, ban));
							trnFilter.add(new GTE(TransactionXInfo.RECEIVE_DATE, invoice.getInvoiceDate()));
								
							Collection<Transaction> trnsCol = trnHome.select(ctx, trnFilter);
							
							LogSupport.debug(ctx, this, "Transaction Collections is : " + trnsCol);
									
							if(!trnsCol.isEmpty())
							{
								for (Transaction transaction : trnsCol)
								{
									if(LogSupport.isDebugEnabled(ctx))
							        {
										LogSupport.debug(ctx, this, "Transaction object is for secondTime " + transaction);
							        }											
									
									if(AdjustmentTypeSupportHelper.get(ctx).isInCategory(ctx, transaction.getAdjustmentType(), AdjustmentTypeEnum.RefundCategory))
									{
										
										amount += transaction.getAmount();
									}
								}
							 }
									
							amount =invoice.getTotalAmount()+(amount);
									
							if(amount<0)
							{
								
								if(refundSpidLevelCfgValidations(ctx, acc, amount))
								{
									if(LogSupport.isDebugEnabled(ctx))
									{
										LogSupport.debug(ctx, this, "Amount recieved" + amount);
									}
									long trnAmount=0;
									And fil = new And();
									fil.add(new EQ(SubscriberXInfo.BAN, acc.getBAN()));
									AdjustmentType refundAdj = refundAdjustmentTypeValidation(ctx, acc);											
									if(refundAdj != null)
									{												
										Transaction refundTrns = createRefundTransaction(ctx, refundAdj, acc, amount, refundAdj.getAction(), jobDate.getTime(),trnHome);
										
										ERLogger.logForAutomaticGenrateRefundTask(ctx, acc, refundTrns, 0, "");
										
										if(LogSupport.isDebugEnabled(ctx))
										{
											LogSupport.debug(ctx, this, "Refund Transaction has been created " + refundTrns);
										}
									}									
									
								}
										
										
							 }										
							
							amount=0;
								
						}
						
					}
					
					
				}
			}
		}catch(Exception e)
		{
			if(e instanceof SQLException)
			{
				
				ERLogger.logForAutomaticGenrateRefundTask(ctx, account,null,((SQLException)e).getErrorCode(), ((SQLException)e).getMessage());
			}else if(e instanceof OcgTransactionException)
			{
				ERLogger.logForAutomaticGenrateRefundTask(ctx, account,null,((OcgTransactionException)e).getErrorCode(), ((OcgTransactionException)e).getMessage());
			}else
			{
				ERLogger.logForAutomaticGenrateRefundTask(ctx, account,null,e.hashCode(), e.getMessage());
			}
			
		}
				
	}
	
	public boolean refundSpidLevelCfgValidations(Context ctx,Account account,long amount) throws HomeException
	{
		Date scDate= account.getLastStateChangeDate();
		Date currDate = new Date();
		Calendar scCal = Calendar.getInstance();
		scCal.setTime(scDate);
		Calendar currCal = Calendar.getInstance();
		currCal.setTime(currDate);
		int diffDays = CalendarSupportHelper.get(ctx).getDifferenceInDays(scCal, currCal);
		
		CRMSpid spid = SpidSupport.getCRMSpid(ctx, account.getSpid());
		if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(this, "Spid Object receieved  : "+ spid);
        }	
		
		
		if(spid != null)
		{			
			if(spid.getRefundDelayDays()<= diffDays)
			{
				if(Math.abs(amount) < Math.abs(spid.getThresholdValue()))
				{
					lowthreshold= true;
				}else
				{
					lowthreshold=false;
				}
				return true;
			}		
		}
		
		return false;
	}
	
	public AdjustmentType refundAdjustmentTypeValidation(Context ctx,Account account) throws HomeException
	{		
		LogSupport.debug(ctx, this, "refund Adjustmenttype objects are :" + accountReasonCodeMappingCol);
				
			
		if(!refundAdjCollection.isEmpty())
		{
			for(RefundAdjustmentTypeMapping refundAdjustmentTypeMapping: refundAdjCollection )
			{
				
				if((refundAdjustmentTypeMapping.getAccountState().getIndex() == account.getState().getIndex())&&(refundAdjustmentTypeMapping.getReasonCode()==account.getStateChangeReason()))
				{
					if(lowthreshold)
					{
						return AdjustmentTypeSupportHelper.get(ctx).getAdjustmentType(ctx,refundAdjustmentTypeMapping.getLowValueCreditBalanceAdjustmentType());
					}else
					{
						return AdjustmentTypeSupportHelper.get(ctx).getAdjustmentType(ctx,refundAdjustmentTypeMapping.getHighValueCreditBalanceAdjustmentType());
					}
				}else
				{
					ERLogger.logForAutomaticGenrateRefundTask(ctx, account, null, 1, "no entity found in refundadjustmenttypemapping table");
				}
				
			}	
		}else
		{
			ERLogger.logForAutomaticGenrateRefundTask(ctx, account, null, 1, "no entity found in refundadjustmenttypemapping table");
		}		
				
		return null;
		
	}
	public Transaction createRefundTransaction(Context ctx,AdjustmentType adjustmentType,Account account,long amount,AdjustmentTypeActionEnum action,Date recievedDate,Home trnsHome) throws HomeException
	{
		String csridentifier = CoreTransactionSupportHelper.get(ctx).getCsrIdentifier(ctx);
		Transaction tran = TransactionSupport.createAccountTransaction(ctx,account, amount, 0, adjustmentType,false, false,
									csridentifier, new Date(), new Date(),	"", 0, "",0, "default","", "");
		// according to requirement we are updating tax amount to zero
		tran.setTaxAuthority(0);
		tran.setTaxPaid(0);
		Transaction transaction = (Transaction)trnsHome.create(ctx, tran);
		
		
		
		return transaction;
		
	}
	private long amount = 0;
	private Date jobrunningDate = null;
	private boolean lowthreshold = false;
	private Account account = null;
	

	
	public long getAmount() {
		return amount;
	}


}
