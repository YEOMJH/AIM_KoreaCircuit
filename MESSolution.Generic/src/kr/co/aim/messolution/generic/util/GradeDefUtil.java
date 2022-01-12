package kr.co.aim.messolution.generic.util;
import java.util.HashMap;
import java.util.List;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.GenericServiceProxy;
import kr.co.aim.greentrack.generic.grade.GradeDef;
public class GradeDefUtil 
{	
	public static GradeDef getGrade(String factoryName, String gradeType, boolean initialFlag)
	{
		return GenericServiceProxy.getGradeMap().getGradeDefI(factoryName, gradeType, initialFlag);
	}
	
	public static String generateGradeSequence(String factoryName, String gradeType, boolean initialFlag, long quantity)
	{
		StringBuffer gradeSequence = new StringBuffer();
		
		try
		{
			String grade = GenericServiceProxy.getGradeMap().getGradeDefI(factoryName, gradeType, initialFlag).getGrade();
			
			for (int i=0;i<quantity;i++)
			{
				gradeSequence.append(grade);
			}
		}
		catch (Exception ex)
		{
			//print error
		}
		
		return gradeSequence.toString();
	}
	
	public static HashMap<String, String> generateGradeMap(String factoryName, String gradeType)
		throws CustomException
	{
		HashMap<String, String> map = new HashMap<String, String>();
		
		List<GradeDef> gradeDefs = GenericServiceProxy.getGradeMap().getGradeFactoryDef().getGradeDef(factoryName, gradeType);
		
		for (GradeDef grade : gradeDefs)
		{
			map.put(grade.getGrade(), "0");
		}
		
		return map;
	}
}
