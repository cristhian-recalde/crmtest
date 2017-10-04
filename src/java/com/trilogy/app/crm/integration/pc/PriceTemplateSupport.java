package com.trilogy.app.crm.integration.pc;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.trilogy.app.crm.bean.ServicePeriodEnum;
import com.trilogy.app.crm.bean.ui.CompatibilitySpecs;
import com.trilogy.app.crm.bean.ui.PriceTemplate;
import com.trilogy.app.crm.bean.ui.PriceTemplateXInfo;
import com.trilogy.app.crm.bean.ui.TechnicalServiceTemplateXInfo;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextLocator;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.util.crmapi.wsdl.v3_0.api.core.io.common.v1.CharSpecType;
import com.trilogy.util.crmapi.wsdl.v3_0.api.core.io.common.v1.CompatibilitySpecIOSave;
import com.trilogy.util.crmapi.wsdl.v3_0.api.core.io.common.v1.ImportCharIOSave;
import com.trilogy.util.crmapi.wsdl.v3_0.api.pc.io.price.v1.Characteristics_type10;
import com.trilogy.util.crmapi.wsdl.v3_0.api.pc.io.price.v1.Characteristics_type8;
import com.trilogy.util.crmapi.wsdl.v3_0.api.pc.io.price.v1.CompatibilitySpecs_type7;
import com.trilogy.util.crmapi.wsdl.v3_0.api.pc.io.price.v1.CompatibilitySpecs_type9;
import com.trilogy.util.crmapi.wsdl.v3_0.api.pc.io.price.v1.OneTimePriceTemplateIOSave;
import com.trilogy.util.crmapi.wsdl.v3_0.api.pc.io.price.v1.OneTimePriceTemplateVersionIOSave;
import com.trilogy.util.crmapi.wsdl.v3_0.api.pc.io.price.v1.PriceTemplateIOSave;
import com.trilogy.util.crmapi.wsdl.v3_0.api.pc.io.price.v1.PriceTemplateVersionIOSave;
import com.trilogy.util.crmapi.wsdl.v3_0.api.pc.io.price.v1.RecurringPriceTemplateIOSave;
//import com.trilogy.util.crmapi.wsdl.v3_0.api.pc.io.technicalservice.v1.Characteristics_type5;
import com.trilogy.util.crmapi.wsdl.v3_0.api.pc.io.technicalservice.v1.RfssVersionIOSave;

/**
 * 
 * @author AChatterjee
 *
 */
public class PriceTemplateSupport {

	/**
	 * Creates an service instance of PriceTemplate from the BSS entity of PriceTemplate
	 * 
	 * @param ctx instance of {@link Context}
	 * @param priceTemplate instance of {@link PriceTemplate}
	 * @param priceTemplateServiceInstance instance of {@link PriceTemplateIOSave}
	 * 
	 * @return priceTemplateServiceInstance instance of {@link PriceTemplateIOSave}
	 * 
	 */
	public static PriceTemplateIOSave getServiceInstance(Context ctx, PriceTemplate priceTemplate, PriceTemplateIOSave priceTemplateServiceInstance) {

		List compatibilitySpecs = priceTemplate.getCompatibilitySpecs();
		boolean debugEnabled = LogSupport.isDebugEnabled(ctx);

		if (debugEnabled) {
			LogSupport.debug(ctx, PriceTemplateSupport.class, ("[getServiceInstance] Getting Compatibility Specs list:" +compatibilitySpecs.toString()));
		}
		
		if(priceTemplate.getChargeScheme() == ServicePeriodEnum.ONE_TIME){
			//For OneTimeFee Price Template
			OneTimePriceTemplateVersionIOSave version = priceTemplateCurrentVersion(ctx, priceTemplate, new OneTimePriceTemplateVersionIOSave());
			OneTimePriceTemplateIOSave oneTimeChargeTemplate =  new OneTimePriceTemplateIOSave();
			oneTimeChargeTemplate.setBusinessKey(String.valueOf(priceTemplate.getID()));
			oneTimeChargeTemplate.setName(priceTemplate.getName());
			oneTimeChargeTemplate.setDescription(priceTemplate.getDescription());
			oneTimeChargeTemplate.setSpid(String.valueOf(priceTemplate.getSpid()));
			oneTimeChargeTemplate.setSource(PCConstants.SOURCE);
			oneTimeChargeTemplate.setExternalKey(PCConstants.EXTERNAL_KEY);
			oneTimeChargeTemplate.setCurrentVersion(version);
			if (compatibilitySpecs.size() > 0) {
				if (debugEnabled) {
					LogSupport.debug(ctx, PriceTemplateSupport.class, ("[getServiceInstance] Setting Compatibility Specs for One Time Charge Template"));
				}
				oneTimeChargeTemplate.setCompatibilitySpecs(getCompatibilitySpecForOneTimePriceTemplate(compatibilitySpecs));
			}

			priceTemplateServiceInstance.setOneTimePriceTemplate(oneTimeChargeTemplate);
		}
		else {
			//For Recurring Price Template
			PriceTemplateVersionIOSave version = priceTemplateCurrentVersion(ctx, priceTemplate, new PriceTemplateVersionIOSave());
			RecurringPriceTemplateIOSave recurringChargeTemplate = new RecurringPriceTemplateIOSave();
			recurringChargeTemplate.setBusinessKey(String.valueOf(priceTemplate.getID()));
			recurringChargeTemplate.setName(priceTemplate.getName());
			recurringChargeTemplate.setDescription(priceTemplate.getDescription());
			recurringChargeTemplate.setSpid(String.valueOf(priceTemplate.getSpid()));
			recurringChargeTemplate.setSource(PCConstants.SOURCE);
			recurringChargeTemplate.setExternalKey(PCConstants.EXTERNAL_KEY);
			recurringChargeTemplate.setFrequency(priceTemplate.getChargeScheme().getName());
			recurringChargeTemplate.setCurrentVersion(version);
			if (compatibilitySpecs.size() > 0) {
				if (debugEnabled) {
					LogSupport.debug(ctx, PriceTemplateSupport.class, ("[getServiceInstance] Setting Compatibility Specs for Recurring Charge Template"));
				}
				recurringChargeTemplate.setCompatibilitySpecs(getCompatibilitySpecForReccuringPriceTemplate(compatibilitySpecs));
			}

			priceTemplateServiceInstance.setRecurringPriceTemplate(recurringChargeTemplate);
		}
		return priceTemplateServiceInstance;
	}

