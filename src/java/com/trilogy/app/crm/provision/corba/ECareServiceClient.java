// INSPECTED: 09/19/2003 GEA
package com.trilogy.app.crm.provision.corba;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAwareSupport;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.support.CorbaSupportHelper;
import com.trilogy.service.corba.CorbaClientProperty;
import com.trilogy.service.corba.CorbaClientPropertyHome;
import com.trilogy.util.corba.CorbaClientException;
import com.trilogy.util.corba.CorbaClientProxy;

public class ECareServiceClient extends ContextAwareSupport
{
	private ECareService service_;

	private CorbaClientProxy corbaProxy_;

	private String username_;

	private String password_;

	CorbaClientProperty property_;

	/**
	 * Constructor. Configures and initializes a connection to the server.
	 * The CorbaClientProperty is to be found in the context under 
	 * CorbaClientProperty.class
	 * 
	 * @param ctx
	 */
	public ECareServiceClient(Context ctx)
	{
		setContext(ctx);
		init();
	}

	/**
	 * Constructor. Configures and initialize a connection to the server.
	 * The CorbaClientProperty will be searched in the property home under the 
	 * key that is passed here.
	 * 
	 * @param ctx
	 * @param key
	 */
	public ECareServiceClient(Context ctx, String key)
	{
		setContext(ctx);
		init(key);
	}

	private void init()
	{
		property_ = (CorbaClientProperty) getContext().get(CorbaClientProperty.class);
		initWithProperty();
	}

	private void init(String key) throws IllegalArgumentException
	{
		Home corbaClientPropertyHome = null;

		corbaClientPropertyHome = (Home) getContext().get(CorbaClientPropertyHome.class);

		if (corbaClientPropertyHome == null)
		{
			throw new IllegalArgumentException("Corba client configuration does not exist");
		}

		try
		{
			property_ = (CorbaClientProperty) corbaClientPropertyHome.find(getContext(), key);
		}
		catch (HomeException e)
		{
			new MinorLogMsg(this, e.getMessage(), e).log(getContext());
			throw new IllegalArgumentException("Unable to load corba proxy for " + key
				+ ". Corba property bean is null.");
		}

		initWithProperty();
	}

	private void initWithProperty()
	{
		if (property_ == null)
		{
			throw new IllegalArgumentException("Can't find ClientProperty for ECareServiceClient");
		}

		try
		{
			new InfoLogMsg(this, property_.toString(), null).log(getContext());

			username_ = property_.getUsername();
			password_ = property_.getPassword();
			service_ = null;
			// ORB orb = ORB.init(new String[] { }, null);
			corbaProxy_ = CorbaSupportHelper.get(getContext()).createProxy(getContext(),property_); 
		}
		catch (Exception e)
		{
			// TODO: snmp external link down trap throw new
		}

		// property_.getConnectFailMsg(),
		// property_.getRetryMax(),
		// property_.getRetryInterval(),

	}

	/**
	 * Returns the connected ECareService
	 * @return connected ECareService, null if error connecting
	 */
	public ECareService getService()
	{
		org.omg.CORBA.Object objServant = null;

		if (service_ != null)
		{
			return service_;
		}

		if (corbaProxy_ == null)
		{
			//ORB orb = ORB.init(new String[] {}, null);
			try
			{
				corbaProxy_ = CorbaSupportHelper.get(getContext()).createProxy(getContext(),property_);
			}
			catch (CorbaClientException ccEx)
			{
				invalidate(ccEx);
				return null;
			}
		}

		objServant = corbaProxy_.instance();
		if (objServant != null)
		{
			try
			{
				// attempt to derive SubProvision
				service_ = ECareServiceFactoryHelper.narrow(objServant).createEcareService(username_, password_);
				if (service_ != null)
				{
					// TODO: snmp external link up trap
				}
				return service_;
			}
			catch (Exception e)
			{
				invalidate(e);
				return null;
			}
		}
		invalidate(null);
		return null;
	}

	public void invalidate(Throwable t)
	{
		// only raise a SNMP trap if this is the first time we discover the
		// connection is gone
		if (service_ != null)
		{
			// TODO: snmp external link down trap throw new
		}
		corbaProxy_.invalidate();
		service_ = null;
	}

	/**
	 * Checks if the client is connected to the server.
	 * 
	 * @return true if connection is ok (service is valid)
	 */
	public boolean isConnected()
	{
		return getService() != null;
	}

	//   public int addSubscriber(
	//      String msisdn,
	//      String imsi,
	//      short spId,
	//      short svcId,
	//      short svcGrade,
	//      short tzOffset,
	//      short ratePlan,
	//      short recurDate,
	//      short scpId,
	//      short hlrId,
	//      boolean enable,
	//      long barringPlan,
	//      short smsCount)
	//   {
	//      int result = -1;
	//      svc_provision3 service = getService();
	//      subsProfile3 profile = null;
	//
	//      if (service != null)
	//      {
	//         profile =
	//            subsProfile(
	//               msisdn,
	//               imsi,
	//               spId,
	//               svcId,
	//               svcGrade,
	//               tzOffset,
	//               ratePlan,
	//               recurDate,
	//               scpId,
	//               hlrId,
	//               enable,
	//               barringPlan,
	//               smsCount);
	//         try
	//         {
	//            result = service.addChangeSub3(profile);
	//         }
	//         catch (org.omg.CORBA.COMM_FAILURE commFail)
	//         {
	//            invalidate(commFail);
	//            result = 301;
	//         }
	//         catch (Exception e)
	//         {
	//            new MinorLogMsg(
	//               this,
	//               "Fail to add new subscriber " + msisdn,
	//               e).log(
	//               getContext());
	//         }
	//      }
	//      else
	//      {
	//         // connection not available
	//         return 301;
	//      }
	//
	//      return result;
	//   }

