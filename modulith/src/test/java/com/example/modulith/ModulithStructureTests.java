package com.example.modulith;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

class ModulithStructureTests {

    @Test
    void verifiesModuleBoundaries() {
        ApplicationModules.of(MainApplication.class).verify();
    }

    @Test
    void writesModuleDocumentation() {
        var modules = ApplicationModules.of(MainApplication.class);
        new Documenter(modules).writeModulesAsPlantUml();
    }
}
