package com.bcnf.model;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a single functional dependency X -> Y where X is the determinant
 * and Y is the consequent.
 *
 * <p>A functional dependency is <em>trivial</em> if the consequent is a subset of
 * the determinant (e.g. A -> A). Trivial dependencies never violate BCNF and are
 * filtered out during validation.</p>
 *
 * @author BCNF Violation Exception Handler (Group-29)
 * @since 1.0
 */
public final class FunctionalDependency implements Serializable {

    private static final long serialVersionUID = 1L;

    private final LinkedHashSet<String> determinant;
    private final LinkedHashSet<String> consequent;

    public FunctionalDependency(Set<String> determinant, Set<String> consequent) {
        this.determinant = Objects.requireNonNull(determinant, "determinant must not be null");
        this.consequent = Objects.requireNonNull(consequent, "consequent must not be null");
        if (determinant.isEmpty()) {
            throw new IllegalArgumentException("determinant must not be empty");
        }
        if (consequent.isEmpty()) {
            throw new IllegalArgumentException("consequent must not be empty");
        }
    }

    public Set<String> getDeterminant() {
        return determinant;
    }

    public Set<String> getConsequent() {
        return consequent;
    }

    /**
     * @return the determinant as an ordered, comma separated string
     */
    public String getDeterminantString() {
        return String.join(", ", determinant);
    }

    /**
     * @return the consequent as an ordered, comma separated string
     */
    public String getConsequentString() {
        return String.join(", ", consequent);
    }

    /**
     * @return true if this is a trivial dependency (consequent is a subset of determinant)
     */
    public boolean isTrivial() {
        return determinant.containsAll(consequent);
    }

    /**
     * Computes the combined set of attributes involved in this dependency.
     */
    public Set<String> getAllAttributes() {
        LinkedHashSet<String> all = new LinkedHashSet<>(determinant);
        all.addAll(consequent);
        return all;
    }

    /**
     * @return the canonical string form "X -> Y"
     */
    @Override
    public String toString() {
        return getDeterminantString() + " -> " + getConsequentString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FunctionalDependency)) return false;
        FunctionalDependency that = (FunctionalDependency) o;
        return determinant.equals(that.determinant) && consequent.equals(that.consequent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(determinant, consequent);
    }
}