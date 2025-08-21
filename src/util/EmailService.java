package util;
import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Content;

import java.io.IOException;
import java.lang.reflect.Method;
import io.github.cdimascio.dotenv.Dotenv;
public class EmailService {

	private static final Dotenv dotenv = Dotenv.load();
	private static final String SENDGRID_API_KEY = dotenv.get("SENDGRID_API_KEY");
    private static final String FROM_EMAIL = "gauravsharma111199@gmail.com";
    private static final String FROM_NAME = "Gaurav Sharma";

    public static boolean sendVerificationEmail(String toEmail, String toName, String verificationToken) {
    	
    	System.out.println("SENDGRID API KEY (loaded): " + dotenv.get("SENDGRID_API_KEY"));

        String subject = "Verify your email – Personal Finance Dashboard";
        String verificationUrl = "http://localhost:8000/verify-email?token=" + verificationToken;

        String content = String.format(
            "Hi %s,\n\nThank you for registering! Please verify your email by clicking the link below:\n\n%s\n\n" +
            "If you didn't request this, you can ignore this email.\n\nThanks,\nGaurav Sharma",
            toName, verificationUrl
        );

        //constructing email to sent to the user
        Email from = new Email(FROM_EMAIL, FROM_NAME);
        Email to = new Email(toEmail);
        Content emailContent = new Content("text/plain", content);
        Mail mail = new Mail(from, subject, to, emailContent);

        SendGrid sg = new SendGrid(SENDGRID_API_KEY);
        Request request = new Request();

        try {
        	request.setMethod(com.sendgrid.Method.valueOf("POST"));
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);

            // Log response for debugging
            System.out.println("Email sent. Status code: " + response.getStatusCode());
            return response.getStatusCode() == 202; // 202 = Accepted
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
