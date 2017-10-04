package com.trilogy.app.crm.numbermgn;

import com.trilogy.app.crm.bean.GSMPackage;
import com.trilogy.app.crm.bean.GSMPackageHome;
import com.trilogy.app.crm.bean.GSMPackageXInfo;
import com.trilogy.app.crm.bean.TDMAPackage;
import com.trilogy.app.crm.bean.TDMAPackageHome;
import com.trilogy.app.crm.bean.TDMAPackageXInfo;
import com.trilogy.app.crm.bean.VSATPackage;
import com.trilogy.app.crm.bean.VSATPackageXInfo;
import com.trilogy.app.crm.home.OldGSMPackageLookupHome;
import com.trilogy.app.crm.home.OldTDMAPackageLookupHome;
import com.trilogy.app.crm.home.OldVSATPackageLookupHome;
import com.trilogy.framework.xhome.beans.FacetMgr;
import com.trilogy.framework.xhome.beans.Identifiable;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xhome.webcontrol.WebControl;
import com.trilogy.framework.xlog.log.MajorLogMsg;


public class PackageChangeAppendHistoryHome extends AppendNumberMgmtHistoryHome implements PackageProcessor
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;


    public PackageChangeAppendHistoryHome(Home delegate)
    {
        super(delegate, PackageMgmtHistoryHome.class);
    }


    public Object store(Context ctx, Object obj) throws HomeException, HomeInternalException
    {
        try
        {
            Object result = super.store(ctx, obj);
            if (obj instanceof GSMPackage)
            {
                processPackage(ctx, (GSMPackage) obj);
            }
            else if (obj instanceof TDMAPackage)
            {
                processPackage(ctx, (TDMAPackage) obj);
            }
            else if (obj instanceof VSATPackage)
            {
                processPackage(ctx, (VSATPackage) obj);
            }
            return result;
        }
        catch (PackageProcessingException e)
        {
            final String message;
            {
                final GenericPackage genericPakcage = (GenericPackage) obj;
                message = "Could not create package History for Package of tyoe[" + obj.getClass().getSimpleName()
                        + "] with ID [" + genericPakcage.getPackId() + "] and Technology ["
                        + genericPakcage.getTechnology() + "]. Reason  [" + e.getMessage() + "]";
            }
            // TODO Auto-generated catch block
            new MajorLogMsg(this, message, e);
            throw new HomeException(message, e);
        }
    }


    protected void compareField(Object oldValue, Object newValue, String propertyName, StringBuilder buffer)
    {
        if (!SafetyUtil.safeEquals(oldValue, newValue))
        {
            buffer.append(propertyName);
            buffer.append(" updated from [");
            buffer.append(oldValue);
            buffer.append("] to [");
            buffer.append(newValue);
            buffer.append("]\n");
        }
    }


    private void gsmPackageHisotry(Context ctx, GSMPackage newBean) throws HomeException
    {
        GSMPackage oldBean = OldGSMPackageLookupHome.getOldGSMPackage(ctx);
        // append history
        if (oldBean != null && !(oldBean.equals(newBean)))
        {
            StringBuilder detail = new StringBuilder();
            FacetMgr fmgr = (FacetMgr) ctx.get(FacetMgr.class);
            MessageMgr mmgr = new MessageMgr(ctx, fmgr.getClass(ctx, Package.class, WebControl.class));
            compareField(oldBean.getPackageGroup(), newBean.getPackageGroup(), mmgr.get(
                    "GSMPackage.packageGroup.Label", GSMPackageXInfo.PACKAGE_GROUP.getLabel(ctx)), detail);
            compareField(oldBean.getIMSI(), newBean.getIMSI(), mmgr.get("GSMPackage.IMSI.Label",
                    GSMPackageXInfo.IMSI.getLabel(ctx)), detail);
            compareField(oldBean.getSerialNo(), newBean.getSerialNo(), mmgr.get("GSMPackage.serialNo.Label",
                    GSMPackageXInfo.SERIAL_NO.getLabel(ctx)), detail);
            compareField(oldBean.getPIN1(), newBean.getPIN1(), mmgr.get("GSMPackage.PIN1.Label",
                    GSMPackageXInfo.PIN1.getLabel(ctx)), detail);
            compareField(oldBean.getPUK1(), newBean.getPUK1(), mmgr.get("GSMPackage.PUK1.Label",
                    GSMPackageXInfo.PUK1.getLabel(ctx)), detail);
            compareField(oldBean.getPIN2(), newBean.getPIN2(), mmgr.get("GSMPackage.PIN2.Label",
                    GSMPackageXInfo.PIN2.getLabel(ctx)), detail);
            compareField(oldBean.getPUK2(), newBean.getPUK2(), mmgr.get("GSMPackage.PUK2.Label",
                    GSMPackageXInfo.PUK2.getLabel(ctx)), detail);
            compareField(oldBean.getDealer(), newBean.getDealer(), mmgr.get("Package.dealer.Label",
                    GSMPackageXInfo.DEALER.getLabel(ctx)), detail);
            compareField(oldBean.getState(), newBean.getState(), mmgr.get("GSMPackage.state.Label",
                    GSMPackageXInfo.STATE.getLabel(ctx)), detail);
            compareField(oldBean.getServiceLogin1(), newBean.getServiceLogin1(), mmgr.get(
                    "GSMPackage.serviceLogin1.Label", GSMPackageXInfo.SERVICE_LOGIN1.getLabel(ctx)), detail);
            compareField(oldBean.getServicePassword1(), newBean.getServicePassword1(), mmgr.get(
                    "GSMPackage.servicePassword1.Label", GSMPackageXInfo.SERVICE_PASSWORD1.getLabel(ctx)), detail);
            compareField(oldBean.getServiceLogin2(), newBean.getServiceLogin2(), mmgr.get(
                    "GSMPackage.serviceLogin2.Label", GSMPackageXInfo.SERVICE_LOGIN2.getLabel(ctx)), detail);
            compareField(oldBean.getServicePassword2(), newBean.getServicePassword2(), mmgr.get(
                    "GSMPackage.servicePassword2.Label", GSMPackageXInfo.SERVICE_PASSWORD2.getLabel(ctx)), detail);
            if (detail.length() > 0)
            {
                /*NumberMgmtHistory history = (NumberMgmtHistory) appendHistory(ctx, newBean.getPackId(),
                        getHistoryEventSupport(ctx).getFeatureModificationEvent(ctx), detail.toString());
                */
               appendHistory(ctx, newBean.getPackId(),
                        getHistoryEventSupport(ctx).getFeatureModificationEvent(ctx), detail.toString());
            }
        }
    }


