package kr.co.aim.messolution.generic.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import kr.co.aim.greentrack.generic.util.StringUtil;

public class EnumInfoUtil 
{
	public interface SorterCondition 
	{
		public enum TSHIPCondition implements SorterCondition 
		{
			P01("Y"), // E : N, B : 1
			P02("N"), // B : N+1
			P04("N"); // B : N+1

			private String bcrFlag;

			private TSHIPCondition(String args)
			{
				this.bcrFlag = args;
			}
			
			public static boolean validate(String portName)
			{
				if (portName == null || StringUtil.isEmpty(portName))
					return false;

				TSHIPCondition[] values = TSHIPCondition.values();

				for (TSHIPCondition var : values)
				{
					if (var.name().equals(portName))
						return true;
				}

				return false;
			}
		}

		public enum BRMACondition implements SorterCondition 
		{
			P01("N"), // B : N+1
			P02("Y"), // E : 15, B : 1
			P04("N"); // B : N+1

			private String bcrFlag;

			private BRMACondition(String args)
			{
				this.bcrFlag = args;
			}
			
			public static boolean validate(String portName)
			{
				if (portName == null || StringUtil.isEmpty(portName))
					return false;

				BRMACondition[] values = BRMACondition.values();

				for (BRMACondition var : values)
				{
					if (var.name().equals(portName))
						return true;
				}

				return false;
			}
		}

//		public enum TPIPRCondition implements SorterCondition 
//		{
//			P01("Y"), // E : N, B : 1
//			P02("N"), // B : N+1
//			P04("N"); // B : N+1
//
//			private String bcrFlag;
//
//			private TPIPRCondition(String args)
//			{
//				this.bcrFlag = args;
//			}
//			
//			public static boolean validate(String portName)
//			{
//				if (portName == null || StringUtil.isEmpty(portName))
//					return false;
//
//				TPIPRCondition[] values = TPIPRCondition.values();
//
//				for (TPIPRCondition var : values)
//				{
//					if (var.name().equals(portName))
//						return true;
//				}
//
//				return false;
//			}
//		}

		public enum CTRAYCondition implements SorterCondition 
		{
			P04("N"); // B : 1

			private String bcrFlag;

			private CTRAYCondition(String args)
			{
				this.bcrFlag = args;
			}
			
			public static boolean validate(String portName)
			{
				if (portName == null || StringUtil.isEmpty(portName))
					return false;

				CTRAYCondition[] values = CTRAYCondition.values();

				for (CTRAYCondition var : values)
				{
					if (var.name().equals(portName))
						return true;
				}

				return false;
			}
		}
		
		public enum BBCondition implements SorterCondition 
		{
			P01("N"), // B : 1
			P02("N"), // B : 1
			P04("N"); // B : 1
			private String bcrFlag;

			private BBCondition(String args)
			{
				this.bcrFlag = args;
			}
			
			public static boolean validate(String portName)
			{
				if (portName == null || StringUtil.isEmpty(portName))
					return false;

				BBCondition[] values = BBCondition.values();

				for (BBCondition var : values)
				{
					if (var.name().equals(portName))
						return true;
				}

				return false;
			}
		}
	}
	
	public enum SorterPort 
	{
		BL("P01", "P04"), BU("P03"), PL("P02"), PU("P05", "P06", "P07", "P08", "P09", "P10");

		private String[] name;

		private SorterPort(String... args)
		{
			this.name = args;
		}

		public static boolean validateType(String portType)
		{
			if (portType == null || StringUtil.isEmpty(portType))
				return false;

			SorterPort[] values = SorterPort.values();

			for (SorterPort var : values)
			{
				if (var.name().equals(portType))
					return true;
			}

			return false;
		}

		public static boolean validate(String portType, String portName)
		{
			if (portType == null || StringUtil.isEmpty(portType) || portName == null || StringUtil.isEmpty(portName))
				return false;

			SorterPort[] values = SorterPort.values();

			for (SorterPort var : values)
			{
				if (var.name().equals(portType))
				{
					if (StringUtil.in(portName, var.name))
						return true;
				}
			}

			return false;
		}

		public static List<String> getPortNameList(String portType)
		{
			if (portType == null || StringUtil.isEmpty(portType))
				return new ArrayList<>();

			SorterPort[] values = SorterPort.values();

			for (SorterPort var : values)
			{
				if (var.name().equals(portType))
				{
					return Arrays.asList(var.name);
				}
			}

			return new ArrayList<>();
		}

