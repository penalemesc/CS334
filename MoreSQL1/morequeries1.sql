--Problem 1
DROP VIEW if EXISTS cellNumbers;

DROP VIEW if EXISTS homeNumbers;

   CREATE VIEW cellNumbers AS
   SELECT phonenumber AS cellNumber,
          P1.empid
     FROM Phones P1
    WHERE phonetype = 'cell';

   CREATE VIEW homeNumbers AS
   SELECT phonenumber AS homeNumber,
          empid
     FROM Phones
    WHERE phonetype = 'home';

   SELECT E.empid,
          E.firstName,
          E.lastName,
          HN.homeNumber,
          CN.cellNumber
     FROM Employees E,
          homeNumbers HN
     FULL OUTER JOIN cellNumbers CN ON HN.empid = CN.empid
    WHERE E.empid = Hn.empid
       OR E.empid = CN.empid
 ORDER BY E.empid ASC;

--Problem 2 --Revised
DROP VIEW if EXISTS fullAmount;

   CREATE VIEW fullAmount AS
   SELECT sum(s.amount) AS quantity,
          s.salesperson
     FROM Sales s,
          Promotions p
    WHERE s.saledate BETWEEN p.startDate AND p.endDate
 GROUP BY (salesperson);

     WITH promoAndSales AS (
             SELECT p.promo,
                    s.salesperson,
                    s.amount,
                    s.saledate
               FROM Promotions p
               LEFT OUTER JOIN sales s ON s.saledate BETWEEN p.startDate AND p.endDate
          )
   SELECT pas.promo,
          s2.salesperson,
          sum(s2.amount)
     FROM promoAndSales pas
     LEFT OUTER JOIN sales s2 ON s2.saledate = pas.saledate
      AND s2.salesperson = pas.salesperson
     LEFT OUTER JOIN fullAmount FA ON pas.salesperson = FA.salesperson
 GROUP BY (pas.promo, s2.salesperson)
