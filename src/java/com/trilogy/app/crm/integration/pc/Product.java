/**
 * 
 */
package com.trilogy.app.crm.integration.pc;

/**
 * @author abhay.parashar
 * Market Interface for deciding type of Product we are dealing with.
 */
public interface Product {
	
	public Product create(Object product);
	
	public Object getProducts();
	
	public boolean isMandtory();

	public void setMandtory(boolean isMandtory);
}