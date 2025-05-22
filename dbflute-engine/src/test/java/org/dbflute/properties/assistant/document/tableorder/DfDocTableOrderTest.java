package org.dbflute.properties.assistant.document.tableorder;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.torque.engine.database.model.Table;
import org.dbflute.unit.EngineTestCase;
import org.dbflute.util.DfCollectionUtil;

/**
 * @author jflute
 * @since 1.2.9 (2024/10/12 Saturday at nakameguro)
 */
public class DfDocTableOrderTest extends EngineTestCase {

    // ===================================================================================
    //                                                                             OrderBy
    //                                                                             =======
    public void test_default_lowerCase_basic() {
        // ## Arrange ##
        DfDocTableOrder tableOrder = new DfDocTableOrder();
        Comparator<Table> orderBy = tableOrder.createTableDisplayOrderBy();
        List<Table> tableList = prepareLowerCaseTableList();

        // ## Act ##
        Collections.sort(tableList, orderBy);

        // ## Assert ##
        log(tableList);
        assertEquals("member_address", tableList.get(0).getName());
        assertEquals("member_security", tableList.get(1).getName());
        assertEquals("member_service", tableList.get(2).getName());
        assertEquals("members", tableList.get(3).getName());
        assertEquals("membership", tableList.get(4).getName());
        assertEquals(5, tableList.size());
    }

    public void test_default_upperCase_basic() {
        // ## Arrange ##
        DfDocTableOrder tableOrder = new DfDocTableOrder();
        Comparator<Table> orderBy = tableOrder.createTableDisplayOrderBy();
        List<Table> tableList = prepareUpperCaseTableList();

        // ## Act ##
        Collections.sort(tableList, orderBy);

        // ## Assert ##
        log(tableList);
        assertEquals("MEMBERS", tableList.get(0).getName());
        assertEquals("MEMBERSHIP", tableList.get(1).getName());
        assertEquals("MEMBER_ADDRESS", tableList.get(2).getName());
        assertEquals("MEMBER_SECURITY", tableList.get(3).getName());
        assertEquals("MEMBER_SERVICE", tableList.get(4).getName());
        assertEquals(5, tableList.size());
    }

    public void test_usePluralFormHead_lowerCase_basic() {
        // ## Arrange ##
        DfDocTableOrder tableOrder = new DfDocTableOrder();
        tableOrder.usePluralFormHead();
        Comparator<Table> orderBy = tableOrder.createTableDisplayOrderBy();
        List<Table> tableList = prepareLowerCaseTableList();

        // ## Act ##
        Collections.sort(tableList, orderBy);

        // ## Assert ##
        log(tableList);
        assertEquals("members", tableList.get(0).getName());
        assertEquals("membership", tableList.get(1).getName());
        assertEquals("member_address", tableList.get(2).getName());
        assertEquals("member_security", tableList.get(3).getName());
        assertEquals("member_service", tableList.get(4).getName());
        assertEquals(5, tableList.size());
    }

    public void test_usePluralFormHead_upperCase_basic() {
        // ## Arrange ##
        DfDocTableOrder tableOrder = new DfDocTableOrder();
        tableOrder.usePluralFormHead();
        Comparator<Table> orderBy = tableOrder.createTableDisplayOrderBy();
        List<Table> tableList = prepareUpperCaseTableList();

        // ## Act ##
        Collections.sort(tableList, orderBy);

        // ## Assert ##
        log(tableList);
        assertEquals("MEMBERS", tableList.get(0).getName());
        assertEquals("MEMBERSHIP", tableList.get(1).getName());
        assertEquals("MEMBER_ADDRESS", tableList.get(2).getName());
        assertEquals("MEMBER_SECURITY", tableList.get(3).getName());
        assertEquals("MEMBER_SERVICE", tableList.get(4).getName());
        assertEquals(5, tableList.size());
    }

    // ===================================================================================
    //                                                                        Small Helper
    //                                                                        ============
    protected List<Table> prepareLowerCaseTableList() {
        List<Table> tableList = DfCollectionUtil.newArrayList();
        registerTable(tableList, "member_security");
        registerTable(tableList, "membership");
        registerTable(tableList, "member_address");
        registerTable(tableList, "members");
        registerTable(tableList, "member_service");
        return tableList;
    }

    protected List<Table> prepareUpperCaseTableList() {
        List<Table> tableList = DfCollectionUtil.newArrayList();
        registerTable(tableList, "MEMBER_SECURITY");
        registerTable(tableList, "MEMBERSHIP");
        registerTable(tableList, "MEMBER_ADDRESS");
        registerTable(tableList, "MEMBERS");
        registerTable(tableList, "MEMBER_SERVICE");
        return tableList;
    }

    protected void registerTable(List<Table> tableList, String tableName) {
        Table table = new Table();
        table.setName(tableName);
        table.setType("table");
        tableList.add(table);
    }
}
