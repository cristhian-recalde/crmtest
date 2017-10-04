/**
 * 
 */
package com.trilogy.app.crm;

import com.trilogy.app.crm.adapters.ProductToServiceAdapter;
import com.trilogy.app.crm.bean.AdjustmentTypeHome;
import com.trilogy.app.crm.bean.AdjustmentTypeXInfo;
import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.ServiceHome;
import com.trilogy.app.crm.bean.core.AdjustmentType;
import com.trilogy.app.crm.bean.ui.AdjustmentTypeNVersion;
import com.trilogy.app.crm.bean.ui.AdjustmentTypeNVersionXDBHome;
import com.trilogy.app.crm.bean.ui.AdjustmentTypeNVersionXInfo;
import com.trilogy.app.crm.bean.ui.PackageProduct;
import com.trilogy.app.crm.bean.ui.PackageProductXInfo;
import com.trilogy.app.crm.bean.ui.PricingVersion;
import com.trilogy.app.crm.bean.ui.PricingVersionXDBHome;
import com.trilogy.app.crm.bean.ui.PricingVersionXInfo;
import com.trilogy.app.crm.bean.ui.ServiceProduct;
import com.trilogy.app.crm.bean.ui.ServiceProductXDBHome;
import com.trilogy.app.crm.bean.ui.ServiceProductXInfo;
import com.trilogy.app.crm.extension.service.BlacklistWhitelistTemplateServiceExtension;
import com.trilogy.app.crm.extension.service.BlacklistWhitelistTemplateServiceExtensionHome;
import com.trilogy.app.crm.extension.service.BlacklistWhitelistTemplateServiceExtensionXInfo;
import com.trilogy.app.crm.extension.service.ExternalServiceTypeExtension;
import com.trilogy.app.crm.extension.service.ExternalServiceTypeExtensionHome;
import com.trilogy.app.crm.extension.service.ExternalServiceTypeExtensionXInfo;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.app.crm.bean.ui.Product;
import com.trilogy.app.crm.bean.ui.ProductXDBHome;
import com.trilogy.app.crm.bean.ui.ProductXInfo;
/**
 * @author abhay.parashar
 *
 */
public class DataModelAdaptionSupport {

	public static Product getProductFromServiceId(Context ctx, long serviceId) throws HomeInternalException, HomeException{
		Home serviceHome = (Home) ctx.get(ProductXDBHome.class);
		And and = new And();
		and.add(new EQ(ProductXInfo.PRODUCT_ID,serviceId));
		return (Product) serviceHome.find(ctx, and);
	}

	public static ServiceProduct getServiceProductFromCompatibilityGroup(Context ctx, long compGroup) throws HomeInternalException, HomeException{
		Home serviceProductHome = (Home) ctx.get(ServiceProductXDBHome.class);
		And and = new And();
		and.add(new EQ(ServiceProductXInfo.COMPATIBILITY_GROUP,compGroup));
		return (ServiceProduct) serviceProductHome.find(ctx, and);
	}

	public static ServiceProduct getLatestServiceVersion(Context ctx, long serviceId) throws HomeInternalException, HomeException{

		Home serviceProductHome = (Home) ctx.get(ServiceProductXDBHome.class);

		Double maxVersionID = (Double)(HomeSupportHelper.get(ctx).max(ctx, ServiceProductXInfo.PRODUCT_VERSION_ID, new EQ(ServiceProductXInfo.PRODUCT_ID,serviceId)));

		// Version not created yet so don't save this service. 
		if(maxVersionID != null){
			int versionId = maxVersionID.intValue();

			And and = new And();
			and.add(new EQ(ServiceProductXInfo.PRODUCT_ID,serviceId));
			and.add(new EQ(ServiceProductXInfo.PRODUCT_VERSION_ID,versionId));
			return (ServiceProduct) serviceProductHome.find(ctx, and);
		}else
			return null;
	}

	public static PricingVersion getAssociatedPricingVersion(Context ctx, long compatibilityGroup) throws HomeInternalException, HomeException{
		Home home = (Home) ctx.get(PricingVersionXDBHome.class);
		And and = new And();
		and.add(new EQ(PricingVersionXInfo.COMPATIBILITY_GROUP,compatibilityGroup));
		return (PricingVersion) home.find(ctx, and);
	}

	public static AdjustmentType getAssociatedAdjustmentType(Context ctx, long adjId,long adjVersionId) throws HomeInternalException, HomeException{
		Home home = (Home) ctx.get(AdjustmentTypeHome.class);
		//ctx.get(AdjustmentTypeNVersionXDBHome.class);
		And and = new And();
		and.add(new EQ(AdjustmentTypeXInfo.CODE,adjId));
		return (AdjustmentType) home.find(ctx, and);
	}

