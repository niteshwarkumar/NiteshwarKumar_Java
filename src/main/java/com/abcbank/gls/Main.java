package com.abcbank.gls;

import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {
 public static void main(String[] args) throws Exception {
	GLS gls = new GLS();
	// delete the existing op file to generate a new one
	Files.deleteIfExists(Paths.get(EConfig.GLS_CONFIG.getValue("gls.resource.input_folderName")
					+ EConfig.GLS_CONFIG.getValue("gls.resource.output_endOf_day")));
	gls.sequenceProcessStock();
}
}
