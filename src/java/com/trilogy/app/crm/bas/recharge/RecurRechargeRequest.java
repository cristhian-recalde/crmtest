package com.trilogy.app.crm.bas.recharge;

import com.trilogy.app.crm.bean.ChargeOptionEnum;
import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.Transaction;
import com.trilogy.app.crm.bean.core.ServiceFee2;

/**
 * @author     ltse
 */
public class RecurRechargeRequest
{
   
   /**
    *  Description of the Field
    */
   public final static int SUCCESS = 0;
   
   /**
    *  Description of the Field
    */
   public final static int SERVICE_NOT_FOUND = 1;
   
   /**
    *  Description of the Field
    */
   public final static int FAIL_QUERY_SERVICE_TABLE = 2;
   
   /**
    *  Description of the Field
    */
   public final static int FAIL_QUERY_TRANSACTION_TABLE = 3;
   
   /**
    *  Description of the Field
    */
   public final static int FAIL_CREATE_TRANSACTION = 4;
   
   /**
    *  Description of the Field
    */
   public final static int FAIL_OTHERS = 5;


   /**
	 *  Description of the Field
	 */
   // DZ: Add a new field due to F.S changed
   protected int ocgResult = -1;   // -1 for Not Apply

   /**
    *  Description of the Field
    */
   protected int result_ = FAIL_OTHERS;
   
   /**
    *  Description of the Field
    */
   protected Transaction resultAdjustment_ = null;

   /**
    *  Description of the Field
    */
   protected boolean duplicateRecordFound_ = false;
   
   /**
    *  Description of the Field
    */
   protected Subscriber sub_ = null;
   
   /**
    *  Description of the Field
    */
   protected ServiceFee2 serviceFee_ = null;
   
   /**
    *  Description of the Field
    */
   protected Service service_ = null;
   
   /**
    *  Description of the Field
    */
   protected String requestString_ = null;
   
   /**
    *  Description of the Field
    */
   protected ChargeOptionEnum chargeOption_ = null;
   
   /**
    *  Description of the Field
    */
   protected double rate_ = 1.00;
   
  
   public RecurRechargeRequest()
   {
       
   }

   /**
    *  Constructor for the RecurRechargeRequest object
    *
    *@param  sub           Description of the Parameter
    *@param  serviceFee    Description of the Parameter
    *@param  chargeOption  Description of the Parameter
    *@param  rate          Description of the Parameter
    */
   public RecurRechargeRequest(Subscriber sub, ServiceFee2 serviceFee, ChargeOptionEnum chargeOption, double rate)
   {
      setSub(sub);
      setServiceFee(serviceFee);
      setChargeOption(chargeOption);
      setRate(rate);

      setRequestString("Subscriber: " + sub.getId() + ", service:" + serviceFee.getServiceId());
   }


   /**
    *  Returns the duplicateRecordFound.
    *
    *@return    boolean
    */
   public boolean isDuplicateRecordFound()
   {
      return duplicateRecordFound_;
   }


   /**
    *  Returns the result.
    *
    *@return    int
    */
   public int getResult()
   {
      return result_;
   }


   /**
    *  Sets the duplicateRecordFound.
    *
    *@param  duplicateRecordFound  The duplicateRecordFound to set
    */
   public void setDuplicateRecordFound(boolean duplicateRecordFound)
   {
      this.duplicateRecordFound_ = duplicateRecordFound;
   }


   /**
    *  Sets the result.
    *
    *@param  result  The result to set
    */
   public void setResult(int result)
   {
      this.result_ = result;
   }


   /**
    *  Returns the service.
    *
    *@return    Service
    */
   public Service getService()
   {
      return service_;
   }


   /**
    *  Returns the serviceFee.
    *
    *@return    ServiceFee2
    */
   public ServiceFee2 getServiceFee()
   {
      return serviceFee_;
   }


   /**
    *  Returns the sub.
    *
    *@return    Subscriber
    */
   public Subscriber getSub()
   {
      return sub_;
   }


   /**
    *  Sets the service.
    *
    *@param  service  The service to set
    */
   public void setService(Service service)
   {
      this.service_ = service;
   }


   /**
    *  Sets the serviceFee.
    *
    *@param  serviceFee  The serviceFee to set
    */
   public void setServiceFee(ServiceFee2 serviceFee)
   {
      this.serviceFee_ = serviceFee;
   }


   /**
    *  Sets the sub.
    *
    *@param  sub  The sub to set
    */
   public void setSub(Subscriber sub)
   {
      this.sub_ = sub;
   }


   /**
    *  Returns the requestString.
    *
    *@return    String
    */
   public String getRequestString()
   {
      return requestString_;
   }


   /**
    *  Sets the requestString.
    *
    *@param  requestString  The requestString to set
    */
   public void setRequestString(String requestString)
   {
      this.requestString_ = requestString;
   }


   /**
    *  Returns the resultAdjustment.
    *
    *@return    Transaction
    */
   public Transaction getResultAdjustment()
   {
      return resultAdjustment_;
   }


   /**
    *  Sets the resultAdjustment_.
    *
    *@param  resultAdjustment  The resultAdjustment_ to set
    */
   public void setResultAdjustment(Transaction resultAdjustment)
   {
      this.resultAdjustment_ = resultAdjustment;
   }


   /**
    *@return    Returns the chargeOption.
    */
   public ChargeOptionEnum getChargeOption()
   {
      return chargeOption_;
   }


   /**
    *@param  chargeOption  The chargeOption to set.
    */
   public void setChargeOption(ChargeOptionEnum chargeOption)
   {
      this.chargeOption_ = chargeOption;
   }


   /**
    *@return    Returns the rate_.
    */
   public double getRate()
   {
      return rate_;
   }


   /**
    *@param  rate_  The rate_ to set.
    */
   public void setRate(double rate_)
   {
      this.rate_ = rate_;
   }

/**
 * @return int
 */
public int getOcgResult()
{
    return ocgResult;
}

/**
 * Sets the ocgResult.
 * @param ocgResult The ocgResult to set
 */
public void setOcgResult(int ocgResult)
{
    this.ocgResult = ocgResult;
}

}

