package com.trilogy.app.crm.external.inBoundFile.loader;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AdjustmentTypeHome;
import com.trilogy.app.crm.bean.AdjustmentTypeXInfo;
import com.trilogy.app.crm.bean.DirectDebitConstants;
import com.trilogy.app.crm.bean.DirectDebitRecord;
import com.trilogy.app.crm.bean.DirectDebitRecordHome;
import com.trilogy.app.crm.bean.DirectDebitRecordXInfo;
import com.trilogy.app.crm.bean.FeeAndPenalty;
import com.trilogy.app.crm.bean.FeeAndPenaltyHome;
import com.trilogy.app.crm.bean.FeeAndPenaltyXInfo;
import com.trilogy.app.crm.bean.InBoundFileTransferData;
import com.trilogy.app.crm.bean.OperatorNotification;
import com.trilogy.app.crm.bean.OperatorNotificationHome;
import com.trilogy.app.crm.bean.OperatorNotificationXInfo;
import com.trilogy.app.crm.bean.PayeeEnum;
import com.trilogy.app.crm.bean.PaymentFTRecords;
import com.trilogy.app.crm.bean.PaymentFTRecordsHome;
import com.trilogy.app.crm.bean.PaymentFileAdjTypeMapping;
import com.trilogy.app.crm.bean.PaymentFileAdjTypeMappingHome;
import com.trilogy.app.crm.bean.PaymentFileAdjTypeMappingXInfo;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.bean.TransactionHome;
import com.trilogy.app.crm.bean.bank.BankHome;
import com.trilogy.app.crm.bean.core.AdjustmentType;
import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.AdjustmentTypeSupportHelper;
import com.trilogy.app.crm.support.DefaultPaymentPlanSupport;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.InboundNotificationSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.support.TransactionSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xlog.log.LogSupport;

public class InBoundFileDDRUpdateLoader extends AbstractInBoundFileDDRUpdateLoader {

	public void init()
	{
		
	}


	public Object newInstance(Context context, Object source)
	{
		return  new InBoundFileTransferData();
	}

