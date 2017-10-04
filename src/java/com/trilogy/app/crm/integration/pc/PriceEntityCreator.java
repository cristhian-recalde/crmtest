package com.trilogy.app.crm.integration.pc;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.trilogy.app.crm.bean.PricePlanVersion;
import com.trilogy.app.crm.bean.PricePlanVersionHome;
import com.trilogy.app.crm.bean.PricePlanVersionXInfo;
import com.trilogy.app.crm.bean.ProductPrice;
import com.trilogy.app.crm.bean.ProductPriceHome;
import com.trilogy.app.crm.bean.ProductPriceXInfo;
import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.ServiceFee2;
import com.trilogy.app.crm.bean.ServiceFee2ID;
import com.trilogy.app.crm.bean.ServiceHome;
import com.trilogy.app.crm.bean.ServicePackageVersion;
import com.trilogy.app.crm.bean.ServicePeriodEnum;
import com.trilogy.app.crm.bean.ServiceXInfo;
import com.trilogy.app.crm.bean.price.ChargeTypeEnum;
import com.trilogy.app.crm.bean.price.OneTimePrice;
import com.trilogy.app.crm.bean.price.OneTimePriceHome;
import com.trilogy.app.crm.bean.price.Price;
import com.trilogy.app.crm.bean.price.PriceHome;
import com.trilogy.app.crm.bean.price.RecurringPrice;
import com.trilogy.app.crm.bean.price.RecurringPriceHome;
import com.trilogy.app.crm.bean.ui.PriceTemplate;
import com.trilogy.app.crm.bean.ui.PriceTemplateHome;
import com.trilogy.app.crm.bean.ui.PriceTemplateXInfo;
import com.trilogy.app.crm.util.MathSupportUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAware;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.msp.SpidAware;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.util.crmapi.wsdl.v3_0.api.pc.io.price.v1.CompositePriceIO;
import com.trilogy.util.crmapi.wsdl.v3_0.api.pc.io.price.v1.CompositePrices_type1;
import com.trilogy.util.crmapi.wsdl.v3_0.api.pc.io.price.v1.OneTimeCharge;
import com.trilogy.util.crmapi.wsdl.v3_0.api.pc.io.price.v1.PriceIO;
import com.trilogy.util.crmapi.wsdl.v3_0.api.pc.io.price.v1.PriceTemplateIO;
import com.trilogy.util.crmapi.wsdl.v3_0.api.pc.io.price.v1.PriceVersionIO;
import com.trilogy.util.crmapi.wsdl.v3_0.api.pc.io.price.v1.RecurringCharge;

public class PriceEntityCreator implements ContextAware, SpidAware {

	private static final String CLASS_NAME = PriceEntityCreator.class.getName();

	public static final String PRODUCT_ID_PRODUCT_PRICE_MAP = "product_id_product_price_map";

	private Context _ctx;
	private int _spid;
	
	private List<OneTimePrice> oneTimePrices = new ArrayList<OneTimePrice>();
	private List<RecurringPrice> recurringPrices = new ArrayList<RecurringPrice>();

	/**
	 * Constructor
	 * 
	 * @param ctx
	 * @param spid
	 */
	public PriceEntityCreator(Context ctx, int spid) {
		this._ctx = ctx;
		this._spid = spid;
	}

