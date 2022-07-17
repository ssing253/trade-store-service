# trade-store-service
Prototype Trade Store Service

It is sample application which take care  of below requirements :
There are couples of validation, we need to provide in the above assignment
1. During transmission if the lower version is being received by the store it will reject the trade and
throw an exception. If the version is same it will override the existing record.
2. Store should not allow the trade which has less maturity date then today date.
3. Store should automatically update the expire flag if in a store the trade crosses the maturity

Main Components:
-----------------
**Code**
TradeController.java
TradeSericeImpl.java
TradeDao.java
TradeExpireScheduler.java

**Junits:**
TradeController.java
TradeSericeTest.java
TradeDaoTest.java

How To Run:
-----------
- To use this application downloan application code using https://github.com/ssing253/trade-store-service.git
(Or alternatiively can import using provided git link & configure/build as maven project)

- Prerequisite: Required Java8 & Maven 3 (atlest for both)   
- Once code is download extract to your local machine
- Under root directory i.e.trade-store-service open/edit setEnv.bat to set below veriables 
           (a) set JAVA_HOME=<>   // give path to  your JDK directory i.e. C:\Program Files\Java\jdk1.8.0_191
           (b) set MVN_HOME=<>    // give path to your MAVEN directory i.e. C:\Program Files\apache-maven-3.6.3
  
- Run build.bat   // it will start downloading maven dependency & build project using using Junits
- Run setupEclipse.bat // it will configure project as Eclipse project (or you can edit if uisng different IDE). Then you can import into eclipse IDE   
- Run run_dev.bat   // if your project is build & run successfully you will see below screen

![image](https://user-images.githubusercontent.com/100430458/179417096-dbcce126-dc3d-4db4-829c-870d1d7e7b8c.png)

- Try opening below url in browser http://localhost:8442/trade  & see if below response is coming

![image](https://user-images.githubusercontent.com/100430458/179417142-d62158e0-6205-4527-891b-c4658b45f93f.png)



 
