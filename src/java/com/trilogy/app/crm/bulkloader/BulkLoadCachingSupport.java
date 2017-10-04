package com.trilogy.app.crm.bulkloader;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.CachingHome;
import com.trilogy.framework.xhome.home.ContextualizingHome;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xhome.home.TransientHome;
import com.trilogy.framework.xhome.txn.CommitRatioHome;

import com.trilogy.app.crm.bean.ActivationReasonCodeHome;
import com.trilogy.app.crm.bean.ActivationReasonCodeTransientHome;
import com.trilogy.app.crm.bean.AuxiliaryServiceHome;
import com.trilogy.app.crm.bean.AuxiliaryServiceTransientHome;
import com.trilogy.app.crm.bean.BillCycleBillingMessageHome;
import com.trilogy.app.crm.bean.BillCycleBillingMessageTransientHome;
import com.trilogy.app.crm.bean.ChargingTypeHome;
import com.trilogy.app.crm.bean.ChargingTypeTransientHome;
import com.trilogy.app.crm.bean.CreditCategoryBillingMessageTransientHome;
import com.trilogy.app.crm.bean.DealerCodeHome;
import com.trilogy.app.crm.bean.DealerCodeTransientHome;
import com.trilogy.app.crm.bean.HomezoneCountHome;
import com.trilogy.app.crm.bean.HomezoneCountTransientHome;
import com.trilogy.app.crm.bean.MsisdnGroupHome;
import com.trilogy.app.crm.bean.MsisdnGroupTransientHome;
import com.trilogy.app.crm.bean.PackageGroupHome;
import com.trilogy.app.crm.bean.PackageGroupTransientHome;
import com.trilogy.app.crm.bean.PricePlanBillingMessageHome;
import com.trilogy.app.crm.bean.PricePlanBillingMessageTransientHome;
import com.trilogy.app.crm.bean.PricePlanGroupHome;
import com.trilogy.app.crm.bean.PricePlanGroupTransientHome;
import com.trilogy.app.crm.bean.PricePlanHome;
import com.trilogy.app.crm.bean.PricePlanTransientHome;
import com.trilogy.app.crm.bean.PricePlanVersionHome;
import com.trilogy.app.crm.bean.PricePlanVersionTransientHome;
import com.trilogy.app.crm.bean.ServiceHome;
import com.trilogy.app.crm.bean.ServicePackageHome;
import com.trilogy.app.crm.bean.ServicePackageTransientHome;
import com.trilogy.app.crm.bean.ServiceTransientHome;
import com.trilogy.app.crm.bean.SpidBillingMessageHome;
import com.trilogy.app.crm.bean.SpidBillingMessageTransientHome;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberNoteHome;
import com.trilogy.app.crm.bean.SubscriberServicesHome;
import com.trilogy.app.crm.bean.TransactionHome;
import com.trilogy.app.crm.bean.service.SubscriberSubscriptionHistoryHome;
import com.trilogy.app.crm.bean.service.SubscriptionProvisioningHistoryHome;
import com.trilogy.app.crm.bundle.BundleProfileHome;
import com.trilogy.app.crm.bundle.BundleProfileTransientHome;
import com.trilogy.app.crm.extension.auxiliaryservice.CallingGroupAuxSvcExtensionHome;
import com.trilogy.app.crm.extension.auxiliaryservice.CallingGroupAuxSvcExtensionTransientHome;
import com.trilogy.app.crm.extension.spid.DuplicateAccountDetectionSpidExtensionHome;
import com.trilogy.app.crm.extension.spid.DuplicateAccountDetectionSpidExtensionTransientHome;
import com.trilogy.app.crm.extension.spid.MinimumAgeLimitSpidExtensionHome;
import com.trilogy.app.crm.extension.spid.MinimumAgeLimitSpidExtensionTransientHome;
import com.trilogy.app.crm.extension.spid.TaxAdaptersSpidExtensionHome;
import com.trilogy.app.crm.extension.spid.TaxAdaptersSpidExtensionTransientHome;
import com.trilogy.app.crm.extension.subscriber.DualBalanceSubExtensionHome;
import com.trilogy.app.crm.extension.subscriber.DualBalanceSubExtensionTransientHome;
import com.trilogy.app.crm.extension.subscriber.OverdraftBalanceSubExtensionHome;
import com.trilogy.app.crm.extension.subscriber.OverdraftBalanceSubExtensionTransientHome;
import com.trilogy.app.crm.extension.subscriber.PPSMSupporteeSubExtensionHome;
import com.trilogy.app.crm.extension.subscriber.PPSMSupporteeSubExtensionTransientHome;
import com.trilogy.app.crm.extension.subscriber.PPSMSupporterSubExtensionHome;
import com.trilogy.app.crm.extension.subscriber.PPSMSupporterSubExtensionTransientHome;
import com.trilogy.app.crm.log.SubscriptionActivationERHome;
import com.trilogy.app.crm.notification.template.TemplateGroupGlobalRecordHome;
import com.trilogy.app.crm.notification.template.TemplateGroupGlobalRecordTransientHome;
import com.trilogy.app.crm.numbermgn.ImsiMgmtHistoryHome;
import com.trilogy.app.crm.numbermgn.MsisdnMgmtHistoryHome;
import com.trilogy.app.crm.numbermgn.PackageMgmtHistoryHome;
import com.trilogy.app.crm.resource.ResourceDeviceDefaultPackageHome;
import com.trilogy.app.crm.resource.ResourceDeviceDefaultPackageTransientHome;

