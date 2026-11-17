package com.bcnf.algorithm;

import com.bcnf.model.Relation;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Finds all candidate keys of a relation given its functional dependencies.
 *
 * <p>A candidate key is a minimal set of attributes whose closure contains every
 * attribute of the relation. The algorithm first computes the closure of every
 * subset of attributes (ordered by size) and keeps the minimal ones.</p>
 *
 * @author BCNF Violation Exception Handler (Group-29)
 * @since 1.0
 */
public final class CandidateKeyFinder {

    private CandidateKeyFinder() {
        // utility class - prevent instantiation
    }

    /**
     * Finds all candidate keys of the given relation.
     *
     * @param relation the relation whose candidate keys are to be found
     * @return the list of candidate keys (each key is an ordered set of attributes)
     */
    public static List<Set<String>> findCandidateKeys(Relation relation) {
        List<String> attributes = new ArrayList<>(relation.getAttributes());
        List<Set<String>> candidateKeys = new ArrayList<>();
        int n = attributes.size();

        // Iterate over all non-empty subsets, ordered by increasing size
        for (int size = 1; size <= n; size++) {
            for (Set<String> subset : subsets(attributes, size)) {
                Set<String> closure = relation.closureOf(subset);
                if (closure.containsAll(relation.getAttributes())) {
                    // Check minimality: no proper subset is already a candidate key
                    if (!isRedundant(subset, candidateKeys)) {
                        candidateKeys.add(subset);
                    }
                }
            }
        }
        return candidateKeys;
    }

    /**
     * Generates all subsets of the given size from the attribute list.
     */
    private static List<Set<String>> subsets(List<String> attributes, int size) {
        List<Set<String>> result = new ArrayList<>();
        generateSubsets(attributes, 0, size, new LinkedHashSet<>(), result);
        return result;
    }

    private static void generateSubsets(List<String> attributes, int start, int size,
                                        LinkedHashSet<String> current, List<Set<String>> result) {
        if (current.size() == size) {
            result.add(new LinkedHashSet<>(current));
            return;
        }
        for (int i = start; i < attributes.size(); i++) {
            current.add(attributes.get(i));
            generateSubsets(attributes, i + 1, size, current, result);
            current.remove(attributes.get(i));
        }
    }

    /**
     * Checks whether a candidate key is redundant given the already-found keys.
     * A key is redundant if any already-found key is a proper subset of it.
     */
    private static boolean isRedundant(Set<String> candidate, List<Set<String>> found) {
        for (Set<String> existing : found) {
            if (existing.size() < candidate.size() && candidate.containsAll(existing)) {
                return true;
            }
        }
        return false;
    }
}