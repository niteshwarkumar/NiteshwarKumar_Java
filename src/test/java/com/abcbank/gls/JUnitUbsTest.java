package com.abcbank.gls;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class JUnitUbsTest {

	@Test
	public void testFileExists() throws IOException {
		// Check if configurations are correct and all the required files are available and accessible
		Assert.assertNotNull(EConfig.GLS_CONFIG.getValue("gls.resource.output_folderName"));
		Assert.assertNotNull(EConfig.GLS_CONFIG.getValue("gls.resource.input_folderName"));
		Assert.assertNotNull(EConfig.GLS_CONFIG.getValue("gls.resource.input_transaction"));
		Assert.assertNotNull(EConfig.GLS_CONFIG.getValue("gls.resource.input_startOf_day"));
		Assert.assertNotNull(EConfig.GLS_CONFIG.getValue("gls.resource.output_endOf_day"));

		Assert.assertNull(EConfig.GLS_CONFIG.getValue("not_in_config")); // some random config

		Assert.assertTrue(new File(EConfig.GLS_CONFIG.getValue("gls.resource.input_folderName")
				+ EConfig.GLS_CONFIG.getValue("gls.resource.input_transaction")).exists());
		Assert.assertTrue(new File(EConfig.GLS_CONFIG.getValue("gls.resource.input_folderName")
				+ EConfig.GLS_CONFIG.getValue("gls.resource.input_startOf_day")).exists());
		Assert.assertTrue(new File(EConfig.GLS_CONFIG.getValue("gls.resource.input_folderName")
				+ EConfig.GLS_CONFIG.getValue("gls.resource.expected_endOf_day")).exists());
		// delete the existing op file to generate a new one
		Files.deleteIfExists(Paths.get(EConfig.GLS_CONFIG.getValue("gls.resource.input_folderName")
				+ EConfig.GLS_CONFIG.getValue("gls.resource.output_endOf_day")));
		Assert.assertFalse(new File(EConfig.GLS_CONFIG.getValue("gls.resource.output_folderName")
				+ EConfig.GLS_CONFIG.getValue("gls.resource.output_endOf_day")).exists());
	}

	@Test
	public void testUtilSeqProcess() throws Exception {
		TransactionUtil util = new TransactionUtil();
		util.loadCurrentStock(EConfig.GLS_CONFIG.getValue("gls.resource.input_folderName")
				+ EConfig.GLS_CONFIG.getValue("gls.resource.input_startOf_day"));
		Assert.assertTrue(util.getStock().size() > 0); // Current stock is loaded to the Map
		
		util.updateStock(EConfig.GLS_CONFIG.getValue("gls.resource.input_folderName")
				+ EConfig.GLS_CONFIG.getValue("gls.resource.input_transaction"));
		Assert.assertFalse(util.getStock().equals(util.getOriginalStock()));// Precautionary check to see if current stocks are updated with transaction file
																		   // Will fail if the transaction file is empty or there is no change in the stock 	
		// delete the existing op file to generate a new one
		Files.deleteIfExists(Paths.get(EConfig.GLS_CONFIG.getValue("gls.resource.input_folderName")
						+ EConfig.GLS_CONFIG.getValue("gls.resource.output_endOf_day")));
		util.generateCurrentStockCsv();
		List<String> expStockFromCSV = getDataFromCSV(EConfig.GLS_CONFIG.getValue("gls.resource.input_folderName")
				+ EConfig.GLS_CONFIG.getValue("gls.resource.expected_endOf_day"));
		List<String> opStockFromCSV = getDataFromCSV(EConfig.GLS_CONFIG.getValue("gls.resource.output_folderName")
				+ EConfig.GLS_CONFIG.getValue("gls.resource.output_endOf_day"));
		Assert.assertTrue(expStockFromCSV.size() == opStockFromCSV.size()); // output matches expected result
		for (String opRows : opStockFromCSV) {
			Assert.assertTrue(expStockFromCSV.contains(opRows)); // output matches expected result
		}
	}
	@Test
	public void testUtilBuklProcess() throws Exception {
		//Bulk Transaction wise Calculation
		TransactionUtil util = new TransactionUtil();
		util.loadCurrentStock(EConfig.GLS_CONFIG.getValue("gls.resource.input_folderName")
				+ EConfig.GLS_CONFIG.getValue("gls.resource.input_startOf_day"));
		Assert.assertTrue(util.getStock().size() > 0); // Current stock is loaded to the Map
		
		util.updateStockInBulk(EConfig.GLS_CONFIG.getValue("gls.resource.input_folderName")
				+ EConfig.GLS_CONFIG.getValue("gls.resource.input_transaction"));
		Assert.assertFalse(util.getStock().equals(util.getOriginalStock()));// Precautionary check to see if current stocks are updated with transaction file
																		   // Will fail if the transaction file is empty or there is no change in the stock 	
		// delete the existing op file to generate a new one
		Files.deleteIfExists(Paths.get(EConfig.GLS_CONFIG.getValue("gls.resource.input_folderName")
						+ EConfig.GLS_CONFIG.getValue("gls.resource.output_endOf_day")));
		//Each Transaction wise Calculation
		util.generateCurrentStockCsv();
		List<String> expStockFromCSV = getDataFromCSV(EConfig.GLS_CONFIG.getValue("gls.resource.input_folderName")
				+ EConfig.GLS_CONFIG.getValue("gls.resource.expected_endOf_day"));
		List<String> opStockFromCSV = getDataFromCSV(EConfig.GLS_CONFIG.getValue("gls.resource.output_folderName")
				+ EConfig.GLS_CONFIG.getValue("gls.resource.output_endOf_day"));
		Assert.assertTrue(expStockFromCSV.size() == opStockFromCSV.size()); // output matches expected result
		for (String opRows : opStockFromCSV) {
			Assert.assertTrue(expStockFromCSV.contains(opRows)); // output matches expected result
		}
	}
	@Test
	public void testGLS() throws Exception {
		GLS gls = new GLS();
		// delete the existing op file to generate a new one
		Files.deleteIfExists(Paths.get(EConfig.GLS_CONFIG.getValue("gls.resource.input_folderName")
								+ EConfig.GLS_CONFIG.getValue("gls.resource.output_endOf_day")));
		List<String> expStockFromCSV = getDataFromCSV(EConfig.GLS_CONFIG.getValue("gls.resource.input_folderName")
				+ EConfig.GLS_CONFIG.getValue("gls.resource.expected_endOf_day"));
		//Bulk Transaction wise Calculation
		gls.bulkProcessStock();
		
		List<String> opStockFromCSV = getDataFromCSV(EConfig.GLS_CONFIG.getValue("gls.resource.output_folderName")
				+ EConfig.GLS_CONFIG.getValue("gls.resource.output_endOf_day"));
		Assert.assertTrue(expStockFromCSV.size() == opStockFromCSV.size()); // output matches expected result
		for (String opRows : opStockFromCSV) {
			Assert.assertTrue(expStockFromCSV.contains(opRows)); // output matches expected result
		}
		
		
		// delete the existing op file to generate a new one
		Files.deleteIfExists(Paths.get(EConfig.GLS_CONFIG.getValue("gls.resource.input_folderName")
										+ EConfig.GLS_CONFIG.getValue("gls.resource.output_endOf_day")));
		//Each Transaction wise Calculation
		gls.sequenceProcessStock();
		opStockFromCSV = getDataFromCSV(EConfig.GLS_CONFIG.getValue("gls.resource.output_folderName")
						+ EConfig.GLS_CONFIG.getValue("gls.resource.output_endOf_day"));
		Assert.assertTrue(expStockFromCSV.size() == opStockFromCSV.size()); // output matches expected result
		for (String opRows : opStockFromCSV) {
			Assert.assertTrue(expStockFromCSV.contains(opRows)); // output matches expected result
		}
	}

	private List<String> getDataFromCSV(String path) {
		List<String> csvRows = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new FileReader(path))) {
			String sCurrentLine;
			while ((sCurrentLine = br.readLine()) != null) {
				if (!sCurrentLine.trim().isEmpty())
					csvRows.add(sCurrentLine);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return (csvRows);
	}
}
