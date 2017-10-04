package com.trilogy.app.crm.integration.pc;

import java.util.Collection;
import java.util.Iterator;

import com.trilogy.app.crm.bean.CatalogEntityEnum;
import com.trilogy.app.crm.bean.CatalogEntityHistory;
import com.trilogy.app.crm.bean.CatalogEntityHistoryHome;
import com.trilogy.app.crm.bean.CatalogEntityHistoryXInfo;
import com.trilogy.app.crm.bean.PricePlanVersion;
import com.trilogy.app.crm.bean.PricePlanVersionHome;
import com.trilogy.app.crm.bean.PricePlanVersionID;
import com.trilogy.app.crm.bean.PricePlanVersionIdentitySupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAware;
import com.trilogy.framework.xhome.context.ContextLocator;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.PMLogMsg;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CsService;
import com.trilogy.util.crmapi.wsdl.v3_0.api.OfferingServiceSkeletonInterface;
import com.trilogy.util.crmapi.wsdl.v3_0.api.ProductCatalogInterfaceException;
import com.trilogy.util.crmapi.wsdl.v3_0.api.pc.io.offering.v1.OfferingIO;
import com.trilogy.util.crmapi.wsdl.v3_0.api.pc.io.offering.v1.OfferingVersionIO;
import com.trilogy.util.crmapi.wsdl.v3_0.api.pc.io.price.v1.PriceIO;
import com.trilogy.pcom.cs.webservices.messages.v1.SetOfferingConsumptionStatusResponse;
import com.trilogy.util.crmapi.wsdl.v3_0.api.core.io.common.v1.ExecutionStatus;
import com.trilogy.util.crmapi.wsdl.v3_0.api.core.io.catalog.v1.SourceSystems;

