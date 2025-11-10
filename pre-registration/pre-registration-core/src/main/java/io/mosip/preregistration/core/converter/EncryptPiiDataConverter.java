package io.mosip.preregistration.core.converter;


import io.mosip.kernel.core.util.DateUtils;
import io.mosip.preregistration.core.context.SpringContext;
import io.mosip.preregistration.core.util.CryptoUtil;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.LocalDateTime;
import java.util.Objects;


@Converter
public class EncryptPiiDataConverter implements AttributeConverter<String, String> {

    private CryptoUtil cryptoUtil;

    @Override
    public String convertToDatabaseColumn(String attribute) {
        this.initUtilityService();
        if (attribute == null) return null;
        LocalDateTime encryptedTimestamp = DateUtils.getUTCCurrentDateTime();
        return this.cryptoUtil.encryptPiiData(attribute, encryptedTimestamp);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        this.initUtilityService();
        if (dbData == null) return null;
        LocalDateTime decodedTimestamp = DateUtils.getUTCCurrentDateTime();
        return this.cryptoUtil.decryptPiiData(dbData, decodedTimestamp);
    }

    private void initUtilityService() {
        if (Objects.isNull(this.cryptoUtil)) {
            try {
                this.cryptoUtil = SpringContext.getBean(CryptoUtil.class);
            } catch (IllegalStateException exception) {
                final String errorMessage = "SpringContext not initialized yet: avoid using EncryptPiiDataConverter before the app context is ready";
                throw new IllegalStateException(errorMessage, exception);
            }
        }
    }

}
