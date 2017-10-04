package com.trilogy.app.crm.integration.pc;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.trilogy.app.crm.adapters.ProductPriceToServiceFeeAdapter;
import com.trilogy.app.crm.bean.PricePlanHome;
import com.trilogy.app.crm.bean.PricePlanVersionHome;
import com.trilogy.app.crm.bean.PricePlanVersionXInfo;
import com.trilogy.app.crm.bean.ProductPriceHome;
import com.trilogy.app.crm.bean.ServiceFee2;
import com.trilogy.app.crm.bean.ServicePackageVersion;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.core.PricePlan;
import com.trilogy.app.crm.bean.core.PricePlanVersion;
import com.trilogy.app.crm.bean.ui.PackageProductHome;
import com.trilogy.app.crm.bean.ui.Product;
import com.trilogy.app.crm.bean.ui.ProductHome;
import com.trilogy.app.crm.bean.ui.ServiceProductHome;
import com.trilogy.app.crm.bean.ui.TechnicalServiceTemplate;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.app.crm.bean.UnifiedCatalogSpidConfig;
import com.trilogy.app.crm.bean.UnifiedCatalogSpidConfigHome;
import com.trilogy.app.crm.technology.TechnologyEnum;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.util.crmapi.wsdl.v3_0.api.pc.io.offering.v1.OfferingIO;
import com.trilogy.util.crmapi.wsdl.v3_0.api.pc.io.offering.v1.OfferingVersionIO;
import com.trilogy.app.crm.bean.SubscriptionTechnologyTypeHolder;

public class PricePlanEntityCreator {

	private Context ctx;
	private int spid;
	private boolean debugEnabled;
	private StringBuilder desc = null;
	private Map<String,com.redknee.app.crm.integration.pc.Product> serviceMap; 

	public PricePlanEntityCreator(Context ctx) {
		this.ctx = ctx;
		this.debugEnabled = LogSupport.isDebugEnabled(ctx);
		serviceMap = new HashMap<String, com.redknee.app.crm.integration.pc.Product>();
		desc = new StringBuilder();
		desc.append("Product ID ");
	}

	private boolean isDebugEnabled() {
		return debugEnabled;
	}
	
	private Context getContext(){
		return ctx;
	}

	private int getSpid() {
		return spid;
	}

	/*
	 * following template pattern here.
	 */
	public void create(Context ctx, PublishOfferPricePlanModel offerPricePlanModel) throws HomeInternalException, HomeException{
		
		if (isDebugEnabled()) {
			LogSupport.debug(ctx, this, "[PricePlanEntityCreator.create] Creating Price plan entity");
		}
		
		PricePlan pp = createPP(offerPricePlanModel);
		spid = pp.getSpid();
		List<PublishedVersionModel> versionModel = offerPricePlanModel.getVersionModel();
		
		for(PublishedVersionModel model:versionModel){
			
			PricePlanVersion ppVersion = createPPVersion(model);
			com.redknee.app.crm.integration.pc.Product products = model.getProducts();
			products.setMandtory(true); // first product is always mandatory
			String key = pp.getId()+""+model.getVersionId();
			if (isDebugEnabled()) {
				LogSupport.debug(ctx, this, "[PricePlanEntityCreator.create] Price plan entity Key:"+key);
			}
			populateOfferVersionProductMap(products,key);
			createProduct(products, model.getProductPrice());
			desc.append(" Added.");
			createProductPrice(model.getProductPrice(),ppVersion);
			//updatePPV for description
            updateDescriptionInPPVersion(ppVersion);
			createLegacyModel(model.getProductPrice(),ppVersion);
		}
	}
	
