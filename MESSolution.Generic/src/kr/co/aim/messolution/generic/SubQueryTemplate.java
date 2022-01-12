package kr.co.aim.messolution.generic;

import java.util.List;

import kr.co.aim.greenframe.infra.*;
import kr.co.aim.greenframe.orm.SqlTemplate;
import kr.co.aim.greentrack.generic.orm.SqlMesTemplate;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

public class SubQueryTemplate extends SqlMesTemplate implements InitializingBean {

	Log logger = LogFactory.getLog(SubQueryTemplate.class.getSimpleName());
	
	@Override
	public void afterPropertiesSet() throws Exception
	{
		logger.info("Initializing sub query template...");
		
		try
		{
			SqlTemplate jdbcSource;
			
			try
			{
				jdbcSource = (SqlTemplate) InfraServiceProxy.getBeanService("SubSqlTemplate");
			}
			catch (Exception ex)
			{
				logger.error(ex);
				
				jdbcSource = null;
				logger.error("nothing for sub data source");
			}
			
			//setSqlTemplate((SqlTemplate) InfraServiceProxy.getBeanService("SubSqlTemplate") );
			
			if (jdbcSource != null)
			{
				setSqlTemplate(jdbcSource);
				
				//connection test
				List<ListOrderedMap> resultMap = queryForList("select * from dual", new Object[] {});
				
				if (resultMap != null)
				{
					logger.info("Connected Succefully at sub data source");
				}
			}
		}
		catch (Exception ex)
		{
			logger.error(ex);
		}
		finally
		{
			logger.info("sub query template initialized");
		}
	}
}
