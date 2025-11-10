/* 
 * Copyright
 * 
 */
package io.mosip.preregistration.datasync.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import io.mosip.preregistration.core.converter.EncryptPiiDataConverter;
import jakarta.persistence.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Component;

import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * This entity class defines the database table details for PreRegistration.
 * 
 * @author Aiham Hasan
 * @since 1.2.0
 *
 */
@Component
@Entity
@Table(name = "applicant_demographic_consumed", schema = "prereg")
@Setter
@NoArgsConstructor
public class DemographicEntityConsumed implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 6705845720255847210L;

	//@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "demographicEntity")
	//private List<DocumentEntity> documentEntity;

	/** The pre registration id. */
	@Column(name = "prereg_id", nullable = false)
	@Id
	private String preRegistrationId;

	/** The JSON */
	@Column(name = "demog_detail", nullable = false, columnDefinition = "bytea")
	private byte[] applicantDetailJson;

	 // Getter and Setter methods for requesttime are overridden manually
	public void setApplicantDetailJson(byte[] applicantDetailJson) {
		this.applicantDetailJson = applicantDetailJson != null ? applicantDetailJson.clone() : null;
	}

	/** The status_code */
	@Column(name = "status_code", nullable = false)
	private String statusCode;

	/** The lang_code */
	@Column(name = "lang_code", nullable = false)
	private String langCode;

	/** The created by. */
	@Column(name = "cr_by")
	@Convert(converter = EncryptPiiDataConverter.class)
	private String createdBy;

	/**
	 * Hashed Created By
	 */
	@Column(name = "cr_by_hash")
	private String createdByHash;

	/** The created appuser by. */
	@Column(name = "cr_appuser_id")
	@Convert(converter = EncryptPiiDataConverter.class)
	private String crAppuserId;

	/** The create date time. */
	@Column(name = "cr_dtimes")
	private LocalDateTime createDateTime;

	/** The updated by. */
	@Column(name = "upd_by")
	@Convert(converter = EncryptPiiDataConverter.class)
	private String updatedBy;

	/** The update date time. */
	@Column(name = "upd_dtimes")
	private LocalDateTime updateDateTime;

	/**
	 * Encrypted Date Time
	 */
	@Column(name = "encrypted_dtimes")
	private LocalDateTime encryptedDateTime;

	@Column(name = "demog_detail_hash")
	private String demogDetailHash;


	@PrePersist
	private void prePersist() {
		// createdBy is the plain value here; @Convert runs at DB interaction time.
		if (this.createdBy != null) {
			this.createdByHash = DigestUtils.sha256Hex(this.createdBy);
		}
	}

	// Optional: if you ever allow changing crBy and want hash kept in sync
	@PreUpdate
	private void preUpdate() {
		if (this.createdBy != null) {
			this.createdByHash = DigestUtils.sha256Hex(this.createdBy);
		}
	}

}
