/*
 *  AbstractAccountSubControllerBorder.java
 *
 *  Author : kgreer
 *  Date   : Apr 11, 2005
 *
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily
 * available. Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee, no unauthorised use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the licence agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved.
 */ 
 
package com.trilogy.app.crm.web.acctmenu;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.framework.xhome.context.*;
import com.trilogy.framework.xhome.web.border.*;
import com.trilogy.framework.xhome.xdb.XPreparedStatement;
import com.trilogy.framework.xhome.xdb.XStatement;
import com.trilogy.framework.xhome.xdb.AbstractXStatement;
import java.sql.SQLException;


/**
 * SubControllerBorder for use with any WebController for Beans which
 * implement the Child interface with Account being their parent.
 *
 * @author  kgreer
 **/
public class AccountSubControllerBorder
   extends SubControllerBorder
{

   public final static String DEFAULT_FIELD_NAME = "BAN";
   
   
   public AccountSubControllerBorder(Class childCls)
   {
      this(childCls, DEFAULT_FIELD_NAME);
   }
   
   public AccountSubControllerBorder(Class childCls, String fieldName)
   {
      super(Account.class, childCls, fieldName);
   }

}


