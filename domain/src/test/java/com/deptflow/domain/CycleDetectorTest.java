package com.deptflow.domain;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CycleDetectorTest {

    @Test
    void nullParentIsNotACycle() {
        assertThat(CycleDetector.wouldCreateCycle(UUID.randomUUID(), null, id -> null)).isFalse();
    }

    @Test
    void selfParentIsACycle() {
        UUID a = UUID.randomUUID();
        assertThat(CycleDetector.wouldCreateCycle(a, a, id -> null)).isTrue();
    }

    @Test
    void ancestorAsParentIsACycle() {
        Map<UUID, UUID> parents = new HashMap<>();
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        UUID c = UUID.randomUUID();
        parents.put(b, a); // b's parent is a
        parents.put(c, b); // c's parent is b
        // reparenting a under c would create a -> c -> b -> a
        assertThat(CycleDetector.wouldCreateCycle(a, c, parents::get)).isTrue();
    }

    @Test
    void unrelatedParentIsNotACycle() {
        Map<UUID, UUID> parents = new HashMap<>();
        UUID a = UUID.randomUUID();
        UUID x = UUID.randomUUID();
        assertThat(CycleDetector.wouldCreateCycle(a, x, parents::get)).isFalse();
    }
}
