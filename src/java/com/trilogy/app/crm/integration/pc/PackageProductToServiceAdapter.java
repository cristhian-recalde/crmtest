package com.trilogy.app.crm.integration.pc;

import com.trilogy.app.crm.bean.ActivationFeeModeEnum;
import com.trilogy.app.crm.bean.AdjustmentXInfo;
import com.trilogy.app.crm.bean.BalanceIndicatorEnum;
import com.trilogy.app.crm.bean.FeePersonalizationRuleEnum;
import com.trilogy.app.crm.bean.FixedIntervalTypeEnum;
import com.trilogy.app.crm.bean.OneTimeTypeEnum;
//import com.trilogy.app.crm.bean.PaymentOptionEnum;
import com.trilogy.app.crm.bean.RefundOptionEnum;
import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.ServicePeriodEnum;
import com.trilogy.app.crm.bean.UnifiedCatalogSpidConfig;
import com.trilogy.app.crm.bean.UnifiedCatalogSpidConfigXInfo;
import com.trilogy.app.crm.bean.ui.ProductXInfo;
import com.trilogy.app.crm.bean.ui.ServiceProduct;
import com.trilogy.app.crm.bean.ui.TechnicalServiceTemplate;
import com.trilogy.app.crm.bean.ui.TechnicalServiceTemplateXInfo;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.app.crm.bean.ui.PackageProduct;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.UnifiedCatalogSpidConfigSupport;
import com.trilogy.app.crm.bean.ui.Product;

/**
 * 
 * @author AChatterjee
 *
 */

public class PackageProductToServiceAdapter  implements Adapter {

	private static final long serialVersionUID = 1L;

	public static String MODULE = PackageProductToServiceAdapter.class.getName();

	@Override
	public Object adapt(Context ctx, Object obj) throws HomeException {

		LogSupport.debug(ctx, MODULE, "[PackageProductToServiceAdapter.adapt] Start");
		
		if (obj instanceof PackageProduct) {
			return adaptService(ctx, (PackageProduct) obj);
		}
		return null;
	}

	@Override
	public Object unAdapt(Context ctx, Object obj) throws HomeException {
		return null;
	}

	public static Service adaptService(Context ctx, PackageProduct packageProduct) throws HomeException {
		Service service = XBeans.instantiate(Service.class, ctx);
		adaptService(ctx, service, packageProduct);
		return service;
	}

