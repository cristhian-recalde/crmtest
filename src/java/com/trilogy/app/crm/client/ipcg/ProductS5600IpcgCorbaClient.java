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
package com.trilogy.app.crm.client.ipcg;

import java.util.regex.Pattern;

import org.omg.CORBA.BooleanHolder;
import org.omg.CORBA.IntHolder;
import org.omg.CORBA.StringHolder;

import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidHome;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.client.AbstractExtendedCrmClient;
import com.trilogy.app.crm.support.ExternalAppSupport;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.log.PMLogMsg;
import com.trilogy.product.s5600.ipcg.provisioning.Gender;
import com.trilogy.product.s5600.ipcg.provisioning.ResponseCode;
import com.trilogy.product.s5600.ipcg.provisioning.SubscriberProfile;
import com.trilogy.product.s5600.ipcg.provisioning.SubscriberProfileHolder;
import com.trilogy.product.s5600.ipcg.provisioning.SubscriberProv;
import com.trilogy.product.s5600.ipcg.provisioning.SubscriberState;


/**
 * IPCG client.
 *
 * @author daniel.zhang@redknee.com
 */
public abstract class ProductS5600IpcgCorbaClient<T extends SubscriberProv> extends AbstractExtendedCrmClient<SubscriberProv, T> implements ProductS5600IpcgClient<T>
{
    /**
     * Create a new instance of <code>ProductS5600IpcgClient</code>.
     *
     * @param ctx
     *            The operating context.
     * @param propertiesKey
     *            The CORBA client properties key.
     */
    public ProductS5600IpcgCorbaClient(final Context ctx, String serviceName, String description, final Class<T> classInterface)
    {
        super(ctx, serviceName, description, classInterface);
    }
    

    /**
     * Make sure null strings are not used by returning an empty string in place of a null
     * string.
     *
     * @param in
     *            Input string.
     * @return If the input string is null, returns an empty string. Otherwise, return the
     *         input string.
     */
    private static String checkNullString(final String in)
    {
        if (in == null)
        {
            return "";
        }
        return in;
    }


    /**
     * Determines if the MSISDN provided is in a valid MSISDN pattern.
     *
     * @param msisdn
     *            MSISDN being examined.
     * @return Returns <code>true</code> if the MSISDN is in a valid pattern,
     *         <code>false</code> otherwise.
     */
    private static boolean isValidMsisdn(final String msisdn)
    {
        if (msisdn == null)
        {
            return false;
        }
        return MSISDN_PATTERN.matcher(msisdn).matches();
    }


    /**
     * Converts a CRM subscriber state into an IPCG subscriber state.
     *
     * @param sub
     *            CRM subscriber.
     * @return The IPCG subscriber state corresponding to the CRM subscriber state.
     */
    private SubscriberState convertState(final Context ctx, final Subscriber sub)
    {
        SubscriberState state;
        switch (sub.getState().getIndex())
        {
            case SubscriberStateEnum.INACTIVE_INDEX:
                state = SubscriberState.INACTIVE;
                break;

            /*
             * TT 8011600037: Subscriber should be disabled on IPCG if it's in barred
             * state.
             */
            case SubscriberStateEnum.LOCKED_INDEX:
                // prepaid only
                state = SubscriberState.INACTIVE;
                break;

            case SubscriberStateEnum.AVAILABLE_INDEX:
                // prepaid only
                state = SubscriberState.ACTIVE;
                break;

            case SubscriberStateEnum.EXPIRED_INDEX:
                // prepaid only
                state = SubscriberState.ACTIVE;
                break;

            case SubscriberStateEnum.SUSPENDED_INDEX:
                // prepaid and postpaid
                state = SubscriberState.INACTIVE;
                break;

            default:
                state = SubscriberState.ACTIVE;
        }

        return state;
    }


