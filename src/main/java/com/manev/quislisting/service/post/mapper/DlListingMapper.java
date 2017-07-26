package com.manev.quislisting.service.post.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.manev.quislisting.domain.*;
import com.manev.quislisting.domain.post.discriminator.DlListing;
import com.manev.quislisting.domain.taxonomy.discriminator.DlCategory;
import com.manev.quislisting.service.dto.UserDTO;
import com.manev.quislisting.service.post.dto.DlListingDTO;
import com.manev.quislisting.service.post.dto.DlListingFieldDTO;
import com.manev.quislisting.service.taxonomy.mapper.DlCategoryMapper;
import com.manev.quislisting.service.taxonomy.mapper.DlLocationMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class DlListingMapper {

    private DlCategoryMapper dlCategoryMapper;
    private DlLocationMapper dlLocationMapper;
    private AttachmentMapper attachmentMapper;

    public DlListingMapper(DlCategoryMapper dlCategoryMapper, DlLocationMapper dlLocationMapper, AttachmentMapper attachmentMapper) {
        this.dlCategoryMapper = dlCategoryMapper;
        this.dlLocationMapper = dlLocationMapper;
        this.attachmentMapper = attachmentMapper;
    }

    public DlListingDTO dlListingToDlListingDTO(DlListing dlListing) {
        DlListingDTO dlListingDTO = new DlListingDTO();
        dlListingDTO.setId(dlListing.getId());
        dlListingDTO.setTitle(dlListing.getTitle());
        dlListingDTO.setName(dlListing.getName());
        dlListingDTO.setContent(dlListing.getContent());
        dlListingDTO.setCreated(dlListing.getCreated());
        dlListingDTO.setModified(dlListing.getModified());
        dlListingDTO.setStatus(dlListing.getStatus());
        dlListingDTO.setLanguageCode(dlListing.getTranslation().getLanguageCode());

        setDlCategories(dlListing, dlListingDTO);

        setDlLocations(dlListing, dlListingDTO);

        setAttachments(dlListing, dlListingDTO);

        setAuthor(dlListing, dlListingDTO);

        setDlListingContentFields(dlListing, dlListingDTO);

        return dlListingDTO;
    }

    private void setDlListingContentFields(DlListing dlListing, DlListingDTO dlListingDTO) {
        try {
            Set<DlListingContentFieldRel> dlListingContentFieldRels = dlListing.getDlListingContentFieldRels();
            if (dlListingContentFieldRels != null) {
                for (DlListingContentFieldRel dlListingContentFieldRel : dlListingContentFieldRels) {
                    DlContentField dlContentField = dlListingContentFieldRel.getDlContentField();
                    String value = "";
                    if (dlContentField.getType().equals(DlContentField.Type.CHECKBOX)) {
                        Set<DlContentFieldItem> dlContentFieldItems = dlListingContentFieldRel.getDlContentFieldItems();
                        List<Long> selectionIds = new ArrayList<>();
                        if (dlContentFieldItems != null) {
                            for (DlContentFieldItem dlContentFieldItem : dlContentFieldItems) {
                                selectionIds.add(dlContentFieldItem.getId());
                            }
                        }
                        value = new ObjectMapper().writeValueAsString(selectionIds);
                    } else if (dlContentField.getType().equals(DlContentField.Type.SELECT)) {
                        Set<DlContentFieldItem> dlContentFieldItems = dlListingContentFieldRel.getDlContentFieldItems();
                        if (dlContentFieldItems != null && !dlContentFieldItems.isEmpty()) {
                            value = String.valueOf(dlContentFieldItems.iterator().next().getId());
                        }
                    } else {
                        value = dlListingContentFieldRel.getValue();
                    }
                    dlListingDTO.addDlListingField(new DlListingFieldDTO(dlContentField.getId(), value));
                }
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    private void setAttachments(DlListing dlListing, DlListingDTO dlListingDTO) {
        Set<DlAttachment> dlAttachments = dlListing.getDlAttachments();
        if (dlAttachments != null && !dlAttachments.isEmpty()) {
            for (DlAttachment attachment : dlAttachments) {
                dlListingDTO.addAttachmentDto(attachmentMapper.attachmentToAttachmentDTO(attachment));
            }
        }
    }

    private void setDlLocations(DlListing dlListing, DlListingDTO dlListingDTO) {
        Set<DlListingLocationRel> dlLocations = dlListing.getDlListingLocationRels();
        if (dlLocations != null && !dlLocations.isEmpty()) {
            for (DlListingLocationRel dlListingLocationRel : dlLocations) {
                dlListingDTO.addDlLocationDto(dlLocationMapper.dlLocationToDlLocationDTO(dlListingLocationRel.getDlLocation()));
            }
        }
    }

    private void setDlCategories(DlListing dlListing, DlListingDTO dlListingDTO) {
        Set<DlCategory> dlCategories = dlListing.getDlCategories();
        if (dlCategories != null && !dlCategories.isEmpty()) {
            for (DlCategory dlCategory : dlCategories) {
                dlListingDTO.addDlCategoryDto(dlCategoryMapper.dlCategoryToDlCategoryDTO(dlCategory));
            }
        }
    }

    private void setAuthor(DlListing dlListing, DlListingDTO dlListingDTO) {
        User user = dlListing.getUser();
        dlListingDTO.setAuthor(new UserDTO(user.getId(), user.getLogin(), user.getFirstName(), user.getLastName()));
    }


}
