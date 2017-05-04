package com.manev.quislisting.domain.taxonomy.discriminator;

import com.manev.quislisting.domain.post.discriminator.DlListing;
import com.manev.quislisting.domain.taxonomy.TermTaxonomy;

import javax.persistence.*;
import java.util.Set;

@Entity
@DiscriminatorValue(value = DlLocation.TAXONOMY)
public class DlLocation extends TermTaxonomy {
    public static final String TAXONOMY = "dl-location";

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private DlLocation parent;

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<DlLocation> children;

    @ManyToMany(fetch = FetchType.LAZY, mappedBy="dlLocations")
    private Set<DlListing> dlListings;

    public DlLocation getParent() {
        return parent;
    }

    public void setParent(DlLocation parent) {
        this.parent = parent;
    }

    public Set<DlLocation> getChildren() {
        return children;
    }

    public void setChildren(Set<DlLocation> children) {
        this.children = children;
    }

    public Set<DlListing> getDlListings() {
        return dlListings;
    }

    public void setDlListings(Set<DlListing> dlListings) {
        this.dlListings = dlListings;
    }
}
