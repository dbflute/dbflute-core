package org.dbflute.logic.doc.lreverse.order;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.torque.engine.database.model.Database;
import org.apache.torque.engine.database.model.ForeignKey;
import org.apache.torque.engine.database.model.Table;
import org.dbflute.unit.EngineTestCase;
import org.dbflute.util.DfCollectionUtil;
import org.dbflute.util.Srl;

/**
 * @author jflute
 * @since 1.3.0 (2025/06/25 Wednesday at ichihara)
 */
public class DfLReverseTableOrderTest extends EngineTestCase {

    // ===================================================================================
    //                                                                              Cyclic
    //                                                                              ======
    public void test_analyzeOrder_cyclicHeadache_basic() {
        // ## Arrange ##
        Database database = new Database() {
            @Override
            public boolean isAvailableSchemaDrivenTable() { // as properties mock
                return false;
            }
        };
        DfLReverseTableOrder tableOrder = new DfLReverseTableOrder(3);
        tableOrder.enableFrameworkDebug();

        List<Table> tableList = newArrayList();
        Table sea = registerTable(database, tableList, "SEA");
        Table hangar = registerTable(database, tableList, "HANGAR", sea);
        Table mystic = registerTable(database, tableList, "MYSTIC", hangar);
        Table daichi = registerTable(database, tableList, "DAICHI", mystic); // before mystic as alphabet
        Table saChoucho = registerTable(database, tableList, "SA_CHOUCHO", mystic); // after mystic as alphabet
        Table sbChouchoRef = registerTable(database, tableList, "SB_CHOUCHO_REF", saChoucho); // before zbReferrerRef

        Table land = registerTable(database, tableList, "LAND");
        Table showbase = registerTable(database, tableList, "SHOWBASE", land);
        Table oneman = registerTable(database, tableList, "ONEMAN", showbase);
        Table zsGreen = registerTable(database, tableList, "ZS_GREEN", oneman); // after zriReferrerRef
        registerFK(daichi, land);

        Table xaCyclic = registerTable(database, tableList, "XA_CYCLIC");
        Table yaCyclic = registerTable(database, tableList, "YA_CYCLIC", xaCyclic);
        Table zaReferrer = registerTable(database, tableList, "ZA_REFERRER", yaCyclic);
        Table zbReferrerRef = registerTable(database, tableList, "ZB_REFERRER_REF", zaReferrer);
        Table zriReferrerRef = registerTable(database, tableList, "ZRI_REFERRER_REF", zaReferrer);
        assertEquals(xaCyclic.getName(), yaCyclic.getForeignKeyList().get(0).getForeignTablePureName());
        registerFK(xaCyclic, yaCyclic);
        registerFK(mystic, xaCyclic); // headache target
        registerFK(yaCyclic, showbase);
        registerFK(sbChouchoRef, zbReferrerRef);
        registerFK(zsGreen, zriReferrerRef);
        assertEquals(2, yaCyclic.getForeignKeyList().size()); // to XA and SHOWBASE

        Collections.sort(tableList, (o1, o2) -> o1.getName().compareTo(o2.getName())); // name order
        log(tableList.stream().map(table -> table.getName()).collect(Collectors.toList()));

        // ## Act ##
        List<List<Table>> sectionListList = tableOrder.analyzeOrder(tableList, DfCollectionUtil.emptyList());

        // ## Assert ##
        assertHasAnyElement(sectionListList);
        for (List<Table> sectionTableList : sectionListList) {
            assertHasAnyElement(sectionTableList);

            log("---"); // for visual check
            Integer maxLength = sectionTableList.stream().map(table -> table.getName().length()).max(Comparator.naturalOrder()).get();
            for (Table table : sectionTableList) {
                List<String> fkList =
                        table.getForeignKeyList().stream().map(fk -> fk.getForeignTablePureName()).collect(Collectors.toList());
                List<String> refList = table.getReferrerList().stream().map(ref -> ref.getTable().getName()).collect(Collectors.toList());
                String tableName = table.getName();
                String interIndent = Srl.indent(maxLength - tableName.length());
                log(tableName + interIndent + " :: fk=" + fkList + " :: ref=" + refList);
            }
        }
        /* (2025/06/26)
        o LAND     :: fk=[] :: ref=[SHOWBASE, DAICHI]
        o SEA      :: fk=[] :: ref=[HANGAR]
        o SHOWBASE :: fk=[LAND] :: ref=[ONEMAN, YA_CYCLIC]
        o ---
        o HANGAR :: fk=[SEA] :: ref=[MYSTIC]
        o ONEMAN :: fk=[SHOWBASE] :: ref=[ZS_GREEN]
        o ---
        o XA_CYCLIC        :: fk=[YA_CYCLIC] :: ref=[YA_CYCLIC, MYSTIC]
        o YA_CYCLIC        :: fk=[XA_CYCLIC, SHOWBASE] :: ref=[ZA_REFERRER, XA_CYCLIC]
        o MYSTIC           :: fk=[HANGAR, XA_CYCLIC] :: ref=[DAICHI, SA_CHOUCHO]
        o ZA_REFERRER      :: fk=[YA_CYCLIC] :: ref=[ZB_REFERRER_REF, ZRI_REFERRER_REF]
        o SA_CHOUCHO       :: fk=[MYSTIC] :: ref=[SB_CHOUCHO_REF]
        o ZB_REFERRER_REF  :: fk=[ZA_REFERRER] :: ref=[SB_CHOUCHO_REF]
        o ZRI_REFERRER_REF :: fk=[ZA_REFERRER] :: ref=[ZS_GREEN]
        o DAICHI           :: fk=[MYSTIC, LAND] :: ref=[]
        o SB_CHOUCHO_REF   :: fk=[SA_CHOUCHO, ZB_REFERRER_REF] :: ref=[]
        o ZS_GREEN         :: fk=[ONEMAN, ZRI_REFERRER_REF] :: ref=[]
         */
        List<Table> flatList = sectionListList.stream().flatMap(list -> list.stream()).collect(Collectors.toList());
        assertEquals(xaCyclic.getName(), flatList.get(flatList.size() - 10).getName());
        assertEquals(yaCyclic.getName(), flatList.get(flatList.size() - 9).getName());
        assertEquals(mystic.getName(), flatList.get(flatList.size() - 8).getName());
        assertEquals(zaReferrer.getName(), flatList.get(flatList.size() - 7).getName());
        assertEquals(saChoucho.getName(), flatList.get(flatList.size() - 6).getName());
        assertEquals(zbReferrerRef.getName(), flatList.get(flatList.size() - 5).getName());
        assertEquals(zriReferrerRef.getName(), flatList.get(flatList.size() - 4).getName());
        assertEquals(daichi.getName(), flatList.get(flatList.size() - 3).getName());
        assertEquals(sbChouchoRef.getName(), flatList.get(flatList.size() - 2).getName());
        assertEquals(zsGreen.getName(), flatList.get(flatList.size() - 1).getName());
        assertEquals(15, flatList.size());
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
        ForeignKey fk = new ForeignKey() {
            @Override
            protected boolean isSuppressReferrerRelation() { // as properties mock
                return false;
            }

            @Override
            public boolean canBeReferrer() { // to avoid non-column FK exception
                return true;
            }
        };
        fk.setName("FK_" + localTable.getName() + "_" + foreignTable.getName()); // required, as map key
        fk.setForeignTablePureName(foreignTable.getName());
        localTable.addForeignKey(fk);
        foreignTable.addReferrer(fk);
    }
}
