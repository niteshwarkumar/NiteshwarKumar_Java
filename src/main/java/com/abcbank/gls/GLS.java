package com.abcbank.gls;

public class GLS {
	
	/*shows the flow for the sequence of the processing
	 *  */
	public void sequenceProcessStock() throws Exception {
		TransactionUtil util = new TransactionUtil(); 
		//Load the current stock for the start of the day
		util.loadCurrentStock(EConfig.GLS_CONFIG.getValue("gls.resource.input_folderName")+EConfig.GLS_CONFIG.getValue("gls.resource.input_startOf_day"));
		//process and update current stock with transactions
		util.updateStock(EConfig.GLS_CONFIG.getValue("gls.resource.input_folderName")+EConfig.GLS_CONFIG.getValue("gls.resource.input_transaction"));
		//generate csv report for the stock
		util.generateCurrentStockCsv();
		
	}
	/*shows the flow for the sequence of the processing
	 *  */
	public void bulkProcessStock() throws Exception {
		TransactionUtil util = new TransactionUtil(); 
		//Load the current stock for the start of the day
		util.loadCurrentStock(EConfig.GLS_CONFIG.getValue("gls.resource.input_folderName")+EConfig.GLS_CONFIG.getValue("gls.resource.input_startOf_day"));
		//process and update current stock with transactions
		util.updateStockInBulk(EConfig.GLS_CONFIG.getValue("gls.resource.input_folderName")+EConfig.GLS_CONFIG.getValue("gls.resource.input_transaction"));
		//generate csv report for the stock
		util.generateCurrentStockCsv();
		
	}
}