public class OfferingServiceImpl implements OfferingServiceSkeletonInterface,
ContextAware {

	
	private static final String MESSAGEID_KEY = "MESSAGEID";

	private static final String CLIENT_KEY = "ESB-CatalogServiceClient-v3_0";
	
	private static final String DEFAULT_BUSINESS_KEY = "0";

	PublishOfferPricePlanAdapter publishOfferPricePlanAdapter = null;

	public OfferingServiceImpl(Context context) {
		super();
		this.context_ = context;
		publishOfferPricePlanAdapter = new PublishOfferPricePlanAdapter(context);
	}

	@Override
	public Context getContext() {
		return this.context_;
	}

	@Override
	public void setContext(Context ctx) {
		this.context_ = ctx;
	}

	@Override
	public void disableOffering(String arg0, String arg1)
	throws ProductCatalogInterfaceException {
		LogSupport.debug(getContext(), MODULE,
		"OfferingServiceImpl-disableOffering");
	}

	@Override
	public void enableOffering(String businessKey, String messageID)
	throws ProductCatalogInterfaceException {
		LogSupport.debug(getContext(), MODULE,
		"OfferingServiceImpl-enableOffering -Start");

		Home catalogEntityHistoryHome = (Home) this.context_
		.get(CatalogEntityHistoryHome.class);

		And filter = new And();
		filter.add(new EQ(CatalogEntityHistoryXInfo.EVENT_ID, messageID));
		filter.add(new EQ(CatalogEntityHistoryXInfo.ENTITY_TYPE,
				CatalogEntityEnum.PricePlanVersion));
		try {
			// Getting the all price plan version Entity from
			// CatalogEntityHistory based on messageId
			Home pricePlanVersionHome = (Home) context_
			.get(PricePlanVersionHome.class);
			Collection<CatalogEntityHistory> entityHistories = catalogEntityHistoryHome.select(context_, filter);

			if (null != entityHistories && !entityHistories.isEmpty()) {
				Iterator<CatalogEntityHistory> iterator = entityHistories
				.iterator();
				// Traverse the all CatalogEntityHistory
				while (iterator.hasNext()) {
					CatalogEntityHistory entityHistory = iterator.next();
					// found the Entity Key which contains ID and Version of the
					// PricePlanVersion as combination of id_version

					String entityKey = entityHistory.getEntityKey();
					if (null != entityKey) {
						PricePlanVersionID planVersionID = (PricePlanVersionID) PricePlanVersionIdentitySupport.instance().fromStringID(entityKey);

						PricePlanVersion pricePlanVersion = (PricePlanVersion) pricePlanVersionHome
						.find(context_, planVersionID);
						if (null != pricePlanVersion) {
							LogSupport.info(context_, this, "This PricePlanVersion will be enabled  : " + pricePlanVersion.getId());
							pricePlanVersion.setEnabled(true);
							// Update the correct version with enable status.
							pricePlanVersionHome.store(context_, pricePlanVersion);
						}

					}
				}
			}
			else
			{
				LogSupport.minor(context_, this, "Message ID either null or invalid! ");
				throw new HomeException("Message ID either null or invalid!");
			}
		} catch (HomeInternalException e) {
			LogSupport.major(context_, this, "fail  enableOffering : " + e);
			throw new ProductCatalogInterfaceException(e);
		} catch (UnsupportedOperationException e) {
			LogSupport.major(context_, this, "Fail enableOffering " + e);
			throw new ProductCatalogInterfaceException(e);
		} catch (HomeException e) {
			LogSupport.major(context_, this, "fail enableOffering : " + e);
			throw new ProductCatalogInterfaceException(e);
		}
		LogSupport.debug(getContext(), MODULE, "OfferingServiceImpl-enableOffering-End");
	}

	@Override
	public void prepareOffering(String messageId, OfferingIO offering) {
		LogSupport.debug(getContext(), MODULE, "OfferingServiceImpl-prepareOffering");
		Context subCtx = getContext().createSubContext();
		PublishOfferPricePlanModel modelOfferPricePlanModel = null;;
		boolean status = false;
		String msg = "";
		
			final PMLogMsg pmLog = new PMLogMsg("AppCrm", "Prepare Offer in BSS");
			//modelOfferPricePlanModel = publishOfferPricePlanAdapter.adapt(subCtx, offering);
			//LogSupport.info(getContext(), this, "Model Offer Price Plan Model: ["+ modelOfferPricePlanModel.toString() + "]");
			subCtx.put(MESSAGEID_KEY, messageId);
			PricePlanEntityCreator creator = new PricePlanEntityCreator(subCtx);
		
		try {
			//creator.create(subCtx, modelOfferPricePlanModel);
			creator.create(subCtx, offering);
			status = true;
		}catch (HomeInternalException hie) {
			status = false;
			msg = hie.getMessage();
			LogSupport.info(getContext(), this, "Key: [" + messageId + "]", hie);
		} catch (HomeException he) {
			status = false;
			msg = he.getMessage();
			LogSupport.info(getContext(), this, "Key: [" + messageId + "]", he);
		} catch (Exception e) {
			status = false;
			msg = e.getMessage();
			LogSupport.info(getContext(), this, "Key: [" + messageId + "]", e);
		}
		
		// populating PRICE, ONETIMEPRICE, RECURRINGPRICE and enriching SERVICE
		// and SERVICEFEE(saved as a clob inside PricePlanVersion)
		if (status && offering != null) {
			PriceEntityCreator priceEntityCreator = new PriceEntityCreator(subCtx,
					Integer.valueOf(offering.getSpid()).intValue());

			if (offering.getEntityBaseIOChoice_type1() != null
					&& offering.getEntityBaseIOChoice_type1().getVersions() != null) {

				OfferingVersionIO[] offeringVersionIOs = offering.getEntityBaseIOChoice_type1().getVersions()
						.getVersion();
				if (offeringVersionIOs != null && offeringVersionIOs.length > 0) {
					// getting the first version
					OfferingVersionIO firstOfferVersion = offeringVersionIOs[0];

					if (firstOfferVersion != null) {
						PriceIO priceIO = firstOfferVersion.getPrice();

						try {
							// creating price entities
							priceEntityCreator.createPriceEntries(priceIO);
							status = true;
						} catch (HomeException e) {
							status = false;
							msg = e.getMessage();
							LogSupport.info(getContext(), this, "Key: [" + messageId + "]", e);
						}
					}
				}
			}
		}

		
		/*Below code is commented as for UDS, integration layer is skipped and no need to invoke setOfferingConsumptionStatus API
		of ESB*/

	/*	try {

			CsService client = (CsService) getContext().get(CLIENT_KEY);
			if(client != null){
				SetOfferingConsumptionStatusResponse res = null;
					if(status){
						res = client
						.setOfferingConsumptionStatus(String.valueOf(offering.getBusinessKey()), SourceSystems.BSS,ExecutionStatus.SUCCESS, messageId, null, null);
						com.redknee.app.crm.log.ERLogger.genrateERprepareOffering(getContext(),ExecutionStatus.SUCCESS,messageId, offering);
					}else {
						res = client
						.setOfferingConsumptionStatus(String.valueOf(offering.getBusinessKey()), SourceSystems.BSS,ExecutionStatus.FAILED, messageId, msg, null);
						com.redknee.app.crm.log.ERLogger.genrateERprepareOffering(getContext(),ExecutionStatus.FAILED,messageId, offering);
					}
				if(res != null){
					LogSupport.info(getContext(), this, "Response:"+ res.getStatus().toString());
				}
				else{
					LogSupport.info(getContext(), this, "Response is null");
				}

			}else{
				LogSupport.info(getContext(), this, "The CS Service [" + CLIENT_KEY + "] is null");
			}

		} catch (Exception e) {
			LogSupport.info(getContext(), this, "Error:Key" + messageId, e);
		}finally{
			pmLog.log(getContext()); 
		}*/
	}

	private Context context_ = ContextLocator.locate();
	public static String MODULE = OfferingServiceImpl.class.getName();
}
