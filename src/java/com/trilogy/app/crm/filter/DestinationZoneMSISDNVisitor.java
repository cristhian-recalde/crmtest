package com.trilogy.app.crm.filter;

import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.app.crm.bean.DestinationZone;
import com.trilogy.app.crm.bean.ZonePrefix;
import com.trilogy.app.crm.bean.MsisdnZonePrefix;
import com.trilogy.app.crm.bean.MsisdnPrefix;

import java.util.List;
import java.util.Iterator;

/**
 * This visitor finds a zone that contains a msisdn
 *
 * @author psperneac
 * @since Apr 29, 2005 5:00:35 PM
 */
public class DestinationZoneMSISDNVisitor implements Visitor
{
    protected String msisdn;
    protected MsisdnZonePrefix saved;

    public DestinationZoneMSISDNVisitor(String msisdn)
    {
        setMsisdn(msisdn);
    }

    public void visit(Context ctx, Object obj) throws AgentException, AbortVisitException
    {
        MsisdnZonePrefix zone=(MsisdnZonePrefix) obj;
        List prefixes=zone.getPrefixes(ctx);

        for(Iterator i=prefixes.iterator();i.hasNext();)
        {
            MsisdnPrefix prefix=(MsisdnPrefix) i.next();

            if(getMsisdn().startsWith(prefix.getPrefix()))
            {
                setSaved(zone);
                throw new AbortVisitException();
            }
        }
    }

    public String getMsisdn()
    {
        return msisdn;
    }

    public void setMsisdn(String msisdn)
    {
        this.msisdn = msisdn;
    }

    public MsisdnZonePrefix getSaved()
    {
        return saved;
    }

    public void setSaved(MsisdnZonePrefix saved)
    {
        this.saved = saved;
    }
}
