package io.mosip.preregistration.core.common.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import io.mosip.preregistration.core.converter.EncryptPiiDataConverter;
import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.codec.digest.DigestUtils;

@Entity
@Getter
@Setter
@Table(name = "applications", schema = "prereg")
public class ApplicationEntity {

	@Id
	@Column(name = "application_id")
	private String applicationId;

	/** Booking Type. **/
	@Column(name = "booking_type", nullable = false)
	private String bookingType;

	/** Booking status. **/
	@Column(name = "booking_status_code")
	private String bookingStatusCode;

	/** Application status. **/
	@Column(name = "application_status_code")
	private String applicationStatusCode;

	/** Appointment date. **/
	@Column(name = "appointment_date")
	private LocalDate appointmentDate;

	/** Booking date. **/
	@Column(name = "booking_date")
	private LocalDate bookingDate;

	/** Registration center id. */
	@Column(name = "regcntr_id")
	private String registrationCenterId;

	/** Slot from time. */
	@Column(name = "slot_from_time")
	private LocalTime slotFromTime;

	/** Slot to time. */
	@Column(name = "slot_to_time")
	private LocalTime slotToTime;

	@Column(name = "contact_info")
	@Convert(converter = EncryptPiiDataConverter.class)
	private String contactInfo;

	/**
	 * Created By
	 */
	@Column(name = "cr_by")
	@Convert(converter = EncryptPiiDataConverter.class)
	private String crBy;

	/**
	 * Hashed Created By
	 */
	@Column(name = "cr_by_hash")
	private String crByHash;

	/**
	 * Created Date Time
	 */
	@Column(name = "cr_dtimes")
	private LocalDateTime crDtime;

	/**
	 * Updated By
	 */
	@Column(name = "upd_by")
	@Convert(converter = EncryptPiiDataConverter.class)
	private String updBy;

	/**
	 * Updated Date Time
	 */
	@Column(name = "upd_dtimes")
	private LocalDateTime updDtime;

	@PrePersist
	private void prePersist() {
		// crBy is the plain value here; @Convert runs at DB interaction time.
		if (this.crBy != null) {
			this.crByHash = DigestUtils.sha256Hex(this.crBy);
		}
	}

	// Optional: if you ever allow changing crBy and want hash kept in sync
	@PreUpdate
	private void preUpdate() {
		if (this.crBy != null) {
			this.crByHash = DigestUtils.sha256Hex(this.crBy);
		}
	}

}