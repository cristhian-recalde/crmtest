package com.trilogy.app.crm.integration.pc;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.trilogy.app.crm.adapters.ProductPriceToServiceFeeAdapter;
import com.trilogy.app.crm.bean.ChargingCycleEnum;
import com.trilogy.app.crm.bean.ExpiryExtensionModeEnum;
import com.trilogy.app.crm.bean.PricePlanFunctionEnum;
import com.trilogy.app.crm.bean.PricePlanHome;
import com.trilogy.app.crm.bean.PricePlanVersionHome;
import com.trilogy.app.crm.bean.PricePlanVersionXInfo;
import com.trilogy.app.crm.bean.ProductPriceHome;
import com.trilogy.app.crm.bean.ServiceFee2;
import com.trilogy.app.crm.bean.ServiceFee2ID;
import com.trilogy.app.crm.bean.ServicePackageVersion;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.ServicePreferenceEnum;
import com.trilogy.app.crm.bean.SubscriptionTechnologyTypeHolder;
import com.trilogy.app.crm.bean.UnifiedCatalogSpidConfig;
import com.trilogy.app.crm.bean.UnifiedCatalogSpidConfigHome;
import com.trilogy.app.crm.bean.core.PricePlan;
import com.trilogy.app.crm.bean.core.PricePlanVersion;
import com.trilogy.app.crm.bean.payment.ContractFeeFrequencyEnum;
import com.trilogy.app.crm.bean.ui.PackageProduct;
import com.trilogy.app.crm.bean.ui.PackageProductHome;
import com.trilogy.app.crm.bean.ui.Product;
import com.trilogy.app.crm.bean.ui.ProductHome;
import com.trilogy.app.crm.bean.ui.ResourceProduct;
import com.trilogy.app.crm.bean.ui.ResourceProductHome;
import com.trilogy.app.crm.bean.ui.ServiceProduct;
import com.trilogy.app.crm.bean.ui.ServiceProductHome;
import com.trilogy.app.crm.bean.ui.TechnicalServiceTemplate;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.technology.TechnologyEnum;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.util.crmapi.wsdl.v3_0.api.core.io.common.v1.CharIO;
import com.trilogy.util.crmapi.wsdl.v3_0.api.core.io.common.v1.NetworkTechnologyIO;
import com.trilogy.util.crmapi.wsdl.v3_0.api.core.io.common.v1.YesNoIndicator;
import com.trilogy.util.crmapi.wsdl.v3_0.api.pc.io.offering.v1.OfferingIO;
import com.trilogy.util.crmapi.wsdl.v3_0.api.pc.io.offering.v1.OfferingVersionIO;
import com.trilogy.util.crmapi.wsdl.v3_0.api.pc.io.offering.v1.Versions_type10;
import com.trilogy.util.crmapi.wsdl.v3_0.api.pc.io.price.v1.PriceIO;
import com.trilogy.util.crmapi.wsdl.v3_0.api.pc.io.product.v1.ProdSpecType;
import com.trilogy.util.crmapi.wsdl.v3_0.api.pc.io.product.v1.ProductComponentIO;
import com.trilogy.util.crmapi.wsdl.v3_0.api.pc.io.product.v1.ProductIO;
import com.trilogy.util.crmapi.wsdl.v3_0.api.pc.io.product.v1.ProductVersionIO;
import com.trilogy.util.crmapi.wsdl.v3_0.api.pc.io.service.v1.CustFacingServSpecVersionIO;
import com.trilogy.util.crmapi.wsdl.v3_0.api.pc.io.technicalservice.v1.RfssIO;
import com.trilogy.util.crmapi.wsdl.v3_0.api.pc.io.technicalservice.v1.RfssVersionIO;
import com.trilogy.util.snippet.log.Logger;
import com.trilogy.app.crm.bean.ProductTypeEnum;
import com.trilogy.app.crm.bean.ProductPrice;

public class PricePlanCreator {
	private static final String ID_PATH_SEPERATOR="~";
	private Context ctx;
	private int spid;
	private StringBuilder desc = null;

	private Map<Long, ProductPrice> prodId_ProductPrice_Map = new HashMap<Long, com.redknee.app.crm.bean.ProductPrice>();

	public PricePlanCreator(Context ctx, int spid) {
		this.ctx = ctx;
		this.spid = spid;
		desc = new StringBuilder();
		desc.append("Product ID ");
	}

	private Context getContext(){
		return ctx;
	}

	private int getSpid() {
		return spid;
	}

	public PricePlan createPricePlan(OfferingIO offering) throws NumberFormatException, HomeInternalException, HomeException {

		Home pricePlanHome = (Home) getContext().get(PricePlanHome.class);
		//This code is to handle duplicate priceplan
		PricePlan pricePlan = (PricePlan) pricePlanHome.find(Long.valueOf(offering.getBusinessKey()));
		if(pricePlan == null){
			pricePlan = (PricePlan) pricePlanHome.create(getContext(), getPricePlan(offering));
		}

		return pricePlan;
	}

