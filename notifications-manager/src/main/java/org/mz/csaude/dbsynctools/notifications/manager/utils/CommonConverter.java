package org.mz.csaude.dbsynctools.notifications.manager.utils;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Converter;
import org.mz.csaude.dbsynctools.notifications.manager.model.NotificationInfo;
import org.springframework.stereotype.Service;

@Converter(generateLoader = true)
@Service
public class CommonConverter {
	
	 private final ObjectMapper mapper = new ObjectMapper();

	 @Converter
	  public byte[] myPackageToByteArray(NotificationInfo source) {
	    try {
	      return mapper.writeValueAsBytes(source);
	    } catch (JsonProcessingException e) {
	      throw new RuntimeException(e);
	    }
	  }

	  @Converter
	  public NotificationInfo byteArrayToMyPackage(byte[] source) {
	    try {
	      return mapper.readValue(source, NotificationInfo.class);
	    } catch (IOException e) {
	      throw new RuntimeException(e);
	    }
	  }

	public <T> T fromJson(String json, Class<T> clazz) throws Exception {
		return mapper.readValue(json, clazz);
	}
}