public class BulkLoadCachingSupport 
{

	private BulkLoadCachingSupport()
	{
		
	}
	
	public static void autoCacheHomes(Context ctx)
	{
		Context subCtx = ctx.createSubContext();
		//subCtx.put(CacheConfigHome.class,NullHome.instance());
		
		wrap(ctx, subCtx, new ServicePackageTransientHome(ctx), ServicePackageHome.class);
		wrap(ctx, subCtx, new ServiceTransientHome(ctx), ServiceHome.class);
		wrap(ctx, subCtx, new BundleProfileTransientHome(ctx), BundleProfileHome.class);
		wrap(ctx, subCtx, new AuxiliaryServiceTransientHome(ctx), AuxiliaryServiceHome.class);
		wrap(ctx, subCtx, new ResourceDeviceDefaultPackageTransientHome(ctx), ResourceDeviceDefaultPackageHome.class);
		wrap(ctx, subCtx, new PricePlanVersionTransientHome(ctx), PricePlanVersionHome.class);
		wrap(ctx, subCtx, new DealerCodeTransientHome(ctx), DealerCodeHome.class);
		wrap(ctx, subCtx, new MsisdnGroupTransientHome(ctx), MsisdnGroupHome.class);
		wrap(ctx, subCtx, new PricePlanGroupTransientHome(ctx), PricePlanGroupHome.class);
		wrap(ctx, subCtx, new PPSMSupporteeSubExtensionTransientHome(ctx), PPSMSupporteeSubExtensionHome.class);
		wrap(ctx, subCtx, new PPSMSupporterSubExtensionTransientHome(ctx), PPSMSupporterSubExtensionHome.class);
		wrap(ctx, subCtx, new DualBalanceSubExtensionTransientHome(ctx), DualBalanceSubExtensionHome.class);
		wrap(ctx, subCtx, new OverdraftBalanceSubExtensionTransientHome(ctx), OverdraftBalanceSubExtensionHome.class);
		wrap(ctx, subCtx, new SpidBillingMessageTransientHome(ctx), SpidBillingMessageHome.class);
		wrap(ctx, subCtx, new BillCycleBillingMessageTransientHome(ctx), BillCycleBillingMessageHome.class);
		wrap(ctx, subCtx, new CreditCategoryBillingMessageTransientHome(ctx), SpidBillingMessageHome.class);
		wrap(ctx, subCtx, new PricePlanBillingMessageTransientHome(ctx), PricePlanBillingMessageHome.class);
		wrap(ctx, subCtx, new ChargingTypeTransientHome(ctx), ChargingTypeHome.class);
		wrap(ctx, subCtx, new PackageGroupTransientHome(ctx), PackageGroupHome.class);
		wrap(ctx, subCtx, new HomezoneCountTransientHome(ctx), HomezoneCountHome.class);
		wrap(ctx, subCtx, new DuplicateAccountDetectionSpidExtensionTransientHome(ctx), DuplicateAccountDetectionSpidExtensionHome.class);
		wrap(ctx, subCtx, new TaxAdaptersSpidExtensionTransientHome(ctx), TaxAdaptersSpidExtensionHome.class);
		wrap(ctx, subCtx, new MinimumAgeLimitSpidExtensionTransientHome(ctx), MinimumAgeLimitSpidExtensionHome.class);
		wrap(ctx, subCtx, new TemplateGroupGlobalRecordTransientHome(ctx), TemplateGroupGlobalRecordHome.class);
		wrap(ctx, subCtx, new ActivationReasonCodeTransientHome(ctx), ActivationReasonCodeHome.class);
		wrap(ctx, subCtx, new PricePlanTransientHome(ctx), PricePlanHome.class);
		wrap(ctx, subCtx, new CallingGroupAuxSvcExtensionTransientHome(ctx), CallingGroupAuxSvcExtensionHome.class);
	}


