package com.trilogy.app.crm.integration.pc;

import com.trilogy.app.crm.bean.ui.PricingTemplate;
//import com.trilogy.util.crmapi.wsdl.v3_0.api.pc.io.price.v1.MonetaryAmountIO;
import com.trilogy.util.crmapi.wsdl.v3_0.api.pc.io.price.v1.PriceTemplateIO;
import com.trilogy.util.crmapi.wsdl.v3_0.api.pc.io.price.v1.RecurringPriceTemplateIOSave;
import com.trilogy.util.crmapi.wsdl.v3_0.api.pc.webservices.messages.v1.CreatePriceTemplateResponse;

/**
 * @author kkadam
 *
 */
public class ProductCatalogAdapter {

	
	public static PriceTemplateIO createPriceTemplateAdapter(
			PricingTemplate pricingtemplate) {
		// TODO Auto-generated method stub
		
		PriceTemplateIO priceTemplateIO = new PriceTemplateIO();
		
		//MonetaryAmountIO monetaryAmountIO = new MonetaryAmountIO();// this object required for charge
		
		 // For Recurring Charge Template
		
		/*RecurringPriceTemplateIOSave[] RecurringChargeTemplateList = new RecurringPriceTemplateIOSave[1];
		RecurringPriceTemplateIOSave recurringChargeTemplate = new RecurringPriceTemplateIOSave();
		 	
		 	 recurringChargeTemplate.setPriceTemplateBusinesskey(String.valueOf(pricingtemplate.getID()));
			 recurringChargeTemplate.setName(pricingtemplate.getName());
			 recurringChargeTemplate.setFrequency(PCConstants.FREQUENCY);
			 recurringChargeTemplate.setCharge(monetaryAmountIO);//
			 //recurringChargeTemplate.setProRated(); // its optional
			 //recurringChargeTemplate.setAdvanceApplicable();//// its optional
			 //recurringChargeTemplate.setRecurrenceCount();// its optional
			 recurringChargeTemplate.setSource(PCConstants.PC_PRICINGTEMPLATE_SOURSE);

			 RecurringChargeTemplateList[0] = recurringChargeTemplate;
			 priceTemplateIO.setRecurringChargeTemplate(RecurringChargeTemplateList);*/
		 
		// For OneTime Charge Template
		 
		 /* OneTimeChargeTemplate[] OneTimeChargeList = new OneTimeChargeTemplate[1];
		    OneTimeChargeTemplate oneTimeChargeTemplate = new OneTimeChargeTemplate();
			 
		 	 oneTimeChargeTemplate.setPriceTemplateBusinesskey(String.valueOf(pricingtemplate.getID()));
			 oneTimeChargeTemplate.setName(pricingtemplate.getName());
			 oneTimeChargeTemplate.setCharge(monetaryAmountIO);
			 //oneTimeChargeTemplate.setChargeApplicationEvent();// its optional
			 oneTimeChargeTemplate.setSource(PCConstants.PC_PRICINGTEMPLATE_SOURSE);//need to confirm
			 
			 OneTimeChargeList[0] = oneTimeChargeTemplate;
		 
		 priceTemplateIO.setOneTimeChargeTemplate(OneTimeChargeList);*/
		 
	   return priceTemplateIO;
	}

	public static String unAdpatPricingTemlate(
			CreatePriceTemplateResponse createPriceTemplateResponseE) {
		return createPriceTemplateResponseE.getStatus().getValue();
	}
}
