package com.trilogy.app.crm.poller.agent;

import java.util.ArrayList;
import java.util.List;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.core.BundleProfile;
import com.trilogy.app.crm.poller.Constants;
import com.trilogy.app.crm.poller.FilterOutException;
import com.trilogy.app.crm.poller.ProcessorInfo;
import com.trilogy.app.crm.poller.event.CRMProcessor;
import com.trilogy.app.crm.poller.event.CRMProcessorSupport;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.BundleSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.msp.Spid;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.log.PMLogMsg;

/**
 * @since 9.4
 * @author sgaidhani
 *
 */
public class BundleDepletionEventAgent implements ContextAgent, Constants {

	/**
	 *
	 */
	public BundleDepletionEventAgent(CRMProcessor processor)
	{
		super();
		processor_ = processor;
	}

	/* (non-Javadoc)
	 * @see com.redknee.framework.xhome.context.ContextAgent#execute(com.redknee.framework.xhome.context.Context)
	 */
	public void execute(Context ctx) throws AgentException
	{

		List params = new ArrayList();
		ProcessorInfo info = (ProcessorInfo) ctx.get(ProcessorInfo.class);

		final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "execute()");

		try
		{
			try
			{
				CRMProcessorSupport.makeArray(ctx, params, info.getRecord(), info.getStartIndex(), ',', info.getErid(), this);
			}
			catch ( FilterOutException e)
			{
				return;
			}

			String msisdn = CRMProcessorSupport.getField(params, BUNDLE_DEPLETE_MSISDN);
			String ban = CRMProcessorSupport.getField(params, BUNDLE_DEPLETE_BAN);
			int bundleId = CRMProcessorSupport.getInt(ctx, CRMProcessorSupport.getField(params, BUNDLE_DEPLETE_BUNDLE_ID), -1);
			int bucketId = CRMProcessorSupport.getInt(ctx, CRMProcessorSupport.getField(params, BUNDLE_DEPLETE_BUCKET_ID), -1);
			int spidId = CRMProcessorSupport.getInt(ctx, CRMProcessorSupport.getField(params, BUNDLE_DEPLETE_SPID), -1);

			if(LogSupport.isDebugEnabled(ctx))
			{
				StringBuilder sb = new StringBuilder();
				sb.append("Processing Bundle Depletion ER 1338 with parameters :");
				sb.append(" spid :");
				sb.append(spidId);
				sb.append(", msisdn :");
				sb.append(msisdn);
				sb.append(", ban :");
				sb.append(ban);
				sb.append(", bundleId :");
				sb.append(bundleId);
				sb.append(", bucketId :");
				sb.append(bucketId);

				LogSupport.debug(ctx, this, sb.toString());
			}

			if(isBundleEligibleForUnprovision(ctx, bundleId, spidId))
			{

				Spid spid = HomeSupportHelper.get(ctx).findBean(ctx, Spid.class, spidId);
				if(spid == null)
				{
					throw new AgentException("Unable to find spid :" + spidId);
				}
				ctx.put(Spid.class, spid);

				Subscriber sub = null;
				if(ban != null && !"".equals(ban.trim()))
				{
					Account account = AccountSupport.getAccount(ctx, ban);
					if(account != null)
					{
						sub = SubscriberSupport.getSubscriberIndividualAccount(ctx, ban);
					}
				}
				else
				{
					sub = SubscriberSupport.lookupActiveSubscriberForMSISDN(ctx, msisdn);
				}

				if(sub == null)
				{
					throw new AgentException("Unable to find subscriber with MSIDN :" + msisdn);
				}

				BundleProfile bundleProfile = BundleSupportHelper.get(ctx).getBundleProfile(ctx, bundleId);
				if(bundleProfile == null)
				{
					throw new AgentException("Unable to find bundle Profile with bundleID :" + bundleId);
				}

				final Object removed = sub.getBundles().remove(Long.valueOf(bundleId));
				if (removed == null)
				{
					new InfoLogMsg(this, "The bundle ID :" + bundleId + " for subscriber " + sub.getId() + " is already removed.", null).log(ctx);
				}
				else
				{
					HomeSupportHelper.get(ctx).storeBean(ctx, sub);
				}
				
				if(LogSupport.isDebugEnabled(ctx))
				{
					StringBuilder sb = new StringBuilder();
					sb.append("Processing Complete for Bundle Depletion ER 1338 with parameters :");
					sb.append(" spid :");
					sb.append(spidId);
					sb.append(", msisdn :");
					sb.append(msisdn);
					sb.append(", ban :");
					sb.append(ban);
					sb.append(", bundleId :");
					sb.append(bundleId);
					sb.append(", bucketId :");
					sb.append(bucketId);
					sb.append(". Bundle removed successfully.");

					LogSupport.debug(ctx, this, sb.toString());
				}
			}
			else
			{
				if(LogSupport.isDebugEnabled(ctx))
				{
					StringBuilder sb = new StringBuilder();
					sb.append("Processing Complete for Bundle Depletion ER 1338 with parameters :");
					sb.append(" spid :");
					sb.append(spidId);
					sb.append(", msisdn :");
					sb.append(msisdn);
					sb.append(", ban :");
					sb.append(ban);
					sb.append(", bundleId :");
					sb.append(bundleId);
					sb.append(", bucketId :");
					sb.append(bucketId);
					sb.append(". Bundle not eligible for Removal.");

					LogSupport.debug(ctx, this, sb.toString());
				}
			}
		}
		catch( Throwable t )
		{
			new MinorLogMsg(this, "Failed to process ER 1338 because of Exception " + t.getMessage(), t).log(ctx);
			processor_.saveErrorRecord(ctx, info.getRecord());
		}
		finally
		{
			pmLogMsg.log(ctx);
		}
	}

	private boolean isBundleEligibleForUnprovision(Context ctx, int bundleId,
			int spidId) throws HomeException, AgentException {

		boolean isFound = false;
		CRMSpid crmSpid = SpidSupport.getCRMSpid(ctx, spidId);
		if(crmSpid == null)
		{
			throw new AgentException("Unable to find CRMSpid :" + spidId);
		}

		String bundleList = crmSpid.getUnprovisionOnDepletion();
		String[] bundleArray = bundleList.split(",");
		for(String configuredBundleId : bundleArray)
		{
			if(configuredBundleId != null && !configuredBundleId.trim().equals("") && Integer.parseInt(configuredBundleId.trim()) == bundleId)
			{
				isFound = true;
				break;
			}					
		}
		return isFound;
	}


	private CRMProcessor processor_= null;
	private static final String PM_MODULE = BundleDepletionEventAgent.class.getName();


}