	public void create(Context subCtx, OfferingIO offering) throws NumberFormatException, HomeInternalException, HomeException {
		int spid = Integer.valueOf(offering.getSpid());
		
		if (isDebugEnabled()) {
			LogSupport.debug(ctx, this, "[PricePlanEntityCreator.create] Creating Price plan entity for spid:"+spid);
		}
		
		//PricePlanCreator pricePlanCreator = new PricePlanCreator(ctx);
		PricePlanCreator pricePlanCreator = new PricePlanCreator(ctx, spid);
		PricePlan pricePlan = pricePlanCreator.createPricePlan(offering);
		OfferingVersionIO[] versions = offering.getEntityBaseIOChoice_type1().getVersions().getVersion();
		if (isDebugEnabled()) {
			LogSupport.debug(ctx, this, "[PricePlanEntityCreator.create] Creating Price plan versions");
		}
		for (OfferingVersionIO version : versions) {
			PricePlanVersion pricePlanVersion = pricePlanCreator.createPricePlanVersion(version, pricePlan.getId());
			pricePlanCreator.createXProduct(version.getProduct(), pricePlanVersion, pricePlan.getSubscriptionType(), version.getPrice());
			//desc.append(" Added.");
//			pricePlanCreator.updateDescriptionInPPVersion(pricePlanVersion);
			
		}
		
	}

	
	/**
     * @param ppVersion
     * @throws HomeException
     * @throws HomeInternalException
     */
    private void updateDescriptionInPPVersion(PricePlanVersion ppVersion)
    throws HomeException, HomeInternalException {
        ppVersion.setDescription(desc.toString());
        desc.setLength(0);
        Home home = (Home) getContext().get(PricePlanVersionHome.class);
        if( isDebugEnabled()) {
        	LogSupport.debug(getContext(), this, "[updateDescriptionInPPVersion] Updating PricePlanVersion with description " + ppVersion.getDescription());
        }
        home.store(getContext(), ppVersion);
    }

	private void createProduct(
			com.redknee.app.crm.integration.pc.Product products, Price productPrice)
	throws HomeInternalException, HomeException {
		
		if(products instanceof PackageProduct){
			if( isDebugEnabled()) {
				LogSupport.debug(getContext(), this, "[createProduct]  Creating package product");
			}
			ProductPrice prodPrice = (ProductPrice)productPrice;
			createPackageProduct((PackageProduct) products, prodPrice);
		}else{
			if( isDebugEnabled()) {
				LogSupport.debug(getContext(), this, "[createProduct]  Creating service product");
			}
			ProductPrice prodPrice = (ProductPrice)productPrice;
			createServiceProduct((ServiceProduct)products, prodPrice);
		}
	}


	private void createServiceProduct(ServiceProduct product, ProductPrice prodPrice) throws HomeInternalException, HomeException {
		Home home = (Home) getContext().get(ProductHome.class);
		Home versionHome = (Home) getContext().get(ServiceProductHome.class);
		ServiceModel serviceProduct = product.getServiceProduct();
		//get productId
		Long productId = Long.parseLong(serviceProduct.getServiceId());
		LogSupport.info(getContext(), this, "[createServiceProduct] ProductId " + productId + " is added to the description.");
		//This has to be handled in a much proper way in coming sprint with package and multiple versions using ProductPrice and Old Version
		desc.append("" + productId + " ");

		if(home.find(productId) == null)
		{
			home.create(getContext(), getProduct(serviceProduct));
			List<ServiceVersionModel> serviceVersions = serviceProduct.getServiceVersion();
			for(ServiceVersionModel version:serviceVersions){
				com.redknee.app.crm.bean.ui.ServiceProduct serviceProductBean = (com.redknee.app.crm.bean.ui.ServiceProduct)versionHome.create(getContext(), getServiceProduct(version));
				prodPrice.setServiceVersionId(serviceProductBean.getProductVersionID());
			}
		}else{
			
		}
	}
	
	private void createLegacyModel(Price productPrice, PricePlanVersion ppVersion) throws HomeInternalException, HomeException {
		if(productPrice!=null){
			createServiceFee2((ProductPrice)productPrice, ppVersion);
		}
	}
	
	
	private void createServiceFee2(ProductPrice productPrice,
			PricePlanVersion ppVersion) {
		try {
			if (isDebugEnabled()) {
				LogSupport.debug(ctx, this, " [createServiceFee2] Start");
			}
			ServiceFee2 serviceFee = (new ProductPriceToServiceFeeAdapter())
					.adapt(ctx, productPrice);
			if (null != serviceFee) {
				addServiceFeeToServicePackageOfPP(ctx, productPrice, ppVersion, serviceFee);
			}
		} catch (HomeException e) {
			LogSupport.minor(getContext(), this.getClass(), "[createServiceFee2] Failed to create service fee for product price [id]:" + productPrice.getProductId()
					+ ", Exception: " + e.getMessage());
		}
	}

