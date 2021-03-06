package com.manev.quislisting.domain.taxonomy.discriminator.builder;

import com.manev.quislisting.domain.Translation;
import com.manev.quislisting.domain.taxonomy.discriminator.DlCategory;

public final class DlCategoryBuilder {
    private Long id;
    private String name;
    private String slug;
    private String taxonomy;
    private String description;
    private Long count = 0L;
    private DlCategory parent;
    private Translation translation;

    private DlCategoryBuilder() {
    }

    public static DlCategoryBuilder aDlCategory() {
        return new DlCategoryBuilder();
    }

    public DlCategoryBuilder withId(Long id) {
        this.id = id;
        return this;
    }

    public DlCategoryBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public DlCategoryBuilder withSlug(String slug) {
        this.slug = slug;
        return this;
    }

    public DlCategoryBuilder withTaxonomy(String taxonomy) {
        this.taxonomy = taxonomy;
        return this;
    }

    public DlCategoryBuilder withDescription(String description) {
        this.description = description;
        return this;
    }

    public DlCategoryBuilder withCount(Long count) {
        this.count = count;
        return this;
    }

    public DlCategoryBuilder withParent(DlCategory dlCategory) {
        this.parent = dlCategory;
        return this;
    }

    public DlCategoryBuilder withTranslation(Translation translation) {
        this.translation = translation;
        return this;
    }

    public DlCategory build() {
        DlCategory dlCategory = new DlCategory();
        dlCategory.setId(id);
        dlCategory.setName(name);
        dlCategory.setSlug(slug);
        dlCategory.setTaxonomy(taxonomy);
        dlCategory.setDescription(description);
        dlCategory.setCount(count);
        dlCategory.setParent(parent);
        dlCategory.setTranslation(translation);
        return dlCategory;
    }

}
