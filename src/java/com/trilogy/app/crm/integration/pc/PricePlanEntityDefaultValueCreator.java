package com.trilogy.app.crm.integration.pc;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.trilogy.app.crm.bean.ChargingCycleEnum;
import com.trilogy.app.crm.bean.ExpiryExtensionModeEnum;
import com.trilogy.app.crm.bean.core.PricePlan;
import com.trilogy.app.crm.bean.PricePlanFunctionEnum;
import com.trilogy.app.crm.bean.core.PricePlanVersion;
import com.trilogy.app.crm.bean.payment.ContractFeeFrequencyEnum;
import com.trilogy.app.crm.bean.ui.ServiceProduct;
import com.trilogy.app.crm.bean.ui.TechnicalServiceTemplate;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextLocator;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.util.crmapi.wsdl.v3_0.api.core.io.common.v1.CharIO;
import com.trilogy.util.crmapi.wsdl.v3_0.api.core.io.common.v1.CharSpecType;
import com.trilogy.util.crmapi.wsdl.v3_0.api.core.io.common.v1.CharValSpecIO;

public class PricePlanEntityDefaultValueCreator {

	public static void setPPDefaultValues(Context ctx, PricePlan pp){
		
		LogSupport.debug(ctx, PricePlanEntityDefaultValueCreator.class, "[PricePlanEntityDefaultValueCreator.setPPDefaultValues] updating price plan with default values");
		
		pp.setPricePlanFunction(PricePlanFunctionEnum.NORMAL);
		pp.setPricePlanGroup(-1);
		pp.setPricePlanPriority(10000);
		pp.setSubscriptionLevel(1);
		pp.setVoiceRatePlan("");
		pp.setSMSRatePlan("");
		pp.setDataRatePlan("");
		pp.setSuspensionOffset(0);
		pp.setExpiryExtention(0);
		pp.setExpiryExtensionMode(ExpiryExtensionModeEnum.EXTEND_FROM_CURRENT_EXPIRYDATE);
		pp.setCurrentVersionChargeCycle(ChargingCycleEnum.MONTHLY);
		//pp.setCurrentVersionCharge(); not sure what to set
		//pp.setCurrentVersion(); not sure what to set
		//pp.setNextVersion(); not sure what to set
		pp.setApplyContractDurationCriteria(false);
		pp.setMinContractDuration(-1);
		pp.setMaxContractDuration(-1);
		pp.setContractDurationUnits(ContractFeeFrequencyEnum.DAY);
		pp.setHybrid(false);
		pp.setApplyMinimumCharge(false);
		pp.setMinimumCharge(0);
		//pp.setFORMULANAME field not present;
		pp.setApplyZeroUsageDiscount(false);
		pp.setDiscountClass(-1);
		pp.setCatalogDriven(true);
		//pp.setDescription field not present;
		
	}

	public static void setPPVersionDefaultValues(Context context,PricePlanVersion ppVersion) {
		
		LogSupport.debug(context, PricePlanEntityDefaultValueCreator.class, "[PricePlanEntityDefaultValueCreator.setPPVersionDefaultValues] updating price plan version with default values");
		
		ppVersion.setDeposit(0);
		ppVersion.setCreditLimit(0);
		ppVersion.setDefaultPerMinuteAirRate(0);
		ppVersion.setOverusageDataRate(0);
		ppVersion.setOverusageSmsRate(0);
		ppVersion.setOverusageVoiceRate(0);
		ppVersion.setChargeCycle(ChargingCycleEnum.MONTHLY);
		ppVersion.setApplyFreeUnits(false);
		ppVersion.setCharge(0);
		ppVersion.setEnabled(false);
		//This default value will be over written with the ProductIDs when update will be invoked.
		ppVersion.setDescription("Default");
	}

