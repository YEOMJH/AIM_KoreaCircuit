package kr.co.aim.messolution.fmb.service;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.greenflow.expression.ExtendedFunction;
import kr.co.aim.greenframe.util.xml.JdomUtils;

import org.jaxen.Context;
import org.jaxen.Function;
import org.jaxen.FunctionCallException;
import org.jaxen.XPathFunctionContext;
import org.jdom.Element;
import org.jdom.xpath.XPath;

public class FMBExpression extends ExtendedFunction{

	public FMBExpression() {
		super();
		
	}
	
	@Override
	public void registerFunctions(XPathFunctionContext functionContext) {
		((XPathFunctionContext) functionContext).registerFunction(null, "getReturnCode", new GetReturnCodetFunction());
		((XPathFunctionContext) functionContext).registerFunction(null, "getXmlData", new GetNodeTextFunction());
		((XPathFunctionContext) functionContext).registerFunction(null, "getChildNodesText", new GetChildNodesTextFunction());
	}
	
	 public int getIntValue(Object obj)
		{
			int value;
			if(obj instanceof Number)
				value = ((Number)obj).intValue();
			else
				value = Integer.parseInt(obj.toString());
			return value;
		}
	 
	private class GetXmlData implements Function 
	{		
		
		public Object call(Context context, List args) throws RuntimeException 
		{			
			String variableName = String.valueOf(args.get(0));
			String locationPath = String.valueOf(args.get(1));
			
			org.jdom.Document jdom = (org.jdom.Document)getBpelExecutionContext().getVariableData().getValue(variableName);
			try {
				if(args.size() > 2)
				{
					int index = getIntValue(args.get(2));
					
					String reply = JdomUtils.getNodeText(jdom, locationPath, index);
					
					System.out.println("getXMLData Resturn Value = " + reply);
					
					return reply;
				}
				else
				{
					String reply = JdomUtils.getNodeText(jdom, locationPath);
					//System.out.println(locationPath + " = " + reply);
					return reply;
				}
			} catch (Exception ex) {
				return "";
			}
		}
	}



	private class addXmlElement implements Function
		{
			public Object call(Context xpath, List args) throws RuntimeException
			{
				String variableName = String.valueOf(args.get(0));
				String locationPath = String.valueOf(args.get(1));
				String elementName = String.valueOf(args.get(2));
				String value = String.valueOf(args.get(3));
				org.jdom.Document jdom = (org.jdom.Document)getBpelExecutionContext().getVariableData().getValue(variableName);
				JdomUtils.addElement(jdom, locationPath, elementName, value);
				return null;
			}
		}
	private class GetReturnCodetFunction implements Function
	{
		public Object call(Context xpath, List list) throws FunctionCallException
		{
			try
			{
				Object obj = list.get(0);
				Object target = getBpelExecutionContext().getVariableData().getValue((String)obj);
				if(target == null) {
					return null;
				}
				String path = "//return/returncode";
				Element node = (Element) XPath.selectSingleNode(target, path);
				return node.getText();
			}
			catch ( Exception ex )
			{
				throw new FunctionCallException( ex );
			}
		}
	}
	private class GetNodeTextFunction implements Function
	{
		public Object call(Context xpath, List list) throws FunctionCallException
		{
			try
			{
				Object obj = list.get(0);
				Object target = getBpelExecutionContext().getVariableData().getValue((String)obj);
				if(target == null) {
					return null;
				}
				String path = (String) list.get(1);
				Element node = (Element) XPath.selectSingleNode(target, path);
				if(node == null) {
					return "";
				}
				return node.getText();
			}
			catch ( Exception ex )
			{
				throw new FunctionCallException( ex );
			}
		}
	}
	private class GetChildNodesTextFunction implements Function
	{
		public Object call(Context xpath, List list) throws FunctionCallException
		{
			try
			{
				Object obj = list.get(0);
				
				String path = (String) list.get(1);
				
				if ( obj instanceof String )
					obj = getBpelExecutionContext().getVariableData().getValue((String)obj);
				
				Element node = (Element) XPath.selectSingleNode(obj, path);
				List<Element> children = node.getChildren();
				List<String> textList = new ArrayList<String>(); 
				for (Element element : children) {
					textList.add(element.getTextTrim());
				}
				
				return textList;
			}
			catch ( Exception ex )
			{
				throw new FunctionCallException( ex );
			}
		}
	}
}
