/**
 * 
 */
package com.trilogy.app.crm.integration.pc;

import java.util.ArrayList;
import java.util.List;

import com.trilogy.app.crm.bean.UnifiedCatalogSpidConfig;
import com.trilogy.app.crm.bean.UnifiedCatalogSpidConfigHome;
import com.trilogy.app.crm.bean.UnifiedCatalogSpidConfigXInfo;
import com.trilogy.app.crm.bean.ui.TechnicalServiceTemplate;
import com.trilogy.app.crm.bean.ui.TechnicalServiceTemplateHome;
import com.trilogy.app.crm.bean.ui.TechnicalServiceTemplateXInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextLocator;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.util.crmapi.wsdl.v3_0.api.pc.io.offering.v1.OfferingIO;
import com.trilogy.util.crmapi.wsdl.v3_0.api.pc.io.offering.v1.OfferingVersionIO;
import com.trilogy.util.crmapi.wsdl.v3_0.api.pc.io.price.v1.CompositePriceIO;
import com.trilogy.util.crmapi.wsdl.v3_0.api.pc.io.price.v1.PriceIO;
import com.trilogy.util.crmapi.wsdl.v3_0.api.pc.io.price.v1.PriceVersionIO;
import com.trilogy.util.crmapi.wsdl.v3_0.api.pc.io.product.v1.ProductIO;

/**
 * @author abhay.parashar
 *
 */
public class PublishOfferSupport {

	/**
	 * 
	 */
	public PublishOfferSupport() {
		// TODO Auto-generated constructor stub
	}

	public OfferingVersionIO[] getOfferVersion(OfferingIO paramOfferingIO) {
		if (paramOfferingIO.getEntityBaseIOChoice_type1().getVersions() != null
				&& paramOfferingIO.getEntityBaseIOChoice_type1().getVersions().getVersion() != null)
			return paramOfferingIO.getEntityBaseIOChoice_type1().getVersions().getVersion();
		
		return null;
	}

	public List<PublishedVersionModel> adapt(Context ctx,OfferingVersionIO[] offeringVersionIO, long id) {
		
		LogSupport.debug(ctx, this,"[PublishOfferSupport.adapt] publish version model for Id:"+String.valueOf(id));
		
		List<PublishedVersionModel> publishedVersionModels= new ArrayList<PublishedVersionModel>();
		
		for(OfferingVersionIO version:offeringVersionIO)
		{
			PublishedVersionModel model = new PublishedVersionModel();
			model.setId(id);
			model.setActivationDate(version.getEffectiveFromDate());
			model.setCreateDate(version.getReleaseOnDate());
			model.setProducts(getProductList(version.getProduct()));
			model.setProductPrice(getProductPriceList(version.getPrice()));
			publishedVersionModels.add(model);
		}
			
		return publishedVersionModels;
	}
	
	public static Product getProductFactory(String value) {
		if(value.equalsIgnoreCase("Composite"))
			return new PackageProduct();
		else
			return new ServiceProduct();
	}
	
	public static TechnicalServiceTemplate getTechnicalServiceTemplate(Context ctx, String templateKey) throws HomeInternalException, HomeException{
		
		LogSupport.debug(ctx, PublishOfferSupport.class,"[PublishOfferSupport.getTechnicalServiceTemplate] fetch technical service template for key:"+templateKey);
		
		Home home = (Home) ctx.get(TechnicalServiceTemplateHome.class);
		TechnicalServiceTemplate template = (TechnicalServiceTemplate) home.find(ctx,new EQ(TechnicalServiceTemplateXInfo.ID,Long.parseLong(templateKey)));
		return template;
	}

