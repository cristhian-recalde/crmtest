package com.trilogy.app.crm.integration.pc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.trilogy.app.crm.bean.GLCodeMapping;
import com.trilogy.app.crm.bean.GLCodeMappingHome;
//import com.trilogy.app.crm.bean.GLCodeN;
import com.trilogy.app.crm.bean.PricePlanVersionID;
import com.trilogy.app.crm.bean.PricePlanVersionIdentitySupport;
import com.trilogy.app.crm.bean.ServiceCategory;
import com.trilogy.app.crm.bean.ServiceCategoryHome;
import com.trilogy.app.crm.bean.ServiceCategoryIdentitySupport;
import com.trilogy.app.crm.bean.ServiceSubTypeEnum;
import com.trilogy.app.crm.bean.ComTemplateActionEnum;
import com.trilogy.app.crm.bean.ui.CompatibilitySpecs;
import com.trilogy.app.crm.bean.ui.TechnicalServiceTemplate;
import com.trilogy.app.crm.bean.ui.TechnicalServiceTemplateXInfo;
import com.trilogy.util.crmapi.wsdl.v3_0.api.core.io.common.v1.CharSpecType;
import com.trilogy.util.crmapi.wsdl.v3_0.api.core.io.common.v1.CharValIO;
import com.trilogy.util.crmapi.wsdl.v3_0.api.core.io.common.v1.CharValIORef;
import com.trilogy.util.crmapi.wsdl.v3_0.api.core.io.common.v1.CharValSpecIO;
import com.trilogy.util.crmapi.wsdl.v3_0.api.core.io.common.v1.CharValueSpecs_type2;
import com.trilogy.util.crmapi.wsdl.v3_0.api.core.io.common.v1.CharValues_type2;
import com.trilogy.util.crmapi.wsdl.v3_0.api.core.io.common.v1.CharValues_type3;
import com.trilogy.util.crmapi.wsdl.v3_0.api.core.io.common.v1.CompatibilitySpecIOSave;
import com.trilogy.util.crmapi.wsdl.v3_0.api.core.io.common.v1.ImportCharIOSave;
import com.trilogy.util.crmapi.wsdl.v3_0.api.core.io.common.v1.ImportCharSpecIO;
import com.trilogy.util.crmapi.wsdl.v3_0.api.core.io.common.v1.NetworkTechnologyIO;
import com.trilogy.util.crmapi.wsdl.v3_0.api.core.io.common.v1.NonVersionEntityStateChangeInput;
import com.trilogy.util.crmapi.wsdl.v3_0.api.core.io.common.v1.SubscriptionTypeIO;
import com.trilogy.util.crmapi.wsdl.v3_0.api.core.io.common.v1.YesNoIndicator;
import com.trilogy.util.crmapi.wsdl.v3_0.api.pc.io.technicalservice.v1.Characteristics_type3;
//import com.trilogy.util.crmapi.wsdl.v3_0.api.pc.io.technicalservice.v1.Characteristics_type5;
import com.trilogy.util.crmapi.wsdl.v3_0.api.pc.io.technicalservice.v1.CompatibilitySpecs_type3;
import com.trilogy.util.crmapi.wsdl.v3_0.api.pc.io.technicalservice.v1.RfssIOSave;
import com.trilogy.util.crmapi.wsdl.v3_0.api.pc.io.technicalservice.v1.RfssVersionIOSave;
import com.trilogy.util.snippet.log.Logger;
import com.trilogy.app.crm.integration.pc.PCConstants;
import com.trilogy.framework.core.platform.CoreSupport;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextLocator;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.xenum.AbstractEnum;
import com.trilogy.framework.xhome.xenum.EnumCollection;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.app.crm.bean.account.SubscriptionType;
import com.trilogy.app.crm.bean.account.SubscriptionTypeHome;

public class TechnicalServiceTemplateSupport {

