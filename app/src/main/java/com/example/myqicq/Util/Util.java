package com.example.myqicq.Util;

import android.content.Context;
import android.view.View;

import androidx.core.content.ContextCompat;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.example.myqicq.R;
import com.google.android.material.snackbar.Snackbar;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Properties;
import java.util.Random;

/**
 * Util class for handling common functionalities such as Snackbar display,
 * email verification, and verification code generation.
 */
public class Util {

    private static final String EMAIL_ACCOUNT = "2071895341@qq.com";
    private static final String EMAIL_PASSWORD = "jdxcqkkoplgqbabd"; // SMTP Authorization Code
    private static final String SMTP_HOST = "smtp.qq.com";
    private static String verificationCode = "";

    /**
     * Displays a Snackbar with a specific type and content.
     *
     * @param type    Type of Snackbar (blue, red, yellow).
     * @param view    The view to attach the Snackbar to.
     * @param content The message content.
     * @param context The application context.
     */
    public static void showSnackBar(String type, View view, String content, Context context) {
        Snackbar snackbar = Snackbar.make(view, content, Snackbar.LENGTH_SHORT);

        int backgroundColor;
        switch (type.toLowerCase()) {
            case "blue":
                backgroundColor = ContextCompat.getColor(context, R.color.Blue);
                break;
            case "red":
                backgroundColor = ContextCompat.getColor(context, R.color.Red);
                break;
            case "yellow":
                backgroundColor = ContextCompat.getColor(context, R.color.Gold);
                break;
            default:
                backgroundColor = ContextCompat.getColor(context, R.color.Black);
                break;
        }

        snackbar.getView().setBackgroundColor(backgroundColor);
        snackbar.show();
    }

    /**
     * Generates a random 4-character verification code consisting of uppercase letters.
     */
    public static void generateCode() {
        Random random = new Random();
        StringBuilder codeBuilder = new StringBuilder();

        for (int i = 0; i < 4; i++) {
            int type = random.nextInt(3);  // 0: 数字, 1: 大写字母, 2: 小写字母

            char randomChar;

            if (type == 0) {
                // 生成数字
                randomChar = (char) (random.nextInt(10) + '0');
            } else if (type == 1) {
                // 生成大写字母
                randomChar = (char) (random.nextInt(26) + 'A');
            } else {
                // 生成小写字母
                randomChar = (char) (random.nextInt(26) + 'a');
            }

            codeBuilder.append(randomChar);
        }

        verificationCode = codeBuilder.toString();
    }


    /**
     * Sends a verification email to the specified recipient.
     *
     * @param email   Recipient email address.
     * @param view    The view to attach Snackbar messages to.
     * @param context The application context.
     */
    public static void sendEmail(String email, View view, Context context) {
        new Thread(() -> {
            Properties props = new Properties();
            props.put("mail.smtp.host", SMTP_HOST);
            props.put("mail.smtp.port", "25");
            props.put("mail.smtp.starttls.enable", "true");

            Session session = Session.getInstance(props, null);
            try {
                MimeMessage message = new MimeMessage(session);
                message.setFrom(new InternetAddress(EMAIL_ACCOUNT, "czk", "UTF-8"));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
                message.setSubject("Account Verification Code");
                message.setSentDate(new Date());
                message.setText("Your verification code is: " + verificationCode);

                Transport.send(message, EMAIL_ACCOUNT, EMAIL_PASSWORD);

                showSnackBar("blue", view, "验证码已发送!", context);
            }
            catch (MessagingException | UnsupportedEncodingException e) {
                showSnackBar("red", view, "验证码发送失败, 请重试.", context);
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Gets the current verification code.
     *
     * @return The verification code.
     */
    public static String getVerificationCode() {
        return verificationCode;
    }

    /**
     * Sets a new verification code.
     *
     * @param code The new verification code.
     */
    public static void setVerificationCode(String code) {
        verificationCode = code;
    }

}
