/*
 * This code is a protected work and subject to domestic and
 * international copyright law(s). A complete listing of
 * authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and
 * other information proprietary, valuable and sensitive to
 * Redknee. No unauthorized use, disclosure, manipulation or
 * otherwise is permitted, and may only be used in
 * accordance with the terms of the license agreement
 * entered into with Redknee Inc. and/or its subsidiaries.
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All
 * Rights Reserved.
 */
package com.trilogy.app.crm.home;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.app.crm.bean.CreditCategory;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xhome.language.MessageMgr;

/**
 * Proxy home to check if the corresponding CreditCategory
 * is used by any account, if so deletion of that
 * CreditCategory is not allowed.
 * 
 * @author amit.baid@redknee.com
 */
public class CreditCategoryAcctRelationCheckHome extends HomeProxy
{

  /**
   * @param ctx
   * @param delegate
   */
  public CreditCategoryAcctRelationCheckHome(Context ctx, Home delegate)
  {
    super(ctx, delegate);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.redknee.framework.xhome.home.HomeProxy#remove(com.redknee.framework.xhome.context.Context,
   * java.lang.Object)
   */
  public void remove(Context ctx, Object obj) throws HomeException
  {
    CreditCategory idBean = (CreditCategory) obj;
    isUsedbyAcct(ctx, idBean);
    super.remove(ctx, obj);
  }

  /**
   * Checks whether or not the CreditCategory is used by any
   * account & throws exception accordingly
   * 
   * @param ctx
   * @param idBean
   * @throws HomeException
   */
  private void isUsedbyAcct(Context ctx, CreditCategory idBean)
      throws HomeException
  {
    final MessageMgr mmgr = new MessageMgr(ctx, this);

    Home AcctHome = (Home) ctx.get(AccountHome.class);
    if (AcctHome == null)
      throw new HomeException(
          mmgr
              .get(
                  "CreditCategory.1",
                  "Could not find account home in context(Required to check CreditCategory association), can not continue."));

    Account account = null;
    try
    {
      account = (Account) AcctHome.find(ctx, new EQ(
          AccountXInfo.CREDIT_CATEGORY, XBeans.toObject(idBean.getCode())));
    }
    catch (Exception e)
    {
      throw new HomeException(
          mmgr.get("CreditCategory.2",
                  "Error while checking account association with CreditCategory, can not continue."));
    }

    if (account != null)
      throw new HomeException(mmgr.get("CreditCategoryInUse",
          "CreditCategory " + idBean.getCode()+ " is under use,can't delete."));

  }

  private static final long serialVersionUID = 1L;

}
