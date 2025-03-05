package org.mz.csaude.dbsyncfeatures.core.manager.utils;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class Utils {


	public static  <T> T fromJson(String json, Class<T> clazz) throws Exception {
		return defaultJsonObjectMapper().readValue(json, clazz);
	}
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
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static <T> T loadObjectFormJSON(Class<T> clazz, File jsonFile) {

		InputStream b = null;

		try {
			jsonFile.getParentFile().mkdirs();

			b = Files.newInputStream(jsonFile.toPath());

			return loadObjectFormJSON(clazz, new String(IOUtils.toByteArray(b)));
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
		finally {
			if (b != null) {
				try {
					b.close();
				}
				catch (IOException e) {}
			}
		}

	}

	public static void writeObjectToFile(Object object, File file) {
		try {
			file.getParentFile().mkdirs();

			Files.write(file.toPath(), parseToJSON(object).getBytes(), StandardOpenOption.CREATE,
			    StandardOpenOption.TRUNCATE_EXISTING);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static String readFileContent(String filePath) throws IOException {
		StringBuilder content = new StringBuilder();

		try(BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
			String line;

			while ((line = reader.readLine()) !=null){
				content.append(line).append(System.lineSeparator());
			}
		}
        return content.toString();
    }

	public static void validateScriptFile(String filePath) throws IOException {
		Path path = Paths.get(filePath);
		if (!Files.exists(path)) {
			throw new IOException("Script file does not exists at path:"  + filePath);
		}
	}

	public static <T> T fromBytes(byte[] bytes, Class<T> clazz) throws JsonProcessingException {
		String json = new String(bytes, StandardCharsets.UTF_8);
		return defaultJsonObjectMapper().readValue(json, clazz);
	}
}