	public static RfssIOSave technicalServiceName(final Context ctx,
			TechnicalServiceTemplate technicalServiceTemplate, RfssIOSave rfss)
			throws Exception {

		LogSupport.info(ctx, "TechnicalServiceTemplateSupport", "[technicalServiceName] Start");

		if (LogSupport.isDebugEnabled(ctx)) {
			LogSupport.debug(ctx, "TechnicalServiceTemplateSupport",
				"[technicalServiceName] Printing technicalServiceTemplate: ID="+String.valueOf(technicalServiceTemplate.getID())
						+ ", Name=" + technicalServiceTemplate.getName()
						+ ", Desc=" + technicalServiceTemplate.getDescription()
						+ ", Spid=" + String.valueOf(technicalServiceTemplate.getSpid()));
		}
		
		RfssVersionIOSave versionIO = technicalServiceCurrentVersion(
				technicalServiceTemplate, new RfssVersionIOSave());
		
		rfss.setBusinessKey(String.valueOf(technicalServiceTemplate.getID()));
		rfss.setName(technicalServiceTemplate.getName());
		rfss.setDescription(technicalServiceTemplate.getDescription());
		rfss.setSource(PCConstants.SOURCE);
		// rfss.setGeneratesUsage(YesNoIndicator.NO);
		rfss.setExternalKey(PCConstants.EXTERNAL_KEY);
		rfss.setCurrentVersion(versionIO);
		rfss.setSpid(String.valueOf(technicalServiceTemplate.getSpid()));

		SubscriptionTypeIO param = new SubscriptionTypeIO();
		Home subscriptionTypeHome = (Home) ctx.get(SubscriptionTypeHome.class);
		SubscriptionType subscriptionType = (SubscriptionType) subscriptionTypeHome
				.find(technicalServiceTemplate.getSubscriptionType());

		// Updated for BSS-4499 Point 8
		param.setName(String.valueOf(subscriptionType.getId()));
		param.setDescription(subscriptionType.getName());

		rfss.setSubscriptionType(param);

		NetworkTechnologyIO ntwkTech = new NetworkTechnologyIO();
		// Updated for BSS-4499 Point 8
		ntwkTech.setName(Short.toString(technicalServiceTemplate
				.getTechnology().getIndex()));
		ntwkTech.setDescription(technicalServiceTemplate.getTechnology()
				.getDescription());

		rfss.setSupportedNetworkTechnology(ntwkTech);

		List compatibilitySpecs = technicalServiceTemplate
				.getCompatibilitySpecs();
		if (compatibilitySpecs.size() > 0) {
			rfss.setCompatibilitySpecs(getCompatibilitySpec(ctx, compatibilitySpecs));
		}
		
		return rfss;
	}

	private static CompatibilitySpecs_type3 getCompatibilitySpec(Context ctx, List compatibilitySpecs) {

		CompatibilitySpecs_type3 compatibilitySpecs_type3 = new CompatibilitySpecs_type3();
		List<CompatibilitySpecIOSave> compatibilitySpec_List = new ArrayList<CompatibilitySpecIOSave>();

		for (Object object : compatibilitySpecs) {

			CompatibilitySpecs spec = (CompatibilitySpecs) object;
			CompatibilitySpecIOSave compatibilitySpecIOSave = new CompatibilitySpecIOSave();
			compatibilitySpecIOSave.setName(spec.getCompatibilitySpecsName());
			
			if (!spec.getCompatibilitySpecsDescription().equals(""))
				compatibilitySpecIOSave.setDescription(spec
						.getCompatibilitySpecsDescription());
			
			compatibilitySpec_List.add(compatibilitySpecIOSave);

		}

		CompatibilitySpecIOSave[] compatibilitySpecIOSaveArray = compatibilitySpec_List
				.toArray(new CompatibilitySpecIOSave[] {});

		compatibilitySpecs_type3
				.setCompatibilitySpec(compatibilitySpecIOSaveArray);

		if (LogSupport.isDebugEnabled(ctx)) {
			LogSupport.debug(ctx, "TechnicalServiceTemplateSupport",
					"[getCompatibilitySpec] Printing CompatibilitySpecs_type3:"
							+ compatibilitySpecs_type3);
		}

		return compatibilitySpecs_type3;
	}

	public static RfssVersionIOSave technicalServiceCurrentVersion(
			TechnicalServiceTemplate technicalServiceTemplate,
			RfssVersionIOSave versionIO) {

		Characteristics_type3 charac = new Characteristics_type3();

		// changes as per new wsdl:
		ImportCharIOSave[] importCharIOSave = technicalServiceNewCharacteristics(technicalServiceTemplate);
		
		if (importCharIOSave.length > 0) {
			charac.setCharacteristic(importCharIOSave);
			versionIO.setCharacteristics(charac);
		}
		
		return versionIO;
	}