	private PricePlan getPricePlan(OfferingIO offering) throws NumberFormatException, HomeInternalException, HomeException {

		Versions_type10 versions_type10 = offering.getEntityBaseIOChoice_type1().getVersions();
		OfferingVersionIO[] versionList = versions_type10.getVersion();
		OfferingVersionIO firstVersion = versionList[0];
		String paymentMode = firstVersion.getTermsAndConditions().getPaymentMode();

		PricePlan pricePlan = new PricePlan();
		pricePlan.setId(Long.valueOf(offering.getBusinessKey()));
		pricePlan.setSubscriptionType(Long.parseLong(offering.getSubscriptionType().getName()));
		pricePlan.setSpid(Integer.parseInt(offering.getSpid()));
		pricePlan.setName(offering.getName());
		pricePlan.setPricePlanType(SubscriberTypeEnum.get(Short.parseShort(getPPType(paymentMode))));
//		pricePlan.setUnifiedOfferId(offering.getBusinessKey());
		//Set the Network type as the value configured against the SPID
		TechnologyEnum technologyType = getTechnologyTypeFromUCSpidConfig(Integer.valueOf(offering.getSpid()), Long.parseLong(offering.getSubscriptionType().getName()));
		if(technologyType != null){
			pricePlan.setTechnology(technologyType);
		}else{
			LogSupport.info(getContext(), this, "No Matching Technology found. Will continue with " + TechnologyEnum.NO_TECH);
			pricePlan.setTechnology(TechnologyEnum.NO_TECH);
		}
		setPPDefaultValues(getContext(), pricePlan);
		return pricePlan;

	}

	/**
	 * @param offering
	 * @param technologyType
	 * @return
	 * @throws HomeException
	 * @throws HomeInternalException
	 * @throws NumberFormatExceptionc
	 * 
	 */
	private TechnologyEnum getTechnologyTypeFromUCSpidConfig(int spid, long subscriptionType) throws HomeException, HomeInternalException, NumberFormatException {
		TechnologyEnum technologyType = null;
		Home home = (Home) ctx.get(UnifiedCatalogSpidConfigHome.class);
		if(home != null){
			UnifiedCatalogSpidConfig unifiedCatalogSpidConfig = (UnifiedCatalogSpidConfig) home.find(ctx, spid);
			if(unifiedCatalogSpidConfig != null){
				Map map = unifiedCatalogSpidConfig.getSubscriptionTechnologyTypeIds();
				if(map != null){
					SubscriptionTechnologyTypeHolder holder = (SubscriptionTechnologyTypeHolder) map.get(subscriptionType);
					if(holder != null){
						technologyType = holder.getDefaultTechnologyType();
					}
				}
			}
		}
		return technologyType;
	}

	private String getPPType(String paymentMode) {
		if(paymentMode.equals("PREPAID")){
			return "1";
		}else{
			return "0";	
		}
	}	

	public void updateDescriptionInPPVersion(PricePlanVersion pricePlanVersion)
	throws HomeException, HomeInternalException {
		pricePlanVersion.setDescription(desc.toString());
		desc.setLength(0);
		Home home = (Home) getContext().get(PricePlanVersionHome.class);
		LogSupport.info(getContext(), this, "Updating PricePlanVersion with description " + pricePlanVersion.getDescription());
		home.store(getContext(), pricePlanVersion);
	}

	public static void setPPDefaultValues(Context ctx, PricePlan pricePlan){
		pricePlan.setPricePlanFunction(PricePlanFunctionEnum.NORMAL);
		pricePlan.setPricePlanGroup(-1);
		pricePlan.setPricePlanPriority(10000);
		pricePlan.setSubscriptionLevel(1);
		pricePlan.setVoiceRatePlan("");
		pricePlan.setSMSRatePlan("");
		pricePlan.setDataRatePlan("");
		pricePlan.setSuspensionOffset(0);
		pricePlan.setExpiryExtention(0);
		pricePlan.setExpiryExtensionMode(ExpiryExtensionModeEnum.EXTEND_FROM_CURRENT_EXPIRYDATE);
		pricePlan.setCurrentVersionChargeCycle(ChargingCycleEnum.MONTHLY);
		pricePlan.setApplyContractDurationCriteria(false);
		pricePlan.setMinContractDuration(-1);
		pricePlan.setMaxContractDuration(-1);
		pricePlan.setContractDurationUnits(ContractFeeFrequencyEnum.DAY);
		pricePlan.setHybrid(false);
		pricePlan.setApplyMinimumCharge(false);
		pricePlan.setMinimumCharge(0);
		pricePlan.setApplyZeroUsageDiscount(false);
		pricePlan.setDiscountClass(-1);
		pricePlan.setCatalogDriven(true);
		((com.redknee.app.crm.bean.AbstractPricePlan)pricePlan).setEnabled(true);

	}

	public PricePlanVersion createPricePlanVersion(OfferingVersionIO version, long pricePlanId) throws HomeInternalException, HomeException {
		Home home = (Home) getContext().get(PricePlanVersionHome.class);
		PricePlanVersion ppVersion = getPricePlanVersion(version, pricePlanId);
		return (PricePlanVersion) home.create(getContext(),ppVersion);
	}