	/**
	 * Creates entries for the following entities:
	 * <ol>
	 * <li>PRICE</li>
	 * <li>ONETIMEPRICE</li>
	 * <li>RECURRINGPRICE</li>
	 * </ol>
	 * Also enriches the {@link Service}s related to the price
	 * 
	 * @param priceIO
	 *            the {@link PriceIO} received in the request
	 * @throws HomeInternalException
	 * @throws HomeException
	 */
	public void createPriceEntries(PriceIO priceIO) throws HomeInternalException, HomeException {
		LogSupport.info(getContext(), CLASS_NAME, "Start of createPriceEntries");

		if (priceIO == null) {
			LogSupport.crit(getContext(), CLASS_NAME, "Input PriceIO is null");
			throw new IllegalArgumentException("Input PriceIO is null");
		}
		// get the price plan versions from priceIO 
		PriceVersionIO[] priceVersions = priceIO.getEntityBaseIOChoice_type0().getVersions().getVersion();
		if (priceVersions != null && priceVersions.length > 0) {
			LogSupport.debug(getContext(), CLASS_NAME, "priceVersions has data::array size = " + priceVersions.length);
			for (PriceVersionIO priceVersionIO : priceVersions) {
				// getting root level composite price
				CompositePriceIO compositePriceIO = priceVersionIO.getVersionIOChoice_type0().getCompositePrice();
				handleCompositePrice(compositePriceIO);
			}
		} else {
			LogSupport.debug(getContext(), CLASS_NAME, "priceVersions is null or empty");
		}

		//populating ONETIMEPRICE
		if (oneTimePrices.size() > 0) {
			Home oneTimePriceHome = (Home) getContext().get(OneTimePriceHome.class);
			if (oneTimePriceHome != null) {
				for (OneTimePrice oneTimePrice : oneTimePrices) {
					if (oneTimePriceHome.find(oneTimePrice.getOneTimePriceId()) != null) {
						LogSupport.debug(getContext(), CLASS_NAME,
								"OneTimePrice(ID=" + oneTimePrice.getOneTimePriceId() + ") already exists.");

						// Updation of OneTimePrice not allowed - commenting code for now
						//oneTimePriceHome.store(getContext(), oneTimePrice);
						//LogSupport.debug(getContext(), CLASS_NAME, "OneTimePrice(ID=" + oneTimePrice.getOneTimePriceId() + ") updated successfully.");

					} else {
						OneTimePrice oneTimePriceCreated = (OneTimePrice) oneTimePriceHome.create(getContext(), oneTimePrice);
						
						Price price = createPrice(ChargeTypeEnum.ONE_TIME, oneTimePriceCreated.getOneTimePriceId(),
								oneTimePriceCreated.getServiceId());
						
						updateServiceForEnriching(oneTimePriceCreated.getServiceId(), oneTimePrice, getPriceTemplate(""+oneTimePriceCreated.getPriceTemplateID()));
						
						updatePricePlanVersionForEnrichingServiceFee(price, oneTimePrice);

						LogSupport.debug(getContext(), CLASS_NAME,
								"OneTimePrice(OneTimePriceID=" + oneTimePrice.getOneTimePriceId() + ") created successfully.");
					}
				}
			}
		}

		//populating RECURRINGPRICE
		if (recurringPrices.size() > 0) {
			Home recurringPriceHome = (Home) getContext().get(RecurringPriceHome.class);
			if (recurringPriceHome != null) {
				for (RecurringPrice recurringPrice : recurringPrices) {
					if (recurringPriceHome.find(recurringPrice.getRecurringPriceId()) != null) {
						LogSupport.debug(getContext(), CLASS_NAME,
								"RecurringPrice(ID=" + recurringPrice.getRecurringPriceId() + ") already exists.");

						// Updation of RecurringPrice not allowed - commenting code for now
						//recurringPriceHome.store(getContext(), recurringPrice);
						//LogSupport.debug(getContext(), CLASS_NAME, "RecurringPrice(ID=" + recurringPrice.getRecurringPriceId() + ") updated successfully.");

					} else {
						RecurringPrice recurringPriceCreated = (RecurringPrice) recurringPriceHome.create(getContext(), recurringPrice);
						
						Price price = createPrice(ChargeTypeEnum.RECCURING, recurringPriceCreated.getRecurringPriceId(),
								recurringPriceCreated.getServiceId());
						
						updateServiceForEnriching(recurringPriceCreated.getServiceId(), recurringPrice, getPriceTemplate(""+recurringPriceCreated.getPriceTemplateID()));

						updatePricePlanVersionForEnrichingServiceFee(price, recurringPrice);

						LogSupport.debug(getContext(), CLASS_NAME, "RecurringPrice(RecurringPriceID="
								+ recurringPrice.getRecurringPriceId() + ") created successfully.");
					}
				}
			}
		}

		LogSupport.info(getContext(), CLASS_NAME, "End of createPriceEntries");
	}
	
