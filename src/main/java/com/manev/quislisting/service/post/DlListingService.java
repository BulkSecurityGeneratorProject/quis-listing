package com.manev.quislisting.service.post;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.manev.quislisting.domain.*;
import com.manev.quislisting.domain.post.discriminator.DlListing;
import com.manev.quislisting.domain.taxonomy.discriminator.DlCategory;
import com.manev.quislisting.domain.taxonomy.discriminator.DlLocation;
import com.manev.quislisting.repository.*;
import com.manev.quislisting.repository.post.DlListingRepository;
import com.manev.quislisting.repository.taxonomy.DlCategoryRepository;
import com.manev.quislisting.repository.taxonomy.DlLocationRepository;
import com.manev.quislisting.security.SecurityUtils;
import com.manev.quislisting.service.post.dto.AttachmentDTO;
import com.manev.quislisting.service.post.dto.DlListingDTO;
import com.manev.quislisting.service.post.dto.DlListingFieldDTO;
import com.manev.quislisting.service.post.mapper.AttachmentMapper;
import com.manev.quislisting.service.post.mapper.DlListingMapper;
import com.manev.quislisting.service.qlml.LanguageService;
import com.manev.quislisting.service.storage.StorageService;
import com.manev.quislisting.service.taxonomy.dto.ActiveLanguageDTO;
import com.manev.quislisting.service.taxonomy.dto.DlCategoryDTO;
import com.manev.quislisting.service.taxonomy.dto.DlLocationDTO;
import com.manev.quislisting.service.util.AttachmentUtil;
import com.manev.quislisting.service.util.SlugUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.jcr.RepositoryException;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.*;


@Service
@Transactional
public class DlListingService {

    private static final String USER_S_WAS_NOT_FOUND_IN_THE_DATABASE = "User : %s was not found in the database";
    private final Logger log = LoggerFactory.getLogger(DlListingService.class);

    private DlListingRepository dlListingRepository;
    private UserRepository userRepository;
    private DlCategoryRepository dlCategoryRepository;
    private DlLocationRepository dlLocationRepository;
    private DlListingMapper dlListingMapper;
    private AttachmentMapper attachmentMapper;
    private StorageService storageService;
    private LanguageService languageService;
    private DlContentFieldRepository dlContentFieldRepository;
    private DlContentFieldItemRepository dlContentFieldItemRepository;
    private DlContentFieldRelationshipRepository dlContentFieldRelationshipRepository;
    private DlAttachmentRepository dlAttachmentRepository;

    public DlListingService(DlListingRepository dlListingRepository, UserRepository userRepository,
                            DlCategoryRepository dlCategoryRepository, DlLocationRepository dlLocationRepository,
                            DlListingMapper dlListingMapper, AttachmentMapper attachmentMapper,
                            StorageService storageService, LanguageService languageService,
                            DlContentFieldRepository dlContentFieldRepository,
                            DlContentFieldItemRepository dlContentFieldItemRepository,
                            DlContentFieldRelationshipRepository dlContentFieldRelationshipRepository,
                            DlAttachmentRepository dlAttachmentRepository) {
        this.dlListingRepository = dlListingRepository;
        this.userRepository = userRepository;
        this.dlCategoryRepository = dlCategoryRepository;
        this.dlLocationRepository = dlLocationRepository;
        this.dlListingMapper = dlListingMapper;
        this.attachmentMapper = attachmentMapper;
        this.storageService = storageService;
        this.languageService = languageService;
        this.dlContentFieldRepository = dlContentFieldRepository;
        this.dlContentFieldItemRepository = dlContentFieldItemRepository;
        this.dlContentFieldRelationshipRepository = dlContentFieldRelationshipRepository;
        this.dlAttachmentRepository = dlAttachmentRepository;
    }

    public DlListingDTO save(DlListingDTO dlListingDTO) {
        log.debug("Request to save DlListingDTO : {}", dlListingDTO);
        ZonedDateTime now = ZonedDateTime.now();

        DlListing dlListingForSaving;
        if (dlListingDTO.getId() != null) {
            dlListingForSaving = dlListingRepository.findOne(dlListingDTO.getId());

            setCommonProperties(dlListingDTO, dlListingForSaving);
            dlListingForSaving.setModified(now);
        } else {
            String currentUserLogin = SecurityUtils.getCurrentUserLogin();
            Optional<User> oneByLogin = userRepository.findOneByLogin(currentUserLogin);
            if (oneByLogin.isPresent()) {
                dlListingForSaving = new DlListing();

                setCommonProperties(dlListingDTO, dlListingForSaving);

                dlListingForSaving.setStatus(DlListing.Status.DRAFT);
                dlListingForSaving.setTranslation(
                        TranslationBuilder.aTranslation()
                                .withLanguageCode(oneByLogin.get().getLangKey())
                                .withTranslationGroup(new TranslationGroup())
                                .build()
                );

                dlListingForSaving.setCreated(now);
                dlListingForSaving.setModified(now);
                dlListingForSaving.setUser(oneByLogin.get());
            } else {
                throw new UsernameNotFoundException(String.format(USER_S_WAS_NOT_FOUND_IN_THE_DATABASE, currentUserLogin));
            }
        }

        DlListing savedDlListing = dlListingRepository.save(dlListingForSaving);
        return dlListingMapper.dlListingToDlListingDTO(savedDlListing);
    }