	private PricePlanVersion getPricePlanVersion(OfferingVersionIO version, long pricePlanId) {
		PricePlanVersion pricePlanVersion = new PricePlanVersion();
		pricePlanVersion.setId(pricePlanId);
		pricePlanVersion.setActivateDate(version.getEffectiveFromDate().getTime());
		if(version.getReleaseOnDate() != null){
			pricePlanVersion.setCreatedDate(version.getReleaseOnDate().getTime());
		}
		setPricePlanVersionDefaultValues(getContext(), pricePlanVersion);
		return pricePlanVersion;
	}

	public static void setPricePlanVersionDefaultValues(Context context,PricePlanVersion pricePlanVersion) {
		pricePlanVersion.setDeposit(0);
		pricePlanVersion.setCreditLimit(0);
		pricePlanVersion.setDefaultPerMinuteAirRate(0);
		pricePlanVersion.setOverusageDataRate(0);
		pricePlanVersion.setOverusageSmsRate(0);
		pricePlanVersion.setOverusageVoiceRate(0);
		pricePlanVersion.setChargeCycle(ChargingCycleEnum.MONTHLY);
		pricePlanVersion.setApplyFreeUnits(false);
		pricePlanVersion.setCharge(0);
		pricePlanVersion.setEnabled(false);
		//This default value will be over written with the ProductIDs when update will be invoked.
		pricePlanVersion.setDescription("Default");
	}

	public void createXProduct(ProductIO product, PricePlanVersion pricePlanVersion, long subscriptionType, PriceIO priceIO) throws HomeException {
		Boolean isMandatory = true;
		LogSupport.debug(getContext(), this, "Checking the Product for its type");
		if(ProdSpecType.Composite.getValue().equalsIgnoreCase(product.getType().getValue()) || ProdSpecType.ProductChoice.getValue().equalsIgnoreCase(product.getType().getValue())){
			LogSupport.debug(getContext(), this, "Got a "+product.getType().getValue()+" Product with business key " + product.getBusinessKey() + "]. Processing it.");
			createPackageProduct(product, pricePlanVersion, subscriptionType, priceIO, isMandatory);
		}else if(ProdSpecType.Atomic.getValue().equalsIgnoreCase(product.getType().getValue())){
			if (product.getResource() != null)
			{
				LogSupport.debug(getContext(), this, "Got a Resource Product with business key " + product.getBusinessKey() + "]. Processing it.");
				createResourceProduct(product, pricePlanVersion, subscriptionType, priceIO, isMandatory);
			}
			else
			{
			LogSupport.debug(getContext(), this, "Got a Service Product with business key " + product.getBusinessKey() + "]. Processing it.");
			createServiceProduct(product, pricePlanVersion, priceIO, isMandatory);
			}
		}else{
			LogSupport.info(getContext(), this, "Invalid Product Type [" + product.getType() + "]");
			//throw new HomeException("Invalid Product Type:"+product.getType().getValue());
		}
		LogSupport.debug(getContext(), this, "Completed All Product Creation");
		// put product price map in ctx for price creation support
		getContext().put(PriceEntityCreator.PRODUCT_ID_PRODUCT_PRICE_MAP, prodId_ProductPrice_Map);
		LogSupport.debug(getContext(), this , "prodId_ProductPrice_Map successfully put in context.");

		//Updating the description that will be persisted in the price plan version.
		desc.append(" Added.");
	}

	private void createServiceProduct(ProductIO productIO, PricePlanVersion pricePlanVersion, PriceIO priceIO, Boolean isMandatory) throws HomeException{
		Home productHome = (Home) getContext().get(ProductHome.class);
		Home serviceProductHome = (Home) getContext().get(ServiceProductHome.class);
		//get productId
		Long productId = Long.parseLong(productIO.getBusinessKey());
		LogSupport.info(getContext(), this, "ProductId " + productId + " is added to the description.");
		//This has to be handled in a much proper way in coming sprint with package and multiple versions using ProductPrice and Old Version
		desc.append("" + productId + " ");
		Product product = (Product)productHome.find(productId);
		if(product == null)
		{
			LogSupport.debug(getContext(), this, "Creating Product for Service Product");
			product = new Product();
			product.setProductId(Long.parseLong(productIO.getBusinessKey()));
			product.setProductType(ProductTypeEnum.SERVICE);
			product.setSpid(getSpid());

			productHome.create(getContext(), product);
			LogSupport.debug(getContext(), this, "Product with id [" + product.getProductId() + "] created");
			//CustFacingServSpecVersionIO[] versions = productIO.getService().getVersions().getVersion();
		}
		ProductVersionIO[] versions = productIO.getVersions().getVersion();

		for(ProductVersionIO version : versions){
			RfssIO technicalTemplateIO = getTechnicalTemplateIO(productIO);
			if(technicalTemplateIO != null){
				LogSupport.debug(getContext(), this, "Creating Service Product");
				ServiceProduct serviceProductBean = (ServiceProduct) serviceProductHome.create(getContext(), getServiceProduct(version, productIO));
				LogSupport.debug(getContext(), this, "Creating Product Price for ServiceProduct with Id [" + serviceProductBean.getProductId() + "] and versionId [" + serviceProductBean.getProductVersionID() + "]");
				ProductPrice productPrice = createProductPriceForServiceProduct(pricePlanVersion, product, serviceProductBean, productIO.getPath(), isMandatory);

				//preparing a map of ProductId and ProductPrice
				prodId_ProductPrice_Map.put(productPrice.getProductId(), productPrice);
				//createLegacyModel(pricePlanVersion, productPrice, productIO.getPath(), isMandatory);
				createLegacyModel(pricePlanVersion, productPrice);
			}
			else{
				LogSupport.debug(getContext(), this, "Technical Template not from Unified Billing. Skipping product creation.");
			}
			}
		//}

	}