private void tdmaPackageHisotry(Context ctx, TDMAPackage newBean) throws HomeException
    {
        TDMAPackage oldBean = OldTDMAPackageLookupHome.getOldTDMAPackage(ctx);
        // append history
        if (oldBean != null && !(oldBean.equals(newBean)))
        {
            StringBuilder detail = new StringBuilder();
            FacetMgr fmgr = (FacetMgr) ctx.get(FacetMgr.class);
            MessageMgr mmgr = new MessageMgr(ctx, fmgr.getClass(ctx, Package.class, WebControl.class));
            compareField(oldBean.getPackageGroup(), newBean.getPackageGroup(), mmgr.get("Package.packageGroup.Label",
                    TDMAPackageXInfo.PACKAGE_GROUP.getLabel(ctx)), detail);
            compareField(oldBean.getMin(), newBean.getMin(), mmgr.get("TDMAPackage.MIN.Label",
                    TDMAPackageXInfo.MIN.getLabel(ctx)), detail);
            compareField(oldBean.getESN(), newBean.getESN(), mmgr.get("TDMAPackage.ESN.Label",
                    TDMAPackageXInfo.ESN.getLabel(ctx)), detail);
            compareField(oldBean.getSerialNo(), newBean.getSerialNo(), mmgr.get("Package.serialNo.Label",
                    TDMAPackageXInfo.SERIAL_NO.getLabel(ctx)), detail);
            compareField(oldBean.getAuthKey(), newBean.getAuthKey(), mmgr.get("TDMAPackage.AUTHKEY.Label",
                    TDMAPackageXInfo.AUTH_KEY.getLabel(ctx)), detail);
            compareField(oldBean.getMassSubsidyKey(), newBean.getMassSubsidyKey(), mmgr.get(
                    "TDMAPackage.MASSSUBSIDYKEY.Label", TDMAPackageXInfo.MASS_SUBSIDY_KEY.getLabel(ctx)), detail);
            compareField(oldBean.getDealer(), newBean.getDealer(), mmgr.get("TDMAPackage.dealer.Label",
                    TDMAPackageXInfo.DEALER.getLabel(ctx)), detail);
            compareField(oldBean.getState(), newBean.getState(), mmgr.get("TDMAPackage.state.Label",
                    TDMAPackageXInfo.STATE.getLabel(ctx)), detail);
            compareField(oldBean.getServiceLogin1(), newBean.getServiceLogin1(), mmgr.get(
                    "TDMAPackage.serviceLogin1.Label", TDMAPackageXInfo.SERVICE_LOGIN1.getLabel(ctx)), detail);
            compareField(oldBean.getServicePassword1(), newBean.getServicePassword1(), mmgr.get(
                    "TDMAPackage.servicePassword1.Label", TDMAPackageXInfo.SERVICE_PASSWORD1.getLabel(ctx)), detail);
            compareField(oldBean.getServiceLogin2(), newBean.getServiceLogin2(), mmgr.get(
                    "TDMAPackage.serviceLogin2.Label", TDMAPackageXInfo.SERVICE_LOGIN2.getLabel(ctx)), detail);
            compareField(oldBean.getServicePassword2(), newBean.getServicePassword2(), mmgr.get(
                    "TDMAPackage.servicePassword2.Label", TDMAPackageXInfo.SERVICE_PASSWORD2.getLabel(ctx)), detail);
            compareField(oldBean.getExternalMSID(), newBean.getExternalMSID(), mmgr.get(
                    "TDMAPackage.externalMSID.Label", TDMAPackageXInfo.EXTERNAL_MSID.getLabel(ctx)), detail);
            if (detail.length() > 0)
            {
                /*
                 * NumberMgmtHistory history = (NumberMgmtHistory) appendHistory(ctx,
                 * newBean.getPackId(),
                 * getHistoryEventSupport(ctx).getFeatureModificationEvent(ctx),
                 * detail.toString());
                 */
                appendTDMAHistory(ctx, newBean.getPackId(), getHistoryEventSupport(ctx).getFeatureModificationEvent(ctx), newBean,
                        detail.toString());
            }
        }
    }


    private void vsatPackageHisotry(Context ctx, VSATPackage newBean) throws HomeException
    {
        VSATPackage oldBean = OldVSATPackageLookupHome.getOldVSATPackage(ctx);
        // append history
        if (oldBean != null && !(oldBean.equals(newBean)))
        {
            StringBuilder detail = new StringBuilder();
            FacetMgr fmgr = (FacetMgr) ctx.get(FacetMgr.class);
            MessageMgr mmgr = new MessageMgr(ctx, fmgr.getClass(ctx, Package.class, WebControl.class));
            compareField(oldBean.getPackageGroup(), newBean.getPackageGroup(), mmgr.get("Package.packageGroup.Label",
                    VSATPackageXInfo.PACKAGE_GROUP.getLabel(ctx)), detail);
            compareField(oldBean.getVsatId(), newBean.getVsatId(), mmgr.get("VSATPackage.vsatId.Label",
                    VSATPackageXInfo.VSAT_ID.getColumnLabel(ctx)), detail);
            compareField(oldBean.getChannel(), newBean.getChannel(), mmgr.get("VSATPackage.channel.Label",
                    VSATPackageXInfo.CHANNEL.getLabel(ctx)), detail);
            compareField(oldBean.getPort(), newBean.getPort(), mmgr.get("Package.port.Label",
                    VSATPackageXInfo.PORT.getLabel(ctx)), detail);
            if (detail.length() > 0)
            {
                /*
                 * NumberMgmtHistory history = (NumberMgmtHistory) appendHistory(ctx,
                 * newBean.getPackId(),
                 * getHistoryEventSupport(ctx).getFeatureModificationEvent(ctx),
                 * detail.toString());
                 */
                appendHistory(ctx, newBean.getPackId(), getHistoryEventSupport(ctx).getFeatureModificationEvent(ctx),
                        detail.toString());
            }
        }
    }


    @Override
    public Object processPackage(Context ctx, GSMPackage card) throws PackageProcessingException
    {
        // TODO Auto-generated method stub
        try
        {
            gsmPackageHisotry(ctx, card);
            return card;
        }
        catch (HomeException e)
        {
            throw new PackageProcessingException("Could not form Package History. Reason [" + e.getMessage() + "]", e);
        }
    }


    @Override
    public Object processPackage(Context ctx, TDMAPackage card) throws PackageProcessingException
    {
        try
        {
            tdmaPackageHisotry(ctx, card);
            return card;
        }
        catch (HomeException e)
        {
            throw new PackageProcessingException("Could not form Package History. Reason [" + e.getMessage() + "]", e);
        }
    }


    @Override
    public Object processPackage(Context ctx, VSATPackage card) throws PackageProcessingException
    {
        try
        {
            vsatPackageHisotry(ctx, card);
            return card;
        }
        catch (HomeException e)
        {
            throw new PackageProcessingException("Could not form Package History. Reason [" + e.getMessage() + "]", e);
        }
    }
}
