package com.trilogy.app.crm.integration.pc;

import java.util.ArrayList;
import java.util.List;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.util.crmapi.wsdl.v3_0.api.pc.io.offering.v1.OfferingVersionIO;
import com.trilogy.util.crmapi.wsdl.v3_0.api.pc.io.price.v1.CompositePriceIO;
import com.trilogy.util.crmapi.wsdl.v3_0.api.pc.io.price.v1.OneTimeCharge;
import com.trilogy.util.crmapi.wsdl.v3_0.api.pc.io.price.v1.PriceIO;
import com.trilogy.util.crmapi.wsdl.v3_0.api.pc.io.price.v1.PriceVersionIO;
import com.trilogy.util.crmapi.wsdl.v3_0.api.pc.io.price.v1.RecurringCharge;
import com.trilogy.util.crmapi.wsdl.v3_0.api.pc.io.product.v1.ProductIO;
import com.trilogy.util.crmapi.wsdl.v3_0.api.pc.io.product.v1.ProductVersionIO;

	
public class OfferingModelAdapter {
	/*Note: Commenting few changes as per new WSDL(causing compilation errors in existing code),need to fix them soon*/
	public PricePlanModel adapt(Context ctx,
			org.apache.axiom.om.OMElement offeringPublicationMessage) {

		LogSupport.info(ctx, this, "[OfferingModelAdapter] Reading Offer Publication Message..!!");
		PricePlanModel pricePlanModel = new PricePlanModel();

		// Get Offering effective version using EffectiveFromDate
		OfferingVersionIO effectiveVersion = getOfferEffectiveVersion(ctx,
				offeringPublicationMessage);
		List<ProductIO> productList = getProductsList(effectiveVersion, ctx);// get
																				// Product
																				// List

		// Get PriceIO from effective version
		PriceIO priceIO = effectiveVersion.getPrice();

		// Get Price effective price version & list of CompositePriceIO
		/*PriceVersionIO priceVersionIO = getEffectivePriceVersion(priceIO);*/
		/*List<CompositePriceIO> compositePriceIOList = getCompositPrices(
				priceVersionIO, ctx);*/

		// add service in ServiceModel with price
		/*List<ServiceModel> serviceModelList = addService(productList,
				compositePriceIOList, ctx);// get service model list
*/
		if (priceIO != null) {

			// set PricePlanModel
			pricePlanModel.setPricePlanName(priceIO.getName());
			pricePlanModel.setServiceProviderID(1); // SPID is 1 for now as not
													// coming from COM
			pricePlanModel.setPricePlanName(priceIO.getName());
			pricePlanModel.setSubcriptionType(1);// need to confirm

			if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport.debug(ctx, this, "[OfferingModelAdapter] PricePlan Model: " + pricePlanModel);
			}

			// set VersionModel
			VersionModel versionModel = new VersionModel();
			/*versionModel.setVersionId(effectiveVersion.getId());*/
			versionModel.setActivationDate(effectiveVersion
					.getEffectiveFromDate());
			versionModel.setDescription("test");// need to confirm
			/*versionModel.setServiceList(serviceModelList);*/// set Service model
															// list

			if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport.debug(ctx, this, "[OfferingModelAdapter] Version Model: " + versionModel);
			}

