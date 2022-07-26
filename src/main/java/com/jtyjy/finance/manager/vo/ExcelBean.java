package com.jtyjy.finance.manager.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author 袁前兼
 * @Date 2021/7/7 10:27
 */
@Data
public class ExcelBean {

    private String a;
    private String b;
    private String c;
    private String d;
    private String e;
    private String f;
    private String g;
    private String h;
    private String i;
    private String j;
    private String k;
    private String l;
    private String m;
    private String n;
    private String o;
    private String p;
    private String q;
    private String r;
    private String s;
    private String t;
    private String u;
    private String v;
    private String w;
    private String x;
    private String y;
    private String z;

    public static ExcelBean transformBean(List<String> row, int errorIndex, String errorMessage) {
        ExcelBean excelBean = new ExcelBean();
        int columnSize = row.size();
        for (int i = 0; i <= errorIndex; i++) {
            if (i < errorIndex) {
                if (i < columnSize) {
                    setProperty(i, row.get(i), excelBean);
                } else {
                    setProperty(i, "", excelBean);
                }
            } else {
                setProperty(i, errorMessage, excelBean);
            }
        }
        return excelBean;
    }

    public static List<List<String>> transformList(List<ExcelBean> list) {
        List<List<String>> dataList = new ArrayList<>();
        list.forEach(v -> dataList.add(addList(v)));
        return dataList;
    }

    private static List<String> addList(ExcelBean bean) {
        List<String> row = new ArrayList<>();
        for (int i = 0; i < 26; i++) {
            String value = null;
            switch (i) {
                case 0:
                    value = bean.getA();
                    break;
                case 1:
                    value = bean.getB();
                    break;
                case 2:
                    value = bean.getC();
                    break;
                case 3:
                    value = bean.getD();
                    break;
                case 4:
                    value = bean.getE();
                    break;
                case 5:
                    value = bean.getF();
                    break;
                case 6:
                    value = bean.getG();
                    break;
                case 7:
                    value = bean.getH();
                    break;
                case 8:
                    value = bean.getI();
                    break;
                case 9:
                    value = bean.getJ();
                    break;
                case 10:
                    value = bean.getK();
                    break;
                case 11:
                    value = bean.getL();
                    break;
                case 12:
                    value = bean.getM();
                    break;
                case 13:
                    value = bean.getN();
                    break;
                case 14:
                    value = bean.getO();
                    break;
                case 15:
                    value = bean.getP();
                    break;
                case 16:
                    value = bean.getQ();
                    break;
                case 17:
                    value = bean.getR();
                    break;
                case 18:
                    value = bean.getS();
                    break;
                case 19:
                    value = bean.getT();
                    break;
                case 20:
                    value = bean.getU();
                    break;
                case 21:
                    value = bean.getV();
                    break;
                case 22:
                    value = bean.getW();
                    break;
                case 23:
                    value = bean.getX();
                    break;
                case 24:
                    value = bean.getY();
                    break;
                case 25:
                    value = bean.getZ();
                    break;
                default:
            }

            if (value == null) {
                break;
            }
            row.add(value);
        }

        return row;
    }

    private static void setProperty(int index, String value, ExcelBean excelBean) {
        switch (index) {
            case 0:
                excelBean.setA(value);
                break;
            case 1:
                excelBean.setB(value);
                break;
            case 2:
                excelBean.setC(value);
                break;
            case 3:
                excelBean.setD(value);
                break;
            case 4:
                excelBean.setE(value);
                break;
            case 5:
                excelBean.setF(value);
                break;
            case 6:
                excelBean.setG(value);
                break;
            case 7:
                excelBean.setH(value);
                break;
            case 8:
                excelBean.setI(value);
                break;
            case 9:
                excelBean.setJ(value);
                break;
            case 10:
                excelBean.setK(value);
                break;
            case 11:
                excelBean.setL(value);
                break;
            case 12:
                excelBean.setM(value);
                break;
            case 13:
                excelBean.setN(value);
                break;
            case 14:
                excelBean.setO(value);
                break;
            case 15:
                excelBean.setP(value);
                break;
            case 16:
                excelBean.setQ(value);
                break;
            case 17:
                excelBean.setR(value);
                break;
            case 18:
                excelBean.setS(value);
                break;
            case 19:
                excelBean.setT(value);
                break;
            case 20:
                excelBean.setU(value);
                break;
            case 21:
                excelBean.setV(value);
                break;
            case 22:
                excelBean.setW(value);
                break;
            case 23:
                excelBean.setX(value);
                break;
            case 24:
                excelBean.setY(value);
                break;
            case 25:
                excelBean.setZ(value);
                break;
            default:
        }
    }
}
