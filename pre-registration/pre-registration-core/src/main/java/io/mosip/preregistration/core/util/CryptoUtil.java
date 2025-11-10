package io.mosip.preregistration.core.util;

import static io.mosip.preregistration.core.constant.PreRegCoreConstant.LOGGER_ID;
import static io.mosip.preregistration.core.constant.PreRegCoreConstant.LOGGER_IDTYPE;
import static io.mosip.preregistration.core.constant.PreRegCoreConstant.LOGGER_SESSIONID;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.preregistration.core.common.dto.CryptoManagerRequestDTO;
import io.mosip.preregistration.core.common.dto.CryptoManagerResponseDTO;
import io.mosip.preregistration.core.common.dto.RequestWrapper;
import io.mosip.preregistration.core.common.dto.ResponseWrapper;
import io.mosip.preregistration.core.config.LoggerConfiguration;
import io.mosip.preregistration.core.exception.EncryptionFailedException;

/**
 * @author Tapaswini Behera
 * @since 1.0.0
 *
 */
@Service
public class CryptoUtil {

	private Logger log = LoggerConfiguration.logConfig(CryptoUtil.class);

	/**
	 * Autowired reference for {@link #restTemplateBuilder}
	 */
	@Qualifier("selfTokenRestTemplate")
	@Autowired
	RestTemplate restTemplate;

	@Value("${cryptoResource.url}")
	public String cryptoResourceUrl;

	@Value("${preregistration.crypto.applicationId}")
	public String cryptoApplcationId;

	@Value("${preregistration.crypto.pii.referenceId}")
	public String cryptoPiiReferenceId;

	@Value("${preregistration.crypto.referenceId}")
	public String cryptoReferenceId;

	@Value("${preregistration.crypto.PrependThumbprint}")
	public boolean cryptoPrependThumbprint;

	public byte[] encrypt(byte[] originalInput, LocalDateTime localDateTime) {
		log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID, "In encrypt method of CryptoUtil service ");

