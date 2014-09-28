
# ========================================================================================
#                                                                                 Overview
#                                                                                 ========
The module for DBFlute generator, called 'DBFlute Module',
which generates classes for your DBFlute programming.


# ========================================================================================
#                                                                              Environment
#                                                                              ===========
[DBFlute Module]
 |-ant                // Apache Ant 1.7.0
 |-etc                // various resources
 |  |-client-template // template files of DBFlute client
 |  |-cmd             // scripts for DBFlute generator
 |  |-license         // license files for dependencies
 |  |-logprop         // settings for DBFlute generator's logging
 |-lib                // libraries DBFlute generator depends on
 |-templates          // velocity templates to generate classes
 |  |-doc                   // templates for documents 
 |  |  |-html               // templates for HTML documents
 |  |  |  |-datamodel.vm    // template file for SchemaHTML
 |  |  |  |-diffmodel.vm    // template file for HistoryHTML or other difference 
 |  |  |  |-table.vm        // template file for tables of SchemaHTML 
 |  |  |-ControlDocument.vm // velocity control file for documents
 |  |-om                    // templates for classes (object models)
 |  |  |-csharp             // templates for C# classes
 |  |  |-java               // templates for Java classes
 |  |  |-ControlGenerateCSharp.vm    // velocity control file for generate task of C#
 |  |  |-ControlGenerateJava.vm      // velocity control file for sql2entity task of Java
 |  |  |-ControlSql2EntityCSharp.vm  // velocity control file for generate task of C#
 |  |  |-ControlSql2EntityJava.vm    // velocity control file for sql2entity task of Java
 |-build-torque.xml              // setting for ant task (DBFlute task)
 |-product-is-dbflute-[version]  // mark file for the DBFlute version
 |-LICENSE                       // LICENSE file for this project
 |-NOTICE                        // NOTICE file for this project
 |-README.txt                    // README file for this project

Basically you don't need to modify them.