	public void load(Context ctx, Object target,Object source) throws Exception
	{
		//check what is source object
		
		if(target instanceof InBoundFileTransferData)
		{
			 
			InBoundFileTransferData inBoundData= (InBoundFileTransferData)target;
			if (LogSupport.isDebugEnabled(ctx))
	        {
	            LogSupport.debug(ctx, InBoundFileDDRUpdateLoader.class, "triggering loader processing logic data recieved is "
	                + inBoundData);
	        }
			if(inBoundData.getRecordType().trim().equals("2"))
			{
				if (LogSupport.isDebugEnabled(ctx))
		        {
		            LogSupport.debug(ctx, InBoundFileDDRUpdateLoader.class, "triggering loader File Body logic with data "
		                + inBoundData);
		        }
				processInboundFileTransferData(ctx, inBoundData);
			}else if(inBoundData.getRecordType().trim().equals("3"))
			{
				if (LogSupport.isDebugEnabled(ctx))
		        {
		            LogSupport.debug(ctx, InBoundFileDDRUpdateLoader.class, "triggering loader File trailer logic with data "
		                + inBoundData);
		        }
				processInboundFileTrailerData(ctx, inBoundData);
			}
			
		}
	}
	private void processInboundFileTrailerData(Context ctx,InBoundFileTransferData inboundFileTransferData)
	{
		if (LogSupport.isDebugEnabled(ctx))
        {
            LogSupport.debug(ctx, InBoundFileDDRUpdateLoader.class, "processing loader File trailer logic with data "
                + inboundFileTransferData);
        }
		operatorNotification(ctx, inboundFileTransferData);
	}
	private void processInboundFileTransferData(Context ctx,InBoundFileTransferData inboundFileTransferData) throws ParseException
	{
		try {
			String accountNumber = inboundFileTransferData.getCustomerAccountNumber().replaceFirst("^0+", "");
			String bankcode=inboundFileTransferData.getBankCode().replaceFirst("^0+", "");
			String externalReceipt = inboundFileTransferData.getReceiptNumber().replaceFirst("^0+", "");
			String tpsID = inboundFileTransferData.getTpsID().replaceFirst("^0+", "");
			String fileName = inboundFileTransferData.getInternalFileName().replaceFirst("^0+", "");
			
			
			if (LogSupport.isDebugEnabled(ctx))
			{
			    LogSupport.debug(ctx, InBoundFileDDRUpdateLoader.class, "processing File Body logic bankcode recieved is "
			        + bankcode);
			}
			
			Home ddrHome = (Home) ctx.get(DirectDebitRecordHome.class);
			Home trnHome = (Home)ctx.get(TransactionHome.class);
			Home penaltyHome = (Home) ctx.get(FeeAndPenaltyHome.class);
			Home subscriberHome = (Home)ctx.get(SubscriberHome.class);
			And filter = new And();
			filter.add(new EQ(DirectDebitRecordXInfo.STATE, DirectDebitConstants.DD_STATE_PENDING));
			filter.add(new EQ(DirectDebitRecordXInfo.BAN, accountNumber));
			filter.add(new EQ(DirectDebitRecordXInfo.PMETHOD_BANK_ID, bankcode));
			Collection<DirectDebitRecord> ddrColl = ddrHome.select(ctx, filter);
			Transaction trans= null;
			if(validateMandatoryFields(ctx, ddrColl, inboundFileTransferData))
			{
				for(DirectDebitRecord ddr : ddrColl)
				{
					if (LogSupport.isDebugEnabled(ctx))
			        {
			            LogSupport.debug(ctx, InBoundFileDDRUpdateLoader.class, "processing file body logic Bank ID recieved with DDR is "
			                + ddr.getPMethodBankID());
			        }
					//total_counter+=total_counter;
					String integralPart = inboundFileTransferData.getReceiptAmountIntegralPart().replaceFirst("^0+", "");
					String decimalPart = inboundFileTransferData.getReceiptAmountDecimalPart().replaceFirst("^0+", "");
					Double amount = Double.valueOf(integralPart+"."+decimalPart);
					Integer responseCode = Integer.valueOf(inboundFileTransferData.getResponseCode().replaceFirst("^0+", ""));
					Date receiptDate = dateExatractor(ctx, inboundFileTransferData);
					Account account = AccountSupport.getAccount(ctx, accountNumber);
					String userID = inboundFileTransferData.getUserID().replaceFirst("^0+", "");
					if (LogSupport.isDebugEnabled(ctx))
			        {
			            LogSupport.debug(ctx, InBoundFileDDRUpdateLoader.class, "processing File Body logic data recieved in inbound file is"
			            		+ " IntegralPart is :" + integralPart +" Decimal Part recieved is : "+decimalPart+" total amount is :" +amount +
			            		"responseCode received is : "+ responseCode+" receipt date extracted is : "+receiptDate);
			        }
					if(responseCode==InBoundFileConstants.RESPONSE_CODE_SUCCESS)
					{						
						AdjustmentType adjustmentType = getAdjustmentTypeForSuccesPayment(ctx, inboundFileTransferData,account);
						if(adjustmentType != null)
						{
							Transaction tran = TransactionSupport.createAccountTransaction(ctx,account, (amount.longValue()*-1), 0,adjustmentType,false, false,
									fileName, receiptDate, new Date(),	"", 0, "",0, "default","", "");
							tran.setTaxPaid(0);
							tran.setAction(adjustmentType.getAction());
							tran.setTransDate(ddr.getDueDate());
							tran.setSubscriberType(account.getSystemType());
							tran.setPayee(PayeeEnum.Account);
							tran.setGLCode(adjustmentType.getGLCodeForSPID(ctx, account.getSpid()));
							tran.setTransactionMethod(ddr.getTransactionMethod());		
							trans = (Transaction)trnHome.create(ctx, tran);
							if (LogSupport.isDebugEnabled(ctx))
					        {
					            LogSupport.debug(ctx, InBoundFileDDRUpdateLoader.class, "processing File Body logic transaction has been created for success payment transaction object is"
					            		 +trans );
					        }
							createPaymentFileTrackerRecordsEntries(ctx, trans, inboundFileTransferData, account, userID, responseCode,ddr);
							updateDDREntities(ctx, ddrHome, ddr,trans,externalReceipt,DirectDebitConstants.DD_STATE_SUCCESS,DirectDebitConstants.DD_REASON_CODE_SUCCESS);
							//success+=success;
							
						}else
						{
							LogSupport.major(ctx, this, "no entity found in paymentfileAdjTypeMapping please configure respective entity");
							throw new HomeException("no entity found in paymentfileAdjTypeMapping please configure respective entity");
						}
						
					}else if(responseCode == InBoundFileConstants.RESPONSE_CODE_FAILURE)
					{
						if(penaltyHome != null)
						{
							And penaltyFilter = new And();
							And subFilter = new And();
							penaltyFilter.add(new EQ(FeeAndPenaltyXInfo.SPID, account.getSpid()));
							penaltyFilter.add(new EQ(FeeAndPenaltyXInfo.REASON_CODE, responseCode));
							
							if(tpsID != null)
							{
								penaltyFilter.add(new EQ(FeeAndPenaltyXInfo.TPS_ID, tpsID));
							}						
							FeeAndPenalty penaltyBean = (FeeAndPenalty) penaltyHome.find(ctx, penaltyFilter);
							if(penaltyBean != null)
							{
								AdjustmentType penaltyAdj = AdjustmentTypeSupportHelper.get(ctx).getAdjustmentType(ctx, penaltyBean.getPenaltyAdjustmentType());
								Transaction penaltyTran = TransactionSupport.createAccountTransaction(ctx,account, penaltyBean.getPenaltyAmount(), 0,penaltyAdj,false, false,
										fileName, new Date(), new Date(),	"", 0, "",0, "default","", "");
								penaltyTran.setTaxPaid(0);
								penaltyTran.setAction(penaltyAdj.getAction());
								penaltyTran.setSubscriberType(account.getSystemType());
								penaltyTran.setPayee(PayeeEnum.Account);
								penaltyTran.setGLCode(penaltyAdj.getGLCodeForSPID(ctx, account.getSpid()));
								penaltyTran.setTransDate(ddr.getDueDate());
								Transaction transaction = (Transaction)trnHome.create(ctx, penaltyTran);
								createPaymentFileTrackerRecordsEntries(ctx, transaction, inboundFileTransferData, account, userID, responseCode,ddr);
								updateDDREntities(ctx, ddrHome, ddr,trans,externalReceipt,DirectDebitConstants.DD_STATE_FAIL,DirectDebitConstants.DDR_PENALY_APPLIED);
								customerNotification(ctx, account, penaltyBean);
								
							}else
							{
								LogSupport.major(ctx, this, "no entity found in FeeAndPenalty please configure respective entity");
							}
						}
						
					}
									
				
				}
				
				trans = null;
			}
		} catch (HomeException e)
		{
			LogSupport.major(ctx, this, e.getMessage());
		}		
		
	}
	private void customerNotification(Context ctx,Account account,FeeAndPenalty penaltyBean)
	{
		if(penaltyBean.getNotifyCustomer())
		{
			try
			{
				Home subHome = (Home)ctx.get(SubscriberHome.class);
				And subFilter = new And();
				subFilter.add(new EQ(SubscriberXInfo.BAN, account.getBAN()));
				
				Collection<Subscriber> subColl = subHome.select(ctx, subFilter);
				
				if((!subColl.isEmpty())&&(subColl != null))
				{
					for(Subscriber sub : subColl)
					{
						InboundNotificationSupport.sendCustomerNotification(ctx, sub, penaltyBean.getCustomerNotificationTemplate());
						if (LogSupport.isDebugEnabled(ctx))
				        {
				            LogSupport.debug(ctx, InBoundFileDDRUpdateLoader.class, "Customer Notification has been sent ");
				        }
					}
				}
			}catch (HomeException e)
			{
				LogSupport.major(ctx, this, "Unable to send notification : "+ e.getMessage());
			}
		}		
	}
	private void operatorNotification(Context ctx,InBoundFileTransferData inBoundData)
	{
		String tpsID = inBoundData.getTpsID().replaceFirst("^0+", "");
		Home opHome = (Home)ctx.get(OperatorNotificationHome.class);
		if((tpsID != null)&&(!tpsID.isEmpty()))
		{
			And opFilter = new And();
			opFilter.add(new EQ(OperatorNotificationXInfo.TPS_ID,tpsID));
			try
			{
				OperatorNotification opBean = (OperatorNotification) opHome.find(ctx, opFilter);
				if((opBean != null)&&(opBean.getNotifyOperator()))
				{
					InboundNotificationSupport.sendOperatorNotification(ctx, opBean.getOperatorNotificationTemplate(), opBean.getSpid(), opBean.getOperatorEmail());
					if (LogSupport.isDebugEnabled(ctx))
			        {
			            LogSupport.debug(ctx, InBoundFileDDRUpdateLoader.class, "Operator Notification has been sent ");
			        }
				}
			}catch (HomeException e)
			{
				LogSupport.major(ctx, this, "exception occured while fetching OPERATOR NOTIFICATION data : "+ e.getMessage());
			}
			
		}
	}
	public boolean validateMandatoryFields(Context ctx,Collection<DirectDebitRecord> ddrColl,InBoundFileTransferData inboundFileTransferdata) throws HomeException
	{
		String paymentMethod = inboundFileTransferdata.getPaymentMethod().replaceFirst("^0+", "");
		String bankcode=inboundFileTransferdata.getBankCode().replaceFirst("^0+", "");
		String userID = inboundFileTransferdata.getUserID().replaceFirst("^0+", "");
		Integer responseCode = Integer.valueOf(inboundFileTransferdata.getResponseCode().replaceFirst("^0+", ""));
		Home recordHome = (Home) ctx.get(PaymentFTRecordsHome.class);
		if (LogSupport.isDebugEnabled(ctx))
        {
            LogSupport.debug(ctx, InBoundFileDDRUpdateLoader.class, "processing File Body logic data recieved in inbound file is"
            		+" paymentMethod :" +paymentMethod+" bankcode : "+bankcode+" userID : "+userID);
        }
		if((ddrColl.isEmpty()) ||(ddrColl.size()==0))
		{
			PaymentFTRecords recordbean = createPaymentFileTrackerRecordsEntries(ctx, null, inboundFileTransferdata, null, userID, responseCode,null);
			recordbean.setParsingStatus(InBoundFileConstants.INVALID_BAN_BANK_ID);
			if(recordHome != null)
			{
				recordHome.store(ctx, recordbean);
				return false;
			}else
			{
				return false;
			}
			
		}
		
		return true;
		
		
	}
	public PaymentFTRecords createPaymentFileTrackerRecordsEntries(Context ctx,Transaction transaction,InBoundFileTransferData inboundFileTransferData,Account account,String userID,Integer responseCode,DirectDebitRecord ddr) throws HomeException
	{
		Home home = (Home) ctx.get(PaymentFTRecordsHome.class);
		String accountNumber = inboundFileTransferData.getCustomerAccountNumber().replaceFirst("^0+", "");
		String bankCode = inboundFileTransferData.getBankCode().replaceFirst("^0+", "");
		String fileName = inboundFileTransferData.getInternalFileName().replaceFirst("^0+", "");
		String paymentType = inboundFileTransferData.getPaymentType().replaceFirst("^0+", "");
		if (LogSupport.isDebugEnabled(ctx))
        {
            LogSupport.debug(ctx, InBoundFileDDRUpdateLoader.class, "processing File Body logic "
            		+ " creating PaymentFIleTrackerRecords entities bank code recieved is : "+ bankCode +" paymentTYpe recieved is :"
            		 +paymentType );
        }
		PaymentFTRecords ftrecord = new PaymentFTRecords();	
		if(account != null)
		{
			ftrecord.setSpid(account.getSpid());
			ftrecord.setBan(account.getBAN());
		}else
		{
			ftrecord.setBan(accountNumber);
		}
		
		if(transaction != null)
		{
			ftrecord.setTransactionID(transaction.getReceiptNum());
			ftrecord.setPaymentAmount(transaction.getAmount());
			ftrecord.setAdjustmentType(transaction.getAdjustmentType());
			ftrecord.setTransactionDtae(transaction.getTransDate());
		}
		if(fileName != null)
		{
			ftrecord.setTpsFileName(fileName);
		}
		ftrecord.setAgent(userID);
		ftrecord.setLocationCode(Long.valueOf(bankCode));
		ftrecord.setPaymentType(paymentType);
		ftrecord.setExtResponceCode(responseCode.toString());
		if(ddr != null)
		{
			ftrecord.setRawRecord(ddr);
		}
		PaymentFTRecords recordBean = (PaymentFTRecords) home.create(ctx, ftrecord);
		
		if (LogSupport.isDebugEnabled(ctx))
        {
            LogSupport.debug(ctx, InBoundFileDDRUpdateLoader.class, "processing File Body logic "
            		+ " PaymentFIleTrackerRecords entity has been created + "
            		 +recordBean );
        }
		return recordBean;
	}
	
