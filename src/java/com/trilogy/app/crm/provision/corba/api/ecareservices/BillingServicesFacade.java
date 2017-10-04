package com.trilogy.app.crm.provision.corba.api.ecareservices;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.Invoice;
import com.trilogy.app.crm.bean.Msisdn;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.pos.AccountAccumulator;
import com.trilogy.app.crm.pos.AccountAccumulatorHome;
import com.trilogy.app.crm.pos.AccountAccumulatorXInfo;
import com.trilogy.app.crm.pos.SubscriberAccumulator;
import com.trilogy.app.crm.pos.SubscriberAccumulatorHome;
import com.trilogy.app.crm.pos.SubscriberAccumulatorXInfo;
import com.trilogy.app.crm.provision.corba.api.ecareservices.billingmgmt.BillingParamID;
import com.trilogy.app.crm.provision.corba.api.ecareservices.billingmgmt.BillingParamSetHolder;
import com.trilogy.app.crm.provision.corba.api.ecareservices.billingmgmt.BillingParameter;
import com.trilogy.app.crm.provision.corba.api.ecareservices.error.ErrorCode;
import com.trilogy.app.crm.provision.corba.api.ecareservices.exception.InvoiceNotFoundException;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.calculation.service.CalculationService;
import com.trilogy.app.crm.support.MsisdnSupport;
import com.trilogy.app.crm.LicenseConstants;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.PMLogMsg;

/**
 * @author kso
 */
public class BillingServicesFacade extends EcareServices implements BillingServices
{
    public int getUsageByAcctId(final Context ctx, final String acctId, final BillingParamID[] reqSet, BillingParamSetHolder outputSet)
    {
        final PMLogMsg pmLogMsg = new PMLogMsg(AccountServicesImpl.class.getName(), "getAccountInfo");
        outputSet.value = new BillingParameter[0];
        try
        {
            if (!isLicensed(ctx, LicenseConstants.BILLING_SVC_LICENSE_KEY))
            {
                new MajorLogMsg(ctx, "Billing Service API is not inactive because of no license", null).log(ctx);

                return ErrorCode.NO_LICENSE;
            }
            
            if (!attemptRate(ctx, LicenseConstants.BILLING_SVC_LICENSE_KEY))
            {
                new MajorLogMsg(ctx, "Billing Service API: it reaches the maxium transction per second.", null).log(ctx);

                return ErrorCode.CONGESTION;
            }

        
            if (reqSet.length == 0)
            {
                return ErrorCode.REQ_PARAMS_EMPTY;
            }
        
            Account acct = null;
            try
            {
                acct = AccountSupport.getAccount(ctx, acctId);
            }
            catch(final HomeException e)
            {
                final String msg = "Got exception " + e.getMessage() + " while finding invoice by account " + acctId;
                new InfoLogMsg(this, msg, e).log(ctx);
                return ErrorCode.INTERNAL_ERROR;
           
            }

            if ( acct == null )
            {
                new InfoLogMsg(this, "Account "+acctId+" Not found", null).log(ctx);
                return ErrorCode.ACCOUNT_NOT_FOUND;
            }
            else if ( !acct.isResponsible())
            {
                new InfoLogMsg(this, "fails to retrieve the invoice because Account "+acctId+" is non-responsible", null).log(ctx);
                return ErrorCode.ACCOUNT_IS_NON_RESPONSIBLE;
            }
            else if ( acct.getSystemType().equals(SubscriberTypeEnum.PREPAID))
            {
                new InfoLogMsg(this, "fails to retrieve the invoice because Account "+acctId+" is prepaid account", null).log(ctx);
                return ErrorCode.ACCOUNT_IS_PREPAID;
            }
        
            CalculationService service = (CalculationService) ctx.get(CalculationService.class);
            final Invoice invoice = service.getMostRecentInvoice(ctx, acctId);
            final AccountAccumulator accum = getAccountAccumulator(ctx, acct);
                    
            outputSet.value = adapter_.adapt(ctx, acct, invoice,accum, reqSet);
            return ErrorCode.SUCCESS;
        }
        catch(final InvoiceNotFoundException infe)
        {
            new InfoLogMsg(this, "This account "+ acctId+" didn't generate any invoice before.", null).log(ctx);
            return ErrorCode.INVOICE_NOT_FOUND;
        }
        catch(final Exception ex)
        {
            new InfoLogMsg(this, "Got an exception while using Billing Service API, Account ID " + acctId, ex).log(ctx);
            return ErrorCode.INTERNAL_ERROR;
            
        }
        finally
        {
            pmLogMsg.log(ctx);
        }

    }
	
