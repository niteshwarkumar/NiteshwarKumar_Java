package com.abcbank.gls;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TransactionUtil {

	// Synchronized to make stock thread safe
	// LinkedHashMap to maintain order of insertion of csv rows
	private Map<String, Integer> stock = Collections.synchronizedMap(new LinkedHashMap<String, Integer>());
	private Map<String, Integer> org_stock = new LinkedHashMap<>();
	private String[] csvHeader;
	private String CSV_SPLIT_BY = ",";
	private String KEY_SEPERATOR = "##";
	private final String NEW_LINE = "\n";
	private  Integer totalVal;
	// process the input transaction and update the stock
	protected void updateStock(String trnx_filepath) throws Exception {
		String trnx_Str = readFile(trnx_filepath);
		if (!trnx_Str.isEmpty()) {
			JSONArray inpTranx_arr = null;
			try {
				inpTranx_arr = new JSONArray(trnx_Str);
			} catch (JSONException je) {

			}
			if (null != inpTranx_arr) {
				inpTranx_arr.forEach(ita -> {
					try {
						updateSingleStock((JSONObject) ita);
					} catch (Exception e) {
					}

				});
			} else {
				throw new Exception("Invalid transaction file");
			}
		}
	}
	
	// process the input transaction and update the stock
		protected void updateStockInBulk(String trnx_filepath) throws Exception {
			String trnx_Str = readFile(trnx_filepath);
			List<String> insKey_Lst= new ArrayList<String>();
			if (!trnx_Str.isEmpty()) {
				JSONArray inpTranx_arr = new JSONArray(trnx_Str);
				if (null != inpTranx_arr) {
					inpTranx_arr.forEach(ita -> {
						try {
							String ins = ((JSONObject) ita).optString(GLS_Constants.INSTRUMENT);
							String trxType = ((JSONObject) ita).optString(GLS_Constants.TNXTYPE);
							String insKey=ins + KEY_SEPERATOR+ trxType;
							if(!insKey_Lst.contains(insKey)) {
								insKey_Lst.add(insKey);
								int totalTranxQty =  getBulkIta(inpTranx_arr,ins, trxType);
								JSONObject bulkIta = new JSONObject();
								bulkIta.put(GLS_Constants.INSTRUMENT, ins);
								bulkIta.put(GLS_Constants.TNXTYPE, trxType);
								bulkIta.put(GLS_Constants.TNXQTY, totalTranxQty);
								updateSingleStock(bulkIta);
							}
							
						} catch (Exception e) {
						}

					});
				}
				} else {
					throw new Exception("Invalid transaction file");
				}
		}

	private int getBulkIta(JSONArray inpTranx_arr, String instrument, String tranxtype) {
		totalVal = 0;
		inpTranx_arr.forEach(ita ->{
			JSONObject ita_Obj = (JSONObject)ita;
			if(ita_Obj.optString(GLS_Constants.INSTRUMENT).equals(instrument)
					&& ita_Obj.optString(GLS_Constants.TNXTYPE).equals(tranxtype)) {
				totalVal+=ita_Obj.optInt(GLS_Constants.TNXQTY);
			}
		});
		return totalVal;
	}

	protected void updateSingleStock(JSONObject inpTranx_obj) throws Exception {
		// get the keys for the stock map
		String key_acct_e = inpTranx_obj.optString(GLS_Constants.INSTRUMENT) + KEY_SEPERATOR + GLS_Constants.ACCTTYPE_E;
		String key_acct_i = inpTranx_obj.optString(GLS_Constants.INSTRUMENT) + KEY_SEPERATOR + GLS_Constants.ACCTTYPE_I;
		int txn_qty = inpTranx_obj.optInt(GLS_Constants.TNXQTY);
		// Transaction logic
		if (inpTranx_obj.optString(GLS_Constants.TNXTYPE).equalsIgnoreCase(GLS_Constants.TNXTYPE_BUY)) {
			stock.put(key_acct_e, stock.getOrDefault(key_acct_e, 0) + txn_qty);
			stock.put(key_acct_i, stock.getOrDefault(key_acct_i, 0) - txn_qty);
		} else if (inpTranx_obj.optString(GLS_Constants.TNXTYPE).equalsIgnoreCase(GLS_Constants.TNXTYPE_SELL)) {
			stock.put(key_acct_e, stock.getOrDefault(key_acct_e, 0) - txn_qty);
			stock.put(key_acct_i, stock.getOrDefault(key_acct_i, 0) + txn_qty);
		} else {
			throw new Exception("Invalid transaction type: '" + inpTranx_obj.optString(GLS_Constants.TNXTYPE) + "'");
		}

	}

	protected Map<String, Integer> getStock() {
		return stock;
	}

	protected Map<String, Integer> getOriginalStock() {
		return org_stock;
	}
// Read file from the disk and pass it as a string
	protected String readFile(String path) throws Exception {
		StringBuilder sb = new StringBuilder();
		try (BufferedReader br = new BufferedReader(new FileReader(path))) {
			String sCurrentLine;
			while ((sCurrentLine = br.readLine()) != null) {
				sb.append(sCurrentLine);
			}
		} catch (IOException e) {
			throw new Exception("File not found: '" + path + "'");
		}
		return sb.toString();
	}

	protected void loadCurrentStock(String path) throws Exception {
		try (BufferedReader br = new BufferedReader(new FileReader(path))) {
			csvHeader = br.readLine().split(CSV_SPLIT_BY);
			String sCurrentLine;
			while ((sCurrentLine = br.readLine()) != null) {
				String[] stockEntry = sCurrentLine.split(CSV_SPLIT_BY);
				/*
				 * Creating key with combination of Instrument and AccountType (Assuming the
				 * Account is fixed and unique ie 101 for E and 201 for I, so ignoring the account code in the key just to reduce key lenght )
				 */
				String key = stockEntry[0] + KEY_SEPERATOR + stockEntry[2];
				Integer val = Integer.parseInt(stockEntry[3]);
				stock.put(key, val);
			}
		} catch (IOException e) {
			throw new Exception("File not found: '" + path + "'");
		}
		// Keep the original stock reference
		org_stock = new LinkedHashMap<String, Integer>(stock);
	}

	public void generateCurrentStockCsv() throws Exception {
		PrintWriter pw = null;
		String opPath = EConfig.GLS_CONFIG.getValue("gls.resource.output_folderName")
				+ EConfig.GLS_CONFIG.getValue("gls.resource.output_endOf_day");
		try {
			pw = new PrintWriter(new File(opPath));
		} catch (FileNotFoundException e) {
			throw new Exception(
					"Error in creating the output file(Please check the permission issue in the output folder): '"
							+ opPath + "'");
		}
		StringBuilder sb = new StringBuilder();
		for (String header : csvHeader) {
			sb.append(header).append(CSV_SPLIT_BY);
		}
		sb.append(GLS_Constants.DELTA);
		sb.append(NEW_LINE);
		stock.forEach((key, val) -> sb.append(key.split(KEY_SEPERATOR)[0]).append(CSV_SPLIT_BY)
				.append(GLS_Constants.getAcctCode(key.split(KEY_SEPERATOR)[1])).append(CSV_SPLIT_BY)
				.append(key.split(KEY_SEPERATOR)[1]).append(CSV_SPLIT_BY).append(val).append(CSV_SPLIT_BY)
				.append((val - org_stock.get(key))).append(NEW_LINE));
		pw.write(sb.toString());
		pw.close();
	}

}
