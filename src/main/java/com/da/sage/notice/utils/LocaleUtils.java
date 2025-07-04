/*********************************************************************************************************************
 * @Author                : Robert Huang<56649783@qq.com>                                                            *
 * @CreatedDate           : 2025-07-03 09:40:32                                                                      *
 * @LastEditors           : Robert Huang<56649783@qq.com>                                                            *
 * @LastEditDate          : 2025-07-03 09:40:33                                                                      *
 * @CopyRight             : Dedienne Aerospace China ZhuHai                                                          *
 ********************************************************************************************************************/

package com.da.sage.notice.utils;

import java.util.Locale;

public class LocaleUtils {
  public static Locale getLocale(String language) {
    if (language == null || language.isEmpty()) {
      return new Locale("en", "US");
    }
    if (language.contains("_")) {
      String[] arr = language.split("_");
      return new Locale(arr[0], arr[1]);
    } else {
      return new Locale(language);
    }
  }

  public static Locale getLocale(String language, String country) {
    if (language == null || language.isEmpty()) {
      return new Locale("en", "US");
    }
    return new Locale(language, country);
  }
}