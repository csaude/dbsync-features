package org.mz.csaude.dbsyncfeatures.manager.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;

@ComponentScans({
	@ComponentScan("org.mz.csaude.dbsyncfeatures.core.manager"),
})
@SpringBootApplication
public class DbsyncManagerApplication {

	public static void main(String[] args) {
		SpringApplication.run(DbsyncManagerApplication.class, args);
	}

}