    /**
     * Adds a new subscriber.
     *
     * @param sub
     *            CRM subscriber to be added.
     * @param billingCycleDate
     *            Bill cycle day.
     * @param timeZone
     *            Time zone of the subscriber.
     * @param ratePlan
     *            Data rate plan of the subscriber.
     * @param scpId
     *            SCP ID.
     * @param subBasedRatingEnabled
     *            Whether subscriber-based rating is enabled.
     * @param serviceGrade
     *            Service grade.
     * @return Result code of the CORBA call.
     * @throws IpcgSubProvException
     *             Thrown if there are problems communicating with IPCG CORBA service.
     */
    public int addSub(final Context ctx, final Subscriber sub, final short billingCycleDate, final String timeZone, final int ratePlan,
        final int scpId, final boolean subBasedRatingEnabled, final int serviceGrade) throws IpcgSubProvException
    {
        final PMLogMsg pmLogMsg = new PMLogMsg(getPMModule(), "addSub()");
        if (!isValidMsisdn(sub.getMSISDN()))
        {
            throw new IpcgSubProvException("MSISDN '" + sub.getMSISDN() + "' is invalid");
        }

        int result = ExternalAppSupport.NO_CONNECTION;
        final SubscriberProv service = getService();

        if (service==null)
        {
            throw new IpcgSubProvException("NGRC down or not available.");
        }

        final SubscriberProfile subProfile = new SubscriberProfile();

        subProfile.msisdn = checkNullString(sub.getMSISDN());
        subProfile.imsi = checkNullString(sub.getIMSI());
        /*
         * TODO [2007-01-17] use subscriber's province?
         */
        subProfile.province = "ON";
        subProfile.billingAccountNum = sub.getBAN();
        if (checkNullString(sub.getBillingLanguage()).equals(""))
        {
            subProfile.language = "EN";
        }
        else
        {
            subProfile.language = sub.getBillingLanguage();
        }
        subProfile.timeZone = checkNullString(timeZone);
        subProfile.spid = sub.getSpid();
        subProfile.billingCycleDate = billingCycleDate;
        subProfile.ratePlan = ratePlan;
        subProfile.scpId = scpId;
        subProfile.subBasedRatingEnabled = subBasedRatingEnabled;
        subProfile.packagePlanId = new int[0];
        subProfile.socName = new String[0];
        subProfile.state = convertState(ctx, sub);
        subProfile.serviceGrade = serviceGrade;
        subProfile.gender = Gender.UNKNOWN;
        // TODO 2008-08-21 date of birth no longer part of subscriber
        //if (sub.getDateOfBirth() == null)
        //{
            subProfile.birthDate = "20050101";
        //}
        //else
        //{
        //    subProfile.birthDate = new SimpleDateFormat(DATE_FORMAT_STRING).format(sub.getDateOfBirth());
        //}
            
        if (LogSupport.isDebugEnabled(getContext()))
        {
           new DebugLogMsg(this, "addSub() sending=" + getSubscriberProfileString(subProfile), null).log(getContext());
        } 

        final StringHolder reason = new StringHolder();
        try
        {
            result = service.addSub(subProfile, reason, new IntHolder());
        }
        catch (final org.omg.CORBA.COMM_FAILURE commFail)
        {
            new MinorLogMsg(this, "Fail to add new subscriber " + sub.getMSISDN(), commFail).log(getContext());
            throw new IpcgSubProvException("Communication error: Request to NGRC failed.", ExternalAppSupport.COMMUNICATION_FAILURE);
        }
        catch (final Exception e)
        {
            new MinorLogMsg(this, "Fail to add new subscriber " + sub.getMSISDN(), e).log(getContext());
            throw new IpcgSubProvException("General failure on NGRC.", ExternalAppSupport.REMOTE_EXCEPTION);
        }
        pmLogMsg.log(getContext());
        return result;
    }