	public static ImportCharIOSave[] technicalServiceNewCharacteristics(
			TechnicalServiceTemplate technicalServiceTemplate) {
		
		Context ctx = ContextLocator.locate();
		boolean debugEnabled = LogSupport.isDebugEnabled(ctx);
		
		List<ImportCharIOSave> importCharIOSaveList = new ArrayList<ImportCharIOSave>();
		
		TechnicalServiceTemplateXInfo xinfo = TechnicalServiceTemplateXInfo
				.instance();
		List<PropertyInfo> properties = xinfo.getProperties(ctx);
		PropertyInfo[] propertyInfo = properties
				.toArray(new PropertyInfo[properties.size()]);

		if (debugEnabled) {
			LogSupport.debug(ctx, "TechnicalServiceTemplateSupport",
				"[technicalServiceNewCharacteristics] Printing TechnicalServiceTemplateXInfo Property Length:"
						+ String.valueOf(propertyInfo.length));
		}
		
		String propertyInfoName = "";
		
		for (int i = 0; i < propertyInfo.length; i++) {
			
			propertyInfoName = propertyInfo[i].getName();
			
			if (debugEnabled) {
				LogSupport.debug(ctx, "TechnicalServiceTemplateSupport",
					"[technicalServiceNewCharacteristics] Printing TechnicalServiceTemplateXInfo Property getName:"
							+ propertyInfoName + "]");
			}
			
			if ((propertyInfoName.equals("type"))
					&& (technicalServiceTemplate.getTypeSendTOpc())) {
				importCharIOSaveList
						.add(getCharacteristics(
								propertyInfo[i],
								String.valueOf(technicalServiceTemplate
										.getType().getIndex()),
								getCharSpecIO(getCharValSpecForEnum(technicalServiceTemplate
										.getType())), CharSpecType.value2,
								technicalServiceTemplate.getTypeNMflag()));
			}
			
			if ((propertyInfoName.equals("serviceCategory"))
					&& (technicalServiceTemplate.getServiceCategorySendTOpc())) {
				importCharIOSaveList
						.add(getCharacteristics(
								propertyInfo[i],
								String.valueOf(technicalServiceTemplate
										.getServiceCategory()),
								getCharSpecIO(getCharValSpecForServiceCat(ctx,
										technicalServiceTemplate
												.getServiceCategory())),
								CharSpecType.value2, technicalServiceTemplate
										.getServiceCategoryNMflag()));
			}
			
			if ((propertyInfoName.equals("callingGroupType"))
					&& (technicalServiceTemplate.getCallingGroupTypeSendTOpc())) {
				importCharIOSaveList
						.add(getCharacteristics(
								propertyInfo[i],
								String.valueOf(technicalServiceTemplate
										.getCallingGroupType().getIndex()),
								getCharSpecIO(getCharValSpecForEnum(technicalServiceTemplate
										.getCallingGroupType())),
								CharSpecType.value2, technicalServiceTemplate
										.getCallingGroupTypeNMflag()));
			}
			
			if ((propertyInfoName.equals("cugTemplateID"))
					&& (technicalServiceTemplate.getCugTemplateIDSendTOpc())) {
				importCharIOSaveList.add(getCharacteristics(propertyInfo[i],
						String.valueOf(technicalServiceTemplate
								.getCugTemplateID()),
						getCharSpecIO(getCharValSpecForFreeEntry()),
						CharSpecType.value3, technicalServiceTemplate
								.getCugTemplateIDNMflag()));
			}
			
			if ((propertyInfoName.equals("executionOrder"))
					&& (technicalServiceTemplate.getExecutionOrderSendTOpc())) {
				importCharIOSaveList.add(getCharacteristics(propertyInfo[i],
						String.valueOf(technicalServiceTemplate
								.getExecutionOrder()),
						getCharSpecIO(getCharValSpecForFreeEntry()),
						CharSpecType.value3, technicalServiceTemplate
								.getExecutionOrderNMflag()));
			}
			
			if ((propertyInfoName.equals("reprovisionOnActive"))
					&& (technicalServiceTemplate
							.getReprovisionOnActiveSendTOpc())) {
				importCharIOSaveList.add(getCharacteristics(propertyInfo[i],
						String.valueOf(technicalServiceTemplate
								.getReprovisionOnActive()),
						getCharSpecIO(getCharValSpecForFreeEntry()),
						CharSpecType.value1, technicalServiceTemplate
								.getReprovisionOnActiveNMflag()));
			}
			
			if ((propertyInfoName.equals("SmartSuspension"))
					&& (technicalServiceTemplate.getSmartSuspensionSendTOpc())) {
				importCharIOSaveList.add(getCharacteristics(propertyInfo[i],
						String.valueOf(technicalServiceTemplate
								.getSmartSuspension()),
						getCharSpecIO(getCharValSpecForFreeEntry()),
						CharSpecType.value1, technicalServiceTemplate
								.getSmartSuspensionNMflag()));
			}

			if ((propertyInfoName.equals("chargeableWhileSuspended"))
					&& (technicalServiceTemplate
							.getChargeableWhileSuspendedSendTOpc())) {
				importCharIOSaveList.add(getCharacteristics(propertyInfo[i],
						String.valueOf(technicalServiceTemplate
								.getChargeableWhileSuspended()),
						getCharSpecIO(getCharValSpecForFreeEntry()),
						CharSpecType.value1, technicalServiceTemplate
								.getChargeableWhileSuspendedNMflag()));
			}
			
			if ((propertyInfoName.equals("chargeableInDurationSuspended"))
					&& (technicalServiceTemplate
							.getChargeableInDurationSuspendedSendTOpc())) {
				importCharIOSaveList.add(getCharacteristics(propertyInfo[i],
						String.valueOf(technicalServiceTemplate
								.getChargeableInDurationSuspended()),
						getCharSpecIO(getCharValSpecForFreeEntry()),
						CharSpecType.value1, technicalServiceTemplate
								.getChargeableInDurationSuspendedNMflag()));
			}
			
			if ((propertyInfoName.equals("restrictProvisioning"))
					&& (technicalServiceTemplate
							.getRestrictProvisioningSendTOpc())) {
				importCharIOSaveList.add(getCharacteristics(propertyInfo[i],
						String.valueOf(technicalServiceTemplate
								.getRestrictProvisioning()),
						getCharSpecIO(getCharValSpecForFreeEntry()),
						CharSpecType.value1, technicalServiceTemplate
								.getRestrictProvisioningNMflag()));
			}
			
			/*if ((propertyInfoName.equals("payerAllowed"))
					&& (technicalServiceTemplate.getPayerAllowedSendTOpc())) {
				importCharIOSaveList.add(getCharacteristics(propertyInfo[i],
						String.valueOf(technicalServiceTemplate
								.getPayerAllowed()),
						getCharSpecIO(getCharValSpecForFreeEntry()),
						CharSpecType.value1, technicalServiceTemplate
								.getPayerAllowedNMflag()));
			}
			*/
			/*if ((propertyInfoName.equals("chargeInBSS"))
					&& (technicalServiceTemplate.getChargeInBSSSendTOpc())) {
				importCharIOSaveList.add(getCharacteristics(propertyInfo[i],
						String.valueOf(technicalServiceTemplate
								.getChargeInBSS()),
						getCharSpecIO(getCharValSpecForFreeEntry()),
						CharSpecType.value1, technicalServiceTemplate
								.getChargeInBSSNMflag()));
			}*/
			
			if ((propertyInfoName.equals("serviceSubType"))
					&& (technicalServiceTemplate.getServiceSubTypeSendTOpc())) {
				importCharIOSaveList
						.add(getCharacteristics(
								propertyInfo[i],
								String.valueOf(technicalServiceTemplate
										.getServiceSubType().getIndex()),
								getCharSpecIO(getCharValSpecForEnum(technicalServiceTemplate
										.getServiceSubType())),
								CharSpecType.value2, technicalServiceTemplate
										.getServiceSubTypeNMflag()));
			}
			
			if ((propertyInfoName.equals("priority"))
					&& (technicalServiceTemplate.getPrioritySendTOpc())) {
				importCharIOSaveList.add(getCharacteristics(propertyInfo[i],
						String.valueOf(technicalServiceTemplate.getPriority()),
						getCharSpecIO(getCharValSpecForFreeEntry()),
						CharSpecType.value3,
						technicalServiceTemplate.getPriorityNMflag()));
			}

			if ((propertyInfoName.equals("billDisplayOrder"))
					&& (technicalServiceTemplate.getBillDisplayOrderSendTOpc())) {
				importCharIOSaveList.add(getCharacteristics(propertyInfo[i],
						String.valueOf(technicalServiceTemplate
								.getBillDisplayOrder()),
						getCharSpecIO(getCharValSpecForFreeEntry()),
						CharSpecType.value3, technicalServiceTemplate
								.getBillDisplayOrderNMflag()));
			}
			
			if ((propertyInfoName.equals("showInInvoice"))
					&& (technicalServiceTemplate.getShowInInvoiceSendTOpc())) {
				importCharIOSaveList.add(getCharacteristics(propertyInfo[i],
						String.valueOf(technicalServiceTemplate
								.getShowInInvoice()),
						getCharSpecIO(getCharValSpecForFreeEntry()),
						CharSpecType.value1, technicalServiceTemplate
								.getShowInInvoiceNMflag()));
			}
			
			if ((propertyInfoName.equals("showZeroAmountInInvoice"))
					&& (technicalServiceTemplate
							.getShowZeroAmountInInvoiceSendTOpc())) {
				importCharIOSaveList.add(getCharacteristics(propertyInfo[i],
						String.valueOf(technicalServiceTemplate
								.getShowZeroAmountInInvoice()),
						getCharSpecIO(getCharValSpecForFreeEntry()),
						CharSpecType.value1, technicalServiceTemplate
								.getShowZeroAmountInInvoiceNMflag()));
			}
			
			if ((propertyInfoName.equals("customDescAllowed"))
					&& (technicalServiceTemplate.getCustomDescAllowedSendTOpc())) {
				importCharIOSaveList.add(getCharacteristics(propertyInfo[i],
						String.valueOf(technicalServiceTemplate
								.getCustomDescAllowed()),
						getCharSpecIO(getCharValSpecForFreeEntry()),
						CharSpecType.value1, technicalServiceTemplate
								.getCustomDescAllowedNMflag()));
			}

		}

		return (ImportCharIOSave[]) importCharIOSaveList
				.toArray(new ImportCharIOSave[importCharIOSaveList.size()]);

	}