	private void updatePricePlanVersionForEnrichingServiceFee(Price price, Object actualPriceObj) throws HomeException {
		LogSupport.debug(getContext(), CLASS_NAME, "Start of updatePricePlanVersionForEnrichingServiceFee");
		
		//enrich serviceFee2
		Home productPriceHome = (Home) getContext().get(ProductPriceHome.class);
		ProductPrice productPrice = (ProductPrice) productPriceHome.find(new EQ(ProductPriceXInfo.PRODUCT_PRICE_ID, price.getProductPriceId()));
		
		Home pricePlanVersionHome = (Home) getContext().get(PricePlanVersionHome.class);
		
		And and = new And();
		and.add(new EQ(PricePlanVersionXInfo.ID, productPrice.getPricePlanId()));
		and.add(new EQ(PricePlanVersionXInfo.VERSION, productPrice.getPricePlanVersionId()));

		PricePlanVersion pricePlanVersion = (PricePlanVersion) pricePlanVersionHome.find(and);
		
		if (pricePlanVersion != null) {
			ServicePackageVersion servicePackageVersion = pricePlanVersion.getServicePackageVersion();
			
			if (null == servicePackageVersion) {
				LogSupport.crit(getContext(), this,
						"ServicePackageVersion is null for PricePlanVersion(id="
								+ productPrice.getPricePlanVersionId()
								+ ", ServiceFee2 will not be updated for product price [id]: "
								+ productPrice.getProductId());

				throw new HomeException("ServicePackageVersion is null for PricePlanVersion(id="
						+ productPrice.getPricePlanVersionId()
						+ ", ServiceFee2 will not be updated for product price [id]: "
						+ productPrice.getProductId());
			}
			
			@SuppressWarnings("unchecked")
			Map<ServiceFee2ID, ServiceFee2> serviceFeeMap = servicePackageVersion.getServiceFees();
			
			if (null == serviceFeeMap) {
				LogSupport.crit(getContext(), this,
						"ServiceFeeMap is null for PricePlanVersion(id="
								+ productPrice.getPricePlanVersionId()
								+ ", ServiceFee2 will not be updated for product price [id]: "
								+ productPrice.getProductId());

				throw new HomeException("ServiceFeeMap is null for PricePlanVersion(id="
						+ productPrice.getPricePlanVersionId()
						+ ", ServiceFee2 will not be updated for product price [id]: "
						+ productPrice.getProductId());
			}
			
			Set<ServiceFee2ID> serviceIdPathCombinations = serviceFeeMap.keySet();
			
			for (ServiceFee2ID serviceIdPath : serviceIdPathCombinations) {
				ServiceFee2 serviceFee2 = serviceFeeMap.get(serviceIdPath);
				if (serviceFee2 != null) {
					if (actualPriceObj instanceof OneTimePrice) {
                        OneTimePrice oneTimePrice = (OneTimePrice) actualPriceObj;
                        if (serviceFee2.getServiceId() == oneTimePrice.getServiceId() && serviceFee2.getPath().equals(oneTimePrice.getPath())) 
                        {
                              enrichServiceFeeWithOneTimePriceData(serviceFee2, oneTimePrice);
                        }
                  } else if (actualPriceObj instanceof RecurringPrice) {
                        RecurringPrice recurringPrice = (RecurringPrice) actualPriceObj;
                        if (serviceFee2.getServiceId() == recurringPrice.getServiceId() && serviceFee2.getPath().equals(recurringPrice.getPath())) 
                        {
                              enrichServiceFeeWithRecurringPriceData(serviceFee2, recurringPrice);
                        }
                  }
				} else {
					LogSupport.debug(getContext(), this,
							"Unable to find ServiceFee2(serviceIdPath=" + serviceIdPath
									+ "), ServiceFee2 will not be updated for product price [id]: "
									+ productPrice.getProductId());
				}
			}
			
			pricePlanVersionHome.store(getContext(), pricePlanVersion);
			LogSupport.debug(getContext(), CLASS_NAME,
					"pricePlanVersion(ID=" + pricePlanVersion.getId() + ") updated successfully.");

		} else {
			LogSupport.debug(getContext(), this,
					"Unable to find PricePlanVersion(id=" + productPrice.getPricePlanVersionId()
							+ "), ServiceFee2 will not be updated for product price [id]: "
							+ productPrice.getProductId());
		}

		LogSupport.debug(getContext(), CLASS_NAME, "End of updatePricePlanVersionForEnrichingServiceFee");
	}

