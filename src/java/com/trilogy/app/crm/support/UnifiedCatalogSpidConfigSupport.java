/**
 * 
 */
package com.trilogy.app.crm.support;

import java.util.Map;

import com.trilogy.app.crm.bean.AdjustmentInfo;
import com.trilogy.app.crm.bean.AdjustmentType;
import com.trilogy.app.crm.bean.AdjustmentTypeXInfo;
import com.trilogy.app.crm.bean.UnifiedCatalogSpidConfig;
import com.trilogy.app.crm.bean.UnifiedCatalogSpidConfigXInfo;
import com.trilogy.app.crm.bean.ui.PackageProduct;
import com.trilogy.app.crm.bean.ui.Product;
import com.trilogy.app.crm.bean.ui.ProductXInfo;
import com.trilogy.app.crm.bean.ui.TechnicalServiceTemplate;
import com.trilogy.app.crm.bean.ui.TechnicalServiceTemplateXInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * @author sachatte
 *
 */
public class UnifiedCatalogSpidConfigSupport {

	public static String CLASS_NAME = UnifiedCatalogSpidConfigSupport.class.getName();

	public static int getAdjustmentType(Context ctx, int spid) {
		UnifiedCatalogSpidConfig spidConfig = null;
		try {
			And filter = new And();
			filter.add(new EQ(UnifiedCatalogSpidConfigXInfo.SPID, spid));
			spidConfig = (UnifiedCatalogSpidConfig) HomeSupportHelper.get(ctx).findBean(ctx,
					UnifiedCatalogSpidConfig.class, filter);
		} catch (Exception e) {
			LogSupport.major(ctx, CLASS_NAME, "Cannot find Adjustment type configuration for Spid ID" + spid);
		}
		if (spidConfig != null) {
			return spidConfig.getDeaultAdjustmentType();
		}
		return 0;
	}

	/**
	 * @param ctx
	 * @param spid
	 * @return String
	 * @throws HomeException
	 */
	public static String getDefaultGLCode(Context ctx, int spid) throws HomeException {
		UnifiedCatalogSpidConfig spidConfig = null;
		try {
			And filter = new And();
			filter.add(new EQ(UnifiedCatalogSpidConfigXInfo.SPID, spid));
			spidConfig = (UnifiedCatalogSpidConfig) HomeSupportHelper.get(ctx).findBean(ctx,
					UnifiedCatalogSpidConfig.class, filter);
		} catch (Exception e) {
			LogSupport.major(ctx, CLASS_NAME, "Cannot find Adjustment type configuration for Spid ID" + spid);
		}

		if (spidConfig == null) {
			LogSupport.major(ctx, CLASS_NAME, "Unified Catalog SPID Config not found for Spid ID" + spid);
			throw new HomeException("Unified Catalog SPID Config not found for Spid ID" + spid);
		}

		AdjustmentType adjustmentType = null;
		try {
			And filterForAdjType = new And();
			filterForAdjType.add(new EQ(AdjustmentTypeXInfo.CODE, spidConfig.getDeaultAdjustmentType()));
			adjustmentType = (AdjustmentType) HomeSupportHelper.get(ctx).findBean(ctx, AdjustmentType.class,
					filterForAdjType);
		} catch (Exception e) {
			LogSupport.major(ctx, CLASS_NAME,
					"Cannot find AdjustmentType with ID [" + spidConfig.getDeaultAdjustmentType() + "]");
		}

		if (adjustmentType == null) {
			LogSupport.major(ctx, CLASS_NAME,
					"Cannot find AdjustmentType with ID [" + spidConfig.getDeaultAdjustmentType() + "]");
			throw new HomeException(
					"Cannot find AdjustmentType with ID [" + spidConfig.getDeaultAdjustmentType() + "]");
		}

		final Map spidInformation = adjustmentType.getAdjustmentSpidInfo();
		final Object key = Integer.valueOf(spid);
		AdjustmentInfo information = (AdjustmentInfo) spidInformation.get(key);

		if (information == null) {
			LogSupport.major(ctx, CLASS_NAME,
					"Cannot find SpidInfo of AdjustmentType with ID [" + spidConfig.getDeaultAdjustmentType() + "]");
			throw new HomeException(
					"Cannot find SpidInfo of AdjustmentType with ID [" + spidConfig.getDeaultAdjustmentType() + "]");
		}

		return information.getGLCode();
	}
	
	public static TechnicalServiceTemplate getPackageProductTemplateForDefaultValues(Context ctx,
			PackageProduct packageProduct) {
		int spid = 0;
		UnifiedCatalogSpidConfig spidConfig = null;
		TechnicalServiceTemplate defaultPackageProductTemplate = null;

		try {
			And filterForProduct = new And();
			filterForProduct.add(new EQ(ProductXInfo.PRODUCT_ID, packageProduct.getProductId()));
			Product product = (Product) HomeSupportHelper.get(ctx).findBean(ctx, Product.class, filterForProduct);
			spid = product.getSpid();
		} catch (Exception e) {

		}
		try {
			And filterForUSCS = new And();
			filterForUSCS.add(new EQ(UnifiedCatalogSpidConfigXInfo.SPID, spid));
			spidConfig = (UnifiedCatalogSpidConfig) HomeSupportHelper.get(ctx).findBean(ctx,
					UnifiedCatalogSpidConfig.class, filterForUSCS);
		} catch (Exception e) {
			LogSupport.major(ctx, CLASS_NAME, "Cannot find Unified Catalog Spid Configuration for Spid ID  " + spid);
		}
		if (spidConfig != null) {
			long id = spidConfig.getDeaultPackageProductTemplate();
			try {
				And filterForTST = new And();
				filterForTST.add(new EQ(TechnicalServiceTemplateXInfo.ID, id));
				defaultPackageProductTemplate = (TechnicalServiceTemplate) HomeSupportHelper.get(ctx).findBean(ctx,
						TechnicalServiceTemplate.class, filterForTST);
			} catch (Exception e) {
				LogSupport.major(ctx, CLASS_NAME,
						"Cannot find Default Package Product Template type configuration for Unified Catalog Spid Configuration of Spid ID "
								+ spid);
			}
		}
		return defaultPackageProductTemplate;
	}
}