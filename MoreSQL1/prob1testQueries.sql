-- Problem 1 test queries
--     Drop table if exists phonenumAndType
--     Create table phonenumAndType (
--           type char(4) not null,
--           number char(10) not null,
--     )
    
--      WITH all_phones (phonetype, phonenumber) AS (
--                SELECT phonetype,
--                     phonenumber
--                FROM phones
--                union All
--              SELECT AP.phonetype,
--                     AP.phonenumber
--                FROM all_phones AP,
--                     phones p
--               WHERE AP.phonetype = p.phonetype
--           )
--     select E.empid, firstName, lastName, AP.phonenumber from Employees E, Phones P, all_phones AP where E.empid = P.empid;

--Select E.empid, E.firstName, E.lastName, P.phonenumber from Employees E join Phones P using (empid)
-- with all_phones as (
--      select phonetype
--      from Phones 
--      union
--      select phonenumber
--      from Phones
-- )
--this returns what I want kinda, there's an extra 
  --  SELECT p1.phonenumber as homeNumber, p2.phonenumber as cellNumber
  --    FROM Phones P1
  --    left outer join Phones P2 on P1.empid = P2.empid and P1.phonetype != P2.phonetype

     --This returns all home numbers
--     SELECT P1.phonenumber AS homeNumber
--      FROM Phones P1
--     WHERE phonetype = 'home';
-- --this returns all cellnumbers
--    SELECT P2.phonenumber AS cellNumber
--      FROM Phones P2
--     WHERE phonetype = 'cell';

--THis works to return all numbers
drop view if exists cellNumbers;
drop view if exists homeNumbers;

   CREATE VIEW cellNumbers AS
   SELECT phonenumber AS cellNumber, P1.empid
     FROM Phones P1
    WHERE phonetype = 'cell';

    Create view homeNumbers AS
    SELECT phonenumber AS homeNumber, empid
     FROM Phones 
    WHERE phonetype = 'home';
    select HN.homeNumber, CN.cellNumber from homeNumbers HN full outer join cellNumbers CN on HN.empid = CN.empid;

--Final query works 
--Empid is changed in reference to whether it has only a cell number or not, other than that it works
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

    