	public static Map<String, String> getCharValSpecForFreeEntry() {
		Map<String, String> map = new HashMap<String, String>();
		return map;
	}

	public static Map<String, String> getCharValSpecForEnum(
			AbstractEnum abstractEnum) {

		Map<String, String> map = new HashMap<String, String>();

		EnumCollection enumCollection = abstractEnum.getCollection();
		Iterator itr = enumCollection.iterator();
		AbstractEnum enumSet = null;
		
		while (itr.hasNext()) {
			enumSet = (AbstractEnum) itr.next();
			
			if (enumSet.getIndex() == 0
					&& abstractEnum instanceof ServiceSubTypeEnum) {
				map.put(String.valueOf(enumSet.getIndex()), "--");
			} else {
				map.put(String.valueOf(enumSet.getIndex()), enumSet.getName());
			}
		}

		return map;

	}

	public static Map<String, String> getCharValSpecForServiceCat(Context ctx,
			long l) {

		Map<String, String> map = new HashMap<String, String>();

		Home home = (Home) ctx.get(ServiceCategoryHome.class);
		Collection<ServiceCategory> coll;
		
		try {
			coll = home.selectAll();
			Iterator<ServiceCategory> itr = coll.iterator();

			while (itr.hasNext()) {

				ServiceCategory servcat = (ServiceCategory) itr.next();
				
				if (!(servcat.getName() == "")) {
					map.put(ServiceCategoryIdentitySupport.instance()
							.toStringID(servcat.getCategoryId()), servcat
							.getName());
				}
				
				if (LogSupport.isDebugEnabled(ctx)) {
					LogSupport.debug(ctx, servcat,
						"[getCharValSpecForServiceCat] Printing ServiceCategory Map Values:" + map);
			
				}
			}

		} catch (HomeInternalException e) {
			Logger.debug(ctx, TechnicalServiceTemplateSupport.class,
					"[getCharValSpecForServiceCat] Home Internal Exception Occured ", e);
		} catch (UnsupportedOperationException e) {
			Logger.debug(ctx, TechnicalServiceTemplateSupport.class,
					"[getCharValSpecForServiceCat] Operation is not supported ", e);
		} catch (HomeException e) {
			Logger.debug(ctx, TechnicalServiceTemplateSupport.class,
					"[getCharValSpecForServiceCat] ServiceCategoryHome not found", e);
		}

		return map;

	}

