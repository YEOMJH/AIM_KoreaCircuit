/**
 * VXG_406_SAP_MASKProductionDataPush_pttBindingQSServiceCallbackHandler.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.7.9  Built on : Nov 16, 2018 (12:05:37 GMT)
 */
package kr.co.aim.messolution.extended.webinterface.webservice;


/**
 *  VXG_406_SAP_MASKProductionDataPush_pttBindingQSServiceCallbackHandler Callback class, Users can extend this class and implement
 *  their own receiveResult and receiveError methods.
 */
public abstract class VXG_406_SAP_MASKProductionDataPush_pttBindingQSServiceCallbackHandler {
    protected Object clientData;

    /**
     * User can pass in any object that needs to be accessed once the NonBlocking
     * Web service call is finished and appropriate method of this CallBack is called.
     * @param clientData Object mechanism by which the user can pass in user data
     * that will be avilable at the time this callback is called.
     */
    public VXG_406_SAP_MASKProductionDataPush_pttBindingQSServiceCallbackHandler(
        Object clientData) {
        this.clientData = clientData;
    }

    /**
     * Please use this constructor if you don't want to set any clientData
     */
    public VXG_406_SAP_MASKProductionDataPush_pttBindingQSServiceCallbackHandler() {
        this.clientData = null;
    }

    /**
     * Get the client data
     */
    public Object getClientData() {
        return clientData;
    }

    /**
     * auto generated Axis2 call back method for vXG_406_SAP_MASKProductionDataPush method
     * override this method for handling normal response from vXG_406_SAP_MASKProductionDataPush operation
     */
    public void receiveResultvXG_406_SAP_MASKProductionDataPush(
        kr.co.aim.messolution.extended.webinterface.webservice.VXG_406_SAP_MASKProductionDataPush_pttBindingQSServiceStub.Response result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from vXG_406_SAP_MASKProductionDataPush operation
     */
    public void receiveErrorvXG_406_SAP_MASKProductionDataPush(
        java.lang.Exception e) {
    }
}