	/**
	 * Map the attributes of the current version of service for RecurringPriceTemplate
	 * 
	 * @param priceTemplate instance of {@link PriceTemplate}
	 * @param priceTemplateVersion instance of {@link PriceTemplateVersionIOSave}
	 * 
	 * @return priceTemplateVersion instance of {@link PriceTemplateVersionIOSave}
	 */
	private static PriceTemplateVersionIOSave priceTemplateCurrentVersion(Context ctx, PriceTemplate priceTemplate, PriceTemplateVersionIOSave priceTemplateVersion) {

		Characteristics_type8 charac = new Characteristics_type8();
		
		if (LogSupport.isDebugEnabled(ctx)) {
			LogSupport.debug(ctx, PriceTemplateSupport.class, ("[priceTemplateCurrentVersion] printing PriceTemplate.getID:" +priceTemplate.getID()));
		}
		
		priceTemplateVersion.setEffectiveFromDate(Calendar.getInstance());
		ImportCharIOSave[] importCharIOSave = priceTemplateNewCharacteristics(ctx, priceTemplate);
		if (importCharIOSave.length > 0) {
			charac.setCharacteristic(importCharIOSave);
			priceTemplateVersion.setCharacteristics(charac);
		}
		return priceTemplateVersion;
	}

	private static ImportCharIOSave[] priceTemplateNewCharacteristics(Context ctx, PriceTemplate priceTemplate) {
		
		List<ImportCharIOSave> importCharIOSaveList = new ArrayList<ImportCharIOSave>();
		PriceTemplateXInfo xinfo = PriceTemplateXInfo.instance();
		List<PropertyInfo> properties = xinfo.getProperties(ctx);
		PropertyInfo[] propertyInfo = properties.toArray(new PropertyInfo[properties.size()]);

		boolean debugEnabled = LogSupport.isDebugEnabled(ctx);

		if (debugEnabled) {
			LogSupport.debug(ctx, "PriceTemplateSupport", "[priceTemplateNewCharacteristics] printing propertyInfo Length: " + propertyInfo.length + "]");
		}
		
		String propertyInfoName = "";
		
		for (int i = 0; i < propertyInfo.length; i++) {

			propertyInfoName = propertyInfo[i].getName();
			if (debugEnabled) {
				LogSupport.debug(ctx, "PriceTemplateSupport", "[priceTemplateNewCharacteristics] printing propertyInfo Name: " + propertyInfoName + "]");
			}
			
			if ((propertyInfoName.equals("forceCharging")) && (priceTemplate.getForceChargingSendToUC())) {
				importCharIOSaveList.add(TemplateUtility.getCharacteristics(propertyInfo[i],
						String.valueOf(priceTemplate.getForceCharging()), TemplateUtility.getCharSpecIO(TemplateUtility.getCharValSpecForFreeEntry()),
						CharSpecType.value1, priceTemplate.getForceChargingNMflag()));
			}

			if ((propertyInfoName.equals("refundable")) && (priceTemplate.getRefundableSendToUC())) {
				importCharIOSaveList.add(TemplateUtility.getCharacteristics(propertyInfo[i],
						String.valueOf(priceTemplate.getRefundable()), TemplateUtility.getCharSpecIO(TemplateUtility.getCharValSpecForFreeEntry()),
						CharSpecType.value1, priceTemplate.getRefundableNMflag()));
			}
			
			/*if ((propertyInfoName.equals("paymentOption")) && (priceTemplate.getPaymentOptionSendToUC())) {
				importCharIOSaveList.add(TemplateUtility.getCharacteristics(propertyInfo[i],
						String.valueOf(priceTemplate.getPaymentOption().getIndex()),
						TemplateUtility.getCharSpecIO(TemplateUtility.getCharValSpecForEnum(priceTemplate.getPaymentOption())),
								CharSpecType.value2, priceTemplate.getPaymentOptionNMflag()));
			}*/

			if ((propertyInfoName.equals("refundOption")) && (priceTemplate.getRefundOptionSendToUC())) {
				importCharIOSaveList.add(TemplateUtility.getCharacteristics(propertyInfo[i],
						String.valueOf(priceTemplate.getRefundOption().getIndex()),
						TemplateUtility.getCharSpecIO(TemplateUtility.getCharValSpecForEnum(priceTemplate.getRefundOption())),
								CharSpecType.value2, priceTemplate.getRefundOptionNMflag()));
			}

		}

		return (ImportCharIOSave[]) importCharIOSaveList.toArray(new ImportCharIOSave[importCharIOSaveList.size()]);

	}

