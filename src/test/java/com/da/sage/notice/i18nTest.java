/*********************************************************************************************************************
 * @Author                : Robert Huang<56649783@qq.com>                                                            *
 * @CreatedDate           : 2025-07-10 10:37:14                                                                      *
 * @LastEditors           : Robert Huang<56649783@qq.com>                                                            *
 * @LastEditDate          : 2025-07-14 17:58:52                                                                      *
 * @CopyRight             : Dedienne Aerospace China ZhuHai                                                          *
 ********************************************************************************************************************/

package com.da.sage.notice;

import java.util.Locale;

import org.junit.jupiter.api.Test;

import com.da.sage.notice.utils.L;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class i18nTest {

  @Test
  public void testCurrency() {
    Locale enLocale = new Locale("en", "US");
    Locale cnLocale = new Locale("zh", "CN");

    log.info(L.getNumber(98838, enLocale));
    log.info(L.getNumber(98838, cnLocale));
    log.info(L.getNumber(98838.0088348, enLocale));
    log.info(L.getNumber(98838.0088348, cnLocale));

    log.info(L.getCurrency(98838, enLocale));
    log.info(L.getCurrency(98838, cnLocale));
    log.info(L.getCurrency(98838.0088348, enLocale));
    log.info(L.getCurrency(98838.0088348, cnLocale));
  }

}
