package com.trilogy.app.crm.adapters;


import com.trilogy.app.crm.DataModelAdaptionSupport;
import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.ui.AdjustmentTypeNVersion;
import com.trilogy.app.crm.bean.ui.PricingVersion;
import com.trilogy.app.crm.bean.ui.Product;
import com.trilogy.app.crm.bean.ui.ServiceProduct;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;

public class ProductToServiceAdapter {
	/**
	 * 
	 */
	public ProductToServiceAdapter() {
	}

	public Object adaptFromServiceID(Context ctx, Object arg1) throws HomeException {
		Long serviceId = (Long)arg1;
		Product product = DataModelAdaptionSupport.getProductFromServiceId(ctx, serviceId);
		return adaptFromProduct(ctx, product);
	}
	
	public Object adaptFromProduct(Context ctx, Object arg1) throws HomeException {
		boolean serviceRecreatedsucessfully = true;
		Product product = (Product)arg1;
		Service service = new Service();
		setFromProduct(product,service);
		ServiceProduct nVersion = setFromServiceProduct(ctx,product,service);
		if(nVersion!=null){
			setServiceExtension(service,nVersion);
			setServiceParameters(service,nVersion);
			PricingVersion priceVersion = setFromPricingVersion(ctx,nVersion,service);
			if(priceVersion!=null){
				AdjustmentTypeNVersion adjustmentType = setFromAdjustmentVersion(ctx,priceVersion,service);
				if(adjustmentType == null)
					serviceRecreatedsucessfully = false;
			}else{
				serviceRecreatedsucessfully = false;
			}
		}else{
			serviceRecreatedsucessfully = false;
		}
		if(serviceRecreatedsucessfully)
			return service;
		else
			return null;
	}

	private void setServiceParameters(Service service, ServiceProduct nVersion) {
		service.setServiceParameters(nVersion.getServiceParameters());
	}

	private void setServiceExtension(Service service, ServiceProduct nVersion) {
	}

	private AdjustmentTypeNVersion setFromAdjustmentVersion(Context ctx,PricingVersion priceVersion, Service service) throws HomeInternalException, HomeException {
		//AdjustmentType adjustmentType = OldBeanAdapterSupport.getAssociatedAdjustmentType(ctx, priceVersion.getAdjustmenttype(), priceVersion.getAdjustmenttypeVid());
		AdjustmentTypeNVersion adjustmentTypeNVersion =  DataModelAdaptionSupport.getAssociatedAdjustmentTypeNVersion(ctx, priceVersion.getAdjustmenttype(), priceVersion.getAdjustmenttypeVid());
		
		if(adjustmentTypeNVersion!=null){
			service.setAdjustmentType(new Long(adjustmentTypeNVersion.getCode()).intValue());
			service.setAdjustmentGLCode(adjustmentTypeNVersion.getGLCode());
			service.setBillGroupID(adjustmentTypeNVersion.getBillGroupID());
			service.setTaxAuthority(adjustmentTypeNVersion.getTaxAuthority());
			
			/*Map<Integer, AdjustmentInfo> adjustmentSpidInfo  = adjustmentType.getAdjustmentSpidInfo();
			if(adjustmentSpidInfo!=null){
				Integer key = Integer.valueOf(service.getSpid());
		        AdjustmentInfo info = adjustmentSpidInfo.get(key);
				
				service.setAdjustmentInvoiceDesc(info.getInvoiceDesc());
				
			}else{
				adjustmentType = null;
			}*/
		}
		return adjustmentTypeNVersion;
	}

	private PricingVersion setFromPricingVersion(Context ctx, ServiceProduct nVersion, Service service) throws HomeInternalException, HomeException 
	{
		PricingVersion pricingVersion = DataModelAdaptionSupport.getAssociatedPricingVersion(ctx, nVersion.getCompatibilityGroup());
		if(pricingVersion!=null){
			service.setRecurrenceInterval(pricingVersion.getRecurrenceInterval());
			service.setDynamicBilling(pricingVersion.getDynamicBilling());
			//service.setBillingMonth(pricingVersion.getBillingMonth());
			//service.setPaymentOption(pricingVersion.getPaymentOption());
			service.setFeePersonalizationRule(pricingVersion.getFeePersonalizationRule());
			service.setActivationFee(pricingVersion.getActivationFee());
			service.setPostpaidSubCreationOnly(pricingVersion.getPostpaidsubscriptiononly());
			//service.setChargePrepaidSubscribers(pricingVersion.getChargePrepaidSubscribers());
			service.setRefundable(pricingVersion.getRefundable());
			service.setRefundOption(pricingVersion.getRefundOption());
			service.setFeePersonalizationAllowed(pricingVersion.getFeePersonalizationAllowed());
			service.setForceCharging(pricingVersion.getForceCharging());
			//service.setFirstMonthFree(pricingVersion.getFirstMonthFree());
			//service.setBalanceIndicator(pricingVersion.getBalanceIndicator());
			service.setAdjustmentType(new Long(pricingVersion.getAdjustmenttype()).intValue());
		}
		return pricingVersion;
	}

