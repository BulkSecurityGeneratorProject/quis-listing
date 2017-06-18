package com.manev.quislisting.repository;

import com.manev.quislisting.domain.DlContentField;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface DlContentFieldRepository extends JpaRepository<DlContentField, Long> {

    List<DlContentField> findAllByDlCategories_idOrDlCategoriesIsNull(Long id);
}
