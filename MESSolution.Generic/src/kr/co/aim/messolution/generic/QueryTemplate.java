package kr.co.aim.messolution.generic;

import java.util.List;
import java.util.Map;

import kr.co.aim.greentrack.generic.exception.DuplicateNameSignal;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.orm.SqlMesTemplate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.osgi.context.BundleContextAware;

public class QueryTemplate extends SqlMesTemplate implements ApplicationContextAware, BundleContextAware {
	
	Log logger = LogFactory.getLog(QueryTemplate.class.getSimpleName());
	
	@Override
	public void setBundleContext(BundleContext arg0) {
		logger.info("Registered MES sql template in bundle");
	}

	@Override
	public void setApplicationContext(ApplicationContext arg0)
			throws BeansException {
		logger.info("Registered MES sql template in aspect exposure");
	}
	
	public List queryForList(String sql, Object... args) throws FrameworkErrorSignal
	{
		return super.queryForList(sql, args);
	}
	
	//boxing tolerance for dictionary object
	public List queryForList(String sql, Map args) throws FrameworkErrorSignal
	{
		return super.queryForList(sql, args);
	}
	
	public int update(String sql, Object... args) throws DuplicateNameSignal, FrameworkErrorSignal
	{
		return super.update(sql, args);
	}
	
	//boxing tolerance for dictionary object
	public int update(String sql, Map args) throws DuplicateNameSignal, FrameworkErrorSignal
	{
		return super.update(sql, args);
	}
}
