implicit 'join' - не рекомендуется:

  - https://riptutorial.com/sql/example/938/implicit-join

    All RDBMSs support it, but the syntax is usually advised against. The reasons why it is a bad idea to use this syntax are: It is possible to get accidental cross joins which then return incorrect results, especially if you have a lot of joins in the query.  If you intended a cross join, then it is not clear from the syntax (write out CROSS JOIN instead), and someone is likely to change it during maintenance.

  - https://en.wikipedia.org/wiki/Join_(SQL)
    
    - the "implicit join notation" is no longer considered a best practice, although database systems
  still support it. 

    - Example of an implicit cross join: (...) In the SQL:2011 standard, cross joins are part of the optional F401, "Extended joined table", package. - соблюдение необязательно.

    - Normal uses are for checking the server's performance. - в нормальной разработке ПО не применяется.

This qualification test is made due to requirements of: `SQL-parser.md`

Built with jdk-15:
~~~
mvn clean package
~~~
Run it with:
~~~
java -jar target/parse-sql-00-1.0-SNAPSHOT.jar
~~~

The SQL and JSON-serialized objects are printed to standard output.

Every application class is located for your review at:
~~~
src/main/java/org/vereshagin/jmisc/ParseSql.java
~~~