	private void createPackageProduct(ProductIO productIO, PricePlanVersion pricePlanVersion, long subscriptionType, PriceIO priceIO, Boolean isMandatory) throws HomeException{
		Long productId = Long.parseLong(productIO.getBusinessKey());

		Home productHome = (Home) getContext().get(ProductHome.class);
		Home packageProductHome = (Home) getContext().get(PackageProductHome.class);
		LogSupport.info(getContext(), this, "ProductId " + productId + " is added to the description.");
		//This has to be handled in a much proper way in coming sprint with package and multiple versions using ProductPrice and Old Version
		desc.append("" + productId + " ");
		Product product = (Product)productHome.find(productId);
		if(product == null)
		{
			Product productBean = new Product();
			productBean.setProductId(Long.parseLong(productIO.getBusinessKey()));
			productBean.setProductType(ProductTypeEnum.PACKAGE);
			productBean.setSpid(getSpid());
			LogSupport.debug(getContext(), this, "Creating Product for Package Product with bean [" + productBean + "]");
			product = (Product) productHome.create(getContext(), productBean);
			LogSupport.debug(getContext(), this, "Product with id [" + product.getProductId() + "] created");
		}
		ProductVersionIO[] versions = productIO.getVersions().getVersion();

		for(ProductVersionIO version : versions){
			LogSupport.debug(getContext(), this, "Creating Package Product");
			PackageProduct packageProductBean = (PackageProduct) packageProductHome.create(getContext(), getPackageProduct(version, productIO, subscriptionType));
			LogSupport.debug(getContext(), this, "Creating Product Price for PackageProduct with Id [" + packageProductBean.getProductId() + "] and versionId [" + packageProductBean.getProductVersionID() + "]");
			ProductPrice productPrice = createProductPriceForPackageProduct(pricePlanVersion, product, packageProductBean, productIO.getPath(), version, isMandatory);

				//preparing a map of ProductId and ProductPrice
				prodId_ProductPrice_Map.put(productPrice.getProductId(), productPrice);
				
				LogSupport.debug(getContext(), this, "createPackageProduct :: Successfully set productPrice on context map");
				
				//createLegacyModel(pricePlanVersion, productPrice, productIO.getPath(), isMandatory);
				createLegacyModel(pricePlanVersion, productPrice);
			}
		//}

		ProductComponentIO[]  components = productIO.getVersions().getVersion()[0].getComponents().getComponent();

		for (ProductComponentIO component : components) {
			ProductIO nextProductIO = component.getProduct();
			YesNoIndicator indicator = component.getMandatory();
			if(indicator.getValue().equals(YesNoIndicator.YES.getValue())){
				LogSupport.debug(getContext(), this, "The component [" + component.getProduct().getBusinessKey() + "] is mandatory");
				isMandatory = true;
			}else if (indicator.getValue().equals(YesNoIndicator.NO.getValue())){
				LogSupport.debug(getContext(), this, "The component [" + component.getProduct().getBusinessKey() + "] is optional");
				isMandatory = false;
			}else{
				LogSupport.debug(getContext(), this, "Invalid occurance type [" + indicator.getValue() + "]");
			}
			LogSupport.debug(getContext(), this, "Iterating through the list to serach for more products");
			if(ProdSpecType.Composite.getValue().equalsIgnoreCase(nextProductIO.getType().getValue())){
				LogSupport.debug(getContext(), this, "Got a Package Product again with business key " + nextProductIO.getBusinessKey() + "]. Processing it.");
				createPackageProduct(nextProductIO, pricePlanVersion, subscriptionType, priceIO, isMandatory);
			}else if(ProdSpecType.Atomic.getValue().equalsIgnoreCase(nextProductIO.getType().getValue())){
				LogSupport.debug(getContext(), this, "Got a Atomic Product with business key " + nextProductIO.getBusinessKey() + "]. Processing it.");
				if (nextProductIO.getResource() != null)
				{
					LogSupport.debug(getContext(), this, "Got a Resource Product with business key " + nextProductIO.getBusinessKey() + "]. Processing it.");
					createResourceProduct(nextProductIO, pricePlanVersion, subscriptionType, priceIO, isMandatory);
				}
				else
				{
					LogSupport.debug(getContext(), this, "Got a Service Product with business key " + nextProductIO.getBusinessKey() + "]. Processing it.");
					createServiceProduct(nextProductIO, pricePlanVersion, priceIO, isMandatory);
				}
			}else{
				LogSupport.info(getContext(), this, "Invalid Product Type [" + nextProductIO.getType() + "]");
				//throw new HomeException("Invalid Product Type");
			}
		}

	}

	
	private void createResourceProduct(ProductIO productIO, PricePlanVersion pricePlanVersion, long subscriptionType, PriceIO priceIO, Boolean isMandatory) throws HomeException{
		Long productId = Long.parseLong(productIO.getBusinessKey());

		Home productHome = (Home) getContext().get(ProductHome.class);
		Home resourceProductHome = (Home) getContext().get(ResourceProductHome.class);
		LogSupport.info(getContext(), this, "ProductId " + productId + " is added to the description.");		
		desc.append("" + productId + " ");
		Product product = (Product)productHome.find(productId);
		if(product == null)
		{
			Product productBean = new Product();
			productBean.setProductId(Long.parseLong(productIO.getBusinessKey()));
			productBean.setProductType(ProductTypeEnum.RESOURCE);
			productBean.setSpid(getSpid());
			LogSupport.debug(getContext(), this, "Creating Product for Resource Product with bean [" + productBean + "]");
			product = (Product) productHome.create(getContext(), productBean);
			LogSupport.debug(getContext(), this, "Product with id [" + product.getProductId() + "] created");
		}
		ProductVersionIO[] versions = productIO.getVersions().getVersion();

		for(ProductVersionIO version : versions){
			LogSupport.debug(getContext(), this, "Creating Resource Product");
			ResourceProduct resourceProductBean = (ResourceProduct) resourceProductHome.create(getContext(), getResourceProduct(version, productIO, subscriptionType));
			LogSupport.debug(getContext(), this, "Creating Product Price for ResourceProduct with Id [" + resourceProductBean.getProductId() + "] and versionId [" + resourceProductBean.getProductVersionID() + "]");
			//ProductPrice productPrice = createProductPriceForResourceProduct(pricePlanVersion, product, resourceProductBean, productIO.getPath(), version, isMandatory);

				//preparing a map of ProductId and ProductPrice
				//prodId_ProductPrice_Map.put(productPrice.getProductId(), productPrice);
				
				LogSupport.debug(getContext(), this, "createResourceProduct :: Successfully set productPrice on context map");			
				
				//createLegacyModel(pricePlanVersion, productPrice);
			}
		//}

		
			
		

	}
	private void createLegacyModel(PricePlanVersion pricePlanVersion, ProductPrice productPrice) throws HomeInternalException, HomeException {
		if(productPrice!=null){
			//createServiceFee2(pricePlanVersion, productPrice, path, isMandatory);
			createServiceFee2(pricePlanVersion, productPrice);
		}
		else{
			LogSupport.info(getContext(), this, "[createLegacyModel] Failed to create service fee since product price is null");
		}
	}


