package org.mz.csaude.dbsyncfeatures.remote.data.share.manager.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Date;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.mz.csaude.dbsyncfeatures.remote.data.share.manager.utils.Utils;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class RemoteDataInfo {
	
	private String messageId;
	
	private Date dateGenerated;
	
	private long qtyRecords;
	
	private String originAppLocationCode;
	
	private String tableName;
	
	private String fileName;
	
	private byte[] data;
	
	public RemoteDataInfo() {
	}
	
	public String getMessageId() {
		return messageId;
	}
	
	public String getTableName() {
		return tableName;
	}
	
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	
	@JsonIgnore
	public boolean isEmpty() {
		return this.qtyRecords == 0;
	}
	
	@JsonIgnore
	public String getDestinationRelativePath() {
		String[] utiParts = {originAppLocationCode, tableName};
	
		return File.separator + StringUtils.join(utiParts, File.separator);
	}
	
	public static RemoteDataInfo init(File dataFile) throws IOException {
		String content = new String(Files.readAllBytes(dataFile.toPath()));
		
		RemoteDataInfo dataInfo = Utils.loadObjectFormJSON(RemoteDataInfo.class, content);
		
		dataInfo.setMessageId(UUID.randomUUID().toString());
		dataInfo.setFileName(dataFile.getName());
		
		dataInfo.setData(new byte[(int) dataFile.length()]);
		
		FileInputStream attStream = new FileInputStream(dataFile);
		
		attStream.read(dataInfo.getData());
		
		attStream.close();
		
		dataInfo.setData(dataInfo.getData());
		
		return dataInfo;
	}
	
	public Date getDateGenerated() {
		return dateGenerated;
	}
	
	public void setDateGenerated(Date dateGenerated) {
		this.dateGenerated = dateGenerated;
	}
	
	public long getQtyRecords() {
		return qtyRecords;
	}
	
	public void setQtyRecords(long qtyRecords) {
		this.qtyRecords = qtyRecords;
	}
	
	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}
	
	public String getOriginAppLocationCode() {
		return originAppLocationCode;
	}
	
	public void setOriginAppLocationCode(String originAppLocationCode) {
		this.originAppLocationCode = originAppLocationCode;
	}
	
	public String getFileName() {
		return fileName;
	}
	
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	public byte[] getData() {
		return data;
	}
	
	public void setData(byte[] data) {
		this.data = data;
	}
	
	@Override
	public String toString() {
		String msg = "[file: " + this.fileName + ",  origin: " + this.originAppLocationCode + ", date: " + this.dateGenerated
		        + ", id :" + this.messageId + ", records: " + this.qtyRecords + "]";
		
		return msg;
	}
}
