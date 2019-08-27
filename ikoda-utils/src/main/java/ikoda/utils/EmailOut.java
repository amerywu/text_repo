package ikoda.utils;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/***
 * Email Utility
 * @author jake
 *
 */
public class EmailOut
{

	private final static String KEY_EMAIL_FROM = "emailfrom";
	private final static String KEY_USERNAME = "username";
	private final static String KEY_HOST = "host";
	private final static String SMTP_AUTH = "mail.smtp.auth";
	private final static String SMTP_STARTTLS = "mail.smtp.starttls.enable";
	private final static String SMTP_HOST = "mail.smtp.host";
	private final static String SMTP_PORT = "mail.smtp.port";
	private final static String SMTP_CHARSET = "mail.mime.charset";

	Properties maileProperties = new Properties();

	private String message;
	private String subject;
	private String password;
	private String username;
	private String fromName;

	private String from;
	private List<String> toList = new ArrayList<String>();
	private List<String> ccList = new ArrayList<String>();
	DataSource attachmentSource;;

	public EmailOut() throws Exception
	{
		try
		{
			username = AdministrationPropertiesSingleton.getInstance().getPropertyValue(KEY_USERNAME);
			fromName = AdministrationPropertiesSingleton.getInstance().getPropertyValue(KEY_EMAIL_FROM);
			password = "wasder98";
			from = username;

			maileProperties.put(SMTP_AUTH, AdministrationPropertiesSingleton.getInstance().getPropertyValue(SMTP_AUTH));
			maileProperties.put(SMTP_STARTTLS,
					AdministrationPropertiesSingleton.getInstance().getPropertyValue(SMTP_STARTTLS));
			maileProperties.put(SMTP_HOST, AdministrationPropertiesSingleton.getInstance().getPropertyValue(SMTP_HOST));
			maileProperties.put(SMTP_PORT, AdministrationPropertiesSingleton.getInstance().getPropertyValue(SMTP_PORT));
			maileProperties.put(SMTP_CHARSET,
					AdministrationPropertiesSingleton.getInstance().getPropertyValue(SMTP_CHARSET));

			SSm.getAppLogger().debug(SMTP_AUTH + " :" + maileProperties.get(SMTP_AUTH));
			SSm.getAppLogger().debug(SMTP_STARTTLS + " :" + maileProperties.get(SMTP_STARTTLS));
			SSm.getAppLogger().debug(SMTP_HOST + " :" + maileProperties.get(SMTP_HOST));
			SSm.getAppLogger().debug(SMTP_PORT + " :" + maileProperties.get(SMTP_PORT));
			SSm.getAppLogger().debug(SMTP_HOST + " :" + maileProperties.get(SMTP_HOST));
			SSm.getAppLogger().debug(SMTP_CHARSET + " :" + maileProperties.get(SMTP_CHARSET));
			SSm.getAppLogger().debug(username);
			SSm.getAppLogger().debug(password);

		}
		catch (Exception e)
		{
			SSm.getAppLogger().error(e.getMessage(), e);
			throw new Exception(e);
		}

	}

	public void addAttachment(File file) throws Exception
	{
		try
		{

			// SSm.getAppLogger().debug("received file " + file.toString());
			attachmentSource = new FileDataSource(file);

		}
		catch (Exception e)
		{
			throw new Exception(e);
		}
	}

	public void addCC(String address)
	{
		ccList.add(address);
	}

	public void addTO(String address)
	{
		toList.add(address);
	}

	private void clear()
	{
		ccList.clear();
		toList.clear();
		subject = "";
		message = "";
		attachmentSource = null;

	}

	public String getFrom()
	{
		return from;
	}

	public String getMessage()
	{
		return message;
	}

	public String getSubject()
	{
		return subject;
	}

	private boolean isValidUTF8(byte[] input)
	{

		CharsetDecoder cs = Charset.forName("UTF-8").newDecoder();

		try
		{
			cs.decode(ByteBuffer.wrap(input));
			return true;
		}
		catch (CharacterCodingException e)
		{
			return false;
		}
	}