	/*private void createServiceFee2(PricePlanVersion pricePlanVersion, ProductPrice productPrice, String path, Boolean isMandatory) {
		try {
			LogSupport.debug(ctx, "PricePlanEntityCreator", " createServiceFee2 -Start");
			ServiceFee2 serviceFee = (new ProductPriceToServiceFeeAdapter()).adapt(ctx, productPrice);
			
			if (null != serviceFee) {
				serviceFee.setPath(path);
				if(isMandatory){
					serviceFee.setServicePreference(ServicePreferenceEnum.MANDATORY);
				}else{
					serviceFee.setServicePreference(ServicePreferenceEnum.OPTIONAL);
				}
				addServiceFeeToServicePackageOfPP(productPrice, pricePlanVersion, serviceFee);
				
			}
		} catch (HomeException e) {
			LogSupport.debug(getContext(), this, "[createServiceFee2] Failed to create service fee for product price [id]:" + productPrice.getProductId()
					+ ", Exception: " + e.getMessage());
		}
	}*/
	
	private void createServiceFee2(PricePlanVersion pricePlanVersion, ProductPrice productPrice) {
		try {
			LogSupport.debug(ctx, "PricePlanEntityCreator", " createServiceFee2 -Start");
			ServiceFee2 serviceFee = (new ProductPriceToServiceFeeAdapter()).adapt(ctx, productPrice);
			
			if (null != serviceFee) {
				addServiceFeeToServicePackageOfPP(productPrice, pricePlanVersion, serviceFee);
				
			}
		} catch (HomeException e) {
			LogSupport.debug(getContext(), this, "[createServiceFee2] Failed to create service fee for product price [id]:" + productPrice.getProductId()
					+ ", Exception: " + e.getMessage());
		}
	}