	/**
	 * Map the attributes of the current version of service for OneTimePriceTemplate
	 * 
	 * @param priceTemplate instance of {@link PriceTemplate}
	 * @param oneTimePriceTemplateVersion instance of {@link OneTimePriceTemplateVersionIOSave}
	 * 
	 * @return oneTimePriceTemplateVersion instance of {@link OneTimePriceTemplateVersionIOSave}
	 */
	private static OneTimePriceTemplateVersionIOSave priceTemplateCurrentVersion(Context ctx, PriceTemplate priceTemplate, OneTimePriceTemplateVersionIOSave oneTimePriceTemplateVersion) {

		oneTimePriceTemplateVersion.setEffectiveFromDate(Calendar.getInstance());
		
		return oneTimePriceTemplateVersion;
	
	}

	/**
	 * Create instance of {@link CompatibilitySpecs_type7} by iterating the compatibilitySpecs for OneTimePriceTemplate saved in BSS
	 * 
	 * @param compatibilitySpecs instance of {@link List}
	 * @return compatibilitySpecsServiceInstance instance of {@link CompatibilitySpecs_type7}
	 * 
	 */
	private static CompatibilitySpecs_type9 getCompatibilitySpecForOneTimePriceTemplate(List compatibilitySpecs) {

		CompatibilitySpecs_type9 compatibilitySpecsServiceInstance = new CompatibilitySpecs_type9();
		List<CompatibilitySpecIOSave> compatibilitySpec_List = new ArrayList<CompatibilitySpecIOSave>();

		for (Object object : compatibilitySpecs) {
			
			CompatibilitySpecs spec = (CompatibilitySpecs) object;
			CompatibilitySpecIOSave compatibilitySpecIOSave = new CompatibilitySpecIOSave();
			compatibilitySpecIOSave.setName(spec.getCompatibilitySpecsName());
			
			if (!spec.getCompatibilitySpecsDescription().equals("")) {
				compatibilitySpecIOSave.setDescription(spec.getCompatibilitySpecsDescription());
			}
			
			compatibilitySpec_List.add(compatibilitySpecIOSave);

		}

		CompatibilitySpecIOSave[] compatibilitySpecIOSaveArray = compatibilitySpec_List.toArray(new CompatibilitySpecIOSave[] {});
		compatibilitySpecsServiceInstance.setCompatibilitySpec(compatibilitySpecIOSaveArray);

		return compatibilitySpecsServiceInstance;
	}

	/**
	 * Create instance of {@link CompatibilitySpecs_type7} by iterating the compatibilitySpecs for ReccuringPriceTemplate saved in BSS
	 * 
	 * @param compatibilitySpecs instance of {@link List}
	 * @return compatibilitySpecsServiceInstance instance of {@link CompatibilitySpecs_type7}
	 * 
	 */
	private static CompatibilitySpecs_type7 getCompatibilitySpecForReccuringPriceTemplate(List compatibilitySpecs) {

		CompatibilitySpecs_type7 compatibilitySpecsServiceInstance = new CompatibilitySpecs_type7();
		List<CompatibilitySpecIOSave> compatibilitySpec_List = new ArrayList<CompatibilitySpecIOSave>();

		for (Object object : compatibilitySpecs) {
			
			CompatibilitySpecs spec = (CompatibilitySpecs) object;
			CompatibilitySpecIOSave compatibilitySpecIOSave = new CompatibilitySpecIOSave();
			compatibilitySpecIOSave.setName(spec.getCompatibilitySpecsName());
			
			if (!spec.getCompatibilitySpecsDescription().equals("")) {
				compatibilitySpecIOSave.setDescription(spec.getCompatibilitySpecsDescription());
			}
			
			compatibilitySpec_List.add(compatibilitySpecIOSave);

		}

		CompatibilitySpecIOSave[] compatibilitySpecIOSaveArray = compatibilitySpec_List.toArray(new CompatibilitySpecIOSave[] {});
		compatibilitySpecsServiceInstance.setCompatibilitySpec(compatibilitySpecIOSaveArray);

		return compatibilitySpecsServiceInstance;
	}


}
