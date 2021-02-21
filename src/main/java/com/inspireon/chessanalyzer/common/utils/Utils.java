package com.inspireon.chessanalyzer.common.utils;

import java.time.LocalDate;

public class Utils {
  public static boolean isSameMonth(LocalDate localDate1, LocalDate localDate2) {
	return localDate1.getMonth().equals(localDate2.getMonth()) && localDate1.getYear() == localDate2.getYear();
  }
}