	private void updateDDREntities(Context ctx,Home home,DirectDebitRecord ddr,Transaction trans,String externalReceipt,int state,int responseCode) throws HomeException
	{
		
		if(ddr != null)
		{
			if (LogSupport.isDebugEnabled(ctx))
	        {
	            LogSupport.debug(ctx, InBoundFileDDRUpdateLoader.class, "processing File Body logic "
	            		+ " going to update DDR recieved  : "+ ddr);
	        }
			
			ddr.setState(state);
			ddr.setReasonCode(responseCode);
			if(trans != null)
			{
				ddr.setTransactionID(trans.getReceiptNum());
				ddr.setReceivedAmount(trans.getAmount());
				ddr.setReceiveDate(trans.getReceiveDate());
				ddr.setReceipt(externalReceipt);
			}
			
			home.store(ctx, ddr);
			
		}
	}
	private Date dateExatractor(Context ctx,InBoundFileTransferData inboundFileTransferData)  throws ParseException
	{
		String receiptDate = inboundFileTransferData.getReceiptDate().replaceFirst("^0+", "");
		Date formattedreceiptDate = new SimpleDateFormat("dd/mm/yyyy hh:mm:ss").parse(receiptDate);
		return formattedreceiptDate;
	}
	
	private AdjustmentType getAdjustmentTypeForSuccesPayment(Context ctx,InBoundFileTransferData inboundFileTransferData,Account account) throws HomeException
	{
		String bankCode = inboundFileTransferData.getBankCode().replaceFirst("^0+", "");
		String paymentType = inboundFileTransferData.getPaymentType().replaceFirst("^0+", "");
		String paymentMethod = inboundFileTransferData.getPaymentMethod().replaceFirst("^0+", "");
		if (LogSupport.isDebugEnabled(ctx))
        {
            LogSupport.debug(ctx, InBoundFileDDRUpdateLoader.class, "processing File Body logic fetching adjustment type from paymentfileAdjTypeMapping"
            		+ " Bankcode is :" + bankCode +" paymenttype recieved is : "+paymentType+" payment method is :" +paymentMethod );
        }
		Home adjPaymentHome = (Home) ctx.get(PaymentFileAdjTypeMappingHome.class);
		Home bankHome = (Home) ctx.get(BankHome.class);
		Home adjHome = (Home) ctx.get(AdjustmentTypeHome.class);
		And filter = new And();
		filter.add(new EQ(PaymentFileAdjTypeMappingXInfo.SPID,account.getSpid()));
		filter.add(new EQ(PaymentFileAdjTypeMappingXInfo.BANK_CODE,bankCode));
		filter.add(new EQ(PaymentFileAdjTypeMappingXInfo.PAYMENT_METHOD,Integer.valueOf(paymentMethod)));
			
		
		PaymentFileAdjTypeMapping adjTypeBean = (PaymentFileAdjTypeMapping) adjPaymentHome.find(ctx, filter);
		if(adjTypeBean != null)
		{
			return AdjustmentTypeSupportHelper.get(ctx).getAdjustmentType(ctx, adjTypeBean.getPenaltyAdjustmentType());
		}else
		{
			return null;
		}
		
	}
	/*public static int total_counter =0;
	public static int success = 0;
	public static int failure = 0;*/
}
