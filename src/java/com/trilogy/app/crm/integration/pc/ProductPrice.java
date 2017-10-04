/**
 * 
 */
package com.trilogy.app.crm.integration.pc;

import java.util.List;

/**
 * @author abhay.parashar
 *
 */
public class ProductPrice implements Price{
	
	private int pricePlanId;
	private long productId;
	private long pricePlanVersionId;
	private long productVariantId;
	private long ServiceVersionId;

	List<Price> prices;
	/**
	 * 
	 */
	public ProductPrice() {
		// TODO Auto-generated constructor stub
	}
	public int getPricePlanId() {
		return pricePlanId;
	}
	public void setPricePlanId(int pricePlanId) {
		this.pricePlanId = pricePlanId;
	}
	public long getProductId() {
		return productId;
	}
	public void setProductId(long productId) {
		this.productId = productId;
	}
	public List<Price> getPrices() {
		return prices;
	}
	public void setPrices(List<Price> prices) {
		this.prices = prices;
	}
	public long getPricePlanVersionId() {
		return pricePlanVersionId;
	}
	public void setPricePlanVersionId(long pricePlanVersionId) {
		this.pricePlanVersionId = pricePlanVersionId;
	}
	public long getProductVariantId() {
		return productVariantId;
	}
	public void setProductVariantId(long productVariantId) {
		this.productVariantId = productVariantId;
	}
	public long getServiceVersionId(){
        return ServiceVersionId;
	}
	public void setServiceVersionId(long ServiceVersionId){
	        this.ServiceVersionId = ServiceVersionId; 
	}
	@Override
	public String toString() {
		return "ProductPrice [pricePlanId=" + pricePlanId + ", productId="
				+ productId + ", pricePlanVersionId=" + pricePlanVersionId
				+ ", productVariantId=" + productVariantId
				+ ", ServiceVersionId=" + ServiceVersionId + ", prices="
				+ prices + "]";
	}

	
}