	private void addServiceFeeToServicePackageOfPP(ProductPrice productPrice, PricePlanVersion pricePlanVersion, ServiceFee2 serviceFee) {
		LogSupport.debug(getContext(), this, "Start of addServiceFeeToServicePackageOfPP");
		try {
			And filter = new And();
			filter.add(new EQ(PricePlanVersionXInfo.ID, productPrice.getPricePlanVersionId()));

			if (null == pricePlanVersion){
				pricePlanVersion = HomeSupportHelper.get(getContext()).findBean(getContext(), PricePlanVersion.class, filter);
			}
			if (pricePlanVersion != null) {
				ServicePackageVersion servicePackageVersion = pricePlanVersion.getServicePackageVersion(getContext());
				if (null == servicePackageVersion) {
					servicePackageVersion = XBeans.instantiate(ServicePackageVersion.class, getContext());
				}
				Map<ServiceFee2ID, ServiceFee2> serviceFeeMap = servicePackageVersion.getServiceFees();
				if (null == serviceFeeMap) {
					serviceFeeMap = new HashMap<ServiceFee2ID, ServiceFee2>();
					//servicePackageVersion.setServiceFees(serviceFeeMap);
				}
				LogSupport.debug(getContext(), this, "Service Fee  " + serviceFee);
				//Resetting the values in PricePlanVersion
		
				//servicePackageVersion.setPath(path);
				if(serviceFeeMap.containsKey((ServiceFee2ID)serviceFee.ID())){
					throw new HomeException("ServiceFee2 id and path combination already exists.");
				}
				
				serviceFeeMap.put((ServiceFee2ID)serviceFee.ID(), serviceFee);
				servicePackageVersion.setServiceFees(serviceFeeMap);
				
				LogSupport.debug(getContext(), this, "servicePackageVersion  " + servicePackageVersion);
		
				pricePlanVersion.setServicePackageVersion(servicePackageVersion);
				LogSupport.debug(getContext(), this, "pricePlanVersion  " + pricePlanVersion);
				PricePlanVersion ppvBean = HomeSupportHelper.get(getContext()).storeBean(ctx, pricePlanVersion);
			}else{
				LogSupport.debug(getContext(), this, "[addServiceFeeToServicePackageOfPP] Unable to find PricePlanVersion [id]: " + productPrice
						.getPricePlanVersionId() + ", ServiceFee2 will not be created for product price [id]: " + productPrice.getProductId());
			}
		} catch (HomeInternalException e) {
			LogSupport.minor(getContext(), this, "[addServiceFeeToServicePackageOfPP] Failed to create service fee for product price [id]:" + productPrice.getProductId()
					+ ", Exception: " + e.getMessage());
		} catch (HomeException e) {
			LogSupport.minor(getContext(), this, "[addServiceFeeToServicePackageOfPP] Failed to create service fee for product price [id]:" + productPrice.getProductId()
					+ ", Exception: " + e.getMessage());
		}
	}


	/**
	 * @param pricePlanVersion
	 * @param product
	 * @param packageProduct 
	 * @param version 
	 * @param isMandatory 
	 * @param string 
	 * @throws HomeException
	 * @throws HomeInternalException
	 */
	private ProductPrice createProductPriceForPackageProduct(PricePlanVersion pricePlanVersion, Product product, PackageProduct packageProduct, String path, ProductVersionIO version, Boolean isMandatory) throws HomeException,
	HomeInternalException {
		Home productPriceHome = (Home) getContext().get(ProductPriceHome.class);

		ProductPrice productPrice = new ProductPrice();
		productPrice.setPricePlanId(pricePlanVersion.getPricePlan(getContext()).getId());
		productPrice.setProductId(product.getProductId());
		productPrice.setPricePlanVersionId(pricePlanVersion.getVersion());
		productPrice.setSpid(getSpid());
		productPrice.setIsPrimary(false);
		productPrice.setProductVersionId(packageProduct.getProductVersionID());
		productPrice.setPath(path);
		//YesNoIndicator indicator = version.getComponents().getComponent()[0].getMandatory();
		if(isMandatory){
			productPrice.setPreference(ServicePreferenceEnum.MANDATORY);
		}else{
			productPrice.setPreference(ServicePreferenceEnum.OPTIONAL);
		}

		ProductPrice productPriceBean = (ProductPrice) productPriceHome.create(ctx, productPrice);
		return productPriceBean;
	}

	private ProductPrice createProductPriceForServiceProduct(PricePlanVersion pricePlanVersion, Product product, ServiceProduct serviceProduct, String path, Boolean isMandatory) throws HomeException,
	HomeInternalException {
		Home productPriceHome = (Home) getContext().get(ProductPriceHome.class);

		ProductPrice productPrice = new ProductPrice();
		productPrice.setPricePlanId(pricePlanVersion.getPricePlan(getContext()).getId());
		productPrice.setProductId(product.getProductId());
		productPrice.setPricePlanVersionId(pricePlanVersion.getVersion());
		productPrice.setSpid(getSpid());
		productPrice.setIsPrimary(false);
		productPrice.setProductVersionId(serviceProduct.getProductVersionID());
		productPrice.setPath(path);
		if(isMandatory){
			productPrice.setPreference(ServicePreferenceEnum.MANDATORY);
		}else{
			productPrice.setPreference(ServicePreferenceEnum.OPTIONAL);
		}
		ProductPrice productPriceBean = (ProductPrice) productPriceHome.create(ctx, productPrice);
		return productPriceBean;
	}

