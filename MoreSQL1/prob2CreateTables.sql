     DROP TABLE Promotions;

     DROP TABLE Sales;

   CREATE TABLE Promotions (
          promo varchar(35) NOT NULL PRIMARY KEY,
          startDate date NOT NULL,
          endDate date NOT NULL
          );

   CREATE TABLE Sales (
          salesPerson varchar(25) NOT NULL,
          saleDate date NOT NULL,
          amount numeric NOT NULL
          );


\copy Promotions from promotions.data
\copy Sales from sales.data