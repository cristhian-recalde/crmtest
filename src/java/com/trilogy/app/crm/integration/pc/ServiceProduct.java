/**
 * 
 */
package com.trilogy.app.crm.integration.pc;

import java.util.HashMap;
import java.util.Map;

import com.trilogy.app.crm.bean.ProductTypeEnum;
import com.trilogy.framework.xhome.context.ContextLocator;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.util.crmapi.wsdl.v3_0.api.core.io.common.v1.CharIO;
import com.trilogy.util.crmapi.wsdl.v3_0.api.core.io.common.v1.NetworkTechnologyIO;
import com.trilogy.util.crmapi.wsdl.v3_0.api.pc.io.product.v1.ProductIO;
import com.trilogy.util.crmapi.wsdl.v3_0.api.pc.io.product.v1.ProductVersionIO;
import com.trilogy.util.crmapi.wsdl.v3_0.api.pc.io.service.v1.CustFacingServSpecVersionIO;
import com.trilogy.util.crmapi.wsdl.v3_0.api.pc.io.technicalservice.v1.RfssIO;
import com.trilogy.util.crmapi.wsdl.v3_0.api.pc.io.technicalservice.v1.RfssVersionIO;

/**
 * @author abhay.parashar
 * Currently Only ServiceProduct is supported as atomic product.
 * In future, DeviceProduct might also be supported.
 */
public class ServiceProduct implements Product{

	private ServiceModel serviceProduct;
	private String productBusinessKey; // Needed for mapping Price
	private boolean isMandtory;
	private static String BILLING_TYPE_SOURCE = "UNIFIEDBILLING";
	/**
	 * 
	 */
	public ServiceProduct() {
		serviceProduct = new ServiceModel();
	}

	@Override
	public Product create(Object product) {
		
		ProductIO io = (ProductIO)product;
		this.productBusinessKey = io.getBusinessKey();	
		
		LogSupport.debug(ContextLocator.locate(), this, "[ServiceProduct.create] creating service product with Id: " + this.productBusinessKey);
		
		this.serviceProduct.setServiceId(io.getBusinessKey());
		this.serviceProduct.setProductType(ProductTypeEnum.SERVICE);
		
		String description = io.getDescription();
		String serviceName = io.getName();
		RfssIO technicalTemplate = getTechnicalServiceTemplateId(io);
		String techServiceTempId = technicalTemplate.getBusinessKey();
		
		
		NetworkTechnology networkTechnology = getNetworkTechology(io);
		
		ProductVersionIO[] serviceVersions = io.getVersions().getVersion();
		for(ProductVersionIO productVersion : serviceVersions)
		{
			ServiceVersionModel serviceVersionModel = new ServiceVersionModel();
			
			this.serviceProduct.getServiceVersion().add(serviceVersionModel);
			
			serviceVersionModel.setServiceName(serviceName);
			
			serviceVersionModel.setDescription(description);
			
			serviceVersionModel.setEffectiveFromDate(productVersion.getEffectiveFromDate());
			
			serviceVersionModel.setTechnicalServiceTemplateId(techServiceTempId);
			
			serviceVersionModel.setCharacteristics(getCharacteristics(technicalTemplate));
			
			serviceVersionModel.setServiceId(this.productBusinessKey);
			
			serviceVersionModel.setNetworkTechnology_(networkTechnology);
		}
		return this;
	}
	
	private Map<String, Object> getCharacteristics(RfssIO technicalTemplate) {

		RfssVersionIO[] charVersions = technicalTemplate.getVersions().getVersion();
		CharIO[] characteristics = charVersions[0].getCharacteristics().getCharacteristic();
		Map<String, Object> characteristicsMap = new HashMap<String, Object>();
		for(CharIO characteristic : characteristics){
			characteristicsMap.put(characteristic.getCharSpec().getName(), characteristic);
		}
		return characteristicsMap;
	}

	private NetworkTechnology getNetworkTechology(ProductIO io) {
		CustFacingServSpecVersionIO[] version = io.getService().getVersions().getVersion();
		RfssIO[] technicalTemplates = version[0].getTechnicalServices().getTechnicalService();
		for(RfssIO technicalTemplate:technicalTemplates){
			if(technicalTemplate.getSource().equalsIgnoreCase(BILLING_TYPE_SOURCE)){
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

	public ServiceModel getServiceProduct() {
		return serviceProduct;
	}

	public void setServiceProduct(ServiceModel serviceProduct) {
		this.serviceProduct = serviceProduct;
	}

	private RfssIO getTechnicalServiceTemplateId(ProductIO io) {
		CustFacingServSpecVersionIO[] version = io.getService().getVersions().getVersion();
		RfssIO[] technicalTemplates = version[0].getTechnicalServices().getTechnicalService();
		for(RfssIO technicalTemplate:technicalTemplates){
			if(technicalTemplate.getSource().equalsIgnoreCase(BILLING_TYPE_SOURCE)){
				return technicalTemplate;
			}
		}
		return null;
	}


	@Override
	public Product getProducts() {
		return this;
	}

	public boolean isMandtory() {
		return isMandtory;
	}

	public void setMandtory(boolean isMandtory) {
		this.isMandtory = isMandtory;
	}

	@Override
	public String toString() {
		return "ServiceProduct [serviceProduct=" + serviceProduct
				+ ", productBusinessKey=" + productBusinessKey
				+ ", isMandtory=" + isMandtory + "]";
	}
	

}