	private PackageProduct getPackageProduct(ProductVersionIO version, ProductIO productIO, long subscriptionType) throws HomeInternalException, HomeException {
		PackageProduct packageProduct = new PackageProduct();
		LogSupport.debug(getContext(), this, "Populating the Package Product Entity");
		packageProduct.setProductId(Long.parseLong(productIO.getBusinessKey()));
		packageProduct.setName(productIO.getName());
		packageProduct.setEffectiveFromDate((version.getEffectiveFromDate().getTime()));
		packageProduct.setDescription(productIO.getDescription());
		packageProduct.setSpid(getSpid());
		//packageProduct.setUnifiedServiceID(productIO.getBusinessKey());
		
		//Fetch package product template from Unified Catalog Spid Config
		LogSupport.debug(getContext(), this, "Searching for the Default Package product Template");
		TechnicalServiceTemplate packageTemplate = PublishOfferSupport.getPackageProductTemplate(ctx, getSpid());

		//If available as a CharSpec, use this otherwise copy it from the template
		LogSupport.debug(getContext(), this, "Populating the default values in Package Product Entity from Template");
		PricePlanEntityDefaultValueCreator.setPackageProductDefaultValues(packageTemplate, packageProduct);
		//If available as a CharSpec, use this otherwise copy it from the template
		//RfssIO technicalTemplateIO = getTechnicalTemplateIO(productIO);
		//getCharacteristics(technicalTemplateIO, packageProduct);

		//Enrich the properties which needs to be calculated
		LogSupport.debug(getContext(), this, "Calculating values and populating in Package Product Entity");
		getCalculatedProperties(packageProduct, productIO, subscriptionType);

		LogSupport.debug(getContext(), this, "Populating the Package Product Entity complete [" + packageProduct + "].");
		return packageProduct;
	}

	private ResourceProduct getResourceProduct(ProductVersionIO version, ProductIO productIO, long subscriptionType) throws HomeInternalException, HomeException {
		ResourceProduct resourceProduct = new ResourceProduct();
		LogSupport.debug(getContext(), this, "Populating the Resource Product Entity");
		resourceProduct.setProductId(Long.parseLong(productIO.getBusinessKey()));
		resourceProduct.setName(productIO.getName());
		resourceProduct.setEffectiveFromDate((version.getEffectiveFromDate().getTime()));
		resourceProduct.setDescription(productIO.getDescription());
		resourceProduct.setSpid(getSpid());
		//resourceProduct.setUnifiedServiceID(productIO.getBusinessKey());
		
		//Fetch resource/device product template from Unified Catalog Spid Config
		LogSupport.debug(getContext(), this, "Searching for the Default Resource product Template");
		TechnicalServiceTemplate resourceTemplate = PublishOfferSupport.getDeviceProductTemplate(ctx, getSpid());

		//If available as a CharSpec, use this otherwise copy it from the template
		LogSupport.debug(getContext(), this, "Populating the default values in Resource Product Entity from Template");
		PricePlanEntityDefaultValueCreator.setResourceProductDefaultValues(resourceTemplate, resourceProduct);
		//If available as a CharSpec, use this otherwise copy it from the template
		//RfssIO technicalTemplateIO = getTechnicalTemplateIO(productIO);
		//getCharacteristics(technicalTemplateIO, resourceProduct);

		//Enrich the properties which needs to be calculated
		LogSupport.debug(getContext(), this, "Calculating values and populating in Resource Product Entity");
		//getCalculatedProperties(resourceProduct, productIO, subscriptionType);

		LogSupport.debug(getContext(), this, "Populating the Resource Product Entity complete [" + resourceProduct + "].");
		return resourceProduct;
	}
	
	private void getCalculatedProperties(PackageProduct packageProduct, ProductIO productIO, long subscriptionType) throws HomeException, HomeInternalException, NumberFormatException {
		ProductComponentIO[] components = productIO.getVersions().getVersion()[0].getComponents().getComponent();
		StringBuilder componentListString = new StringBuilder();
		for (ProductComponentIO component : components) {
			componentListString.append(component.getProduct().getBusinessKey() + " | ");
		}
		packageProduct.setComponents(componentListString.toString());
		packageProduct.setLastModifiedDate(new Date());
		packageProduct.setSubscriptionType(subscriptionType);
		TechnicalServiceTemplate packageTemplate = PublishOfferSupport.getPackageProductTemplate(ctx, getSpid());
		packageProduct.setTechnicalServiceTemplateId(packageTemplate.getID());
		packageProduct.setTechnologyType(getTechnologyTypeFromUCSpidConfig(getSpid(), subscriptionType));


	}

