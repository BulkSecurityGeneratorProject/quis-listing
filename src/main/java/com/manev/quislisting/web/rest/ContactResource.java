package com.manev.quislisting.web.rest;

import com.manev.quislisting.service.EmailSendingService;
import com.manev.quislisting.service.dto.ContactDTO;
import com.manev.quislisting.web.rest.util.HeaderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.LocaleResolver;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Locale;

import static com.manev.quislisting.web.rest.RestRouter.RESOURCE_API_CONTACTS;

@RestController
@RequestMapping(RESOURCE_API_CONTACTS)
public class ContactResource {

    private final Logger log = LoggerFactory.getLogger(ContactResource.class);
    private final EmailSendingService emailSendingService;

    private final LocaleResolver localeResolver;
    private final MessageSource messageSource;

    public ContactResource(EmailSendingService emailSendingService, LocaleResolver localeResolver, MessageSource messageSource) {
        this.emailSendingService = emailSendingService;
        this.localeResolver = localeResolver;
        this.messageSource = messageSource;
    }


    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> createEmailNotification(@Valid @RequestBody ContactDTO contactDTO,
                                                        HttpServletRequest request) {
        log.debug("REST request to sent ContactDTO : {}", contactDTO);

        Locale locale = localeResolver.resolveLocale(request);
        String language = locale.getLanguage();
        log.debug("Language from cookie: {}", language);

        emailSendingService.sendContactUs(contactDTO, language);
        return ResponseEntity
                .ok()
                .headers(HeaderUtil.createAlert(messageSource
                        .getMessage("page.contact.message.sent_success", null, locale), "Contacts"))
                .build();
    }

}