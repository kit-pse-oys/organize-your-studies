package de.pse.oys.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

/**
 * CostMatrix – TODO: Beschreibung ergänzen
 *
 * @author uhupo
 * @version 1.0
 */

@Entity
public class CostMatrix {
    @Id
    private Long id;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
