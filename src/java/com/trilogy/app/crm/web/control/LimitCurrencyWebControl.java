package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;

import com.trilogy.framework.core.web.XCurrencyWebControl;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.webcontrol.WebControl;


/*
 *  Web Control used to display Monthly Spend Limit.  If monthly limit is -1, then we would write out "Infinite" 
 */
public class LimitCurrencyWebControl
   extends XCurrencyWebControl
{


	private final static WebControl instance__ = new LimitCurrencyWebControl();

	public static WebControl instance()
	{
		return instance__;
	}


   public LimitCurrencyWebControl()
   {
      super(true);
   }


   public LimitCurrencyWebControl(boolean showCurrency)
   {
      super(showCurrency);
   }


   public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
   {
      int   mode   = ctx.getInt("MODE", DISPLAY_MODE);

      switch (mode)
      {
         case CREATE_MODE:
         case EDIT_MODE:
            super.toWeb(ctx, out, name, obj);
            break;
         case DISPLAY_MODE:
         default:
            if ( obj != null ) 
            {
                long  limit  = ((Number) obj).longValue();
                if ( limit < 0 )
                {
                    out.print("No Limit");
                }
                else
                {
                    super.toWeb(ctx, out, name, obj);
                }
            }
      }
   }


}

