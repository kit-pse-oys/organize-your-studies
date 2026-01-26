package de.pse.oys.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Repräsentiert die Kostenmatrix einer Aufgabe.
 * Diese Matrix speichert die vom Planungsalgorithmus berechneten Kostenprofile,
 * um die optimale Platzierung von Lerneinheiten im Zeitplan zu bestimmen.
 * @author utgid
 * @version 1.0
 */
@Entity
@Table(name = "cost_matrices")
public class CostMatrix {

    /** Eindeutige Kennung der Kostenmatrix (readOnly). */
    @Id
    @Column(name = "matrixid", updatable = false)
    private UUID matrixId;

    /**
     * Die eigentlichen Kostendaten im JSON-Format.
     * Nutzt den PostgreSQL-Datentyp JSONB für effiziente Abfragen.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "costs", nullable = false)
    private String costs;

    /** Flag, das angibt, ob die Matrix aufgrund von Änderungen veraltet ist. */
    @Column(name = "is_outdated", nullable = false)
    private boolean isOutdated = false;

    /** Zeitpunkt der letzten Aktualisierung der Matrix. */
    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated = LocalDateTime.now();

    /** Die zugehörige Aufgabe (1:1 Beziehung). */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "taskid", nullable = false, unique = true)
    private Task task;

    /**
     * Standardkonstruktor für JPA/Hibernate.
     */
    public CostMatrix() {
    }

    /**
     * Erzeugt eine neue Kostenmatrix für eine spezifische Aufgabe.
     *
     * @param matrixId Eindeutige ID.
     * @param costs    Initiales Kostenprofil als JSON-String.
     * @param task     Die zugehörige Aufgabe.
     */
    public CostMatrix(UUID matrixId, String costs, Task task) {
        this.matrixId = matrixId;
        this.costs = costs;
        this.task = task;
        this.lastUpdated = LocalDateTime.now();
    }

    /**
     * Markiert die Matrix als veraltet, um eine Neuberechnung anzustoßen.
     */
    public void markAsOutdated() {
        this.isOutdated = true;
    }

    // Getter

    /** @return Die ID der Matrix. */
    public UUID getMatrixId() { return matrixId; }

    /** @return Die Kostendaten als JSON-String. */
    public String getCosts() { return costs; }

    /** @return true, wenn die Matrix neu berechnet werden muss. */
    public boolean isOutdated() { return isOutdated; }

    /** @return Den Zeitpunkt der letzten Berechnung. */
    public LocalDateTime getLastUpdated() { return lastUpdated; }

    /** @return Die verknüpfte Aufgabe. */
    public Task getTask() { return task; }

    // Setter

    /** @param costs Die neuen Kostendaten. */
    public void setCosts(String costs) {
        this.costs = costs;
        this.isOutdated = false;
        this.lastUpdated = LocalDateTime.now();
    }


}