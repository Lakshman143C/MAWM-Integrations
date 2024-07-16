# **ItemInbound**

**Build & Local Setup Instructions:**

***Prerequisites: Docker desktop should be installed.***
1. Clone the repo.
2. Refresh the maven dependencies.
3. run -> mvn clean install
4. Create Run configuration in Intellij pointing to 'com.floordecor.inbound.InboundApplication.java'
5. Add Env variable as 'spring.profiles.active=local' in run configuration
6. Docker commands to be executed:

   Mysql - docker run -d  --name mysql-container  -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=fndBatchJob -p 3306:3306 mysql:latest
   SFTP - docker run --name sftp-server -p 22:22 -d atmoz/sftp test:test123:::apps

7. After bringing this instance, we need to create the listening source folders in sftp.

   /apps/scope/mif-dropbox/test-oms-dropbox/item/drop

   /apps/scope/mif-dropbox/test-oms-dropbox/item/output

   /apps/scope/mif-dropbox/test-oms-dropbox/OMSKULocation/drop

   /apps/scope/mif-dropbox/test-oms-dropbox/OMSKULocation/output/store

   /apps/scope/mif-dropbox/test-oms-dropbox/OMSKULocation/output/vendor

   /apps/scope/mif-dropbox/test-oms-dropbox/ItemSubstitution/drop

   /apps/scope/mif-dropbox/test-oms-dropbox/ItemSubstitution/output


8. Start the server using configuration created on step 4.