    /**
     * Update subscriber.
     *
     * @param subProfile
     *            Subscriber profile.
     * @param reason
     *            Reason of the update.
     * @param tag
     *            Tag of the update.
     * @return Result code from IPCG CORBA service.
     * @deprecated Pascal from data team suggested to use the
     *             {@link #addChangeSub(SubscriberProfile)} instead.
     */
    @Deprecated
    public int updateSub(final SubscriberProfile subProfile, final StringHolder reason, final IntHolder tag)
    {
        final PMLogMsg pmLogMsg = new PMLogMsg(getPMModule(), "updateSub()");
        // TODO: need impl?
        new DebugLogMsg(this, "Empty method updateSub() called", null).log(getContext());
        pmLogMsg.log(getContext());
        return 0;
    }


    /**
     * Changes the subscriber's bill cycle day. If the subscriber does not exist on IPCG,
     * create the subscriber with the new bill cycle day instead.
     *
     * @param sub
     *            The subscriber to be updated.
     * @param billingCycleDate
     *            New bill cycle day.
     * @return Result code of IPCG CORBA service.
     * @throws IpcgSubProvException
     *             Thrown if there are problems connecting to IPCG CORBA service.
     */
    public int addChangeSubBillCycleDate(final Subscriber sub, final short billingCycleDate)
        throws IpcgSubProvException

    {
        final SubscriberProfileHolder subProfileHolder = new SubscriberProfileHolder();
        subProfileHolder.value = new SubscriberProfile();

     // UMP-3348: Allow Data service for broadband and voice for wireline subscriptions
        // hack fix to support Multiplay capability, as legacy interfaces did not consider subscription type other than AIRTIME
        // msisdn|subscriptionType will be passed to URCS
		
		String paramGetSub = null;
		if(allowMultiSubPerAccount(getContext(), sub))
		{
			paramGetSub = sub.getMsisdn()+"|"+sub.getSubscriptionType();
		}
		else
		{
			paramGetSub = sub.getMsisdn();
		}
        
        getSub(paramGetSub, subProfileHolder);
        
        subProfileHolder.value.billingCycleDate = billingCycleDate;
        return addChangeSub(subProfileHolder.value);
    }


    /**
     * Updates the subscriber. If the subscriber does not exist on IPCG, create the
     * subscriber with the new parameters instead.
     *
     * @param sub
     *            The subscriber to be updated.
     * @param billingCycleDate
     *            New bill cycle day.
     * @param ratePlan
     *            IPCG rate plan.
     * @param serviceGrade
     *            IPCG service grade.
     * @return Result code of IPCG CORBA service.
     * @throws IpcgSubProvException
     *             Thrown if there are problems connecting to IPCG CORBA service.
     */
    public int addChangeSub(final Context ctx, final Subscriber sub, final short billingCycleDate, final int ratePlan,
        final int serviceGrade) throws IpcgSubProvException
    {
        final SubscriberProfileHolder subProfileHolder = new SubscriberProfileHolder();
        subProfileHolder.value = new SubscriberProfile();
        
     // UMP-3348: Allow Data service for broadband and voice for wireline subscriptions
        // hack fix to support Multiplay capability, as legacy interfaces did not consider subscription type other than AIRTIME
        // msisdn|subscriptionType will be passed to URCS
		
		String paramGetSub = null;
		if(allowMultiSubPerAccount(ctx, sub))
		{
			paramGetSub = sub.getMsisdn()+"|"+sub.getSubscriptionType();
		}
		else
		{
			paramGetSub = sub.getMsisdn();
		}
        
        getSub(paramGetSub, subProfileHolder);
        subProfileHolder.value = subProfileHolder.value;
        subProfileHolder.value.ratePlan = ratePlan;
        subProfileHolder.value.billingCycleDate = billingCycleDate;
        subProfileHolder.value.state = convertState(ctx, sub);
        subProfileHolder.value.serviceGrade = serviceGrade;
        return addChangeSub(subProfileHolder.value);
    }

	
	/**
	 * Multiplay capability
	 * @param context
	 * @param subscriberAccount
	 * @return
	 * @throws AgentException
	 */
	private boolean allowMultiSubPerAccount(final Context context, final Subscriber subscriberAccount)
    throws IpcgSubProvException
	{
	    final int spid = subscriberAccount.getSpid();
	
	    final Home home = (Home)context.get(CRMSpidHome.class);
	    try
	    {
	    	final CRMSpid serviceProvider = (CRMSpid)home.find(context, Integer.valueOf(spid));
	    	if (serviceProvider == null)
		    {
		        throw new IpcgSubProvException(
		            "Failed to locate service provider profile " + spid + " for account " + subscriberAccount.getBAN());
		    }
	    	return serviceProvider.isAllowMultiSubForOneAccount();
	    }
	    catch(HomeException he)
	    {
	    	throw new IpcgSubProvException(
		            "Exception while looking for spid " + spid + " for account " + subscriberAccount.getBAN() +" "+ he.getMessage());
	    }
	}
    

