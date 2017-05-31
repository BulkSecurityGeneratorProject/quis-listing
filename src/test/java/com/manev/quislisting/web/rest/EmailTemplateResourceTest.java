package com.manev.quislisting.web.rest;

import com.manev.QuisListingApp;
import com.manev.quislisting.domain.EmailTemplate;
import com.manev.quislisting.domain.qlml.QlString;
import com.manev.quislisting.domain.qlml.StringTranslation;
import com.manev.quislisting.repository.EmailTemplateRepository;
import com.manev.quislisting.service.EmailTemplateService;
import com.manev.quislisting.service.dto.EmailTemplateDTO;
import com.manev.quislisting.service.mapper.EmailTemplateMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;

import static com.manev.quislisting.web.rest.Constants.RESOURCE_API_ADMIN_EMAIL_TEMPLATE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Created by adri on 4/5/2017.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = QuisListingApp.class)
public class EmailTemplateResourceTest {


    public static final String DEFAULT_NAME = "Default_Name";
    public static final String DEFAULT_TEXT = "Default_Text";
    public static final String DEFAULT_TEXT_BG = "Default_Text_BG";
    public static final String UPDATE_DEFAULT_NAME = "Update_Default_Name";
    public static final String UPDATE_DEFAULT_TEXT = "Update_DefaultText";
    public static final String UPDATE_DEFAULT_TEXT_BG = "Update_Default_Text_BG";

    @Autowired
    private EmailTemplateRepository emailTemplateRepository;
    @Autowired
    private EmailTemplateMapper emailTemplateMapper;
    @Autowired
    private EmailTemplateService emailTemplateService;
    @Autowired
    private MappingJackson2HttpMessageConverter jackson2HttpMessageConverter;
    @Autowired

    private PageableHandlerMethodArgumentResolver pageableHandlerMethodArgumentResolver;
    private EmailTemplate emailTemplate;
    private MockMvc restEmailNotificationMockMvc;

