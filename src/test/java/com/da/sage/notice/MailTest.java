/*********************************************************************************************************************
 * @Author                : Robert Huang<56649783@qq.com>                                                            *
 * @CreatedDate           : 2025-07-03 14:02:15                                                                      *
 * @LastEditors           : Robert Huang<56649783@qq.com>                                                            *
 * @LastEditDate          : 2025-07-04 23:55:40                                                                      *
 * @CopyRight             : Dedienne Aerospace China ZhuHai                                                          *
 ********************************************************************************************************************/

package com.da.sage.notice;

import org.junit.jupiter.api.Test;

import com.da.sage.notice.service.MailService;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.AddressException;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class MailTest {

  @Test
  public void sendTestEmail() throws AddressException, MessagingException {
    MailService.setSender("xxxxx@dedienne-aero.com");
    MailService.setPassword("xxxxx");

    MailService.sendEmail(
        "[SageAssistant]",
        "这个是测试的邮件。<br>请忽略。",
        "SAGE_ASSISTANT_TEST@dedienne-aero.com",
        null);
  }

}