	private Price getProductPriceList(PriceIO price) {
		
		Context ctx = ContextLocator.locate();
		LogSupport.debug(ctx, this,"[PublishOfferSupport.getProductPriceList] fetch product price list");
		
		ProductPrice productPrice = new ProductPrice();
		PriceVersionIO[] pricingVersion = price.getEntityBaseIOChoice_type0().getVersions().getVersion();
		
		for(PriceVersionIO priceVersionIO:pricingVersion){
			CompositePriceIO compositPrice = priceVersionIO.getVersionIOChoice_type0().getCompositePrice();
			productPrice.setProductId(compositPrice.getProductBusinessKey() == null ? -1l : Long.valueOf(compositPrice.getProductBusinessKey()));
			productPrice.setProductVariantId(compositPrice.getProductVariantKey() == null ? -1l : Long.valueOf(compositPrice.getProductVariantKey()));
			createPriceFromCompositePrice(compositPrice,productPrice);
		}
		return productPrice;
	}

	private Product getProductList(ProductIO product) {
		return getProductFactory(product.getType().getValue()).create(product);
	}
	/*
	 * Recursively creates PricefromProductPrice
	 */
	private void createPriceFromCompositePrice(CompositePriceIO compositPrice,	ProductPrice productPrice) {
		Context ctx = ContextLocator.locate();
		LogSupport.debug(ctx, this,"[PublishOfferSupport.createPriceFromCompositePrice] create price from Composite price");
		
		if (compositPrice.getCompositePrices() != null) {
			for (CompositePriceIO compPrice : compositPrice.getCompositePrices().getCompositePrice()) {
				if (compPrice.getCompositePrices() != null) {
					ProductPrice pp = new ProductPrice();
					pp.setProductId(Long.valueOf(compPrice.getProductBusinessKey()));
					pp.setProductVariantId(Long.valueOf(compPrice.getProductVariantKey()));
					if (productPrice.getPrices() == null)
						productPrice.setPrices(new ArrayList<Price>());
					productPrice.getPrices().add(pp);
					createPriceFromCompositePrice(compPrice, pp);
				} else {
					// productPrice.getPrices().add(e) add individual price here
				}
			}
		}
	}

	public static TechnicalServiceTemplate getPackageProductTemplate(Context ctx, int spid) throws HomeInternalException, HomeException {
		
		if (LogSupport.isDebugEnabled(ctx)) {
			LogSupport.debug(ctx, PublishOfferSupport.class,"[PublishOfferSupport.getPackageProductTemplate] fetch package product template for spid:"+String.valueOf(spid));
		}
		
		Home configHome = (Home) ctx.get(UnifiedCatalogSpidConfigHome.class);
		UnifiedCatalogSpidConfig config = (UnifiedCatalogSpidConfig) configHome.find(ctx,new EQ(UnifiedCatalogSpidConfigXInfo.SPID, spid));
		
		Home templateHome = (Home) ctx.get(TechnicalServiceTemplateHome.class);
		TechnicalServiceTemplate template = (TechnicalServiceTemplate) templateHome.find(ctx,new EQ(TechnicalServiceTemplateXInfo.ID, config.getDeaultPackageProductTemplate()));
		return template;
	}
	
	public static TechnicalServiceTemplate getDeviceProductTemplate(Context ctx, int spid) throws HomeInternalException, HomeException {
		
		if (LogSupport.isDebugEnabled(ctx)) {
			LogSupport.debug(ctx, PublishOfferSupport.class,"[PublishOfferSupport.getDeviceProductTemplate] fetch device product template for spid:"+String.valueOf(spid));
		}
		
		Home configHome = (Home) ctx.get(UnifiedCatalogSpidConfigHome.class);
		UnifiedCatalogSpidConfig config = (UnifiedCatalogSpidConfig) configHome.find(ctx,new EQ(UnifiedCatalogSpidConfigXInfo.SPID, spid));
		
		Home templateHome = (Home) ctx.get(TechnicalServiceTemplateHome.class);
		TechnicalServiceTemplate template = (TechnicalServiceTemplate) templateHome.find(ctx,new EQ(TechnicalServiceTemplateXInfo.ID, config.getDeaultDeviceProductTemplate()));
		return template;
	}
}
