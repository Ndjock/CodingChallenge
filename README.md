# CodingChallenge
Open-wt coding challenge

###  (SpringBoot) MainClass:

*/openwt/interview/coding/challenge/CodingChallengeApplication.java*

### Buidling and running:

mvn package -Dmaven.test.skip=true (skipping the testing part)
 
java -jar target\CodingChallenge-0.0.1-SNAPSHOT.jar

### Access Localhost

curl -v -u dmorde0@patch.com:dmorde0@patch.com http://localhost:8080/contacts/

### Authentification: 
Authentification-type: Basic authentication http

username/password: *contact_email*/*contact_email*

whereas *contact_email* is the email column of the table *contacts*. The data about the table *contacts* is found on the liquibase changelog
*/src/main/resources/db/changelogs/dml/dml-insert-contacts-skills.sql*

some emails are: 
* dmorde0@patch.com
* pabramovitch1@cnn.com
* jlabrenz2@berkeley.edu
* nearney3@bing.com

others can be found using SQL: select email from contacts;

### DML and DDL:

Managed by Liquibase (see files in folder */src/main/resources/db/changelogs*)

### Use cases made

* _UC1_ 
* _UC2_
* _UC3_
* _UC4_ (only part 1 -> Contact Authentication)

