/**
 * QueryFaultException.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.4  Built on : Dec 28, 2015 (10:03:39 GMT)
 */
package kr.co.aim.messolution.extended.webinterface.webservice;

public class QueryFaultException extends java.lang.Exception {
    private static final long serialVersionUID = 1602804806163L;
    private kr.co.aim.messolution.extended.webinterface.webservice.VXG_370_SAP_CloseOrder_pttBindingQSServiceStub.QueryFault faultMessage;

    public QueryFaultException() {
        super("QueryFaultException");
    }

    public QueryFaultException(java.lang.String s) {
        super(s);
    }

    public QueryFaultException(java.lang.String s, java.lang.Throwable ex) {
        super(s, ex);
    }

    public QueryFaultException(java.lang.Throwable cause) {
        super(cause);
    }

    public void setFaultMessage(
        kr.co.aim.messolution.extended.webinterface.webservice.VXG_370_SAP_CloseOrder_pttBindingQSServiceStub.QueryFault msg) {
        faultMessage = msg;
    }

    public kr.co.aim.messolution.extended.webinterface.webservice.VXG_370_SAP_CloseOrder_pttBindingQSServiceStub.QueryFault getFaultMessage() {
        return faultMessage;
    }
}
