
package kr.co.aim.messolution.extended.object.management.data;

import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.processflow.management.iter.PFIValueSetter;
import kr.co.aim.greentrack.processflow.management.iter.ProcessFlowIterator;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.ProductSpec;
import kr.co.aim.greentrack.product.management.data.ProductSpecKey;

public class MaskPFIValueSetter //extends PFIValueSetter<TestData, ProductSpec, ProcessFlow>
{
//	public MaskPFIValueSetter(ProcessFlowIterator aProcessFlowIterator, TestData aOldData, TestData aNewData)
//			throws NotFoundSignal, FrameworkErrorSignal
//	{
//		super(aProcessFlowIterator, aOldData, aNewData);
//	}
//
//	@Override
//	protected void setNamePreFix()
//	{
//		super.namePreFix = "TestData";
//	}
//
//	@Override
//	protected void setProductSpec(TestData data)
//	{
//		setProductSpec(data.getFactoryName(), data.getProductSpecName(), data.getProductSpecVersion());
//
//	}
//
//	private void setProductSpec(String aFactoryName, String aProductSpecName, String aProductSpecVersion)
//			throws NotFoundSignal, FrameworkErrorSignal
//	{
//		ProductSpecKey productSpecKey = new ProductSpecKey();
//		productSpecKey.setFactoryName(aFactoryName);
//		productSpecKey.setProductSpecName(aProductSpecName);
//		productSpecKey.setProductSpecVersion(aProductSpecVersion);
//
//		this.productSpec = ProductServiceProxy.getProductSpecService().selectByKey(productSpecKey);
//	}
}
