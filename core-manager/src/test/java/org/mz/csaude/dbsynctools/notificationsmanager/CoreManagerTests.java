package org.mz.csaude.dbsynctools.notificationsmanager;

import com.jcraft.jsch.JSchException;
import org.junit.jupiter.api.Test;
import org.mz.csaude.dbsyncfeatures.core.manager.CoreManagerApplication;
import org.mz.csaude.dbsyncfeatures.core.manager.utils.SSHCommandExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.ActiveProfiles;

import javax.mail.MessagingException;
import java.io.IOException;

@SpringBootTest(classes = CoreManagerApplication.class,webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("remote")
class CoreManagerTests {

	@Autowired
	private TestRestTemplate restTemplate;

	@Test
	void contextLoads() {
	}

	@Test
	public void processBashCommand_shouldLoginToEipAndExecuteUpdateCommand() throws IOException, MessagingException, JSchException, InterruptedException {
		SSHCommandExecutor sshCommandExecutor = new SSHCommandExecutor();
		sshCommandExecutor.processBashCommand("echo $system.user");
		//Assertions.assertNotNull(sshCommandExecutor);
	}

}
