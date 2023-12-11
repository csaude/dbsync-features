package org.mz.csaude.dbsyncfeatures.core.manager.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.camel.Converter;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Converter(generateLoader = true)
@Service
@Component
public class CommonConverter {
	
	 private static final ObjectMapper mapper = new ObjectMapper();

	public static  <T> T fromJson(String json, Class<T> clazz) throws Exception {
		return mapper.readValue(json, clazz);
	}

	@SuppressWarnings("deprecation")
	public static ObjectMapper defaultJsonObjectMapper() {
		final ObjectMapper result = new ObjectMapper();
		result.configure(SerializationFeature.INDENT_OUTPUT, true);
		result.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		result.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		result.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
		result.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
		result.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
		result.setSerializationInclusion(JsonInclude.Include.NON_NULL);

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

