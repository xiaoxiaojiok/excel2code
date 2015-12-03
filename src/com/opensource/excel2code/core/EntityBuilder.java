package com.opensource.excel2code.core;

import com.opensource.excel2code.entity.Column;
import com.opensource.excel2code.entity.Table;
import com.opensource.excel2code.helper.JavaCodeHelper;
import com.opensource.excel2code.helper.TypeMapping;
import com.opensource.excel2code.window.MainWindow;

import java.util.Map;


/**
 * 实体生成类
 */
public class EntityBuilder {
    /**
     * 生成实体
     * @param window
     * @param excelTables
     */
    public static void builder(MainWindow window, Map<String, Table> excelTables) {
        for (String tableKey : excelTables.keySet()) {
            Table table = excelTables.get(tableKey);

            //通过表名取得类[如:com.test.T_DB_pserson改为com.test.PsersonInfo]
            String className = JavaCodeHelper.getClassAllName(table.getPackName() + "." + table.getTableName());

            //初始化
            JavaCodeHelper codeFile = new JavaCodeHelper(className, false, table.getTableName_ch() + JavaCodeHelper.NEWLINE + "*" + className, table.getTableName());

            codeFile.addConstructor(null);//默认构造方法

            codeFile.implementInterface("java.io.Serializable");
            codeFile.implortPage("javax.persistence.Column");
            codeFile.implortPage("javax.persistence.Entity");
            codeFile.implortPage("javax.persistence.Table");
            codeFile.implortPage("javax.persistence.Id");
            codeFile.implortPage("javax.persistence.GeneratedValue");
            codeFile.implortPage("javax.persistence.GenerationType");

            //其他属性
            for (String key : table.getColumns().keySet()) {
                Column c = table.getColumns().get(key);
                String fk = c.getForeignKey();
                if (fk != null && fk.length() > 0 && !"null".equals(fk)) {//关联字段
                    String filedName = "";
                    if (c.getFiledName() != null) {
                        filedName = c.getFiledName().trim();
                    }
                    fk = fk.replace("[", "");
                    String fks[] = fk.split("]");
                    String fullType = JavaCodeHelper.getClassAllName(fks[0]);//类型
                    codeFile.implortPage(fullType);//包入类型包
                    String type = JavaCodeHelper.classNameSubPackage(fullType)[1];
                    codeFile.addField("private", type, filedName, ";");
                    codeFile.insertDescription(filedName, c.getFiledName2(), true);//添加注解和注释
                    codeFile.addGetterAndSetter(filedName, type);//get and set
                } else {//普通字段
                    String filedName = "";
                    if (c.getFiledName() != null) {
                        filedName = c.getFiledName().trim();
                    }
                    System.out.println(c.getTableName() + "--" + c.getFiledName2() + "--" + filedName + "--->" + c.getFiledType());
                    String fullType = TypeMapping.sqlToJava(c.getFiledType());
                    String type = JavaCodeHelper.classNameSubPackage(fullType)[1];
                    codeFile.implortPage(fullType);//包入类型包
                    codeFile.addField("private", type, filedName, ";");
                    codeFile.insertDescription(filedName, c.getFiledName2(), true);//添加注解和注释
                    codeFile.addGetterAndSetter(filedName, type);//get and set
                }
            }

            try {
                codeFile.buider();//生成文件
                window.print("生成:" + className);
            } catch (Exception err) {
                err.printStackTrace();
            }
        }
    }

}