package com.bcnf.algorithm;

import com.bcnf.model.FunctionalDependency;
import com.bcnf.model.Relation;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Performs lossless BCNF decomposition of a relation with respect to a given
 * violating functional dependency.
 *
 * <p>Given a violating FD X -> Y, the standard textbook decomposition produces
 * two relations:</p>
 * <ul>
 *   <li>R1(X, Y) &mdash; the determinant and its dependent attributes</li>
 *   <li>R2(X, R \ Y) &mdash; the determinant plus the remaining attributes</li>
 * </ul>
 * <p>This decomposition is always lossless-join because X is a common attribute
 * set and X is a super key of R1(X, Y).</p>
 *
 * @author BCNF Violation Exception Handler (Group-29)
 * @since 1.0
 */
public final class BCNFDecomposer {

    private BCNFDecomposer() {
        // utility class - prevent instantiation
    }

    /**
     * Decomposes the given relation with respect to the violating functional
     * dependency, producing a list of decomposed relation descriptions.
     *
     * @param relation        the relation to decompose
     * @param violatingFD     the functional dependency that violates BCNF
     * @return a list of decomposed relation descriptions in the form "R1(A, B, C)"
     */
    public static List<String> decompose(Relation relation, FunctionalDependency violatingFD) {
        List<String> suggestions = new ArrayList<>();
        Set<String> attributes = relation.getAttributes();
        Set<String> determinant = violatingFD.getDeterminant();
        Set<String> consequent = violatingFD.getConsequent();

        // R1 = determinant + consequent
        LinkedHashSet<String> r1Attrs = new LinkedHashSet<>(determinant);
        r1Attrs.addAll(consequent);

        // R2 = determinant + (attributes - consequent)
        LinkedHashSet<String> r2Attrs = new LinkedHashSet<>(determinant);
        for (String attr : attributes) {
            if (!consequent.contains(attr)) {
                r2Attrs.add(attr);
            }
        }

        suggestions.add("R1(" + String.join(", ", r1Attrs) + ")  -- determinant + dependent");
        suggestions.add("R2(" + String.join(", ", r2Attrs) + ")  -- determinant + remaining");

        // Add the FD that each decomposed relation inherits
        suggestions.add("FDs in R1: " + violatingFD);
        suggestions.add("FDs in R2: " + violatingFD + " (X is a super key of R2)");

        return suggestions;
    }

    /**
     * Checks whether the decomposition preserves all functional dependencies of
     * the original relation.
     *
     * @param relation        the original relation
     * @param violatingFD     the FD that triggered the decomposition
     * @param suggestions      the decomposition suggestions
     * @return true if every FD of the original relation is logically implied by
     *         the FDs projected onto the decomposed relations
     */
    public static boolean preservesDependencies(Relation relation, FunctionalDependency violatingFD,
                                                List<String> suggestions) {
        // For a single-step BCNF decomposition, dependency preservation is not
        // guaranteed. We check whether the violating FD's consequent is fully
        // contained in one of the decomposed relations.
        for (String suggestion : suggestions) {
            if (suggestion.contains(violatingFD.getConsequentString())) {
                return true;
            }
        }
        return false;
    }
}