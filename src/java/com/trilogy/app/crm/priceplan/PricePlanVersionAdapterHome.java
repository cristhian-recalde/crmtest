package com.trilogy.app.crm.priceplan;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import com.trilogy.app.crm.bean.PricePlanVersion;
import com.trilogy.app.crm.bean.ProductPrice;
import com.trilogy.app.crm.bean.ProductPriceHome;
import com.trilogy.app.crm.bean.ProductPriceXInfo;
//import com.trilogy.app.crm.bean.ProductTypeEnum;
import com.trilogy.app.crm.bean.ProductTypeEnum;
import com.trilogy.app.crm.bean.ProductPriceUi;
//import com.trilogy.app.crm.bean.ServiceFees3;
import com.trilogy.app.crm.bean.ui.PackageProduct;
import com.trilogy.app.crm.bean.ui.PackageProductHome;
import com.trilogy.app.crm.bean.ui.PackageProductXInfo;
import com.trilogy.app.crm.bean.ui.Product;
import com.trilogy.app.crm.bean.ui.ProductHome;
import com.trilogy.app.crm.bean.ui.ServiceProduct;
import com.trilogy.app.crm.bean.ui.ServiceProductHome;
import com.trilogy.app.crm.bean.ui.ServiceProductXInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * 
 * @author AChatterjee
 *
 */
public class PricePlanVersionAdapterHome extends HomeProxy{

	private static final long serialVersionUID = 1L;

	public PricePlanVersionAdapterHome(final Context ctx, final Home delegate)
	{
		super(ctx, delegate);
	}


	@Override
	public Object find(Context ctx, Object obj) throws HomeException,
	HomeInternalException {

		PricePlanVersion pricePlanVersion = (PricePlanVersion)getDelegate(ctx).find(obj);
		attachProductPrice(ctx, pricePlanVersion);
		return pricePlanVersion;
	}



	@Override
	public Collection<PricePlanVersion> select(Context ctx, Object obj) throws HomeException,
	HomeInternalException {

		Collection<PricePlanVersion> pricePlanVersions  = getDelegate(ctx).select(obj);

		if(pricePlanVersions != null && !pricePlanVersions.isEmpty()){
			Iterator<PricePlanVersion> iterator = pricePlanVersions.iterator();
			while(iterator.hasNext()){
				PricePlanVersion pricePlanVersion = (PricePlanVersion) iterator.next();
				attachProductPrice(ctx, pricePlanVersion);
			}
		}
		return pricePlanVersions;
	}

	/*private void attachProductPrice(Context ctx,
			PricePlanVersion pricePlanVersion) throws HomeException,
			HomeInternalException {
		if(pricePlanVersion != null){
			final And filter = new And();
			filter.add(new EQ(ProductPriceXInfo.PRICE_PLAN_ID, pricePlanVersion.getId()));
			filter.add(new EQ(ProductPriceXInfo.PRICE_PLAN_VERSION_ID, pricePlanVersion.getVersion()));

			Home home = (Home) ctx.get(ProductPriceHome.class);

			ProductPrice productPrice = (ProductPrice) home.find(ctx, filter);
			if(productPrice != null){
				Long productId = productPrice.getProductId();
				Long productVersionId = productPrice.getProductVersionId();
				String productName = getProductName(ctx, productId, productVersionId);
				ProductPriceUi serviceFee = new ProductPriceUi();
				serviceFee.setProductId(productId);
				serviceFee.setProductName(productName);
				Map<Long, Object> serviceFeeMap = new HashMap<Long, Object>();
				serviceFeeMap.put(productId, serviceFee);
				pricePlanVersion.setProductFees(serviceFeeMap);
			}
		}
	}*/
	
	
	private void attachProductPrice(Context ctx, PricePlanVersion pricePlanVersion) throws HomeException, HomeInternalException {
		if(pricePlanVersion != null){
			Map<Long, Object> serviceFeeMap = new HashMap<Long, Object>();
			final And filter = new And();
			filter.add(new EQ(ProductPriceXInfo.PRICE_PLAN_ID, pricePlanVersion.getId()));
			filter.add(new EQ(ProductPriceXInfo.PRICE_PLAN_VERSION_ID, pricePlanVersion.getVersion()));

			Home home = (Home) ctx.get(ProductPriceHome.class);

			Collection<ProductPrice> productPrices = (Collection<ProductPrice>) home.select(ctx, filter);
			if(productPrices != null && !productPrices.isEmpty()){
				Iterator<ProductPrice> iterator = productPrices.iterator();
				while(iterator.hasNext()){
					ProductPrice productPrice = (ProductPrice) iterator.next();
					if(productPrice != null){
						Long productId = productPrice.getProductId();
						Long productVersionId = productPrice.getProductVersionId();
						String productName = getProductName(ctx, productId, productVersionId);
						ProductPriceUi serviceFee = new ProductPriceUi();
						serviceFee.setProductId(productId);
						serviceFee.setProductName(productName);
						serviceFee.setServicePreference(productPrice.getPreference());
						serviceFee.setPrimary(false);
						serviceFeeMap.put(productId, serviceFee);
					}
				}
				pricePlanVersion.setProductFees(serviceFeeMap);
			}
			
		}
	}

	private String getProductName(Context ctx, Long productId, Long productVersionId) throws HomeInternalException, HomeException{
		String productName = null;
		Home productHome = (Home) ctx.get(ProductHome.class);

		if(productHome != null){
			Product product = (Product)productHome.find(productId);
			if(product != null){
				ProductTypeEnum productType = product.getProductType();
				if(productType.equals(ProductTypeEnum.SERVICE))
				{
					final And filter = new And();
					filter.add(new EQ(ServiceProductXInfo.PRODUCT_VERSION_ID, productVersionId));
					filter.add(new EQ(ServiceProductXInfo.PRODUCT_ID, productId));
					Home seviceProductHome = (Home) ctx.get(ServiceProductHome.class);

					if(seviceProductHome != null){
						ServiceProduct serviceProduct = (ServiceProduct) seviceProductHome.find(filter);
						if(serviceProduct != null){
							productName = serviceProduct.getName();
						}
						else{
							LogSupport.debug(ctx, this, "Service product with productId [" + productId + "] and productVersionId [" + productVersionId + "] not found in DB");
						}
					}
				}
				else if(productType.equals(ProductTypeEnum.PACKAGE)){
					final And filter = new And();
					filter.add(new EQ(PackageProductXInfo.PRODUCT_VERSION_ID, productVersionId));
					filter.add(new EQ(PackageProductXInfo.PRODUCT_ID, productId));
					Home packageProductHome = (Home) ctx.get(PackageProductHome.class);

					if(packageProductHome != null){
						PackageProduct packageProduct = (PackageProduct) packageProductHome.find(filter);
						if(packageProduct != null){
							productName = packageProduct.getName();
						}
						else{
							LogSupport.debug(ctx, this, "Package product with productId [" + productId + "] and productVersionId [" + productVersionId + "] not found in DB");
						}
					}
				}
				else{
					LogSupport.debug(ctx, this, "Invalid product type of product with productId [" + productId + "].");
				}
			}
			else{
				LogSupport.debug(ctx, this, "Product with productId [" + productId + "] not found in DB. Name cannot be determined.");
			}
		}
		return productName;
	}
}
