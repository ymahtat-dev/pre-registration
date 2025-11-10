package io.mosip.preregistration.core.util.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;

import io.mosip.kernel.core.exception.ServiceError;
import org.apache.commons.codec.binary.Base64;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

import io.mosip.preregistration.core.common.dto.CryptoManagerResponseDTO;
import io.mosip.preregistration.core.common.dto.ResponseWrapper;
import io.mosip.preregistration.core.config.TestConfig;
import io.mosip.preregistration.core.util.CryptoUtil;
import io.mosip.preregistration.core.util.RequestValidator;

/**
 * CryptoUtil Test
 * 
 * @version 1.0.0
 * @author M1043226
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = { TestConfig.class, TestContext.class, WebApplicationContext.class, CryptoUtil.class })
public class CryptoUtilTest {

	@Autowired
	CryptoUtil crypto;
	
	@MockBean
	private RequestValidator validator;

	@MockBean(name="selfTokenRestTemplate")
	RestTemplate restTemplate;
	
	@Value("${cryptoResource.url}")
	public String cryptoResourceUrl;

	@Test
	public void encryptSuccessTest() {
		CryptoManagerResponseDTO cryptoRes = new CryptoManagerResponseDTO();
		cryptoRes.setData("fuyftwfd");
		ResponseWrapper<CryptoManagerResponseDTO> resEntity=new ResponseWrapper<>();
		resEntity.setResponse(cryptoRes);
		ResponseEntity<ResponseWrapper<CryptoManagerResponseDTO>> res = new ResponseEntity<>(resEntity, HttpStatus.OK);
		Mockito.when(restTemplate.exchange(Mockito.anyString(), eq(HttpMethod.POST), Mockito.any(),
				eq(new ParameterizedTypeReference<ResponseWrapper<CryptoManagerResponseDTO>>() {
				}))).thenReturn(res);
		assertNotNull(crypto.encrypt("hello".getBytes(), LocalDateTime.now()));

	}
	

	@Test(expected=HttpClientErrorException.class)
	public void encryptFailedExceptionTest() {
		HttpClientErrorException ex = new HttpClientErrorException(HttpStatus.OK);
		Mockito.when(restTemplate.exchange(Mockito.anyString(), eq(HttpMethod.POST), Mockito.any(),
				eq(new ParameterizedTypeReference<ResponseWrapper<CryptoManagerResponseDTO>>() {
				}))).thenThrow(ex);
		crypto.encrypt("hello".getBytes(), LocalDateTime.now());

	}
	
	/*
	 * @Test public void decryptSuccessTest() { CryptoManagerResponseDTO cryptoRes1
	 * = new CryptoManagerResponseDTO(); cryptoRes1.setData(
	 * "fGKe7i36VWALNj889wy1DTU4PII_J3IlQuC-CEtj_HWakHf3NNIHdTK8pP33uhH-lstOZM35dnC9-piqby3eiB9msEMHgItzzK2pKRSTqz-q0e7521hHGV_J9yKQxLSuRunE6rukY4gwkq7l3Q-jLl7cAE29Pz4ReYCLExDDQ7Wcq03xp_cabJ2pYbRsSYvF4bKLkS8BAyDhZo1Nk5sd5012Hv-khB7ePKxkGlDCVhgyS0PCT80f75ADSCsCfSrnKNqjOA_gjWvc_Oips9uddaD0o2NPpHqDYJa6hg3Z4KzgIwZ4dd62VwO8aInITItLwB1wQtGcK9gDaPqZ1s21oCNLRVlfU1BMSVRURVIj3co-62Kc0b3NUwB4n01-3ZxwmLehGpPFOPi1XNRK1Sw"
	 * ); ResponseWrapper<CryptoManagerResponseDTO> resEntity=new
	 * ResponseWrapper<>(); resEntity.setResponse(cryptoRes1);
	 * ResponseEntity<ResponseWrapper<CryptoManagerResponseDTO>> res = new
	 * ResponseEntity<>(resEntity, HttpStatus.OK);
	 * Mockito.when(restTemplate.exchange(Mockito.anyString(),
	 * Mockito.eq(HttpMethod.POST), Mockito.any(), Mockito.eq(new
	 * ParameterizedTypeReference<ResponseWrapper<CryptoManagerResponseDTO>>() {
	 * }))).thenReturn(res);
	 * assertNotNull(crypto.decrypt("hello".getBytes(),LocalDateTime.now()));
	 * 
	 * }
	 * 
	 * @Test(expected=HttpClientErrorException.class) public void
	 * decryptFailedExceptionTest() { HttpClientErrorException ex = new
	 * HttpClientErrorException(HttpStatus.OK);
	 * Mockito.when(restTemplate.exchange(Mockito.anyString(),
	 * Mockito.eq(HttpMethod.POST), Mockito.any(), Mockito.eq(new
	 * ParameterizedTypeReference<ResponseWrapper<CryptoManagerResponseDTO>>() {
	 * }))).thenThrow(ex); crypto.decrypt("hello".getBytes(),LocalDateTime.now());
	 * 
	 * }
	 */


	@Test
	public void encryptPiiData_success() {
		final CryptoManagerResponseDTO cryptoResponse = new CryptoManagerResponseDTO();
		cryptoResponse.setData("ciphertext-abc123");
		final ResponseWrapper<CryptoManagerResponseDTO> wrappedResponse = new ResponseWrapper<>();
		wrappedResponse.setResponse(cryptoResponse);
		final ResponseEntity<ResponseWrapper<CryptoManagerResponseDTO>> responseEntity = new ResponseEntity<>(
				wrappedResponse,
				HttpStatus.OK
		);
		Mockito.when(restTemplate.exchange(
				Mockito.anyString(),
				eq(HttpMethod.POST),
				Mockito.any(),
				eq(new ParameterizedTypeReference<ResponseWrapper<CryptoManagerResponseDTO>>() {})
		)).thenReturn(responseEntity);
		final String out = crypto.encryptPiiData("hello", LocalDateTime.now());
		assertEquals("ciphertext-abc123", out);
	}

	@Test
	public void encryptPiiData_serviceReturnsErrors_throwsEncryptionFailedException() {
		final ResponseWrapper<CryptoManagerResponseDTO> wrappedResponse = new ResponseWrapper<>();
		final ServiceError error = new ServiceError();
		error.setErrorCode("CRYPTO-ERR");
		error.setMessage("Failed");
		wrappedResponse.setErrors(Collections.singletonList(error));
		final ResponseEntity<ResponseWrapper<CryptoManagerResponseDTO>> responseEntity = new ResponseEntity<>(
				wrappedResponse,
				HttpStatus.OK
		);
		Mockito.when(restTemplate.exchange(
				Mockito.anyString(),
				eq(HttpMethod.POST),
				Mockito.any(),
				eq(new ParameterizedTypeReference<ResponseWrapper<CryptoManagerResponseDTO>>() {})
		)).thenReturn(responseEntity);
		assertThrows(
				io.mosip.preregistration.core.exception.EncryptionFailedException.class,
				() -> crypto.encryptPiiData("hello", LocalDateTime.now())
		);
	}

	@Test
	public void encryptPiiData_restTemplateThrows_propagates() {
		final HttpClientErrorException errorException = new HttpClientErrorException(HttpStatus.BAD_REQUEST);
		Mockito.when(restTemplate.exchange(
				Mockito.anyString(),
				eq(HttpMethod.POST),
				Mockito.any(),
				eq(new ParameterizedTypeReference<ResponseWrapper<CryptoManagerResponseDTO>>() {})
		)).thenThrow(errorException);
		assertThrows(
				HttpClientErrorException.class,
				() -> crypto.encryptPiiData("hello", LocalDateTime.now())
		);
	}

	@Test
	public void decryptPiiData_success() {
		final byte[] plain = "world".getBytes(StandardCharsets.UTF_8);
		final String base64 = Base64.encodeBase64String(plain);
		final CryptoManagerResponseDTO cryptoResponse = new CryptoManagerResponseDTO();
		cryptoResponse.setData(base64);
		final ResponseWrapper<CryptoManagerResponseDTO> wrappedResponse = new ResponseWrapper<>();
		wrappedResponse.setResponse(cryptoResponse);
		final ResponseEntity<ResponseWrapper<CryptoManagerResponseDTO>> responseEntity = new ResponseEntity<>(
				wrappedResponse,
				HttpStatus.OK
		);
		Mockito.when(restTemplate.exchange(
				Mockito.anyString(),
				eq(HttpMethod.POST),
				Mockito.any(),
				eq(new ParameterizedTypeReference<ResponseWrapper<CryptoManagerResponseDTO>>() {})
		)).thenReturn(responseEntity);
		final String out = crypto.decryptPiiData("ignored-ciphertext", LocalDateTime.now());
		assertEquals(java.util.Arrays.toString(plain), out);
	}

	@Test
	public void decryptPiiData_serviceReturnsErrors_throwsEncryptionFailedException() {
		final ResponseWrapper<CryptoManagerResponseDTO> wrappedResponse = new ResponseWrapper<>();
		final ServiceError error = new ServiceError();
		error.setErrorCode("CRYPTO-ERR");
		error.setMessage("Failed");
		wrappedResponse.setErrors(Collections.singletonList(error));
		final ResponseEntity<ResponseWrapper<CryptoManagerResponseDTO>> responseEntity = new ResponseEntity<>(
				wrappedResponse,
				HttpStatus.OK
		);
		Mockito.when(restTemplate.exchange(
				Mockito.anyString(),
				eq(HttpMethod.POST),
				Mockito.any(),
				eq(new ParameterizedTypeReference<ResponseWrapper<CryptoManagerResponseDTO>>() {})
		)).thenReturn(responseEntity);
		assertThrows(
				io.mosip.preregistration.core.exception.EncryptionFailedException.class,
				() -> crypto.decryptPiiData("cipher", LocalDateTime.now())
		);
	}

	@Test
	public void decryptPiiData_restTemplateThrows_propagates() {
		final HttpClientErrorException errorException = new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR);
		Mockito.when(restTemplate.exchange(
				Mockito.anyString(),
				eq(HttpMethod.POST),
				Mockito.any(),
				eq(new ParameterizedTypeReference<ResponseWrapper<CryptoManagerResponseDTO>>() {})
		)).thenThrow(errorException);
		assertThrows(
				HttpClientErrorException.class,
				() -> crypto.decryptPiiData("cipher", LocalDateTime.now())
		);
	}

}