	private static String getKey(Class clazz)
	{
		return "BulkLoadCache - " + clazz.getName();
	}
	
	/**
	 * 
	 * Wraps home pipeline with a CachingHome.
	 * 
	 * @param ctx
	 * @param cacheCtx
	 * @param transientHome
	 * @param clazz TODO
	 */
	private static void wrap(Context ctx, Context cacheCtx, TransientHome transientHome, Class clazz)
	{
		Home sourceHome = (Home) ctx.get(clazz);
		Home cacheHome = wrapTransientHomeWithContextualizingHome(ctx, transientHome, sourceHome);
		
		ctx.put(clazz, new CachingHome(cacheCtx, getKey(clazz), cacheHome, sourceHome) );
	}
	
	/**
	 * If the home being cached contains ContextualizingHome, then it is necessary to wrap the TransientHome with 
	 * ContextualizingHome, otherwise, bean will returned from the cached home will return the stale context.
	 * 
	 * @param ctx
	 * @param transientHome
	 * @param sourceHome
	 * @return
	 */
	private static Home wrapTransientHomeWithContextualizingHome(Context ctx, TransientHome transientHome, Home sourceHome)
	{
		if(sourceHome instanceof HomeProxy)
		{
			HomeProxy homeProxy = (HomeProxy) sourceHome;
			Home contextualizingHome = homeProxy.findDecorator(ContextualizingHome.class);
			if(contextualizingHome != null)
			{
				return new ContextualizingHome(ctx, transientHome);
			}
		}
		return transientHome;
	}
	
//	public void func(Context ctx)
//	{
//		try
//		{
//			Context subCtx = (Context) ctx.createSubContext();
//			CachingHome cachingHome = new CachingHome(subCtx, ServiceHome.class.getName(), 
//					new ServiceTransientHome(ctx), (Home)ctx.get(ServiceHome.class));
//			
//			Service service = (Service) cachingHome.find(ctx, 538L);
//			print(service.getContext());
//		}
//		catch(Throwable t)
//		{
//			print(t);
//		}
//	}

	public static void buildCommitRatioHome(Context ctx, int commitRatio)
	{
		ctx.put(MsisdnMgmtHistoryHome.class, new CommitRatioHome(ctx, commitRatio, (Home)ctx.get(MsisdnMgmtHistoryHome.class)) );
		ctx.put(SubscriberHome.class, new CommitRatioHome(ctx, commitRatio, (Home)ctx.get(SubscriberHome.class)) );
		ctx.put(TransactionHome.class, new CommitRatioHome(ctx, commitRatio, (Home)ctx.get(TransactionHome.class)) );
		ctx.put(PackageMgmtHistoryHome.class, new CommitRatioHome(ctx, commitRatio, (Home)ctx.get(PackageMgmtHistoryHome.class)) );
		ctx.put(SubscriberServicesHome.class, new CommitRatioHome(ctx, commitRatio, (Home)ctx.get(SubscriberServicesHome.class)) );
		ctx.put(SubscriptionProvisioningHistoryHome.class, new CommitRatioHome(ctx, commitRatio, (Home)ctx.get(SubscriptionProvisioningHistoryHome.class)) );
		ctx.put(ImsiMgmtHistoryHome.class, new CommitRatioHome(ctx, commitRatio, (Home)ctx.get(ImsiMgmtHistoryHome.class)) );
		ctx.put(SubscriberSubscriptionHistoryHome.class, new CommitRatioHome(ctx, commitRatio, (Home)ctx.get(SubscriberSubscriptionHistoryHome.class)) );
		ctx.put(SubscriptionActivationERHome.class, new CommitRatioHome(ctx, commitRatio, (Home)ctx.get(SubscriptionActivationERHome.class)) );
		ctx.put(SubscriberNoteHome.class, new CommitRatioHome(ctx, commitRatio, (Home)ctx.get(SubscriberNoteHome.class)) );
		ctx.put(HomezoneCountHome.class, new CommitRatioHome(ctx, commitRatio, (Home)ctx.get(HomezoneCountHome.class)) );
	}
	
	class CommitRatioFacade
	{
		
	}
}