	private void enrichServiceFeeWithRecurringPriceData(ServiceFee2 serviceFee2, RecurringPrice recurringPrice) {
		LogSupport.debug(getContext(), CLASS_NAME, "Start of enrichServiceFeeWithRecurringPriceData");
		serviceFee2.setFee(recurringPrice.getAmount());
		serviceFee2.setRecurrenceInterval(recurringPrice.getRecurrenceInterval());
		LogSupport.debug(getContext(), CLASS_NAME, "End of enrichServiceFeeWithRecurringPriceData");
	}

	private void enrichServiceFeeWithOneTimePriceData(ServiceFee2 serviceFee2, OneTimePrice oneTimePrice) {
		LogSupport.debug(getContext(), CLASS_NAME, "Start of enrichServiceFeeWithOneTimePriceData");
		serviceFee2.setFee(oneTimePrice.getAmount());
		LogSupport.debug(getContext(), CLASS_NAME, "End of enrichServiceFeeWithOneTimePriceData");
	}

	/**
	 * Prepares instance level list of OneTimePrice, RecurringPrice, Price and
	 * Service, which need to be created/updated in the DB. This is a recursive
	 * method as {@link CompositePriceIO} may have 'n' level nesting of
	 * {@link CompositePriceIO}s
	 * 
	 * @param compositePriceIO
	 *            {@link CompositePriceIO}
	 * @throws NumberFormatException
	 * @throws HomeInternalException
	 * @throws HomeException
	 */
	private void handleCompositePrice(CompositePriceIO compositePriceIO)
			throws NumberFormatException, HomeInternalException, HomeException {
		LogSupport.debug(getContext(), CLASS_NAME, "Start of handleCompositePrice");
		if(compositePriceIO != null) {
			// One time charge 
			OneTimeCharge oneTimeCharge = compositePriceIO.getOneTimeCharge();
			if (oneTimeCharge != null) {
				PriceTemplateIO oneTimeChargePT = oneTimeCharge.getPriceTemplate();
				if (oneTimeChargePT != null && PCConstants.SOURCE.equals(oneTimeChargePT.getSource())) {

					// getting the OneTimePrice populated with the default values
					// from PriceTemplate
					OneTimePrice oneTimePrice = getDefaultOneTimePrice(oneTimeChargePT);

					// setting data explicitly provided
					oneTimePrice.setPriceTemplateID(Long.parseLong(oneTimeChargePT.getBusinessKey()));
					
					oneTimePrice.setAmount(MathSupportUtil.round(getContext(), getSpid(), oneTimeCharge.getCharge().getAmount()));
					//oneTimePrice.setCurrency(oneTimeCharge.getCharge().getCurrency());
					//oneTimePrice.setTaxIncluded(oneTimeCharge.getCharge().getTaxIncluded());
					
					oneTimePrice.setServiceId(Long.parseLong(compositePriceIO.getProductBusinessKey()));
					oneTimePrice.setPath(compositePriceIO.getPath());
					oneTimePrices.add(oneTimePrice);
				}
			}

			// Recurring Charge
			RecurringCharge recurringCharge = compositePriceIO.getRecurringCharge();
			if (recurringCharge != null) {
				PriceTemplateIO recurringChargePT = recurringCharge.getPriceTemplate();
				if (recurringChargePT != null && PCConstants.SOURCE.equals(recurringChargePT.getSource())) {

					// getting the RecurringCharge populated with the default values
					// from PriceTemplate
					RecurringPrice recurringPrice = getDefaultRecurringPrice(recurringChargePT);

					// setting data explicitly provided
					recurringPrice.setPriceTemplateID(Long.parseLong(recurringChargePT.getBusinessKey()));
					
					//recurringPrice.setActivationFee(recurringCharge.getProRated());
					recurringPrice.setAmount(MathSupportUtil.round(getContext(), getSpid(), recurringCharge.getCharge().getAmount()));
					//recurringPrice.setCurrency(recurringCharge.getCharge().getCurrency());
					recurringPrice.setChargeScheme(ServicePeriodEnum.getByName(recurringCharge.getFrequency()));
					//recurringPrice.setRecurrenceInterval(recurringCharge.getRecurrenceCount());
					//recurringPrice.setTaxIncluded(recurringCharge.getCharge().getTaxIncluded());
					
					recurringPrice.setServiceId(Long.parseLong(compositePriceIO.getProductBusinessKey()));
					recurringPrice.setPath(compositePriceIO.getPath());
					recurringPrices.add(recurringPrice);
				}
			}

			// getting inner level composite price if any
			CompositePrices_type1 compositePrices = compositePriceIO.getCompositePrices();
			if (compositePrices != null) {
				CompositePriceIO[] innerPriceIOs = compositePrices.getCompositePrice();
				if (innerPriceIOs != null && innerPriceIOs.length > 0) {
					for (CompositePriceIO innerPriceIO : innerPriceIOs) {
						handleCompositePrice(innerPriceIO);
					}
				}
			}

		} else {
			LogSupport.debug(getContext(), CLASS_NAME, "compositePriceIO is null");
		}
				
		LogSupport.debug(getContext(), CLASS_NAME, "End of handleCompositePrice");
	}

