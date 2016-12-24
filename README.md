DBFlute
=======================
DBFlute core libraries for Java8

- dbflute-engine: class generator tool
- dbflute-runtime: jar library for application

## to DB change
DBFlute has tolerance to DB change.

- Lean Startup & Incremental Development
- Implementing with Designing Development

## Example Code

*ConditionBean
```java
List<Member> memberList = memberBhv.selectList(cb -> {
    cb.setupSelect_MemberStatus();
    cb.query().setMemberName_LikeSearch("S", op -> op.likePrefix());
    cb.query().existsPurchase(purchaseCB -> {
        purchaseCB.query().setPurchasePrice_GreaterEqual(200);
    });
    cb.query().addOrderBy_Birthdate_Desc();
});
```

```sql
select dfloc.MEMBER_ID as MEMBER_ID, dfloc.MEMBER_NAME as ...
     , dfrel_0.MEMBER_STATUS_CODE as MEMBER_STATUS_CODE_0, ...
  from MEMBER dfloc
    inner join MEMBER_STATUS dfrel_0 on dfloc.MEMBER_STATUS_CODE = dfrel_0.MEMBER_STATUS_CODE
 where dfloc.MEMBER_NAME like 'S%' escape '|'
   and exists (select sub1loc.MEMBER_ID
                 from PURCHASE sub1loc
                where sub1loc.MEMBER_ID = dfloc.MEMBER_ID
                  and sub1loc.PURCHASE_PRICE >= 200
       )
 order by dfloc.BIRTHDATE desc
```

*OutsideSql
```sql
/*
 [Example for Simple Select]
 It uses CustomizeEntity and ParameterBean.
*/
-- #df:entity#

-- !df:pmb!
-- !!AutoDetect!!

select mb.MEMBER_ID
     , mb.MEMBER_NAME
     , mb.BIRTHDATE
     , stat.MEMBER_STATUS_NAME
  from MEMBER mb
    left outer join MEMBER_STATUS stat
      on mb.MEMBER_STATUS_CODE = stat.MEMBER_STATUS_CODE
 /*BEGIN*/
 where
   /*IF pmb.memberId != null*/
   mb.MEMBER_ID = /*pmb.memberId*/3
   /*END*/
   /*IF pmb.memberName != null*/
   and mb.MEMBER_NAME like /*pmb.memberName*/'S%' -- // keyword for prefix search
   /*END*/
   /*IF pmb.birthdate != null*/
   and mb.BIRTHDATE = /*pmb.birthdate*/'1966-09-15' -- // used as equal
   /*END*/
 /*END*/
 order by mb.BIRTHDATE desc, mb.MEMBER_ID asc
```
```java
SimpleMemberPmb pmb = new SimpleMemberPmb();
pmb.setMemberName_PrefixSearch("S");
List<SimpleMember> memberList = memberBhv.outsideSql().selectList(pmb);
```

# Quick Trial
you can clone the example project of DBFlute:  

1. git clone https://github.com/dbflute/dbflute-howto.git
2. prepare database by *ReplaceSchema at DBFlute client directory 'dbflute_maihamadb'
3. compile it by Java8, on e.g. Eclipse or IntelliJ or ... as Maven project
4. see HowToSelectTest.java  
you can implement condition-bean as trial, and can execute it by JUnit

*ReplaceSchema
```java
// call manage.sh at lastaflute-example-harbor/dbflute_maihamadb
// and select replace-schema in displayed menu
...:dbflute_maihamadb ...$ sh manage.sh
```
# Setup DBFlute (Install)
See for the detail:  
http://dbflute.seasar.org/ja/environment/setup/maven.html  
(sorry Japanese site now...English coming soon)

*for DBFlute Runtime
```xml
  <properties>
    <dbflute.version>1.1.1</dbflute.version>
    ...
  </properties>
  ...

  <dependencies>
    <dependency>
      <groupId>org.dbflute</groupId>
      <artifactId>dbflute-runtime</artifactId>
      <version>${dbflute.version}</version>
    </dependency>
    ...
  </dependencies>
```

*to download DBFlute Engine
```xml
  <plugin>
      <groupId>org.dbflute</groupId>
      <artifactId>dbflute-maven-plugin</artifactId>
      <version>1.1.0</version>
      <configuration>
          <clientProject>xxxdb</clientProject>
          <packageBase>com.xxx.dbflute</packageBase>
      </configuration>
  </plugin>
```

# Information
## License
Apache License 2.0

## Official site
http://dbflute.seasar.org/  
(sorry Japanese site now...English coming soon)

# Thanks, Framewoks
DBFlute forks Apache Torque and S2Dao and (heavily) extends it.  
And is influenced by Doma, ActiveRecord  
If the frameworks were not there, no DBFlute here.

I appreciate every framework.

# Thanks, Friends
DBFlute is used by:  
- Fess: https://github.com/codelibs/fess
- S2Robot: https://github.com/codelibs/s2robot
- and other many business projects, comming soon...