    private void setCommonProperties(DlListingDTO dlListingDTO, DlListing dlListingForSaving) {
        dlListingForSaving.setTitle(dlListingDTO.getTitle());
        dlListingForSaving.setName(SlugUtil.getFileNameSlug(dlListingDTO.getTitle()));
        dlListingForSaving.setContent(dlListingDTO.getContent());

        setCategory(dlListingDTO, dlListingForSaving);
        setLocation(dlListingDTO, dlListingForSaving);

        List<DlListingFieldDTO> dlListingFieldDTOS = dlListingDTO.getDlListingFields();

        if (dlListingFieldDTOS != null) {
            Set<DlListingContentFieldRel> existingDlListingContentFieldRels = dlListingForSaving.getDlListingContentFieldRels();
            for (DlListingFieldDTO dlListingFieldDTO : dlListingFieldDTOS) {
                DlContentField dlContentField = dlContentFieldRepository.findOne(dlListingFieldDTO.getId());
                DlListingContentFieldRel dlListingContentFieldRel = findDlContentFieldRelationship(dlContentField, existingDlListingContentFieldRels);

                updateDlContentFieldRelationship(dlListingForSaving, dlListingFieldDTO, dlContentField, dlListingContentFieldRel);
            }
        }
    }

    private void updateDlContentFieldRelationship(DlListing dlListingForSaving, DlListingFieldDTO dlListingFieldDTO, DlContentField dlContentField, DlListingContentFieldRel dlListingContentFieldRel) {
        if (dlListingContentFieldRel != null) {
            // update existing relationship object
            setContentFieldRelationValue(dlListingFieldDTO, dlContentField, dlListingContentFieldRel);
        } else {
            // create new relationship object
            DlListingContentFieldRel newDlListingContentFieldRel = new DlListingContentFieldRel();
            newDlListingContentFieldRel.setDlContentField(dlContentField);
            newDlListingContentFieldRel.setDlListing(dlListingForSaving);
            setContentFieldRelationValue(dlListingFieldDTO, dlContentField, newDlListingContentFieldRel);
            dlListingForSaving.addDlContentFieldRelationships(newDlListingContentFieldRel);
        }
    }

    private void setContentFieldRelationValue(DlListingFieldDTO dlListingFieldDTO, DlContentField dlContentField, DlListingContentFieldRel newDlListingContentFieldRel) {
        try {
            if (dlContentField.getType().equals(DlContentField.Type.CHECKBOX)) {
                // make relation with the selection items
                if (!StringUtils.isEmpty(dlListingFieldDTO.getValue())) {
                    Long[] ids = new ObjectMapper().readValue(dlListingFieldDTO.getValue(), Long[].class);
                    List<Long> idsAsList = Arrays.asList(ids);
                    if (!idsAsList.isEmpty()) {
                        Set<DlContentFieldItem> byIdIn = dlContentFieldItemRepository.findByIdIn(idsAsList);
                        newDlListingContentFieldRel.setDlContentFieldItems(byIdIn);
                    }
                }
            } else if (dlContentField.getType().equals(DlContentField.Type.SELECT)) {
                if (!StringUtils.isEmpty(dlListingFieldDTO.getValue())) {
                    DlContentFieldItem dlContentFieldItem = dlContentFieldItemRepository.findOne(Long.valueOf(dlListingFieldDTO.getValue()));
                    Set<DlContentFieldItem> dlContentFieldItemSet = new HashSet<>();
                    dlContentFieldItemSet.add(dlContentFieldItem);
                    newDlListingContentFieldRel.setDlContentFieldItems(dlContentFieldItemSet);
                }
            } else {
                // set value
                newDlListingContentFieldRel.setValue(dlListingFieldDTO.getValue());
            }
        } catch (IOException e) {
            e.printStackTrace();
            // TODO do something
        }
    }

    private DlListingContentFieldRel findDlContentFieldRelationship(DlContentField dlContentField, Set<DlListingContentFieldRel> dlListingContentFieldRels) {
        if (dlListingContentFieldRels != null) {
            for (DlListingContentFieldRel dlListingContentFieldRel : dlListingContentFieldRels) {
                if (dlContentField.getId().equals(dlListingContentFieldRel.getDlContentField().getId())) {
                    return dlListingContentFieldRel;
                }
            }
        }

        return null;
    }