    /**
     * Enable subscriber.
     *
     * @param msisdn
     *            Subscriber MSISDN.
     * @param enabled
     *            Whether to enable or disable the subscriber.
     * @return Result code of IPCG CORBA service.
     * @throws IpcgSubProvException
     *             Thrown if there are problems connecting to IPCG CORBA service.
     */
    public int enableSubscriber(final String msisdn, final boolean enabled) throws IpcgSubProvException
    {
        final SubscriberProfileHolder subProfileHolder = new SubscriberProfileHolder();
        subProfileHolder.value = new SubscriberProfile();
        SubscriberState expectedState = null;

        if (enabled)
        {
            expectedState = SubscriberState.ACTIVE;
        }
        else
        {
            expectedState = SubscriberState.INACTIVE;
        }
        
        getSub(msisdn, subProfileHolder);
        
        if (subProfileHolder.value.state.value() != expectedState.value())
        {
            subProfileHolder.value.state = expectedState;
            return addChangeSub(subProfileHolder.value);
        }
        else
        {
            return ResponseCode.SUCCESS;
        }
    }


    /**
     * Updates the subscriber. If the subscriber does not exist on IPCG, create the
     * subscriber instead.
     *
     * @param subProfile
     *            The (IPCG) subscriber to be updated.
     * @return Result code of IPCG CORBA service.
     * @throws IpcgSubProvException
     *             Thrown if there are problems connecting to IPCG CORBA service.
     */
    public int addChangeSub(final SubscriberProfile subProfile) throws IpcgSubProvException
    {
        final PMLogMsg pmLogMsg = new PMLogMsg(getPMModule(), "addChangeSub()");

        int result = ExternalAppSupport.NO_CONNECTION;
        final SubscriberProv service = getService();
        final SubscriberProfileHolder subProfileHolder = new SubscriberProfileHolder();
        subProfileHolder.value = subProfile;
        
        if (LogSupport.isDebugEnabled(getContext()))
        {
            new DebugLogMsg(this, "addChangeSub() sending=" + getSubscriberProfileString(subProfile), null).log(getContext());
        }
        
        if (service==null)
        {
            throw new IpcgSubProvException("NGRC down or not available.");
        }

        try
        {
            result = service.addChangeSub(subProfile, subProfileHolder, new BooleanHolder(), new StringHolder(),
                new IntHolder());
        }
        catch (final org.omg.CORBA.COMM_FAILURE commFail)
        {
            new MinorLogMsg(this, "Fail to update subscriber " + subProfile.msisdn, commFail).log(getContext());
            throw new IpcgSubProvException("Communication error: Request to NGRC failed.", ExternalAppSupport.COMMUNICATION_FAILURE);
        }
        catch (final Exception e)
        {
            new MinorLogMsg(this, "Fail to update subscriber " + subProfile.msisdn, e).log(getContext());
            throw new IpcgSubProvException("General failure on NGRC.", ExternalAppSupport.REMOTE_EXCEPTION);
        }

        pmLogMsg.log(getContext());
        return result;
    }


