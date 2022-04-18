package com.moneybags.tempfly.util.data.files;

import java.io.InputStream;

import com.moneybags.tempfly.util.data.config.ConfigProvider;

public interface ResourceProvider {

	InputStream getResourceStream(String embedded);
	
	ConfigProvider getConfigProvider();
	
	
	
}
