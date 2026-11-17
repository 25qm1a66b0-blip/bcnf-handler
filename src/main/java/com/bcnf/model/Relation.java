package com.bcnf.model;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents a relational schema: a named set of attributes together with its
 * set of functional dependencies.
 *
 * <p>The attributes are stored in an insertion-ordered {@link LinkedHashSet} so
 * that the original column order is preserved for output purposes.</p>
 *
 * @author BCNF Violation Exception Handler (Group-29)
 * @since 1.0
 */
public final class Relation implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String name;
    private final LinkedHashSet<String> attributes;
    private final LinkedHashSet<FunctionalDependency> functionalDependencies;

    public Relation(String name, LinkedHashSet<String> attributes, LinkedHashSet<FunctionalDependency> functionalDependencies) {
        this.name = Objects.requireNonNull(name, "name must not be null").trim();
        if (this.name.isEmpty()) {
            throw new IllegalArgumentException("relation name must not be empty");
        }
        this.attributes = Objects.requireNonNull(attributes, "attributes must not be null");
        this.functionalDependencies = functionalDependencies == null ? new LinkedHashSet<>() : functionalDependencies;
    }

    public String getName() {
        return name;
    }

    public Set<String> getAttributes() {
        return attributes;
    }

    public Set<FunctionalDependency> getFunctionalDependencies() {
        return functionalDependencies;
    }

    /**
     * @return the attribute set as an ordered, comma separated string
     */
    public String getAttributesString() {
        return String.join(", ", attributes);
    }

    /**
     * @return a copy of the functional dependency set
     */
    public List<FunctionalDependency> getFunctionalDependencyList() {
        return List.copyOf(functionalDependencies);
    }

    /**
     * Computes the closure of a set of attributes under the relation's FDs
     * using the standard Armstrong axiom closure algorithm (O(n^2)).
     *
     * @param attributeSet the starting set of attributes
     * @return the closure of attributeSet
     */
    public Set<String> closureOf(Set<String> attributeSet) {
        Objects.requireNonNull(attributeSet, "attributeSet must not be null");
        LinkedHashSet<String> closure = new LinkedHashSet<>(attributeSet);
        boolean changed;
        do {
            changed = false;
            for (FunctionalDependency fd : functionalDependencies) {
                if (closure.containsAll(fd.getDeterminant())) {
                    if (closure.addAll(fd.getConsequent())) {
                        changed = true;
                    }
                }
            }
        } while (changed);
        return closure;
    }

    /**
     * @return a canonical, human readable representation of the relation
     */
    @Override
    public String toString() {
        return name + "(" + getAttributesString() + ")";
    }
}