    /**
     * Retrieves the subscriber profile from IPCG.
     *
     * @param msisdn
     *            MSISDN of the subscriber to be retrieved.
     * @return Result code of IPCG CORBA service.
     * @throws IpcgSubProvException
     *             Thrown if there are problems connecting to IPCG CORBA service.
     */
    public int getSub(final String msisdn) throws IpcgSubProvException
    {
        final SubscriberProfile subProfile = new SubscriberProfile();
        final SubscriberProfileHolder subProfileHolder = new SubscriberProfileHolder();
        subProfileHolder.value = subProfile;

        return getSub(msisdn, subProfileHolder);
    }


    /**
     * Retrieves the subscriber profile from IPCG.
     *
     * @param msisdn
     *            MSISDN of the subscriber to be retrieved.
     * @param subProfile
     *            Holder of the subscriber profile to be retrieved.
     * @return Result code of IPCG CORBA service.
     * @throws IpcgSubProvException
     *             Thrown if there are problems connecting to IPCG CORBA service.
     */
    public int getSub(final String msisdn, final SubscriberProfileHolder subProfile) throws IpcgSubProvException
    {
        final PMLogMsg pmLogMsg = new PMLogMsg(getPMModule(), "getSub()");
        int result = ExternalAppSupport.NO_CONNECTION;
        final SubscriberProv service = getService();

        if (service==null)
        {
            throw new IpcgSubProvException("NGRC down or not available.");
        }

        try
        {
            result = service.getSub(msisdn, subProfile);
        }
        catch (final org.omg.CORBA.COMM_FAILURE commFail)
        {
            new MinorLogMsg(this, "Fail to retrieve ipcg subscriber profile " + msisdn, commFail).log(getContext());
            throw new IpcgSubProvException("Communication error: Request to NGRC failed.", ExternalAppSupport.COMMUNICATION_FAILURE);
        }
        catch (final Exception e)
        {
            new MinorLogMsg(this, "Fail to retrieve ipcg subscriber profile " + msisdn, e).log(getContext());
            throw new IpcgSubProvException("General failure on NGRC.", ExternalAppSupport.REMOTE_EXCEPTION);
        }
        pmLogMsg.log(getContext());
        
        if (LogSupport.isDebugEnabled(getContext()))
        {
            new DebugLogMsg(this, "getSub() returned=" + getSubscriberProfileString(subProfile.value), null).log(getContext());
        }
        
        return result;
    }
    
    
    private String getSubscriberProfileString(SubscriberProfile sub)
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append("SubscriberProfile(")      
        .append("msisdn: ")
        .append(sub.msisdn)
        .append(", ")
        .append("imsi: ")
        .append(sub.imsi)
        .append(", ")
        .append("spid: ")
        .append(sub.spid)
        .append(", ")
        .append("billingCycleDate: ")
        .append(sub.billingCycleDate)
        .append(", ")
        .append("timeZone: ")
        .append(sub.timeZone)
        .append(", ")
        .append("ratePlan: ")
        .append(sub.ratePlan)
        .append(", ")
        .append("scpId: ")
        .append(sub.scpId)
        .append(", ")
        .append("state: ")
        .append(sub.state.value())
        .append(", ")
        .append("serviceGrade: ")
        .append(sub.serviceGrade)
        .append(")");
        return buffer.toString();
    }



    /**
     * Removes subscriber from IPCG.
     *
     * @param msisdn
     *            MSISDN of the subscriber to be removed.
     * @return Result code of IPCG CORBA service.
     * @throws IpcgSubProvException
     *             Thrown if there are problems connecting to IPCG CORBA service.
     */
    public int deleteSub(final String msisdn) throws IpcgSubProvException
    {
        final PMLogMsg pmLogMsg = new PMLogMsg(getPMModule(), "deleteSub()");
        int result = ExternalAppSupport.NO_CONNECTION;
        final SubscriberProv service = getService();

        if (service==null)
        {
            throw new IpcgSubProvException("NGRC down or not available.");
        }

        try
        {
            result = service.deleteSub(msisdn);
        }
        catch (final org.omg.CORBA.COMM_FAILURE commFail)
        {
            new MinorLogMsg(this, "Fail to delete ipcg subscriber profile " + msisdn, commFail).log(getContext());
            throw new IpcgSubProvException("Communication error: Request to NGRC failed.", ExternalAppSupport.COMMUNICATION_FAILURE);
        }
        catch (final Exception e)
        {
            new MinorLogMsg(this, "Fail to retrieve ipcg subscriber profile " + msisdn, e).log(getContext());
            throw new IpcgSubProvException("General failure on NGRC.", ExternalAppSupport.REMOTE_EXCEPTION);
        }
        pmLogMsg.log(getContext());
        if (result != 0)
        {
            if ( MSISDN_DOES_NOT_EXIST == result || IMSI_DOES_NOT_EXIST == result)
            {
                return 0;
            }
            else
            {
                return result;
            }
        }
        /*
         * TODO [2007-01-17]: Return result instead?
         */
        return 0;
    }


    /**
     * Adds package plan.
     *
     * @param msisdn
     *            MSISDN of the subscriber to be updated.
     * @param packagePlanId
     *            New package plan ID.
     * @param socName
     *            SOC name.
     * @return Result code from IPCG CORBA service.
     */
    public int addPackagePlan(final String msisdn, final int packagePlanId, final String socName)
    {
        final PMLogMsg pmLogMsg = new PMLogMsg(getPMModule(), "addPackagePlan()");
        // TODO: need impl?
        new DebugLogMsg(this, "Empty method addPackagePlan() called", null).log(getContext());
        pmLogMsg.log(getContext());
        return 0;
    }


    /**
     * Removes a subscriber from a package plan.
     *
     * @param msisdn
     *            MSISDN of the subscriber to be updated.
     * @param packagePlanId
     *            New package plan ID.
     * @return Result code from IPCG CORBA service.
     */
    public int deletePackagePlan(final String msisdn, final int packagePlanId)
    {
        final PMLogMsg pmLogMsg = new PMLogMsg(getPMModule(), "deletePackagePlan()");
        new DebugLogMsg(this, "Empty method deletePackagePlan() called", null).log(getContext());
        pmLogMsg.log(getContext());
        return 0;
    }


    /**
     * Updates the MSISDN of a subscriber.
     *
     * @param oldMsisdn
     *            Old MSISDN.
     * @param newMsisdn
     *            New MSISDN.
     * @return Result code from IPCG CORBA service.
     */
    public int updateMsisdn(final String oldMsisdn, final String newMsisdn)
    {
        final PMLogMsg pmLogMsg = new PMLogMsg(getPMModule(), "updateMsisdn()");
        // TODO: need impl?
        new DebugLogMsg(this, "Empty method updateMsisdn() called", null).log(getContext());
        pmLogMsg.log(getContext());
        return 0;
    }


    /**
     * Determines whether a MSISDN exists.
     *
     * @param msisdn
     *            MSISDN to be looked up.
     * @return Result code from IPCG CORBA service.
     */
    public int isMsisdnExist(final String msisdn)
    {
        final PMLogMsg pmLogMsg = new PMLogMsg(getPMModule(), "isMsisdnExist()");
        // TODO: need impl?
        new DebugLogMsg(this, "Empty method isMsisdnExist() called", null).log(getContext());
        pmLogMsg.log(getContext());
        return 0;
    }

    protected abstract String getPMModule();

    /**
     * Accepted MSISDN pattern.
     */
    public static final Pattern MSISDN_PATTERN = Pattern.compile("[0-9]{3,20}");
    
    private static final int IMSI_DOES_NOT_EXIST = 10;

    private static final int MSISDN_DOES_NOT_EXIST = 3;
}
