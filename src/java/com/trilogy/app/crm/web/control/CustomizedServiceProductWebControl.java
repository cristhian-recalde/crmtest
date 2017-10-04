package com.trilogy.app.crm.web.control;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CUGTypeEnum;
import com.trilogy.app.crm.bean.ClosedUserGroupTemplateHome;
import com.trilogy.app.crm.bean.ClosedUserGroupTemplateXInfo;
import com.trilogy.app.crm.bean.ui.ServiceProduct;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.Or;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.app.crm.bean.ui.Product;
import com.trilogy.app.crm.bean.ui.ProductXInfo;


public class CustomizedServiceProductWebControl extends
		CustomizedClosedUserGroupTemplateKeyWebControl {

	public CustomizedServiceProductWebControl(int listSize, boolean autoPreview, Object optionalValue, PropertyInfo cugTemplateIDProperty, CUGTypeEnum cugType)
    {
		super(listSize, autoPreview, optionalValue, cugTemplateIDProperty,cugType);
    }

	public Context filterDeprecatedClosedUserGroupTemplates(final Context ctx) {

		Context subContext = ctx.createSubContext();
		Object obj = ctx.get(AbstractWebControl.BEAN);
		Long cugTemplateID = null;

		if (cugTemplateIDProperty_ != null) {
			cugTemplateID = (Long) cugTemplateIDProperty_.get(obj);
		}

		long spid = getSpidFromParent(ctx,obj);

		if (spid < 0) {
			// Hack to fetch spid if it is not available in the bean. This hack
			// is done for FNF extension.
			// A clean fix should be given for this.
			Account act = (Account) ctx.get(Account.class);
			if (act != null) {
				spid = act.getSpid();
			}
		}

		Predicate filter = null;

		if (spid > 0) {
			filter = new And().add(
					new EQ(ClosedUserGroupTemplateXInfo.DEPRECATED,
							Boolean.FALSE)).add(
					new EQ(ClosedUserGroupTemplateXInfo.SPID, spid));
		} else {
			filter = new EQ(ClosedUserGroupTemplateXInfo.DEPRECATED,
					Boolean.FALSE);
		}

		// Adding selected CUG Template, if it's deprecated.
		if ((cugTemplateID != null) && (cugTemplateID.longValue() >= 0)) {
			filter = new Or().add(filter).add(
					new EQ(ClosedUserGroupTemplateXInfo.ID, cugTemplateID));
		}

		if (cugType_ != null) {
			filter = new And().add(filter).add(
					new EQ(ClosedUserGroupTemplateXInfo.CUG_TYPE, cugType_));
		}

		final Home originalHome = (Home) subContext
				.get(ClosedUserGroupTemplateHome.class);
		final Home newHome = new HomeProxy(subContext, originalHome).where(
				subContext, filter);
		subContext.put(ClosedUserGroupTemplateHome.class, newHome);

		return subContext;

	}

	private long getSpidFromParent(Context ctx, Object obj) {
		ServiceProduct nVersion = (ServiceProduct)obj;
		
		long spid = 0;
		try{
			And filter = new And();
			filter.add(new EQ(ProductXInfo.PRODUCT_ID,nVersion.getProductId()));
			Product service = HomeSupportHelper.get(ctx).findBean(ctx, Product.class, filter);
			spid = service.getSpid();
			//HomeSupportHelper.get(ctx).findBean(ctx, CRMSpid.class, new And().add(new EQ(CRMSp)));
		
		} catch (Exception e) {
			LogSupport.minor(ctx, this,"CRMSpid: Can not find" + e.getMessage());
		}
		
		return spid;
	}

}