	public static AdjustmentTypeNVersion getAssociatedAdjustmentTypeNVersion(Context ctx, long adjustmentTypeId,long adjVersionId) throws HomeInternalException, HomeException{
		Home home = (Home) ctx.get(AdjustmentTypeNVersionXDBHome.class);
		And and = new And();
		and.add(new EQ(AdjustmentTypeNVersionXInfo.CODE,adjustmentTypeId));
		and.add(new EQ(AdjustmentTypeNVersionXInfo.VERSION_ID,adjVersionId));
		return (AdjustmentTypeNVersion) home.find(ctx, and);
	}
	/* 
	 * In case of create, we migh end up updating extisting service in TransientHome  
	 *  as we might be creating a new higher version of the service as the TransientHome
	 *  has Service created from older version. 
	 */
	public static void createServiceFromServiceProduct(Context ctx, Object ret) throws HomeException{
		ServiceProduct serviceProduct = (ServiceProduct)ret;
		Home serviceHome = (Home) ctx.get(ServiceHome.class);
		createOrUpdateService(ctx, serviceProduct, serviceHome);
	}

	/* 
	 *  In case of update, We might end up creating a new Service in the TransientHome
	 *  as we might be updating a compatibility group on the service which is associated to certain price version. 
	 */
	public static void updateServiceFromServiceProduct(Context ctx, Object ret) throws HomeInternalException, HomeException{
		ServiceProduct version = (ServiceProduct)ret;
		Double maxVersionID = (Double)(HomeSupportHelper.get(ctx).max(ctx, ServiceProductXInfo.PRODUCT_VERSION_ID, new EQ(ServiceProductXInfo.PRODUCT_ID,version.getProductId())));
		if(maxVersionID!=null && maxVersionID.intValue() == version.getProductVersionID()){
			Home serviceHome = (Home) ctx.get(ServiceHome.class);
			createOrUpdateService(ctx, version, serviceHome);
		}
	}

	public static void updateServiceOnServiceProductRemove(Context ctx, Object ret) throws HomeInternalException, HomeException{
		ServiceProduct version = (ServiceProduct)ret;
		Home serviceHome = (Home) ctx.get(ServiceHome.class);
		// revert to last previous version if any 
		Double maxVersionID = (Double)(HomeSupportHelper.get(ctx).max(ctx, ServiceProductXInfo.PRODUCT_VERSION_ID, new EQ(ServiceProductXInfo.PRODUCT_ID,version.getProductId())));
		if(maxVersionID!=null){
			if (serviceHome != null) {
				Object service = new ProductToServiceAdapter()	.adaptFromServiceID(ctx, version.getProductId());
				if(service!=null) // if older version exist then update with that version.
					serviceHome.store(ctx, service);
				else if(serviceHome.find(version.getProductId())!=null && service ==null) // if it was the last 
				{
					Service removedService = new Service();
					removedService.setID(version.getProductId());
					serviceHome.remove(removedService);
				}
			}
		}else{
			Service service = new Service();
			service.setID(version.getProductId());
			serviceHome.remove(service);
		}
	}

	private static void createOrUpdateService(Context ctx, Object obj, Home serviceHome) throws HomeException, HomeInternalException {

		if(obj instanceof ServiceProduct){
			ServiceProduct serviceProduct = (ServiceProduct)obj;
			if(serviceHome != null){
				Object service = new ProductToServiceAdapter().adaptFromServiceID(ctx, serviceProduct.getProductId());
				// Store will be called with newer version is created and older version data is already present in Transient Home.
				if(service!=null){
					if (serviceHome.find(serviceProduct.getProductId()) != null){
						serviceHome.store(ctx, service);
					} else {
						serviceHome.create(ctx, getProductFromServiceId(ctx, serviceProduct.getProductId()));
					}
				}
			}
		}else if(obj instanceof PackageProduct){
			PackageProduct packageProduct = (PackageProduct)obj;
			if(serviceHome != null){
				Object service = new ProductToServiceAdapter().adaptFromServiceID(ctx, packageProduct.getProductId());
				// Store will be called with newer version is created and older version data is already present in Transient Home.
				if(service!=null){
					if (serviceHome.find(packageProduct.getProductId()) != null){
						serviceHome.store(ctx, service);
					} else {
						serviceHome.create(ctx, getProductFromServiceId(ctx, packageProduct.getProductId()));
					}
				}
			}
		}

	}

	public static long getCompatibilityGroupOnPricingVersion(Context ctx, long versionId) throws HomeInternalException, HomeException {
		Home home = (Home) ctx.get(PricingVersionXDBHome.class);
		And and = new And();
		and.add(new EQ(PricingVersionXInfo.VERSION_ID,versionId));
		PricingVersion version =  (PricingVersion) home.find(ctx, and);
		return version.getCompatibilityGroup();
	}