	public static void setServiceProductDefaultValues(TechnicalServiceTemplate tst,ServiceProduct serviceProduct) {
		
		LogSupport.debug(ContextLocator.locate(), "PricePlanEntityDefaultValueCreator", "[setServiceProductDefaultValues] updating service product with default values from technical service template id:"+String.valueOf(tst.getID()));
		serviceProduct.setSubscriptionType(tst.getSubscriptionType());
		serviceProduct.setType(tst.getType());
		serviceProduct.setTechnicalServiceTemplateId(tst.getID());
		serviceProduct.setServiceCategory(tst.getServiceCategory());
		serviceProduct.setVmPlanId(tst.getVmPlanId());
		serviceProduct.setXmlProvSvcType(tst.getXmlProvSvcType());
		serviceProduct.setSPGServiceType(tst.getSPGServiceType());
		serviceProduct.setExecutionOrder(tst.getExecutionOrder());
		serviceProduct.setBillDisplayOrder(tst.getBillDisplayOrder());
		serviceProduct.setEnableCLTC(tst.getEnableCLTC());
		serviceProduct.setClctThreshold(tst.getClctThreshold());
		serviceProduct.setReprovisionOnActive(tst.getReprovisionOnActive());
		serviceProduct.setSmartSuspension(tst.getSmartSuspension());
		serviceProduct.setChargeableWhileSuspended(tst.getChargeableWhileSuspended());
		serviceProduct.setRestrictProvisioning(tst.getRestrictProvisioning());
		serviceProduct.setShowInInvoice(tst.getShowInInvoice());
		serviceProduct.setShowZeroAmountInInvoice(tst.getShowZeroAmountInInvoice());
		serviceProduct.setExternalServiceCode(tst.getExternalServiceCode());
		serviceProduct.setHandler(tst.getHandler());
		//serviceProduct.setPayerAllowed(tst.getPayerAllowed());
		serviceProduct.setCustomDescAllowed(tst.getCustomDescAllowed());
		serviceProduct.setPrepaidProvisionConfigs(tst.getPrepaidProvisionConfigs());
		serviceProduct.setPrepaidResumeConfigs(tst.getPrepaidResumeConfigs());
		serviceProduct.setPrepaidSuspendConfigs(tst.getPrepaidSuspendConfigs());
		serviceProduct.setPrepaidUnprovisionConfigs(tst.getPrepaidUnprovisionConfigs());
		serviceProduct.setProvisionConfigs(tst.getProvisionConfigs());
		serviceProduct.setResumeConfigs(tst.getResumeConfigs());
		serviceProduct.setSuspendConfigs(tst.getSuspendConfigs());
		serviceProduct.setUnprovisionConfigs(tst.getUnprovisionConfigs());
		serviceProduct.setServiceSubType(tst.getServiceSubType());
		serviceProduct.setServiceParameters(tst.getServiceParameters());
		serviceProduct.setServiceExtensions(tst.getServiceExtensions());
		serviceProduct.setPriority(tst.getPriority());
		serviceProduct.setCallingGroupType(tst.getCallingGroupType());
		serviceProduct.setCugTemplateID(tst.getCugTemplateID());
		serviceProduct.setPermission(getRootPermission(tst.getSpid(),tst.getType().getIndex(),tst.getIdentifier()));
		serviceProduct.setSpid(tst.getSpid());
	}
	
