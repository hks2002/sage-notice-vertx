/*********************************************************************************************************************
 * @Author                : Robert Huang<56649783@qq.com>                                                            *
 * @CreatedDate           : 2025-07-14 17:01:21                                                                      *
 * @LastEditors           : Robert Huang<56649783@qq.com>                                                            *
 * @LastEditDate          : 2025-07-17 15:18:33                                                                      *
 * @CopyRight             : Dedienne Aerospace China ZhuHai                                                          *
 ********************************************************************************************************************/

package com.da.sage.notice.utils;

public class TD {
  public static String N(String s) {
    return "<td>" + s + "</td>";
  }

  public static String N(String s, String c) {
    return "<td style=\"color: " + c + "\">" + s + "</td>";
  }

  public static String R(String s) {
    return "<td align=\"right\">" + s + "</td>";
  }

  public static String R(String s, String c) {
    return "<td align=\"right\" style=\"color: " + c + "\">" + s + "</td>";
  }

  public static String C(String s) {
    return "<td align=\"center\">" + s + "</td>";
  }

  public static String C(String s, String c) {
    return "<td align=\"center\" style=\"color: " + c + "\">" + s + "</td>";
  }

  public static String L(String s) {
    return "<td align=\"left\">" + s + "</td>";
  }

  public static String L(String s, String c) {
    return "<td align=\"left\" style=\"color: " + c + "\">" + s + "</td>";
  }

  public static String N(Integer i) {
    return "<td>" + i + "</td>";
  }

  public static String N(Integer i, String c) {
    return "<td style=\"color: " + c + "\">" + i + "</td>";
  }

  public static String R(Integer i) {
    return "<td align=\"right\">" + i + "</td>";
  }

  public static String R(Integer i, String c) {
    return "<td align=\"right\" style=\"color: " + c + "\">" + i + "</td>";
  }

  public static String C(Integer i) {
    return "<td align=\"center\">" + i + "</td>";
  }

  public static String C(Integer i, String c) {
    return "<td align=\"center\" style=\"color: " + c + "\">" + i + "</td>";
  }

  public static String L(Integer i) {
    return "<td align=\"left\">" + i + "</td>";
  }

  public static String L(Integer i, String c) {
    return "<td align=\"left\" style=\"color: " + c + "\">" + i + "</td>";
  }
}
