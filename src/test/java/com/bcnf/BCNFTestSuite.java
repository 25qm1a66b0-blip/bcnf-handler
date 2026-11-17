package com.bcnf;

import com.bcnf.algorithm.BCNFDecomposer;
import com.bcnf.algorithm.BCNFValidator;
import com.bcnf.algorithm.CandidateKeyFinder;
import com.bcnf.exception.BCNFViolationException;
import com.bcnf.model.FunctionalDependency;
import com.bcnf.model.Relation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for the BCNF Violation Exception Handler.
 * Covers: candidate key finding, BCNF validation, exception throwing,
 * and decomposition suggestions.
 *
 * @author BCNF Violation Exception Handler (Group-29)
 */
class BCNFTestSuite {

    private Relation studentCourse;

    @BeforeEach
    void setUp() {
        // Classic textbook example: StudentCourse(StudentID, Course, Instructor)
        // FDs: (StudentID, Course) -> Instructor
        //       Instructor -> Course  (violates BCNF)
        Set<String> attrs = new LinkedHashSet<>(List.of("StudentID", "Course", "Instructor"));
        Set<FunctionalDependency> fds = new LinkedHashSet<>();
        fds.add(new FunctionalDependency(Set.of("StudentID", "Course"), Set.of("Instructor")));
        fds.add(new FunctionalDependency(Set.of("Instructor"), Set.of("Course")));
        studentCourse = new Relation("StudentCourse", attrs, fds);
    }

    @Test
    @DisplayName("Candidate Key Finder - identifies {StudentID, Course} as the only candidate key")
    void testCandidateKeys() {
        List<Set<String>> keys = CandidateKeyFinder.findCandidateKeys(studentCourse);
        assertEquals(1, keys.size(), "Should have exactly one candidate key");
        assertTrue(keys.get(0).containsAll(Set.of("StudentID", "Course")));
    }

    @Test
    @DisplayName("BCNF Validator - passes for a relation in BCNF")
    void testValidBCNF() {
        // A relation in BCNF: R(A, B, C) with FD A -> B, C and A is the key
        Set<String> attrs = new LinkedHashSet<>(List.of("A", "B", "C"));
        Set<FunctionalDependency> fds = new LinkedHashSet<>();
        fds.add(new FunctionalDependency(Set.of("A"), Set.of("B", "C")));
        Relation r = new Relation("R", attrs, fds);

        assertDoesNotThrow(() -> BCNFValidator.validate(r));
    }

    @Test
    @DisplayName("BCNF Validator - throws exception for violating FD (Instructor -> Course)")
    void testViolationThrown() {
        BCNFViolationException ex = assertThrows(BCNFViolationException.class,
                () -> BCNFValidator.validate(studentCourse));

        assertEquals("Instructor -> Course", ex.getViolatingDependency().toString());
        assertEquals("StudentCourse", ex.getRelation().getName());
        assertTrue(ex.getCandidateKeys().contains("{StudentID, Course}"));
    }

    @Test
    @DisplayName("Exception payload - contains comprehensive diagnostic info")
    void testExceptionPayload() {
        BCNFViolationException ex = assertThrows(BCNFViolationException.class,
                () -> BCNFValidator.validate(studentCourse));

        // Check message contains key elements
        String msg = ex.getMessage();
        assertTrue(msg.contains("BCNF VIOLATION DETECTED"));
        assertTrue(msg.contains("StudentCourse"));
        assertTrue(msg.contains("Instructor -> Course"));
        assertTrue(msg.contains("NOT a super key"));
        assertTrue(msg.contains("anomalies"));

        // Check properties
        assertTrue(ex.isLossless(), "BCNF decomposition is always lossless");
        assertFalse(ex.isDependencyPreserving(), "This particular FD is not dependency preserving");

        // Check suggestions
        List<String> suggestions = ex.getDecompositionSuggestions();
        assertTrue(suggestions.size() >= 2);
        assertTrue(suggestions.get(0).contains("R1"));
        assertTrue(suggestions.get(1).contains("R2"));
    }

    @Test
    @DisplayName("Decomposer - produces R1(Instructor, Course) and R2(StudentID, Course, Instructor)")
    void testDecomposition() {
        BCNFViolationException ex = assertThrows(BCNFViolationException.class,
                () -> BCNFValidator.validate(studentCourse));

        List<String> suggestions = ex.getDecompositionSuggestions();
        assertTrue(suggestions.get(0).contains("Instructor, Course"));
        assertTrue(suggestions.get(1).contains("StudentID, Course, Instructor"));
    }

    @Test
    @DisplayName("FunctionalDependency - trivial dependencies are correctly identified")
    void testTrivialDependency() {
        FunctionalDependency trivial = new FunctionalDependency(
                Set.of("A", "B"), Set.of("A"));
        assertTrue(trivial.isTrivial());

        FunctionalDependency nonTrivial = new FunctionalDependency(
                Set.of("A"), Set.of("B"));
        assertFalse(nonTrivial.isTrivial());
    }

    @Test
    @DisplayName("Relation closure - computes attribute closures correctly")
    void testClosure() {
        // For StudentCourse with FDs: SC->I, I->C
        // closure of {Instructor} should be {Instructor, Course}
        Set<String> closure = studentCourse.closureOf(Set.of("Instructor"));
        assertTrue(closure.containsAll(Set.of("Instructor", "Course")));
        assertFalse(closure.contains("StudentID"));

        // closure of {StudentID, Course} should be all attributes
        Set<String> closureSC = studentCourse.closureOf(Set.of("StudentID", "Course"));
        assertTrue(closureSC.containsAll(Set.of("StudentID", "Course", "Instructor")));
    }

    @Test
    @DisplayName("Another BCNF violation example - R(A,B,C,D) with A->B, B->C")
    void testAnotherViolation() {
        Set<String> attrs = new LinkedHashSet<>(List.of("A", "B", "C", "D"));
        Set<FunctionalDependency> fds = new LinkedHashSet<>();
        fds.add(new FunctionalDependency(Set.of("A"), Set.of("B")));
        fds.add(new FunctionalDependency(Set.of("B"), Set.of("C"))); // B is not a super key
        fds.add(new FunctionalDependency(Set.of("A", "D"), Set.of("C", "D"))); // AD is a candidate key
        Relation r = new Relation("R", attrs, fds);

        BCNFViolationException ex = assertThrows(BCNFViolationException.class,
                () -> BCNFValidator.validate(r));

        assertEquals("B -> C", ex.getViolatingDependency().toString());
    }
}