	public static Service adaptService(Context ctx, Service service, PackageProduct packageProduct) throws HomeException {
		
		TechnicalServiceTemplate defaultPackageProductTemplate = UnifiedCatalogSpidConfigSupport
				.getPackageProductTemplateForDefaultValues(ctx, packageProduct);
		
		if (LogSupport.isDebugEnabled(ctx)) {
			LogSupport.debug(ctx, MODULE, "[PackageProductToServiceAdapter.adaptService] package product Id is [" + packageProduct.getProductId() + "]");
		}
		
		service.setID(packageProduct.getProductId());
		service.setName(packageProduct.getName());
		
		//Attributes to be populated from defaultPackageProductTemplate
		
		service.setSPGServiceType(defaultPackageProductTemplate.getSPGServiceType());
		service.setAllowCarryOver(defaultPackageProductTemplate.getAllowCarryOver());
		service.setSmartSuspension(defaultPackageProductTemplate.getSmartSuspension());
		service.setCallingGroupType(defaultPackageProductTemplate.getCallingGroupType().getIndex());
		//service.setChargeInBSS(defaultPackageProductTemplate.getChargeInBSS());
		service.setCugTemplateID(defaultPackageProductTemplate.getCugTemplateID());
		service.setServiceParameters(defaultPackageProductTemplate.getServiceParameters());
		service.setServiceSubType(defaultPackageProductTemplate.getServiceSubType());
		service.setTechnology(defaultPackageProductTemplate.getTechnology());
		service.setType(defaultPackageProductTemplate.getType());
		service.setVmPlanId(defaultPackageProductTemplate.getVmPlanId());
		service.setXmlProvSvcType(defaultPackageProductTemplate.getXmlProvSvcType());
		
		//Attributes populated from packageProduct bean
		
		//service.setAdjustmentType(getAdjustmentType(ctx, packageProduct.getSpid())); //use spid default
		
		//Setting the GL Code based on the default spid level adjustment type
		service.setAdjustmentGLCode(UnifiedCatalogSpidConfigSupport.getDefaultGLCode(ctx, packageProduct.getSpid()));

		service.setBillDisplayOrder(packageProduct.getBillDisplayOrder());
		service.setChargeableWhileSuspended(packageProduct.getChargeableWhileSuspended());
		service.setClctThreshold(packageProduct.getClctThreshold());
		service.setCustomDescAllowed(packageProduct.getCustomDescAllowed());
		service.setEnableCLTC(packageProduct.getEnableCLTC());
		service.setExecutionOrder(packageProduct.getExecutionOrder());
		service.setExternalServiceCode(packageProduct.getExternalServiceCode());
		//service.setPayerAllowed(packageProduct.getPayerAllowed());
		service.setPermission(packageProduct.getPermission());
		service.setPrepaidProvisionConfigs(packageProduct.getPrepaidProvisionConfigs());
		service.setPrepaidResumeConfigs(packageProduct.getPrepaidResumeConfigs());
		service.setPrepaidSuspendConfigs(packageProduct.getPrepaidSuspendConfigs());
		service.setPrepaidUnprovisionConfigs(packageProduct.getPrepaidUnprovisionConfigs());
		//service.setPriority(packageProduct.getPriority());
		service.setProvisionConfigs(packageProduct.getProvisionConfigs());
		service.setReprovisionOnActive(packageProduct.getReprovisionOnActive());
		service.setRestrictProvisioning(packageProduct.getRestrictProvisioning());
		service.setResumeConfigs(packageProduct.getResumeConfigs());
		service.setServiceCategory(packageProduct.getServiceCategory());
		service.setShowInInvoice(packageProduct.getShowInInvoice());
		service.setShowZeroAmountInInvoice(packageProduct.getShowZeroAmountInInvoice());
		service.setSpid(packageProduct.getSpid());
		service.setSubscriptionType(packageProduct.getSubscriptionType());
		service.setSuspendConfigs(packageProduct.getSuspendConfigs());
		//service.setUnifiedServiceID(packageProduct.getUnifiedServiceID());
		service.setUnprovisionConfigs(packageProduct.getUnprovisionConfigs());
		
		//Attributes to be populated from defaultPriceTemplate
		service.setActivationFee(ActivationFeeModeEnum.FULL);
		service.setChargeScheme(ServicePeriodEnum.MONTHLY);
		service.setRecurrenceType(OneTimeTypeEnum.ONE_OFF_FIXED_DATE_RANGE);
		service.setRecurrenceInterval(2);
		service.setBillingMonth(2);
		service.setDynamicBilling(false);
		service.setStartDate(null);
		service.setEndDate(null);
		service.setValidity(30);
		service.setFixedInterval(FixedIntervalTypeEnum.DAYS);
		//service.setPaymentOption(PaymentOptionEnum.PAY_IN_ADVANCE);
		service.setFeePersonalizationRule(FeePersonalizationRuleEnum.APPLY_DURING_BILL_PERIOD);
		service.setTaxAuthority(1);
		service.setPostpaidSubCreationOnly(false);
		//service.setChargePrepaidSubscribers(defaultPackageProductTemplate.getChargeableWhileSuspended());
		//service.setApprovalRequired(false);
		service.setRefundOption(RefundOptionEnum.BASED_ON_DAYS);
		service.setRefundable(true);
		service.setFeePersonalizationAllowed(false);
		//service.setFirstMonthFree(false);
		service.setForceCharging(false);
		//service.setBalanceIndicator(BalanceIndicatorEnum.PRIMARY);
		
		service.setAdjustmentTypeName(null);
		service.setAdjustmentTypeDesc(null);
		service.setAdjustmentInvoiceDesc(null);
		service.setBillGroupID(null);
		
		if (LogSupport.isDebugEnabled(ctx)) {
			LogSupport.debug(ctx, MODULE,"Service Bean to be saved is [" + service + "]");
		}
		
		LogSupport.debug(ctx, MODULE,"Adapt Service Product to Service "+service.getID());
		
		return service;
	}
}
