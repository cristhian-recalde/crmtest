package com.trilogy.app.crm.web.border;

import com.trilogy.app.crm.bean.ui.Product;
import com.trilogy.app.crm.bean.ui.ProductSearchWebControl;
import com.trilogy.app.crm.bean.ui.ProductSearchXInfo;
import com.trilogy.app.crm.bean.ui.ProductXInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.web.search.SearchBorder;
import com.trilogy.framework.xhome.web.search.SelectSearchAgent;
//import com.trilogy.framework.xhome.web.search.WildcardSelectSearchAgent;

public class ProductSearchBorder extends SearchBorder{
	public ProductSearchBorder(Context ctx ) {
		super(ctx, Product.class, new ProductSearchWebControl());

		addAgent( new SelectSearchAgent(ProductXInfo.PRODUCT_ID, ProductSearchXInfo.ID,false).addIgnore(Long.valueOf(-1)));
		
		SelectSearchAgent spidAgent = new SelectSearchAgent(ProductXInfo.SPID, ProductSearchXInfo.SPID,false);
     	addAgent(spidAgent.addIgnore(Integer.valueOf(9999)));

     	addAgent(new SelectSearchAgent(ProductXInfo.PRODUCT_TYPE,ProductSearchXInfo.PRODUCT_TYPE,false));
	}
}
