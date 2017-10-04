package com.trilogy.app.crm.provision.corba.api.ecareservices;

import java.util.Date;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.provision.corba.api.ecareservices.acctmgmt.AccountParamID;
import com.trilogy.app.crm.provision.corba.api.ecareservices.acctmgmt.AccountParamSetHolder;
import com.trilogy.app.crm.provision.corba.api.ecareservices.acctmgmt.AccountParameter;
import com.trilogy.app.crm.provision.corba.api.ecareservices.error.ErrorCode;
import com.trilogy.app.crm.provision.corba.api.ecareservices.exception.AccountNotFoundException;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.LicenseConstants;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.PMLogMsg;

public class AccountServicesFacade extends EcareServices
{

    /*
     * To retrieve the account. @param msisdn @param reqSet @param outputSet
     */
    public int getAccountInfoByMsisdn(Context ctx, String msisdn, AccountParamID[] reqSet,
            AccountParamSetHolder outputSet)
    {
        final PMLogMsg pmLogMsg = new PMLogMsg(AccountServicesImpl.class.getName(), "getAccountInfoByMsisdn");
        outputSet.value = new AccountParameter[0];
        try
        {
            if (!isLicensed(ctx, LicenseConstants.ACCT_SVC_LICENSE_KEY))
            {
                new MajorLogMsg(ctx, "Billing Service API is not inactive because of no license", null).log(ctx);

                return ErrorCode.NO_LICENSE;
            }
            
            if (!attemptRate(ctx, LicenseConstants.ACCT_SVC_LICENSE_KEY))
            {
                new MajorLogMsg(ctx, "Account Service API: it reaches the maxium transction per second.", null).log(ctx);

                return ErrorCode.CONGESTION;
            }

            if (reqSet.length == 0)
            {

                return ErrorCode.REQ_PARAMS_EMPTY;
            }

            Account acct = null;
            Subscriber sub = null;
            try
            {
                sub = SubscriberSupport.lookupSubscriberForMSISDNLimited(ctx, msisdn, new Date());
                if (sub != null)
                {
                    acct = sub.getAccount(ctx);
                }
            } catch (HomeException e)
            {
                String msg = "Got exception " + e.getMessage() + " while finding account by msisdn " + msisdn;
                new InfoLogMsg(this, msg, e).log(ctx);
                return ErrorCode.INTERNAL_ERROR;
            }

            outputSet.value = adapter.adapt(ctx, acct, sub, reqSet);

            return ErrorCode.SUCCESS;
        } 
        catch (AccountNotFoundException anfe)
        {
            new InfoLogMsg(this, "Responsible Account Not Found by msisdn " + msisdn, anfe).log(ctx);
            return ErrorCode.ACCOUNT_NOT_FOUND;
        } 
        catch (Exception ex)
        {
            new InfoLogMsg(this, "Got an exception while using Account Service API, msisdn " + msisdn, ex).log(ctx);
            return ErrorCode.INTERNAL_ERROR;
        } 
        finally
        {
            pmLogMsg.log(ctx);
        }

    }

    private AccountAdapter adapter = new AccountAdapter();

}