	private void addServiceFeeToServicePackageOfPP(Context ctx,
			ProductPrice productPrice, PricePlanVersion ppVersion, ServiceFee2 serviceFee) {
		
		if (isDebugEnabled()) {
			LogSupport.debug(getContext(), this.getClass(), "[addServiceFeeToServicePackageOfPP] Adding service fee to service Package");
		}
		
		try {
			And filter = new And();
			filter.add(new EQ(PricePlanVersionXInfo.ID, productPrice
					.getPricePlanVersionId()));

			if (null == ppVersion){
				ppVersion = HomeSupportHelper.get(ctx).findBean(ctx,
					PricePlanVersion.class, filter);
			}
			
			if (ppVersion != null) {
				ServicePackageVersion servicePackageVersion = ppVersion
						.getServicePackageVersion(ctx);
				if (null == servicePackageVersion) {
					servicePackageVersion = XBeans.instantiate(
							ServicePackageVersion.class, ctx);
				}
				Map<Long, ServiceFee2> serviceFeeMap = servicePackageVersion
						.getServiceFees();
				if (null == serviceFeeMap) {
					serviceFeeMap = new HashMap<Long, ServiceFee2>();
				}
				
				serviceFeeMap.put(serviceFee.getServiceId(), serviceFee);
				
				HomeSupportHelper.get(ctx).storeBean(ctx, ppVersion);
			}else{
				if (isDebugEnabled()) {
					LogSupport.debug(getContext(), this.getClass(), "[addServiceFeeToServicePackageOfPP] Unable to find PricePlanVersion [id]: " + productPrice
						.getPricePlanVersionId() + ", ServiceFee2 will not be created for product price [id]: " + productPrice.getProductId());
				}
			}
		} catch (HomeInternalException e) {
			LogSupport.minor(getContext(), this.getClass(), "[addServiceFeeToServicePackageOfPP] Failed to create service fee for product price [id]:" + productPrice.getProductId()
					+ ", Exception: " + e.getMessage());
		} catch (HomeException e) {
			LogSupport.minor(getContext(), this.getClass(), "[addServiceFeeToServicePackageOfPP] Failed to create service fee for product price [id]:" + productPrice.getProductId()
					+ ", Exception: " + e.getMessage());
		}
	}


	private void createPackageProduct(PackageProduct packageProduct, ProductPrice prodPrice) throws HomeInternalException, HomeException{
		

		Long productId = Long.parseLong(packageProduct.getProductId());
		LogSupport.info(getContext(), this, "[createPackageProduct] ProductId " + productId + " is added to the description.");
		desc.append("" + productId + " ");
		
		Home productHome = (Home) getContext().get(ProductHome.class);
		Home packageProductHome = (Home) getContext().get(PackageProductHome.class);
		
		ServiceModel pProduct =  packageProduct.getPackageProduct();
		if(productHome.find(productId) == null)
		{
			productHome.create(getContext(), getProduct(pProduct));
			List<ServiceVersionModel> serviceVersions = pProduct.getServiceVersion();
			for(ServiceVersionModel version : serviceVersions){
				com.redknee.app.crm.bean.ui.PackageProduct packageProductBean = (com.redknee.app.crm.bean.ui.PackageProduct) packageProductHome.create(getContext(), getPackageProduct(version));
				prodPrice.setServiceVersionId(packageProductBean.getProductVersionID());
			}
		}else{
			
		}
		List<com.redknee.app.crm.integration.pc.Product> products = packageProduct.getProducts();
		if(products.size() > 0){
			Iterator<com.redknee.app.crm.integration.pc.Product> productIterator = products.iterator();
			while(productIterator.hasNext()){
				com.redknee.app.crm.integration.pc.Product product = (com.redknee.app.crm.integration.pc.Product) productIterator.next();
				if(product instanceof PackageProduct){
					createPackageProduct((PackageProduct)product, prodPrice);
				}
				else{
					createServiceProduct((ServiceProduct)product, prodPrice);
				}
			}
		}
		
	}
	