	/**
	 * Prepares instance level list of {@link Service}, which needs to be updated in the
	 * DB.
	 * 
	 * @param serviceId
	 *            {@link String} productBusinessKey associated with the price
	 *            entry
	 * @param price
	 *            {@link Object} the actual price object, {@link OneTimePrice}
	 *            or {@link RecurringPrice}
	 * @throws HomeInternalException
	 * @throws HomeException
	 */
	private void updateServiceForEnriching(long serviceId, Object price, PriceTemplate priceTemplate)
			throws HomeInternalException, HomeException {
		LogSupport.debug(getContext(), CLASS_NAME, "Start of addServiceForEnriching");

		Home serviceHome = (Home) getContext().get(ServiceHome.class);
		if (serviceHome != null) {
			Object serviceObject = serviceHome.find(new EQ(ServiceXInfo.ID, serviceId));
			if (serviceObject != null) {
				Service service = (Service) serviceObject;
				LogSupport.debug(getContext(), CLASS_NAME, "Service(ID=" + service.getID() + ") exists.");

				if (price instanceof OneTimePrice) {
					OneTimePrice oneTimePrice = (OneTimePrice) price;
					service = enrichServiceWithOneTimePriceData(service, oneTimePrice, priceTemplate);
				} else if (price instanceof RecurringPrice) {
					RecurringPrice recurringPrice = (RecurringPrice) price;
					service = enrichServiceWithRecurringPriceData(service, recurringPrice, priceTemplate);
				}

				serviceHome.store(getContext(), service);
				LogSupport.debug(getContext(), CLASS_NAME, "Service(ID=" + service.getID() + ") updated successfully.");
			
			} else {
				LogSupport.debug(getContext(), CLASS_NAME, "Service(ID=" + serviceId + ") does not exist.");
			}
		}

		LogSupport.debug(getContext(), CLASS_NAME, "End of addServiceForEnriching");
	}
	
	/**
	 * Enriches/sets {@link OneTimePrice} data to {@link Service}
	 * 
	 * @param service
	 *            {@link Service} the service that needs to be enriched
	 * @param oneTimePrice
	 *            {@link OneTimePrice} the oneTimePrice from which the data
	 *            needs to be taken
	 * @return {@link Service} the enriched service
	 */
	private Service enrichServiceWithOneTimePriceData(Service service, OneTimePrice oneTimePrice, PriceTemplate priceTemplate) {
		LogSupport.debug(getContext(), CLASS_NAME, "Start of enrichServiceWithOneTimePriceData");
		
		//service.setAdjustmentType(oneTimePrice.getAdjustmentType());
		service.setRecurrenceType(oneTimePrice.getRecurrenceType());
		service.setStartDate(oneTimePrice.getStartDate());
		service.setEndDate(oneTimePrice.getEndDate());
		service.setValidity(oneTimePrice.getValidity());
		service.setFixedInterval(oneTimePrice.getFixedInterval());
		service.setTaxAuthority(oneTimePrice.getTaxAuthority());
		//service.setChargePrepaidSubscribers(oneTimePrice.getChargePrepaidSubscribers());
		service.setRefundOption(oneTimePrice.getRefundOption());
		service.setRefundable(oneTimePrice.getRefundable());
		service.setFeePersonalizationAllowed(oneTimePrice.getFeePersonalizationAllowed());
		service.setForceCharging(oneTimePrice.getForceCharging());

		service = enrichServiceWithAdjustmentData(service, priceTemplate);

		LogSupport.debug(getContext(), CLASS_NAME, "End of enrichServiceWithOneTimePriceData");
		return service;
	}

