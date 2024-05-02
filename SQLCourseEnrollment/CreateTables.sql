  drop table student cascade;
  drop table dept cascade;
  drop table prof cascade;
  drop table course cascade;
  drop table major cascade;
  drop table section cascade;
  drop table enroll cascade;

   CREATE TABLE student (
          sid int PRIMARY KEY,
          sname varchar(50),
          sex char,
          age int,
          YEAR int,
          gpa float
          );

   CREATE TABLE dept (dname varchar(50) PRIMARY KEY, numphds int);

   CREATE TABLE prof (
          pname varchar(50) PRIMARY KEY,
          dname varchar(50) REFERENCES dept (dname)
          );

   CREATE TABLE course (
          cno int,
          cname varchar(50),
          dname varchar(50)  REFERENCES dept (dname)
          primary key (cno, dname)
          );

   CREATE TABLE major (
          dname varchar(50) PRIMARY KEY REFERENCES dept (dname),
          sid int  REFERENCES student (sid)
          );

   CREATE TABLE section (
          dname varchar(50) REFERENCES dept (dname),
          cno int REFERENCES course (cno),
          sectno float primary key,
          pname varchar(50)
          );

   CREATE TABLE enroll (
          sid int PRIMARY KEY REFERENCES student (sid),
          grade float,
          dname varchar(50) REFERENCES dept (dname),
          cno int REFERENCES course (cno),
          sectno float REFERENCES section (sectno)
          );