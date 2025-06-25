package org.dbflute.logic.doc.lreverse.order;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.torque.engine.database.model.Database;
import org.apache.torque.engine.database.model.ForeignKey;
import org.apache.torque.engine.database.model.Table;
import org.dbflute.unit.EngineTestCase;
import org.dbflute.util.DfCollectionUtil;

/**
 * @author jflute
 * @since 1.3.0 (2025/06/25 Wednesday at ichihara)
 */
public class DfLReverseTableOrderTest extends EngineTestCase {

    public void test_analyzeOrder_cyclicHeadache() {
        // ## Arrange ##
        Database database = new Database() {
            @Override
            public boolean isAvailableSchemaDrivenTable() {
                return false;
            }
        };
        DfLReverseTableOrder tableOrder = new DfLReverseTableOrder(3);
        tableOrder.enableFrameworkDebug();
        List<Table> tableList = newArrayList();
        Table sea = registerTable(database, tableList, "SEA");
        Table hangar = registerTable(database, tableList, "HANGAR", sea);
        Table mystic = registerTable(database, tableList, "MYSTIC", hangar);
        Table land = registerTable(database, tableList, "LAND");
        Table showbase = registerTable(database, tableList, "SHOWBASE", land);
        registerTable(database, tableList, "ONEMAN", showbase);

        Table xxxCyclic = registerTable(database, tableList, "XXX_CYCLIC");
        Table yyyCyclic = registerTable(database, tableList, "YYY_CYCLIC", xxxCyclic);
        Table zzzReferrer = registerTable(database, tableList, "ZZZ_REFERRER", yyyCyclic);
        assertEquals(xxxCyclic.getName(), yyyCyclic.getForeignKeyList().get(0).getForeignTablePureName());
        registerFK(xxxCyclic, yyyCyclic);
        registerFK(mystic, xxxCyclic); // headache target
        registerFK(yyyCyclic, showbase);
        assertEquals(2, yyyCyclic.getForeignKeyList().size()); // to xxx and showbase

        Collections.sort(tableList, (o1, o2) -> o1.getName().compareTo(o2.getName())); // name order
        log(tableList.stream().map(table -> table.getName()).collect(Collectors.toList()));

        // ## Act ##
        List<List<Table>> sectionListList = tableOrder.analyzeOrder(tableList, DfCollectionUtil.emptyList());

        // ## Assert ##
        assertHasAnyElement(sectionListList);
        for (List<Table> sectionTableList : sectionListList) {
            assertHasAnyElement(sectionTableList);
            log("---");
            for (Table table : sectionTableList) {
                log(table.getName());
            }
        }
        List<Table> flatList = sectionListList.stream().flatMap(list -> list.stream()).collect(Collectors.toList());
        assertEquals(mystic.getName(), flatList.get(flatList.size() - 2).getName()); // should be after xxx/yyy
        assertEquals(zzzReferrer.getName(), flatList.get(flatList.size() - 1).getName());
    }

    // ===================================================================================
    //                                                                        Assist Logic
    //                                                                        ============
    private Table registerTable(Database database, List<Table> tableList, String tableName) {
        Table table = new Table();
        table.setName(tableName);
        table.setType("TABLE");
        tableList.add(table);
        database.addTable(table);
        return table;
    }

    private Table registerTable(Database database, List<Table> tableList, String tableName, Table foreignTable) {
        Table table = registerTable(database, tableList, tableName);
        registerFK(table, foreignTable);
        return table;
    }

    private void registerFK(Table localTable, Table foreignTable) {
        ForeignKey fk = new ForeignKey();
        fk.setName("FK_" + localTable.getName() + "_" + foreignTable.getName());
        fk.setForeignTablePureName(foreignTable.getName());
        localTable.addForeignKey(fk);
    }
}
