Basic Working Example of [Requirements](https://docs.google.com/document/d/1b4Nj7ERF4ymwSXmoch3GoCeppcenE2Jv7dzUFZ6s4B8/edit?tab=t.0)

# Pre-requisites
1) mysql server running
1) password for server is set in applicaiton.yml named *my-secret-pw*
   1) Please change to mysql server running on your local env
   1) Please ensure if changing username/password combi, that the user has permissions to create tables and databases
1) **Optional**: run */docs/docker-compose.yml* to avoid tweaking mysql on your local system

# To run App
~~~bash
mvn clean /Users/dave/Documents/SpringBoot/Projects/BookShop/docs/docker-compose.ymlspring-boot:run
~~~

@see on browser http://localhost:8090/v1/web/home


## Brief overview
2 pre-configured users
1) username: *admin* password: *admin* to login for the ablity to add/edit/delete books
1) username: *jane.doe* password: *jane.doe* for the ability to add items to a customers cart

database is preloaded with some books. **NB** When restarting the server, if books have been deleted they will get re added @see *src/main/resources/data.sql* script (as is POC)

## Design
@see /docs folder for basic analysis of requirements
Flow is implemented through thymeleaf, basic api functionality http://localhost:8090/swagger-ui/index.html