	private ServiceProduct getServiceProduct(ProductVersionIO version, ProductIO productIO) throws HomeException{
		ServiceProduct serviceProduct = new ServiceProduct();
		LogSupport.debug(getContext(), this, "Populating the Service Product Entity");
		serviceProduct.setProductId(Long.parseLong(productIO.getBusinessKey()));
		serviceProduct.setDescription(productIO.getDescription());
		serviceProduct.setName(productIO.getName());
		serviceProduct.setEffectiveFromDate(version.getEffectiveFromDate().getTime());
		//serviceProduct.setUnifiedServiceID(productIO.getBusinessKey());

		RfssIO technicalTemplateIO = getTechnicalTemplateIO(productIO);
		LogSupport.debug(getContext(), this, "Searching for the Technical Service Template");
		TechnicalServiceTemplate tst = PublishOfferSupport.getTechnicalServiceTemplate(ctx, technicalTemplateIO.getBusinessKey());
		if(tst == null){
			throw new HomeException("Technical Service Template with business key [" + technicalTemplateIO.getBusinessKey() + "] not found. Please provide a valid technical service template");
		}
		//Need to check technical template exists
		LogSupport.debug(getContext(), this, "Populating the default values in Service Product Entity from Template");
		PricePlanEntityDefaultValueCreator.setServiceProductDefaultValues(tst, serviceProduct);
		NetworkTechnology networkTechnology = getNetworkTechology(productIO);
		if(networkTechnology != null){
			serviceProduct.setTechnologyType(TechnologyEnum.get(networkTechnology.getIndex()));
		}
		else{
			LogSupport.info(ctx, this, "Network technoloy not found in request, setting from Technical Service Template.");
			serviceProduct.setTechnologyType(tst.getTechnology());
		}
		LogSupport.debug(getContext(), this, "Populating the Servuce Product Entity complete [" + serviceProduct + "].");
		return serviceProduct;
	}

	/**
	 * @param productIO
	 * @return 
	 */
	private RfssIO getTechnicalTemplateIO(ProductIO productIO) {
		CustFacingServSpecVersionIO[] serviceVersionsIO = productIO.getService().getVersions().getVersion();
		RfssIO[] technicalTemplatesIO = serviceVersionsIO[0].getTechnicalServices().getTechnicalService();
		for(RfssIO technicalTemplateIO : technicalTemplatesIO){
			if(technicalTemplateIO.getSource().equalsIgnoreCase(PCConstants.SOURCE)){
				return technicalTemplateIO;
			}
		}
		return null;
	}



	private NetworkTechnology getNetworkTechology(ProductIO productIO) {
		CustFacingServSpecVersionIO[] version = productIO.getService().getVersions().getVersion();
		RfssIO[] technicalTemplates = version[0].getTechnicalServices().getTechnicalService();
		for(RfssIO technicalTemplate:technicalTemplates){
			if(technicalTemplate.getSource().equalsIgnoreCase(PCConstants.SOURCE)){
				NetworkTechnology networkTechnology = new NetworkTechnology();
				NetworkTechnologyIO ntIO = technicalTemplate.getSupportedNetworkTechnology();
				if(ntIO != null){
					networkTechnology.setIndex(Short.parseShort(ntIO.getName()));
					networkTechnology.setDescription(ntIO.getDescription());
					return networkTechnology;
				}

			}
		}
		return null;
	}

	private void getCharacteristics(RfssIO technicalTemplate, PackageProduct packageProduct) {

		RfssVersionIO[] charVersions = technicalTemplate.getVersions().getVersion();
		CharIO[] characteristics = charVersions[0].getCharacteristics().getCharacteristic();
		Map<String, Object> characteristicsMap = new HashMap<String, Object>();
		for(CharIO characteristic : characteristics){
			String charSpecName = characteristic.getCharSpec().getName();

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

	/**
	 * @param serviceId of {@link ServiceFee2} entity 
	 * @param path of {@link ServiceFee2} entity
	 * @return String Combination of Service Id and separator and Path of serviceFeeMap which contains ServiceFees of ServicePackageVersion
	 *//*	 
	public static String getServiceFee2Key(long serviceId, String path) {
		if (path == null) {
			throw new IllegalArgumentException("Path is null");
		} else if (serviceId == -1) {
			throw new IllegalArgumentException("Service Id is invalid");
		}
		return serviceId + ID_PATH_SEPERATOR + path;
	}
	
	*//**	
	 * @param serviceFee2Key Combination of Service Id and separator and Path
	 * @return Long value of ServiceId of {@link ServiceFee2}
	 *//* 
	public static long getServiceIdFromServiceFee2Key(String serviceFee2Key) {
		if (serviceFee2Key != null) {
			String[] tokens = serviceFee2Key.split(ID_PATH_SEPERATOR);
			if (tokens != null && tokens.length == 2) {
				return Long.parseLong(tokens[0]);
			} else {
				return -1;
			}
		} else {
			return -1;
		}
	}
	
	*//**	
	 * @param serviceFee2Key  Combination of Service Id and separator and Path 
	 * @return String value of Path entity of {@link ServiceFee2}
	 *//*	 
	public static String getPathFromServiceFee2Key(String serviceFee2Key) {
		if (serviceFee2Key != null) {
			String[] tokens = serviceFee2Key.split(ID_PATH_SEPERATOR);
			if (tokens != null && tokens.length == 2) {
				return tokens[1];
			} else {
				return null;
			}
		} else {
			return null;
		}
	}*/
	
}
