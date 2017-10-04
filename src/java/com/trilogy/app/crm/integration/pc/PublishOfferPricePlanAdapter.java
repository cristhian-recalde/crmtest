/**
 * 
 */
package com.trilogy.app.crm.integration.pc;

import java.util.List;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.util.crmapi.wsdl.v3_0.api.pc.io.offering.v1.OfferingIO;
import com.trilogy.util.crmapi.wsdl.v3_0.api.pc.io.offering.v1.OfferingVersionIO;
import com.trilogy.util.crmapi.wsdl.v3_0.api.pc.io.offering.v1.Versions_type10;


/**
 * @author abhay.parashar
 *  Adapter for converting publish offering COM request to BSS Data Model
 *  Naming class based on WSDL function as PricePlanModel is already defined for some other API.
 */
public class PublishOfferPricePlanAdapter {

	private PublishOfferSupport support;
	
	private Context ctx;
	
	private boolean debugEnabled;

	public PublishOfferPricePlanAdapter(Context ctx) {
		support = new PublishOfferSupport();
		this.ctx = ctx; 
		this.debugEnabled = LogSupport.isDebugEnabled(ctx);
	}
	
	private boolean isDebugEnabled() {
		return debugEnabled;
	}

	public PublishOfferPricePlanModel adapt(Context ctx, OfferingIO paramOfferingIO) {
		if (isDebugEnabled()) {
			LogSupport.debug(ctx, this, "[PublishOfferPricePlanAdapter.adapt] Offering key:"+paramOfferingIO.getBusinessKey()+" & Offering Name:"+paramOfferingIO.getName());
		}
		PublishOfferPricePlanModel pricePlanModel = new PublishOfferPricePlanModel();
		setPPFields(pricePlanModel,paramOfferingIO);
		createPPVersions(pricePlanModel,paramOfferingIO,pricePlanModel.getPricePlanId());
		return pricePlanModel;
	}

	private void createPPVersions(PublishOfferPricePlanModel pricePlanModel, OfferingIO paramOfferingIO, long ppID) {
		if (isDebugEnabled()) {
			LogSupport.debug(ctx, this, "[PublishOfferPricePlanAdapter.createPPVersions] Offering key:"+paramOfferingIO.getBusinessKey()+" & Price plan id:"+ppID);
		}
		OfferingVersionIO[] offeringIO = support.getOfferVersion(paramOfferingIO);
		List<PublishedVersionModel> offeringVersion = support.adapt(ctx, offeringIO,ppID);
		pricePlanModel.setVersionModel(offeringVersion);
	}

	private void setPPFields(PublishOfferPricePlanModel pricePlanModel,	OfferingIO paramOfferingIO) {
		if (isDebugEnabled()) {
			LogSupport.debug(ctx, this, "[PublishOfferPricePlanAdapter.setPPFields] Offering key:"+paramOfferingIO.getBusinessKey()+" & Offering Name:"+paramOfferingIO.getName());
		}
		pricePlanModel.setPricePlanId(Long.parseLong(paramOfferingIO.getBusinessKey()));
		Versions_type10 versions_type10 = paramOfferingIO.getEntityBaseIOChoice_type1().getVersions();
		OfferingVersionIO[] versionList = versions_type10.getVersion();
		OfferingVersionIO firstVersion = versionList[0];
		String paymentMode = firstVersion.getTermsAndConditions().getPaymentMode();
		//pricePlanModel.setPricePlanType(Long.parseLong(getSubscriptionType(paramOfferingIO.getEntityBaseIOChoice_type1().getCurrentVersion()[0].getTermsAndConditions().getPaymentMode())));
		pricePlanModel.setPricePlanType(Long.parseLong(getPPType(paymentMode)));
		pricePlanModel.setPricePlanName(paramOfferingIO.getName());
		//pricePlanModel.setUnifiedOfferId(paramOfferingIO.getBusinessKey());
		pricePlanModel.setServiceProviderID(Integer.parseInt(paramOfferingIO.getSpid()));
		pricePlanModel.setSubscriptionType(Long.parseLong(paramOfferingIO.getSubscriptionType().getName()));
	}
	
	private String getPPType(String paymentMode) {
		if(paymentMode.equals("PREPAID")){
			return "1";
		}else{
			return "0";	
		}
	}	
}