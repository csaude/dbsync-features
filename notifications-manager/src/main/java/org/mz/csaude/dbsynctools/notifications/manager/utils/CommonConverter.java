package org.mz.csaude.dbsynctools.notifications.manager.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Converter;
import org.springframework.stereotype.Service;

@Converter(generateLoader = true)
@Service
public class CommonConverter {
	
	 private final ObjectMapper mapper = new ObjectMapper();

	public <T> T fromJson(String json, Class<T> clazz) throws Exception {
		return mapper.readValue(json, clazz);
	}
}

