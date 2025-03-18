package org.mz.csaude.dbsyncfeatures.notifications.manager.central;

import org.apache.camel.builder.RouteBuilder;
import org.mz.csaude.dbsyncfeatures.core.manager.utils.ApplicationProfile;
import org.mz.csaude.dbsyncfeatures.notifications.manager.central.utils.MailConfig;
import org.mz.csaude.dbsyncfeatures.notifications.manager.model.EmailNotificationLog;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile(ApplicationProfile.CENTRAL)
public class EmailNotificationPollingRouter extends RouteBuilder {

    @Value("${email.schedule}")
    private String emailSchedule;

    @Value("${smtp.host.name}")
    private String host;

    @Value("${smtp.host.port}")
    private int port;

    @Value("${smtp.auth.user}")
    private String username;

    @Value("${smtp.auth.pass}")
    private String password;

    @Override
    public void configure() throws Exception {

        MailConfig mailConfig = new MailConfig(host, port, username, password);
        from("timer:pollDatabase?period=" + emailSchedule)
                .routeId("email-notification-polling-route")
                .to("jpa:UpdateQueue?query=SELECT l FROM EmailNotificationLog l WHERE l.emailSent = false")
                .split(body()) // Process each record individually
                .process(new EmailNotificationPollingProcessor(mailConfig))
                .process((exchange) -> {
                    boolean emailSent = (boolean) exchange.getProperty("emailSent");
                    if(emailSent){
                        EmailNotificationLog emailNotificationLog = exchange.getIn().getBody(EmailNotificationLog.class);
                        emailNotificationLog.setEmailSent(Boolean.TRUE);
                    }
                })
                .to("jpa://EmailNotificationLog?useExecuteUpdate=true")
                .log("The email of the site ${body.siteId} with subject ${body.subject} has been published");
    }
}