			pricePlanModel.set_versionModel(versionModel);
		} else {
			LogSupport.minor(ctx, this, "Error: priceIO is Null");
		}
		return pricePlanModel;
	}

	/**
	 * In this method we will add service in ServiceModel with PriceModel
	 * 
	 * @param productList
	 * @param pricePlan
	 * @param priceList
	 * @param ctx
	 * @return ServiceModel List
	 */
	private List<ServiceModel> addService(List<ProductIO> productList,
			List<CompositePriceIO> priceList, Context ctx) {

		List<ServiceModel> serviceModelList = new ArrayList<ServiceModel>();

		/*if (productList.size() > 0 && priceList.size() > 0) {

			for (ProductIO product : productList) {
				// set values to Service Model
				ServiceModel serviceModel = new ServiceModel();
				serviceModel.setServiceId(product.getBusinessKey());
				serviceModel.setServiceName(product.getName());
				serviceModel.setServiceProviderID(1);// SPID is 1 for now as not
														// coming from COM
				serviceModel.setPrice_(getPriceModels(priceList,
						product.getBusinessKey(), ctx));

				LogSupport.debug(ctx, this, "Service Model: " + serviceModel);

				serviceModelList.add(serviceModel);
			}
		} else {
			LogSupport.minor(ctx, this,
					"Error: Offering ProductList or PriceList is Null");
		}*/
		return serviceModelList;

	}

	/**
	 * This method return PriceModel using productBusinessKey from list of
	 * CompositePriceIO
	 * 
	 * @param priceList
	 * @param productBusinessKey
	 * @return PriceModel
	 */
	private List<PriceModel> getPriceModels(List<CompositePriceIO> priceList,
			String productBusinessKey, Context ctx) {

		if (LogSupport.isDebugEnabled(ctx)) {
			LogSupport.debug(ctx, this, "[getPriceModels] fetch Price Models matching Key: " + productBusinessKey);
		}
		
		List<PriceModel> priceModelList = new ArrayList<PriceModel>();
		for (CompositePriceIO compositePriceIO : priceList) {

			if (compositePriceIO.getProductBusinessKey().equals(
					productBusinessKey)) {

				if (compositePriceIO.getOneTimeCharge() != null) {

					PriceModel priceModel = new PriceModel();
					OneTimeCharge oneTimeCharge = compositePriceIO
							.getOneTimeCharge();

					priceModel.setPriceType(PriceModel.PriceType.ONE_TIME);
					//priceModel.setPriceTemplateBusinesskey(oneTimeCharge.getBusinesskey()); //due to compile time
					//priceModel.setAmount(oneTimeCharge.getCharge().getAmount()); //due to compile time
					/*priceModel.setTaxIncluded(oneTimeCharge.getCharge()
							.getTaxIncluded().getValue());*/

					priceModelList.add(priceModel);
				}

				if (compositePriceIO.getOneTimeCharge() != null) {

					PriceModel priceModel = new PriceModel();
					RecurringCharge recurringCharge = compositePriceIO
							.getRecurringCharge();

					priceModel.setPriceType(PriceModel.PriceType.RECCURING);
					//priceModel.setPriceTemplateBusinesskey(recurringCharge.getName()); //due to compile time
					//priceModel.setAmount(recurringCharge.getCharge().getAmount());//due to compile time
							
					/*priceModel.setTaxIncluded(recurringCharge.getCharge()
							.getTaxIncluded().getValue());*/

					priceModelList.add(priceModel);
				}
			}
		}

		return priceModelList;
	}

	/**
	 * This method return the list of get CompositPrices(Price List) from
	 * CompositePriceIO
	 * 
	 * @param priceVersionIO
	 * @param ctx
	 * @return List<CompositePriceIO>
	 */
	private List<CompositePriceIO> getCompositPrices(
			PriceVersionIO priceVersionIO, Context ctx) {

		List<CompositePriceIO> priceList = new ArrayList<CompositePriceIO>();
		CompositePriceIO compositePriceIO = priceVersionIO
				.getVersionIOChoice_type0().getCompositePrice();
		/*CompositePrices_type0 compositePrices_type0 = priceVersionIO
				.getVersionIOChoice_type0().getCompositePrice()
				.getCompositePrices();

		CompositePriceIO[] compositePriceIOList = compositePrices_type0
				.getCompositePrice();
		if (compositePriceIOList.length > 0) {
			for (CompositePriceIO compositePrice : compositePriceIOList) {
				priceList.add(compositePrice);
			}
		} else {
			priceList.add(compositePriceIO);
		}*/
		return priceList;
	}

	/**
	 * In this method return the Effective PriceVersion from PriceIO
	 * 
	 * @param priceIO
	 * @return PriceVersionIO
	 */
	
	/*Commenting out for new wsdl compliance*/
	/*private PriceVersionIO getEffectivePriceVersion(PriceIO priceIO) {

		Versions_type4 versions_type4 = priceIO.getVersions();
		PriceVersionIO[] priceVersionIO = versions_type4.getVersion();
		PriceVersionIO effectiveVersion = priceVersionIO[0];
		for (PriceVersionIO version : priceVersionIO) {
			if (effectiveVersion.getEffectiveFromDate().before(
					version.getEffectiveFromDate())) {
				effectiveVersion = version;
			}
		}
		return effectiveVersion;
	}*/

	/**
	 * This method return product list from offering version
	 * 
	 * @param effectiveVersion
	 * @param ctx
	 * @return list of products
	 */
	private List<ProductIO> getProductsList(OfferingVersionIO effectiveVersion,
			Context ctx) {

		List<ProductIO> productList = new ArrayList<ProductIO>();
		ProductIO product = effectiveVersion.getProduct();

		ProductVersionIO productVersionIO = getProductEffectiveVersion(product,
				ctx);

		/*Components_type3 components_type3 = productVersionIO.getComponents();
		ProductComponentIO[] productComponentIOList = components_type3
				.getComponent();*/

		// check if available number of productComponent in <component>
		/*if (productComponentIOList.length > 0) {
			for (ProductComponentIO productComponentIO : productComponentIOList) {
				productList.add(productComponentIO.getProduct());
			}
		} else {
			productList.add(product);
		}*/

		return productList;
	}

	/**
	 * this method return effective product version from EffectiveFromDate of
	 * Versions
	 * 
	 * @param product
	 * @param ctx
	 * @return ProductEffectiveVersion
	 */
	private ProductVersionIO getProductEffectiveVersion(ProductIO product,
			Context ctx) {
		if (product == null) {
			LogSupport.minor(ctx, this,
					"Error in Get ProductEffective Version - Product is Null");
			return null;
		}
		/*Versions_type5 versions_type5 = product.getVersions();
		ProductVersionIO[] productVersionIO = versions_type5.getVersion();
*/
		/*ProductVersionIO effectiveVersion = productVersionIO[0];
		for (ProductVersionIO version : productVersionIO) {

			if (effectiveVersion.getEffectiveFromDate().before(
					version.getEffectiveFromDate())) {
				effectiveVersion = version;
			}

		}

		return effectiveVersion;*/
		// remove this:
		return null;
	}

	/**
	 * its return OfferEffectiveVersion using EffectiveFromDate of Versions
	 * 
	 * @param ctx
	 * @param offeringPublicationMessage
	 * @return OfferEffectiveVersion
	 */
	private OfferingVersionIO getOfferEffectiveVersion(Context ctx,
			org.apache.axiom.om.OMElement offeringPublicationMessage) {
		if (offeringPublicationMessage == null) {
			LogSupport
					.minor(ctx, this,
							"Error in Get Offer Version - OfferingPublicationMessage is Null");
			return null;
		}

		/*OfferingIO offeringIO = offeringPublicationMessage.getOffering();
		Versions_type6 versions = offeringIO.getVersions();
		OfferingVersionIO[] offeringVersion = versions.getVersion();
		OfferingVersionIO effectiveVersion = offeringVersion[0];
		for (OfferingVersionIO version : offeringVersion) {
			if (effectiveVersion.getEffectiveFromDate().before(
					version.getEffectiveFromDate())) {
				effectiveVersion = version;
			}
		}

		return effectiveVersion;*/
		// remove this:
				return null;
		
	}
}
