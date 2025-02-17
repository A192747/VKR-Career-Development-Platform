package org.example.mainservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

@SpringBootTest
class MainServiceApplicationTests {

	@Test
	void writeDocumentationSnippets() {

		var modules = ApplicationModules.of(MainServiceApplication.class).verify();

		new Documenter(modules)
				.writeModulesAsPlantUml()
				.writeIndividualModulesAsPlantUml();
	}

	@Test
	void createModuleDocumentation() {
		ApplicationModules modules = ApplicationModules.of(MainServiceApplication.class);
		new Documenter(modules)
				.writeDocumentation()
				.writeIndividualModulesAsPlantUml();
	}

}
