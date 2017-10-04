package com.trilogy.app.crm.integration.pc;

import com.trilogy.app.crm.bean.ActivationFeeModeEnum;
import com.trilogy.app.crm.bean.BalanceIndicatorEnum;
import com.trilogy.app.crm.bean.FeePersonalizationRuleEnum;
import com.trilogy.app.crm.bean.FixedIntervalTypeEnum;
import com.trilogy.app.crm.bean.OneTimeTypeEnum;
//import com.trilogy.app.crm.bean.PaymentOptionEnum;
import com.trilogy.app.crm.bean.RefundOptionEnum;
import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.ui.ServiceProduct;
import com.trilogy.app.crm.bean.ui.TechnicalServiceTemplate;
import com.trilogy.app.crm.bean.ui.TechnicalServiceTemplateXInfo;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.UnifiedCatalogSpidConfigSupport;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

public class ServiceProductToServiceAdapter implements Adapter {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public Object adapt(Context ctx, Object obj) throws HomeException {
		if (LogSupport.isDebugEnabled(ctx)) {
			LogSupport.debug(ctx, this, "[ServiceProductToServiceAdapter.adapt] Start");
		}
		if (obj instanceof ServiceProduct) {
			return adaptService(ctx, (ServiceProduct) obj);
		}
		return null;
	}

	@Override
	public Object unAdapt(Context paramContext, Object paramObject)
			throws HomeException {
		return null;
	}

	public static Service adaptService(Context ctx,
			ServiceProduct serviceProductBean) throws IllegalArgumentException, HomeException {
		if (LogSupport.isDebugEnabled(ctx)) {
			LogSupport.debug(ctx, MODULE, "[ServiceProductToServiceAdapter.adaptService] Service product id:"+serviceProductBean.getProductId());
		}
		Service service = XBeans.instantiate(Service.class, ctx);
		adaptService(ctx, service, serviceProductBean);
		return service;
	}

