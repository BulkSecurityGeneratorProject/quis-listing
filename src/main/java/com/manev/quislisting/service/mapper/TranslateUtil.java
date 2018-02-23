package com.manev.quislisting.service.mapper;

import com.manev.quislisting.domain.DlContentField;
import com.manev.quislisting.domain.DlContentFieldItem;
import com.manev.quislisting.domain.qlml.QlString;
import com.manev.quislisting.domain.qlml.StringTranslation;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.Set;

public class TranslateUtil {

    private TranslateUtil() {
        // private constructor
    }

    public static String getTranslatedString(DlContentFieldItem dlContentFieldItem, String languageCode) {
        if (!StringUtils.isEmpty(languageCode) && dlContentFieldItem.getQlString() != null) {
            QlString qlString = dlContentFieldItem.getQlString();
            String translation = searchString(qlString, languageCode);
            if (!StringUtils.isEmpty(translation)) return translation;
        }
        return dlContentFieldItem.getValue();
    }

    public static String getTranslatedString(DlContentField dlContentField, String languageCode) {
        if (!StringUtils.isEmpty(languageCode) && dlContentField.getQlString() != null) {
            QlString qlString = dlContentField.getQlString();
            String translation = searchString(qlString, languageCode);
            if (!StringUtils.isEmpty(translation)) return translation;
        }
        return dlContentField.getName();
    }

    public static String getTranslatedStringDescription(DlContentField dlContentField, String languageCode) {
        if (!StringUtils.isEmpty(languageCode) && dlContentField.getQlStringDescription() != null) {
            QlString qlString = dlContentField.getQlStringDescription();
            String translation = searchString(qlString, languageCode);
            if (!StringUtils.isEmpty(translation)) return translation;
        }
        return dlContentField.getDescription();
    }

    private static String searchString(QlString qlString, String languageCode) {
        Set<StringTranslation> stringTranslation = qlString.getStringTranslation();

        if (!CollectionUtils.isEmpty(stringTranslation)) {
            for (StringTranslation translation : stringTranslation) {
                if (translation.getLanguageCode().equals(languageCode)) {
                    return translation.getValue();
                }
            }
        }

        return null;
    }
}
