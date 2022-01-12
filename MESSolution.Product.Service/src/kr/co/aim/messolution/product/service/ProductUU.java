package kr.co.aim.messolution.product.service;

import kr.co.aim.greentrack.product.management.info.ext.ProductU;

public class ProductUU extends ProductU {
		
	private  String productRequestName;
	
	public void setProductRequestName(String productRequestName)
	{
		this.productRequestName = productRequestName;
	}
	
	public String getProductRequestName()
	{
		return productRequestName;
	}
}
