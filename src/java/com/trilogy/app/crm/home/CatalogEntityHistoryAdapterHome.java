package com.trilogy.app.crm.home;

import com.trilogy.app.crm.bean.CatalogEntityEnum;
import com.trilogy.app.crm.bean.CatalogEntityHistory;
import com.trilogy.app.crm.bean.CatalogEntityHistoryHome;
import com.trilogy.app.crm.bean.PricePlan;
import com.trilogy.app.crm.bean.PricePlanIdentitySupport;
import com.trilogy.app.crm.bean.ProductPriceIdentitySupport;
import com.trilogy.app.crm.bean.price.OneTimePrice;
import com.trilogy.app.crm.bean.price.OneTimePriceIdentitySupport;
import com.trilogy.app.crm.bean.price.Price;
import com.trilogy.app.crm.bean.price.PriceIdentitySupport;
import com.trilogy.app.crm.bean.price.RecurringPrice;
import com.trilogy.app.crm.bean.price.RecurringPriceIdentitySupport;
import com.trilogy.app.crm.bean.PricePlanVersion;
import com.trilogy.app.crm.bean.PricePlanVersionIdentitySupport;
import com.trilogy.app.crm.bean.ProductPrice;
import com.trilogy.app.crm.bean.ui.PackageProduct;
import com.trilogy.app.crm.bean.ui.PackageProductIdentitySupport;
import com.trilogy.app.crm.bean.ui.Product;
import com.trilogy.app.crm.bean.ui.ProductIdentitySupport;
import com.trilogy.app.crm.bean.ui.ServiceProduct;
import com.trilogy.app.crm.bean.ui.ServiceProductIdentitySupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * 
 * 
 * @author SaPaul
 * @since 
 */
public class CatalogEntityHistoryAdapterHome extends HomeProxy {

	private static final long serialVersionUID = 1L;

	private CatalogEntityEnum catalogEntityEnum;

	private static final String MESSAGE_ID = "MESSAGEID";

	public CatalogEntityHistoryAdapterHome(Context ctx, Home delegate, CatalogEntityEnum catalogEntityEnum) {
		super(ctx, delegate);
		this.catalogEntityEnum = catalogEntityEnum;

	}

	public CatalogEntityHistoryAdapterHome(Context ctx, Home delegate) {
		super(ctx, delegate);

		this.catalogEntityEnum = CatalogEntityEnum.PricePlan;

	}

	@Override
	public Object create(Context ctx, Object obj) throws HomeException, HomeInternalException {

		LogSupport.debug(ctx, "CatalogEntityHistoryAdapterHome", " create -Start");

		Object ret  = super.create(ctx, obj);
		CatalogEntityHistory catalogEntityHistory = new CatalogEntityHistory();

		String eventId = (String) ctx.get(MESSAGE_ID);
		if(eventId != null && !eventId.isEmpty()){

			Home home = (Home) ctx.get(CatalogEntityHistoryHome.class);

			switch (this.catalogEntityEnum.getIndex()) {
				case CatalogEntityEnum.PricePlan_INDEX:
					PricePlan pricePlan = (PricePlan) obj;
					catalogEntityHistory.setEntityType(catalogEntityEnum);
					catalogEntityHistory.setEventID(eventId);
					// need to generate from entity
					catalogEntityHistory.setEntityKey(PricePlanIdentitySupport.instance().toStringID(PricePlanIdentitySupport.instance().ID(pricePlan)));
					break;

				case CatalogEntityEnum.PricePlanVersion_INDEX:
					PricePlanVersion pricePlanVersion = (PricePlanVersion) obj;

					catalogEntityHistory.setEntityType(catalogEntityEnum);
					catalogEntityHistory.setEventID(eventId);
					catalogEntityHistory.setEntityKey(PricePlanVersionIdentitySupport.instance().toStringID(PricePlanVersionIdentitySupport.instance().ID(pricePlanVersion)));

					break;
				case CatalogEntityEnum.Product_INDEX:
					Product product=(Product)obj;
					catalogEntityHistory.setEntityType(catalogEntityEnum);
					catalogEntityHistory.setEventID(eventId);
					catalogEntityHistory.setEntityKey(ProductIdentitySupport.instance().toStringID(ProductIdentitySupport.instance().ID(product)));
					break;
				case CatalogEntityEnum.ProductPrice_INDEX:
					ProductPrice productPrice=(ProductPrice)obj;
					catalogEntityHistory.setEntityType(catalogEntityEnum);
					catalogEntityHistory.setEventID(eventId);
					catalogEntityHistory.setEntityKey(ProductPriceIdentitySupport.instance().toStringID(ProductPriceIdentitySupport.instance().ID(productPrice)));
					break;
				case CatalogEntityEnum.ServiceProduct_INDEX:
					ServiceProduct serviceProduct=(ServiceProduct)obj;
					catalogEntityHistory.setEntityType(catalogEntityEnum);
					catalogEntityHistory.setEventID(eventId);
					catalogEntityHistory.setEntityKey(ServiceProductIdentitySupport.instance().toStringID(ServiceProductIdentitySupport.instance().ID(serviceProduct)));
					break;
				case CatalogEntityEnum.PackageProduct_INDEX:	
					PackageProduct packageProduct = (PackageProduct)obj;
					catalogEntityHistory.setEntityType(catalogEntityEnum);
					catalogEntityHistory.setEventID(eventId);
					catalogEntityHistory.setEntityKey(PackageProductIdentitySupport.instance().toStringID(PackageProductIdentitySupport.instance().ID(packageProduct)));
					break;
				case CatalogEntityEnum.Price_INDEX:
					Price price = (Price)obj;
					catalogEntityHistory.setEntityType(catalogEntityEnum);
					catalogEntityHistory.setEventID(eventId);
					catalogEntityHistory.setEntityKey(PriceIdentitySupport.instance().toStringID(PriceIdentitySupport.instance().ID(price)));
					break;
				case CatalogEntityEnum.OneTimePrice_INDEX:      
					OneTimePrice oneTimePrice = (OneTimePrice)obj;
					catalogEntityHistory.setEntityType(catalogEntityEnum);
					catalogEntityHistory.setEventID(eventId);
					catalogEntityHistory.setEntityKey(OneTimePriceIdentitySupport.instance().toStringID(OneTimePriceIdentitySupport.instance().ID(oneTimePrice)));
					break;
				case CatalogEntityEnum.RecurringPrice_INDEX:      
					RecurringPrice recurringPrice = (RecurringPrice)obj;
					catalogEntityHistory.setEntityType(catalogEntityEnum);
					catalogEntityHistory.setEventID(eventId);
					catalogEntityHistory.setEntityKey(RecurringPriceIdentitySupport.instance().toStringID(RecurringPriceIdentitySupport.instance().ID(recurringPrice)));
					break;


				default:
					LogSupport.debug(ctx, "Entity Not found ", "catalogEntityEnum");
					break;
			}

			home.create(catalogEntityHistory);
			LogSupport.debug(ctx, "CatalogEntityHistoryAdapterHome", " create -End");
		}else{
			LogSupport.debug(ctx, "CatalogEntityHistoryAdapterHome", " EventId is Null or Empty");
		}

		return ret;

	}

}