    private void setLocation(DlListingDTO dlListingDTO, DlListing dlListingForSaving) {
        // set location
        if (dlListingDTO.getDlLocations() != null && !dlListingDTO.getDlLocations().isEmpty()) {
            DlLocationDTO dlLocationDTO = dlListingDTO.getDlLocations().get(0);
            if (dlLocationDTO.getId() != -1L) {
                DlLocation dlLocation = dlLocationRepository.findOne(dlLocationDTO.getId());
                Set<DlListingLocationRel> dlListingLocationRels = dlListingForSaving.getDlListingLocationRels();
                if (dlListingLocationRels != null && !dlListingLocationRels.isEmpty()) {
                    // update
                    DlListingLocationRel dlListingLocationRel = dlListingLocationRels.iterator().next();
                    dlListingLocationRel.setDlLocation(dlLocation);
                } else {
                    // create new
                    DlListingLocationRel dlListingLocationRel = new DlListingLocationRel();
                    dlListingLocationRel.setDlLocation(dlLocation);
                    dlListingLocationRel.setDlListing(dlListingForSaving);
                    dlListingForSaving.addDlLocationRelationship(dlListingLocationRel);
                }
            }
        }
    }

    private void setCategory(DlListingDTO dlListingDTO, DlListing dlListingForSaving) {
        // set category
        if (dlListingDTO.getDlCategories() != null && !dlListingDTO.getDlCategories().isEmpty()) {
            DlCategoryDTO dlCategoryDTO = dlListingDTO.getDlCategories().get(0);
            DlCategory dlCategory = dlCategoryRepository.findOne(dlCategoryDTO.getId());
            Set<DlCategory> dlCategories = new HashSet<>();
            dlCategories.add(dlCategory);
            dlListingForSaving.setDlCategories(dlCategories);
        }
    }


    public Page<DlListingDTO> findAll(Pageable pageable, Map<String, String> allRequestParams) {
        log.debug("Request to get all DlListingDTO");
        String languageCode = allRequestParams.get("languageCode");
        Page<DlListing> result = dlListingRepository.findAllByTranslation_languageCode(pageable, languageCode);
        return result.map(dlListingMapper::dlListingToDlListingDTO);
    }

    @Transactional(readOnly = true)
    public DlListingDTO findOne(Long id) {
        log.debug("Request to get DlListingDTO: {}", id);
        DlListing result = dlListingRepository.findOne(id);
        return result != null ? dlListingMapper.dlListingToDlListingDTO(result) : null;
    }

    public void delete(Long id) {
        log.debug("Request to delete DlCategoryDTO : {}", id);
        dlListingRepository.delete(id);
    }

    public DlListingDTO deleteDlListingAttachment(Long id, Long attachmentId) throws IOException, RepositoryException {
        log.debug("Request to delete attachment with id : {}, from DlCategoryDTO : {}", attachmentId, id);
        DlListing dlListing = dlListingRepository.findOne(id);
        DlAttachment attachment = dlListing.removeAttachment(attachmentId);
        dlListing.setModified(ZonedDateTime.now());
        DlListing result = dlListingRepository.save(dlListing);

        if (attachment != null) {
            dlAttachmentRepository.delete(attachment);
            List<String> filePaths = AttachmentUtil.getFilePaths(attachment);
            storageService.delete(filePaths);
        }

        return dlListingMapper.dlListingToDlListingDTO(result);
    }

    public void publish(DlListingDTO dlListingDTO) {
        DlListing dlListing = dlListingRepository.findOne(dlListingDTO.getId());
        dlListing.setStatus(DlListing.Status.PUBLISH);
        dlListingRepository.save(dlListing);
    }

    public DlListingDTO uploadFile(Map<String, MultipartFile> fileMap, Long id) throws IOException, RepositoryException {
        DlListing dlListing = dlListingRepository.findOne(id);

        String currentUserLogin = SecurityUtils.getCurrentUserLogin();
        Optional<User> oneByLogin = userRepository.findOneByLogin(currentUserLogin);
        if (oneByLogin.isPresent()) {
            for (MultipartFile file : fileMap.values()) {
                AttachmentDTO attachmentDto = storageService.store(file);
                DlAttachment dlAttachment = attachmentMapper.attachmentDTOToAttachment(attachmentDto);
                dlAttachment.setDlListing(dlListing);
                dlListing.addDlAttachment(dlAttachment);
            }

            DlListing result = dlListingRepository.save(dlListing);
            return dlListingMapper.dlListingToDlListingDTO(result);
        } else {
            throw new UsernameNotFoundException(String.format(USER_S_WAS_NOT_FOUND_IN_THE_DATABASE, currentUserLogin));
        }
    }

    public List<ActiveLanguageDTO> findAllActiveLanguages() {
        log.debug("Request to retrieve all active languages");
        return languageService.findAllActiveLanguages(dlListingRepository);
    }

    public Page<DlListingDTO> findAllByLanguageAndUser(Pageable pageable, String language) {
        String currentUserLogin = SecurityUtils.getCurrentUserLogin();
        Optional<User> oneByLogin = userRepository.findOneByLogin(currentUserLogin);

        Page<DlListing> result = dlListingRepository.findAllByTranslation_languageCodeAndUser(pageable, language, oneByLogin.get());
        return result.map(dlListingMapper::dlListingToDlListingDTO);
    }
}
