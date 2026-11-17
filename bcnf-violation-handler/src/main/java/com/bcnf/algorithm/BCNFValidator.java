package com.bcnf.algorithm;

import com.bcnf.exception.BCNFViolationException;
import com.bcnf.model.FunctionalDependency;
import com.bcnf.model.Relation;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Validates whether a relation satisfies Boyce-Codd Normal Form (BCNF).
 *
 * <p>A relation is in BCNF if and only if for every non-trivial functional
 * dependency X -> Y, the determinant X is a <em>super key</em> of the relation
 * (i.e. the closure of X under the FDs contains every attribute of the relation).</p>
 *
 * <p>The algorithm used here follows the standard textbook approach:
 * <ol>
 *   <li>Compute all candidate keys from the FD set.</li>
 *   <li>For each non-trivial FD, check whether the determinant is a super key.</li>
 *   <li>If a violation is found, throw a {@link BCNFViolationException} carrying
 *       the full diagnostic payload.</li>
 * </ol>
 * </p>
 *
 * @author BCNF Violation Exception Handler (Group-29)
 * @since 1.0
 */
public final class BCNFValidator {

    private BCNFValidator() {
        // utility class - prevent instantiation
    }

    /**
     * Validates the given relation against BCNF.
     *
     * <p>If the relation violates BCNF, a {@link BCNFViolationException} is thrown
     * containing the violating dependency, the candidate keys, and the suggested
     * decomposition produced by {@link BCNFDecomposer}.</p>
     *
     * @param relation the relation to validate
     * @return true if the relation is in BCNF (no exception thrown)
     * @throws BCNFViolationException if the relation is not in BCNF
     */
    public static boolean validate(Relation relation) {
        if (relation == null) {
            throw new IllegalArgumentException("relation must not be null");
        }

        List<Set<String>> candidateKeys = CandidateKeyFinder.findCandidateKeys(relation);

        for (FunctionalDependency fd : relation.getFunctionalDependencies()) {
            if (fd.isTrivial()) {
                continue; // trivial dependencies never violate BCNF
            }
            if (!isSuperKey(fd.getDeterminant(), relation, candidateKeys)) {
                // Build decomposition suggestions before throwing
                List<String> suggestions = BCNFDecomposer.decompose(relation, fd);

                boolean lossless = true; // textbook BCNF decomposition is always lossless
                boolean dependencyPreserving = !suggestions.isEmpty()
                        && BCNFDecomposer.preservesDependencies(relation, fd, suggestions);

                throw new BCNFViolationException(
                        relation,
                        fd,
                        formatCandidateKeys(candidateKeys),
                        suggestions,
                        lossless,
                        dependencyPreserving
                );
            }
        }
        return true;
    }

    /**
     * Checks whether the given determinant is a super key of the relation.
     * A super key is a set of attributes whose closure contains every attribute
     * of the relation.
     */
    private static boolean isSuperKey(Set<String> determinant, Relation relation,
                                      List<Set<String>> candidateKeys) {
        // Fast path: determinant is a superset of any candidate key
        for (Set<String> ck : candidateKeys) {
            if (determinant.containsAll(ck)) {
                return true;
            }
        }
        // Slow path: compute the closure and compare against the full attribute set
        Set<String> closure = relation.closureOf(determinant);
        return closure.containsAll(relation.getAttributes());
    }

    /**
     * Formats a list of attribute sets as ordered, comma separated strings.
     */
    private static List<String> formatCandidateKeys(List<Set<String>> candidateKeys) {
        List<String> formatted = new ArrayList<>();
        for (Set<String> ck : candidateKeys) {
            formatted.add("{" + String.join(", ", ck) + "}");
        }
        return formatted;
    }
}