package com.opensource.excel2code.helper;

import com.opensource.excel2code.entity.Column;
import com.opensource.excel2code.entity.Table;
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


/**
 * Excel工具类<br>包括得到所有表及字段<br>
 */
public class ExcelHelper {

    /**
     * 从Excel中得到表
     *
     * @param file       文件
     * @param readColumn 是否读取列信息
     */
    public static Map<String, Table> getAllTables(File file, boolean readColumn) throws BiffException, IOException {
        Workbook wb = Workbook.getWorkbook(file);//构造Workbook（工作薄）对象

        Map<String, Table> tables = new HashMap<String, Table>();

        //获得了Workbook对象之后，就可以通过它得到Sheet对象了
        Sheet[] sheet = wb.getSheets();

        //读取第一张表
        Sheet firSheet = sheet[0];
        for (int i = 1; i < firSheet.getRows(); i++) {//从第2张开始
            //构建一张表
            Table table = new Table($(firSheet.getRow(i), 1), $(firSheet.getRow(i), 2), $(firSheet.getRow(i), 3), $(firSheet.getRow(i), 4));

            //需要读限列信息
            if (readColumn) {
                for (int j = 1; j < sheet.length; j++) {//找出对应的表
                    String tn = $(sheet[j].getRow(1), 0);//得到表名
                    if (table.getTableName().equals(tn)) {//找到对应的表后，读取列信息
                        table.setColumns(getColumnsByTable(sheet[j]));
                        break;
                    }
                }
            }
            tables.put(table.getTableName(), table);//键为小写
        }
        wb.close();//最后关闭资源，释放内存
        return tables;
    }

    /**
     * 得到某表的所有字段
     *
     * @param tableName 表名
     */
    public static Map<String, Column> getColumnsByTable(File file, String tableName) throws BiffException, IOException {
        Map<String, Column> columns = null;
        Workbook wb = Workbook.getWorkbook(file);//构造Workbook对象

        Sheet[] sheet = wb.getSheets();

        //对每个工作表进行循环,找出对应表
        for (int i = 1; i < sheet.length; i++) {
            String tn = $(sheet[i].getRow(1), 0);//表名
            if (tableName.equals(tn)) {//找到对应的表
                columns = getColumnsByTable(sheet[i]);
            }
        }
        return columns;
    }

    /**
     * 将excel中sheet的每一行封装为Column
     */
    private static Map<String, Column> getColumnsByTable(Sheet sheet) {
        Map<String, Column> columns = new HashMap<String, Column>();
        for (int j = 1; j < sheet.getRows(); j++) {
            Cell[] cells = sheet.getRow(j);
            Column column = new Column();
            column.setTableName($(cells, 0));// 表名
            column.setFiledName($(cells, 1));// 字段名
            column.setFiledName2($(cells, 2));//中文字段名
            column.setForeignKey($(cells, 4));// 外键
            column.setDesc($(cells, 5));//备注

            // 是否为主键(默认id为主键)
            if ("id".equals(column.getFiledName().toLowerCase())) {
                column.setPrimaryKey(true);
            } else {
                column.setPrimaryKey(false);
            }

            //处理数据类型
            String type = $(cells, 3);// 类型和长度// varchat(10,3)
            if (type.indexOf("(") == -1) {// 没有长度的类型
                column.setHasLength(false);
                column.setFiledType(type);// 类型
            } else {// 有长度的类型
                column.setHasLength(true);
                int beginIndex = type.indexOf("(");
                int endIndex = type.indexOf(")");
                String typeTemp = type.substring(0, beginIndex);// 类型
                String typeLengthTemp = type.substring(beginIndex + 1, endIndex);// 长度
                int dianIndex = typeLengthTemp.indexOf(",");
                if (dianIndex == -1) {// 一位长度
                    column.setHasPrecision(false);// 无精度
                    column.setFiledLength(Integer.parseInt(typeLengthTemp.trim()));
                } else {// 多位长度
                    column.setHasPrecision(true);// 有精度
                    column.setFiledLength(Integer.parseInt(typeLengthTemp.substring(0, dianIndex).trim()));
                    column.setPrecision(Integer.parseInt(typeLengthTemp.substring(dianIndex + 1).trim()));
                }
                column.setFiledType(typeTemp);
            }
            columns.put(column.getFiledName(), column);//键为小写
        }
        return columns;
    }

    /**
     * 根据表名得到某一张表
     *
     * @param tableName  表名
     * @param readColumn 是否需要读取列信息
     */
    public static Table getTable(File file, String tableName, boolean readColumn) throws BiffException, IOException {
        Table table = null;
        Workbook wb = Workbook.getWorkbook(file);//构造Workbook对象

        //获得了Workbook对象之后，就可以通过它得到Sheet对象了
        Sheet[] sheet = wb.getSheets();

        //读取第一张表
        Sheet firSheet = sheet[0];
        for (int i = 1; i < firSheet.getRows(); i++) {
            String tn = $(firSheet.getRow(i), 2);//表名

            //找到对应的表
            if (tableName.equals(tn)) {
                table = new Table($(firSheet.getRow(i), 1), $(firSheet.getRow(i), 2), $(firSheet.getRow(i), 3), $(firSheet.getRow(i), 4));
                if (readColumn) {
                    table.setColumns(getColumnsByTable(file, tableName));
                }
                break;
            }
        }
        wb.close();//最后关闭资源，释放内存
        return table;
    }

    /**
     * 从Cell[]得到第index个cell的内容<br>
     * 如果index>=Cell.length()，或cell[index]为空,则返回""
     */
    private static String $(Cell[] cs, int index) {
        if (index >= cs.length) {
            return "";
        }
        if (cs[index] == null) {
            return "";
        }
        return cs[index].getContents();
    }
}