	public static void createExternalServiceTypeExtensionFromProduct(Context ctx,ExternalServiceTypeExtension 	externalServiceExtension ) throws HomeInternalException, HomeException{
		Home extension = (Home) ctx.get(ExternalServiceTypeExtensionHome.class);

		if(extension.find(ctx,new EQ(ExternalServiceTypeExtensionXInfo.SERVICE_ID,externalServiceExtension.getServiceId())) == null)
			extension.create(ctx, externalServiceExtension);
		else
			updateExternalServiceTypeExtensionFromProduct(ctx, externalServiceExtension);
	}
	public static void updateExternalServiceTypeExtensionFromProduct(Context ctx,ExternalServiceTypeExtension 	externalServiceExtension ) throws HomeInternalException, HomeException{
		Home extension = (Home) ctx.get(ExternalServiceTypeExtensionHome.class);

		if(extension.find(ctx,new EQ(ExternalServiceTypeExtensionXInfo.SERVICE_ID,externalServiceExtension.getServiceId())) != null)
			extension.store(ctx, externalServiceExtension);
		else
			createExternalServiceTypeExtensionFromProduct(ctx, externalServiceExtension);
	}
	public static void removeExternalServiceTypeExtensionFromProduct(Context ctx,ExternalServiceTypeExtension 	externalServiceExtension ) throws HomeInternalException, HomeException{
		Home extension = (Home) ctx.get(ExternalServiceTypeExtensionHome.class);
		extension.remove(ctx, externalServiceExtension);
	}

	public static void createBlacklistWhitelistTemplateServiceExtensionFromProduct(Context ctx,BlacklistWhitelistTemplateServiceExtension externalServiceExtension ) throws HomeInternalException, HomeException{
		Home extension = (Home) ctx.get(BlacklistWhitelistTemplateServiceExtensionHome.class);
		if(extension.find(ctx,new EQ(BlacklistWhitelistTemplateServiceExtensionXInfo.SERVICE_ID,externalServiceExtension.getServiceId())) == null)
			extension.create(ctx, externalServiceExtension);
		else
			updateBlacklistWhitelistTemplateServiceExtensionFromProduct(ctx, externalServiceExtension);
	}
	public static void updateBlacklistWhitelistTemplateServiceExtensionFromProduct(Context ctx,BlacklistWhitelistTemplateServiceExtension externalServiceExtension ) throws HomeInternalException, HomeException{
		Home extension = (Home) ctx.get(BlacklistWhitelistTemplateServiceExtensionHome.class);
		if(extension.find(ctx,new EQ(BlacklistWhitelistTemplateServiceExtensionXInfo.SERVICE_ID,externalServiceExtension.getServiceId())) != null)
			extension.store(ctx, externalServiceExtension);
		else
			createBlacklistWhitelistTemplateServiceExtensionFromProduct(ctx, externalServiceExtension);
	}
	public static void removeBlacklistWhitelistTemplateServiceExtensionFromProduct(Context ctx,BlacklistWhitelistTemplateServiceExtension 	externalServiceExtension ) throws HomeInternalException, HomeException{
		Home extension = (Home) ctx.get(BlacklistWhitelistTemplateServiceExtensionHome.class);
		extension.remove(ctx, externalServiceExtension);
	}

	public static void createServiceFromPackageProduct(Context ctx, PackageProduct packageProduct) throws HomeException {
		Home serviceHome = (Home) ctx.get(ServiceHome.class);
		createOrUpdateService(ctx, packageProduct, serviceHome);

	}

	public static void updateServiceFromPackageProduct(Context ctx, PackageProduct packageProduct) throws HomeException {
		Double maxVersionID = (Double)(HomeSupportHelper.get(ctx).max(ctx, PackageProductXInfo.PRODUCT_VERSION_ID, new EQ(PackageProductXInfo.PRODUCT_ID, packageProduct.getProductId())));
		if(maxVersionID!=null && maxVersionID.intValue() == packageProduct.getProductVersionID()){
			Home serviceHome = (Home) ctx.get(ServiceHome.class);
			createOrUpdateService(ctx, packageProduct, serviceHome);
		}

	}

	public static void updateServiceOnPackageProductRemove(Context ctx, PackageProduct packageProduct) throws HomeException {
		
		Home serviceHome = (Home) ctx.get(ServiceHome.class);
		// revert to last previous version if any 
		Double maxVersionID = (Double)(HomeSupportHelper.get(ctx).max(ctx, PackageProductXInfo.PRODUCT_VERSION_ID, new EQ(PackageProductXInfo.PRODUCT_ID, packageProduct.getProductId())));
		if(maxVersionID!=null){
			if (serviceHome != null) {
				Object service = new ProductToServiceAdapter().adaptFromServiceID(ctx, packageProduct.getProductId());
				if(service!=null) // if older version exist then update with that version.
					serviceHome.store(ctx, service);
				else if(serviceHome.find(packageProduct.getProductId()) != null && service == null) // if it was the last 
				{
					Service removedService = new Service();
					removedService.setID(packageProduct.getProductId());
					serviceHome.remove(removedService);
				}
			}
		}else{
			Service service = new Service();
			service.setID(packageProduct.getProductId());
			serviceHome.remove(service);
		}
	}
}