	public static Service adaptService(Context ctx, Service service,
			ServiceProduct serviceProductBean) throws IllegalArgumentException, HomeException {
		
		if (LogSupport.isDebugEnabled(ctx)) {
			LogSupport.debug(ctx, MODULE, "[ServiceProductToServiceAdapter.adaptService] Service product id:"+String.valueOf(serviceProductBean.getProductId()));
		}
		
		TechnicalServiceTemplate defaultServiceProductTemplate = getServiceProductTemplateForDefaultValues(ctx, serviceProductBean.getTechnicalServiceTemplateId());
		//service.setAdjustmentType(getAdjustmentType(ctx,serviceProductBean.getSpid())); //use spid default
		//Setting the GL Code based on the default spid level adjustment type
		service.setAdjustmentGLCode(UnifiedCatalogSpidConfigSupport.getDefaultGLCode(ctx, serviceProductBean.getSpid()));
		
		service.setID(serviceProductBean.getProductId());
		service.setName(serviceProductBean.getName());
		service.setSpid(serviceProductBean.getSpid());
		service.setSubscriptionType(serviceProductBean.getSubscriptionType());
		service.setTechnology(serviceProductBean.getTechnologyType());
		service.setType(serviceProductBean.getType());
		service.setServiceCategory(serviceProductBean.getServiceCategory());
		service.setCallingGroupType(serviceProductBean.getCallingGroupType().getIndex());
		service.setCugTemplateID(serviceProductBean.getCugTemplateID());
		service.setVmPlanId(serviceProductBean.getVmPlanId()); //Added
		service.setXmlProvSvcType(serviceProductBean.getXmlProvSvcType());
		service.setSPGServiceType(serviceProductBean.getSPGServiceType());
		service.setAllowCarryOver(serviceProductBean.getAllowCarryOver());
		service.setExecutionOrder(serviceProductBean.getExecutionOrder());
		service.setBillDisplayOrder(serviceProductBean.getBillDisplayOrder());
		service.setEnableCLTC(serviceProductBean.getEnableCLTC());
		service.setClctThreshold(serviceProductBean.getClctThreshold());
		service.setReprovisionOnActive(serviceProductBean.getReprovisionOnActive());
		service.setSmartSuspension(serviceProductBean.getSmartSuspension());
		service.setChargeableWhileSuspended(serviceProductBean.getChargeableWhileSuspended()); //No value
		service.setRestrictProvisioning(serviceProductBean.getRestrictProvisioning());
		service.setShowInInvoice(serviceProductBean.getShowInInvoice());
		service.setShowZeroAmountInInvoice(serviceProductBean.getShowZeroAmountInInvoice());
		service.setExternalServiceCode(serviceProductBean.getExternalServiceCode());
		//service.setUnifiedServiceID(serviceProductBean.getUnifiedServiceID());
		service.setHandler(serviceProductBean.getHandler()); //Added
		//service.setPayerAllowed(serviceProductBean.getPayerAllowed());
		service.setCustomDescAllowed(serviceProductBean.getCustomDescAllowed());
		service.setPrepaidProvisionConfigs(serviceProductBean.getPrepaidProvisionConfigs());
		service.setPrepaidUnprovisionConfigs(serviceProductBean.getPrepaidUnprovisionConfigs());
		service.setPrepaidSuspendConfigs(serviceProductBean.getPrepaidSuspendConfigs());
		service.setPrepaidResumeConfigs(serviceProductBean.getPrepaidResumeConfigs());
		service.setProvisionConfigs(serviceProductBean.getProvisionConfigs());
		service.setUnprovisionConfigs(serviceProductBean.getUnprovisionConfigs());
		service.setSuspendConfigs(serviceProductBean.getSuspendConfigs());
		service.setResumeConfigs(serviceProductBean.getResumeConfigs());
		service.setPermission(serviceProductBean.getPermission());
		//service.setChargeInBSS(defaultServiceProductTemplate.getChargeInBSS());	//Set from Technical Service Template
		service.setServiceSubType(serviceProductBean.getServiceSubType());
		service.setServiceParameters(serviceProductBean.getServiceParameters());
		//service.setPriority(serviceProductBean.getPriority());
		
		//Actual Values to be populated from PriceTemplate
		service.setChargeScheme(serviceProductBean.getChargeScheme());
		service.setRecurrenceInterval(2);
		service.setDynamicBilling(false);
		service.setBillingMonth(2);
		service.setRecurrenceType(OneTimeTypeEnum.ONE_OFF_FIXED_DATE_RANGE);
		service.setStartDate(null);
		service.setEndDate(null);
		service.setValidity(30);
		service.setFixedInterval(FixedIntervalTypeEnum.DAYS);
		//service.setPaymentOption(PaymentOptionEnum.PAY_IN_ADVANCE);
		service.setFeePersonalizationRule(FeePersonalizationRuleEnum.APPLY_DURING_BILL_PERIOD);
		service.setTaxAuthority(0); //Added
		service.setActivationFee(ActivationFeeModeEnum.FULL);
		service.setPostpaidSubCreationOnly(false);
		//service.setChargePrepaidSubscribers(serviceProductBean.getChargeableWhileSuspended());
		//service.setApprovalRequired(false); //Added
		service.setRefundable(true);
		service.setRefundOption(RefundOptionEnum.BASED_ON_DAYS);
		service.setFeePersonalizationAllowed(false);
		service.setForceCharging(false);
		//service.setFirstMonthFree(false);
		//service.setBalanceIndicator(BalanceIndicatorEnum.PRIMARY);
		service.setBillGroupID(null);
		
		if (LogSupport.isDebugEnabled(ctx)) {
			LogSupport.debug(ctx, MODULE,"[ServiceProductToServiceAdapter.adaptService] Adapt Service Product to Service "+String.valueOf(service.getID()));
		}
		
		return service;
	}

	private static TechnicalServiceTemplate getServiceProductTemplateForDefaultValues(Context ctx, long technicalServiceTemplateId) {
		
		if (LogSupport.isDebugEnabled(ctx)) {
			LogSupport.debug(ctx, MODULE,"[getServiceProductTemplateForDefaultValues] fetch Service Product for Id:"+String.valueOf(technicalServiceTemplateId));
		}
		
		TechnicalServiceTemplate defaultServiceProductTemplate = null;
		
		try {
			And filterForTST = new And();
			filterForTST.add(new EQ(TechnicalServiceTemplateXInfo.ID, technicalServiceTemplateId));
			defaultServiceProductTemplate = (TechnicalServiceTemplate) HomeSupportHelper.get(ctx).findBean(ctx, TechnicalServiceTemplate.class, filterForTST);
		} catch (Exception e) {
			LogSupport.major(ctx, MODULE,"Cannot find Default Technical Service Template with ID [" + technicalServiceTemplateId + "]");
		}
		return defaultServiceProductTemplate;
	}

	public static String MODULE = ServiceProductToServiceAdapter.class
			.getName();
}
