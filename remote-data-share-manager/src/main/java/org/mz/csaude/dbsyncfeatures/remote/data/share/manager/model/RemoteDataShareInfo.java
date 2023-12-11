package org.mz.csaude.dbsyncfeatures.remote.data.share.manager.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.mz.csaude.dbsyncfeatures.core.manager.utils.ApplicationProfile;
import org.springframework.context.annotation.Profile;

@Entity
@Table(name = "remote_data_share_info")
@Profile(ApplicationProfile.CENTRAL)
public class RemoteDataShareInfo {

	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Id
	private Long id;

	private String originAppLocationCode;

	private Date requestDate;

	private Date importFinishDate;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getOriginAppLocationCode() {
		return originAppLocationCode;
	}

	public void setOriginAppLocationCode(String originAppLocationCode) {
		this.originAppLocationCode = originAppLocationCode;
	}

	public Date getRequestDate() {
		return requestDate;
	}

	public void setRequestDate(Date requestDate) {
		this.requestDate = requestDate;
	}

	public Date getImportFinishDate() {
		return importFinishDate;
	}

	public void setImportFinishDate(Date importFinishDate) {
		this.importFinishDate = importFinishDate;
	}

}