	public static void setPackageProductDefaultValues(TechnicalServiceTemplate tst, com.redknee.app.crm.bean.ui.PackageProduct packageProduct) {
		
		LogSupport.debug(ContextLocator.locate(), PricePlanEntityDefaultValueCreator.class, "[setPackageProductDefaultValues] updating package product with default values from technical service template id:"+String.valueOf(tst.getID()));
		
		//ApprovalRequired is not available in TST
		//packageProduct.setApprovalRequired();
		
		packageProduct.setBillDisplayOrder(tst.getBillDisplayOrder());		
		packageProduct.setChargeableWhileSuspended(tst.getChargeableWhileSuspended());
		packageProduct.setClctThreshold(tst.getClctThreshold());
		packageProduct.setCustomDescAllowed(tst.getCustomDescAllowed());
		packageProduct.setEnableCLTC(tst.getEnableCLTC());
		packageProduct.setExecutionOrder(tst.getExecutionOrder());
		packageProduct.setExternalServiceCode(tst.getExternalServiceCode());
		//packageProduct.setPayerAllowed(tst.getPayerAllowed());
		//Permission is not available in TST
		packageProduct.setPermission(getRootPermission(tst.getSpid(),tst.getType().getIndex(),tst.getIdentifier()));
		packageProduct.setPrepaidProvisionConfigs(tst.getPrepaidProvisionConfigs());
		packageProduct.setPrepaidResumeConfigs(tst.getPrepaidResumeConfigs());
		packageProduct.setPrepaidSuspendConfigs(tst.getPrepaidSuspendConfigs());
		packageProduct.setPrepaidUnprovisionConfigs(tst.getPrepaidUnprovisionConfigs());
		packageProduct.setPriority(tst.getPriority());
		packageProduct.setProvisionConfigs(tst.getProvisionConfigs());
		packageProduct.setReprovisionOnActive(tst.getReprovisionOnActive());
		packageProduct.setRestrictProvisioning(tst.getRestrictProvisioning());
		packageProduct.setResumeConfigs(tst.getResumeConfigs());
		packageProduct.setServiceCategory(tst.getServiceCategory());
		packageProduct.setShowInInvoice(tst.getShowInInvoice());
		packageProduct.setShowZeroAmountInInvoice(tst.getShowZeroAmountInInvoice());
		packageProduct.setSmartSuspension(tst.getSmartSuspension());
		packageProduct.setSuspendConfigs(tst.getSuspendConfigs());
		packageProduct.setUnprovisionConfigs(tst.getUnprovisionConfigs());
		
		//Since these are not available as compatibility specs, adding them from template
		packageProduct.setClctThreshold(tst.getClctThreshold());
		
	}
	
	
	
public static void setResourceProductDefaultValues(TechnicalServiceTemplate tst, com.redknee.app.crm.bean.ui.ResourceProduct resourceProduct) {
		
		LogSupport.debug(ContextLocator.locate(), PricePlanEntityDefaultValueCreator.class, "[setResourceProductDefaultValues] updating resource product with default values from technical service template id:"+String.valueOf(tst.getID()));
		
		//ApprovalRequired is not available in TST
		//resourceProduct.setApprovalRequired();
		resourceProduct.setTechnicalServiceTemplateId(tst.getID());	
		resourceProduct.setSubscriptionType(tst.getSubscriptionType());
		resourceProduct.setBillDisplayOrder(tst.getBillDisplayOrder());
		resourceProduct.setChargeableWhileSuspended(tst.getChargeableWhileSuspended());
		resourceProduct.setClctThreshold(tst.getClctThreshold());
		resourceProduct.setCustomDescAllowed(tst.getCustomDescAllowed());
		resourceProduct.setEnableCLTC(tst.getEnableCLTC());
		resourceProduct.setExecutionOrder(tst.getExecutionOrder());
		resourceProduct.setExternalServiceCode(tst.getExternalServiceCode());
		//resourceProduct.setPayerAllowed(tst.getPayerAllowed());
		//Permission is not available in TST
		resourceProduct.setPermission(getRootPermission(tst.getSpid(),tst.getType().getIndex(),tst.getIdentifier()));
		resourceProduct.setPrepaidProvisionConfigs(tst.getPrepaidProvisionConfigs());
		resourceProduct.setPrepaidResumeConfigs(tst.getPrepaidResumeConfigs());
		resourceProduct.setPrepaidSuspendConfigs(tst.getPrepaidSuspendConfigs());
		resourceProduct.setPrepaidUnprovisionConfigs(tst.getPrepaidUnprovisionConfigs());
		resourceProduct.setPriority(tst.getPriority());
		resourceProduct.setProvisionConfigs(tst.getProvisionConfigs());
		resourceProduct.setReprovisionOnActive(tst.getReprovisionOnActive());
		resourceProduct.setRestrictProvisioning(tst.getRestrictProvisioning());
		resourceProduct.setResumeConfigs(tst.getResumeConfigs());
		resourceProduct.setServiceCategory(tst.getServiceCategory());
		resourceProduct.setShowInInvoice(tst.getShowInInvoice());
		resourceProduct.setShowZeroAmountInInvoice(tst.getShowZeroAmountInInvoice());
		resourceProduct.setSmartSuspension(tst.getSmartSuspension());
		resourceProduct.setSuspendConfigs(tst.getSuspendConfigs());
		resourceProduct.setUnprovisionConfigs(tst.getUnprovisionConfigs());		
		resourceProduct.setClctThreshold(tst.getClctThreshold());
		
	}
	
