
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
o ant-1.7.0            // not latest version because of many ant-call's PermGen
o commons-compress-1.0 // not latest version because of nest library