	private Object getPackageProduct(ServiceVersionModel version) throws HomeInternalException, HomeException {
		com.redknee.app.crm.bean.ui.PackageProduct packageProduct = new com.redknee.app.crm.bean.ui.PackageProduct();
		
		if (isDebugEnabled()) {
			LogSupport.debug(getContext(), this, "[getPackageProduct] Service version id:"+version.getServiceId());
		}
		
		packageProduct.setProductId(Long.parseLong(version.getServiceId()));
		packageProduct.setName(version.getServiceName());
		packageProduct.setEffectiveFromDate((version.getEffectiveFromDate().getTime()));
		packageProduct.setSpid(getSpid());
		packageProduct.setDescription(version.getDescription());
		
		//Fetch package product template from Unified Catalog Spid Config
		TechnicalServiceTemplate packageTemplate = PublishOfferSupport.getPackageProductTemplate(ctx, getSpid());
		
		//If available as a CharSpec, use this otherwise copy it from the template
		PricePlanEntityDefaultValueCreator.setPackageProductDefaultValues(packageTemplate, packageProduct);
		
		//TODO If available as a CharSpec, use this otherwise copy it from the template
		
		PricePlanEntityDefaultValueCreator.setCharacteristics(packageProduct, version);
		
		
		if(version.getNetworkTechnology_() != null){
			packageProduct.setTechnologyType(TechnologyEnum.get(version.getNetworkTechnology_().getIndex()));
		}
		else{
			LogSupport.info(ctx, this, "[getPackageProduct] Network technoloy not found in request, setting from Technical Service Template.");
			
			packageProduct.setTechnologyType(packageTemplate.getTechnology());
		}
		return packageProduct;
	}

	private PricePlan createPP(PublishOfferPricePlanModel offerPricePlanModel) throws HomeInternalException, HomeException {
		
		if (isDebugEnabled()) {
			LogSupport.debug(getContext(), this, "[createPP] Create Price Plan for id:"+String.valueOf(offerPricePlanModel.getPricePlanId()));
		}
		
		Home home = (Home) getContext().get(PricePlanHome.class);
		//This code is to handle duplicate priceplan
		PricePlan pp = (PricePlan)home.find(offerPricePlanModel.getPricePlanId());
		if(pp == null){
			pp = (PricePlan) home.create(getContext(), getPricePlan(offerPricePlanModel));
		}
		return pp;
	}

	private PricePlanVersion createPPVersion(PublishedVersionModel model) throws HomeInternalException, HomeException {
		if (isDebugEnabled()) {
			LogSupport.debug(getContext(), this, "[createPPVersion] Create Price Plan version for id:"+String.valueOf(model.getId()));
		}
		Home home = (Home) getContext().get(PricePlanVersionHome.class);
		PricePlanVersion ppVersion = getPricePlanVersion(model);
		//ppVersion.setSpid(getSpid()); need to add spid in ppVersion
		return (PricePlanVersion) home.create(getContext(),ppVersion);
	}

	private void createProductPrice(Price productPrice, PricePlanVersion ppVersion) throws HomeInternalException, HomeException {
		if (isDebugEnabled()) {
			LogSupport.debug(getContext(), this, "[createProductPrice] Create Product Price for Price Plan version id:"+String.valueOf(ppVersion.getId()));
		}
		if(productPrice!=null){
			Home home = (Home) getContext().get(ProductPriceHome.class);
			home.create(getContext(),getProductPrice(productPrice,ppVersion));
			ProductPrice pprice = (ProductPrice)productPrice;
			if(pprice.getPrices()!=null){
				for(Price price: pprice.getPrices()){
					createProductPrice(price,ppVersion);
				}
			}
		}
	}

	private com.redknee.app.crm.bean.ProductPrice getProductPrice(Price productPrice, PricePlanVersion ppVersion) {
		
		ProductPrice prodPrice = (ProductPrice)productPrice; 
		if (isDebugEnabled()) {
			LogSupport.debug(getContext(), this, "[getProductPrice] fetch Product for Id"+String.valueOf(prodPrice.getProductId())+" & for Price Plan version id:"+String.valueOf(ppVersion.getId()));
		}
		com.redknee.app.crm.bean.ProductPrice price = new com.redknee.app.crm.bean.ProductPrice();
		price.setProductId(prodPrice.getProductId());
		price.setPricePlanVersionId(ppVersion.getVersion());
		price.setSpid(getSpid());
		price.setPricePlanId(ppVersion.getId());
		price.setIsPrimary(false);
		price.setProductVersionId(prodPrice.getServiceVersionId());
		return price;
	}

