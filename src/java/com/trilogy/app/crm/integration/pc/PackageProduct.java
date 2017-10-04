/**
 * 
 */
package com.trilogy.app.crm.integration.pc;

import java.util.ArrayList;
import java.util.List;

import com.trilogy.app.crm.bean.ProductTypeEnum;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextLocator;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.util.crmapi.wsdl.v3_0.api.pc.io.product.v1.ProductComponentIO;
import com.trilogy.util.crmapi.wsdl.v3_0.api.pc.io.product.v1.ProductIO;
import com.trilogy.util.crmapi.wsdl.v3_0.api.pc.io.product.v1.ProductVersionIO;

/**
 * @author abhay.parashar
 * Composite Product contains more than one service
 */
public class PackageProduct implements Product {
	
	//Primary key
	private String productId;
	private String description;
	private String serviceName;
	private boolean isMandtory;

	// List of Products associated with this Package Product.
	private List<Product> products;
	
	// List of Prices associated with this Package Product
	private List<PriceModel> price_;
	
	private ServiceModel packageProduct;
	
	
	
	public PackageProduct() {
		products = new ArrayList<Product>();
		packageProduct = new ServiceModel();
	}

	
	public String getProductId() {
		return productId;
	}


	public void setProductId(String productId) {
		this.productId = productId;
	}


	public String getDescription() {
		return description;
	}


	public void setDescription(String description) {
		this.description = description;
	}


	public String getServiceName() {
		return serviceName;
	}


	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}


	public List<PriceModel> getPrice_() {
		return price_;
	}


	public void setPrice_(List<PriceModel> price_) {
		this.price_ = price_;
	}


	public List<Product> getProducts() {
		return products;
	}


	public void setProducts(List<Product> products) {
		this.products = products;
	}

	

	public boolean isMandtory() {
		return isMandtory;
	}


	public void setMandtory(boolean isMandtory) {
		this.isMandtory = isMandtory;
	}
	
	public ServiceModel getPackageProduct() {
		return packageProduct;
	}

	public void setPackageProduct(ServiceModel serviceProduct) {
		this.packageProduct = serviceProduct;
	}


	@Override
	public Product create(Object product) {
		
		ProductIO io = (ProductIO)product;
		
		LogSupport.debug(ContextLocator.locate(), this, "[PackageProduct.create] Creating package product with Id:"+io.getBusinessKey());
		
		ProductVersionIO[] productVersions = io.getVersions().getVersion();
		
		for(ProductVersionIO productVersion : productVersions){
			
			PackageProduct packageProduct = new PackageProduct();
			packageProduct.setDescription(io.getDescription());
			packageProduct.setProductId(io.getBusinessKey());
			packageProduct.setServiceName(io.getName());
			
			this.packageProduct.setServiceId(io.getBusinessKey());
			this.packageProduct.setProductType(ProductTypeEnum.PACKAGE);
			
			products.add(packageProduct);
			
			ProductComponentIO[] components = productVersion.getComponents().getComponent();
			
			for(ProductComponentIO component:components){
				ProductIO productComponent = component.getProduct();
				Product prod =PublishOfferSupport.getProductFactory(productComponent.getType().getValue()).create(productComponent);
				prod.setMandtory(Boolean.parseBoolean(component.getMandatory().getValue()));
				packageProduct.getProducts().add(prod);
			}
		}
		
		return this;
	}


	@Override
	public String toString() {
		return "PackageProduct [productId=" + productId + ", description="
				+ description + ", serviceName=" + serviceName
				+ ", isMandtory=" + isMandtory + ", products=" + products
				+ ", price_=" + price_ + "]";
	}
	
	

}
