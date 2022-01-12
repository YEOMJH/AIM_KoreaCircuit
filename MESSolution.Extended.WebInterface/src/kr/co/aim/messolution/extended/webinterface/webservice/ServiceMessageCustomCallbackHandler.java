/**
 * ServiceMessageCustomCallbackHandler.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.7.9  Built on : Nov 16, 2018 (12:05:37 GMT)
 */
package kr.co.aim.messolution.extended.webinterface.webservice;


/**
 *  ServiceMessageCustomCallbackHandler Callback class, Users can extend this class and implement
 *  their own receiveResult and receiveError methods.
 */
public abstract class ServiceMessageCustomCallbackHandler {
    protected Object clientData;

    /**
     * User can pass in any object that needs to be accessed once the NonBlocking
     * Web service call is finished and appropriate method of this CallBack is called.
     * @param clientData Object mechanism by which the user can pass in user data
     * that will be avilable at the time this callback is called.
     */
    public ServiceMessageCustomCallbackHandler(Object clientData) {
        this.clientData = clientData;
    }

    /**
     * Please use this constructor if you don't want to set any clientData
     */
    public ServiceMessageCustomCallbackHandler() {
        this.clientData = null;
    }

    /**
     * Get the client data
     */
    public Object getClientData() {
        return clientData;
    }

    /**
     * auto generated Axis2 call back method for deleteCustomMessage method
     * override this method for handling normal response from deleteCustomMessage operation
     */
    public void receiveResultdeleteCustomMessage(
        kr.co.aim.messolution.extended.webinterface.webservice.ServiceMessageCustomStub.DeleteCustomMessageResponse result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from deleteCustomMessage operation
     */
    public void receiveErrordeleteCustomMessage(java.lang.Exception e) {
    }

    /**
     * auto generated Axis2 call back method for alterCustomMessageComplex method
     * override this method for handling normal response from alterCustomMessageComplex operation
     */
    public void receiveResultalterCustomMessageComplex(
        kr.co.aim.messolution.extended.webinterface.webservice.ServiceMessageCustomStub.AlterCustomMessageComplexResponse result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from alterCustomMessageComplex operation
     */
    public void receiveErroralterCustomMessageComplex(java.lang.Exception e) {
    }

    /**
     * auto generated Axis2 call back method for sendCustomMessage_Login method
     * override this method for handling normal response from sendCustomMessage_Login operation
     */
    public void receiveResultsendCustomMessage_Login(
        kr.co.aim.messolution.extended.webinterface.webservice.ServiceMessageCustomStub.SendCustomMessage_LoginResponse result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from sendCustomMessage_Login operation
     */
    public void receiveErrorsendCustomMessage_Login(java.lang.Exception e) {
    }

    /**
     * auto generated Axis2 call back method for sendCustomMessage method
     * override this method for handling normal response from sendCustomMessage operation
     */
    public void receiveResultsendCustomMessage(
        kr.co.aim.messolution.extended.webinterface.webservice.ServiceMessageCustomStub.SendCustomMessageResponse result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from sendCustomMessage operation
     */
    public void receiveErrorsendCustomMessage(java.lang.Exception e) {
    }

    /**
     * auto generated Axis2 call back method for deleteCustomMessageSingle method
     * override this method for handling normal response from deleteCustomMessageSingle operation
     */
    public void receiveResultdeleteCustomMessageSingle(
        kr.co.aim.messolution.extended.webinterface.webservice.ServiceMessageCustomStub.DeleteCustomMessageSingleResponse result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from deleteCustomMessageSingle operation
     */
    public void receiveErrordeleteCustomMessageSingle(java.lang.Exception e) {
    }

    /**
     * auto generated Axis2 call back method for deleteCustomMessageComplex method
     * override this method for handling normal response from deleteCustomMessageComplex operation
     */
    public void receiveResultdeleteCustomMessageComplex(
        kr.co.aim.messolution.extended.webinterface.webservice.ServiceMessageCustomStub.DeleteCustomMessageComplexResponse result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from deleteCustomMessageComplex operation
     */
    public void receiveErrordeleteCustomMessageComplex(java.lang.Exception e) {
    }

    /**
     * auto generated Axis2 call back method for sendCustomMessageSingle method
     * override this method for handling normal response from sendCustomMessageSingle operation
     */
    public void receiveResultsendCustomMessageSingle(
        kr.co.aim.messolution.extended.webinterface.webservice.ServiceMessageCustomStub.SendCustomMessageSingleResponse result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from sendCustomMessageSingle operation
     */
    public void receiveErrorsendCustomMessageSingle(java.lang.Exception e) {
    }

    /**
     * auto generated Axis2 call back method for sendCustomMessage1 method
     * override this method for handling normal response from sendCustomMessage1 operation
     */
    public void receiveResultsendCustomMessage1(
        kr.co.aim.messolution.extended.webinterface.webservice.ServiceMessageCustomStub.SendCustomMessage1Response result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from sendCustomMessage1 operation
     */
    public void receiveErrorsendCustomMessage1(java.lang.Exception e) {
    }

    /**
     * auto generated Axis2 call back method for alterCustomMessageSingle method
     * override this method for handling normal response from alterCustomMessageSingle operation
     */
    public void receiveResultalterCustomMessageSingle(
        kr.co.aim.messolution.extended.webinterface.webservice.ServiceMessageCustomStub.AlterCustomMessageSingleResponse result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from alterCustomMessageSingle operation
     */
    public void receiveErroralterCustomMessageSingle(java.lang.Exception e) {
    }

    /**
     * auto generated Axis2 call back method for sendCustomMessageComplex method
     * override this method for handling normal response from sendCustomMessageComplex operation
     */
    public void receiveResultsendCustomMessageComplex(
        kr.co.aim.messolution.extended.webinterface.webservice.ServiceMessageCustomStub.SendCustomMessageComplexResponse result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from sendCustomMessageComplex operation
     */
    public void receiveErrorsendCustomMessageComplex(java.lang.Exception e) {
    }

    /**
     * auto generated Axis2 call back method for sendCustomMessage_WorkCode method
     * override this method for handling normal response from sendCustomMessage_WorkCode operation
     */
    public void receiveResultsendCustomMessage_WorkCode(
        kr.co.aim.messolution.extended.webinterface.webservice.ServiceMessageCustomStub.SendCustomMessage_WorkCodeResponse result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from sendCustomMessage_WorkCode operation
     */
    public void receiveErrorsendCustomMessage_WorkCode(java.lang.Exception e) {
    }

    /**
     * auto generated Axis2 call back method for sendCustomMessage_LastName method
     * override this method for handling normal response from sendCustomMessage_LastName operation
     */
    public void receiveResultsendCustomMessage_LastName(
        kr.co.aim.messolution.extended.webinterface.webservice.ServiceMessageCustomStub.SendCustomMessage_LastNameResponse result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from sendCustomMessage_LastName operation
     */
    public void receiveErrorsendCustomMessage_LastName(java.lang.Exception e) {
    }

    /**
     * auto generated Axis2 call back method for alterCustomMessage method
     * override this method for handling normal response from alterCustomMessage operation
     */
    public void receiveResultalterCustomMessage(
        kr.co.aim.messolution.extended.webinterface.webservice.ServiceMessageCustomStub.AlterCustomMessageResponse result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from alterCustomMessage operation
     */
    public void receiveErroralterCustomMessage(java.lang.Exception e) {
    }
}
