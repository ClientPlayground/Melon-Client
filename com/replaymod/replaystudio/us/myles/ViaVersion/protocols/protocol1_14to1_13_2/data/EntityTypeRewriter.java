package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_14to1_13_2.data;

import com.google.common.base.Optional;
import java.util.HashMap;
import java.util.Map;

public class EntityTypeRewriter {
  private static Map<Integer, Integer> entityTypes = new HashMap<>();
  
  static {
    regEnt(6, 7);
    regEnt(7, 8);
    regEnt(8, 9);
    regEnt(9, 10);
    regEnt(10, 11);
    regEnt(11, 12);
    regEnt(12, 13);
    regEnt(13, 14);
    regEnt(14, 15);
    regEnt(15, 16);
    regEnt(16, 17);
    regEnt(17, 18);
    regEnt(18, 19);
    regEnt(19, 20);
    regEnt(20, 21);
    regEnt(21, 22);
    regEnt(22, 23);
    regEnt(23, 24);
    regEnt(24, 25);
    regEnt(25, 26);
    regEnt(26, 28);
    regEnt(27, 29);
    regEnt(28, 30);
    regEnt(29, 31);
    regEnt(30, 32);
    regEnt(31, 33);
    regEnt(32, 34);
    regEnt(33, 35);
    regEnt(34, 36);
    regEnt(35, 37);
    regEnt(36, 38);
    regEnt(37, 39);
    regEnt(38, 40);
    regEnt(39, 41);
    regEnt(40, 42);
    regEnt(41, 43);
    regEnt(42, 44);
    regEnt(43, 45);
    regEnt(44, 46);
    regEnt(45, 47);
    regEnt(46, 48);
    regEnt(47, 49);
    regEnt(48, 6);
    regEnt(49, 51);
    regEnt(50, 53);
    regEnt(51, 54);
    regEnt(52, 55);
    regEnt(53, 56);
    regEnt(54, 57);
    regEnt(55, 58);
    regEnt(56, 59);
    regEnt(57, 60);
    regEnt(58, 61);
    regEnt(59, 62);
    regEnt(60, 63);
    regEnt(61, 64);
    regEnt(62, 65);
    regEnt(63, 66);
    regEnt(64, 67);
    regEnt(65, 68);
    regEnt(66, 69);
    regEnt(67, 70);
    regEnt(68, 71);
    regEnt(69, 72);
    regEnt(70, 73);
    regEnt(71, 74);
    regEnt(72, 76);
    regEnt(73, 77);
    regEnt(74, 78);
    regEnt(75, 79);
    regEnt(76, 80);
    regEnt(77, 81);
    regEnt(78, 83);
    regEnt(79, 84);
    regEnt(80, 85);
    regEnt(81, 86);
    regEnt(82, 89);
    regEnt(83, 90);
    regEnt(84, 91);
    regEnt(85, 92);
    regEnt(86, 93);
    regEnt(87, 94);
    regEnt(88, 95);
    regEnt(89, 96);
    regEnt(90, 97);
    regEnt(91, 99);
    regEnt(92, 100);
    regEnt(93, 101);
    regEnt(94, 82);
  }
  
  private static void regEnt(int type1_13, int type1_14) {
    entityTypes.put(Integer.valueOf(type1_13), Integer.valueOf(type1_14));
  }
  
  public static Optional<Integer> getNewId(int type1_13) {
    return Optional.fromNullable(entityTypes.get(Integer.valueOf(type1_13)));
  }
}
