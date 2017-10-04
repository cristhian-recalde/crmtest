package com.trilogy.app.crm.xhome;

import java.io.Serializable;

public class MsisdnOwnershipReAcquireCmd
    implements Serializable
{
    public MsisdnOwnershipReAcquireCmd()
    {
    }

    public MsisdnOwnershipReAcquireCmd(String msisdn)
    {
        msisdn_ = msisdn;
    }

    public String getMsisdn()
    {
        return msisdn_;
    }

    private String msisdn_ = "";
    private static final long serialVersionUID = 20634587213498234L;
}