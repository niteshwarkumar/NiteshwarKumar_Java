package com.abcbank.gls;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public enum EConfig {

	GLS_CONFIG("GlsConfig.cfg");

	private Properties prop;
	private final String cfgName;

	private EConfig(String cfgName) {
		this.cfgName = cfgName;
	}

	public String getValue(String key) {
		String val = "";
		try {
			if (loadConfig(this.cfgName)) {
				val = (String) prop.getProperty(key);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return val;
	}

	private boolean loadConfig(String name) throws Exception {
		InputStream in;
		boolean rc = false;

		// Get class loader
		ClassLoader cl = this.getClass().getClassLoader();
		if (cl != null) {
			in = cl.getResourceAsStream(name);
		} else {
			in = ClassLoader.getSystemResourceAsStream(name);
		}
		// If the input stream is null, then the configuration file was not found
		if (in == null) {
			throw new Exception("Data source configuration file '" + name + "' not found");
		} else {
			try {
				loadProp(in);
				if (prop == null)
					rc = false;
				rc = true;
			} finally {
				// Closing the input stream
				if (in != null) {
					try {
						in.close();
					} catch (Exception ex) {
					}
				}
			}
		}
		return rc;
	}

	private boolean loadProp(InputStream in) throws IOException {
		boolean done = false;
		prop = new Properties();
		// Load the configuration file into the properties table
		prop.load(in);
		done = true;
		return done;
	}

}
