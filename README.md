Built with jdk-15:
```
mvn clean package
```
Run it with:
```
java -jar target/parse-sql-00-1.0-SNAPSHOT.jar
```

The SQL and JSON-serialized objects are printed to standard output.

Every application class is located for your review at:
```
src/main/java/org/vereshagin/jmisc/ParseSql.java
```

Features considered unnecessary:

- tests;

- `HAVING` from task's SQL.
