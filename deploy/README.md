SmartSpend production frontend: https://smartspendtracking.netlify.app
Backend: EC2 98.94.12.136, Amazon Linux, Java 22, local MariaDB.

Run `./mvnw clean package` to build and test. Tests use an isolated H2 database
and mocked email delivery; they do not change production accounts.
Then run `python3 deploy/prepare-ec2-jar.py` to produce the EC2 release JAR
without bundled local database or mail secrets.

The production backend runs as `smartspend.service`. Settings, including email
credentials and `app.frontend-url`, live in `/etc/smartspend/application.properties`
on EC2. Preserve this file when updating the JAR.

Copy `target/Smartspend-backend-0.0.1-SNAPSHOT-ec2.jar` to EC2 as
`/home/ec2-user/Smartspend-backend-0.0.1-SNAPSHOT.jar`, retain a backup of the previous JAR, then run
`sudo systemctl restart smartspend`. Check `sudo systemctl status smartspend`
and `sudo journalctl -u smartspend -n 80` if startup fails.

Frontend updates must include all files from `src/main/resources/static`,
`login.html` copied to `index.html`, and the existing `_redirects` file.
Run `python3 deploy/prepare-frontend.py` to prepare this folder.
Publish `netlify-frontend` to Netlify project ID
`76d288fa-3352-4b5e-b678-29dc988f11af`; keep using that existing project.

Recovery links expire after 30 minutes. Verification links expire after 24 hours.
Links are single use; issuing a new email link replaces the previous active link.
Existing accounts can continue to log in without mandatory verification.