	public boolean send() throws Exception, MessagingException
	{
		try
		{
			SSm.getAppLogger().info("Sending to " + toList);
			String encodingOptions = "text/plain; charset=UTF-8";
			// Properties mailProps = new Properties();

			// Set properties required to connect to Gmail's SMTP server

			// SSm.getLogger().debug(username);
			// SSm.getLogger().debug(password);
			SSm.getAppLogger().info("authenticating.......");
			// Create a username-password authenticator to authenticate SMTP
			// session
			Authenticator authenticator = new Authenticator()
			{
				// override the getPasswordAuthentication method
				protected PasswordAuthentication getPasswordAuthentication()
				{
					return new PasswordAuthentication(username, password);
				}
			};

			SSm.getAppLogger().info("gettting session...");
			// Create the mail session
			Session session = Session.getInstance(maileProperties, authenticator);
			session.setDebug(true);

			MimeMessage mimeMessage = new MimeMessage(session);
			mimeMessage.setHeader("Content-Type", encodingOptions);

			// Set From: header field of the header.
			mimeMessage.setFrom(new InternetAddress(from, fromName));

			// Set To: header field of the header.
			for (String s : toList)
			{
				if (null == s)
				{
					throw new Exception("Email address is null");
				}
				mimeMessage.addRecipients(Message.RecipientType.TO, InternetAddress.parse(s));
			}

			for (String s : ccList)
			{
				mimeMessage.addRecipients(Message.RecipientType.CC, InternetAddress.parse(s));
			}
			if (!isValidUTF8(subject.getBytes()))
			{
				throw new Exception("Invalid utf-8");
			}

			// Set Subject: header field
			mimeMessage.setSubject(subject, "UTF-8");

			// Create the message part
			// MimeBodyPart messageBodyPart = new MimeBodyPart();
			// messageBodyPart.setContent(message, encodingOptions);

			// Create the message part
			MimeBodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setHeader("Content-Type", encodingOptions);
			// Now set the actual message
			messageBodyPart.setText(message, "utf-8", "plain");

			// Now set the actual message
			// messageBodyPart.setText(message, "utf-8", "html");

			Multipart multipart = new MimeMultipart();

			// Set text message part
			multipart.addBodyPart(messageBodyPart);

			// Part two is attachment
			if (null != attachmentSource)
			{
				messageBodyPart = new MimeBodyPart();

				messageBodyPart.setDataHandler(new DataHandler(attachmentSource));
				messageBodyPart.setFileName(attachmentSource.getName());
				multipart.addBodyPart(messageBodyPart);

			}

			// Send the complete message parts
			mimeMessage.setContent(multipart);

			// Send message
			// Transport.send(mimeMessage);

			SSm.getAppLogger().debug("Subject: " + mimeMessage.getSubject());
			SSm.getAppLogger().debug("Language " + mimeMessage.getContentLanguage());
			SSm.getAppLogger().debug("Encoding " + mimeMessage.getEncoding());

			Multipart mp = (Multipart) mimeMessage.getContent();

			for (int i = 0; i < mp.getCount(); i++)
			{
				BodyPart bodyPart = mp.getBodyPart(i);

				/*
				 * InputStream stream = bodyPart.getInputStream(); BufferedReader br = new
				 * BufferedReader(new InputStreamReader(stream));
				 * 
				 * while (br.ready()) { SSm.getLogger().debug(br.readLine()); }
				 */

			}

			Transport.send(mimeMessage);

			SSm.getAppLogger().info("\n\nSent message successfully....");

			clear();

			return true;

		}
		catch (MessagingException mex)
		{
			SSm.getAppLogger().error(mex.getMessage());
			throw mex;
		}
		catch (Exception e)
		{
			SSm.getAppLogger().error(e.getMessage(), e);
			throw new Exception(e.getMessage(), e);
		}
	}

	public void setFrom(String from)
	{
		this.from = from;
	}

	public void setMessage(String message)
	{
		this.message = message;
		SSm.getAppLogger().debug("Message: " + message);
	}

	public void setSubject(String subject)
	{
		this.subject = subject;
	}

}
