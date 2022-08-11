package com.atguigu.gulimall.thirdparty.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Random;

/**
 * @author 无名氏
 * @date 2022/8/3
 * @Description:
 */
@Component
public class MailComponent {

    @Autowired
    JavaMailSenderImpl mailSender;
    @Value("${spring.mail.username}")
    String username;


    public void sendMail(String fromMail,String targetMail,int length) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
        String codeNum = generateVerificationCode(length);
        System.out.println(codeNum);
        //标题
        helper.setSubject("您的验证码为：" + codeNum);
        //内容
        helper.setText("您好！,您的验证码为：" + "<h2>" + codeNum + "</h2>" + "千万不能告诉别人哦！", true);
        //邮件发送者，必须和配置文件里的一样，不然授权码匹配不上
        helper.setFrom(fromMail);
        //邮件接收者
        helper.setTo(targetMail);
        mailSender.send(mimeMessage);
        System.out.println("邮件发送成功！");
    }

    public void sendMail(String targetMail,int length) throws MessagingException {
        this.sendMail(username,targetMail,length);
    }

    public void sendMail(String targetMail) throws MessagingException {
        this.sendMail(username,targetMail,6);
    }

    /**
     * 生成指定数目的验证码
     * @param length
     * @return
     */
    private String generateVerificationCode(int length){
        String codeNum = "";
        int[] code = new int[3];
        Random random = new Random();
        //自动生成验证码
        for (int i = 0; i < length; i++) {
            int num = random.nextInt(10) + 48;
            int uppercase = random.nextInt(26) + 65;
            int lowercase = random.nextInt(26) + 97;
            code[0] = num;
            code[1] = uppercase;
            code[2] = lowercase;
            codeNum += (char) code[random.nextInt(3)];
        }
        return codeNum;
    }
}
