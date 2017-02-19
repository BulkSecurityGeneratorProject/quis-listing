package com.manev.quislisting.repository.taxonomy;

import com.manev.quislisting.domain.taxonomy.TermTaxonomy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface TermTaxonomyRepository<T extends TermTaxonomy> extends JpaRepository<T, Long> {
}