		ResponseEntity<ResponseWrapper<CryptoManagerResponseDTO>> response = null;
		byte[] encryptedBytes = null;
		try {
			String encodedBytes = io.mosip.kernel.core.util.CryptoUtil.encodeToURLSafeBase64(originalInput);
			CryptoManagerRequestDTO dto = new CryptoManagerRequestDTO();
			dto.setApplicationId(cryptoApplcationId);
			dto.setData(encodedBytes);
			dto.setReferenceId(cryptoReferenceId);
			dto.setTimeStamp(localDateTime);
			dto.setPrependThumbprint(cryptoPrependThumbprint);
			RequestWrapper<CryptoManagerRequestDTO> requestKernel = new RequestWrapper<>();
			requestKernel.setRequest(dto);
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<RequestWrapper<CryptoManagerRequestDTO>> request = new HttpEntity<>(requestKernel, headers);
			log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
					"In encrypt method of CryptoUtil service cryptoResourceUrl: " + cryptoResourceUrl + "/encrypt");
			response = restTemplate.exchange(cryptoResourceUrl + "/encrypt", HttpMethod.POST, request,
					new ParameterizedTypeReference<ResponseWrapper<CryptoManagerResponseDTO>>() {
					});
			log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID, "encrypt response of " + response);
			ResponseWrapper<CryptoManagerResponseDTO> body = response.getBody();
			if (body != null) {
				if (!(body.getErrors() == null || body.getErrors().isEmpty())) {
					throw new EncryptionFailedException(body.getErrors(), null);
				}
				if (body.getResponse() != null) {
					encryptedBytes = body.getResponse().getData().getBytes();
				}
			}
		} catch (Exception ex) {
			log.debug(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID, ExceptionUtils.getStackTrace(ex));
			log.error(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
					"In encrypt method of CryptoUtil Util for Exception- " + ex.getMessage());
			throw ex;
		}
		return encryptedBytes;

	}


	public String encryptPiiData(String originalData, LocalDateTime encryptedTimestamp) {
		log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID, "In encryptPiiData method of CryptoUtil service ");
		ResponseEntity<ResponseWrapper<CryptoManagerResponseDTO>> response = null;
		String encryptedData = null;
		try {
			final byte[] originalBytes = originalData.getBytes();
			final String encodedBytes = io.mosip.kernel.core.util.CryptoUtil.encodeToURLSafeBase64(originalBytes);
			final CryptoManagerRequestDTO dto = new CryptoManagerRequestDTO();
			dto.setApplicationId(cryptoApplcationId);
			dto.setReferenceId(cryptoPiiReferenceId);
			dto.setTimeStamp(encryptedTimestamp);
			dto.setData(encodedBytes);
			final RequestWrapper<CryptoManagerRequestDTO> requestKernel = new RequestWrapper<>();
			requestKernel.setRequest(dto);
			final HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			final HttpEntity<RequestWrapper<CryptoManagerRequestDTO>> request = new HttpEntity<>(requestKernel, headers);
			log.info(
					LOGGER_SESSIONID,
					LOGGER_IDTYPE,
					LOGGER_ID,
					"In encryptPiiData method of CryptoUtil service cryptoResourceUrl: " + cryptoResourceUrl + "/encrypt"
			);
			response = restTemplate.exchange(
					cryptoResourceUrl + "/encrypt",
					HttpMethod.POST,
					request,
					new ParameterizedTypeReference<>() {}
			);
			log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID, "encryptPiiData response of " + response);
			ResponseWrapper<CryptoManagerResponseDTO> body = response.getBody();
			if (body != null) {
				if (!(body.getErrors() == null || body.getErrors().isEmpty())) {
					throw new EncryptionFailedException(body.getErrors(), null);
				}
				if (body.getResponse() != null) {
					encryptedData = body.getResponse().getData();
				}
			}
		} catch (Exception ex) {
			log.debug(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID, ExceptionUtils.getStackTrace(ex));
			log.error(
					LOGGER_SESSIONID,
					LOGGER_IDTYPE,
					LOGGER_ID,
					"In encrypt method of CryptoUtil Util for Exception- " + ex.getMessage()
			);
			throw ex;
		}
		return encryptedData;
	}

	public byte[] decrypt(byte[] originalInput, LocalDateTime localDateTime) {
		log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID, "In decrypt method of CryptoUtil service ");
		ResponseEntity<ResponseWrapper<CryptoManagerResponseDTO>> response = null;
		byte[] decodedBytes = null;
		try {

			CryptoManagerRequestDTO dto = new CryptoManagerRequestDTO();
			dto.setApplicationId(cryptoApplcationId);
			dto.setData(new String(originalInput, StandardCharsets.UTF_8));
			dto.setReferenceId(cryptoReferenceId);
			dto.setTimeStamp(localDateTime);
			dto.setPrependThumbprint(cryptoPrependThumbprint);
			RequestWrapper<CryptoManagerRequestDTO> requestKernel = new RequestWrapper<>();
			requestKernel.setRequest(dto);

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			HttpEntity<RequestWrapper<CryptoManagerRequestDTO>> request = new HttpEntity<>(requestKernel, headers);
			log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
					"In decrypt method of CryptoUtil service cryptoResourceUrl: " + cryptoResourceUrl + "/decrypt");
			response = restTemplate.exchange(cryptoResourceUrl + "/decrypt", HttpMethod.POST, request,
					new ParameterizedTypeReference<ResponseWrapper<CryptoManagerResponseDTO>>() {
					});
			ResponseWrapper<CryptoManagerResponseDTO> body = response.getBody();
			if (body != null) {
				if (!(body.getErrors() == null || body.getErrors().isEmpty())) {
					throw new EncryptionFailedException(body.getErrors(), null);
				}
				if (body.getResponse() != null) {
					decodedBytes = Base64.decodeBase64(body.getResponse().getData().getBytes());
				}
			}
		} catch (Exception ex) {
			log.debug(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID, ExceptionUtils.getStackTrace(ex));
			log.error(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
					"In decrypt method of CryptoUtil Util for Exception- " + ex.getMessage());
			throw ex;
		}
		return decodedBytes;
	}

	public String decryptPiiData(String encryptedData, LocalDateTime decodedTimestamp) {
		log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID, "In decryptPiiData method of CryptoUtil service ");
		ResponseEntity<ResponseWrapper<CryptoManagerResponseDTO>> response = null;
		String decodedData = null;
		try {
			final CryptoManagerRequestDTO dto = new CryptoManagerRequestDTO();
			dto.setApplicationId(cryptoApplcationId);
			dto.setReferenceId(cryptoPiiReferenceId);
			dto.setTimeStamp(decodedTimestamp);
			dto.setData(encryptedData);
			final RequestWrapper<CryptoManagerRequestDTO> requestKernel = new RequestWrapper<>();
			requestKernel.setRequest(dto);
			final HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			final HttpEntity<RequestWrapper<CryptoManagerRequestDTO>> request = new HttpEntity<>(requestKernel, headers);
			log.info(
					LOGGER_SESSIONID,
					LOGGER_IDTYPE,
					LOGGER_ID,
					"In decryptPiiData method of CryptoUtil service cryptoResourceUrl: " + cryptoResourceUrl + "/decrypt"
			);
			response = restTemplate.exchange(
					cryptoResourceUrl + "/decrypt",
					HttpMethod.POST,
					request,
					new ParameterizedTypeReference<>() {}
			);
			ResponseWrapper<CryptoManagerResponseDTO> body = response.getBody();
			if (body != null) {
				if (!(body.getErrors() == null || body.getErrors().isEmpty())) {
					throw new EncryptionFailedException(body.getErrors(), null);
				}
				if (body.getResponse() != null) {
					final byte[] decodedBytes = Base64.decodeBase64(body.getResponse().getData().getBytes());
					decodedData = new String(decodedBytes, StandardCharsets.UTF_8);
				}
			}
		} catch (Exception ex) {
			log.debug(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID, ExceptionUtils.getStackTrace(ex));
			log.error(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
					"In decrypt method of CryptoUtil Util for Exception- " + ex.getMessage());
			throw ex;
		}
		return decodedData;
	}
}