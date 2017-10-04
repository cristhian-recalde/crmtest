package com.trilogy.app.crm.bean;


public class ChargerHandlerConfigID
{

    public ChargerHandlerConfigID()
    { 
    }
    
    public ChargerHandlerConfigID(short subType, int action, int chargableItem)
    { 
        this.setSubType( subType);
        this.setAction( action);
        this.setChargableItem (chargableItem);
    }

	public short getSubType() {
		return subType_;
	}

	public void setSubType(short subType_) {
		this.subType_ = subType_;
	}

	public int getAction() {
		return action_;
	}

	public void setAction(int action_) {
		this.action_ = action_;
	}

	public int getChargableItem() {
		return chargableItem_;
	}

	public void setChargableItem(int chargableItem_) {
		this.chargableItem_ = chargableItem_;
	}    

    protected short            subType_;
    protected int              action_;  
    protected int              chargableItem_;



    
}
