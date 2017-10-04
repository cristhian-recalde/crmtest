/**
 * 
 */
package com.trilogy.app.crm.integration.pc;

import java.util.Calendar;

/**
 * @author abhay.parashar
 *
 */
public class PublishedVersionModel {

	private long versionId;
	private long id;
	private Calendar activationDate;
	private Calendar createDate;       
	private String description;
	private Product products;
	private Price productPrice;
	/**
	 * 
	 */
	public PublishedVersionModel() {
	}
	public long getVersionId() {
		return versionId;
	}
	public void setVersionId(long versionId) {
		this.versionId = versionId;
	}
	public Calendar getActivationDate() {
		return activationDate;
	}
	public void setActivationDate(Calendar activationDate) {
		this.activationDate = activationDate;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Calendar getCreateDate() {
		return createDate;
	}
	public void setCreateDate(Calendar createDate) {
		this.createDate = createDate;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public Product getProducts() {
		return products;
	}
	public void setProducts(Product products) {
		this.products = products;
	}
	public Price getProductPrice() {
		return productPrice;
	}
	public void setProductPrice(Price productPrice) {
		this.productPrice = productPrice;
	}
	@Override
	public String toString() {
		return "PublishedVersionModel [versionId=" + versionId + ", id=" + id
				+ ", activationDate=" + activationDate + ", createDate="
				+ createDate + ", description=" + description + ", products="
				+ products + ", productPrice=" + productPrice + "]";
	}
}
