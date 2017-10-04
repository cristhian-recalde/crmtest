/**
 * 
 */
package com.trilogy.app.crm.support;

import java.util.Collection;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.ActivationReasonCode;
import com.trilogy.app.crm.bean.ActivationReasonCodeHome;
import com.trilogy.app.crm.bean.ActivationReasonCodeXInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.MinorLogMsg;


/**
 * @author kumaran.sivasubramaniam
 */
public class ActivationReasonCodeSupport
{

    /**
     * Gets conversion activation reason code. If it doesn't exist, then it will create one
     * 
     * @param context
     * @param type
     * @return
     * @throws HomeException
     */
    public static ActivationReasonCode getConversionActivationReasonCode(final Context ctx, int spid)
            throws HomeException
    {
        final Home activationHome = (Home) ctx.get(ActivationReasonCodeHome.class);

        if (activationHome == null)
        {
            throw new HomeException("Startup error: no ActivationReasonCodeHome found in context.");
        }

        final And condition = new And();
        condition.add(new EQ(ActivationReasonCodeXInfo.MESSAGE, Common.CONVERSION_ACTIVATION_REASON_CODE));
        condition.add(new EQ(ActivationReasonCodeXInfo.SPID, spid));

        final Collection conversionReasonList = activationHome.select(ctx, condition);
        ActivationReasonCode conversionReasonCode = null;

        if ((conversionReasonList == null) || (conversionReasonList.isEmpty()))
        {
            conversionReasonCode = new ActivationReasonCode();
            conversionReasonCode.setMessage(Common.CONVERSION_ACTIVATION_REASON_CODE);
            conversionReasonCode.setSpid(spid);
            conversionReasonCode = (ActivationReasonCode) activationHome.create(ctx, conversionReasonCode);
        }
        else
        {
            conversionReasonCode = (ActivationReasonCode) conversionReasonList.iterator().next();
        }

        return conversionReasonCode;
    }


    /**
     * @param ctx
     * @param activationCode
     * @return
     */
    public static String getActivationReasonCodeMessage(Context ctx, int activationCode)
    {
        String message = "";
        Home activHome = (Home) ctx.get(ActivationReasonCodeHome.class);

        if (activHome != null)
        {
            try
            {
                ActivationReasonCode code = (ActivationReasonCode) activHome.find(ctx, Integer.valueOf(activationCode));

                if (code != null)
                {
                    message = code.getMessage();
                }
                else
                {
                    new MinorLogMsg(ActivationReasonCodeSupport.class,
                            "Cannot find ActivationReasonCode with id: " + activationCode, null).log(ctx);
                }
            }
            catch (HomeException e)
            {
                new MinorLogMsg(ActivationReasonCodeSupport.class,
                        "Cannot find ActivationReasonCode with id: " + activationCode + " Error: " + e.getMessage(),
                        e).log(ctx);
            }
        }

        return message;
    }

}