	//   public int updateSubscriber(
	//      String msisdn,
	//      String imsi,
	//      short spId,
	//      short svcId,
	//      short svcGrade,
	//      short tzOffset,
	//      short ratePlan,
	//      short recurDate,
	//      short scpId,
	//      short hlrId,
	//      boolean enable,
	//      long barringPlan,
	//      short smsCount)
	//   {
	//      subsProfile3 profile =
	//         subsProfile(
	//            msisdn,
	//            imsi,
	//            spId,
	//            svcId,
	//            svcGrade,
	//            tzOffset,
	//            ratePlan,
	//            recurDate,
	//            scpId,
	//            hlrId,
	//            enable,
	//            barringPlan,
	//            smsCount);
	//      return updateSubscriber(profile);
	//   }

	//   protected int updateSubscriber(subsProfile3 profile)
	//   {
	//      int result = -1;
	//      svc_provision3 service = getService();
	//
	//      if (service != null)
	//      {
	//         try
	//         {
	//            result = service.addChangeSub3(profile);
	//         }
	//         catch (org.omg.CORBA.COMM_FAILURE commFail)
	//         {
	//            invalidate(commFail);
	//            result = 301;
	//         }
	//         catch (Exception e)
	//         {
	//            new MinorLogMsg(
	//               this,
	//               "Fail to change subscriber " + profile.msisdn,
	//               null).log(
	//               getContext());
	//         }
	//      }
	//      else
	//      {
	//         // connection not available
	//         result = 301;
	//      }
	//
	//      return result;
	//   }

	//   public int enableSubscriber(String msisdn, boolean isEnable)
	//   {
	//      subsProfile3 profile = getSubsProfile(msisdn);
	//      if (profile == null)
	//      {
	//         return 201;
	//      }
	//      profile.enable = isEnable;
	//      return updateSubscriber(profile);
	//   }

	//   public int updateRatePlan(String msisdn, short ratePlan)
	//   {
	//      subsProfile3 profile = getSubsProfile(msisdn);
	//      if (profile == null)
	//      {
	//         return 201;
	//      }
	//      profile.ratePlan = ratePlan;
	//      return updateSubscriber(profile);
	//   }

	//   public int updateImsi(String msisdn, String imsi)
	//   {
	//      subsProfile3 profile = getSubsProfile(msisdn);
	//      if (profile == null)
	//      {
	//         return 201;
	//      }
	//      profile.imsi = imsi;
	//      return updateSubscriber(profile);
	//   }

	//   public int deleteSubscriber(String msisdn)
	//   {
	//      int result = -1;
	//      svc_provision3 service = getService();
	//
	//      if (service != null)
	//      {
	//         try
	//         {
	//            result = service.deleteSub3(msisdn);
	//         }
	//         catch (org.omg.CORBA.COMM_FAILURE commFail)
	//         {
	//            invalidate(commFail);
	//            result = 301;
	//         }
	//         catch (Exception e)
	//         {
	//            new MinorLogMsg(
	//               this,
	//               "Fail to delete subscriber " + msisdn,
	//               null).log(
	//               getContext());
	//         }
	//      }
	//      else
	//      {
	//         // connection not available
	//         result = 301;
	//      }
	//
	//      return result;
	//   }

	//   public subsProfile3 getSubsProfile(String msisdn)
	//   {
	//      int result = -1;
	//      svc_provision3 service = getService();
	//      subsProfile3Holder profileHolder = null;
	//
	//      // REVIEW(readability): Add blank lines between 'if' blocks to reduce the
	//      // risk that the reader mistakenly thinks 'else if'. GEA
	//      if (service != null)
	//      {
	//         try
	//         {
	//            profileHolder = new subsProfile3Holder();
	//            result = service.getSub3(msisdn, profileHolder);
	//         }
	//         catch (org.omg.CORBA.COMM_FAILURE commFail)
	//         {
	//            invalidate(commFail);
	//            result = 301;
	//         }
	//         catch (Exception e)
	//         {
	//            new MinorLogMsg(
	//               this,
	//               "Fail to retrieve subscriber " + msisdn,
	//               e).log(
	//               getContext());
	//         }
	//      }
	//
	//      if (result == 0 && profileHolder != null)
	//      {
	//         return profileHolder.value;
	//      }
	//      else
	//      {
	//         return null;
	//      }
	//   }

	//   public int getSmsSent(String msisdn)
	//   {
	//      subsProfile3 profile = getSubsProfile(msisdn);
	//      if (profile != null)
	//      {
	//         return profile.smsCount;
	//      }
	//      return 0;
	//   }

	//   private subsProfile3 subsProfile(
	//      String msisdn,
	//      String imsi,
	//      short spId,
	//      short svcId,
	//      short svcGrade,
	//      short tzOffset,
	//      short ratePlan,
	//      short recurDate,
	//      short scpId,
	//      short hlrId,
	//      boolean enable,
	//      long barringPlan,
	//      short smsCount)
	//   {
	//      subsProfile3 profile = new subsProfile3();
	//
	//      profile.msisdn = msisdn;
	//      profile.imsi = imsi;
	//      profile.spid = spId;
	//      profile.svcid = svcId;
	//      profile.svcGrade = svcGrade;
	//      profile.TzOffset = tzOffset;
	//      profile.ratePlan = ratePlan;
	//      profile.recurDate = recurDate;
	//      profile.scpid = scpId;
	//      profile.hlrid = hlrId;
	//      profile.enable = enable;
	//      profile.barringplan = barringPlan;
	//      profile.smsCount = smsCount;
	//
	//      return profile;
	//   }
}
