package com.trilogy.app.crm.integration.pc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.trilogy.app.crm.bean.ServiceSubTypeEnum;
import com.trilogy.app.crm.bean.ComTemplateActionEnum;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.ContextLocator;
import com.trilogy.framework.xhome.xenum.AbstractEnum;
import com.trilogy.framework.xhome.xenum.EnumCollection;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.util.crmapi.wsdl.v3_0.api.core.io.common.v1.CharSpecType;
import com.trilogy.util.crmapi.wsdl.v3_0.api.core.io.common.v1.CharValIO;
import com.trilogy.util.crmapi.wsdl.v3_0.api.core.io.common.v1.CharValSpecIO;
import com.trilogy.util.crmapi.wsdl.v3_0.api.core.io.common.v1.CharValueSpecs_type2;
import com.trilogy.util.crmapi.wsdl.v3_0.api.core.io.common.v1.CharValues_type2;
import com.trilogy.util.crmapi.wsdl.v3_0.api.core.io.common.v1.CharValues_type3;
import com.trilogy.util.crmapi.wsdl.v3_0.api.core.io.common.v1.ImportCharIOSave;
import com.trilogy.util.crmapi.wsdl.v3_0.api.core.io.common.v1.ImportCharSpecIO;
import com.trilogy.util.crmapi.wsdl.v3_0.api.core.io.common.v1.YesNoIndicator;

public class TemplateUtility {
	

	private static final String MIN_INT_VALUE = "-2147483648";
	private static final String MAX_INT_VALUE = "2147483647";
	private static final String MIN_LONG_VALUE = "-9223372036854775808";	
	private static final String MAX_LONG_VALUE = "9223372036854775807";

	public static Map<String, String> getCharValSpecForFreeEntry() {
		Map<String, String> map = new HashMap<String, String>();
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
		
		LogSupport.debug(ContextLocator.locate(), PriceTemplateSupport.class.getName(), "[getCharacteristics] Value: " + value);
		ImportCharIOSave importCharIOSave = new ImportCharIOSave();
		
		try {
			ImportCharSpecIO localCharSpec = new ImportCharSpecIO();
			
			String businessKey = PCConstants.PRICE_TEMPLATE_CS_BK_PREFIX + property.getName();
			localCharSpec.setBusinessKey(businessKey);
			localCharSpec.setName(property.getLabel());
			
			//localCharSpec.setSource(PCConstants.SOURCE);
			Class type = property.getType();

			String dataType = type.getSimpleName();
			
			CharValueSpecs_type2 charValueSpecs_type2 = charValueSpecs_type;
			
			if(charSpecType2.equals(CharSpecType.value3)){
				List<CharValSpecIO> charValue = new ArrayList<CharValSpecIO>();
				if(dataType.contains("Long")){
					charValue.add(getCharValSpecForValueRange("Long"));
				}	
				else if (dataType.contains("Integer")){
					charValue.add(getCharValSpecForValueRange("Integer"));
				}
				charValueSpecs_type2.setCharValueSpec((CharValSpecIO[])charValue.toArray(new CharValSpecIO[charValue.size()]));
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
	
	public static CharValSpecIO getCharValSpecForValueRange(String type) {
		CharValSpecIO charValue = new CharValSpecIO();
		if(type.equals("Long")){
			charValue.setMinValue(MIN_LONG_VALUE);
			charValue.setMaxValue(MAX_LONG_VALUE);
		}
		else if(type.equals("Integer")){
			charValue.setMinValue(MIN_INT_VALUE);
			charValue.setMaxValue(MAX_INT_VALUE);
		}
		return charValue;
	}
	
	public static Map<String, String> getCharValSpecForEnum(AbstractEnum abstractEnum) {

		Map<String, String> map = new HashMap<String, String>();

		EnumCollection enumCollection = abstractEnum.getCollection();
		Iterator itr = enumCollection.iterator();
		AbstractEnum enumSet = null;
		while (itr.hasNext()) {
			enumSet = (AbstractEnum) itr.next();
			if (enumSet.getIndex() == 0 && abstractEnum instanceof ServiceSubTypeEnum) {
				map.put(String.valueOf(enumSet.getIndex()), "--");
			} else {
				map.put(String.valueOf(enumSet.getIndex()), enumSet.getName());
			}
		}

		return map;

	}
}