	private ServiceProduct setFromServiceProduct(Context ctx,Product product, Service service) throws HomeInternalException, HomeException
	{
		ServiceProduct nVersion = DataModelAdaptionSupport.getLatestServiceVersion(ctx, product.getProductId());
		if(nVersion!=null){
			nVersion.setContext(ctx);
			service.setSubscriptionType(nVersion.getSubscriptionType());
			service.setTechnology(nVersion.getTechnologyType());
			service.setChargeScheme(nVersion.getChargeScheme());
			service.setRecurrenceType(nVersion.getRecurrenceType());
			service.setStartDate(nVersion.getStartDate());
			service.setEndDate(nVersion.getEndDate());
			service.setValidity(nVersion.getValidity());
			service.setFixedInterval(nVersion.getFixedInterval());
			service.setType(nVersion.getType());
			service.setServiceCategory(nVersion.getServiceCategory());
			service.setCallingGroupType(nVersion.getCallingGroupType().getIndex());
			service.setCugTemplateID(nVersion.getCugTemplateID());
			service.setVmPlanId(nVersion.getVmPlanId());
			service.setXmlProvSvcType(nVersion.getXmlProvSvcType());
			service.setSPGServiceType(nVersion.getSPGServiceType());
			service.setAllowCarryOver(nVersion.getAllowCarryOver());
			service.setExecutionOrder(nVersion.getExecutionOrder());
			service.setBillDisplayOrder(nVersion.getBillDisplayOrder());
			service.setEnableCLTC(nVersion.getEnableCLTC());
			service.setClctThreshold(nVersion.getClctThreshold());
			service.setReprovisionOnActive(nVersion.getReprovisionOnActive());
			service.setSmartSuspension(nVersion.getSmartSuspension());
			service.setChargeableWhileSuspended(nVersion.getChargeableWhileSuspended());
			service.setRestrictProvisioning(nVersion.getRestrictProvisioning());
			service.setShowInInvoice(nVersion.getShowInInvoice());
			service.setShowZeroAmountInInvoice(nVersion.getShowZeroAmountInInvoice());
			service.setExternalServiceCode(nVersion.getExternalServiceCode());
			//service.setUnifiedServiceID(nVersion.getUnifiedServiceID());
			service.setHandler(nVersion.getHandler());
			//service.setPayerAllowed(nVersion.getPayerAllowed());
			service.setCustomDescAllowed(nVersion.getCustomDescAllowed());
			service.setPrepaidProvisionConfigs(nVersion.getPrepaidProvisionConfigs());
			service.setPrepaidUnprovisionConfigs(nVersion.getPrepaidUnprovisionConfigs());
			service.setPrepaidSuspendConfigs(nVersion.getPrepaidSuspendConfigs());
			service.setPrepaidResumeConfigs(nVersion.getPrepaidResumeConfigs());
			service.setProvisionConfigs(nVersion.getProvisionConfigs());
			service.setUnprovisionConfigs(nVersion.getUnprovisionConfigs());
			service.setSuspendConfigs(nVersion.getSuspendConfigs());
			service.setResumeConfigs(nVersion.getResumeConfigs());
			service.setPermission(nVersion.getPermission());
			//service.setChargeInBSS(nVersion.getChargeInBSS());
			service.setServiceSubType(nVersion.getServiceSubType());
			service.setServiceParameters(nVersion.getServiceParameters());
			//service.setPriority(nVersion.getPriority());
			}
		return nVersion;
	}

	private void setFromProduct(Product product, Service service) 
	{
		service.setID(product.getProductId());
		service.setName(product.getProductType().getName());
		service.setSpid(product.getSpid());
	}

}