		public String[] getPortNameList()
		{
			return this.name;
		}
	}

	public enum SorterOperationCondition 
	{
		TSHIP("T-SHIP")
		{
			public List<String> getPortNameList()
			{
				List<String> portNameList = new ArrayList<>();
				
				for (SorterCondition.TSHIPCondition condition : SorterCondition.TSHIPCondition.values())
					portNameList.add(condition.name());

				return portNameList;
			};

			public String getBCRFlag(String portName)
			{
				if (!SorterCondition.TSHIPCondition.validate(portName))
					return null;

				return SorterCondition.TSHIPCondition.valueOf(portName).bcrFlag;
			}
		},
		BRMA("B-RMA")
		{
			public List<String>getPortNameList()
			{
				List<String> portNameList = new ArrayList<>();

				for (SorterCondition.BRMACondition condition : SorterCondition.BRMACondition.values())
					portNameList.add(condition.name());

				return portNameList;
			};

			public String getBCRFlag(String portName)
			{
				if (!SorterCondition.BRMACondition.validate(portName))
					return null;

				return SorterCondition.BRMACondition.valueOf(portName).bcrFlag;
			}
		},
//		TPIPR("T-PIPR")
//		{
//			public List<String> getPortNameList()
//			{
//				List<String> portNameList = new ArrayList<>();
//
//				for (SorterCondition.TPIPRCondition condition : SorterCondition.TPIPRCondition.values())
//					portNameList.add(condition.name());
//
//				return portNameList;
//			};
//
//			public String getBCRFlag(String portName)
//			{
//				if (!SorterCondition.TPIPRCondition.validate(portName))
//					return null;
//
//				return SorterCondition.TPIPRCondition.valueOf(portName).bcrFlag;
//			}
//		},
		CTRAY("C-TRAY")
		{
			public List<String> getPortNameList()
			{
				List<String> portNameList = new ArrayList<>();

				for (SorterCondition.CTRAYCondition condition : SorterCondition.CTRAYCondition.values())
					portNameList.add(condition.name());

				return portNameList;
			};

			public String getBCRFlag(String portName)
			{
				if (!SorterCondition.CTRAYCondition.validate(portName))
					return null;

				return SorterCondition.CTRAYCondition.valueOf(portName).bcrFlag;
			}
		},
		BB("B-B")
		{
			public List<String> getPortNameList()
			{
				List<String> portNameList = new ArrayList<>();

				for (SorterCondition.BBCondition condition : SorterCondition.BBCondition.values())
					portNameList.add(condition.name());

				return portNameList;
			};

			public String getBCRFlag(String portName)
			{
				if (!SorterCondition.BBCondition.validate(portName))
					return null;

				return SorterCondition.BBCondition.valueOf(portName).bcrFlag;
			}
		};

		private String[] name;

		private SorterOperationCondition(String... args)
		{
			this.name = args;
		}
		
		public abstract List<String> getPortNameList();
		public abstract String getBCRFlag(String portName);

		public static boolean validate(String operationMode)
		{
			if (operationMode == null || StringUtil.isEmpty(operationMode))
				return false;

			SorterOperationCondition[] values = SorterOperationCondition.values();

			for (SorterOperationCondition var : values)
			{
				if (var.name[0].equals(operationMode))
					return true;
			}

			return false;
		}

		public String getOperationMode()
		{
			return this.name[0];
		}

		public static List<String> getPortNameList(String operationMode)
		{
			List<String> returnVal = new ArrayList<>();
			
			if (SorterOperationCondition.validate(operationMode))
			{
				for (SorterOperationCondition var : SorterOperationCondition.values())
				{
					if (var.name[0].equals(operationMode))
						return var.getPortNameList();
				}
			}
			else
			{
				return new ArrayList<>();
			}

			return returnVal;
		}
		
		public static String getBCRFlag(String operationMode,String portName)
		{
			String returnVal = null;

			if (SorterOperationCondition.validate(operationMode))
			{
				for (SorterOperationCondition var : SorterOperationCondition.values())
				{
					if (var.name[0].equals(operationMode))
						return var.getBCRFlag(portName);
				}
			}
			else
			{
				return null;
			}

			return returnVal;
		}
	}
}
