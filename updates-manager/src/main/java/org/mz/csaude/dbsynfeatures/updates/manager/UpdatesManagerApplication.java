package org.mz.csaude.dbsynfeatures.updates.manager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;

@SpringBootApplication
@ComponentScans({
		@ComponentScan("org.mz.csaude.dbsynfeatures.core.manager"),
})
public class UpdatesManagerApplication {

	public static void main(String[] args) {
		SpringApplication.run(UpdatesManagerApplication.class, args);
	}

}
