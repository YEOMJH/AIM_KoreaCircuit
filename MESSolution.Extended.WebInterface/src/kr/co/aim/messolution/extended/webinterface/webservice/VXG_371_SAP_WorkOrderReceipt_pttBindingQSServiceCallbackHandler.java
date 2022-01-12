/**
 * VXG_371_SAP_WorkOrderReceipt_pttBindingQSServiceCallbackHandler.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.4  Built on : Dec 28, 2015 (10:03:39 GMT)
 */
package kr.co.aim.messolution.extended.webinterface.webservice;


/**
 *  VXG_371_SAP_WorkOrderReceipt_pttBindingQSServiceCallbackHandler Callback class, Users can extend this class and implement
 *  their own receiveResult and receiveError methods.
 */
public abstract class VXG_371_SAP_WorkOrderReceipt_pttBindingQSServiceCallbackHandler {
    protected Object clientData;

    /**
     * User can pass in any object that needs to be accessed once the NonBlocking
     * Web service call is finished and appropriate method of this CallBack is called.
     * @param clientData Object mechanism by which the user can pass in user data
     * that will be avilable at the time this callback is called.
     */
    public VXG_371_SAP_WorkOrderReceipt_pttBindingQSServiceCallbackHandler(
        Object clientData) {
        this.clientData = clientData;
    }

    /**
     * Please use this constructor if you don't want to set any clientData
     */
    public VXG_371_SAP_WorkOrderReceipt_pttBindingQSServiceCallbackHandler() {
        this.clientData = null;
    }

    /**
     * Get the client data
     */
    public Object getClientData() {
        return clientData;
    }

    /**
     * auto generated Axis2 call back method for vXG_371_SAP_WorkOrderReceipt method
     * override this method for handling normal response from vXG_371_SAP_WorkOrderReceipt operation
     */
    public void receiveResultvXG_371_SAP_WorkOrderReceipt(
        kr.co.aim.messolution.extended.webinterface.webservice.VXG_371_SAP_WorkOrderReceipt_pttBindingQSServiceStub.Response result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from vXG_371_SAP_WorkOrderReceipt operation
     */
    public void receiveErrorvXG_371_SAP_WorkOrderReceipt(java.lang.Exception e) {
    }
}
