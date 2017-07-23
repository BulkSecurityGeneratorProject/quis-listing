package com.manev.quislisting.service.post.dto;

import com.manev.quislisting.domain.post.discriminator.DlListing;
import com.manev.quislisting.service.taxonomy.dto.DlCategoryDTO;
import com.manev.quislisting.service.taxonomy.dto.DlLocationDTO;

import java.util.ArrayList;
import java.util.List;

public class DlListingDTO extends AbstractPostDTO {

    private String expirationDate;
    private DlListing.Status status;
    private List<DlCategoryDTO> dlCategories;
    private List<DlLocationDTO> dlLocations;
    private List<DlListingFieldDTO> dlListingFields;
    private List<AttachmentDTO> attachments;

    public String getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
    }

    public DlListing.Status getStatus() {
        return status;
    }

    public void setStatus(DlListing.Status status) {
        this.status = status;
    }

    public List<DlLocationDTO> getDlLocations() {
        return dlLocations;
    }

    public void setDlLocations(List<DlLocationDTO> dlLocations) {
        this.dlLocations = dlLocations;
    }

    public List<DlCategoryDTO> getDlCategories() {
        return dlCategories;
    }

    public void setDlCategories(List<DlCategoryDTO> dlCategories) {
        this.dlCategories = dlCategories;
    }

    public void addDlCategoryDto(DlCategoryDTO dlCategoryDTO) {
        if (dlCategories == null) {
            dlCategories = new ArrayList<>();
        }
        dlCategories.add(dlCategoryDTO);
    }

    public void addDlLocationDto(DlLocationDTO dlLocationDTO) {
        if (dlLocations == null) {
            dlLocations = new ArrayList<>();
        }
        dlLocations.add(dlLocationDTO);
    }

    public List<DlListingFieldDTO> getDlListingFields() {
        return dlListingFields;
    }

    public void setDlListingFields(List<DlListingFieldDTO> dlListingFields) {
        this.dlListingFields = dlListingFields;
    }

    public void addDlListingField(DlListingFieldDTO dlListingFieldDTO) {
        if (dlListingFields == null) {
            dlListingFields = new ArrayList<>();
        }
        dlListingFields.add(dlListingFieldDTO);
    }

    public List<AttachmentDTO> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<AttachmentDTO> attachments) {
        this.attachments = attachments;
    }

    public void addAttachmentDto(AttachmentDTO attachmentDTO) {
        if (attachments == null) {
            attachments = new ArrayList<>();
        }
        attachments.add(attachmentDTO);
    }
}
