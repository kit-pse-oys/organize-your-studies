\# Database (bwCloud) – Organize Your Studies



We use a PostgreSQL database running as a Docker container on our bwCloud VM.



Setup summary

\- PostgreSQL runs in Docker as container: oys-db

\- Data is persisted via a Docker volume (survives restarts)

\- The schema is imported from schema.sql



Prerequisites (on the VM)

Check that Docker is available:

docker --version

docker compose version



Start the database (on the VM)

cd ~/oys-db

docker compose up -d

docker ps



Import the schema (first setup / after reset)

Make sure schema.sql is located at ~/oys-db/schema.sql, then run:

cd ~/oys-db

docker exec -i oys-db psql -U oys -d oys < schema.sql



Verify

1\) Logs should contain “ready to accept connections”:

docker logs oys-db --tail 30



2\) Simple query:

docker exec -it oys-db psql -U oys -d oys -c "SELECT 1;"



3\) List tables:

docker exec -it oys-db psql -U oys -d oys -c "\\dt"



Stop the database

cd ~/oys-db

docker compose down



Connection details (for Spring running on the same VM)

Host: 127.0.0.1

Port: 5432

Database: oys

Username: oys

Password: oys123



Spring (example settings)

spring.datasource.url=jdbc:postgresql://127.0.0.1:5432/oys

spring.datasource.username=oys

spring.datasource.password=oys123

spring.jpa.hibernate.ddl-auto=validate



Optional: Connect from laptop via SSH tunnel

Keep this SSH session open:

ssh -L 5432:127.0.0.1:5432 ubuntu@193.196.36.40



Then connect your DB tool locally to 127.0.0.1:5432 with the same DB/user/password.



