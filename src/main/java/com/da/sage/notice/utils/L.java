/*********************************************************************************************************************
 * @Author                : Robert Huang<56649783@qq.com>                                                            *
 * @CreatedDate           : 2025-07-03 09:40:32                                                                      *
 * @LastEditors           : Robert Huang<56649783@qq.com>                                                            *
 * @LastEditDate          : 2025-11-07 09:25:27                                                                      *
 * @CopyRight             : Dedienne Aerospace China ZhuHai                                                          *
 ********************************************************************************************************************/

package com.da.sage.notice.utils;

import java.sql.Date;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class L {
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

  public static String getDate(Long date, Locale locale) {
    // DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.LONG, locale);
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    return dateFormat.format(new Date(date));
  }

  private static NumberFormat getNumberFormat(Locale locale) {
    return NumberFormat.getInstance(locale);
  }

  public static String getNumber(Integer n, Locale locale) {
    return getNumberFormat(locale).format(n);
  }

  public static String getNumber(Long n, Locale locale) {
    return getNumberFormat(locale).format(n);
  }

  public static String getNumber(Float n, Locale locale) {
    return getNumberFormat(locale).format(n);
  }

  public static String getNumber(Double n, Locale locale) {
    return getNumberFormat(locale).format(n);
  }

  private static NumberFormat getCurrencyFormat(Locale locale) {
    return NumberFormat.getCurrencyInstance(locale);
  }

  private static String getCurrencySymbol(Locale locale) {
    DecimalFormat decimalFormat = (DecimalFormat) DecimalFormat.getCurrencyInstance(locale);
    return decimalFormat.getDecimalFormatSymbols().getCurrencySymbol();
  }

  public static String getCurrency(Integer n, Locale locale) {
    return getCurrencyFormat(locale).format(n).replace(getCurrencySymbol(locale), "");
  }

  public static String getCurrency(Long n, Locale locale) {
    return getCurrencyFormat(locale).format(n).replace(getCurrencySymbol(locale), "");
  }

  public static String getCurrency(Float n, Locale locale) {
    return getCurrencyFormat(locale).format(n).replace(getCurrencySymbol(locale), "");
  }

  public static String getCurrency(Double n, Locale locale) {
    return getCurrencyFormat(locale).format(n).replace(getCurrencySymbol(locale), "");
  }
}