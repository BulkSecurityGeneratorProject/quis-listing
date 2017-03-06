package com.manev.quislisting.service.taxonomy;

import com.manev.quislisting.domain.TranslationGroup;
import com.manev.quislisting.domain.taxonomy.discriminator.DlCategory;
import com.manev.quislisting.repository.TranslationGroupRepository;
import com.manev.quislisting.repository.taxonomy.DlCategoryRepository;
import com.manev.quislisting.service.taxonomy.dto.DlCategoryDTO;
import com.manev.quislisting.service.taxonomy.mapper.DlCategoryMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;


@Service
@Transactional
public class DlCategoryService {

    private final Logger log = LoggerFactory.getLogger(DlCategoryService.class);

    private DlCategoryRepository dlCategoryRepository;

    private TranslationGroupRepository translationGroupRepository;

    private DlCategoryMapper dlCategoryMapper;

    public DlCategoryService(DlCategoryRepository dlCategoryRepository, DlCategoryMapper dlCategoryMapper,
                             TranslationGroupRepository translationGroupRepository) {
        this.dlCategoryRepository = dlCategoryRepository;
        this.dlCategoryMapper = dlCategoryMapper;
        this.translationGroupRepository = translationGroupRepository;
    }

    public DlCategoryDTO save(DlCategoryDTO dlCategoryDTO) {
        log.debug("Request to save DlCategoryDTO : {}", dlCategoryDTO);

        DlCategory dlCategory = dlCategoryMapper.dlCategoryDTOTodlCategory(dlCategoryDTO);
        if (dlCategoryDTO.getTrGroupId() != null) {
            dlCategory.getTranslation().setTranslationGroup(translationGroupRepository.findOne(dlCategoryDTO.getTrGroupId()));
        } else {
            dlCategory.getTranslation().setTranslationGroup(new TranslationGroup());
        }
        if (dlCategoryDTO.getParentId() != null) {
            dlCategory.setParent(dlCategoryRepository.findOne(dlCategoryDTO.getParentId()));
        }
        dlCategory = dlCategoryRepository.save(dlCategory);
        return dlCategoryMapper.dlCategoryToDlCategoryDTO(dlCategory);
    }

    public Page<DlCategoryDTO> findAll(Pageable pageable, Map<String, String> allRequestParams) {
        log.debug("Request to get all DlCategoryDTO");
        Page<DlCategory> result = dlCategoryRepository.findAll(pageable);
        List<DlCategoryDTO> dlCategoryDTOs = dlCategoryMapper.dlCategoryToDlCategoryDtoFlat(result);
        return new PageImpl<>(dlCategoryDTOs, pageable, result.getTotalElements());
    }

    @Transactional(readOnly = true)
    public DlCategoryDTO findOne(Long id) {
        log.debug("Request to get DlCategoryDTO : {}", id);
        DlCategory result = dlCategoryRepository.findOne(id);
        return result != null ? dlCategoryMapper.dlCategoryToDlCategoryDTO(result) : null;
    }

    public void delete(Long id) {
        log.debug("Request to delete DlCategoryDTO : {}", id);
        dlCategoryRepository.delete(id);
    }

}
