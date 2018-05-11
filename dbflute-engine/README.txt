
# ========================================================================================
#                                                                                 Overview
#                                                                                 ========
The project for DBFlute as generator, called 'DBFlute Engine',
which finally creates 'DBFlute Module' users can download.


# ========================================================================================
#                                                                              Environment
#                                                                              ===========
[DBFlute Project]
 |-deploy-script      // scripts to deploy DBFlute
 |-dist               // temporary for distributions
 |-embedded           // resources embedded in DBFlute module
 |-lib                // libraries DBFlute engine refers (needs to compile)
 |-src                // sources of DBFlute engine
 |-target             // compiled classes of DBFlute engine
 |-build.xml          // ANT setting file for DBFlute (Java)
 |-buildnet.xml       // ANT setting file for DBFlute (.NET)
 |-LICENSE            // LICENSE file for this project
 |-NOTICE             // NOTICE file for this project
 |-README.txt         // README file for this project

[Dependencies]
o ant-1.7.0                // not latest version because of many ant-call's PermGen
o commons-compress-1.0     // for ZIP archiver
o dbflute-runtime
   |-slf4j-api-1.7.12

o slf4j-log4j12-1.7.12.jar // for logging
   |-log4j-1.2.17.jar

o poi-3.12                 // for data XLS
   |-commons-codec-1.9     // actually not used here

o velocity-1.7             // for templates
   |-commons-collections-3.2.1
   |-commons-lang-2.4      // actually 2.5 used here

o xercesImpl-2.8.1         // for SchemaXML
