package org.mz.csaude.dbsynctools.notificationsmanager;

import java.io.IOException;

import javax.mail.MessagingException;

import org.junit.jupiter.api.Test;
import org.mz.csaude.dbsyncfeatures.core.manager.utils.SSHCommandExecutor;
import org.mz.csaude.dbsyncfeatures.manager.app.DbsyncManagerApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.ActiveProfiles;

import com.jcraft.jsch.JSchException;

@SpringBootTest(classes = DbsyncManagerApplication.class,webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
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
