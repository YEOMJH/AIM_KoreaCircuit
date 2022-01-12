package kr.co.aim.messolution.product.service;

import kr.co.aim.greentrack.product.management.info.ext.ProductPGS;

public class ProductPP extends ProductPGS {
	
		private  String productRequestName;
		
		private String carrierName;
		
		public void setProductRequestName(String productRequestName)
		{
			this.productRequestName = productRequestName;
		}
		
		public String getProductRequestName()
		{
			return productRequestName;
		}
		
		
		public void setCarrierName(String carrierName)
		{
			this.carrierName = carrierName;
		}
		
		public String getCarrierName()
		{
			return carrierName;
		}
}
