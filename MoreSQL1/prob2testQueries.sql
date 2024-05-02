--test   
     DROP VIEW IF EXISTS fullAmount;

SELECT p.promo,
          s.salesperson,
          s.amount
     FROM Promotions p
     LEFT OUTER JOIN sales s ON s.saledate BETWEEN p.startDate AND p.endDate;

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
     FROM sales s2,
          promoAndSales pas
    WHERE s2.saledate = pas.saledate
      AND s2.salesperson = pas.salesperson
 GROUP BY (pas.promo, s2.salesperson);



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
	  left outer join fullAmount FA on pas.salesperson = FA.salesperson
 GROUP BY (pas.promo, s2.salesperson)