	private static String getRootPermission(int spid, short type,
			long identifier) {
		return  ROOT_PERMISSION + "." + spid + "." + type + "." + identifier;
	}
    public static String ROOT_PERMISSION = "app.crm.service";
    
    
	public static void setCharacteristics(com.redknee.app.crm.bean.ui.PackageProduct packageProduct, ServiceVersionModel version) {
		
		Context ctx = ContextLocator.locate();
		boolean debugEnabled = LogSupport.isDebugEnabled(ctx);
		if (debugEnabled) {
			LogSupport.debug(ctx, PricePlanEntityDefaultValueCreator.class, "[setCharacteristics] updating product characteristics with values from Service Version model service Id  :"+version.getServiceId());
		}
		
		Map<String, Object> characteristicsMap = version.getCharacteristics();
		Set<String> charSpecNames = characteristicsMap.keySet();
		for (String charSpecName : charSpecNames) {
			
			CharIO characteristic = (CharIO) characteristicsMap.get(charSpecName);
			
			if (debugEnabled) {
				LogSupport.debug(ctx, PricePlanEntityDefaultValueCreator.class, "[setCharacteristics] updating product characteristic for :"+charSpecName);
			}
			
			if(charSpecName.equals("Service Category")){
				String value = characteristic.getCharValues().getCharValue()[0].getValue();
				packageProduct.setServiceCategory(Long.valueOf(value));
			}
			if(charSpecName.equals("Execution Order")){
				String value = characteristic.getCharValues().getCharValue()[0].getValue();
				packageProduct.setExecutionOrder(Integer.valueOf(value));
			}
			if(charSpecName.equals("Reprovision on Active")){
				String value = characteristic.getCharValues().getCharValue()[0].getValue();
				packageProduct.setReprovisionOnActive(Boolean.valueOf(value));
			}
			if(charSpecName.equals("Smart Suspension")){
				String value = characteristic.getCharValues().getCharValue()[0].getValue();
				packageProduct.setSmartSuspension(Boolean.valueOf(value));
			}
			if(charSpecName.equals("Chargeable While Suspended/In Arrears")){
				String value = characteristic.getCharValues().getCharValue()[0].getValue();
				packageProduct.setChargeableWhileSuspended(Boolean.valueOf(value));
			}
			if(charSpecName.equals("Restrict Provisioning")){
				
			}
			if(charSpecName.equals("Is Payer Allowed?")){
				String value = characteristic.getCharValues().getCharValue()[0].getValue();
				//packageProduct.setPayerAllowed(Boolean.valueOf(value));
			}
			if(charSpecName.equals("Charge In BSS")){
				
			}
			if(charSpecName.equals("Service Sub Type")){
				String value = characteristic.getCharValues().getCharValue()[0].getValue();
				packageProduct.setBillDisplayOrder(Integer.valueOf(value));
			}
			if(charSpecName.equals("Priority")){
				String value = characteristic.getCharValues().getCharValue()[0].getValue();
				packageProduct.setPriority(Integer.valueOf(value));
			}
			if(charSpecName.equals("Order of Bill Display")){
				
			}
			if(charSpecName.equals("Show In Invoice")){
				String value = characteristic.getCharValues().getCharValue()[0].getValue();
				packageProduct.setShowInInvoice(Boolean.valueOf(value));
			}
			if(charSpecName.equals("Show Zero Amount In Invoice")){
				String value = characteristic.getCharValues().getCharValue()[0].getValue();
				packageProduct.setShowZeroAmountInInvoice(Boolean.valueOf(value));
			}
			if(charSpecName.equals("Customized Invoice Description Allowed")){
				String value = characteristic.getCharValues().getCharValue()[0].getValue();
				packageProduct.setCustomDescAllowed(Boolean.valueOf(value));
			}
		}
	}
}
