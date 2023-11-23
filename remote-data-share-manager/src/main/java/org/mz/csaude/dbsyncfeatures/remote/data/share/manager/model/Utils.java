package org.mz.csaude.dbsyncfeatures.remote.data.share.manager.model;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class Utils {
	
	@SuppressWarnings("deprecation")
	public static ObjectMapper defaultJsonObjectMapper() {
		final ObjectMapper result = new ObjectMapper();
		result.configure(SerializationFeature.INDENT_OUTPUT, true);
		result.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		result.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		result.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
		result.configure(Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
		result.configure(Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
		result.setSerializationInclusion(Include.NON_NULL);
		
		return result;
	}
	
	/**
	 * Converte um objecto em Json
	 * 
	 * @param objecto a converter
	 * @return O JSON correspondente a este objecto
	 */
	public static String parseToJSON(Object objecto) {
		try {
			return defaultJsonObjectMapper().writeValueAsString(objecto);
		}
		catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static <T> T loadObjectFormJSON(Class<T> clazz, String json) {
		try {
			return defaultJsonObjectMapper().readValue(json, clazz);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
}
