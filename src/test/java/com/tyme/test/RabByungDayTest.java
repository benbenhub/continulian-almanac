package com.tyme.test;

import com.tyme.culture.Zodiac;
import com.tyme.rabbyung.RabByungDay;
import com.tyme.rabbyung.RabByungElement;
import com.tyme.rabbyung.RabByungYear;
import com.tyme.solar.SolarDay;
import org.junit.Assert;
import org.junit.Test;

/**
 * 藏历日测试
 *
 * @author 6tail
 */
public class RabByungDayTest {

  @Test
  public void test0() {
    Assert.assertEquals("第十六饶迥铁虎年十二月初一", SolarDay.fromYmd(1951, 1, 8).getRabByungDay().toString());
    RabByungYear y = RabByungYear.fromElementZodiac(15, RabByungElement.fromName("铁"), Zodiac.fromName("虎"));
    Assert.assertEquals("1951年1月8日", RabByungDay.fromYmd(y.getYear(), 12, 1).getSolarDay().toString());
  }

  @Test
  public void test1() {
    Assert.assertEquals("第十八饶迥铁马年十二月三十", SolarDay.fromYmd(2051, 2, 11).getRabByungDay().toString());
    RabByungYear y = RabByungYear.fromElementZodiac(17, RabByungElement.fromName("铁"), Zodiac.fromName("马"));
    Assert.assertEquals(2050, y.getYear());
    RabByungDay d = RabByungDay.fromYmd(y.getYear(), 12, 30);
    Assert.assertEquals("第十八饶迥铁马年十二月三十", d.toString());
    Assert.assertEquals("2051年2月11日", d.getSolarDay().toString());
  }

  @Test
  public void test2() {
    Assert.assertEquals("第十七饶迥木蛇年二月廿五", SolarDay.fromYmd(2025, 4, 23).getRabByungDay().toString());
    RabByungYear y = RabByungYear.fromElementZodiac(16, RabByungElement.fromName("木"), Zodiac.fromName("蛇"));
    Assert.assertEquals(2025, y.getYear());
    Assert.assertEquals("2025年4月23日", RabByungDay.fromYmd(y.getYear(), 2, 25).getSolarDay().toString());
  }

  @Test
  public void test3() {
    Assert.assertEquals("第十六饶迥铁兔年正月初二", SolarDay.fromYmd(1951, 2, 8).getRabByungDay().toString());
    RabByungYear y = RabByungYear.fromElementZodiac(15, RabByungElement.fromName("铁"), Zodiac.fromName("兔"));
    Assert.assertEquals("1951年2月8日", RabByungDay.fromYmd(y.getYear(), 1, 2).getSolarDay().toString());
  }

  @Test
  public void test4() {
    Assert.assertEquals("第十六饶迥铁虎年十二月闰十六", SolarDay.fromYmd(1951, 1, 24).getRabByungDay().toString());
    RabByungYear y = RabByungYear.fromElementZodiac(15, RabByungElement.fromName("铁"), Zodiac.fromName("虎"));
    Assert.assertEquals("1951年1月24日", RabByungDay.fromYmd(y.getYear(), 12, -16).getSolarDay().toString());
  }

  @Test
  public void test5() {
    Assert.assertEquals("第十六饶迥铁牛年五月十一", SolarDay.fromYmd(1961, 6, 24).getRabByungDay().toString());
    RabByungYear y = RabByungYear.fromElementZodiac(15, RabByungElement.fromName("铁"), Zodiac.fromName("牛"));
    Assert.assertEquals(1961, y.getYear());
    Assert.assertEquals("1961年6月24日", RabByungDay.fromYmd(y.getYear(), 5, 11).getSolarDay().toString());
  }

  @Test
  public void test6() {
    Assert.assertEquals("第十六饶迥铁兔年十二月廿八", SolarDay.fromYmd(1952, 2, 23).getRabByungDay().toString());
    RabByungYear y = RabByungYear.fromElementZodiac(15, RabByungElement.fromName("铁"), Zodiac.fromName("兔"));
    Assert.assertEquals(1951, y.getYear());
    Assert.assertEquals("1952年2月23日", RabByungDay.fromYmd(y.getYear(), 12, 28).getSolarDay().toString());
  }

  @Test
  public void test7() {
    Assert.assertEquals("第十七饶迥木蛇年二月廿九", SolarDay.fromYmd(2025, 4, 26).getRabByungDay().toString());
  }

  @Test
  public void test8() {
    Assert.assertEquals("第十七饶迥木蛇年二月廿七", SolarDay.fromYmd(2025, 4, 25).getRabByungDay().toString());
  }

}