    public static AccountAccumulator getAccountAccumulator(Context ctx, Account acct) throws HomeException 
    {
    	AccountAccumulator accum = null;
    	Home acctAccHome = (Home) ctx.get(AccountAccumulatorHome.class);
    	if (acctAccHome != null)
    	{
    		accum = (AccountAccumulator) acctAccHome.find(ctx, new EQ(AccountAccumulatorXInfo.BAN, acct.getBAN()));
    	}
		return accum;
	}

	public int getUsageByMSISDN(Context ctx, String msisdn, BillingParamID[] reqSet, BillingParamSetHolder outputSet) 
    {
        final PMLogMsg pmLogMsg = new PMLogMsg(AccountServicesImpl.class.getName(), "getAccountInfoByMsisdn");
        outputSet.value = new BillingParameter[0];
        try
        {
            if (!isLicensed(ctx, LicenseConstants.BILLING_SVC_LICENSE_KEY))
            {
                new MajorLogMsg(ctx, "Billing Service API is not inactive because of no license", null).log(ctx);

                return ErrorCode.NO_LICENSE;
            }
            
            if (!attemptRate(ctx, LicenseConstants.BILLING_SVC_LICENSE_KEY))
            {
                new MajorLogMsg(ctx, "Billing Service API: it reaches the maxium transction per second.", null).log(ctx);

                return ErrorCode.CONGESTION;
            }

        
            if (reqSet.length == 0)
            {
                return ErrorCode.REQ_PARAMS_EMPTY;
            }
        
            Msisdn number = null;
            try
            {
                number = MsisdnSupport.getMsisdn(ctx, msisdn);
            }
            catch(final HomeException e)
            {
                final String msg = "Got exception " + e.getMessage() + " while finding msisdn " + msisdn;
                new InfoLogMsg(this, msg, e).log(ctx);
                return ErrorCode.INTERNAL_ERROR;
           
            }

            if ( number == null )
            {
                new InfoLogMsg(this, "Telephone number "+msisdn+" Not found", null).log(ctx);
                return ErrorCode.SUB_NOT_FOUND;
            }
            else if ( number.getSubscriberType() != SubscriberTypeEnum.POSTPAID)
            {
                new InfoLogMsg(this, "fails to retrieve the invoice because the msisdn "+msisdn+" is not postpaid", null).log(ctx);
                return ErrorCode.SUB_IS_PREPAID;
            }
        
            final SubscriberAccumulator accumulator = getMSISDNAccumulator(ctx, number);
                    
            outputSet.value = msisdnAdapter_.adapt(ctx, number, accumulator, reqSet);
            return ErrorCode.SUCCESS;
        }
        catch(final InvoiceNotFoundException infe)
        {
            new InfoLogMsg(this, "This subscriber "+ msisdn+" didn't generate any invoice before.", null).log(ctx);
            return ErrorCode.INVOICE_NOT_FOUND;
        }
        catch(final Exception ex)
        {
            new InfoLogMsg(this, "Got an exception while using Billing Service API, MSISDN ID " + msisdn, ex).log(ctx);
            return ErrorCode.INTERNAL_ERROR;
            
        }
        finally
        {
            pmLogMsg.log(ctx);
        }
	}

	private SubscriberAccumulator getMSISDNAccumulator(Context ctx, Msisdn number) throws HomeException 
	{
		SubscriberAccumulator accum = null;
    	Home accHome = (Home) ctx.get(SubscriberAccumulatorHome.class);
    	if (accHome != null)
    	{
    		accum = (SubscriberAccumulator) accHome.find(ctx, new EQ(SubscriberAccumulatorXInfo.MSISDN, number.getMsisdn()));
    	}
		return accum;
	}

	private BillingAdapter adapter_ = new BillingAdapter();
	private SubscriberBillingAdapter msisdnAdapter_ = new SubscriberBillingAdapter();
	

}