	/**
	 * Enriches/sets {@link OneTimePrice} data to {@link Service}
	 * 
	 * @param service
	 *            {@link Service} the service that needs to be enriched
	 * @param recurringPrice
	 *            {@link RecurringPrice} the recurringPrice from which the data
	 *            needs to be taken
	 * @return {@link Service} the enriched service
	 */
	private Service enrichServiceWithRecurringPriceData(Service service, RecurringPrice recurringPrice, PriceTemplate priceTemplate) {
		LogSupport.debug(getContext(), CLASS_NAME, "Start of enrichServiceWithRecurringPriceData");
		service.setActivationFee(recurringPrice.getActivationFee());
		service.setTaxAuthority(recurringPrice.getTaxAuthority());
		//service.setChargePrepaidSubscribers(recurringPrice.getChargePrepaidSubscribers());
		service.setRefundOption(recurringPrice.getRefundOption());
		service.setRefundable(recurringPrice.getRefundable());
		service.setFeePersonalizationAllowed(recurringPrice.getFeePersonalizationAllowed());
		service.setForceCharging(recurringPrice.getForceCharging());
		service.setRecurrenceInterval(recurringPrice.getRecurrenceInterval());
		service.setBillingMonth(recurringPrice.getBillingMonth());
		service.setDynamicBilling(recurringPrice.getDynamicBilling());
		//service.setPaymentOption(recurringPrice.getPaymentOption());
		service.setFeePersonalizationRule(recurringPrice.getFeePersonalizationRule());
		
		service = enrichServiceWithAdjustmentData(service, priceTemplate);

		LogSupport.debug(getContext(), CLASS_NAME, "End of enrichServiceWithRecurringPriceData");
		return service;
	}
	
	/**
	 * Enriches/sets adjustment data from {@link PriceTemplate} to
	 * {@link Service}
	 * 
	 * @param service
	 *            {@link Service} the service that needs to be enriched
	 * @param priceTemplate
	 *            {@link PriceTemplate} the priceTemplate from which the data
	 *            needs to be taken
	 * @return {@link Service} the enriched service
	 */
	private Service enrichServiceWithAdjustmentData(Service service, PriceTemplate priceTemplate) {
		LogSupport.debug(getContext(), CLASS_NAME, "Start of enrichServiceWithAdjustmentData");
		
		service.setAdjustmentTypeDesc(priceTemplate.getAdjustmentTypeDesc());
		service.setAdjustmentGLCode(priceTemplate.getAdjustmentGLCode());
		service.setAdjustmentInvoiceDesc(priceTemplate.getAdjustmentInvoiceDesc());
		service.setBillGroupID(priceTemplate.getBillGroupID());

		LogSupport.debug(getContext(), CLASS_NAME, "End of enrichServiceWithAdjustmentData");
		return service;
	}

	/**
	 * Prepares instance level list of {@link Price}, which needs to be created
	 * in the DB.
	 * 
	 * @param chargeType
	 *            {@link ChargeTypeEnum}
	 * @param chargeId
	 *            long the corresponding oneTimePriceId or recurringPriceId
	 * @param productBusinessKey
	 *            {@link String} the productBusinessKey associated with the
	 *            price
	 * @return {@link Price}
	 * @throws HomeException 
	 * @throws HomeInternalException 
	 */
	private Price createPrice(ChargeTypeEnum chargeType, long chargeId, long productId)
			throws HomeInternalException, HomeException {
		LogSupport.debug(getContext(), CLASS_NAME, "Start of createPrice");

		// get productPrice map from ctx
		if (getContext().get(PRODUCT_ID_PRODUCT_PRICE_MAP) != null) {
			@SuppressWarnings("unchecked")
			Map<String, ProductPrice> prodId_ProductPrice_Map = (Map<String, ProductPrice>) getContext()
					.get(PRODUCT_ID_PRODUCT_PRICE_MAP);
			ProductPrice productPrice = prodId_ProductPrice_Map.get(productId);

			if (productPrice != null) {
				Price price = new Price();
				price.setChargeId(chargeId);
				price.setChargeType(chargeType);
				price.setProductPriceId(productPrice.getProductPriceId());

				Home priceHome = (Home) getContext().get(PriceHome.class);
				if (priceHome != null) {
					price = (Price) priceHome.create(getContext(), price);
					LogSupport.debug(getContext(), CLASS_NAME,
							"Price(ChargeID=" + price.getChargeId() + ") entry created successfully.");
					LogSupport.debug(getContext(), CLASS_NAME, "End of createPrice");
					return price;
				} else {
					throw new HomeException("priceHome is null");
				}
			} else {
				throw new HomeException("productPrice is null");
			}
		} else {
			throw new HomeException("productPrice map not available in context");
		}
	}