	public static CharValueSpecs_type2 getCharSpecIO(Map map) {
		
		CharValueSpecs_type2 charValueSpecs_type = new CharValueSpecs_type2();
		CharValSpecIO charValueSpecIO = null;

		Iterator<Map.Entry<String, String>> entries = map.entrySet().iterator();
		
		while (entries.hasNext()) {
			charValueSpecIO = new CharValSpecIO();
			Map.Entry<String, String> entry = entries.next();
			charValueSpecIO.setBoundValue(String.valueOf(entry.getKey()));
			charValueSpecIO.setDisplayText(entry.getValue());
			charValueSpecs_type.addCharValueSpec(charValueSpecIO);
		}

		return charValueSpecs_type;
	}

	public static ImportCharIOSave getCharacteristics(PropertyInfo property,
			String value, CharValueSpecs_type2 charValueSpecs_type,
			CharSpecType charSpecType2, ComTemplateActionEnum comTemplateAction) {
		
		ImportCharIOSave importCharIOSave = new ImportCharIOSave();
		try {
			ImportCharSpecIO localCharSpec = new ImportCharSpecIO();
			String businessKey = PCConstants.TECHNICAL_SERVICE_TEMPLATE_CS_BK_PREFIX
					+ property.getName();
			localCharSpec.setBusinessKey(businessKey);
			localCharSpec.setName(property.getLabel());
			// localCharSpec.setDescription(property.get);
			Class type = property.getType();

			String dataType = type.getSimpleName();

			CharValueSpecs_type2 charValueSpecs_type2 = charValueSpecs_type;

			if (charSpecType2.equals(CharSpecType.value3)) {
				List<CharValSpecIO> charValue = new ArrayList<CharValSpecIO>();
				if (dataType.contains("Long")) {
					charValue.add(getCharValSpecForValueRange("Long"));
				} else if (dataType.contains("Integer")) {
					charValue.add(getCharValSpecForValueRange("Integer"));
				}
				charValueSpecs_type2
						.setCharValueSpec((CharValSpecIO[]) charValue
								.toArray(new CharValSpecIO[charValue.size()]));
			}

			if ((dataType.contains("Enum")) || (dataType.contains("Long")))
				dataType = "Integer";
			localCharSpec.setValueDataType(dataType);

			CharSpecType charSpecType = charSpecType2;
			localCharSpec.setCharValueSpecs(charValueSpecs_type2);
			localCharSpec.setCharSpecType(charSpecType);

			if (!comTemplateAction.equals(ComTemplateActionEnum.OPEN)) {
				CharValues_type2 localCharValues = new CharValues_type2();
				//CharValues_type3 localCharValues = new CharValues_type3();
				CharValIO localCharValue = new CharValIO();
				localCharValue.setValue(value);
				localCharValues.addCharValue(localCharValue);
				importCharIOSave.setCharValues(localCharValues);
			}
			importCharIOSave.setCharSpec(localCharSpec);
			
		} catch (Exception e) {
			LogSupport.minor(ContextLocator.locate(),
					TechnicalServiceTemplateSupport.class.getName(),
					"Error in charSpec", e);

		}

		YesNoIndicator yesNo = YesNoIndicator.YES;
		/*
		 * if(status) { yesNo = YesNoIndicator.YES; }
		 */
		// Need to set Hidden field config
		return importCharIOSave;
	}

	/**
	 * Method to change state of Technical service to RELEASED
	 * 
	 * @param techService
	 * @param nvObj
	 * @return
	 */
	public static NonVersionEntityStateChangeInput releaseTechnicalService(
			TechnicalServiceTemplate techService,
			NonVersionEntityStateChangeInput nvObj) {
		
		nvObj.setBusinessKey(String.valueOf(techService.getID()));
		nvObj.setCurrentEvent("RELEASE");
		
		return nvObj;
	}

	public static CharValSpecIO getCharValSpecForValueRange(String type) {
		
		CharValSpecIO charValue = new CharValSpecIO();
		
		if (type.equals("Long")) {
			charValue.setMinValue(MIN_LONG_VALUE);
			charValue.setMaxValue(MAX_LONG_VALUE);
		} else if (type.equals("Integer")) {
			charValue.setMinValue(MIN_INT_VALUE);
			charValue.setMaxValue(MAX_INT_VALUE);
		}
		
		return charValue;
	}

	private static final String MIN_INT_VALUE = "-2147483648";
	private static final String MAX_INT_VALUE = "2147483647";
	private static final String MIN_LONG_VALUE = "-9223372036854775808";
	private static final String MAX_LONG_VALUE = "9223372036854775807";

}