    public static EmailTemplate createEntity() {
        EmailTemplate emailTemplate = new EmailTemplate();
        emailTemplate.setName(DEFAULT_NAME);
        emailTemplate.setText(DEFAULT_TEXT);
        return emailTemplate;
    }

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        EmailTemplateResource emailTemplateResource = new EmailTemplateResource(emailTemplateService);
        this.restEmailNotificationMockMvc = MockMvcBuilders.standaloneSetup(emailTemplateResource)
                .setCustomArgumentResolvers(pageableHandlerMethodArgumentResolver)
                .setMessageConverters(jackson2HttpMessageConverter)
                .build();
    }

    @Before
    public void initTest() {
        emailTemplateRepository.deleteAll();
        emailTemplate = createEntity();
    }

    @Test
    @Transactional
    public void createEmailTemplate() throws Exception {
        int databaseSizeBeforeCrate = emailTemplateRepository.findAll().size();

        setDefautQlString(emailTemplate);

        EmailTemplateDTO emailTemplateDTO = emailTemplateMapper.emailTemplateToEmailTemplateDTO(emailTemplate);
        restEmailNotificationMockMvc.perform(post(RESOURCE_API_ADMIN_EMAIL_TEMPLATE)
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(emailTemplateDTO)))
                .andExpect(status().isCreated());

        List<EmailTemplate> emailTemplates = emailTemplateRepository.findAll();
        assertThat(emailTemplates).hasSize(databaseSizeBeforeCrate + 1);

        EmailTemplate emailTemplateSaved = emailTemplates.get(emailTemplates.size() - 1);
        assertThat(emailTemplateSaved.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(emailTemplateSaved.getText()).isEqualTo(DEFAULT_TEXT);

        assertThat(emailTemplateSaved.getQlString().getValue()).isEqualTo(DEFAULT_TEXT);
    }

    @Test
    @Transactional
    public void getEmailNotification() throws Exception {
        EmailTemplate emailTemplateSaved = emailTemplateRepository.saveAndFlush(emailTemplate);
        setDefautQlString(emailTemplateSaved);
        emailTemplateRepository.saveAndFlush(emailTemplateSaved);

        restEmailNotificationMockMvc.perform(get(RESOURCE_API_ADMIN_EMAIL_TEMPLATE + "/{id}",
                emailTemplateSaved.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$.id").value(emailTemplateSaved.getId()))
                .andExpect(jsonPath("$.name").value(emailTemplateSaved.getName()))
                .andExpect(jsonPath("$.text").value(emailTemplateSaved.getText()))
                .andExpect(jsonPath("$.qlString.value").value(emailTemplateSaved.getQlString().getValue()))
                .andExpect(jsonPath("$.qlString.stringTranslation[0].value")
                        .value(emailTemplateSaved.getQlString().getStringTranslation().iterator().next().getValue()));


    }

    private void setDefautQlString(EmailTemplate emailTemplateSaved) {
        QlString qlString = new QlString();
        qlString.setValue(DEFAULT_TEXT);
        qlString.setLanguageCode("en");
        qlString.setName("email-template-#" + emailTemplate.getId());
        qlString.setContext(EmailTemplateService.CONTEXT);
        qlString.setStatus(0);

        StringTranslation stringTranslationBG = new StringTranslation();
        stringTranslationBG.setLanguageCode("bg");
        stringTranslationBG.setValue(DEFAULT_TEXT_BG);
        stringTranslationBG.setQlString(qlString);
        stringTranslationBG.setStatus(Boolean.FALSE);
        stringTranslationBG.setTranslationDate(ZonedDateTime.now());

        qlString.addStringTranslation(stringTranslationBG);

        emailTemplateSaved.setQlString(qlString);
    }

    @Test
    @Transactional
    public void getNonExistingEmailNotification() throws Exception {
        restEmailNotificationMockMvc.perform(get(RESOURCE_API_ADMIN_EMAIL_TEMPLATE + "/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateEmailNotification() throws Exception {
        setDefautQlString(emailTemplate);
        emailTemplateRepository.saveAndFlush(emailTemplate);
        int databaseSizeBeforeUpdate = emailTemplateRepository.findAll().size();

        EmailTemplate updateEmailTemplate = emailTemplateRepository.findOne(this.emailTemplate.getId());
        updateEmailTemplate.setText(UPDATE_DEFAULT_TEXT);
        updateEmailTemplate.setName(UPDATE_DEFAULT_NAME);
        updateEmailTemplate.getQlString().setValue(UPDATE_DEFAULT_TEXT);
        updateEmailTemplate.getQlString().getStringTranslation().iterator().next().setValue(UPDATE_DEFAULT_TEXT_BG);

        EmailTemplateDTO updateEmailTemplateDTO = emailTemplateMapper.emailTemplateToEmailTemplateDTO(updateEmailTemplate);
        restEmailNotificationMockMvc.perform(put(RESOURCE_API_ADMIN_EMAIL_TEMPLATE)
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(updateEmailTemplateDTO)))
                .andExpect(status().isOk());

        List<EmailTemplate> all = emailTemplateRepository.findAll();
        assertThat(all).hasSize(databaseSizeBeforeUpdate);
        EmailTemplate emailTemplateSaved = emailTemplateRepository.findOne(updateEmailTemplate.getId());
        assertThat(emailTemplateSaved.getName()).isEqualTo(UPDATE_DEFAULT_NAME);
        assertThat(emailTemplateSaved.getText()).isEqualTo(UPDATE_DEFAULT_TEXT);

        // verify also that qlString is created
        assertThat(emailTemplateSaved.getQlString().getValue()).isEqualTo(UPDATE_DEFAULT_TEXT);
        assertThat(emailTemplateSaved.getQlString().getStringTranslation().iterator().next().getValue()).isEqualTo(UPDATE_DEFAULT_TEXT_BG);
    }

    @Test
    @Transactional
    public void updateNoneExistingEmailNotification() throws Exception {
        setDefautQlString(emailTemplate);
        int databaseSizeBeforeUpdate = emailTemplateRepository.findAll().size();
        EmailTemplateDTO emailTemplateDTO = emailTemplateMapper.emailTemplateToEmailTemplateDTO(emailTemplate);
        restEmailNotificationMockMvc.perform(put(RESOURCE_API_ADMIN_EMAIL_TEMPLATE)
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(emailTemplateDTO)))
                .andExpect(status().isCreated());

        List<EmailTemplate> all = emailTemplateRepository.findAll();
        assertThat(all).hasSize(databaseSizeBeforeUpdate + 1);

    }

    @Test
    @Transactional
    public void deleteEmailNotification() throws Exception {
        emailTemplateRepository.saveAndFlush(emailTemplate);
        int databaseSizeBeforeDelete = emailTemplateRepository.findAll().size();

        restEmailNotificationMockMvc.perform(delete(RESOURCE_API_ADMIN_EMAIL_TEMPLATE + "/{id}",
                emailTemplate.getId())
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());

        List<EmailTemplate> all = emailTemplateRepository.findAll();
        assertThat(all).hasSize(databaseSizeBeforeDelete - 1);

    }

}
