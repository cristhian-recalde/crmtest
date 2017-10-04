package com.trilogy.app.crm.bean;

import com.trilogy.app.crm.client.alcatel.AccountUsageXInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.json.JSONParser;
import com.trilogy.framework.xhome.parse.Parser;
import com.trilogy.framework.xhome.support.IdentitySupport;

/**
 * This dual identity support is used when trying to display both
 * Account beans and Subscriber beans in the same TreeWebControl
 */
public class AccountSubscriberIdentitySupport
   implements IdentitySupport
{
   private static final IdentitySupport instance__ = new AccountSubscriberIdentitySupport();

   public AccountSubscriberIdentitySupport()
   {
      accountIdSupport_ = AccountIdentitySupport.instance();
      subscriberIdSupport_ = SubscriberIdentitySupport.instance();
   }

   public static IdentitySupport instance()
   {
      return instance__;
   }

   /**
    * not implemented because it is not necessary for TreeWebControl
    */
   public Object toBean(Object id)
   {
      return null;
   }

   public Object setID(Object bean, Object id)
   {
      if (bean instanceof Account)
      {
         return accountIdSupport_.setID(bean, id);
      }
      if (bean instanceof Subscriber)
      {
         return subscriberIdSupport_.setID(bean, id);
      }
      // this shouldn't happen
      return null;
   }

   public Object fromStringID(String id)
   {
      return id;
   }

   public String toStringID(Object id)
   {
      return (String) ((String) id);
   }

   public Object ID(Object bean)
   {
      if (bean instanceof Account)
      {
         return accountIdSupport_.ID(bean);
      }
      if (bean instanceof Subscriber)
      {
         return subscriberIdSupport_.ID(bean);
      }
      // this shouldn't happen
      return null;
   }

   /** @return true iff the supplied object is of a suitable identity type. **/
   public boolean isKey(Object obj)
   {
       return ( obj instanceof String );
   }

   public Parser createParser(Context ctx)
   {
       return JSONParser.parser(ctx, AccountXInfo.BAN);
   }
   private IdentitySupport accountIdSupport_;
   private IdentitySupport subscriberIdSupport_;


    @Override
    public Object toKey(Object id)
    {
        if (id instanceof Account)
            return ID(id);
        if (id instanceof Subscriber)
            return ID(id);
        return isKey(id) ? id : null;
    }
}