	private PricePlan getPricePlan(PublishOfferPricePlanModel offerPricePlanModel) throws HomeInternalException, HomeException {
		PricePlan pp = new PricePlan();
		if (isDebugEnabled()) {
			LogSupport.debug(getContext(), this, "[getPricePlan] fetching Price plan for Id"+String.valueOf(offerPricePlanModel.getPricePlanId()));
		}
		pp.setId(offerPricePlanModel.getPricePlanId());
		pp.setSubscriptionType(offerPricePlanModel.getSubscriptionType());
		pp.setSpid(offerPricePlanModel.getServiceProviderID());
		pp.setName(offerPricePlanModel.getPricePlanName());
		pp.setPricePlanType(SubscriberTypeEnum.get((short) offerPricePlanModel.getPricePlanType()));
		//pp.setUnifiedOfferId(offerPricePlanModel.getUnifiedOfferId());
		//Set the Network type as the value configured against the SPID
        TechnologyEnum technologyType = null;
        Home home = (Home) ctx.get(UnifiedCatalogSpidConfigHome.class);
        if(home != null){
        	UnifiedCatalogSpidConfig unifiedCatalogSpidConfig = (UnifiedCatalogSpidConfig) home.find(ctx, offerPricePlanModel.getServiceProviderID());
        	if(unifiedCatalogSpidConfig != null){
        		Map map = unifiedCatalogSpidConfig.getSubscriptionTechnologyTypeIds();
        		if(map != null){
        			SubscriptionTechnologyTypeHolder holder = (SubscriptionTechnologyTypeHolder) map.get(offerPricePlanModel.getSubscriptionType());
        			if(holder != null){
        				technologyType = holder.getDefaultTechnologyType();
        			}
        		}
        	}
        }
        if(technologyType != null){
        	pp.setTechnology(technologyType);
        }else{
        	LogSupport.info(getContext(), this, "[getPricePlan] No Matching Technology found. Will continue with " + TechnologyEnum.NO_TECH);
        	pp.setTechnology(TechnologyEnum.NO_TECH);
        }
		PricePlanEntityDefaultValueCreator.setPPDefaultValues(getContext(), pp);
		return pp;
	}

	private PricePlanVersion getPricePlanVersion(PublishedVersionModel model) {
		PricePlanVersion ppVersion = new PricePlanVersion();
		ppVersion.setId(model.getId());
		ppVersion.setActivateDate(model.getActivationDate().getTime());
		if(model.getCreateDate() != null){
			ppVersion.setCreatedDate(model.getCreateDate().getTime());
		}
		PricePlanEntityDefaultValueCreator.setPPVersionDefaultValues(getContext(), ppVersion);
		return ppVersion;
	}

	private Product getProduct(ServiceModel serviceProduct) {
		com.redknee.app.crm.bean.ui.Product product = new Product();
		product.setProductId(Long.parseLong(serviceProduct.getServiceId()));
		product.setProductType(serviceProduct.getProductType());
		product.setSpid(getSpid());
		return product;
	}

	private com.redknee.app.crm.bean.ui.ServiceProduct getServiceProduct(ServiceVersionModel version) throws HomeInternalException, HomeException {
		com.redknee.app.crm.bean.ui.ServiceProduct serviceProduct = new com.redknee.app.crm.bean.ui.ServiceProduct();
		serviceProduct.setProductId(Long.parseLong(version.getServiceId()));
		serviceProduct.setDescription(version.getDescription());
		serviceProduct.setName(version.getServiceName());
		serviceProduct.setEffectiveFromDate(version.getEffectiveFromDate().getTime());
		//serviceProduct.setUnifiedServiceID(version.getServiceId());
		TechnicalServiceTemplate tst = PublishOfferSupport.getTechnicalServiceTemplate(ctx, version.getTechnicalServiceTemplateId());
		PricePlanEntityDefaultValueCreator.setServiceProductDefaultValues(tst, serviceProduct);
		if(version.getNetworkTechnology_() != null){
			serviceProduct.setTechnologyType(TechnologyEnum.get(version.getNetworkTechnology_().getIndex()));
		}
		else{
			LogSupport.info(ctx, this, "[getServiceProduct] Network technoloy not found in request, setting from Technical Service Template.");
			serviceProduct.setTechnologyType(tst.getTechnology());
		}
		return serviceProduct;
	}

	private void populateOfferVersionProductMap(Object products, String key) {
		if(products instanceof PackageProduct){
			PackageProduct prod = (PackageProduct)products;
			String prodKey = key.concat(prod.getProductId());
			serviceMap.put(prodKey, prod);
			for(com.redknee.app.crm.integration.pc.Product product : prod.getProducts())
				populateOfferVersionProductMap(key,prodKey);
		}else{
			ServiceProduct prod = (ServiceProduct)products;
			serviceMap.put(key.concat(prod.getServiceProduct().getServiceId()), prod);
		}
	}

}
