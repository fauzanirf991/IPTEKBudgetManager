package com.fauzan.budget.server;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

class MigrationTest {
    private MigrationTool.Input sample() throws Exception { return new Gson().fromJson(Files.readString(Path.of("../migration/sample.json")), MigrationTool.Input.class); }
    @Test void sourceSampleReconciles() throws Exception { assertDoesNotThrow(() -> MigrationTool.validate(sample())); }
    @Test void discrepancyAndOrphansAreRejectedBeforeWriting() throws Exception {
        var input = sample(); input.expectedBalances.put("KAS", 0L); assertThrows(IllegalArgumentException.class, () -> MigrationTool.validate(input));
        var orphan = sample(); orphan.records.get(4).eventId = "missing"; assertThrows(IllegalArgumentException.class, () -> MigrationTool.validate(orphan));
    }
}