	/**
	 * Returns a {@link OneTimePrice} after setting the default values from
	 * {@link PriceTemplate}
	 * 
	 * @param oneTimeChargePT
	 *            the {@link PriceTemplateIO} received in the request
	 * @return {@link OneTimePrice} populated with default value from
	 *         {@link PriceTemplate}
	 * @throws HomeException
	 * @throws HomeInternalException
	 * @throws NumberFormatException
	 */
	private OneTimePrice getDefaultOneTimePrice(PriceTemplateIO oneTimeChargePT)
			throws NumberFormatException, HomeInternalException, HomeException {
		LogSupport.debug(getContext(), CLASS_NAME, "Start of getDefaultOneTimePrice");

		PriceTemplate priceTemplate = getPriceTemplate(oneTimeChargePT.getBusinessKey());
		if (priceTemplate == null) {
			throw new HomeException("PriceTemplate not found for ID=" + oneTimeChargePT.getBusinessKey());
		}

		OneTimePrice oneTimePrice = new OneTimePrice();

		oneTimePrice.setAdjustmentType(priceTemplate.getAdjustmentType());
		oneTimePrice.assertAdjustmentTypeVersionId(0);
		//oneTimePrice.setAmount(0);
		
		oneTimePrice.setBalanceIndicator(priceTemplate.getBalanceIndicator());
		
		oneTimePrice.setChargePrepaidSubscribers(priceTemplate.getChargePrepaidSubscribers());
		//oneTimePrice.setCompatibilityGroups(priceTemplate.getCompatibilitySpecs());
		oneTimePrice.setCreatedDate(new Date());
		//oneTimePrice.setCurrency(null);

		oneTimePrice.setDescription(priceTemplate.getDescription());

		oneTimePrice.setEndDate(priceTemplate.getEndDate());

		oneTimePrice.setFeePersonalizationAllowed(priceTemplate.getFeePersonalizationAllowed());
		oneTimePrice.setForceCharging(priceTemplate.getForceCharging());
		oneTimePrice.setFixedInterval(priceTemplate.getFixedInterval());

		oneTimePrice.setGlCode(priceTemplate.getAdjustmentGLCode());
		
		oneTimePrice.setName(priceTemplate.getName());
		
		//oneTimePrice.setOneTimePriceId(/*will get generated*/);
		
		oneTimePrice.setPriceTemplateID(priceTemplate.getID());
		
		//oneTimePrice.setRechargeFailureAction(null);
		oneTimePrice.setRecurrenceType(priceTemplate.getRecurrenceType());
		oneTimePrice.setRefundable(priceTemplate.getRefundable());
		oneTimePrice.setRefundOption(priceTemplate.getRefundOption());

		//oneTimePrice.setServiceId(0);
		oneTimePrice.setSpid(priceTemplate.getSpid());
		oneTimePrice.setStartDate(priceTemplate.getStartDate());
		
		oneTimePrice.setTaxAuthority(priceTemplate.getTaxAuthority());
		//oneTimePrice.setTaxIncluded(false);
		
		oneTimePrice.setValidity(priceTemplate.getValidity());

		LogSupport.debug(getContext(), CLASS_NAME, "End of getDefaultOneTimePrice");
		
		return oneTimePrice;
	}

