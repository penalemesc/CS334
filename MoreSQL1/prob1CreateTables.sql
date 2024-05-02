     DROP TABLE Employees;

     DROP TABLE Phones;

   CREATE TABLE Employees (
          empid integer PRIMARY KEY,
          firstName varchar(20) NOT NULL,
          lastName varchar(20) NOT NULL
          );

   CREATE TABLE Phones (
          empid integer NOT NULL REFERENCES Employees,
          phonetype char(4) NOT NULL CHECK (phonetype IN ('home', 'cell')),
          phonenumber char(10) NOT NULL,
          PRIMARY KEY (empid, phonetype)
          );

   INSERT INTO Employees
   VALUES (1, 'te', 'st');

   INSERT INTO Employees
   VALUES (2, 'michael', 'jackson');

   INSERT INTO Employees
   VALUES (3, 'the', 'shakers');

   INSERT INTO Employees
   VALUES (4, 'tony', 'stark');
   
   INSERT INTO Phones
   VALUES (1, 'home', 1234);

   INSERT INTO Phones
   VALUES (1, 'cell', 5678);

   INSERT INTO Phones
   VALUES (2, 'home', 2345);

   INSERT INTO Phones
   VALUES (3, 'cell', 3456);

   INSERT INTO Phones
   VALUES (4, 'cell', 4567);

   INSERT INTO Phones
   VALUES (4, 'home', 5678);

