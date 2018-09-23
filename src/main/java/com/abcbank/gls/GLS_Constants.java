package com.abcbank.gls;

public class GLS_Constants {

	public static final String TNXTYPE = "TransactionType";
	public static final String TNXQTY = "TransactionQuantity";
	public static final String INSTRUMENT = "Instrument";
	public static final String TNXID = "TransactionId";
	public static final String TNXTYPE_BUY = "B";
	public static final String TNXTYPE_SELL = "S";
	public static final String ACCTTYPE_E = "E";
	public static final String ACCTTYPE_I = "I";
	private static final String ACCTCODE_E = "101";
	private static final String ACCTCODE_I = "201";
	public static final Object DELTA = "Delta";
	
	//get the acct code for a giveb acct type
	public static String getAcctCode(String AcctType) {
		String acctCode = "";
		if(AcctType.equals(ACCTTYPE_E)) acctCode = ACCTCODE_E;
		if(AcctType.equals(ACCTTYPE_I)) acctCode = ACCTCODE_I;
		return acctCode;
	}

}