	/**
	 * Returns a {@link RecurringPrice} after setting the default values from
	 * {@link PriceTemplate}
	 * 
	 * @param recurringChargePT
	 *            the {@link PriceTemplateIO} received in the request
	 * @return {@link RecurringPrice} populated with default value from
	 *         {@link PriceTemplate}
	 * @throws HomeException
	 * @throws HomeInternalException
	 * @throws NumberFormatException
	 */
	private RecurringPrice getDefaultRecurringPrice(PriceTemplateIO recurringChargePT)
			throws NumberFormatException, HomeInternalException, HomeException {
		LogSupport.debug(getContext(), CLASS_NAME, "Start of getDefaultRecurringPrice");

		PriceTemplate priceTemplate = getPriceTemplate(recurringChargePT.getBusinessKey());

		if (priceTemplate == null) {
			throw new HomeException("PriceTemplate not found for ID=" + recurringChargePT.getBusinessKey());
		}

		RecurringPrice recurringPrice = new RecurringPrice();

		recurringPrice.setActivationFee(priceTemplate.getActivationFee());
		recurringPrice.setAdjustmentTypeId(priceTemplate.getAdjustmentType());
		
		recurringPrice.setBalanceIndicator(priceTemplate.getBalanceIndicator());
		recurringPrice.setBillingMonth(priceTemplate.getBillingMonth());
		
		recurringPrice.setChargePrepaidSubscribers(priceTemplate.getChargePrepaidSubscribers());
		recurringPrice.setChargeScheme(priceTemplate.getChargeScheme());
		//recurringPrice.setCompatibilityGroups(priceTemplate.getCompatibilitySpecs());
		recurringPrice.setCreatedDate(new Date());
		//recurringPrice.setCurrency(null);
		
		recurringPrice.setDescription(priceTemplate.getDescription());
		recurringPrice.setDynamicBilling(priceTemplate.getDynamicBilling());

		recurringPrice.setFeePersonalizationAllowed(priceTemplate.getFeePersonalizationAllowed());
		recurringPrice.setFeePersonalizationRule(priceTemplate.getFeePersonalizationRule());
		//recurringPrice.setFirstMonthFree(priceTemplate.getFirstMonthFree());
		recurringPrice.setForceCharging(priceTemplate.getForceCharging());
		
		recurringPrice.setGlCode(priceTemplate.getAdjustmentGLCode());
		
		recurringPrice.setName(priceTemplate.getName());

		//recurringPrice.setPaymentOption(priceTemplate.getPaymentOption());
		recurringPrice.setPriceTemplateID(priceTemplate.getID());
//		recurringPrice.setProrationRule(false);

//		recurringPrice.setRechargeFailureAction(null);
		recurringPrice.setRecurrenceInterval(priceTemplate.getRecurrenceInterval());
		
		//recurringPrice.setRecurringPriceId(/*will get generated*/);

		recurringPrice.setRefundable(priceTemplate.getRefundable());
		recurringPrice.setRefundOption(priceTemplate.getRefundOption());
		
//		recurringPrice.setServiceId(0);
		recurringPrice.setSpid(priceTemplate.getSpid());

		recurringPrice.setTaxAuthority(priceTemplate.getTaxAuthority());
//		recurringPrice.setTaxIncluded(false);

		LogSupport.debug(getContext(), CLASS_NAME, "End of getDefaultRecurringPrice");

		return recurringPrice;
	}

	/**
	 * Returns the {@link PriceTemplate} corresponding to the input templateId
	 * 
	 * @param ctx
	 *            {@link Context} the context
	 * @param templateId
	 *            {@link String} the templateId
	 * @return {@link PriceTemplate}
	 * @throws HomeException
	 * @throws HomeInternalException
	 * @throws NumberFormatException
	 *             if the priceTemplateId is not a number
	 */
	private PriceTemplate getPriceTemplate(String templateId)
			throws NumberFormatException, HomeInternalException, HomeException {
		Home priceTemplateHome = (Home) getContext().get(PriceTemplateHome.class);
		PriceTemplate priceTemplate = (PriceTemplate) priceTemplateHome.find(getContext(),
				new EQ(PriceTemplateXInfo.ID, Long.parseLong(templateId)));
		
		
		return priceTemplate;
	}
	
	@Override
	public Context getContext() {
		return _ctx;
	}

	@Override
	public void setContext(Context ctx) {
		this._ctx = ctx;
	}

	@Override
	public int getSpid() {
		return _spid;
	}

	@Override
	public void setSpid(int spid) {
		this._spid = spid;
	}
}